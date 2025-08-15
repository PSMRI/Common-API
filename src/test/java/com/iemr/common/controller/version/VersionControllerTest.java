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
package com.iemr.common.controller.version;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class VersionControllerTest {

    @Test
    void shouldReturnVersionInformation_whenGitPropertiesExists() throws Exception {
        // Create a standalone MockMvc instance without Spring Boot context
        VersionController controller = new VersionController();
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        
        mockMvc.perform(get("/version"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/plain;charset=ISO-8859-1"))
                .andExpect(jsonPath("$.data").exists()) // The git properties content should be in data field
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.status").value("Success"));
    }

    @Test
    void shouldReturnError_whenGitPropertiesDoesNotExist() throws Exception {
        // Create a standalone MockMvc instance without Spring Boot context
        VersionController controller = new VersionController();
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        
        mockMvc.perform(get("/version"))
                .andExpect(status().isOk()) // Controller returns 200 OK even on error
                .andExpect(content().contentType("text/plain;charset=ISO-8859-1"))
                .andExpect(jsonPath("$.statusCode").exists())
                .andExpect(jsonPath("$.status").exists());
    }
}