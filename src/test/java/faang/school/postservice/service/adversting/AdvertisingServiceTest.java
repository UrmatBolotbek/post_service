package faang.school.postservice.service.adversting;

import faang.school.postservice.client.PaymentServiceClient;
import faang.school.postservice.config.context.UserContext;
import faang.school.postservice.dto.adversting.AdvertisingRequestDto;
import faang.school.postservice.mapper.adversting.AdMapper;
import faang.school.postservice.model.ad.Ad;
import faang.school.postservice.repository.ad.AdRepository;
import faang.school.postservice.validator.adversting.AdvertisingValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
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
import static org.mockito.Mockito.verifyNoInteractions;
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
    private AdvertisingRequestDto advertisingRequest;

    @Mock
    private UserContext userContext;
    @InjectMocks
    private AdvertisingService advertisingService;

    private AdMapper adMapper;

    @BeforeEach
    void setUp() {

        Clock fixedClock = Clock.fixed(Instant.parse("2024-12-15T15:51:25.624831Z"), ZoneId.systemDefault());
        adMapper = Mappers.getMapper(AdMapper.class);
        advertisingService = new AdvertisingService(adRepository, adMapper, advertisingValidator, paymentServiceClient);
    }

    @Test
    void buyAdvertising_InvalidPeriod_ShouldThrowException() {
        // Arrange
        long postId = 2L;
        AdvertisingRequestDto advertisingRequest = new AdvertisingRequestDto(0, postId); // 0 дней — недопустимый период

        doThrow(new IllegalArgumentException("Days must be a positive number."))
                .when(advertisingValidator).validateDate(advertisingRequest);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                    advertisingService.buyAdvertising(advertisingRequest,userContext.getUserId());
                }
        );

        // Assert
        assertEquals("Days must be a positive number.", exception.getMessage());
        verify(advertisingValidator).validateDate(advertisingRequest);
        verify(advertisingValidator, never()).validatePostForAdvertising(anyLong());
        verify(adRepository, never()).save(any(Ad.class));
        verifyNoInteractions(paymentServiceClient);
    }
}
