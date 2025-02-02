package faang.school.postservice.mapper.comment;

import faang.school.postservice.dto.comment.CommentRequestDto;
import faang.school.postservice.dto.comment.CommentResponseDto;
import faang.school.postservice.model.Comment;
import faang.school.postservice.model.Like;
import faang.school.postservice.news_feed.dto.serializable.CommentCache;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CommentMapper {
    @Mapping(source = "likes", target = "likeIds", qualifiedByName = "mapLikesToLikeIds")
    @Mapping(source = "post.id", target = "postId")
    CommentResponseDto toDto(Comment comment);

    @Mapping(source = "likes", target = "likeIds", qualifiedByName = "mapLikesToLikeIds")
    @Mapping(source = "post.id", target = "postId")
    List<CommentResponseDto> toDto(List<Comment> comments);

    @Mapping(source = "postId", target = "post.id")
    Comment toEntity(CommentRequestDto commentDto);

    @Mapping(source = "postId", target = "post.id")
    List<Comment> toEntity(List<CommentResponseDto> commentDtos);

    @Mapping(source = "likes", target = "likesCount", qualifiedByName = "mapLikes")
    @Mapping(source = "post.id", target = "postId")
    CommentCache toCommentCache(Comment comment);

    @Named("mapLikesToLikeIds")
    default List<Long> mapLikesToLikeIds(List<Like> likes) {
        if (likes == null) {
            return null;
        }
        return likes.stream()
                .map(Like::getId)
                .toList();
    }

    @Named("mapLikes")
    default Long mapLikes(List<Like> likes) {
        if (likes == null) return 0L;
        return (long) likes.size();
    }
}