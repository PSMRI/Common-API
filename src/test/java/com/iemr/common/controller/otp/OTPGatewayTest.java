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
package com.iemr.common.controller.otp;

import com.iemr.common.data.otp.OTPRequestParsor;
import com.iemr.common.service.otp.OTPHandler;
import com.iemr.common.utils.mapper.InputMapper;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OTPGatewayTest {

    @InjectMocks
    private OTPGateway otpGateway;

    @Mock
    private OTPHandler otpHandler;

    @Mock
    private Logger logger;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(otpGateway, "logger", logger);
        InputMapper.gson();
    }

    // --- sendOTP tests ---

    @Test
    void testSendOTP_Success() throws Exception {
        String requestJson = "{\"mobNo\":\"1234567890\"}";
        when(otpHandler.sendOTP(any(OTPRequestParsor.class))).thenReturn("success");

        String responseString = otpGateway.sendOTP(requestJson);

        verify(otpHandler, times(1)).sendOTP(any(OTPRequestParsor.class));
        assertTrue(responseString.contains("\"statusCode\":200"));
        assertTrue(responseString.contains("\"status\":\"Success\""));
        assertTrue(responseString.contains("\"errorMessage\":\"Success\""));
        assertTrue(responseString.contains("\"data\":{\"response\":\"success\"}"));
    }

    @Test
    void testSendOTP_HandlerReturnsFailureString() throws Exception {
        String requestJson = "{\"mobNo\":\"1234567890\"}";
        when(otpHandler.sendOTP(any(OTPRequestParsor.class))).thenReturn("failure");

        String responseString = otpGateway.sendOTP(requestJson);

        verify(otpHandler, times(1)).sendOTP(any(OTPRequestParsor.class));
        assertTrue(responseString.contains("\"statusCode\":5000"));
        assertTrue(responseString.contains("\"status\":\"failure\""));
        assertTrue(responseString.contains("\"errorMessage\":\"failure\""));
    }

    @Test
    void testSendOTP_InputMapperThrowsException() throws Exception {
        String requestJson = "invalid json";

        String responseString = otpGateway.sendOTP(requestJson);

        verify(otpHandler, times(0)).sendOTP(any(OTPRequestParsor.class));
        verify(logger, times(1)).error(Mockito.startsWith("error in sending OTP : "));
        assertTrue(responseString.contains("\"statusCode\":5000"));
        assertTrue(responseString.contains("\"status\":\"error : com.google.gson.JsonSyntaxException"));
        assertTrue(responseString.contains("\"errorMessage\":\"error : com.google.gson.JsonSyntaxException"));
    }

    @Test
    void testSendOTP_HandlerThrowsException() throws Exception {
        String requestJson = "{\"mobNo\":\"1234567890\"}";
        when(otpHandler.sendOTP(any(OTPRequestParsor.class))).thenThrow(new RuntimeException("OTP service unavailable"));

        String responseString = otpGateway.sendOTP(requestJson);

        verify(otpHandler, times(1)).sendOTP(any(OTPRequestParsor.class));
        verify(logger, times(1)).error(Mockito.startsWith("error in sending OTP : "));
        assertTrue(responseString.contains("\"statusCode\":5000"));
        assertTrue(responseString.contains("\"status\":\"error : java.lang.RuntimeException: OTP service unavailable"));
        assertTrue(responseString.contains("\"errorMessage\":\"error : java.lang.RuntimeException: OTP service unavailable"));
    }

    // --- validateOTP tests ---

