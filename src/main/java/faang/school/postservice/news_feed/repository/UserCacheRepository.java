package faang.school.postservice.news_feed.repository;

import faang.school.postservice.dto.user.UserDto;
import faang.school.postservice.news_feed.dto.serializable.PostCache;
import faang.school.postservice.news_feed.repository.key.PostKey;
import faang.school.postservice.news_feed.repository.key.UserKey;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.Boolean.FALSE;

@RequiredArgsConstructor
@Repository
public class UserCacheRepository {
    private final RedisTemplate<String, UserDto> userDtoRedisTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisTransaction redisTransaction;
    private final ZSetRepository zSetRepository;
    private final UserKey userKey;
    private final PostKey postKey;

    @Value("${spring.data.redis.ttl.feed.user_hour}")
    private int userTTL;

    @Value("${app.post.cache.news_feed.user_feed_size_zset_index}")
    private long userFeedSizeZSetIndex;

    public void save(UserDto user) {
        String key = userKey.build(user.getId());

        redisTransaction.execute(userDtoRedisTemplate, key, operations -> {
            operations.multi();
            userDtoRedisTemplate.opsForValue().set(key, user, Duration.ofHours(userTTL));
            return operations.exec();
        });
    }

    public void saveAll(Collection<UserDto> userDtoList) {
        userDtoList.forEach(this::save);
    }

    public void userFeedUpdate(long userId, long postId, long timestamp) {
        String userFeedKey = userKey.buildFeedKey(userId);
        String postKey = this.postKey.build(postId);
        zSetRepository.setAndRemoveRange(userFeedKey, postKey, timestamp, userFeedSizeZSetIndex);
    }

    public void mapAndSavePostIdsToFeed(long userId, List<PostCache> postDtoList) {
        String feedUserKey = userKey.buildFeedKey(userId);

        Set<ZSetOperations.TypedTuple<String>> tuples = postDtoList.stream()
                .map(post -> new DefaultTypedTuple<>(postKey.build(post.getId()), (double) getTimestamp(post.getPublishedAt())))
                .collect(Collectors.toSet());

        zSetRepository.saveTuplesByKey(feedUserKey, tuples);
    }

    public Optional<UserDto> findById(long id) {
        String key = userKey.build(id);
        return Optional.ofNullable(userDtoRedisTemplate.opsForValue().get(key));
    }

    public Set<String> findPostIdsInUserFeed(long userId, long offset, long limit) {
        String feedUserKey = userKey.buildFeedKey(userId);
        return zSetRepository.getValuesInRange(feedUserKey, offset, limit);
    }

    public List<Long> filterUserIdsOnWithoutCache(Set<Long> userIds) {
        return userIds.stream()
                .filter(userId -> FALSE.equals(redisTemplate.hasKey(userKey.build(userId))))
                .toList();
    }

    private long getTimestamp(LocalDateTime date) {
        return date.toInstant(ZoneOffset.UTC).toEpochMilli();
    }
}
