package faang.school.postservice.controller.advertising;

import com.fasterxml.jackson.databind.ObjectMapper;
import faang.school.postservice.config.context.UserContext;
import faang.school.postservice.dto.adversting.AdDto;
import faang.school.postservice.dto.adversting.AdvertisingRequest;
import faang.school.postservice.service.adversting.AdvertisingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDateTime;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(AdvertisingController.class)
public class AdvertisingControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdvertisingService advertisingService;

    @MockBean
    private UserContext userContext;

    private ObjectMapper objectMapper = new ObjectMapper();
    @Test
    void buyAdvertising_ShouldReturnAdDto_WhenValidRequest() throws Exception {
        // Arrange
        AdvertisingRequest request = new AdvertisingRequest(7, 1L); // 7 дней, пост ID = 1
        AdDto adDto = new AdDto(
                1L, // postId
                2L, // userId
                7,  // duration
                100, // price
                LocalDateTime.now(), // startDate
                LocalDateTime.now().plusDays(7) // endDate
        );
        when(advertisingService.buyAdvertising(request)).thenReturn(adDto);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/advertising/buy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))) // Передаём тело запроса в JSON
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.postId").value(1L))
                .andExpect(MockMvcResultMatchers.jsonPath("$.userId").value(2L))
                .andExpect(MockMvcResultMatchers.jsonPath("$.duration").value(7))
                .andExpect(MockMvcResultMatchers.jsonPath("$.price").value(100));

        verify(advertisingService).buyAdvertising(request);
    }


    @Test
    void buyAdvertising_ShouldReturnInternalServerError_WhenServiceThrowsException() throws Exception {
        // Arrange
        AdvertisingRequest request = new AdvertisingRequest(7, 1L); // 7 дней, пост ID = 1

        when(advertisingService.buyAdvertising(request))
                .thenThrow(new RuntimeException("Internal server error"));

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/advertising/buy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
        verify(advertisingService).buyAdvertising(request);
    }

}
