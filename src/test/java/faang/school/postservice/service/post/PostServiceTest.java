package faang.school.postservice.service.post;

import faang.school.postservice.config.api.SpellingConfig;
import faang.school.postservice.dto.post.PostFilterDto;
import faang.school.postservice.dto.post.PostRequestDto;
import faang.school.postservice.dto.post.PostResponseDto;
import faang.school.postservice.dto.post.PostUpdateDto;
import faang.school.postservice.kafka.EventsManager;
import faang.school.postservice.mapper.post.PostMapper;
import faang.school.postservice.model.Like;
import faang.school.postservice.model.Post;
import faang.school.postservice.repository.PostRepository;
import faang.school.postservice.service.post.filter.PostFilters;
import faang.school.postservice.service.resource.ResourceService;
import faang.school.postservice.util.ModerationDictionary;
import faang.school.postservice.validator.post.PostValidator;
import faang.school.postservice.validator.resource.ResourceValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
    @InjectMocks
    private PostService postService;

    private Post post;

    @BeforeEach
    void setUp() {
        lenient().doAnswer(invocation -> {
            Runnable r = invocation.getArgument(0);
            r.run();
            return null;
        }).when(spellingExecutor).execute(any(Runnable.class));

        PostRequestDto postDto = new PostRequestDto();
        List<Post> preparedPosts = new ArrayList<>();

        post = new Post();
        post.setId(1L);
        post.setContent("This is errror");
        postDto.setAuthorId(1L);
        post.setLikes(List.of(new Like(), new Like(), new Like()));
        post.setPublished(false);
        post.setDeleted(false);
        post.setCreatedAt(LocalDateTime.now().plusDays(1));
        preparedPosts.add(post);

        Post post2 = new Post();
        post2.setId(2L);
        post2.setLikes(List.of(new Like(), new Like()));
        post2.setPublished(true);
        post2.setDeleted(false);
        post2.setCreatedAt(LocalDateTime.now().plusDays(2));
        preparedPosts.add(post2);

        Post post3 = new Post();
        post3.setId(3L);
        post3.setLikes(List.of(new Like(), new Like(), new Like()));
        post3.setPublished(false);
        post3.setDeleted(false);
        post3.setCreatedAt(LocalDateTime.now().plusDays(3));
        preparedPosts.add(post3);

        Post post4 = new Post();
        post4.setId(4L);
        post4.setLikes(List.of(new Like(), new Like()));
        post4.setPublished(false);
        post4.setDeleted(true);
        post4.setCreatedAt(LocalDateTime.now().plusDays(4));
        preparedPosts.add(post4);

        PostResponseDto postResponseDto = new PostResponseDto();
        postResponseDto.setId(1L);
        postResponseDto.setAuthorId(1L);
    }

    @Test
    void testCheckSpellingSuccess() throws InterruptedException {
        String prepareDate = "{\"elements\":[{\"id\":0,\"errors\":[{\"suggestions\":" +
                "[\"error\",\"Rorer\",\"eerier\",\"arrear\",\"rower\",\"Euro\",\"rehear\",\"err\",\"ROR\",\"Orr\"]" +
                ",\"position\":8,\"word\":\"errror\"}]}],\"spellingErrorCount\":1}";
        List<Post> posts = List.of(post);

        when(postRepository.findByPublishedFalse()).thenReturn(posts);
        when(api.getKey()).thenReturn("key");
        when(api.getEndpoint()).thenReturn("endpoint");
        when(restTemplate.postForObject(any(String.class), any(HttpEntity.class), eq(String.class)))
                .thenReturn(prepareDate);

        postService.checkSpelling();

        verify(postRepository, times(1)).findByPublishedFalse();
        verify(api, times(1)).getKey();
        verify(api, times(1)).getEndpoint();
        verify(postRepository, times(1)).save(any(Post.class));

        Thread.sleep(200);

        assertEquals("This is error", posts.get(0).getContent());
    }

    @Test
    public void shouldCreatePosts() {
        PostRequestDto requestDto = PostRequestDto.builder()
                .authorId(1L)
                .projectId(2L)
                .content("Sample Content")
                .build();

        List<MultipartFile> images = List.of(mock(MultipartFile.class));
        List<MultipartFile> audio = List.of(mock(MultipartFile.class));

        Post post = new Post();
        post.setId(1L);
        post.setResources(new ArrayList<>());

        Post mappedPost = new Post();
        mappedPost.setPublished(false);
        mappedPost.setDeleted(false);
        mappedPost.setLikes(new ArrayList<>());
        mappedPost.setComments(new ArrayList<>());
        mappedPost.setResources(new ArrayList<>());

        PostResponseDto responseDto = PostResponseDto.builder()
                .id(1L)
                .content("Sample Content")
                .authorId(1L)
                .projectId(2L)
                .build();

        when(postMapper.toEntity(requestDto)).thenReturn(mappedPost);
        when(postRepository.save(any(Post.class))).thenReturn(post);
        when(postMapper.toDto(any(Post.class))).thenReturn(responseDto);

        PostResponseDto actualResponse = postService.create(requestDto, images, audio);

        assertEquals(1L, actualResponse.getId());
        assertEquals("Sample Content", actualResponse.getContent());

        verify(postValidator, times(1)).validateCreate(requestDto);
        verify(resourceService, times(2)).uploadResources(anyList(), anyString(), eq(post));
        verify(postRepository, times(2)).save(any(Post.class));
    }

    @Test
    public void shouldPublishPost() {
        Long postId = 1L;
        Post post = new Post();
        post.setId(postId);
        post.setPublished(false);

        Post updatedPost = new Post();
        updatedPost.setId(postId);
        updatedPost.setPublished(true);

        PostResponseDto postDto = new PostResponseDto();
        postDto.setId(postId);

        when(postValidator.validateAndGetPostById(postId)).thenReturn(post);
        when(postRepository.save(post)).thenReturn(updatedPost);
        when(postMapper.toDto(updatedPost)).thenReturn(postDto);

        PostResponseDto result = postService.publishPost(postId);

        verify(postValidator).validatePublish(post);
        verify(postRepository).save(post);
        verify(postMapper).toDto(updatedPost);
        assertEquals(result.getId(), postId);
    }

    @Test
    void shouldUpdatePostSuccessfully() {
        Long postId = 1L;

        PostUpdateDto updateDto = PostUpdateDto.builder()
                .content("Updated Content")
                .imageFilesIdsToDelete(List.of(1L))
                .audioFilesIdsToDelete(List.of(2L))
                .build();

        List<MultipartFile> images = List.of(mock(MultipartFile.class));
        List<MultipartFile> audio = List.of(mock(MultipartFile.class));

        Post post = new Post();
        post.setId(postId);
        post.setResources(new ArrayList<>());

        PostResponseDto responseDto = PostResponseDto.builder()
                .id(postId)
                .content("Updated Content")
                .build();

        when(postRepository.getPostById(postId)).thenReturn(post);
        when(postRepository.save(any(Post.class))).thenReturn(post);
        when(postMapper.toDto(post)).thenReturn(responseDto);

        PostResponseDto actualResponse = postService.updatePost(postId, updateDto, images, audio);

        assertEquals(postId, actualResponse.getId());
        assertEquals("Updated Content", actualResponse.getContent());

        verify(postRepository, times(1)).getPostById(postId);
        verify(resourceService, times(1)).deleteResources(eq(List.of(1L)));
        verify(resourceService, times(1)).deleteResources(eq(List.of(2L)));
        verify(resourceService, times(1)).uploadResources(eq(images), eq("image"), eq(post));
        verify(resourceService, times(1)).uploadResources(eq(audio), eq("audio"), eq(post));
        verify(resourceValidator, times(1)).validateResourceCounts(post);
        verify(postRepository, times(1)).save(post);
    }

    @Test
    public void shouldDeletePost() {
        Long postId = 1L;
        Post post = new Post();
        post.setId(postId);
        post.setPublished(true);
        post.setDeleted(false);

        Post updatedPost = new Post();
        updatedPost.setId(postId);
        updatedPost.setPublished(false);
        updatedPost.setDeleted(true);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postRepository.save(post)).thenReturn(updatedPost);

        postService.deletePost(postId);

        verify(postValidator).validateDelete(post);
        verify(postRepository).findById(postId);
        verify(postRepository).save(post);
        assertTrue(updatedPost.isDeleted());
    }

    @Test
    public void shouldReturnPostById() {
        Long postId = 1L;
        Post post = new Post();
        post.setId(postId);

        PostResponseDto postDto = new PostResponseDto();
        postDto.setId(postId);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postMapper.toDto(post)).thenReturn(postDto);

        PostResponseDto result = postService.getPostById(postId);

        verify(postRepository).findById(postId);
        verify(postMapper).toDto(post);
        assertEquals(postId, result.getId());
    }

    @Test
    public void shouldFilterPosts() {
        PostFilterDto filterDto = new PostFilterDto();
        PostFilters filter = mock(PostFilters.class);

        Post post1 = new Post();
        Post post2 = new Post();

        PostResponseDto postDto1 = new PostResponseDto();
        PostResponseDto postDto2 = new PostResponseDto();

        when(postRepository.findAll()).thenReturn(Arrays.asList(post1, post2));
        when(postFilters.stream()).thenReturn(Stream.of(filter));
        when(filter.isApplicable(filterDto)).thenReturn(true);
        when(filter.apply(any(), eq(filterDto))).thenReturn(Stream.of(post1, post2));
        when(postMapper.toListPostDto(anyList())).thenReturn(Arrays.asList(postDto1, postDto2));

        List<PostResponseDto> result = postService.getPosts(filterDto);

        verify(postRepository).findAll();
        verify(filter).isApplicable(filterDto);
        verify(filter).apply(any(), eq(filterDto));
        verify(postMapper).toListPostDto(anyList());
        assertEquals(2, result.size());
    }

    @Test
    public void testVerifyPostsForModeration() {
        Post post1 = new Post();
        post1.setId(1L);
        post1.setContent("Test1");
        Post post2 = new Post();
        post2.setId(2L);
        post2.setContent("Test2");
        List<Post> batch = Arrays.asList(post1, post2);

        when(moderationDictionary.isVerified(post1.getContent())).thenReturn(true);
        when(moderationDictionary.isVerified(post2.getContent())).thenReturn(false);

        postService.verifyPostsForModeration(batch);

        assertEquals(LocalDateTime.now().getMinute(), post1.getVerifiedDate().getMinute());
        assertTrue(post1.getVerified());
        assertEquals(LocalDateTime.now().getMinute(), post2.getVerifiedDate().getMinute());
        assertFalse(post2.getVerified());

        verify(postRepository, times(2)).save(any(Post.class));
    }
}
