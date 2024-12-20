package faang.school.postservice.dto.post;

import faang.school.postservice.model.enums.PostType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostFilterDto {
    private Long authorId;
    private Long projectId;
    private boolean published;
    private PostType type;
}
