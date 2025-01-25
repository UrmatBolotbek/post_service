package faang.school.postservice.service.hashtag;

import faang.school.postservice.dto.post.PostResponseDto;
import faang.school.postservice.mapper.post.PostMapper;
import faang.school.postservice.model.Hashtag;
import faang.school.postservice.model.Post;
import faang.school.postservice.repository.HashTagRepository;
import faang.school.postservice.validator.post.PostValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HashtagServiceTest {

    @Captor
    private ArgumentCaptor<Hashtag> hashtagCaptor;

    @InjectMocks
    private HashtagService hashtagService;

    @Mock
    private HashTagRepository hashTagRepository;

    @Mock
    private PostValidator postValidator;

    @Mock
    private PostMapper postMapper;

    private Hashtag hashtag;
    private Post post;

    @BeforeEach
    public void setUp() {
        hashtag = Hashtag.builder()
                .posts(new ArrayList<>())
                .title("#test")
                .build();
        post = Post.builder()
                .id(14L)
                .hashtags(new ArrayList<>())
                .content("content")
                .build();
    }

    @Test
    void testGetPostsByHashtag() {
        List<Post> posts = List.of(post);
        when(hashTagRepository.findAllByHashtagTitle("#test")).thenReturn(posts);
        PostResponseDto postDto = new PostResponseDto();
        postDto.setContent("content");
        when(postMapper.toListPostDto(posts)).thenReturn(List.of(postDto));
        List<PostResponseDto> result = hashtagService.getPostsByHashtag("#test");
        verify(hashTagRepository).findAllByHashtagTitle("#test");
        verify(postMapper).toListPostDto(posts);
        verifyNoMoreInteractions(hashTagRepository, postValidator, postMapper);
        assertEquals(1, result.size());
        assertEquals("content", result.get(0).getContent());
    }

    @Test
    void testCreateHashtagToPost_NewHashtag() {
        String hashtagTitle = "#test";
        long postId = 14L;
        long userId = 15L;
        when(postValidator.validateAndGetPostById(postId)).thenReturn(post);
        doNothing().when(postValidator).validateUserExist(userId);
        doNothing().when(postValidator).validateUserToPost(post, userId);
        when(hashTagRepository.findByTitle(hashtagTitle)).thenReturn(Optional.empty());
        hashtagService.createHashtagToPost(hashtagTitle, postId, userId);
        verify(hashTagRepository).save(hashtagCaptor.capture());
        Hashtag savedHashtag = hashtagCaptor.getValue();
        assertEquals(hashtagTitle, savedHashtag.getTitle());
        assertEquals(1, savedHashtag.getPosts().size());
        assertEquals(post, savedHashtag.getPosts().get(0));
        verify(postValidator).validateAndGetPostById(postId);
        verify(postValidator).validateUserExist(userId);
        verify(postValidator).validateUserToPost(post, userId);
        verify(hashTagRepository).findByTitle(hashtagTitle);
        verify(hashTagRepository).save(savedHashtag);
        verifyNoMoreInteractions(hashTagRepository, postValidator, postMapper);
    }

    @Test
    void testCreateHashtagToPost_ExistingHashtag() {
        String hashtagTitle = "#test";
        long postId = 14L;
        long userId = 15L;
        Hashtag existingHashtag = Hashtag.builder()
                .posts(new ArrayList<>())
                .title(hashtagTitle)
                .build();
        when(postValidator.validateAndGetPostById(postId)).thenReturn(post);
        doNothing().when(postValidator).validateUserExist(userId);
        doNothing().when(postValidator).validateUserToPost(post, userId);
        doNothing().when(postValidator).validatePostHatThisHashtag(post, existingHashtag);
        when(hashTagRepository.findByTitle(hashtagTitle)).thenReturn(Optional.of(existingHashtag));
        hashtagService.createHashtagToPost(hashtagTitle, postId, userId);
        verify(hashTagRepository).save(hashtagCaptor.capture());
        Hashtag savedHashtag = hashtagCaptor.getValue();
        assertEquals(hashtagTitle, savedHashtag.getTitle());
        assertEquals(1, savedHashtag.getPosts().size());
        assertEquals(post, savedHashtag.getPosts().get(0));
        verify(postValidator).validateAndGetPostById(postId);
        verify(postValidator).validateUserExist(userId);
        verify(postValidator).validateUserToPost(post, userId);
        verify(hashTagRepository).findByTitle(hashtagTitle);
        verify(postValidator).validatePostHatThisHashtag(post, existingHashtag);
        verify(hashTagRepository).save(existingHashtag);
        verifyNoMoreInteractions(hashTagRepository, postValidator, postMapper);
    }

    @Test
    void testGetAllHashtagByPostId() {
        long postId = 14L;
        long userId = 15L;
        post.setHashtags(List.of(hashtag));
        when(postValidator.validateAndGetPostById(postId)).thenReturn(post);
        doNothing().when(postValidator).validateUserExist(userId);
        doNothing().when(postValidator).validateUserToPost(post, userId);
        List<String> hashtagTitles = hashtagService.getAllHashtagByPostId(postId, userId);
        assertEquals(1, hashtagTitles.size());
        assertEquals("#test", hashtagTitles.get(0));
        verify(postValidator).validateAndGetPostById(postId);
        verify(postValidator).validateUserExist(userId);
        verify(postValidator).validateUserToPost(post, userId);
        verifyNoMoreInteractions(hashTagRepository, postValidator, postMapper);
    }
}
