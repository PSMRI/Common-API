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


import com.fasterxml.jackson.core.JsonProcessingException;
// import com.fasterxml.jackson.databind.JsonMappingException;
import com.iemr.common.data.directory.InstituteDirectoryMapping;
import com.iemr.common.repository.directory.DirectoryMappingRepository;
// import com.iemr.common.utils.exception.IEMRException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DirectoryMappingServiceImplTest {

    @Test
    void testSetDirectoryRepository() {
        DirectoryMappingServiceImpl service = new DirectoryMappingServiceImpl();
        DirectoryMappingRepository mockRepo = mock(DirectoryMappingRepository.class);
        service.setDirectoryRepository(mockRepo);
        // No assertion needed, just ensure no exception and setter is covered
    }
    @Mock
    private DirectoryMappingRepository directoryMappingRepository;

    @InjectMocks
    private DirectoryMappingServiceImpl directoryMappingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private String buildRequestJson(Integer dirId, Integer subDirId, Integer stateId, Integer districtId, Integer blockId) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        if (dirId != null) sb.append("\"instituteDirectoryID\":" + dirId + ",");
        if (subDirId != null) sb.append("\"instituteSubDirectoryID\":" + subDirId + ",");
        if (stateId != null) sb.append("\"stateID\":" + stateId + ",");
        if (districtId != null) sb.append("\"districtID\":" + districtId + ",");
        if (blockId != null) sb.append("\"blockID\":" + blockId + ",");
        if (sb.charAt(sb.length()-1) == ',') sb.deleteCharAt(sb.length()-1);
        sb.append("}");
        return sb.toString();
    }

    @Test
    void testFindActiveInstituteDirectories_WithBlockId() throws Exception {
        String request = buildRequestJson(1, 2, 3, 4, 5);
        InstituteDirectoryMapping mockMapping = mock(InstituteDirectoryMapping.class);
        when(mockMapping.getBlockID()).thenReturn(5);
        when(mockMapping.getInstituteDirectoryID()).thenReturn(1);
        when(mockMapping.getInstituteSubDirectoryID()).thenReturn(2);
        when(mockMapping.getStateID()).thenReturn(3);
        when(mockMapping.getDistrictID()).thenReturn(4);
        List<InstituteDirectoryMapping> expected = Collections.singletonList(mockMapping);
        when(directoryMappingRepository.findAciveInstituteDirectories(1, 2, 3, 4, 5)).thenReturn(expected);

        List<InstituteDirectoryMapping> result = directoryMappingService.findAciveInstituteDirectories(request);
        assertEquals(expected, result);
        verify(directoryMappingRepository).findAciveInstituteDirectories(1, 2, 3, 4, 5);
    }

    @Test
    void testFindActiveInstituteDirectories_WithStateAndDistrict() throws Exception {
        String request = buildRequestJson(1, 2, 3, 4, null);
        InstituteDirectoryMapping mockMapping = mock(InstituteDirectoryMapping.class);
        when(mockMapping.getBlockID()).thenReturn(null);
        when(mockMapping.getStateID()).thenReturn(3);
        when(mockMapping.getDistrictID()).thenReturn(4);
        when(mockMapping.getInstituteDirectoryID()).thenReturn(1);
        when(mockMapping.getInstituteSubDirectoryID()).thenReturn(2);
        List<InstituteDirectoryMapping> expected = Collections.singletonList(mockMapping);
        when(directoryMappingRepository.findAciveInstituteDirectories(1, 2, 3, 4)).thenReturn(expected);

        List<InstituteDirectoryMapping> result = directoryMappingService.findAciveInstituteDirectories(request);
        assertEquals(expected, result);
        verify(directoryMappingRepository).findAciveInstituteDirectories(1, 2, 3, 4);
    }

    @Test
    void testFindActiveInstituteDirectories_DefaultCase() throws Exception {
        String request = buildRequestJson(1, 2, null, null, null);
        InstituteDirectoryMapping mockMapping = mock(InstituteDirectoryMapping.class);
        when(mockMapping.getBlockID()).thenReturn(null);
        when(mockMapping.getStateID()).thenReturn(null);
        when(mockMapping.getDistrictID()).thenReturn(null);
        when(mockMapping.getInstituteDirectoryID()).thenReturn(1);
        when(mockMapping.getInstituteSubDirectoryID()).thenReturn(2);
        List<InstituteDirectoryMapping> expected = Collections.singletonList(mockMapping);
        when(directoryMappingRepository.findAciveInstituteDirectories(1, 2)).thenReturn(expected);

        List<InstituteDirectoryMapping> result = directoryMappingService.findAciveInstituteDirectories(request);
        assertEquals(expected, result);
        verify(directoryMappingRepository).findAciveInstituteDirectories(1, 2);
    }

    @Test
    void testFindActiveInstituteDirectories_JsonProcessingException() {
        String invalidJson = "{";
        assertThrows(JsonProcessingException.class, () ->
                directoryMappingService.findAciveInstituteDirectories(invalidJson));
    }
}
