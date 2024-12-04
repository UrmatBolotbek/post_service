package faang.school.postservice.controller;

import faang.school.postservice.controller.like.LikeController;
import faang.school.postservice.dto.user.UserDto;
import faang.school.postservice.service.like.LikeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.when;

import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LikeControllerTest {

    @InjectMocks
    private LikeController likeController;

    @Mock
    private LikeService likeService;

    @BeforeEach
    void setUp() {
        likeController = new LikeController(likeService);
    }

    @Test
    public void testGetUsersByCommentLikes() {

        long commentId = 1L;

        List<UserDto> mockUsers = List.of(
                new UserDto(1L, "John Doe", "john@example.com", "123456789", 0),
                new UserDto(2L, "Jane Doe", "jane@example.com", "987654321", 0)
        );

        when(likeService.getUsersByCommentId(commentId)).thenReturn(mockUsers);

        // Act
        List<UserDto> result = likeController.getUsersByCommentLikes(commentId);

        assertEquals(2, result.size());
        assertEquals("John Doe", result.get(0).getUsername());
        assertEquals("Jane Doe", result.get(1).getUsername());
        verify(likeService, times(1)).getUsersByCommentId(commentId);
    }

    @Test
    public void testGetUsersByCommentLikesEmpty() {
        // Arrange
        long commentId = 1L;

        when(likeService.getUsersByCommentId(commentId)).thenReturn(List.of());

        // Act
        List<UserDto> result = likeController.getUsersByCommentLikes(commentId);

        // Assert
        assertEquals(0, result.size());
        verify(likeService, times(1)).getUsersByCommentId(commentId);
    }

    @Test
    public void testGetUsersByPostLikes() {
        long postId = 1L;

        List<UserDto> mockUsers = List.of(
                new UserDto(1L, "John Doe", "john@example.com", "123456789", 0),
                new UserDto(2L, "Jane Doe", "jane@example.com", "987654321", 0)
        );

        when(likeService.getUsersByPostId(postId)).thenReturn(mockUsers);

        List<UserDto> result = likeController.getUsersByPostLikes(postId);

        assertEquals(2, result.size());
        assertEquals("John Doe", result.get(0).getUsername());
        assertEquals("Jane Doe", result.get(1).getUsername());

        verify(likeService, times(1)).getUsersByPostId(postId);
    }

    @Test
    public void testGetUsersByPostLikesEmpty() {
        long postId = 1L;

        when(likeService.getUsersByPostId(postId)).thenReturn(List.of());

        List<UserDto> result = likeController.getUsersByPostLikes(postId);

        assertEquals(0, result.size());

        verify(likeService, times(1)).getUsersByPostId(postId);
    }
}


