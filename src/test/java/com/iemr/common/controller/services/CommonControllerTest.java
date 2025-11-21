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
package com.iemr.common.controller.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.iemr.common.service.services.CommonService;
import com.iemr.common.service.services.Services;
import com.iemr.common.utils.response.OutputResponse;
import com.iemr.common.data.category.SubCategoryDetails;
import com.iemr.common.data.service.SubService;
import com.iemr.common.data.category.CategoryDetails;
import com.iemr.common.data.users.ServiceMaster;
import java.util.List;
import java.util.ArrayList;

@ExtendWith(MockitoExtension.class)
class CommonControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private CommonController commonController;

    @Mock
    private CommonService commonService;

    @Mock
    private Services services;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(commonController).build();
    }

    @Test
    void getSubcategories_Success() throws Exception {
        String requestJson = "{\"categoryID\":1}";
        String serviceResponseData = "[{\"subCategoryID\":101,\"subCategoryName\":\"SubCategory A\"}]";
        OutputResponse expectedOutputResponse = new OutputResponse();
        expectedOutputResponse.setResponse(serviceResponseData);

        Iterable<SubCategoryDetails> mockedSubCategories = Mockito.mock(Iterable.class);
        Mockito.when(mockedSubCategories.toString()).thenReturn(serviceResponseData);
        Mockito.when(commonService.getSubCategories(Mockito.anyString())).thenReturn(mockedSubCategories);

        mockMvc.perform(MockMvcRequestBuilders.post("/service/subcategory")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .header("Authorization", "Bearer test_token"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(expectedOutputResponse.toString()));
    }

    @Test
    void getSubcategories_ServiceThrowsException() throws Exception {
        String requestJson = "{\"categoryID\":1}";
        String errorMessage = "Test service exception for subcategories";
        
        Mockito.when(commonService.getSubCategories(Mockito.anyString())).thenThrow(new RuntimeException(errorMessage));

        OutputResponse expectedErrorResponse = new OutputResponse();
        expectedErrorResponse.setError(new RuntimeException(errorMessage));

        mockMvc.perform(MockMvcRequestBuilders.post("/service/subcategory")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .header("Authorization", "Bearer test_token"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(expectedErrorResponse.toString()));
    }

    @Test
    void getSubCategoryFilesWithURL_Success() throws Exception {
        String requestJson = "{\"categoryID\":1, \"providerServiceMapID\":10, \"subCategoryID\":101}";
        String serviceResponseData = "[{\"fileName\":\"file1.pdf\",\"fileURL\":\"http://example.com/file1.pdf\"}]";
        OutputResponse expectedOutputResponse = new OutputResponse();
        expectedOutputResponse.setResponse(serviceResponseData);

        List<SubCategoryDetails> mockedSubCategoryFiles = Mockito.mock(List.class);
        Mockito.when(mockedSubCategoryFiles.toString()).thenReturn(serviceResponseData);
        Mockito.when(commonService.getSubCategoryFilesWithURL(Mockito.anyString())).thenReturn(mockedSubCategoryFiles);

        mockMvc.perform(MockMvcRequestBuilders.post("/service/getSubCategoryFilesWithURL")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .header("Authorization", "Bearer test_token"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(expectedOutputResponse.toString()));
    }

    @Test
    void getSubCategoryFilesWithURL_ServiceThrowsException() throws Exception {
        String requestJson = "{\"categoryID\":1, \"providerServiceMapID\":10, \"subCategoryID\":101}";
        String errorMessage = "Test service exception for subcategory files";

        Mockito.when(commonService.getSubCategoryFilesWithURL(Mockito.anyString())).thenThrow(new RuntimeException(errorMessage));

        OutputResponse expectedErrorResponse = new OutputResponse();
        expectedErrorResponse.setError(new RuntimeException(errorMessage));

        mockMvc.perform(MockMvcRequestBuilders.post("/service/getSubCategoryFilesWithURL")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .header("Authorization", "Bearer test_token"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(expectedErrorResponse.toString()));
    }

    @Test
    void getservicetypes_Success() throws Exception {
        String requestJson = "{\"providerServiceMapID\":10}";
        String serviceResponseData = "[{\"serviceTypeID\":1,\"serviceTypeName\":\"Type A\"}]";
        OutputResponse expectedOutputResponse = new OutputResponse();
        expectedOutputResponse.setResponse(serviceResponseData);

        Iterable<SubService> mockedServiceTypes = Mockito.mock(Iterable.class);
        Mockito.when(mockedServiceTypes.toString()).thenReturn(serviceResponseData);
        Mockito.when(commonService.getActiveServiceTypes(Mockito.anyString())).thenReturn(mockedServiceTypes);

        mockMvc.perform(MockMvcRequestBuilders.post("/service/servicetypes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .header("Authorization", "Bearer test_token"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(expectedOutputResponse.toString()));
    }

    @Test
    void getservicetypes_ServiceThrowsException() throws Exception {
        String requestJson = "{\"providerServiceMapID\":10}";
        String errorMessage = "Test service exception for service types";

        Mockito.when(commonService.getActiveServiceTypes(Mockito.anyString())).thenThrow(new RuntimeException(errorMessage));

        OutputResponse expectedErrorResponse = new OutputResponse();
        expectedErrorResponse.setError(new RuntimeException(errorMessage));

        mockMvc.perform(MockMvcRequestBuilders.post("/service/servicetypes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .header("Authorization", "Bearer test_token"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(expectedErrorResponse.toString()));
    }

    @Test
    void serviceList_Success() throws Exception {
        String requestJson = "{}";
        String serviceResponseData = "[{\"serviceID\":1,\"serviceName\":\"Service 1\"},{\"serviceID\":2,\"serviceName\":\"Service 2\"}]";
        OutputResponse expectedOutputResponse = new OutputResponse();
        expectedOutputResponse.setResponse(serviceResponseData);

        List<ServiceMaster> mockedServiceList = Mockito.mock(List.class);
        Mockito.when(mockedServiceList.toString()).thenReturn(serviceResponseData);
        Mockito.when(services.servicesList()).thenReturn(mockedServiceList);

        mockMvc.perform(MockMvcRequestBuilders.post("/service/serviceList")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .header("Authorization", "Bearer test_token"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(expectedOutputResponse.toString()));
    }

    @Test
    void serviceList_ServiceThrowsException() throws Exception {
        String requestJson = "{}";
        String errorMessage = "Test service exception for service list";

        Mockito.when(services.servicesList()).thenThrow(new RuntimeException(errorMessage));

        OutputResponse expectedErrorResponse = new OutputResponse();
        expectedErrorResponse.setError(new RuntimeException(errorMessage));

        mockMvc.perform(MockMvcRequestBuilders.post("/service/serviceList")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .header("Authorization", "Bearer test_token"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(expectedErrorResponse.toString()));
    }

    @Test
    void getSubCategoryFiles_Success() throws Exception {
        String requestJson = "{\"categoryID\":1, \"providerServiceMapID\":10, \"subCategoryID\":101}";
        String serviceResponseData = "[{\"fileName\":\"doc1.pdf\",\"fileURL\":\"http://example.com/doc1.pdf\"}]";
        OutputResponse expectedOutputResponse = new OutputResponse();
        expectedOutputResponse.setResponse(serviceResponseData);

        List<SubCategoryDetails> mockedSubCategoryFiles = Mockito.mock(List.class);
        Mockito.when(mockedSubCategoryFiles.toString()).thenReturn(serviceResponseData);
        Mockito.when(commonService.getSubCategoryFiles(Mockito.anyString())).thenReturn(mockedSubCategoryFiles);

        mockMvc.perform(MockMvcRequestBuilders.post("/service/getSubCategoryFiles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .header("Authorization", "Bearer test_token"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(expectedOutputResponse.toString()));
    }

    @Test
    void getSubCategoryFiles_ServiceThrowsException() throws Exception {
        String requestJson = "{\"categoryID\":1, \"providerServiceMapID\":10, \"subCategoryID\":101}";
        String errorMessage = "Test service exception for getSubCategoryFiles";

        Mockito.when(commonService.getSubCategoryFiles(Mockito.anyString())).thenThrow(new RuntimeException(errorMessage));

        OutputResponse expectedErrorResponse = new OutputResponse();
        expectedErrorResponse.setError(new RuntimeException(errorMessage));

        mockMvc.perform(MockMvcRequestBuilders.post("/service/getSubCategoryFiles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .header("Authorization", "Bearer test_token"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(expectedErrorResponse.toString()));
    }

    @Test
    void getcategoriesById_Success() throws Exception {
        String requestJson = "{\"subServiceID\":\"1\",\"providerServiceMapID\":\"10\"}";
        String serviceResponseData = "[{\"categoryID\":1,\"categoryName\":\"Category X\"}]";
        OutputResponse expectedOutputResponse = new OutputResponse();
        expectedOutputResponse.setResponse(serviceResponseData);

        Iterable<CategoryDetails> mockedCategories = Mockito.mock(Iterable.class);
        Mockito.when(mockedCategories.toString()).thenReturn(serviceResponseData);
        Mockito.when(commonService.getCategories(Mockito.anyString())).thenReturn(mockedCategories);

        mockMvc.perform(MockMvcRequestBuilders.post("/service/categoryByID")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .header("Authorization", "Bearer test_token"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(expectedOutputResponse.toString()));
    }

    @Test
    void getcategoriesById_ServiceThrowsException() throws Exception {
        String requestJson = "{\"subServiceID\":\"1\",\"providerServiceMapID\":\"10\"}";
        String errorMessage = "Test service exception for getcategoriesById";

        Mockito.when(commonService.getCategories(Mockito.anyString())).thenThrow(new RuntimeException(errorMessage));

        OutputResponse expectedErrorResponse = new OutputResponse();
        expectedErrorResponse.setError(new RuntimeException(errorMessage));

        mockMvc.perform(MockMvcRequestBuilders.post("/service/categoryByID")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .header("Authorization", "Bearer test_token"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(expectedErrorResponse.toString()));
    }
}