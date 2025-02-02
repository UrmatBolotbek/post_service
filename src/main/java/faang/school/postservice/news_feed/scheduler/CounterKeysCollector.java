package faang.school.postservice.news_feed.scheduler;

import faang.school.postservice.news_feed.dto.counter.CommentCountersKeysEvent;
import faang.school.postservice.news_feed.dto.counter.CommentLikeCounterKeysEvent;
import faang.school.postservice.news_feed.dto.counter.PostLikeCountersKeysEvent;
import faang.school.postservice.news_feed.dto.counter.PostViewCountersKeysEvent;
import faang.school.postservice.news_feed.kafka.publisher.simple.CommentCountersKeysToKafkaPublisher;
import faang.school.postservice.news_feed.kafka.publisher.simple.CommentLikeCounterKeysToKafkaPublisher;
import faang.school.postservice.news_feed.kafka.publisher.simple.PostLikeCountersKeysToKafkaPublisher;
import faang.school.postservice.news_feed.kafka.publisher.simple.PostViewCountersKeysToKafkaPublisher;
import faang.school.postservice.news_feed.repository.CommentCacheRepository;
import faang.school.postservice.news_feed.repository.PostCacheRepository;
import faang.school.postservice.news_feed.service.feed.util.EventsPartitioner;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Component
public class CounterKeysCollector {
    private final CommentLikeCounterKeysToKafkaPublisher commentLikeCountersPublisher;
    private final CommentCountersKeysToKafkaPublisher commentCounterPublisher;
    private final PostLikeCountersKeysToKafkaPublisher likeCountersPublisher;
    private final PostViewCountersKeysToKafkaPublisher viewCountersPublisher;
    private final CommentCacheRepository commentCacheRepository;
    private final PostCacheRepository postCacheRepository;
    private final EventsPartitioner partitioner;

    @Scheduled(cron = "${app.post.feed.scheduler.cron.post_comment_counter_collector}")
    public void collectCommentCounters() {
        Set<String> countersKeys = postCacheRepository.getCommentCounterKeys();
        List<CommentCountersKeysEvent> messages =
                partitioner.partitionCommentCounterKeysAndMapToMessage(new ArrayList<>(countersKeys));

        messages.forEach(commentCounterPublisher::publish);
    }

    @Scheduled(cron = "${app.post.feed.scheduler.cron.post_comment_like_counter_collector}")
    public void collectCommentLikeCounters() {
        Set<String> counterKeys = commentCacheRepository.getCommentLikeCounterKeys();
        List<CommentLikeCounterKeysEvent> messages =
                partitioner.partitionCommentLikeCounterKeysAndMapToMessage(new ArrayList<>(counterKeys));

        messages.forEach(commentLikeCountersPublisher::publish);
    }

    @Scheduled(cron = "${app.post.feed.scheduler.cron.post_like_counter_collector}")
    public void collectPostLikeCounters() {
        Set<String> counterKeys = postCacheRepository.getLikeCounterKeys();
        List<PostLikeCountersKeysEvent> messages =
                partitioner.partitionLikeCounterKeysAndMapToMessage(new ArrayList<>(counterKeys));

        messages.forEach(likeCountersPublisher::publish);
    }

    @Scheduled(cron = "${app.post.feed.scheduler.cron.post_view_counter_collector}")
    public void collectPostViewCounters() {
        Set<String> countersKeys = postCacheRepository.getViewCounterKeys();
        List<PostViewCountersKeysEvent> messages =
                partitioner.partitionViewCounterKeysAndMapToMessage(new ArrayList<>(countersKeys));

        messages.forEach(viewCountersPublisher::publish);
    }
}
