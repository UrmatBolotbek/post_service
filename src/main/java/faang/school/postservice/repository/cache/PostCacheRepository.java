package faang.school.postservice.repository.cache;

import faang.school.postservice.model.cache.PostCache;
import org.springframework.data.keyvalue.repository.KeyValueRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostCacheRepository extends KeyValueRepository<PostCache, Long> {
}
