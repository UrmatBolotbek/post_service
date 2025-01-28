package faang.school.postservice.service.post;

import faang.school.postservice.config.api.SpellingConfig;
import faang.school.postservice.dto.post.PostFilterDto;
import faang.school.postservice.dto.post.PostRequestDto;
import faang.school.postservice.dto.post.PostResponseDto;
import faang.school.postservice.dto.post.PostUpdateDto;
import faang.school.postservice.kafka.EventsManager;
import faang.school.postservice.mapper.post.PostMapper;
import faang.school.postservice.mapper.resource.ResourceMapper;
import faang.school.postservice.model.Post;
import faang.school.postservice.repository.PostRepository;
import faang.school.postservice.service.news_feed.PostCacheService;
import faang.school.postservice.service.post.filter.PostFilters;
import faang.school.postservice.service.resource.ResourceService;
import faang.school.postservice.service.s3.S3Service;
import faang.school.postservice.util.ModerationDictionary;
import faang.school.postservice.validator.post.PostValidator;
import faang.school.postservice.validator.resource.ResourceValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PostServiceTest {

    @Mock
    private PostRepository postRepository;
    @Mock
    private PostMapper postMapper;
    @Mock
    private PostValidator postValidator;
    @Mock
    private RestTemplate restTemplate;
    @Mock
    private ExecutorService spellingExecutor;
    @Mock
    private SpellingConfig api;
    @Mock
    private ResourceValidator resourceValidator;
    @Mock
    private ResourceService resourceService;
    @Mock
    private List<PostFilters> postFilters;
    @Mock
    private ModerationDictionary moderationDictionary;
    @Mock
    private EventsManager eventsManager;
    @Mock
    private PostCacheService postCacheService;
    @Mock
    private ResourceMapper resourceMapper;
    @Mock
    private S3Service s3Service;

    @InjectMocks
    private PostService postService;

    private Post post;

    @BeforeEach
    void setUp() {
        post = new Post();
        post.setId(1L);
        post.setContent("This is errror");
        post.setPublished(false);
        post.setDeleted(false);
        post.setCreatedAt(LocalDateTime.now().plusDays(1));
        post.setResources(new ArrayList<>());
    }

    @Test
    void testCreatePost() {
        PostRequestDto requestDto = PostRequestDto.builder()
                .content("Hello")
                .build();

        Post mappedPost = new Post();
        mappedPost.setId(10L);
        mappedPost.setContent("Hello mapped");
        mappedPost.setResources(new ArrayList<>());

        when(postMapper.toEntity(requestDto)).thenReturn(mappedPost);
        when(postRepository.save(any(Post.class))).thenReturn(mappedPost);

        PostResponseDto mappedDto = new PostResponseDto();
        mappedDto.setId(10L);
        mappedDto.setContent("Hello mapped");
        when(postMapper.toDto(mappedPost)).thenReturn(mappedDto);

        List<MultipartFile> images = List.of(mock(MultipartFile.class));
        List<MultipartFile> audio = List.of(mock(MultipartFile.class));

        PostResponseDto result = postService.create(requestDto, images, audio);

        assertEquals(10L, result.getId());
        assertEquals("Hello mapped", result.getContent());
        verify(postValidator).validateCreate(requestDto);
        verify(resourceService).uploadResources(eq(images), eq("image"), eq(mappedPost));
        verify(resourceService).uploadResources(eq(audio), eq("audio"), eq(mappedPost));
        verify(postRepository, times(2)).save(mappedPost);
        verify(postMapper).toDto(mappedPost);
    }

    @Test
    void testUpdatePost() {
        Long postId = 5L;
        PostUpdateDto updateDto = PostUpdateDto.builder()
                .content("Updated")
                .imageFilesIdsToDelete(List.of(1L))
                .audioFilesIdsToDelete(List.of(2L))
                .build();

        List<MultipartFile> images = List.of(mock(MultipartFile.class));
        List<MultipartFile> audio = List.of(mock(MultipartFile.class));

        Post foundPost = new Post();
        foundPost.setId(postId);
        foundPost.setContent("Old content");
        foundPost.setResources(new ArrayList<>());

        when(postRepository.getPostById(postId)).thenReturn(foundPost);
        when(postRepository.save(foundPost)).thenReturn(foundPost);

        PostResponseDto mappedDto = new PostResponseDto();
        mappedDto.setId(postId);
        mappedDto.setContent("Updated");
        when(postMapper.toDto(foundPost)).thenReturn(mappedDto);

        PostResponseDto result = postService.updatePost(postId, updateDto, images, audio);

        assertEquals(postId, result.getId());
        assertEquals("Updated", result.getContent());
        verify(postRepository).getPostById(postId);
        verify(resourceService).deleteResources(List.of(1L));
        verify(resourceService).deleteResources(List.of(2L));
        verify(resourceService).uploadResources(eq(images), eq("image"), eq(foundPost));
        verify(resourceService).uploadResources(eq(audio), eq("audio"), eq(foundPost));
        verify(resourceValidator).validateResourceCounts(foundPost);
        verify(postRepository).save(foundPost);
        verify(postMapper).toDto(foundPost);
    }

    @Test
    void testGetPost_CacheHit() {
        Long postId = 1L;
        PostResponseDto cachedDto = new PostResponseDto();
        cachedDto.setId(postId);
        cachedDto.setContent("Cached content");

        when(postCacheService.getPostFromCacheOrDb(postId)).thenReturn(cachedDto);

        PostResponseDto result = postService.getPost(postId);
        assertEquals(postId, result.getId());
        assertEquals("Cached content", result.getContent());

        verify(postCacheService).getPostFromCacheOrDb(postId);
        verify(postCacheService).incrementViews(postId);
        verifyNoInteractions(postRepository);
    }

    @Test
    void testGetPost_CacheMiss() {
        Long postId = 2L;
        when(postCacheService.getPostFromCacheOrDb(postId)).thenReturn(null);

        Post dbPost = new Post();
        dbPost.setId(postId);
        dbPost.setContent("DB content");
        dbPost.setResources(new ArrayList<>());
        when(postRepository.getPostById(postId)).thenReturn(dbPost);

        PostResponseDto mappedDto = new PostResponseDto();
        mappedDto.setId(postId);
        mappedDto.setContent("DB content");
        when(postMapper.toDto(dbPost)).thenReturn(mappedDto);

        PostResponseDto result = postService.getPost(postId);
        assertEquals(postId, result.getId());
        assertEquals("DB content", result.getContent());

        verify(postCacheService).getPostFromCacheOrDb(postId);
        verify(postRepository).getPostById(postId);
        verify(postMapper).toDto(dbPost);
        verify(postCacheService).savePostCache(mappedDto);
        verify(postCacheService).incrementViews(postId);
    }

    @Test
    void testPublishPost() {
        Long postId = 1L;
        Post postEntity = new Post();
        postEntity.setId(postId);
        postEntity.setPublished(false);

        Post saved = new Post();
        saved.setId(postId);
        saved.setPublished(true);

        when(postValidator.validateAndGetPostById(postId)).thenReturn(postEntity);
        doNothing().when(postValidator).validatePublish(any(Post.class));
        when(postRepository.save(postEntity)).thenReturn(saved);

        PostResponseDto mappedDto = new PostResponseDto();
        mappedDto.setId(postId);
        mappedDto.setAuthorId(999L);
        mappedDto.setContent("Published");
        when(postMapper.toDto(saved)).thenReturn(mappedDto);

        PostResponseDto result = postService.publishPost(postId);

        assertTrue(saved.isPublished());
        assertEquals(postId, result.getId());
        assertEquals("Published", result.getContent());

        verify(postValidator).validatePublish(postEntity);
        verify(postRepository).save(postEntity);
        verify(postMapper).toDto(saved);
        verify(eventsManager).generateAndSendAuthorCachedEvent(999L);
        verify(postCacheService).savePostCache(mappedDto);
        verify(eventsManager).generateAndSendPostCachedEvent(mappedDto);
    }

    @Test
    void testDeletePost() {
        Long postId = 55L;
        Post p = new Post();
        p.setId(postId);
        p.setPublished(true);
        p.setDeleted(false);

        Post afterSave = new Post();
        afterSave.setId(postId);
        afterSave.setPublished(false);
        afterSave.setDeleted(true);

        when(postRepository.findById(postId)).thenReturn(Optional.of(p));
        when(postRepository.save(p)).thenReturn(afterSave);

        postService.deletePost(postId);

        assertTrue(afterSave.isDeleted());
        verify(postValidator).validateDelete(p);
        verify(postRepository).findById(postId);
        verify(postRepository).save(p);
    }

    @Test
    void testGetPostById() {
        Long postId = 77L;
        Post p = new Post();
        p.setId(postId);
        p.setContent("Post content");
        p.setResources(new ArrayList<>());
        when(postRepository.findById(postId)).thenReturn(Optional.of(p));

        PostResponseDto mapped = new PostResponseDto();
        mapped.setId(postId);
        mapped.setContent("Post content");
        when(postMapper.toDto(p)).thenReturn(mapped);

        PostResponseDto result = postService.getPostById(postId);
        assertEquals(postId, result.getId());
        assertEquals("Post content", result.getContent());

        verify(postRepository).findById(postId);
        verify(postMapper).toDto(p);
    }

    @Test
    void testCheckSpellingSuccess() throws InterruptedException {
        doAnswer(invocation -> {
            Runnable r = invocation.getArgument(0);
            r.run();
            return null;
        }).when(spellingExecutor).execute(any(Runnable.class));

        List<Post> posts = new ArrayList<>();
        posts.add(post);

        when(postRepository.findByPublishedFalse()).thenReturn(posts);
        when(api.getKey()).thenReturn("apiKey");
        when(api.getEndpoint()).thenReturn("endpoint");
        String responseJson = "{\"elements\":[{\"id\":0,\"errors\":[{\"suggestions\":[\"error\"],\"word\":\"errror\"}]}],\"spellingErrorCount\":1}";
        when(restTemplate.postForObject(eq("endpoint"), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseJson);

        postService.checkSpelling();

        verify(postRepository).findByPublishedFalse();
        verify(restTemplate).postForObject(eq("endpoint"), any(HttpEntity.class), eq(String.class));
        verify(postRepository).save(any(Post.class));
        assertEquals("This is error", post.getContent());
    }

    @Test
    void testGetPosts() {
        PostFilterDto filterDto = new PostFilterDto();
        when(postRepository.findAll()).thenReturn(List.of(post));

        PostFilters mockFilter = mock(PostFilters.class);
        when(postFilters.stream()).thenReturn(Stream.of(mockFilter));
        when(mockFilter.isApplicable(filterDto)).thenReturn(true);
        when(mockFilter.apply(any(), eq(filterDto))).thenAnswer(i -> i.getArgument(0));

        PostResponseDto mappedDto = new PostResponseDto();
        mappedDto.setId(100L);
        when(postMapper.toListPostDto(anyList())).thenReturn(List.of(mappedDto));

        List<PostResponseDto> result = postService.getPosts(filterDto);
        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).getId());

        verify(postRepository).findAll();
        verify(postFilters).stream();
        verify(mockFilter).isApplicable(filterDto);
        verify(mockFilter).apply(any(), eq(filterDto));
        verify(postMapper).toListPostDto(anyList());
    }

    @Test
    void testVerifyPostsForModeration() {
        Post p1 = new Post();
        p1.setId(1L);
        p1.setContent("Text1");
        p1.setResources(new ArrayList<>());
        Post p2 = new Post();
        p2.setId(2L);
        p2.setContent("Text2");
        p2.setResources(new ArrayList<>());

        List<Post> posts = List.of(p1, p2);
        when(moderationDictionary.isVerified("Text1")).thenReturn(true);
        when(moderationDictionary.isVerified("Text2")).thenReturn(false);

        postService.verifyPostsForModeration(posts);

        assertTrue(p1.getVerified());
        assertFalse(p2.getVerified());
        verify(postRepository, times(2)).save(any(Post.class));
    }

    @Test
    void testCheckSpellingHttpClientErrorException() {
        when(postRepository.findByPublishedFalse()).thenReturn(List.of(post));
        when(api.getKey()).thenReturn("apiKey");
        when(api.getEndpoint()).thenReturn("endpoint");

        HttpClientErrorException httpException =
                new HttpClientErrorException(org.springframework.http.HttpStatus.BAD_REQUEST, "Bad Request");
        when(restTemplate.postForObject(eq("endpoint"), any(HttpEntity.class), eq(String.class)))
                .thenThrow(httpException);

        assertThrows(HttpClientErrorException.class, () -> postService.checkSpelling());
        verify(postRepository).findByPublishedFalse();
        verify(restTemplate).postForObject(eq("endpoint"), any(HttpEntity.class), eq(String.class));
    }
}
