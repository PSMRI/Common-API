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
package com.iemr.common.controller.eausadha;

import com.iemr.common.model.eAusadha.EAusadhaDTO;
import com.iemr.common.service.beneficiary.EAusadhaService;
import com.iemr.common.utils.response.OutputResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EAusadhaControllerTest {

    @Mock
    private EAusadhaService eAusadhaService;

    @InjectMocks
    private EAusadhaController eAusadhaController;

    // To mock the logger, we need to use reflection or a test utility
    // For this exercise, we'll assume direct mocking of the logger field is not feasible
    // without additional setup (e.g., PowerMock or specific Spring Test utilities for private fields).
    // We will focus on verifying the functional output and service interactions.
    // If logger verification were strictly required, one would typically use a test appender
    // or a library like LogCaptor to assert log messages.

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // The logger field in EAusadhaController is initialized statically.
        // To mock it, one would typically use reflection or a test utility like
        // ReflectionTestUtils.setField(eAusadhaController, "logger", mockLogger);
        // Assuming this is not allowed by the prompt's constraints, we won't mock the logger directly.
    }

    @Test
    void testCreateEAusadha_Success() throws Exception {
        // Arrange
        EAusadhaDTO eAusadhaDTO = new EAusadhaDTO(1, Timestamp.from(Instant.now()));
        String authorization = "Bearer token123";
        String serviceResponse = "{\"message\":\"EAusadha created successfully\"}";

        when(eAusadhaService.createEAusadha(any(EAusadhaDTO.class), eq(authorization)))
                .thenReturn(serviceResponse);

        // Act
        String result = eAusadhaController.createEAusadha(eAusadhaDTO, authorization);

        // Assert
        assertNotNull(result);

        JSONObject jsonResult = new JSONObject(result);
        assertEquals(OutputResponse.SUCCESS, jsonResult.getInt("statusCode"));
        assertEquals("Success", jsonResult.getString("status"));
        assertEquals("Success", jsonResult.getString("errorMessage"));
        
        // The data field in OutputResponse.setResponse can be a JSON object or a string.
        // If it's a string, it gets wrapped in {"response":"$$STRING"}
        // In this case, serviceResponse is a JSON string, so it should be parsed as a JSON object.
        JSONObject data = jsonResult.getJSONObject("data");
        assertEquals("EAusadha created successfully", data.getString("message"));

        verify(eAusadhaService, times(1)).createEAusadha(eAusadhaDTO, authorization);
        // Verification for logger.info("get eausadha request:" + eAusadhaDTO); would go here
        // if the logger was mockable and its interactions were being verified.
    }

    @Test
    void testCreateEAusadha_ServiceThrowsException() throws Exception {
        // Arrange
        EAusadhaDTO eAusadhaDTO = new EAusadhaDTO(2, Timestamp.from(Instant.now()));
        String authorization = "Bearer token456";
        String errorMessage = "Simulated service error";

        when(eAusadhaService.createEAusadha(any(EAusadhaDTO.class), eq(authorization)))
                .thenThrow(new RuntimeException(errorMessage));

        // Act
        String result = eAusadhaController.createEAusadha(eAusadhaDTO, authorization);

        // Assert
        assertNotNull(result);

        JSONObject jsonResult = new JSONObject(result);
        assertEquals(5000, jsonResult.getInt("statusCode")); // Error code set in controller
        assertEquals("Error while entering the Stocks.", jsonResult.getString("status")); // Set by setError(code, message)
        assertEquals("Error while entering the Stocks.", jsonResult.getString("errorMessage")); // Set by setError(code, message)
        
        // Data should be null or empty in case of error, depending on OutputResponse implementation
        // OutputResponse.toString() with excludeFieldsWithoutExposeAnnotation will not expose 'data' if it's not set.
        // In this case, 'data' is not set on error, so it won't be in the JSON.
        // We can assert that 'data' key is not present or is null if it were always present.
        // Based on OutputResponse.toString(), 'data' is only exposed if it's set.
        // So, we assert that the error message is correct.

        verify(eAusadhaService, times(1)).createEAusadha(eAusadhaDTO, authorization);
        // Verification for logger.error (implicitly via OutputResponse.setError) would go here
        // if the logger was mockable and its interactions were being verified.
    }
}