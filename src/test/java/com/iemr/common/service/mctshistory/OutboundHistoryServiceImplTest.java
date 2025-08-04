package com.iemr.common.service.mctshistory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.iemr.common.data.mctshistory.MctsCallResponseDetail;
import com.iemr.common.data.mctshistory.MctsOutboundCallDetail;
import com.iemr.common.repository.mctshistory.OutboundHistoryRepository;
import com.iemr.common.repository.mctshistory.OutboundResponseRepository;
import com.iemr.common.utils.exception.IEMRException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OutboundHistoryServiceImplTest {

    @Mock
    private OutboundHistoryRepository outboundHistoryRepository;

    @Mock
    private OutboundResponseRepository outboundResponseRepository;

    @InjectMocks
    private OutboundHistoryServiceImpl outboundHistoryService;

    private String callDetailJson;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        callDetailJson = "{\"beneficiaryRegID\":123,\"callDetailID\":456}";
    }

    @Test
    void getCallHistory_returnsListString() throws Exception {
        MctsOutboundCallDetail callDetail = new MctsOutboundCallDetail();
        callDetail.setBeneficiaryRegID(123L);
        ArrayList<MctsOutboundCallDetail> mockList = new ArrayList<>();
        mockList.add(callDetail);
        when(outboundHistoryRepository.getCallHistory(123L)).thenReturn(mockList);

        String result = outboundHistoryService.getCallHistory(callDetailJson);

        assertNotNull(result);
        assertTrue(result.contains("beneficiaryRegID"));
        verify(outboundHistoryRepository).getCallHistory(123L);
    }

    @Test
    void getMctsCallResponse_returnsListString() throws Exception {
        MctsCallResponseDetail responseDetail = new MctsCallResponseDetail();
        ArrayList<MctsCallResponseDetail> mockList = new ArrayList<>();
        mockList.add(responseDetail);
        when(outboundResponseRepository.getMctsCallResponse(456L)).thenReturn(mockList);

        String result = outboundHistoryService.getMctsCallResponse(callDetailJson);

        assertNotNull(result);
        verify(outboundResponseRepository).getMctsCallResponse(456L);
    }

    @Test
    void getCallHistory_invalidJson_throwsException() {
        String invalidJson = "not a json";
        assertThrows(JsonProcessingException.class, () -> outboundHistoryService.getCallHistory(invalidJson));
    }

    @Test
    void getMctsCallResponse_invalidJson_throwsException() {
        String invalidJson = "not a json";
        assertThrows(JsonProcessingException.class, () -> outboundHistoryService.getMctsCallResponse(invalidJson));
    }
}
