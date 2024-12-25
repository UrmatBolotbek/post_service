package faang.school.postservice.service.adversting;

import faang.school.postservice.client.PaymentServiceClient;
import faang.school.postservice.config.context.UserContext;
import faang.school.postservice.dto.adversting.AdDto;
import faang.school.postservice.dto.adversting.AdvertisingRequestDto;
import faang.school.postservice.dto.bought.AdBoughtEvent;
import faang.school.postservice.dto.payment.Currency;
import faang.school.postservice.dto.payment.PaymentRequest;
import faang.school.postservice.dto.payment.PaymentResponse;
import faang.school.postservice.mapper.adversting.AdMapper;
import faang.school.postservice.model.Post;
import faang.school.postservice.model.ad.Ad;
import faang.school.postservice.model.enums.AdverstisingPeriod;
import faang.school.postservice.publisher.bought.AdBoughtEventPublisher;
import faang.school.postservice.repository.ad.AdRepository;
import faang.school.postservice.validator.adversting.AdvertisingValidator;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Slf4j
@Service
@AllArgsConstructor
public class AdvertisingService {
    private final AdRepository adRepository;
    private final AdMapper adMapper;
    private final AdvertisingValidator advertisingValidator;
    private final PaymentServiceClient paymentServiceClient;
    private final AdBoughtEventPublisher adBoughtEventPublisher;

    public AdDto buyAdvertising(AdvertisingRequestDto request, long userId) {
        advertisingValidator.validateDate(request);
        advertisingValidator.validatePostForAdvertising(request.getPostId());

        AdverstisingPeriod period = AdverstisingPeriod.fromDays(request.getDays());
        processPayment(userId, BigDecimal.valueOf(AdverstisingPeriod.fromDays(request.getDays()).getPrice()));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endDate = now.plusDays(period.getDays());
        Post post = Post.builder().id(request.getPostId()).build();
        Ad newAd = adMapper.toEntity(post, userId, now, endDate);

        adRepository.save(newAd);

        publishAdBoughtEvent(newAd, userId, period);
        return adMapper.toDto(newAd, period);
    }


    private void processPayment(long userId, BigDecimal price) {
        PaymentRequest paymentRequest = new PaymentRequest(userId, price, Currency.USD);
        ResponseEntity<PaymentResponse> response = paymentServiceClient.sendPayment(paymentRequest);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Payment failed");
        }
    }

    void publishAdBoughtEvent(Ad ad, long userId, AdverstisingPeriod period) {
        AdBoughtEvent adBoughtEvent = AdBoughtEvent.builder()
                .postId(ad.getPost().getId())
                .userId(userId)
                .paymentAmount((double) period.getPrice())
                .duration(period.getDays())
                .period(period)
                .purchaseTime(LocalDateTime.now())
                .build();

        adBoughtEventPublisher.publish(adBoughtEvent);
        log.info("AdBoughtEvent published for adId {}", ad.getId());
    }

}
