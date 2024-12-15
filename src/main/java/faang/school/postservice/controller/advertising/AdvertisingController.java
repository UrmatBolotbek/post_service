package faang.school.postservice.controller.advertising;

import faang.school.postservice.dto.adversting.AdDto;
import faang.school.postservice.model.enums.AdverstisingPeriod;
import faang.school.postservice.service.adversting.AdvertisingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/advertising")

public class AdvertisingController {
    private final AdvertisingService adverstisingService;

    @GetMapping("buy/{days}/{userId}/{postId}")
    public AdDto buyAdvertising(@PathVariable int days, @PathVariable long userId, @PathVariable long postId) {
        AdverstisingPeriod period = AdverstisingPeriod.fromDays(days);
        return adverstisingService.buyAdvertising(userId, postId, period);
    }
}
