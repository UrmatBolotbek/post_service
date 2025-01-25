package faang.school.postservice.model.cache;

import faang.school.postservice.dto.comment.CommentResponseDto;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@RedisHash(value = "posts")
public class PostCache implements Serializable {
    @Id
    private Long id;
    private String content;
    private Long authorId;
    private Integer likes;
    private Integer views;
    private List<CommentResponseDto> comments;
    private LocalDateTime publishedAt;

    @TimeToLive
    private Long expiration;
}
