package com.iemr.common.controller.lungassessment;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import com.iemr.common.service.lungassessment.LungAssessmentService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = LungAssessmentController.class, 
            excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class})
@ContextConfiguration(classes = {LungAssessmentController.class})
class LungAssessmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LungAssessmentService lungAssessmentService;

    // Helper methods removed - we'll use jsonPath assertions instead to match actual OutputResponse structure

    @Test
    void shouldStartAssessment_whenValidFileAndRequestProvided() throws Exception {
        String requestJson = "{\"patientId\":123, \"type\":\"cough\"}";
        String serviceResponse = "Assessment initiated successfully: ID_123";
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.wav",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                "audio_data".getBytes()
        );

        when(lungAssessmentService.initiateAssesment(anyString(), any(MultipartFile.class)))
                .thenReturn(serviceResponse);

        mockMvc.perform(multipart("/lungAssessment/startAssesment")
                .file(file)
                .param("request", requestJson)
                .header("Authorization", "Bearer token")) // Authorization header is required by controller
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/plain;charset=UTF-8")) // Controller returns String, not JSON object
                .andExpect(jsonPath("$.data.response").value(serviceResponse))
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.status").value("Success"));
    }

    @Test
    void shouldReturnError_whenStartAssessmentFails() throws Exception {
        String requestJson = "{\"patientId\":123, \"type\":\"cough\"}";
        String errorMessage = "Failed to process audio file due to server error.";
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.wav",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                "audio_data".getBytes()
        );

        when(lungAssessmentService.initiateAssesment(anyString(), any(MultipartFile.class)))
                .thenThrow(new RuntimeException(errorMessage));

        mockMvc.perform(multipart("/lungAssessment/startAssesment")
                .file(file)
                .param("request", requestJson)
                .header("Authorization", "Bearer token"))
                .andExpect(status().isOk()) // Controller catches exception and returns 200 OK with error in body
                .andExpect(content().contentType("text/plain;charset=UTF-8")) // Controller returns String, not JSON object
                .andExpect(jsonPath("$.statusCode").value(5000))
                .andExpect(jsonPath("$.errorMessage").value(errorMessage))
                .andExpect(jsonPath("$.status").value(containsString("Failed with")));
    }

    @Test
    void shouldGetAssessment_whenAssessmentIdIsValid() throws Exception {
        String assessmentId = "ASSESS_456";
        String serviceResponse = "{\"assessmentId\":\"ASSESS_456\", \"status\":\"completed\", \"result\":\"normal\"}";

        when(lungAssessmentService.getAssesment(assessmentId)).thenReturn(serviceResponse);

        mockMvc.perform(get("/lungAssessment/getAssesment/{assessmentId}", assessmentId)
                .header("Authorization", "Bearer token")) // Authorization header is required by controller
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/plain;charset=UTF-8")) // Controller returns String, not JSON object
                .andExpect(jsonPath("$.data.assessmentId").value("ASSESS_456"))
                .andExpect(jsonPath("$.data.status").value("completed"))
                .andExpect(jsonPath("$.data.result").value("normal"))
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.status").value("Success"));
    }

    @Test
    void shouldReturnError_whenGetAssessmentFails() throws Exception {
        String assessmentId = "INVALID_ID";
        String errorMessage = "Assessment not found for ID: " + assessmentId;

        when(lungAssessmentService.getAssesment(assessmentId)).thenThrow(new RuntimeException(errorMessage));

        mockMvc.perform(get("/lungAssessment/getAssesment/{assessmentId}", assessmentId)
                .header("Authorization", "Bearer token"))
                .andExpect(status().isOk()) // Controller catches exception and returns 200 OK with error in body
                .andExpect(content().contentType("text/plain;charset=UTF-8")) // Controller returns String, not JSON object
                .andExpect(jsonPath("$.statusCode").value(5000))
                .andExpect(jsonPath("$.errorMessage").value(errorMessage))
                .andExpect(jsonPath("$.status").value(containsString("Failed with")));
    }

    @Test
    void shouldGetAssessmentDetails_whenPatientIdIsValid() throws Exception {
        Long patientId = 789L;
        String serviceResponse = "[{\"assessmentId\":\"ASSESS_789_1\", \"date\":\"2023-01-01\"}, {\"assessmentId\":\"ASSESS_789_2\", \"date\":\"2023-02-01\"}]";

        when(lungAssessmentService.getAssessmentDetails(patientId)).thenReturn(serviceResponse);

        mockMvc.perform(get("/lungAssessment/getAssesmentDetails/{patientId}", patientId)
                .header("Authorization", "Bearer token")) // Authorization header is required by controller
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/plain;charset=UTF-8")) 
                .andExpect(jsonPath("$.data[0].assessmentId").value("ASSESS_789_1"))
                .andExpect(jsonPath("$.data[0].date").value("2023-01-01"))
                .andExpect(jsonPath("$.data[1].assessmentId").value("ASSESS_789_2"))
                .andExpect(jsonPath("$.data[1].date").value("2023-02-01"))
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.status").value("Success"));
    }

    @Test
    void shouldReturnError_whenGetAssessmentDetailsFails() throws Exception {
        Long patientId = 999L;
        String errorMessage = "No assessment details found for patient ID: " + patientId;

        when(lungAssessmentService.getAssessmentDetails(patientId)).thenThrow(new RuntimeException(errorMessage));

        mockMvc.perform(get("/lungAssessment/getAssesmentDetails/{patientId}", patientId)
                .header("Authorization", "Bearer token"))
                .andExpect(status().isOk()) // Controller catches exception and returns 200 OK with error in body
                .andExpect(content().contentType("text/plain;charset=UTF-8")) 
                .andExpect(jsonPath("$.statusCode").value(5000))
                .andExpect(jsonPath("$.errorMessage").value(errorMessage))
                .andExpect(jsonPath("$.status").value(containsString("Failed with")));
    }
}