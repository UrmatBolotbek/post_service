package faang.school.postservice.repository.cache;

import faang.school.postservice.model.cache.AuthorCache;
import org.springframework.data.keyvalue.repository.KeyValueRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthorCacheRepository extends KeyValueRepository<AuthorCache, Long> {
}
