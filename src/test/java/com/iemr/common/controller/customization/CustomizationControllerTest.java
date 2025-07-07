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
package com.iemr.common.controller.customization;

import com.iemr.common.data.customization.SectionFieldsMappingDTO;
import com.iemr.common.data.customization.SectionProjectMappingDTO;
import com.iemr.common.service.customization.CustomizationService;
import com.iemr.common.utils.response.OutputResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CustomizationControllerTest {

    @InjectMocks
    private CustomizationController customizationController;

    @Mock
    private CustomizationService customizationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAddProject_Success() throws Exception {
        String request = "{\"name\":\"Test Project\"}";
        String authorization = "Bearer token";
        String serviceResponse = "{\"status\":\"Success\",\"data\":\"Project added\"}";

        when(customizationService.addProject(request, authorization)).thenReturn(serviceResponse);

        String result = customizationController.addProject(request, authorization);

        OutputResponse expectedResponse = new OutputResponse();
        expectedResponse.setResponse(serviceResponse);
        assertEquals(expectedResponse.toString(), result);
        verify(customizationService).addProject(request, authorization);
    }

    @Test
    void testAddProject_Exception() throws Exception {
        String request = "{\"name\":\"Test Project\"}";
        String authorization = "Bearer token";
        String errorMessage = "Service error";
        Exception serviceException = new Exception(errorMessage);

        doThrow(serviceException).when(customizationService).addProject(request, authorization);

        String result = customizationController.addProject(request, authorization);

        OutputResponse expectedResponse = new OutputResponse();
        expectedResponse.setError(5000, errorMessage);
        assertEquals(expectedResponse.toString(), result);
        verify(customizationService).addProject(request, authorization);
    }

    @Test
    void testGetProjectNames_Success() {
        Integer serviceProviderId = 1;
        String serviceResponse = "{\"status\":\"Success\",\"data\":[\"Project A\", \"Project B\"]}";

        when(customizationService.getProjectNames(serviceProviderId)).thenReturn(serviceResponse);

        String result = customizationController.getProjectNames(serviceProviderId);

        OutputResponse expectedResponse = new OutputResponse();
        expectedResponse.setResponse(serviceResponse);
        assertEquals(expectedResponse.toString(), result);
        verify(customizationService).getProjectNames(serviceProviderId);
    }

    @Test
    void testGetProjectNames_Exception() {
        Integer serviceProviderId = 1;
        RuntimeException serviceException = new RuntimeException("DB connection failed");

        doThrow(serviceException).when(customizationService).getProjectNames(serviceProviderId);

        String result = customizationController.getProjectNames(serviceProviderId);

        OutputResponse expectedResponse = new OutputResponse();
        expectedResponse.setError(serviceException);
        assertEquals(expectedResponse.toString(), result);
        verify(customizationService).getProjectNames(serviceProviderId);
        // Verification of logger.info call for this method would require mocking a private final logger field,
        // which is not directly supported by standard Mockito without reflection or PowerMockito.
    }

    @Test
    void testUpdateProject_Success() throws Exception {
        String request = "{\"id\":1,\"name\":\"Updated Project\"}";
        String authorization = "Bearer token";
        String serviceResponse = "{\"status\":\"Success\",\"data\":\"Project updated\"}";

        when(customizationService.updateProject(request, authorization)).thenReturn(serviceResponse);

        String result = customizationController.updateProject(request, authorization);

        OutputResponse expectedResponse = new OutputResponse();
        expectedResponse.setResponse(serviceResponse);
        assertEquals(expectedResponse.toString(), result);
        verify(customizationService).updateProject(request, authorization);
    }

    @Test
    void testUpdateProject_Exception() throws Exception {
        String request = "{\"id\":1,\"name\":\"Updated Project\"}";
        String authorization = "Bearer token";
        String errorMessage = "Update failed";
        Exception serviceException = new Exception(errorMessage);

        doThrow(serviceException).when(customizationService).updateProject(request, authorization);

        String result = customizationController.updateProject(request, authorization);

        OutputResponse expectedResponse = new OutputResponse();
        expectedResponse.setError(5000, errorMessage);
        assertEquals(expectedResponse.toString(), result);
        verify(customizationService).updateProject(request, authorization);
    }

    @Test
    void testSaveProjectToServiceline_Success() throws Exception {
        String request = "{\"projectId\":1,\"servicelineId\":10}";
        String authorization = "Bearer token";
        String serviceResponse = "{\"status\":\"Success\",\"data\":\"Saved to serviceline\"}";

        when(customizationService.saveProjectToServiceline(request, authorization)).thenReturn(serviceResponse);

        String result = customizationController.saveProjectToServiceline(request, authorization);

        OutputResponse expectedResponse = new OutputResponse();
        expectedResponse.setResponse(serviceResponse);
        assertEquals(expectedResponse.toString(), result);
        verify(customizationService).saveProjectToServiceline(request, authorization);
    }

    @Test
    void testSaveProjectToServiceline_Exception() throws Exception {
        String request = "{\"projectId\":1,\"servicelineId\":10}";
        String authorization = "Bearer token";
        String errorMessage = "Save failed";
        Exception serviceException = new Exception(errorMessage);

        doThrow(serviceException).when(customizationService).saveProjectToServiceline(request, authorization);

        String result = customizationController.saveProjectToServiceline(request, authorization);

        OutputResponse expectedResponse = new OutputResponse();
        expectedResponse.setError(5000, errorMessage);
        assertEquals(expectedResponse.toString(), result);
        verify(customizationService).saveProjectToServiceline(request, authorization);
    }

    @Test
    void testFetchProjectServiceline_Success() throws Exception {
        String request = "{\"projectId\":1}";
        String authorization = "Bearer token";
        String serviceResponse = "{\"status\":\"Success\",\"data\":{\"servicelines\":[]}}";

        when(customizationService.fetchProjectServiceline(request, authorization)).thenReturn(serviceResponse);

        String result = customizationController.fetchProjectServiceline(request, authorization);

        OutputResponse expectedResponse = new OutputResponse();
        expectedResponse.setResponse(serviceResponse);
        assertEquals(expectedResponse.toString(), result);
        verify(customizationService).fetchProjectServiceline(request, authorization);
    }

    @Test
    void testFetchProjectServiceline_Exception() throws Exception {
        String request = "{\"projectId\":1}";
        String authorization = "Bearer token";
        String errorMessage = "Fetch failed";
        Exception serviceException = new Exception(errorMessage);

        doThrow(serviceException).when(customizationService).fetchProjectServiceline(request, authorization);

        String result = customizationController.fetchProjectServiceline(request, authorization);

        OutputResponse expectedResponse = new OutputResponse();
        expectedResponse.setError(5000, errorMessage);
        assertEquals(expectedResponse.toString(), result);
        verify(customizationService).fetchProjectServiceline(request, authorization);
    }

    @Test
    void testUpdateProjectToServiceline_Success() throws Exception {
        String request = "{\"projectId\":1,\"servicelineId\":10}";
        String authorization = "Bearer token";
        String serviceResponse = "{\"status\":\"Success\",\"data\":\"Updated serviceline\"}";

        when(customizationService.updateProjectToServiceline(request, authorization)).thenReturn(serviceResponse);

        String result = customizationController.updateProjectToServiceline(request, authorization);

        OutputResponse expectedResponse = new OutputResponse();
        expectedResponse.setResponse(serviceResponse);
        assertEquals(expectedResponse.toString(), result);
        verify(customizationService).updateProjectToServiceline(request, authorization);
    }

    @Test
    void testUpdateProjectToServiceline_Exception() throws Exception {
        String request = "{\"projectId\":1,\"servicelineId\":10}";
        String authorization = "Bearer token";
        String errorMessage = "Update serviceline failed";
        Exception serviceException = new Exception(errorMessage);

        doThrow(serviceException).when(customizationService).updateProjectToServiceline(request, authorization);

        String result = customizationController.updateProjectToServiceline(request, authorization);

        OutputResponse expectedResponse = new OutputResponse();
        expectedResponse.setError(5000, errorMessage);
        assertEquals(expectedResponse.toString(), result);
        verify(customizationService).updateProjectToServiceline(request, authorization);
    }

    @Test
    void testGetSections_Success() {
        String serviceResponse = "{\"status\":\"Success\",\"data\":[\"Section A\", \"Section B\"]}";

        when(customizationService.getSections()).thenReturn(serviceResponse);

        String result = customizationController.getSections();

        OutputResponse expectedResponse = new OutputResponse();
        expectedResponse.setResponse(serviceResponse);
        assertEquals(expectedResponse.toString(), result);
        verify(customizationService).getSections();
    }

    @Test
    void testGetSections_Exception() {
        RuntimeException serviceException = new RuntimeException("Section fetch failed");

        doThrow(serviceException).when(customizationService).getSections();

        String result = customizationController.getSections();

        OutputResponse expectedResponse = new OutputResponse();
        expectedResponse.setError(serviceException);
        assertEquals(expectedResponse.toString(), result);
        verify(customizationService).getSections();
        // Verification of logger.info call for this method would require mocking a private final logger field,
        // which is not directly supported by standard Mockito without reflection or PowerMockito.
    }

    @Test
    void testUpdateSectionAndFields_Success() throws Exception {
        String request = "{\"sectionId\":1,\"fields\":[]}";
        String authorization = "Bearer token";
        String serviceResponse = "{\"status\":\"Success\",\"data\":\"Section and fields updated\"}";

        when(customizationService.updateSectionAndFields(request, authorization)).thenReturn(serviceResponse);

        String result = customizationController.updateSectionAndFields(request, authorization);

        OutputResponse expectedResponse = new OutputResponse();
        expectedResponse.setResponse(serviceResponse);
        assertEquals(expectedResponse.toString(), result);
        verify(customizationService).updateSectionAndFields(request, authorization);
    }

    @Test
    void testUpdateSectionAndFields_Exception() throws Exception {
        String request = "{\"sectionId\":1,\"fields\":[]}";
        String authorization = "Bearer token";
        String errorMessage = "Update section failed";
        Exception serviceException = new Exception(errorMessage);

        doThrow(serviceException).when(customizationService).updateSectionAndFields(request, authorization);

        String result = customizationController.updateSectionAndFields(request, authorization);

        OutputResponse expectedResponse = new OutputResponse();
        expectedResponse.setError(5000, errorMessage);
        assertEquals(expectedResponse.toString(), result);
        verify(customizationService).updateSectionAndFields(request, authorization);
    }

    @Test
    void testSaveSectionAndFields_Success() throws Exception {
        SectionFieldsMappingDTO dto = new SectionFieldsMappingDTO();
        dto.setSectionId(1);
        dto.setSectionName("Test Section");
        String authorization = "Bearer token";
        String serviceResponse = "{\"status\":\"Success\",\"data\":\"Section fields saved\"}";

        when(customizationService.saveSectionAndFields(any(SectionFieldsMappingDTO.class), anyString())).thenReturn(serviceResponse);

        String result = customizationController.saveSectionAndFields(dto, authorization);

        OutputResponse expectedResponse = new OutputResponse();
        expectedResponse.setResponse(serviceResponse);
        assertEquals(expectedResponse.toString(), result);
        verify(customizationService).saveSectionAndFields(dto, authorization);
    }

    @Test
    void testSaveSectionAndFields_Exception() throws Exception {
        SectionFieldsMappingDTO dto = new SectionFieldsMappingDTO();
        dto.setSectionId(1);
        String authorization = "Bearer token";
        String errorMessage = "Save section fields failed";
        Exception serviceException = new Exception(errorMessage);

        doThrow(serviceException).when(customizationService).saveSectionAndFields(any(SectionFieldsMappingDTO.class), anyString());

        String result = customizationController.saveSectionAndFields(dto, authorization);

        OutputResponse expectedResponse = new OutputResponse();
        expectedResponse.setError(5000, errorMessage);
        assertEquals(expectedResponse.toString(), result);
        verify(customizationService).saveSectionAndFields(dto, authorization);
    }

    @Test
    void testMapSectionToProject_Success() throws Exception {
        SectionProjectMappingDTO dto = new SectionProjectMappingDTO();
        dto.setProjectId(1);
        dto.setProjectName("Project X");
        String authorization = "Bearer token";
        String serviceResponse = "{\"status\":\"Success\",\"data\":\"Section mapped to project\"}";

        when(customizationService.mapSectionToProject(any(SectionProjectMappingDTO.class), anyString())).thenReturn(serviceResponse);

        String result = customizationController.mapSectionToProject(dto, authorization);

        OutputResponse expectedResponse = new OutputResponse();
        expectedResponse.setResponse(serviceResponse);
        assertEquals(expectedResponse.toString(), result);
        verify(customizationService).mapSectionToProject(dto, authorization);
    }

    @Test
    void testMapSectionToProject_Exception() throws Exception {
        SectionProjectMappingDTO dto = new SectionProjectMappingDTO();
        dto.setProjectId(1);
        String authorization = "Bearer token";
        String errorMessage = "Map section to project failed";
        Exception serviceException = new Exception(errorMessage);

        doThrow(serviceException).when(customizationService).mapSectionToProject(any(SectionProjectMappingDTO.class), anyString());

        String result = customizationController.mapSectionToProject(dto, authorization);

        OutputResponse expectedResponse = new OutputResponse();
        expectedResponse.setError(5000, errorMessage);
        assertEquals(expectedResponse.toString(), result);
        verify(customizationService).mapSectionToProject(dto, authorization);
    }

    @Test
    void testFetchMappedSectionsInProject_Success() throws Exception {
        String request = "{\"projectId\":1}";
        String authorization = "Bearer token";
        String serviceResponse = "{\"status\":\"Success\",\"data\":{\"sections\":[]}}";

        when(customizationService.fetchMappedSectionsInProject(request, authorization)).thenReturn(serviceResponse);

        String result = customizationController.fetchMappedSectionsInProject(request, authorization);

        OutputResponse expectedResponse = new OutputResponse();
        expectedResponse.setResponse(serviceResponse);
        assertEquals(expectedResponse.toString(), result);
        verify(customizationService).fetchMappedSectionsInProject(request, authorization);
    }

    @Test
    void testFetchMappedSectionsInProject_Exception() throws Exception {
        String request = "{\"projectId\":1}";
        String authorization = "Bearer token";
        String errorMessage = "Fetch mapped sections failed";
        Exception serviceException = new Exception(errorMessage);

        doThrow(serviceException).when(customizationService).fetchMappedSectionsInProject(request, authorization);

        String result = customizationController.fetchMappedSectionsInProject(request, authorization);

        OutputResponse expectedResponse = new OutputResponse();
        expectedResponse.setError(5000, errorMessage);
        assertEquals(expectedResponse.toString(), result);
        verify(customizationService).fetchMappedSectionsInProject(request, authorization);
    }

    @Test
    void testFetchMappedFields_Success() throws Exception {
        String request = "{\"sectionId\":1}";
        String authorization = "Bearer token";
        String serviceResponse = "{\"status\":\"Success\",\"data\":{\"fields\":[]}}";

        when(customizationService.fetchMappedFields(request, authorization)).thenReturn(serviceResponse);

        String result = customizationController.fetchMappedFields(request, authorization);

        OutputResponse expectedResponse = new OutputResponse();
        expectedResponse.setResponse(serviceResponse);
        assertEquals(expectedResponse.toString(), result);
        verify(customizationService).fetchMappedFields(request, authorization);
    }

    @Test
    void testFetchMappedFields_Exception() throws Exception {
        String request = "{\"sectionId\":1}";
        String authorization = "Bearer token";
        String errorMessage = "Fetch mapped fields failed";
        Exception serviceException = new Exception(errorMessage);

        doThrow(serviceException).when(customizationService).fetchMappedFields(request, authorization);

        String result = customizationController.fetchMappedFields(request, authorization);

        OutputResponse expectedResponse = new OutputResponse();
        expectedResponse.setError(5000, errorMessage);
        assertEquals(expectedResponse.toString(), result);
        verify(customizationService).fetchMappedFields(request, authorization);
    }

    @Test
    void testFetchAllData_Success() throws Exception {
        String request = "{}";
        String authorization = "Bearer token";
        String serviceResponse = "{\"status\":\"Success\",\"data\":{\"allData\":[]}}";

        when(customizationService.fetchAllData(request, authorization)).thenReturn(serviceResponse);

        String result = customizationController.fetchAllData(request, authorization);

        OutputResponse expectedResponse = new OutputResponse();
        expectedResponse.setResponse(serviceResponse);
        assertEquals(expectedResponse.toString(), result);
        verify(customizationService).fetchAllData(request, authorization);
    }

    @Test
    void testFetchAllData_Exception() throws Exception {
        String request = "{}";
        String authorization = "Bearer token";
        String errorMessage = "Fetch all data failed";
        Exception serviceException = new Exception(errorMessage);

        doThrow(serviceException).when(customizationService).fetchAllData(request, authorization);

        String result = customizationController.fetchAllData(request, authorization);

        OutputResponse expectedResponse = new OutputResponse();
        expectedResponse.setError(5000, errorMessage);
        assertEquals(expectedResponse.toString(), result);
        verify(customizationService).fetchAllData(request, authorization);
    }

    @Test
    void testGetfileldType_Success() {
        String serviceResponse = "{\"status\":\"Success\",\"data\":[\"Type A\", \"Type B\"]}";

        when(customizationService.getfileldType()).thenReturn(serviceResponse);

        String result = customizationController.getfileldType();

        OutputResponse expectedResponse = new OutputResponse();
        expectedResponse.setResponse(serviceResponse);
        assertEquals(expectedResponse.toString(), result);
        verify(customizationService).getfileldType();
    }

    @Test
    void testGetfileldType_Exception() {
        RuntimeException serviceException = new RuntimeException("Field type fetch failed");

        doThrow(serviceException).when(customizationService).getfileldType();

        String result = customizationController.getfileldType();

        OutputResponse expectedResponse = new OutputResponse();
        expectedResponse.setError(serviceException);
        assertEquals(expectedResponse.toString(), result);
        verify(customizationService).getfileldType();
        // Verification of logger.info call for this method would require mocking a private final logger field,
        // which is not directly supported by standard Mockito without reflection or PowerMockito.
    }
}