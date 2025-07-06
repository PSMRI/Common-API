package com.iemr.common.controller.cti;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.iemr.common.data.cti.CustomerLanguage;
import com.iemr.common.service.cti.CTIService;
import com.iemr.common.utils.response.OutputResponse;

@ExtendWith(MockitoExtension.class)
@DisplayName("ComputerTelephonyIntegrationController Tests")
class ComputerTelephonyIntegrationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CTIService ctiService;

    @InjectMocks
    private ComputerTelephonyIntegrationController computerTelephonyIntegrationController;

    // Test constants
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_TOKEN = "Bearer mock_token";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(computerTelephonyIntegrationController).build();
    }


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
                .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().json(expectedServiceResponse.toString()));

        // Verify that the service method was called
        verify(ctiService, times(1)).getCampaignSkills(any(String.class), any(String.class));
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
                .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().json(expectedServiceResponse.toString()));

        verify(ctiService, times(1)).getAgentState(any(String.class), any(String.class));
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
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().json(expectedServiceResponse.toString()));

        verify(ctiService, times(1)).agentLogout(any(String.class), any(String.class));
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
                .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().json(expectedServiceResponse.toString()));

        verify(ctiService, times(1)).transferCall(any(String.class), any(String.class));
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
                .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
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
                .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .content(requestJson))
                .andExpect(status().isOk()) // Controller returns 200 OK even on internal error
                .andExpect(content().contentType("application/json"))
                .andExpect(content().json(expectedErrorResponse.toString()));

        verify(ctiService, times(1)).getCampaignSkills(any(String.class), any(String.class));
    }

    @Test
    @DisplayName("Test getAgentCallStats - Success")
    void getAgentCallStats_Success() throws Exception {
        String requestJson = "{\"agent_id\":\"agent123\"}";
        OutputResponse expectedServiceResponse = new OutputResponse();
        expectedServiceResponse.setResponse("{\"calls_handled\":10,\"calls_missed\":2}");

        when(ctiService.getAgentCallStats(any(String.class), any(String.class)))
                .thenReturn(expectedServiceResponse);

        mockMvc.perform(post("/cti/getAgentCallStats")
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().json(expectedServiceResponse.toString()));

        verify(ctiService, times(1)).getAgentCallStats(any(String.class), any(String.class));
    }

    @Test
    @DisplayName("Test getAgentCallStats - Service Exception")
    void getAgentCallStats_ServiceException() throws Exception {
        String requestJson = "{\"agent_id\":\"agent123\"}";
        RuntimeException serviceException = new RuntimeException("Service failure");

        when(ctiService.getAgentCallStats(any(String.class), any(String.class)))
                .thenThrow(serviceException);

        OutputResponse expectedErrorResponse = new OutputResponse();
        expectedErrorResponse.setError(serviceException);

        mockMvc.perform(post("/cti/getAgentCallStats")
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().json(expectedErrorResponse.toString()));

        verify(ctiService, times(1)).getAgentCallStats(any(String.class), any(String.class));
    }

    @Test
    @DisplayName("Test getCampaignNames - Success")
    void getCampaignNames_Success() throws Exception {
        String requestJson = "{\"serviceName\":\"TestService\",\"type\":\"inbound\"}";
        OutputResponse expectedServiceResponse = new OutputResponse();
        expectedServiceResponse.setResponse("[\"Campaign1\",\"Campaign2\"]");

        when(ctiService.getCampaignNames(any(String.class), any(String.class)))
                .thenReturn(expectedServiceResponse);

        mockMvc.perform(post("/cti/getCampaignNames")
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().json(expectedServiceResponse.toString()));

        verify(ctiService, times(1)).getCampaignNames(any(String.class), any(String.class));
    }

    @Test
    @DisplayName("Test getCampaignNames - Service Exception")
    void getCampaignNames_ServiceException() throws Exception {
        String requestJson = "{\"serviceName\":\"TestService\",\"type\":\"outbound\"}";
        RuntimeException serviceException = new RuntimeException("Service failure");

        when(ctiService.getCampaignNames(any(String.class), any(String.class)))
                .thenThrow(serviceException);

        OutputResponse expectedErrorResponse = new OutputResponse();
        expectedErrorResponse.setError(serviceException);

        mockMvc.perform(post("/cti/getCampaignNames")
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().json(expectedErrorResponse.toString()));

        verify(ctiService, times(1)).getCampaignNames(any(String.class), any(String.class));
    }

    @Test
    @DisplayName("Test getLoginKey - Success")
    void getLoginKey_Success() throws Exception {
        String requestJson = "{\"username\":\"testuser\",\"password\":\"testpass\"}";
        OutputResponse expectedServiceResponse = new OutputResponse();
        expectedServiceResponse.setResponse("{\"login_key\":\"abcd1234\"}");

        when(ctiService.getLoginKey(any(String.class), any(String.class)))
                .thenReturn(expectedServiceResponse);

        mockMvc.perform(post("/cti/getLoginKey")
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().json(expectedServiceResponse.toString()));

        verify(ctiService, times(1)).getLoginKey(any(String.class), any(String.class));
    }

    @Test
    @DisplayName("Test getLoginKey - Service Exception")
    void getLoginKey_ServiceException() throws Exception {
        String requestJson = "{\"username\":\"testuser\",\"password\":\"wrongpass\"}";
        RuntimeException serviceException = new RuntimeException("Invalid credentials");

        when(ctiService.getLoginKey(any(String.class), any(String.class)))
                .thenThrow(serviceException);

        OutputResponse expectedErrorResponse = new OutputResponse();
        expectedErrorResponse.setError(serviceException);

        mockMvc.perform(post("/cti/getLoginKey")
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().json(expectedErrorResponse.toString()));

        verify(ctiService, times(1)).getLoginKey(any(String.class), any(String.class));
    }

    @Test
    @DisplayName("Test getOnlineAgents - Success")
    void getOnlineAgents_Success() throws Exception {
        String requestJson = "{\"agent_id\":\"agent123\"}";
        OutputResponse expectedServiceResponse = new OutputResponse();
        expectedServiceResponse.setResponse("[{\"agent_id\":\"agent1\",\"status\":\"online\"},{\"agent_id\":\"agent2\",\"status\":\"online\"}]");

        when(ctiService.getOnlineAgents(any(String.class), any(String.class)))
                .thenReturn(expectedServiceResponse);

        mockMvc.perform(post("/cti/getOnlineAgents")
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().json(expectedServiceResponse.toString()));

        verify(ctiService, times(1)).getOnlineAgents(any(String.class), any(String.class));
    }

    @Test
    @DisplayName("Test getOnlineAgents - Service Exception")
    void getOnlineAgents_ServiceException() throws Exception {
        String requestJson = "{\"agent_id\":\"agent123\"}";
        RuntimeException serviceException = new RuntimeException("Service failure");

        when(ctiService.getOnlineAgents(any(String.class), any(String.class)))
                .thenThrow(serviceException);

        OutputResponse expectedErrorResponse = new OutputResponse();
        expectedErrorResponse.setError(serviceException);

        mockMvc.perform(post("/cti/getOnlineAgents")
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().json(expectedErrorResponse.toString()));

        verify(ctiService, times(1)).getOnlineAgents(any(String.class), any(String.class));
    }

    @Test
    @DisplayName("Test callBeneficiary - Success")
    void callBeneficiary_Success() throws Exception {
        String requestJson = "{\"agent_id\":\"agent123\",\"phone_num\":\"1234567890\"}";
        OutputResponse expectedServiceResponse = new OutputResponse();
        expectedServiceResponse.setResponse("{\"call_status\":\"initiated\"}");

        when(ctiService.callBeneficiary(any(String.class), any(String.class)))
                .thenReturn(expectedServiceResponse);

        mockMvc.perform(post("/cti/callBeneficiary")
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().json(expectedServiceResponse.toString()));

        verify(ctiService, times(1)).callBeneficiary(any(String.class), any(String.class));
    }

    @Test
    @DisplayName("Test callBeneficiary - Service Exception")
    void callBeneficiary_ServiceException() throws Exception {
        String requestJson = "{\"agent_id\":\"agent123\",\"phone_num\":\"invalid\"}";
        RuntimeException serviceException = new RuntimeException("Invalid phone number");

        when(ctiService.callBeneficiary(any(String.class), any(String.class)))
                .thenThrow(serviceException);

        OutputResponse expectedErrorResponse = new OutputResponse();
        expectedErrorResponse.setError(serviceException);

        mockMvc.perform(post("/cti/callBeneficiary")
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().json(expectedErrorResponse.toString()));

        verify(ctiService, times(1)).callBeneficiary(any(String.class), any(String.class));
    }

    @Test
    @DisplayName("Test addUpdateUserData - Success")
    void addUpdateUserData_Success() throws Exception {
        String requestJson = "{\"username\":\"testuser\",\"password\":\"testpass\",\"firstname\":\"John\"," +
                             "\"lastname\":\"Doe\",\"phone\":\"1234567890\",\"email\":\"john@example.com\"," +
                             "\"role\":\"Agent\",\"designation\":\"Senior Agent\"}";
        OutputResponse expectedServiceResponse = new OutputResponse();
        expectedServiceResponse.setResponse("{\"status\":\"User data updated\"}");

        when(ctiService.addUpdateUserData(any(String.class), any(String.class)))
                .thenReturn(expectedServiceResponse);

        mockMvc.perform(post("/cti/addUpdateUserData")
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().json(expectedServiceResponse.toString()));

        verify(ctiService, times(1)).addUpdateUserData(any(String.class), any(String.class));
    }

    @Test
    @DisplayName("Test addUpdateUserData - Service Exception")
    void addUpdateUserData_ServiceException() throws Exception {
        String requestJson = "{\"username\":\"testuser\",\"password\":\"testpass\"}";
        RuntimeException serviceException = new RuntimeException("Missing required fields");

        when(ctiService.addUpdateUserData(any(String.class), any(String.class)))
                .thenThrow(serviceException);

        OutputResponse expectedErrorResponse = new OutputResponse();
        expectedErrorResponse.setError(serviceException);

        mockMvc.perform(post("/cti/addUpdateUserData")
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().json(expectedErrorResponse.toString()));

        verify(ctiService, times(1)).addUpdateUserData(any(String.class), any(String.class));
    }

    @Test
    @DisplayName("Test getTransferCampaigns - Success")
    void getTransferCampaigns_Success() throws Exception {
        String requestJson = "{\"agent_id\":\"agent123\"}";
        OutputResponse expectedServiceResponse = new OutputResponse();
        expectedServiceResponse.setResponse("[\"Campaign1\",\"Campaign2\"]");

        when(ctiService.getTransferCampaigns(any(String.class), any(String.class)))
                .thenReturn(expectedServiceResponse);

        mockMvc.perform(post("/cti/getTransferCampaigns")
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().json(expectedServiceResponse.toString()));

        verify(ctiService, times(1)).getTransferCampaigns(any(String.class), any(String.class));
    }

    @Test
    @DisplayName("Test getTransferCampaigns - Service Exception")
    void getTransferCampaigns_ServiceException() throws Exception {
        String requestJson = "{\"agent_id\":\"agent123\"}";
        RuntimeException serviceException = new RuntimeException("Service failure");

        when(ctiService.getTransferCampaigns(any(String.class), any(String.class)))
                .thenThrow(serviceException);

        OutputResponse expectedErrorResponse = new OutputResponse();
        expectedErrorResponse.setError(serviceException);

        mockMvc.perform(post("/cti/getTransferCampaigns")
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().json(expectedErrorResponse.toString()));

        verify(ctiService, times(1)).getTransferCampaigns(any(String.class), any(String.class));
    }

    @Test
    @DisplayName("Test getCampaignRoles - Success")
    void getCampaignRoles_Success() throws Exception {
        String requestJson = "{\"campaign\":\"TestCampaign\"}";
        OutputResponse expectedServiceResponse = new OutputResponse();
        expectedServiceResponse.setResponse("[\"Role1\",\"Role2\"]");

        when(ctiService.getCampaignRoles(any(String.class), any(String.class)))
                .thenReturn(expectedServiceResponse);

        mockMvc.perform(post("/cti/getCampaignRoles")
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().json(expectedServiceResponse.toString()));

        verify(ctiService, times(1)).getCampaignRoles(any(String.class), any(String.class));
    }

    @Test
    @DisplayName("Test getCampaignRoles - Service Exception")
    void getCampaignRoles_ServiceException() throws Exception {
        String requestJson = "{\"campaign\":\"NonExistentCampaign\"}";
        RuntimeException serviceException = new RuntimeException("Campaign not found");

        when(ctiService.getCampaignRoles(any(String.class), any(String.class)))
                .thenThrow(serviceException);

        OutputResponse expectedErrorResponse = new OutputResponse();
        expectedErrorResponse.setError(serviceException);

        mockMvc.perform(post("/cti/getCampaignRoles")
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().json(expectedErrorResponse.toString()));

        verify(ctiService, times(1)).getCampaignRoles(any(String.class), any(String.class));
    }

    @Test
    @DisplayName("Test setCallDisposition - Success")
    void setCallDisposition_Success() throws Exception {
        String requestJson = "{\"agent_id\":\"agent123\",\"cust_disp\":\"Resolved\",\"category\":\"Support\"," +
                             "\"session_id\":\"sess123\"}";
        OutputResponse expectedServiceResponse = new OutputResponse();
        expectedServiceResponse.setResponse("{\"status\":\"Disposition set\"}");

        when(ctiService.setCallDisposition(any(String.class), any(String.class)))
                .thenReturn(expectedServiceResponse);

        mockMvc.perform(post("/cti/setCallDisposition")
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().json(expectedServiceResponse.toString()));

        verify(ctiService, times(1)).setCallDisposition(any(String.class), any(String.class));
    }

    @Test
    @DisplayName("Test setCallDisposition - Service Exception")
    void setCallDisposition_ServiceException() throws Exception {
        String requestJson = "{\"agent_id\":\"agent123\",\"cust_disp\":\"Invalid\"}";
        RuntimeException serviceException = new RuntimeException("Invalid disposition");

        when(ctiService.setCallDisposition(any(String.class), any(String.class)))
                .thenThrow(serviceException);

        OutputResponse expectedErrorResponse = new OutputResponse();
        expectedErrorResponse.setError(serviceException);

        mockMvc.perform(post("/cti/setCallDisposition")
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().json(expectedErrorResponse.toString()));

        verify(ctiService, times(1)).setCallDisposition(any(String.class), any(String.class));
    }

    @Test
    @DisplayName("Test createVoiceFile - Success")
    void createVoiceFile_Success() throws Exception {
        String requestJson = "{\"agent_id\":\"agent123\",\"session_id\":\"sess123\"}";
        OutputResponse expectedServiceResponse = new OutputResponse();
        expectedServiceResponse.setResponse("{\"file_id\":\"file123\"}");

        when(ctiService.createVoiceFile(any(String.class), any(String.class)))
                .thenReturn(expectedServiceResponse);

        mockMvc.perform(post("/cti/createVoiceFile")
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().json(expectedServiceResponse.toString()));

        verify(ctiService, times(1)).createVoiceFile(any(String.class), any(String.class));
    }

    @Test
    @DisplayName("Test createVoiceFile - Service Exception")
    void createVoiceFile_ServiceException() throws Exception {
        String requestJson = "{\"agent_id\":\"agent123\",\"session_id\":\"invalid\"}";
        RuntimeException serviceException = new RuntimeException("Invalid session");

        when(ctiService.createVoiceFile(any(String.class), any(String.class)))
                .thenThrow(serviceException);

        OutputResponse expectedErrorResponse = new OutputResponse();
        expectedErrorResponse.setError(serviceException);

        mockMvc.perform(post("/cti/createVoiceFile")
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().json(expectedErrorResponse.toString()));

        verify(ctiService, times(1)).createVoiceFile(any(String.class), any(String.class));
    }

    @Test
    @DisplayName("Test getVoiceFile - Success")
    void getVoiceFile_Success() throws Exception {
        String requestJson = "{\"agent_id\":\"agent123\",\"session_id\":\"sess123\"}";
        OutputResponse expectedServiceResponse = new OutputResponse();
        expectedServiceResponse.setResponse("{\"file_url\":\"http://example.com/file123.wav\"}");

        when(ctiService.getVoiceFile(any(String.class), any(String.class)))
                .thenReturn(expectedServiceResponse);

        mockMvc.perform(post("/cti/getVoiceFile")
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().json(expectedServiceResponse.toString()));

        verify(ctiService, times(1)).getVoiceFile(any(String.class), any(String.class));
    }

    @Test
    @DisplayName("Test getVoiceFile - Service Exception")
    void getVoiceFile_ServiceException() throws Exception {
        String requestJson = "{\"agent_id\":\"agent123\",\"session_id\":\"nonexistent\"}";
        RuntimeException serviceException = new RuntimeException("File not found");

        when(ctiService.getVoiceFile(any(String.class), any(String.class)))
                .thenThrow(serviceException);

        OutputResponse expectedErrorResponse = new OutputResponse();
        expectedErrorResponse.setError(serviceException);

        mockMvc.perform(post("/cti/getVoiceFile")
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().json(expectedErrorResponse.toString()));

        verify(ctiService, times(1)).getVoiceFile(any(String.class), any(String.class));
    }

    @Test
    @DisplayName("Test disconnectCall - Success")
    void disconnectCall_Success() throws Exception {
        String requestJson = "{\"agent_id\":\"agent123\"}";
        OutputResponse expectedServiceResponse = new OutputResponse();
        expectedServiceResponse.setResponse("{\"status\":\"Call disconnected\"}");

        when(ctiService.disconnectCall(any(String.class), any(String.class)))
                .thenReturn(expectedServiceResponse);

        mockMvc.perform(post("/cti/disconnectCall")
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().json(expectedServiceResponse.toString()));

        verify(ctiService, times(1)).disconnectCall(any(String.class), any(String.class));
    }

    @Test
    @DisplayName("Test disconnectCall - Service Exception")
    void disconnectCall_ServiceException() throws Exception {
        String requestJson = "{\"agent_id\":\"agent123\"}";
        RuntimeException serviceException = new RuntimeException("No active call");

        when(ctiService.disconnectCall(any(String.class), any(String.class)))
                .thenThrow(serviceException);

        OutputResponse expectedErrorResponse = new OutputResponse();
        expectedErrorResponse.setError(serviceException);

        mockMvc.perform(post("/cti/disconnectCall")
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().json(expectedErrorResponse.toString()));

        verify(ctiService, times(1)).disconnectCall(any(String.class), any(String.class));
    }

    @Test
    @DisplayName("Test switchToInbound - Success")
    void switchToInbound_Success() throws Exception {
        String requestJson = "{\"agent_id\":\"agent123\"}";
        OutputResponse expectedServiceResponse = new OutputResponse();
        expectedServiceResponse.setResponse("{\"status\":\"Switched to inbound\"}");

        when(ctiService.switchToInbound(any(String.class), any(String.class)))
                .thenReturn(expectedServiceResponse);

        mockMvc.perform(post("/cti/switchToInbound")
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().json(expectedServiceResponse.toString()));

        verify(ctiService, times(1)).switchToInbound(any(String.class), any(String.class));
    }

    @Test
    @DisplayName("Test switchToInbound - Service Exception")
    void switchToInbound_ServiceException() throws Exception {
        String requestJson = "{\"agent_id\":\"agent123\"}";
        RuntimeException serviceException = new RuntimeException("Switch failed");

        when(ctiService.switchToInbound(any(String.class), any(String.class)))
                .thenThrow(serviceException);

        OutputResponse expectedErrorResponse = new OutputResponse();
        expectedErrorResponse.setError(serviceException);

        mockMvc.perform(post("/cti/switchToInbound")
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().json(expectedErrorResponse.toString()));

        verify(ctiService, times(1)).switchToInbound(any(String.class), any(String.class));
    }

    @Test
    @DisplayName("Test switchToOutbound - Success")
    void switchToOutbound_Success() throws Exception {
        String requestJson = "{\"agent_id\":\"agent123\"}";
        OutputResponse expectedServiceResponse = new OutputResponse();
        expectedServiceResponse.setResponse("{\"status\":\"Switched to outbound\"}");

        when(ctiService.switchToOutbound(any(String.class), any(String.class)))
                .thenReturn(expectedServiceResponse);

        mockMvc.perform(post("/cti/switchToOutbound")
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().json(expectedServiceResponse.toString()));

        verify(ctiService, times(1)).switchToOutbound(any(String.class), any(String.class));
    }

    @Test
    @DisplayName("Test switchToOutbound - Service Exception")
    void switchToOutbound_ServiceException() throws Exception {
        String requestJson = "{\"agent_id\":\"agent123\"}";
        RuntimeException serviceException = new RuntimeException("Switch failed");

        when(ctiService.switchToOutbound(any(String.class), any(String.class)))
                .thenThrow(serviceException);

        OutputResponse expectedErrorResponse = new OutputResponse();
        expectedErrorResponse.setError(serviceException);

        mockMvc.perform(post("/cti/switchToOutbound")
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().json(expectedErrorResponse.toString()));

        verify(ctiService, times(1)).switchToOutbound(any(String.class), any(String.class));
    }

    @Test
    @DisplayName("Test getAgentIPAddress - Success")
    void getAgentIPAddress_Success() throws Exception {
        String requestJson = "{\"agent_id\":\"agent123\"}";
        OutputResponse expectedServiceResponse = new OutputResponse();
        expectedServiceResponse.setResponse("{\"ip_address\":\"192.168.1.100\"}");

        when(ctiService.getAgentIPAddress(any(String.class), any(String.class)))
                .thenReturn(expectedServiceResponse);

        mockMvc.perform(post("/cti/getAgentIPAddress")
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().json(expectedServiceResponse.toString()));

        verify(ctiService, times(1)).getAgentIPAddress(any(String.class), any(String.class));
    }

    @Test
    @DisplayName("Test getAgentIPAddress - Service Exception")
    void getAgentIPAddress_ServiceException() throws Exception {
        String requestJson = "{\"agent_id\":\"agent123\"}";
        RuntimeException serviceException = new RuntimeException("Agent not found");

        when(ctiService.getAgentIPAddress(any(String.class), any(String.class)))
                .thenThrow(serviceException);

        OutputResponse expectedErrorResponse = new OutputResponse();
        expectedErrorResponse.setError(serviceException);

        mockMvc.perform(post("/cti/getAgentIPAddress")
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().json(expectedErrorResponse.toString()));

        verify(ctiService, times(1)).getAgentIPAddress(any(String.class), any(String.class));
    }

    @Test
    @DisplayName("Test getAvailableAgentSkills - Success")
    void getAvailableAgentSkills_Success() throws Exception {
        String requestJson = "{\"campaignName\":\"TestCampaign\",\"skill\":\"Sales\"}";
        OutputResponse expectedServiceResponse = new OutputResponse();
        expectedServiceResponse.setResponse("[{\"agent_id\":\"agent1\",\"skill\":\"Sales\"},{\"agent_id\":\"agent2\",\"skill\":\"Sales\"}]");

        when(ctiService.getAvailableAgentSkills(any(String.class), any(String.class)))
                .thenReturn(expectedServiceResponse);

        mockMvc.perform(post("/cti/getAvailableAgentSkills")
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().json(expectedServiceResponse.toString()));

        verify(ctiService, times(1)).getAvailableAgentSkills(any(String.class), any(String.class));
    }

    @Test
    @DisplayName("Test getAvailableAgentSkills - Service Exception")
    void getAvailableAgentSkills_ServiceException() throws Exception {
        String requestJson = "{\"campaignName\":\"NonExistentCampaign\",\"skill\":\"Sales\"}";
        RuntimeException serviceException = new RuntimeException("Campaign not found");

        when(ctiService.getAvailableAgentSkills(any(String.class), any(String.class)))
                .thenThrow(serviceException);

        OutputResponse expectedErrorResponse = new OutputResponse();
        expectedErrorResponse.setError(serviceException);

        mockMvc.perform(post("/cti/getAvailableAgentSkills")
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().json(expectedErrorResponse.toString()));

        verify(ctiService, times(1)).getAvailableAgentSkills(any(String.class), any(String.class));
    }

    @Test
    @DisplayName("Test getIVRSPathDetails - Success")
    void getIVRSPathDetails_Success() throws Exception {
        String requestJson = "{\"agent_id\":\"123\"}";
        OutputResponse expectedServiceResponse = new OutputResponse();
        expectedServiceResponse.setResponse("{\"path_details\":[{\"path\":\"Menu1\",\"options\":[\"Option1\",\"Option2\"]}]}");

        when(ctiService.getIVRSPathDetails(any(String.class), any(String.class)))
                .thenReturn(expectedServiceResponse);

        mockMvc.perform(post("/cti/getIVRSPathDetails")
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().json(expectedServiceResponse.toString()));

        verify(ctiService, times(1)).getIVRSPathDetails(any(String.class), any(String.class));
    }

    @Test
    @DisplayName("Test getIVRSPathDetails - Service Exception")
    void getIVRSPathDetails_ServiceException() throws Exception {
        String requestJson = "{\"agent_id\":\"123\"}";
        RuntimeException serviceException = new RuntimeException("IVRS service unavailable");

        when(ctiService.getIVRSPathDetails(any(String.class), any(String.class)))
                .thenThrow(serviceException);

        OutputResponse expectedErrorResponse = new OutputResponse();
        expectedErrorResponse.setError(serviceException);

        mockMvc.perform(post("/cti/getIVRSPathDetails")
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().json(expectedErrorResponse.toString()));

        verify(ctiService, times(1)).getIVRSPathDetails(any(String.class), any(String.class));
    }

    @Test
    @DisplayName("Test doAgentLogout - Service Exception")
    void doAgentLogout_ServiceException() throws Exception {
        String requestJson = "{\"agent_id\":\"agent123\"}";
        RuntimeException serviceException = new RuntimeException("Logout failed");

        when(ctiService.agentLogout(any(String.class), any(String.class)))
                .thenThrow(serviceException);

        OutputResponse expectedErrorResponse = new OutputResponse();
        expectedErrorResponse.setError(serviceException);

        mockMvc.perform(post("/cti/doAgentLogout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().json(expectedErrorResponse.toString()));

        verify(ctiService, times(1)).agentLogout(any(String.class), any(String.class));
    }

    @Test
    @DisplayName("Test transferCall - Service Exception")
    void transferCall_ServiceException() throws Exception {
        String requestJson = "{\"transfer_from\":\"agent1\", \"transfer_to\":\"agent2\"}";
        RuntimeException serviceException = new RuntimeException("Transfer failed");

        when(ctiService.transferCall(any(String.class), any(String.class)))
                .thenThrow(serviceException);

        OutputResponse expectedErrorResponse = new OutputResponse();
        expectedErrorResponse.setError(serviceException);

        mockMvc.perform(post("/cti/transferCall")
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().json(expectedErrorResponse.toString()));

        verify(ctiService, times(1)).transferCall(any(String.class), any(String.class));
    }

    @Test
    @DisplayName("Test customerPreferredLanguage - Service Exception")
    void customerPreferredLanguage_ServiceException() throws Exception {
        String requestJson = "{\"cust_ph_no\":\"invalid\",\"campaign_name\":\"TestCampaign\"}";
        RuntimeException serviceException = new RuntimeException("Invalid phone number");

        when(ctiService.customerPreferredLanguage(any(CustomerLanguage.class), any(String.class)))
                .thenThrow(serviceException);

        OutputResponse expectedErrorResponse = new OutputResponse();
        expectedErrorResponse.setError(serviceException);

        mockMvc.perform(post("/cti/customerPreferredLanguage")
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().json(expectedErrorResponse.toString()));

        verify(ctiService, times(1)).customerPreferredLanguage(any(CustomerLanguage.class), any(String.class));
    }

    @Test
    @DisplayName("Test getAgentState - Service Exception")
    void getAgentState_ServiceException() throws Exception {
        String requestJson = "{\"agent_id\":\"agent123\"}";
        RuntimeException serviceException = new RuntimeException("Agent not found");

        when(ctiService.getAgentState(any(String.class), any(String.class)))
                .thenThrow(serviceException);

        OutputResponse expectedErrorResponse = new OutputResponse();
        expectedErrorResponse.setError(serviceException);

        mockMvc.perform(post("/cti/getAgentState")
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().json(expectedErrorResponse.toString()));

        verify(ctiService, times(1)).getAgentState(any(String.class), any(String.class));
    }

    @Test
    @DisplayName("Test Request with X-FORWARDED-FOR header")
    void getCampaignSkills_WithXForwardedForHeader() throws Exception {
        String requestJson = "{\"campaign_name\":\"TestCampaign\"}";
        OutputResponse expectedServiceResponse = new OutputResponse();
        expectedServiceResponse.setResponse("[\"Skill1\", \"Skill2\"]");

        when(ctiService.getCampaignSkills(any(String.class), any(String.class)))
                .thenReturn(expectedServiceResponse);

        mockMvc.perform(post("/cti/getCampaignSkills")
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .header("X-FORWARDED-FOR", "192.168.1.100")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().json(expectedServiceResponse.toString()));

        verify(ctiService, times(1)).getCampaignSkills(any(String.class), any(String.class));
    }

    @Test
    @DisplayName("Test Request with empty X-FORWARDED-FOR header")
    void getCampaignSkills_WithEmptyXForwardedForHeader() throws Exception {
        String requestJson = "{\"campaign_name\":\"TestCampaign\"}";
        OutputResponse expectedServiceResponse = new OutputResponse();
        expectedServiceResponse.setResponse("[\"Skill1\", \"Skill2\"]");

        when(ctiService.getCampaignSkills(any(String.class), any(String.class)))
                .thenReturn(expectedServiceResponse);

        mockMvc.perform(post("/cti/getCampaignSkills")
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .header("X-FORWARDED-FOR", "")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().json(expectedServiceResponse.toString()));

        verify(ctiService, times(1)).getCampaignSkills(any(String.class), any(String.class));
    }
}
