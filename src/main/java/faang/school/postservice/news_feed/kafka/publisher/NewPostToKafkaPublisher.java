package faang.school.postservice.news_feed.kafka.publisher;

import faang.school.postservice.aspects.publisher.Publisher;
import faang.school.postservice.client.UserServiceClient;
import faang.school.postservice.model.Post;
import faang.school.postservice.news_feed.dto.event.NewPostEvent;
import faang.school.postservice.news_feed.enums.PublisherType;
import faang.school.postservice.news_feed.kafka.publisher.builder.NewPostEventBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

import static faang.school.postservice.news_feed.enums.PublisherType.NEW_POST;

@Getter
@RequiredArgsConstructor
@Component
public class NewPostToKafkaPublisher implements Publisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final UserServiceClient userServiceClient;
    private final NewPostEventBuilder builder;
    private final PublisherType type = NEW_POST;

    @Value("${spring.kafka.topic.post.new}")
    private String topicName;

    @Override
    public void publish(JoinPoint joinPoint, Object returnedValue) {
        if (returnedValue == null) {
            return;
        }
        Post post = (Post) returnedValue;
        NewPostEvent message = builder.build(post, getFollowersId(post.getAuthorId()));

        kafkaTemplate.send(topicName, message);
    }

    private List<Long> getFollowersId(Long authorId) {
        if (authorId == null) {
            return List.of();
        }
        return userServiceClient.getFollowersId(authorId);
    }
}
