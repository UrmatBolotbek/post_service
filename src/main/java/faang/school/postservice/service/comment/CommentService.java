package faang.school.postservice.service.comment;

import faang.school.postservice.dto.comment.CommentRequestDto;
import faang.school.postservice.dto.comment.CommentResponseDto;
import faang.school.postservice.dto.comment.CommentUpdateRequestDto;
import faang.school.postservice.mapper.comment.CommentMapper;
import faang.school.postservice.model.Comment;
import faang.school.postservice.repository.CommentRepository;
import faang.school.postservice.validator.comment.CommentValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {
    private final CommentRepository commentRepository;
    private final CommentValidator commentValidator;
    private final CommentMapper commentMapper;

    public CommentResponseDto createComment(CommentRequestDto commentRequestDto) {
        commentValidator.validateAuthorExists(commentRequestDto.getAuthorId());
        commentValidator.validatePostExists(commentRequestDto.getPostId());

        Comment comment = commentMapper.toEntity(commentRequestDto);
        comment.setLikes(new ArrayList<>());

        Comment savedComment = commentRepository.save(comment);
        log.info("New comment with id: {} created", comment.getId());
        return commentMapper.toDto(savedComment);
    }

    public CommentResponseDto updateComment(CommentUpdateRequestDto commentUpdateRequestDto) {
        Comment commentToUpdate = commentRepository.getCommentById(commentUpdateRequestDto.getCommentId());

        String postContent = commentUpdateRequestDto.getContent();
        commentToUpdate.setContent(postContent);

        commentRepository.save(commentToUpdate);
        log.info("Comment with id: {} updated", commentUpdateRequestDto.getCommentId());
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
        log.info("Comment with id: {} deleted", commentId);
    }
}