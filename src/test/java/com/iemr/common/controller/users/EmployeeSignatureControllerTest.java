package com.iemr.common.controller.users;

import com.iemr.common.data.users.EmployeeSignature;
import com.iemr.common.service.users.EmployeeSignatureServiceImpl;
import com.iemr.common.utils.response.OutputResponse;
import com.google.gson.Gson;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = EmployeeSignatureController.class, 
            excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class})
@ContextConfiguration(classes = {EmployeeSignatureController.class})
class EmployeeSignatureControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeSignatureServiceImpl employeeSignatureServiceImpl;

    // Although InputMapper is a dependency in the controller, it's not directly used in the test logic
    // for these endpoints, but we include it as a MockBean if it were to be autowired.
    // private InputMapper inputMapper; // Not autowired, so no @MockBean needed unless it was @Autowired

    private final Long TEST_USER_ID = 123L;
    private final String TEST_FILE_NAME = "signature.png";
    private final String TEST_FILE_TYPE = "image/png";
    private final byte[] TEST_SIGNATURE_BYTES = "test_signature_data".getBytes();

    @Test
    void fetchFile_shouldReturnSignature_whenSignatureExists() throws Exception {
        EmployeeSignature mockSignature = new EmployeeSignature(TEST_USER_ID, TEST_SIGNATURE_BYTES, TEST_FILE_TYPE, TEST_FILE_NAME);

        when(employeeSignatureServiceImpl.fetchSignature(TEST_USER_ID)).thenReturn(mockSignature);

        mockMvc.perform(get("/signature1/{userID}", TEST_USER_ID)
                .header(HttpHeaders.AUTHORIZATION, "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, TEST_FILE_TYPE))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + TEST_FILE_NAME + "\""))
                .andExpect(content().bytes(TEST_SIGNATURE_BYTES));
    }

    @Test
    void fetchFile_shouldReturnBadRequest_whenSignatureServiceThrowsException() throws Exception {
        when(employeeSignatureServiceImpl.fetchSignature(TEST_USER_ID)).thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(get("/signature1/{userID}", TEST_USER_ID)
                .header(HttpHeaders.AUTHORIZATION, "Bearer token"))
                .andExpect(status().isBadRequest())
                .andExpect(content().bytes(new byte[] {})); // Expect empty byte array body
    }

    @Test
    void fetchFileFromCentral_shouldReturnSignatureJson_whenSignatureExists() throws Exception {
        EmployeeSignature mockSignature = new EmployeeSignature(TEST_USER_ID, TEST_SIGNATURE_BYTES, TEST_FILE_TYPE, TEST_FILE_NAME);

        when(employeeSignatureServiceImpl.fetchSignature(TEST_USER_ID)).thenReturn(mockSignature);

        OutputResponse expectedResponse = new OutputResponse();
        expectedResponse.setResponse(new Gson().toJson(mockSignature));

        mockMvc.perform(get("/signature1/getSignClass/{userID}", TEST_USER_ID)
                .header(HttpHeaders.AUTHORIZATION, "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResponse.toString()));
    }

    @Test
    void fetchFileFromCentral_shouldReturnNoRecordFoundError_whenSignatureDoesNotExist() throws Exception {
        when(employeeSignatureServiceImpl.fetchSignature(TEST_USER_ID)).thenReturn(null);

        OutputResponse expectedResponse = new OutputResponse();
        expectedResponse.setError(5000, "No record found");

        mockMvc.perform(get("/signature1/getSignClass/{userID}", TEST_USER_ID)
                .header(HttpHeaders.AUTHORIZATION, "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResponse.toString()));
    }

    @Test
    void fetchFileFromCentral_shouldReturnErrorJson_whenSignatureServiceThrowsException() throws Exception {
        String errorMessage = "Central service error";
        when(employeeSignatureServiceImpl.fetchSignature(TEST_USER_ID)).thenThrow(new RuntimeException(errorMessage));

        OutputResponse expectedResponse = new OutputResponse();
        expectedResponse.setError(5000, errorMessage);

        mockMvc.perform(get("/signature1/getSignClass/{userID}", TEST_USER_ID)
                .header(HttpHeaders.AUTHORIZATION, "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResponse.toString()));
    }

    @Test
    void existFile_shouldReturnTrue_whenSignatureExists() throws Exception {
        when(employeeSignatureServiceImpl.existSignature(TEST_USER_ID)).thenReturn(true);

        OutputResponse expectedResponse = new OutputResponse();
        expectedResponse.setResponse("true");

        mockMvc.perform(get("/signature1/signexist/{userID}", TEST_USER_ID)
                .header(HttpHeaders.AUTHORIZATION, "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResponse.toString()));
    }

    @Test
    void existFile_shouldReturnFalse_whenSignatureDoesNotExist() throws Exception {
        when(employeeSignatureServiceImpl.existSignature(TEST_USER_ID)).thenReturn(false);

        OutputResponse expectedResponse = new OutputResponse();
        expectedResponse.setResponse("false");

        mockMvc.perform(get("/signature1/signexist/{userID}", TEST_USER_ID)
                .header(HttpHeaders.AUTHORIZATION, "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResponse.toString()));
    }

    @Test
    void existFile_shouldReturnErrorJson_whenSignatureServiceThrowsException() throws Exception {
        String errorMessage = "Existence check failed";
        RuntimeException serviceException = new RuntimeException(errorMessage);
        when(employeeSignatureServiceImpl.existSignature(TEST_USER_ID)).thenThrow(serviceException);

        OutputResponse expectedResponse = new OutputResponse();
        expectedResponse.setError(serviceException); // OutputResponse.setError(Exception e) sets message from e.getMessage()

        mockMvc.perform(get("/signature1/signexist/{userID}", TEST_USER_ID)
                .header(HttpHeaders.AUTHORIZATION, "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResponse.toString()));
    }
}