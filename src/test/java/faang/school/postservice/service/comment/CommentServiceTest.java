package faang.school.postservice.service.comment;

import faang.school.postservice.client.UserServiceClient;
import faang.school.postservice.dto.comment.CommentRequestDto;
import faang.school.postservice.dto.comment.CommentResponseDto;
import faang.school.postservice.dto.comment.CommentUpdateRequestDto;
import faang.school.postservice.dto.events_dto.CommentEventDto;
import faang.school.postservice.dto.user.UserDto;
import faang.school.postservice.dto.user.UserForBanEventDto;
import faang.school.postservice.mapper.comment.CommentMapper;
import faang.school.postservice.model.Comment;
import faang.school.postservice.model.Post;
import faang.school.postservice.news_feed.dto.serializable.CommentCache;
import faang.school.postservice.news_feed.repository.CommentCacheRepository;
import faang.school.postservice.news_feed.repository.UserCacheRepository;
import faang.school.postservice.publisher.CommentEventPublisher;
import faang.school.postservice.publisher.UserBanEventPublisher;
import faang.school.postservice.repository.CommentRepository;
import faang.school.postservice.repository.PostRepository;
import faang.school.postservice.util.ModerationDictionary;
import faang.school.postservice.validator.comment.CommentValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private CommentValidator commentValidator;
    @Mock
    private CommentMapper commentMapper;
    @Mock
    private PostRepository postRepository;
    @Mock
    private CommentEventPublisher commentEventPublisher;
    @Mock
    private ModerationDictionary moderationDictionary;
    @Mock
    private UserBanEventPublisher banPublisher;
    @Mock
    private UserCacheRepository userCacheRepository;
    @Mock
    private CommentCacheRepository commentCacheRepository;
    @Mock
    private UserServiceClient userServiceClient;
    @Mock
    private faang.school.postservice.config.context.UserContext userContext;
    @InjectMocks
    private CommentService commentService;

    private static final Long VALID_COMMENT_ID = 1L;
    private static final Long VALID_POST_ID = 22L;
    private static final Long VALID_POST_AUTHOR_ID = 33L;
    private static final String VALID_CONTENT = "Test Content";
    private static final String UPDATED_CONTENT = "Updated Content";
    private static final LocalDateTime CREATED_AT_FOR_OLDER_COMMENT = LocalDateTime.of(2023, 11, 10, 10, 0);
    private static final LocalDateTime CREATED_AT_FOR_NEWER_COMMENT = LocalDateTime.of(2024, 11, 11, 10, 0);
    private static final UserDto VALID_USER_DTO = new UserDto(1L, "John Doe", "johndoe@gmail.com", "1234567", 5);

    @Test
    void createComment_shouldCreateCommentSuccessfully() {
        Post post = new Post();
        post.setId(VALID_POST_ID);
        post.setAuthorId(VALID_POST_AUTHOR_ID);
        CommentRequestDto commentRequestDto = new CommentRequestDto();
        commentRequestDto.setPostId(VALID_POST_ID);
        commentRequestDto.setAuthorId(VALID_USER_DTO.getId());
        commentRequestDto.setContent(VALID_CONTENT);
        Comment comment = new Comment();
        comment.setId(VALID_COMMENT_ID);
        comment.setPost(post);
        comment.setLikes(new ArrayList<>());
        CommentResponseDto expectedResponse = new CommentResponseDto();
        expectedResponse.setId(VALID_COMMENT_ID);
        expectedResponse.setAuthorId(VALID_USER_DTO.getId());
        expectedResponse.setPostId(VALID_POST_ID);
        expectedResponse.setContent(VALID_CONTENT);
        when(commentMapper.toEntity(commentRequestDto)).thenReturn(comment);
        when(commentRepository.save(comment)).thenReturn(comment);
        when(commentMapper.toDto(comment)).thenReturn(expectedResponse);
        when(postRepository.getPostById(VALID_POST_ID)).thenReturn(post);
        when(moderationDictionary.isVerified(comment.getContent())).thenReturn(true);
        when(userContext.getUserId()).thenReturn(VALID_USER_DTO.getId());
        when(userServiceClient.getUser(VALID_USER_DTO.getId())).thenReturn(VALID_USER_DTO);
        CommentCache commentCache = new CommentCache();
        commentCache.setId(VALID_COMMENT_ID);
        when(commentMapper.toCommentCache(comment)).thenReturn(commentCache);
        CommentResponseDto actualResponse = commentService.createComment(commentRequestDto);
        verify(commentValidator).validateAuthorExists(commentRequestDto.getAuthorId());
        verify(commentValidator).validatePostExists(commentRequestDto.getPostId());
        verify(commentRepository).save(comment);
        verify(commentMapper).toEntity(commentRequestDto);
        verify(commentMapper).toDto(comment);
        verify(postRepository).getPostById(VALID_POST_ID);
        ArgumentCaptor<CommentEventDto> eventCaptor = ArgumentCaptor.forClass(CommentEventDto.class);
        verify(commentEventPublisher).publish(eventCaptor.capture());
        CommentEventDto actualEvent = eventCaptor.getValue();
        assertNotNull(actualEvent);
        assertEquals(VALID_POST_AUTHOR_ID, actualEvent.getPostAuthorId());
        assertEquals(VALID_USER_DTO.getId(), actualEvent.getCommentAuthorId());
        assertEquals(VALID_POST_ID, actualEvent.getPostId());
        assertEquals(VALID_COMMENT_ID, actualEvent.getCommentId());
        assertEquals(VALID_CONTENT, actualEvent.getCommentContent());
        verify(userCacheRepository).save(VALID_USER_DTO);
        verify(commentCacheRepository).save(commentCache);
        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    void updateComment_shouldUpdateCommentSuccessfully() {
        CommentUpdateRequestDto updateDto = new CommentUpdateRequestDto();
        updateDto.setCommentId(VALID_COMMENT_ID);
        updateDto.setContent(UPDATED_CONTENT);
        Comment existingComment = new Comment();
        existingComment.setId(VALID_COMMENT_ID);
        existingComment.setContent(VALID_CONTENT);
        CommentResponseDto expectedOutput = new CommentResponseDto();
        expectedOutput.setId(VALID_COMMENT_ID);
        expectedOutput.setContent(UPDATED_CONTENT);
        when(commentRepository.getCommentById(updateDto.getCommentId())).thenReturn(existingComment);
        when(commentMapper.toDto(existingComment)).thenReturn(expectedOutput);
        CommentResponseDto actualOutput = commentService.updateComment(updateDto);
        verify(commentRepository).save(existingComment);
        assertEquals(UPDATED_CONTENT, existingComment.getContent());
        verify(commentMapper).toCommentCache(existingComment);
        verify(commentCacheRepository).save(any());
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void getCommentsByPostId_shouldReturnCommentsInDescendingOrder() {
        Long postId = VALID_POST_ID;
        Comment comment1 = new Comment();
        comment1.setCreatedAt(CREATED_AT_FOR_OLDER_COMMENT);
        Comment comment2 = new Comment();
        comment2.setCreatedAt(CREATED_AT_FOR_NEWER_COMMENT);
        List<Comment> comments = new ArrayList<>();
        comments.add(comment1);
        comments.add(comment2);
        CommentResponseDto dto1 = new CommentResponseDto();
        CommentResponseDto dto2 = new CommentResponseDto();
        when(commentRepository.findAllByPostId(postId)).thenReturn(comments);
        when(commentMapper.toDto(comments)).thenReturn(List.of(dto2, dto1));
        List<CommentResponseDto> actualOutput = commentService.getCommentsByPostId(postId);
        verify(commentValidator).validatePostExists(postId);
        comments.sort(Comparator.comparing(Comment::getCreatedAt).reversed());
        assertEquals(List.of(dto2, dto1), actualOutput);
    }

    @Test
    void deleteComment_shouldDeleteCommentSuccessfully() {
        Long commentId = VALID_COMMENT_ID;
        commentService.deleteComment(commentId);
        verify(commentRepository).deleteById(commentId);
        verify(commentCacheRepository).deleteById(commentId);
        verifyNoMoreInteractions(commentRepository);
    }

    @Test
    void testCommenterBanner() {
        when(commentRepository.getAuthorIdsForBanFromComments()).thenReturn(List.of(24L));
        commentService.commenterBanner();
        ArgumentCaptor<UserForBanEventDto> eventCaptor = ArgumentCaptor.forClass(UserForBanEventDto.class);
        verify(banPublisher).publish(eventCaptor.capture());
        UserForBanEventDto actualEvent = eventCaptor.getValue();
        assertNotNull(actualEvent);
        assertEquals(24L, actualEvent.getId());
        verify(commentRepository).findAllByAuthorId(24L);
    }
}
