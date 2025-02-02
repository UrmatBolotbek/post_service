package faang.school.postservice.news_feed.kafka.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import faang.school.postservice.news_feed.dto.counter.CommentCountersKeysEvent;
import faang.school.postservice.news_feed.dto.counter.CommentLikeCounterKeysEvent;
import faang.school.postservice.news_feed.dto.counter.PostLikeCountersKeysEvent;
import faang.school.postservice.news_feed.dto.counter.PostViewCountersKeysEvent;
import faang.school.postservice.news_feed.dto.event.CommentLikeEvent;
import faang.school.postservice.news_feed.dto.event.NewCommentEvent;
import faang.school.postservice.news_feed.dto.event.NewPostEvent;
import faang.school.postservice.news_feed.dto.event.PostLikeEvent;
import faang.school.postservice.news_feed.dto.event.PostViewEvent;
import faang.school.postservice.news_feed.dto.event.UsersFeedUpdateEvent;
import faang.school.postservice.news_feed.service.cache.CacheUpdateService;
import faang.school.postservice.news_feed.service.feed.FeedHeaterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaListeners {
    private final CacheUpdateService cacheUpdateService;
    private final FeedHeaterService feedHeaterService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${spring.kafka.topic.post.new}", groupId = "${spring.kafka.consumer.group-id}")
    public void newPost(String message) {
        NewPostEvent newPostEvent = readMessage(message, NewPostEvent.class);
        cacheUpdateService.partitionSubscribersAndPublish(newPostEvent);
    }

    @KafkaListener(topics = "${spring.kafka.topic.post.update_feeds}", groupId = "${spring.kafka.consumer.group-id}")
    public void updateSubscribersFeeds(String message) {
        NewPostEvent newPostEvent = readMessage(message, NewPostEvent.class);
        cacheUpdateService.usersFeedUpdate(newPostEvent.getPostId(), newPostEvent.getCreatedAtTimestamp(),
                newPostEvent.getFollowersIds());
    }

    @KafkaListener(topics = "${spring.kafka.topic.post.view}", groupId = "${spring.kafka.consumer.group-id}")
    public void viewPost(String message) {
        PostViewEvent postViewEvent = readMessage(message, PostViewEvent.class);
        cacheUpdateService.postsViewsIncrByIds(postViewEvent.getPostsIds());
    }

    @KafkaListener(topics = "${spring.kafka.topic.post.update_views}", groupId = "${spring.kafka.consumer.group-id}")
    public void postViewsUpdate(String message) {
        PostViewCountersKeysEvent viewsEvent = readMessage(message, PostViewCountersKeysEvent.class);
        cacheUpdateService.postViewsUpdate(viewsEvent.getViewCountersKeys());
    }

    @KafkaListener(topics = "${spring.kafka.topic.post.like}", groupId = "${spring.kafka.consumer.group-id}")
    public void likePost(String message) {
        PostLikeEvent postLikeEvent = readMessage(message, PostLikeEvent.class);
        cacheUpdateService.postLikesIncrById(postLikeEvent.getPostId());
    }

    @KafkaListener(topics = "${spring.kafka.topic.post.update_likes}", groupId = "${spring.kafka.consumer.group-id}")
    public void postLikesUpdate(String message) {
        PostLikeCountersKeysEvent likesEvent = readMessage(message, PostLikeCountersKeysEvent.class);
        cacheUpdateService.postLikesUpdate(likesEvent.getLikeCountersKeys());
    }

    @KafkaListener(topics = "${spring.kafka.topic.post.comment}", groupId = "${spring.kafka.consumer.group-id}")
    public void commentPost(String message) {
        NewCommentEvent commentPostEvent = readMessage(message, NewCommentEvent.class);
        cacheUpdateService.commentsCounterIncrById(commentPostEvent.getPostId());
    }

    @KafkaListener(topics = "${spring.kafka.topic.post.update_comments}", groupId = "${spring.kafka.consumer.group-id}")
    public void commentsUpdate(String message) {
        CommentCountersKeysEvent likesEvent = readMessage(message, CommentCountersKeysEvent.class);
        cacheUpdateService.commentsUpdate(likesEvent.getCommentCountersKeys());
    }

    @KafkaListener(topics = "${spring.kafka.topic.post.like_post_comment}", groupId = "${spring.kafka.consumer.group-id}")
    public void likeComment(String message) {
        CommentLikeEvent commentLikeEvent = readMessage(message, CommentLikeEvent.class);
        cacheUpdateService.commentLikesIncrById(commentLikeEvent.getCommentId());
    }

    @KafkaListener(topics = "${spring.kafka.topic.post.update_post_comments_likes}", groupId = "${spring.kafka.consumer.group-id}")
    public void commentLikesUpdate(String message) {
        CommentLikeCounterKeysEvent commentLikesEvent = readMessage(message, CommentLikeCounterKeysEvent.class);
        cacheUpdateService.commentLikesUpdate(commentLikesEvent.getCommentLikeCounterKeys());
    }

    @KafkaListener(topics = "${spring.kafka.topic.user.feed_update}", groupId = "${spring.kafka.consumer.group-id}")
    public void usersFeedUpdate(String message) {
        UsersFeedUpdateEvent usersFeedUpdateEvent = readMessage(message, UsersFeedUpdateEvent.class);
        feedHeaterService.updateUsersFeeds(usersFeedUpdateEvent.getUserIds());
    }

    private <T> T readMessage(String message, Class<T> clazz) {
        try {
            return objectMapper.readValue(message, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
