package faang.school.postservice.service.adversting;

import faang.school.postservice.client.PaymentServiceClient;
import faang.school.postservice.config.context.UserContext;
import faang.school.postservice.dto.adversting.AdvertisingRequestDto;
import faang.school.postservice.dto.bought.AdBoughtEvent;
import faang.school.postservice.mapper.adversting.AdMapper;
import faang.school.postservice.model.Post;
import faang.school.postservice.model.ad.Ad;
import faang.school.postservice.model.enums.AdverstisingPeriod;
import faang.school.postservice.publisher.bought.AdBoughtEventPublisher;
import faang.school.postservice.repository.ad.AdRepository;
import faang.school.postservice.validator.adversting.AdvertisingValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
public class AdvertisingServiceTest {
    @Mock
    private PaymentServiceClient paymentServiceClient;

    @Mock
    private AdRepository adRepository;

    @Mock
    private AdvertisingValidator advertisingValidator;
    @Mock
    private UserContext userContext;
    @Mock
    private AdBoughtEventPublisher adBoughtEventPublisher;
    @InjectMocks
    private AdvertisingService advertisingService;

    private AdMapper adMapper;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(Instant.parse("2024-12-15T15:51:25.624831Z"), ZoneId.systemDefault());
        adMapper = Mappers.getMapper(AdMapper.class);
        advertisingService = new AdvertisingService(adRepository, adMapper, advertisingValidator, paymentServiceClient, adBoughtEventPublisher);
    }

    @Test
    void buyAdvertising_InvalidPeriod_ShouldThrowException() {

        long postId = 2L;
        AdvertisingRequestDto advertisingRequest = new AdvertisingRequestDto(0, postId); // 0 дней — недопустимый период

        doThrow(new IllegalArgumentException("Days must be a positive number."))
                .when(advertisingValidator).validateDate(advertisingRequest);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                    advertisingService.buyAdvertising(advertisingRequest, userContext.getUserId());
                }
        );

        assertEquals("Days must be a positive number.", exception.getMessage());
        verify(advertisingValidator).validateDate(advertisingRequest);
        verify(advertisingValidator, never()).validatePostForAdvertising(anyLong());
        verify(adRepository, never()).save(any(Ad.class));
        verifyNoInteractions(paymentServiceClient);
    }

    @Test
    void publishAdBoughtEvent_ShouldPublishEvent() {
        // Given
        Post post = new Post();
        long userId = 1L;
        Ad ad = Ad.builder()
                .post(post)
                .build();
        ad.setId(100L);

        post.setId(2L);
        ad.setPost(post);

        AdverstisingPeriod period = AdverstisingPeriod.DEY; // 1 день = 10 единиц

        advertisingService.publishAdBoughtEvent(ad, userId, period);
        ArgumentCaptor<AdBoughtEvent> eventCaptor = ArgumentCaptor.forClass(AdBoughtEvent.class);
        verify(adBoughtEventPublisher).publish(eventCaptor.capture());

        AdBoughtEvent publishedEvent = eventCaptor.getValue();
        assertNotNull(publishedEvent);
        assertEquals(2L, publishedEvent.getPostId());
        assertEquals(userId, publishedEvent.getUserId());
        assertEquals(Double.valueOf(10), publishedEvent.getPaymentAmount());
        assertEquals(Integer.valueOf(1), publishedEvent.getDuration());
        assertEquals(period, publishedEvent.getPeriod());
        assertNotNull(publishedEvent.getPurchaseTime());

    }
}
