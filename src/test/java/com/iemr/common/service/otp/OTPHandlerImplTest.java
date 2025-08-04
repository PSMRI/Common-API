package com.iemr.common.service.otp;

import com.google.common.cache.LoadingCache;
import com.iemr.common.data.otp.OTPRequestParsor;
import com.iemr.common.service.users.IEMRAdminUserServiceImpl;
import com.iemr.common.utils.config.ConfigProperties;
import com.iemr.common.utils.http.HttpUtils;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OTPHandlerImplTest {
    @InjectMocks
    OTPHandlerImpl otpHandler;

    @Mock
    HttpUtils httpUtils;
    @Mock
    IEMRAdminUserServiceImpl iEMRAdminUserServiceImpl;
    @Mock
    OTPRequestParsor otpRequestParsor;

    @BeforeEach
    public void setup() {
        ReflectionTestUtils.setField(otpHandler, "httpUtils", httpUtils);
        ReflectionTestUtils.setField(otpHandler, "iEMRAdminUserServiceImpl", iEMRAdminUserServiceImpl);
    }

    @Test
    public void testSendOTP_success() throws Exception {
        OTPRequestParsor req = mock(OTPRequestParsor.class);
        when(req.getMobNo()).thenReturn("1234567890");
        // Mock static ConfigProperties
        try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
            configMock.when(() -> ConfigProperties.getPropertyByName("send-message-url")).thenReturn("send-url");
            configMock.when(() -> ConfigProperties.getPropertyByName("sms-gateway-url")).thenReturn("gateway");
            configMock.when(() -> ConfigProperties.getPropertyByName("sms-username")).thenReturn("user");
            configMock.when(() -> ConfigProperties.getPropertyByName("sms-password")).thenReturn("pass");
            configMock.when(() -> ConfigProperties.getPropertyByName("sms-sender-number")).thenReturn("sender");
            when(httpUtils.getV1(anyString())).thenReturn(new ResponseEntity<>("OK", HttpStatus.OK));
            String result = otpHandler.sendOTP(req);
            assertEquals("success", result);
        }
    }

    @Test
    public void testResendOTP_success() throws Exception {
        OTPRequestParsor req = mock(OTPRequestParsor.class);
        when(req.getMobNo()).thenReturn("1234567890");
        try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
            configMock.when(() -> ConfigProperties.getPropertyByName("send-message-url")).thenReturn("send-url");
            configMock.when(() -> ConfigProperties.getPropertyByName("sms-gateway-url")).thenReturn("gateway");
            configMock.when(() -> ConfigProperties.getPropertyByName("sms-username")).thenReturn("user");
            configMock.when(() -> ConfigProperties.getPropertyByName("sms-password")).thenReturn("pass");
            configMock.when(() -> ConfigProperties.getPropertyByName("sms-sender-number")).thenReturn("sender");
            when(httpUtils.getV1(anyString())).thenReturn(new ResponseEntity<>("OK", HttpStatus.OK));
            String result = otpHandler.resendOTP(req);
            assertEquals("success", result);
        }
    }

    @Test
    public void testValidateOTP_success() throws Exception {
        OTPRequestParsor req = mock(OTPRequestParsor.class);
        when(req.getMobNo()).thenReturn("1234567890");
        int otp = otpHandler.generateOTP("1234567890");
        when(req.getOtp()).thenReturn(otp);
        String encrypted = ReflectionTestUtils.invokeMethod(otpHandler, "getEncryptedOTP", otp);
        // Set the cache to the correct value
        @SuppressWarnings("unchecked")
        LoadingCache<String, String> cache = (LoadingCache<String, String>) ReflectionTestUtils.getField(otpHandler, "otpCache");
        cache.put("1234567890", encrypted);
        // Mock admin user service
        JSONObject keyObj = new JSONObject();
        keyObj.put("key", "val");
        when(iEMRAdminUserServiceImpl.generateKeyPostOTPValidation(any(JSONObject.class))).thenReturn(keyObj);
        JSONObject result = otpHandler.validateOTP(req);
        assertEquals("val", result.get("key"));
    }

    @Test
    public void testValidateOTP_failure() throws Exception {
        OTPRequestParsor req = mock(OTPRequestParsor.class);
        when(req.getMobNo()).thenReturn("1234567890");
        when(req.getOtp()).thenReturn(999999); // Intentionally wrong OTP
        // Generate and cache OTP
        otpHandler.generateOTP("1234567890");
        // Don't set the cache to the correct value
        assertThrows(Exception.class, () -> otpHandler.validateOTP(req));
    }

    @Test
    public void testGenerateOTP_and_getEncryptedOTP() throws Exception {
        int otp = otpHandler.generateOTP("key");
        assertTrue(otp >= 100000 && otp <= 999999);
        String encrypted = ReflectionTestUtils.invokeMethod(otpHandler, "getEncryptedOTP", otp);
        assertNotNull(encrypted);
        assertEquals(64, encrypted.length());
    }

    @Test
    public void testSendSMS_success() throws Exception {
        try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
            configMock.when(() -> ConfigProperties.getPropertyByName("send-message-url")).thenReturn("send-url");
            configMock.when(() -> ConfigProperties.getPropertyByName("sms-gateway-url")).thenReturn("gateway");
            configMock.when(() -> ConfigProperties.getPropertyByName("sms-username")).thenReturn("user");
            configMock.when(() -> ConfigProperties.getPropertyByName("sms-password")).thenReturn("pass");
            configMock.when(() -> ConfigProperties.getPropertyByName("sms-sender-number")).thenReturn("sender");
            when(httpUtils.getV1(anyString())).thenReturn(new ResponseEntity<>("OK", HttpStatus.OK));
            // Use reflection to call private method
            ReflectionTestUtils.invokeMethod(otpHandler, "sendSMS", 123456, "1234567890", "OTP is ");
        }
    }

    @Test
    public void testSendSMS_failure_status() throws Exception {
        try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
            configMock.when(() -> ConfigProperties.getPropertyByName("send-message-url")).thenReturn("send-url");
            configMock.when(() -> ConfigProperties.getPropertyByName("sms-gateway-url")).thenReturn("gateway");
            configMock.when(() -> ConfigProperties.getPropertyByName("sms-username")).thenReturn("user");
            configMock.when(() -> ConfigProperties.getPropertyByName("sms-password")).thenReturn("pass");
            configMock.when(() -> ConfigProperties.getPropertyByName("sms-sender-number")).thenReturn("sender");
            when(httpUtils.getV1(anyString())).thenReturn(new ResponseEntity<>("fail", HttpStatus.BAD_REQUEST));
            Exception ex = assertThrows(Exception.class, () ->
                ReflectionTestUtils.invokeMethod(otpHandler, "sendSMS", 123456, "1234567890", "OTP is "));
            assertNull(ex.getMessage());
        }
    }

    @Test
    public void testSendSMS_failure_gateway_error() throws Exception {
        try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
            configMock.when(() -> ConfigProperties.getPropertyByName("send-message-url")).thenReturn("send-url");
            configMock.when(() -> ConfigProperties.getPropertyByName("sms-gateway-url")).thenReturn("gateway");
            configMock.when(() -> ConfigProperties.getPropertyByName("sms-username")).thenReturn("user");
            configMock.when(() -> ConfigProperties.getPropertyByName("sms-password")).thenReturn("pass");
            configMock.when(() -> ConfigProperties.getPropertyByName("sms-sender-number")).thenReturn("sender");
            when(httpUtils.getV1(anyString())).thenReturn(new ResponseEntity<>("0x200 - Invalid Username or Password", HttpStatus.OK));
            Exception ex = assertThrows(Exception.class, () ->
                ReflectionTestUtils.invokeMethod(otpHandler, "sendSMS", 123456, "1234567890", "OTP is "));
            assertNull(ex.getMessage());
        }
    }
}
