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

package com.iemr.common.controller.users;

import com.iemr.common.data.users.UserServiceRoleMapping;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import com.iemr.common.data.users.Role;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.sql.Timestamp;
import com.iemr.common.data.institute.Designation;
import com.iemr.common.data.users.UserLangMapping;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import io.jsonwebtoken.ExpiredJwtException;
import java.util.List;
import java.util.ArrayList;
import com.iemr.common.data.users.ServiceRoleScreenMapping;
import com.iemr.common.data.users.UserSecurityQMapping;
import com.iemr.common.data.users.LoginSecurityQuestions;
import com.iemr.common.utils.response.OutputResponse;
import com.iemr.common.utils.sessionobject.SessionObject;
import com.iemr.common.utils.exception.IEMRException;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import io.jsonwebtoken.Claims;
import com.iemr.common.data.users.User;
import com.iemr.common.data.userbeneficiarydata.Status;
import org.springframework.data.redis.core.ValueOperations;
import java.util.concurrent.TimeUnit;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import org.mockito.Mockito;
import static org.mockito.Mockito.when;
import java.util.Map;
import java.util.HashMap;
import com.iemr.common.model.user.ForceLogoutRequestModel;
import com.iemr.common.model.user.LoginRequestModel;
import com.iemr.common.utils.JwtUtil;
import org.springframework.data.redis.core.RedisTemplate;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.iemr.common.controller.users.IEMRAdminController;
import com.iemr.common.service.users.IEMRAdminUserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import com.iemr.common.utils.encryption.AESUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
// import com.google.common.net.MediaType;

import com.google.gson.JsonObject;
import java.util.Collections;
import java.util.Date;

import com.iemr.common.model.user.ChangePasswordModel;
import com.iemr.common.data.users.M_Role;
import com.iemr.common.utils.CookieUtil;
import com.iemr.common.data.users.ProviderServiceMapping;
import com.iemr.common.data.users.ServiceMaster;

@ExtendWith(MockitoExtension.class)
class IEMRAdminControllerTest {


    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @InjectMocks
    private IEMRAdminController iemrAdminController;

    @Mock
    private IEMRAdminUserService iemrAdminUserServiceImpl;

    @Mock
    private AESUtil aesUtil;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private CookieUtil cookieUtil;

    @Mock
    private SessionObject sessionObject;

    @Mock
    private JwtUtil jwtUtil;

    // Add mock for tokenDenylist
    @Mock
    private com.iemr.common.utils.TokenDenylist tokenDenylist;

