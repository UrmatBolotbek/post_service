package faang.school.postservice.model.cache;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@RedisHash(value = "author")
public class AuthorCache implements Serializable {
    @Id
    private Long id;
    private String username;

    @TimeToLive
    private Long expiration;
}
