package faang.school.postservice.news_feed.service.feed;

import faang.school.postservice.client.UserServiceClient;
import faang.school.postservice.dto.user.UserDto;
import faang.school.postservice.news_feed.dto.serializable.CommentCache;
import faang.school.postservice.news_feed.dto.serializable.PostCache;
import faang.school.postservice.news_feed.repository.CommentCacheRepository;
import faang.school.postservice.news_feed.repository.PostCacheRepository;
import faang.school.postservice.news_feed.repository.UserCacheRepository;
import faang.school.postservice.news_feed.repository.key.PostKey;
import faang.school.postservice.service.post.PostService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executor;

import static faang.school.postservice.util.post.PostCacheFabric.buildDefaultPostDto;
import static faang.school.postservice.util.post.PostCacheFabric.buildDefaultUserDto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedServiceTest {

    private static final Long USER_ID = 1L;
    private static final Long OFFSET = 0L;
    private static final Long LIMIT = 10L;
    private static final long POST_ID = 1L;
    private static final String CACHE_KEY = "post_id:1";

    @Mock
    private CommentCacheRepository commentCacheRepository;

    @Mock
    private PostCacheRepository postCacheRepository;

    @Mock
    private UserCacheRepository userCacheRepository;

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private PostService postService;

    @Mock
    private Executor usersFeedsUpdatePool;

    @Mock
    private FeedHeaterService feedHeaterService;

    @Mock
    private PostKey postKey;

    @InjectMocks
    private FeedService feedService;

    @Test
    void testGetFeed_withCacheHit() {
        Set<String> keys = Set.of(CACHE_KEY);
        PostCache post = buildDefaultPostDto(POST_ID);
        UserDto userDto = buildDefaultUserDto();

        when(userCacheRepository.findPostIdsInUserFeed(USER_ID, OFFSET, LIMIT)).thenReturn(keys);
        when(postCacheRepository.findByKey(CACHE_KEY)).thenReturn(Optional.of(post));
        when(userCacheRepository.findById(post.getAuthorId())).thenReturn(Optional.of(userDto));
        when(commentCacheRepository.findAllByPostId(post.getId())).thenReturn(post.getComments());

        List<PostCache> result = feedService.getFeed(USER_ID, OFFSET, LIMIT);

        assertThat(result).hasSize(1);
        verify(userCacheRepository).findPostIdsInUserFeed(USER_ID, OFFSET, LIMIT);
        verify(postCacheRepository).findByKey(CACHE_KEY);
        verify(usersFeedsUpdatePool, never()).execute(any(Runnable.class));
    }

    @Test
    void testGetFeed_withEmptyCache() {
        Set<String> emptyKeys = Set.of();
        PostCache post = buildDefaultPostDto(POST_ID);
        List<PostCache> postsFromService = List.of(post);
        UserDto userDto = buildDefaultUserDto();

        when(userCacheRepository.findPostIdsInUserFeed(USER_ID, OFFSET, LIMIT)).thenReturn(emptyKeys);
        when(postService.getSetOfPosts(USER_ID, OFFSET, LIMIT)).thenReturn(postsFromService);
        when(userCacheRepository.findById(post.getAuthorId())).thenReturn(Optional.of(userDto));
        when(commentCacheRepository.findAllByPostId(post.getId())).thenReturn(post.getComments());

        List<PostCache> result = feedService.getFeed(USER_ID, OFFSET, LIMIT);

        assertThat(result).isEqualTo(postsFromService);
        verify(postService).getSetOfPosts(USER_ID, OFFSET, LIMIT);

        ArgumentCaptor<Runnable> runCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(usersFeedsUpdatePool).execute(runCaptor.capture());
        runCaptor.getValue().run();
        verify(feedHeaterService).updateUserFeed(USER_ID);
    }

    @Test
    void testFindPostsInCache_successful() {
        Set<String> keys = Set.of(CACHE_KEY);
        UserDto userDto = buildDefaultUserDto();
        PostCache post = buildDefaultPostDto(POST_ID);

        when(postCacheRepository.findByKey(CACHE_KEY)).thenReturn(Optional.of(post));
        when(userCacheRepository.findById(post.getAuthorId())).thenReturn(Optional.of(userDto));
        when(commentCacheRepository.findAllByPostId(post.getId())).thenReturn(post.getComments());

        List<PostCache> result = ReflectionTestUtils.invokeMethod(feedService, "findPostsInCache", keys);

        assertThat(result).hasSize(1);
        verify(userCacheRepository, times(2)).findById(post.getAuthorId());
        verify(commentCacheRepository).findAllByPostId(post.getId());
        verify(userServiceClient, never()).getUser(anyLong());
    }

    @Test
    void testFindPostsInCache_postAuthorNotFoundInCache() {
        Set<String> keys = Set.of(CACHE_KEY);
        UserDto userDto = buildDefaultUserDto();
        PostCache post = buildDefaultPostDto(POST_ID);

        when(postCacheRepository.findByKey(CACHE_KEY)).thenReturn(Optional.of(post));
        when(userCacheRepository.findById(post.getAuthorId()))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(userDto));
        when(commentCacheRepository.findAllByPostId(post.getId())).thenReturn(post.getComments());
        when(userServiceClient.getUser(post.getAuthorId())).thenReturn(userDto);

        ReflectionTestUtils.invokeMethod(feedService, "findPostsInCache", keys);

        verify(userCacheRepository, times(2)).findById(post.getAuthorId());
        verify(commentCacheRepository).findAllByPostId(post.getId());
        verify(userServiceClient).getUser(post.getAuthorId());
        verify(userCacheRepository).save(userDto);
    }

    @Test
    void testFindPostsInCache_commentAuthorNotFoundInCache() {
        Set<String> keys = Set.of(CACHE_KEY);
        UserDto userDto = buildDefaultUserDto();
        PostCache post = buildDefaultPostDto(POST_ID);
        CommentCache comment = post.getComments().get(0);
        Long commentAuthorId = comment.getAuthorId();

        when(postCacheRepository.findByKey(CACHE_KEY)).thenReturn(Optional.of(post));
        when(userCacheRepository.findById(post.getAuthorId())).thenReturn(Optional.of(userDto));
        when(userCacheRepository.findById(commentAuthorId))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(userDto));
        when(commentCacheRepository.findAllByPostId(post.getId())).thenReturn(post.getComments());
        when(userServiceClient.getUser(commentAuthorId)).thenReturn(userDto);

        ReflectionTestUtils.invokeMethod(feedService, "findPostsInCache", keys);

        verify(userCacheRepository, times(2)).findById(commentAuthorId);
        verify(commentCacheRepository).findAllByPostId(post.getId());
        verify(userServiceClient).getUser(commentAuthorId);
        verify(userCacheRepository).save(userDto);
    }

    @Test
    void testSetAuthors_successful() {
        UserDto userDto = buildDefaultUserDto();
        PostCache post = buildDefaultPostDto(POST_ID);
        if (post.getComments().isEmpty()) {
            CommentCache dummyComment = new CommentCache();
            dummyComment.setAuthorId(2L);
            post.getComments().add(dummyComment);
        }
        List<PostCache> posts = List.of(post);

        when(userCacheRepository.findById(post.getAuthorId())).thenReturn(Optional.of(userDto));
        when(commentCacheRepository.findAllByPostId(post.getId())).thenReturn(post.getComments());
        for (CommentCache comment : post.getComments()) {
            when(userCacheRepository.findById(comment.getAuthorId())).thenReturn(Optional.of(userDto));
        }

        ReflectionTestUtils.invokeMethod(feedService, "setAuthors", posts);

        assertThat(post.getAuthorDto()).isEqualTo(userDto);
        for (CommentCache comment : post.getComments()) {
            assertThat(comment.getAuthorDto()).isEqualTo(userDto);
        }
    }
}
