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
package com.iemr.common.service.scheme;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iemr.common.data.kmfilemanager.KMFileManager;
import com.iemr.common.data.scheme.Scheme;
import com.iemr.common.repository.scheme.SchemeRepository;
import com.iemr.common.service.kmfilemanager.KMFileManagerService;
import com.iemr.common.utils.exception.IEMRException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchemeServiceImplTest {
 

    @InjectMocks
    private SchemeServiceImpl schemeService;

    @Mock
    private SchemeRepository schemeRepository;

    @Mock
    private KMFileManagerService kmFileManagerService;

    @Mock
    private Logger logger;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(schemeService, "logger", logger);
    }

    @Test
    void test_getSchemeByID_success() throws Exception {
        Integer schemeId = 1;
        Scheme expectedScheme = new Scheme();
        expectedScheme.setSchemeID(schemeId);
        expectedScheme.setSchemeName("Test Scheme");

        when(schemeRepository.getSchemeByID(schemeId)).thenReturn(expectedScheme);

        Scheme actualScheme = schemeService.getSchemeByID(schemeId);

        assertNotNull(actualScheme);
        assertEquals(expectedScheme.getSchemeID(), actualScheme.getSchemeID());
        assertEquals(expectedScheme.getSchemeName(), actualScheme.getSchemeName());
        verify(schemeRepository, times(1)).getSchemeByID(schemeId);
    }

    @Test
    void test_getSchemeByID_repositoryThrowsException() {
        Integer schemeId = 1;
        when(schemeRepository.getSchemeByID(schemeId)).thenThrow(new RuntimeException("DB Error"));

        Exception exception = assertThrows(RuntimeException.class, () -> schemeService.getSchemeByID(schemeId));

        assertEquals("DB Error", exception.getMessage());
        verify(schemeRepository, times(1)).getSchemeByID(schemeId);
    }

    @Test
    void test_getSchemeByID_notFound() throws Exception {
        Integer schemeId = 99;
        when(schemeRepository.getSchemeByID(schemeId)).thenReturn(null);

        Scheme actualScheme = schemeService.getSchemeByID(schemeId);

        assertNull(actualScheme);
        verify(schemeRepository, times(1)).getSchemeByID(schemeId);
    }

    @Test
    void test_deletedata_success() {
        Scheme deleteData = new Scheme();
        deleteData.setSchemeID(1);
        deleteData.setDeleted(true);

        when(schemeRepository.save(any(Scheme.class))).thenReturn(deleteData);

        String result = schemeService.deletedata(deleteData);

        assertEquals("success", result);
        verify(schemeRepository, times(1)).save(deleteData);
    }

    @Test
    void test_deletedata_repositoryThrowsException() {
        Scheme deleteData = new Scheme();
        deleteData.setSchemeID(1);
        deleteData.setDeleted(true);

        when(schemeRepository.save(any(Scheme.class))).thenThrow(new RuntimeException("Save failed"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> schemeService.deletedata(deleteData));

        assertEquals("Save failed", exception.getMessage());
        verify(schemeRepository, times(1)).save(deleteData);
    }

    @Test
    void test_save_withNewKmFileManager_success() throws Exception {
        KMFileManager kmFileManager = new KMFileManager();
        kmFileManager.setFileContent("base64content");
        kmFileManager.setFileExtension("pdf");
        kmFileManager.setFileName("document.pdf");

        Scheme schemeRequest = new Scheme();
        schemeRequest.setSchemeID(1);
        schemeRequest.setSchemeName("New Scheme");
        schemeRequest.setKmFileManager(kmFileManager);
        schemeRequest.setKmFileManagerID(null);

        Integer expectedKmFileManagerId = 101;
        String kmFileManagerRespJson = "[{\"kmFileManagerID\":" + expectedKmFileManagerId + ",\"fileUID\":\"uuid123\"}]";

        when(kmFileManagerService.addKMFile(anyString())).thenReturn(kmFileManagerRespJson);
        when(schemeRepository.save(any(Scheme.class))).thenAnswer(invocation -> {
            Scheme savedScheme = invocation.getArgument(0);
            savedScheme.setSchemeID(1);
            return savedScheme;
        });

        Scheme resultScheme = schemeService.save(schemeRequest);

        assertNotNull(resultScheme);
        assertEquals(expectedKmFileManagerId, resultScheme.getKmFileManagerID());
        assertEquals(schemeRequest.getSchemeID(), resultScheme.getSchemeID());
        assertEquals(schemeRequest.getSchemeName(), resultScheme.getSchemeName());
        assertNotNull(resultScheme.getKmFileManager());

        verify(kmFileManagerService, times(1)).addKMFile(anyString());
        verify(schemeRepository, times(1)).save(schemeRequest);

        verify(logger, times(1)).info("KmFileManager: " + schemeRequest.getKmFileManager());
        verify(logger, times(1)).info("FileContent: " + schemeRequest.getKmFileManager().getFileContent());
        verify(logger, times(1)).info("FileExtension: " + schemeRequest.getKmFileManager().getFileExtension());
        verify(logger, times(1)).info("FileName: " + schemeRequest.getKmFileManager().getFileName());
        verify(logger, times(1)).info(eq("addKMFile request: [" + schemeRequest.getKmFileManager().toString() + "]"));
        verify(logger, times(1)).info("addKMFile response " + kmFileManagerRespJson);
    }

    @Test
    void test_save_withExistingKmFileManagerID_success() throws Exception {
        KMFileManager kmFileManager = new KMFileManager();
        kmFileManager.setFileContent("base64content");
        kmFileManager.setFileExtension("pdf");
        kmFileManager.setFileName("document.pdf");

        Scheme schemeRequest = new Scheme();
        schemeRequest.setSchemeID(1);
        schemeRequest.setSchemeName("Existing Scheme");
        schemeRequest.setKmFileManager(kmFileManager);
        schemeRequest.setKmFileManagerID(100);

        when(schemeRepository.save(any(Scheme.class))).thenReturn(schemeRequest);

        Scheme resultScheme = schemeService.save(schemeRequest);

        assertNotNull(resultScheme);
        assertEquals(100, resultScheme.getKmFileManagerID());
        verify(kmFileManagerService, never()).addKMFile(anyString());
        verify(schemeRepository, times(1)).save(schemeRequest);

        verify(logger, never()).info(startsWith("KmFileManager:"));
        verify(logger, never()).info(startsWith("FileContent:"));
        verify(logger, never()).info(startsWith("FileExtension:"));
        verify(logger, never()).info(startsWith("FileName:"));
        verify(logger, never()).info(startsWith("addKMFile request:"));
        verify(logger, never()).info(startsWith("addKMFile response"));
    }

    @Test
    void test_save_withoutKmFileManager_success() throws Exception {
        Scheme schemeRequest = new Scheme();
        schemeRequest.setSchemeID(1);
        schemeRequest.setSchemeName("Scheme without KMFile");
        schemeRequest.setKmFileManager(null);
        schemeRequest.setKmFileManagerID(null);

        when(schemeRepository.save(any(Scheme.class))).thenReturn(schemeRequest);

        Scheme resultScheme = schemeService.save(schemeRequest);

        assertNotNull(resultScheme);
        assertNull(resultScheme.getKmFileManagerID());
        verify(kmFileManagerService, never()).addKMFile(anyString());
        verify(schemeRepository, times(1)).save(schemeRequest);

        verify(logger, never()).info(startsWith("KmFileManager:"));
        verify(logger, never()).info(startsWith("FileContent:"));
        verify(logger, never()).info(startsWith("FileExtension:"));
        verify(logger, never()).info(startsWith("FileName:"));
        verify(logger, never()).info(startsWith("addKMFile request:"));
        verify(logger, never()).info(startsWith("addKMFile response"));
    }

    @Test
    void test_save_withNewKmFileManager_addKMFileThrowsIOException() throws NoSuchAlgorithmException, IOException, IEMRException {
        KMFileManager kmFileManager = new KMFileManager();
        kmFileManager.setFileContent("base64content");
        kmFileManager.setFileExtension("pdf");
        kmFileManager.setFileName("document.pdf");

        Scheme schemeRequest = new Scheme();
        schemeRequest.setSchemeID(1);
        schemeRequest.setKmFileManager(kmFileManager);
        schemeRequest.setKmFileManagerID(null);

        try {
            when(kmFileManagerService.addKMFile(anyString())).thenThrow(new IOException("File upload failed"));
        } catch (IOException | NoSuchAlgorithmException | IEMRException e) {
            fail("Unexpected exception during mock setup: " + e.getMessage());
        }

        IOException exception = assertThrows(IOException.class, () -> schemeService.save(schemeRequest));

        assertEquals("File upload failed", exception.getMessage());
        verify(kmFileManagerService, times(1)).addKMFile(anyString());
        verify(schemeRepository, never()).save(any(Scheme.class));
    }

    @Test
    void test_save_withNewKmFileManager_addKMFileThrowsNoSuchAlgorithmException() throws NoSuchAlgorithmException, IOException, IEMRException
     {
        KMFileManager kmFileManager = new KMFileManager();
        kmFileManager.setFileContent("base64content");
        kmFileManager.setFileExtension("pdf");
        kmFileManager.setFileName("document.pdf");

        Scheme schemeRequest = new Scheme();
        schemeRequest.setSchemeID(1);
        schemeRequest.setKmFileManager(kmFileManager);
        schemeRequest.setKmFileManagerID(null);

        try {
            when(kmFileManagerService.addKMFile(anyString())).thenThrow(new NoSuchAlgorithmException("Algorithm not found"));
        } catch (IOException | NoSuchAlgorithmException | IEMRException e) {
            fail("Unexpected exception during mock setup: " + e.getMessage());
        }

        NoSuchAlgorithmException exception = assertThrows(NoSuchAlgorithmException.class, () -> schemeService.save(schemeRequest));

        assertEquals("Algorithm not found", exception.getMessage());
        verify(kmFileManagerService, times(1)).addKMFile(anyString());
        verify(schemeRepository, never()).save(any(Scheme.class));
    }

    @Test
    void test_save_withNewKmFileManager_addKMFileThrowsIEMRException() throws NoSuchAlgorithmException, IOException, IEMRException
     {
        KMFileManager kmFileManager = new KMFileManager();
        kmFileManager.setFileContent("base64content");
        kmFileManager.setFileExtension("pdf");
        kmFileManager.setFileName("document.pdf");

        Scheme schemeRequest = new Scheme();
        schemeRequest.setSchemeID(1);
        schemeRequest.setKmFileManager(kmFileManager);
        schemeRequest.setKmFileManagerID(null);

        try {
            when(kmFileManagerService.addKMFile(anyString())).thenThrow(new IEMRException("IEMR error"));
        } catch (IOException | NoSuchAlgorithmException | IEMRException e) {
            fail("Unexpected exception during mock setup: " + e.getMessage());
        }

        IEMRException exception = assertThrows(IEMRException.class, () -> schemeService.save(schemeRequest));

        assertEquals("IEMR error", exception.getMessage());
        verify(kmFileManagerService, times(1)).addKMFile(anyString());
        verify(schemeRepository, never()).save(any(Scheme.class));
    }

    @Test
    void test_save_withNewKmFileManager_repositorySaveThrowsException() throws NoSuchAlgorithmException, IOException, IEMRException
     {
        KMFileManager kmFileManager = new KMFileManager();
        kmFileManager.setFileContent("base64content");
        kmFileManager.setFileExtension("pdf");
        kmFileManager.setFileName("document.pdf");

        Scheme schemeRequest = new Scheme();
        schemeRequest.setSchemeID(1);
        schemeRequest.setKmFileManager(kmFileManager);
        schemeRequest.setKmFileManagerID(null);

        Integer expectedKmFileManagerId = 101;
        String kmFileManagerRespJson = "[{\"kmFileManagerID\":" + expectedKmFileManagerId + ",\"fileUID\":\"uuid123\"}]";

        try {
            when(kmFileManagerService.addKMFile(anyString())).thenReturn(kmFileManagerRespJson);
        } catch (IOException | NoSuchAlgorithmException | IEMRException e) {
            fail("Unexpected exception during mock setup: " + e.getMessage());
        }
        when(schemeRepository.save(any(Scheme.class))).thenThrow(new RuntimeException("DB Save Error"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> schemeService.save(schemeRequest));

        assertEquals("DB Save Error", exception.getMessage());
        verify(kmFileManagerService, times(1)).addKMFile(anyString());
        verify(schemeRepository, times(1)).save(schemeRequest);
    }

    @Test
    void test_save_withNewKmFileManager_missingFileContent() throws Exception {
        KMFileManager kmFileManager = new KMFileManager();
        kmFileManager.setFileContent(null);
        kmFileManager.setFileExtension("pdf");
        kmFileManager.setFileName("document.pdf");

        Scheme schemeRequest = new Scheme();
        schemeRequest.setSchemeID(1);
        schemeRequest.setSchemeName("Scheme Missing Content");
        schemeRequest.setKmFileManager(kmFileManager);
        schemeRequest.setKmFileManagerID(null);

        when(schemeRepository.save(any(Scheme.class))).thenReturn(schemeRequest);

        Scheme resultScheme = schemeService.save(schemeRequest);

        assertNotNull(resultScheme);
        assertNull(resultScheme.getKmFileManagerID());
        verify(kmFileManagerService, never()).addKMFile(anyString());
        verify(schemeRepository, times(1)).save(schemeRequest);

        verify(logger, times(1)).info("KmFileManager: " + schemeRequest.getKmFileManager());
        verify(logger, times(1)).info("FileContent: " + schemeRequest.getKmFileManager().getFileContent());
        verify(logger, times(1)).info("FileExtension: " + schemeRequest.getKmFileManager().getFileExtension());
        verify(logger, times(1)).info("FileName: " + schemeRequest.getKmFileManager().getFileName());
        verify(logger, never()).info(startsWith("addKMFile request:"));
        verify(logger, never()).info(startsWith("addKMFile response"));
    }

    @Test
    void test_save_withNewKmFileManager_missingFileExtension() throws Exception {
        KMFileManager kmFileManager = new KMFileManager();
        kmFileManager.setFileContent("base64content");
        kmFileManager.setFileExtension(null);
        kmFileManager.setFileName("document.pdf");

        Scheme schemeRequest = new Scheme();
        schemeRequest.setSchemeID(1);
        schemeRequest.setSchemeName("Scheme Missing Extension");
        schemeRequest.setKmFileManager(kmFileManager);
        schemeRequest.setKmFileManagerID(null);

        when(schemeRepository.save(any(Scheme.class))).thenReturn(schemeRequest);

        Scheme resultScheme = schemeService.save(schemeRequest);

        assertNotNull(resultScheme);
        assertNull(resultScheme.getKmFileManagerID());
        verify(kmFileManagerService, never()).addKMFile(anyString());
        verify(schemeRepository, times(1)).save(schemeRequest);

        verify(logger, times(1)).info("KmFileManager: " + schemeRequest.getKmFileManager());
        verify(logger, times(1)).info("FileContent: " + schemeRequest.getKmFileManager().getFileContent());
        verify(logger, times(1)).info("FileExtension: " + schemeRequest.getKmFileManager().getFileExtension());
        verify(logger, times(1)).info("FileName: " + schemeRequest.getKmFileManager().getFileName());
        verify(logger, never()).info(startsWith("addKMFile request:"));
        verify(logger, never()).info(startsWith("addKMFile response"));
    }

    @Test
    void test_save_withNewKmFileManager_missingFileName() throws Exception {
        KMFileManager kmFileManager = new KMFileManager();
        kmFileManager.setFileContent("base64content");
        kmFileManager.setFileExtension("pdf");
        kmFileManager.setFileName(null);

        Scheme schemeRequest = new Scheme();
        schemeRequest.setSchemeID(1);
        schemeRequest.setSchemeName("Scheme Missing Name");
        schemeRequest.setKmFileManager(kmFileManager);
        schemeRequest.setKmFileManagerID(null);

        when(schemeRepository.save(any(Scheme.class))).thenReturn(schemeRequest);

        Scheme resultScheme = schemeService.save(schemeRequest);

        assertNotNull(resultScheme);
        assertNull(resultScheme.getKmFileManagerID());
        verify(kmFileManagerService, never()).addKMFile(anyString());
        verify(schemeRepository, times(1)).save(schemeRequest);

        verify(logger, times(1)).info("KmFileManager: " + schemeRequest.getKmFileManager());
        verify(logger, times(1)).info("FileContent: " + schemeRequest.getKmFileManager().getFileContent());
        verify(logger, times(1)).info("FileExtension: " + schemeRequest.getKmFileManager().getFileExtension());
        verify(logger, times(1)).info("FileName: " + schemeRequest.getKmFileManager().getFileName());
        verify(logger, never()).info(startsWith("addKMFile request:"));
        verify(logger, never()).info(startsWith("addKMFile response"));
    }

       @Test
    void test_getSchemeList_withValidFileUID() throws Exception {
        Integer providerServiceMapID = 1;
        KMFileManager kmFileManager = new KMFileManager();
        kmFileManager.setFileUID("fileuid123");
        Object[] schemeObj = new Object[] {1, "SchemeName", "Desc", 2, 3, true, "Type", kmFileManager};
        List<Object[]> schemes = new ArrayList<>();
        schemes.add(schemeObj);
        when(schemeRepository.getschemeList(providerServiceMapID)).thenReturn(schemes);

        List<Scheme> result = schemeService.getSchemeList(providerServiceMapID);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("SchemeName", result.get(0).getSchemeName());
        assertEquals("fileuid123", result.get(0).getKmFileManager().getFileUID());
        assertNotNull(result.get(0).getKmFilePath());
    }

    @Test
    void test_getSchemeList_withNullKMFileManager() throws Exception {
        Integer providerServiceMapID = 1;
        Object[] schemeObj = new Object[] {1, "SchemeName", "Desc", 2, 3, true, "Type", null};
        List<Object[]> schemes = new ArrayList<>();
        schemes.add(schemeObj);
        when(schemeRepository.getschemeList(providerServiceMapID)).thenReturn(schemes);

        List<Scheme> result = schemeService.getSchemeList(providerServiceMapID);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertNull(result.get(0).getKmFilePath());
    }

    @Test
    void test_getSchemeList_withKMFileManagerNullFileUID() throws Exception {
        Integer providerServiceMapID = 1;
        KMFileManager kmFileManager = new KMFileManager();
        kmFileManager.setFileUID(null);
        Object[] schemeObj = new Object[] {1, "SchemeName", "Desc", 2, 3, true, "Type", kmFileManager};
        List<Object[]> schemes = new ArrayList<>();
        schemes.add(schemeObj);
        when(schemeRepository.getschemeList(providerServiceMapID)).thenReturn(schemes);

        List<Scheme> result = schemeService.getSchemeList(providerServiceMapID);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertNull(result.get(0).getKmFilePath());
    }
}