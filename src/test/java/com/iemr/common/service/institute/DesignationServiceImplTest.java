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
package com.iemr.common.service.institute;

import com.iemr.common.data.institute.Designation;
import com.iemr.common.repository.institute.DesignationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DesignationServiceImplTest {
    @InjectMocks
    DesignationServiceImpl service;

    @Mock
    DesignationRepository designationRepository;
    @Mock
    Logger logger;

    @BeforeEach
    public void setup() {
        ReflectionTestUtils.setField(service, "logger", logger);
    }

    @Test
    public void testGetDesignations_withResults() {
        List<Designation> mockList = Arrays.asList(mock(Designation.class), mock(Designation.class));
        when(designationRepository.findAciveDesignations()).thenReturn(mockList);
        List<Designation> result = service.getDesignations();
        assertEquals(2, result.size());
        verify(designationRepository).findAciveDesignations();
        verify(logger).info(contains("getDesignations returning "));
    }

    @Test
    public void testGetDesignations_empty() {
        when(designationRepository.findAciveDesignations()).thenReturn(Collections.emptyList());
        List<Designation> result = service.getDesignations();
        assertTrue(result.isEmpty());
        verify(designationRepository).findAciveDesignations();
        verify(logger).info(contains("getDesignations returning "));
    }
}
