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
package com.iemr.common.service.services;

import com.iemr.common.data.users.ServiceMaster;
import com.iemr.common.repository.services.ServiesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ServicesImplTest {
    @InjectMocks
    ServicesImpl servicesImpl;

    @Mock
    ServiesRepository serviesRepository;

    @Test
    public void testServicesList_withResults() {
        Set<Object[]> repoResult = new HashSet<>();
        repoResult.add(new Object[]{1, "Service1", "Desc1", true});
        repoResult.add(new Object[]{2, "Service2", "Desc2", false});
        when(serviesRepository.getActiveServicesList()).thenReturn(repoResult);

        List<ServiceMaster> result = servicesImpl.servicesList();
        assertEquals(2, result.size());
        result.sort(Comparator.comparing(ServiceMaster::getServiceID));
        assertEquals(1, result.get(0).getServiceID());
        assertEquals("Service1", result.get(0).getServiceName());
        assertEquals("Desc1", result.get(0).getServiceDesc());
        assertTrue(result.get(0).isDeleted());
        assertEquals(2, result.get(1).getServiceID());
        assertEquals("Service2", result.get(1).getServiceName());
        assertEquals("Desc2", result.get(1).getServiceDesc());
        assertFalse(result.get(1).isDeleted());
    }

    @Test
    public void testServicesList_withNullObject() {
        Set<Object[]> repoResult = new HashSet<>();
        repoResult.add(null);
        when(serviesRepository.getActiveServicesList()).thenReturn(repoResult);
        List<ServiceMaster> result = servicesImpl.servicesList();
        assertEquals(0, result.size());
    }

    @Test
    public void testServicesList_withShortArray() {
        Set<Object[]> repoResult = new HashSet<>();
        repoResult.add(new Object[]{1, "Service1"}); // length < 4
        when(serviesRepository.getActiveServicesList()).thenReturn(repoResult);
        List<ServiceMaster> result = servicesImpl.servicesList();
        assertEquals(0, result.size());
    }

    @Test
    public void testServicesList_empty() {
        when(serviesRepository.getActiveServicesList()).thenReturn(new HashSet<>());
        List<ServiceMaster> result = servicesImpl.servicesList();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
