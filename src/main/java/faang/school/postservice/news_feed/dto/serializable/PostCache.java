package faang.school.postservice.news_feed.dto.serializable;

import com.fasterxml.jackson.annotation.JsonFormat;
import faang.school.postservice.dto.user.UserDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static faang.school.postservice.util.LocalDateTimePatterns.DATE_TIME_PATTERN;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostCache implements PostViewEventParticipant {
    private Long id;
    private String content;
    private Long authorId;
    private UserDto authorDto;

    @Builder.Default
    private Long views = 0L;

    private Long likesCount;
    private Long commentsCount;
    private Set<Long> albumIds;
    private Set<Long> resourceIds;

    @JsonFormat(pattern = DATE_TIME_PATTERN)
    private LocalDateTime publishedAt;

    @JsonFormat(pattern = DATE_TIME_PATTERN)
    private LocalDateTime updatedAt;

    private List<String> hashTags;
    private List<CommentCache> comments;
}

