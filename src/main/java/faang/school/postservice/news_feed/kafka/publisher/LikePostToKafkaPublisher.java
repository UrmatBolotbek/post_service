package faang.school.postservice.news_feed.kafka.publisher;

import faang.school.postservice.aspects.publisher.Publisher;
import faang.school.postservice.model.Like;
import faang.school.postservice.news_feed.dto.event.PostLikeEvent;
import faang.school.postservice.news_feed.enums.PublisherType;
import faang.school.postservice.news_feed.kafka.publisher.builder.PostLikeEventBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import static faang.school.postservice.news_feed.enums.PublisherType.POST_LIKE;

@Getter
@RequiredArgsConstructor
@Component
public class LikePostToKafkaPublisher implements Publisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final PostLikeEventBuilder builder;
    private final PublisherType type = POST_LIKE;

    @Value("${spring.kafka.topic.post.like}")
    private String topicName;

    @Override
    public void publish(JoinPoint joinPoint, Object returnedValue) {
        if (returnedValue == null) {
            return;
        }
        PostLikeEvent message = builder.build((Like) returnedValue);

        kafkaTemplate.send(topicName, message);
    }
}
