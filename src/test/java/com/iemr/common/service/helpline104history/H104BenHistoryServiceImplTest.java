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
