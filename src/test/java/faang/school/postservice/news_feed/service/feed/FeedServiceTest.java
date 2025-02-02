package faang.school.postservice.news_feed.service.feed;

import faang.school.postservice.client.UserServiceClient;
import faang.school.postservice.dto.user.UserDto;
import faang.school.postservice.news_feed.dto.serializable.PostCache;
import faang.school.postservice.news_feed.repository.CommentCacheRepository;
import faang.school.postservice.news_feed.repository.PostCacheRepository;
import faang.school.postservice.news_feed.repository.UserCacheRepository;
import faang.school.postservice.news_feed.repository.ZSetRepository;
import faang.school.postservice.service.post.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executor;

import static faang.school.postservice.util.post.PostCacheFabric.buildDefaultPostDto;
import static faang.school.postservice.util.post.PostCacheFabric.buildDefaultUserDto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeedServiceTest {
    private static final String FEED_PREF = "feed/";
    private static final String POST_ID_PREF = "post_id:";
    private static final String USER_ID_PREF = "user_id:";
    private static final Long FEED_SIZE = 10L;
    private static final Long USER_ID = 1L;
    private static final Long OFFSET = 0L;
    private static final Long LIMIT = 10L;
    private static final long POST_ID = 1;
    private static final long AUTHOR_ID = 2;
    private static final long TIMESTAMP = 123456789;
    private static final List<Long> USER_IDS = List.of(1L, 2L, 3L);
    private static final Set<String> KEYS = Set.of("key:1", "key:2", "key:3");

    @Mock
    private CommentCacheRepository commentCacheRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private PostCacheRepository postCacheRepository;

    @Mock
    private UserCacheRepository userCacheRepository;

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private ZSetRepository zSetRepository;

    @Mock
    private PostService postService;

    @Mock
    private Executor executor;

    @InjectMocks
    private FeedService feedService;

    @BeforeEach
    void setUpd() {
        ReflectionTestUtils.setField(feedService, "feedUserIdPrefix", FEED_PREF);
        ReflectionTestUtils.setField(feedService, "postIdPrefix", POST_ID_PREF);
        ReflectionTestUtils.setField(feedService, "userIdPrefix", FEED_PREF);
        ReflectionTestUtils.setField(feedService, "userFeedSize", FEED_SIZE);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGetSetOfPosts_successful() {
        when(zSetRepository.getValuesInRange(anyString(), anyLong(), anyLong())).thenReturn(KEYS);

        feedService.getFeed(USER_ID, OFFSET, LIMIT);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Long> longCaptor = ArgumentCaptor.forClass(Long.class);

        verify(zSetRepository).getValuesInRange(keyCaptor.capture(), longCaptor.capture(), longCaptor.capture());

        assertThat(keyCaptor.getValue()).isEqualTo(FEED_PREF + USER_ID);
        assertThat(longCaptor.getAllValues().get(0)).isEqualTo(OFFSET);
        assertThat(longCaptor.getAllValues().get(1)).isEqualTo(LIMIT);

        ArgumentCaptor<Set<String>> keysCaptor = ArgumentCaptor.forClass(Set.class);

        verify(postCacheRepository).findAll(keysCaptor.capture());

        assertThat(keysCaptor.getValue()).isEqualTo(KEYS);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGetSetOfPosts_postIdsIsEmpty() {
        PostCache post = buildDefaultPostDto(POST_ID);
        List<PostCache> postDtoList = List.of(post);

        when(zSetRepository.getValuesInRange(anyString(), anyLong(), anyLong())).thenReturn(Set.of());
        when(postService.getSetOfPosts(anyLong(), anyLong(), anyLong())).thenReturn(postDtoList);

        feedService.getFeed(USER_ID, OFFSET, LIMIT);

        ArgumentCaptor<Long> longCaptor = ArgumentCaptor.forClass(Long.class);
        verify(postService).getSetOfPosts(longCaptor.capture(), longCaptor.capture(), longCaptor.capture());

        assertThat(longCaptor.getAllValues().get(0)).isEqualTo(USER_ID);
        assertThat(longCaptor.getAllValues().get(1)).isEqualTo(OFFSET);
        assertThat(longCaptor.getAllValues().get(2)).isEqualTo(LIMIT);

        ArgumentCaptor<Runnable> runCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(executor).execute(runCaptor.capture());

        runCaptor.getValue().run();

        ArgumentCaptor<Set<String>> keysCaptor = ArgumentCaptor.forClass(Set.class);
        verify(postCacheRepository).findAll(keysCaptor.capture());

        assertThat(keysCaptor.getValue()).isEqualTo(Set.of(POST_ID_PREF + post.getId()));
    }

    @Test
    void testFindPostsInCache_successful() {
        UserDto userDto = buildDefaultUserDto();
        PostCache post = buildDefaultPostDto(POST_ID);
        List<PostCache> postDtoList = List.of(post);

        when(postCacheRepository.findAll(KEYS)).thenReturn(postDtoList);
        when(userCacheRepository.findById(USER_ID)).thenReturn(Optional.of(userDto));
        when(commentCacheRepository.findAllByPostId(post.getId())).thenReturn(post.getComments());

        ReflectionTestUtils.invokeMethod(feedService, "findPostsInCache", KEYS);

        ArgumentCaptor<Long> longCaptor = ArgumentCaptor.forClass(Long.class);
        verify(userCacheRepository, times(2)).findById(longCaptor.capture());
        verify(commentCacheRepository).findAllByPostId(longCaptor.capture());

        verify(userServiceClient, never()).getUser(USER_ID);
    }

    @Test
    void testFindPostsInCache_postAuthorNotFoundInCache() {
        UserDto userDto = buildDefaultUserDto();
        PostCache post = buildDefaultPostDto(POST_ID);
        List<PostCache> postDtoList = List.of(post);

        when(postCacheRepository.findAll(KEYS)).thenReturn(postDtoList);
        when(userCacheRepository.findById(USER_ID))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(userDto));
        when(commentCacheRepository.findAllByPostId(post.getId())).thenReturn(post.getComments());
        when(userServiceClient.getUser(USER_ID)).thenReturn(userDto);

        ReflectionTestUtils.invokeMethod(feedService, "findPostsInCache", KEYS);

        ArgumentCaptor<Long> longCaptor = ArgumentCaptor.forClass(Long.class);
        verify(userCacheRepository, times(2)).findById(longCaptor.capture());
        verify(commentCacheRepository).findAllByPostId(longCaptor.capture());

        verify(userServiceClient).getUser(USER_ID);
        verify(userCacheRepository).save(userDto);
    }

    @Test
    void testFindPostsInCache_commentAuthorNotFoundInCache() {
        UserDto userDto = buildDefaultUserDto();
        PostCache post = buildDefaultPostDto(POST_ID);
        List<PostCache> postDtoList = List.of(post);

        when(postCacheRepository.findAll(KEYS)).thenReturn(postDtoList);
        when(userCacheRepository.findById(USER_ID))
                .thenReturn(Optional.of(userDto))
                .thenReturn(Optional.empty());
        when(commentCacheRepository.findAllByPostId(post.getId())).thenReturn(post.getComments());
        when(userServiceClient.getUser(USER_ID)).thenReturn(userDto);

        ReflectionTestUtils.invokeMethod(feedService, "findPostsInCache", KEYS);

        ArgumentCaptor<Long> longCaptor = ArgumentCaptor.forClass(Long.class);
        verify(userCacheRepository, times(2)).findById(longCaptor.capture());
        verify(commentCacheRepository).findAllByPostId(longCaptor.capture());

        verify(userServiceClient).getUser(USER_ID);
        verify(userCacheRepository).save(userDto);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testEnrichAuthors_successful() {
        UserDto userDto = buildDefaultUserDto();
        PostCache post = buildDefaultPostDto(POST_ID);
        List<PostCache> postDtoList = List.of(post);

        ReflectionTestUtils.invokeMethod(feedService, "enrichAuthors", postDtoList);

        verify(postCacheRepository).saveAll(anyList());

        ArgumentCaptor<Set<String>> keysCaptor = ArgumentCaptor.forClass(Set.class);
        verify(postCacheRepository).findAll(keysCaptor.capture());

        assertThat(keysCaptor.getValue()).isEqualTo(Set.of(POST_ID_PREF + post.getId()));
    }

    @SuppressWarnings("unchecked")
    @Test
    void testUpdatePostsCommentsAuthorInCache() {
        UserDto userDto = buildDefaultUserDto();
        PostCache post = buildDefaultPostDto(POST_ID);
        List<PostCache> postDtoList = List.of(post);

        when(redisTemplate.hasKey(anyString())).thenReturn(false);
        when(userServiceClient.getUsersByIds(anyList())).thenReturn(List.of(userDto));

        ReflectionTestUtils.invokeMethod(feedService, "updatePostsCommentsAuthorInCache", postDtoList);

        ArgumentCaptor<List<PostCache>> postDtoListCaptor = ArgumentCaptor.forClass(List.class);
        verify(postCacheRepository).saveAll(postDtoListCaptor.capture());
        assertThat(postDtoListCaptor.getValue()).isEqualTo(postDtoList);

        ArgumentCaptor<List<UserDto>> userDtoListCaptor = ArgumentCaptor.forClass(List.class);
        verify(userCacheRepository).saveAll(userDtoListCaptor.capture());
        assertThat(userDtoListCaptor.getValue()).isEqualTo(List.of(userDto));
    }
}