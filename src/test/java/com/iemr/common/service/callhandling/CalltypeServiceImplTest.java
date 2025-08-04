package com.iemr.common.service.callhandling;

import com.iemr.common.data.callhandling.CallType;
import com.iemr.common.repository.callhandling.IEMRCalltypeRepositoryImplCustom;
import com.iemr.common.utils.exception.IEMRException;
import com.iemr.common.utils.mapper.InputMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

@ExtendWith(MockitoExtension.class)
class CalltypeServiceImplTest {
    @InjectMocks
    private CalltypeServiceImpl service;
    @Mock
    private IEMRCalltypeRepositoryImplCustom repo;

    // Use real CallType objects for V1 tests, only mock/stub as needed per test

    @Test
    void getAllCalltypes_inboundOutbound_success() throws Exception {
        String json = "{}";
        CallType inboundOutboundProvider = mock(CallType.class);
        when(inboundOutboundProvider.getProviderServiceMapID()).thenReturn(1);
        when(inboundOutboundProvider.getIsInbound()).thenReturn(true);
        when(inboundOutboundProvider.getIsOutbound()).thenReturn(true);
        try (MockedStatic<InputMapper> inputMapperStatic = Mockito.mockStatic(InputMapper.class)) {
            InputMapper mockInputMapper = mock(InputMapper.class);
            inputMapperStatic.when(InputMapper::gson).thenReturn(mockInputMapper);
            when(mockInputMapper.fromJson(anyString(), eq(CallType.class))).thenReturn(inboundOutboundProvider);
            Set<Object[]> resultSet = new HashSet<>();
            resultSet.add(new Object[]{"type", 1, "desc", "group", true, true, true, true});
            when(repo.getCallTypes(1, true, true)).thenReturn(resultSet);
            List<CallType> result = service.getAllCalltypes(json);
            assertEquals(1, result.size());
        }
    }

    @Test
    void getAllCalltypes_default_success() throws Exception {
        String json = "{}";
        try (MockedStatic<InputMapper> inputMapperStatic = Mockito.mockStatic(InputMapper.class)) {
            InputMapper mockInputMapper = mock(InputMapper.class);
            inputMapperStatic.when(InputMapper::gson).thenReturn(mockInputMapper);
            CallType defaultProvider = mock(CallType.class);
            when(mockInputMapper.fromJson(anyString(), eq(CallType.class))).thenReturn(defaultProvider);
            when(defaultProvider.getProviderServiceMapID()).thenReturn(4);
            Set<Object[]> resultSet = new HashSet<>();
            resultSet.add(new Object[]{"type", 1, "desc", "group", true, true, true, true});
            when(repo.getCallTypes(4, false, false)).thenReturn(resultSet);
            List<CallType> result = service.getAllCalltypes(json);
            assertEquals(1, result.size());
        }
    }

    @Test
    void getAllCalltypes_emptyResult() throws Exception {
        String json = "{}";
        try (MockedStatic<InputMapper> inputMapperStatic = Mockito.mockStatic(InputMapper.class)) {
            InputMapper mockInputMapper = mock(InputMapper.class);
            inputMapperStatic.when(InputMapper::gson).thenReturn(mockInputMapper);
            CallType defaultProvider = mock(CallType.class);
            when(mockInputMapper.fromJson(anyString(), eq(CallType.class))).thenReturn(defaultProvider);
            when(defaultProvider.getProviderServiceMapID()).thenReturn(4);
            when(repo.getCallTypes(4, false, false)).thenReturn(new HashSet<>());
            List<CallType> result = service.getAllCalltypes(json);
            assertTrue(result.isEmpty());
        }
    }

    @Test
    void getAllCalltypesV1_inboundOutbound_success() throws Exception {
        String json = "{\"providerServiceMapID\":1,\"isInbound\":true,\"isOutbound\":true}";
        Set<Object[]> resultSet = new HashSet<>();
        resultSet.add(new Object[]{"type", 1, "desc", "group", true, true, true, true});
        when(repo.getCallTypes(1, true, true)).thenReturn(resultSet);
        String result = service.getAllCalltypesV1(json);
        assertTrue(result.contains("callGroupType"));
    }

    @Test
    void getAllCalltypesV1_inbound_success() throws Exception {
        String json = "{\"providerServiceMapID\":2,\"isInbound\":true}";
        Set<Object[]> resultSet = new HashSet<>();
        resultSet.add(new Object[]{"type", 1, "desc", "group", true, true, true, true});
        when(repo.getInboundCallTypes(2, true)).thenReturn(resultSet);
        String result = service.getAllCalltypesV1(json);
        assertTrue(result.contains("callGroupType"));
    }

    @Test
    void getAllCalltypesV1_outbound_success() throws Exception {
        String json = "{\"providerServiceMapID\":3,\"isOutbound\":true}";
        Set<Object[]> resultSet = new HashSet<>();
        resultSet.add(new Object[]{"type", 1, "desc", "group", true, true, true, true});
        when(repo.getOutboundCallTypes(3, true)).thenReturn(resultSet);
        String result = service.getAllCalltypesV1(json);
        assertTrue(result.contains("callGroupType"));
    }

    @Test
    void getAllCalltypesV1_default_success() throws Exception {
        String json = "{\"providerServiceMapID\":4}";
        Set<Object[]> resultSet = new HashSet<>();
        resultSet.add(new Object[]{"type", 1, "desc", "group", true, true, true, true});
        when(repo.getCallTypes(4)).thenReturn(resultSet);
        String result = service.getAllCalltypesV1(json);
        assertTrue(result.contains("callGroupType"));
    }

    @Test
    void getAllCalltypesV1_emptyResult() throws Exception {
        String json = "{\"providerServiceMapID\":4}";
        when(repo.getCallTypes(4)).thenReturn(new HashSet<>());
        String result = service.getAllCalltypesV1(json);
        assertTrue(result.contains("[]"));
    }
}
