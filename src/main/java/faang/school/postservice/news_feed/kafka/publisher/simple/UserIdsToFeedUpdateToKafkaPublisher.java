package faang.school.postservice.news_feed.kafka.publisher.simple;

import faang.school.postservice.news_feed.dto.event.UsersFeedUpdateEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class UserIdsToFeedUpdateToKafkaPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.topic.user.feed_update}")
    private String topicName;

    public void publish(UsersFeedUpdateEvent message) {
        kafkaTemplate.send(topicName, message);
    }
}
