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
package com.iemr.common.utils.validator;

import com.iemr.common.utils.config.ConfigProperties;
import com.iemr.common.utils.exception.IEMRException;
import com.iemr.common.utils.redis.RedisSessionException;
import com.iemr.common.utils.sessionobject.SessionObject;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ValidatorTest {

    @Mock
    private SessionObject session;

    @InjectMocks
    private Validator validator;

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        // Reset the static field 'enableIPValidation' in Validator before each test.
        // This is crucial because the Validator constructor modifies a static field,
        // which can cause test interference if not reset.
        java.lang.reflect.Field field = Validator.class.getDeclaredField("enableIPValidation");
        field.setAccessible(true);
        field.set(null, false); // Set it back to its default/initial state
    }

    @Test
    void testSetSessionObject() {
        // This method is primarily for dependency injection, which @InjectMocks handles.
        // We can verify that the session object is indeed set by ensuring the validator
        // instance is not null, implying successful injection.
        assertNotNull(validator);
        // Further verification would require reflection to access the private 'session' field,
        // which is generally considered overkill for simple setters handled by Mockito.
    }

    @Test
    void testValidatorConstructor_EnableIPValidationTrue() {
        try (MockedStatic<ConfigProperties> mockedConfigProperties = Mockito.mockStatic(ConfigProperties.class)) {
            mockedConfigProperties.when(() -> ConfigProperties.getBoolean("enableIPValidation")).thenReturn(true);

            // Re-instantiate validator to trigger constructor with mocked static method
            validator = new Validator();
            validator.setSessionObject(session); // Manually inject mock session after re-instantiation

            // Verify that ConfigProperties.getBoolean was called exactly once
            mockedConfigProperties.verify(() -> ConfigProperties.getBoolean("enableIPValidation"), times(1));

            // Verify the static field 'enableIPValidation' in Validator is true using reflection
            try {
                java.lang.reflect.Field field = Validator.class.getDeclaredField("enableIPValidation");
                field.setAccessible(true);
                assertTrue((Boolean) field.get(null), "enableIPValidation static field should be true");
            } catch (NoSuchFieldException | IllegalAccessException e) {
                fail("Could not access static field 'enableIPValidation' for verification", e);
            }
        }
    }

    @Test
    void testValidatorConstructor_EnableIPValidationFalse() {
        try (MockedStatic<ConfigProperties> mockedConfigProperties = Mockito.mockStatic(ConfigProperties.class)) {
            mockedConfigProperties.when(() -> ConfigProperties.getBoolean("enableIPValidation")).thenReturn(false);

            // Re-instantiate validator to trigger constructor with mocked static method
            validator = new Validator();
            validator.setSessionObject(session); // Manually inject mock session after re-instantiation

            // Verify that ConfigProperties.getBoolean was called exactly once
            mockedConfigProperties.verify(() -> ConfigProperties.getBoolean("enableIPValidation"), times(1));

            // Verify the static field 'enableIPValidation' in Validator is false using reflection
            try {
                java.lang.reflect.Field field = Validator.class.getDeclaredField("enableIPValidation");
                field.setAccessible(true);
                assertFalse((Boolean) field.get(null), "enableIPValidation static field should be false");
            } catch (NoSuchFieldException | IllegalAccessException e) {
                fail("Could not access static field 'enableIPValidation' for verification", e);
            }
        }
    }

    @Test
    void testUpdateCacheObj_NoIPValidation_Success() throws RedisSessionException, JSONException {
        String key = "testKey";
        String ipKey = "someIpKey"; // This parameter is not used in the method's logic
        JSONObject responseObj = new JSONObject();
        responseObj.put("loginIPAddress", "192.168.1.1");
        responseObj.put("someOtherData", "value");

        // Mock session.getSessionObject to return null (no existing session)
        when(session.getSessionObject(key)).thenReturn(null);
        // Mock session.setSessionObject to return "OK" on success
        when(session.setSessionObject(eq(key), anyString())).thenReturn("OK");

        JSONObject result = validator.updateCacheObj(responseObj, key, ipKey);

        // Verify interactions with the mocked session object
        verify(session, times(1)).getSessionObject(key);
        verify(session, times(1)).setSessionObject(eq(key), anyString());

        // Assertions on the returned JSONObject
        assertNotNull(result);
        assertEquals(key, result.getString("key"));
        assertEquals("login success", result.getString("sessionStatus"));
        assertEquals("192.168.1.1", result.getString("loginIPAddress")); // Original data should be preserved
        assertEquals("value", result.getString("someOtherData"));
    }

    @Test
    void testUpdateCacheObj_IPValidationEnabled_SameIP_Success() throws RedisSessionException, JSONException {
        try (MockedStatic<ConfigProperties> mockedConfigProperties = Mockito.mockStatic(ConfigProperties.class)) {
            mockedConfigProperties.when(() -> ConfigProperties.getBoolean("enableIPValidation")).thenReturn(true);

            // Re-instantiate validator to trigger constructor with mocked static method
            validator = new Validator();
            validator.setSessionObject(session); // Manually inject mock session after re-instantiation

            String key = "testKey";
            String ipKey = "someIpKey";
            JSONObject responseObj = new JSONObject();
            responseObj.put("loginIPAddress", "192.168.1.1");
            responseObj.put("someOtherData", "value");

            JSONObject existingSessionObj = new JSONObject();
            existingSessionObj.put("loginIPAddress", "192.168.1.1"); // Existing session has same IP
            when(session.getSessionObject(key)).thenReturn(existingSessionObj.toString());
            when(session.setSessionObject(eq(key), anyString())).thenReturn("OK");

            JSONObject result = validator.updateCacheObj(responseObj, key, ipKey);

            verify(session, times(1)).getSessionObject(key);
            verify(session, times(1)).setSessionObject(eq(key), anyString());

            assertNotNull(result);
            assertEquals(key, result.getString("key"));
            assertEquals("login success", result.getString("sessionStatus"));
            assertEquals("192.168.1.1", result.getString("loginIPAddress"));
            assertEquals("value", result.getString("someOtherData"));
        }
    }

    @Test
    void testUpdateCacheObj_IPValidationEnabled_DifferentIP_ReturnsEmptyResponseObj() throws RedisSessionException, JSONException {
        try (MockedStatic<ConfigProperties> mockedConfigProperties = Mockito.mockStatic(ConfigProperties.class)) {
            mockedConfigProperties.when(() -> ConfigProperties.getBoolean("enableIPValidation")).thenReturn(true);

            // Re-instantiate validator to trigger constructor with mocked static method
            validator = new Validator();
            validator.setSessionObject(session); // Manually inject mock session after re-instantiation

            String key = "testKey";
            String ipKey = "someIpKey";
            JSONObject responseObj = new JSONObject();
            responseObj.put("loginIPAddress", "192.168.1.2"); // Request IP
            responseObj.put("someOtherData", "value");

            JSONObject existingSessionObj = new JSONObject();
            existingSessionObj.put("loginIPAddress", "192.168.1.1"); // Logged in IP (different)
            when(session.getSessionObject(key)).thenReturn(existingSessionObj.toString());

            JSONObject result = validator.updateCacheObj(responseObj, key, ipKey);

            verify(session, times(1)).getSessionObject(key);
            verify(session, never()).setSessionObject(eq(key), anyString()); // Should not set session if IPs differ

            assertNotNull(result);
            assertEquals(key, result.getString("key"));
            assertEquals("login success, but user logged in from 192.168.1.1", result.getString("sessionStatus"));
            // The original responseObj should be cleared and only key and sessionStatus added
            assertFalse(result.has("loginIPAddress")); // Original data should be removed
            assertFalse(result.has("someOtherData"));
            // TODO: Verify logger.error("Logged in IP : ... Request IP : ...") was called
        }
    }

    @Test
    void testUpdateCacheObj_RedisSessionExceptionOnGetSession() throws RedisSessionException, JSONException {
        String key = "testKey";
        String ipKey = "someIpKey";
        JSONObject responseObj = new JSONObject();
        responseObj.put("loginIPAddress", "192.168.1.1");

        // Mock getSessionObject to throw an exception
        when(session.getSessionObject(key)).thenThrow(new RedisSessionException("Redis get error"));
        // Mock setSessionObject to succeed, as it's called after the catch block for getSessionObject
        when(session.setSessionObject(eq(key), anyString())).thenReturn("OK");

        JSONObject result = validator.updateCacheObj(responseObj, key, ipKey);

        verify(session, times(1)).getSessionObject(key);
        verify(session, times(1)).setSessionObject(eq(key), anyString()); // setSessionObject is still called

        assertNotNull(result);
        assertEquals(key, result.getString("key"));
        // The status will still be "login success" because the exception from getSessionObject is caught,
        // and then the code proceeds to set the session and status.
        assertEquals("login success", result.getString("sessionStatus"));
       assertEquals("192.168.1.1", result.getString("loginIPAddress"), "Result should contain 'loginIPAddress' when get session fails but set still happens");


        // TODO: Verify logger.error("Session validation failed with exception", e) was called
    }

