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
package com.iemr.common.controller.carestream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.concurrent.TimeUnit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Minimal test for CareStreamCreateOrderController focusing on HTTP layer only.
 * These tests avoid socket connections to prevent timeouts.
 */
@ExtendWith(MockitoExtension.class)
@Timeout(value = 3, unit = TimeUnit.SECONDS)
class CareStreamCreateOrderControllerMinimalTest {

    private MockMvc mockMvc;

    @InjectMocks
    private CareStreamCreateOrderController controller;

    private final String validJsonInput = "{\"firstName\":\"John\",\"lastName\":\"Doe\",\"patientID\":\"P123\",\"dob\":\"1990-01-01\",\"gender\":\"M\",\"acc\":\"ACC123\"}";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    // Test constants
    private static final String CREATE_ORDER_URL = "/carestream/createOrder";
    private static final String UPDATE_ORDER_URL = "/carestream/UpdateOrder";
    private static final String DELETE_ORDER_URL = "/carestream/deleteOrder";
    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_TOKEN = "Bearer test-token";

    // Test authorization header requirements
    @Test
    void createOrder_shouldRequireAuthorizationHeader() throws Exception {
        mockMvc.perform(post(CREATE_ORDER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(validJsonInput))
                .andExpect(status().isNotFound()); // 404 because headers="Authorization" is required
    }

    @Test
    void updateOrder_shouldRequireAuthorizationHeader() throws Exception {
        mockMvc.perform(post(UPDATE_ORDER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(validJsonInput))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteOrder_shouldRequireAuthorizationHeader() throws Exception {
        mockMvc.perform(post(DELETE_ORDER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(validJsonInput))
                .andExpect(status().isNotFound());
    }

    // Test empty body handling (should fail fast without socket connection)
    @Test
    void createOrder_shouldHandleEmptyBody() throws Exception {
        mockMvc.perform(post(CREATE_ORDER_URL)
                .header(AUTH_HEADER, BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isBadRequest()); // Empty body returns 400 Bad Request
    }

    // Test endpoint path variations
    @Test
    void shouldReturn404ForInvalidPaths() throws Exception {
        mockMvc.perform(post("/carestream/invalidEndpoint")
                .header(AUTH_HEADER, BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(validJsonInput))
                .andExpect(status().isNotFound());
    }

    // Test HTTP method variations
    @Test
    void shouldReturn405ForUnsupportedHttpMethods() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(CREATE_ORDER_URL)
                .header(AUTH_HEADER, BEARER_TOKEN))
                .andExpect(status().isMethodNotAllowed());
    }
}
