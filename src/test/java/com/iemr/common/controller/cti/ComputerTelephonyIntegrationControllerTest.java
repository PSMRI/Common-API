package com.iemr.common.controller.cti;
import org.springframework.test.context.ContextConfiguration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
// ...existing code...
import org.springframework.boot.test.mock.mockito.MockBean;
// ...existing code...
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.iemr.common.data.cti.CustomerLanguage;
import com.iemr.common.service.cti.CTIService;
import com.iemr.common.utils.response.OutputResponse;

@WebMvcTest(controllers = ComputerTelephonyIntegrationController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = {ComputerTelephonyIntegrationController.class})
@DisplayName("ComputerTelephonyIntegrationController Tests")

class ComputerTelephonyIntegrationControllerTest {

    @Autowired
    private MockMvc mockMvc; // Used to test controller endpoints

    @MockBean
    private CTIService ctiService; // Mock the service layer dependency


    @Test
    @DisplayName("Test getCampaignSkills - Success")
    void getCampaignSkills_Success() throws Exception {
        // Prepare request body
        String requestJson = "{\"campaign_name\":\"TestCampaign\"}";

        // Prepare expected service response
        OutputResponse expectedServiceResponse = new OutputResponse();
        expectedServiceResponse.setResponse("[\"Skill1\", \"Skill2\"]");

        // Mock the service call
        when(ctiService.getCampaignSkills(any(String.class), any(String.class)))
                .thenReturn(expectedServiceResponse);

        // Perform the request and assert
        mockMvc.perform(post("/cti/getCampaignSkills")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer mock_token")
                .content(requestJson)
                .with(request -> { request.setRemoteAddr("127.0.0.1"); return request; }))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedServiceResponse.toString())); // OutputResponse toString() returns JSON string

        // Verify that the service method was called
        verify(ctiService, times(1)).getCampaignSkills(requestJson, "127.0.0.1"); // Assuming default remote address
    }

    @Test
    @DisplayName("Test getAgentState - Success")
    void getAgentState_Success() throws Exception {
        String requestJson = "{\"agent_id\":\"agent123\"}";
        OutputResponse expectedServiceResponse = new OutputResponse();
        expectedServiceResponse.setResponse("{\"state\":\"Available\"}");

        when(ctiService.getAgentState(any(String.class), any(String.class)))
                .thenReturn(expectedServiceResponse);

        mockMvc.perform(post("/cti/getAgentState")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer mock_token")
                .content(requestJson)
                .with(request -> { request.setRemoteAddr("127.0.0.1"); return request; }))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedServiceResponse.toString()));

        verify(ctiService, times(1)).getAgentState(requestJson, "127.0.0.1");
    }

    @Test
    @DisplayName("Test doAgentLogout - Success")
    void doAgentLogout_Success() throws Exception {
        String requestJson = "{\"agent_id\":\"agent123\"}";
        OutputResponse expectedServiceResponse = new OutputResponse();
        expectedServiceResponse.setResponse("{\"message\":\"Logout successful\"}");

        when(ctiService.agentLogout(any(String.class), any(String.class)))
                .thenReturn(expectedServiceResponse);

        mockMvc.perform(post("/cti/doAgentLogout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .with(request -> { request.setRemoteAddr("127.0.0.1"); return request; })) // No Authorization header required for this endpoint
                .andExpect(status().isOk())
                .andExpect(content().json(expectedServiceResponse.toString()));

        verify(ctiService, times(1)).agentLogout(requestJson, "127.0.0.1");
    }

    @Test
    @DisplayName("Test transferCall - Success")
    void transferCall_Success() throws Exception {
        String requestJson = "{\"transfer_from\":\"agent1\", \"transfer_to\":\"agent2\", " +
                             "\"transfer_campaign_info\":\"NewCampaign\", \"skill_transfer_flag\":1, " +
                             "\"skill\":\"Sales\", \"benCallID\":12345, \"agentIPAddress\":\"192.168.1.100\", " +
                             "\"callTypeID\":1}";
        OutputResponse expectedServiceResponse = new OutputResponse();
        expectedServiceResponse.setResponse("{\"transferStatus\":\"Success\"}");

        when(ctiService.transferCall(any(String.class), any(String.class)))
                .thenReturn(expectedServiceResponse);

        mockMvc.perform(post("/cti/transferCall")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer mock_token")
                .content(requestJson)
                .with(request -> { request.setRemoteAddr("127.0.0.1"); return request; }))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedServiceResponse.toString()));

        verify(ctiService, times(1)).transferCall(requestJson, "127.0.0.1");
    }

    @Test
    @DisplayName("Test customerPreferredLanguage - Success")
    void customerPreferredLanguage_Success() throws Exception {
        // Prepare the JSON string as it would come in the request body
        String requestJson = "{" +
                             "\"cust_ph_no\":\"1234567890\"," +
                             "\"campaign_name\":\"TestCampaign\"," +
                             "\"language\":\"English\"," +
                             "\"action\":\"update\"" +
                             "}";

        OutputResponse expectedServiceResponse = new OutputResponse();
        expectedServiceResponse.setResponse("{\"status\":\"Language updated\"}");

        // Mock the service call with the CustomerLanguage object deserialized from JSON
        when(ctiService.customerPreferredLanguage(any(CustomerLanguage.class), any(String.class)))
                .thenReturn(expectedServiceResponse);

        mockMvc.perform(post("/cti/customerPreferredLanguage")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer mock_token")
                .content(requestJson)
                .with(request -> { request.setRemoteAddr("127.0.0.1"); return request; }))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedServiceResponse.toString()));

        // Verify with the actual CustomerLanguage object that would be deserialized in the controller
        verify(ctiService, times(1)).customerPreferredLanguage(any(CustomerLanguage.class), any(String.class));
    }

    @Test
    @DisplayName("Test getCampaignSkills - Service Exception")
    void getCampaignSkills_ServiceException() throws Exception {
        String requestJson = "{\"campaign_name\":\"ErrorCampaign\"}";
        RuntimeException serviceException = new RuntimeException("Service failure");

        // Mock the service to throw an exception
        when(ctiService.getCampaignSkills(any(String.class), any(String.class)))
                .thenThrow(serviceException);

        // Prepare expected error response
        OutputResponse expectedErrorResponse = new OutputResponse();
        expectedErrorResponse.setError(serviceException);

        // Perform the request and assert that an error response is returned
        mockMvc.perform(post("/cti/getCampaignSkills")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer mock_token")
                .content(requestJson)
                .with(request -> { request.setRemoteAddr("127.0.0.1"); return request; }))
                .andExpect(status().isOk()) // Controller returns 200 OK even on internal error
                .andExpect(content().json(expectedErrorResponse.toString())); // OutputResponse sets error: true

        verify(ctiService, times(1)).getCampaignSkills(requestJson, "127.0.0.1");
    }
}
