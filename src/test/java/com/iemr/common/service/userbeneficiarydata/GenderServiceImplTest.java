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

import com.iemr.common.data.userbeneficiarydata.Gender;
import com.iemr.common.repository.userbeneficiarydata.GenderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GenderServiceImplTest {
    @InjectMocks
    GenderServiceImpl service;

    @Mock
    GenderRepository genderRepository;

    @Test
    public void testGetActiveGenders() {
        Object[] obj = new Object[]{1, "Male"};
        Set<Object[]> set = new HashSet<>();
        set.add(obj);
        when(genderRepository.findAciveGenders()).thenReturn(set);
        List<Gender> result = service.getActiveGenders();
        assertEquals(1, result.size());
        Gender gender = result.get(0);
        boolean found = false;
        StringBuilder fieldNames = new StringBuilder();
        for (java.lang.reflect.Field field : Gender.class.getDeclaredFields()) {
            field.setAccessible(true);
            fieldNames.append(field.getName()).append(", ");
            try {
                Object value = field.get(gender);
                if (Objects.equals(value, 1) || Objects.equals(value, "Male")) {
                    found = true;
                }
            } catch (Exception ignored) {}
        }
        if (!found) {
            fail("Gender id or name not found in any field. Fields: " + fieldNames);
        }
    }

    @Test
    public void testGetActiveGenders_empty() {
        when(genderRepository.findAciveGenders()).thenReturn(new HashSet<>());
        List<Gender> result = service.getActiveGenders();
        assertTrue(result.isEmpty());
    }

    @Test
    public void testSetGenderServiceImpl() {
        GenderServiceImpl impl = new GenderServiceImpl();
        GenderRepository mockRepo = mock(GenderRepository.class);
        impl.setGenderServiceImpl(mockRepo);
        // Use reflection to verify the field is set
        try {
            java.lang.reflect.Field field = GenderServiceImpl.class.getDeclaredField("genderRepository");
            field.setAccessible(true);
            assertSame(mockRepo, field.get(impl));
        } catch (Exception e) {
            fail("Setter did not set the field correctly: " + e.getMessage());
        }
    }
}
