package faang.school.postservice.news_feed.kafka.publisher.simple;

import faang.school.postservice.news_feed.dto.event.NewPostEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class PartitionSubscribersToKafkaPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.topic.post.update_feeds}")
    private String topicName;

    public void publish(NewPostEvent message) {

        kafkaTemplate.send(topicName, message);
    }
}
