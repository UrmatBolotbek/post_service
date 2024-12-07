package faang.school.postservice.validator.comment;

import faang.school.postservice.client.UserServiceClient;
import faang.school.postservice.dto.comment.CommentRequestDto;
import faang.school.postservice.dto.comment.CommentUpdateRequestDto;
import faang.school.postservice.dto.user.UserDto;
import faang.school.postservice.exception.DataValidationException;
import faang.school.postservice.repository.CommentRepository;
import faang.school.postservice.repository.PostRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentValidatorTest {
    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private CommentValidator commentValidator;

    private static final String EMPTY_CONTENT = "";
    private static final String LONGER_THAN_4096_SYMBOLS_CONTENT = "a".repeat(4097);
    private static final String VALID_CONTENT = "some content";
    private static final Long VALID_COMMENT_ID = 1L;
    private static final Long VALID_AUTHOR_ID = 1L;
    private static final Long VALID_POST_ID = 1L;
    private static final UserDto VALID_USER_DTO =
            new UserDto(1L, "John Doe", "JohnDoe@gmail.com", "1234567", 5);

    @Test
    public void validateComment_shouldThrowException_whenContentIsNull() {
        CommentRequestDto commentRequestDto = new CommentRequestDto();
        commentRequestDto.setContent(null);

        DataValidationException exception = assertThrows(DataValidationException.class, () -> commentValidator.validateCommentDtoInput(commentRequestDto));

        assertEquals("Comment content is empty", exception.getMessage());
    }

    @Test
    public void validateComment_shouldThrowException_whenContentIsEmpty() {
        CommentRequestDto commentRequestDto = new CommentRequestDto();
        commentRequestDto.setContent(EMPTY_CONTENT);

        DataValidationException exception = assertThrows(DataValidationException.class, () -> {
            commentValidator.validateCommentDtoInput(commentRequestDto);
        });

        assertEquals("Comment content is empty", exception.getMessage());
    }

    @Test
    public void validateComment_shouldThrowException_whenContentIsTooLong() {
        CommentRequestDto commentRequestDto = new CommentRequestDto();
        commentRequestDto.setContent(LONGER_THAN_4096_SYMBOLS_CONTENT);

        DataValidationException exception = assertThrows(DataValidationException.class, () -> {
            commentValidator.validateCommentDtoInput(commentRequestDto);
        });

        assertEquals("Comment content is too long", exception.getMessage());
    }

    @Test
    public void validateComment_shouldThrowException_whenAuthorIdIsNull() {
        CommentRequestDto commentRequestDto = new CommentRequestDto();
        commentRequestDto.setContent(VALID_CONTENT);
        commentRequestDto.setAuthorId(null);

        DataValidationException exception = assertThrows(DataValidationException.class, () -> {
            commentValidator.validateCommentDtoInput(commentRequestDto);
        });

        assertEquals("Comment must have an author", exception.getMessage());
    }

    @Test
    public void validateComment_shouldThrowException_whenPostIdIsNull() {
        CommentRequestDto commentRequestDto = new CommentRequestDto();
        commentRequestDto.setAuthorId(VALID_AUTHOR_ID);
        commentRequestDto.setContent(VALID_CONTENT);
        commentRequestDto.setPostId(null);

        DataValidationException exception = assertThrows(DataValidationException.class, () -> {
            commentValidator.validateCommentDtoInput(commentRequestDto);
        });

        assertEquals("Comment must relate to a post", exception.getMessage());
    }

    @Test
    void validateCommentUpdateDto_shouldThrowException_whenContentIsNull() {
        CommentUpdateRequestDto updatingDto = new CommentUpdateRequestDto();
        updatingDto.setCommentId(VALID_COMMENT_ID);
        updatingDto.setContent(null);

        DataValidationException exception = assertThrows(DataValidationException.class, () -> {
            commentValidator.validateCommentUpdateDto(updatingDto);
        });

        assertEquals("Comment content is empty", exception.getMessage());
    }

    @Test
    void validateCommentUpdateDto_shouldThrowException_whenContentIsEmpty() {
        CommentUpdateRequestDto updatingDto = new CommentUpdateRequestDto();
        updatingDto.setCommentId(VALID_COMMENT_ID);
        updatingDto.setContent(EMPTY_CONTENT);

        DataValidationException exception = assertThrows(DataValidationException.class, () -> {
            commentValidator.validateCommentUpdateDto(updatingDto);
        });

        assertEquals("Comment content is empty", exception.getMessage());
    }

    @Test
    void validateCommentUpdateDto_shouldThrowException_whenContentIsTooLong() {
        CommentUpdateRequestDto updatingDto = new CommentUpdateRequestDto();
        updatingDto.setCommentId(VALID_COMMENT_ID);
        updatingDto.setContent(LONGER_THAN_4096_SYMBOLS_CONTENT);

        DataValidationException exception = assertThrows(DataValidationException.class, () -> {
            commentValidator.validateCommentUpdateDto(updatingDto);
        });

        assertEquals("Comment content is too long", exception.getMessage());
    }

    @Test
    void validateCommentUpdateDto_shouldPass_whenAllValidationsPass() {
        CommentUpdateRequestDto updatingDto = new CommentUpdateRequestDto();
        updatingDto.setCommentId(VALID_COMMENT_ID);
        updatingDto.setContent(VALID_CONTENT);

        assertDoesNotThrow(() -> commentValidator.validateCommentUpdateDto(updatingDto));
    }

    @Test
    void validateCommentExists_shouldThrowException_whenCommentDoesNotExist() {
        Long commentId = VALID_COMMENT_ID;
        when(commentRepository.existsById(commentId)).thenReturn(false);

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            commentValidator.validateCommentExists(commentId);
        });

        assertEquals("Comment with id " + commentId + " does not exist", exception.getMessage());
    }

    @Test
    public void validatePostExists_shouldThrowException_whenPostDoesNotExist() {
        CommentRequestDto commentRequestDto = new CommentRequestDto();
        commentRequestDto.setPostId(2L);

        when(postRepository.existsById(2L)).thenReturn(false);

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            commentValidator.validatePostExists(commentRequestDto.getPostId());
        });

        assertEquals("Post with id 2 does not exist", exception.getMessage());
    }

    @Test
    public void validatePostExists_shouldPass_whenPostExists() {
        CommentRequestDto commentRequestDto = new CommentRequestDto();
        commentRequestDto.setPostId(VALID_POST_ID);

        when(postRepository.existsById(commentRequestDto.getPostId())).thenReturn(true);

        assertDoesNotThrow(() -> commentValidator.validatePostExists(commentRequestDto.getPostId()));
    }

    @Test
    public void validateAuthorExists_shouldThrowException_whenUserDoesNotExist() {
        CommentRequestDto commentRequestDto = new CommentRequestDto();
        commentRequestDto.setAuthorId(VALID_AUTHOR_ID);

        when(userServiceClient.getUser(VALID_AUTHOR_ID))
                .thenThrow(new IllegalArgumentException("User with id 1 does not exist"));

        DataValidationException exception = assertThrows(DataValidationException.class, () -> {
            commentValidator.validateAuthorExists(commentRequestDto.getAuthorId());
        });

        assertEquals("User with id 1 does not exist", exception.getMessage());
    }

    @Test
    public void validateAuthorExists_shouldPass_whenUserExists() {
        CommentRequestDto commentRequestDto = new CommentRequestDto();
        commentRequestDto.setAuthorId(VALID_AUTHOR_ID);

        when(userServiceClient.getUser(1L)).thenReturn(VALID_USER_DTO);

        assertDoesNotThrow(() -> commentValidator.validateAuthorExists(commentRequestDto.getAuthorId()));
    }
}