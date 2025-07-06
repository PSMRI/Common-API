package com.iemr.common.controller.callhandling;

import static org.mockito.ArgumentMatchers.any;    
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import java.util.Arrays;
import java.util.List;


import com.iemr.common.data.callhandling.CallType;
import com.iemr.common.service.callhandling.CalltypeServiceImpl;
import com.iemr.common.service.callhandling.BeneficiaryCallService;
import com.iemr.common.utils.response.OutputResponse;

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

@ExtendWith(MockitoExtension.class)
@DisplayName("CallController Tests")
class CallControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CalltypeServiceImpl calltypeServiceImpl;

    @Mock
    private BeneficiaryCallService beneficiaryCallService;

    @Mock
    private com.iemr.common.utils.sessionobject.SessionObject s;

    @InjectMocks
    private CallController callController;

    // Test constants
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_TOKEN = "Bearer mock_token";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(callController).build();
    }
    // --- Additional edge and error case tests for full coverage ---

    
    @Test
    @DisplayName("getAllCallTypes - Null Response")
    void getAllCallTypes_NullResponse() throws Exception {
        String requestJson = "{\"providerServiceMapID\":1}";
        when(calltypeServiceImpl.getAllCalltypes(any(String.class))).thenReturn(null);
        mockMvc.perform(post("/call/getCallTypes")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer mock_token")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("status")));
    }

    @Test
    @DisplayName("getCallTypesV1 - Null Response")
    void getCallTypesV1_NullResponse() throws Exception {
        String requestJson = "{\"providerServiceMapID\":1}";
        when(calltypeServiceImpl.getAllCalltypesV1(any(String.class))).thenReturn(null);
        mockMvc.perform(post("/call/getCallTypesV1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer mock_token")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("status")));
    }

    @Test
    @DisplayName("startCall - Null Remote Address")
    void startCall_NullRemoteAddress() throws Exception {
        String requestJson = "{\"calledServiceID\":1}";
        com.iemr.common.data.callhandling.BeneficiaryCall call = new com.iemr.common.data.callhandling.BeneficiaryCall();
        when(beneficiaryCallService.createCall(any(String.class), any(String.class))).thenReturn(call);
        mockMvc.perform(post("/call/startCall")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer mock_token")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("status")));
    }

@Test
    @DisplayName("updateBeneficiaryIDInCall - Null Response")
    void updateBeneficiaryIDInCall_NullReturn() throws Exception {
        String requestJson = "{\"benCallID\":1,\"isCalledEarlier\":true,\"beneficiaryRegID\":2}";
        when(beneficiaryCallService.updateBeneficiaryIDInCall(any(String.class))).thenReturn(null);
        mockMvc.perform(post("/call/updatebeneficiaryincall")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer mock_token")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("benCallID")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("beneficiaryRegID")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("isCalledEarlier")));
    }

    @Test
    @DisplayName("closeCall - Null Remote Address")
    void closeCall_NullRemoteAddress() throws Exception {
        String requestJson = "{\"benCallID\":1}";
        when(beneficiaryCallService.closeCall(any(String.class), any(String.class))).thenReturn(1);
        mockMvc.perform(post("/call/closeCall")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer mock_token")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("updateCount")));
    }

    @Test
    @DisplayName("outboundCallList - Null Return")
    void outboundCallList_NullReturn() throws Exception {
        String requestJson = "{\"providerServiceMapID\":1}";
        when(beneficiaryCallService.outboundCallList(any(String.class), any(String.class))).thenReturn(null);
        mockMvc.perform(post("/call/outboundCallList")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer mock_token")
                .content(requestJson))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("unblockBlockedNumbers - Null Return")
    void unblockBlockedNumbers_NullReturn() throws Exception {
        when(beneficiaryCallService.unblockBlockedNumbers()).thenReturn(null);
        mockMvc.perform(get("/call/unblockBlockedNumbers")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer mock_token"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("outboundCallCount - Null Return")
    void outboundCallCount_NullReturn() throws Exception {
        String requestJson = "{\"providerServiceMapID\":1}";
        when(beneficiaryCallService.outboundCallCount(any(String.class))).thenReturn(null);
        mockMvc.perform(post("/call/outboundCallCount")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer mock_token")
                .content(requestJson))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("filterCallList - Null Return")
    void filterCallList_NullReturn() throws Exception {
        String requestJson = "{\"calledServiceID\":1}";
        when(beneficiaryCallService.filterCallList(any(String.class), any(String.class))).thenReturn(null);
        mockMvc.perform(post("/call/filterCallList")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer mock_token")
                .content(requestJson))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("filterCallListPage - Null Return")
    void filterCallListPage_NullReturn() throws Exception {
        String requestJson = "{\"calledServiceID\":1,\"pageNo\":1,\"pageSize\":10}";
        when(beneficiaryCallService.filterCallListWithPagination(any(String.class), any(String.class))).thenReturn(null);
        mockMvc.perform(post("/call/filterCallListPage")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer mock_token")
                .content(requestJson))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("outboundAllocation - Null Return")
    void outboundAllocation_NullReturn() throws Exception {
        String requestJson = "{\"userID\":[1,2],\"allocateNo\":5}";
        when(beneficiaryCallService.outboundAllocation(any(String.class))).thenReturn(null);
        mockMvc.perform(post("/call/outboundAllocation")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer mock_token")
                .content(requestJson))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("completeOutboundCall - Null Return")
    void completeOutboundCall_NullReturn() throws Exception {
        String requestJson = "{\"outboundCallReqID\":1,\"isCompleted\":true}";
        when(beneficiaryCallService.completeOutboundCall(any(String.class))).thenReturn(null);
        mockMvc.perform(post("/call/completeOutboundCall")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer mock_token")
                .content(requestJson))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("updateOutboundCall - Null Return")
    void updateOutboundCall_NullReturn() throws Exception {
        String requestJson = "{\"outboundCallReqID\":1,\"isCompleted\":true,\"callTypeID\":2}";
        when(beneficiaryCallService.updateOutboundCall(any(String.class))).thenReturn(null);
        mockMvc.perform(post("/call/updateOutboundCall")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer mock_token")
                .content(requestJson))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("resetOutboundCall - Null Return")
    void resetOutboundCall_NullReturn() throws Exception {
        String requestJson = "{\"outboundCallReqIDs\":[1,2,3]}";
        when(beneficiaryCallService.resetOutboundCall(any(String.class))).thenReturn(null);
        mockMvc.perform(post("/call/resetOutboundCall")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer mock_token")
                .content(requestJson))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("getBlacklistNumbers - Null Return")
    void getBlacklistNumbers_NullReturn() throws Exception {
        String requestJson = "{\"providerServiceMapID\":1}";
        when(beneficiaryCallService.getBlacklistNumbers(any(String.class))).thenReturn(null);
        mockMvc.perform(post("/call/getBlacklistNumbers")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer mock_token")
                .content(requestJson))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("blockPhoneNumber - Empty Response")
    void blockPhoneNumber_EmptyResponse() throws Exception {
        String requestJson = "{\"phoneBlockID\":1}";
        OutputResponse emptyResponse = new OutputResponse();
        when(beneficiaryCallService.blockPhoneNumber(any(String.class))).thenReturn(emptyResponse);
        mockMvc.perform(post("/call/blockPhoneNumber")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer mock_token")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("status")));
    }

    @Test
    @DisplayName("unblockPhoneNumber - Empty Response")
    void unblockPhoneNumber_EmptyResponse() throws Exception {
        String requestJson = "{\"phoneBlockID\":1}";
        OutputResponse emptyResponse = new OutputResponse();
        when(beneficiaryCallService.unblockPhoneNumber(any(String.class))).thenReturn(emptyResponse);
        mockMvc.perform(post("/call/unblockPhoneNumber")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer mock_token")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("status")));
    }

    @Test
    @DisplayName("updateBeneficiaryCallCDIStatus - Null Return")
    void updateBeneficiaryCallCDIStatus_NullReturn() throws Exception {
        String requestJson = "{\"benCallID\":1,\"cDICallStatus\":\"done\"}";
        when(beneficiaryCallService.updateBeneficiaryCallCDIStatus(any(String.class))).thenReturn(null);
        mockMvc.perform(post("/call/updateBeneficiaryCallCDIStatus")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer mock_token")
                .content(requestJson))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("getCallHistoryByCallID - Null Return")
    void getCallHistoryByCallID_NullReturn() throws Exception {
        String requestJson = "{\"callID\":\"abc\"}";
        when(beneficiaryCallService.getCallHistoryByCallID(any(String.class))).thenReturn(null);
        mockMvc.perform(post("/call/getCallHistoryByCallID")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer mock_token")
                .content(requestJson))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("outboundCallListByCallID - Null Return")
    void outboundCallListByCallID_NullReturn() throws Exception {
        String requestJson = "{\"providerServiceMapID\":1,\"callID\":\"abc\"}";
        when(beneficiaryCallService.outboundCallListByCallID(any(String.class))).thenReturn(null);
        mockMvc.perform(post("/call/outboundCallListByCallID")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer mock_token")
                .content(requestJson))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("nueisanceCallHistory - Null Return")
    void nueisanceCallHistory_NullReturn() throws Exception {
        String requestJson = "{\"calledServiceID\":1,\"phoneNo\":\"123\",\"count\":2}";
        when(beneficiaryCallService.nueisanceCallHistory(any(String.class), any(String.class))).thenReturn(null);
        mockMvc.perform(post("/call/nueisanceCallHistory")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer mock_token")
                .content(requestJson))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("getAllCallTypes - Success")
    void getAllCallTypes_Success() throws Exception {
        String requestJson = "{\"providerServiceMapID\":1}";
        List<CallType> callTypes = Arrays.asList(new CallType(), new CallType());
        OutputResponse expectedResponse = new OutputResponse();
        expectedResponse.setResponse(callTypes.toString());

        when(calltypeServiceImpl.getAllCalltypes(any(String.class))).thenReturn(callTypes);

        mockMvc.perform(post("/call/getCallTypes")
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResponse.toString()));
    }

    @Test
    @DisplayName("getAllCallTypes - Service Exception")
    void getAllCallTypes_ServiceException() throws Exception {
        String requestJson = "{\"providerServiceMapID\":1}";
        RuntimeException ex = new RuntimeException("Service failure");
        OutputResponse expectedError = new OutputResponse();
        expectedError.setError(ex);
        when(calltypeServiceImpl.getAllCalltypes(any(String.class))).thenThrow(ex);

        mockMvc.perform(post("/call/getCallTypes")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer mock_token")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedError.toString()));
    }

    @Test
    @DisplayName("getCallTypesV1 - Success")
    void getCallTypesV1_Success() throws Exception {
        String requestJson = "{\"providerServiceMapID\":1}";
        String callTypesJson = "[\"A\",\"B\"]";
        OutputResponse expectedResponse = new OutputResponse();
        expectedResponse.setResponse(callTypesJson);
        when(calltypeServiceImpl.getAllCalltypesV1(any(String.class))).thenReturn(callTypesJson);

        mockMvc.perform(post("/call/getCallTypesV1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer mock_token")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResponse.toString()));
    }

    @Test
    @DisplayName("getCallTypesV1 - Service Exception")
    void getCallTypesV1_ServiceException() throws Exception {
        String requestJson = "{\"providerServiceMapID\":1}";
        RuntimeException ex = new RuntimeException("Service failure");
        OutputResponse expectedError = new OutputResponse();
        expectedError.setError(ex);
        when(calltypeServiceImpl.getAllCalltypesV1(any(String.class))).thenThrow(ex);

        mockMvc.perform(post("/call/getCallTypesV1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer mock_token")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedError.toString()));
    }

    @Test
    @DisplayName("startCall - Success")
    void startCall_Success() throws Exception {
        String requestJson = "{\"calledServiceID\":1}";
        com.iemr.common.data.callhandling.BeneficiaryCall call = new com.iemr.common.data.callhandling.BeneficiaryCall();
        OutputResponse expectedResponse = new OutputResponse();
        expectedResponse.setResponse(call.toString());
        when(beneficiaryCallService.createCall(any(String.class), any(String.class))).thenReturn(call);

        mockMvc.perform(post("/call/startCall")
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResponse.toString()));
    }

    @Test
    @DisplayName("startCall - Service Exception")
    void startCall_ServiceException() throws Exception {
        String requestJson = "{\"calledServiceID\":1}";
        RuntimeException ex = new RuntimeException("Service failure");
        OutputResponse expectedError = new OutputResponse();
        expectedError.setError(ex);
        when(beneficiaryCallService.createCall(any(String.class), any(String.class))).thenThrow(ex);

        mockMvc.perform(post("/call/startCall")
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedError.toString()));
    }

    @Test
    @DisplayName("updateBeneficiaryIDInCall - Success")
    void updateBeneficiaryIDInCall_Success() throws Exception {
        String requestJson = "{\"benCallID\":1,\"isCalledEarlier\":true,\"beneficiaryRegID\":2}";
        when(beneficiaryCallService.updateBeneficiaryIDInCall(any(String.class))).thenReturn(1);
        // The controller puts this value into the response JSON
        String expectedSubstring = "\"updatedCount\":1";

        mockMvc.perform(post("/call/updatebeneficiaryincall")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer mock_token")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(expectedSubstring)));
    }

    @Test
    @DisplayName("updateBeneficiaryIDInCall - JSONException")
    void updateBeneficiaryIDInCall_JSONException() throws Exception {
        String invalidJson = "not a json";
        mockMvc.perform(post("/call/updatebeneficiaryincall")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer mock_token")
                .content(invalidJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("error")));
    }

    @Test
    @DisplayName("updateBeneficiaryIDInCall - Service Exception")
    void updateBeneficiaryIDInCall_ServiceException() throws Exception {
        String requestJson = "{\"benCallID\":1,\"isCalledEarlier\":true,\"beneficiaryRegID\":2}";
        RuntimeException ex = new RuntimeException("Service failure");
        when(beneficiaryCallService.updateBeneficiaryIDInCall(any(String.class))).thenThrow(ex);
        mockMvc.perform(post("/call/updatebeneficiaryincall")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer mock_token")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("error")));
    }

    @Test
    @DisplayName("closeCall - Success")
    void closeCall_Success() throws Exception {
        String requestJson = "{\"benCallID\":1}";
        int updateCount = 1;
        // The controller puts this value into the response JSON
        String expectedSubstring = "\"updateCount\":" + updateCount;
        when(beneficiaryCallService.closeCall(any(String.class), any(String.class))).thenReturn(updateCount);

        mockMvc.perform(post("/call/closeCall")
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(expectedSubstring)));
    }

    @Test
    @DisplayName("outboundCallList - Success")
    void outboundCallList_Success() throws Exception {
        String requestJson = "{\"providerServiceMapID\":1}";
        String expectedResponse = "[outbound call list]";
        when(beneficiaryCallService.outboundCallList(any(String.class), any(String.class))).thenReturn(expectedResponse);

        mockMvc.perform(post("/call/outboundCallList")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer mock_token")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(expectedResponse)));
    }

    @Test
    @DisplayName("unblockBlockedNumbers - Success")
    void unblockBlockedNumbers_Success() throws Exception {
        String expectedResponse = "[unblocked numbers]";
        when(beneficiaryCallService.unblockBlockedNumbers()).thenReturn(expectedResponse);

        mockMvc.perform(get("/call/unblockBlockedNumbers")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer mock_token"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(expectedResponse)));
    }

    @Test
    @DisplayName("outboundCallCount - Success")
    void outboundCallCount_Success() throws Exception {
        String requestJson = "{\"providerServiceMapID\":1}";
        String expectedResponse = "5";
        when(beneficiaryCallService.outboundCallCount(any(String.class))).thenReturn(expectedResponse);
        mockMvc.perform(post("/call/outboundCallCount")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer mock_token")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(expectedResponse)));
    }

    @Test
    @DisplayName("filterCallList - Success")
    void filterCallList_Success() throws Exception {
        String requestJson = "{\"calledServiceID\":1}";
        String expectedResponse = "[filtered call list]";
        when(beneficiaryCallService.filterCallList(any(String.class), any(String.class))).thenReturn(expectedResponse);
        mockMvc.perform(post("/call/filterCallList")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer mock_token")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(expectedResponse)));
    }

    @Test
    @DisplayName("filterCallListPage - Success")
    void filterCallListPage_Success() throws Exception {
        String requestJson = "{\"calledServiceID\":1,\"pageNo\":1,\"pageSize\":10}";
        String expectedResponse = "[filtered call list page]";
        when(beneficiaryCallService.filterCallListWithPagination(any(String.class), any(String.class))).thenReturn(expectedResponse);
        mockMvc.perform(post("/call/filterCallListPage")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer mock_token")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(expectedResponse)));
    }

    @Test
    @DisplayName("outboundAllocation - Success")
    void outboundAllocation_Success() throws Exception {
        String requestJson = "{\"userID\":[1,2],\"allocateNo\":5}";
        String expectedResponse = "[outbound allocation]";
        when(beneficiaryCallService.outboundAllocation(any(String.class))).thenReturn(expectedResponse);
        mockMvc.perform(post("/call/outboundAllocation")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer mock_token")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(expectedResponse)));
    }

    @Test
    @DisplayName("completeOutboundCall - Success")
    void completeOutboundCall_Success() throws Exception {
        String requestJson = "{\"outboundCallReqID\":1,\"isCompleted\":true}";
        String expectedResponse = "[complete outbound call]";
        when(beneficiaryCallService.completeOutboundCall(any(String.class))).thenReturn(expectedResponse);
        mockMvc.perform(post("/call/completeOutboundCall")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer mock_token")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(expectedResponse)));
    }

    @Test
    @DisplayName("updateOutboundCall - Success")
    void updateOutboundCall_Success() throws Exception {
        String requestJson = "{\"outboundCallReqID\":1,\"isCompleted\":true,\"callTypeID\":2}";
        String expectedResponse = "[update outbound call]";
        when(beneficiaryCallService.updateOutboundCall(any(String.class))).thenReturn(expectedResponse);
        mockMvc.perform(post("/call/updateOutboundCall")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer mock_token")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(expectedResponse)));
    }

    @Test
    @DisplayName("resetOutboundCall - Success")
    void resetOutboundCall_Success() throws Exception {
        String requestJson = "{\"outboundCallReqIDs\":[1,2,3]}";
        String expectedResponse = "[reset outbound call]";
        when(beneficiaryCallService.resetOutboundCall(any(String.class))).thenReturn(expectedResponse);
        mockMvc.perform(post("/call/resetOutboundCall")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer mock_token")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(expectedResponse)));
    }

    @Test
    @DisplayName("getBlacklistNumbers - Success")
    void getBlacklistNumbers_Success() throws Exception {
        String requestJson = "{\"providerServiceMapID\":1}";
        String expectedResponse = "[blacklist numbers]";
        when(beneficiaryCallService.getBlacklistNumbers(any(String.class))).thenReturn(expectedResponse);
        mockMvc.perform(post("/call/getBlacklistNumbers")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer mock_token")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(expectedResponse)));
    }

    @Test
    @DisplayName("blockPhoneNumber - Success")
    void blockPhoneNumber_Success() throws Exception {
        String requestJson = "{\"phoneBlockID\":1}";
        OutputResponse expectedResponse = new OutputResponse();
        expectedResponse.setResponse("blocked");
        when(beneficiaryCallService.blockPhoneNumber(any(String.class))).thenReturn(expectedResponse);
        mockMvc.perform(post("/call/blockPhoneNumber")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer mock_token")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("blocked")));
    }

    @Test
    @DisplayName("unblockPhoneNumber - Success")
    void unblockPhoneNumber_Success() throws Exception {
        String requestJson = "{\"phoneBlockID\":1}";
        OutputResponse expectedResponse = new OutputResponse();
        expectedResponse.setResponse("unblocked");
        when(beneficiaryCallService.unblockPhoneNumber(any(String.class))).thenReturn(expectedResponse);
        mockMvc.perform(post("/call/unblockPhoneNumber")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer mock_token")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("unblocked")));
    }

    @Test
    @DisplayName("updateBeneficiaryCallCDIStatus - Success")
    void updateBeneficiaryCallCDIStatus_Success() throws Exception {
        String requestJson = "{\"benCallID\":1,\"cDICallStatus\":\"done\"}";
        int updatedCount = 1;
        String expectedSubstring = "\"updatedCount\":" + updatedCount;
        when(beneficiaryCallService.updateBeneficiaryCallCDIStatus(any(String.class))).thenReturn(updatedCount);
        mockMvc.perform(post("/call/updateBeneficiaryCallCDIStatus")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer mock_token")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(expectedSubstring)));
    }

    @Test
    @DisplayName("getCallHistoryByCallID - Success")
    void getCallHistoryByCallID_Success() throws Exception {
        String requestJson = "{\"callID\":\"abc\"}";
        List<com.iemr.common.data.callhandling.BeneficiaryCall> callHistory = Arrays.asList(new com.iemr.common.data.callhandling.BeneficiaryCall());
        when(beneficiaryCallService.getCallHistoryByCallID(any(String.class))).thenReturn(callHistory);
        mockMvc.perform(post("/call/getCallHistoryByCallID")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer mock_token")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"status\":")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"data\":")));
    }

    @Test
    @DisplayName("outboundCallListByCallID - Success")
    void outboundCallListByCallID_Success() throws Exception {
        String requestJson = "{\"providerServiceMapID\":1,\"callID\":\"abc\"}";
        String expectedResponse = "[outbound call list by call id]";
        when(beneficiaryCallService.outboundCallListByCallID(any(String.class))).thenReturn(expectedResponse);
        mockMvc.perform(post("/call/outboundCallListByCallID")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer mock_token")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(expectedResponse)));
    }

    @Test
    @DisplayName("nueisanceCallHistory - Success")
    void nueisanceCallHistory_Success() throws Exception {
        String requestJson = "{\"calledServiceID\":1,\"phoneNo\":\"123\",\"count\":2}";
        String expectedResponse = "[nuisance call history]";
        when(beneficiaryCallService.nueisanceCallHistory(any(String.class), any(String.class))).thenReturn(expectedResponse);
        mockMvc.perform(post("/call/nueisanceCallHistory")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer mock_token")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(expectedResponse)));
    }

    @Test
    @DisplayName("beneficiaryByCallID - Success")
    void beneficiaryByCallID_Success() throws Exception {
        com.iemr.common.model.beneficiary.CallRequestByIDModel requestModel = new com.iemr.common.model.beneficiary.CallRequestByIDModel();
        com.iemr.common.model.beneficiary.BeneficiaryCallModel callModel = new com.iemr.common.model.beneficiary.BeneficiaryCallModel();
        String jsonString = "{\"callID\":\"abc\"}";
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        String callModelJson = mapper.writeValueAsString(callModel);
        OutputResponse expectedResponse = new OutputResponse();
        expectedResponse.setResponse(callModelJson);
        org.mockito.Mockito.when(beneficiaryCallService.beneficiaryByCallID(any(com.iemr.common.model.beneficiary.CallRequestByIDModel.class), any(String.class))).thenReturn(callModel);
        mockMvc.perform(post("/call/beneficiaryByCallID")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer mock_token")
                .content(jsonString))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("status")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("data")));
    }

    @Test
    @DisplayName("isAvailed - Success")
    void isAvailed_Success() throws Exception {
        String requestJson = "{\"beneficiaryRegID\":1,\"receivedRoleName\":\"role\"}";
        com.iemr.common.model.beneficiary.BeneficiaryCallModel model = new com.iemr.common.model.beneficiary.BeneficiaryCallModel();
        org.mockito.Mockito.when(beneficiaryCallService.isAvailed(any(com.iemr.common.model.beneficiary.BeneficiaryCallModel.class))).thenReturn(true);
        mockMvc.perform(post("/call/isAvailed")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer mock_token")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("status")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("data")));
    }

    @Test
    @DisplayName("getBenRequestedOutboundCall - Success")
    void getBenRequestedOutboundCall_Success() throws Exception {
        String requestJson = "{\"beneficiaryRegID\":1,\"calledServiceID\":2,\"is1097\":true}";
        com.iemr.common.model.beneficiary.BeneficiaryCallModel model = new com.iemr.common.model.beneficiary.BeneficiaryCallModel();
        java.util.List<com.iemr.common.data.callhandling.OutboundCallRequest> outboundCallRequests = java.util.Arrays.asList(org.mockito.Mockito.mock(com.iemr.common.data.callhandling.OutboundCallRequest.class));
        org.mockito.Mockito.when(beneficiaryCallService.getBenRequestedOutboundCall(any(com.iemr.common.model.beneficiary.BeneficiaryCallModel.class))).thenReturn(outboundCallRequests);
        mockMvc.perform(post("/call/getBenRequestedOutboundCall")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer mock_token")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("status")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("data")));
    }

    @Test
    @DisplayName("isAutoPreviewDialing - Success")
    void isAutoPreviewDialing_Success() throws Exception {
        String requestJson = "{\"providerServiceMapID\":1,\"isDialPreferenceManual\":true}";
        com.iemr.common.data.users.ProviderServiceMapping mapping = new com.iemr.common.data.users.ProviderServiceMapping();
        org.mockito.Mockito.when(beneficiaryCallService.isAutoPreviewDialing(any(com.iemr.common.data.users.ProviderServiceMapping.class))).thenReturn("true");
        mockMvc.perform(post("/call/isAutoPreviewDialing")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer mock_token")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("status")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("data")));
    }

    @Test
    @DisplayName("checkAutoPreviewDialing - Success")
    void checkAutoPreviewDialing_Success() throws Exception {
        String requestJson = "{\"providerServiceMapID\":1}";
        org.mockito.Mockito.when(beneficiaryCallService.checkAutoPreviewDialing(any(com.iemr.common.data.users.ProviderServiceMapping.class))).thenReturn("checked");
        mockMvc.perform(post("/call/checkAutoPreviewDialing")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer mock_token")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("status")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("data")));
    }

    @Test
    @DisplayName("getFilePathCTI - Success")
    void getFilePathCTI_Success() throws Exception {
        String requestJson = "{\"agentID\":\"a\",\"callID\":\"b\"}";
        org.mockito.Mockito.when(beneficiaryCallService.cTIFilePathNew(any(String.class))).thenReturn("/path/to/file");
        mockMvc.perform(post("/call/getFilePathCTI")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer mock_token")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("status")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("data")));
    }

    @Test
    @DisplayName("redisInsert - Success")
    void redisInsert_Success() throws Exception {
        String requestJson = "{\"key\":\"k\",\"value\":\"v\"}";
        org.mockito.Mockito.when(s.setSessionObject(any(String.class), any(String.class))).thenReturn("inserted");
        mockMvc.perform(post("/call/redisInsert")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer mock_token")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("status")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("data")));
    }

    @Test
    @DisplayName("redisFetch - Success")
    void redisFetch_Success() throws Exception {
        String requestJson = "{\"sessionID\":\"abc\"}";
        org.mockito.Mockito.when(s.getSessionObject(any(String.class))).thenReturn("fetched");
        mockMvc.perform(post("/call/redisFetch")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer mock_token")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("status")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("data")));
    }

    @Test
    @DisplayName("beneficiaryByCallID - Service Exception")
    void beneficiaryByCallID_ServiceException() throws Exception {
        String jsonString = "{\"callID\":\"abc\"}";
        RuntimeException ex = new RuntimeException("Service failure");
        org.mockito.Mockito.when(beneficiaryCallService.beneficiaryByCallID(any(com.iemr.common.model.beneficiary.CallRequestByIDModel.class), any(String.class))).thenThrow(ex);
        mockMvc.perform(post("/call/beneficiaryByCallID")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer mock_token")
                .content(jsonString))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("error")));
    }

    @Test
    @DisplayName("isAvailed - Service Exception")
    void isAvailed_ServiceException() throws Exception {
        String requestJson = "{\"beneficiaryRegID\":1,\"receivedRoleName\":\"role\"}";
        RuntimeException ex = new RuntimeException("Service failure");
        org.mockito.Mockito.when(beneficiaryCallService.isAvailed(any(com.iemr.common.model.beneficiary.BeneficiaryCallModel.class))).thenThrow(ex);
        mockMvc.perform(post("/call/isAvailed")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer mock_token")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("error")));
    }

    @Test
    @DisplayName("getBenRequestedOutboundCall - Service Exception")
    void getBenRequestedOutboundCall_ServiceException() throws Exception {
        String requestJson = "{\"beneficiaryRegID\":1,\"calledServiceID\":2,\"is1097\":true}";
        RuntimeException ex = new RuntimeException("Service failure");
        org.mockito.Mockito.when(beneficiaryCallService.getBenRequestedOutboundCall(any(com.iemr.common.model.beneficiary.BeneficiaryCallModel.class))).thenThrow(ex);
        mockMvc.perform(post("/call/getBenRequestedOutboundCall")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer mock_token")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("error")));
    }

    @Test
    @DisplayName("isAutoPreviewDialing - Service Exception")
    void isAutoPreviewDialing_ServiceException() throws Exception {
        String requestJson = "{\"providerServiceMapID\":1,\"isDialPreferenceManual\":true}";
        RuntimeException ex = new RuntimeException("Service failure");
        org.mockito.Mockito.when(beneficiaryCallService.isAutoPreviewDialing(any(com.iemr.common.data.users.ProviderServiceMapping.class))).thenThrow(ex);
        mockMvc.perform(post("/call/isAutoPreviewDialing")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer mock_token")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("error")));
    }

    @Test
    @DisplayName("checkAutoPreviewDialing - Service Exception")
    void checkAutoPreviewDialing_ServiceException() throws Exception {
        String requestJson = "{\"providerServiceMapID\":1}";
        RuntimeException ex = new RuntimeException("Service failure");
        org.mockito.Mockito.when(beneficiaryCallService.checkAutoPreviewDialing(any(com.iemr.common.data.users.ProviderServiceMapping.class))).thenThrow(ex);
        mockMvc.perform(post("/call/checkAutoPreviewDialing")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer mock_token")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("error")));
    }

    @Test
    @DisplayName("getFilePathCTI - Service Exception")
    void getFilePathCTI_ServiceException() throws Exception {
        String requestJson = "{\"agentID\":\"a\",\"callID\":\"b\"}";
        RuntimeException ex = new RuntimeException("Service failure");
        org.mockito.Mockito.when(beneficiaryCallService.cTIFilePathNew(any(String.class))).thenThrow(ex);
        mockMvc.perform(post("/call/getFilePathCTI")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer mock_token")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("error")));
    }

    @Test
    @DisplayName("redisInsert - Service Exception")
    void redisInsert_ServiceException() throws Exception {
        String requestJson = "{\"key\":\"k\",\"value\":\"v\"}";
        RuntimeException ex = new RuntimeException("Service failure");
        org.mockito.Mockito.when(s.setSessionObject(any(String.class), any(String.class))).thenThrow(ex);
        mockMvc.perform(post("/call/redisInsert")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer mock_token")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("error")));
    }

    @Test
    @DisplayName("redisFetch - Service Exception")
    void redisFetch_ServiceException() throws Exception {
        String requestJson = "{\"sessionID\":\"abc\"}";
        RuntimeException ex = new RuntimeException("Service failure");
        org.mockito.Mockito.when(s.getSessionObject(any(String.class))).thenThrow(ex);
        mockMvc.perform(post("/call/redisFetch")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer mock_token")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("error")));
    }
}
