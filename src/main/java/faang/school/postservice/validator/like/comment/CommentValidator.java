package faang.school.postservice.validator.like.comment;

import faang.school.postservice.repository.CommentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class CommentValidator {
    private final CommentRepository commentRepository;

    public CommentValidator(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    public void validateCommentExists(long commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new EntityNotFoundException("Comment with id " + commentId + " does not exist.");
        }
    }
}

