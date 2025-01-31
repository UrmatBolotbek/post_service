package faang.school.postservice.dto.post;

import faang.school.postservice.dto.comment.CommentResponseDto;
import faang.school.postservice.dto.resource.ResourceResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostResponseDto {
    private Long id;
    private String content;
    private Long authorId;
    private boolean published;
    private Long projectId;
    private LocalDateTime createdAt;
    private List<Long> likeIds;
    private Integer countLikes;
    private Integer views;
    private List<CommentResponseDto> comments;
    private LocalDateTime scheduledAt;
    private LocalDateTime publishedAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private List<ResourceResponseDto> images;
    private List<ResourceResponseDto> audio;
}