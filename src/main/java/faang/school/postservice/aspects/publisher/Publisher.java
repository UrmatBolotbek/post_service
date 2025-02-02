package faang.school.postservice.aspects.publisher;

import faang.school.postservice.news_feed.enums.PublisherType;
import org.aspectj.lang.JoinPoint;

public interface Publisher {
    PublisherType getType();

    void publish(JoinPoint joinPoint, Object returnedValue);
}
