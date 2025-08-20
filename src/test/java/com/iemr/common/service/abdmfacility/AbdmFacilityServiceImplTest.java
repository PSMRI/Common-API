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
package com.iemr.common.service.abdmfacility;

import com.iemr.common.data.users.ProviderServiceAddressMapping;
import com.iemr.common.repository.abdmfacility.AbdmFacilityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AbdmFacilityServiceImplTest {

    @Mock
    private AbdmFacilityRepository abdmFacilityRepo;

    @InjectMocks
    private AbdmFacilityServiceImpl abdmFacilityService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getMappedAbdmFacility_returnsToString() {
        int psmId = 123;
        ProviderServiceAddressMapping mapping = mock(ProviderServiceAddressMapping.class);
        when(abdmFacilityRepo.getAbdmFacility(psmId)).thenReturn(mapping);
        when(mapping.toString()).thenReturn("mockedToString");

        String result = abdmFacilityService.getMappedAbdmFacility(psmId);

        assertEquals("mockedToString", result);
        verify(abdmFacilityRepo).getAbdmFacility(psmId);
    }
}
