package faang.school.postservice.kafka.event;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthorCachedEvent {
    private Long authorId;
}
