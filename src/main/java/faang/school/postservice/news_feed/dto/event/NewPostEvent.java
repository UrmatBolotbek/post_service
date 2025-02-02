package faang.school.postservice.news_feed.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewPostEvent {
    private Long postId;
    private Long authorId;
    private Long createdAtTimestamp;
    private List<Long> followersIds;
}
