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
package com.iemr.common.controller.users;

import com.iemr.common.data.users.EmployeeSignature;
import com.iemr.common.service.users.EmployeeSignatureServiceImpl;
import com.iemr.common.utils.response.OutputResponse;
import com.google.gson.Gson;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class EmployeeSignatureControllerTest {

    private MockMvc mockMvc;

    @Mock
    private EmployeeSignatureServiceImpl employeeSignatureServiceImpl;

    @InjectMocks
    private EmployeeSignatureController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    // Test constants for better maintainability
    private static final Long TEST_USER_ID = 123L;
    private static final String TEST_FILE_NAME = "signature.png";
    private static final String TEST_FILE_TYPE = "image/png";
    private static final byte[] TEST_SIGNATURE_BYTES = "test_signature_data".getBytes();
    private static final String BEARER_TOKEN = "Bearer token";
    
    // API endpoints
    private static final String FETCH_SIGNATURE_URL = "/signature1/{userID}";
    private static final String FETCH_SIGNATURE_CLASS_URL = "/signature1/getSignClass/{userID}";
    private static final String SIGNATURE_EXISTS_URL = "/signature1/signexist/{userID}";

    // Helper method to create test signature
    private EmployeeSignature createTestSignature() {
        return new EmployeeSignature(TEST_USER_ID, TEST_SIGNATURE_BYTES, TEST_FILE_TYPE, TEST_FILE_NAME);
    }

    @Test
    void fetchFile_shouldReturnSignature_whenSignatureExists() throws Exception {
        EmployeeSignature mockSignature = createTestSignature();

        when(employeeSignatureServiceImpl.fetchSignature(TEST_USER_ID)).thenReturn(mockSignature);

        mockMvc.perform(get(FETCH_SIGNATURE_URL, TEST_USER_ID)
                .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, TEST_FILE_TYPE))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + TEST_FILE_NAME + "\""))
                .andExpect(content().bytes(TEST_SIGNATURE_BYTES));
    }

    @Test
    void fetchFile_shouldReturnBadRequest_whenSignatureServiceThrowsException() throws Exception {
        when(employeeSignatureServiceImpl.fetchSignature(TEST_USER_ID)).thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(get(FETCH_SIGNATURE_URL, TEST_USER_ID)
                .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN))
                .andExpect(status().isBadRequest())
                .andExpect(content().bytes(new byte[] {})); // Expect empty byte array body
    }

    @Test
    void fetchFileFromCentral_shouldReturnSignatureJson_whenSignatureExists() throws Exception {
        EmployeeSignature mockSignature = createTestSignature();

        when(employeeSignatureServiceImpl.fetchSignature(TEST_USER_ID)).thenReturn(mockSignature);

        OutputResponse expectedResponse = new OutputResponse();
        expectedResponse.setResponse(new Gson().toJson(mockSignature));

        mockMvc.perform(get(FETCH_SIGNATURE_CLASS_URL, TEST_USER_ID)
                .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResponse.toString()));
    }

    @Test
    void fetchFileFromCentral_shouldReturnNoRecordFoundError_whenSignatureDoesNotExist() throws Exception {
        when(employeeSignatureServiceImpl.fetchSignature(TEST_USER_ID)).thenReturn(null);

        OutputResponse expectedResponse = new OutputResponse();
        expectedResponse.setError(5000, "No record found");

        mockMvc.perform(get(FETCH_SIGNATURE_CLASS_URL, TEST_USER_ID)
                .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResponse.toString()));
    }

    @Test
    void fetchFileFromCentral_shouldReturnErrorJson_whenSignatureServiceThrowsException() throws Exception {
        String errorMessage = "Central service error";
        when(employeeSignatureServiceImpl.fetchSignature(TEST_USER_ID)).thenThrow(new RuntimeException(errorMessage));

        OutputResponse expectedResponse = new OutputResponse();
        expectedResponse.setError(5000, errorMessage);

        mockMvc.perform(get(FETCH_SIGNATURE_CLASS_URL, TEST_USER_ID)
                .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResponse.toString()));
    }

    @Test
    void existFile_shouldReturnTrue_whenSignatureExists() throws Exception {
        when(employeeSignatureServiceImpl.existSignature(TEST_USER_ID)).thenReturn(true);

        OutputResponse expectedResponse = new OutputResponse();
        expectedResponse.setResponse("true");

        mockMvc.perform(get(SIGNATURE_EXISTS_URL, TEST_USER_ID)
                .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResponse.toString()));
    }

    @Test
    void existFile_shouldReturnFalse_whenSignatureDoesNotExist() throws Exception {
        when(employeeSignatureServiceImpl.existSignature(TEST_USER_ID)).thenReturn(false);

        OutputResponse expectedResponse = new OutputResponse();
        expectedResponse.setResponse("false");

        mockMvc.perform(get(SIGNATURE_EXISTS_URL, TEST_USER_ID)
                .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN))
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

        mockMvc.perform(get(SIGNATURE_EXISTS_URL, TEST_USER_ID)
                .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResponse.toString()));
    }
}