package faang.school.postservice.news_feed.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.function.BiConsumer;

@RequiredArgsConstructor
@Service
public class RedisOperations {
    private final RedisTemplate<String, String> stringValueRedisTemplate;
    private final RedisTemplate<String, Long> longValueRedisTemplate;
    private final RedisTransaction redisTransaction;

    public <T> void assignFieldByCounter(String counterKey, String objectKey, RedisTemplate<String, T> redisTemplate,
                                         Duration duration, BiConsumer<T, Long> consumer) {
        redisTransaction.execute(redisTemplate, objectKey, operations -> {
            String counterValueStr = stringValueRedisTemplate.opsForValue().get(counterKey);
            T cacheDto = redisTemplate.opsForValue().get(objectKey);

            long counterValue = accrueViews(cacheDto, Long.parseLong(counterValueStr), consumer);

            operations.multi();

            decrementViewsAndSaveChanges(cacheDto, counterKey, counterValue, redisTemplate, objectKey, duration);

            return operations.exec();
        });
    }

    private <T> long accrueViews(T cacheDto, long counterValue, BiConsumer<T, Long> consumer) {
        consumer.accept(cacheDto, counterValue);
        return counterValue;
    }

    private <T> void decrementViewsAndSaveChanges(T cacheDto, String counterKey, long counterValue,
                                                  RedisTemplate<String, T> redisTemplate,
                                                  String objectKey, Duration duration) {
        longValueRedisTemplate.opsForValue().decrement(counterKey, counterValue);
        redisTemplate.opsForValue().set(objectKey, cacheDto, duration);
    }
}
