package com.iemr.common.controller.nhmdashboard;

import com.iemr.common.data.nhm_dashboard.AbandonCallSummary;
import com.iemr.common.service.nhm_dashboard.NHM_DashboardService;
import com.iemr.common.utils.response.OutputResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NationalHealthMissionDashboardControllerTest {

    @InjectMocks
    private NationalHealthMissionDashboardController nationalHealthMissionDashboardController;

    @Mock
    private NHM_DashboardService nHM_DashboardService;

    @Mock
    private Logger logger; // Mock the logger instance

    private Gson gson;

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        MockitoAnnotations.openMocks(this);

        // Manually inject the mock logger into the controller using reflection.
        // This is necessary because the logger field is private final and not exposed via a setter.
        Field loggerField = NationalHealthMissionDashboardController.class.getDeclaredField("logger");
        loggerField.setAccessible(true); // Allow access to the private field
        loggerField.set(nationalHealthMissionDashboardController, logger); // Set the mock logger instance

        // Initialize Gson for parsing the JSON string output from the controller.
        // The GsonBuilder configuration should match how OutputResponse.toString() serializes.
        gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation() // Exclude fields not marked with @Expose
                .setLongSerializationPolicy(com.google.gson.LongSerializationPolicy.STRING) // Handle Longs as Strings
                .create();
    }

    @Test
    void testPushAbandonCallsFromC_Zentrix_Success() throws Exception {
        // Arrange
        AbandonCallSummary abandonCallSummary = new AbandonCallSummary(); // POJO, no need to mock
        String serviceResponse = "{\"message\":\"Call pushed successfully\"}"; // Example valid JSON object string
        when(nHM_DashboardService.pushAbandonCalls(any(AbandonCallSummary.class))).thenReturn(serviceResponse);

        // Act
        String result = nationalHealthMissionDashboardController.pushAbandonCallsFromC_Zentrix(abandonCallSummary);

        // Assert
        assertNotNull(result);
        OutputResponse output = gson.fromJson(result, OutputResponse.class);
        assertEquals(OutputResponse.SUCCESS, output.getStatusCode());
        assertEquals("Success", output.getStatus());
        assertEquals("Success", output.getErrorMessage());
        // For a valid JSON object string, OutputResponse.setResponse sets 'data' to the parsed JsonObject.
        // OutputResponse.getData() then returns the string representation of that JsonObject.
        assertEquals(serviceResponse, output.getData());
        verify(nHM_DashboardService).pushAbandonCalls(abandonCallSummary);
    }

    @Test
    void testPushAbandonCallsFromC_Zentrix_Exception() throws Exception {
        // Arrange
        AbandonCallSummary abandonCallSummary = new AbandonCallSummary();
        String errorMessage = "Service error during push";
        Exception testException = new Exception(errorMessage);
        doThrow(testException).when(nHM_DashboardService).pushAbandonCalls(any(AbandonCallSummary.class));

        // Act
        String result = nationalHealthMissionDashboardController.pushAbandonCallsFromC_Zentrix(abandonCallSummary);

        // Assert
        assertNotNull(result);
        OutputResponse output = gson.fromJson(result, OutputResponse.class);
        assertEquals(OutputResponse.GENERIC_FAILURE, output.getStatusCode());
        assertEquals(errorMessage, output.getStatus());
        assertEquals(errorMessage, output.getErrorMessage());
        verify(nHM_DashboardService).pushAbandonCalls(abandonCallSummary);
        // Verify that the error logger was called with the expected message
        verify(logger).error("error in NHM Push Abandon call API : " + errorMessage);
    }

    @Test
    void testGetAbandonCalls_Success() throws Exception {
        // Arrange
        String serviceResponse = "{\"calls\":[{\"id\":1,\"phone\":\"123\"}]}"; // Example valid JSON object string
        when(nHM_DashboardService.getAbandonCalls()).thenReturn(serviceResponse);

        // Act
        String result = nationalHealthMissionDashboardController.getAbandonCalls();

        // Assert
        assertNotNull(result);
        OutputResponse output = gson.fromJson(result, OutputResponse.class);
        assertEquals(OutputResponse.SUCCESS, output.getStatusCode());
        assertEquals("Success", output.getStatus());
        assertEquals("Success", output.getErrorMessage());
        JsonElement expectedJson = JsonParser.parseString(serviceResponse);
        JsonElement actualJson = JsonParser.parseString(output.getData());
          assertEquals(expectedJson, actualJson);
        verify(nHM_DashboardService).getAbandonCalls();
    }

    @Test
    void testGetAbandonCalls_Exception() throws Exception {
        // Arrange
        String errorMessage = "Failed to retrieve abandon calls";
        Exception testException = new Exception(errorMessage);
        doThrow(testException).when(nHM_DashboardService).getAbandonCalls();

        // Act
        String result = nationalHealthMissionDashboardController.getAbandonCalls();

        // Assert
        assertNotNull(result);
        OutputResponse output = gson.fromJson(result, OutputResponse.class);
        assertEquals(OutputResponse.GENERIC_FAILURE, output.getStatusCode());
        assertEquals(errorMessage, output.getStatus());
        assertEquals(errorMessage, output.getErrorMessage());
        verify(nHM_DashboardService).getAbandonCalls();
        // Verify that the error logger was called
        verify(logger).error("error in get Abandon call API : " + errorMessage);
    }

    @Test
    void testGetAgentSummaryReport_Success() throws Exception {
        // Arrange
        String serviceResponse = "[{\"agentName\":\"John Doe\",\"totalCalls\":10}]"; // Example valid JSON array string
        when(nHM_DashboardService.getAgentSummaryReport()).thenReturn(serviceResponse);

        // Act
        String result = nationalHealthMissionDashboardController.getAgentSummaryReport();

        // Assert
        assertNotNull(result);
        OutputResponse output = gson.fromJson(result, OutputResponse.class);
        assertEquals(OutputResponse.SUCCESS, output.getStatusCode());
        assertEquals("Success", output.getStatus());
        assertEquals("Success", output.getErrorMessage());
        JsonElement expectedJson = JsonParser.parseString(serviceResponse);
        JsonElement actualJson = JsonParser.parseString(output.getData());
        assertEquals(expectedJson, actualJson);

        verify(nHM_DashboardService).getAgentSummaryReport();
    }

    @Test
    void testGetAgentSummaryReport_Exception() throws Exception {
        // Arrange
        String errorMessage = "Error fetching agent summary";
        Exception testException = new Exception(errorMessage);
        doThrow(testException).when(nHM_DashboardService).getAgentSummaryReport();

        // Act
        String result = nationalHealthMissionDashboardController.getAgentSummaryReport();

        // Assert
        assertNotNull(result);
        OutputResponse output = gson.fromJson(result, OutputResponse.class);
        assertEquals(OutputResponse.GENERIC_FAILURE, output.getStatusCode());
        assertEquals(errorMessage, output.getStatus());
        assertEquals(errorMessage, output.getErrorMessage());
        verify(nHM_DashboardService).getAgentSummaryReport();
        // Verify that the error logger was called
        verify(logger).error("error in get agent summary report API : " + errorMessage);
    }

    @Test
    void testGetDetailedCallReport_Success() throws Exception {
        // Arrange
        String serviceResponse = "[{\"callId\":\"abc\",\"duration\":120}]"; // Example valid JSON array string
        when(nHM_DashboardService.getDetailedCallReport()).thenReturn(serviceResponse);

        // Act
        String result = nationalHealthMissionDashboardController.getDetailedCallReport();

        // Assert
        assertNotNull(result);
        OutputResponse output = gson.fromJson(result, OutputResponse.class);
        assertEquals(OutputResponse.SUCCESS, output.getStatusCode());
        assertEquals("Success", output.getStatus());
        assertEquals("Success", output.getErrorMessage());
        assertEquals(serviceResponse, output.getData());
        verify(nHM_DashboardService).getDetailedCallReport();
    }

    @Test
    void testGetDetailedCallReport_Exception() throws Exception {
        // Arrange
        String errorMessage = "Error fetching detailed call report";
        Exception testException = new Exception(errorMessage);
        doThrow(testException).when(nHM_DashboardService).getDetailedCallReport();

        // Act
        String result = nationalHealthMissionDashboardController.getDetailedCallReport();

        // Assert
        assertNotNull(result);
        OutputResponse output = gson.fromJson(result, OutputResponse.class);
        assertEquals(OutputResponse.GENERIC_FAILURE, output.getStatusCode());
        assertEquals(errorMessage, output.getStatus());
        assertEquals(errorMessage, output.getErrorMessage());
        verify(nHM_DashboardService).getDetailedCallReport();
        // Verify that the error logger was called
        verify(logger).error("error in get detailed call report API : " + errorMessage);
    }
}