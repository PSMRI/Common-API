package com.iemr.common.controller.scheme;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iemr.common.data.scheme.Scheme;
import com.iemr.common.service.scheme.SchemeServiceImpl;
import com.iemr.common.utils.response.OutputResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import ch.qos.logback.classic.LoggerContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SchemeControllerTest {

    @InjectMocks
    private SchemeController schemeController;

    @Mock
    private SchemeServiceImpl schemeServiceImpl;

    private ObjectMapper objectMapper;
    private ListAppender<ILoggingEvent> listAppender;
    private ch.qos.logback.classic.Logger testLogger;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
         objectMapper.configure(com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        // Setup logging capture for the controller's logger
        testLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(SchemeController.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        testLogger.addAppender(listAppender);
        testLogger.setLevel(Level.INFO); // Set level to capture INFO and ERROR
    }

    // Helper to check if a log message exists
    private boolean logContains(String message, Level level) {
        return listAppender.list.stream()
                .anyMatch(event -> event.getMessage().contains(message) && event.getLevel().equals(level));
    }

    @Test
    void testSaveSchemeDetails_Success() throws Exception {
        // Arrange
        Scheme requestScheme = new Scheme();
        requestScheme.setSchemeName("Test Scheme");
        requestScheme.setSchemeDesc("Description");
        requestScheme.setCreatedBy("testuser");
        requestScheme.setProviderServiceMapID(1);

        String requestJson = objectMapper.writeValueAsString(requestScheme);

        Scheme savedScheme = new Scheme();
        savedScheme.setSchemeID(101);
        savedScheme.setSchemeName("Test Scheme");
        savedScheme.setSchemeDesc("Description");
        savedScheme.setCreatedBy("testuser");
        savedScheme.setProviderServiceMapID(1);

        when(schemeServiceImpl.save(any(Scheme.class))).thenReturn(savedScheme);

        // Act
        String response = schemeController.saveSchemeDetails(requestJson);

        // Assert
        verify(schemeServiceImpl, times(1)).save(any(Scheme.class));
        OutputResponse outputResponse = objectMapper.readValue(response, OutputResponse.class);
        assertTrue(outputResponse.isSuccess());
        assertTrue(outputResponse.getData().contains("\"schemeID\":101"));
        assertTrue(logContains("saveSchemeDetails response:", Level.INFO));
    }

    @Test
    void testSaveSchemeDetails_Exception() throws Exception {
        // Arrange
        Scheme requestScheme = new Scheme();
        requestScheme.setSchemeName("Test Scheme");
        String requestJson = objectMapper.writeValueAsString(requestScheme);

        String errorMessage = "Database error";
        when(schemeServiceImpl.save(any(Scheme.class))).thenThrow(new RuntimeException(errorMessage));

        // Act
        String response = schemeController.saveSchemeDetails(requestJson);

        // Assert
        verify(schemeServiceImpl, times(1)).save(any(Scheme.class));
        OutputResponse outputResponse = objectMapper.readValue(response, OutputResponse.class);
        assertFalse(outputResponse.isSuccess());
        assertTrue(outputResponse.getErrorMessage().contains(errorMessage));
        assertTrue(logContains("saveSchemeDetails failed with error " + errorMessage, Level.ERROR));
        assertTrue(logContains("saveSchemeDetails response:", Level.INFO));
    }

    @Test
    void testGetSchemeList_Success_WithSchemes() throws Exception {
        // Arrange
        Integer providerServiceMapID = 1;
        String requestJson = "{\"providerServiceMapID\":" + providerServiceMapID + "}";

        List<Scheme> schemes = new ArrayList<>();
        Scheme scheme1 = new Scheme();
        scheme1.setSchemeID(1);
        scheme1.setSchemeName("Scheme One");
        schemes.add(scheme1);
        Scheme scheme2 = new Scheme();
        scheme2.setSchemeID(2);
        scheme2.setSchemeName("Scheme Two");
        schemes.add(scheme2);

        when(schemeServiceImpl.getSchemeList(providerServiceMapID)).thenReturn(schemes);

        // Act
        String response = schemeController.getSchemeList(requestJson);

        // Assert
        verify(schemeServiceImpl, times(1)).getSchemeList(providerServiceMapID);
        OutputResponse outputResponse = objectMapper.readValue(response, OutputResponse.class);
        assertTrue(outputResponse.isSuccess());
        assertTrue(outputResponse.getData().contains("\"schemeID\":1"));
        assertTrue(outputResponse.getData().contains("\"schemeID\":2"));
        assertTrue(logContains("getSchemeList request " + requestJson, Level.INFO));
        assertTrue(logContains("getSchemeList response:", Level.INFO));
    }

    @Test
    void testGetSchemeList_Success_NoSchemesAvailableEmptyList() throws Exception {
        // Arrange
        Integer providerServiceMapID = 1;
        String requestJson = "{\"providerServiceMapID\":" + providerServiceMapID + "}";

        when(schemeServiceImpl.getSchemeList(providerServiceMapID)).thenReturn(Collections.emptyList());

        // Act
        String response = schemeController.getSchemeList(requestJson);

        // Assert
        verify(schemeServiceImpl, times(1)).getSchemeList(providerServiceMapID);
        OutputResponse outputResponse = objectMapper.readValue(response, OutputResponse.class);
        assertTrue(outputResponse.isSuccess());
        assertTrue(outputResponse.getData().equals("[]")); // Empty list toString() is "[]"
        assertTrue(logContains("getSchemeList request " + requestJson, Level.INFO));
        assertTrue(logContains("getSchemeList response:", Level.INFO));
    }

    @Test
    void testGetSchemeList_Success_NoSchemesAvailableNull() throws Exception {
        // Arrange
        Integer providerServiceMapID = 1;
        String requestJson = "{\"providerServiceMapID\":" + providerServiceMapID + "}";

        when(schemeServiceImpl.getSchemeList(providerServiceMapID)).thenReturn(null);

        // Act
        String response = schemeController.getSchemeList(requestJson);

        // Assert
        verify(schemeServiceImpl, times(1)).getSchemeList(providerServiceMapID);
        OutputResponse outputResponse = objectMapper.readValue(response, OutputResponse.class);
        assertFalse(outputResponse.isSuccess());
        assertTrue(outputResponse.getErrorMessage().contains("No schemes available"));
        assertTrue(outputResponse.getStatusCode() == 5000);
        assertTrue(logContains("getSchemeList request " + requestJson, Level.INFO));
        assertTrue(logContains("getSchemeList response:", Level.INFO));
    }

    @Test
    void testGetSchemeList_Exception() throws Exception {
        // Arrange
        Integer providerServiceMapID = 1;
        String requestJson = "{\"providerServiceMapID\":" + providerServiceMapID + "}";

        String errorMessage = "Service unavailable";
        when(schemeServiceImpl.getSchemeList(anyInt())).thenThrow(new RuntimeException(errorMessage));

        // Act
        String response = schemeController.getSchemeList(requestJson);

        // Assert
        verify(schemeServiceImpl, times(1)).getSchemeList(providerServiceMapID);
        OutputResponse outputResponse = objectMapper.readValue(response, OutputResponse.class);
        assertFalse(outputResponse.isSuccess());
        assertTrue(outputResponse.getErrorMessage().contains(errorMessage));
        assertTrue(logContains("getSchemeList failed with error " + errorMessage, Level.ERROR));
        assertTrue(logContains("getSchemeList response:", Level.INFO));
    }

    @Test
    void testDeleteScheme_Success() throws Exception {
        // Arrange
        Integer schemeID = 1;
        Boolean deletedStatus = true;
        String requestJson = "{\"schemeID\":" + schemeID + ",\"deleted\":" + deletedStatus + "}";

        Scheme existingScheme = new Scheme();
        existingScheme.setSchemeID(schemeID);
        existingScheme.setDeleted(false); // Initially not deleted

        when(schemeServiceImpl.getSchemeByID(schemeID)).thenReturn(existingScheme);
        when(schemeServiceImpl.deletedata(any(Scheme.class))).thenReturn("success");

        // Act
        String response = schemeController.deleteScheme(requestJson);

        // Assert
        verify(schemeServiceImpl, times(1)).getSchemeByID(schemeID);
        verify(schemeServiceImpl, times(1)).deletedata(existingScheme); // Verify with the modified object
        assertTrue(existingScheme.getDeleted()); // Verify the scheme object was updated before passing to service

        OutputResponse outputResponse = objectMapper.readValue(response, OutputResponse.class);
        assertTrue(outputResponse.isSuccess());
        assertTrue(outputResponse.getData().contains("success"));
        assertTrue(logContains("delete scheme request " + requestJson, Level.INFO));
        assertTrue(logContains("getSchemeList response:", Level.INFO)); // Controller logs this as getSchemeList response
    }

    @Test
    void testDeleteScheme_SchemeNotFound() throws Exception {
        // Arrange
        Integer schemeID = 1;
        Boolean deletedStatus = true;
        String requestJson = "{\"schemeID\":" + schemeID + ",\"deleted\":" + deletedStatus + "}";

        when(schemeServiceImpl.getSchemeByID(schemeID)).thenReturn(null);

        // Act
        String response = schemeController.deleteScheme(requestJson);

        // Assert
        verify(schemeServiceImpl, times(1)).getSchemeByID(schemeID);
        verify(schemeServiceImpl, never()).deletedata(any(Scheme.class)); // Should not call deleteData
        OutputResponse outputResponse = objectMapper.readValue(response, OutputResponse.class);
        assertFalse(outputResponse.isSuccess());
        assertTrue(outputResponse.getErrorMessage().contains("No schemes available"));
        assertTrue(outputResponse.getStatusCode() == 5000);
        assertTrue(logContains("delete scheme request " + requestJson, Level.INFO));
        assertTrue(logContains("getSchemeList response:", Level.INFO));
    }

    @Test
    void testDeleteScheme_GetSchemeByID_Exception() throws Exception {
        // Arrange
        Integer schemeID = 1;
        Boolean deletedStatus = true;
        String requestJson = "{\"schemeID\":" + schemeID + ",\"deleted\":" + deletedStatus + "}";

        String errorMessage = "DB connection failed";
        when(schemeServiceImpl.getSchemeByID(anyInt())).thenThrow(new RuntimeException(errorMessage));

        // Act
        String response = schemeController.deleteScheme(requestJson);

        // Assert
        verify(schemeServiceImpl, times(1)).getSchemeByID(schemeID);
        verify(schemeServiceImpl, never()).deletedata(any(Scheme.class));
        OutputResponse outputResponse = objectMapper.readValue(response, OutputResponse.class);
        assertFalse(outputResponse.isSuccess());
        assertTrue(outputResponse.getErrorMessage().contains(errorMessage));
        assertTrue(logContains("getSchemeList failed with error " + errorMessage, Level.ERROR)); // Controller logs this as getSchemeList failed
        assertTrue(logContains("getSchemeList response:", Level.INFO));
    }

    @Test
    void testDeleteScheme_DeleteData_Exception() throws Exception {
        // Arrange
        Integer schemeID = 1;
        Boolean deletedStatus = true;
        String requestJson = "{\"schemeID\":" + schemeID + ",\"deleted\":" + deletedStatus + "}";

        Scheme existingScheme = new Scheme();
        existingScheme.setSchemeID(schemeID);
        existingScheme.setDeleted(false);

        String errorMessage = "Failed to update status";
        when(schemeServiceImpl.getSchemeByID(schemeID)).thenReturn(existingScheme);
        when(schemeServiceImpl.deletedata(any(Scheme.class))).thenThrow(new RuntimeException(errorMessage));

        // Act
        String response = schemeController.deleteScheme(requestJson);

        // Assert
        verify(schemeServiceImpl, times(1)).getSchemeByID(schemeID);
        verify(schemeServiceImpl, times(1)).deletedata(existingScheme);
        OutputResponse outputResponse = objectMapper.readValue(response, OutputResponse.class);
        assertFalse(outputResponse.isSuccess());
        assertTrue(outputResponse.getErrorMessage().contains(errorMessage));
        assertTrue(logContains("getSchemeList failed with error " + errorMessage, Level.ERROR)); // Controller logs this as getSchemeList failed
        assertTrue(logContains("getSchemeList response:", Level.INFO));
    }
}