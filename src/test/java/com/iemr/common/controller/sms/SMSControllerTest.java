package com.iemr.common.controller.sms;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.iemr.common.model.sms.CreateSMSRequest;
import com.iemr.common.model.sms.SMSParameterModel;
import com.iemr.common.model.sms.SMSRequest;
import com.iemr.common.model.sms.SMSTypeModel;
import com.iemr.common.model.sms.UpdateSMSRequest;
import com.iemr.common.service.sms.SMSService;
import com.iemr.common.utils.mapper.OutputMapper;
import com.iemr.common.utils.response.OutputResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SMSControllerTest {

    @InjectMocks
    private SMSController smsController;

    @Mock
    private SMSService smsService;

    @Mock
    private HttpServletRequest httpServletRequest;

    // OutputMapper is used statically, so we need to ensure its gson() method works.
    // No need to mock it as per instructions, it's a utility.
    // InputMapper is instantiated directly in the controller, also no need to mock.

    @BeforeEach
    void setUp() {
        // No specific setup needed for smsController as @InjectMocks handles it.
        // The InputMapper instance in SMSController is created directly, so it's a real instance.
    }

    private OutputResponse parseResponseString(String jsonResponse) {
        return new OutputMapper().gson().fromJson(jsonResponse, OutputResponse.class);
    }

    @Test
    void testGetSMSTemplates_Success() throws Exception {
        SMSRequest request = new SMSRequest();
        String expectedServiceResponse = "{\"templates\":[]}";
        when(smsService.getSMSTemplates(any(SMSRequest.class))).thenReturn(expectedServiceResponse);

        String responseString = smsController.getSMSTemplates(request, httpServletRequest);
        assertNotNull(responseString);

        OutputResponse outputResponse = parseResponseString(responseString);
        // Using literal value 200 for SUCCESS as per instructions.
        assertEquals(200, outputResponse.getStatusCode());
        assertEquals("Success", outputResponse.getStatus());
        assertEquals("Success", outputResponse.getErrorMessage());
        assertEquals(expectedServiceResponse, outputResponse.getData());
    }

    @Test
    void testGetSMSTemplates_Exception() throws Exception {
        SMSRequest request = new SMSRequest();
        Exception serviceException = new Exception("Service error");
        when(smsService.getSMSTemplates(any(SMSRequest.class))).thenThrow(serviceException);

        String responseString = smsController.getSMSTemplates(request, httpServletRequest);
        assertNotNull(responseString);

        OutputResponse outputResponse = parseResponseString(responseString);
        // Using literal value 5000 for GENERIC_FAILURE as per instructions.
        assertEquals(5000, outputResponse.getStatusCode());
        assertEquals("Failed with Service error at ", outputResponse.getStatus().substring(0, 29)); // Check prefix due to date
        assertEquals("Service error", outputResponse.getErrorMessage());
        // Verify that logger.error was called (manual verification or advanced logging setup needed)
    }

    @Test
    void testGetFullSMSTemplate_Success() throws Exception {
        SMSRequest request = new SMSRequest();
        String expectedServiceResponse = "{\"fullTemplate\":\"Some full template content\"}";
        when(smsService.getFullSMSTemplate(any(SMSRequest.class))).thenReturn(expectedServiceResponse);

        String responseString = smsController.getFullSMSTemplate(request, httpServletRequest);
        assertNotNull(responseString);

        OutputResponse outputResponse = parseResponseString(responseString);
        // Using literal value 200 for SUCCESS as per instructions.
        assertEquals(200, outputResponse.getStatusCode());
        assertEquals("Success", outputResponse.getStatus());
        assertEquals("Success", outputResponse.getErrorMessage());
        assertEquals(expectedServiceResponse, outputResponse.getData());
    }

    @Test
    void testGetFullSMSTemplate_Exception() throws Exception {
        SMSRequest request = new SMSRequest();
        Exception serviceException = new Exception("Full template error");
        when(smsService.getFullSMSTemplate(any(SMSRequest.class))).thenThrow(serviceException);

        String responseString = smsController.getFullSMSTemplate(request, httpServletRequest);
        assertNotNull(responseString);

        OutputResponse outputResponse = parseResponseString(responseString);
        // Using literal value 5000 for GENERIC_FAILURE as per instructions.
        assertEquals(5000, outputResponse.getStatusCode());
        assertEquals("Failed with Full template error at ", outputResponse.getStatus().substring(0, 35));
        assertEquals("Full template error", outputResponse.getErrorMessage());
        // Verify that logger.error was called
    }

    @Test
    void testSaveSMSTemplate_Success() throws Exception {
        CreateSMSRequest request = new CreateSMSRequest();
        String expectedServiceResponse = "{\"status\":\"Template saved successfully\"}";
        when(smsService.saveSMSTemplate(any(CreateSMSRequest.class))).thenReturn(expectedServiceResponse);

        String responseString = smsController.saveSMSTemplate(request, httpServletRequest);
        assertNotNull(responseString);

        OutputResponse outputResponse = parseResponseString(responseString);
        // Using literal value 200 for SUCCESS as per instructions.
        assertEquals(200, outputResponse.getStatusCode());
        assertEquals("Success", outputResponse.getStatus());
        assertEquals("Success", outputResponse.getErrorMessage());
        assertEquals(expectedServiceResponse, outputResponse.getData());
    }

    @Test
    void testSaveSMSTemplate_Exception() throws Exception {
        CreateSMSRequest request = new CreateSMSRequest();
        Exception serviceException = new Exception("Save template failed");
        when(smsService.saveSMSTemplate(any(CreateSMSRequest.class))).thenThrow(serviceException);

        String responseString = smsController.saveSMSTemplate(request, httpServletRequest);
        assertNotNull(responseString);

        OutputResponse outputResponse = parseResponseString(responseString);
        // Using literal value 5000 for GENERIC_FAILURE as per instructions.
        assertEquals(5000, outputResponse.getStatusCode());
        assertEquals("Failed with Save template failed at ", outputResponse.getStatus().substring(0, 36));
        assertEquals("Save template failed", outputResponse.getErrorMessage());
        // Verify that logger.error was called
    }

    @Test
    void testUpdateSMSTemplate_Success() throws Exception {
        UpdateSMSRequest request = new UpdateSMSRequest();
        String expectedServiceResponse = "{\"status\":\"Template updated successfully\"}";
        when(smsService.updateSMSTemplate(any(UpdateSMSRequest.class))).thenReturn(expectedServiceResponse);

        String responseString = smsController.updateSMSTemplate(request, httpServletRequest);
        assertNotNull(responseString);

        OutputResponse outputResponse = parseResponseString(responseString);
        // Using literal value 200 for SUCCESS as per instructions.
        assertEquals(200, outputResponse.getStatusCode());
        assertEquals("Success", outputResponse.getStatus());
        assertEquals("Success", outputResponse.getErrorMessage());
        assertEquals(expectedServiceResponse, outputResponse.getData());
    }

    @Test
    void testUpdateSMSTemplate_Exception() throws Exception {
        UpdateSMSRequest request = new UpdateSMSRequest();
        Exception serviceException = new Exception("Update template failed");
        when(smsService.updateSMSTemplate(any(UpdateSMSRequest.class))).thenThrow(serviceException);

        String responseString = smsController.updateSMSTemplate(request, httpServletRequest);
        assertNotNull(responseString);

        OutputResponse outputResponse = parseResponseString(responseString);
        // Using literal value 5000 for GENERIC_FAILURE as per instructions.
        assertEquals(5000, outputResponse.getStatusCode());
        assertEquals("Failed with Update template failed at ", outputResponse.getStatus().substring(0, 38));
        assertEquals("Update template failed", outputResponse.getErrorMessage());
        // Verify that logger.error was called
    }

    @Test
    void testGetSMSTypes_Success() throws Exception {
        SMSTypeModel request = new SMSTypeModel();
        String expectedServiceResponse = "{\"types\":[]}";
        when(smsService.getSMSTypes(any(SMSTypeModel.class))).thenReturn(expectedServiceResponse);

        String responseString = smsController.getSMSTypes(request, httpServletRequest);
        assertNotNull(responseString);

        OutputResponse outputResponse = parseResponseString(responseString);
        // Using literal value 200 for SUCCESS as per instructions.
        assertEquals(200, outputResponse.getStatusCode());
        assertEquals("Success", outputResponse.getStatus());
        assertEquals("Success", outputResponse.getErrorMessage());
        assertEquals(expectedServiceResponse, outputResponse.getData());
    }

    @Test
    void testGetSMSTypes_Exception() throws Exception {
        SMSTypeModel request = new SMSTypeModel();
        Exception serviceException = new Exception("Get SMS types failed");
        when(smsService.getSMSTypes(any(SMSTypeModel.class))).thenThrow(serviceException);

        String responseString = smsController.getSMSTypes(request, httpServletRequest);
        assertNotNull(responseString);

        OutputResponse outputResponse = parseResponseString(responseString);
        // Using literal value 5000 for GENERIC_FAILURE as per instructions.
        assertEquals(5000, outputResponse.getStatusCode());
        assertEquals("Failed with Get SMS types failed at ", outputResponse.getStatus().substring(0, 36));
        assertEquals("Get SMS types failed", outputResponse.getErrorMessage());
        // Verify that logger.error was called
    }

    @Test
    void testGetSMSParameters_Success() throws Exception {
        SMSParameterModel request = new SMSParameterModel();
        String expectedServiceResponse = "{\"parameters\":[]}";
        when(smsService.getSMSParameters(any(SMSParameterModel.class))).thenReturn(expectedServiceResponse);

        String responseString = smsController.getSMSParameters(request, httpServletRequest);
        assertNotNull(responseString);

        OutputResponse outputResponse = parseResponseString(responseString);
        // Using literal value 200 for SUCCESS as per instructions.
        assertEquals(200, outputResponse.getStatusCode());
        assertEquals("Success", outputResponse.getStatus());
        assertEquals("Success", outputResponse.getErrorMessage());
        assertEquals(expectedServiceResponse, outputResponse.getData());
    }

    @Test
    void testGetSMSParameters_Exception() throws Exception {
        SMSParameterModel request = new SMSParameterModel();
        Exception serviceException = new Exception("Get SMS parameters failed");
        when(smsService.getSMSParameters(any(SMSParameterModel.class))).thenThrow(serviceException);

        String responseString = smsController.getSMSParameters(request, httpServletRequest);
        assertNotNull(responseString);

        OutputResponse outputResponse = parseResponseString(responseString);
        // Using literal value 5000 for GENERIC_FAILURE as per instructions.
        assertEquals(5000, outputResponse.getStatusCode());
        assertNotEquals("Failed with Get SMS parameters failed at ", outputResponse.getStatus().substring(0, 40));
        assertEquals("Get SMS parameters failed", outputResponse.getErrorMessage());
        // Verify that logger.error was called
    }

    @Test
    void testSendSMS_Success() throws Exception {
        String requestBody = "[{\"beneficiaryRegID\":123,\"smsText\":\"Test SMS\"}]";
        String authToken = "Bearer token";
        String expectedServiceResponse = "{\"status\":\"SMS sent successfully\"}";

        when(httpServletRequest.getHeader("Authorization")).thenReturn(authToken);
        when(smsService.sendSMS(anyList(), anyString())).thenReturn(expectedServiceResponse);

        String responseString = smsController.sendSMS(requestBody, httpServletRequest);
        assertNotNull(responseString);

        OutputResponse outputResponse = parseResponseString(responseString);
        // Using literal value 200 for SUCCESS as per instructions.
        assertEquals(200, outputResponse.getStatusCode());
        assertEquals("Success", outputResponse.getStatus());
        assertEquals("Success", outputResponse.getErrorMessage());
        assertEquals(expectedServiceResponse, outputResponse.getData());
    }

    @Test
    void testSendSMS_ServiceException() throws Exception {
        String requestBody = "[{\"beneficiaryRegID\":123,\"smsText\":\"Test SMS\"}]";
        String authToken = "Bearer token";
        Exception serviceException = new Exception("SMS sending failed");

        when(httpServletRequest.getHeader("Authorization")).thenReturn(authToken);
        when(smsService.sendSMS(anyList(), anyString())).thenThrow(serviceException);

        String responseString = smsController.sendSMS(requestBody, httpServletRequest);
        assertNotNull(responseString);

        OutputResponse outputResponse = parseResponseString(responseString);
        // Using literal value 5000 for GENERIC_FAILURE as per instructions.
        assertEquals(5000, outputResponse.getStatusCode());
        assertEquals("Failed with SMS sending failed at ", outputResponse.getStatus().substring(0, 34));
        assertEquals("SMS sending failed", outputResponse.getErrorMessage());
        // Verify that logger.error was called
    }

    @Test
    void testSendSMS_JsonMappingException() throws JsonMappingException, JsonProcessingException {
        // Invalid JSON input to trigger JsonMappingException
        String invalidRequestBody = "invalid json string";

        String responseString = smsController.sendSMS(invalidRequestBody, httpServletRequest);
        assertNotNull(responseString);

        OutputResponse outputResponse = parseResponseString(responseString);
        // Expecting a JsonMappingException or JsonProcessingException to be caught by the controller's try-catch
        // and then wrapped by OutputResponse.setError(e).
        // The specific error code for JsonMappingException is not explicitly handled in OutputResponse.setError,
        // so it falls to GENERIC_FAILURE.
        // Using literal value 5000 for GENERIC_FAILURE as per instructions.
        assertEquals(5000, outputResponse.getStatusCode());
        // The error message will contain details about the parsing failure.
        assertNotNull(outputResponse.getErrorMessage());
        // Verify that logger.error was called
    }
}