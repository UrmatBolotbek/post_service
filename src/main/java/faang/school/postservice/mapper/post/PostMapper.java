package faang.school.postservice.mapper.post;

import faang.school.postservice.dto.post.PostRequestDto;
import faang.school.postservice.dto.post.PostResponseDto;
import faang.school.postservice.mapper.resource.ResourceMapper;
import faang.school.postservice.model.Comment;
import faang.school.postservice.model.Like;
import faang.school.postservice.model.Post;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.List;


@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = ResourceMapper.class)
public interface PostMapper {

    @Mapping(target = "images", ignore = true)
    @Mapping(target = "audio", ignore = true)
    @Mapping(source = "likes", target = "likeIds", qualifiedByName = "mapLikesToLikeIds")
    @Mapping(source = "comments", target = "commentIds", qualifiedByName = "mapCommentsToCommentIds")
    PostResponseDto toDto(Post post);

    Post toEntity(PostRequestDto postDto);

    @Mapping(target = "images", ignore = true)
    @Mapping(target = "audio", ignore = true)
    @Mapping(source = "likes", target = "likeIds", qualifiedByName = "mapLikesToLikeIds")
    @Mapping(source = "comments", target = "commentIds", qualifiedByName = "mapCommentsToCommentIds")
    List<PostResponseDto> toListPostDto(List<Post> posts);

    @Named("mapLikesToLikeIds")
    default List<Long> mapLikesToLikeIds(List<Like> likes) {
        if (likes == null) {
            return null;
        }
        return likes.stream()
                .map(Like::getId)
                .toList();
    }

    @Named("mapCommentsToCommentIds")
    default List<Long> mapCommentsToCommentIds(List<Comment> comments) {
        if (comments == null) {
            return null;
        }
        return comments.stream()
                .map(Comment::getId)
                .toList();
    }
}