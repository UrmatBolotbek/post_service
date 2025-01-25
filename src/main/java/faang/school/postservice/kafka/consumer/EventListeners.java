package faang.school.postservice.kafka.consumer;

import faang.school.postservice.dto.post.PostResponseDto;
import faang.school.postservice.kafka.event.AuthorCachedEvent;
import faang.school.postservice.service.news_feed.AuthorCacheService;
import faang.school.postservice.service.news_feed.PostCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class EventListeners {
    private final AuthorCacheService authorCacheService;
    private final PostCacheService postCacheService;

    @KafkaListener(topics = "${spring.kafka.topic-name.author-cashed:author-cashed}", groupId = "${spring.kafka.consumer.group-id}")
    public void authorCachedListener(AuthorCachedEvent event, Acknowledgment acknowledgment) {
        try {
            authorCacheService.saveAuthorCache(event.getAuthorId());
            acknowledgment.acknowledge();
            log.info("Author with id:{} is successfully added to cache.", event.getAuthorId());
        } catch (Exception e) {
            log.error("Author with id:{} is not added to cache.", event.getAuthorId());
            throw e;
        }
    }

    @KafkaListener(topics = "${spring.kafka.topic-name.post-cashed:post-cashed}", groupId = "${spring.kafka.consumer.group-id}")
    public void postCachedListener(PostResponseDto event, Acknowledgment acknowledgment) {
        try {
            postCacheService.savePostCache(event);
            acknowledgment.acknowledge();
            log.info("Post with id:{} is successfully added to cache.", event.getId());
        } catch (Exception e) {
            log.error("Post with id:{} is not added to cache.", event.getId());
            throw e;
        }
    }
}

