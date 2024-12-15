package faang.school.postservice.service.adversting;

import faang.school.postservice.client.PaymentServiceClient;
import faang.school.postservice.dto.adversting.AdDto;
import faang.school.postservice.mapper.adversting.AdMapper;
import faang.school.postservice.model.Post;
import faang.school.postservice.model.ad.Ad;
import faang.school.postservice.model.enums.AdverstisingPeriod;
import faang.school.postservice.repository.ad.AdRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AdvertisingServiceTest {
    @Mock
    private PaymentServiceClient paymentServiceClient;

    @Mock
    private AdRepository adRepository;

    @InjectMocks
    private AdvertisingService advertisingService;

    private AdMapper adMapper;

    @BeforeEach
    void setUp() {

        Clock fixedClock = Clock.fixed(Instant.parse("2024-12-15T15:51:25.624831Z"), ZoneId.systemDefault());
        adMapper = Mappers.getMapper(AdMapper.class);
        advertisingService = new AdvertisingService(paymentServiceClient, adRepository, adMapper, fixedClock);
    }

    @Test
    void buyAdvertising_PaymentFailed_ShouldThrowException() {
        long userId = 1L;
        long postId = 2L;
        AdverstisingPeriod period = AdverstisingPeriod.WEEK;

        when(adRepository.findByPostId(postId)).thenReturn(Optional.empty());
        when(paymentServiceClient.processPayment(userId, period.getPrice())).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                advertisingService.buyAdvertising(userId, postId, period)
        );

        assertEquals("Payment failed", exception.getMessage());
        verify(paymentServiceClient).processPayment(userId, period.getPrice());
        verify(adRepository, never()).save(any(Ad.class));
    }
}
