package faang.school.postservice.service.news_feed;

import faang.school.postservice.dto.comment.CommentResponseDto;
import faang.school.postservice.dto.post.PostResponseDto;
import faang.school.postservice.exception.RedisLockException;
import faang.school.postservice.mapper.cache.PostCacheMapper;
import faang.school.postservice.model.cache.PostCache;
import faang.school.postservice.repository.redis.PostCacheRepository;
import faang.school.postservice.service.news_feed.cache.RedisTransactionManager;
import faang.school.postservice.service.news_feed.cache.RedisTransactionResult;
import faang.school.postservice.service.post.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class PostCacheService {
    private final PostCacheRepository postCacheRepository;
    private final PostCacheMapper postCacheMapper;
    private final PostService postService;
    private final RedisTransactionManager<String, PostCache> redisTransactionManager;
    private final RedisTemplate<String, PostCache> postRedisTemplate;
    @Value("${spring.data.redis.time-to-live}")
    private Long defaultExpiration;
    @Value("${spring.data.redis.max-comments:3}")
    private int maxComments;

    public void savePostCache(PostResponseDto dto) {
        PostCache cache = postCacheMapper.toPostCache(dto);
        if (cache.getLikes() == null) cache.setLikes(0);
        if (cache.getViews() == null) cache.setViews(0);
        if (cache.getComments() == null) cache.setComments(new ArrayList<>());
        cache.setExpiration(defaultExpiration);
        postCacheRepository.save(cache);
    }

    @Retryable(retryFor = RedisLockException.class, maxAttempts = 20, backoff = @Backoff(delay = 500))
    public void incrementLikes(Long postId) {
        String key = "posts:" + postId;
        RedisTransactionResult result = redisTransactionManager.updateRedisEntity(key, postRedisTemplate, (postCache, operations) -> {
            int curr = postCache.getLikes() == null ? 0 : postCache.getLikes();
            postCache.setLikes(curr + 1);
            operations.opsForValue().set(key, postCache);
        });
        if (result == RedisTransactionResult.NOT_FOUND) {
            PostResponseDto dto = postService.getPost(postId);
            if (dto != null && dto.getId() != null) savePostCache(dto);
        }
        if (result == RedisTransactionResult.LOCK_EXCEPTION) {
            throw new RedisLockException("Post %s was updated in concurrent transaction".formatted(postId));
        }
    }

    @Retryable(retryFor = RedisLockException.class, maxAttempts = 20, backoff = @Backoff(delay = 500))
    public void incrementViews(Long postId) {
        String key = "posts:" + postId;
        RedisTransactionResult result = redisTransactionManager.updateRedisEntity(key, postRedisTemplate, (postCache, operations) -> {
            int curr = postCache.getViews() == null ? 0 : postCache.getViews();
            postCache.setViews(curr + 1);
            operations.opsForValue().set(key, postCache);
        });
        if (result == RedisTransactionResult.NOT_FOUND) {
            PostResponseDto dto = postService.getPost(postId);
            if (dto != null && dto.getId() != null) savePostCache(dto);
        }
        if (result == RedisTransactionResult.LOCK_EXCEPTION) {
            throw new RedisLockException("Post %s was updated in concurrent transaction".formatted(postId));
        }
    }

    @Retryable(retryFor = RedisLockException.class, maxAttempts = 20, backoff = @Backoff(delay = 500))
    public void addComment(Long postId, CommentResponseDto commentDto) {
        String key = "posts:" + postId;
        RedisTransactionResult result = redisTransactionManager.updateRedisEntity(key, postRedisTemplate, (cache, operations) -> {
            List<CommentResponseDto> list = cache.getComments();
            if (list == null) list = new ArrayList<>();
            if (list.size() >= maxComments) list.remove(0);
            list.add(commentDto);
            cache.setComments(list);
            operations.opsForValue().set(key, cache);
        });
        if (result == RedisTransactionResult.NOT_FOUND) {
            PostResponseDto dto = postService.getPost(postId);
            if (dto != null && dto.getId() != null) {
                dto.setComments(List.of(commentDto));
                savePostCache(dto);
            }
        }
        if (result == RedisTransactionResult.LOCK_EXCEPTION) {
            throw new RedisLockException("Post %s was updated in concurrent transaction".formatted(postId));
        }
    }

    public PostResponseDto getPostFromCacheOrDb(Long postId) {
        String key = "posts:" + postId;
        PostCache cached = postRedisTemplate.opsForValue().get(key);
        if (cached != null) {
            return postCacheMapper.toDto(cached);
        }
        PostResponseDto dto = postService.getPost(postId);
        if (dto != null && dto.getId() != null) savePostCache(dto);
        return dto;
    }

    public List<PostCache> getPostCacheByIds(List<Long> postIds) {
        List<PostCache> iterable = postCacheRepository.findAllById(postIds);
        return StreamSupport.stream(iterable.spliterator(), false)
                .toList();
    }
}
