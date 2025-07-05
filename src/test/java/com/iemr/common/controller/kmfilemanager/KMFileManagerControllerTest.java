package com.iemr.common.controller.kmfilemanager;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.iemr.common.data.kmfilemanager.KMFileManager;
import com.iemr.common.service.kmfilemanager.KMFileManagerService;
import com.iemr.common.service.scheme.SchemeServiceImpl;
import com.iemr.common.service.services.CommonServiceImpl;
import com.iemr.common.utils.response.OutputResponse;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;


import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest(classes = {KMFileManagerController.class}, 
    properties = {"spring.main.allow-bean-definition-overriding=true"})
@AutoConfigureMockMvc
class KMFileManagerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CommonServiceImpl commonServiceImpl;
    @MockBean
    private KMFileManagerService kmFileManagerService;
    @MockBean
    private SchemeServiceImpl schemeServiceImpl;
    @MockBean
    private ObjectMapper objectMapper;

    @Test
    void saveFiles_success() throws Exception {
        String requestJson = "[{\"fileName\":\"doc1\"}]";
        String expectedServiceResponse = "Files saved successfully";
        OutputResponse expected = new OutputResponse();
        expected.setResponse(expectedServiceResponse);

        when(commonServiceImpl.saveFiles(anyList())).thenReturn(expectedServiceResponse);

        mockMvc.perform(post("/kmfilemanager/saveFiles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().json(expected.toString()));
    }

    @Test
    void saveFiles_serviceThrowsException() throws Exception {
        String requestJson = "[{\"fileName\":\"doc1\"}]";
        when(commonServiceImpl.saveFiles(anyList())).thenThrow(new RuntimeException("fail"));

        mockMvc.perform(post("/kmfilemanager/saveFiles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(5000))
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void addFile_success() throws Exception {
        String requestJson = "{\"fileName\":\"test.txt\"}";
        String expectedServiceResponse = "ok";
        OutputResponse expected = new OutputResponse();
        expected.setResponse(expectedServiceResponse);

        when(kmFileManagerService.addKMFile(Mockito.eq(requestJson))).thenReturn(expectedServiceResponse);

        mockMvc.perform(post("/kmfilemanager/addFile")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer dummy")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().json(expected.toString()));
    }

    @Test
    void addFile_serviceThrowsException() throws Exception {
        String requestJson = "{\"fileName\":\"test.txt\"}";
        when(kmFileManagerService.addKMFile(Mockito.eq(requestJson))).thenThrow(new RuntimeException("fail"));

        mockMvc.perform(post("/kmfilemanager/addFile")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer dummy")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(5000))
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void getKMFileDownloadURL_success() throws Exception {
        String requestJson = "{\"fileId\":1}";
        KMFileManager km = new KMFileManager();
        String expectedUrl = "http://file.url";
        OutputResponse expected = new OutputResponse();
        expected.setResponse(expectedUrl);

        when(objectMapper.readValue(requestJson, KMFileManager.class)).thenReturn(km);
        when(schemeServiceImpl.getFilePath(km)).thenReturn(expectedUrl);

        mockMvc.perform(post("/kmfilemanager/getKMFileDownloadURL")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer dummy")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().json(expected.toString()));
    }

    @Test
    void getKMFileDownloadURL_objectMapperThrowsException() throws Exception {
        String requestJson = "bad json";
        when(objectMapper.readValue(requestJson, KMFileManager.class)).thenThrow(new RuntimeException("parse error"));

        mockMvc.perform(post("/kmfilemanager/getKMFileDownloadURL")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer dummy")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(5000))
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void getKMFileDownloadURL_serviceThrowsException() throws Exception {
        String requestJson = "{\"fileId\":1}";
        KMFileManager km = new KMFileManager();
        when(objectMapper.readValue(requestJson, KMFileManager.class)).thenReturn(km);
        when(schemeServiceImpl.getFilePath(km)).thenThrow(new RuntimeException("fail"));

        mockMvc.perform(post("/kmfilemanager/getKMFileDownloadURL")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer dummy")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(5000))
                .andExpect(jsonPath("$.error").exists());
    }
}
