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
package com.iemr.common.controller.door_to_door_app;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.iemr.common.controller.door_to_door_app.DoorToDoorAppController;


import org.mockito.ArgumentCaptor;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.containsString;

import com.iemr.common.service.door_to_door_app.DoorToDoorService;
import com.iemr.common.data.door_to_door_app.RequestParser;
import com.iemr.common.utils.response.OutputResponse;

@ExtendWith(MockitoExtension.class)
class DoorToDoorAppControllerTest {
    private MockMvc mockMvc;

    @Mock
    DoorToDoorService doorToDoorService;

    @InjectMocks
    DoorToDoorAppController doorToDoorAppController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(doorToDoorAppController).build();
    }

    @Test
    void getUserDetails_Success() throws Exception {
        String requestJson = "{\"userId\":\"testUser\"}";
        String serviceResponse = "{\"userDetails\":\"some data\"}";
        OutputResponse expectedOutputResponse = new OutputResponse();
        expectedOutputResponse.setResponse(serviceResponse);

        when(doorToDoorService.getUserDetails(anyString())).thenReturn(serviceResponse);

        mockMvc.perform(post("/doortodoorapp/getUserDetails")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedOutputResponse.toString()));

        // Verify service called with correct argument (full JSON string)
        verify(doorToDoorService).getUserDetails(requestJson);
    }

    @Test
    void getUserDetails_ServiceReturnsNull() throws Exception {
        String requestJson = "{\"userId\":\"testUser\"}";
        OutputResponse expectedOutputResponse = new OutputResponse();
        expectedOutputResponse.setError(5000, "user details not found");

        when(doorToDoorService.getUserDetails(anyString())).thenReturn(null);

        mockMvc.perform(post("/doortodoorapp/getUserDetails")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedOutputResponse.toString()));

        // Verify service called with correct argument (full JSON string)
        verify(doorToDoorService).getUserDetails(requestJson);
    }

    @Test
    void getUserDetails_ServiceThrowsException() throws Exception {
        String requestJson = "{\"userId\":\"testUser\"}";
        OutputResponse expectedOutputResponse = new OutputResponse();
        Exception serviceException = new RuntimeException("Service error");
        expectedOutputResponse.setError(5000, "Unable to get user data, exception occured. " + serviceException.toString());

        when(doorToDoorService.getUserDetails(anyString())).thenThrow(serviceException);

        mockMvc.perform(post("/doortodoorapp/getUserDetails")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedOutputResponse.toString()));

        // Verify service called with correct argument (full JSON string)
        verify(doorToDoorService).getUserDetails(requestJson);
    }

    @Test
    void getSuspectedData_HRP_TB_NCD_Success() throws Exception {
        String requestJson = "{\"benRegID\":123,\"suspectedTB\":\"Y\",\"suspectedHRP\":\"N\",\"suspectedNCD\":\"Y\",\"suspectedNCDDiseases\":\"Diabetes\"}";
        String serviceResponse = "{\"suspectedStatus\":\"success\"}";
        OutputResponse expectedOutputResponse = new OutputResponse();
        expectedOutputResponse.setResponse(serviceResponse);

        when(doorToDoorService.get_NCD_TB_HRP_Suspected_Status(any(RequestParser.class))).thenReturn(serviceResponse);

        mockMvc.perform(post("/doortodoorapp/getSuspectedData_HRP_TB_NCD")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedOutputResponse.toString()));

        // Capture and verify RequestParser argument
        ArgumentCaptor<RequestParser> captor = ArgumentCaptor.forClass(RequestParser.class);
        verify(doorToDoorService).get_NCD_TB_HRP_Suspected_Status(captor.capture());
        RequestParser actual = captor.getValue();
        // Assert expected fields
        org.junit.jupiter.api.Assertions.assertEquals(123, actual.getBenRegID());
        org.junit.jupiter.api.Assertions.assertEquals("Y", actual.getSuspectedTB());
        org.junit.jupiter.api.Assertions.assertEquals("N", actual.getSuspectedHRP());
        org.junit.jupiter.api.Assertions.assertEquals("Y", actual.getSuspectedNCD());
        org.junit.jupiter.api.Assertions.assertEquals("Diabetes", actual.getSuspectedNCDDiseases());
    }

    @Test
    void getSuspectedData_HRP_TB_NCD_ServiceReturnsNull() throws Exception {
        String requestJson = "{\"benRegID\":123,\"suspectedTB\":\"Y\",\"suspectedHRP\":\"N\",\"suspectedNCD\":\"Y\",\"suspectedNCDDiseases\":\"Diabetes\"}";
        OutputResponse expectedOutputResponse = new OutputResponse();
        expectedOutputResponse.setError(5000, "Error in getting suspected information");

        when(doorToDoorService.get_NCD_TB_HRP_Suspected_Status(any(RequestParser.class))).thenReturn(null);

        mockMvc.perform(post("/doortodoorapp/getSuspectedData_HRP_TB_NCD")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedOutputResponse.toString()));

        // Capture and verify RequestParser argument
        ArgumentCaptor<RequestParser> captor = ArgumentCaptor.forClass(RequestParser.class);
        verify(doorToDoorService).get_NCD_TB_HRP_Suspected_Status(captor.capture());
        RequestParser actual = captor.getValue();
        org.junit.jupiter.api.Assertions.assertEquals(123, actual.getBenRegID());
        org.junit.jupiter.api.Assertions.assertEquals("Y", actual.getSuspectedTB());
        org.junit.jupiter.api.Assertions.assertEquals("N", actual.getSuspectedHRP());
        org.junit.jupiter.api.Assertions.assertEquals("Y", actual.getSuspectedNCD());
        org.junit.jupiter.api.Assertions.assertEquals("Diabetes", actual.getSuspectedNCDDiseases());
    }

    @Test
    void getSuspectedData_HRP_TB_NCD_ServiceThrowsException() throws Exception {
        String requestJson = "{\"benRegID\":123,\"suspectedTB\":\"Y\",\"suspectedHRP\":\"N\",\"suspectedNCD\":\"Y\",\"suspectedNCDDiseases\":\"Diabetes\"}";
        OutputResponse expectedOutputResponse = new OutputResponse();
        Exception serviceException = new RuntimeException("Service error");
        expectedOutputResponse.setError(5000, "Error in getting suspected information, exception occured. " + serviceException.toString());

        when(doorToDoorService.get_NCD_TB_HRP_Suspected_Status(any(RequestParser.class))).thenThrow(serviceException);

        mockMvc.perform(post("/doortodoorapp/getSuspectedData_HRP_TB_NCD")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedOutputResponse.toString()));

        // Capture and verify RequestParser argument
        ArgumentCaptor<RequestParser> captor = ArgumentCaptor.forClass(RequestParser.class);
        verify(doorToDoorService).get_NCD_TB_HRP_Suspected_Status(captor.capture());
        RequestParser actual = captor.getValue();
        org.junit.jupiter.api.Assertions.assertEquals(123, actual.getBenRegID());
        org.junit.jupiter.api.Assertions.assertEquals("Y", actual.getSuspectedTB());
        org.junit.jupiter.api.Assertions.assertEquals("N", actual.getSuspectedHRP());
        org.junit.jupiter.api.Assertions.assertEquals("Y", actual.getSuspectedNCD());
        org.junit.jupiter.api.Assertions.assertEquals("Diabetes", actual.getSuspectedNCDDiseases());
    }

    @Test
    void getSuspectedData_HRP_TB_NCD_InvalidJson() throws Exception {
        String invalidRequestJson = "{invalid json}";

        mockMvc.perform(post("/doortodoorapp/getSuspectedData_HRP_TB_NCD")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content(invalidRequestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(5000)))
                // The 'status' field in OutputResponse is set to the error message when setError(int, String) is called.
                .andExpect(jsonPath("$.status", containsString("Error in getting suspected information, exception occured.")))
                .andExpect(jsonPath("$.errorMessage", containsString("Error in getting suspected information, exception occured.")));

        // Service should not be called due to invalid JSON
        verify(doorToDoorService, org.mockito.Mockito.never()).get_NCD_TB_HRP_Suspected_Status(any(RequestParser.class));
    }
}