package com.iemr.common.controller.everwell.callhandle;

import com.iemr.common.service.everwell.EverwellCallHandlingService;
import com.iemr.common.utils.response.OutputResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.InjectMocks;
import org.mockito.Mock;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.slf4j.LoggerFactory;

import org.mockito.MockitoAnnotations;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EverwellCallControllerTest {

    @InjectMocks
    private EverwellCallController everwellCallController;

    @Mock
    private EverwellCallHandlingService beneficiaryCallService;

    private ListAppender<ILoggingEvent> listAppender;
    private Logger everwellCallControllerLogger;

    @BeforeEach
    void setUp() {
        // Initialize logger for testing
        everwellCallControllerLogger = (Logger) LoggerFactory.getLogger(EverwellCallController.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        everwellCallControllerLogger.addAppender(listAppender);
    }

    @AfterEach
    void tearDown() {
        everwellCallControllerLogger.detachAppender(listAppender);
        listAppender.stop();
    }

    @Test
    void testOutboundCallCount_Success() throws Exception {
        String request = "{\"providerServiceMapID\":1, \"assignedUserID\":10}";
        String serviceResponse = "{\"count\":100}";
        when(beneficiaryCallService.outboundCallCount(anyString())).thenReturn(serviceResponse);

        String response = everwellCallController.outboundCallCount(request);

        OutputResponse expectedOutput = new OutputResponse();
        expectedOutput.setResponse(serviceResponse);
        assertEquals(expectedOutput.toString(), response);

        // Verify info log
        List<ILoggingEvent> logsList = listAppender.list;
        assertFalse(logsList.isEmpty());
        assertTrue(logsList.stream().anyMatch(event ->
                event.getLevel() == Level.INFO &&
                event.getMessage().contains("outboundCallCount request " + request)
        ));
    }

    @Test
    void testOutboundCallCount_Exception() throws Exception {
        String request = "{\"providerServiceMapID\":1, \"assignedUserID\":10}";
        Exception testException = new RuntimeException("Service error for count");
        when(beneficiaryCallService.outboundCallCount(anyString())).thenThrow(testException);

        String response = everwellCallController.outboundCallCount(request);

        OutputResponse expectedOutput = new OutputResponse();
        expectedOutput.setError(testException);
        assertEquals(expectedOutput.toString(), response);

        // Verify error log (note: controller logs "outboundCallList failed" for outboundCallCount)
        List<ILoggingEvent> logsList = listAppender.list;
        assertFalse(logsList.isEmpty());
        assertTrue(logsList.stream().anyMatch(event ->
                event.getLevel() == Level.ERROR &&
                event.getMessage().contains("outboundCallList failed with error " + testException.getMessage()) &&
                event.getThrowableProxy().getMessage().equals(testException.getMessage())
        ));
    }

    @Test
    void testOutboundAllocation_Success() throws Exception {
        String request = "{\"AgentID\":[1,2], \"allocateNo\":5, \"outboundCallRequests\":[]}";
        String serviceResponse = "{\"allocated\":true}";
        when(beneficiaryCallService.outboundAllocation(anyString())).thenReturn(serviceResponse);

        String response = everwellCallController.outboundAllocation(request);

        OutputResponse expectedOutput = new OutputResponse();
        expectedOutput.setResponse(serviceResponse);
        assertEquals(expectedOutput.toString(), response);
    }

    @Test
    void testOutboundAllocation_Exception() throws Exception {
        String request = "{\"AgentID\":[1,2], \"allocateNo\":5, \"outboundCallRequests\":[]}";
        Exception testException = new RuntimeException("Allocation service error");
        when(beneficiaryCallService.outboundAllocation(anyString())).thenThrow(testException);

        String response = everwellCallController.outboundAllocation(request);

        OutputResponse expectedOutput = new OutputResponse();
        expectedOutput.setError(testException);
        assertEquals(expectedOutput.toString(), response);

        // Verify error log
        List<ILoggingEvent> logsList = listAppender.list;
        assertFalse(logsList.isEmpty());
        assertTrue(logsList.stream().anyMatch(event ->
                event.getLevel() == Level.ERROR &&
                event.getMessage().contains("outboundAllocation failed with error " + testException.getMessage()) &&
                event.getThrowableProxy().getMessage().equals(testException.getMessage())
        ));
    }

    @Test
    void testOutboundCallList_Success() throws Exception {
        String request = "{\"providerServiceMapID\":1, \"AgentID\":10}";
        String serviceResponse = "[{\"callId\":1, \"name\":\"Test\"}]";
        when(beneficiaryCallService.outboundCallList(anyString())).thenReturn(serviceResponse);

        String response = everwellCallController.outboundCallList(request);

        OutputResponse expectedOutput = new OutputResponse();
        expectedOutput.setResponse(serviceResponse);
        assertEquals(expectedOutput.toString(), response);
    }

    @Test
    void testOutboundCallList_Exception() throws Exception {
        String request = "{\"providerServiceMapID\":1, \"AgentID\":10}";
        Exception testException = new RuntimeException("Call list service error");
        when(beneficiaryCallService.outboundCallList(anyString())).thenThrow(testException);

        String response = everwellCallController.outboundCallList(request);

        OutputResponse expectedOutput = new OutputResponse();
        expectedOutput.setError(testException);
        assertEquals(expectedOutput.toString(), response);

        // Verify error log
        List<ILoggingEvent> logsList = listAppender.list;
        assertFalse(logsList.isEmpty());
        assertTrue(logsList.stream().anyMatch(event ->
                event.getLevel() == Level.ERROR &&
                event.getMessage().contains("outboundCallList failed with error " + testException.getMessage()) &&
                event.getThrowableProxy().getMessage().equals(testException.getMessage())
        ));
    }

    @Test
    void testResetOutboundCall_Success() throws Exception {
        String request = "{\"EAPIIDs\":[1,2,3]}";
        String serviceResponse = "reset_success";
        when(beneficiaryCallService.resetOutboundCall(anyString())).thenReturn(serviceResponse);

        String response = everwellCallController.resetOutboundCall(request);

        OutputResponse expectedOutput = new OutputResponse();
        expectedOutput.setResponse(serviceResponse);
        assertEquals(expectedOutput.toString(), response);
    }

    @Test
    void testResetOutboundCall_Exception() throws Exception {
        String request = "{\"EAPIIDs\":[1,2,3]}";
        Exception testException = new RuntimeException("Reset service error");
        when(beneficiaryCallService.resetOutboundCall(anyString())).thenThrow(testException);

        String response = everwellCallController.resetOutboundCall(request);

        OutputResponse expectedOutput = new OutputResponse();
        expectedOutput.setError(testException);
        assertEquals(expectedOutput.toString(), response);

        // Verify error log
        List<ILoggingEvent> logsList = listAppender.list;
        assertFalse(logsList.isEmpty());
        assertTrue(logsList.stream().anyMatch(event ->
                event.getLevel() == Level.ERROR &&
                event.getMessage().contains("outboundAllocation failed with error " + testException.getMessage()) && // Typo in controller
                event.getThrowableProxy().getMessage().equals(testException.getMessage())
        ));
    }

    @Test
    void testSaveCallDetails_Success() throws Exception {
        String request = "{\"EAPIIDs\":[1], \"feedback\":\"good\"}";
        String serviceResponse = "save_success";
        when(beneficiaryCallService.saveDetails(anyString())).thenReturn(serviceResponse);

        String response = everwellCallController.saveCallDetails(request);

        OutputResponse expectedOutput = new OutputResponse();
        expectedOutput.setResponse(serviceResponse);
        assertEquals(expectedOutput.toString(), response);
    }

    @Test
    void testSaveCallDetails_Exception() throws Exception {
        String request = "{\"EAPIIDs\":[1], \"feedback\":\"good\"}";
        Exception testException = new RuntimeException("Save details error");
        when(beneficiaryCallService.saveDetails(anyString())).thenThrow(testException);

        String response = everwellCallController.saveCallDetails(request);

        OutputResponse expectedOutput = new OutputResponse();
        expectedOutput.setError(testException);
        assertEquals(expectedOutput.toString(), response);

        // Verify error log
        List<ILoggingEvent> logsList = listAppender.list;
        assertFalse(logsList.isEmpty());
        assertTrue(logsList.stream().anyMatch(event ->
                event.getLevel() == Level.ERROR &&
                event.getMessage().contains("outboundAllocation failed with error " + testException.getMessage()) && // Typo in controller
                event.getThrowableProxy().getMessage().equals(testException.getMessage())
        ));
    }

    @Test
    void testCompleteOutboundCall_Success() throws Exception {
        String request = "{\"EAPIID\":1, \"isCompleted\":true}";
        String serviceResponse = "success";
        when(beneficiaryCallService.completeOutboundCall(anyString())).thenReturn(serviceResponse);

        String response = everwellCallController.completeOutboundCall(request);

        OutputResponse expectedOutput = new OutputResponse();
        expectedOutput.setResponse(serviceResponse);
        assertEquals(expectedOutput.toString(), response);
    }

    @Test
    void testCompleteOutboundCall_ServiceReturnsNonSuccess() throws Exception {
        String request = "{\"EAPIID\":1, \"isCompleted\":true}";
        String serviceResponse = "failed"; // Not "success"
        when(beneficiaryCallService.completeOutboundCall(anyString())).thenReturn(serviceResponse);

        String response = everwellCallController.completeOutboundCall(request);

        OutputResponse expectedOutput = new OutputResponse();
        expectedOutput.setError(5000, "error in updating data");
        assertEquals(expectedOutput.toString(), response);
    }

    @Test
    void testCompleteOutboundCall_Exception() throws Exception {
        String request = "{\"EAPIID\":1, \"isCompleted\":true}";
        Exception testException = new RuntimeException("Complete call error");
        when(beneficiaryCallService.completeOutboundCall(anyString())).thenThrow(testException);

        String response = everwellCallController.completeOutboundCall(request);

        OutputResponse expectedOutput = new OutputResponse();
        expectedOutput.setError(testException);
        assertEquals(expectedOutput.toString(), response);

        // Verify error log
        List<ILoggingEvent> logsList = listAppender.list;
        assertFalse(logsList.isEmpty());
        assertTrue(logsList.stream().anyMatch(event ->
                event.getLevel() == Level.ERROR &&
                event.getMessage().contains("outboundAllocation failed with error " + testException.getMessage()) && // Typo in controller
                event.getThrowableProxy().getMessage().equals(testException.getMessage())
        ));
    }

    @Test
    void testGetEverwellfeedbackDetails_Success() throws Exception {
        String request = "{\"EverwellID\":123}";
        String serviceResponse = "{\"feedback\":\"positive\"}";
        when(beneficiaryCallService.getEverwellFeedback(anyString())).thenReturn(serviceResponse);

        String response = everwellCallController.getEverwellfeedbackDetails(request);

        OutputResponse expectedOutput = new OutputResponse();
        expectedOutput.setResponse(serviceResponse);
        assertEquals(expectedOutput.toString(), response);
    }

    @Test
    void testGetEverwellfeedbackDetails_ServiceReturnsNull() throws Exception {
        String request = "{\"EverwellID\":123}";
        when(beneficiaryCallService.getEverwellFeedback(anyString())).thenReturn(null);

        String response = everwellCallController.getEverwellfeedbackDetails(request);

        OutputResponse expectedOutput = new OutputResponse();
        expectedOutput.setError(5000, "error in fetching data");
        assertEquals(expectedOutput.toString(), response);
    }

    @Test
    void testGetEverwellfeedbackDetails_Exception() throws Exception {
        String request = "{\"EverwellID\":123}";
        Exception testException = new RuntimeException("Feedback service error");
        when(beneficiaryCallService.getEverwellFeedback(anyString())).thenThrow(testException);

        String response = everwellCallController.getEverwellfeedbackDetails(request);

        OutputResponse expectedOutput = new OutputResponse();
        expectedOutput.setError(testException);
        assertEquals(expectedOutput.toString(), response);

        // Verify error log
        List<ILoggingEvent> logsList = listAppender.list;
        assertFalse(logsList.isEmpty());
        assertTrue(logsList.stream().anyMatch(event ->
                event.getLevel() == Level.ERROR &&
                event.getMessage().contains("outboundAllocation failed with error " + testException.getMessage()) && // Typo in controller
                event.getThrowableProxy().getMessage().equals(testException.getMessage())
        ));
    }

    @Test
    void testOutboundCallListWithMobileNumber_Success() throws Exception {
        String request = "{\"PrimaryNumber\":\"1234567890\", \"providerServiceMapID\":1}";
        String serviceResponse = "[{\"callId\":1, \"mobile\":\"1234567890\"}]";
        when(beneficiaryCallService.outboundCallListWithMobileNumber(anyString())).thenReturn(serviceResponse);

        String response = everwellCallController.outboundCallListWithMobileNumber(request);

        OutputResponse expectedOutput = new OutputResponse();
        expectedOutput.setResponse(serviceResponse);
        assertEquals(expectedOutput.toString(), response);
    }

    @Test
    void testOutboundCallListWithMobileNumber_Exception() throws Exception {
        String request = "{\"PrimaryNumber\":\"1234567890\", \"providerServiceMapID\":1}";
        Exception testException = new RuntimeException("Mobile number list error");
        when(beneficiaryCallService.outboundCallListWithMobileNumber(anyString())).thenThrow(testException);

        String response = everwellCallController.outboundCallListWithMobileNumber(request);

        OutputResponse expectedOutput = new OutputResponse();
        expectedOutput.setError(testException);
        assertEquals(expectedOutput.toString(), response);

        // Verify error log - controller only logs message, not throwable
        List<ILoggingEvent> logsList = listAppender.list;
        assertFalse(logsList.isEmpty());
        assertTrue(logsList.stream().anyMatch(event ->
                event.getLevel() == Level.ERROR &&
                event.getMessage().contains("outboundCallList failed with error " + testException.getMessage())
        ));
    }

    @Test
    void testCheckIfCalledOrNot_Success() throws Exception {
        String request = "{\"providerServiceMapID\":1, \"eapiId\":10}";
        String serviceResponse = "{\"alreadyCalled\":true}";
        when(beneficiaryCallService.checkAlreadyCalled(anyString())).thenReturn(serviceResponse);

        String response = everwellCallController.checkIfCalledOrNot(request);

        OutputResponse expectedOutput = new OutputResponse();
        expectedOutput.setResponse(serviceResponse);
        assertEquals(expectedOutput.toString(), response);
    }

    @Test
    void testCheckIfCalledOrNot_Exception() throws Exception {
        String request = "{\"providerServiceMapID\":1, \"eapiId\":10}";
        Exception testException = new RuntimeException("Check already called error");
        when(beneficiaryCallService.checkAlreadyCalled(anyString())).thenThrow(testException);

        String response = everwellCallController.checkIfCalledOrNot(request);

        OutputResponse expectedOutput = new OutputResponse();
        expectedOutput.setError(testException);
        assertEquals(expectedOutput.toString(), response);

        // Verify error log
        List<ILoggingEvent> logsList = listAppender.list;
        assertFalse(logsList.isEmpty());
        assertTrue(logsList.stream().anyMatch(event ->
                event.getLevel() == Level.ERROR &&
                event.getMessage().contains("checkIfAlreadyCalled failed with error " + testException.getMessage()) &&
                event.getThrowableProxy().getMessage().equals(testException.getMessage())
        ));
    }
}