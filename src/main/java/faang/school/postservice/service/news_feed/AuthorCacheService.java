package faang.school.postservice.service.news_feed;

import faang.school.postservice.client.UserServiceClient;
import faang.school.postservice.dto.user.UserDto;
import faang.school.postservice.mapper.cache.AuthorCacheMapper;
import faang.school.postservice.model.cache.AuthorCache;
import faang.school.postservice.repository.redis.AuthorCacheRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthorCacheService {
    private final AuthorCacheRepository repository;
    private final AuthorCacheMapper authorCacheMapper;
    private final UserServiceClient userServiceClient;
    @Value("${spring.data.redis.time-to-live}")
    private Long defaultExpiration;

    public void saveAuthorCache(Long authorId) {
        UserDto author = userServiceClient.getUser(authorId);
        AuthorCache entity = authorCacheMapper.toAuthorCache(author);
        entity.setExpiration(defaultExpiration);
        repository.save(entity);
    }
}
