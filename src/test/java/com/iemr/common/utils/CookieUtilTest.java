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
package com.iemr.common.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CookieUtilTest {

    @InjectMocks
    private CookieUtil cookieUtil;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetCookieValue_CookiePresent() {
        Cookie[] cookies = {
                new Cookie("cookie1", "value1"),
                new Cookie("testCookie", "testValue"),
                new Cookie("cookie3", "value3")
        };
        when(request.getCookies()).thenReturn(cookies);

        Optional<String> result = cookieUtil.getCookieValue(request, "testCookie");

        assertTrue(result.isPresent());
        assertEquals("testValue", result.get());
    }

    @Test
    void testGetCookieValue_CookieNotPresent() {
        Cookie[] cookies = {
                new Cookie("cookie1", "value1"),
                new Cookie("cookie3", "value3")
        };
        when(request.getCookies()).thenReturn(cookies);

        Optional<String> result = cookieUtil.getCookieValue(request, "nonExistentCookie");

        assertFalse(result.isPresent());
    }

    @Test
    void testGetCookieValue_NoCookies() {
        when(request.getCookies()).thenReturn(null);

        Optional<String> result = cookieUtil.getCookieValue(request, "anyCookie");

        assertFalse(result.isPresent());
    }

    @Test
    void testGetCookieValue_EmptyCookiesArray() {
        when(request.getCookies()).thenReturn(new Cookie[]{});

        Optional<String> result = cookieUtil.getCookieValue(request, "anyCookie");

        assertFalse(result.isPresent());
    }

    @Test
    void testAddJwtTokenToCookie_ProductionTrue() {
        ReflectionTestUtils.setField(cookieUtil, "isProduction", true);
        String jwtToken = "mockJwtToken";

        cookieUtil.addJwtTokenToCookie(jwtToken, response, request);

        ArgumentCaptor<String> headerNameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> headerValueCaptor = ArgumentCaptor.forClass(String.class);

        verify(response).addHeader(headerNameCaptor.capture(), headerValueCaptor.capture());

        assertEquals("Set-Cookie", headerNameCaptor.getValue());
        String expectedCookieHeader = "Jwttoken=mockJwtToken; Path=/; Max-Age=86400; HttpOnly; SameSite=Strict; Secure";
        assertEquals(expectedCookieHeader, headerValueCaptor.getValue());
    }

    @Test
    void testAddJwtTokenToCookie_ProductionFalse() {
        ReflectionTestUtils.setField(cookieUtil, "isProduction", false);
        String jwtToken = "anotherJwtToken";

        cookieUtil.addJwtTokenToCookie(jwtToken, response, request);

        ArgumentCaptor<String> headerNameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> headerValueCaptor = ArgumentCaptor.forClass(String.class);

        verify(response).addHeader(headerNameCaptor.capture(), headerValueCaptor.capture());

        assertEquals("Set-Cookie", headerNameCaptor.getValue());
        String expectedCookieHeader = "Jwttoken=anotherJwtToken; Path=/; Max-Age=86400; HttpOnly; SameSite=None; Secure";
        assertEquals(expectedCookieHeader, headerValueCaptor.getValue());
    }

    @Test
    void testGetJwtTokenFromCookie_TokenPresent() {
        Cookie[] cookies = {
                new Cookie("someOtherCookie", "value"),
                new Cookie("Jwttoken", "actualJwtValue"),
                new Cookie("anotherCookie", "anotherValue")
        };
        when(request.getCookies()).thenReturn(cookies);

        String result = CookieUtil.getJwtTokenFromCookie(request);

        assertNotNull(result);
        assertEquals("actualJwtValue", result);
    }

    @Test
    void testGetJwtTokenFromCookie_TokenNotPresent() {
        Cookie[] cookies = {
                new Cookie("someOtherCookie", "value"),
                new Cookie("anotherCookie", "anotherValue")
        };
        when(request.getCookies()).thenReturn(cookies);

        String result = CookieUtil.getJwtTokenFromCookie(request);

        assertNull(result);
    }

    @Test
    void testGetJwtTokenFromCookie_NoCookies() {
        when(request.getCookies()).thenReturn(null);

        String result = CookieUtil.getJwtTokenFromCookie(request);

        assertNull(result);
    }

    @Test
    void testGetJwtTokenFromCookie_EmptyCookiesArray() {
        when(request.getCookies()).thenReturn(new Cookie[]{});

        String result = CookieUtil.getJwtTokenFromCookie(request);

        assertNull(result);
    }

    @Test
    void testGetJwtTokenFromCookie_CaseInsensitive() {
        Cookie[] cookies = {
                new Cookie("jwtTOKEN", "caseInsensitiveValue")
        };
        when(request.getCookies()).thenReturn(cookies);

        String result = CookieUtil.getJwtTokenFromCookie(request);

        assertNotNull(result);
        assertEquals("caseInsensitiveValue", result);
    }
}