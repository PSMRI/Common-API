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

import com.iemr.common.data.beneficiary.BeneficiaryOccupation;
import com.iemr.common.repository.beneficiary.BeneficiaryOccupationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BeneficiaryOccupationServiceImplTest {
    @Mock
    private BeneficiaryOccupationRepository beneficiaryOccupationRepository;

    @InjectMocks
    private BeneficiaryOccupationServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSetBeneficiaryOccupationRepository() {
        BeneficiaryOccupationServiceImpl impl = new BeneficiaryOccupationServiceImpl();
        BeneficiaryOccupationRepository repo = mock(BeneficiaryOccupationRepository.class);
        impl.setBeneficiaryOccupationRepository(repo);
        // No exception, repository is set
    }

    @Test
    void testGetActiveOccupations_withValidData() {
        Set<Object[]> repoResult = new HashSet<>();
        repoResult.add(new Object[]{1L, "TestOccupation"});
        BeneficiaryOccupation occupation = new BeneficiaryOccupation().getOccupation(1L, "TestOccupation");
        when(beneficiaryOccupationRepository.getActiveOccupations()).thenReturn(repoResult);
        List<BeneficiaryOccupation> result = service.getActiveOccupations();
        assertEquals(1, result.size());
        BeneficiaryOccupation actual = result.get(0);
        assertEquals(occupation.getOccupationID(), actual.getOccupationID());
        assertEquals(occupation.getOccupationType(), actual.getOccupationType());
    }

    @Test
    void testGetActiveOccupations_withNullObject() {
        Set<Object[]> repoResult = new HashSet<>();
        repoResult.add(null);
        when(beneficiaryOccupationRepository.getActiveOccupations()).thenReturn(repoResult);
        List<BeneficiaryOccupation> result = service.getActiveOccupations();
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetActiveOccupations_withInvalidLength() {
        Set<Object[]> repoResult = new HashSet<>();
        repoResult.add(new Object[]{1L}); // length < 2
        when(beneficiaryOccupationRepository.getActiveOccupations()).thenReturn(repoResult);
        List<BeneficiaryOccupation> result = service.getActiveOccupations();
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetActiveOccupations_withEmptySet() {
        when(beneficiaryOccupationRepository.getActiveOccupations()).thenReturn(Collections.emptySet());
        List<BeneficiaryOccupation> result = service.getActiveOccupations();
        assertTrue(result.isEmpty());
    }
}
