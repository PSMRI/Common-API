package com.iemr.common.service.lungassessment;

import com.iemr.common.data.lungassessment.*;

import org.springframework.util.MultiValueMap;
import org.springframework.util.LinkedMultiValueMap;
import java.math.BigInteger;
import com.iemr.common.repo.lungassessment.LungAssessmentRepository;
import com.iemr.common.utils.mapper.InputMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class LungAssessmentServiceImplTest {

    @InjectMocks
    LungAssessmentServiceImpl service;

    @Mock
    LungAssessmentRepository lungAssessmentRepository;

    @Mock
    MultipartFile multipartFile;

    @BeforeEach
    public void setup() throws Exception {
        MockitoAnnotations.openMocks(this);
        setField("lungAssessmentPath", "/tmp/");
        setField("lungAssessmentAdminLoginUrl", "http://localhost/auth");
        setField("lungAssessmentEmail", "test@example.com");
        setField("lungAssessmentPassword", "password");
        setField("lungAssessmentValidateCoughUrl", "http://localhost/validateCough");
        setField("lungAssessmentAssesmentUrl", "http://localhost/startAssessment");
        setField("lungAssessmentGetAssesmentUrl", "http://localhost/getAssessment");
    }

    private void setField(String name, Object value) throws Exception {
        if (value == null) {
            throw new IllegalArgumentException("Value for field '" + name + "' is null");
        }
        java.lang.reflect.Field f = LungAssessmentServiceImpl.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(service, value);
    }

    @Test
    public void testGetLungAssessmentAdminLogin_success() throws Exception {
        // ...existing code...
        LungAssessmentAuthenticateResponse resp = new LungAssessmentAuthenticateResponse();
        resp.setAccessToken("token123");
        String respJson = new com.google.gson.Gson().toJson(resp);
        ResponseEntity<String> responseEntity = new ResponseEntity<>(respJson, HttpStatus.OK);
        try (MockedConstruction<RestTemplate> mocked = mockConstruction(RestTemplate.class,
                (mock, context) -> when(mock.exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class))).thenReturn(responseEntity))) {
            String token = service.getLungAssessmentAdminLogin("test@example.com", "password");
            assertEquals("token123", token);
        }
    }

    @Test
    public void testGetLungAssessmentAdminLogin_httpError() {
        try (MockedConstruction<RestTemplate> mocked = mockConstruction(RestTemplate.class,
                (mock, context) -> when(mock.exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class)))
                        .thenThrow(new org.springframework.web.client.HttpClientErrorException(HttpStatus.BAD_REQUEST)))) {
            RuntimeException ex = assertThrows(RuntimeException.class, () ->
                    service.getLungAssessmentAdminLogin("test@example.com", "password"));
            assertTrue(ex.getMessage().contains("get authentication failed with error"));
        }
    }

    @Test
    public void testGetLungAssessmentAdminLogin_otherError() {
        try (MockedConstruction<RestTemplate> mocked = mockConstruction(RestTemplate.class,
                (mock, context) -> when(mock.exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class)))
                        .thenThrow(new RuntimeException("other error")))) {
            RuntimeException ex = assertThrows(RuntimeException.class, () ->
                    service.getLungAssessmentAdminLogin("test@example.com", "password"));
            assertTrue(ex.getMessage().contains("get authentication failed with error"));
        }
    }

    @Test
    public void testVerifyCough_success() throws Exception {
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getBytes()).thenReturn("bytes".getBytes());
        LungAssessmentValidateCoughReponseDTO dto = new LungAssessmentValidateCoughReponseDTO();
        Map<String, Object> data = new HashMap<>();
        data.put("isValidCough", true);
        dto.setData(data);
        dto.setStatus("SUCCESS");
        String respJson = new com.google.gson.Gson().toJson(dto);
        ResponseEntity<String> responseEntity = new ResponseEntity<>(respJson, HttpStatus.OK);
        try (MockedConstruction<RestTemplate> mocked = mockConstruction(RestTemplate.class,
                (mock, context) -> when(mock.exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class))).thenReturn(responseEntity))) {
            Boolean result = service.verifyCough(multipartFile, "token", 1L, "temp.wav");
            assertTrue(result);
        }
    }

    @Test
    public void testVerifyCough_fileMissing() {
        when(multipartFile.isEmpty()).thenReturn(true);
        Exception ex = assertThrows(Exception.class, () ->
                service.verifyCough(multipartFile, "token", 1L, "temp.wav"));
        assertTrue(ex.getMessage().contains("file is missing"));
    }

    @Test
    public void testVerifyCough_apiError() throws Exception {
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getBytes()).thenReturn("bytes".getBytes());
        ResponseEntity<String> responseEntity = new ResponseEntity<>("{}", HttpStatus.BAD_REQUEST);
        try (MockedConstruction<RestTemplate> mocked = mockConstruction(RestTemplate.class,
                (mock, context) -> when(mock.exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class))).thenReturn(responseEntity))) {
            Exception ex = assertThrows(RuntimeException.class, () ->
                    service.verifyCough(multipartFile, "token", 1L, "temp.wav"));
            assertTrue(ex.getMessage().contains("Lung assessment validate cough quality exception"));
        }
    }

    @Test
    public void testVerifyCough_invalidResponse() throws Exception {
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getBytes()).thenReturn("bytes".getBytes());
        LungAssessmentValidateCoughReponseDTO dto = new LungAssessmentValidateCoughReponseDTO();
        dto.setData(new HashMap<>());
        dto.setStatus("FAIL");
        String respJson = new com.google.gson.Gson().toJson(dto);
        ResponseEntity<String> responseEntity = new ResponseEntity<>(respJson, HttpStatus.OK);
        try (MockedConstruction<RestTemplate> mocked = mockConstruction(RestTemplate.class,
                (mock, context) -> when(mock.exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class))).thenReturn(responseEntity))) {
            RuntimeException ex = assertThrows(RuntimeException.class, () ->
                    service.verifyCough(multipartFile, "token", 1L, "temp.wav"));
            assertTrue(ex.getMessage().contains("cough file validation is failed"));
        }
    }

    @Test
    public void testInitiateAssesment_success() throws Exception {
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getBytes()).thenReturn("bytes".getBytes());
        LungAssessment reqObj = new LungAssessment();
        reqObj.setPatientId(1L);
        // Use a mock or dummy Symptoms object if LungAssessment.Symptoms does not exist
        reqObj.setSymptoms(mock(SymptomsDTO.class));
        reqObj.setId(java.math.BigInteger.valueOf(1L));
        String reqJson = new com.google.gson.Gson().toJson(reqObj);
        when(lungAssessmentRepository.save(any())).thenReturn(reqObj);
        LungAssessmentResponseDTO respDTO = new LungAssessmentResponseDTO();
        respDTO.setStatus("SUCCESS");
        // Set all required fields in LungAssessment for response data
        LungAssessment dataObj = new LungAssessment();
        dataObj.setRecord_duration(10.0);
        dataObj.setStatus("SUCCESS");
        dataObj.setRisk("LOW");
        dataObj.setCough_severity_score(1);
        dataObj.setCough_pattern("pattern");
        dataObj.setDry_cough_count(2);
        dataObj.setWet_cough_count(3);
        dataObj.setSeverity("MILD");
        dataObj.setMessage("OK");
        dataObj.setPatientId(1L);
        dataObj.setSymptoms(mock(SymptomsDTO.class));
        dataObj.setId(java.math.BigInteger.valueOf(1L));
        respDTO.setData(dataObj);
        String respJson = new com.google.gson.Gson().toJson(respDTO);
        ResponseEntity<String> responseEntity = new ResponseEntity<>(respJson, HttpStatus.OK);
        try (MockedConstruction<RestTemplate> mocked = mockConstruction(RestTemplate.class,
                (mock, context) -> when(mock.exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class))).thenReturn(responseEntity))) {
            try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class)) {
                InputMapper mockMapper = mock(InputMapper.class);
                when(mockMapper.fromJson(anyString(), eq(LungAssessment.class))).thenReturn(reqObj);
                when(mockMapper.fromJson(anyString(), eq(LungAssessmentResponseDTO.class))).thenReturn(respDTO);
                // Properly mock cough validation response
                LungAssessmentValidateCoughReponseDTO coughResp = new LungAssessmentValidateCoughReponseDTO();
                Map<String, Object> coughData = new HashMap<>();
                coughData.put("isValidCough", true);
                coughResp.setData(coughData);
                coughResp.setStatus("SUCCESS");
                when(mockMapper.fromJson(anyString(), eq(LungAssessmentValidateCoughReponseDTO.class))).thenReturn(coughResp);
                inputMapperMock.when(InputMapper::gson).thenReturn(mockMapper);
                String result = service.initiateAssesment(reqJson, multipartFile);
                assertNotNull(result);
            }
        }
    }

    @Test
    public void testInitiateAssesment_fileMissing() {
        when(multipartFile.isEmpty()).thenReturn(true);
        Exception ex = assertThrows(Exception.class, () ->
                service.initiateAssesment("{}", multipartFile));
        assertTrue(ex.getMessage().contains("file is missing"));
    }

    @Test
    public void testInitiateAssesment_missingPatientId() throws Exception {
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getBytes()).thenReturn("bytes".getBytes());
        LungAssessment reqObj = new LungAssessment();
        reqObj.setSymptoms(mock(SymptomsDTO.class));
        String reqJson = new com.google.gson.Gson().toJson(reqObj);
        Exception ex = assertThrows(RuntimeException.class, () ->
                service.initiateAssesment(reqJson, multipartFile));
        assertTrue(ex.getMessage().contains("Missing patient Id"));
    }

    @Test
    public void testInitiateAssesment_coughValidationFail() throws Exception {
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getBytes()).thenReturn("bytes".getBytes());
        LungAssessment reqObj = new LungAssessment();
        reqObj.setPatientId(1L);
        reqObj.setSymptoms(mock(SymptomsDTO.class));
        String reqJson = new com.google.gson.Gson().toJson(reqObj);
        when(lungAssessmentRepository.save(any())).thenReturn(reqObj);
        LungAssessmentServiceImpl spyService = Mockito.spy(service);
        doReturn(false).when(spyService).verifyCough(any(), anyString(), anyLong(), anyString());
        Exception ex = assertThrows(RuntimeException.class, () ->
                spyService.initiateAssesment(reqJson, multipartFile));
        assertTrue(ex.getMessage().contains("Lung assessment API exception"));
    }

    @Test
    public void testInitiateAssesment_apiError() throws Exception {
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getBytes()).thenReturn("bytes".getBytes());
        LungAssessment reqObj = new LungAssessment();
        reqObj.setPatientId(1L);
        reqObj.setSymptoms(mock(SymptomsDTO.class));
        String reqJson = new com.google.gson.Gson().toJson(reqObj);
        when(lungAssessmentRepository.save(any())).thenReturn(reqObj);
        LungAssessmentServiceImpl spyService = Mockito.spy(service);
        doReturn(true).when(spyService).verifyCough(any(), anyString(), anyLong(), anyString());
        ResponseEntity<String> responseEntity = new ResponseEntity<>("{}", HttpStatus.BAD_REQUEST);
        try (MockedConstruction<RestTemplate> mocked = mockConstruction(RestTemplate.class,
                (mock, context) -> when(mock.exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class))).thenReturn(responseEntity))) {
            Exception ex = assertThrows(RuntimeException.class, () ->
                    spyService.initiateAssesment(reqJson, multipartFile));
            assertTrue(ex.getMessage().contains("Lung assessment API exception"));
        }
    }

    @Test
    public void testGetAssesment_success() throws Exception {
        LungAssessment entity = new LungAssessment();
        entity.setId(BigInteger.valueOf(1L));
        entity.setStatus("SUCCESS");
        List<LungAssessment> list = Arrays.asList(entity);
        when(lungAssessmentRepository.findByAssessmentId(anyString())).thenReturn(list);
        String result = service.getAssesment("assessId");
        assertTrue(result.contains("\"id\":1"));
    }

    @Test
    public void testGetAssesment_entityUpdateFromApi() throws Exception {
        // Entity with non-success status, triggers API call branch
        LungAssessment entity = new LungAssessment();
        entity.setId(BigInteger.valueOf(1L));
        entity.setStatus("PENDING");
        // Set all fields that will be updated by the service
        entity.setRecord_duration(0.0);
        entity.setRisk("");
        entity.setCough_severity_score(0);
        entity.setCough_pattern("");
        entity.setDry_cough_count(0);
        entity.setWet_cough_count(0);
        entity.setSeverity("");
        List<LungAssessment> list = Arrays.asList(entity);
        when(lungAssessmentRepository.findByAssessmentId(anyString())).thenReturn(list);

        // Prepare API response DTO with all required fields
        LungAssessmentResponseDTO respDTO = new LungAssessmentResponseDTO();
        LungAssessment dataObj = new LungAssessment();
        dataObj.setRecord_duration(10.0);
        dataObj.setStatus("SUCCESS");
        dataObj.setRisk("LOW");
        dataObj.setCough_severity_score(1);
        dataObj.setCough_pattern("pattern");
        dataObj.setDry_cough_count(2);
        dataObj.setWet_cough_count(3);
        dataObj.setSeverity("MILD");
        respDTO.setData(dataObj);
        respDTO.setStatus("SUCCESS");
        String respJson = new com.google.gson.Gson().toJson(respDTO);
        ResponseEntity<String> responseEntity = new ResponseEntity<>(respJson, HttpStatus.OK);

        // ArgumentCaptor to capture the entity passed to save()
        ArgumentCaptor<LungAssessment> captor = ArgumentCaptor.forClass(LungAssessment.class);
        when(lungAssessmentRepository.save(any())).thenAnswer(invocation -> {
            LungAssessment updated = invocation.getArgument(0);
            return updated;
        });

        // Spy the service to stub getLungAssessmentAdminLogin
        LungAssessmentServiceImpl spyService = Mockito.spy(service);
        doReturn("dummyToken").when(spyService).getLungAssessmentAdminLogin(anyString(), anyString());

        try (MockedConstruction<RestTemplate> mocked = mockConstruction(RestTemplate.class,
                (mock, context) -> when(mock.exchange(anyString(), eq(HttpMethod.GET), any(), eq(String.class))).thenReturn(responseEntity))) {
            try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class)) {
                InputMapper mockMapper = mock(InputMapper.class);
                when(mockMapper.fromJson(anyString(), eq(LungAssessmentResponseDTO.class))).thenReturn(respDTO);
                inputMapperMock.when(InputMapper::gson).thenReturn(mockMapper);
                String result = spyService.getAssesment("assessId");
                // ...existing assertions...
                boolean idPresent = result.contains("\"id\":1") || result.contains("\"id\":\"1\"") || result.contains("\"id\":1.0");
                assertTrue(idPresent, "id field missing or incorrect. Actual result: " + result);
                assertTrue(result.contains("\"status\":\"SUCCESS\""), "status field missing or incorrect");
                assertTrue(result.contains("\"risk\":\"LOW\""), "risk field missing or incorrect");
                assertTrue(result.contains("\"cough_severity_score\":1"), "cough_severity_score field missing or incorrect");
                assertTrue(result.contains("\"cough_pattern\":\"pattern\""), "cough_pattern field missing or incorrect");
                assertTrue(result.contains("\"dry_cough_count\":2"), "dry_cough_count field missing or incorrect");
                assertTrue(result.contains("\"wet_cough_count\":3"), "wet_cough_count field missing or incorrect");
                assertTrue(result.contains("\"severity\":\"MILD\""), "severity field missing or incorrect");
                // Explicitly verify setters and save for coverage
                verify(lungAssessmentRepository, atLeastOnce()).save(captor.capture());
                LungAssessment updatedEntity = captor.getValue();
                assertEquals(10.0, updatedEntity.getRecord_duration(), "record_duration not set correctly");
                assertEquals("SUCCESS", updatedEntity.getStatus(), "status not set correctly");
                assertEquals("LOW", updatedEntity.getRisk(), "risk not set correctly");
                assertEquals(1, updatedEntity.getCough_severity_score(), "cough_severity_score not set correctly");
                assertEquals("pattern", updatedEntity.getCough_pattern(), "cough_pattern not set correctly");
                assertEquals(2, updatedEntity.getDry_cough_count(), "dry_cough_count not set correctly");
                assertEquals(3, updatedEntity.getWet_cough_count(), "wet_cough_count not set correctly");
                assertEquals("MILD", updatedEntity.getSeverity(), "severity not set correctly");
            }
        }
    }

    @Test
    public void testGetAssesment_statusNotSuccess() throws Exception {
        LungAssessment entity = new LungAssessment();
        entity.setId(BigInteger.valueOf(1L));
        entity.setStatus("FAIL");
        List<LungAssessment> list = Arrays.asList(entity);
        when(lungAssessmentRepository.findByAssessmentId(anyString())).thenReturn(list);
        ResponseEntity<String> responseEntity = new ResponseEntity<>("{}", HttpStatus.BAD_REQUEST);
        try (MockedConstruction<RestTemplate> mocked = mockConstruction(RestTemplate.class,
                (mock, context) -> when(mock.exchange(anyString(), eq(HttpMethod.GET), any(), eq(String.class))).thenReturn(responseEntity))) {
            Exception ex = assertThrows(Exception.class, () ->
                    service.getAssesment("assessId"));
            assertTrue(ex.getMessage().contains("get assesment API failed"));
        }
    }

    @Test
    public void testGetAssesment_invalidId() {
        when(lungAssessmentRepository.findByAssessmentId(anyString())).thenReturn(new ArrayList<>());
        Exception ex = assertThrows(Exception.class, () ->
                service.getAssesment("badId"));
        assertTrue(ex.getMessage().contains("Invalid assessment id"));
    }

    @Test
    public void testGetAssesment_apiException() {
        when(lungAssessmentRepository.findByAssessmentId(anyString())).thenThrow(new RuntimeException("repo error"));
        Exception ex = assertThrows(Exception.class, () ->
                service.getAssesment("badId"));
        assertTrue(ex.getMessage().contains("get assesment API failed"));
    }

    @Test
    public void testGetAssessmentDetails_success() throws Exception {
        LungAssessment entity = new LungAssessment();
        entity.setId(BigInteger.valueOf(1L));
        List<LungAssessment> list = Arrays.asList(entity);
        when(lungAssessmentRepository.findByPatientId(anyLong())).thenReturn(list);
        String result = service.getAssessmentDetails(1L);
        assertTrue(result.contains("\"id\":1"));
    }
}