@Test
void testValidateOTP_Success() throws Exception {
    String requestJson = "{\"mobNo\":\"1234567890\",\"otp\":1234}";
    JSONObject handlerResponse = new JSONObject();
    handlerResponse.put("status", "validated");
    handlerResponse.put("message", "OTP is valid");

    when(otpHandler.validateOTP(any(OTPRequestParsor.class))).thenReturn(handlerResponse);

    String responseString = otpGateway.validateOTP(requestJson);

    verify(otpHandler, times(1)).validateOTP(any(OTPRequestParsor.class));

    JSONObject respJson = new JSONObject(responseString);

    assertEquals(200, respJson.getInt("statusCode"));
    assertEquals("Success", respJson.getString("status"));
    assertEquals("Success", respJson.getString("errorMessage"));

    JSONObject data = respJson.getJSONObject("data");
    assertEquals("validated", data.getString("status"));
    assertEquals("OTP is valid", data.getString("message"));
}


    @Test
    void testValidateOTP_HandlerReturnsNull() throws Exception {
        String requestJson = "{\"mobNo\":\"1234567890\",\"otp\":1234}";
        when(otpHandler.validateOTP(any(OTPRequestParsor.class))).thenReturn(null);

        String responseString = otpGateway.validateOTP(requestJson);

        verify(otpHandler, times(1)).validateOTP(any(OTPRequestParsor.class));
        assertTrue(responseString.contains("\"statusCode\":5000"));
        assertTrue(responseString.contains("\"status\":\"failure\""));
        assertTrue(responseString.contains("\"errorMessage\":\"failure\""));
    }

    @Test
    void testValidateOTP_InputMapperThrowsException() throws Exception {
        String requestJson = "invalid json for validation";

        String responseString = otpGateway.validateOTP(requestJson);

        verify(otpHandler, times(0)).validateOTP(any(OTPRequestParsor.class));
        verify(logger, times(1)).error(Mockito.startsWith("error in validating OTP : "));
        assertTrue(responseString.contains("\"statusCode\":5000"));
        assertTrue(responseString.contains("\"status\":\"error : com.google.gson.JsonSyntaxException"));
        assertTrue(responseString.contains("\"errorMessage\":\"error : com.google.gson.JsonSyntaxException"));
    }

    @Test
    void testValidateOTP_HandlerThrowsException() throws Exception {
        String requestJson = "{\"mobNo\":\"1234567890\",\"otp\":1234}";
        when(otpHandler.validateOTP(any(OTPRequestParsor.class))).thenThrow(new Exception("Validation service error"));

        String responseString = otpGateway.validateOTP(requestJson);

        verify(otpHandler, times(1)).validateOTP(any(OTPRequestParsor.class));
        verify(logger, times(1)).error(Mockito.startsWith("error in validating OTP : "));
        assertTrue(responseString.contains("\"statusCode\":5000"));
        assertTrue(responseString.contains("\"status\":\"error : java.lang.Exception: Validation service error"));
        assertTrue(responseString.contains("\"errorMessage\":\"error : java.lang.Exception: Validation service error"));
    }

    // --- resendOTP tests ---

    @Test
    void testResendOTP_Success() throws Exception {
        String requestJson = "{\"mobNo\":\"0987654321\"}";
        when(otpHandler.resendOTP(any(OTPRequestParsor.class))).thenReturn("success");

        String responseString = otpGateway.resendOTP(requestJson);

        verify(otpHandler, times(1)).resendOTP(any(OTPRequestParsor.class));
        assertTrue(responseString.contains("\"statusCode\":200"));
        assertTrue(responseString.contains("\"status\":\"Success\""));
        assertTrue(responseString.contains("\"errorMessage\":\"Success\""));
        assertTrue(responseString.contains("\"data\":{\"response\":\"success\"}"));
    }

    @Test
    void testResendOTP_HandlerReturnsFailureString() throws Exception {
        String requestJson = "{\"mobNo\":\"0987654321\"}";
        when(otpHandler.resendOTP(any(OTPRequestParsor.class))).thenReturn("failure");

        String responseString = otpGateway.resendOTP(requestJson);

        verify(otpHandler, times(1)).resendOTP(any(OTPRequestParsor.class));
        assertTrue(responseString.contains("\"statusCode\":5000"));
        assertTrue(responseString.contains("\"status\":\"failure\""));
        assertTrue(responseString.contains("\"errorMessage\":\"failure\""));
    }

    @Test
    void testResendOTP_InputMapperThrowsException() throws Exception {
        String requestJson = "{invalid json for resend}";

        String responseString = otpGateway.resendOTP(requestJson);

        verify(otpHandler, times(0)).resendOTP(any(OTPRequestParsor.class));
        verify(logger, times(1)).error(Mockito.startsWith("error in re-sending OTP : "));
        assertTrue(responseString.contains("\"statusCode\":5000"));
        assertTrue(responseString.contains("\"status\":\"error : com.google.gson.JsonSyntaxException"));
        assertTrue(responseString.contains("\"errorMessage\":\"error : com.google.gson.JsonSyntaxException"));
    }

    @Test
    void testResendOTP_HandlerThrowsException() throws Exception {
        String requestJson = "{\"mobNo\":\"0987654321\"}";
        when(otpHandler.resendOTP(any(OTPRequestParsor.class))).thenThrow(new IllegalStateException("Resend service error"));

        String responseString = otpGateway.resendOTP(requestJson);

        verify(otpHandler, times(1)).resendOTP(any(OTPRequestParsor.class));
        verify(logger, times(1)).error(Mockito.startsWith("error in re-sending OTP : "));
        assertTrue(responseString.contains("\"statusCode\":5000"));
        assertTrue(responseString.contains("\"status\":\"error : java.lang.IllegalStateException: Resend service error"));
        assertTrue(responseString.contains("\"errorMessage\":\"error : java.lang.IllegalStateException: Resend service error"));
    }
}
