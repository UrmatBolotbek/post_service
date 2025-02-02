package faang.school.postservice.util.post;

import faang.school.postservice.dto.user.UserDto;
import faang.school.postservice.model.Album;
import faang.school.postservice.model.Comment;
import faang.school.postservice.model.Like;
import faang.school.postservice.model.Post;
import faang.school.postservice.model.Resource;
import faang.school.postservice.news_feed.dto.serializable.CommentCache;
import faang.school.postservice.news_feed.dto.serializable.PostCache;
import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static org.mockito.Mockito.mock;

@UtilityClass
public class PostCacheFabric {
    private static final int DEFAULT_NUMBER_OF_OBJECT = 3;

    public static Post buildPost(Long id, String content) {
        return Post
                .builder()
                .id(id)
                .content(content)
                .build();
    }

    public static Post buildPost(Long id, boolean published) {
        return Post
                .builder()
                .id(id)
                .published(published)
                .build();
    }

    public static Post buildPost(Long id, String content, Long authorId) {
        return Post
                .builder()
                .id(id)
                .content(content)
                .authorId(authorId)
                .build();
    }

    public static Post buildPost(Long id, String content, Long authorId, List<String> hashTags) {
        return Post
                .builder()
                .id(id)
                .content(content)
                .authorId(authorId)
                .hashtags(hashTags)
                .build();
    }

    public static Post buildPost(String content, List<String> hashTags) {
        return Post
                .builder()
                .content(content)
                .hashtags(hashTags)
                .build();
    }

    public static Post buildPost(Long id, String content, boolean deleted, boolean published,
                                 Long authorId, Long projectId,
                                 LocalDateTime createdAt, LocalDateTime publishedAt) {
        return Post
                .builder()
                .id(id)
                .content(content)
                .deleted(deleted)
                .publishedAt(publishedAt)
                .authorId(authorId)
                .projectId(projectId)
                .createdAt(createdAt)
                .publishedAt(publishedAt)
                .build();
    }

    public static PostCache buildPostCacheDto() {
        return PostCache
                .builder()
                .build();
    }

    public static PostCache buildPostCacheDto(Long id) {
        return PostCache
                .builder()
                .id(id)
                .build();
    }

    public static PostCache buildPostCacheDto(Long id, LocalDateTime publishedAt) {
        return PostCache
                .builder()
                .id(id)
                .publishedAt(publishedAt)
                .build();
    }

    public static List<PostCache> buildPostCacheDtosWithTags(int number) {
        return LongStream
                .rangeClosed(1, number)
                .mapToObj(i -> buildPostCacheDtoWithTags(i, buildHashTags(DEFAULT_NUMBER_OF_OBJECT)))
                .toList();
    }

    public static PostCache buildPostCacheDtoWithTags(Long id, List<String> tags) {
        return PostCache
                .builder()
                .id(id)
                .hashTags(tags)
                .build();
    }

    public static List<String> buildHashTags(int number) {
        return IntStream
                .rangeClosed(1, number)
                .mapToObj(i -> "tag" + i)
                .toList();
    }

    public static List<PostCache> buildPostCacheDtosForMapping() {
        return IntStream
                .range(0, DEFAULT_NUMBER_OF_OBJECT)
                .mapToObj(i -> buildPostCacheDtoForTestMapToPostCacheDto())
                .toList();
    }

    public static PostCache buildPostCacheDtoForTestMapToPostCacheDto() {
        return PostCache
                .builder()
                .likesCount((long) getListOfIds(DEFAULT_NUMBER_OF_OBJECT).size())
                .commentsCount((long) getListOfIds(DEFAULT_NUMBER_OF_OBJECT).size())
                .albumIds(getSetOfIds(DEFAULT_NUMBER_OF_OBJECT))
                .resourceIds(getSetOfIds(DEFAULT_NUMBER_OF_OBJECT))
                .comments(List.of())
                .build();
    }

    public static List<Long> getListOfIds(int numberOfIds) {
        List<Long> ids = new ArrayList<>();
        LongStream.rangeClosed(1, numberOfIds)
                .forEach(ids::add);
        return ids;
    }

    public static Set<Long> getSetOfIds(int numberOfIds) {
        Set<Long> ids = new HashSet<>();
        LongStream.rangeClosed(1, numberOfIds)
                .forEach(ids::add);
        return ids;
    }

    public static List<Post> buildPostsForMapping() {
        return IntStream
                .range(0, DEFAULT_NUMBER_OF_OBJECT)
                .mapToObj(i -> buildPostForTestMapToPostCacheDto())
                .toList();
    }

    public static Post buildPostForTestMapToPostCacheDto() {
        return Post
                .builder()
                .likes(buildLikes(DEFAULT_NUMBER_OF_OBJECT))
                .comments(buildComments(DEFAULT_NUMBER_OF_OBJECT))
                .albums(buildAlbums(DEFAULT_NUMBER_OF_OBJECT))
                .resources(buildResource(DEFAULT_NUMBER_OF_OBJECT))
                .build();
    }

    public static List<Like> buildLikes(int numberOfLikes) {
        return LongStream
                .rangeClosed(1, numberOfLikes)
                .mapToObj(PostCacheFabric::buildLike)
                .toList();
    }

    public static Like buildLike(Long id) {
        return Like
                .builder()
                .id(id)
                .build();
    }

    public static List<Comment> buildComments(int numberOfComments) {
        return LongStream
                .rangeClosed(1, numberOfComments)
                .mapToObj(PostCacheFabric::buildComment)
                .toList();
    }

    public static Comment buildComment(Long id) {
        return Comment
                .builder()
                .id(id)
                .build();
    }

    public static List<Album> buildAlbums(int numberOfAlbums) {
        return LongStream
                .rangeClosed(1, numberOfAlbums)
                .mapToObj(PostCacheFabric::buildAlbum)
                .toList();
    }

    public static Album buildAlbum(Long id) {
        return Album
                .builder()
                .id(id)
                .build();
    }

    public static List<Resource> buildResource(int numberOfResource) {
        return LongStream
                .rangeClosed(1, numberOfResource)
                .mapToObj(PostCacheFabric::buildResource)
                .toList();
    }

    public static Resource buildResource(Long id) {
        return Resource
                .builder()
                .id(id)
                .build();
    }

    public static PostCache buildDefaultPostDto(Long id) {
        return PostCache.builder()
                .id(id)
                .content("content")
                .authorId(1L)
                .authorDto(mock(UserDto.class))
                .likesCount(0L)
                .publishedAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .comments(List.of(buildDefaultCommentDto(id)))
                .build();
    }

    public static CommentCache buildDefaultCommentDto(Long postId) {
        return CommentCache.builder()
                .id(1L)
                .postId(postId)
                .authorId(1L)
                .build();
    }

    public static UserDto buildDefaultUserDto() {
        return UserDto.builder()
                .id(1L)
                .build();
    }
}
