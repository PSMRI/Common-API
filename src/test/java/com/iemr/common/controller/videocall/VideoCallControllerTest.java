package com.iemr.common.controller.videocall;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iemr.common.model.videocall.UpdateCallRequest;
import com.iemr.common.model.videocall.VideoCallRequest;
import com.iemr.common.service.videocall.VideoCallService;
import com.iemr.common.utils.response.OutputResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class VideoCallControllerTest {
    private MockMvc mockMvc;
    private VideoCallService videoCallService;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() throws Exception {
        videoCallService = Mockito.mock(VideoCallService.class);
        VideoCallController controller = new VideoCallController();
        // Use reflection to inject the mock into the private field
        java.lang.reflect.Field serviceField = VideoCallController.class.getDeclaredField("videoCallService");
        serviceField.setAccessible(true);
        serviceField.set(controller, videoCallService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    // Tests for generateJitsiLink()
    @Test
    void shouldReturnMeetingLink_whenGenerateLinkIsSuccessful() throws Exception {
        String expectedLink = "https://jitsi.example.com/test-meeting-link";
        when(videoCallService.generateMeetingLink()).thenReturn(expectedLink);

        mockMvc.perform(post("/video-consultation/generate-link")
                .header("Authorization", "Bearer dummy_token")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meetingLink").value(expectedLink));

        verify(videoCallService, times(1)).generateMeetingLink();
    }

    @Test
    void shouldReturnInternalServerError_whenGenerateLinkFails() throws Exception {
        String errorMessage = "Failed to generate link";
        when(videoCallService.generateMeetingLink()).thenThrow(new RuntimeException(errorMessage));

        mockMvc.perform(post("/video-consultation/generate-link")
                .header("Authorization", "Bearer dummy_token")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value(errorMessage));

        verify(videoCallService, times(1)).generateMeetingLink();
    }

    // Tests for sendVideoLink()
    @Test
    void shouldReturnSuccessResponse_whenSendLinkIsSuccessful() throws Exception {
        String requestJson = "{\"patientId\":123,\"providerId\":456,\"meetingLink\":\"test_link\"}";
        String serviceResponse = "{\"status\":\"success\",\"message\":\"Link sent successfully\"}";

        when(videoCallService.sendMeetingLink(any(VideoCallRequest.class))).thenReturn(serviceResponse);

        mockMvc.perform(post("/video-consultation/send-link")
                .header("Authorization", "Bearer dummy_token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(serviceResponse));

        verify(videoCallService, times(1)).sendMeetingLink(any(VideoCallRequest.class));
    }

    @Test
    void shouldReturnErrorResponse_whenSendLinkFails() throws Exception {
        String requestJson = "{\"patientId\":123,\"providerId\":456,\"meetingLink\":\"test_link\"}";
        String errorMessage = "Failed to send link";
        OutputResponse expectedOutputResponse = new OutputResponse();
        expectedOutputResponse.setError(new RuntimeException(errorMessage));

        when(videoCallService.sendMeetingLink(any(VideoCallRequest.class))).thenThrow(new RuntimeException(errorMessage));

        mockMvc.perform(post("/video-consultation/send-link")
                .header("Authorization", "Bearer dummy_token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk()) // Controller returns 200 OK even on error, with error in body
                .andExpect(content().json(expectedOutputResponse.toString()));

        verify(videoCallService, times(1)).sendMeetingLink(any(VideoCallRequest.class));
    }

    // Tests for updateCallStatus()
    @Test
    void shouldReturnSuccess_whenUpdateCallStatusIsSuccessful() throws Exception {
        UpdateCallRequest request = new UpdateCallRequest();
        request.setMeetingLink("test_meeting_link");
        request.setCallStatus("COMPLETED");
        String serviceResult = "Call status updated successfully";

        when(videoCallService.updateCallStatus(any(UpdateCallRequest.class))).thenReturn(serviceResult);

        mockMvc.perform(post("/video-consultation/update-call-status")
                .header("Authorization", "Bearer dummy_token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Success"));

        verify(videoCallService, times(1)).updateCallStatus(any(UpdateCallRequest.class));
    }

    @Test
    void shouldReturnBadRequest_whenUpdateCallStatusHasMissingMeetingLink() throws Exception {
        UpdateCallRequest request = new UpdateCallRequest();
        request.setCallStatus("COMPLETED"); // Missing meetingLink

        mockMvc.perform(post("/video-consultation/update-call-status")
                .header("Authorization", "Bearer dummy_token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Meeting Link and Status are required"));

        verifyNoInteractions(videoCallService); // Service should not be called due to validation error
    }

    @Test
    void shouldReturnBadRequest_whenUpdateCallStatusHasMissingCallStatus() throws Exception {
        UpdateCallRequest request = new UpdateCallRequest();
        request.setMeetingLink("test_meeting_link"); // Missing callStatus

        mockMvc.perform(post("/video-consultation/update-call-status")
                .header("Authorization", "Bearer dummy_token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Meeting Link and Status are required"));

        verifyNoInteractions(videoCallService); // Service should not be called due to validation error
    }

    @Test
    void shouldReturnOkWithErrorInBody_whenUpdateCallStatusServiceFails() throws Exception {
        UpdateCallRequest request = new UpdateCallRequest();
        request.setMeetingLink("test_meeting_link");
        request.setCallStatus("COMPLETED");
        String errorMessage = "Database error during update";
        OutputResponse expectedOutputResponse = new OutputResponse();
        expectedOutputResponse.setError(new RuntimeException(errorMessage));

        when(videoCallService.updateCallStatus(any(UpdateCallRequest.class))).thenThrow(new RuntimeException(errorMessage));

        mockMvc.perform(post("/video-consultation/update-call-status")
                .header("Authorization", "Bearer dummy_token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()) // Controller returns 200 OK even on error, with error in body
                .andExpect(content().json(expectedOutputResponse.toString()));

        verify(videoCallService, times(1)).updateCallStatus(any(UpdateCallRequest.class));
    }
}