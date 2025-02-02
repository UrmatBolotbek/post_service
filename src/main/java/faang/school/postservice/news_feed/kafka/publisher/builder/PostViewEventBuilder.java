package faang.school.postservice.news_feed.kafka.publisher.builder;

import faang.school.postservice.news_feed.dto.event.PostViewEvent;
import faang.school.postservice.news_feed.dto.serializable.PostViewEventParticipant;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PostViewEventBuilder {
    public PostViewEvent build(List<PostViewEventParticipant> posts) {
        List<Long> postIds = posts.stream()
                .map(PostViewEventParticipant::getId)
                .toList();

        return PostViewEvent.builder()
                .postsIds(postIds)
                .build();
    }
}
