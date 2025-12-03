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
package com.iemr.common.service.beneficiary;

import com.iemr.common.data.beneficiary.SexualOrientation;
import com.iemr.common.repository.userbeneficiarydata.SexualOrientationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SexualOrientationServiceImplTest {
    @Mock
    private SexualOrientationRepository sexualOrientationRepository;

    @InjectMocks
    private SexualOrientationServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSetDirectoryRepository() {
        SexualOrientationServiceImpl impl = new SexualOrientationServiceImpl();
        SexualOrientationRepository repo = mock(SexualOrientationRepository.class);
        impl.setDirectoryRepository(repo);
        // No exception, repository is set
    }

    @Test
    void testGetSexualOrientations_withValidData() {
        Set<Object[]> repoResult = new HashSet<>();
        repoResult.add(new Object[]{(short)1, "Test"});
        when(sexualOrientationRepository.findAciveOrientations()).thenReturn(repoResult);
        List<SexualOrientation> result = service.getSexualOrientations();
        assertEquals(1, result.size());
        assertEquals((short)1, result.get(0).getSexualOrientationId());
        assertEquals("Test", result.get(0).getSexualOrientation());
    }

    @Test
    void testGetSexualOrientations_withNullObject() {
        Set<Object[]> repoResult = new HashSet<>();
        repoResult.add(null);
        when(sexualOrientationRepository.findAciveOrientations()).thenReturn(repoResult);
        List<SexualOrientation> result = service.getSexualOrientations();
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetSexualOrientations_withInvalidLength() {
        Set<Object[]> repoResult = new HashSet<>();
        repoResult.add(new Object[]{(short)1}); // length != 2
        when(sexualOrientationRepository.findAciveOrientations()).thenReturn(repoResult);
        List<SexualOrientation> result = service.getSexualOrientations();
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetSexualOrientations_withEmptySet() {
        when(sexualOrientationRepository.findAciveOrientations()).thenReturn(Collections.emptySet());
        List<SexualOrientation> result = service.getSexualOrientations();
        assertTrue(result.isEmpty());
    }
}
