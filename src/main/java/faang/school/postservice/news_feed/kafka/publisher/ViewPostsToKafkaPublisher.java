package faang.school.postservice.news_feed.kafka.publisher;

import faang.school.postservice.aspects.publisher.Publisher;
import faang.school.postservice.news_feed.dto.event.PostViewEvent;
import faang.school.postservice.news_feed.dto.serializable.PostViewEventParticipant;
import faang.school.postservice.news_feed.enums.PublisherType;
import faang.school.postservice.news_feed.kafka.publisher.builder.PostViewEventBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

import static faang.school.postservice.news_feed.enums.PublisherType.POST_VIEW;

@Getter
@RequiredArgsConstructor
@Component
public class ViewPostsToKafkaPublisher implements Publisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final PostViewEventBuilder builder;
    private final PublisherType type = POST_VIEW;

    @Value("${spring.kafka.topic.post.view}")
    private String topicName;

    @Override
    public void publish(JoinPoint joinPoint, Object returnedValue) {
        if (returnedValue == null) {
            return;
        }

        PostViewEvent message;

        if (returnedValue instanceof List) {
            message = builder.build((List<PostViewEventParticipant>) returnedValue);
        } else {
            message = builder.build(List.of((PostViewEventParticipant) returnedValue));
        }

        kafkaTemplate.send(topicName, message);
    }
}
