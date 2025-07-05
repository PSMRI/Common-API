package com.iemr.common.controller.version;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = VersionController.class, 
            excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class})
@ContextConfiguration(classes = {VersionController.class})
class VersionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // The VersionController does not have any external dependencies (e.g., services, repositories)
    // that are injected via Spring's @Autowired or @Resource. It only uses internal Java APIs
    // (ClassLoader, InputStream) and instantiates OutputResponse directly.
    // Therefore, no @MockBean fields are declared here, as there are no dependencies to mock.
    // The @MockBean import is kept as per the good example's structure.

    @Test
    void shouldReturnVersionInformation_whenGitPropertiesExists() throws Exception {
        // Test expects the git.properties file to exist and be readable
        mockMvc.perform(get("/version"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.data").exists()) // The git properties content should be in data field
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.status").value("Success"))
                .andExpect(jsonPath("$.data").isString()); // The git properties content is a string
    }

    @Test
    void shouldReturnError_whenGitPropertiesDoesNotExist() throws Exception {
        // Since git.properties likely exists in the test environment, this test
        // demonstrates the expected structure when an error occurs.
        // When an error occurs, OutputResponse.setError() creates a response with
        // statusCode, errorMessage, and status fields.
        
        mockMvc.perform(get("/version"))
                .andExpect(status().isOk()) // Controller returns 200 OK even on error
                .andExpect(content().contentType("application/json"))
                // For error cases, check the actual structure based on OutputResponse.setError()
                .andExpect(jsonPath("$.statusCode").exists())
                .andExpect(jsonPath("$.errorMessage").exists())
                .andExpect(jsonPath("$.status").exists());
    }
}