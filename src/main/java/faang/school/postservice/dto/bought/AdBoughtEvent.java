package faang.school.postservice.dto.bought;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class AdBoughtEvent {
    private Long postId;
    private Long userIs;
    private Double paymentAmount;
    private Integer duration;
    private LocalDateTime purchaseTime;
}

