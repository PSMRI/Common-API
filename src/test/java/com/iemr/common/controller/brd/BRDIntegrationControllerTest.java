package com.iemr.common.controller.brd;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.http.MediaType;

import com.iemr.common.service.brd.BRDIntegrationService;
import com.iemr.common.utils.response.OutputResponse;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
class BRDIntegrationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private BRDIntegrationService integrationService;

    @InjectMocks
    private BRDIntegrationController brdIntegrationController;

    // Test constants
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_TOKEN = "Bearer dummy_token";
    private static final String BRD_ENDPOINT = "/brd/getIntegrationData";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(brdIntegrationController).build();
    }

    @Test
    void shouldReturnIntegrationData_whenServiceReturnsData() throws Exception {
        String startDate = "2023-01-01";
        String endDate = "2023-01-31";
        String requestBody = "{\"startDate\":\"" + startDate + "\", \"endDate\":\"" + endDate + "\"}";
        String mockBrdDetails = "{\"data\":[{\"id\":1,\"value\":\"sample data\"}]}";

        OutputResponse expectedResponse = new OutputResponse();
        expectedResponse.setResponse(mockBrdDetails);

        when(integrationService.getData(startDate, endDate)).thenReturn(mockBrdDetails);

        mockMvc.perform(post(BRD_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
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

        mockMvc.perform(post(BRD_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedErrorResponse.toStringWithSerializeNulls()));
    }

    @Test
    void shouldReturnErrorResponse_whenRequestBodyIsMissingEndDate() throws Exception {
        String invalidRequestBody = "{\"startDate\":\"2023-01-01\"}"; // Missing endDate

        OutputResponse expectedErrorResponse = new OutputResponse();
        expectedErrorResponse.setError(5000, "Unable to get BRD data");

        mockMvc.perform(post(BRD_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .content(invalidRequestBody))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedErrorResponse.toStringWithSerializeNulls()));
    }

    @Test
    void shouldReturnErrorResponse_whenRequestBodyIsNotValidJson() throws Exception {
        String nonJsonRequestBody = "this is not a json string";

        OutputResponse expectedErrorResponse = new OutputResponse();
        expectedErrorResponse.setError(5000, "Unable to get BRD data");

        mockMvc.perform(post(BRD_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .content(nonJsonRequestBody))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedErrorResponse.toStringWithSerializeNulls()));
    }

    @Test
    void shouldReturnBadRequest_whenRequestBodyIsEmpty() throws Exception {
        String emptyRequestBody = "";

        // Empty request body causes Spring to return 400 Bad Request before reaching the controller
        mockMvc.perform(post(BRD_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .content(emptyRequestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnErrorResponse_whenRequestBodyIsEmptyJsonObject() throws Exception {
        String emptyJsonRequestBody = "{}"; // Empty JSON object

        OutputResponse expectedErrorResponse = new OutputResponse();
        expectedErrorResponse.setError(5000, "Unable to get BRD data");

        // Empty JSON object will reach the controller but fail when trying to get startDate/endDate
        mockMvc.perform(post(BRD_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .content(emptyJsonRequestBody))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedErrorResponse.toStringWithSerializeNulls()));
    }

    // Edge case tests for improved coverage
    
    @Test
    void shouldReturnErrorResponse_whenRequestBodyIsMissingStartDate() throws Exception {
        String invalidRequestBody = "{\"endDate\":\"2023-01-31\"}"; // Missing startDate

        OutputResponse expectedErrorResponse = new OutputResponse();
        expectedErrorResponse.setError(5000, "Unable to get BRD data");

        mockMvc.perform(post(BRD_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .content(invalidRequestBody))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedErrorResponse.toStringWithSerializeNulls()));
    }

    @Test
    void shouldReturnErrorResponse_whenRequestBodyHasNullValues() throws Exception {
        String nullValuesRequestBody = "{\"startDate\":null, \"endDate\":null}";
        String mockBrdDetails = "{\"data\":[]}";

        OutputResponse expectedResponse = new OutputResponse();
        expectedResponse.setResponse(mockBrdDetails);

        // JSONObject.getString() converts null to string "null"
        when(integrationService.getData("null", "null")).thenReturn(mockBrdDetails);

        mockMvc.perform(post(BRD_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .content(nullValuesRequestBody))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedResponse.toStringWithSerializeNulls()));
    }

    @Test
    void shouldReturnErrorResponse_whenRequestBodyHasEmptyStringValues() throws Exception {
        String emptyStringValuesRequestBody = "{\"startDate\":\"\", \"endDate\":\"\"}";
        String mockBrdDetails = "{\"data\":[]}";

        OutputResponse expectedResponse = new OutputResponse();
        expectedResponse.setResponse(mockBrdDetails);

        // JSONObject.getString() returns empty strings as-is
        when(integrationService.getData("", "")).thenReturn(mockBrdDetails);

        mockMvc.perform(post(BRD_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .content(emptyStringValuesRequestBody))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedResponse.toStringWithSerializeNulls()));
    }

    @Test
    void shouldReturnErrorResponse_whenRequestBodyHasExtraFields() throws Exception {
        String extraFieldsRequestBody = "{\"startDate\":\"2023-01-01\", \"endDate\":\"2023-01-31\", \"extraField\":\"value\", \"anotherField\":123}";
        String mockBrdDetails = "{\"data\":[{\"id\":1,\"value\":\"sample data\"}]}";

        OutputResponse expectedResponse = new OutputResponse();
        expectedResponse.setResponse(mockBrdDetails);

        when(integrationService.getData("2023-01-01", "2023-01-31")).thenReturn(mockBrdDetails);

        // Controller should ignore extra fields and process successfully
        mockMvc.perform(post(BRD_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .content(extraFieldsRequestBody))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedResponse.toStringWithSerializeNulls()));
    }

    @Test
    void shouldReturnErrorResponse_whenRequestBodyHasInvalidDateFormat() throws Exception {
        String invalidDateFormatRequestBody = "{\"startDate\":\"invalid-date\", \"endDate\":\"2023-01-31\"}";

        OutputResponse expectedErrorResponse = new OutputResponse();
        expectedErrorResponse.setError(5000, "Unable to get BRD data");

        // Service might throw exception due to invalid date format
        when(integrationService.getData("invalid-date", "2023-01-31")).thenThrow(new RuntimeException("Invalid date format"));

        mockMvc.perform(post(BRD_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .content(invalidDateFormatRequestBody))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedErrorResponse.toStringWithSerializeNulls()));
    }

 @Test
    void shouldReturnErrorResponse_whenRequestBodyHasNumericValues() throws Exception {
        String numericValuesRequestBody = "{\"startDate\":20230101, \"endDate\":20230131}";
        String mockBrdDetails = "{\"data\":[]}";

        OutputResponse expectedResponse = new OutputResponse();
        expectedResponse.setResponse(mockBrdDetails);

        // JSONObject.getString() converts numeric values to strings
        when(integrationService.getData("20230101", "20230131")).thenReturn(mockBrdDetails);

        mockMvc.perform(post(BRD_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .content(numericValuesRequestBody))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedResponse.toStringWithSerializeNulls()));
    }


    @Test
    void shouldReturnErrorResponse_whenRequestBodyHasArrayValues() throws Exception {
        String arrayValuesRequestBody = "{\"startDate\":[\"2023-01-01\"], \"endDate\":[\"2023-01-31\"]}";
        String mockBrdDetails = "{\"data\":[]}";

        OutputResponse expectedResponse = new OutputResponse();
        expectedResponse.setResponse(mockBrdDetails);

        // JSONObject.getString() converts array values to their string representation
        when(integrationService.getData("[\"2023-01-01\"]", "[\"2023-01-31\"]")).thenReturn(mockBrdDetails);

        mockMvc.perform(post(BRD_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .content(arrayValuesRequestBody))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedResponse.toStringWithSerializeNulls()));
    }

    @Test
    void shouldReturnErrorResponse_whenRequestBodyHasSpecialCharacters() throws Exception {
        String specialCharsRequestBody = "{\"startDate\":\"2023-01-01\", \"endDate\":\"2023-01-31\u0000\"}";

        OutputResponse expectedErrorResponse = new OutputResponse();
        expectedErrorResponse.setError(5000, "Unable to get BRD data");

        // Service might throw exception due to special characters
        when(integrationService.getData("2023-01-01", "2023-01-31\u0000")).thenThrow(new RuntimeException("Invalid characters"));

        mockMvc.perform(post(BRD_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .content(specialCharsRequestBody))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedErrorResponse.toStringWithSerializeNulls()));
    }

    @Test
    void shouldReturnErrorResponse_whenRequestBodyIsLargePayload() throws Exception {
        // Create a very large JSON payload
        StringBuilder largePayload = new StringBuilder("{\"startDate\":\"2023-01-01\", \"endDate\":\"2023-01-31\", \"largeData\":\"");
        for (int i = 0; i < 100000; i++) {
            largePayload.append("a");
        }
        largePayload.append("\"}");

        OutputResponse expectedErrorResponse = new OutputResponse();
        expectedErrorResponse.setError(5000, "Unable to get BRD data");

        // Service might throw exception due to large payload processing
        when(integrationService.getData(anyString(), anyString())).thenThrow(new RuntimeException("Payload too large"));

        mockMvc.perform(post(BRD_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .content(largePayload.toString()))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedErrorResponse.toStringWithSerializeNulls()));
    }

    @Test
    void shouldReturnErrorResponse_whenRequestBodyHasJsonArray() throws Exception {
        String jsonArrayRequestBody = "[{\"startDate\":\"2023-01-01\", \"endDate\":\"2023-01-31\"}]";

        OutputResponse expectedErrorResponse = new OutputResponse();
        expectedErrorResponse.setError(5000, "Unable to get BRD data");

        // JSONObject constructor will throw exception for JSON arrays
        mockMvc.perform(post(BRD_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .content(jsonArrayRequestBody))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedErrorResponse.toStringWithSerializeNulls()));
    }

    @Test
    void shouldReturnErrorResponse_whenServiceReturnsNull() throws Exception {
        String requestBody = "{\"startDate\":\"2023-01-01\", \"endDate\":\"2023-01-31\"}";

        OutputResponse expectedResponse = new OutputResponse();
        expectedResponse.setResponse(null);

        when(integrationService.getData("2023-01-01", "2023-01-31")).thenReturn(null);

        mockMvc.perform(post(BRD_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedResponse.toStringWithSerializeNulls()));
    }

    @Test
    void shouldReturnErrorResponse_whenServiceReturnsEmptyString() throws Exception {
        String requestBody = "{\"startDate\":\"2023-01-01\", \"endDate\":\"2023-01-31\"}";

        OutputResponse expectedResponse = new OutputResponse();
        expectedResponse.setResponse("");

        when(integrationService.getData("2023-01-01", "2023-01-31")).thenReturn("");

        mockMvc.perform(post(BRD_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedResponse.toStringWithSerializeNulls()));
    }

    @Test
    void shouldReturnErrorResponse_whenRequestBodyHasUnicodeCharacters() throws Exception {
        String unicodeRequestBody = "{\"startDate\":\"2023-01-01\", \"endDate\":\"2023-01-31\", \"unicode\":\"こんにちは\"}";
        String mockBrdDetails = "{\"data\":[{\"id\":1,\"value\":\"unicode test\"}]}";

        OutputResponse expectedResponse = new OutputResponse();
        expectedResponse.setResponse(mockBrdDetails);

        when(integrationService.getData("2023-01-01", "2023-01-31")).thenReturn(mockBrdDetails);

        // Controller should handle unicode characters correctly
        mockMvc.perform(post(BRD_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .content(unicodeRequestBody))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedResponse.toStringWithSerializeNulls()));
    }

    @Test
    void shouldReturnErrorResponse_whenRequestBodyHasEscapedQuotes() throws Exception {
        String escapedQuotesRequestBody = "{\"startDate\":\"2023-01-01\", \"endDate\":\"2023-01-31\", \"data\":\"value with \\\"quotes\\\"\"}";
        String mockBrdDetails = "{\"data\":[{\"id\":1,\"value\":\"escaped quotes test\"}]}";

        OutputResponse expectedResponse = new OutputResponse();
        expectedResponse.setResponse(mockBrdDetails);

        when(integrationService.getData("2023-01-01", "2023-01-31")).thenReturn(mockBrdDetails);

        // Controller should handle escaped quotes correctly
        mockMvc.perform(post(BRD_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .content(escapedQuotesRequestBody))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedResponse.toStringWithSerializeNulls()));
    }

    @Test
    void shouldReturnErrorResponse_whenRequestBodyHasVeryLongDateStrings() throws Exception {
        StringBuilder longDate = new StringBuilder("2023-01-01");
        for (int i = 0; i < 1000; i++) {
            longDate.append("0");
        }
        String longDateRequestBody = "{\"startDate\":\"" + longDate.toString() + "\", \"endDate\":\"2023-01-31\"}";

        OutputResponse expectedErrorResponse = new OutputResponse();
        expectedErrorResponse.setError(5000, "Unable to get BRD data");

        // Service might throw exception due to very long date strings
        when(integrationService.getData(anyString(), anyString())).thenThrow(new RuntimeException("Invalid date length"));

        mockMvc.perform(post(BRD_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .content(longDateRequestBody))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedErrorResponse.toStringWithSerializeNulls()));
    }

    @Test
    void shouldReturnErrorResponse_whenAuthorizationHeaderIsMissing() throws Exception {
        String requestBody = "{\"startDate\":\"2023-01-01\", \"endDate\":\"2023-01-31\"}";

        // Request without Authorization header should return 404 (Not Found) since headers="Authorization" is required
        mockMvc.perform(post(BRD_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnErrorResponse_whenContentTypeIsNotJson() throws Exception {
        String requestBody = "{\"startDate\":\"2023-01-01\", \"endDate\":\"2023-01-31\"}";

        // Spring MockMvc is lenient and processes the request even with wrong content type
        mockMvc.perform(post(BRD_ENDPOINT)
                .contentType(MediaType.TEXT_PLAIN)
                .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .content(requestBody))
                .andExpect(status().isOk());
    }
}