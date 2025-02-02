package faang.school.postservice.mapper.post;

import faang.school.postservice.dto.post.PostRequestDto;
import faang.school.postservice.dto.post.PostResponseDto;
import faang.school.postservice.mapper.comment.CommentMapper;
import faang.school.postservice.mapper.resource.ResourceMapper;
import faang.school.postservice.model.Album;
import faang.school.postservice.model.Comment;
import faang.school.postservice.model.Like;
import faang.school.postservice.model.Post;
import faang.school.postservice.model.Resource;
import faang.school.postservice.news_feed.dto.serializable.CommentCache;
import faang.school.postservice.news_feed.dto.serializable.PostCache;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {ResourceMapper.class, CommentMapper.class})
public interface PostMapper {

    @Mapping(target = "images", ignore = true)
    @Mapping(target = "audio", ignore = true)
    @Mapping(source = "likes", target = "likeIds", qualifiedByName = "mapPostLikesToLikeIds")
    @Mapping(source = "comments", target = "comments")
    PostResponseDto toDto(Post post);

    Post toEntity(PostRequestDto postDto);

    @Mapping(target = "images", ignore = true)
    @Mapping(target = "audio", ignore = true)
    @Mapping(source = "likes", target = "likeIds", qualifiedByName = "mapPostLikesToLikeIds")
    @Mapping(source = "comments", target = "comments")
    List<PostResponseDto> toListPostDto(List<Post> posts);

    @Mapping(source = "likes", target = "likesCount", qualifiedByName = "ourMapLikes")
    @Mapping(source = "comments", target = "comments", qualifiedByName = "mapCommentsToDto")
    @Mapping(source = "albums", target = "albumIds", qualifiedByName = "mapAlbums")
    @Mapping(source = "resources", target = "resourceIds", qualifiedByName = "mapResources")
    PostCache toPostCache(Post post);

    List<PostCache> mapToPostCaches(List<Post> posts);

    @Mapping(source = "likes", target = "likesCount", qualifiedByName = "ourMapLikes")
    @Mapping(source = "post.id", target = "postId")
    CommentCache toCommentCache(Comment comment);

    @Named("mapPostLikesToLikeIds")
    default List<Long> mapPostLikesToLikeIds(List<Like> likes) {
        if (likes == null) {
            return null;
        }
        return likes.stream().map(Like::getId).collect(Collectors.toList());
    }

    @Named("ourMapLikes")
    default Long ourMapLikes(List<Like> likes) {
        return likes == null ? 0L : (long) likes.size();
    }

    @Named("mapComments")
    default Long mapComments(List<Comment> comments) {
        return comments == null ? 0L : (long) comments.size();
    }

    @Named("mapAlbums")
    default Set<Long> mapAlbums(List<Album> albums) {
        return albums == null ? null : albums.stream().map(Album::getId).collect(Collectors.toSet());
    }

    @Named("mapResources")
    default Set<Long> mapResources(List<Resource> resources) {
        return resources == null ? null : resources.stream().map(Resource::getId).collect(Collectors.toSet());
    }

    @Named("mapCommentsToDto")
    default List<CommentCache> mapCommentsToDto(List<Comment> comments) {
        int defaultLimit = 3;
        return comments.stream()
                .filter(comment -> comment.getCreatedAt() != null)
                .sorted(Comparator.comparing(Comment::getCreatedAt).reversed())
                .limit(defaultLimit)
                .map(this::toCommentCache)
                .collect(Collectors.toList());
    }
}
