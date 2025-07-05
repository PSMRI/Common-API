package com.iemr.common.controller.snomedct;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.gson.Gson;
import com.iemr.common.data.snomedct.SCTDescription;
import com.iemr.common.service.snomedct.SnomedService;
import com.iemr.common.utils.mapper.InputMapper;
import com.iemr.common.utils.response.OutputResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SnomedControllerTest {

    @InjectMocks
    private SnomedController snomedController;

    @Mock
    private SnomedService snomedService;

    @Mock
    private Logger logger; // Mock the logger

    @Mock
    private InputMapper mockInputMapperInstance; // Mock for the instance returned by InputMapper.gson()

    private MockedStatic<InputMapper> mockedStaticInputMapper;

    @BeforeEach
    void setUp() {
        // Inject the mocked logger into the controller
        ReflectionTestUtils.setField(snomedController, "logger", logger);

        // Start mocking the static InputMapper class
        mockedStaticInputMapper = Mockito.mockStatic(InputMapper.class);
        // When InputMapper.gson() is called, return our mock instance
        mockedStaticInputMapper.when(InputMapper::gson).thenReturn(mockInputMapperInstance);
    }

    @AfterEach
    void tearDown() {
        // Close the static mock after each test
        mockedStaticInputMapper.close();
    }

    @Test
    void setSnomedService_ShouldSetService() {
        // Given
        SnomedService newSnomedService = Mockito.mock(SnomedService.class);

        // When
        snomedController.setSnomedService(newSnomedService);

        // Then
        // Verify that the snomedService field in the controller is set to newSnomedService
        SnomedService actualService = (SnomedService) ReflectionTestUtils.getField(snomedController, "snomedService");
        assertEquals(newSnomedService, actualService);
    }

    @Test
    void getSnomedCTRecord_Success() throws Exception {
        // Given
        String requestJson = "{\"term\":\"testTerm\"}";
        SCTDescription inputSCTDescription = new SCTDescription();
        inputSCTDescription.setTerm("testTerm");

        SCTDescription foundSCTDescription = new SCTDescription("12345", "Found Term");
        foundSCTDescription.setConceptID("12345"); // Ensure conceptID is not null

        // Stub InputMapper.gson().fromJson() to return the input SCTDescription
        when(mockInputMapperInstance.fromJson(requestJson, SCTDescription.class)).thenReturn(inputSCTDescription);

        // Stub SnomedService.findSnomedCTRecordFromTerm()
        when(snomedService.findSnomedCTRecordFromTerm("testTerm")).thenReturn(foundSCTDescription);

        // Expected OutputResponse
        OutputResponse expectedOutput = new OutputResponse();
        expectedOutput.setResponse(new Gson().toJson(foundSCTDescription));

        // When
        String result = snomedController.getSnomedCTRecord(requestJson);

        // Then
        assertEquals(expectedOutput.toString(), result);
        verify(logger, times(1)).info("getSnomedCTRecord request " + inputSCTDescription.toString());
        verify(logger, times(1)).info("ggetSnomedCTRecord response: " + expectedOutput);
    }

    @Test
    void getSnomedCTRecord_NoRecordsFound_NullFromService() throws Exception {
        // Given
        String requestJson = "{\"term\":\"nonExistentTerm\"}";
        SCTDescription inputSCTDescription = new SCTDescription();
        inputSCTDescription.setTerm("nonExistentTerm");

        // Stub InputMapper.gson().fromJson()
        when(mockInputMapperInstance.fromJson(requestJson, SCTDescription.class)).thenReturn(inputSCTDescription);

        // Stub SnomedService.findSnomedCTRecordFromTerm() to return null
        when(snomedService.findSnomedCTRecordFromTerm("nonExistentTerm")).thenReturn(null);

        // Expected OutputResponse
        OutputResponse expectedOutput = new OutputResponse();
        expectedOutput.setResponse("No Records Found");

        // When
        String result = snomedController.getSnomedCTRecord(requestJson);

        // Then
        assertEquals(expectedOutput.toString(), result);
        verify(logger, times(1)).info("getSnomedCTRecord request " + inputSCTDescription.toString());
        verify(logger, times(1)).info("ggetSnomedCTRecord response: " + expectedOutput);
    }

    @Test
    void getSnomedCTRecord_NoRecordsFound_NullConceptID() throws Exception {
        // Given
        String requestJson = "{\"term\":\"termWithNullConceptID\"}";
        SCTDescription inputSCTDescription = new SCTDescription();
        inputSCTDescription.setTerm("termWithNullConceptID");

        SCTDescription foundSCTDescription = new SCTDescription("12345", "Term With Null ConceptID");
        foundSCTDescription.setConceptID(null); // Simulate null conceptID

        // Stub InputMapper.gson().fromJson()
        when(mockInputMapperInstance.fromJson(requestJson, SCTDescription.class)).thenReturn(inputSCTDescription);

        // Stub SnomedService.findSnomedCTRecordFromTerm() to return SCTDescription with null conceptID
        when(snomedService.findSnomedCTRecordFromTerm("termWithNullConceptID")).thenReturn(foundSCTDescription);

        // Expected OutputResponse
        OutputResponse expectedOutput = new OutputResponse();
        expectedOutput.setResponse("No Records Found");

        // When
        String result = snomedController.getSnomedCTRecord(requestJson);

        // Then
        assertEquals(expectedOutput.toString(), result);
        verify(logger, times(1)).info("getSnomedCTRecord request " + inputSCTDescription.toString());
        verify(logger, times(1)).info("ggetSnomedCTRecord response: " + expectedOutput);
    }

    @Test
    void getSnomedCTRecord_ExceptionHandling() throws Exception {
        // Given
        String requestJson = "{\"term\":\"invalidJson\"}";
        Exception testException = new RuntimeException("Test exception during JSON parsing");

        // Stub InputMapper.gson().fromJson() to throw an exception
        when(mockInputMapperInstance.fromJson(requestJson, SCTDescription.class)).thenThrow(testException);

        // Expected OutputResponse for error
        OutputResponse expectedOutput = new OutputResponse();
        expectedOutput.setError(testException); // OutputResponse handles the error message and status code

        // When
        String result = snomedController.getSnomedCTRecord(requestJson);

        // Then
        assertEquals(expectedOutput.toString(), result);
        // Verify logger.error is called
        verify(logger, times(1)).error("ggetSnomedCTRecord failed with error " + testException.getMessage(), testException);
    }

    @Test
    void getSnomedCTRecordList_Success() throws Exception {
        // Given
        String requestJson = "{\"term\":\"testTermList\"}";
        SCTDescription inputSCTDescription = new SCTDescription();
        inputSCTDescription.setTerm("testTermList");

        String sctListJson = "[{\"conceptID\":\"1\",\"term\":\"Term1\"},{\"conceptID\":\"2\",\"term\":\"Term2\"}]";

        // Stub InputMapper.gson().fromJson()
        when(mockInputMapperInstance.fromJson(requestJson, SCTDescription.class)).thenReturn(inputSCTDescription);

        // Stub SnomedService.findSnomedCTRecordList()
        when(snomedService.findSnomedCTRecordList(inputSCTDescription)).thenReturn(sctListJson);

        // Expected OutputResponse
        OutputResponse expectedOutput = new OutputResponse();
        expectedOutput.setResponse(sctListJson);

        // When
        String result = snomedController.getSnomedCTRecordList(requestJson);

        // Then
        assertEquals(expectedOutput.toString(), result);
        verify(logger, times(1)).info("getSnomedCTRecord request " + inputSCTDescription.toString());
        verify(logger, times(1)).info("ggetSnomedCTRecord response: " + expectedOutput);
    }

    @Test
    void getSnomedCTRecordList_NoRecordsFound() throws Exception {
        // Given
        String requestJson = "{\"term\":\"nonExistentTermList\"}";
        SCTDescription inputSCTDescription = new SCTDescription();
        inputSCTDescription.setTerm("nonExistentTermList");

        // Stub InputMapper.gson().fromJson()
        when(mockInputMapperInstance.fromJson(requestJson, SCTDescription.class)).thenReturn(inputSCTDescription);

        // Stub SnomedService.findSnomedCTRecordList() to return null
        when(snomedService.findSnomedCTRecordList(inputSCTDescription)).thenReturn(null);

        // Expected OutputResponse
        OutputResponse expectedOutput = new OutputResponse();
        expectedOutput.setResponse("No Records Found");

        // When
        String result = snomedController.getSnomedCTRecordList(requestJson);

        // Then
        assertEquals(expectedOutput.toString(), result);
        verify(logger, times(1)).info("getSnomedCTRecord request " + inputSCTDescription.toString());
        verify(logger, times(1)).info("ggetSnomedCTRecord response: " + expectedOutput);
    }

    @Test
    void getSnomedCTRecordList_ExceptionHandling() throws Exception {
        // Given
        String requestJson = "{\"term\":\"invalidJsonList\"}";
        Exception testException = new RuntimeException("Test exception during list processing");

        // Stub InputMapper.gson().fromJson() to throw an exception
        when(mockInputMapperInstance.fromJson(requestJson, SCTDescription.class)).thenThrow(testException);

        // Expected OutputResponse for error
        OutputResponse expectedOutput = new OutputResponse();
        expectedOutput.setError(testException);

        // When
        String result = snomedController.getSnomedCTRecordList(requestJson);

        // Then
        assertEquals(expectedOutput.toString(), result);
        // Verify logger.error is called
        verify(logger, times(1)).error("ggetSnomedCTRecord failed with error " + testException.getMessage(), testException);
    }
}