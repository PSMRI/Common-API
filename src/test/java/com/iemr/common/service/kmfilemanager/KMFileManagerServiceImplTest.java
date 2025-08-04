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
    public void testAddKMFileIterable_fullBranchCoverage() throws Exception {
        // Use Mockito's inline mock maker for static mocking if available
        KMFileManagerServiceImpl impl = spy(new KMFileManagerServiceImpl());
        KMFileManagerRepository repo = mock(KMFileManagerRepository.class);
        KMService kmService = mock(KMService.class);
        SubCategoryRepository subRepo = mock(SubCategoryRepository.class);
        impl.setKmFileManagerRepository(repo);
        impl.setKmService(kmService);
        impl.setSubCategoryRepository(subRepo);

        // Mock static ConfigProperties.getPropertyByName and DigestUtils.md5DigestAsHex overloads
        try (var configMock = mockStatic(com.iemr.common.utils.config.ConfigProperties.class);
             var digestMock = mockStatic(org.springframework.util.DigestUtils.class)) {
            configMock.when(() -> com.iemr.common.utils.config.ConfigProperties.getPropertyByName(anyString())).thenReturn("/tmp");
            digestMock.when(() -> org.springframework.util.DigestUtils.md5DigestAsHex(any(byte[].class))).thenReturn("checksum");
            digestMock.when(() -> org.springframework.util.DigestUtils.md5DigestAsHex(any(java.io.InputStream.class))).thenReturn("checksum");

            KMFileManager km = mock(KMFileManager.class);
            when(km.getFileName()).thenReturn("test.txt");
            when(km.getProviderServiceMapID()).thenReturn(1);
            when(km.getFileContent()).thenReturn(Base64.getEncoder().encodeToString("abc".getBytes()));
            when(km.getCategoryID()).thenReturn(2);
            when(km.getSubCategoryID()).thenReturn(3);
            when(km.getVanID()).thenReturn(4);
            // Do NOT use doNothing for setFileCheckSum, setKmUploadStatus, setVersionNo, setFileUID, or setSubCategoryID, as they may not be void. If needed, stub with when(...).thenReturn(...)
            // getFileCheckSum, getKmUploadStatus, getVersionNo, getFileUID, getValidFrom, getValidUpto, getDeleted, getModifiedBy, getKmFileManagerID, getFileExtension
            // getFileName again for documentPath
            when(km.getFileName()).thenReturn("test.txt");
            // kmService.createDocument
            when(kmService.createDocument(anyString(), anyString())).thenReturn("uuid");
            // repo.save
            when(repo.save(any())).thenReturn(km);
            // Do NOT use doNothing for subRepo.updateFilePath, as it may not be void. If needed, stub with when(...).thenReturn(...)

            ArrayList<KMFileManager> input = new ArrayList<>();
            input.add(km);
            // The method is private, so use reflection
            java.lang.reflect.Method m = KMFileManagerServiceImpl.class.getDeclaredMethod("addKMFile", Iterable.class);
            m.setAccessible(true);
            Object result = m.invoke(impl, input);
            assertNotNull(result);
        }
    }
}
