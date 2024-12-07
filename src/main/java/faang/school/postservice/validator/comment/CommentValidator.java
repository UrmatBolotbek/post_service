package faang.school.postservice.validator.comment;

import faang.school.postservice.client.UserServiceClient;
import faang.school.postservice.dto.comment.CommentRequestDto;
import faang.school.postservice.dto.comment.CommentUpdateRequestDto;
import faang.school.postservice.dto.user.UserDto;
import faang.school.postservice.exception.DataValidationException;
import faang.school.postservice.repository.CommentRepository;
import faang.school.postservice.repository.PostRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class CommentValidator {
    private final UserServiceClient userServiceClient;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    public void validateCommentDtoInput(CommentRequestDto commentRequestDto) {
        validateCommentContentInRequestDto(commentRequestDto.getContent());
        if (commentRequestDto.getAuthorId() == null) {
            log.error("Data validation Exception - comment author id is null");
            throw new DataValidationException("Comment must have an author");
        }
        if (commentRequestDto.getPostId() == null) {
            log.error("Data validation Exception - comment post id is null");
            throw new DataValidationException("Comment must relate to a post");
        }
    }

    public void validateCommentUpdateDto(CommentUpdateRequestDto commentUpdateRequestDto) {
        validateCommentContentInRequestDto(commentUpdateRequestDto.getContent());
        Long commentId = commentUpdateRequestDto.getCommentId();
        if (commentId == null) {
            log.error("Data validation Exception - comment id is null");
            throw new DataValidationException("Comment must have an id");
        }
    }

    private static void validateCommentContentInRequestDto(String content) {
        if (content == null || content.isEmpty()) {
            log.error("Data validation Exception - comment content is empty on update");
            throw new DataValidationException("Comment content is empty");
        }
        if (content.length() > 4096) {
            log.error("Data validation Exception - comment content too long on update");
            throw new DataValidationException("Comment content is too long");
        }
    }

    public void validateCommentExists(Long commentId) {
        boolean commentExists = commentRepository.existsById(commentId);
        if (!commentExists) {
            log.error("Data validation Exception - comment does not exist");
            throw new EntityNotFoundException("Comment with id " + commentId + " does not exist");
        }
    }

    public void validatePostExists(Long postId) {
        boolean postExists = postRepository.existsById(postId);
        if (!postExists) {
            log.error("Data validation Exception - post does not exist");
            throw new EntityNotFoundException("Post with id " + postId + " does not exist");
        }
    }

    public void validateAuthorExists(long authorId) {
        try {
            UserDto userDto = userServiceClient.getUser(authorId);
            if (userDto == null) {
                log.error("Data validation Exception - author does not exist");
                throw new EntityNotFoundException("User with id " + authorId + " does not exist");
            }
        } catch (Exception e) {
            log.error("Error validating user existence: {}", e.getMessage());
            throw new DataValidationException("User with id " + authorId + " does not exist");
        }
    }
}