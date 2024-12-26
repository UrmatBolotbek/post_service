package faang.school.postservice.repository;

import faang.school.postservice.model.Hashtag;
import faang.school.postservice.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HashTagRepository extends JpaRepository<Hashtag, Long> {

    @Query(nativeQuery = true, value = """
            SELECT p.* FROM post p
            JOIN post_hashtag ph ON ph.post_id = p.id
            JOIN hashtag h ON ph.hashtag_id = h.id
            WHERE h.title = ?1
            """)
    List<Post> findAllByHashtagTitle(String hashtagTitle);

    Optional<Hashtag> findByTitle(String title);

}
