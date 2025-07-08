package com.iemr.common.controller.uptsu;
import com.iemr.common.service.uptsu.UptsuService;
import com.iemr.common.utils.response.OutputResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UPTechnicalSupportControllerTest {
    private MockMvc mockMvc;
    private UptsuService uptsuService;

    @BeforeEach
    void setUp() throws Exception {
        uptsuService = Mockito.mock(UptsuService.class);
        UPTechnicalSupportController controller = new UPTechnicalSupportController();
        // Use reflection to inject the mock into the private field
        java.lang.reflect.Field serviceField = UPTechnicalSupportController.class.getDeclaredField("uptsuService");
        serviceField.setAccessible(true);
        serviceField.set(controller, uptsuService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void shouldReturnFacilityData_whenGetFacilityIsSuccessful() throws Exception {
        Integer providerServiceMapID = 1;
        String blockName = "TestBlock";
        String mockServiceResponseData = "{\"id\":1,\"name\":\"Test Facility\"}";

        when(uptsuService.getFacility(providerServiceMapID, blockName)).thenReturn(mockServiceResponseData);

        mockMvc.perform(get("/uptsu/get/facilityMaster/{providerServiceMapID}/{blockName}", providerServiceMapID, blockName)
                .header("Authorization", "Bearer token")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Test Facility"));
    }

    @Test
    void shouldReturnError_whenGetFacilityThrowsException() throws Exception {
        Integer providerServiceMapID = 1;
        String blockName = "TestBlock";
        String errorMessage = "Service unavailable";

        when(uptsuService.getFacility(providerServiceMapID, blockName)).thenThrow(new RuntimeException(errorMessage));

        mockMvc.perform(get("/uptsu/get/facilityMaster/{providerServiceMapID}/{blockName}", providerServiceMapID, blockName)
                .header("Authorization", "Bearer token")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorMessage").value(errorMessage));
    }

    @Test
    void shouldReturnSuccess_whenSaveAppointmentDetailsIsSuccessful() throws Exception {
        String requestBody = "{\"appointmentId\":123,\"details\":\"some details\"}";
        String authorizationHeader = "Bearer token";
        String mockServiceResponse = "Appointment saved successfully";

        when(uptsuService.saveAppointmentDetails(requestBody, authorizationHeader)).thenReturn(mockServiceResponse);

        mockMvc.perform(post("/uptsu/save/appointment-details")
                .header("Authorization", authorizationHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.response").value(mockServiceResponse));
    }

    @Test
    void shouldReturnError_whenSaveAppointmentDetailsThrowsException() throws Exception {
        String requestBody = "{\"appointmentId\":123,\"details\":\"some details\"}";
        String authorizationHeader = "Bearer token";
        String errorMessage = "Failed to save appointment";

        when(uptsuService.saveAppointmentDetails(requestBody, authorizationHeader)).thenThrow(new RuntimeException(errorMessage));

        mockMvc.perform(post("/uptsu/save/appointment-details")
                .header("Authorization", authorizationHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorMessage").value(errorMessage));
    }

    @Test
    void shouldReturnError_whenSaveAppointmentDetailsHasInvalidRequestBody() throws Exception {
        String invalidRequestBody = "{invalid json}";
        String authorizationHeader = "Bearer token";
        String errorMessage = "JSON parse error";

        when(uptsuService.saveAppointmentDetails(anyString(), anyString())).thenThrow(new RuntimeException(errorMessage));

        mockMvc.perform(post("/uptsu/save/appointment-details")
                .header("Authorization", authorizationHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequestBody)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorMessage").value(errorMessage));
    }
}