package faang.school.postservice.service.news_feed;

import faang.school.postservice.client.UserServiceClient;
import faang.school.postservice.dto.user.UserDto;
import faang.school.postservice.mapper.cache.AuthorCacheMapper;
import faang.school.postservice.model.cache.AuthorCache;
import faang.school.postservice.repository.redis.AuthorCacheRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorCacheServiceTest {

    @Mock
    private AuthorCacheRepository repository;

    @Mock
    private AuthorCacheMapper authorCacheMapper;

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private AuthorCacheService authorCacheService;

    @Test
    void testSaveAuthorCache() {
        Long postAuthorId = 123L;

        UserDto userDto = UserDto.builder()
                .id(postAuthorId)
                .username("John Doe")
                .email("john@test.com")
                .phone("1234567890")
                .build();

        AuthorCache authorCache = AuthorCache.builder()
                .id(postAuthorId)
                .username("John Doe")
                .build();

        when(userServiceClient.getUser(postAuthorId)).thenReturn(userDto);
        when(authorCacheMapper.toAuthorCache(userDto)).thenReturn(authorCache);

        authorCacheService.saveAuthorCache(postAuthorId);

        verify(userServiceClient).getUser(postAuthorId);
        verify(authorCacheMapper).toAuthorCache(userDto);
        verify(repository).save(authorCache);
        verifyNoMoreInteractions(userServiceClient, authorCacheMapper, repository);
    }
}
