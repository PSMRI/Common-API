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
import javax.ws.rs.core.MediaType;
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