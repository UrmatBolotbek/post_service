package faang.school.postservice.news_feed.service.feed.util;

import faang.school.postservice.news_feed.dto.counter.CommentCountersKeysEvent;
import faang.school.postservice.news_feed.dto.counter.CommentLikeCounterKeysEvent;
import faang.school.postservice.news_feed.dto.counter.PostLikeCountersKeysEvent;
import faang.school.postservice.news_feed.dto.counter.PostViewCountersKeysEvent;
import faang.school.postservice.news_feed.dto.event.NewPostEvent;
import faang.school.postservice.news_feed.dto.event.UsersFeedUpdateEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class EventsPartitionerTest {
    private static final int LIMIT = 10;
    private static final long POST_ID = 1;
    private static final long AUTHOR_ID = 2;
    private static final long TIMESTAMP = 123456789;
    private static final List<Long> USER_IDS = List.of(1L, 2L, 3L);
    private static final List<String> KEYS = List.of("key:1", "key:2", "key:3");

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(eventsPartitioner, "followerPartitionsLimit", LIMIT);
        ReflectionTestUtils.setField(eventsPartitioner, "viewCountersPartitionLimit", LIMIT);
        ReflectionTestUtils.setField(eventsPartitioner, "likeCountersPartitionLimit", LIMIT);
        ReflectionTestUtils.setField(eventsPartitioner, "commentCountersPartitionLimit", LIMIT);
        ReflectionTestUtils.setField(eventsPartitioner, "commentLikesPartitionLimit", LIMIT);
        ReflectionTestUtils.setField(eventsPartitioner, "usersFeedUpdatePartitionLimit", LIMIT);
    }

    @InjectMocks
    private EventsPartitioner eventsPartitioner;

    @Test
    void test_partitionSubscribersAndMapToMessage_successful() {
        NewPostEvent newPostEvent = NewPostEvent.builder()
                .postId(POST_ID)
                .authorId(AUTHOR_ID)
                .createdAtTimestamp(TIMESTAMP)
                .followersIds(USER_IDS)
                .build();

        assertThat(eventsPartitioner.partitionSubscribersAndMapToMessage(POST_ID, AUTHOR_ID, TIMESTAMP, USER_IDS))
                .isEqualTo(List.of(newPostEvent));
    }

    @Test
    void test_partitionViewCounterKeysAndMapToMessage_successful() {
        PostViewCountersKeysEvent postViewCountersKeysEvent = new PostViewCountersKeysEvent(KEYS);

        assertThat(eventsPartitioner.partitionViewCounterKeysAndMapToMessage(KEYS))
                .isEqualTo(List.of(postViewCountersKeysEvent));
    }

    @Test
    void test_partitionLikeCounterKeysAndMapToMessage_successful() {
        PostLikeCountersKeysEvent event = new PostLikeCountersKeysEvent(KEYS);

        assertThat(eventsPartitioner.partitionLikeCounterKeysAndMapToMessage(KEYS))
                .isEqualTo(List.of(event));
    }

    @Test
    void test_partitionCommentCounterKeysAndMapToMessage_successful() {
        CommentCountersKeysEvent event = new CommentCountersKeysEvent(KEYS);

        assertThat(eventsPartitioner.partitionCommentCounterKeysAndMapToMessage(KEYS))
                .isEqualTo(List.of(event));
    }

    @Test
    void test_partitionCommentLikeCounterKeysAndMapToMessage_successful() {
        CommentLikeCounterKeysEvent event = new CommentLikeCounterKeysEvent(KEYS);

        assertThat(eventsPartitioner.partitionCommentLikeCounterKeysAndMapToMessage(KEYS))
                .isEqualTo(List.of(event));
    }

    @Test
    void test_partitionUserIdsAndMapToMessage_successful() {
        UsersFeedUpdateEvent event = new UsersFeedUpdateEvent(USER_IDS);

        assertThat(eventsPartitioner.partitionUserIdsAndMapToMessage(USER_IDS))
                .isEqualTo(List.of(event));
    }
}