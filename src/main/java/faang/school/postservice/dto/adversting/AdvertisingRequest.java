package faang.school.postservice.dto.adversting;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdvertisingRequest {
    @Min(1)
    private int days;
    @NotNull
    private Long postId;
}
