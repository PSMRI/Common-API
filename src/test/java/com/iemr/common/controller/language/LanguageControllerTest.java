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
package com.iemr.common.controller.language;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iemr.common.data.userbeneficiarydata.Language;
import com.iemr.common.service.userbeneficiarydata.LanguageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class LanguageControllerTest {

    @Mock
    private LanguageService languageService;

    @InjectMocks
    private LanguageController languageController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
    }

    @Test
    void setLanguageService_shouldSetService() {
        // Verify that the service is injected by Mockito
        assertNotNull(languageController.languageService, "LanguageService should be set by @InjectMocks");
    }

    @Test
    void getLanguageList_success() throws Exception {
        // Arrange - create actual Language objects instead of mocking the list
        List<Language> mockLanguageList = new ArrayList<>();
        Language lang1 = mock(Language.class);
        Language lang2 = mock(Language.class);
        when(lang1.toString()).thenReturn("Language1");
        when(lang2.toString()).thenReturn("Language2");
        mockLanguageList.add(lang1);
        mockLanguageList.add(lang2);
        
        when(languageService.getActiveLanguages()).thenReturn(mockLanguageList);

        // Act
        String result = languageController.getLanguageList();

        // Assert
        verify(languageService, times(1)).getActiveLanguages();
        
        // Parse the JSON response and verify the fields
        JsonNode jsonNode = objectMapper.readTree(result);
        
        // Verify the structure and values
        assertEquals(200, jsonNode.get("statusCode").asInt());
        assertEquals("Success", jsonNode.get("status").asText());
        assertEquals("Success", jsonNode.get("errorMessage").asText());
        
        // Verify the data array contains expected languages
        JsonNode dataNode = jsonNode.get("data");
        assertTrue(dataNode.isArray());
        assertEquals(2, dataNode.size());
        assertEquals("Language1", dataNode.get(0).asText());
        assertEquals("Language2", dataNode.get(1).asText());
    }

    @Test
    void getLanguageList_exception() throws Exception {
        // Arrange
        String errorMessage = "Database connection failed";
        RuntimeException testException = new RuntimeException(errorMessage);
        when(languageService.getActiveLanguages()).thenThrow(testException);

        // Act
        String result = languageController.getLanguageList();

        // Assert
        verify(languageService, times(1)).getActiveLanguages();
        
        // Parse the JSON response and verify the error fields
        JsonNode jsonNode = objectMapper.readTree(result);
        
        // Verify the error structure and values
        assertEquals(5000, jsonNode.get("statusCode").asInt());
        assertEquals(errorMessage, jsonNode.get("errorMessage").asText());
        
        // Verify the status contains the expected error message and timestamp format
        String statusText = jsonNode.get("status").asText();
        assertTrue(statusText.startsWith("Failed with " + errorMessage + " at "));
        assertTrue(statusText.contains("Please try after some time"));
    }
}