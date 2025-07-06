package com.iemr.common.controller.kmfilemanager;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.iemr.common.data.kmfilemanager.KMFileManager;
import com.iemr.common.service.kmfilemanager.KMFileManagerService;
import com.iemr.common.service.scheme.SchemeServiceImpl;
import com.iemr.common.service.services.CommonServiceImpl;
import com.iemr.common.utils.response.OutputResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class KMFileManagerControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CommonServiceImpl commonServiceImpl;
    @Mock
    private KMFileManagerService kmFileManagerService;
    @Mock
    private SchemeServiceImpl schemeServiceImpl;
    @Mock
    private ObjectMapper objectMapper;

    private KMFileManagerController kmFileManagerController;

    // Test constants
    private static final String SAVE_FILES_URL = "/kmfilemanager/saveFiles";
    private static final String ADD_FILE_URL = "/kmfilemanager/addFile";
    private static final String GET_DOWNLOAD_URL = "/kmfilemanager/getKMFileDownloadURL";
    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_TOKEN = "Bearer dummy";
    private static final String CONTENT_TYPE = "application/json";

    @BeforeEach
    void setUp() {
        // Create controller instance manually to ensure proper dependency injection
        kmFileManagerController = new KMFileManagerController(commonServiceImpl);
        kmFileManagerController.setKmFileManagerService(kmFileManagerService);
        // Set @Autowired fields using reflection
        ReflectionTestUtils.setField(kmFileManagerController, "objectMapper", objectMapper);
        ReflectionTestUtils.setField(kmFileManagerController, "schemeServiceImpl", schemeServiceImpl);
        
        mockMvc = MockMvcBuilders.standaloneSetup(kmFileManagerController).build();
    }

    // Helper method to create expected success output
    private String createSuccessOutput(String data) {
        OutputResponse response = new OutputResponse();
        response.setResponse(data);
        return response.toString();
    }

    @Test
    void saveFiles_success() throws Exception {
        String requestJson = "[{\"fileName\":\"doc1\"}]";
        String expectedServiceResponse = "Files saved successfully";

        when(commonServiceImpl.saveFiles(anyList())).thenReturn(expectedServiceResponse);

        mockMvc.perform(post(SAVE_FILES_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(CONTENT_TYPE))
                .andExpect(content().string(createSuccessOutput(expectedServiceResponse)));
    }

    @Test
    void saveFiles_serviceThrowsException() throws Exception {
        String requestJson = "[{\"fileName\":\"doc1\"}]";
        when(commonServiceImpl.saveFiles(anyList())).thenThrow(new RuntimeException("fail"));

        mockMvc.perform(post(SAVE_FILES_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(CONTENT_TYPE))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("fail")));
    }

    @Test
    void addFile_success() throws Exception {
        String requestJson = "{\"fileName\":\"test.txt\"}";
        String expectedServiceResponse = "ok";

        when(kmFileManagerService.addKMFile(Mockito.eq(requestJson))).thenReturn(expectedServiceResponse);

        mockMvc.perform(post(ADD_FILE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTH_HEADER, BEARER_TOKEN)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(CONTENT_TYPE))
                .andExpect(content().string(createSuccessOutput(expectedServiceResponse)));
    }

    @Test
    void addFile_serviceThrowsException() throws Exception {
        String requestJson = "{\"fileName\":\"test.txt\"}";
        when(kmFileManagerService.addKMFile(Mockito.eq(requestJson))).thenThrow(new RuntimeException("fail"));

        mockMvc.perform(post(ADD_FILE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTH_HEADER, BEARER_TOKEN)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(CONTENT_TYPE))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("fail")));
    }

    @Test
    void getKMFileDownloadURL_success() throws Exception {
        String requestJson = "{\"fileId\":1}";
        KMFileManager km = new KMFileManager();
        String expectedUrl = "http://file.url";

        when(objectMapper.readValue(requestJson, KMFileManager.class)).thenReturn(km);
        when(schemeServiceImpl.getFilePath(km)).thenReturn(expectedUrl);

        mockMvc.perform(post(GET_DOWNLOAD_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTH_HEADER, BEARER_TOKEN)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(CONTENT_TYPE))
                .andExpect(content().string(createSuccessOutput(expectedUrl)));
    }

    @Test
    void getKMFileDownloadURL_objectMapperThrowsException() throws Exception {
        String requestJson = "bad json";
        when(objectMapper.readValue(requestJson, KMFileManager.class)).thenThrow(new RuntimeException("parse error"));

        mockMvc.perform(post(GET_DOWNLOAD_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTH_HEADER, BEARER_TOKEN)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(CONTENT_TYPE))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("parse error")));
    }

    @Test
    void getKMFileDownloadURL_serviceThrowsException() throws Exception {
        String requestJson = "{\"fileId\":1}";
        KMFileManager km = new KMFileManager();
        when(objectMapper.readValue(requestJson, KMFileManager.class)).thenReturn(km);
        when(schemeServiceImpl.getFilePath(km)).thenThrow(new RuntimeException("fail"));

        mockMvc.perform(post(GET_DOWNLOAD_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTH_HEADER, BEARER_TOKEN)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(CONTENT_TYPE))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("fail")));
    }
}
