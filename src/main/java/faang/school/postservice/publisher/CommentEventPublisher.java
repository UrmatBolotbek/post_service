package faang.school.postservice.publisher;

import com.fasterxml.jackson.databind.ObjectMapper;
import faang.school.postservice.dto.events_dto.CommentEventDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class CommentEventPublisher extends EventPublisherAbstract<CommentEventDto> {

    @Value("${spring.data.redis.channels.comment-channel}")
    private String channelName;

    public CommentEventPublisher(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        super(redisTemplate, objectMapper);
    }


    public void publish(CommentEventDto commentEventDto) {
        handleEvent(commentEventDto, channelName);
    }
}