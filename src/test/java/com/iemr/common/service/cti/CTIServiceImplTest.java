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
package com.iemr.common.service.cti;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.Logger;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.iemr.common.data.cti.AgentCallStats;
import com.iemr.common.data.cti.AgentLoginKey;
import com.iemr.common.data.cti.AgentSkills;
import com.iemr.common.data.cti.AgentState;
import com.iemr.common.data.cti.AutoPreviewDial;
import com.iemr.common.data.cti.BlockUnblockNumber;
import com.iemr.common.data.cti.CTICampaigns;
import com.iemr.common.data.cti.CTIResponse;
import com.iemr.common.data.cti.CTIResponseTemp;
import com.iemr.common.data.cti.CTIUser;
import com.iemr.common.data.cti.CTIVoiceFile;
import com.iemr.common.data.cti.CallBeneficiary;
import com.iemr.common.data.cti.CallDisposition;
import com.iemr.common.data.cti.CampaignNames;
import com.iemr.common.data.cti.CampaignRole;
import com.iemr.common.data.cti.CampaignSkills;
import com.iemr.common.data.cti.CustomerLanguage;
import com.iemr.common.data.cti.TransferCall;
import com.iemr.common.data.callhandling.CallType;
import com.iemr.common.data.callhandling.BeneficiaryCall;
import com.iemr.common.repository.callhandling.BeneficiaryCallRepository;
import com.iemr.common.repository.callhandling.IEMRCalltypeRepositoryImplCustom;
import com.iemr.common.utils.config.ConfigProperties;
import com.iemr.common.utils.http.HttpUtils;
import com.iemr.common.utils.mapper.InputMapper;
import com.iemr.common.utils.response.OutputResponse;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class CTIServiceImplTest {

    @InjectMocks
    private CTIServiceImpl ctiServiceImpl;

    @Mock
    private BeneficiaryCallRepository beneficiaryCallRepository;

    @Mock
    private CTIService ctiService;

    @Mock
    private IEMRCalltypeRepositoryImplCustom iemrCalltypeRepositoryImplCustom;

    @Mock
    private HttpUtils httpUtils;

    @Mock
    private Logger logger;

    private final String TEST_IP = "192.168.1.1";
    private final String TEST_AGENT_ID = "agent123";
    private final String TEST_SERVER_URL = "http://cti-server.com";

    @BeforeEach
    void setUp() {
        // Set up HttpUtils static mock
        ReflectionTestUtils.setField(ctiServiceImpl, "httpUtils", httpUtils);
    }

    private ObjectMapper getConfiguredObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper;
    }

    @Test
    void testConstructor() {
        CTIServiceImpl service = new CTIServiceImpl();
        assertNotNull(service);
    }

    @Test
    void testCallUrl() {
        String expectedResponse = "test response";
        when(httpUtils.get(anyString())).thenReturn(expectedResponse);

        String result = ctiServiceImpl.callUrl("http://test.com");

        assertEquals(expectedResponse, result);
        verify(httpUtils).get("http://test.com");
    }

    @Test
    void testCallPostUrl() {
        String expectedResponse = "post response";
        when(httpUtils.post(anyString(), anyString())).thenReturn(expectedResponse);

        String result = ctiServiceImpl.callPostUrl("http://test.com", "json data");

        assertEquals(expectedResponse, result);
        verify(httpUtils).post("http://test.com", "json data");
    }

    // ===============================
    // BATCH 1: First 5 Methods
    // ===============================

    @Test
    void testAddUpdateUserData_Success() throws Exception {
        try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
            configMock.when(() -> ConfigProperties.getPropertyByName("add-update-user-data"))
                    .thenReturn("http://CTI_SERVER/api/user?username=USERNAME&password=PASSWORD&firstname=FIRSTNAME&lastname=LASTNAME&phone=PHONE_NO&email=EMAIL&role=ROLE&timeout=SESSION_TIMEOUT&designation=DESIGNATION&ip=AGENT_IP");
            configMock.when(() -> ConfigProperties.getPropertyByName("cti-server-ip"))
                    .thenReturn(TEST_SERVER_URL);

            CTIUser ctiUser = new CTIUser();
            ctiUser.setUsername("testuser");
            ctiUser.setPassword("testpass");
            ctiUser.setFirstname("Test");
            ctiUser.setLastname("User");
            ctiUser.setPhone("9876543210");
            ctiUser.setEmail("test@example.com");
            ctiUser.setRole("AGENT");
            ctiUser.setDesignation("Agent");
            // Prevent Jackson from serializing logger
            ReflectionTestUtils.setField(ctiUser, "logger", null);

            String requestJson = getConfiguredObjectMapper().writeValueAsString(ctiUser);

            CTIResponse ctiResponse = new CTIResponse();
            ctiResponse.setResponse_code("1");
            ctiResponse.setStatus("SUCCESS");

            CTIUser responseUser = new CTIUser();
            responseUser.setResponse(ctiResponse);

            String responseJson = getConfiguredObjectMapper().writeValueAsString(responseUser);

            try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class)) {
                InputMapper mockInputMapper = mock(InputMapper.class);
                inputMapperMock.when(() -> InputMapper.gson()).thenReturn(mockInputMapper);
                when(mockInputMapper.fromJson(responseJson, CTIUser.class)).thenReturn(responseUser);

                when(httpUtils.get(anyString())).thenReturn(responseJson);

                OutputResponse result = ctiServiceImpl.addUpdateUserData(requestJson, TEST_IP);

                assertNotNull(result);
                assertNotNull(result.getData());
            }
        }
    }

    @Test
    void testAddUpdateUserData_WithNullValues() throws Exception {
        try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
            configMock.when(() -> ConfigProperties.getPropertyByName("add-update-user-data"))
                    .thenReturn("http://CTI_SERVER/api/user?username=USERNAME&password=PASSWORD&firstname=FIRSTNAME&lastname=LASTNAME&phone=PHONE_NO&email=EMAIL&role=ROLE&timeout=SESSION_TIMEOUT&designation=DESIGNATION&ip=AGENT_IP");
            configMock.when(() -> ConfigProperties.getPropertyByName("cti-server-ip"))
                    .thenReturn(TEST_SERVER_URL);

            CTIUser ctiUser = new CTIUser();
            // All null values to test null handling
            ctiUser.setUsername(null);
            ctiUser.setPassword(null);
            ctiUser.setFirstname(null);
            ctiUser.setLastname(null);
            ctiUser.setPhone(null);
            ctiUser.setEmail(null);
            ctiUser.setRole(null);
            ctiUser.setDesignation(null);
            ReflectionTestUtils.setField(ctiUser, "logger", null);

            String requestJson = getConfiguredObjectMapper().writeValueAsString(ctiUser);

            CTIResponse ctiResponse = new CTIResponse();
            ctiResponse.setResponse_code("0");
            ctiResponse.setStatus("FAILURE");
            ctiResponse.setReason("Invalid user data");

            CTIUser responseUser = new CTIUser();
            responseUser.setResponse(ctiResponse);

            String responseJson = getConfiguredObjectMapper().writeValueAsString(responseUser);

            try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class)) {
                InputMapper mockInputMapper = mock(InputMapper.class);
                inputMapperMock.when(() -> InputMapper.gson()).thenReturn(mockInputMapper);
                when(mockInputMapper.fromJson(responseJson, CTIUser.class)).thenReturn(responseUser);

                when(httpUtils.get(anyString())).thenReturn(responseJson);

                OutputResponse result = ctiServiceImpl.addUpdateUserData(requestJson, TEST_IP);

                assertNotNull(result);
                assertNotNull(result.getErrorMessage());
            }
        }
    }

    @Test
    void testTransferCall_ToAgent_Success() throws Exception {
        try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
            configMock.when(() -> ConfigProperties.getPropertyByName("call-transfer-to-agent-URL"))
                    .thenReturn("http://CTI_SERVER/api/transfer/agent?from=TRANSFER_FROM&to=TRANSFER_TO&campaign=CAMPAIGN_NAME&skill=SKILL_NAME&flag=SKILL_TRANSFER_FLAG&ip=AGENT_IP");
            configMock.when(() -> ConfigProperties.getPropertyByName("cti-server-ip"))
                    .thenReturn(TEST_SERVER_URL);

            TransferCall transferCall = new TransferCall();
            transferCall.setTransfer_to("agent456");  // Non-empty transfer_to
            transferCall.setTransfer_from("agent123");
            transferCall.setTransfer_campaign_info("TestCampaign");
            transferCall.setSkill_transfer_flag("Y");
            transferCall.setSkill("English");
            transferCall.setAgentIPAddress(null); // Null to skip updateCallDisposition

            String requestJson = "{\"transfer_to\":\"agent456\",\"transfer_from\":\"agent123\"}";

            CTIResponse ctiResponse = new CTIResponse();
            ctiResponse.setResponse_code("1");
            ctiResponse.setStatus("SUCCESS");

            TransferCall responseTransfer = new TransferCall();
            responseTransfer.setResponse(ctiResponse);

            String responseJson = "{\"response\":{\"response_code\":\"1\",\"status\":\"SUCCESS\"}}";

            try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class)) {
                InputMapper mockInputMapper = mock(InputMapper.class);
                inputMapperMock.when(() -> InputMapper.gson()).thenReturn(mockInputMapper);
                when(mockInputMapper.fromJson(responseJson, TransferCall.class)).thenReturn(responseTransfer);

                when(httpUtils.get(anyString())).thenReturn(responseJson);

                OutputResponse result = ctiServiceImpl.transferCall(requestJson, TEST_IP);

                assertNotNull(result);
                assertNotNull(result.getData());
            }
        }
    }

    @Test
    void testTransferCall_ToCampaign_Success() throws Exception {
        try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
            configMock.when(() -> ConfigProperties.getPropertyByName("call-transfer-to-campaign-URL"))
                    .thenReturn("http://CTI_SERVER/api/transfer/campaign?from=TRANSFER_FROM&to=TRANSFER_TO&campaign=CAMPAIGN_NAME&skill=SKILL_NAME&flag=SKILL_TRANSFER_FLAG&ip=AGENT_IP");
            configMock.when(() -> ConfigProperties.getPropertyByName("cti-server-ip"))
                    .thenReturn(TEST_SERVER_URL);

            TransferCall transferCall = new TransferCall();
            transferCall.setTransfer_to(""); // Empty transfer_to triggers campaign transfer
            transferCall.setTransfer_from("agent123");
            transferCall.setTransfer_campaign_info("TestCampaign");
            transferCall.setSkill_transfer_flag("Y");
            transferCall.setSkill("English");
            transferCall.setAgentIPAddress(TEST_IP); // Non-null to trigger updateCallDisposition

            String requestJson = "{\"transfer_to\":\"\",\"transfer_from\":\"agent123\"}";

            CTIResponse ctiResponse = new CTIResponse();
            ctiResponse.setResponse_code("0");
            ctiResponse.setStatus("FAILURE");
            ctiResponse.setReason("Transfer failed");

            TransferCall responseTransfer = new TransferCall();
            responseTransfer.setResponse(ctiResponse);

            String responseJson = "{\"response\":{\"response_code\":\"0\",\"status\":\"FAILURE\",\"reason\":\"Transfer failed\"}}";

            try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class)) {
                InputMapper mockInputMapper = mock(InputMapper.class);
                inputMapperMock.when(() -> InputMapper.gson()).thenReturn(mockInputMapper);
                when(mockInputMapper.fromJson(responseJson, TransferCall.class)).thenReturn(responseTransfer);

                when(httpUtils.get(anyString())).thenReturn(responseJson);

                OutputResponse result = ctiServiceImpl.transferCall(requestJson, TEST_IP);

                assertNotNull(result);
                assertNotNull(result.getErrorMessage());
            }
        }
    }

    @Test
    void testTransferCall_WithNullTransferCall() throws Exception {
        try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
            configMock.when(() -> ConfigProperties.getPropertyByName("call-transfer-to-campaign-URL"))
                    .thenReturn("http://CTI_SERVER/api/transfer/campaign?from=TRANSFER_FROM&to=TRANSFER_TO&campaign=CAMPAIGN_NAME&skill=SKILL_NAME&flag=SKILL_TRANSFER_FLAG&ip=AGENT_IP");
            configMock.when(() -> ConfigProperties.getPropertyByName("cti-server-ip"))
                    .thenReturn(TEST_SERVER_URL);

            // Test with null transferCall object
            String requestJson = "{}";

            CTIResponse ctiResponse = new CTIResponse();
            ctiResponse.setResponse_code("1");
            ctiResponse.setStatus("SUCCESS");

            TransferCall responseTransfer = new TransferCall();
            responseTransfer.setResponse(ctiResponse);

            String responseJson = "{\"response\":{\"response_code\":\"1\",\"status\":\"SUCCESS\"}}";

            try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class)) {
                InputMapper mockInputMapper = mock(InputMapper.class);
                inputMapperMock.when(() -> InputMapper.gson()).thenReturn(mockInputMapper);
                when(mockInputMapper.fromJson(responseJson, TransferCall.class)).thenReturn(responseTransfer);

                when(httpUtils.get(anyString())).thenReturn(responseJson);

                OutputResponse result = ctiServiceImpl.transferCall(requestJson, TEST_IP);

                assertNotNull(result);
                assertNotNull(result.getData());
            }
        }
    }

    @Test
    void testAddUpdateAgentSkills_Success() throws Exception {
        try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
            configMock.when(() -> ConfigProperties.getPropertyByName("add-update-agent-skills-URL"))
                    .thenReturn("http://CTI_SERVER/api/skills?agent=AGENT_ID&skill=SKILL_NAME&weight=WEIGHTAGE&operation=OPERATION&ip=AGENT_IP");
            configMock.when(() -> ConfigProperties.getPropertyByName("cti-server-ip"))
                    .thenReturn(TEST_SERVER_URL);
            configMock.when(() -> ConfigProperties.getPropertyByName("get-agent-ip-address-URL"))
                    .thenReturn("http://CTI_SERVER/api/agentip?agent=AGENT_ID");

        AgentSkills agentSkills = new AgentSkills();
        // Use ReflectionTestUtils to set the field that getAgentID() reads from
        ReflectionTestUtils.setField(agentSkills, "agentid", TEST_AGENT_ID);
        agentSkills.setSkill("English");
        agentSkills.setWeight("5");
        agentSkills.setType("ADD");
        // Don't set response to avoid null pointer in setResponse method

        // Use hand-crafted JSON to avoid serialization issues with null response field
        String requestJson = "{\"agentid\":\"" + TEST_AGENT_ID + "\",\"skill\":\"English\",\"weight\":\"5\",\"type\":\"ADD\"}";            // Use full AgentSkills JSON object for correct deserialization
            String responseJson = "{"
                + "\"agent_id\":\"agent123\"," 
                + "\"skill\":\"English\"," 
                + "\"weight\":\"5\"," 
                + "\"type\":\"ADD\"," 
                + "\"response\":{\"response_code\":\"1\",\"status\":\"SUCCESS\",\"reason\":\"\"}"
                + "}";

            try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class)) {
                InputMapper mockInputMapper = mock(InputMapper.class);
                inputMapperMock.when(() -> InputMapper.gson()).thenReturn(mockInputMapper);
                // Create expected AgentSkills object for mock
                CTIResponse ctiResponse = new CTIResponse();
                ctiResponse.setResponse_code("1");
                ctiResponse.setStatus("SUCCESS");
                ctiResponse.setReason("");
                ctiResponse.setSkill("English");
                ctiResponse.setWeight("5");
                AgentSkills responseSkills = new AgentSkills();
                // Use ReflectionTestUtils to set the response field directly to match getResponseObj() called in implementation
                ReflectionTestUtils.setField(responseSkills, "response", ctiResponse);
                when(mockInputMapper.fromJson(responseJson, AgentSkills.class)).thenReturn(responseSkills);

                // Mock getAgentIP call response and AgentState for it
                String agentIPResponse = "{\"response\":{\"response_code\":\"1\",\"status\":\"SUCCESS\",\"agent_ip\":\"192.168.1.100\"}}";
                CTIResponseTemp agentIPCtiResponse = new CTIResponseTemp();
                agentIPCtiResponse.setResponse_code("1");
                agentIPCtiResponse.setStatus("SUCCESS");
                agentIPCtiResponse.setAgent_ip("192.168.1.100");
                AgentState agentIPState = new AgentState();
                agentIPState.setResponse(agentIPCtiResponse);
                
                when(mockInputMapper.fromJson(agentIPResponse, AgentState.class)).thenReturn(agentIPState);
                when(httpUtils.get(contains("agentip"))).thenReturn(agentIPResponse);
                when(httpUtils.get(contains("skills"))).thenReturn(responseJson);

                CTIServiceImpl spyService = spy(ctiServiceImpl);
                doReturn("0.0.0.0").when(spyService).getAgentIP(TEST_AGENT_ID);

                OutputResponse result = spyService.addUpdateAgentSkills(requestJson, TEST_IP);

                assertNotNull(result);
                assertNotNull(result.getData());
            }
        }
    }

    @Test
    void testAddUpdateAgentSkills_Failure() throws Exception {
        try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
            configMock.when(() -> ConfigProperties.getPropertyByName("add-update-agent-skills-URL"))
                    .thenReturn("http://CTI_SERVER/api/skills?agent=AGENT_ID&skill=SKILL_NAME&weight=WEIGHTAGE&operation=OPERATION&ip=AGENT_IP");
            configMock.when(() -> ConfigProperties.getPropertyByName("cti-server-ip"))
                    .thenReturn(TEST_SERVER_URL);

        AgentSkills agentSkills = new AgentSkills();
        // Use ReflectionTestUtils to set the field that getAgentID() reads from (null for failure test)
        ReflectionTestUtils.setField(agentSkills, "agentid", null);
        agentSkills.setSkill(null); // Test null skill
        agentSkills.setWeight(null); // Test null weight
        agentSkills.setType(null); // Test null type
        // Don't set response to avoid null pointer in setResponse method

        // Use hand-crafted JSON to avoid serialization issues
        String requestJson = "{\"agentid\":null,\"skill\":null,\"weight\":null,\"type\":null}";

        String responseJson = "{"
            + "\"agent_id\":null," 
            + "\"skill\":null," 
            + "\"weight\":null," 
            + "\"type\":null," 
            + "\"response\":{\"response_code\":\"0\",\"status\":\"FAILURE\",\"reason\":\"Invalid data\"}"
            + "}";

            try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class)) {
                InputMapper mockInputMapper = mock(InputMapper.class);
                inputMapperMock.when(() -> InputMapper.gson()).thenReturn(mockInputMapper);
                // Create expected AgentSkills object for mock
                CTIResponse ctiResponse = new CTIResponse();
                ctiResponse.setResponse_code("0");
                ctiResponse.setStatus("FAILURE");
                ctiResponse.setReason("Invalid data");
                AgentSkills responseSkills = new AgentSkills();
                // Use ReflectionTestUtils to set the response field directly to match getResponseObj() called in implementation
                ReflectionTestUtils.setField(responseSkills, "response", ctiResponse);

                when(mockInputMapper.fromJson(responseJson, AgentSkills.class)).thenReturn(responseSkills);
                when(httpUtils.get(anyString())).thenReturn(responseJson);

                CTIServiceImpl spyService = spy(ctiServiceImpl);
                doReturn("0.0.0.0").when(spyService).getAgentIP(anyString());

                OutputResponse result = spyService.addUpdateAgentSkills(requestJson, TEST_IP);
                assertNotNull(result);
                assertEquals("Invalid data", result.getErrorMessage());
            }
        }
    }

    @Test
    void testSetCallDisposition_Success() throws Exception {
        try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
            configMock.when(() -> ConfigProperties.getPropertyByName("update-call-disposition-URL"))
                    .thenReturn("http://CTI_SERVER/api/disposition?agent=AGENT_ID&ip=AGENT_IP&subtype=CALL_SUB_TYPE&type=CALL_TYPE&session=SESSION_ID");
            configMock.when(() -> ConfigProperties.getPropertyByName("cti-server-ip"))
                    .thenReturn(TEST_SERVER_URL);

            CallDisposition disposition = new CallDisposition();
            disposition.setAgent_id(TEST_AGENT_ID);
            disposition.setCust_disp("Customer Complaint");
            disposition.setCategory("Support");
            disposition.setSession_id("session123");

            String requestJson = getConfiguredObjectMapper().writeValueAsString(disposition);

            CTIResponse ctiResponse = new CTIResponse();
            ctiResponse.setResponse_code("1");
            ctiResponse.setStatus("SUCCESS");

            CallDisposition responseDisposition = new CallDisposition();
            responseDisposition.setResponse(ctiResponse);

            String responseJson = getConfiguredObjectMapper().writeValueAsString(responseDisposition);

            try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class)) {
                InputMapper mockInputMapper = mock(InputMapper.class);
                inputMapperMock.when(() -> InputMapper.gson()).thenReturn(mockInputMapper);
                when(mockInputMapper.fromJson(responseJson, CallDisposition.class)).thenReturn(responseDisposition);

                when(httpUtils.get(anyString())).thenReturn(responseJson);

                CTIServiceImpl spyService = spy(ctiServiceImpl);
                doReturn("192.168.1.100").when(spyService).getAgentIP(TEST_AGENT_ID);

                OutputResponse result = spyService.setCallDisposition(requestJson, TEST_IP);

                assertNotNull(result);
                assertNotNull(result.getData());
            }
        }
    }

    @Test
    void testSetCallDisposition_WithNullValues() throws Exception {
        try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
            configMock.when(() -> ConfigProperties.getPropertyByName("update-call-disposition-URL"))
                    .thenReturn("http://CTI_SERVER/api/disposition?agent=AGENT_ID&ip=AGENT_IP&subtype=CALL_SUB_TYPE&type=CALL_TYPE&session=SESSION_ID");
            configMock.when(() -> ConfigProperties.getPropertyByName("cti-server-ip"))
                    .thenReturn(TEST_SERVER_URL);

            CallDisposition disposition = new CallDisposition();
            disposition.setAgent_id(null); // Test null agent ID
            disposition.setCust_disp(null); // Test null disposition
            disposition.setCategory(null); // Test null category
            disposition.setSession_id(null); // Test null session ID

            String requestJson = getConfiguredObjectMapper().writeValueAsString(disposition);

            CTIResponse ctiResponse = new CTIResponse();
            ctiResponse.setResponse_code("0");
            ctiResponse.setStatus("FAILURE");
            ctiResponse.setReason("Invalid parameters");

            CallDisposition responseDisposition = new CallDisposition();
            responseDisposition.setResponse(ctiResponse);

            String responseJson = getConfiguredObjectMapper().writeValueAsString(responseDisposition);

            try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class)) {
                InputMapper mockInputMapper = mock(InputMapper.class);
                inputMapperMock.when(() -> InputMapper.gson()).thenReturn(mockInputMapper);
                when(mockInputMapper.fromJson(responseJson, CallDisposition.class)).thenReturn(responseDisposition);

                when(httpUtils.get(anyString())).thenReturn(responseJson);

                CTIServiceImpl spyService = spy(ctiServiceImpl);
                doReturn("0.0.0.0").when(spyService).getAgentIP("");

                OutputResponse result = spyService.setCallDisposition(requestJson, TEST_IP);

                assertNotNull(result);
                assertNotNull(result.getErrorMessage());
            }
        }
    }

    @Test
    void testGetIVRSPathDetails_Success() throws Exception {
        try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
            configMock.when(() -> ConfigProperties.getPropertyByName("agent-ivrs-path-URL"))
                    .thenReturn("http://CTI_SERVER/api/ivrs/path?agent=AGENT_ID");
            configMock.when(() -> ConfigProperties.getPropertyByName("cti-server-ip"))
                    .thenReturn(TEST_SERVER_URL);

            AgentState zoneData = new AgentState();
            zoneData.setAgent_id(TEST_AGENT_ID);

            String requestJson = "{\"agent_id\":\"" + TEST_AGENT_ID + "\"}";

            CTIResponseTemp ctiResponse = new CTIResponseTemp();
            ctiResponse.setResponse_code("1");
            ctiResponse.setStatus("SUCCESS");
            ctiResponse.setIvrs_path("English->Support->CustomerCare");

            AgentState responseState = new AgentState();
            responseState.setResponse(ctiResponse);

            String responseJson = "{\"response\":{\"response_code\":\"1\",\"status\":\"SUCCESS\",\"ivrs_path\":\"English->Support->CustomerCare\"}}";

            try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class)) {
                InputMapper mockInputMapper = mock(InputMapper.class);
                inputMapperMock.when(() -> InputMapper.gson()).thenReturn(mockInputMapper);
                when(mockInputMapper.fromJson(responseJson, AgentState.class)).thenReturn(responseState);

                when(httpUtils.get(anyString())).thenReturn(responseJson);

                OutputResponse result = ctiServiceImpl.getIVRSPathDetails(requestJson, TEST_IP);

                assertNotNull(result);
                assertNotNull(result.getData());
            }
        }
    }

    @Test
    void testGetIVRSPathDetails_WithShortIvrsPath() throws Exception {
        try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
            configMock.when(() -> ConfigProperties.getPropertyByName("agent-ivrs-path-URL"))
                    .thenReturn("http://CTI_SERVER/api/ivrs/path?agent=AGENT_ID");
            configMock.when(() -> ConfigProperties.getPropertyByName("cti-server-ip"))
                    .thenReturn(TEST_SERVER_URL);

            AgentState zoneData = new AgentState();
            zoneData.setAgent_id(null); // Test null agent ID

            String requestJson = "{\"agent_id\":null}";

            CTIResponseTemp ctiResponse = new CTIResponseTemp();
            ctiResponse.setResponse_code("1");
            ctiResponse.setStatus("SUCCESS");
            ctiResponse.setIvrs_path("English"); // Short path with less than 3 parts

            AgentState responseState = new AgentState();
            responseState.setResponse(ctiResponse);

            String responseJson = "{\"response\":{\"response_code\":\"1\",\"status\":\"SUCCESS\",\"ivrs_path\":\"English\"}}";

            try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class)) {
                InputMapper mockInputMapper = mock(InputMapper.class);
                inputMapperMock.when(() -> InputMapper.gson()).thenReturn(mockInputMapper);
                when(mockInputMapper.fromJson(responseJson, AgentState.class)).thenReturn(responseState);

                when(httpUtils.get(anyString())).thenReturn(responseJson);

                OutputResponse result = ctiServiceImpl.getIVRSPathDetails(requestJson, TEST_IP);

                assertNotNull(result);
                assertNotNull(result.getData());
            }
        }
    }

    @Test
    void testGetIVRSPathDetails_Failure() throws Exception {
        try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
            configMock.when(() -> ConfigProperties.getPropertyByName("agent-ivrs-path-URL"))
                    .thenReturn("http://CTI_SERVER/api/ivrs/path?agent=AGENT_ID");
            configMock.when(() -> ConfigProperties.getPropertyByName("cti-server-ip"))
                    .thenReturn(TEST_SERVER_URL);

            AgentState zoneData = new AgentState();
            zoneData.setAgent_id(TEST_AGENT_ID);

            String requestJson = "{\"agent_id\":\"" + TEST_AGENT_ID + "\"}";

            CTIResponseTemp ctiResponse = new CTIResponseTemp();
            ctiResponse.setResponse_code("0");
            ctiResponse.setStatus("FAILURE");
            ctiResponse.setReason("No IVRS path found");

            AgentState responseState = new AgentState();
            responseState.setResponse(ctiResponse);

            String responseJson = "{\"response\":{\"response_code\":\"0\",\"status\":\"FAILURE\",\"reason\":\"No IVRS path found\"}}";

            try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class)) {
                InputMapper mockInputMapper = mock(InputMapper.class);
                inputMapperMock.when(() -> InputMapper.gson()).thenReturn(mockInputMapper);
                when(mockInputMapper.fromJson(responseJson, AgentState.class)).thenReturn(responseState);

                when(httpUtils.get(anyString())).thenReturn(responseJson);

                OutputResponse result = ctiServiceImpl.getIVRSPathDetails(requestJson, TEST_IP);

                assertNotNull(result);
                assertNotNull(result.getErrorMessage());
            }
        }
    }

    // ===============================
    // BATCH 2: Next 5 Methods
    // ===============================

    @Test
    void testCallBeneficiary_Success() throws Exception {
        try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
            configMock.when(() -> ConfigProperties.getPropertyByName("call-beneficiary-URL"))
                    .thenReturn("http://CTI_SERVER/api/call?agent=AGENT_ID&phone=PHONE_NO&ip=AGENT_IP");
            configMock.when(() -> ConfigProperties.getPropertyByName("cti-server-ip"))
                    .thenReturn(TEST_SERVER_URL);

            CallBeneficiary callBeneficiary = new CallBeneficiary();
            callBeneficiary.setAgent_id(TEST_AGENT_ID);
            callBeneficiary.setPhone_num("9876543210");

            // Use hand-crafted JSON to avoid logger serialization issues
            String requestJson = "{\"agent_id\":\"" + TEST_AGENT_ID + "\",\"phone_num\":\"9876543210\"}";

            CTIResponse ctiResponse = new CTIResponse();
            ctiResponse.setResponse_code("1");
            ctiResponse.setStatus("SUCCESS");

            CallBeneficiary responseCall = new CallBeneficiary();
            responseCall.setResponse(ctiResponse);

            // Use hand-crafted JSON to avoid logger serialization issues
            String responseJson = "{\"response\":{\"response_code\":\"1\",\"status\":\"SUCCESS\"}}";

            try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class)) {
                InputMapper mockInputMapper = mock(InputMapper.class);
                inputMapperMock.when(() -> InputMapper.gson()).thenReturn(mockInputMapper);
                when(mockInputMapper.fromJson(responseJson, CallBeneficiary.class)).thenReturn(responseCall);

                when(httpUtils.get(anyString())).thenReturn(responseJson);

                CTIServiceImpl spyService = spy(ctiServiceImpl);
                doReturn("192.168.1.100").when(spyService).getAgentIP(TEST_AGENT_ID);

                OutputResponse result = spyService.callBeneficiary(requestJson, TEST_IP);

                assertNotNull(result);
                assertNotNull(result.getData());
            }
        }
    }

    @Test
    void testCallBeneficiary_Failure() throws Exception {
        try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
            configMock.when(() -> ConfigProperties.getPropertyByName("call-beneficiary-URL"))
                    .thenReturn("http://CTI_SERVER/api/call?agent=AGENT_ID&phone=PHONE_NO&ip=AGENT_IP");
            configMock.when(() -> ConfigProperties.getPropertyByName("cti-server-ip"))
                    .thenReturn(TEST_SERVER_URL);

            CallBeneficiary callBeneficiary = new CallBeneficiary();
            callBeneficiary.setAgent_id(null); // Null agent ID for failure case
            callBeneficiary.setPhone_num(null); // Null phone number for failure case

            // Use hand-crafted JSON to avoid logger serialization issues
            String requestJson = "{\"agent_id\":null,\"phone_num\":null}";

            CTIResponse ctiResponse = new CTIResponse();
            ctiResponse.setResponse_code("0");
            ctiResponse.setStatus("FAILURE");
            ctiResponse.setReason("Invalid agent or phone number");

            CallBeneficiary responseCall = new CallBeneficiary();
            responseCall.setResponse(ctiResponse);

            // Use hand-crafted JSON to avoid logger serialization issues
            String responseJson = "{\"response\":{\"response_code\":\"0\",\"status\":\"FAILURE\",\"reason\":\"Invalid agent or phone number\"}}";

            try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class)) {
                InputMapper mockInputMapper = mock(InputMapper.class);
                inputMapperMock.when(() -> InputMapper.gson()).thenReturn(mockInputMapper);
                when(mockInputMapper.fromJson(responseJson, CallBeneficiary.class)).thenReturn(responseCall);

                when(httpUtils.get(anyString())).thenReturn(responseJson);

                CTIServiceImpl spyService = spy(ctiServiceImpl);
                doReturn("0.0.0.0").when(spyService).getAgentIP("");

                OutputResponse result = spyService.callBeneficiary(requestJson, TEST_IP);

                assertNotNull(result);
                assertNotNull(result.getErrorMessage());
            }
        }
    }

    @Test
    void testDisconnectCall_Success() throws Exception {
        try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
            configMock.when(() -> ConfigProperties.getPropertyByName("disonnect-api-URL"))
                    .thenReturn("http://cti_server/api/disconnect?agent=AGENT_ID&session=SESSION_ID&ip=AGENT_IP&feedback=IS_FEEDBACK");
            configMock.when(() -> ConfigProperties.getPropertyByName("cti-server-ip"))
                    .thenReturn(TEST_SERVER_URL);
            configMock.when(() -> ConfigProperties.getPropertyByName("get-agent-ip-address-URL"))
                    .thenReturn("http://CTI_SERVER/api/agentip?agent=AGENT_ID");

            AgentSkills agentSkills = new AgentSkills();
            // Use ReflectionTestUtils to set the field that getAgentID() reads from
            ReflectionTestUtils.setField(agentSkills, "agentid", TEST_AGENT_ID);
            ReflectionTestUtils.setField(agentSkills, "call_id", "session123");
            ReflectionTestUtils.setField(agentSkills, "isFeedback", 1);
            // Don't serialize response field, use hand-crafted JSON instead

            // Use hand-crafted JSON to avoid serialization issues
            String requestJson = "{\"agentid\":\"" + TEST_AGENT_ID + "\",\"call_id\":\"session123\",\"isFeedback\":1}";

            CTIResponse ctiResponse = new CTIResponse();
            ctiResponse.setResponse_code("1");
            ctiResponse.setStatus("SUCCESS");

            AgentSkills responseSkills = new AgentSkills();
            responseSkills.setResponse(ctiResponse);

            // Use hand-crafted JSON to avoid response field serialization issues
            String responseJson = "{\"response\":{\"response_code\":\"1\",\"status\":\"SUCCESS\"}}";

            try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class)) {
                InputMapper mockInputMapper = mock(InputMapper.class);
                inputMapperMock.when(() -> InputMapper.gson()).thenReturn(mockInputMapper);
                
                // Use ReflectionTestUtils to set the response field directly to match getResponseObj() called in implementation
                ReflectionTestUtils.setField(responseSkills, "response", ctiResponse);
                when(mockInputMapper.fromJson(responseJson, AgentSkills.class)).thenReturn(responseSkills);

                // Mock getAgentIP call response and AgentState for it
                String agentIPResponse = "{\"response\":{\"response_code\":\"1\",\"status\":\"SUCCESS\",\"agent_ip\":\"192.168.1.100\"}}";
                CTIResponseTemp agentIPCtiResponse = new CTIResponseTemp();
                agentIPCtiResponse.setResponse_code("1");
                agentIPCtiResponse.setStatus("SUCCESS");
                agentIPCtiResponse.setAgent_ip("192.168.1.100");
                AgentState agentIPState = new AgentState();
                agentIPState.setResponse(agentIPCtiResponse);
                
                when(mockInputMapper.fromJson(agentIPResponse, AgentState.class)).thenReturn(agentIPState);
                when(httpUtils.get(contains("agentip"))).thenReturn(agentIPResponse);
                when(httpUtils.get(contains("disconnect"))).thenReturn(responseJson);

                CTIServiceImpl spyService = spy(ctiServiceImpl);
                doReturn("192.168.1.100").when(spyService).getAgentIP(TEST_AGENT_ID);

                OutputResponse result = spyService.disconnectCall(requestJson, TEST_IP);

                assertNotNull(result);
                assertNotNull(result.getData());
            }
        }
    }

    @Test
    void testDisconnectCall_Failure() throws Exception {
        try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
            configMock.when(() -> ConfigProperties.getPropertyByName("disonnect-api-URL"))
                    .thenReturn("http://cti_server/api/disconnect?agent=AGENT_ID&session=SESSION_ID&ip=AGENT_IP&feedback=IS_FEEDBACK");
            configMock.when(() -> ConfigProperties.getPropertyByName("cti-server-ip"))
                    .thenReturn(TEST_SERVER_URL);
            configMock.when(() -> ConfigProperties.getPropertyByName("get-agent-ip-address-URL"))
                    .thenReturn("http://CTI_SERVER/api/agentip?agent=AGENT_ID");

            AgentSkills agentSkills = new AgentSkills();
            // Use ReflectionTestUtils to set null values for failure case
            ReflectionTestUtils.setField(agentSkills, "agentid", null);
            ReflectionTestUtils.setField(agentSkills, "call_id", null);
            ReflectionTestUtils.setField(agentSkills, "isFeedback", 0);

            // Use hand-crafted JSON to avoid serialization issues with null response field
            String requestJson = "{\"agentid\":null,\"call_id\":null,\"isFeedback\":0}";

            CTIResponse ctiResponse = new CTIResponse();
            ctiResponse.setResponse_code("0");
            ctiResponse.setStatus("FAILURE");
            ctiResponse.setReason("Invalid session or agent");

            AgentSkills responseSkills = new AgentSkills();
            responseSkills.setResponse(ctiResponse);

            // Use hand-crafted JSON to avoid response field serialization issues
            String responseJson = "{\"response\":{\"response_code\":\"0\",\"status\":\"FAILURE\",\"reason\":\"Invalid session or agent\"}}";

            try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class)) {
                InputMapper mockInputMapper = mock(InputMapper.class);
                inputMapperMock.when(() -> InputMapper.gson()).thenReturn(mockInputMapper);
                
                // Use ReflectionTestUtils to set the response field directly to match getResponseObj() called in implementation
                ReflectionTestUtils.setField(responseSkills, "response", ctiResponse);
                when(mockInputMapper.fromJson(responseJson, AgentSkills.class)).thenReturn(responseSkills);

                // Mock getAgentIP call response and AgentState for it
                String agentIPResponse = "{\"response\":{\"response_code\":\"1\",\"status\":\"SUCCESS\",\"agent_ip\":\"192.168.1.100\"}}";
                CTIResponseTemp agentIPCtiResponse = new CTIResponseTemp();
                agentIPCtiResponse.setResponse_code("1");
                agentIPCtiResponse.setStatus("SUCCESS");
                agentIPCtiResponse.setAgent_ip("192.168.1.100");
                AgentState agentIPState = new AgentState();
                agentIPState.setResponse(agentIPCtiResponse);
                
                when(mockInputMapper.fromJson(agentIPResponse, AgentState.class)).thenReturn(agentIPState);
                when(httpUtils.get(contains("agentip"))).thenReturn(agentIPResponse);
                when(httpUtils.get(contains("disconnect"))).thenReturn(responseJson);

                CTIServiceImpl spyService = spy(ctiServiceImpl);
                doReturn("0.0.0.0").when(spyService).getAgentIP("");

                OutputResponse result = spyService.disconnectCall(requestJson, TEST_IP);

                assertNotNull(result);
                assertNotNull(result.getErrorMessage());
            }
        }
    }

    @Test
    void testCustomerPreferredLanguage_Success() throws Exception {
        try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
            configMock.when(() -> ConfigProperties.getPropertyByName("preferred-language-URL"))
                    .thenReturn("http://CTI_SERVER/api/language?campaign=CAMPAIGN_NAME&language=LANGUAGE_NAME&phone=CUSTOMER_PHONE&action=ACTION_NAME");
            configMock.when(() -> ConfigProperties.getPropertyByName("cti-server-ip"))
                    .thenReturn(TEST_SERVER_URL);

            CustomerLanguage customerLanguage = new CustomerLanguage();
            customerLanguage.setCampaign_name("TestCampaign");
            customerLanguage.setLanguage("English");
            customerLanguage.setCust_ph_no("9876543210");
            customerLanguage.setAction("SET");

            CTIResponse ctiResponse = new CTIResponse();
            ctiResponse.setResponse_code("1");
            ctiResponse.setStatus("SUCCESS");

            CustomerLanguage responseLanguage = new CustomerLanguage();
            responseLanguage.setResponse(ctiResponse);

            // Use hand-crafted JSON to avoid serialization issues
            String responseJson = "{\"response\":{\"response_code\":\"1\",\"status\":\"SUCCESS\"}}";

            ObjectMapper mockObjectMapper = mock(ObjectMapper.class);
            when(mockObjectMapper.readValue(responseJson, CustomerLanguage.class)).thenReturn(responseLanguage);

            when(httpUtils.get(anyString())).thenReturn(responseJson);

            // Create service instance manually and inject the mocked ObjectMapper
            CTIServiceImpl testService = new CTIServiceImpl();
            ReflectionTestUtils.setField(testService, "httpUtils", httpUtils);

            OutputResponse result = testService.customerPreferredLanguage(customerLanguage, TEST_IP);

            assertNotNull(result);
            assertNotNull(result.getData());
        }
    }

    @Test
    void testCustomerPreferredLanguage_Failure() throws Exception {
        try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
            configMock.when(() -> ConfigProperties.getPropertyByName("preferred-language-URL"))
                    .thenReturn("http://CTI_SERVER/api/language?campaign=CAMPAIGN_NAME&language=LANGUAGE_NAME&phone=CUSTOMER_PHONE&action=ACTION_NAME");
            configMock.when(() -> ConfigProperties.getPropertyByName("cti-server-ip"))
                    .thenReturn(TEST_SERVER_URL);

            CustomerLanguage customerLanguage = new CustomerLanguage();
            customerLanguage.setCampaign_name(null); // Null values for failure case
            customerLanguage.setLanguage(null);
            customerLanguage.setCust_ph_no(null);
            customerLanguage.setAction(null);

            CTIResponse ctiResponse = new CTIResponse();
            ctiResponse.setResponse_code("0");
            ctiResponse.setStatus("FAILURE");
            ctiResponse.setReason("Invalid language preference data");

            CustomerLanguage responseLanguage = new CustomerLanguage();
            responseLanguage.setResponse(ctiResponse);

            // Use hand-crafted JSON to avoid serialization issues
            String responseJson = "{\"response\":{\"response_code\":\"0\",\"status\":\"FAILURE\",\"reason\":\"Invalid language preference data\"}}";

            ObjectMapper mockObjectMapper = mock(ObjectMapper.class);
            when(mockObjectMapper.readValue(responseJson, CustomerLanguage.class)).thenReturn(responseLanguage);

            when(httpUtils.get(anyString())).thenReturn(responseJson);

            // Create service instance manually and inject the mocked ObjectMapper
            CTIServiceImpl testService = new CTIServiceImpl();
            ReflectionTestUtils.setField(testService, "httpUtils", httpUtils);

            OutputResponse result = testService.customerPreferredLanguage(customerLanguage, TEST_IP);

            assertNotNull(result);
            assertNotNull(result.getErrorMessage());
        }
    }

    @Test
    void testAddAutoDialNumbers_Success() throws Exception {
        try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
            configMock.when(() -> ConfigProperties.getPropertyByName("add-auto-dail-numbers-URL"))
                    .thenReturn("http://CTI_SERVER/api/autodial/add");
            configMock.when(() -> ConfigProperties.getPropertyByName("cti-server-ip"))
                    .thenReturn(TEST_SERVER_URL);

            AutoPreviewDial autoPreviewDial1 = new AutoPreviewDial();
            autoPreviewDial1.setAgent_id(TEST_AGENT_ID);
            ReflectionTestUtils.setField(autoPreviewDial1, "camp_name", "TestCampaign");
            autoPreviewDial1.setMobile("9876543210");

            AutoPreviewDial[] autoPreviewDialArray = {autoPreviewDial1};

            String requestJson = getConfiguredObjectMapper().writeValueAsString(autoPreviewDialArray);

            CTIResponse ctiResponse = new CTIResponse();
            ctiResponse.setResponse_code("1");
            ctiResponse.setStatus("SUCCESS");

            AutoPreviewDial responseAutoDial = new AutoPreviewDial();
            responseAutoDial.setResponse(ctiResponse);

            String responseJson = getConfiguredObjectMapper().writeValueAsString(responseAutoDial);

            try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class)) {
                InputMapper mockInputMapper = mock(InputMapper.class);
                inputMapperMock.when(() -> InputMapper.gson()).thenReturn(mockInputMapper);
                when(mockInputMapper.fromJson(responseJson, AutoPreviewDial.class)).thenReturn(responseAutoDial);

                when(httpUtils.post(anyString(), anyString())).thenReturn(responseJson);

                CTIServiceImpl spyService = spy(ctiServiceImpl);
                doReturn("192.168.1.100").when(spyService).getAgentIP(TEST_AGENT_ID);

                OutputResponse result = spyService.addAutoDialNumbers(requestJson, TEST_IP);

                assertNotNull(result);
                assertNotNull(result.getData());
            }
        }
    }

    @Test
    void testAddAutoDialNumbers_Failure() throws Exception {
        try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
            configMock.when(() -> ConfigProperties.getPropertyByName("add-auto-dail-numbers-URL"))
                    .thenReturn("http://CTI_SERVER/api/autodial/add");
            configMock.when(() -> ConfigProperties.getPropertyByName("cti-server-ip"))
                    .thenReturn(TEST_SERVER_URL);

            AutoPreviewDial autoPreviewDial1 = new AutoPreviewDial();
            autoPreviewDial1.setAgent_id(null); // Null values for failure case
            ReflectionTestUtils.setField(autoPreviewDial1, "camp_name", null);
            autoPreviewDial1.setMobile(null);

            AutoPreviewDial[] autoPreviewDialArray = {autoPreviewDial1};

            String requestJson = getConfiguredObjectMapper().writeValueAsString(autoPreviewDialArray);

            CTIResponse ctiResponse = new CTIResponse();
            ctiResponse.setResponse_code("0");
            ctiResponse.setStatus("FAILURE");
            ctiResponse.setReason("Invalid auto dial data");

            AutoPreviewDial responseAutoDial = new AutoPreviewDial();
            responseAutoDial.setResponse(ctiResponse);

            String responseJson = getConfiguredObjectMapper().writeValueAsString(responseAutoDial);

            try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class)) {
                InputMapper mockInputMapper = mock(InputMapper.class);
                inputMapperMock.when(() -> InputMapper.gson()).thenReturn(mockInputMapper);
                when(mockInputMapper.fromJson(responseJson, AutoPreviewDial.class)).thenReturn(responseAutoDial);

                when(httpUtils.post(anyString(), anyString())).thenReturn(responseJson);

                CTIServiceImpl spyService = spy(ctiServiceImpl);
                doReturn("0.0.0.0").when(spyService).getAgentIP("");

                OutputResponse result = spyService.addAutoDialNumbers(requestJson, TEST_IP);

                assertNotNull(result);
                assertNotNull(result.getErrorMessage());
            }
        }
    }

    @Test
    void testSetAutoDialNumbers_Success() throws Exception {
        try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
            configMock.when(() -> ConfigProperties.getPropertyByName("set-auto-dail-numbers-URL"))
                    .thenReturn("http://CTI_SERVER/api/autodial/set?agent=AGENT_ID&campaign=CAMP_NAME&mobile=MOBILE&ip=AGENT_IP");
            configMock.when(() -> ConfigProperties.getPropertyByName("cti-server-ip"))
                    .thenReturn(TEST_SERVER_URL);

            AutoPreviewDial autoPreviewDial = new AutoPreviewDial();
            autoPreviewDial.setAgent_id(TEST_AGENT_ID);
            ReflectionTestUtils.setField(autoPreviewDial, "camp_name", "TestCampaign");
            autoPreviewDial.setMobile("9876543210");

            String requestJson = getConfiguredObjectMapper().writeValueAsString(autoPreviewDial);

            CTIResponse ctiResponse = new CTIResponse();
            ctiResponse.setResponse_code("1");
            ctiResponse.setStatus("SUCCESS");

            AutoPreviewDial responseAutoDial = new AutoPreviewDial();
            responseAutoDial.setResponse(ctiResponse);

            String responseJson = getConfiguredObjectMapper().writeValueAsString(responseAutoDial);

            try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class)) {
                InputMapper mockInputMapper = mock(InputMapper.class);
                inputMapperMock.when(() -> InputMapper.gson()).thenReturn(mockInputMapper);
                when(mockInputMapper.fromJson(responseJson, AutoPreviewDial.class)).thenReturn(responseAutoDial);

                when(httpUtils.get(anyString())).thenReturn(responseJson);

                CTIServiceImpl spyService = spy(ctiServiceImpl);
                doReturn("192.168.1.100").when(spyService).getAgentIP(TEST_AGENT_ID);

                OutputResponse result = spyService.setAutoDialNumbers(requestJson, TEST_IP);

                assertNotNull(result);
                assertNotNull(result.getData());
            }
        }
    }

    @Test
    void testSetAutoDialNumbers_Failure() throws Exception {
        try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
            configMock.when(() -> ConfigProperties.getPropertyByName("set-auto-dail-numbers-URL"))
                    .thenReturn("http://CTI_SERVER/api/autodial/set?agent=AGENT_ID&campaign=CAMP_NAME&mobile=MOBILE&ip=AGENT_IP");
            configMock.when(() -> ConfigProperties.getPropertyByName("cti-server-ip"))
                    .thenReturn(TEST_SERVER_URL);

            AutoPreviewDial autoPreviewDial = new AutoPreviewDial();
            autoPreviewDial.setAgent_id(null); // Null values for failure case
            ReflectionTestUtils.setField(autoPreviewDial, "camp_name", null);
            autoPreviewDial.setMobile(null);

            String requestJson = getConfiguredObjectMapper().writeValueAsString(autoPreviewDial);

            CTIResponse ctiResponse = new CTIResponse();
            ctiResponse.setResponse_code("0");
            ctiResponse.setStatus("FAILURE");
            ctiResponse.setReason("Invalid auto dial configuration");

            AutoPreviewDial responseAutoDial = new AutoPreviewDial();
            responseAutoDial.setResponse(ctiResponse);

            String responseJson = getConfiguredObjectMapper().writeValueAsString(responseAutoDial);

            try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class)) {
                InputMapper mockInputMapper = mock(InputMapper.class);
                inputMapperMock.when(() -> InputMapper.gson()).thenReturn(mockInputMapper);
                when(mockInputMapper.fromJson(responseJson, AutoPreviewDial.class)).thenReturn(responseAutoDial);

                when(httpUtils.get(anyString())).thenReturn(responseJson);

                CTIServiceImpl spyService = spy(ctiServiceImpl);
                doReturn("0.0.0.0").when(spyService).getAgentIP("");

                OutputResponse result = spyService.setAutoDialNumbers(requestJson, TEST_IP);

                assertNotNull(result);
                assertNotNull(result.getErrorMessage());
            }
        }
    }

    // ===============================
    // BATCH 3: Additional Methods for Complete Coverage
    // ===============================

    @Test
    void testGetAgentState_Success() throws Exception {
        try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
            configMock.when(() -> ConfigProperties.getPropertyByName("get-agent-status-URL"))
                    .thenReturn("http://CTI_SERVER/api/agent/status?agent=AGENT_ID&ip=AGENT_IP");
            configMock.when(() -> ConfigProperties.getPropertyByName("cti-server-ip"))
                    .thenReturn(TEST_SERVER_URL);

            AgentState agentState = new AgentState();
            agentState.setAgent_id(TEST_AGENT_ID);

            String requestJson = "{\"agent_id\":\"" + TEST_AGENT_ID + "\"}";

            CTIResponseTemp ctiResponse = new CTIResponseTemp();
            ctiResponse.setResponse_code("1");
            ctiResponse.setStatus("SUCCESS");
            ctiResponse.setState("READY");

            AgentState responseState = new AgentState();
            responseState.setResponse(ctiResponse);

            String responseJson = "{\"response\":{\"response_code\":\"1\",\"status\":\"SUCCESS\",\"state\":\"READY\"}}";

            try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class)) {
                InputMapper mockInputMapper = mock(InputMapper.class);
                inputMapperMock.when(() -> InputMapper.gson()).thenReturn(mockInputMapper);
                when(mockInputMapper.fromJson(responseJson, AgentState.class)).thenReturn(responseState);

                when(httpUtils.get(anyString())).thenReturn(responseJson);

                CTIServiceImpl spyService = spy(ctiServiceImpl);
                doReturn("192.168.1.100").when(spyService).getAgentIP(TEST_AGENT_ID);

                OutputResponse result = spyService.getAgentState(requestJson, TEST_IP);

                assertNotNull(result);
                assertNotNull(result.getData());
            }
        }
    }

    @Test
    void testGetAgentState_Failure() throws Exception {
        try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
            configMock.when(() -> ConfigProperties.getPropertyByName("get-agent-status-URL"))
                    .thenReturn("http://CTI_SERVER/api/agent/status?agent=AGENT_ID&ip=AGENT_IP");
            configMock.when(() -> ConfigProperties.getPropertyByName("cti-server-ip"))
                    .thenReturn(TEST_SERVER_URL);

            AgentState agentState = new AgentState();
            agentState.setAgent_id(null); // Null for failure case

            String requestJson = "{\"agent_id\":null}";

            CTIResponseTemp ctiResponse = new CTIResponseTemp();
            ctiResponse.setResponse_code("0");
            ctiResponse.setStatus("FAILURE");
            ctiResponse.setReason("Agent not found");

            AgentState responseState = new AgentState();
            responseState.setResponse(ctiResponse);

            String responseJson = "{\"response\":{\"response_code\":\"0\",\"status\":\"FAILURE\",\"reason\":\"Agent not found\"}}";

            try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class)) {
                InputMapper mockInputMapper = mock(InputMapper.class);
                inputMapperMock.when(() -> InputMapper.gson()).thenReturn(mockInputMapper);
                when(mockInputMapper.fromJson(responseJson, AgentState.class)).thenReturn(responseState);

                when(httpUtils.get(anyString())).thenReturn(responseJson);

                CTIServiceImpl spyService = spy(ctiServiceImpl);
                doReturn("0.0.0.0").when(spyService).getAgentIP("");

                OutputResponse result = spyService.getAgentState(requestJson, TEST_IP);

                assertNotNull(result);
                assertNotNull(result.getErrorMessage());
            }
        }
    }

    @Test
    void testGetAgentCallStats_Success() throws Exception {
        try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
            configMock.when(() -> ConfigProperties.getPropertyByName("get-agent-call-stats-URL"))
                    .thenReturn("http://CTI_SERVER/api/agent/stats?agent=AGENT_ID&ip=AGENT_IP");
            configMock.when(() -> ConfigProperties.getPropertyByName("cti-server-ip"))
                    .thenReturn(TEST_SERVER_URL);

            AgentCallStats agentCallStats = new AgentCallStats();
            ReflectionTestUtils.setField(agentCallStats, "agent_id", TEST_AGENT_ID);

            String requestJson = "{\"agent_id\":\"" + TEST_AGENT_ID + "\"}";

            CTIResponse ctiResponse = new CTIResponse();
            ctiResponse.setResponse_code("1");
            ctiResponse.setStatus("SUCCESS");
            ctiResponse.setTotal_calls("10");

            AgentCallStats responseStats = new AgentCallStats();
            responseStats.setResponse(ctiResponse);

            String responseJson = "{\"response\":{\"response_code\":\"1\",\"status\":\"SUCCESS\",\"total_calls\":\"10\"}}";

            try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class)) {
                InputMapper mockInputMapper = mock(InputMapper.class);
                inputMapperMock.when(() -> InputMapper.gson()).thenReturn(mockInputMapper);
                when(mockInputMapper.fromJson(responseJson, AgentCallStats.class)).thenReturn(responseStats);

                when(httpUtils.get(anyString())).thenReturn(responseJson);

                CTIServiceImpl spyService = spy(ctiServiceImpl);
                doReturn("192.168.1.100").when(spyService).getAgentIP(TEST_AGENT_ID);

                OutputResponse result = spyService.getAgentCallStats(requestJson, TEST_IP);

                assertNotNull(result);
                assertNotNull(result.getData());
            }
        }
    }

    @Test
    void testDoAgentLogin_Success() throws Exception {
        try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
            configMock.when(() -> ConfigProperties.getPropertyByName("do-agent-login-URL"))
                    .thenReturn("http://CTI_SERVER/api/agent/login?agent=AGENT_ID&ip=AGENT_IP");
            configMock.when(() -> ConfigProperties.getPropertyByName("cti-server-ip"))
                    .thenReturn(TEST_SERVER_URL);

            AgentState agentState = new AgentState();
            agentState.setAgent_id(TEST_AGENT_ID);

            String requestJson = "{\"agent_id\":\"" + TEST_AGENT_ID + "\"}";

            CTIResponseTemp ctiResponse = new CTIResponseTemp();
            ctiResponse.setResponse_code("1");
            ctiResponse.setStatus("SUCCESS");

            AgentState responseState = new AgentState();
            responseState.setResponse(ctiResponse);

            String responseJson = "{\"response\":{\"response_code\":\"1\",\"status\":\"SUCCESS\"}}";

            try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class)) {
                InputMapper mockInputMapper = mock(InputMapper.class);
                inputMapperMock.when(() -> InputMapper.gson()).thenReturn(mockInputMapper);
                when(mockInputMapper.fromJson(responseJson, AgentState.class)).thenReturn(responseState);

                when(httpUtils.get(anyString())).thenReturn(responseJson);

                CTIServiceImpl spyService = spy(ctiServiceImpl);
                doReturn("192.168.1.100").when(spyService).getAgentIP(TEST_AGENT_ID);

                OutputResponse result = spyService.doAgentLogin(requestJson, TEST_IP);

                assertNotNull(result);
                assertNotNull(result.getData());
            }
        }
    }

    @Test
    void testAgentLogout_Success() throws Exception {
        try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
            configMock.when(() -> ConfigProperties.getPropertyByName("do-agent-logout-URL"))
                    .thenReturn("http://CTI_SERVER/api/agent/logout?agent=AGENT_ID&ip=AGENT_IP");
            configMock.when(() -> ConfigProperties.getPropertyByName("cti-server-ip"))
                    .thenReturn(TEST_SERVER_URL);

            AgentState agentState = new AgentState();
            agentState.setAgent_id(TEST_AGENT_ID);

            String requestJson = "{\"agent_id\":\"" + TEST_AGENT_ID + "\"}";

            CTIResponseTemp ctiResponse = new CTIResponseTemp();
            ctiResponse.setResponse_code("1");
            ctiResponse.setStatus("SUCCESS");

            AgentState responseState = new AgentState();
            responseState.setResponse(ctiResponse);

            String responseJson = "{\"response\":{\"response_code\":\"1\",\"status\":\"SUCCESS\"}}";

            try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class)) {
                InputMapper mockInputMapper = mock(InputMapper.class);
                inputMapperMock.when(() -> InputMapper.gson()).thenReturn(mockInputMapper);
                when(mockInputMapper.fromJson(responseJson, AgentState.class)).thenReturn(responseState);

                when(httpUtils.get(anyString())).thenReturn(responseJson);

                CTIServiceImpl spyService = spy(ctiServiceImpl);
                doReturn("192.168.1.100").when(spyService).getAgentIP(TEST_AGENT_ID);

                OutputResponse result = spyService.agentLogout(requestJson, TEST_IP);

                assertNotNull(result);
                assertNotNull(result.getData());
            }
        }
    }

    @Test
    void testGetOnlineAgents_Success() throws Exception {
        try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
            configMock.when(() -> ConfigProperties.getPropertyByName("do-online-agent-URL"))
                    .thenReturn("http://CTI_SERVER/api/agents/online?agent=AGENT_ID&ip=AGENT_IP");
            configMock.when(() -> ConfigProperties.getPropertyByName("cti-server-ip"))
                    .thenReturn(TEST_SERVER_URL);
            configMock.when(() -> ConfigProperties.getPropertyByName("get-agent-ip-address-URL"))
                    .thenReturn("http://CTI_SERVER/api/agentip?agent=AGENT_ID");

            AgentState agentState = new AgentState();
            agentState.setAgent_id(TEST_AGENT_ID);

            String requestJson = "{\"agent_id\":\"" + TEST_AGENT_ID + "\"}";

            CTIResponseTemp ctiResponse = new CTIResponseTemp();
            ctiResponse.setResponse_code("1");
            ctiResponse.setStatus("SUCCESS");

            AgentState responseState = new AgentState();
            responseState.setResponse(ctiResponse);

            String responseJson = "{\"response\":{\"response_code\":\"1\",\"status\":\"SUCCESS\"}}";

            try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class)) {
                InputMapper mockInputMapper = mock(InputMapper.class);
                inputMapperMock.when(() -> InputMapper.gson()).thenReturn(mockInputMapper);
                when(mockInputMapper.fromJson(responseJson, AgentState.class)).thenReturn(responseState);

                // Mock getAgentIP call response and AgentState for it
                String agentIPResponse = "{\"response\":{\"response_code\":\"1\",\"status\":\"SUCCESS\",\"agent_ip\":\"192.168.1.100\"}}";
                CTIResponseTemp agentIPCtiResponse = new CTIResponseTemp();
                agentIPCtiResponse.setResponse_code("1");
                agentIPCtiResponse.setStatus("SUCCESS");
                agentIPCtiResponse.setAgent_ip("192.168.1.100");
                AgentState agentIPState = new AgentState();
                agentIPState.setResponse(agentIPCtiResponse);
                
                when(mockInputMapper.fromJson(agentIPResponse, AgentState.class)).thenReturn(agentIPState);
                when(httpUtils.get(contains("agentip"))).thenReturn(agentIPResponse);
                when(httpUtils.get(contains("online"))).thenReturn(responseJson);

                OutputResponse result = ctiServiceImpl.getOnlineAgents(requestJson, TEST_IP);

                assertNotNull(result);
                assertNotNull(result.getData());
            }
        }
    }


    // ...existing code...

    @Test
    void testGetCampaignRoles_Success() throws Exception {
        try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
            configMock.when(() -> ConfigProperties.getPropertyByName("get-campaign-roles-URL"))
                    .thenReturn("http://CTI_SERVER/api/campaign/roles?campaign=CAMPAIGN_NAME");
            configMock.when(() -> ConfigProperties.getPropertyByName("cti-server-ip"))
                    .thenReturn(TEST_SERVER_URL);

            CampaignRole campaignRole = new CampaignRole();
            campaignRole.setCampaign("TestCampaign");

            String requestJson = "{\"campaign\":\"TestCampaign\"}";

            CTIResponse ctiResponse = new CTIResponse();
            ctiResponse.setResponse_code("1");
            ctiResponse.setStatus("SUCCESS");

            CampaignRole responseRole = new CampaignRole();
            responseRole.setResponse(ctiResponse);

            String responseJson = "{\"response\":{\"response_code\":\"1\",\"status\":\"SUCCESS\"}}";

            try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class)) {
                InputMapper mockInputMapper = mock(InputMapper.class);
                inputMapperMock.when(() -> InputMapper.gson()).thenReturn(mockInputMapper);
                when(mockInputMapper.fromJson(responseJson, CampaignRole.class)).thenReturn(responseRole);

                when(httpUtils.get(anyString())).thenReturn(responseJson);

                OutputResponse result = ctiServiceImpl.getCampaignRoles(requestJson, TEST_IP);

                assertNotNull(result);
                assertNotNull(result.getData());
            }
        }
    }

    @Test
    void testGetCampaignSkills_Success() throws Exception {
        try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
            configMock.when(() -> ConfigProperties.getPropertyByName("get-campaign-skills-URL"))
                    .thenReturn("http://CTI_SERVER/api/campaign/skills?campaign=CAMPAIGN_NAME&ip=AGENT_IP");
            configMock.when(() -> ConfigProperties.getPropertyByName("cti-server-ip"))
                    .thenReturn(TEST_SERVER_URL);

            CampaignSkills campaignSkills = new CampaignSkills();
            campaignSkills.setCampaign("TestCampaign");

            String requestJson = "{\"campaign\":\"TestCampaign\"}";

            CTIResponse ctiResponse = new CTIResponse();
            ctiResponse.setResponse_code("1");
            ctiResponse.setStatus("SUCCESS");

            CampaignSkills responseSkills = new CampaignSkills();
            responseSkills.setResponse(ctiResponse);

            String responseJson = "{\"response\":{\"response_code\":\"1\",\"status\":\"SUCCESS\"}}";

            try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class)) {
                InputMapper mockInputMapper = mock(InputMapper.class);
                inputMapperMock.when(() -> InputMapper.gson()).thenReturn(mockInputMapper);
                when(mockInputMapper.fromJson(responseJson, CampaignSkills.class)).thenReturn(responseSkills);

                when(httpUtils.get(anyString())).thenReturn(responseJson);

                OutputResponse result = ctiServiceImpl.getCampaignSkills(requestJson, TEST_IP);

                assertNotNull(result);
                assertNotNull(result.getData());
            }
        }
    }

    @Test
    void testGetAgentIPAddress_Success() throws Exception {
        try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
            configMock.when(() -> ConfigProperties.getPropertyByName("get-agent-ip-address-URL"))
                    .thenReturn("http://CTI_SERVER/api/agent/ip?agent=AGENT_ID");
            configMock.when(() -> ConfigProperties.getPropertyByName("cti-server-ip"))
                    .thenReturn(TEST_SERVER_URL);

            AgentState agentState = new AgentState();
            agentState.setAgent_id(TEST_AGENT_ID);

            String requestJson = "{\"agent_id\":\"" + TEST_AGENT_ID + "\"}";

            CTIResponseTemp ctiResponse = new CTIResponseTemp();
            ctiResponse.setResponse_code("1");
            ctiResponse.setStatus("SUCCESS");
            ctiResponse.setAgent_ip("192.168.1.100");

            AgentState responseState = new AgentState();
            responseState.setResponse(ctiResponse);

            String responseJson = "{\"response\":{\"response_code\":\"1\",\"status\":\"SUCCESS\",\"agent_ip\":\"192.168.1.100\"}}";

            try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class)) {
                InputMapper mockInputMapper = mock(InputMapper.class);
                inputMapperMock.when(() -> InputMapper.gson()).thenReturn(mockInputMapper);
                when(mockInputMapper.fromJson(responseJson, AgentState.class)).thenReturn(responseState);

                when(httpUtils.get(anyString())).thenReturn(responseJson);

                OutputResponse result = ctiServiceImpl.getAgentIPAddress(requestJson, TEST_IP);

                assertNotNull(result);
                assertNotNull(result.getData());
            }
        }
    }

    @Test
    void testGetVoiceFileNew_Success() throws Exception {
        try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
            configMock.when(() -> ConfigProperties.getPropertyByName("get-voice-file-URL-New"))
                    .thenReturn("http://CTI_SERVER/api/voicefile/new?agent=AGENT_ID&session=SESSION_ID");
            configMock.when(() -> ConfigProperties.getPropertyByName("cti-server-ip"))
                    .thenReturn(TEST_SERVER_URL);

            CTIVoiceFile voiceFile = new CTIVoiceFile();
            voiceFile.setAgent_id(TEST_AGENT_ID);
            voiceFile.setSession_id("session123");

            String requestJson = "{\"agent_id\":\"" + TEST_AGENT_ID + "\",\"session_id\":\"session123\"}";

            String response = "{\"response\":\"voicefile\\/path\\/test.wav\"}";

            when(httpUtils.get(anyString())).thenReturn(response);

            OutputResponse result = ctiServiceImpl.getVoiceFileNew(requestJson, TEST_IP);

            assertNotNull(result);
            // The service returns the raw JSON string response
            assertEquals(response, result.getData());
        }
    }

    @Test
    void testUpdateCallDisposition_CallsSetCallDisposition() throws Exception {
        TransferCall transferCall = new TransferCall();
        transferCall.setCallTypeID(1);
        transferCall.setBenCallID(2L);
        transferCall.setAgentIPAddress(TEST_IP);

        CallType callType = new CallType();
        callType.setCallType("Support");
        callType.setCallGroupType("General");

        BeneficiaryCall callData = new BeneficiaryCall();
        callData.setCallID("session123");
        callData.setAgentID(TEST_AGENT_ID);

        when(iemrCalltypeRepositoryImplCustom.getCallTypeDetails(anyInt())).thenReturn(callType);
        when(beneficiaryCallRepository.findCallDetails(anyLong())).thenReturn(callData);

        OutputResponse mockResponse = new OutputResponse();
        mockResponse.setResponse("success");
        when(ctiService.setCallDisposition(anyString(), anyString())).thenReturn(mockResponse);

        // Call the private method via reflection
        var method = CTIServiceImpl.class.getDeclaredMethod("updateCallDisposition", TransferCall.class, String.class);
        method.setAccessible(true);
        method.invoke(ctiServiceImpl, transferCall, TEST_IP);

        verify(ctiService, atLeastOnce()).setCallDisposition(anyString(), eq(TEST_IP));
    }

    @Test
    void testGetAgentIP_Success() throws Exception {
        try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
            configMock.when(() -> ConfigProperties.getPropertyByName("get-agent-ip-address-URL"))
                    .thenReturn("http://CTI_SERVER/api/agent/ip?agent=AGENT_ID");
            configMock.when(() -> ConfigProperties.getPropertyByName("cti-server-ip"))
                    .thenReturn(TEST_SERVER_URL);

            String responseJson = "{\"response\":{\"response_code\":\"1\",\"status\":\"SUCCESS\",\"agent_ip\":\"192.168.1.100\"}}";

            AgentState responseState = new AgentState();
            CTIResponseTemp ctiResponse = new CTIResponseTemp();
            ctiResponse.setResponse_code("1");
            ctiResponse.setStatus("SUCCESS");
            ctiResponse.setAgent_ip("192.168.1.100");
            responseState.setResponse(ctiResponse);

            try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class)) {
                InputMapper mockInputMapper = mock(InputMapper.class);
                inputMapperMock.when(() -> InputMapper.gson()).thenReturn(mockInputMapper);
                when(mockInputMapper.fromJson(responseJson, AgentState.class)).thenReturn(responseState);

                when(httpUtils.get(anyString())).thenReturn(responseJson);

                String agentIP = ctiServiceImpl.getAgentIP(TEST_AGENT_ID);

                assertEquals("192.168.1.100", agentIP);
            }
        }
    }

    @Test
    void testSwitchToInbound_Success() throws Exception {
        try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
            configMock.when(() -> ConfigProperties.getPropertyByName("switch-to-inbound-URL"))
                    .thenReturn("http://CTI_SERVER/api/agent/inbound?agent=AGENT_ID&ip=AGENT_IP");
            configMock.when(() -> ConfigProperties.getPropertyByName("cti-server-ip"))
                    .thenReturn(TEST_SERVER_URL);

            AgentState agentState = new AgentState();
            agentState.setAgent_id(TEST_AGENT_ID);
            String requestJson = "{\"agent_id\":\"" + TEST_AGENT_ID + "\"}";

            CTIResponseTemp ctiResponse = new CTIResponseTemp();
            ctiResponse.setResponse_code("1");
            ctiResponse.setStatus("SUCCESS");

            AgentState responseState = new AgentState();
            responseState.setResponse(ctiResponse);
            String responseJson = "{\"response\":{\"response_code\":\"1\",\"status\":\"SUCCESS\"}}";

            try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class)) {
                InputMapper mockInputMapper = mock(InputMapper.class);
                inputMapperMock.when(() -> InputMapper.gson()).thenReturn(mockInputMapper);
                when(mockInputMapper.fromJson(responseJson, AgentState.class)).thenReturn(responseState);
                when(httpUtils.get(anyString())).thenReturn(responseJson);

                CTIServiceImpl spyService = spy(ctiServiceImpl);
                doReturn("192.168.1.100").when(spyService).getAgentIP(TEST_AGENT_ID);

                OutputResponse result = spyService.switchToInbound(requestJson, TEST_IP);
                assertNotNull(result);
                assertNotNull(result.getData());
            }
        }
    }

    @Test
    void testSwitchToInbound_Failure() throws Exception {
        try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
            configMock.when(() -> ConfigProperties.getPropertyByName("switch-to-inbound-URL"))
                    .thenReturn("http://CTI_SERVER/api/agent/inbound?agent=AGENT_ID&ip=AGENT_IP");
            configMock.when(() -> ConfigProperties.getPropertyByName("cti-server-ip"))
                    .thenReturn(TEST_SERVER_URL);

            AgentState agentState = new AgentState();
            agentState.setAgent_id(TEST_AGENT_ID);
            String requestJson = "{\"agent_id\":\"" + TEST_AGENT_ID + "\"}";

            CTIResponseTemp ctiResponse = new CTIResponseTemp();
            ctiResponse.setResponse_code("0");
            ctiResponse.setStatus("FAILURE");
            ctiResponse.setReason("Failed to switch to inbound");

            AgentState responseState = new AgentState();
            responseState.setResponse(ctiResponse);
            String responseJson = "{\"response\":{\"response_code\":\"0\",\"status\":\"FAILURE\",\"reason\":\"Failed to switch to inbound\"}}";

            try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class)) {
                InputMapper mockInputMapper = mock(InputMapper.class);
                inputMapperMock.when(() -> InputMapper.gson()).thenReturn(mockInputMapper);
                when(mockInputMapper.fromJson(responseJson, AgentState.class)).thenReturn(responseState);
                when(httpUtils.get(anyString())).thenReturn(responseJson);

                CTIServiceImpl spyService = spy(ctiServiceImpl);
                doReturn("192.168.1.100").when(spyService).getAgentIP(TEST_AGENT_ID);

                OutputResponse result = spyService.switchToInbound(requestJson, TEST_IP);
                assertNotNull(result);
                assertEquals("Failed to switch to inbound", result.getErrorMessage());
            }
        }
    }

    @Test
    void testSwitchToOutbound_Success() throws Exception {
        try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
            configMock.when(() -> ConfigProperties.getPropertyByName("switch-to-outbound-URL"))
                    .thenReturn("http://CTI_SERVER/api/agent/outbound?agent=AGENT_ID&ip=AGENT_IP");
            configMock.when(() -> ConfigProperties.getPropertyByName("cti-server-ip"))
                    .thenReturn(TEST_SERVER_URL);

            AgentState agentState = new AgentState();
            agentState.setAgent_id(TEST_AGENT_ID);
            String requestJson = "{\"agent_id\":\"" + TEST_AGENT_ID + "\"}";

            CTIResponseTemp ctiResponse = new CTIResponseTemp();
            ctiResponse.setResponse_code("1");
            ctiResponse.setStatus("SUCCESS");

            AgentState responseState = new AgentState();
            responseState.setResponse(ctiResponse);
            String responseJson = "{\"response\":{\"response_code\":\"1\",\"status\":\"SUCCESS\"}}";

            try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class)) {
                InputMapper mockInputMapper = mock(InputMapper.class);
                inputMapperMock.when(() -> InputMapper.gson()).thenReturn(mockInputMapper);
                when(mockInputMapper.fromJson(responseJson, AgentState.class)).thenReturn(responseState);
                when(httpUtils.get(anyString())).thenReturn(responseJson);

                CTIServiceImpl spyService = spy(ctiServiceImpl);
                doReturn("192.168.1.100").when(spyService).getAgentIP(TEST_AGENT_ID);

                OutputResponse result = spyService.switchToOutbound(requestJson, TEST_IP);
                assertNotNull(result);
                assertNotNull(result.getData());
            }
        }
    }

    @Test
    void testSwitchToOutbound_Failure() throws Exception {
        try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
            configMock.when(() -> ConfigProperties.getPropertyByName("switch-to-outbound-URL"))
                    .thenReturn("http://CTI_SERVER/api/agent/outbound?agent=AGENT_ID&ip=AGENT_IP");
            configMock.when(() -> ConfigProperties.getPropertyByName("cti-server-ip"))
                    .thenReturn(TEST_SERVER_URL);

            AgentState agentState = new AgentState();
            agentState.setAgent_id(TEST_AGENT_ID);
            String requestJson = "{\"agent_id\":\"" + TEST_AGENT_ID + "\"}";

            CTIResponseTemp ctiResponse = new CTIResponseTemp();
            ctiResponse.setResponse_code("0");
            ctiResponse.setStatus("FAILURE");
            ctiResponse.setReason("Failed to switch to outbound");

            AgentState responseState = new AgentState();
            responseState.setResponse(ctiResponse);
            String responseJson = "{\"response\":{\"response_code\":\"0\",\"status\":\"FAILURE\",\"reason\":\"Failed to switch to outbound\"}}";

            try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class)) {
                InputMapper mockInputMapper = mock(InputMapper.class);
                inputMapperMock.when(() -> InputMapper.gson()).thenReturn(mockInputMapper);
                when(mockInputMapper.fromJson(responseJson, AgentState.class)).thenReturn(responseState);
                when(httpUtils.get(anyString())).thenReturn(responseJson);

                CTIServiceImpl spyService = spy(ctiServiceImpl);
                doReturn("192.168.1.100").when(spyService).getAgentIP(TEST_AGENT_ID);

                OutputResponse result = spyService.switchToOutbound(requestJson, TEST_IP);
                assertNotNull(result);
                assertEquals("Failed to switch to outbound", result.getErrorMessage());
            }
        }
    }

    @Test
    void testGetVoiceFile_Success() throws Exception {
        try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
            configMock.when(() -> ConfigProperties.getPropertyByName("get-voice-file-URL"))
                    .thenReturn("http://CTI_SERVER/api/voicefile?agent=AGENT_ID&session=SESSION_ID");
            configMock.when(() -> ConfigProperties.getPropertyByName("cti-server-ip"))
                    .thenReturn(TEST_SERVER_URL);

            CTIVoiceFile voiceFile = new CTIVoiceFile();
            voiceFile.setAgent_id(TEST_AGENT_ID);
            voiceFile.setSession_id("session123");
            String requestJson = "{\"agent_id\":\"" + TEST_AGENT_ID + "\",\"session_id\":\"session123\"}";

            CTIResponse ctiResponse = new CTIResponse();
            ctiResponse.setResponse_code("1");
            ctiResponse.setStatus("SUCCESS");

            CTIVoiceFile responseVoiceFile = new CTIVoiceFile();
            responseVoiceFile.setResponse(ctiResponse);
            String responseJson = "{\"response\":{\"response_code\":\"1\",\"status\":\"SUCCESS\"}}";

            try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class)) {
                InputMapper mockInputMapper = mock(InputMapper.class);
                inputMapperMock.when(() -> InputMapper.gson()).thenReturn(mockInputMapper);
                when(mockInputMapper.fromJson(responseJson, CTIVoiceFile.class)).thenReturn(responseVoiceFile);
                when(httpUtils.get(anyString())).thenReturn(responseJson);

                OutputResponse result = ctiServiceImpl.getVoiceFile(requestJson, TEST_IP);
                assertNotNull(result);
                assertNotNull(result.getData());
            }
        }
    }

    @Test
    void testCreateVoiceFile_Success() throws Exception {
        try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
            configMock.when(() -> ConfigProperties.getPropertyByName("mix-voice-file-URL"))
                    .thenReturn("http://CTI_SERVER/api/voicefile/create?agent=AGENT_ID&session=SESSION_ID");
            configMock.when(() -> ConfigProperties.getPropertyByName("cti-server-ip"))
                    .thenReturn(TEST_SERVER_URL);

            CTIVoiceFile voiceFile = new CTIVoiceFile();
            voiceFile.setAgent_id(TEST_AGENT_ID);
            voiceFile.setSession_id("session123");
            String requestJson = "{\"agent_id\":\"" + TEST_AGENT_ID + "\",\"session_id\":\"session123\"}";

            CTIResponse ctiResponse = new CTIResponse();
            ctiResponse.setResponse_code("1");
            ctiResponse.setStatus("SUCCESS");

            CTIVoiceFile responseVoiceFile = new CTIVoiceFile();
            responseVoiceFile.setResponse(ctiResponse);
            String responseJson = "{\"response\":{\"response_code\":\"1\",\"status\":\"SUCCESS\"}}";

            try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class)) {
                InputMapper mockInputMapper = mock(InputMapper.class);
                inputMapperMock.when(() -> InputMapper.gson()).thenReturn(mockInputMapper);
                when(mockInputMapper.fromJson(responseJson, CTIVoiceFile.class)).thenReturn(responseVoiceFile);
                when(httpUtils.get(anyString())).thenReturn(responseJson);

                OutputResponse result = ctiServiceImpl.createVoiceFile(requestJson, TEST_IP);
                assertNotNull(result);
                assertNotNull(result.getData());
            }
        }
    }

    @Test
    void testBlockNumber_Success() throws Exception {
        try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
            configMock.when(() -> ConfigProperties.getPropertyByName("block-api-URL"))
                    .thenReturn("http://CTI_SERVER/api/number/block?mobile=MOBILE&campaign=CAMPAIGN_NAME");
            configMock.when(() -> ConfigProperties.getPropertyByName("cti-server-ip"))
                    .thenReturn(TEST_SERVER_URL);

            BlockUnblockNumber blockNumber = new BlockUnblockNumber();
            blockNumber.setPhoneNo("9999999999");
            blockNumber.setCampaignName(TEST_AGENT_ID);
            String requestJson = "{\"phoneNo\":\"9999999999\",\"campaignName\":\"" + TEST_AGENT_ID + "\"}";

            CTIResponse ctiResponse = new CTIResponse();
            ctiResponse.setResponse_code("1");
            ctiResponse.setStatus("SUCCESS");

            BlockUnblockNumber responseBlock = new BlockUnblockNumber();
            responseBlock.setResponse(ctiResponse);
            String responseJson = "{\"response\":{\"response_code\":\"1\",\"status\":\"SUCCESS\"}}";

            try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class)) {
                InputMapper mockInputMapper = mock(InputMapper.class);
                inputMapperMock.when(() -> InputMapper.gson()).thenReturn(mockInputMapper);
                when(mockInputMapper.fromJson(responseJson, BlockUnblockNumber.class)).thenReturn(responseBlock);
                when(httpUtils.get(anyString())).thenReturn(responseJson);

                CTIServiceImpl spyService = spy(ctiServiceImpl);
                doReturn("192.168.1.100").when(spyService).getAgentIP(anyString());

                OutputResponse result = spyService.blockNumber(requestJson, TEST_IP);
                assertNotNull(result);
                assertNotNull(result.getData());
            }
        }
    }

    @Test
    void testBlockNumber_Failure() throws Exception {
        try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
            configMock.when(() -> ConfigProperties.getPropertyByName("block-api-URL"))
                    .thenReturn("http://CTI_SERVER/api/number/block?mobile=MOBILE&campaign=CAMPAIGN_NAME");
            configMock.when(() -> ConfigProperties.getPropertyByName("cti-server-ip"))
                    .thenReturn(TEST_SERVER_URL);

            BlockUnblockNumber blockNumber = new BlockUnblockNumber();
            blockNumber.setPhoneNo("9999999999");
            blockNumber.setCampaignName(TEST_AGENT_ID);
            String requestJson = "{\"phoneNo\":\"9999999999\",\"campaignName\":\"" + TEST_AGENT_ID + "\"}";

            CTIResponse ctiResponse = new CTIResponse();
            ctiResponse.setResponse_code("0");
            ctiResponse.setStatus("FAILURE");
            ctiResponse.setReason("Failed to block number");

            BlockUnblockNumber responseBlock = new BlockUnblockNumber();
            responseBlock.setResponse(ctiResponse);
            String responseJson = "{\"response\":{\"response_code\":\"0\",\"status\":\"FAILURE\",\"reason\":\"Failed to block number\"}}";

            try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class)) {
                InputMapper mockInputMapper = mock(InputMapper.class);
                inputMapperMock.when(() -> InputMapper.gson()).thenReturn(mockInputMapper);
                when(mockInputMapper.fromJson(responseJson, BlockUnblockNumber.class)).thenReturn(responseBlock);
                when(httpUtils.get(anyString())).thenReturn(responseJson);

                CTIServiceImpl spyService = spy(ctiServiceImpl);
                doReturn("192.168.1.100").when(spyService).getAgentIP(anyString());

                OutputResponse result = spyService.blockNumber(requestJson, TEST_IP);
                assertNotNull(result);
                assertEquals("Failed to block number", result.getErrorMessage());
            }
        }
    }

    @Test
    void testUnblockNumber_Success() throws Exception {
        try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
            configMock.when(() -> ConfigProperties.getPropertyByName("unblock-api-URL"))
                    .thenReturn("http://CTI_SERVER/api/number/unblock?mobile=MOBILE&campaign=CAMPAIGN_NAME");
            configMock.when(() -> ConfigProperties.getPropertyByName("cti-server-ip"))
                    .thenReturn(TEST_SERVER_URL);

            BlockUnblockNumber unblockNumber = new BlockUnblockNumber();
            unblockNumber.setPhoneNo("8888888888");
            unblockNumber.setCampaignName(TEST_AGENT_ID);
            String requestJson = "{\"phoneNo\":\"8888888888\",\"campaignName\":\"" + TEST_AGENT_ID + "\"}";

            CTIResponse ctiResponse = new CTIResponse();
            ctiResponse.setResponse_code("1");
            ctiResponse.setStatus("SUCCESS");

            BlockUnblockNumber responseUnblock = new BlockUnblockNumber();
            responseUnblock.setResponse(ctiResponse);
            String responseJson = "{\"response\":{\"response_code\":\"1\",\"status\":\"SUCCESS\"}}";

            try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class)) {
                InputMapper mockInputMapper = mock(InputMapper.class);
                inputMapperMock.when(() -> InputMapper.gson()).thenReturn(mockInputMapper);
                when(mockInputMapper.fromJson(responseJson, BlockUnblockNumber.class)).thenReturn(responseUnblock);
                when(httpUtils.get(anyString())).thenReturn(responseJson);

                CTIServiceImpl spyService = spy(ctiServiceImpl);
                doReturn("192.168.1.100").when(spyService).getAgentIP(anyString());

                OutputResponse result = spyService.unblockNumber(requestJson, TEST_IP);
                assertNotNull(result);
                assertNotNull(result.getData());
            }
        }
    }

    @Test
    void testUnblockNumber_Failure() throws Exception {
        try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
            configMock.when(() -> ConfigProperties.getPropertyByName("unblock-api-URL"))
                    .thenReturn("http://CTI_SERVER/api/number/unblock?mobile=MOBILE&campaign=CAMPAIGN_NAME");
            configMock.when(() -> ConfigProperties.getPropertyByName("cti-server-ip"))
                    .thenReturn(TEST_SERVER_URL);

            BlockUnblockNumber unblockNumber = new BlockUnblockNumber();
            unblockNumber.setPhoneNo("8888888888");
            unblockNumber.setCampaignName(TEST_AGENT_ID);
            String requestJson = "{\"phoneNo\":\"8888888888\",\"campaignName\":\"" + TEST_AGENT_ID + "\"}";

            CTIResponse ctiResponse = new CTIResponse();
            ctiResponse.setResponse_code("0");
            ctiResponse.setStatus("FAILURE");
            ctiResponse.setReason("Failed to unblock number");

            BlockUnblockNumber responseUnblock = new BlockUnblockNumber();
            responseUnblock.setResponse(ctiResponse);
            String responseJson = "{\"response\":{\"response_code\":\"0\",\"status\":\"FAILURE\",\"reason\":\"Failed to unblock number\"}}";

            try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class)) {
                InputMapper mockInputMapper = mock(InputMapper.class);
                inputMapperMock.when(() -> InputMapper.gson()).thenReturn(mockInputMapper);
                when(mockInputMapper.fromJson(responseJson, BlockUnblockNumber.class)).thenReturn(responseUnblock);
                when(httpUtils.get(anyString())).thenReturn(responseJson);

                CTIServiceImpl spyService = spy(ctiServiceImpl);
                doReturn("192.168.1.100").when(spyService).getAgentIP(anyString());

                OutputResponse result = spyService.unblockNumber(requestJson, TEST_IP);
                assertNotNull(result);
                assertEquals("Failed to unblock number", result.getErrorMessage());
            }
        }
    }

    @Test
    void testGetAvailableAgentSkills_Success() throws Exception {
        try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
            configMock.when(() -> ConfigProperties.getPropertyByName("get-available-agents-URL"))
                    .thenReturn("http://CTI_SERVER/api/agent/skills/available?skill=SKILL&campaign=CAMPAIGN_NAME");
            configMock.when(() -> ConfigProperties.getPropertyByName("cti-server-ip"))
                    .thenReturn(TEST_SERVER_URL);

            AgentSkills agentSkills = new AgentSkills();
            agentSkills.setAgent_id(TEST_AGENT_ID);
            String requestJson = "{\"agent_id\":\"" + TEST_AGENT_ID + "\"}";

            CTIResponse ctiResponse = new CTIResponse();
            ctiResponse.setResponse_code("1");
            ctiResponse.setStatus("SUCCESS");

            AgentSkills responseSkills = new AgentSkills();
            responseSkills.setResponse(ctiResponse);
            String responseJson = "{\"response\":{\"response_code\":\"1\",\"status\":\"SUCCESS\"}}";

            try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class)) {
                InputMapper mockInputMapper = mock(InputMapper.class);
                inputMapperMock.when(() -> InputMapper.gson()).thenReturn(mockInputMapper);
                when(mockInputMapper.fromJson(responseJson, AgentSkills.class)).thenReturn(responseSkills);
                when(httpUtils.get(anyString())).thenReturn(responseJson);

                CTIServiceImpl spyService = spy(ctiServiceImpl);
                doReturn("192.168.1.100").when(spyService).getAgentIP(TEST_AGENT_ID);

                OutputResponse result = spyService.getAvailableAgentSkills(requestJson, TEST_IP);
                assertNotNull(result);
                assertNotNull(result.getData());
            }
        }
    }

    @Test
    void testGetAvailableAgentSkills_Failure() throws Exception {
        try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
            configMock.when(() -> ConfigProperties.getPropertyByName("get-available-agents-URL"))
                    .thenReturn("http://CTI_SERVER/api/agent/skills/available?skill=SKILL&campaign=CAMPAIGN_NAME");
            configMock.when(() -> ConfigProperties.getPropertyByName("cti-server-ip"))
                    .thenReturn(TEST_SERVER_URL);

            AgentSkills agentSkills = new AgentSkills();
            agentSkills.setAgent_id(TEST_AGENT_ID);
            String requestJson = "{\"agent_id\":\"" + TEST_AGENT_ID + "\"}";

            CTIResponse ctiResponse = new CTIResponse();
            ctiResponse.setResponse_code("0");
            ctiResponse.setStatus("FAILURE");
            ctiResponse.setReason("No skills available");

            AgentSkills responseSkills = new AgentSkills();
            responseSkills.setResponse(ctiResponse);
            String responseJson = "{\"response\":{\"response_code\":\"0\",\"status\":\"FAILURE\",\"reason\":\"No skills available\"}}";

            try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class)) {
                InputMapper mockInputMapper = mock(InputMapper.class);
                inputMapperMock.when(() -> InputMapper.gson()).thenReturn(mockInputMapper);
                when(mockInputMapper.fromJson(responseJson, AgentSkills.class)).thenReturn(responseSkills);
                when(httpUtils.get(anyString())).thenReturn(responseJson);

                CTIServiceImpl spyService = spy(ctiServiceImpl);
                doReturn("192.168.1.100").when(spyService).getAgentIP(TEST_AGENT_ID);

                OutputResponse result = spyService.getAvailableAgentSkills(requestJson, TEST_IP);
                assertNotNull(result);
                assertEquals("No skills available", result.getErrorMessage());
            }
        }
    }

    @Test
    void testGetOnlineAgents_Failure() throws Exception {
        try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
            configMock.when(() -> ConfigProperties.getPropertyByName("do-online-agent-URL"))
                    .thenReturn("http://CTI_SERVER/api/agents/online?agent=AGENT_ID&ip=AGENT_IP");
            configMock.when(() -> ConfigProperties.getPropertyByName("cti-server-ip"))
                    .thenReturn(TEST_SERVER_URL);
            configMock.when(() -> ConfigProperties.getPropertyByName("get-agent-ip-address-URL"))
                    .thenReturn("http://CTI_SERVER/api/agentip?agent=AGENT_ID");

            AgentState agentState = new AgentState();
            agentState.setAgent_id(TEST_AGENT_ID);
            String requestJson = "{\"agent_id\":\"" + TEST_AGENT_ID + "\"}";

            CTIResponseTemp ctiResponse = new CTIResponseTemp();
            ctiResponse.setResponse_code("0");
            ctiResponse.setStatus("FAILURE");
            ctiResponse.setReason("No agents online");

            AgentState responseState = new AgentState();
            responseState.setResponse(ctiResponse);
            String responseJson = "{\"response\":{\"response_code\":\"0\",\"status\":\"FAILURE\",\"reason\":\"No agents online\"}}";

            try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class)) {
                InputMapper mockInputMapper = mock(InputMapper.class);
                inputMapperMock.when(() -> InputMapper.gson()).thenReturn(mockInputMapper);
                when(mockInputMapper.fromJson(responseJson, AgentState.class)).thenReturn(responseState);
                
                // Mock getAgentIP call response and AgentState for it
                String agentIPResponse = "{\"response\":{\"response_code\":\"1\",\"status\":\"SUCCESS\",\"agent_ip\":\"192.168.1.100\"}}";
                CTIResponseTemp agentIPCtiResponse = new CTIResponseTemp();
                agentIPCtiResponse.setResponse_code("1");
                agentIPCtiResponse.setStatus("SUCCESS");
                agentIPCtiResponse.setAgent_ip("192.168.1.100");
                AgentState agentIPState = new AgentState();
                agentIPState.setResponse(agentIPCtiResponse);
                
                when(mockInputMapper.fromJson(agentIPResponse, AgentState.class)).thenReturn(agentIPState);
                when(httpUtils.get(contains("agentip"))).thenReturn(agentIPResponse);
                when(httpUtils.get(contains("online"))).thenReturn(responseJson);

                OutputResponse result = ctiServiceImpl.getOnlineAgents(requestJson, TEST_IP);
                assertNotNull(result);
                assertEquals("No agents online", result.getErrorMessage());
            }
        }
    }

    @Test
    void testGetCampaignNames_Success() throws Exception {
        try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
            configMock.when(() -> ConfigProperties.getPropertyByName("get-campaign-name-URL"))
                    .thenReturn("http://CTI_SERVER/api/campaigns?search=SEARCH_KEY&type=CAMPAIGN_TYPE&ip=AGENT_IP");
            configMock.when(() -> ConfigProperties.getPropertyByName("cti-server-ip"))
                    .thenReturn(TEST_SERVER_URL);

            CampaignNames campaignNames = new CampaignNames();
            campaignNames.setServiceName("104");
            campaignNames.setType("INBOUND");

            String requestJson = "{\"serviceName\":\"104\",\"type\":\"INBOUND\"}";

            CTIResponseTemp ctiResponse = new CTIResponseTemp();
            ctiResponse.setResponse_code("1");
            ctiResponse.setStatus("SUCCESS");

            CampaignNames responseCampaigns = new CampaignNames();
            responseCampaigns.setResponse(ctiResponse);
            String responseJson = "{\"response\":{\"response_code\":\"1\",\"status\":\"SUCCESS\"}}";

            try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class)) {
                InputMapper mockInputMapper = mock(InputMapper.class);
                inputMapperMock.when(() -> InputMapper.gson()).thenReturn(mockInputMapper);
                when(mockInputMapper.fromJson(responseJson, CampaignNames.class)).thenReturn(responseCampaigns);
                when(httpUtils.get(anyString())).thenReturn(responseJson);

                OutputResponse result = ctiServiceImpl.getCampaignNames(requestJson, TEST_IP);
                assertNotNull(result);
                assertNotNull(result.getData());
            }
        }
    }

    @Test
    void testGetCampaignNames_Failure() throws Exception {
        try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
            configMock.when(() -> ConfigProperties.getPropertyByName("get-campaign-name-URL"))
                    .thenReturn("http://CTI_SERVER/api/campaigns?search=SEARCH_KEY&type=CAMPAIGN_TYPE&ip=AGENT_IP");
            configMock.when(() -> ConfigProperties.getPropertyByName("cti-server-ip"))
                    .thenReturn(TEST_SERVER_URL);

            CampaignNames campaignNames = new CampaignNames();
            campaignNames.setServiceName("INVALID");
            campaignNames.setType("UNKNOWN");

            String requestJson = "{\"serviceName\":\"INVALID\",\"type\":\"UNKNOWN\"}";

            CTIResponseTemp ctiResponse = new CTIResponseTemp();
            ctiResponse.setResponse_code("0");
            ctiResponse.setStatus("FAILURE");
            ctiResponse.setReason("No campaigns found");

            CampaignNames responseCampaigns = new CampaignNames();
            responseCampaigns.setResponse(ctiResponse);
            String responseJson = "{\"response\":{\"response_code\":\"0\",\"status\":\"FAILURE\",\"reason\":\"No campaigns found\"}}";

            try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class)) {
                InputMapper mockInputMapper = mock(InputMapper.class);
                inputMapperMock.when(() -> InputMapper.gson()).thenReturn(mockInputMapper);
                when(mockInputMapper.fromJson(responseJson, CampaignNames.class)).thenReturn(responseCampaigns);
                when(httpUtils.get(anyString())).thenReturn(responseJson);

                OutputResponse result = ctiServiceImpl.getCampaignNames(requestJson, TEST_IP);
                assertNotNull(result);
                assertEquals("No campaigns found", result.getErrorMessage());
            }
        }
    }

    @Test
    void testGetLoginKey_Success() throws Exception {
        try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
            configMock.when(() -> ConfigProperties.getPropertyByName("get-login-key-URL"))
                    .thenReturn("http://CTI_SERVER/api/login?username=USERNAME&password=PASSWORD&ip=AGENT_IP");
            configMock.when(() -> ConfigProperties.getPropertyByName("cti-server-ip"))
                    .thenReturn(TEST_SERVER_URL);

            AgentLoginKey loginKey = new AgentLoginKey();
            loginKey.setUsername("testuser");
            loginKey.setPassword("testpass");

            String requestJson = "{\"username\":\"testuser\",\"password\":\"testpass\"}";

            CTIResponse ctiResponse = new CTIResponse();
            ctiResponse.setResponse_code("1");
            ctiResponse.setStatus("SUCCESS");
            ctiResponse.setLogin_key("123456789");

            AgentLoginKey responseLogin = new AgentLoginKey();
            responseLogin.setResponse(ctiResponse);
            String responseJson = "{\"response\":{\"response_code\":\"1\",\"status\":\"SUCCESS\",\"login_key\":\"123456789\"}}";

            try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class)) {
                InputMapper mockInputMapper = mock(InputMapper.class);
                inputMapperMock.when(() -> InputMapper.gson()).thenReturn(mockInputMapper);
                when(mockInputMapper.fromJson(responseJson, AgentLoginKey.class)).thenReturn(responseLogin);
                when(httpUtils.get(anyString())).thenReturn(responseJson);

                OutputResponse result = ctiServiceImpl.getLoginKey(requestJson, TEST_IP);
                assertNotNull(result);
                assertNotNull(result.getData());
            }
        }
    }

    @Test
    void testGetLoginKey_Failure() throws Exception {
        try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
            configMock.when(() -> ConfigProperties.getPropertyByName("get-login-key-URL"))
                    .thenReturn("http://CTI_SERVER/api/login?username=USERNAME&password=PASSWORD&ip=AGENT_IP");
            configMock.when(() -> ConfigProperties.getPropertyByName("cti-server-ip"))
                    .thenReturn(TEST_SERVER_URL);

            AgentLoginKey loginKey = new AgentLoginKey();
            loginKey.setUsername("invalid");
            loginKey.setPassword("invalid");

            String requestJson = "{\"username\":\"invalid\",\"password\":\"invalid\"}";

            CTIResponse ctiResponse = new CTIResponse();
            ctiResponse.setResponse_code("0");
            ctiResponse.setStatus("FAILURE");
            ctiResponse.setReason("Invalid credentials");

            AgentLoginKey responseLogin = new AgentLoginKey();
            responseLogin.setResponse(ctiResponse);
            String responseJson = "{\"response\":{\"response_code\":\"0\",\"status\":\"FAILURE\",\"reason\":\"Invalid credentials\"}}";

            try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class)) {
                InputMapper mockInputMapper = mock(InputMapper.class);
                inputMapperMock.when(() -> InputMapper.gson()).thenReturn(mockInputMapper);
                when(mockInputMapper.fromJson(responseJson, AgentLoginKey.class)).thenReturn(responseLogin);
                when(httpUtils.get(anyString())).thenReturn(responseJson);

                OutputResponse result = ctiServiceImpl.getLoginKey(requestJson, TEST_IP);
                assertNotNull(result);
                assertEquals("Invalid credentials", result.getErrorMessage());
            }
        }
    }

    @Test
    void testGetTransferCampaigns_Success() throws Exception {
        try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
            configMock.when(() -> ConfigProperties.getPropertyByName("fetch-transferrable-campaigns-URL"))
                    .thenReturn("http://CTI_SERVER/api/campaigns/transfer?agent=AGENT_ID&ip=AGENT_IP");
            configMock.when(() -> ConfigProperties.getPropertyByName("cti-server-ip"))
                    .thenReturn(TEST_SERVER_URL);

            CTICampaigns campaigns = new CTICampaigns();
            campaigns.setAgent_id(TEST_AGENT_ID);

            String requestJson = "{\"agent_id\":\"" + TEST_AGENT_ID + "\"}";

            CTIResponseTemp ctiResponse = new CTIResponseTemp();
            ctiResponse.setResponse_code("1");
            ctiResponse.setStatus("SUCCESS");
            // Mock campaign list - create a JsonArray instead
            com.google.gson.JsonArray campaignArray = new com.google.gson.JsonArray();
            campaignArray.add("Campaign1");
            campaignArray.add("Campaign2");
            ctiResponse.setCampaign(campaignArray);

            CTICampaigns responseCampaigns = new CTICampaigns();
            responseCampaigns.setResponse(ctiResponse);
            String responseJson = "{\"response\":{\"response_code\":\"1\",\"status\":\"SUCCESS\",\"campaign\":[\"Campaign1\",\"Campaign2\"]}}";

            try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class)) {
                InputMapper mockInputMapper = mock(InputMapper.class);
                inputMapperMock.when(() -> InputMapper.gson()).thenReturn(mockInputMapper);
                when(mockInputMapper.fromJson(responseJson, CTICampaigns.class)).thenReturn(responseCampaigns);
                when(httpUtils.get(anyString())).thenReturn(responseJson);

                CTIServiceImpl spyService = spy(ctiServiceImpl);
                doReturn("192.168.1.100").when(spyService).getAgentIP(TEST_AGENT_ID);

                OutputResponse result = spyService.getTransferCampaigns(requestJson, TEST_IP);
                assertNotNull(result);
                assertNotNull(result.getData());
            }
        }
    }

    @Test
    void testGetTransferCampaigns_Failure() throws Exception {
        try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
            configMock.when(() -> ConfigProperties.getPropertyByName("fetch-transferrable-campaigns-URL"))
                    .thenReturn("http://CTI_SERVER/api/campaigns/transfer?agent=AGENT_ID&ip=AGENT_IP");
            configMock.when(() -> ConfigProperties.getPropertyByName("cti-server-ip"))
                    .thenReturn(TEST_SERVER_URL);

            CTICampaigns campaigns = new CTICampaigns();
            campaigns.setAgent_id(TEST_AGENT_ID);

            String requestJson = "{\"agent_id\":\"" + TEST_AGENT_ID + "\"}";

            CTIResponseTemp ctiResponse = new CTIResponseTemp();
            ctiResponse.setResponse_code("0");
            ctiResponse.setStatus("FAILURE");
            ctiResponse.setReason("No transferrable campaigns");
            // Empty campaign list for failure - create empty JsonArray
            com.google.gson.JsonArray emptyCampaignArray = new com.google.gson.JsonArray();
            ctiResponse.setCampaign(emptyCampaignArray);

            CTICampaigns responseCampaigns = new CTICampaigns();
            responseCampaigns.setResponse(ctiResponse);
            String responseJson = "{\"response\":{\"response_code\":\"0\",\"status\":\"FAILURE\",\"reason\":\"No transferrable campaigns\",\"campaign\":[]}}";

            try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class)) {
                InputMapper mockInputMapper = mock(InputMapper.class);
                inputMapperMock.when(() -> InputMapper.gson()).thenReturn(mockInputMapper);
                when(mockInputMapper.fromJson(responseJson, CTICampaigns.class)).thenReturn(responseCampaigns);
                when(httpUtils.get(anyString())).thenReturn(responseJson);

                CTIServiceImpl spyService = spy(ctiServiceImpl);
                doReturn("192.168.1.100").when(spyService).getAgentIP(TEST_AGENT_ID);

                OutputResponse result = spyService.getTransferCampaigns(requestJson, TEST_IP);
                assertNotNull(result);
                assertEquals("No Campaigns Available", result.getErrorMessage());
            }
        }
    }



// ...existing code...
}
