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
package com.iemr.common.controller.email;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.mockito.ArgumentMatchers.anyString;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.iemr.common.service.email.EmailService;
import com.iemr.common.utils.response.OutputResponse;
import org.springframework.http.MediaType;
import java.lang.reflect.Field;

@ExtendWith(MockitoExtension.class)
class EmailControllerTest {
    private MockMvc mockMvc;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private EmailController emailController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(emailController).build();
    }

    @Test
    void getAuthorityEmailID_shouldReturnSuccessResponse() throws Exception {
        String requestBody = "{\"districtID\":1}";
        String serviceResponse = "{\"email\":\"test@example.com\"}";
        OutputResponse expectedOutputResponse = new OutputResponse();
        expectedOutputResponse.setResponse(serviceResponse);

        when(emailService.getAuthorityEmailID(anyString())).thenReturn(serviceResponse);

        mockMvc.perform(post("/emailController/getAuthorityEmailID")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedOutputResponse.toString()));
    }

    @Test
    void getAuthorityEmailID_shouldReturnErrorResponseOnError() throws Exception {
        String requestBody = "{\"districtID\":1}";
        String errorMessage = "Simulated service error";
        OutputResponse expectedOutputResponse = new OutputResponse();
        expectedOutputResponse.setError(new Exception(errorMessage));

        when(emailService.getAuthorityEmailID(anyString())).thenThrow(new Exception(errorMessage));

        mockMvc.perform(post("/emailController/getAuthorityEmailID")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedOutputResponse.toString(), false));
    }

    @Test
    void SendEmail_shouldReturnSuccessString() throws Exception {
        String requestBody = "{\"FeedbackID\":123,\"emailID\":\"test@example.com\",\"is1097\":true}";
        String expectedServiceResponse = "Email sent successfully";

        OutputResponse expectedOutputResponse = new OutputResponse();
        expectedOutputResponse.setResponse(expectedServiceResponse);

        when(emailService.SendEmail(anyString(), anyString())).thenReturn(expectedServiceResponse);

        mockMvc.perform(post("/emailController/SendEmail")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedOutputResponse.toString()));
    }

    @Test
    void sendEmailGeneral_shouldReturnSuccessString() throws Exception {
        String requestBody = "{\"requestID\":\"req123\",\"emailType\":\"typeA\",\"emailID\":\"general@example.com\"}";
        String expectedServiceResponse = "General email sent successfully";

        OutputResponse expectedOutputResponse = new OutputResponse();
        expectedOutputResponse.setResponse(expectedServiceResponse);

        when(emailService.sendEmailGeneral(anyString(), anyString())).thenReturn(expectedServiceResponse);

        mockMvc.perform(post("/emailController/sendEmailGeneral")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedOutputResponse.toString()));
    }

    @Test
    void setEmailService_shouldSetService() throws NoSuchFieldException, IllegalAccessException {
        EmailService anotherMockEmailService = mock(EmailService.class);

        emailController.setEmailService(anotherMockEmailService);

        Field emailServiceField = EmailController.class.getDeclaredField("emailService");
        emailServiceField.setAccessible(true);

        EmailService actualEmailService = (EmailService) emailServiceField.get(emailController);

        assertEquals(anotherMockEmailService, actualEmailService);
    }
}