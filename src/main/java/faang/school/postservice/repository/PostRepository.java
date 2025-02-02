package faang.school.postservice.repository;

import faang.school.postservice.model.Post;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends CrudRepository<Post, Long> {

    List<Post> findByAuthorId(long authorId);

    List<Post> findByProjectId(long projectId);

    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.likes WHERE p.projectId = :projectId")
    List<Post> findByProjectIdWithLikes(long projectId);

    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.likes WHERE p.authorId = :authorId")
    List<Post> findByAuthorIdWithLikes(long authorId);

    @Query("SELECT p FROM Post p WHERE p.published = false AND p.deleted = false AND p.scheduledAt <= CURRENT_TIMESTAMP")
    List<Post> findReadyToPublish();

    @Query("SELECT p FROM Post p WHERE p.published = false")
    List<Post> findByPublishedFalse();

    default Post getPostById(Long id) {
        return findById(id).orElseThrow(() -> new EntityNotFoundException("Post with id " + id + " not found"));
    }

    List<Post> findByVerifiedIsNull();

    @Query(nativeQuery = true, value = """
            SELECT *
            FROM post
            WHERE author_id IN (:authorsId)
            ORDER BY created_at DESC
            OFFSET :offset
            LIMIT :limit
            """)
    List<Post> findSetOfPostsByAuthorsIds(@Param("offset") long offset, @Param("limit") long limit,
                                          @Param("authorsId") List<Long> authorsId);
}
