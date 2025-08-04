package com.iemr.common.service.recaptcha;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CaptchaValidationServiceTest {
    private CaptchaValidationService service;

    @BeforeEach
    void setUp() throws Exception {
        service = new CaptchaValidationService();
        setField(service, "secretKey", "dummy-secret");
        setField(service, "captchaVerifyUrl", "http://dummy-url");
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    void validateCaptcha_nullToken_returnsTrue() {
        assertTrue(service.validateCaptcha(null));
    }

    @Test
    void validateCaptcha_emptyToken_returnsTrue() {
        assertTrue(service.validateCaptcha("   "));
    }

    @Test
    void validateCaptcha_successTrue_returnsTrue() {
        RestTemplate restTemplateMock = mock(RestTemplate.class);
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("success", true);
        ResponseEntity<Map> responseEntity = ResponseEntity.ok(responseMap);

        try (MockedStatic<RestTemplate> ignored = Mockito.mockStatic(RestTemplate.class, invocation -> restTemplateMock)) {
            // Use spy to intercept new RestTemplate()
            try (var restSpy = Mockito.mockConstruction(RestTemplate.class, (mock, context) -> {
                when(mock.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class))).thenReturn(responseEntity);
            })) {
                assertTrue(service.validateCaptcha("valid-token"));
            }
        }
    }

    @Test
    void validateCaptcha_successFalse_returnsFalse() {
        RestTemplate restTemplateMock = mock(RestTemplate.class);
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("success", false);
        ResponseEntity<Map> responseEntity = ResponseEntity.ok(responseMap);

        try (var restSpy = Mockito.mockConstruction(RestTemplate.class, (mock, context) -> {
            when(mock.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class))).thenReturn(responseEntity);
        })) {
            assertFalse(service.validateCaptcha("invalid-token"));
        }
    }

    @Test
    void validateCaptcha_successNotBoolean_returnsFalse() {
        RestTemplate restTemplateMock = mock(RestTemplate.class);
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("success", "not-a-boolean");
        ResponseEntity<Map> responseEntity = ResponseEntity.ok(responseMap);

        try (var restSpy = Mockito.mockConstruction(RestTemplate.class, (mock, context) -> {
            when(mock.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class))).thenReturn(responseEntity);
        })) {
            assertFalse(service.validateCaptcha("weird-token"));
        }
    }

    @Test
    void validateCaptcha_exception_returnsFalse() {
        try (var restSpy = Mockito.mockConstruction(RestTemplate.class, (mock, context) -> {
            when(mock.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class))).thenThrow(new RuntimeException("fail"));
        })) {
            assertFalse(service.validateCaptcha("any-token"));
        }
    }
}
