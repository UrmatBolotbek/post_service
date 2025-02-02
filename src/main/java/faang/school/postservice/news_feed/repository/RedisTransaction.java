package faang.school.postservice.news_feed.repository;

import lombok.NonNull;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.List;
import java.util.function.Function;

@Service
public class RedisTransaction {
    @Retryable(retryFor = OptimisticLockingFailureException.class,
            maxAttemptsExpression = "#{@environment.getProperty('spring.data.redis.retry.opt_lock_max_attempt')}")
    public void execute(RedisTemplate<?, ?> template, String key,
                        Function<RedisOperations<?, ?>, List<Object>> function) {
        template.execute(new SessionCallback<>() {
            @Override
            public Object execute(@NonNull RedisOperations operations) throws DataAccessException {
                try {
                    operations.watch(key);

                    List<Object> result = function.apply(operations);

                    if (result.isEmpty()) {
                        throw new OptimisticLockingFailureException("Redis optimistic lock exception by key: " + key);
                    }
                    return result;
                } catch (JedisConnectionException exception) {
                    operations.discard();
                    throw exception;
                }
            }
        });
    }
}
