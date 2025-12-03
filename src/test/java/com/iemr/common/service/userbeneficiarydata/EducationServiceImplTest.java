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
package com.iemr.common.service.userbeneficiarydata;

import com.iemr.common.data.beneficiary.BeneficiaryEducation;
import com.iemr.common.repository.beneficiary.EducationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EducationServiceImplTest {
    @InjectMocks
    EducationServiceImpl service;

    @Mock
    EducationRepository educationRepository;

    @Test
    public void testGetActiveEducations() {
        Object[] obj = new Object[]{1L, "EducationName"};
        Set<Object[]> set = new HashSet<>();
        set.add(obj);
        when(educationRepository.findActiveEducations()).thenReturn(set);
        List<BeneficiaryEducation> result = service.getActiveEducations();
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getEducationID());
        // Use reflection to print all fields and assert on the correct one for education name
        boolean found = false;
        StringBuilder fieldNames = new StringBuilder();
        for (java.lang.reflect.Field field : BeneficiaryEducation.class.getDeclaredFields()) {
            field.setAccessible(true);
            fieldNames.append(field.getName()).append(", ");
            try {
                Object value = field.get(result.get(0));
                if ("EducationName".equals(value)) {
                    found = true;
                    assertEquals("EducationName", value);
                }
            } catch (Exception ignored) {}
        }
        if (!found) {
            fail("Education name not found in any field. Fields: " + fieldNames);
        }
    }

    @Test
    public void testGetActiveEducations_empty() {
        when(educationRepository.findActiveEducations()).thenReturn(new HashSet<>());
        List<BeneficiaryEducation> result = service.getActiveEducations();
        assertTrue(result.isEmpty());
    }

    @Test
    public void testSetEducationServiceImpl() {
        EducationServiceImpl impl = new EducationServiceImpl();
        EducationRepository mockRepo = mock(EducationRepository.class);
        impl.setEducationServiceImpl(mockRepo);
        // Use reflection to verify the field is set
        try {
            java.lang.reflect.Field field = EducationServiceImpl.class.getDeclaredField("educationRepository");
            field.setAccessible(true);
            assertSame(mockRepo, field.get(impl));
        } catch (Exception e) {
            fail("Setter did not set the field correctly: " + e.getMessage());
        }
    }
}
