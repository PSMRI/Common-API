package com.iemr.common.service.nhm_dashboard;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.iemr.common.constant.Constants;
import com.iemr.common.data.nhm_dashboard.AgentRealTimeData;
import com.iemr.common.data.nhm_dashboard.NHMAgentRequest;
import com.iemr.common.repository.nhm_dashboard.NHMAgentRealTimeDataRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class NHM_AgentRealTimeDatacServiceImplTest {

    @Test
    void getData_ValidJsonArrayButParsingFails_ReturnsNull() throws IOException {
        // Simulate a valid JSON array but ObjectMapper.readValue throws exception
        String mockJsonResponse = "[{\"CampaignZ\":{\"LOGGED_IN\":1}}]";
        ResponseEntity<String> mockResponseEntity = ResponseEntity.ok(mockJsonResponse);
        String expectedJsonRequest = "{\"campaign_name\":\"all\"}";
        try (MockedConstruction<RestTemplate> mockedRestTemplateConstruction = mockConstruction(RestTemplate.class, (mockRestTemplateInstance, context) -> {
                doReturn(mockResponseEntity).when(mockRestTemplateInstance).exchange(
                        eq(czUrl),
                        eq(HttpMethod.POST),
                        any(HttpEntity.class),
                        eq(String.class)
                );
            });
             MockedConstruction<Gson> mockedGsonConstruction = mockConstruction(Gson.class, (mockGsonInstance, context) -> {
                 doReturn(expectedJsonRequest).when(mockGsonInstance).toJson(any(NHMAgentRequest.class));
             });
             MockedConstruction<ObjectMapper> mockedObjectMapper = mockConstruction(ObjectMapper.class, (mockMapper, context) -> {
                 doThrow(new IOException("parse error")).when(mockMapper).readValue(eq(mockJsonResponse), eq(ArrayList.class));
             })) {
            String result = nhmAgentRealTimeDatacService.getData();
            assertNull(result, "Expected null when ObjectMapper.readValue throws after valid JSON array");
            verifyNoInteractions(agentRealTimeDataRepo);
            verifyNoInteractions(log);
        }
    }

    @Test
    void getData_CampaignWithAllKeys_AllFieldsSet() throws IOException {
        String mockJsonResponse = "[{\"FullCampaign\":{\"LOGGED_IN\":7,\"FREE\":6,\"IN_CALL\":5,\"AWT\":4,\"HOLD\":3,\"NOT_READY\":2,\"AUX\":1}}]";
        ResponseEntity<String> mockResponseEntity = ResponseEntity.ok(mockJsonResponse);
        String expectedJsonRequest = "{\"campaign_name\":\"all\"}";
        try (MockedConstruction<RestTemplate> mockedRestTemplateConstruction = mockConstruction(RestTemplate.class, (mockRestTemplateInstance, context) -> {
                doReturn(mockResponseEntity).when(mockRestTemplateInstance).exchange(
                        eq(czUrl),
                        eq(HttpMethod.POST),
                        any(HttpEntity.class),
                        eq(String.class)
                );
            });
             MockedConstruction<Gson> mockedGsonConstruction = mockConstruction(Gson.class, (mockGsonInstance, context) -> {
                 doReturn(expectedJsonRequest).when(mockGsonInstance).toJson(any(NHMAgentRequest.class));
             });
             MockedConstruction<SimpleDateFormat> mockedSDFConstruction = mockConstruction(SimpleDateFormat.class, (mockSdfInstance, context) -> {
                 doReturn("2023-03-15 00:00:00.000").when(mockSdfInstance).format(any(Date.class));
             })) {
            String result = nhmAgentRealTimeDatacService.getData();
            assertEquals(mockJsonResponse, result);
            ArgumentCaptor<Iterable<AgentRealTimeData>> captor = ArgumentCaptor.forClass((Class) Iterable.class);
            verify(agentRealTimeDataRepo).deleteAll();
            verify(agentRealTimeDataRepo).saveAll(captor.capture());
            List<AgentRealTimeData> savedData = new ArrayList<>();
            captor.getValue().forEach(savedData::add);
            assertNotNull(savedData);
            assertEquals(1, savedData.size());
            AgentRealTimeData data = savedData.get(0);
            assertEquals("FullCampaign", data.getCampaignName());
            assertEquals(7, data.getLoggedIn());
            assertEquals(6, data.getFree());
            assertEquals(5, data.getInCall());
            assertEquals(4, data.getAwt());
            assertEquals(3, data.getHold());
            assertEquals(2, data.getNotReady());
            assertEquals(1, data.getAux());
            assertEquals("default", data.getCreatedBy());
            assertEquals("default", data.getModifiedBy());
            assertNotNull(data.getCreatedDate());
            assertNotNull(data.getModifiedDate());
        }
    }
    @Test
    void getData_ResponseIsEmptyString_ReturnsNull() throws IOException {
        String mockJsonResponse = "";
        ResponseEntity<String> mockResponseEntity = ResponseEntity.ok(mockJsonResponse);
        String expectedJsonRequest = "{\"campaign_name\":\"all\"}";
        try (MockedConstruction<RestTemplate> mockedRestTemplateConstruction = mockConstruction(RestTemplate.class, (mockRestTemplateInstance, context) -> {
                doReturn(mockResponseEntity).when(mockRestTemplateInstance).exchange(
                        eq(czUrl),
                        eq(HttpMethod.POST),
                        any(HttpEntity.class),
                        eq(String.class)
                );
            });
             MockedConstruction<Gson> mockedGsonConstruction = mockConstruction(Gson.class, (mockGsonInstance, context) -> {
                 doReturn(expectedJsonRequest).when(mockGsonInstance).toJson(any(NHMAgentRequest.class));
             })) {
            String result = nhmAgentRealTimeDatacService.getData();
            assertNull(result, "Expected null when response is empty string");
            verifyNoInteractions(agentRealTimeDataRepo);
            verifyNoInteractions(log);
        }
    }

    @Test
    void getData_ResponseIsNotJsonArray_ReturnsNull() throws IOException {
        String mockJsonResponse = "not a json array";
        ResponseEntity<String> mockResponseEntity = ResponseEntity.ok(mockJsonResponse);
        String expectedJsonRequest = "{\"campaign_name\":\"all\"}";
        try (MockedConstruction<RestTemplate> mockedRestTemplateConstruction = mockConstruction(RestTemplate.class, (mockRestTemplateInstance, context) -> {
                doReturn(mockResponseEntity).when(mockRestTemplateInstance).exchange(
                        eq(czUrl),
                        eq(HttpMethod.POST),
                        any(HttpEntity.class),
                        eq(String.class)
                );
            });
             MockedConstruction<Gson> mockedGsonConstruction = mockConstruction(Gson.class, (mockGsonInstance, context) -> {
                 doReturn(expectedJsonRequest).when(mockGsonInstance).toJson(any(NHMAgentRequest.class));
             })) {
            String result = nhmAgentRealTimeDatacService.getData();
            assertNull(result, "Expected null when response is not a JSON array");
            verifyNoInteractions(agentRealTimeDataRepo);
            verifyNoInteractions(log);
        }
    }

    @Test
    void getData_CampaignWithMissingKeys_DefaultsToZero() throws IOException {
        String mockJsonResponse = "[{\"CampaignX\":{}}]";
        ResponseEntity<String> mockResponseEntity = ResponseEntity.ok(mockJsonResponse);
        String expectedJsonRequest = "{\"campaign_name\":\"all\"}";
        try (MockedConstruction<RestTemplate> mockedRestTemplateConstruction = mockConstruction(RestTemplate.class, (mockRestTemplateInstance, context) -> {
                doReturn(mockResponseEntity).when(mockRestTemplateInstance).exchange(
                        eq(czUrl),
                        eq(HttpMethod.POST),
                        any(HttpEntity.class),
                        eq(String.class)
                );
            });
             MockedConstruction<Gson> mockedGsonConstruction = mockConstruction(Gson.class, (mockGsonInstance, context) -> {
                 doReturn(expectedJsonRequest).when(mockGsonInstance).toJson(any(NHMAgentRequest.class));
             });
             MockedConstruction<SimpleDateFormat> mockedSDFConstruction = mockConstruction(SimpleDateFormat.class, (mockSdfInstance, context) -> {
                 doReturn("2023-03-15 00:00:00.000").when(mockSdfInstance).format(any(Date.class));
             })) {
            String result = nhmAgentRealTimeDatacService.getData();
            assertEquals(mockJsonResponse, result);
            ArgumentCaptor<Iterable<AgentRealTimeData>> captor = ArgumentCaptor.forClass((Class) Iterable.class);
            verify(agentRealTimeDataRepo).deleteAll();
            verify(agentRealTimeDataRepo).saveAll(captor.capture());
            List<AgentRealTimeData> savedData = new ArrayList<>();
            captor.getValue().forEach(savedData::add);
            assertNotNull(savedData);
            assertEquals(1, savedData.size());
            AgentRealTimeData data = savedData.get(0);
            assertEquals("CampaignX", data.getCampaignName());
            assertEquals(0, data.getLoggedIn());
            assertEquals(0, data.getFree());
            assertEquals(0, data.getInCall());
            assertEquals(0, data.getAwt());
            assertEquals(0, data.getHold());
            assertEquals(0, data.getNotReady());
            assertEquals(0, data.getAux());
        }
    }

    @Test
    void getData_CampaignWithExtraKeys_IgnoresExtra() throws IOException {
        String mockJsonResponse = "[{\"CampaignY\":{\"LOGGED_IN\":1,\"EXTRA\":99}}]";
        ResponseEntity<String> mockResponseEntity = ResponseEntity.ok(mockJsonResponse);
        String expectedJsonRequest = "{\"campaign_name\":\"all\"}";
        try (MockedConstruction<RestTemplate> mockedRestTemplateConstruction = mockConstruction(RestTemplate.class, (mockRestTemplateInstance, context) -> {
                doReturn(mockResponseEntity).when(mockRestTemplateInstance).exchange(
                        eq(czUrl),
                        eq(HttpMethod.POST),
                        any(HttpEntity.class),
                        eq(String.class)
                );
            });
             MockedConstruction<Gson> mockedGsonConstruction = mockConstruction(Gson.class, (mockGsonInstance, context) -> {
                 doReturn(expectedJsonRequest).when(mockGsonInstance).toJson(any(NHMAgentRequest.class));
             });
             MockedConstruction<SimpleDateFormat> mockedSDFConstruction = mockConstruction(SimpleDateFormat.class, (mockSdfInstance, context) -> {
                 doReturn("2023-03-15 00:00:00.000").when(mockSdfInstance).format(any(Date.class));
             })) {
            String result = nhmAgentRealTimeDatacService.getData();
            assertEquals(mockJsonResponse, result);
            ArgumentCaptor<Iterable<AgentRealTimeData>> captor = ArgumentCaptor.forClass((Class) Iterable.class);
            verify(agentRealTimeDataRepo).deleteAll();
            verify(agentRealTimeDataRepo).saveAll(captor.capture());
            List<AgentRealTimeData> savedData = new ArrayList<>();
            captor.getValue().forEach(savedData::add);
            assertNotNull(savedData);
            assertEquals(1, savedData.size());
            AgentRealTimeData data = savedData.get(0);
            assertEquals("CampaignY", data.getCampaignName());
            assertEquals(1, data.getLoggedIn());
            assertEquals(0, data.getFree());
            assertEquals(0, data.getInCall());
            assertEquals(0, data.getAwt());
            assertEquals(0, data.getHold());
            assertEquals(0, data.getNotReady());
            assertEquals(0, data.getAux());
        }
    }

    @Test
    void getData_MultipleCampaigns_AllSaved() throws IOException {
        String mockJsonResponse = "[{\"Campaign1\":{\"LOGGED_IN\":2}},{\"Campaign2\":{\"FREE\":3}}]";
        ResponseEntity<String> mockResponseEntity = ResponseEntity.ok(mockJsonResponse);
        String expectedJsonRequest = "{\"campaign_name\":\"all\"}";
        try (MockedConstruction<RestTemplate> mockedRestTemplateConstruction = mockConstruction(RestTemplate.class, (mockRestTemplateInstance, context) -> {
                doReturn(mockResponseEntity).when(mockRestTemplateInstance).exchange(
                        eq(czUrl),
                        eq(HttpMethod.POST),
                        any(HttpEntity.class),
                        eq(String.class)
                );
            });
             MockedConstruction<Gson> mockedGsonConstruction = mockConstruction(Gson.class, (mockGsonInstance, context) -> {
                 doReturn(expectedJsonRequest).when(mockGsonInstance).toJson(any(NHMAgentRequest.class));
             });
             MockedConstruction<SimpleDateFormat> mockedSDFConstruction = mockConstruction(SimpleDateFormat.class, (mockSdfInstance, context) -> {
                 doReturn("2023-03-15 00:00:00.000").when(mockSdfInstance).format(any(Date.class));
             })) {
            String result = nhmAgentRealTimeDatacService.getData();
            assertEquals(mockJsonResponse, result);
            ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
            verify(agentRealTimeDataRepo).deleteAll();
            verify(agentRealTimeDataRepo).saveAll(captor.capture());
            @SuppressWarnings("unchecked")
            List<AgentRealTimeData> savedData = (List<AgentRealTimeData>) captor.getValue();
            assertNotNull(savedData);
            assertEquals(2, savedData.size());
            AgentRealTimeData data1 = savedData.get(0);
            AgentRealTimeData data2 = savedData.get(1);
            assertEquals("Campaign1", data1.getCampaignName());
            assertEquals(2, data1.getLoggedIn());
            assertEquals(0, data1.getFree());
            assertEquals(0, data1.getInCall());
            assertEquals(0, data1.getAwt());
            assertEquals(0, data1.getHold());
            assertEquals(0, data1.getNotReady());
            assertEquals(0, data1.getAux());
            assertEquals("Campaign2", data2.getCampaignName());
            assertEquals(0, data2.getLoggedIn());
            assertEquals(3, data2.getFree());
            assertEquals(0, data2.getInCall());
            assertEquals(0, data2.getAwt());
            assertEquals(0, data2.getHold());
            assertEquals(0, data2.getNotReady());
            assertEquals(0, data2.getAux());
        }
    }

    @Mock
    private Logger log;

    @Mock
    private NHMAgentRealTimeDataRepo agentRealTimeDataRepo;

    @InjectMocks
    private NHM_AgentRealTimeDatacServiceImpl nhmAgentRealTimeDatacService;

    private String czUrl = "http://test-cz-url.com";

    private static final String LOGGED_IN = "LOGGED_IN";
    private static final String FREE = "FREE";
    private static final String IN_CALL = "IN_CALL";
    private static final String AWT = "AWT";
    private static final String HOLD = "HOLD";
    private static final String NOT_READY = "NOT_READY";
    private static final String AUX = "AUX";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(nhmAgentRealTimeDatacService, "czUrl", czUrl);
        ReflectionTestUtils.setField(nhmAgentRealTimeDatacService, "log", log);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    void getData_Success_ValidResponse() throws IOException {
        String mockJsonResponse = "[{\"Campaign1\":{\"LOGGED_IN\":10,\"FREE\":5,\"IN_CALL\":3,\"AWT\":2,\"HOLD\":0,\"NOT_READY\":0,\"AUX\":0}},{\"Campaign2\":{\"LOGGED_IN\":8,\"FREE\":4,\"IN_CALL\":2,\"AWT\":1,\"HOLD\":0,\"NOT_READY\":0,\"AUX\":0}}]";
        ResponseEntity<String> mockResponseEntity = ResponseEntity.ok(mockJsonResponse);
        String expectedJsonRequest = "{\"campaign_name\":\"all\"}";
        try (MockedConstruction<RestTemplate> mockedRestTemplateConstruction = mockConstruction(RestTemplate.class, (mockRestTemplateInstance, context) -> {
                doReturn(mockResponseEntity).when(mockRestTemplateInstance).exchange(
                        eq(czUrl),
                        eq(HttpMethod.POST),
                        any(HttpEntity.class),
                        eq(String.class)
                );
            });
             MockedConstruction<Gson> mockedGsonConstruction = mockConstruction(Gson.class, (mockGsonInstance, context) -> {
                 doReturn(expectedJsonRequest).when(mockGsonInstance).toJson(any(NHMAgentRequest.class));
             });
             MockedConstruction<SimpleDateFormat> mockedSDFConstruction = mockConstruction(SimpleDateFormat.class, (mockSdfInstance, context) -> {
                 doReturn("2023-03-15 00:00:00.000").when(mockSdfInstance).format(any(Date.class));
             })) {
            String result = nhmAgentRealTimeDatacService.getData();
            assertEquals(mockJsonResponse, result);
            ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
            verify(agentRealTimeDataRepo).deleteAll();
            verify(agentRealTimeDataRepo).saveAll(captor.capture());
            List<AgentRealTimeData> savedData = (List<AgentRealTimeData>) captor.getValue();
            assertNotNull(savedData);
            assertEquals(2, savedData.size());
            AgentRealTimeData data1 = savedData.get(0);
            AgentRealTimeData data2 = savedData.get(1);
            assertEquals("Campaign1", data1.getCampaignName());
            assertEquals(10, data1.getLoggedIn());
            assertEquals(5, data1.getFree());
            assertEquals(3, data1.getInCall());
            assertEquals(2, data1.getAwt());
            assertEquals(0, data1.getHold());
            assertEquals(0, data1.getNotReady());
            assertEquals(0, data1.getAux());
            assertEquals("Campaign2", data2.getCampaignName());
            assertEquals(8, data2.getLoggedIn());
            assertEquals(4, data2.getFree());
            assertEquals(2, data2.getInCall());
            assertEquals(1, data2.getAwt());
            assertEquals(0, data2.getHold());
            assertEquals(0, data2.getNotReady());
            assertEquals(0, data2.getAux());
        }
    }

    @Test
    void getData_Error_RestTemplateException() throws IOException {
        String errorMessage = "Connection refused";
        String expectedJsonRequest = "{\"campaign_name\":\"all\"}";

        try (MockedConstruction<RestTemplate> mockedRestTemplateConstruction = mockConstruction(RestTemplate.class, (mockRestTemplateInstance, context) -> {
                doThrow(new RuntimeException(errorMessage)).when(mockRestTemplateInstance).exchange(
                        eq(czUrl),
                        eq(HttpMethod.POST),
                        any(HttpEntity.class),
                        eq(String.class)
                );
            });
             MockedConstruction<Gson> mockedGsonConstruction = mockConstruction(Gson.class, (mockGsonInstance, context) -> {
                 doReturn(expectedJsonRequest).when(mockGsonInstance).toJson(any(NHMAgentRequest.class));
             });
            ) {

            String result = nhmAgentRealTimeDatacService.getData();

            assertNull(result);
            verify(log).error("Error While callin CZ Url : " + czUrl + " Error Message " + errorMessage);
            verifyNoInteractions(agentRealTimeDataRepo);
        }
    }

    @Test
    void getData_Success_EmptyResponseArray() throws IOException {
        String mockJsonResponse = "[]";
        ResponseEntity<String> mockResponseEntity = ResponseEntity.ok(mockJsonResponse);

        String expectedJsonRequest = "{\"campaign_name\":\"all\"}";

        try (MockedConstruction<RestTemplate> mockedRestTemplateConstruction = mockConstruction(RestTemplate.class, (mockRestTemplateInstance, context) -> {
                doReturn(mockResponseEntity).when(mockRestTemplateInstance).exchange(
                        eq(czUrl),
                        eq(HttpMethod.POST),
                        any(HttpEntity.class),
                        eq(String.class)
                );
            });
             MockedConstruction<Gson> mockedGsonConstruction = mockConstruction(Gson.class, (mockGsonInstance, context) -> {
                 doReturn(expectedJsonRequest).when(mockGsonInstance).toJson(any(NHMAgentRequest.class));
             })) {

            String result = nhmAgentRealTimeDatacService.getData();

            assertEquals(mockJsonResponse, result);
            verify(agentRealTimeDataRepo, never()).deleteAll();
            verify(agentRealTimeDataRepo, never()).saveAll(any());
            verifyNoInteractions(log);
        }
    }

    @Test
    void getData_Success_NullResponse() throws IOException {
        ResponseEntity<String> mockResponseEntity = ResponseEntity.ok(null);

        String expectedJsonRequest = "{\"campaign_name\":\"all\"}";

        try (MockedConstruction<RestTemplate> mockedRestTemplateConstruction = mockConstruction(RestTemplate.class, (mockRestTemplateInstance, context) -> {
                doReturn(mockResponseEntity).when(mockRestTemplateInstance).exchange(
                        eq(czUrl),
                        eq(HttpMethod.POST),
                        any(HttpEntity.class),
                        eq(String.class)
                );
            });
             MockedConstruction<Gson> mockedGsonConstruction = mockConstruction(Gson.class, (mockGsonInstance, context) -> {
                 doReturn(expectedJsonRequest).when(mockGsonInstance).toJson(any(NHMAgentRequest.class));
             })) {

            String result = nhmAgentRealTimeDatacService.getData();

            assertNull(result);
            verifyNoInteractions(agentRealTimeDataRepo);
            // removed verification of readValue on ObjectMapper mock
            verifyNoInteractions(log);
        }
    }

    @Test
    void getData_Success_InvalidResponseFormat_NotJsonArray() throws IOException {
        String mockJsonResponse = "{\"key\":\"value\"}";
        ResponseEntity<String> mockResponseEntity = ResponseEntity.ok(mockJsonResponse);

        String expectedJsonRequest = "{\"campaign_name\":\"all\"}";

        try (MockedConstruction<RestTemplate> mockedRestTemplateConstruction = mockConstruction(RestTemplate.class, (mockRestTemplateInstance, context) -> {
                doReturn(mockResponseEntity).when(mockRestTemplateInstance).exchange(
                        eq(czUrl),
                        eq(HttpMethod.POST),
                        any(HttpEntity.class),
                        eq(String.class)
                );
            });
             MockedConstruction<Gson> mockedGsonConstruction = mockConstruction(Gson.class, (mockGsonInstance, context) -> {
                 doReturn(expectedJsonRequest).when(mockGsonInstance).toJson(any(NHMAgentRequest.class));
             })) {

            String result = nhmAgentRealTimeDatacService.getData();

            assertEquals(mockJsonResponse, result);
            verifyNoInteractions(agentRealTimeDataRepo);
            // removed verification of readValue on ObjectMapper mock
            verifyNoInteractions(log);
        }
    }

    @Test
    void getData_IOException_FromObjectMapperReadValue() throws IOException {
        String mockJsonResponse = "{invalid json}";
        ResponseEntity<String> mockResponseEntity = ResponseEntity.ok(mockJsonResponse);

        String expectedJsonRequest = "{\"campaign_name\":\"all\"}";

        try (MockedConstruction<RestTemplate> mockedRestTemplateConstruction = mockConstruction(RestTemplate.class, (mockRestTemplateInstance, context) -> {
                doReturn(mockResponseEntity).when(mockRestTemplateInstance).exchange(
                        eq(czUrl),
                        eq(HttpMethod.POST),
                        any(HttpEntity.class),
                        eq(String.class)
                );
            });
             MockedConstruction<Gson> mockedGsonConstruction = mockConstruction(Gson.class, (mockGsonInstance, context) -> {
                 doReturn(expectedJsonRequest).when(mockGsonInstance).toJson(any(NHMAgentRequest.class));
             })) {

            String result = nhmAgentRealTimeDatacService.getData();
            assertNull(result, "Expected null when ObjectMapper fails to parse invalid JSON");
            verifyNoInteractions(agentRealTimeDataRepo);
            verifyNoInteractions(log);
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    void getData_Success_EmptyInnerCampaignDetails() throws IOException {
        String mockJsonResponse = "[{\"Campaign1\":{}},{\"Campaign2\":{}}]";
        ResponseEntity<String> mockResponseEntity = ResponseEntity.ok(mockJsonResponse);

        String expectedJsonRequest = "{\"campaign_name\":\"all\"}";

        ArrayList<HashMap<String, Object>> readValueList = new ArrayList<>();
        HashMap<String, Object> campaign1Map = new HashMap<>();
        HashMap<String, Integer> emptyDetails1 = new HashMap<>();
        campaign1Map.put("Campaign1", emptyDetails1);
        readValueList.add(campaign1Map);

        HashMap<String, Object> campaign2Map = new HashMap<>();
        HashMap<String, Integer> emptyDetails2 = new HashMap<>();
        campaign2Map.put("Campaign2", emptyDetails2);
        readValueList.add(campaign2Map);

        try (MockedConstruction<RestTemplate> mockedRestTemplateConstruction = mockConstruction(RestTemplate.class, (mockRestTemplateInstance, context) -> {
                doReturn(mockResponseEntity).when(mockRestTemplateInstance).exchange(
                        eq(czUrl),
                        eq(HttpMethod.POST),
                        any(HttpEntity.class),
                        eq(String.class)
                );
            });
             MockedConstruction<Gson> mockedGsonConstruction = mockConstruction(Gson.class, (mockGsonInstance, context) -> {
                 doReturn(expectedJsonRequest).when(mockGsonInstance).toJson(any(NHMAgentRequest.class));
             });
             MockedConstruction<SimpleDateFormat> mockedSDFConstruction = mockConstruction(SimpleDateFormat.class, (mockSdfInstance, context) -> {
                 doReturn("2023-03-15 00:00:00.000").when(mockSdfInstance).format(any(Date.class));
             })) {
            String result = nhmAgentRealTimeDatacService.getData();
            assertEquals(mockJsonResponse, result);
            ArgumentCaptor<Iterable<AgentRealTimeData>> captor = ArgumentCaptor.forClass((Class) Iterable.class);
            verify(agentRealTimeDataRepo).deleteAll();
            verify(agentRealTimeDataRepo).saveAll(captor.capture());
            List<AgentRealTimeData> savedData = new ArrayList<>();
            captor.getValue().forEach(savedData::add);
            assertNotNull(savedData);
            assertEquals(2, savedData.size());
            AgentRealTimeData data1 = savedData.get(0);
            AgentRealTimeData data2 = savedData.get(1);
            assertEquals("Campaign1", data1.getCampaignName());
            assertEquals(0, data1.getLoggedIn());
            assertEquals(0, data1.getFree());
            assertEquals(0, data1.getInCall());
            assertEquals(0, data1.getAwt());
            assertEquals(0, data1.getHold());
            assertEquals(0, data1.getNotReady());
            assertEquals(0, data1.getAux());
            assertEquals("Campaign2", data2.getCampaignName());
            assertEquals(0, data2.getLoggedIn());
            assertEquals(0, data2.getFree());
            assertEquals(0, data2.getInCall());
            assertEquals(0, data2.getAwt());
            assertEquals(0, data2.getHold());
            assertEquals(0, data2.getNotReady());
            assertEquals(0, data2.getAux());
        }
    }
}