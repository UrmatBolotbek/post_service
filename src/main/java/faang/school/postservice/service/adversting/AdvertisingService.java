package faang.school.postservice.service.adversting;

import faang.school.postservice.client.PaymentServiceClient;
import faang.school.postservice.config.context.UserContext;
import faang.school.postservice.dto.adversting.AdDto;
import faang.school.postservice.dto.adversting.AdvertisingRequest;
import faang.school.postservice.dto.payment.PaymentRequest;
import faang.school.postservice.mapper.adversting.AdMapper;
import faang.school.postservice.model.Post;
import faang.school.postservice.model.ad.Ad;
import faang.school.postservice.model.enums.AdverstisingPeriod;
import faang.school.postservice.repository.ad.AdRepository;
import faang.school.postservice.validator.adversting.AdvertisingValidator;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@AllArgsConstructor
public class AdvertisingService {
    private final AdRepository adRepository;
    private final AdMapper adMapper;
    private final AdvertisingValidator advertisingValidator;
    private final UserContext userContext;

    public AdDto buyAdvertising(AdvertisingRequest request) {
        long userId = userContext.getUserId();

        advertisingValidator.validateDate(request);
        advertisingValidator.validatePostForAdvertising(request.getPostId());

        AdverstisingPeriod period = AdverstisingPeriod.fromDays(request.getDays());
        advertisingValidator.validatePayment(userId, period.getPrice());


        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endDate = now.plusDays(period.getDays());
        Post post = Post.builder().id(request.getPostId()).build();
        Ad newAd = adMapper.toEntity(post, userId, now, endDate);

        adRepository.save(newAd);

        return adMapper.toDto(newAd, period);
    }
}
