package faang.school.postservice.controller.advertising;

import faang.school.postservice.config.context.UserContext;
import faang.school.postservice.dto.adversting.AdDto;
import faang.school.postservice.dto.adversting.AdvertisingRequest;
import faang.school.postservice.model.enums.AdverstisingPeriod;
import faang.school.postservice.service.adversting.AdvertisingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/advertising")
public class AdvertisingController {
    private final AdvertisingService advertisingService;
    private final UserContext userContext;

    @PostMapping("/buy")
    public AdDto buyAdvertising(@RequestBody AdvertisingRequest request) {
      return advertisingService.buyAdvertising(request);
    }
}
