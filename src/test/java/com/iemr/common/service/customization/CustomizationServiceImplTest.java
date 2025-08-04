
package com.iemr.common.service.customization;

import com.google.gson.Gson;
import com.iemr.common.data.customization.SectionProjectMapping;
import com.iemr.common.data.customization.SectionProjectMappingDTO;
import com.iemr.common.repo.customization.SectionProjectMappingRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomizationServiceImplTest {

    @InjectMocks
    private CustomizationServiceImpl customizationService;

    @Mock
    private SectionProjectMappingRepo sectionProjectMappingRepo;

    @Captor
    private ArgumentCaptor<List<SectionProjectMapping>> sectionProjectMappingListCaptor;
    // --- Additional mocks for other methods ---
    @Mock private com.iemr.common.repo.customization.ProjectCustomizationRepo projectCustomizationRepo;
    @Mock private com.iemr.common.repo.customization.SectionMasterCustomizationRepo sectionMasterCustomizationRepo;
    @Mock private com.iemr.common.repo.customization.ServicelineCustomizationRepo servicelineCustomizationRepo;
    @Mock private com.iemr.common.repo.customization.SectionAndFieldsMappingRepo sectionAndFieldsMappingRepo;
    @Mock private com.iemr.common.repo.customization.V_CustomizationDataFieldsRepo v_CustomizationDataFieldsRepo;
    @Mock private com.iemr.common.repo.customization.FieldTypeRepo fieldTypeRepo;

    // --- addProject ---
    @Test
    void addProject_success() throws Exception {
        com.iemr.common.data.customization.ProjectCustomization project = new com.iemr.common.data.customization.ProjectCustomization();
        project.setProjectName("P");
        project.setServiceProviderId(1);
        when(projectCustomizationRepo.findByProjectName("P", 1)).thenReturn(java.util.Collections.emptySet());
        when(projectCustomizationRepo.save(any())).thenReturn(project);
        String req = new com.google.gson.Gson().toJson(project);
        String result = customizationService.addProject(req, "auth");
        assertTrue(result.contains("Project Added Successfully"));
    }

    @Test
    void addProject_alreadyExists() {
        com.iemr.common.data.customization.ProjectCustomization project = new com.iemr.common.data.customization.ProjectCustomization();
        project.setProjectName("P");
        project.setServiceProviderId(1);
        java.util.Set<com.iemr.common.data.customization.ProjectCustomization> set = new java.util.HashSet<>();
        set.add(project);
        when(projectCustomizationRepo.findByProjectName("P", 1)).thenReturn(set);
        String req = new com.google.gson.Gson().toJson(project);
        Exception ex = assertThrows(com.iemr.common.utils.exception.IEMRException.class, () -> customizationService.addProject(req, "auth"));
        assertEquals("project already exists", ex.getMessage());
    }

    // --- getProjectNames ---
    @Test
    void getProjectNames_success() {
        java.util.ArrayList<com.iemr.common.data.customization.ProjectCustomization> list = new java.util.ArrayList<>();
        when(projectCustomizationRepo.findByServiceProviderId(1)).thenReturn(list);
        String result = customizationService.getProjectNames(1);
        assertTrue(result.contains("[]"));
    }

    // --- updateProject ---
    @Test
    void updateProject_success() throws Exception {
        com.iemr.common.data.customization.ProjectCustomization project = new com.iemr.common.data.customization.ProjectCustomization();
        project.setProjectId(1);
        project.setServiceProviderId(2);
        project.setDeleted(false);
        when(projectCustomizationRepo.findServiceProviderId(2)).thenReturn(2);
        when(projectCustomizationRepo.save(any())).thenReturn(project);
        String req = new com.google.gson.Gson().toJson(project);
        String result = customizationService.updateProject(req, "auth");
        assertTrue(result.contains("Project updated Successfully"));
    }

    @Test
    void updateProject_deletedFlag() throws Exception {
        com.iemr.common.data.customization.ProjectCustomization project = new com.iemr.common.data.customization.ProjectCustomization();
        project.setProjectId(1);
        project.setServiceProviderId(2);
        project.setDeleted(true);
        project.setProjectName("P");
        when(projectCustomizationRepo.findServiceProviderId(2)).thenReturn(2);
        when(projectCustomizationRepo.save(any())).thenReturn(project);
        when(servicelineCustomizationRepo.updateDeletedFlag(1, "P", true)).thenReturn(1);
        String req = new com.google.gson.Gson().toJson(project);
        String result = customizationService.updateProject(req, "auth");
        assertTrue(result.contains("Project updated Successfully"));
    }

    @Test
    void updateProject_exception() {
        com.iemr.common.data.customization.ProjectCustomization project = new com.iemr.common.data.customization.ProjectCustomization();
        project.setProjectId(1);
        project.setServiceProviderId(2);
        when(projectCustomizationRepo.findServiceProviderId(2)).thenThrow(new RuntimeException("fail"));
        String req = new com.google.gson.Gson().toJson(project);
        Exception ex = assertThrows(Exception.class, () -> customizationService.updateProject(req, "auth"));
        assertEquals("fail", ex.getMessage());
    }

    // --- saveProjectToServiceline ---
    @Test
    void saveProjectToServiceline_success() throws Exception {
        com.iemr.common.data.customization.ServicelineCustomization[] arr = new com.iemr.common.data.customization.ServicelineCustomization[1];
        com.iemr.common.data.customization.ServicelineCustomization s = new com.iemr.common.data.customization.ServicelineCustomization();
        s.setServiceLineId(1); s.setServiceProviderId(2); s.setStateId(3); s.setDistrictId(4); s.setBlockId(5);
        arr[0] = s;
        when(servicelineCustomizationRepo.findByProjectName(1, 2, 3, 4, 5)).thenReturn(java.util.Collections.emptySet());
        when(servicelineCustomizationRepo.save(any())).thenReturn(s);
        String req = new com.google.gson.Gson().toJson(arr);
        String result = customizationService.saveProjectToServiceline(req, "auth");
        assertTrue(result.contains("Save Project to Serviceline Successfully"));
    }

    @Test
    void saveProjectToServiceline_alreadyMapped() {
        com.iemr.common.data.customization.ServicelineCustomization[] arr = new com.iemr.common.data.customization.ServicelineCustomization[1];
        com.iemr.common.data.customization.ServicelineCustomization s = new com.iemr.common.data.customization.ServicelineCustomization();
        s.setServiceLineId(1); s.setServiceProviderId(2); s.setStateId(3); s.setDistrictId(4); s.setBlockId(5);
        arr[0] = s;
        java.util.Set<com.iemr.common.data.customization.ServicelineCustomization> set = new java.util.HashSet<>();
        set.add(s);
        when(servicelineCustomizationRepo.findByProjectName(1, 2, 3, 4, 5)).thenReturn(set);
        String req = new com.google.gson.Gson().toJson(arr);
        Exception ex = assertThrows(Exception.class, () -> customizationService.saveProjectToServiceline(req, "auth"));
        assertTrue(ex.getMessage().contains("project already mapped"));
    }

    @Test
    void saveProjectToServiceline_exception() {
        com.iemr.common.data.customization.ServicelineCustomization[] arr = new com.iemr.common.data.customization.ServicelineCustomization[1];
        com.iemr.common.data.customization.ServicelineCustomization s = new com.iemr.common.data.customization.ServicelineCustomization();
        s.setServiceLineId(1); s.setServiceProviderId(2); s.setStateId(3); s.setDistrictId(4); s.setBlockId(5);
        arr[0] = s;
        when(servicelineCustomizationRepo.findByProjectName(1, 2, 3, 4, 5)).thenThrow(new RuntimeException("fail"));
        String req = new com.google.gson.Gson().toJson(arr);
        Exception ex = assertThrows(Exception.class, () -> customizationService.saveProjectToServiceline(req, "auth"));
        assertEquals("fail", ex.getMessage());
    }

    // --- fetchProjectServiceline ---
    @Test
    void fetchProjectServiceline_success() throws Exception {
        com.iemr.common.data.customization.ServicelineCustomization s = new com.iemr.common.data.customization.ServicelineCustomization();
        s.setServiceLineId(1); s.setServiceLine("sl"); s.setStateId(2); s.setStateName("sn"); s.setDistrictId(3); s.setDistrictName("dn"); s.setServiceProviderId(4);
        java.util.List<com.iemr.common.data.customization.ServicelineCustomization> list = new java.util.ArrayList<>();
        when(servicelineCustomizationRepo.getServicelineProject(1, "sl", 2, "sn", 3, "dn", 4)).thenReturn(list);
        String req = new com.google.gson.Gson().toJson(s);
        String result = customizationService.fetchProjectServiceline(req, "auth");
        assertTrue(result.contains("[]"));
    }

    @Test
    void fetchProjectServiceline_exception() {
        com.iemr.common.data.customization.ServicelineCustomization s = new com.iemr.common.data.customization.ServicelineCustomization();
        s.setServiceLineId(1); s.setServiceLine("sl"); s.setStateId(2); s.setStateName("sn"); s.setDistrictId(3); s.setDistrictName("dn"); s.setServiceProviderId(4);
        when(servicelineCustomizationRepo.getServicelineProject(anyInt(), anyString(), anyInt(), anyString(), anyInt(), anyString(), anyInt())).thenThrow(new RuntimeException("fail"));
        String req = new com.google.gson.Gson().toJson(s);
        Exception ex = assertThrows(Exception.class, () -> customizationService.fetchProjectServiceline(req, "auth"));
        assertEquals("fail", ex.getMessage());
    }

    // --- updateProjectToServiceline ---
    @Test
    void updateProjectToServiceline_success() throws Exception {
        com.iemr.common.data.customization.ServicelineCustomization s = new com.iemr.common.data.customization.ServicelineCustomization();
        s.setServiceProviderId(1); s.setServiceLineId(2); s.setStateId(3); s.setDistrictId(4); s.setBlockId(5);
        when(servicelineCustomizationRepo.findServiceProviderId(1, 2, 3, 4, 5)).thenReturn(10);
        when(servicelineCustomizationRepo.save(any())).thenReturn(s);
        String req = new com.google.gson.Gson().toJson(s);
        String result = customizationService.updateProjectToServiceline(req, "auth");
        assertTrue(result.contains("Project to Serviceline Updated Successfully"));
    }

    @Test
    void updateProjectToServiceline_invalidRequest() {
        com.iemr.common.data.customization.ServicelineCustomization s = new com.iemr.common.data.customization.ServicelineCustomization();
        s.setServiceProviderId(1); s.setServiceLineId(2); s.setStateId(3); s.setDistrictId(4); s.setBlockId(5);
        when(servicelineCustomizationRepo.findServiceProviderId(1, 2, 3, 4, 5)).thenReturn(null);
        String req = new com.google.gson.Gson().toJson(s);
        Exception ex = assertThrows(Exception.class, () -> customizationService.updateProjectToServiceline(req, "auth"));
        assertEquals("Invalid request: please pass valid request", ex.getMessage());
    }

    @Test
    void updateProjectToServiceline_exception() {
        com.iemr.common.data.customization.ServicelineCustomization s = new com.iemr.common.data.customization.ServicelineCustomization();
        s.setServiceProviderId(1); s.setServiceLineId(2); s.setStateId(3); s.setDistrictId(4); s.setBlockId(5);
        when(servicelineCustomizationRepo.findServiceProviderId(anyInt(), anyInt(), anyInt(), anyInt(), anyInt())).thenThrow(new RuntimeException("fail"));
        String req = new com.google.gson.Gson().toJson(s);
        Exception ex = assertThrows(Exception.class, () -> customizationService.updateProjectToServiceline(req, "auth"));
        assertEquals("fail", ex.getMessage());
    }

    // --- getSections ---
    @Test
    void getSections_success() {
        java.util.ArrayList<com.iemr.common.data.customization.SectionMasterCustomization> list = new java.util.ArrayList<>();
        when(sectionMasterCustomizationRepo.findSections()).thenReturn(list);
        String result = customizationService.getSections();
        assertTrue(result.contains("[]"));
    }


    // --- getfileldType ---
    @Test
    void getfileldType_success() {
        java.util.List<com.iemr.common.data.customization.FieldType> list = new java.util.ArrayList<>();
        when(fieldTypeRepo.findFields()).thenReturn(list);
        String result = customizationService.getfileldType();
        assertTrue(result.contains("[]"));
    }

    // --- saveSectionAndFields ---
    @Test
    void saveSectionAndFields_success() throws Exception {
        com.iemr.common.data.customization.SectionFieldsMappingDTO dto = new com.iemr.common.data.customization.SectionFieldsMappingDTO();
        dto.setProjectId(1);
        dto.setSectionId(2);
        dto.setCreatedBy("user");
        dto.setServiceProviderId(3);
        com.iemr.common.data.customization.SectionAndFieldsMapping field = new com.iemr.common.data.customization.SectionAndFieldsMapping();
        field.setFieldName("field1");
        field.setServiceProviderId(3);
        field.setAllowMax(10);
        field.setAllowMin(1);
        field.setFieldTypeId(5);
        field.setFieldType("type");
        field.setRank(1);
        field.setAllowText("true");
        field.setIsRequired(true);
        field.setIsEditable(true);
        field.setFieldTitle("title");
        field.setPlaceholder("ph");
        field.setOptions(new String[] {"opt1", "opt2"});
        java.util.List<com.iemr.common.data.customization.SectionAndFieldsMapping> fields = new java.util.ArrayList<>();
        fields.add(field);
        dto.setFields(fields);
        when(sectionAndFieldsMappingRepo.getByFieldName(anyString(), anyInt(), anyInt())).thenReturn(java.util.Collections.emptyList());
        when(sectionAndFieldsMappingRepo.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
        String result = customizationService.saveSectionAndFields(dto, "auth");
        assertTrue(result.contains("section and fields mapping done successfully"));
    }

    @Test
    void saveSectionAndFields_fieldNameExists() {
        com.iemr.common.data.customization.SectionFieldsMappingDTO dto = new com.iemr.common.data.customization.SectionFieldsMappingDTO();
        dto.setProjectId(1);
        dto.setSectionId(2);
        dto.setCreatedBy("user");
        dto.setServiceProviderId(3);
        com.iemr.common.data.customization.SectionAndFieldsMapping field = new com.iemr.common.data.customization.SectionAndFieldsMapping();
        field.setFieldName("field1");
        field.setServiceProviderId(3);
        java.util.List<com.iemr.common.data.customization.SectionAndFieldsMapping> fields = new java.util.ArrayList<>();
        fields.add(field);
        dto.setFields(fields);
        when(sectionAndFieldsMappingRepo.getByFieldName(anyString(), anyInt(), anyInt())).thenReturn(java.util.Collections.singletonList(field));
        Exception ex = assertThrows(Exception.class, () -> customizationService.saveSectionAndFields(dto, "auth"));
        assertTrue(ex.getMessage().contains("Field name already exists"));
    }

    @Test
    void saveSectionAndFields_invalidRequest() {
        com.iemr.common.data.customization.SectionFieldsMappingDTO dto = new com.iemr.common.data.customization.SectionFieldsMappingDTO();
        Exception ex = assertThrows(Exception.class, () -> customizationService.saveSectionAndFields(dto, "auth"));
        assertTrue(ex.getMessage().contains("Please pass valid request"));
    }

    @Test
    void saveSectionAndFields_repoThrowsException() {
        com.iemr.common.data.customization.SectionFieldsMappingDTO dto = new com.iemr.common.data.customization.SectionFieldsMappingDTO();
        dto.setProjectId(1);
        java.util.List<com.iemr.common.data.customization.SectionAndFieldsMapping> fields = new java.util.ArrayList<>();
        com.iemr.common.data.customization.SectionAndFieldsMapping field = new com.iemr.common.data.customization.SectionAndFieldsMapping();
        field.setFieldName("field1");
        field.setServiceProviderId(null); // matches actual call
        field.setProjectId(1); // matches actual call
        fields.add(field);
        dto.setFields(fields);
        when(sectionAndFieldsMappingRepo.getByFieldName(eq("field1"), eq(null), eq(1))).thenThrow(new RuntimeException("fail"));
        Exception ex = assertThrows(Exception.class, () -> customizationService.saveSectionAndFields(dto, "auth"));
        assertEquals("fail", ex.getMessage());
    }

    // --- fetchMappedFields ---
    @Test
    void fetchMappedFields_success() throws Exception {
        com.iemr.common.data.customization.SectionAndFieldsMapping mapping = new com.iemr.common.data.customization.SectionAndFieldsMapping();
        mapping.setSectionId(1);
        mapping.setServiceProviderId(2);
        mapping.setProjectId(3);
        mapping.setId(10);
        mapping.setFieldName("field");
        mapping.setPlaceholder("ph");
        mapping.setFieldTypeId(5);
        mapping.setFieldType("type");
        mapping.setAllowMin(1);
        mapping.setAllowMax(10);
        mapping.setRank(1);
        mapping.setAllowText("true");
        mapping.setIsRequired(true);
        mapping.setIsEditable(true);
        mapping.setFieldTitle("title");
        mapping.setDeleted(false);
        mapping.setCreatedBy("user");
        mapping.setOption("opt1,opt2");
        when(sectionAndFieldsMappingRepo.findSectionIdAndSectionNameAndServiceProviderId(anyInt(), anyInt(), anyInt())).thenReturn(java.util.Collections.singletonList(mapping));
        when(sectionMasterCustomizationRepo.findSectionName(anyInt())).thenReturn("SectionName");
        String req = new com.google.gson.Gson().toJson(mapping);
        String result = customizationService.fetchMappedFields(req, "auth");
        assertTrue(result.contains("field"));
        assertTrue(result.contains("SectionName"));
    }

    @Test
    void fetchMappedFields_projectIdNull() {
        com.iemr.common.data.customization.SectionAndFieldsMapping mapping = new com.iemr.common.data.customization.SectionAndFieldsMapping();
        mapping.setSectionId(1);
        mapping.setServiceProviderId(2);
        String req = new com.google.gson.Gson().toJson(mapping);
        Exception ex = assertThrows(Exception.class, () -> customizationService.fetchMappedFields(req, "auth"));
        assertTrue(ex.getMessage().contains("ProjectId is required"));
    }

    @Test
    void fetchMappedFields_repoThrowsException() {
        com.iemr.common.data.customization.SectionAndFieldsMapping mapping = new com.iemr.common.data.customization.SectionAndFieldsMapping();
        mapping.setSectionId(1);
        mapping.setServiceProviderId(2);
        mapping.setProjectId(3);
        when(sectionAndFieldsMappingRepo.findSectionIdAndSectionNameAndServiceProviderId(anyInt(), anyInt(), anyInt())).thenThrow(new RuntimeException("fail"));
        String req = new com.google.gson.Gson().toJson(mapping);
        Exception ex = assertThrows(Exception.class, () -> customizationService.fetchMappedFields(req, "auth"));
        assertEquals("fail", ex.getMessage());
    }

    // --- updateSectionAndFields ---
    @Test
    void updateSectionAndFields_success() throws Exception {
        com.iemr.common.data.customization.SectionAndFieldsMapping mapping = new com.iemr.common.data.customization.SectionAndFieldsMapping();
        mapping.setId(1);
        mapping.setProjectId(2);
        mapping.setFieldName("field");
        mapping.setIsRequired(true);
        mapping.setDeleted(false);
        mapping.setIsEditable(true);
        mapping.setFieldTitle("title");
        mapping.setAllowMin(1);
        mapping.setAllowMax(10);
        mapping.setAllowText("true");
        mapping.setPlaceholder("ph");
        mapping.setFieldType("type");
        mapping.setOptions(new String[] {"opt1", "opt2"});
        com.iemr.common.data.customization.ProjectCustomization project = new com.iemr.common.data.customization.ProjectCustomization();
        when(sectionAndFieldsMappingRepo.getById(1)).thenReturn(mapping);
        when(projectCustomizationRepo.findById(2)).thenReturn(java.util.Optional.of(project));
        when(sectionAndFieldsMappingRepo.save(any())).thenReturn(mapping);
        String req = new com.google.gson.Gson().toJson(mapping);
        String result = customizationService.updateSectionAndFields(req, "auth");
        assertTrue(result.contains("section and fields mapping updated successfully"));
    }

    @Test
    void updateSectionAndFields_invalidRequest() {
        com.iemr.common.data.customization.SectionAndFieldsMapping mapping = new com.iemr.common.data.customization.SectionAndFieldsMapping();
        String req = new com.google.gson.Gson().toJson(mapping);
        Exception ex = assertThrows(Exception.class, () -> customizationService.updateSectionAndFields(req, "auth"));
        assertTrue(ex.getMessage().contains("please pass valid request"));
    }

    @Test
    void updateSectionAndFields_repoThrowsException() {
        com.iemr.common.data.customization.SectionAndFieldsMapping mapping = new com.iemr.common.data.customization.SectionAndFieldsMapping();
        mapping.setId(1);
        when(sectionAndFieldsMappingRepo.getById(1)).thenThrow(new RuntimeException("fail"));
        String req = new com.google.gson.Gson().toJson(mapping);
        Exception ex = assertThrows(Exception.class, () -> customizationService.updateSectionAndFields(req, "auth"));
        assertEquals("fail", ex.getMessage());
    }

    // --- fetchAllData ---
    @Test
    void fetchAllData_success() throws Exception {
        com.iemr.common.data.customization.V_CustomizationDataFields data = new com.iemr.common.data.customization.V_CustomizationDataFields();
        data.setServiceLineId(1);
        data.setServiceLine("sl");
        data.setStateId(2);
        data.setDistrictId(3);
        data.setBlockId(4);
        data.setServiceProviderId(5);
        com.iemr.common.data.customization.ServicelineCustomization slc = new com.iemr.common.data.customization.ServicelineCustomization();
        slc.setProjectId(10);
        when(servicelineCustomizationRepo.checkExistingData(anyInt(), anyString(), anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(slc);
        com.iemr.common.data.customization.V_CustomizationDataFields resultField = new com.iemr.common.data.customization.V_CustomizationDataFields();
        resultField.setOption("opt1,opt2");
        java.util.List<com.iemr.common.data.customization.V_CustomizationDataFields> tempList = new java.util.ArrayList<>();
        tempList.add(resultField);
        when(v_CustomizationDataFieldsRepo.getAllData(anyString(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), any())).thenReturn(tempList);
        String req = new com.google.gson.Gson().toJson(data);
        String result = customizationService.fetchAllData(req, "auth");
        assertTrue(result.contains("opt1"));
    }

    @Test
    void fetchAllData_noProjectId() throws Exception {
        com.iemr.common.data.customization.V_CustomizationDataFields data = new com.iemr.common.data.customization.V_CustomizationDataFields();
        data.setServiceLineId(1);
        data.setServiceLine("sl");
        data.setStateId(2);
        data.setDistrictId(3);
        data.setBlockId(4);
        data.setServiceProviderId(5);
        when(servicelineCustomizationRepo.checkExistingData(anyInt(), anyString(), anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(null);
        when(v_CustomizationDataFieldsRepo.getAllData(anyString(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), any())).thenReturn(new java.util.ArrayList<>());
        String req = new com.google.gson.Gson().toJson(data);
        String result = customizationService.fetchAllData(req, "auth");
        assertTrue(result.contains("[]"));
    }

    @Test
    void fetchAllData_repoThrowsException() {
        com.iemr.common.data.customization.V_CustomizationDataFields data = new com.iemr.common.data.customization.V_CustomizationDataFields();
        data.setServiceLineId(1);
        data.setServiceLine("sl");
        data.setStateId(2);
        data.setDistrictId(3);
        data.setBlockId(4);
        data.setServiceProviderId(5);
        when(servicelineCustomizationRepo.checkExistingData(anyInt(), anyString(), anyInt(), anyInt(), anyInt(), anyInt())).thenThrow(new RuntimeException("fail"));
        String req = new com.google.gson.Gson().toJson(data);
        Exception ex = assertThrows(Exception.class, () -> customizationService.fetchAllData(req, "auth"));
        assertEquals("fail", ex.getMessage());
    }

    // --- fetchMappedSectionsInProject ---
    @Test
    void fetchMappedSectionsInProject_success() throws Exception {
        com.iemr.common.data.customization.SectionProjectMapping mapping = new com.iemr.common.data.customization.SectionProjectMapping();
        mapping.setProjectId(1);
        mapping.setProjectName("proj");
        mapping.setServiceProviderId(2);
        java.util.List<com.iemr.common.data.customization.SectionProjectMapping> list = new java.util.ArrayList<>();
        list.add(mapping);
        when(sectionProjectMappingRepo.findMappedSectionsInProject(anyInt(), anyString(), anyInt())).thenReturn(list);
        String req = new com.google.gson.Gson().toJson(mapping);
        String result = customizationService.fetchMappedSectionsInProject(req, "auth");
        assertTrue(result.contains("proj"));
    }

    @Test
    void fetchMappedSectionsInProject_repoThrowsException() {
        com.iemr.common.data.customization.SectionProjectMapping mapping = new com.iemr.common.data.customization.SectionProjectMapping();
        mapping.setProjectId(1);
        mapping.setProjectName("proj");
        mapping.setServiceProviderId(2);
        when(sectionProjectMappingRepo.findMappedSectionsInProject(anyInt(), anyString(), anyInt())).thenThrow(new RuntimeException("fail"));
        String req = new com.google.gson.Gson().toJson(mapping);
        Exception ex = assertThrows(Exception.class, () -> customizationService.fetchMappedSectionsInProject(req, "auth"));
        assertEquals("fail", ex.getMessage());
    }

    // ...existing code...

    @Test
    void mapSectionToProject_success_noExistingMappings() throws Exception {
        // Arrange
        SectionProjectMappingDTO dto = new SectionProjectMappingDTO();
        dto.setProjectId(1);
        dto.setProjectName("Test Project");
        dto.setServiceProviderId(10);
        dto.setCreatedBy("test_user");

        List<SectionProjectMapping> newSections = new ArrayList<>();
        SectionProjectMapping newSection1 = new SectionProjectMapping();
        newSection1.setSectionId(101);
        newSection1.setSectionName("Section A");
        newSections.add(newSection1);
        dto.setSections(newSections);

        when(sectionProjectMappingRepo.findMappedSectionsInProject(1, "Test Project", 10))
            .thenReturn(Collections.emptyList());
        when(sectionProjectMappingRepo.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        String result = customizationService.mapSectionToProject(dto, "some-auth-token");

        // Assert
        verify(sectionProjectMappingRepo, times(1)).findMappedSectionsInProject(1, "Test Project", 10);
        verify(sectionProjectMappingRepo, times(1)).saveAll(sectionProjectMappingListCaptor.capture());

        List<SectionProjectMapping> savedList = sectionProjectMappingListCaptor.getValue();
        assertNotNull(savedList);
        assertEquals(1, savedList.size());

        SectionProjectMapping savedMapping1 = savedList.get(0);
        assertEquals(1, savedMapping1.getProjectId());
        assertEquals("Test Project", savedMapping1.getProjectName());
        assertEquals(10, savedMapping1.getServiceProviderId());
        assertEquals("test_user", savedMapping1.getCreatedBy());
        assertEquals(false, savedMapping1.getDeleted());
        assertEquals(101, savedMapping1.getSectionId());
        assertEquals("Section A", savedMapping1.getSectionName());

        Map<String, Object> expectedResponseMap = new HashMap<>();
        expectedResponseMap.put("response", "section and project mapping done successfully");
        String expectedJson = new Gson().toJson(expectedResponseMap);
        assertEquals(expectedJson, result);
    }

    @Test
    void mapSectionToProject_nullDto_throwsException() {
        // Arrange
        SectionProjectMappingDTO dto = null;

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            customizationService.mapSectionToProject(dto, "some-auth-token");
        });

        assertEquals("Invalid request: please pass valid request", exception.getMessage());
        verify(sectionProjectMappingRepo, never()).findMappedSectionsInProject(any(), any(), any());
        verify(sectionProjectMappingRepo, never()).saveAll(any());
    }

    @Test
    void mapSectionToProject_nullSectionsList_throwsException() {
        // Arrange
        SectionProjectMappingDTO dto = new SectionProjectMappingDTO();
        dto.setProjectId(1);
        dto.setSections(null);

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            customizationService.mapSectionToProject(dto, "some-auth-token");
        });

        assertEquals("Invalid request: please pass valid request", exception.getMessage());
        verify(sectionProjectMappingRepo, never()).findMappedSectionsInProject(any(), any(), any());
        verify(sectionProjectMappingRepo, never()).saveAll(any());
    }

    @Test
    void mapSectionToProject_emptySectionsList_throwsException() {
        // Arrange
        SectionProjectMappingDTO dto = new SectionProjectMappingDTO();
        dto.setProjectId(1);
        dto.setSections(new ArrayList<>());

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            customizationService.mapSectionToProject(dto, "some-auth-token");
        });

        assertEquals("Invalid request: please pass valid request", exception.getMessage());
        verify(sectionProjectMappingRepo, never()).findMappedSectionsInProject(any(), any(), any());
        verify(sectionProjectMappingRepo, never()).saveAll(any());
    }

    @Test
    void mapSectionToProject_repoThrowsException_rethrowsException() {
        // Arrange
        SectionProjectMappingDTO dto = new SectionProjectMappingDTO();
        dto.setProjectId(1);
        dto.setProjectName("Test Project");
        dto.setServiceProviderId(10);
        dto.setSections(Collections.singletonList(new SectionProjectMapping()));

        String exceptionMessage = "Database connection failed";
        when(sectionProjectMappingRepo.findMappedSectionsInProject(1, "Test Project", 10))
            .thenThrow(new RuntimeException(exceptionMessage));

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            customizationService.mapSectionToProject(dto, "some-auth-token");
        });

        assertEquals(exceptionMessage, exception.getMessage());
        verify(sectionProjectMappingRepo, times(1)).findMappedSectionsInProject(1, "Test Project", 10);
        verify(sectionProjectMappingRepo, never()).saveAll(any());
    }
}