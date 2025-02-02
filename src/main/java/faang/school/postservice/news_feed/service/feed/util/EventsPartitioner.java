package faang.school.postservice.news_feed.service.feed.util;

import faang.school.postservice.news_feed.dto.counter.CommentCountersKeysEvent;
import faang.school.postservice.news_feed.dto.counter.CommentLikeCounterKeysEvent;
import faang.school.postservice.news_feed.dto.counter.PostLikeCountersKeysEvent;
import faang.school.postservice.news_feed.dto.counter.PostViewCountersKeysEvent;
import faang.school.postservice.news_feed.dto.event.NewPostEvent;
import faang.school.postservice.news_feed.dto.event.UsersFeedUpdateEvent;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class EventsPartitioner {
    @Value("${app.post.feed.update.followers_partitions_limit}")
    private int followerPartitionsLimit;

    @Value("${app.post.feed.update.view_counter_partition_limit}")
    private int viewCountersPartitionLimit;

    @Value("${app.post.feed.update.like_counter_partition_limit}")
    private int likeCountersPartitionLimit;

    @Value("${app.post.feed.update.comment_counter_partition_limit}")
    private int commentCountersPartitionLimit;

    @Value("${app.post.feed.update.post_comment_likes_partition_limit}")
    private int commentLikesPartitionLimit;

    @Value("${app.post.feed.update.users_feed_update_partition_limit}")
    private int usersFeedUpdatePartitionLimit;

    public List<NewPostEvent> partitionSubscribersAndMapToMessage(Long postId, Long authorId, Long timestamp,
                                                                  List<Long> usersId) {
        return partitionAndMap(usersId, followerPartitionsLimit, group ->
                NewPostEvent.builder()
                        .postId(postId)
                        .authorId(authorId)
                        .createdAtTimestamp(timestamp)
                        .followersIds(group)
                        .build());
    }

    public List<PostViewCountersKeysEvent> partitionViewCounterKeysAndMapToMessage(List<String> keys) {
        return partitionAndMap(keys, viewCountersPartitionLimit, PostViewCountersKeysEvent::new);
    }

    public List<PostLikeCountersKeysEvent> partitionLikeCounterKeysAndMapToMessage(List<String> keys) {
        return partitionAndMap(keys, likeCountersPartitionLimit, PostLikeCountersKeysEvent::new);
    }

    public List<CommentCountersKeysEvent> partitionCommentCounterKeysAndMapToMessage(List<String> keys) {
        return partitionAndMap(keys, commentCountersPartitionLimit, CommentCountersKeysEvent::new);
    }

    public List<CommentLikeCounterKeysEvent> partitionCommentLikeCounterKeysAndMapToMessage(List<String> keys) {
        return partitionAndMap(keys, commentLikesPartitionLimit, CommentLikeCounterKeysEvent::new);
    }

    public List<UsersFeedUpdateEvent> partitionUserIdsAndMapToMessage(List<Long> ids) {
        return partitionAndMap(ids, usersFeedUpdatePartitionLimit, UsersFeedUpdateEvent::new);
    }

    private <T, K> List<T> partitionAndMap(List<K> list, int size, Function<List<K>, T> function) {
        List<List<K>> lists = ListUtils.partition(list, size);
        return lists.stream()
                .map(function)
                .toList();
    }
}
