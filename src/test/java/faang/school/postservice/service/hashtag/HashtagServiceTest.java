package faang.school.postservice.service.hashtag;

import faang.school.postservice.dto.post.PostResponseDto;
import faang.school.postservice.mapper.post.PostMapperImpl;
import faang.school.postservice.model.Hashtag;
import faang.school.postservice.model.Post;
import faang.school.postservice.repository.HashTagRepository;
import faang.school.postservice.repository.PostRepository;
import faang.school.postservice.validator.post.PostValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class HashtagServiceTest {

    @InjectMocks
    private HashtagService hashtagService;

    @Mock
    private HashTagRepository hashTagRepository;
    @Mock
    private PostRepository postRepository;
    @Mock
    private PostValidator postValidator;
    @Spy
    private PostMapperImpl postMapper;

    private Hashtag hashtag;
    private Post post;

    @BeforeEach
    public void setUp() {
        hashtag = Hashtag.builder()
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
        post.setHashtags(List.of(hashtag));
        List<Post> posts = List.of(post);

        when(hashTagRepository.findAllByHashtagTitle("#test")).thenReturn(posts);

        List<PostResponseDto> answer = hashtagService.getPostsByHashtag("#test");
        assertEquals(answer.get(0).getContent(), post.getContent());
    }

//    @Test
//    void testCreateHashtagToPost() {
//        when(postValidator.validateAndGetPostById(post.getId())).thenReturn(post);
//
//        hashtagService.createHashtagToPost("#test", 14L, 15L);
//
//        verify(postRepository).save(post);
//        assertEquals(post.getHashtags().get(0), hashtag);
//    }

    @Test
    void testGetAllHashtagByPostId() {
        post.setHashtags(List.of(hashtag));
        when(postValidator.validateAndGetPostById(post.getId())).thenReturn(post);
        List<String> answer = hashtagService.getAllHashtagByPostId(post.getId(), 15L);
        assertEquals(answer.get(0), hashtag.getTitle());
    }

}
