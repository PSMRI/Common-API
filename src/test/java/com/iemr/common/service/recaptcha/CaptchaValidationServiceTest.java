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
