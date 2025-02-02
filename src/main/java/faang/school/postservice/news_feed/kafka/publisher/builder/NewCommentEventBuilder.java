package faang.school.postservice.news_feed.kafka.publisher.builder;

import faang.school.postservice.model.Comment;
import faang.school.postservice.news_feed.dto.event.NewCommentEvent;
import org.springframework.stereotype.Component;

@Component
public class NewCommentEventBuilder {
    public NewCommentEvent build(Comment comment) {
        return NewCommentEvent.builder()
                .postId(comment.getPost().getId())
                .build();
    }
}
