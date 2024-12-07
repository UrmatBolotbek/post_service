package faang.school.postservice.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import faang.school.postservice.dto.events_dto.CommentEventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CommentEventPublisher {

    @Value("${spring.data.redis.channels.comment_notification_channel}")
    private String channelName;
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    public void publish(CommentEventDto commentEventDto) {
        String json;
        try {
            json = objectMapper.writeValueAsString(commentEventDto);
        } catch (JsonProcessingException e) {
            log.error("Failed to turn commentEvent into json for comment with id {}", commentEventDto.getCommentId());
            throw new RuntimeException(e);
        }
        redisTemplate.convertAndSend(channelName, json);
    }
}