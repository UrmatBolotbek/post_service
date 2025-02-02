package faang.school.postservice.news_feed.kafka.publisher.simple;

import faang.school.postservice.news_feed.dto.counter.PostLikeCountersKeysEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PostLikeCountersKeysToKafkaPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.topic.post.update_likes}")
    private String topicName;

    public void publish(PostLikeCountersKeysEvent message) {
        kafkaTemplate.send(topicName, message);
    }
}
