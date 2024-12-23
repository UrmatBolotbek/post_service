package faang.school.postservice.dto.adversting;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class AdDto {
    private final Long postId;
    private final Long userId;
    private final int duration;
    private final int price;
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;
}
