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
package com.iemr.common.controller.uptsu;
import com.iemr.common.service.uptsu.UptsuService;
import com.iemr.common.utils.response.OutputResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UPTechnicalSupportControllerTest {
    private MockMvc mockMvc;

    @Mock
    private UptsuService uptsuService;

    @InjectMocks
    private UPTechnicalSupportController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void shouldReturnFacilityData_whenGetFacilityIsSuccessful() throws Exception {
        Integer providerServiceMapID = 1;
        String blockName = "TestBlock";
        String mockServiceResponseData = "{\"id\":1,\"name\":\"Test Facility\"}";

        when(uptsuService.getFacility(providerServiceMapID, blockName)).thenReturn(mockServiceResponseData);

        mockMvc.perform(get("/uptsu/get/facilityMaster/{providerServiceMapID}/{blockName}", providerServiceMapID, blockName)
                .header("Authorization", "Bearer token")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Test Facility"));
    }

    @Test
    void shouldReturnError_whenGetFacilityThrowsException() throws Exception {
        Integer providerServiceMapID = 1;
        String blockName = "TestBlock";
        String errorMessage = "Service unavailable";

        when(uptsuService.getFacility(providerServiceMapID, blockName)).thenThrow(new RuntimeException(errorMessage));

        mockMvc.perform(get("/uptsu/get/facilityMaster/{providerServiceMapID}/{blockName}", providerServiceMapID, blockName)
                .header("Authorization", "Bearer token")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorMessage").value(errorMessage));
    }

    @Test
    void shouldReturnSuccess_whenSaveAppointmentDetailsIsSuccessful() throws Exception {
        String requestBody = "{\"appointmentId\":123,\"details\":\"some details\"}";
        String authorizationHeader = "Bearer token";
        String mockServiceResponse = "Appointment saved successfully";

        when(uptsuService.saveAppointmentDetails(requestBody, authorizationHeader)).thenReturn(mockServiceResponse);

        mockMvc.perform(post("/uptsu/save/appointment-details")
                .header("Authorization", authorizationHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.response").value(mockServiceResponse));
    }

    @Test
    void shouldReturnError_whenSaveAppointmentDetailsThrowsException() throws Exception {
        String requestBody = "{\"appointmentId\":123,\"details\":\"some details\"}";
        String authorizationHeader = "Bearer token";
        String errorMessage = "Failed to save appointment";

        when(uptsuService.saveAppointmentDetails(requestBody, authorizationHeader)).thenThrow(new RuntimeException(errorMessage));

        mockMvc.perform(post("/uptsu/save/appointment-details")
                .header("Authorization", authorizationHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorMessage").value(errorMessage));
    }

    @Test
    void shouldReturnError_whenSaveAppointmentDetailsHasInvalidRequestBody() throws Exception {
        String invalidRequestBody = "{invalid json}";
        String authorizationHeader = "Bearer token";
        String errorMessage = "JSON parse error";

        when(uptsuService.saveAppointmentDetails(anyString(), anyString())).thenThrow(new RuntimeException(errorMessage));

        mockMvc.perform(post("/uptsu/save/appointment-details")
                .header("Authorization", authorizationHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequestBody)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorMessage").value(errorMessage));
    }
}