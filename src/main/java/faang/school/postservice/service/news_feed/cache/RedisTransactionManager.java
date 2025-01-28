package faang.school.postservice.service.news_feed.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.function.BiConsumer;

@Slf4j
@Component
public class RedisTransactionManager<K, V> {
    public RedisTransactionResult updateRedisEntity(
            K key,
            RedisTemplate<K, V> redisCacheTemplate,
            BiConsumer<V, RedisOperations<K, V>> action
    ) {
        try {
            redisCacheTemplate.watch(key);
            V entity = redisCacheTemplate.opsForValue().get(key);
            if (entity == null) {
                redisCacheTemplate.unwatch();
                return RedisTransactionResult.NOT_FOUND;
            }
            redisCacheTemplate.multi();
            action.accept(entity, redisCacheTemplate);
            if (redisCacheTemplate.exec() == null) {
                return RedisTransactionResult.LOCK_EXCEPTION;
            }
            return RedisTransactionResult.SUCCESS;
        } catch (RedisConnectionFailureException e) {
            return RedisTransactionResult.FAILURE;
        }
    }
}
