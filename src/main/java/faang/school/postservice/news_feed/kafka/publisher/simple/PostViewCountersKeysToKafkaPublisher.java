package faang.school.postservice.news_feed.kafka.publisher.simple;

import faang.school.postservice.news_feed.dto.counter.PostViewCountersKeysEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PostViewCountersKeysToKafkaPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.topic.post.update_views}")
    private String topicName;

    public void publish(PostViewCountersKeysEvent message) {
        kafkaTemplate.send(topicName, message);
    }
}
