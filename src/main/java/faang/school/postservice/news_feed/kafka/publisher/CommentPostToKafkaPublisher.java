package faang.school.postservice.news_feed.kafka.publisher;

import faang.school.postservice.aspects.publisher.Publisher;
import faang.school.postservice.client.UserServiceClient;
import faang.school.postservice.config.context.UserContext;
import faang.school.postservice.mapper.comment.CommentMapper;
import faang.school.postservice.model.Comment;
import faang.school.postservice.news_feed.dto.event.NewCommentEvent;
import faang.school.postservice.news_feed.enums.PublisherType;
import faang.school.postservice.news_feed.kafka.publisher.builder.NewCommentEventBuilder;
import faang.school.postservice.news_feed.repository.UserCacheRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import static faang.school.postservice.news_feed.enums.PublisherType.POST_COMMENT;

@Getter
@RequiredArgsConstructor
@Component
public class CommentPostToKafkaPublisher implements Publisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final UserCacheRepository userCacheRepository;
    private final UserServiceClient userServiceClient;
    private final NewCommentEventBuilder builder;
    private final CommentMapper commentMapper;
    private final UserContext userContext;
    private final PublisherType type = POST_COMMENT;

    @Value("${spring.kafka.topic.post.comment}")
    private String topicName;

    @Override
    public void publish(JoinPoint joinPoint, Object returnedValue) {
        if (returnedValue == null) {
            return;
        }
        NewCommentEvent message = builder.build((Comment) returnedValue);

        kafkaTemplate.send(topicName, message);
    }
}
