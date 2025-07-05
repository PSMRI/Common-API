package com.iemr.common.controller.brd;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import com.iemr.common.service.brd.BRDIntegrationService;
import com.iemr.common.utils.response.OutputResponse;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyString;

@WebMvcTest(controllers = BRDIntegrationController.class, 
            excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class})
@ContextConfiguration(classes = {BRDIntegrationController.class})
class BRDIntegrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BRDIntegrationService integrationService;

    @Test
    void shouldReturnIntegrationData_whenServiceReturnsData() throws Exception {
        String startDate = "2023-01-01";
        String endDate = "2023-01-31";
        String requestBody = "{\"startDate\":\"" + startDate + "\", \"endDate\":\"" + endDate + "\"}";
        String mockBrdDetails = "{\"data\":[{\"id\":1,\"value\":\"sample data\"}]}";

        OutputResponse expectedResponse = new OutputResponse();
        expectedResponse.setResponse(mockBrdDetails);

        when(integrationService.getData(startDate, endDate)).thenReturn(mockBrdDetails);

        mockMvc.perform(post("/brd/getIntegrationData")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer dummy_token")
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedResponse.toStringWithSerializeNulls()));
    }

    @Test
    void shouldReturnErrorResponse_whenServiceThrowsException() throws Exception {
        String startDate = "2023-01-01";
        String endDate = "2023-01-31";
        String requestBody = "{\"startDate\":\"" + startDate + "\", \"endDate\":\"" + endDate + "\"}";

        OutputResponse expectedErrorResponse = new OutputResponse();
        expectedErrorResponse.setError(5000, "Unable to get BRD data");

        when(integrationService.getData(anyString(), anyString())).thenThrow(new RuntimeException("Simulated service error"));

        mockMvc.perform(post("/brd/getIntegrationData")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer dummy_token")
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedErrorResponse.toStringWithSerializeNulls()));
    }

    @Test
    void shouldReturnErrorResponse_whenRequestBodyIsMissingEndDate() throws Exception {
        String invalidRequestBody = "{\"startDate\":\"2023-01-01\"}"; // Missing endDate

        OutputResponse expectedErrorResponse = new OutputResponse();
        expectedErrorResponse.setError(5000, "Unable to get BRD data");

        mockMvc.perform(post("/brd/getIntegrationData")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer dummy_token")
                .content(invalidRequestBody))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedErrorResponse.toStringWithSerializeNulls()));
    }

    @Test
    void shouldReturnErrorResponse_whenRequestBodyIsNotValidJson() throws Exception {
        String nonJsonRequestBody = "this is not a json string";

        OutputResponse expectedErrorResponse = new OutputResponse();
        expectedErrorResponse.setError(5000, "Unable to get BRD data");

        mockMvc.perform(post("/brd/getIntegrationData")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer dummy_token")
                .content(nonJsonRequestBody))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedErrorResponse.toStringWithSerializeNulls()));
    }

    @Test
    void shouldReturnBadRequest_whenRequestBodyIsEmpty() throws Exception {
        String emptyRequestBody = "";

        // Empty request body causes Spring to return 400 Bad Request before reaching the controller
        mockMvc.perform(post("/brd/getIntegrationData")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer dummy_token")
                .content(emptyRequestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnErrorResponse_whenRequestBodyIsEmptyJsonObject() throws Exception {
        String emptyJsonRequestBody = "{}"; // Empty JSON object

        OutputResponse expectedErrorResponse = new OutputResponse();
        expectedErrorResponse.setError(5000, "Unable to get BRD data");

        // Empty JSON object will reach the controller but fail when trying to get startDate/endDate
        mockMvc.perform(post("/brd/getIntegrationData")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer dummy_token")
                .content(emptyJsonRequestBody))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedErrorResponse.toStringWithSerializeNulls()));
    }
}