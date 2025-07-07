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
package com.iemr.common.controller.abdmfacility;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;

import com.iemr.common.service.abdmfacility.AbdmFacilityService;
import com.iemr.common.utils.response.OutputResponse;

@ExtendWith(MockitoExtension.class)
class AbdmFacilityControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AbdmFacilityService abdmFacilityService;

    @InjectMocks
    private AbdmFacilityController abdmFacilityController;

    // Test constants
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_TOKEN = "Bearer some_valid_token";
    private static final String FACILITY_ENDPOINT = "/facility/getWorklocationMappedAbdmFacility/{workLocationId}";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(abdmFacilityController).build();
    }

    @Test
    void shouldReturnAbdmFacilityDetails_whenServiceReturnsData() throws Exception {
        int workLocationId = 123;
        String mockServiceResponse = "{\"facilityId\": \"1234\", \"facilityName\": \"Test Facility\"}";
        
        OutputResponse outputResponse = new OutputResponse();
        outputResponse.setResponse(mockServiceResponse);
        String expectedResponseBody = outputResponse.toString();

        when(abdmFacilityService.getMappedAbdmFacility(workLocationId)).thenReturn(mockServiceResponse);

        mockMvc.perform(get(FACILITY_ENDPOINT, workLocationId)
                .header(AUTHORIZATION_HEADER, BEARER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResponseBody));
        
        verify(abdmFacilityService).getMappedAbdmFacility(workLocationId);
    }

    @Test
    void shouldReturnErrorResponse_whenServiceThrowsException() throws Exception {
        int workLocationId = 456;
        String errorMessage = "Internal service error occurred";
        
        OutputResponse outputResponse = new OutputResponse();
        outputResponse.setError(5000, errorMessage);
        String expectedResponseBody = outputResponse.toString();

        when(abdmFacilityService.getMappedAbdmFacility(workLocationId)).thenThrow(new RuntimeException(errorMessage));

        mockMvc.perform(get(FACILITY_ENDPOINT, workLocationId)
                .header(AUTHORIZATION_HEADER, BEARER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResponseBody));
        
        verify(abdmFacilityService).getMappedAbdmFacility(workLocationId);
    }

    @Test
    void shouldReturnBadRequest_whenAuthorizationHeaderIsMissing() throws Exception {
        int workLocationId = 789;

        // The controller method requires Authorization header, so missing header should return 400
        mockMvc.perform(get(FACILITY_ENDPOINT, workLocationId))
                .andExpect(status().isBadRequest());
        
        // Service should not be called when required header is missing
        verifyNoInteractions(abdmFacilityService);
    }
}