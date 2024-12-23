package faang.school.postservice.service.adversting;

import faang.school.postservice.client.PaymentServiceClient;
import faang.school.postservice.config.context.UserContext;
import faang.school.postservice.dto.adversting.AdDto;
import faang.school.postservice.dto.adversting.AdvertisingRequest;
import faang.school.postservice.mapper.adversting.AdMapper;
import faang.school.postservice.model.Post;
import faang.school.postservice.model.ad.Ad;
import faang.school.postservice.model.enums.AdverstisingPeriod;
import faang.school.postservice.repository.ad.AdRepository;
import faang.school.postservice.validator.adversting.AdvertisingValidator;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AdvertisingServiceTest {
    @Mock
    private PaymentServiceClient paymentServiceClient;

    @Mock
    private AdRepository adRepository;

    @Mock
    private AdvertisingValidator advertisingValidator;
    @Mock
    private AdvertisingRequest advertisingRequest;

    @Mock
    private UserContext userContext;
    @InjectMocks
    private AdvertisingService advertisingService;

    private AdMapper adMapper;

    @BeforeEach
    void setUp() {

        Clock fixedClock = Clock.fixed(Instant.parse("2024-12-15T15:51:25.624831Z"), ZoneId.systemDefault());
        adMapper = Mappers.getMapper(AdMapper.class);
        advertisingService = new AdvertisingService(adRepository, adMapper, advertisingValidator, userContext);
    }

    @Test
    void buyAdvertising_InvalidPeriod_ShouldThrowException() {
        // Arrange
        long userId = 1L;
        long postId = 2L;
        AdvertisingRequest advertisingRequest = new AdvertisingRequest(1, postId); // 0 дней — недопустимый период

        when(userContext.getUserId()).thenReturn(userId);
        doThrow(new IllegalArgumentException("Days must be a positive number."))
                .when(advertisingValidator).validateDate(advertisingRequest);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                advertisingService.buyAdvertising(advertisingRequest)
        );

        // Assert
        assertEquals("Days must be a positive number.", exception.getMessage());
        verify(advertisingValidator).validateDate(advertisingRequest);
        verify(advertisingValidator, never()).validatePostForAdvertising(anyLong());
        verify(advertisingValidator, never()).validatePayment(anyLong(), anyInt());
        verify(adRepository, never()).save(any(Ad.class));
    }
}
