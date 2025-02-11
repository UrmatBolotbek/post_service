package faang.school.postservice.dto.post;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostUpdateDto {
    private String content;
    private List<Long> imageFilesIdsToDelete;
    private List<Long> audioFilesIdsToDelete;
}