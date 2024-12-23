package faang.school.postservice.repository;

import faang.school.postservice.model.Comment;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment c WHERE c.post.id = :postId")
    List<Comment> findAllByPostId(long postId);

    @Modifying
    @Query("DELETE FROM Comment c WHERE c.id = :id")
    void deleteAllById(@Param("id") Long id);

    @Query("SELECT c.authorId FROM Comment c WHERE c.verified = false GROUP BY c.authorId HAVING COUNT(c) > 5")
    List<Long> getAuthorIdsForBanFromComments();

    default Comment getCommentById(Long id) {
        return findById(id).orElseThrow(() -> new EntityNotFoundException("Comment with id " + id + " not found"));
    }
}
