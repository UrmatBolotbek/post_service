package faang.school.postservice.kafka;

import faang.school.postservice.client.UserServiceClient;
import faang.school.postservice.dto.post.PostResponseDto;
import faang.school.postservice.dto.user.UserDto;
import faang.school.postservice.kafka.event.AuthorCachedEvent;
import faang.school.postservice.kafka.producer.EventProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventsManager {
    private final EventProducer eventProducer;
    private final UserServiceClient userServiceClient;

    public void generateAndSendAuthorCachedEvent(Long authorId) {
        UserDto author = userServiceClient.getUser(authorId);
        AuthorCachedEvent event = AuthorCachedEvent.builder()
                .authorId(author.getId())
                .build();

        eventProducer.sendAuthorCashedEvent(event);
    }

    public void generateAndSendPostCachedEvent(PostResponseDto event) {
        eventProducer.sendPostCashedEvent(event);
    }
}
