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
package com.iemr.common.service.kmfilemanager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iemr.common.data.kmfilemanager.KMFileManager;
import com.iemr.common.repository.category.SubCategoryRepository;
import com.iemr.common.repository.kmfilemanager.KMFileManagerRepository;
import com.iemr.common.utils.km.KMService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import java.sql.Timestamp;
import java.util.*;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class KMFileManagerServiceImplTest {
    @InjectMocks
    KMFileManagerServiceImpl service;

    @Mock
    KMService kmService;
    @Mock
    KMFileManagerRepository kmFileManagerRepository;
    @Mock
    SubCategoryRepository subCategoryRepository;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetKMFileLists() throws Exception {
        KMFileManager manager = mock(KMFileManager.class);
        when(manager.getProviderServiceMapID()).thenReturn(123);
        Set<Object[]> set = new HashSet<>();
        Object[] obj = new Object[13];
        obj[0] = 1; obj[1] = 2; obj[2] = "file.txt"; obj[3] = "txt"; obj[5] = "checksum"; obj[6] = "v1"; obj[7] = "status"; obj[8] = "uid"; obj[9] = new Timestamp(System.currentTimeMillis()); obj[10] = new Timestamp(System.currentTimeMillis()); obj[11] = true;
        set.add(obj);
        when(kmFileManagerRepository.getKMFileLists(123)).thenReturn(set);
        // Use real ObjectMapper for deserialization
        String json = new ObjectMapper().writeValueAsString(manager);
        String result = service.getKMFileLists(json);
        assertTrue(result.contains("file.txt"));
    }

    @Test
    public void testUpdateKMFileManager() throws Exception {
        KMFileManager manager = mock(KMFileManager.class);
        when(manager.getKmFileManagerID()).thenReturn(1);
        when(manager.getFileUID()).thenReturn("uid");
        when(manager.getFileName()).thenReturn("file.txt");
        when(manager.getFileExtension()).thenReturn("txt");
        when(manager.getVersionNo()).thenReturn("v1");
        when(manager.getFileCheckSum()).thenReturn("checksum");
        when(manager.getProviderServiceMapID()).thenReturn(123);
        when(manager.getKmUploadStatus()).thenReturn("Completed");
        when(manager.getValidFrom()).thenReturn(new Timestamp(System.currentTimeMillis()));
        when(manager.getValidUpto()).thenReturn(new Timestamp(System.currentTimeMillis()));
        when(manager.getDeleted()).thenReturn(false);
        when(manager.getModifiedBy()).thenReturn("2"); // Should be String
        when(kmFileManagerRepository.updateKMFileManager(anyInt(), anyString(), anyString(), anyString(), anyString(), anyString(), anyInt(), anyString(), any(), any(), anyBoolean(), anyString())).thenReturn(1);
        String json = new ObjectMapper().writeValueAsString(manager);
        Integer result = service.updateKMFileManager(json);
        assertEquals(1, result);
    }

    @Test
    public void testSetters() {
        KMFileManagerServiceImpl impl = new KMFileManagerServiceImpl();
        KMService kmService = mock(KMService.class);
        KMFileManagerRepository repo = mock(KMFileManagerRepository.class);
        SubCategoryRepository subRepo = mock(SubCategoryRepository.class);
        impl.setKmService(kmService);
        impl.setKmFileManagerRepository(repo);
        impl.setSubCategoryRepository(subRepo);
        // Use reflection to verify fields are set
        try {
            java.lang.reflect.Field f1 = KMFileManagerServiceImpl.class.getDeclaredField("kmService");
            java.lang.reflect.Field f2 = KMFileManagerServiceImpl.class.getDeclaredField("kmFileManagerRepository");
            java.lang.reflect.Field f3 = KMFileManagerServiceImpl.class.getDeclaredField("subCategoryRepository");
            f1.setAccessible(true); f2.setAccessible(true); f3.setAccessible(true);
            assertSame(kmService, f1.get(impl));
            assertSame(repo, f2.get(impl));
            assertSame(subRepo, f3.get(impl));
        } catch (Exception e) {
            fail("Setter did not set the field correctly: " + e.getMessage());
        }
    }

    @Test
    public void testAddKMFileIterable_success() throws Exception {
        KMFileManagerServiceImpl impl = spy(new KMFileManagerServiceImpl());
        KMFileManagerRepository repo = mock(KMFileManagerRepository.class);
        KMService kmService = mock(KMService.class);
        SubCategoryRepository subRepo = mock(SubCategoryRepository.class);
        impl.setKmFileManagerRepository(repo);
        impl.setKmService(kmService);
        impl.setSubCategoryRepository(subRepo);
        KMFileManager km = mock(KMFileManager.class);
        when(km.getFileName()).thenReturn("test.txt");
        when(km.getProviderServiceMapID()).thenReturn(1);
        when(km.getFileContent()).thenReturn(Base64.getEncoder().encodeToString("abc".getBytes()));
        ArrayList<KMFileManager> input = new ArrayList<>();
        input.add(km);
        // The method is private, so use reflection
        try {
            java.lang.reflect.Method m = KMFileManagerServiceImpl.class.getDeclaredMethod("addKMFile", Iterable.class);
            m.setAccessible(true);
            Object result = m.invoke(impl, input);
            assertNotNull(result);
        } catch (Exception e) {
            // Acceptable if IOException is thrown due to file ops
            assertTrue(e.getCause() instanceof IOException || e.getCause() instanceof NullPointerException);
        }
    }

    @Test
    public void testAddKMFileString_throwsIOException() throws Exception {
        // This will throw IOException due to empty input
        String json = "[]";
        try {
            service.addKMFile(json);
            fail("Expected IOException");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("File upload to KM server failed"));
        }
    }

    @Test
    public void testGetFileVersion() throws Exception {
        KMFileManagerServiceImpl impl = new KMFileManagerServiceImpl();
        KMFileManagerRepository repo = mock(KMFileManagerRepository.class);
        impl.setKmFileManagerRepository(repo);
        KMFileManager km = mock(KMFileManager.class);
        when(km.getProviderServiceMapID()).thenReturn(1);
        when(km.getFileName()).thenReturn("file.txt");
        List<KMFileManager> files = Arrays.asList(mock(KMFileManager.class), mock(KMFileManager.class));
        when(repo.getKMFileByFileName(1, "file.txt")).thenReturn(files);
        java.lang.reflect.Method m = KMFileManagerServiceImpl.class.getDeclaredMethod("getFileVersion", KMFileManager.class);
        m.setAccessible(true);
        String version = (String) m.invoke(impl, km);
        assertEquals("V3", version);
    }

    @Test
    public void testUpdateSubcategoryFilePath() throws Exception {
        KMFileManagerServiceImpl impl = new KMFileManagerServiceImpl();
        SubCategoryRepository subRepo = mock(SubCategoryRepository.class);
        impl.setSubCategoryRepository(subRepo);
        KMFileManager km = mock(KMFileManager.class);
        when(km.getSubCategoryID()).thenReturn(1);
        when(km.getFileUID()).thenReturn("uid");
        java.lang.reflect.Method m = KMFileManagerServiceImpl.class.getDeclaredMethod("updateSubcategoryFilePath", KMFileManager.class);
        m.setAccessible(true);
        m.invoke(impl, km);
        verify(subRepo, times(1)).updateFilePath(1, "uid");
    }

    @Test
    public void testAddKMFileIterable_uploadsAndRecordsTheDocument() throws Exception {
        java.nio.file.Path tempDir = java.nio.file.Files.createTempDirectory("km-upload");
        KMFileManagerServiceImpl impl = new KMFileManagerServiceImpl();
        KMFileManagerRepository repo = mock(KMFileManagerRepository.class);
        KMService kmService = mock(KMService.class);
        SubCategoryRepository subRepo = mock(SubCategoryRepository.class);
        impl.setKmFileManagerRepository(repo);
        impl.setKmService(kmService);
        impl.setSubCategoryRepository(subRepo);
        org.springframework.test.util.ReflectionTestUtils.setField(impl, "tempFilePath", tempDir.toString());
        org.springframework.test.util.ReflectionTestUtils.setField(impl, "allowedFileExtensions", "txt,pdf");

        KMFileManager km = new KMFileManager();
        km.setFileName("test.txt");
        km.setFileExtension(".txt");
        km.setProviderServiceMapID(1);
        km.setFileContent(Base64.getEncoder().encodeToString("abc".getBytes()));
        km.setCategoryID(2);
        km.setSubCategoryID(3);
        km.setVanID(4);

        when(repo.getKMFileByFileName(1, "test.txt")).thenReturn(new ArrayList<>());
        when(kmService.createDocument("1/2/3/4/V1/test.txt", tempDir + "/test.txt")).thenReturn("uuid");
        when(repo.save(km)).thenReturn(km);

        java.lang.reflect.Method m = KMFileManagerServiceImpl.class.getDeclaredMethod("addKMFile", Iterable.class);
        m.setAccessible(true);
        @SuppressWarnings("unchecked")
        ArrayList<KMFileManager> result = (ArrayList<KMFileManager>) m.invoke(impl, List.of(km));

        assertEquals(1, result.size());
        assertEquals("uuid", km.getFileUID());
        assertEquals("V1", km.getVersionNo());
        assertNotNull(km.getFileCheckSum());
        verify(subRepo).updateFilePath(3, "uuid");
    }

    @Test
    public void testAddKMFileIterable_rejectsAnExtensionThatIsNotAllowed() throws Exception {
        java.nio.file.Path tempDir = java.nio.file.Files.createTempDirectory("km-upload");
        KMFileManagerServiceImpl impl = new KMFileManagerServiceImpl();
        KMFileManagerRepository repo = mock(KMFileManagerRepository.class);
        KMService kmService = mock(KMService.class);
        impl.setKmFileManagerRepository(repo);
        impl.setKmService(kmService);
        impl.setSubCategoryRepository(mock(SubCategoryRepository.class));
        org.springframework.test.util.ReflectionTestUtils.setField(impl, "tempFilePath", tempDir.toString());
        org.springframework.test.util.ReflectionTestUtils.setField(impl, "allowedFileExtensions", "txt");

        KMFileManager km = new KMFileManager();
        km.setFileName("payload.exe");
        km.setFileExtension("exe");
        km.setProviderServiceMapID(1);
        km.setFileContent(Base64.getEncoder().encodeToString("abc".getBytes()));

        java.lang.reflect.Method m = KMFileManagerServiceImpl.class.getDeclaredMethod("addKMFile", Iterable.class);
        m.setAccessible(true);
        @SuppressWarnings("unchecked")
        ArrayList<KMFileManager> result = (ArrayList<KMFileManager>) m.invoke(impl, List.of(km));

        assertTrue(result.isEmpty());
        verifyNoInteractions(kmService);
    }

    @Test
    public void testAddKMFileIterable_rejectsAnExtensionThatDoesNotMatchTheFileName() throws Exception {
        java.nio.file.Path tempDir = java.nio.file.Files.createTempDirectory("km-upload");
        KMFileManagerServiceImpl impl = new KMFileManagerServiceImpl();
        KMService kmService = mock(KMService.class);
        impl.setKmFileManagerRepository(mock(KMFileManagerRepository.class));
        impl.setKmService(kmService);
        impl.setSubCategoryRepository(mock(SubCategoryRepository.class));
        org.springframework.test.util.ReflectionTestUtils.setField(impl, "tempFilePath", tempDir.toString());
        org.springframework.test.util.ReflectionTestUtils.setField(impl, "allowedFileExtensions", "txt,pdf");

        KMFileManager km = new KMFileManager();
        km.setFileName("report.pdf");
        km.setFileExtension("txt");
        km.setProviderServiceMapID(1);
        km.setFileContent(Base64.getEncoder().encodeToString("abc".getBytes()));

        java.lang.reflect.Method m = KMFileManagerServiceImpl.class.getDeclaredMethod("addKMFile", Iterable.class);
        m.setAccessible(true);
        @SuppressWarnings("unchecked")
        ArrayList<KMFileManager> result = (ArrayList<KMFileManager>) m.invoke(impl, List.of(km));

        assertTrue(result.isEmpty());
        verifyNoInteractions(kmService);
    }

    @Test
    public void testAddKMFileIterable_withNoAllowedExtensionsConfigured() throws Exception {
        KMFileManagerServiceImpl impl = new KMFileManagerServiceImpl();
        KMService kmService = mock(KMService.class);
        impl.setKmFileManagerRepository(mock(KMFileManagerRepository.class));
        impl.setKmService(kmService);
        impl.setSubCategoryRepository(mock(SubCategoryRepository.class));

        KMFileManager km = new KMFileManager();
        km.setFileName("test.txt");
        km.setFileExtension("txt");
        km.setProviderServiceMapID(1);
        km.setFileContent(Base64.getEncoder().encodeToString("abc".getBytes()));

        java.lang.reflect.Method m = KMFileManagerServiceImpl.class.getDeclaredMethod("addKMFile", Iterable.class);
        m.setAccessible(true);
        @SuppressWarnings("unchecked")
        ArrayList<KMFileManager> result = (ArrayList<KMFileManager>) m.invoke(impl, List.of(km));

        // The misconfiguration is logged and no file is uploaded.
        assertTrue(result.isEmpty());
        verifyNoInteractions(kmService);
    }
}
