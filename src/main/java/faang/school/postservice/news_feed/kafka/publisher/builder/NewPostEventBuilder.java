package faang.school.postservice.news_feed.kafka.publisher.builder;

import faang.school.postservice.model.Post;
import faang.school.postservice.news_feed.dto.event.NewPostEvent;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.util.List;

@Component
public class NewPostEventBuilder {
    public NewPostEvent build(Post post, List<Long> followerIds) {
        return NewPostEvent.builder()
                .postId(post.getId())
                .authorId(post.getAuthorId())
                .createdAtTimestamp(post.getCreatedAt().toInstant(ZoneOffset.UTC).toEpochMilli())
                .followersIds(followerIds)
                .build();
    }
}
