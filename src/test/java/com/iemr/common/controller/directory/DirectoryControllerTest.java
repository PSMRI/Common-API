package com.iemr.common.controller.directory;

import com.iemr.common.data.directory.Directory;
import com.iemr.common.data.directory.InstituteDirectoryMapping;
import com.iemr.common.data.directory.SubDirectory;
import com.iemr.common.service.directory.DirectoryMappingService;
import com.iemr.common.service.directory.DirectoryService;
import com.iemr.common.service.directory.SubDirectoryService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DirectoryController.class, 
           excludeAutoConfiguration = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
@ContextConfiguration(classes = {DirectoryController.class})
class DirectoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DirectoryService directoryService;

    @MockBean
    private SubDirectoryService subDirectoryService;

    @MockBean
    private DirectoryMappingService directoryMappingService;

    // Test for getDirectory()
    @Test
    void shouldReturnDirectories_whenGetDirectoryIsCalled() throws Exception {
        // Arrange
        // Create a proper list with sample data to avoid JSONObject.put Collection issues
        List<Directory> mockDirectories = Arrays.asList(
            new Directory(1, "Test Directory 1"),
            new Directory(2, "Test Directory 2")
        );
        when(directoryService.getDirectories()).thenReturn(mockDirectories);

        // Act & Assert
        mockMvc.perform(post("/directory/getDirectory")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/plain;charset=UTF-8")) // Controller returns String
                .andExpect(jsonPath("$.data.directory").isArray())
                .andExpect(jsonPath("$.data.directory[0].instituteDirectoryID").value(1))
                .andExpect(jsonPath("$.data.directory[0].instituteDirectoryName").value("Test Directory 1"));
    }

    @Test
    void shouldReturnError_whenGetDirectoryThrowsException() throws Exception {
        // Arrange
        when(directoryService.getDirectories()).thenThrow(new RuntimeException("Service error"));

        // Act & Assert
        mockMvc.perform(post("/directory/getDirectory")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(5000))
                .andExpect(jsonPath("$.errorMessage").exists());
    }

    // Test for getDirectoryV1()
    @Test
    void shouldReturnDirectoriesV1_whenValidProviderServiceMapIDProvided() throws Exception {
        // Arrange
        String requestBody = "{\"providerServiceMapID\":101}";
        List<Directory> mockDirectories = Collections.emptyList();
        when(directoryService.getDirectories(anyInt())).thenReturn(mockDirectories);

        // Act & Assert
        mockMvc.perform(post("/directory/getDirectoryV1")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    void shouldReturnError_whenInvalidRequestBodyForGetDirectoryV1() throws Exception {
        // Arrange
        String invalidRequestBody = "{\"invalidField\":\"value\"}";

        // Act & Assert
        mockMvc.perform(post("/directory/getDirectoryV1")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(5000));
    }

    @Test
    void shouldReturnError_whenGetDirectoryV1ServiceThrowsException() throws Exception {
        // Arrange
        String requestBody = "{\"providerServiceMapID\":101}";
        when(directoryService.getDirectories(anyInt())).thenThrow(new RuntimeException("Service error"));

        // Act & Assert
        mockMvc.perform(post("/directory/getDirectoryV1")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(5000))
                .andExpect(jsonPath("$.errorMessage").exists());
    }

    // Test for getSubDirectory()
    @Test
    void shouldReturnSubDirectories_whenValidInstituteDirectoryIDProvided() throws Exception {
        // Arrange
        String requestBody = "{\"instituteDirectoryID\":201}";
        List<SubDirectory> mockSubDirectories = Collections.emptyList();
        when(subDirectoryService.getSubDirectories(anyInt())).thenReturn(mockSubDirectories);

        // Act & Assert
        mockMvc.perform(post("/directory/getSubDirectory")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    void shouldReturnError_whenInvalidRequestBodyForGetSubDirectory() throws Exception {
        // Arrange
        String invalidRequestBody = "{\"wrongField\":\"value\"}";
        
        // Act & Assert
        mockMvc.perform(post("/directory/getSubDirectory")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(5000));
    }

    @Test
    void shouldReturnError_whenGetSubDirectoryServiceThrowsException() throws Exception {
        // Arrange
        String requestBody = "{\"instituteDirectoryID\":201}";
        when(subDirectoryService.getSubDirectories(anyInt())).thenThrow(new RuntimeException("Service error"));

        // Act & Assert
        mockMvc.perform(post("/directory/getSubDirectory")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(5000))
                .andExpect(jsonPath("$.errorMessage").exists());
    }

    // Test for getInstitutesDirectories()
    @Test
    void shouldReturnInstitutesDirectories_whenValidRequestProvided() throws Exception {
        // Arrange
        String requestBody = "{\"instituteDirectoryID\":1, \"instituteSubDirectoryID\":10, \"stateID\":100, \"districtID\":1000, \"blockID\":10000}";
        List<InstituteDirectoryMapping> mockMappings = Collections.emptyList();
        when(directoryMappingService.findAciveInstituteDirectories(anyString())).thenReturn(mockMappings);

        // Act & Assert
        mockMvc.perform(post("/directory/getInstitutesDirectories")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    void shouldReturnError_whenInvalidRequestBodyForGetInstitutesDirectories() throws Exception {
        // Arrange
        String invalidRequestBody = "{\"invalid_field\":\"value\"}";
        // Mock the service to throw an exception when it receives invalid data
        when(directoryMappingService.findAciveInstituteDirectories(anyString()))
            .thenThrow(new RuntimeException("Invalid request data"));
        
        // Act & Assert
        mockMvc.perform(post("/directory/getInstitutesDirectories")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(5000));
    }

    @Test
    void shouldReturnError_whenGetInstitutesDirectoriesServiceThrowsException() throws Exception {
        // Arrange
        String requestBody = "{\"instituteDirectoryID\":1, \"instituteSubDirectoryID\":10, \"stateID\":100, \"districtID\":1000}";
        when(directoryMappingService.findAciveInstituteDirectories(anyString())).thenThrow(new RuntimeException("Service error"));

        // Act & Assert
        mockMvc.perform(post("/directory/getInstitutesDirectories")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(5000))
                .andExpect(jsonPath("$.errorMessage").exists());
    }
}
