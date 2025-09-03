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

import com.iemr.common.service.questionconfig.QuestionnaireService;
import com.iemr.common.utils.response.OutputResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuestionnaireControllerTest {

    @InjectMocks
    private QuestionnaireController questionnaireController;

    @Mock
    private QuestionnaireService questionnaireService;

    @Test
    void testSetQuestionnaireService() throws Exception {
        // Create a new instance of the controller to explicitly test the setter
        QuestionnaireController controllerUnderTestSetter = new QuestionnaireController();
        // Create a separate mock service for this specific test
        QuestionnaireService specificMockService = Mockito.mock(QuestionnaireService.class);

        // Set the mock service using the setter
        controllerUnderTestSetter.setQuestionnaireService(specificMockService);

        // Now, call a method on the controller that uses the service
        String expectedServiceResponse = "{\"status\":\"success\",\"data\":\"test\"}";
        when(specificMockService.getQuestionnaireList()).thenReturn(expectedServiceResponse);

        String result = controllerUnderTestSetter.questionTypeList();

        // Verify that the method on the *specificMockService* was called
        verify(specificMockService).getQuestionnaireList();

        // Verify the output is as expected from the service response
        OutputResponse expectedOutputResponse = new OutputResponse();
        expectedOutputResponse.setResponse(expectedServiceResponse);
        assertEquals(expectedOutputResponse.toString(), result);
    }

    @Test
    void testCreateQuestionnaire_Success() throws Exception {
        String request = "{\"key\":\"value\"}";
        String serviceResponse = "{\"status\":\"success\",\"data\":\"created\"}";
        when(questionnaireService.createQuestionnaire(request)).thenReturn(serviceResponse);

        String result = questionnaireController.createQuestionnaire(request);

        assertNotNull(result);
        OutputResponse expectedResponse = new OutputResponse();
        expectedResponse.setResponse(serviceResponse);
        assertEquals(expectedResponse.toString(), result);
        verify(questionnaireService).createQuestionnaire(request);
    }

    @Test
    void testCreateQuestionnaire_Failure() throws Exception {
        String request = "{\"key\":\"value\"}";
        Exception serviceException = new RuntimeException("Service error during creation");
        doThrow(serviceException).when(questionnaireService).createQuestionnaire(request);

        String result = questionnaireController.createQuestionnaire(request);

        assertNotNull(result);
        OutputResponse expectedResponse = new OutputResponse();
        expectedResponse.setError(serviceException);
        assertEquals(expectedResponse.toString(), result);
        verify(questionnaireService).createQuestionnaire(request);
        // Logging verification:
        // Due to the 'final' nature of the logger field in QuestionnaireController,
        // direct mocking and verification of logger calls (e.g., logger.error())
        // using Mockito is not straightforward without reflection or custom LoggerFactory setup.
        // In a real project, one might use a test appender for Logback/Log4j to capture logs
        // or use ReflectionTestUtils to set the logger field to a mock.
        // For this exercise, we acknowledge that an error log would be generated here.
    }

    @Test
    void testQuestionTypeList_Success() throws Exception {
        String serviceResponse = "[{\"id\":1,\"name\":\"Q1\"},{\"id\":2,\"name\":\"Q2\"}]";
        when(questionnaireService.getQuestionnaireList()).thenReturn(serviceResponse);

        String result = questionnaireController.questionTypeList();

        assertNotNull(result);
        OutputResponse expectedResponse = new OutputResponse();
        expectedResponse.setResponse(serviceResponse);
        assertEquals(expectedResponse.toString(), result);
        verify(questionnaireService).getQuestionnaireList();
    }

    @Test
    void testQuestionTypeList_Failure() throws Exception {
        Exception serviceException = new RuntimeException("Service error during list retrieval");
        doThrow(serviceException).when(questionnaireService).getQuestionnaireList();

        String result = questionnaireController.questionTypeList();

        assertNotNull(result);
        OutputResponse expectedResponse = new OutputResponse();
        expectedResponse.setError(serviceException);
        assertEquals(expectedResponse.toString(), result);
        verify(questionnaireService).getQuestionnaireList();
        // Logging verification:
        // Similar to testCreateQuestionnaire_Failure, direct mocking and verification
        // of the 'final' logger field is not straightforward.
        // An error log would be generated here.
    }
}