package com.iemr.common.controller.lungassessment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import com.iemr.common.service.lungassessment.LungAssessmentService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@ExtendWith(MockitoExtension.class)
class LungAssessmentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private LungAssessmentService lungAssessmentService;

    @InjectMocks
    private LungAssessmentController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice() // Add global exception handlers if any
                .build();
    }

    // Test data constants for better maintainability
    private static final String VALID_REQUEST_JSON = "{\"patientId\":123, \"type\":\"cough\"}";
    private static final String CONTENT_TYPE = "text/plain;charset=ISO-8859-1";
    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_TOKEN = "Bearer token";
    
    // API endpoints
    private static final String START_ASSESSMENT_URL = "/lungAssessment/startAssesment";
    private static final String GET_ASSESSMENT_URL = "/lungAssessment/getAssesment/{assessmentId}";
    private static final String GET_ASSESSMENT_DETAILS_URL = "/lungAssessment/getAssesmentDetails/{patientId}";
    
    // Test data
    private static final String ASSESSMENT_ID = "ASSESS_456";
    private static final Long PATIENT_ID = 789L;
    private static final String SUCCESS_RESPONSE = "Assessment initiated successfully: ID_123";

    private MockMultipartFile createTestFile() {
        return new MockMultipartFile(
                "file",
                "test.wav",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                "audio_data".getBytes()
        );
    }
    

    @Test
    void shouldStartAssessment_whenValidFileAndRequestProvided() throws Exception {
        MockMultipartFile file = createTestFile();

        when(lungAssessmentService.initiateAssesment(anyString(), any(MultipartFile.class)))
                .thenReturn(SUCCESS_RESPONSE);

        mockMvc.perform(multipart(START_ASSESSMENT_URL)
                .file(file)
                .param("request", VALID_REQUEST_JSON)
                .header(AUTH_HEADER, BEARER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().contentType(CONTENT_TYPE))
                .andExpect(jsonPath("$.data.response").value(SUCCESS_RESPONSE))
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.status").value("Success"));
    }

    @Test
    void shouldReturnError_whenStartAssessmentFails() throws Exception {
        String errorMessage = "Failed to process audio file due to server error.";
        MockMultipartFile file = createTestFile();

        when(lungAssessmentService.initiateAssesment(anyString(), any(MultipartFile.class)))
                .thenThrow(new RuntimeException(errorMessage));

        mockMvc.perform(multipart(START_ASSESSMENT_URL)
                .file(file)
                .param("request", VALID_REQUEST_JSON)
                .header(AUTH_HEADER, BEARER_TOKEN))
                .andExpect(status().isOk()) // Controller catches exception and returns 200 OK with error in body
                .andExpect(content().contentType(CONTENT_TYPE))
                .andExpect(jsonPath("$.statusCode").value(5000))
                .andExpect(jsonPath("$.errorMessage").value(errorMessage))
                .andExpect(jsonPath("$.status").value(containsString("Failed with")));
    }

    @Test
    void shouldGetAssessment_whenAssessmentIdIsValid() throws Exception {
        String serviceResponse = "{\"assessmentId\":\"ASSESS_456\", \"status\":\"completed\", \"result\":\"normal\"}";

        when(lungAssessmentService.getAssesment(ASSESSMENT_ID)).thenReturn(serviceResponse);

        mockMvc.perform(get(GET_ASSESSMENT_URL, ASSESSMENT_ID)
                .header(AUTH_HEADER, BEARER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().contentType(CONTENT_TYPE))
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

        mockMvc.perform(get(GET_ASSESSMENT_URL, assessmentId)
                .header(AUTH_HEADER, BEARER_TOKEN))
                .andExpect(status().isOk()) // Controller catches exception and returns 200 OK with error in body
                .andExpect(content().contentType(CONTENT_TYPE))
                .andExpect(jsonPath("$.statusCode").value(5000))
                .andExpect(jsonPath("$.errorMessage").value(errorMessage))
                .andExpect(jsonPath("$.status").value(containsString("Failed with")));
    }

    @Test
    void shouldGetAssessmentDetails_whenPatientIdIsValid() throws Exception {
        String serviceResponse = "[{\"assessmentId\":\"ASSESS_789_1\", \"date\":\"2023-01-01\"}, {\"assessmentId\":\"ASSESS_789_2\", \"date\":\"2023-02-01\"}]";

        when(lungAssessmentService.getAssessmentDetails(PATIENT_ID)).thenReturn(serviceResponse);

        mockMvc.perform(get(GET_ASSESSMENT_DETAILS_URL, PATIENT_ID)
                .header(AUTH_HEADER, BEARER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().contentType(CONTENT_TYPE)) 
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

        mockMvc.perform(get(GET_ASSESSMENT_DETAILS_URL, patientId)
                .header(AUTH_HEADER, BEARER_TOKEN))
                .andExpect(status().isOk()) // Controller catches exception and returns 200 OK with error in body
                .andExpect(content().contentType(CONTENT_TYPE)) 
                .andExpect(jsonPath("$.statusCode").value(5000))
                .andExpect(jsonPath("$.errorMessage").value(errorMessage))
                .andExpect(jsonPath("$.status").value(containsString("Failed with")));
    }
}