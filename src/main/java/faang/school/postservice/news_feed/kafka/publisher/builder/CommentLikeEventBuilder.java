package faang.school.postservice.news_feed.kafka.publisher.builder;

import faang.school.postservice.model.Like;
import faang.school.postservice.news_feed.dto.event.CommentLikeEvent;
import org.springframework.stereotype.Component;

@Component
public class CommentLikeEventBuilder {
    public CommentLikeEvent build(Like like) {
        return CommentLikeEvent.builder()
                .postId(like.getComment().getPost().getId())
                .commentId(like.getComment().getId())
                .build();
    }
}
