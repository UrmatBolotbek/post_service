package faang.school.postservice.service.adversting;

import faang.school.postservice.client.PaymentServiceClient;
import faang.school.postservice.dto.adversting.AdDto;
import faang.school.postservice.mapper.adversting.AdMapper;
import faang.school.postservice.model.Post;
import faang.school.postservice.model.ad.Ad;
import faang.school.postservice.model.enums.AdverstisingPeriod;
import faang.school.postservice.repository.ad.AdRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@AllArgsConstructor
public class AdvertisingService {
    private final PaymentServiceClient paymentServiceClient;
    private final AdRepository adRepository;
    private final AdMapper adMapper;
    private final Clock clock;

    public AdDto buyAdvertising(long userId, long postId, AdverstisingPeriod period) {

        Optional<Ad> existingAd = adRepository.findByPostId(postId);
        if (existingAd.isPresent()) {
            throw new IllegalStateException("This post is already being advertised");
        }

        boolean paymentSuccess = paymentServiceClient.processPayment(userId, period.getPrice());
        if (!paymentSuccess) {
            throw new RuntimeException("Payment failed");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endDate = now.plusDays(period.getDays());
        Post post = Post.builder().id(postId).build();
        Ad newAd = adMapper.toEntity(post, userId, now, endDate);

        adRepository.save(newAd);

        return adMapper.toDto(newAd, period);
    }
}
