package faang.school.postservice.validator.post;

import faang.school.postservice.client.ProjectServiceClient;
import faang.school.postservice.client.UserServiceClient;
import faang.school.postservice.dto.post.PostRequestDto;

import java.util.Optional;
import faang.school.postservice.excaption.post.PostException;
import faang.school.postservice.model.Post;
import faang.school.postservice.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PostValidateTest {

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private ProjectServiceClient projectServiceClient;
    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private PostValidator postValidator;


    @BeforeEach
    public void setUp() {
        postValidator = new PostValidator(userServiceClient, projectServiceClient, postRepository); // ensure this matches your actual constructor/setup
    }

    @Test
    public void shouldReturnPostWhenFoundById() {

        Long postId = 1L;
        Post existingPost = new Post();
        existingPost.setId(postId);
        existingPost.setContent("Sample content");
        when(postRepository.getPostById(postId)).thenReturn(existingPost);

        Post result = postValidator.validateAndGetPostById(postId);

        assertNotNull(result);
        assertEquals(postId, result.getId());
        assertEquals("Sample content", result.getContent());
        verify(postRepository).getPostById(postId);
    }

    @Test
    void shouldValidateUserExist() {

        Long userId = 1L;

        postValidator.validateUserExist(userId);

        verify(userServiceClient).getUser(userId);
    }

    @Test
    void shouldValidateProjectExist() {

        Long projectId = 1L;

        postValidator.validateProjectExist(projectId);

        verify(projectServiceClient).getProject(projectId);
    }

    @Test
    void shouldValidateCreateWithAuthorId() {

        PostRequestDto postDto = new PostRequestDto();

        postDto.setAuthorId(1L);

        postValidator.validateCreate(postDto);

        verify(userServiceClient).getUser(postDto.getAuthorId());
        verifyNoInteractions(projectServiceClient);
    }

    @Test
    void shouldValidateCreateWithProjectId() {

        PostRequestDto postDto = new PostRequestDto();
        postDto.setProjectId(1L);

        postValidator.validateCreate(postDto);

        verify(projectServiceClient).getProject(postDto.getProjectId());
        verifyNoInteractions(userServiceClient);
    }


    @Test
    void shouldValidatePublish() {
        Post post = new Post();
        post.setPublished(false);

        postValidator.validatePublish(post);
    }

    @Test
    void shouldThrowWhenPostAlreadyPublished() {
        Post post = new Post();
        post.setPublished(true);

        PostException exception = assertThrows(PostException.class, () -> {
            postValidator.validatePublish(post);
        });

        assertEquals("Post is already published", exception.getMessage());
    }

    @Test
    void shouldValidateDelete() {
        Post post = new Post();
        post.setDeleted(false);

        postValidator.validateDelete(post);
    }

    @Test
    void shouldThrowWhenPostAlreadyDeleted() {
        Post post = new Post();
        post.setDeleted(true);

        PostException exception = assertThrows(PostException.class, () -> {
            postValidator.validateDelete(post);
        });

        assertEquals("Post already deleted", exception.getMessage());
    }
}
