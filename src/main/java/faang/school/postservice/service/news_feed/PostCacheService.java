package faang.school.postservice.service.news_feed;

import faang.school.postservice.dto.post.PostResponseDto;
import faang.school.postservice.mapper.cache.PostCacheMapper;
import faang.school.postservice.model.cache.PostCache;
import faang.school.postservice.repository.redis.PostCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostCacheService {
    private final PostCacheRepository postCacheRepository;
    private final PostCacheMapper postCacheMapper;

    public void savePostCache(PostResponseDto postDto) {
        PostCache postCache = postCacheMapper.toPostCache(postDto);
        postCacheRepository.save(postCache);
    }
}