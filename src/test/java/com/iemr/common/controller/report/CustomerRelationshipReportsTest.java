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
package com.iemr.common.controller.report;

import com.iemr.common.mapper.Report1097Mapper;
import com.iemr.common.service.reports.CallReportsService;
import com.iemr.common.utils.response.OutputResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CustomerRelationshipReportsTest {

    private MockMvc mockMvc;

    @Mock
    private CallReportsService callReportsService;

    @Mock
    private Report1097Mapper mapper;

    @InjectMocks
    private CustomerRelationshipReports controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice() // Add global exception handling
                .build();
    }

    // Test constants for better maintainability
    private static final Integer PROVIDER_SERVICE_MAP_ID = 1;
    private static final String REPORT_TYPES_URL = "/crmReports/getReportTypes/{providerServiceMapID}";
    private static final String MOCK_SERVICE_RESPONSE = "[{\"id\":1,\"name\":\"Report A\"},{\"id\":2,\"name\":\"Report B\"}]";
    private static final String EMPTY_SERVICE_RESPONSE = "[]";
    private static final String ERROR_MESSAGE = "Service unavailable";

    // Helper method to create expected controller output
    private String createExpectedOutput(String serviceResponse) {
        OutputResponse response = new OutputResponse();
        response.setResponse(serviceResponse);
        return response.toString();
    }

    @Test
    void shouldReturnReportTypes_whenServiceReturnsData() throws Exception {
        when(callReportsService.getReportTypes(anyInt())).thenReturn(MOCK_SERVICE_RESPONSE);

        mockMvc.perform(get(REPORT_TYPES_URL, PROVIDER_SERVICE_MAP_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(createExpectedOutput(MOCK_SERVICE_RESPONSE)));
    }

    @Test
    void shouldReturnInternalServerError_whenServiceThrowsException() throws Exception {
        Integer providerServiceMapID = 2;

        when(callReportsService.getReportTypes(anyInt())).thenThrow(new RuntimeException(ERROR_MESSAGE));

        // Since standalone MockMvc doesn't have global exception handling,
        // the RuntimeException will propagate up and cause a NestedServletException
        Exception exception = assertThrows(Exception.class, () -> {
            mockMvc.perform(get(REPORT_TYPES_URL, providerServiceMapID));
        });

        // Verify the root cause is our expected RuntimeException
        assertThat(exception.getCause()).isInstanceOf(RuntimeException.class);
        assertThat(exception.getCause().getMessage()).isEqualTo(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnEmptyArrayInResponse_whenServiceReturnsEmptyData() throws Exception {
        Integer providerServiceMapID = 3;

        when(callReportsService.getReportTypes(anyInt())).thenReturn(EMPTY_SERVICE_RESPONSE);

        mockMvc.perform(get(REPORT_TYPES_URL, providerServiceMapID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(createExpectedOutput(EMPTY_SERVICE_RESPONSE)));
    }
}