package faang.school.postservice.dto.resource;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResourceResponseDto {
    private Long id;
    private String type;
    private Long postId;
    private String downloadUrl;
}