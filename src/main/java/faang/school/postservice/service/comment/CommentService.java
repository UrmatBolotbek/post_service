package faang.school.postservice.service.comment;

import faang.school.postservice.annotations.publisher.PublishEvent;
import faang.school.postservice.client.UserServiceClient;
import faang.school.postservice.config.context.UserContext;
import faang.school.postservice.dto.comment.CommentRequestDto;
import faang.school.postservice.dto.comment.CommentResponseDto;
import faang.school.postservice.dto.comment.CommentUpdateRequestDto;
import faang.school.postservice.dto.events_dto.CommentEventDto;
import faang.school.postservice.dto.user.UserDto;
import faang.school.postservice.dto.user.UserForBanEventDto;
import faang.school.postservice.mapper.comment.CommentMapper;
import faang.school.postservice.model.Comment;
import faang.school.postservice.news_feed.dto.serializable.CommentCache;
import faang.school.postservice.news_feed.repository.CommentCacheRepository;
import faang.school.postservice.news_feed.repository.UserCacheRepository;
import faang.school.postservice.publisher.CommentEventPublisher;
import faang.school.postservice.publisher.UserBanEventPublisher;
import faang.school.postservice.repository.CommentRepository;
import faang.school.postservice.repository.PostRepository;
import faang.school.postservice.util.ModerationDictionary;
import faang.school.postservice.validator.comment.CommentValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static faang.school.postservice.news_feed.enums.PublisherType.POST_COMMENT;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentValidator commentValidator;
    private final CommentMapper commentMapper;
    private final CommentEventPublisher commentEventPublisher;
    private final UserBanEventPublisher banPublisher;
    private final PostRepository postRepository;
    private final ModerationDictionary moderationDictionary;
    private final UserServiceClient userServiceClient;
    private final UserCacheRepository userCacheRepository;
    private final CommentCacheRepository commentCacheRepository;
    private final UserContext userContext;

    @PublishEvent(type = POST_COMMENT)
    @Transactional
    public CommentResponseDto createComment(CommentRequestDto commentRequestDto) {
        commentValidator.validateAuthorExists(commentRequestDto.getAuthorId());
        commentValidator.validatePostExists(commentRequestDto.getPostId());

        Comment comment = commentMapper.toEntity(commentRequestDto);
        comment.setLikes(new ArrayList<>());

        if (!moderationDictionary.isVerified(comment.getContent())) {
            comment.setVerified(false);
        }

        Comment savedComment = commentRepository.save(comment);
        CommentResponseDto commentResponseDto = commentMapper.toDto(savedComment);
        log.info("New comment with id: {} created", comment.getId());

        CommentEventDto commentEventDto = createCommentEventDto(commentResponseDto);
        commentEventPublisher.publish(commentEventDto);
        log.info("Notification about new comment sent to notification service {}", commentEventDto);

        UserDto userDto = userServiceClient.getUser(userContext.getUserId());
        userCacheRepository.save(userDto);
        CommentCache commentCache = commentMapper.toCommentCache(comment);
        commentCacheRepository.save(commentCache);

        return commentResponseDto;
    }

    private CommentEventDto createCommentEventDto(CommentResponseDto commentResponseDto) {
        CommentEventDto commentEventDto = new CommentEventDto();
        commentEventDto.setPostAuthorId(postRepository.getPostById(commentResponseDto.getPostId()).getAuthorId());
        commentEventDto.setCommentAuthorId(commentResponseDto.getAuthorId());
        commentEventDto.setPostId(commentResponseDto.getPostId());
        commentEventDto.setCommentId(commentResponseDto.getId());
        commentEventDto.setCommentContent(commentResponseDto.getContent());
        commentEventDto.setCommentedAt(LocalDateTime.now());
        return commentEventDto;
    }

    @Transactional
    public CommentResponseDto updateComment(CommentUpdateRequestDto commentUpdateRequestDto) {
        Comment commentToUpdate = commentRepository.getCommentById(commentUpdateRequestDto.getCommentId());

        String postContent = commentUpdateRequestDto.getContent();
        commentToUpdate.setContent(postContent);

        commentRepository.save(commentToUpdate);
        log.info("Comment with id: {} updated", commentUpdateRequestDto.getCommentId());

        CommentCache commentCache = commentMapper.toCommentCache(commentToUpdate);
        commentCacheRepository.save(commentCache);

        return commentMapper.toDto(commentToUpdate);
    }

    public List<CommentResponseDto> getCommentsByPostId(Long postId) {
        commentValidator.validatePostExists(postId);

        List<Comment> commentsByPostId = commentRepository.findAllByPostId(postId);
        commentsByPostId.sort(Comparator.comparing(Comment::getCreatedAt).reversed());

        log.info("Retrieved all the comments for the post with id: {}", postId);
        return commentMapper.toDto(commentsByPostId);
    }

    public void deleteComment(Long commentId) {
        commentRepository.deleteById(commentId);
        commentCacheRepository.deleteById(commentId);
        log.info("Comment with id: {} deleted", commentId);
    }

    @Async("moderationPool")
    @Transactional
    public void commenterBanner() {
        commentRepository.getAuthorIdsForBanFromComments().forEach(authorId -> {
            UserForBanEventDto eventDto = new UserForBanEventDto();
            eventDto.setId(authorId);
            banPublisher.publish(eventDto);
            List<Comment> commentsFromUser = commentRepository.findAllByAuthorId(authorId);
            commentsFromUser.forEach(comment -> comment.setVision(false));
            log.info("Author with authorId {} is banned", authorId);
        });
    }

}