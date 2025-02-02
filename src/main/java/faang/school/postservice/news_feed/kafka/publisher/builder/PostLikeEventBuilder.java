package faang.school.postservice.news_feed.kafka.publisher.builder;

import faang.school.postservice.model.Like;
import faang.school.postservice.news_feed.dto.event.PostLikeEvent;
import org.springframework.stereotype.Component;

@Component
public class PostLikeEventBuilder {
    public PostLikeEvent build(Like like) {
        return PostLikeEvent.builder()
                .postId(like.getPost().getId())
                .build();
    }
}