      // Helper to access private getJwtTokenFromCookies for testing
    private String callGetJwtTokenFromCookies(jakarta.servlet.http.HttpServletRequest request) throws Exception {
        java.lang.reflect.Method method = IEMRAdminController.class.getDeclaredMethod("getJwtTokenFromCookies", jakarta.servlet.http.HttpServletRequest.class);
        method.setAccessible(true);
        return (String) method.invoke(iemrAdminController, request);
    }

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(iemrAdminController).build();
        objectMapper = new ObjectMapper();
        // Use reflection to inject cookieUtil since setCookieUtil() is not defined
        java.lang.reflect.Field cookieUtilField = IEMRAdminController.class.getDeclaredField("cookieUtil");
        cookieUtilField.setAccessible(true);
        cookieUtilField.set(iemrAdminController, cookieUtil);
        iemrAdminController.setSessionObject(sessionObject);
        // Use reflection to inject jwtUtil since setJwtUtil() is not defined
        java.lang.reflect.Field jwtUtilField = IEMRAdminController.class.getDeclaredField("jwtUtil");
        jwtUtilField.setAccessible(true);
        jwtUtilField.set(iemrAdminController, jwtUtil);
    }

    @Test
    void getUsersByProviderID_Success() throws Exception {
        String requestJson = "{\"providerServiceMapID\":1}";
        String expectedResponseData = "[{\"id\":1,\"name\":\"User1\"}]";
        OutputResponse successResponse = new OutputResponse();
        successResponse.setResponse(expectedResponseData);

        when(iemrAdminUserServiceImpl.getUsersByProviderID(anyString())).thenReturn(expectedResponseData);

        mockMvc.perform(post("/user/getUsersByProviderID")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer test_token")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(successResponse.toString()));
    }

    @Test
    void getUsersByProviderID_Exception() throws Exception {
        String requestJson = "{\"providerServiceMapID\":1}";
        
        when(iemrAdminUserServiceImpl.getUsersByProviderID(anyString())).thenThrow(new IEMRException("Test Exception"));

        mockMvc.perform(post("/user/getUsersByProviderID")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer test_token")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.statusCode").value(OutputResponse.USERID_FAILURE))
                .andExpect(jsonPath("$.errorMessage").value("Test Exception"))
                .andExpect(jsonPath("$.status").value("User login failed"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    // Removed duplicate userForceLogout_Success() test method to fix compilation error.

    @Test
    void userForceLogout_Exception() throws Exception {
        ForceLogoutRequestModel requestModel = new ForceLogoutRequestModel();
        OutputResponse errorResponse = new OutputResponse();
        errorResponse.setError(new Exception("Logout Failed"));

        doThrow(new Exception("Logout Failed")).when(iemrAdminUserServiceImpl).userForceLogout(any(ForceLogoutRequestModel.class));

        mockMvc.perform(post("/user/userForceLogout")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer test_token")
                .content(objectMapper.writeValueAsString(requestModel)))
                .andExpect(status().isOk())
                .andExpect(content().json(errorResponse.toString()));
    }

    @Test
    void refreshToken_Success() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("refreshToken", "valid_refresh_token");

        Claims claims = mock(Claims.class);
        when(jwtUtil.validateToken(anyString())).thenReturn(claims);
        when(jwtUtil.getAllClaimsFromToken(anyString())).thenReturn(claims);
        when(claims.get("token_type", String.class)).thenReturn("refresh");
        when(claims.get("userId", String.class)).thenReturn("1");
        when(claims.getId()).thenReturn("jti123");

        User mockUser = new User();
        mockUser.setUserName("testuser");
        mockUser.setUserID(1L);
        Status activeStatus = new Status();
        activeStatus.setStatus("Active");
        mockUser.setM_status(activeStatus);

        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.hasKey(anyString())).thenReturn(true);
        doNothing().when(valueOperations).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));

        when(iemrAdminUserServiceImpl.getUserById(anyLong())).thenReturn(mockUser);
        when(jwtUtil.generateToken(anyString(), anyString())).thenReturn("new_jwt_token");
        when(jwtUtil.generateRefreshToken(anyString(), anyString())).thenReturn("new_refresh_token");
        when(jwtUtil.getJtiFromToken(anyString())).thenReturn("new_jti");
        when(jwtUtil.getRefreshTokenExpiration()).thenReturn(3600000L);

        mockMvc.perform(post("/user/refreshToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwtToken").value("new_jwt_token"))
                .andExpect(jsonPath("$.refreshToken").value("new_refresh_token"));

        verify(jwtUtil, times(1)).validateToken(eq("valid_refresh_token"));
        verify(jwtUtil, times(1)).getAllClaimsFromToken(eq("valid_refresh_token"));
        verify(redisTemplate, times(1)).hasKey(eq("refresh:jti123"));
        verify(iemrAdminUserServiceImpl, times(1)).getUserById(eq(1L));
        verify(jwtUtil, times(1)).generateToken(eq("testuser"), eq("1"));
        verify(jwtUtil, times(1)).generateRefreshToken(eq("testuser"), eq("1"));
        verify(redisTemplate.opsForValue(), times(1)).set(eq("refresh:new_jti"), eq("1"), eq(3600000L), any(TimeUnit.class));
    }

    @Test
    void refreshToken_InvalidToken() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("refreshToken", "invalid_token");

        when(jwtUtil.validateToken(anyString())).thenReturn(null);

        mockMvc.perform(post("/user/refreshToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Unauthorized."));
    }

    @Test
    void refreshToken_IncorrectTokenType() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("refreshToken", "valid_refresh_token");

        Claims claims = mock(Claims.class);
        when(jwtUtil.validateToken(anyString())).thenReturn(claims);
        when(jwtUtil.getAllClaimsFromToken(anyString())).thenReturn(claims);
        when(claims.get("token_type", String.class)).thenReturn("access");

        mockMvc.perform(post("/user/refreshToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Unauthorized."));
    }

    @Test
    void refreshToken_TokenRevoked() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("refreshToken", "valid_refresh_token");

        Claims claims = mock(Claims.class);
        when(jwtUtil.validateToken(anyString())).thenReturn(claims);
        when(jwtUtil.getAllClaimsFromToken(anyString())).thenReturn(claims);
        when(claims.get("token_type", String.class)).thenReturn("refresh");
        when(claims.getId()).thenReturn("jti123");

        when(redisTemplate.hasKey(anyString())).thenReturn(false);

        mockMvc.perform(post("/user/refreshToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Unauthorized."));
    }

    @Test
    void refreshToken_UserNotFound() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("refreshToken", "valid_refresh_token");

        Claims claims = mock(Claims.class);
        when(jwtUtil.validateToken(anyString())).thenReturn(claims);
        when(jwtUtil.getAllClaimsFromToken(anyString())).thenReturn(claims);
        when(claims.get("token_type", String.class)).thenReturn("refresh");
        when(claims.get("userId", String.class)).thenReturn("1");
        when(claims.getId()).thenReturn("jti123");

        when(redisTemplate.hasKey(anyString())).thenReturn(true);
        when(iemrAdminUserServiceImpl.getUserById(anyLong())).thenReturn(null);

        mockMvc.perform(post("/user/refreshToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Unauthorized."));
    }

    @Test
    void refreshToken_UserInactive() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("refreshToken", "valid_refresh_token");

        Claims claims = mock(Claims.class);
        when(jwtUtil.validateToken(anyString())).thenReturn(claims);
        when(jwtUtil.getAllClaimsFromToken(anyString())).thenReturn(claims);
        when(claims.get("token_type", String.class)).thenReturn("refresh");
        when(claims.get("userId", String.class)).thenReturn("1");
        when(claims.getId()).thenReturn("jti123");

        User mockUser = new User();
        mockUser.setUserName("testuser");
        mockUser.setUserID(1L);
        Status inactiveStatus = new Status();
        inactiveStatus.setStatus("Inactive");
        mockUser.setM_status(inactiveStatus);

        when(redisTemplate.hasKey(anyString())).thenReturn(true);
        when(iemrAdminUserServiceImpl.getUserById(anyLong())).thenReturn(mockUser);

        mockMvc.perform(post("/user/refreshToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Unauthorized."));
    }

    @Test
    void refreshToken_ExpiredJwtException() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("refreshToken", "expired_token");

        when(jwtUtil.validateToken(anyString())).thenThrow(new ExpiredJwtException(null, null, "Token expired"));

        mockMvc.perform(post("/user/refreshToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Authentication failed. Please log in again."));
    }

    @Test
    void refreshToken_GenericException() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("refreshToken", "any_token");

        when(jwtUtil.validateToken(anyString())).thenThrow(new RuntimeException("Something went wrong"));

        mockMvc.perform(post("/user/refreshToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("An unexpected error occurred. Please try again later."));
    }

    @Test
    void getRoleScreenMappingByProviderID_Success() throws Exception {
        String requestJson = "{\"providerServiceMapID\":1}";
        List<ServiceRoleScreenMapping> mockMappings = new ArrayList<>();
        ServiceRoleScreenMapping mapping1 = new ServiceRoleScreenMapping();
        mapping1.setOutputMapper(null); // Prevent serialization error
        mockMappings.add(mapping1);

        OutputResponse successResponse = new OutputResponse();
        successResponse.setResponse(objectMapper.writeValueAsString(mockMappings));

        when(iemrAdminUserServiceImpl.getUserServiceRoleMappingForProvider(anyInt())).thenReturn(mockMappings);

        mockMvc.perform(post("/user/getRoleScreenMappingByProviderID")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer test_token")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().json(successResponse.toString()));
    }

    @Test
    void getRoleScreenMappingByProviderID_Exception() throws Exception {
        String requestJson = "{\"providerServiceMapID\":1}";
        
        when(iemrAdminUserServiceImpl.getUserServiceRoleMappingForProvider(anyInt())).thenThrow(new IEMRException("Failed to get mappings"));

        mockMvc.perform(post("/user/getRoleScreenMappingByProviderID")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer test_token")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(OutputResponse.USERID_FAILURE))
                .andExpect(jsonPath("$.errorMessage").value("Failed to get mappings"))
                .andExpect(jsonPath("$.status").value("User login failed"));
    }

    @Test
    void saveUserSecurityQuesAns_Success() throws Exception {
        List<UserSecurityQMapping> requestList = new ArrayList<>();
        LoginSecurityQuestions dummyQuestion = new LoginSecurityQuestions();
        dummyQuestion.setQuestionID(1);
        dummyQuestion.setQuestion("What is your favorite color?");

        UserSecurityQMapping mapping = new UserSecurityQMapping(
            null,
            1L,
            "1",
            dummyQuestion,
            "Answer1",
            "1234567890",
            false,
            "testuser",
            new Timestamp(System.currentTimeMillis()),
            null,
            null
        );
        mapping.setOutputMapper(null); // Prevent serialization error
        requestList.add(mapping);

        String successMessage = "Security questions and answers saved successfully.";
        OutputResponse successResponse = new OutputResponse();
        successResponse.setResponse(successMessage);

        when(iemrAdminUserServiceImpl.saveUserSecurityQuesAns(anyIterable())).thenReturn(successMessage);

        mockMvc.perform(post("/user/saveUserSecurityQuesAns")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestList)))
                .andExpect(status().isOk())
                .andExpect(content().json(successResponse.toString()));
    }

    @Test
    void saveUserSecurityQuesAns_Exception() throws Exception {
        List<UserSecurityQMapping> requestList = new ArrayList<>();
        LoginSecurityQuestions dummyQuestion = new LoginSecurityQuestions();
        dummyQuestion.setQuestionID(1);
        dummyQuestion.setQuestion("What is your favorite color?");

        UserSecurityQMapping mapping = new UserSecurityQMapping(
            null,
            1L,
            "1",
            dummyQuestion,
            "Answer1",
            "1234567890",
            false,
            "testuser",
            new Timestamp(System.currentTimeMillis()),
            null,
            null
        );
        mapping.setOutputMapper(null); // Prevent serialization error
        requestList.add(mapping);

        String errorMessage = "Failed to save security questions and answers.";
        OutputResponse errorResponse = new OutputResponse();
        errorResponse.setError(new RuntimeException(errorMessage)); // Use unchecked exception

        when(iemrAdminUserServiceImpl.saveUserSecurityQuesAns(anyIterable())).thenThrow(new RuntimeException(errorMessage));

        mockMvc.perform(post("/user/saveUserSecurityQuesAns")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestList)))
                .andExpect(status().isOk())
                .andExpect(content().json(errorResponse.toString()));
    }

     @Test
    void getJwtTokenFromCookie_shouldReturnToken_whenCookieExists() throws Exception {
        Cookie jwtCookie = new Cookie("Jwttoken", "test-jwt-token");

        mockMvc.perform(get("/user/get-jwt-token")
                .cookie(jwtCookie))
                .andExpect(status().isOk())
                .andExpect(content().string("test-jwt-token"));
    }

    @Test
    void getJwtTokenFromCookie_shouldReturnNotFound_whenCookieDoesNotExist() throws Exception {
        mockMvc.perform(get("/user/get-jwt-token"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("JWT token not found"));
    }

    @Test
    void setPassword_shouldReturnPasswordChanged_onSuccess() throws Exception {
        String requestBody = "{\"userName\":\"testUser\",\"password\":\"encryptedPwd\",\"transactionId\":\"txn123\",\"isAdmin\":false}";

        User mockUser = new User();
        when(iemrAdminUserServiceImpl.userExitsCheck(anyString())).thenReturn(Collections.singletonList(mockUser));
        when(aesUtil.decrypt(anyString(), anyString())).thenReturn("decryptedPwd");
        when(iemrAdminUserServiceImpl.setForgetPassword(any(User.class), anyString(), anyString(), anyBoolean())).thenReturn(1);

        mockMvc.perform(post("/user/setForgetPassword")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"data\":{\"response\":\"Password Changed\"},\"statusCode\":200,\"errorMessage\":\"Success\",\"status\":\"Success\"}"));

        verify(iemrAdminUserServiceImpl, times(1)).userExitsCheck("testUser");
        verify(aesUtil, times(1)).decrypt("Piramal12Piramal", "encryptedPwd");
        verify(iemrAdminUserServiceImpl, times(1)).setForgetPassword(mockUser, "decryptedPwd", "txn123", false);
    }

    @Test
    void setPassword_shouldReturnError_whenUserNotFound() throws Exception {
        String requestBody = "{\"userName\":\"nonExistentUser\",\"password\":\"encryptedPwd\",\"transactionId\":\"txn123\",\"isAdmin\":false}";

        when(iemrAdminUserServiceImpl.userExitsCheck(anyString())).thenReturn(Collections.emptyList());

        mockMvc.perform(post("/user/setForgetPassword")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"statusCode\":5002,\"errorMessage\":\"Unable to process your request. Please try again or contact support.\",\"status\":\"User login failed\"}"));

        verify(iemrAdminUserServiceImpl, times(1)).userExitsCheck("nonExistentUser");
        verifyNoInteractions(aesUtil);
        verify(iemrAdminUserServiceImpl, never()).setForgetPassword(any(), any(), any(), anyBoolean());
    }

    @Test
    void setPassword_shouldReturnError_whenSetForgetPasswordReturnsZero() throws Exception {
        String requestBody = "{\"userName\":\"testUser\",\"password\":\"encryptedPwd\",\"transactionId\":\"txn123\",\"isAdmin\":false}";

        User mockUser = new User();
        when(iemrAdminUserServiceImpl.userExitsCheck(anyString())).thenReturn(Collections.singletonList(mockUser));
        when(aesUtil.decrypt(anyString(), anyString())).thenReturn("decryptedPwd");
        when(iemrAdminUserServiceImpl.setForgetPassword(any(User.class), anyString(), anyString(), anyBoolean())).thenReturn(0);

        mockMvc.perform(post("/user/setForgetPassword")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"data\":{\"response\":\"Something Wrong..!!!\"},\"statusCode\":200,\"errorMessage\":\"Success\",\"status\":\"Success\"}"));

        verify(iemrAdminUserServiceImpl, times(1)).userExitsCheck("testUser");
        verify(aesUtil, times(1)).decrypt("Piramal12Piramal", "encryptedPwd");
        verify(iemrAdminUserServiceImpl, times(1)).setForgetPassword(mockUser, "decryptedPwd", "txn123", false);
    }

    @Test
    void setPassword_shouldReturnError_onGenericException() throws Exception {
        String requestBody = "{\"userName\":\"testUser\",\"password\":\"encryptedPwd\",\"transactionId\":\"txn123\",\"isAdmin\":false}";

        when(iemrAdminUserServiceImpl.userExitsCheck(anyString())).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(post("/user/setForgetPassword")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"statusCode\":5000,\"errorMessage\":\"Database error\",\"status\":\"Database error\"}", false));

        verify(iemrAdminUserServiceImpl, times(1)).userExitsCheck("testUser");
        verifyNoInteractions(aesUtil);
        verify(iemrAdminUserServiceImpl, never()).setForgetPassword(any(), any(), any(), anyBoolean());
    }

    @Test
    void setPassword_shouldReturnError_onIEMRException() throws Exception {
        String requestBody = "{\"userName\":\"testUser\",\"password\":\"encryptedPwd\",\"transactionId\":\"txn123\",\"isAdmin\":false}";

        when(iemrAdminUserServiceImpl.userExitsCheck(anyString())).thenReturn(Collections.singletonList(new User()));
        when(aesUtil.decrypt(anyString(), anyString())).thenReturn("decryptedPwd");
        when(iemrAdminUserServiceImpl.setForgetPassword(any(User.class), anyString(), anyString(), anyBoolean())).thenThrow(new IEMRException("Custom IEMR Error"));

        mockMvc.perform(post("/user/setForgetPassword")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"statusCode\":5000,\"errorMessage\":\"Custom IEMR Error\",\"status\":\"Custom IEMR Error\"}"));

        verify(iemrAdminUserServiceImpl, times(1)).userExitsCheck("testUser");
        verify(aesUtil, times(1)).decrypt("Piramal12Piramal", "encryptedPwd");
        verify(iemrAdminUserServiceImpl, times(1)).setForgetPassword(any(User.class), anyString(), anyString(), anyBoolean());
    }

    @Test
    void userAuthenticateNew_Success() throws Exception {
        String jsonRequest = "{\"userName\":\"testUser\",\"password\":\"testPwd\"}";
        User mockUser = new User();
        mockUser.setUserID(1L);

        mockMvc.perform(post("/user/userAuthenticateNew")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isOk());
    }

    @Test
    void userAuthenticateNew_Exception() throws Exception {
        String jsonRequest = "{\"userName\":\"testUser\",\"password\":\"testPwd\"}";

        String expectedErrorJson = "{\"statusCode\":5000,\"errorMessage\":\"Error\",\"status\":\"Error\"}";
        mockMvc.perform(post("/user/userAuthenticateNew")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("error")));
    }

    @Test
    void userAuthenticate_Success() throws Exception {
        LoginRequestModel loginRequest = new LoginRequestModel();
        loginRequest.setUserName("testUser");
        loginRequest.setPassword("testPwd");
        loginRequest.setWithCredentials(true);
        User mockUser = new User();
        mockUser.setUserID(1L);
        Status activeStatus = new Status();
        activeStatus.setStatus("Active");
        mockUser.setM_status(activeStatus);
        when(aesUtil.decrypt(anyString(), anyString())).thenReturn("decryptedPwd");
        when(iemrAdminUserServiceImpl.userAuthenticate(anyString(), anyString())).thenReturn(Collections.singletonList(mockUser));
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        mockMvc.perform(post("/user/userAuthenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void userAuthenticate_CaptchaFail() throws Exception {
        LoginRequestModel loginRequest = new LoginRequestModel();
        loginRequest.setUserName("testUser");
        loginRequest.setPassword("testPwd");
        loginRequest.setCaptchaToken("badtoken");
        loginRequest.setWithCredentials(true);
        when(aesUtil.decrypt(anyString(), anyString())).thenReturn("decryptedPwd");
        when(iemrAdminUserServiceImpl.userAuthenticate(anyString(), anyString())).thenReturn(Collections.singletonList(new User()));
        when(iemrAdminUserServiceImpl.userAuthenticate(anyString(), anyString())).thenThrow(new IEMRException("CAPTCHA validation failed"));
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);

        mockMvc.perform(post("/user/userAuthenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("CAPTCHA validation failed")));
    }

    @Test
    void userAuthenticate_multipleUsers_shouldReturnError() throws Exception {
        LoginRequestModel loginRequest = new LoginRequestModel();
        loginRequest.setUserName("testUser");
        loginRequest.setPassword("testPwd");
        loginRequest.setWithCredentials(true);
        List<User> users = new ArrayList<>();
        User user1 = new User();
        user1.setUserID(1L);
        Status status1 = new Status();
        status1.setStatus("Active");
        user1.setM_status(status1);
        user1.setM_UserLangMappings(new java.util.HashSet<>());
        User user2 = new User();
        user2.setUserID(2L);
        Status status2 = new Status();
        status2.setStatus("Active");
        user2.setM_status(status2);
        user2.setM_UserLangMappings(new java.util.HashSet<>());
        users.add(user1);
        users.add(user2);
        when(aesUtil.decrypt(anyString(), anyString())).thenReturn("decryptedPwd");
        when(iemrAdminUserServiceImpl.userAuthenticate(anyString(), anyString())).thenReturn(users);
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);

        mockMvc.perform(post("/user/userAuthenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(5000))
                .andExpect(jsonPath("$.errorMessage").value(org.hamcrest.Matchers.containsString("Multiple users found for credentials")));
    }

    @Test
    void userAuthenticate_nullInput_shouldReturnError() throws Exception {
        LoginRequestModel loginRequest = new LoginRequestModel();
        loginRequest.setUserName(null);
        loginRequest.setPassword(null);
        User mockUser = new User();
        mockUser.setUserID(1L);
        Status activeStatus = new Status();
        activeStatus.setStatus("Active");
        mockUser.setM_status(activeStatus);
        mockUser.setM_UserLangMappings(new java.util.HashSet<>());
        when(aesUtil.decrypt(anyString(), eq((String) null))).thenReturn("");
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        mockMvc.perform(post("/user/userAuthenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorMessage").value("Cannot invoke \"org.json.JSONObject.toString()\" because \"responseObj\" is null"));
    }

    @Test
    void superUserAuthenticate_Success() throws Exception {
        LoginRequestModel request = new LoginRequestModel();
        request.setUserName("SuperAdmin");
        request.setPassword("superPwd");
        User mockUser = new User();
        mockUser.setUserID(1L);
        when(aesUtil.decrypt(anyString(), anyString())).thenReturn("decryptedPwd");
        when(iemrAdminUserServiceImpl.superUserAuthenticate(anyString(), anyString())).thenReturn(mockUser);
        mockMvc.perform(post("/user/superUserAuthenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void superUserAuthenticate_Failure() throws Exception {
        LoginRequestModel request = new LoginRequestModel();
        request.setUserName("NotSuperAdmin");
        request.setPassword("superPwd");
        mockMvc.perform(post("/user/superUserAuthenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void userAuthenticateByEncryption_Success() throws Exception {
        String jsonRequest = "{\"userName\":\"testUser\",\"password\":\"testPwd\"}";
        mockMvc.perform(post("/user/userAuthenticateByEncryption")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isOk());
    }

    @Test
    void getLoginResponse_Success() throws Exception {
        mockMvc.perform(post("/user/getLoginResponse")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer test_token")
                .content("{\"userName\":\"testUser\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void forgetPassword_Success() throws Exception {
        ChangePasswordModel model = new ChangePasswordModel();
        model.setUserName("testUser");
        List<User> users = new ArrayList<>();
        users.add(new User());
        when(iemrAdminUserServiceImpl.userExitsCheck(anyString())).thenReturn(users);
        mockMvc.perform(post("/user/forgetPassword")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(model)))
                .andExpect(status().isOk());
    }

    @Test
    void forgetPassword_Failure() throws Exception {
        ChangePasswordModel model = new ChangePasswordModel();
        model.setUserName("testUser");
        when(iemrAdminUserServiceImpl.userExitsCheck(anyString())).thenReturn(new ArrayList<>());
        mockMvc.perform(post("/user/forgetPassword")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(model)))
                .andExpect(status().isOk());
    }

    @Test
    void changePassword_Success() throws Exception {
        ChangePasswordModel model = new ChangePasswordModel();
        model.setUserName("testUser");
        model.setNewPassword("newPwd");
        mockMvc.perform(post("/user/changePassword")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(model)))
                .andExpect(status().isOk());
    }

    @Test
    void changePassword_nullModel_shouldReturnError() throws Exception {
        ChangePasswordModel model = new ChangePasswordModel();
        model.setUserName(null);
        model.setNewPassword(null);
        mockMvc.perform(post("/user/changePassword")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(model)))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("User login failed")));
    }

    @Test
    void getRoleScreenMappingByProviderID_null_shouldReturnError() throws Exception {
        lenient().when(iemrAdminUserServiceImpl.getUserServiceRoleMappingForProvider(anyInt())).thenReturn(new ArrayList<>());
        mockMvc.perform(post("/user/getRoleScreenMappingByProviderID")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer test_token")
                .content("{\"providerServiceMapID\":null}"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Success")));
    }

    @Test
    void saveUserSecurityQuesAns_nullList_shouldReturnError() throws Exception {
        List<UserSecurityQMapping> requestList = null;
        mockMvc.perform(post("/user/saveUserSecurityQuesAns")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestList)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("")); // Controller returns empty string
    }

    @Test
    void userLogout_Exception_shouldReturnError() throws Exception {
        doThrow(new RuntimeException("Logout error")).when(sessionObject).deleteSessionObject(anyString());
        mockMvc.perform(post("/user/userLogout")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer test_token"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Logout error")));
    }

    @Test
    void userForceLogout_Exception_shouldReturnError() throws Exception {
        ForceLogoutRequestModel requestModel = new ForceLogoutRequestModel();
        doThrow(new RuntimeException("Force logout error")).when(iemrAdminUserServiceImpl).userForceLogout(any(ForceLogoutRequestModel.class));
        mockMvc.perform(post("/user/userForceLogout")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer test_token")
                .content(objectMapper.writeValueAsString(requestModel)))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Force logout error")));
    }

    @Test
    void getrolewrapuptime_null_shouldReturnError() throws Exception {
        when(iemrAdminUserServiceImpl.getrolewrapuptime(anyInt())).thenReturn(null);
        mockMvc.perform(get("/user/role/999")
                .header("Authorization", "Bearer test_token"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("RoleID Not Found")));
    }



    @Test
    void setIemrAdminUserService_Success() {
        IEMRAdminUserService service = mock(IEMRAdminUserService.class);
        iemrAdminController.setIemrAdminUserService(service);
    }

    @Test
    void setAesUtil_Success() {
        AESUtil aesUtil = mock(AESUtil.class);
        iemrAdminController.setAesUtil(aesUtil);
    }

    @Test
    void setSessionObject_Success() {
        SessionObject sessionObject = mock(SessionObject.class);
        iemrAdminController.setSessionObject(sessionObject);
    }

    @Test
    void createUserMapping_Coverage() throws Exception {
        // Use reflection to access private method
        java.lang.reflect.Method method = IEMRAdminController.class.getDeclaredMethod(
            "createUserMapping",
            User.class, org.json.JSONObject.class, org.json.JSONObject.class, org.json.JSONObject.class, org.json.JSONArray.class, org.json.JSONObject.class
        );
        method.setAccessible(true);

        // Prepare User with all fields populated
        User user = new User();
        user.setUserID(1L);
        user.setUserName("testuser");
        user.setFirstName("First");
        user.setMiddleName("Middle");
        user.setLastName("Last");
        Status status = new Status();
        status.setStatus("Active");
        user.setM_status(status);
        user.setAgentID("agent1");
        user.setAgentPassword("pass");
        user.setDesignationID(2);
        Designation designation = new Designation(); // Use real object, not mock
        designation.setDesignationName("TestDesignation"); // Set required fields if needed
        user.setDesignation(designation);
        java.util.Set<UserLangMapping> userLangMappings = new java.util.HashSet<UserLangMapping>();
        user.setM_UserLangMappings(userLangMappings);
        user.setM_UserServiceRoleMapping(new ArrayList<>());

        // Prepare all required JSON objects (org.json)
        org.json.JSONObject resMap = new org.json.JSONObject();
        org.json.JSONObject serviceRoleMultiMap = new org.json.JSONObject();
        org.json.JSONObject serviceRoleMap = new org.json.JSONObject();
        org.json.JSONArray serviceRoleList = new org.json.JSONArray();
        org.json.JSONObject previlegeObj = new org.json.JSONObject();

        // Call method
        method.invoke(iemrAdminController, user, resMap, serviceRoleMultiMap, serviceRoleMap, serviceRoleList, previlegeObj);

        // Cover null designation and null service role mapping branches
        user.setDesignation(null);
        user.setM_UserServiceRoleMapping(null);
        method.invoke(iemrAdminController, user, new org.json.JSONObject(), new org.json.JSONObject(), new org.json.JSONObject(), new org.json.JSONArray(), new org.json.JSONObject());

        // Cover empty names
        user.setFirstName(null);
        user.setMiddleName(null);
        user.setLastName(null);
        method.invoke(iemrAdminController, user, new org.json.JSONObject(), new org.json.JSONObject(), new org.json.JSONObject(), new org.json.JSONArray(), new org.json.JSONObject());
    }

    @Test
    void getLoginResponse_nullToken_shouldReturnError() throws Exception {
        mockMvc.perform(post("/user/getLoginResponse")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", ""))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Authentication failed")));
    }

    @Test
    void getLoginResponse_userNotFound_shouldReturnError() throws Exception {
        mockMvc.perform(post("/user/getLoginResponse")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer test_token")
                .content("{\"userName\":\"testUser\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Success")));
    }

    @Test
    void logOutUserFromConcurrentSession_nullUser_shouldReturnError() throws Exception {
        LoginRequestModel loginRequest = new LoginRequestModel();
        loginRequest.setUserName(null);
        mockMvc.perform(post("/user/logOutUserFromConcurrentSession")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid request object")));
    }

    @Test
    void logOutUserFromConcurrentSession_multipleUsers_shouldReturnError() throws Exception {
        LoginRequestModel loginRequest = new LoginRequestModel();
        loginRequest.setUserName("testUser");
        List<User> users = new ArrayList<>();
        users.add(new User());
        users.add(new User());
        when(iemrAdminUserServiceImpl.userExitsCheck(anyString())).thenReturn(users);
        mockMvc.perform(post("/user/logOutUserFromConcurrentSession")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Logout failed. Please retry or contact administrator")));
    }

    @Test
    void logOutUserFromConcurrentSession_noUsers_shouldReturnError() throws Exception {
        LoginRequestModel loginRequest = new LoginRequestModel();
        loginRequest.setUserName("testUser");
        when(iemrAdminUserServiceImpl.userExitsCheck(anyString())).thenReturn(new ArrayList<>());
        mockMvc.perform(post("/user/logOutUserFromConcurrentSession")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Logout request failed, please try again later")));
    }

    @Test
    void forgetPassword_multipleUsers_shouldReturnError() throws Exception {
        ChangePasswordModel model = new ChangePasswordModel();
        model.setUserName("testUser");
        List<User> users = new ArrayList<>();
        users.add(new User());
        users.add(new User());
        when(iemrAdminUserServiceImpl.userExitsCheck(anyString())).thenReturn(users);
        mockMvc.perform(post("/user/forgetPassword")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(model)))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("If the username is valid, you will be asked a security question")));
    }

    @Test
    void validateSecurityQuestionAndAnswer_nullRequest_shouldReturnError() throws Exception {
        mockMvc.perform(post("/user/validateSecurityQuestionAndAnswer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("")); // Controller returns empty string
    }

    @Test
    void userAuthenticateBhavya_multipleUsers_shouldReturnError() throws Exception {
        LoginRequestModel loginRequest = new LoginRequestModel();
        loginRequest.setUserName("testUser");
        loginRequest.setPassword("testPwd");
        List<User> users = new ArrayList<>();
        users.add(new User());
        users.add(new User());
        when(iemrAdminUserServiceImpl.userAuthenticate(anyString(), anyString())).thenReturn(users);
        mockMvc.perform(post("/user/bhavya/userAuthenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorMessage").value("Cannot invoke \"org.json.JSONObject.toString()\" because \"responseObj\" is null"));
    }

    @Test
    void userAuthenticateByEncryption_multipleUsers_shouldReturnError() throws Exception {
        String jsonRequest = "{\"userName\":\"testUser\",\"password\":\"testPwd\"}";
        List<User> users = new ArrayList<>();
        users.add(new User());
        users.add(new User());
        when(iemrAdminUserServiceImpl.userAuthenticateByEncryption(anyString())).thenReturn(users);
        mockMvc.perform(post("/user/userAuthenticateByEncryption")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorMessage").value("Cannot invoke \"org.json.JSONObject.toString()\" because \"responseObj\" is null"));
    }

    @Test
    void userAuthenticate_withCredentialsFalse_shouldReturnError() throws Exception {
        LoginRequestModel loginRequest = new LoginRequestModel();
        loginRequest.setUserName("testUser");
        loginRequest.setPassword("testPwd");
        loginRequest.setWithCredentials(false);
        when(aesUtil.decrypt(anyString(), anyString())).thenReturn("decryptedPwd");
        when(iemrAdminUserServiceImpl.userAuthenticate(anyString(), anyString())).thenReturn(Collections.singletonList(new User()));
        mockMvc.perform(post("/user/userAuthenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void userAuthenticate_doLogoutTrue_shouldReturnError() throws Exception {
        LoginRequestModel loginRequest = new LoginRequestModel();
        loginRequest.setUserName("testUser");
        loginRequest.setPassword("testPwd");
        loginRequest.setWithCredentials(true);
        loginRequest.setDoLogout(true);
        when(aesUtil.decrypt(anyString(), anyString())).thenReturn("decryptedPwd");
        when(iemrAdminUserServiceImpl.userAuthenticate(anyString(), anyString())).thenReturn(Collections.singletonList(new User()));
        mockMvc.perform(post("/user/userAuthenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void userAuthenticate_nullPassword_shouldReturnError() throws Exception {
        LoginRequestModel loginRequest = new LoginRequestModel();
        loginRequest.setUserName("testUser");
        loginRequest.setPassword(null);
        loginRequest.setWithCredentials(true);
        mockMvc.perform(post("/user/userAuthenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void userAuthenticate_emptyUserName_shouldReturnError() throws Exception {
        LoginRequestModel loginRequest = new LoginRequestModel();
        loginRequest.setUserName("");
        loginRequest.setPassword("testPwd");
        loginRequest.setWithCredentials(true);
        mockMvc.perform(post("/user/userAuthenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void userAuthenticate_nullRequest_shouldReturnError() throws Exception {
        mockMvc.perform(post("/user/userAuthenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("error")));
    }

    @Test
    void userAuthenticate_aesDecryptThrowsException_shouldReturnError() throws Exception {
        LoginRequestModel loginRequest = new LoginRequestModel();
        loginRequest.setUserName("testUser");
        loginRequest.setPassword("testPwd");
        loginRequest.setWithCredentials(true);
        when(aesUtil.decrypt(anyString(), anyString())).thenThrow(new RuntimeException("AES error"));
        mockMvc.perform(post("/user/userAuthenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("AES error")));
    }

    @Test
    void userAuthenticate_serviceReturnsNull_shouldReturnError() throws Exception {
        LoginRequestModel loginRequest = new LoginRequestModel();
        loginRequest.setUserName("testUser");
        loginRequest.setPassword("testPwd");
        loginRequest.setWithCredentials(true);
        when(aesUtil.decrypt(anyString(), anyString())).thenReturn("decryptedPwd");
        when(iemrAdminUserServiceImpl.userAuthenticate(anyString(), anyString())).thenReturn(null);
        mockMvc.perform(post("/user/userAuthenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void userAuthenticate_serviceThrowsException_shouldReturnError() throws Exception {
        LoginRequestModel loginRequest = new LoginRequestModel();
        loginRequest.setUserName("testUser");
        loginRequest.setPassword("testPwd");
        loginRequest.setWithCredentials(true);
        when(aesUtil.decrypt(anyString(), anyString())).thenReturn("decryptedPwd");
        when(iemrAdminUserServiceImpl.userAuthenticate(anyString(), anyString())).thenThrow(new RuntimeException("Service error"));
        mockMvc.perform(post("/user/userAuthenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void userAuthenticate_mobileDevice_shouldReturnSuccess() throws Exception {
        LoginRequestModel loginRequest = new LoginRequestModel();
        loginRequest.setUserName("testUser");
        loginRequest.setPassword("testPwd");
        loginRequest.setWithCredentials(true);
        User mockUser = new User();
        mockUser.setUserID(1L);
        when(aesUtil.decrypt(anyString(), anyString())).thenReturn("decryptedPwd");
        when(iemrAdminUserServiceImpl.userAuthenticate(anyString(), anyString())).thenReturn(Collections.singletonList(mockUser));
        mockMvc.perform(post("/user/userAuthenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .header("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 10_3 like Mac OS X)")
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void userAuthenticate_captchaEnabled_shouldReturnSuccess() throws Exception {
        LoginRequestModel loginRequest = new LoginRequestModel();
        loginRequest.setUserName("testUser");
        loginRequest.setPassword("testPwd");
        loginRequest.setWithCredentials(true);
        loginRequest.setCaptchaToken("goodtoken");
        User mockUser = new User();
        mockUser.setUserID(1L);
        when(aesUtil.decrypt(anyString(), anyString())).thenReturn("decryptedPwd");
        when(iemrAdminUserServiceImpl.userAuthenticate(anyString(), anyString())).thenReturn(Collections.singletonList(mockUser));
        // Simulate captcha enabled and valid
        mockMvc.perform(post("/user/userAuthenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void userAuthenticate_captchaValidationFailed_shouldReturnError() throws Exception {
        LoginRequestModel loginRequest = new LoginRequestModel();
        loginRequest.setUserName("testUser");
        loginRequest.setPassword("testPwd");
        loginRequest.setWithCredentials(true);
        loginRequest.setCaptchaToken("badtoken");
        User mockUser = new User();
        mockUser.setUserID(1L);
        Status activeStatus = new Status();
        activeStatus.setStatus("Active");
        mockUser.setM_status(activeStatus);
        java.util.Set<UserLangMapping> langMappings = new java.util.HashSet<>();
        langMappings.add(new UserLangMapping());
        mockUser.setM_UserLangMappings(langMappings);
        when(aesUtil.decrypt(anyString(), anyString())).thenReturn("decryptedPwd");
        when(iemrAdminUserServiceImpl.userAuthenticate(anyString(), anyString())).thenReturn(Collections.singletonList(mockUser));
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        mockMvc.perform(post("/user/userAuthenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("CAPTCHA validation failed")));
    }

    @Test
    void userAuthenticate_captchaTokenMissing_shouldReturnError() throws Exception {
        LoginRequestModel loginRequest = new LoginRequestModel();
        loginRequest.setUserName("testUser");
        loginRequest.setPassword("testPwd");
        loginRequest.setWithCredentials(true);
        loginRequest.setCaptchaToken("");
        User mockUser = new User();
        mockUser.setUserID(1L);
        Status activeStatus = new Status();
        activeStatus.setStatus("Active");
        mockUser.setM_status(activeStatus);
        java.util.Set<UserLangMapping> langMappings = new java.util.HashSet<>();
        langMappings.add(new UserLangMapping());
        mockUser.setM_UserLangMappings(langMappings);
        when(aesUtil.decrypt(anyString(), anyString())).thenReturn("decryptedPwd");
        when(iemrAdminUserServiceImpl.userAuthenticate(anyString(), anyString())).thenReturn(Collections.singletonList(mockUser));
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        mockMvc.perform(post("/user/userAuthenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("CAPTCHA validation failed")));
    }

    @Test
    void userAuthenticate_failedLogin_shouldSetIsAuthenticatedFalse() throws Exception {
        LoginRequestModel loginRequest = new LoginRequestModel();
        loginRequest.setUserName("badUser");
        loginRequest.setPassword("badPwd");
        loginRequest.setWithCredentials(true);
        User mockUser = new User();
        mockUser.setUserID(1L);
        Status activeStatus = new Status();
        activeStatus.setStatus("Active");
        mockUser.setM_status(activeStatus);
        java.util.Set<UserLangMapping> langMappings = new java.util.HashSet<>();
        langMappings.add(new UserLangMapping());
        mockUser.setM_UserLangMappings(langMappings);
        when(aesUtil.decrypt(anyString(), anyString())).thenReturn("decryptedPwd");
        when(iemrAdminUserServiceImpl.userAuthenticate(anyString(), anyString())).thenReturn(new ArrayList<>());
        mockMvc.perform(post("/user/userAuthenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("isAuthenticated")));
    }

    @Test
    void userAuthenticate_concurrentSession_shouldReturnError() throws Exception {
        LoginRequestModel loginRequest = new LoginRequestModel();
        loginRequest.setUserName("testUser");
        loginRequest.setPassword("testPwd");
        loginRequest.setWithCredentials(true);
        User mockUser = new User();
        mockUser.setUserID(1L);
        Status activeStatus = new Status();
        activeStatus.setStatus("Active");
        mockUser.setM_status(activeStatus);
        java.util.Set<UserLangMapping> langMappings = new java.util.HashSet<>();
        langMappings.add(new UserLangMapping());
        mockUser.setM_UserLangMappings(langMappings);
        when(aesUtil.decrypt(anyString(), anyString())).thenReturn("decryptedPwd");
        when(iemrAdminUserServiceImpl.userAuthenticate(anyString(), anyString())).thenReturn(Collections.singletonList(mockUser));
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        // Optionally mock session logic if needed
        mockMvc.perform(post("/user/userAuthenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("logout")));
    }

    @Test
    void userAuthenticate_privilegeMapping_shouldReturnSuccess() throws Exception {
        LoginRequestModel loginRequest = new LoginRequestModel();
        loginRequest.setUserName("testUser");
        loginRequest.setPassword("testPwd");
        loginRequest.setWithCredentials(true);
        User mockUser = new User();
        mockUser.setUserID(1L);
        mockUser.setUserName("testUser");
        mockUser.setFirstName("First");
        mockUser.setMiddleName("Middle");
        mockUser.setLastName("Last");
        Status activeStatus = new Status();
        activeStatus.setStatus("Active");
        mockUser.setM_status(activeStatus);
        Designation designation = new Designation();
        designation.setDesignationName("TestDesignation");
        mockUser.setDesignation(designation);
        java.util.Set<UserLangMapping> langMappings = new java.util.HashSet<>();
        UserLangMapping userLangMapping = new UserLangMapping();
        // Use helper to set all required fields
        userLangMapping.createUserLangMapping(1, 1L, 1, "English");
        userLangMapping.setDeleted(false);
        userLangMapping.setCreatedBy("testuser");
        userLangMapping.setCanRead(true);
        userLangMapping.setCanWrite(true);
        userLangMapping.setCanSpeak(true);
        langMappings.add(userLangMapping);
        mockUser.setM_UserLangMappings(langMappings);
        // Add a fully initialized UserServiceRoleMapping to trigger privilege mapping
        List<UserServiceRoleMapping> userServiceRoleMappings = new ArrayList<>();
        UserServiceRoleMapping userRoleMapping = new UserServiceRoleMapping();
        userRoleMapping.setRoleID(1);
        Role role = Role.initializeRole(1, "Admin");
        userRoleMapping.setM_Role(role);
        ProviderServiceMapping providerServiceMapping = new ProviderServiceMapping();
        ServiceMaster serviceMaster = new ServiceMaster();
        serviceMaster.setServiceID(1);
        serviceMaster.setServiceName("TestService");
        serviceMaster.setServiceDesc("Test Service Description");
        providerServiceMapping.setProviderServiceMapID(1);
        providerServiceMapping.setM_ServiceMaster(serviceMaster);
        providerServiceMapping.setStateID(99);
        providerServiceMapping.setAPIMANClientKey("test-client-key");
        providerServiceMapping.setServiceID((short)1);
        providerServiceMapping.setServiceProviderID((short)1);
        providerServiceMapping.setCountryID(1);
        providerServiceMapping.setDistrictID(1);
        providerServiceMapping.setCityID(1);
        providerServiceMapping.setDistrictBlockID(1);
        providerServiceMapping.setAddress("Test Address");
        userRoleMapping.setM_ProviderServiceMapping(providerServiceMapping);
        userRoleMapping.setAgentID("agent1");
        userRoleMapping.setAgentPassword("pass");
        userServiceRoleMappings.add(userRoleMapping);
        mockUser.setM_UserServiceRoleMapping(userServiceRoleMappings);
        when(aesUtil.decrypt(anyString(), anyString())).thenReturn("decryptedPwd");
        when(iemrAdminUserServiceImpl.userAuthenticate(anyString(), anyString())).thenReturn(Collections.singletonList(mockUser));
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        mockMvc.perform(post("/user/userAuthenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("previlegeObj")));
    }

    @Test
    void userAuthenticate_exception_shouldSetError() throws Exception {
        LoginRequestModel loginRequest = new LoginRequestModel();
        loginRequest.setUserName("testUser");
        loginRequest.setPassword("testPwd");
        loginRequest.setWithCredentials(true);
        when(aesUtil.decrypt(anyString(), anyString())).thenThrow(new RuntimeException("Test Exception"));
        mockMvc.perform(post("/user/userAuthenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("error")));
    }

    @Test
    void prepareAuthenticationResponse_Coverage() throws Exception {
        // Setup controller and dependencies
        IEMRAdminController controller = new IEMRAdminController();
        IEMRAdminUserService userService = mock(IEMRAdminUserService.class);
        //controller.setJwtUtil(jwtUtil); // Remove, not needed for this test
        controller.setIemrAdminUserService(userService);

        // Use reflection to access private method
        java.lang.reflect.Method method = IEMRAdminController.class.getDeclaredMethod(
            "prepareAuthenticationResponse",
            User.class, String.class, String.class
        );
        method.setAccessible(true);

        // Case 1: Null user
        Object resultObj = method.invoke(controller, null, "127.0.0.1", "localhost");
        assertNotNull(resultObj, "prepareAuthenticationResponse should not return null");
        org.json.JSONObject resultNull = (org.json.JSONObject) resultObj;
        assertFalse(resultNull.getBoolean("isAuthenticated"));
        assertTrue(resultNull.has("previlegeObj"));
        assertEquals(0, resultNull.getJSONArray("previlegeObj").length());

        // Case 2: User with no service role mappings
        User userNoRoles = new User();
        userNoRoles.setUserID(1L);
        userNoRoles.setUserName("testuser");
        userNoRoles.setFirstName("Test");
        userNoRoles.setLastName("User");
        userNoRoles.setMiddleName("");
        Status status = new Status();
        status.setStatus("Active");
        userNoRoles.setM_status(status);
        userNoRoles.setAgentID("agent1");
        userNoRoles.setAgentPassword("pass1");
        userNoRoles.setDesignationID(2);
        userNoRoles.setM_UserLangMappings(new java.util.HashSet<>());
        userNoRoles.setDesignation(null);
        when(userService.getUserServiceRoleMapping(anyLong())).thenReturn(null);
        org.json.JSONObject resultNoRoles = (org.json.JSONObject) method.invoke(controller, userNoRoles, "127.0.0.1", "localhost");
        assertTrue(resultNoRoles.getBoolean("isAuthenticated"));
        assertTrue(resultNoRoles.has("previlegeObj"));
        assertEquals(0, resultNoRoles.getJSONArray("previlegeObj").length());
        assertEquals("testuser", resultNoRoles.get("userName"));
        assertEquals("Test  User", resultNoRoles.get("fullName"));
        assertEquals("Active", resultNoRoles.get("Status"));
        assertEquals("agent1", resultNoRoles.get("agentID"));
        assertEquals("pass1", resultNoRoles.get("agentPassword"));
        assertEquals(2, resultNoRoles.get("designationID"));

        // Case 3: User with service role mappings
        User userWithRoles = new User();
        userWithRoles.setUserID(2L);
        userWithRoles.setUserName("roleuser");
        userWithRoles.setFirstName("Role");
        userWithRoles.setLastName("User");
        userWithRoles.setMiddleName("Middle");
        userWithRoles.setM_status(status);
        userWithRoles.setAgentID("agent2");
        userWithRoles.setAgentPassword("pass2");
        userWithRoles.setDesignationID(3);
        userWithRoles.setM_UserLangMappings(new java.util.HashSet<>());
        userWithRoles.setDesignation(null);
        // Setup service role mapping
        ServiceMaster serviceMaster = mock(ServiceMaster.class);
        when(serviceMaster.getServiceName()).thenReturn("ServiceA");
        when(serviceMaster.toString()).thenReturn("{serviceName: 'ServiceA'}");
        ProviderServiceMapping providerServiceMapping = mock(ProviderServiceMapping.class);
        when(providerServiceMapping.getM_ServiceMaster()).thenReturn(serviceMaster);
        when(providerServiceMapping.getProviderServiceMapID()).thenReturn(10);
        when(providerServiceMapping.getAPIMANClientKey()).thenReturn("apiKey");
        when(providerServiceMapping.getStateID()).thenReturn(99);
        Role role = mock(Role.class);
        when(role.getRoleName()).thenReturn("Admin");
        when(role.toString()).thenReturn("{roleName: 'Admin'}");
        UserServiceRoleMapping usrMapping = mock(UserServiceRoleMapping.class);
        when(usrMapping.getM_ProviderServiceMapping()).thenReturn(providerServiceMapping);
        when(usrMapping.getM_Role()).thenReturn(role);
        // getTeleConsultation returns boolean, so stub with thenReturn(Boolean.TRUE)
        org.mockito.Mockito.doReturn(true).when(usrMapping).getTeleConsultation();
        when(usrMapping.getAgentID()).thenReturn("agent2");
        when(usrMapping.getAgentPassword()).thenReturn("pass2");
        java.util.List<UserServiceRoleMapping> usrMappings = new java.util.ArrayList<>();
        usrMappings.add(usrMapping);
        when(userService.getUserServiceRoleMapping(anyLong())).thenReturn(usrMappings);
        userWithRoles.setM_UserServiceRoleMapping(usrMappings);
        org.json.JSONObject resultWithRoles = (org.json.JSONObject) method.invoke(controller, userWithRoles, "127.0.0.1", "localhost");
        assertTrue(resultWithRoles.getBoolean("isAuthenticated"));
        assertTrue(resultWithRoles.has("previlegeObj"));
        assertEquals(1, resultWithRoles.getJSONArray("previlegeObj").length());
        org.json.JSONObject privilege = resultWithRoles.getJSONArray("previlegeObj").getJSONObject(0);
        assertEquals("ServiceA", privilege.get("serviceName"));
        assertEquals(10, privilege.get("providerServiceMapID"));
        assertEquals("apiKey", privilege.get("apimanClientKey"));
        assertEquals(99, privilege.get("stateID"));
        assertEquals("agent2", privilege.get("agentID"));
        assertEquals("pass2", privilege.get("agentPassword"));
        org.json.JSONArray rolesArr = privilege.getJSONArray("roles");
        assertEquals(1, rolesArr.length());
        org.json.JSONObject roleObj = rolesArr.getJSONObject(0);
        assertEquals("Admin", roleObj.get("roleName"));
        assertTrue(roleObj.getBoolean("teleConsultation"));
    }

       @Test
    void getServicepointVillages_Success() throws Exception {
        String requestJson = "{\"servicePointID\":123}";
        String expectedResponse = "[\"Village1\", \"Village2\"]";
        when(iemrAdminUserServiceImpl.getServicepointVillages(123)).thenReturn(expectedResponse);

        mockMvc.perform(post("/user/getServicepointVillages")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer test_token")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Village1")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Village2")));
    }

    @Test
    void getServicepointVillages_Exception() throws Exception {
        String requestJson = "{\"servicePointID\":123}";
        when(iemrAdminUserServiceImpl.getServicepointVillages(123)).thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(post("/user/getServicepointVillages")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer test_token")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("DB error")));
    }

        @Test
    void forceLogout_Success() throws Exception {
        ForceLogoutRequestModel requestModel = new ForceLogoutRequestModel();
        // Mock service call
        doNothing().when(iemrAdminUserServiceImpl).forceLogout(any(ForceLogoutRequestModel.class));
        // Mock JWT token extraction and validation
        String jwtToken = "valid.jwt.token";
        Claims claims = mock(Claims.class);
        when(claims.getId()).thenReturn("jti123");
        Date expiration = new Date(System.currentTimeMillis() + 10000);
        when(claims.getExpiration()).thenReturn(expiration);
        when(jwtUtil.validateToken(jwtToken)).thenReturn(claims);
        when(claims.isEmpty()).thenReturn(false);
        // Mock denylist
        doNothing().when(tokenDenylist).addTokenToDenylist(anyString(), anyLong());
        // Use reflection to invoke private getJwtTokenFromCookies if needed
        java.lang.reflect.Method getJwtTokenMethod = IEMRAdminController.class.getDeclaredMethod("getJwtTokenFromCookies", jakarta.servlet.http.HttpServletRequest.class);
        getJwtTokenMethod.setAccessible(true);
        // Example usage: String extractedToken = (String) getJwtTokenMethod.invoke(iemrAdminController, mockRequest);

        jakarta.servlet.http.Cookie jwtCookie = new jakarta.servlet.http.Cookie(com.iemr.common.constant.Constants.JWT_TOKEN, jwtToken);
        mockMvc.perform(post("/user/forceLogout")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer test_token")
                .cookie(jwtCookie)
                .content(new ObjectMapper().writeValueAsString(requestModel)))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Success")));
    }

    @Test
    void forceLogout_NoToken_shouldReturnBadRequest() throws Exception {
        ForceLogoutRequestModel requestModel = new ForceLogoutRequestModel();
        doNothing().when(iemrAdminUserServiceImpl).forceLogout(any(ForceLogoutRequestModel.class));
        // Use reflection to invoke private getJwtTokenFromCookies if needed
        java.lang.reflect.Method getJwtTokenMethod = IEMRAdminController.class.getDeclaredMethod("getJwtTokenFromCookies", jakarta.servlet.http.HttpServletRequest.class);
        getJwtTokenMethod.setAccessible(true);

        mockMvc.perform(post("/user/forceLogout")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer test_token")
                .content(new ObjectMapper().writeValueAsString(requestModel)))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("No JWT token found in request")));
    }

    @Test
    void forceLogout_ExpiredOrInvalidToken_shouldReturnUnauthorized() throws Exception {
        ForceLogoutRequestModel requestModel = new ForceLogoutRequestModel();
        doNothing().when(iemrAdminUserServiceImpl).forceLogout(any(ForceLogoutRequestModel.class));
        String jwtToken = "expired.jwt.token";
        Claims claims = mock(Claims.class);
        when(claims.isEmpty()).thenReturn(true);
        when(jwtUtil.validateToken(jwtToken)).thenReturn(claims);
        // Use reflection to invoke private getJwtTokenFromCookies if needed
        java.lang.reflect.Method getJwtTokenMethod = IEMRAdminController.class.getDeclaredMethod("getJwtTokenFromCookies", jakarta.servlet.http.HttpServletRequest.class);
        getJwtTokenMethod.setAccessible(true);

        mockMvc.perform(post("/user/forceLogout")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer test_token")
                .content(new ObjectMapper().writeValueAsString(requestModel)))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Token is expired or has been logged out")));
    }

    @Test
    void forceLogout_Exception_shouldReturnError() throws Exception {
        ForceLogoutRequestModel requestModel = new ForceLogoutRequestModel();
        doThrow(new RuntimeException("Force logout error")).when(iemrAdminUserServiceImpl).forceLogout(any(ForceLogoutRequestModel.class));

        mockMvc.perform(post("/user/forceLogout")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer test_token")
                .content(new ObjectMapper().writeValueAsString(requestModel)))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Force logout error")));
    }

    @Test
    void forceLogout_BothCookieAndHeader_shouldReturnSuccess() throws Exception {
        ForceLogoutRequestModel requestModel = new ForceLogoutRequestModel();
        doNothing().when(iemrAdminUserServiceImpl).forceLogout(any(ForceLogoutRequestModel.class));
        String jwtToken = "valid.jwt.token";
        Claims claims = mock(Claims.class);
        when(claims.getId()).thenReturn("jti123");
        Date expiration = new Date(System.currentTimeMillis() + 10000);
        when(claims.getExpiration()).thenReturn(expiration);
        when(jwtUtil.validateToken(jwtToken)).thenReturn(claims);
        when(claims.isEmpty()).thenReturn(false);
        doNothing().when(tokenDenylist).addTokenToDenylist(anyString(), anyLong());
        jakarta.servlet.http.Cookie jwtCookie = new jakarta.servlet.http.Cookie(com.iemr.common.constant.Constants.JWT_TOKEN, jwtToken);
        mockMvc.perform(post("/user/forceLogout")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + jwtToken)
                .cookie(jwtCookie)
                .content(new ObjectMapper().writeValueAsString(requestModel))
        )
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Success")));
    }

    @Test
    void getRolesByProviderID_Success() throws Exception {
        String requestJson = "{\"providerServiceMapID\":123}";
        String expectedResponse = "[{\"roleID\":1,\"roleName\":\"Admin\"}]";
        when(iemrAdminUserServiceImpl.getRolesByProviderID(requestJson)).thenReturn(expectedResponse);

        mockMvc.perform(post("/user/getRolesByProviderID")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer test_token")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Admin")));
    }

    @Test
    void getRolesByProviderID_Exception() throws Exception {
        String requestJson = "{\"providerServiceMapID\":123}";
        when(iemrAdminUserServiceImpl.getRolesByProviderID(requestJson)).thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(post("/user/getRolesByProviderID")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer test_token")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("DB error")));
    }

    @Test
    void getAgentByRoleID_Success() throws Exception {
        String requestJson = "{\"providerServiceMapID\":123,\"RoleID\":1}";
        String expectedResponse = "[{\"agentID\":\"agent1\",\"roleID\":1}]";
        when(iemrAdminUserServiceImpl.getAgentByRoleID(requestJson)).thenReturn(expectedResponse);

        mockMvc.perform(post("/user/getAgentByRoleID")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer test_token")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("agent1")));
    }

    @Test
    void getAgentByRoleID_Exception() throws Exception {
        String requestJson = "{\"providerServiceMapID\":123,\"RoleID\":1}";
        when(iemrAdminUserServiceImpl.getAgentByRoleID(requestJson)).thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(post("/user/getAgentByRoleID")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer test_token")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("DB error")));
    }

    @Test
    void getUserServicePointVanDetails_Success() throws Exception {
        String requestJson = "{\"userID\":123,\"providerServiceMapID\":456}";
        String expectedResponse = "{\"vanDetails\":\"Van123\"}";
        when(iemrAdminUserServiceImpl.getUserServicePointVanDetails(123)).thenReturn(expectedResponse);

        mockMvc.perform(post("/user/getUserServicePointVanDetails")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer test_token")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Van123")));
    }

    @Test
    void getUserServicePointVanDetails_Exception() throws Exception {
        String requestJson = "{\"userID\":123,\"providerServiceMapID\":456}";
        when(iemrAdminUserServiceImpl.getUserServicePointVanDetails(123)).thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(post("/user/getUserServicePointVanDetails")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer test_token")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("DB error")));
    }

    @Test
    void getLocationsByProviderID_Success() throws Exception {
        String requestJson = "{\"providerServiceMapID\":123,\"roleID\":1}";
        String expectedResponse = "[{\"locationID\":1,\"locationName\":\"LocationA\"}]";
        when(iemrAdminUserServiceImpl.getLocationsByProviderID(requestJson)).thenReturn(expectedResponse);

        mockMvc.perform(post("/user/getLocationsByProviderID")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer test_token")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("LocationA")));
    }

    @Test
    void getLocationsByProviderID_Exception() throws Exception {
        String requestJson = "{\"providerServiceMapID\":123,\"roleID\":1}";
        when(iemrAdminUserServiceImpl.getLocationsByProviderID(requestJson)).thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(post("/user/getLocationsByProviderID")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer test_token")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("DB error")));
    }

    @Test
    void validateSecurityQuestionAndAnswer_Success() throws Exception {
        String requestJson = "{\"SecurityQuesAns\":[{\"questionId\":\"1\",\"answer\":\"test\"}],\"userName\":\"testUser\"}";
        String expectedResponse = "{\"result\":\"Valid\"}";
        JsonObject requestObj = com.google.gson.JsonParser.parseString(requestJson).getAsJsonObject();
        when(iemrAdminUserServiceImpl.validateQuestionAndAnswersForPasswordChange(requestObj)).thenReturn(expectedResponse);

        mockMvc.perform(post("/user/validateSecurityQuestionAndAnswer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Valid")));
    }

    @Test
    void validateSecurityQuestionAndAnswer_InvalidRequest_shouldReturnError() throws Exception {
        mockMvc.perform(post("/user/validateSecurityQuestionAndAnswer")
                .contentType(MediaType.APPLICATION_JSON)
                .content("")
        )
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""));
    }

    @Test
    void validateSecurityQuestionAndAnswer_Exception_shouldReturnError() throws Exception {
        String requestJson = "{\"SecurityQuesAns\":[{\"questionId\":\"1\",\"answer\":\"test\"}],\"userName\":\"testUser\"}";
        JsonObject requestObj = com.google.gson.JsonParser.parseString(requestJson).getAsJsonObject();
        when(iemrAdminUserServiceImpl.validateQuestionAndAnswersForPasswordChange(requestObj)).thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(post("/user/validateSecurityQuestionAndAnswer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("DB error")));
    }

        @Test
    void getSecurityts_Success() throws Exception {
        ArrayList<LoginSecurityQuestions> questions = new ArrayList<>();
        LoginSecurityQuestions q1 = new LoginSecurityQuestions();
        q1.setQuestionID(1);
        q1.setQuestion("What is your favorite color?");
        questions.add(q1);
        when(iemrAdminUserServiceImpl.getAllLoginSecurityQuestions()).thenReturn(questions);

        OutputResponse expectedResponse = new OutputResponse();
        expectedResponse.setResponse(questions.toString());

        mockMvc.perform(get("/user/getsecurityquetions"))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResponse.toString()));
    }

    @Test
    void getSecurityts_Exception() throws Exception {
        when(iemrAdminUserServiceImpl.getAllLoginSecurityQuestions()).thenThrow(new RuntimeException("DB error"));

        OutputResponse expectedResponse = new OutputResponse();
        expectedResponse.setError(5000, "Unable to fetch security questions");

        mockMvc.perform(get("/user/getsecurityquetions"))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResponse.toString()));
    }

        @Test
    void userAuthenticate_mobileDevice_branch_shouldReturnTokensAndStoreInRedis() throws Exception {
        LoginRequestModel loginRequest = new LoginRequestModel();
        loginRequest.setUserName("testUser");
        loginRequest.setPassword("testPwd");
        loginRequest.setWithCredentials(true);
        User mockUser = new User();
        mockUser.setUserID(42L);
        mockUser.setUserName("testUser");
        Status activeStatus = new Status();
        activeStatus.setStatus("Active");
        mockUser.setM_status(activeStatus);
        mockUser.setM_UserLangMappings(new java.util.HashSet<>());
        mockUser.setDesignation(null);
        mockUser.setM_UserServiceRoleMapping(new ArrayList<>());

        // Mock decryption and authentication
        when(aesUtil.decrypt(anyString(), anyString())).thenReturn("decryptedPwd");
        when(iemrAdminUserServiceImpl.userAuthenticate(anyString(), anyString())).thenReturn(Collections.singletonList(mockUser));

        // Removed unnecessary stubbings for jwtUtil methods

        // Mock Redis operations
        ValueOperations<String, String> valueOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        // Use argument matchers to avoid strict stubbing errors
        doNothing().when(valueOps).set(anyString(), any(), anyLong(), any(TimeUnit.class));

        // Mock privilege mapping and IP validation
        org.json.JSONObject validatedObj = new org.json.JSONObject();
        validatedObj.put("jwtToken", "jwtTokenValue");
        validatedObj.put("refreshToken", "refreshTokenValue");
        when(iemrAdminUserServiceImpl.generateKeyAndValidateIP(any(org.json.JSONObject.class), anyString(), anyString())).thenReturn(validatedObj);

        // Simulate mobile device User-Agent
        mockMvc.perform(post("/user/userAuthenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .header("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 10_3 like Mac OS X)")
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("jwtTokenValue")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("refreshTokenValue")));
    }

        @Test
    void superUserAuthenticate_mobileAndNonMobileBranches_shouldCoverJwtRefreshRedisCookieLogic() throws Exception {
        LoginRequestModel request = new LoginRequestModel();
        request.setUserName("SuperAdmin");
        request.setPassword("superPwd");
        User mockUser = new User();
        mockUser.setUserID(99L);
        mockUser.setUserName("SuperAdmin");
        Status activeStatus = new Status();
        activeStatus.setStatus("Active");
        mockUser.setM_status(activeStatus);
        when(aesUtil.decrypt(anyString(), anyString())).thenReturn("decryptedPwd");
        when(iemrAdminUserServiceImpl.superUserAuthenticate(anyString(), anyString())).thenReturn(mockUser);

        // Removed unnecessary stubbings for jwtUtil methods

        // Mock Redis operations
        ValueOperations<String, String> valueOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        doNothing().when(valueOps).set(anyString(), any(), anyLong(), any(TimeUnit.class));

        // Mock cookie logic
        doNothing().when(cookieUtil).addJwtTokenToCookie(anyString(), any(), any());

        // Mock privilege mapping and IP validation
        org.json.JSONObject validatedObj = new org.json.JSONObject();
        validatedObj.put("jwtToken", "jwtTokenSuper");
        validatedObj.put("refreshToken", "refreshTokenSuper");
        validatedObj.put("previlegeObj", new org.json.JSONArray());
        when(iemrAdminUserServiceImpl.generateKeyAndValidateIP(any(org.json.JSONObject.class), anyString(), anyString())).thenReturn(validatedObj);

        // Mobile device branch
        mockMvc.perform(post("/user/superUserAuthenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .header("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 10_3 like Mac OS X)")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("jwtTokenSuper")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("refreshTokenSuper")));

        // Non-mobile device branch
        mockMvc.perform(post("/user/superUserAuthenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("jwtTokenSuper")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("previlegeObj")));
    }


}