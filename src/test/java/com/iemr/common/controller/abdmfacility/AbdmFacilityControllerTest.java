package com.iemr.common.controller.abdmfacility;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;

import com.iemr.common.service.abdmfacility.AbdmFacilityService;
import com.iemr.common.utils.response.OutputResponse;

@WebMvcTest(controllers = AbdmFacilityController.class, 
            excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class})
@ContextConfiguration(classes = {AbdmFacilityController.class})
class AbdmFacilityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AbdmFacilityService abdmFacilityService;

    private final String AUTH_HEADER_VALUE = "Bearer some_valid_token";

    @Test
    void shouldReturnAbdmFacilityDetails_whenServiceReturnsData() throws Exception {
        int workLocationId = 123;
        String mockServiceResponse = "{\"facilityId\": \"1234\", \"facilityName\": \"Test Facility\"}";
        
        OutputResponse outputResponse = new OutputResponse();
        outputResponse.setResponse(mockServiceResponse);
        String expectedResponseBody = outputResponse.toString();

        when(abdmFacilityService.getMappedAbdmFacility(workLocationId)).thenReturn(mockServiceResponse);

        mockMvc.perform(get("/facility/getWorklocationMappedAbdmFacility/{workLocationId}", workLocationId)
                .header("Authorization", AUTH_HEADER_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResponseBody));
        
        verify(abdmFacilityService).getMappedAbdmFacility(workLocationId);
    }

    @Test
    void shouldReturnErrorResponse_whenServiceThrowsException() throws Exception {
        int workLocationId = 456;
        String errorMessage = "Internal service error occurred";
        
        OutputResponse outputResponse = new OutputResponse();
        outputResponse.setError(5000, errorMessage);
        String expectedResponseBody = outputResponse.toString();

        when(abdmFacilityService.getMappedAbdmFacility(workLocationId)).thenThrow(new RuntimeException(errorMessage));

        mockMvc.perform(get("/facility/getWorklocationMappedAbdmFacility/{workLocationId}", workLocationId)
                .header("Authorization", AUTH_HEADER_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResponseBody));
        
        verify(abdmFacilityService).getMappedAbdmFacility(workLocationId);
    }

    @Test
    void shouldReturnBadRequest_whenAuthorizationHeaderIsMissing() throws Exception {
        int workLocationId = 789;

        // The controller method requires Authorization header, so missing header should return 400
        mockMvc.perform(get("/facility/getWorklocationMappedAbdmFacility/{workLocationId}", workLocationId))
                .andExpect(status().isBadRequest());
        
        // Service should not be called when required header is missing
        verifyNoInteractions(abdmFacilityService);
    }
}