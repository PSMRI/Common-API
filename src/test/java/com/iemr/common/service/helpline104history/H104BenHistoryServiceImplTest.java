package com.iemr.common.service.helpline104history;

import com.iemr.common.data.helpline104history.H104BenMedHistory;
import com.iemr.common.repository.helpline104history.H104BenHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class H104BenHistoryServiceImplTest {

    @Mock
    private H104BenHistoryRepository smpleBenHistoryRepositoryRepository;

    @InjectMocks
    private H104BenHistoryServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void geSmpleBenHistory_returnsList() {
        Long beneficiaryId = 42L;
        ArrayList<H104BenMedHistory> mockList = new ArrayList<>();
        H104BenMedHistory medHistory = new H104BenMedHistory();
        mockList.add(medHistory);

        when(smpleBenHistoryRepositoryRepository.getBenHistory(beneficiaryId)).thenReturn(mockList);

        ArrayList<H104BenMedHistory> result = service.geSmpleBenHistory(beneficiaryId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertSame(medHistory, result.get(0));
        verify(smpleBenHistoryRepositoryRepository).getBenHistory(beneficiaryId);
    }
}
