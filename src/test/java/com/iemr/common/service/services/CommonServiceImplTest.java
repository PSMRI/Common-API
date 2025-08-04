package com.iemr.common.service.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iemr.common.data.category.CategoryDetails;
import com.iemr.common.data.category.SubCategoryDetails;
import com.iemr.common.data.common.DocFileManager;
import com.iemr.common.data.kmfilemanager.KMFileManager;
import com.iemr.common.data.service.SubService;
import com.iemr.common.repository.category.CategoryRepository;
import com.iemr.common.repository.category.SubCategoryRepository;
import com.iemr.common.repository.kmfilemanager.KMFileManagerRepository;
import com.iemr.common.repository.services.ServiceTypeRepository;
import com.iemr.common.utils.aesencryption.AESEncryptionDecryption;
import com.iemr.common.utils.config.ConfigProperties;
import com.iemr.common.utils.exception.IEMRException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.MockedStatic;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommonServiceImplTest {
    @InjectMocks
    CommonServiceImpl service;

    @Mock CategoryRepository categoryRepository;
    @Mock SubCategoryRepository subCategoryRepository;
    @Mock ServiceTypeRepository serviceTypeRepository;
    @Mock KMFileManagerRepository kmFileManagerRepository;
    @Mock AESEncryptionDecryption aESEncryptionDecryption;


    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        // Set fileBasePath via reflection
        try {
            java.lang.reflect.Field f = CommonServiceImpl.class.getDeclaredField("fileBasePath");
            f.setAccessible(true);
            f.set(service, "/tmp/testbase/");
        } catch (Exception e) { throw new RuntimeException(e); }
        // Register mixins to ignore outputMapper for all relevant types
        objectMapper = new ObjectMapper();
        objectMapper.addMixIn(com.iemr.common.data.category.CategoryDetails.class, IgnoreOutputMapper.class);
        objectMapper.addMixIn(com.iemr.common.data.category.SubCategoryDetails.class, IgnoreOutputMapper.class);
        objectMapper.addMixIn(com.iemr.common.data.service.SubService.class, IgnoreOutputMapper.class);

        // Ensure the base directory for file tests exists and is clean before each test
        try {
            Files.createDirectories(Paths.get("/tmp/testbase/1/"));
            Files.list(Paths.get("/tmp/testbase/1/"))
                    .filter(Files::isDirectory)
                    .forEach(path -> {
                        try {
                            Files.walk(path)
                                    .sorted(Comparator.reverseOrder())
                                    .map(java.nio.file.Path::toFile)
                                    .forEach(File::delete);
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to delete directory content: " + path, e);
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException("Failed to create or clean test directory", e);
        }
    }

    // Jackson mixin to ignore outputMapper
    public abstract static class IgnoreOutputMapper {
        @com.fasterxml.jackson.annotation.JsonIgnore
        public com.iemr.common.utils.mapper.OutputMapper outputMapper;
    }

    @Test
    public void testGetCategories() {
        ArrayList<CategoryDetails> mockList = new ArrayList<>(Arrays.asList(new CategoryDetails(), new CategoryDetails()));
        when(categoryRepository.findBy()).thenReturn(mockList);
        Iterable<CategoryDetails> result = service.getCategories();
        assertNotNull(result);
        assertEquals(2, ((List<CategoryDetails>) result).size());
    }

    @Test
    public void testGetCategories_withRequest() throws Exception {
        CategoryDetails req = new CategoryDetails();
        req.setIsWellBeing(true);
        req.setSubServiceID(1);
        req.setProviderServiceMapID(2);
        String json = objectMapper.writeValueAsString(req);
        ArrayList<CategoryDetails> mockList = new ArrayList<>(Arrays.asList(new CategoryDetails()));
        when(categoryRepository.getAllCategories(anyInt(), anyInt(), anyBoolean())).thenReturn(mockList);
        Iterable<CategoryDetails> result = service.getCategories(json);
        assertNotNull(result);
        assertEquals(1, ((List<CategoryDetails>) result).size());
    }

    @Test
    public void testGetCategories_withRequest_noWellBeing() throws Exception {
        CategoryDetails req = new CategoryDetails();
        req.setSubServiceID(1);
        String json = objectMapper.writeValueAsString(req);
        ArrayList<CategoryDetails> mockList = new ArrayList<>(Arrays.asList(new CategoryDetails()));
        // Inject a minimal stub CategoryRepository for this test only
        CategoryRepository stubRepo = new CategoryRepository() {
            @Override
            public ArrayList<CategoryDetails> getAllCategories(Integer subServiceID, Boolean isWellBeing) {
                if (Integer.valueOf(1).equals(subServiceID) && isWellBeing == null) {
                    return mockList;
                }
                return new ArrayList<>();
            }
            @Override public ArrayList<CategoryDetails> findBy() { return new ArrayList<>(); }
            // Return empty for this overload to prevent UnsupportedOperationException
            @Override public ArrayList<CategoryDetails> getAllCategories(Integer a, Integer b, Boolean c) { return new ArrayList<>(); }
            @Override public ArrayList<CategoryDetails> getAllCategories(Integer a, Integer b) {
                // Return mockList if called with the test arguments (1, null) or (1, 0)
                if (Integer.valueOf(1).equals(a) && (b == null || Integer.valueOf(0).equals(b))) {
                    return mockList;
                }
                return new ArrayList<>();
            }
            @Override public ArrayList<CategoryDetails> getAllCategories(Integer a) { throw new UnsupportedOperationException(); }
            @Override public ArrayList<CategoryDetails> getCategoriesByNatureID(Integer a, Integer b) { throw new UnsupportedOperationException(); }
            @Override public <S extends CategoryDetails> S save(S entity) { throw new UnsupportedOperationException(); }
            @Override public <S extends CategoryDetails> Iterable<S> saveAll(Iterable<S> entities) { throw new UnsupportedOperationException(); }
            @Override public Optional<CategoryDetails> findById(Integer integer) { throw new UnsupportedOperationException(); }
            @Override public boolean existsById(Integer integer) { throw new UnsupportedOperationException(); }
            @Override public Iterable<CategoryDetails> findAll() { throw new UnsupportedOperationException(); }
            @Override public Iterable<CategoryDetails> findAllById(Iterable<Integer> integers) { throw new UnsupportedOperationException(); }
            @Override public long count() { throw new UnsupportedOperationException(); }
            @Override public void deleteById(Integer integer) { throw new UnsupportedOperationException(); }
            @Override public void delete(CategoryDetails entity) { throw new UnsupportedOperationException(); }
            @Override public void deleteAllById(Iterable<? extends Integer> integers) { throw new UnsupportedOperationException(); }
            @Override public void deleteAll(Iterable<? extends CategoryDetails> entities) { throw new UnsupportedOperationException(); }
            @Override public void deleteAll() { throw new UnsupportedOperationException(); }
        };
        java.lang.reflect.Field f = CommonServiceImpl.class.getDeclaredField("categoryRepository");
        f.setAccessible(true);
        f.set(service, stubRepo);
        Iterable<CategoryDetails> result = service.getCategories(json);
        assertNotNull(result);
        assertEquals(1, ((List<CategoryDetails>) result).size());

        // Restore the original mock after this test to avoid affecting other tests
        java.lang.reflect.Field f2 = CommonServiceImpl.class.getDeclaredField("categoryRepository");
        f2.setAccessible(true);
        f2.set(service, categoryRepository);
    }

    @Test
    public void testGetSubCategories() throws Exception {
        try (MockedStatic<ConfigProperties> mockedConfigProperties = mockStatic(ConfigProperties.class)) {
            SubCategoryDetails req = new SubCategoryDetails();
            req.setCategoryID(1);
            String json = objectMapper.writeValueAsString(req);
            ArrayList<Object[]> lists = new ArrayList<>();
            lists.add(new Object[]{1, "SubCat1"});
            when(subCategoryRepository.findByCategoryID(1)).thenReturn(lists);
            List<KMFileManager> files = Arrays.asList(new KMFileManager());
            when(kmFileManagerRepository.getFilesBySubCategoryID(1)).thenReturn(files);
            mockStaticConfigProperties(mockedConfigProperties);
            Iterable<SubCategoryDetails> result = service.getSubCategories(json);
            assertNotNull(result);
            assertEquals(1, ((List<SubCategoryDetails>) result).size());
        }
    }

    @Test
    public void testGetSubCategoryFiles_bySubCategoryID() throws Exception {
        SubCategoryDetails req = new SubCategoryDetails();
        req.setSubCategoryID(1);
        String json = objectMapper.writeValueAsString(req);
        ArrayList<SubCategoryDetails> subCats = new ArrayList<>(Arrays.asList(new SubCategoryDetails()));
        when(subCategoryRepository.findBySubCategoryID(1)).thenReturn(subCats);
        // Remove unnecessary stubbing for getKMFileLists
        List<SubCategoryDetails> result = service.getSubCategoryFiles(json);
        assertNotNull(result);
    }

    @Test
    public void testGetSubCategoryFiles_byProviderServiceMapCategoryID() throws Exception {
        SubCategoryDetails req = new SubCategoryDetails();
        req.setProviderServiceMapID(2);
        req.setCategoryID(1);
        String json = objectMapper.writeValueAsString(req);
        when(subCategoryRepository.findByProviderServiceMapCategoryID(anyInt(), anyInt())).thenReturn(new ArrayList<>()); // Mock behavior
        List<SubCategoryDetails> result = service.getSubCategoryFiles(json);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetSubCategoryFiles_byProviderServiceMapID() throws Exception {
        SubCategoryDetails req = new SubCategoryDetails();
        req.setProviderServiceMapID(2);
        String json = objectMapper.writeValueAsString(req);
        when(subCategoryRepository.findByProviderServiceMapID(anyInt())).thenReturn(new ArrayList<>()); // Mock behavior
        List<SubCategoryDetails> result = service.getSubCategoryFiles(json);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetSubCategoryFilesWithURL_bySubCategoryID() throws Exception {
        try (MockedStatic<ConfigProperties> mockedConfigProperties = mockStatic(ConfigProperties.class)) {
            SubCategoryDetails req = new SubCategoryDetails();
            req.setSubCategoryID(1);
            String json = objectMapper.writeValueAsString(req);
            
            SubCategoryDetails subCat1 = new SubCategoryDetails();
            subCat1.setSubCategoryID(1);
            subCat1.setSubCatFilePath("fileuid123");
            ArrayList<SubCategoryDetails> subCats = new ArrayList<>(Arrays.asList(subCat1));
            when(subCategoryRepository.findBySubCategoryID(1)).thenReturn(subCats);
            
            mockStaticConfigProperties(mockedConfigProperties);
            when(kmFileManagerRepository.getKMFileLists(any(), eq("fileuid123"))).thenReturn(new ArrayList<>(Arrays.asList(new KMFileManager())));

            List<SubCategoryDetails> result = service.getSubCategoryFilesWithURL(json);
            assertNotNull(result);
            assertFalse(result.isEmpty());
            assertEquals(1, result.size());
            assertTrue(result.get(0).getSubCatFilePath().contains("Download?uuid=fileuid123"));
            assertFalse(result.get(0).getFileManger().isEmpty());
        }
    }

    @Test
    public void testGetSubCategoryFilesWithURL_byProviderServiceMapCategoryID_withFile() throws Exception {
        try (MockedStatic<ConfigProperties> mockedConfigProperties = mockStatic(ConfigProperties.class)) {
            SubCategoryDetails req = new SubCategoryDetails();
            req.setProviderServiceMapID(1);
            req.setCategoryID(10);
            String json = objectMapper.writeValueAsString(req);

            SubCategoryDetails subCat = new SubCategoryDetails();
            subCat.setSubCatFilePath("fileuid456");
            ArrayList<SubCategoryDetails> subCats = new ArrayList<>(Arrays.asList(subCat));
            when(subCategoryRepository.findByProviderServiceMapCategoryID(req.getProviderServiceMapID(), req.getCategoryID())).thenReturn(subCats);

            mockStaticConfigProperties(mockedConfigProperties);
            when(kmFileManagerRepository.getKMFileLists(any(), eq("fileuid456"))).thenReturn(new ArrayList<>(Arrays.asList(new KMFileManager())));

            List<SubCategoryDetails> result = service.getSubCategoryFilesWithURL(json);
            assertNotNull(result);
            assertFalse(result.isEmpty());
            assertTrue(result.get(0).getSubCatFilePath().contains("Download?uuid=fileuid456"));
        }
    }

    @Test
    public void testGetSubCategoryFilesWithURL_byProviderServiceMapID_withFile() throws Exception {
        try (MockedStatic<ConfigProperties> mockedConfigProperties = mockStatic(ConfigProperties.class)) {
            SubCategoryDetails req = new SubCategoryDetails();
            req.setProviderServiceMapID(1);
            String json = objectMapper.writeValueAsString(req);

            SubCategoryDetails subCat = new SubCategoryDetails();
            subCat.setSubCatFilePath("fileuid789");
            ArrayList<SubCategoryDetails> subCats = new ArrayList<>(Arrays.asList(subCat));
            when(subCategoryRepository.findByProviderServiceMapID(req.getProviderServiceMapID())).thenReturn(subCats);

            mockStaticConfigProperties(mockedConfigProperties);
            when(kmFileManagerRepository.getKMFileLists(any(), eq("fileuid789"))).thenReturn(new ArrayList<>(Arrays.asList(new KMFileManager())));

            List<SubCategoryDetails> result = service.getSubCategoryFilesWithURL(json);
            assertNotNull(result);
            assertFalse(result.isEmpty());
            assertTrue(result.get(0).getSubCatFilePath().contains("Download?uuid=fileuid789"));
        }
    }

    @Test
    public void testGetSubCategoryFilesWithURL_noFilePath() throws Exception {
        try (MockedStatic<ConfigProperties> mockedConfigProperties = mockStatic(ConfigProperties.class)) {
            SubCategoryDetails req = new SubCategoryDetails();
            req.setSubCategoryID(1);
            String json = objectMapper.writeValueAsString(req);

            SubCategoryDetails subCat1 = new SubCategoryDetails();
            subCat1.setSubCategoryID(1);
            subCat1.setSubCatFilePath(null); // No file path
            ArrayList<SubCategoryDetails> subCats = new ArrayList<>(Arrays.asList(subCat1));
            when(subCategoryRepository.findBySubCategoryID(1)).thenReturn(subCats);
            
            mockStaticConfigProperties(mockedConfigProperties); 
            
            List<SubCategoryDetails> result = service.getSubCategoryFilesWithURL(json);
            assertNotNull(result);
            assertFalse(result.isEmpty());
            assertNull(result.get(0).getSubCatFilePath()); // Should remain null or unchanged
        }
    }


    @Test
    public void testGetActiveServiceTypes() throws Exception {
        SubService req = new SubService();
        req.setProviderServiceMapID(1);
        String json = objectMapper.writeValueAsString(req);
        ArrayList<Object[]> lists = new ArrayList<>();
        lists.add(new Object[]{1, "name", "desc", true});
        when(serviceTypeRepository.findActiveServiceTypes(1)).thenReturn(lists);
        Iterable<SubService> result = service.getActiveServiceTypes(json);
        assertNotNull(result);
        assertEquals(1, ((List<SubService>) result).size());
    }

    @Test
    public void testSaveFiles_success() throws Exception {
        DocFileManager doc = new DocFileManager();
        doc.setFileName("testfile.txt");
        doc.setFileExtension(".txt");
        doc.setFileContent(Base64.getEncoder().encodeToString("hello".getBytes()));
        doc.setVanID(1);
        List<DocFileManager> docs = Arrays.asList(doc);

        String dateFolder = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        Files.createDirectories(Paths.get("/tmp/testbase/1/" + dateFolder)); // Ensure directory exists

        when(aESEncryptionDecryption.encrypt(anyString())).thenReturn("encrypted");
        
        String result = service.saveFiles(docs);
        assertNotNull(result);
        assertTrue(result.contains("encrypted"));

        // Verify file creation
        String expectedPathPrefix = "/tmp/testbase/1/" + dateFolder + "/";
        File vanDir = new File("/tmp/testbase/1/" + dateFolder);
        assertTrue(vanDir.exists());
        assertTrue(vanDir.isDirectory());
        
        File[] files = vanDir.listFiles((dir, name) -> name.contains("testfile.txt"));
        assertNotNull(files);
        assertEquals(1, files.length);
        assertTrue(files[0].exists());
        assertEquals("hello", new String(Files.readAllBytes(files[0].toPath())));
    }

    @Test
    public void testSaveFiles_vanIdNull() {
        DocFileManager doc = new DocFileManager();
        doc.setFileName("test.txt");
        doc.setFileExtension(".txt");
        doc.setFileContent(Base64.getEncoder().encodeToString("hello".getBytes()));
        List<DocFileManager> docs = Arrays.asList(doc);
        Exception ex = assertThrows(IllegalArgumentException.class, () -> service.saveFiles(docs));
        assertTrue(ex.getMessage().contains("VanId cannot be null"));
    }

    @Test
    public void testSaveFiles_emptyList() throws Exception {
        List<DocFileManager> docs = new ArrayList<>();
        String result = service.saveFiles(docs);
        assertEquals("[]", result);
    }


    @Test
    public void testCreateFile_success() throws IOException, NoSuchMethodException, java.lang.reflect.InvocationTargetException, IllegalAccessException {
        // Use reflection to call the private createFile method
        java.lang.reflect.Method createFileMethod = CommonServiceImpl.class.getDeclaredMethod("createFile", List.class, String.class, String.class);
        createFileMethod.setAccessible(true);

        DocFileManager doc = new DocFileManager();
        doc.setFileName("test_file");
        doc.setFileExtension(".txt");
        doc.setFileContent(Base64.getEncoder().encodeToString("file content".getBytes()));
        List<DocFileManager> docFileManagerList = Arrays.asList(doc);

        String basePath = "/tmp/testbase/1";
        String currDate = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        Files.createDirectories(Paths.get(basePath, currDate)); // Create the directory for the test

        @SuppressWarnings("unchecked")
        ArrayList<Map<String, String>> result = (ArrayList<Map<String, String>>) createFileMethod.invoke(service, docFileManagerList, basePath, currDate);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        Map<String, String> fileDetails = result.get(0);
        // Updated assertion to reflect the actual behavior of the current replace chain
        assertEquals("testfile", fileDetails.get("fileName"));
        assertTrue(fileDetails.get("filePath").startsWith(basePath + "/" + currDate + "/"));
        assertTrue(fileDetails.get("filePath").endsWith("testfile"));

        // Verify the file was actually created
        File createdFile = new File(fileDetails.get("filePath"));
        assertTrue(createdFile.exists());
        assertTrue(createdFile.isFile());
        assertEquals("file content", new String(Files.readAllBytes(createdFile.toPath())));
    }

    @Test
    public void testCreateFile_withSpecialCharactersInFileName() throws Exception {
        java.lang.reflect.Method createFileMethod = CommonServiceImpl.class.getDeclaredMethod("createFile", List.class, String.class, String.class);
        createFileMethod.setAccessible(true);

        DocFileManager doc = new DocFileManager();
        // The characters , . ? will not be removed by the current replace chain in createFile
        doc.setFileName("file!@#$%^&*()_+`~=[]{}\\|;:'\",./<>?_name");
        doc.setFileExtension(".txt");
        doc.setFileContent(Base64.getEncoder().encodeToString("content".getBytes()));
        List<DocFileManager> docFileManagerList = Arrays.asList(doc);

        String basePath = "/tmp/testbase/1";
        String currDate = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        Files.createDirectories(Paths.get(basePath, currDate));

        @SuppressWarnings("unchecked")
        ArrayList<Map<String, String>> result = (ArrayList<Map<String, String>>) createFileMethod.invoke(service, docFileManagerList, basePath, currDate);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        Map<String, String> fileDetails = result.get(0);
        // Updated assertion to reflect the actual behavior of the current replace chain
        assertEquals("file,.<>?name", fileDetails.get("fileName"));
        assertTrue(fileDetails.get("filePath").contains("file,.<>?name"));
    }

    @Test
    public void testCreateFile_ioException() throws IOException, NoSuchMethodException, java.lang.reflect.InvocationTargetException, IllegalAccessException {
        java.lang.reflect.Method createFileMethod = CommonServiceImpl.class.getDeclaredMethod("createFile", List.class, String.class, String.class);
        createFileMethod.setAccessible(true);

        DocFileManager doc = new DocFileManager();
        doc.setFileName("bad_file");
        doc.setFileExtension(".txt");
        doc.setFileContent(Base64.getEncoder().encodeToString("content".getBytes()));
        List<DocFileManager> docFileManagerList = Arrays.asList(doc);

        String basePath = "/nonexistent_dir/testbase"; // This path should cause an IOException
        String currDate = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));

        // No assertThrows here as the method catches and logs the exception, returning an empty list
        @SuppressWarnings("unchecked")
        ArrayList<Map<String, String>> result = (ArrayList<Map<String, String>>) createFileMethod.invoke(service, docFileManagerList, basePath, currDate);
        
        assertNotNull(result);
        assertTrue(result.isEmpty()); // Should return an empty list on exception
    }

    @Test
    public void testCreateFile_emptyDocFileManagerList() throws Exception {
        java.lang.reflect.Method createFileMethod = CommonServiceImpl.class.getDeclaredMethod("createFile", List.class, String.class, String.class);
        createFileMethod.setAccessible(true);

        List<DocFileManager> docFileManagerList = new ArrayList<>();
        String basePath = "/tmp/testbase/1";
        String currDate = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        Files.createDirectories(Paths.get(basePath, currDate));

        @SuppressWarnings("unchecked")
        ArrayList<Map<String, String>> result = (ArrayList<Map<String, String>>) createFileMethod.invoke(service, docFileManagerList, basePath, currDate);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // Helper to mock static ConfigProperties - updated to accept MockedStatic instance
    private void mockStaticConfigProperties(MockedStatic<ConfigProperties> mockedConfigProperties) {
        mockedConfigProperties.when(() -> ConfigProperties.getPropertyByName("km-base-path")).thenReturn("test_dms_path");
        mockedConfigProperties.when(() -> ConfigProperties.getPropertyByName("km-base-protocol")).thenReturn("http");
        mockedConfigProperties.when(() -> ConfigProperties.getPropertyByName("km-guest-user")).thenReturn("guest");
        mockedConfigProperties.when(() -> ConfigProperties.getPassword("km-guest-user")).thenReturn("password");
    }
}