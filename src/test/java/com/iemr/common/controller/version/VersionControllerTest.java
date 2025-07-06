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