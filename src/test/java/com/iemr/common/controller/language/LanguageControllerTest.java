package com.iemr.common.controller.language;

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

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
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
        
        // Verify the content of the returned JSON
        String expectedJson = "{\"data\":[\"Language1\",\"Language2\"],\"statusCode\":200,\"errorMessage\":\"Success\",\"status\":\"Success\"}";
        assertEquals(expectedJson, result);
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
        
        // Verify the content of the returned JSON for error
        assertTrue(result.contains("\"statusCode\":5000"));
        assertTrue(result.contains("\"errorMessage\":\"" + errorMessage + "\""));
        assertTrue(result.contains("\"status\":\"Failed with " + errorMessage + " at "));
    }
}