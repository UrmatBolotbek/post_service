package faang.school.postservice.news_feed.kafka.publisher;

import faang.school.postservice.aspects.publisher.Publisher;
import faang.school.postservice.model.Like;
import faang.school.postservice.news_feed.dto.event.CommentLikeEvent;
import faang.school.postservice.news_feed.enums.PublisherType;
import faang.school.postservice.news_feed.kafka.publisher.builder.CommentLikeEventBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import static faang.school.postservice.news_feed.enums.PublisherType.COMMENT_LIKE;

@Getter
@RequiredArgsConstructor
@Component
public class LikeCommentToKafkaPublisher implements Publisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final CommentLikeEventBuilder builder;
    private final PublisherType type = COMMENT_LIKE;

    @Value("${spring.kafka.topic.post.like_post_comment}")
    private String topicName;

    @Override
    public void publish(JoinPoint joinPoint, Object returnedValue) {
        if (returnedValue == null) {
            return;
        }
        CommentLikeEvent message = builder.build((Like) returnedValue);

        kafkaTemplate.send(topicName, message);
    }
}
