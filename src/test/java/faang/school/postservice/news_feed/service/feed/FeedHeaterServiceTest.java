package faang.school.postservice.news_feed.service.feed;

import faang.school.postservice.client.UserServiceClient;
import faang.school.postservice.dto.user.UserDto;
import faang.school.postservice.news_feed.dto.event.UsersFeedUpdateEvent;
import faang.school.postservice.news_feed.dto.serializable.PostCache;
import faang.school.postservice.news_feed.kafka.publisher.simple.UserIdsToFeedUpdateToKafkaPublisher;
import faang.school.postservice.news_feed.repository.CommentCacheRepository;
import faang.school.postservice.news_feed.repository.PostCacheRepository;
import faang.school.postservice.news_feed.repository.UserCacheRepository;
import faang.school.postservice.news_feed.service.feed.util.EventsPartitioner;
import faang.school.postservice.service.post.PostService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FeedHeaterServiceTest {

    @Mock
    private UserIdsToFeedUpdateToKafkaPublisher userIdsPublisher;
    @Mock
    private CommentCacheRepository commentCacheRepository;
    @Mock
    private PostCacheRepository postCacheRepository;
    @Mock
    private UserCacheRepository userCacheRepository;
    @Mock
    private UserServiceClient userServiceClient;
    @Mock
    private EventsPartitioner partitioner;
    @Mock
    private Executor usersFeedsUpdatePool;
    @Mock
    private PostService postService;
    @InjectMocks
    private FeedHeaterService feedHeaterService;

    @Test
    void heatUsersFeeds_shouldPublishMessages() {
        List<Long> userIds = Arrays.asList(1L, 2L, 3L);
        when(userServiceClient.getAllIds()).thenReturn(userIds);

        UsersFeedUpdateEvent event1 = UsersFeedUpdateEvent.builder()
                .userIds(Arrays.asList(1L, 2L))
                .build();
        UsersFeedUpdateEvent event2 = UsersFeedUpdateEvent.builder()
                .userIds(List.of(3L))
                .build();
        List<UsersFeedUpdateEvent> events = Arrays.asList(event1, event2);

        when(partitioner.partitionUserIdsAndMapToMessage(userIds)).thenReturn(events);

        feedHeaterService.heatUsersFeeds();

        verify(userIdsPublisher, times(2)).publish(any(UsersFeedUpdateEvent.class));

        ArgumentCaptor<UsersFeedUpdateEvent> captor = ArgumentCaptor.forClass(UsersFeedUpdateEvent.class);
        verify(userIdsPublisher, times(2)).publish(captor.capture());
        List<UsersFeedUpdateEvent> capturedEvents = captor.getAllValues();

        assertEquals(2, capturedEvents.size());
        assertEquals(event1, capturedEvents.get(0));
        assertEquals(event2, capturedEvents.get(1));
        verify(userServiceClient).getAllIds();
        verify(partitioner).partitionUserIdsAndMapToMessage(userIds);
    }

    @Test
    void updateUsersFeeds_shouldExecuteUpdateUserFeedForEachUser() {
        List<Long> userIds = Arrays.asList(1L, 2L);
        doAnswer(invocation -> {
            Runnable r = invocation.getArgument(0);
            r.run();
            return null;
        }).when(usersFeedsUpdatePool).execute(any(Runnable.class));
        PostCache postCache = PostCache.builder().id(100L).build();
        List<PostCache> postCacheList = List.of(postCache);
        when(postService.getSetOfPosts(eq(1L), eq(0L), any(Long.class))).thenReturn(postCacheList);
        when(postService.getSetOfPosts(eq(2L), eq(0L), any(Long.class))).thenReturn(postCacheList);
        when(postCacheRepository.filterPostsOnWithoutCache(postCacheList)).thenReturn(postCacheList);
        when(postCacheRepository.mapPostDtoListToAuthorsIds(postCacheList)).thenReturn(Set.of(10L, 20L));
        when(userCacheRepository.filterUserIdsOnWithoutCache(any(Set.class))).thenReturn(List.of(10L));
        when(userServiceClient.getUsersByIds(anyList())).thenReturn(List.of(new UserDto(10L, "TestUser", "test@example.com", "123", 5)));
        feedHeaterService.updateUsersFeeds(userIds);
        verify(usersFeedsUpdatePool, times(userIds.size())).execute(any(Runnable.class));
    }

    @Test
    void updateUserFeed_shouldUpdateUserFeedProperly() {
        long userId = 1L;
        PostCache postCache = PostCache.builder().id(100L).comments(List.of()).build();
        List<PostCache> postCacheList = List.of(postCache);
        when(postService.getSetOfPosts(eq(userId), eq(0L), any(Long.class))).thenReturn(postCacheList);
        when(postCacheRepository.filterPostsOnWithoutCache(postCacheList)).thenReturn(postCacheList);
        when(postCacheRepository.mapPostDtoListToAuthorsIds(postCacheList)).thenReturn(Set.of(10L, 20L));
        when(userCacheRepository.filterUserIdsOnWithoutCache(any(Set.class))).thenReturn(List.of(10L));
        List<UserDto> userDtos = List.of(new UserDto(10L, "TestUser", "test@example.com", "123", 5));
        when(userServiceClient.getUsersByIds(anyList())).thenReturn(userDtos);
        feedHeaterService.updateUserFeed(userId);
        verify(userCacheRepository).mapAndSavePostIdsToFeed(userId, postCacheList);
        verify(postCacheRepository).saveAll(postCacheList);
        verify(commentCacheRepository, times(postCacheList.size())).saveAll(anyList());
        verify(userCacheRepository).saveAll(userDtos);
    }
}
