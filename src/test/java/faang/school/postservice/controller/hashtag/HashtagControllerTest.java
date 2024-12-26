package faang.school.postservice.controller.hashtag;

import faang.school.postservice.config.context.UserContext;
import faang.school.postservice.dto.post.PostResponseDto;
import faang.school.postservice.service.hashtag.HashtagService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class HashtagControllerTest {

    private MockMvc mockMvc;
    @InjectMocks
    private HashtagController hashtagController;
    @Mock
    private HashtagService hashtagService;
    @Mock
    private UserContext context;

    private PostResponseDto postResponseDto;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(hashtagController).build();

        postResponseDto = PostResponseDto.builder()
                .content("content")
                .build();
    }

    @Test
    void testGetPostsByHashtag() throws Exception {
        when(hashtagService.getPostsByHashtag("#example")).thenReturn(List.of(postResponseDto));

        mockMvc.perform(get("/api/v1/hashtags/posts")
                        .param("hashtag", "#example"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content", is("content"))); // предполагаемое содержимое
    }

    @Test
    void testCreateHashtagToPost() throws Exception {
        when(context.getUserId()).thenReturn(25L);

        mockMvc.perform(get("/api/v1/hashtags/post/17")
                .param("hashtag", "#example"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetAllHashtagByPostId() throws Exception {
        when(context.getUserId()).thenReturn(25L);
        when(hashtagService.getAllHashtagByPostId(17L, 25L)).thenReturn(List.of("#HappyNewJahr"));

        mockMvc.perform(get("/api/v1/hashtags/post/17"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]", is("#HappyNewJahr")));
    }

}