@Test
void testUpdateCacheObj_RedisSessionExceptionOnSetSession() throws RedisSessionException, JSONException {
    String key = "testKey";
    String ipKey = "someIpKey";
    JSONObject responseObj = new JSONObject();
    responseObj.put("loginIPAddress", "192.168.1.1");

    when(session.getSessionObject(key)).thenReturn(null); // No existing session
    when(session.setSessionObject(eq(key), anyString())).thenThrow(new RedisSessionException("Redis set error"));

    JSONObject result = validator.updateCacheObj(responseObj, key, ipKey);

    verify(session, times(1)).getSessionObject(key);
    verify(session, times(1)).setSessionObject(eq(key), anyString());

    assertNotNull(result);
    assertFalse(result.has("key"), "Result should NOT contain 'key' when Redis set fails");
    assertTrue(result.has("sessionStatus"), "Result should contain 'sessionStatus' when Redis set fails");
    assertEquals("session creation failed", result.getString("sessionStatus"));
    assertTrue(result.has("loginIPAddress"), "Result should still contain original 'loginIPAddress'");
    assertEquals("192.168.1.1", result.getString("loginIPAddress"));
}

    @Test
    void testUpdateCacheObj_JSONExceptionOnSessionData() throws RedisSessionException, JSONException {
        String key = "testKey";
        String ipKey = "someIpKey";
        JSONObject responseObj = new JSONObject();
        responseObj.put("loginIPAddress", "192.168.1.1");

        // Mock session.getSessionObject to return invalid JSON string
        when(session.getSessionObject(key)).thenReturn("this is not valid json");
        when(session.setSessionObject(eq(key), anyString())).thenReturn("OK");

        JSONObject result = validator.updateCacheObj(responseObj, key, ipKey);

        verify(session, times(1)).getSessionObject(key);
        verify(session, times(1)).setSessionObject(eq(key), anyString());

        assertNotNull(result);
        assertEquals(key, result.getString("key"));
        assertEquals("login success", result.getString("sessionStatus"));
        assertEquals("192.168.1.1", result.getString("loginIPAddress"));
        // TODO: Verify logger.error("Session validation failed with exception", e) was called (due to JSONException)
    }

    @Test
    void testGetSessionObject_Success() throws RedisSessionException {
        String key = "testKey";
        String expectedSessionData = "{\"data\":\"someValue\"}";
        when(session.getSessionObject(key)).thenReturn(expectedSessionData);

        String result = validator.getSessionObject(key);

        verify(session, times(1)).getSessionObject(key);
        assertEquals(expectedSessionData, result);
    }

    @Test
    void testGetSessionObject_RedisSessionException() throws RedisSessionException {
        String key = "testKey";
        when(session.getSessionObject(key)).thenThrow(new RedisSessionException("Redis error during get"));

        // Expect the RedisSessionException to be re-thrown
        assertThrows(RedisSessionException.class, () -> validator.getSessionObject(key));

        verify(session, times(1)).getSessionObject(key);
    }

    @Test
    void testCheckKeyExists_NoIPValidation_Success() throws RedisSessionException, IEMRException, JSONException {
        String loginKey = "testLoginKey";
        String ipAddress = "192.168.1.1";
        JSONObject sessionObj = new JSONObject();
        sessionObj.put("loginIPAddress", "192.168.1.1"); // IP address is present but not checked
        when(session.getSessionObject(loginKey)).thenReturn(sessionObj.toString());

        // No exception should be thrown
        assertDoesNotThrow(() -> validator.checkKeyExists(loginKey, ipAddress));

        verify(session, times(1)).getSessionObject(loginKey);
    }

    @Test
    void testCheckKeyExists_IPValidationEnabled_SameIP_Success() throws RedisSessionException, IEMRException, JSONException {
        try (MockedStatic<ConfigProperties> mockedConfigProperties = Mockito.mockStatic(ConfigProperties.class)) {
            mockedConfigProperties.when(() -> ConfigProperties.getBoolean("enableIPValidation")).thenReturn(true);

            // Re-instantiate validator to trigger constructor with mocked static method
            validator = new Validator();
            validator.setSessionObject(session); // Manually inject mock session after re-instantiation

            String loginKey = "testLoginKey";
            String ipAddress = "192.168.1.1";
            JSONObject sessionObj = new JSONObject();
            sessionObj.put("loginIPAddress", "192.168.1.1"); // Logged in IP is same as request IP
            when(session.getSessionObject(loginKey)).thenReturn(sessionObj.toString());

            // No exception should be thrown
            assertDoesNotThrow(() -> validator.checkKeyExists(loginKey, ipAddress));

            verify(session, times(1)).getSessionObject(loginKey);
        }
    }

    @Test
    void testCheckKeyExists_IPValidationEnabled_DifferentIP_ThrowsIEMRException() throws RedisSessionException, JSONException {
        try (MockedStatic<ConfigProperties> mockedConfigProperties = Mockito.mockStatic(ConfigProperties.class)) {
            mockedConfigProperties.when(() -> ConfigProperties.getBoolean("enableIPValidation")).thenReturn(true);

            // Re-instantiate validator to trigger constructor with mocked static method
            validator = new Validator();
            validator.setSessionObject(session); // Manually inject mock session after re-instantiation

            String loginKey = "testLoginKey";
            String ipAddress = "192.168.1.2"; // Request IP
            JSONObject sessionObj = new JSONObject();
            sessionObj.put("loginIPAddress", "192.168.1.1"); // Logged in IP (different)
            when(session.getSessionObject(loginKey)).thenReturn(sessionObj.toString());

            // Expect IEMRException due to IP mismatch
            IEMRException thrown = assertThrows(IEMRException.class, () -> validator.checkKeyExists(loginKey, ipAddress));
            assertEquals("Session is expired. Please login again.", thrown.getMessage());

            verify(session, times(1)).getSessionObject(loginKey);
            // TODO: Verify logger.error("Logged in IP : ... Request IP : ...") was called
        }
    }

    @Test
    void testCheckKeyExists_RedisSessionException_ThrowsIEMRException() throws RedisSessionException {
        String loginKey = "testLoginKey";
        String ipAddress = "192.168.1.1";
        // Mock getSessionObject to throw RedisSessionException
        when(session.getSessionObject(loginKey)).thenThrow(new RedisSessionException("Redis error"));

        // Expect IEMRException as the catch block converts any exception to IEMRException
        IEMRException thrown = assertThrows(IEMRException.class, () -> validator.checkKeyExists(loginKey, ipAddress));
        assertEquals("Session is expired. Please login again.", thrown.getMessage());

        verify(session, times(1)).getSessionObject(loginKey);
    }

    @Test
    void testCheckKeyExists_JSONExceptionOnSessionData_ThrowsIEMRException() throws RedisSessionException {
        String loginKey = "testLoginKey";
        String ipAddress = "192.168.1.1";
        // Mock getSessionObject to return an invalid JSON string
        when(session.getSessionObject(loginKey)).thenReturn("invalid json string");

        // Expect IEMRException as the JSONException is caught and re-thrown as IEMRException
        IEMRException thrown = assertThrows(IEMRException.class, () -> validator.checkKeyExists(loginKey, ipAddress));
        assertEquals("Session is expired. Please login again.", thrown.getMessage());

        verify(session, times(1)).getSessionObject(loginKey);
    }

    @Test
    void testCheckKeyExists_SessionObjectIsNull_ThrowsIEMRException() throws RedisSessionException {
        String loginKey = "testLoginKey";
        String ipAddress = "192.168.1.1";
        // Mock getSessionObject to return null
        when(session.getSessionObject(loginKey)).thenReturn(null);

        // Expect IEMRException because new JSONObject(null) will throw NullPointerException,
        // which is caught and re-thrown as IEMRException.
        IEMRException thrown = assertThrows(IEMRException.class, () -> validator.checkKeyExists(loginKey, ipAddress));
        assertEquals("Session is expired. Please login again.", thrown.getMessage());

        verify(session, times(1)).getSessionObject(loginKey);
    }

    @Test
    void testCheckKeyExists_SessionObjectMissingIP_ThrowsIEMRException() throws RedisSessionException, JSONException {
        try (MockedStatic<ConfigProperties> mockedConfigProperties = Mockito.mockStatic(ConfigProperties.class)) {
            mockedConfigProperties.when(() -> ConfigProperties.getBoolean("enableIPValidation")).thenReturn(true);

            // Re-instantiate validator to trigger constructor with mocked static method
            validator = new Validator();
            validator.setSessionObject(session); // Manually inject mock session after re-instantiation

            String loginKey = "testLoginKey";
            String ipAddress = "192.168.1.1";
            JSONObject sessionObj = new JSONObject();
            // Missing "loginIPAddress" key, which will cause JSONException when .getString("loginIPAddress") is called
            when(session.getSessionObject(loginKey)).thenReturn(sessionObj.toString());

            // Expect IEMRException as the JSONException is caught and re-thrown as IEMRException
            IEMRException thrown = assertThrows(IEMRException.class, () -> validator.checkKeyExists(loginKey, ipAddress));
            assertEquals("Session is expired. Please login again.", thrown.getMessage());

            verify(session, times(1)).getSessionObject(loginKey);
        }
    }
}