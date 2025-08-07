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
package com.iemr.common.controller.helpline104history;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iemr.common.data.helpline104history.H104BenMedHistory;
import com.iemr.common.service.helpline104history.H104BenHistoryServiceImpl;
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

import java.util.ArrayList;
import java.sql.Timestamp;
import java.sql.Date;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.hamcrest.Matchers.containsString;

@ExtendWith(MockitoExtension.class)
class Helpline104BeneficiaryHistoryControllerTest {

    private MockMvc mockMvc;

    @Mock
    private H104BenHistoryServiceImpl smpleBenHistoryServiceImpl;

    @InjectMocks
    private Helpline104BeneficiaryHistoryController controller;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void getBenCaseSheet_Success() throws Exception {
        Long beneficiaryRegID = 12345L;
        String requestJson = "{\"beneficiaryRegID\":" + beneficiaryRegID + "}";

        ArrayList<H104BenMedHistory> mockHistoryList = new ArrayList<>();
        H104BenMedHistory history1 = new H104BenMedHistory(beneficiaryRegID, "John Doe", 30, 1, "Male", "Algo1", "Symptom1", "Summary1", "SumID1", "None", "Diagnosis1", "DiagID1", "Advice1", 1L, "HAO1", "CO1", "MO1", "Remarks1", false, "User1", new Timestamp(System.currentTimeMillis()), "User1", new java.sql.Date(System.currentTimeMillis()));
        H104BenMedHistory history2 = new H104BenMedHistory(beneficiaryRegID, "Jane Doe", 25, 2, "Female", "Algo2", "Symptom2", "Summary2", "SumID2", "Pollen", "Diagnosis2", "DiagID2", "Advice2", 2L, "HAO2", "CO2", "MO2", "Remarks2", false, "User2", new Timestamp(System.currentTimeMillis()), "User2", new java.sql.Date(System.currentTimeMillis()));
        mockHistoryList.add(history1);
        mockHistoryList.add(history2);

        when(smpleBenHistoryServiceImpl.geSmpleBenHistory(beneficiaryRegID)).thenReturn(mockHistoryList);

        // Assuming H104BenMedHistory fields are not @Expose, OutputMapper.gson().toJson(this) will produce "{}"
        // And ArrayList.toString() will produce "[{}, {}]"
        String expectedDataJson = "[{}, {}]";

        String expectedResponseJson = String.format(
            "{\"data\":%s,\"statusCode\":%d,\"errorMessage\":\"%s\",\"status\":\"%s\"}",
            expectedDataJson,
            OutputResponse.SUCCESS,
            "Success",
            "Success"
        );

        mockMvc.perform(post("/beneficiary/get104BenMedHistory")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedResponseJson));
    }

    @Test
    void getBenCaseSheet_ServiceException() throws Exception {
        Long beneficiaryRegID = 12345L;
        String requestJson = "{\"beneficiaryRegID\":" + beneficiaryRegID + "}";
        String errorMessage = "Simulated database error";

        when(smpleBenHistoryServiceImpl.geSmpleBenHistory(anyLong())).thenThrow(new RuntimeException(errorMessage));

        mockMvc.perform(post("/beneficiary/get104BenMedHistory")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.statusCode").value(OutputResponse.GENERIC_FAILURE))
                .andExpect(jsonPath("$.errorMessage").value(errorMessage))
                .andExpect(jsonPath("$.status", containsString("Failed with " + errorMessage)))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void getBenCaseSheet_InvalidInput() throws Exception {
        String invalidRequestJson = "{\"beneficiaryRegID\":\"not_a_long\"}";
        String expectedErrorMessagePart = "Cannot deserialize value of type `java.lang.Long` from String \"not_a_long\"";

        mockMvc.perform(post("/beneficiary/get104BenMedHistory")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.statusCode").value(OutputResponse.GENERIC_FAILURE))
                .andExpect(jsonPath("$.errorMessage", containsString(expectedErrorMessagePart)))
                .andExpect(jsonPath("$.status", containsString("Failed with " + expectedErrorMessagePart)))
                .andExpect(jsonPath("$.data").doesNotExist());
    }
}