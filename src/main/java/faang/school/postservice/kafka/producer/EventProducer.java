package faang.school.postservice.kafka.producer;

import faang.school.postservice.dto.post.PostResponseDto;
import faang.school.postservice.kafka.event.AuthorCachedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventProducer {
    @Value("${spring.kafka.topic-name.author-cached:author-cashed}")
    private String authorTopic;
    @Value("${spring.kafka.topic-name.post-cached:post-cashed}")
    private String postCashedTopic;

    private final KafkaTemplate<Long, Object> kafkaTemplate;

    public void sendAuthorCashedEvent(AuthorCachedEvent event) {
        kafkaTemplate.send(authorTopic, event);
    }

    public void sendPostCashedEvent(PostResponseDto event) {
        kafkaTemplate.send(postCashedTopic, event);
    }
}
