package faang.school.postservice.service.news_feed;

import faang.school.postservice.dto.post.PostResponseDto;
import faang.school.postservice.mapper.cache.PostCacheMapper;
import faang.school.postservice.model.cache.PostCache;
import faang.school.postservice.repository.redis.PostCacheRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostCacheServiceTest {

    @Mock
    private PostCacheRepository postCacheRepository;

    @Mock
    private PostCacheMapper postCacheMapper;

    @InjectMocks
    private PostCacheService postCacheService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void testSavePostCache() {
        PostResponseDto postResponseDto = new PostResponseDto();
        PostCache postCache = new PostCache();

        when(postCacheMapper.toPostCache(postResponseDto)).thenReturn(postCache);

        postCacheService.savePostCache(postResponseDto);

        verify(postCacheMapper).toPostCache(postResponseDto);
        verify(postCacheRepository).save(postCache);
        verifyNoMoreInteractions(postCacheMapper, postCacheRepository);
    }
}
