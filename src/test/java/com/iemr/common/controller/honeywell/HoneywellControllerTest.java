package com.iemr.common.controller.honeywell;

import com.iemr.common.service.honeywell.HoneywellService;
import com.iemr.common.utils.response.OutputResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@ExtendWith(MockitoExtension.class)
class HoneywellControllerTest {

    @InjectMocks
    private HoneywellController honeywellController;

    @Mock
    private HoneywellService honeywellService;

    @Mock
    private Logger mockLogger;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(honeywellController, "logger", mockLogger);
    }

    @Test
    void testGetRealtimeDistrictWiseCallReport_Success() throws Exception {
        String expectedServiceResponse = "{\"data\":\"realtime_report_data\"}";
        when(honeywellService.getRealtimeDistrictWiseCallReport()).thenReturn(expectedServiceResponse);

        String result = honeywellController.getRealtimeDistrictWiseCallReport();
        assertNotNull(result);

        JsonObject jsonResult = JsonParser.parseString(result).getAsJsonObject();
        assertEquals(OutputResponse.SUCCESS, jsonResult.get("statusCode").getAsInt());
        assertEquals("Success", jsonResult.get("status").getAsString());
        assertEquals("Success", jsonResult.get("errorMessage").getAsString());
        assertEquals(JsonParser.parseString(expectedServiceResponse), jsonResult.get("data"));

        verify(honeywellService).getRealtimeDistrictWiseCallReport();
    }

    @Test
    void testGetRealtimeDistrictWiseCallReport_Exception() throws Exception {
        RuntimeException exception = new RuntimeException("Service unavailable");
        when(honeywellService.getRealtimeDistrictWiseCallReport()).thenThrow(exception);

        String result = honeywellController.getRealtimeDistrictWiseCallReport();
        assertNotNull(result);

        JsonObject jsonResult = JsonParser.parseString(result).getAsJsonObject();
        assertEquals(OutputResponse.GENERIC_FAILURE, jsonResult.get("statusCode").getAsInt());
        assertTrue(jsonResult.get("status").getAsString().startsWith("Failed with Service unavailable at"));
        assertEquals("Service unavailable", jsonResult.get("errorMessage").getAsString());

        verify(honeywellService).getRealtimeDistrictWiseCallReport();
        verify(mockLogger).error(any(String.class), eq(exception));
    }

    @Test
    void testGetDistrictWiseCallReport_Success() throws Exception {
        String requestBody = "{\"district\":\"someDistrict\"}";
        String expectedServiceResponse = "{\"data\":\"district_report_data\"}";
        when(honeywellService.getDistrictWiseCallReport(requestBody)).thenReturn(expectedServiceResponse);

        String result = honeywellController.getDistrictWiseCallReport(requestBody, null);
        assertNotNull(result);

        JsonObject jsonResult = JsonParser.parseString(result).getAsJsonObject();
        assertEquals(OutputResponse.SUCCESS, jsonResult.get("statusCode").getAsInt());
        assertEquals("Success", jsonResult.get("status").getAsString());
        assertEquals("Success", jsonResult.get("errorMessage").getAsString());
        assertEquals(JsonParser.parseString(expectedServiceResponse), jsonResult.get("data"));

        verify(honeywellService).getDistrictWiseCallReport(requestBody);
        verify(mockLogger).info("getDistrictWiseCallReport request " + requestBody);
    }

    @Test
    void testGetDistrictWiseCallReport_Exception() throws Exception {
        String requestBody = "{\"district\":\"someDistrict\"}";
        Exception exception = new Exception("District report failed");
        when(honeywellService.getDistrictWiseCallReport(requestBody)).thenThrow(exception);

        String result = honeywellController.getDistrictWiseCallReport(requestBody, null);
        assertNotNull(result);

        JsonObject jsonResult = JsonParser.parseString(result).getAsJsonObject();
        assertEquals(OutputResponse.GENERIC_FAILURE, jsonResult.get("statusCode").getAsInt());
        assertTrue(jsonResult.get("status").getAsString().startsWith("Failed with District report failed at"));
        assertEquals("District report failed", jsonResult.get("errorMessage").getAsString());

        verify(honeywellService).getDistrictWiseCallReport(requestBody);
        verify(mockLogger).info("getDistrictWiseCallReport request " + requestBody);
        verify(mockLogger).error(any(String.class), eq(exception));
    }

    @Test
    void testGetDistrictWiseCallReport_RequestBodyEmpty() throws Exception {
        String requestBody = "";
        String expectedServiceResponse = "{\"status\":\"handledEmptyBody\"}";
        when(honeywellService.getDistrictWiseCallReport(requestBody)).thenReturn(expectedServiceResponse);

        String result = honeywellController.getDistrictWiseCallReport(requestBody, null);
        assertNotNull(result);

        JsonObject jsonResult = JsonParser.parseString(result).getAsJsonObject();
        assertEquals(OutputResponse.SUCCESS, jsonResult.get("statusCode").getAsInt());
        assertEquals(JsonParser.parseString(expectedServiceResponse), jsonResult.get("data"));

        verify(honeywellService).getDistrictWiseCallReport(requestBody);
        verify(mockLogger).info("getDistrictWiseCallReport request " + requestBody);
    }

    @Test
    void testGetDistrictWiseCallReport_RequestBodyNull() throws Exception {
        String requestBody = null;
        String expectedServiceResponse = "{\"status\":\"handledNullBody\"}";
        when(honeywellService.getDistrictWiseCallReport(requestBody)).thenReturn(expectedServiceResponse);

        String result = honeywellController.getDistrictWiseCallReport(requestBody, null);
        assertNotNull(result);

        JsonObject jsonResult = JsonParser.parseString(result).getAsJsonObject();
        assertEquals(OutputResponse.SUCCESS, jsonResult.get("statusCode").getAsInt());
        assertEquals(JsonParser.parseString(expectedServiceResponse), jsonResult.get("data"));

        verify(honeywellService).getDistrictWiseCallReport(requestBody);
        verify(mockLogger).info("getDistrictWiseCallReport request " + requestBody);
    }

    @Test
    void testGetUrbanRuralCallReport_Success() throws Exception {
        String requestBody = "{\"type\":\"urban\"}";
        String expectedServiceResponse = "{\"data\":\"urban_rural_report_data\"}";
        when(honeywellService.getUrbanRuralCallReport(requestBody)).thenReturn(expectedServiceResponse);

        String result = honeywellController.getUrbanRuralCallReport(requestBody, null);
        assertNotNull(result);

        JsonObject jsonResult = JsonParser.parseString(result).getAsJsonObject();
        assertEquals(OutputResponse.SUCCESS, jsonResult.get("statusCode").getAsInt());
        assertEquals(JsonParser.parseString(expectedServiceResponse), jsonResult.get("data"));

        verify(honeywellService).getUrbanRuralCallReport(requestBody);
        verify(mockLogger).info("getUrbanRuralCallReport request " + requestBody);
    }

    @Test
    void testGetUrbanRuralCallReport_Exception() throws Exception {
        String requestBody = "{\"type\":\"urban\"}";
        Exception exception = new Exception("Urban/rural report failed");
        when(honeywellService.getUrbanRuralCallReport(requestBody)).thenThrow(exception);

        String result = honeywellController.getUrbanRuralCallReport(requestBody, null);
        assertNotNull(result);

        JsonObject jsonResult = JsonParser.parseString(result).getAsJsonObject();
        assertEquals(OutputResponse.GENERIC_FAILURE, jsonResult.get("statusCode").getAsInt());
        assertTrue(jsonResult.get("status").getAsString().startsWith("Failed with Urban/rural report failed at"));
        assertEquals("Urban/rural report failed", jsonResult.get("errorMessage").getAsString());

        verify(honeywellService).getUrbanRuralCallReport(requestBody);
        verify(mockLogger).info("getUrbanRuralCallReport request " + requestBody);
        verify(mockLogger).error(any(String.class), eq(exception));
    }

    @Test
    void testGetUrbanRuralCallReport_RequestBodyNull_Exception() throws Exception {
        String requestBody = null;
        Exception exception = new IllegalArgumentException("Request body cannot be null");
        when(honeywellService.getUrbanRuralCallReport(requestBody)).thenThrow(exception);

        String result = honeywellController.getUrbanRuralCallReport(requestBody, null);
        assertNotNull(result);

        JsonObject jsonResult = JsonParser.parseString(result).getAsJsonObject();
        assertEquals(OutputResponse.GENERIC_FAILURE, jsonResult.get("statusCode").getAsInt());
        assertTrue(jsonResult.get("status").getAsString().startsWith("Failed with Request body cannot be null at"));
        assertEquals("Request body cannot be null", jsonResult.get("errorMessage").getAsString());

        verify(honeywellService).getUrbanRuralCallReport(requestBody);
        verify(mockLogger).info("getUrbanRuralCallReport request " + requestBody);
        verify(mockLogger).error(any(String.class), eq(exception));
    }
}
