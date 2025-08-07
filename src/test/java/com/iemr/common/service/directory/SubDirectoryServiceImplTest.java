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
package com.iemr.common.service.directory;

import com.iemr.common.data.directory.SubDirectory;
import com.iemr.common.repository.directory.SubDirectoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SubDirectoryServiceImplTest {
    @Mock
    private SubDirectoryRepository subDirectoryRepository;

    @InjectMocks
    private SubDirectoryServiceImpl subDirectoryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetSubDirectories_normal() {
        Set<Object[]> mockResult = new HashSet<>();
        mockResult.add(new Object[]{1, "SubA"});
        mockResult.add(new Object[]{2, "SubB"});
        mockResult.add(null); // Should be ignored
        mockResult.add(new Object[]{}); // Should be ignored
        mockResult.add(new Object[]{3}); // Should be ignored (length != 2)
        when(subDirectoryRepository.findAciveSubDirectories(10)).thenReturn(mockResult);

        List<SubDirectory> result = subDirectoryService.getSubDirectories(10);
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getInstituteSubDirectoryID());
        assertEquals("SubA", result.get(0).getInstituteSubDirectoryName());
        assertEquals(2, result.get(1).getInstituteSubDirectoryID());
        assertEquals("SubB", result.get(1).getInstituteSubDirectoryName());
        verify(subDirectoryRepository).findAciveSubDirectories(10);
    }

    @Test
    void testGetSubDirectories_empty() {
        when(subDirectoryRepository.findAciveSubDirectories(99)).thenReturn(Collections.emptySet());
        List<SubDirectory> result = subDirectoryService.getSubDirectories(99);
        assertTrue(result.isEmpty());
        verify(subDirectoryRepository).findAciveSubDirectories(99);
    }

    @Test
    void testSetSubDirectoryRepository() {
        SubDirectoryServiceImpl service = new SubDirectoryServiceImpl();
        SubDirectoryRepository mockRepo = mock(SubDirectoryRepository.class);
        service.setSubDirectoryRepository(mockRepo);
        // No assertion needed, just ensure setter is covered
    }
}
