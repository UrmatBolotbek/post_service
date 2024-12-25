package faang.school.postservice.publisher;

import com.fasterxml.jackson.databind.ObjectMapper;
import faang.school.postservice.dto.user.UserForBanEventDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class UserBanEventPublisher extends EventPublisherAbstract<UserForBanEventDto> {

    @Value("${spring.data.redis.channels.ban-channel}")
    private String channelName;

    public UserBanEventPublisher(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        super(redisTemplate, objectMapper);
    }

    public void publish(UserForBanEventDto userForBanEventDto) {
        handleEvent(userForBanEventDto, channelName);
    }

}
