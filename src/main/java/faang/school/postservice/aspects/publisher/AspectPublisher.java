package faang.school.postservice.aspects.publisher;

import faang.school.postservice.annotations.publisher.PublishEvent;
import faang.school.postservice.config.context.UserContext;
import faang.school.postservice.news_feed.enums.PublisherType;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.stream.Collectors;

@Aspect
@Component
public class AspectPublisher {
    private final Map<PublisherType, Publisher> publishers;
    private final Executor executor;
    private final UserContext userContext;

    public AspectPublisher(UserContext userContext, Executor postCacheServicePool, List<Publisher> publishers) {
        this.userContext = userContext;
        this.executor = postCacheServicePool;
        this.publishers = publishers.stream()
                .collect(Collectors.toMap(Publisher::getType, Function.identity()));
    }

    @AfterReturning(pointcut = "@annotation(publishEvent)", returning = "returnedValue",
            argNames = "joinPoint, publishEvent, returnedValue")
    public void publishEvent(JoinPoint joinPoint, PublishEvent publishEvent, Object returnedValue) {
        Long userId = userContext.getUserId();

        executor.execute(() -> execute(joinPoint, publishEvent, returnedValue, userId));
    }

    private void execute(JoinPoint joinPoint, PublishEvent publishEvent, Object returnedValue, Long userId) {
        userContext.setUserId(userId);

        Publisher publisher = publishers.get(publishEvent.type());
        publisher.publish(joinPoint, returnedValue);
    }
}
