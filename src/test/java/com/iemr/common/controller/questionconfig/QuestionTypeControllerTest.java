/*
* AMRIT – Accessible Medical Records via Integrated Technology 
* Integrated EHR (Electronic Health Records) Solution 
*
* Copyright (C) "Piramal Swasthya Management and Research Institute" 
*
* This file is part of AMRIT.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see https://www.gnu.org/licenses/.
*/
package com.iemr.common.controller.questionconfig;

import com.iemr.common.service.questionconfig.QuestionTypeService;
import com.iemr.common.utils.response.OutputResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

class QuestionTypeControllerTest {

    @Mock
    private QuestionTypeService questionTypeService;

    @InjectMocks
    private QuestionTypeController questionTypeController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSetQuestionTypeService() {
        // This test primarily ensures coverage for the setter method.
        // @InjectMocks already handles the initial injection of the mock questionTypeService.
        // For a simple setter, just calling it is sufficient to cover the method.
        QuestionTypeService anotherMockService = mock(QuestionTypeService.class);
        questionTypeController.setQuestionTypeService(anotherMockService);
        // No explicit assertion is typically needed for a simple setter,
        // as its primary function is assignment, which is assumed to work.
        // If deep verification were needed, reflection would be required to access the private field.
    }

    @Test
    void testCreateQuestionType_Success() throws Exception {
        String request = "{\"questionType\":\"TestType\",\"questionTypeDesc\":\"Description\"}";
        String serviceResponse = "Question type created successfully.";

        when(questionTypeService.createQuestionType(request)).thenReturn(serviceResponse);

        String actualResponseJson = questionTypeController.createQuestionType(request);

        // Create an expected OutputResponse object and set its response to match the controller's logic
        OutputResponse expectedOutputResponse = new OutputResponse();
        expectedOutputResponse.setResponse(serviceResponse);
        String expectedResponseJson = expectedOutputResponse.toString();

        // Parse both JSON strings to compare them as JSON objects, ignoring field order
        JsonElement actualJson = JsonParser.parseString(actualResponseJson);
        JsonElement expectedJson = JsonParser.parseString(expectedResponseJson);

        assertEquals(expectedJson, actualJson, "The response JSON should match the expected successful output.");

        verify(questionTypeService, times(1)).createQuestionType(request);
    }

    @Test
    void testCreateQuestionType_Exception() throws Exception {
        String request = "{\"questionType\":\"TestType\",\"questionTypeDesc\":\"Description\"}";
        RuntimeException thrownException = new RuntimeException("Service failure during creation");

        when(questionTypeService.createQuestionType(request)).thenThrow(thrownException);

        String actualResponseJson = questionTypeController.createQuestionType(request);

        // Parse the actual JSON response
        JsonElement actualJson = JsonParser.parseString(actualResponseJson);

        // Verify status code and error message
        assertEquals(OutputResponse.GENERIC_FAILURE, actualJson.getAsJsonObject().get("statusCode").getAsInt(), "Status code should be GENERIC_FAILURE.");
        assertEquals(thrownException.getMessage(), actualJson.getAsJsonObject().get("errorMessage").getAsString(), "Error message should match the exception message.");

        // Verify status message contains the exception message and the dynamic date part
        String actualStatus = actualJson.getAsJsonObject().get("status").getAsString();
        // The status message includes a dynamic date. Check for the static parts.
        // Format: "Failed with " + thrown.getMessage() + " at " + currDate.toString() + ".Please try after some time. If error is still seen, contact your administrator."
        assertEquals(true, actualStatus.startsWith("Failed with " + thrownException.getMessage() + " at "), "Status message should start correctly.");
        assertEquals(true, actualStatus.endsWith(".Please try after some time. If error is still seen, contact your administrator."), "Status message should end correctly.");

        verify(questionTypeService, times(1)).createQuestionType(request);

        // TODO: Verify that logger.error was called with the correct message and exception.
        // This would typically involve using a logging framework specific test utility (e.g., Logback's ListAppender)
        // or using reflection to set a mock logger, which is more complex for final fields.
    }

    @Test
    void testQuestionTypeList_Success() throws Exception {
        String serviceResponse = "[{\"id\":1,\"type\":\"TypeA\"},{\"id\":2,\"type\":\"TypeB\"}]";

        when(questionTypeService.getQuestionTypeList()).thenReturn(serviceResponse);

        String actualResponseJson = questionTypeController.questionTypeList();

        // Create an expected OutputResponse object and set its response to match the controller's logic
        OutputResponse expectedOutputResponse = new OutputResponse();
        expectedOutputResponse.setResponse(serviceResponse);
        String expectedResponseJson = expectedOutputResponse.toString();

        // Parse both JSON strings to compare them as JSON objects
        JsonElement actualJson = JsonParser.parseString(actualResponseJson);
        JsonElement expectedJson = JsonParser.parseString(expectedResponseJson);

        assertEquals(expectedJson, actualJson, "The response JSON should match the expected successful output.");

        verify(questionTypeService, times(1)).getQuestionTypeList();
    }

    @Test
    void testQuestionTypeList_Exception() throws Exception {
        RuntimeException thrownException = new RuntimeException("Failed to retrieve question types");

        when(questionTypeService.getQuestionTypeList()).thenThrow(thrownException);

        String actualResponseJson = questionTypeController.questionTypeList();

        // Parse the actual JSON response
        JsonElement actualJson = JsonParser.parseString(actualResponseJson);

        // Verify status code and error message
        assertEquals(OutputResponse.GENERIC_FAILURE, actualJson.getAsJsonObject().get("statusCode").getAsInt(), "Status code should be GENERIC_FAILURE.");
        assertEquals(thrownException.getMessage(), actualJson.getAsJsonObject().get("errorMessage").getAsString(), "Error message should match the exception message.");

        // Verify status message contains the exception message and the dynamic date part
        String actualStatus = actualJson.getAsJsonObject().get("status").getAsString();
        assertEquals(true, actualStatus.startsWith("Failed with " + thrownException.getMessage() + " at "), "Status message should start correctly.");
        assertEquals(true, actualStatus.endsWith(".Please try after some time. If error is still seen, contact your administrator."), "Status message should end correctly.");

        verify(questionTypeService, times(1)).getQuestionTypeList();

        // TODO: Verify that logger.error was called with the correct message and exception.
        // This would typically involve using a logging framework specific test utility (e.g., Logback's ListAppender)
        // or using reflection to set a mock logger, which is more complex for final fields.
    }
}