package faang.school.postservice.news_feed.service.feed;

import faang.school.postservice.annotations.publisher.PublishEvent;
import faang.school.postservice.client.UserServiceClient;
import faang.school.postservice.dto.user.UserDto;
import faang.school.postservice.news_feed.dto.serializable.CommentCache;
import faang.school.postservice.news_feed.dto.serializable.PostCache;
import faang.school.postservice.news_feed.repository.CommentCacheRepository;
import faang.school.postservice.news_feed.repository.PostCacheRepository;
import faang.school.postservice.news_feed.repository.UserCacheRepository;
import faang.school.postservice.news_feed.repository.key.PostKey;
import faang.school.postservice.service.post.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

import static faang.school.postservice.news_feed.enums.PublisherType.POST_VIEW;

@Service
@RequiredArgsConstructor
public class FeedService {
    private final CommentCacheRepository commentCacheRepository;
    private final PostCacheRepository postCacheRepository;
    private final UserCacheRepository userCacheRepository;
    private final UserServiceClient userServiceClient;
    private final FeedHeaterService feedHeaterService;
    private final Executor usersFeedsUpdatePool;
    private final PostService postService;
    private final PostKey postKey;

    @PublishEvent(type = POST_VIEW)
    public List<PostCache> getFeed(long userId, long offset, long limit) {
        Set<String> postIds = userCacheRepository.findPostIdsInUserFeed(userId, offset, limit);

        if (!postIds.isEmpty()) {
            return findPostsInCache(postIds);
        }

        List<PostCache> posts = postService.getSetOfPosts(userId, offset, limit);
        setAuthors(posts);

        usersFeedsUpdatePool.execute(() -> feedHeaterService.updateUserFeed(userId));

        return posts;
    }

    private List<PostCache> findPostsInCache(Set<String> postIds) {
        List<PostCache> posts = postIds.stream()
                .map(key -> postCacheRepository.findByKey(key).orElseGet(() ->
                        findSaveToCacheAndGetPostCacheDto(key)))
                .toList();

        setAuthors(posts);

        return posts;
    }

    private void setAuthors(List<PostCache> postDtoList) {
        postDtoList.forEach(postDto -> {
            postDto.setAuthorDto(userCacheRepository.findById(postDto.getAuthorId()).orElseGet(() ->
                    findSaveToCacheAndGetUserDto(postDto.getAuthorId())));

            List<CommentCache> commentList = commentCacheRepository.findAllByPostId(postDto.getId());

            if (commentList.isEmpty()) {
                commentList = postDto.getComments();
            }

            commentList.forEach(comment ->
                    comment.setAuthorDto(userCacheRepository.findById(comment.getAuthorId()).orElseGet(() ->
                            findSaveToCacheAndGetUserDto(comment.getAuthorId()))));

            postDto.setComments(commentList);
        });
    }

    private UserDto findSaveToCacheAndGetUserDto(Long userId) {
        UserDto userDto = userServiceClient.getUser(userId);
        userCacheRepository.save(userDto);
        return userDto;
    }

    private PostCache findSaveToCacheAndGetPostCacheDto(String key) {
        long id = postKey.getPostIdFrom(key);
        PostCache postCache = postService.findPostCacheById(id);

        postCacheRepository.save(postCache);
        commentCacheRepository.saveAll(postCache.getComments());

        return postCache;
    }
}
