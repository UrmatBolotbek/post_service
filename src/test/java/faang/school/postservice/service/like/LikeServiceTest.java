package faang.school.postservice.service.like;

import faang.school.postservice.client.UserServiceClient;
import faang.school.postservice.dto.like.LikeRequestDto;
import faang.school.postservice.dto.like.LikeResponseDto;
import faang.school.postservice.dto.user.UserDto;
import faang.school.postservice.exception.DataValidationException;
import faang.school.postservice.mapper.like.LikeMapperImpl;
import faang.school.postservice.model.Comment;
import faang.school.postservice.model.Like;
import faang.school.postservice.model.Post;
import faang.school.postservice.repository.CommentRepository;
import faang.school.postservice.repository.LikeRepository;
import faang.school.postservice.repository.PostRepository;
import faang.school.postservice.validator.comment.CommentValidator;
import faang.school.postservice.validator.like.LikeValidator;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LikeServiceTest {

    @Captor
    private ArgumentCaptor<Like> likeCaptor;

    @InjectMocks
    private LikeService likeService;

    @Mock
    private LikeRepository likeRepository;
    @Mock
    private PostRepository postRepository;
    @Mock
    private CommentRepository commentRepository;
    @Spy
    private LikeMapperImpl likeMapper;
    @Mock
    private LikeValidator validator;
    @Mock
    private UserServiceClient userServiceClient;
    @Mock
    private CommentValidator commentValidator;

    private static final int BATCH_SIZE = 100;

    private LikeRequestDto acceptanceLikeDto;
    private Post post;
    private Like like;
    private Comment comment;

    @BeforeEach
    public void setUp() {
        acceptanceLikeDto = LikeRequestDto.builder()
                .userId(1L)
                .build();
        comment = new Comment();
        comment.setId(10L);
        comment.setLikes(new ArrayList<>());
        post = new Post();
        post.setId(5L);
        post.setLikes(new ArrayList<>());
        like = new Like();
        like.setId(1L);
        like.setUserId(1L);
    }

    @Test
    public void testPostLikeSuccess() {
        when(postRepository.findById(5L)).thenReturn(Optional.of(post));
        when(validator.validatePostHasLike(5L, 1L)).thenReturn(true);
        LikeResponseDto response = likeService.postLike(acceptanceLikeDto, 5L);
        verify(likeRepository).save(likeCaptor.capture());
        verify(postRepository).save(post);
        Like capturedLike = likeCaptor.getValue();
        assertEquals(post, capturedLike.getPost());
        assertEquals(List.of(capturedLike), post.getLikes());
    }

    @Test
    public void testPostLikeFailure() {
        when(postRepository.findById(5L)).thenReturn(Optional.empty());
        assertThrows(DataValidationException.class, () -> likeService.postLike(acceptanceLikeDto, 5L));
    }

    @Test
    public void testPostLikeWithPostAlreadyLiked() {
        when(postRepository.findById(5L)).thenReturn(Optional.of(post));
        when(validator.validatePostHasLike(5L, 1L)).thenReturn(false);
        assertThrows(DataValidationException.class, () -> likeService.postLike(acceptanceLikeDto, 5L));
    }

    @Test
    public void testCommentLikeSuccess() {
        when(commentRepository.findById(10L)).thenReturn(Optional.of(comment));
        when(validator.validateCommentHasLike(10L, 1L)).thenReturn(true);
        LikeResponseDto response = likeService.commentLike(acceptanceLikeDto, 10L);
        verify(likeRepository).save(likeCaptor.capture());
        verify(commentRepository).save(comment);
        Like capturedLike = likeCaptor.getValue();
        assertEquals(comment, capturedLike.getComment());
        assertEquals(List.of(capturedLike), comment.getLikes());
    }

    @Test
    public void testCommentLikeWithCommentAlreadyLiked() {
        when(commentRepository.findById(10L)).thenReturn(Optional.of(comment));
        when(validator.validateCommentHasLike(10L, 1L)).thenReturn(false);
        assertThrows(DataValidationException.class, () -> likeService.commentLike(acceptanceLikeDto, 10L));
    }

    @Test
    public void testDeleteLikeFromPost() {
        post.getLikes().add(like);
        when(postRepository.findById(5L)).thenReturn(Optional.of(post));
        when(validator.validatePostHasLike(5L, 1L)).thenReturn(false);
        likeService.deleteLikeFromPost(acceptanceLikeDto, 5L);
        verify(likeRepository).deleteByPostIdAndUserId(post.getId(), 1L);
        assertTrue(post.getLikes().isEmpty());
    }

    @Test
    public void testDeleteLikeFromPostWithPostNotLiked() {
        when(postRepository.findById(5L)).thenReturn(Optional.of(post));
        when(validator.validatePostHasLike(5L, 1L)).thenReturn(true);
        assertThrows(DataValidationException.class, () -> likeService.deleteLikeFromPost(acceptanceLikeDto, 5L));
    }

    @Test
    public void testDeleteLikeFromComment() {
        comment.getLikes().add(like);
        when(commentRepository.findById(10L)).thenReturn(Optional.of(comment));
        when(validator.validateCommentHasLike(10L, 1L)).thenReturn(false);
        likeService.deleteLikeFromComment(acceptanceLikeDto, 10L);
        verify(likeRepository).deleteByCommentIdAndUserId(comment.getId(), 1L);
        assertTrue(comment.getLikes().isEmpty());
    }

    @Test
    public void testDeleteLikeFromCommentWithCommentNotLiked() {
        when(commentRepository.findById(10L)).thenReturn(Optional.of(comment));
        when(validator.validateCommentHasLike(10L, 1L)).thenReturn(true);
        assertThrows(DataValidationException.class, () -> likeService.deleteLikeFromComment(acceptanceLikeDto, 10L));
    }

    @Test
    void testGetUsersByPostId_Success() {
        Post p = new Post();
        p.setId(5L);
        List<Like> likes = List.of(
                Like.builder().userId(1L).post(p).build(),
                Like.builder().userId(2L).post(p).build()
        );
        List<Long> userIds = List.of(1L, 2L);
        List<UserDto> userDtos = List.of(
                new UserDto(1L, "User1", "user1@example.com", "Address1", 25),
                new UserDto(2L, "User2", "user2@example.com", "Address2", 30)
        );
        when(likeRepository.findByPostId(5L)).thenReturn(likes);
        when(userServiceClient.getUsersByIds(anyList())).thenReturn(userDtos);
        List<UserDto> result = likeService.getUsersByPostId(5L);
        assertEquals(2, result.size());
        assertTrue(result.contains(new UserDto(1L, "User1", "user1@example.com", "Address1", 25)));
        assertTrue(result.contains(new UserDto(2L, "User2", "user2@example.com", "Address2", 30)));
        verify(likeRepository).findByPostId(5L);
        verify(userServiceClient).getUsersByIds(userIds);
    }

    @Test
    void testGetUsersByPostId_UserServiceError() {
        Post p = new Post();
        p.setId(5L);
        List<Like> likes = List.of(
                Like.builder().userId(1L).post(p).build(),
                Like.builder().userId(2L).post(p).build()
        );
        List<Long> userIds = List.of(1L, 2L);
        when(likeRepository.findByPostId(5L)).thenReturn(likes);
        when(userServiceClient.getUsersByIds(anyList())).thenThrow(new RuntimeException("Service unavailable"));
        List<UserDto> result = likeService.getUsersByPostId(5L);
        assertTrue(result.isEmpty());
        verify(likeRepository).findByPostId(5L);
        verify(userServiceClient).getUsersByIds(userIds);
    }

    @Test
    void testGetUsersByCommentId_Success() {
        long commentId = 10L;
        List<Like> likes = List.of(
                Like.builder().userId(1L).comment(comment).build(),
                Like.builder().userId(2L).comment(comment).build()
        );
        List<Long> userIds = List.of(1L, 2L);
        List<UserDto> userDtos = List.of(
                new UserDto(1L, "User1", "user1@example.com", "Address1", 25),
                new UserDto(2L, "User2", "user2@example.com", "Address2", 30)
        );
        when(likeRepository.findByCommentId(commentId)).thenReturn(likes);
        when(userServiceClient.getUsersByIds(userIds)).thenReturn(userDtos);
        List<UserDto> result = likeService.getUsersByCommentId(commentId);
        assertEquals(2, result.size());
        assertTrue(result.contains(new UserDto(1L, "User1", "user1@example.com", "Address1", 25)));
        assertTrue(result.contains(new UserDto(2L, "User2", "user2@example.com", "Address2", 30)));
        verify(commentValidator).validateCommentExists(commentId);
        verify(likeRepository).findByCommentId(commentId);
        verify(userServiceClient).getUsersByIds(userIds);
    }

    @Test
    void testGetUsersByCommentId_CommentNotFound() {
        long commentId = 10L;
        doThrow(new EntityNotFoundException("Comment with id " + commentId + " does not exist."))
                .when(commentValidator).validateCommentExists(commentId);
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> likeService.getUsersByCommentId(commentId));
        assertEquals("Comment with id 10 does not exist.", exception.getMessage());
        verify(commentValidator).validateCommentExists(commentId);
        verifyNoInteractions(likeRepository);
        verifyNoInteractions(userServiceClient);
    }
}
