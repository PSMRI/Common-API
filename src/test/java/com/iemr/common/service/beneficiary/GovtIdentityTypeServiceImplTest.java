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

import com.iemr.common.data.beneficiary.GovtIdentityType;
import com.iemr.common.repository.beneficiary.GovtIdentityTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GovtIdentityTypeServiceImplTest {
    @Mock
    private GovtIdentityTypeRepository govtIdentityTypeRepository;

    @InjectMocks
    private GovtIdentityTypeServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSetBeneficiaryOccupationRepository() {
        GovtIdentityTypeServiceImpl impl = new GovtIdentityTypeServiceImpl();
        GovtIdentityTypeRepository repo = mock(GovtIdentityTypeRepository.class);
        impl.setBeneficiaryOccupationRepository(repo);
        // No exception, repository is set
    }

    @Test
    void testGetActiveIDTypes_withValidData() {
        Set<Object[]> repoResult = new HashSet<>();
        repoResult.add(new Object[]{1, "Aadhaar", true});
        GovtIdentityType expected = new GovtIdentityType().getConstructor(1, "Aadhaar", true);
        when(govtIdentityTypeRepository.getActiveIDTypes()).thenReturn(repoResult);
        List<GovtIdentityType> result = service.getActiveIDTypes();
        assertEquals(1, result.size());
        GovtIdentityType actual = result.get(0);
        assertEquals(expected.getGovtIdentityTypeID(), actual.getGovtIdentityTypeID());
        assertEquals(expected.getIdentityType(), actual.getIdentityType());
        assertEquals(expected.getDeleted(), actual.getDeleted());
    }

    @Test
    void testGetActiveIDTypes_withNullObject() {
        Set<Object[]> repoResult = new HashSet<>();
        repoResult.add(null);
        when(govtIdentityTypeRepository.getActiveIDTypes()).thenReturn(repoResult);
        List<GovtIdentityType> result = service.getActiveIDTypes();
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetActiveIDTypes_withInvalidLength() {
        Set<Object[]> repoResult = new HashSet<>();
        repoResult.add(new Object[]{1, "Aadhaar"}); // length < 3
        when(govtIdentityTypeRepository.getActiveIDTypes()).thenReturn(repoResult);
        List<GovtIdentityType> result = service.getActiveIDTypes();
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetActiveIDTypes_withEmptySet() {
        when(govtIdentityTypeRepository.getActiveIDTypes()).thenReturn(Collections.emptySet());
        List<GovtIdentityType> result = service.getActiveIDTypes();
        assertTrue(result.isEmpty());
    }
}
