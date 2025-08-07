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

import com.iemr.common.data.beneficiary.BeneficiaryType;
import com.iemr.common.repository.beneficiary.IEMRBeneficiaryTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IEMRBeneficiaryTypeServiceImplTest {
    @Mock
    private IEMRBeneficiaryTypeRepository iemrBeneficiaryTypeRepository;

    @InjectMocks
    private IEMRBeneficiaryTypeServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAddRelation() {
        BeneficiaryType type = new BeneficiaryType();
        when(iemrBeneficiaryTypeRepository.save(type)).thenReturn(type);
        BeneficiaryType result = service.addRelation(type);
        assertSame(type, result);
        verify(iemrBeneficiaryTypeRepository).save(type);
    }

    @Test
    void testGetRelations() {
        BeneficiaryType type1 = new BeneficiaryType();
        BeneficiaryType type2 = new BeneficiaryType();
        List<BeneficiaryType> types = Arrays.asList(type1, type2);
        when(iemrBeneficiaryTypeRepository.findAll()).thenReturn(types);
        Iterable<BeneficiaryType> result = service.getRelations();
        assertTrue(result.iterator().hasNext());
        verify(iemrBeneficiaryTypeRepository).findAll();
    }
}
