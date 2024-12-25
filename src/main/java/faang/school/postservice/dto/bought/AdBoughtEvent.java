package faang.school.postservice.dto.bought;

import faang.school.postservice.model.enums.AdverstisingPeriod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class AdBoughtEvent {
    private Long postId;
    private Long userId;
    private Double paymentAmount;
    private Integer duration;
    private AdverstisingPeriod period;
    private LocalDateTime purchaseTime;
}

