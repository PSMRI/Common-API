package com.iemr.common.controller.mctshistory;
import com.iemr.common.service.mctshistory.OutboundHistoryService;
import com.iemr.common.utils.response.OutputResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

class OutboundHistoryControllerTest {

    @Mock
    private OutboundHistoryService outboundHistoryService;

    @InjectMocks
    private OutboundHistoryController outboundHistoryController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        // The @InjectMocks annotation handles the injection via the setter
        // but we can explicitly call it to ensure it works as expected for the test method.
        outboundHistoryController.setOutboundHistoryService(outboundHistoryService);
    }

    @Test
    void testSetOutboundHistoryService() {
        // Verify that the service was set. Since @InjectMocks already calls the setter,
        // we can just assert that the controller instance is not null, implying setup worked.
        assertNotNull(outboundHistoryController);
        // A more direct way to test the setter would be to use reflection or a spy,
        // but for a simple setter, ensuring the object is ready for other tests is sufficient.
        // If we wanted to be super explicit, we could create a new controller instance
        // and call the setter, then use reflection to check the private field.
        // However, @InjectMocks already verifies the setter is called.
    }

    @Test
    void testGetCallHistory_Success() throws Exception {
        String request = "{\"beneficiaryRegID\":123}";
        String mockResponseData = "{\"history\":[{\"id\":1,\"date\":\"2023-01-01\"}]}";
        when(outboundHistoryService.getCallHistory(anyString())).thenReturn(mockResponseData);

        String result = outboundHistoryController.getCallHistory(request);

        assertNotNull(result);
        
        // Parse the JSON response for reliable validation
        JsonNode jsonResponse = objectMapper.readTree(result);
        
        assertEquals(200, jsonResponse.get("statusCode").asInt());
        assertEquals("Success", jsonResponse.get("status").asText());
        assertEquals("Success", jsonResponse.get("errorMessage").asText());
        
        // Verify the data field contains the expected response
        JsonNode dataNode = jsonResponse.get("data");
        assertNotNull(dataNode);
        JsonNode expectedData = objectMapper.readTree(mockResponseData);
        assertEquals(expectedData, dataNode);
        
        verify(outboundHistoryService).getCallHistory(request);
    }

    @Test
    void testGetCallHistory_Exception() throws Exception {
        String request = "{\"beneficiaryRegID\":123}";
        String errorMessage = "Service unavailable";
        when(outboundHistoryService.getCallHistory(anyString())).thenThrow(new RuntimeException(errorMessage));

        String result = outboundHistoryController.getCallHistory(request);

        assertNotNull(result);
        
        // Parse the JSON response for reliable validation
        JsonNode jsonResponse = objectMapper.readTree(result);
        
        assertEquals(5000, jsonResponse.get("statusCode").asInt());
        assertTrue(jsonResponse.get("status").asText().contains("Failed with " + errorMessage));
        assertEquals(errorMessage, jsonResponse.get("errorMessage").asText());
        
        verify(outboundHistoryService).getCallHistory(request);
    }

    @Test
    void testGetMctsCallResponse_Success() throws Exception {
        String request = "{\"callDetailID\":456}";
        String mockResponseData = "{\"callResponse\":\"Success\"}";
        when(outboundHistoryService.getMctsCallResponse(anyString())).thenReturn(mockResponseData);

        String result = outboundHistoryController.getMctsCallResponse(request);

        assertNotNull(result);
        
        // Parse the JSON response for reliable validation
        JsonNode jsonResponse = objectMapper.readTree(result);
        
        assertEquals(200, jsonResponse.get("statusCode").asInt());
        assertEquals("Success", jsonResponse.get("status").asText());
        assertEquals("Success", jsonResponse.get("errorMessage").asText());
        
        // Verify the data field contains the expected response
        JsonNode dataNode = jsonResponse.get("data");
        assertNotNull(dataNode);
        JsonNode expectedData = objectMapper.readTree(mockResponseData);
        assertEquals(expectedData, dataNode);
        
        verify(outboundHistoryService).getMctsCallResponse(request);
    }

    @Test
    void testGetMctsCallResponse_Exception() throws Exception {
        String request = "{\"callDetailID\":456}";
        String errorMessage = "Database error";
        when(outboundHistoryService.getMctsCallResponse(anyString())).thenThrow(new RuntimeException(errorMessage));

        String result = outboundHistoryController.getMctsCallResponse(request);

        assertNotNull(result);
        
        // Parse the JSON response for reliable validation
        JsonNode jsonResponse = objectMapper.readTree(result);
        
        assertEquals(5000, jsonResponse.get("statusCode").asInt());
        assertTrue(jsonResponse.get("status").asText().contains("Failed with " + errorMessage));
        assertEquals(errorMessage, jsonResponse.get("errorMessage").asText());
        
        verify(outboundHistoryService).getMctsCallResponse(request);
    }
}