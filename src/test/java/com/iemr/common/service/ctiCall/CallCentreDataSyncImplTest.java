package com.iemr.common.service.ctiCall;

import com.iemr.common.data.callhandling.BeneficiaryCall;
import com.iemr.common.data.report.CTIData;
import com.iemr.common.data.report.CTIResponse;
import com.iemr.common.repository.report.CallReportRepo;
import com.iemr.common.service.cti.CTIService;
import com.iemr.common.utils.config.ConfigProperties;
import com.iemr.common.utils.http.HttpUtils;
import com.iemr.common.utils.mapper.InputMapper;
import com.iemr.common.utils.response.OutputResponse;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CallCentreDataSyncImplTest {
    @InjectMocks
    CallCentreDataSyncImpl service = new CallCentreDataSyncImpl();
    CallCentreDataSyncImpl spyService;

    @Mock
    CallReportRepo callReportRepo;
    @Mock
    CTIService ctiService;
    @Mock
    HttpUtils httpUtils;
    @Mock
    BeneficiaryCall beneficiaryCall;
    @Mock
    OutputResponse outputResponse;
    @Mock
    CTIData ctiData;
    @Mock
    CTIResponse ctiResponse;

    @BeforeEach
    public void setup() {
        ReflectionTestUtils.setField(service, "ctiServerIP", "127.0.0.1");
        ReflectionTestUtils.setField(service, "callinfoapiURL", "http://CTI_SERVER/api/AGENT_ID/SESSION_ID/PHONE_NO");
        ReflectionTestUtils.setField(service, "CZduration", "10");
        // Static field for ctiLoggerURL is set in class static init
        spyService = spy(service);
    }

    @Test
    public void testCallUrl() {
        String url = "http://test-url";
        try (MockedConstruction<HttpUtils> mocked = Mockito.mockConstruction(HttpUtils.class, (mock, context) -> {
            when(mock.get(url)).thenReturn("result");
        })) {
            String result = service.callUrl(url);
            assertEquals("result", result);
        }
    }

    @Test
    public void testCtiDataSync_emptyList() {
        when(callReportRepo.getAllBenCallIDetails(any(), any())).thenReturn(Collections.emptyList());
        service.ctiDataSync();
        verify(callReportRepo).getAllBenCallIDetails(any(), any());
    }

    @Test
    public void testCtiDataSync_withData_success() throws Exception {
        BeneficiaryCall call = mock(BeneficiaryCall.class);
        when(call.getCallID()).thenReturn("callid");
        when(call.getAgentID()).thenReturn("agentid");
        when(call.getPhoneNo()).thenReturn("1234567890");
        List<BeneficiaryCall> list = Arrays.asList(call);
        when(callReportRepo.getAllBenCallIDetails(any(), any())).thenReturn(list);
        JSONObject requestFile = new JSONObject();
        requestFile.put("agent_id", "agentid");
        requestFile.put("session_id", "callid");
        OutputResponse response1 = mock(OutputResponse.class);
        when(ctiService.getVoiceFileNew(anyString(), anyString())).thenReturn(response1);
        when(response1.getStatusCode()).thenReturn(200);
        when(response1.getData()).thenReturn("{\"response\":\"/recording/path/meeting.mp4\"}");
        // Mock static InputMapper.gson() to return a real Gson instance
        try (MockedStatic<InputMapper> mockedStatic = mockStatic(InputMapper.class)) {
            mockedStatic.when(InputMapper::gson).thenCallRealMethod(); // Use the real method if possible
            try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
                configMock.when(() -> ConfigProperties.getPropertyByName("cti-logger_base_url")).thenReturn("http://cti-logger-url");
                doReturn("{\"response\":{\"response_code\":\"1\",\"call_duration\":\"10\",\"call_end_date_time\":\"2025-07-27T10:00:00\",\"call_start_date_time\":\"2025-07-27T09:00:00\"}}\n").when(spyService).callUrl(startsWith("http://"));
                spyService.ctiDataSync();
                verify(callReportRepo).getAllBenCallIDetails(any(), any());
            }
        }
    }

    @Test
    public void testCtiDataSync_withData_exception() throws Exception {
        BeneficiaryCall call = mock(BeneficiaryCall.class);
        when(call.getCallID()).thenReturn("callid");
        when(call.getAgentID()).thenReturn("agentid");
        List<BeneficiaryCall> list = Arrays.asList(call);
        when(callReportRepo.getAllBenCallIDetails(any(), any())).thenReturn(list);
        when(ctiService.getVoiceFileNew(anyString(), anyString())).thenThrow(new RuntimeException("fail"));
        spyService.ctiDataSync();
        verify(callReportRepo).getAllBenCallIDetails(any(), any());
    }
}
