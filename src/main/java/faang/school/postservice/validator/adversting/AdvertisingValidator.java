package faang.school.postservice.validator.adversting;

import faang.school.postservice.client.PaymentServiceClient;
import faang.school.postservice.dto.adversting.AdvertisingRequest;
import faang.school.postservice.dto.payment.PaymentRequest;
import faang.school.postservice.dto.payment.PaymentResponse;
import faang.school.postservice.model.ad.Ad;
import faang.school.postservice.repository.PostRepository;
import faang.school.postservice.repository.ad.AdRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

@AllArgsConstructor
public class AdvertisingValidator {
    private final AdRepository adRepository;
    private final PostRepository postRepository;
    private final PaymentServiceClient paymentServiceClient;
    private static final String DEFAULT_CURRENCY = "USD";


    public void validatePostForAdvertising(long postId) {
        if (!postRepository.existsById(postId)) {
            throw new IllegalArgumentException("Post with id " + postId + " does not exist");
        }

        Optional<Ad> existingAd = adRepository.findByPostId(postId);
        if (existingAd.isPresent()) {
            throw new IllegalStateException("This post is already being advertised");
        }
    }

    public void validatePayment(long userId, int price) {
        PaymentRequest paymentRequest = new PaymentRequest(
                userId,
                price,
                DEFAULT_CURRENCY
        );
        ResponseEntity<PaymentResponse> response = paymentServiceClient.sendPayment(paymentRequest);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Payment failed");
        }
    }

    public void validateDate(AdvertisingRequest request){
        if (request.getDays() <= 0) {
            throw new IllegalArgumentException("Days must be a positive number.");
        }

    }


}
