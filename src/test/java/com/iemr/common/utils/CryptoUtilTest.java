package com.iemr.common.utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class CryptoUtilTest {
    private CryptoUtil cryptoUtil;
    @BeforeEach
    void setUp() {
        cryptoUtil = new CryptoUtil();
    }
    @Test
    void testEncryptAndDecrypt_SimpleString() throws Exception {
        String originalValue = "Hello, World!";
        String encryptedValue = cryptoUtil.encrypt(originalValue);
        assertNotNull(encryptedValue);
        String decryptedValue = cryptoUtil.decrypt(encryptedValue);
        assertEquals(originalValue, decryptedValue);
    }
    @Test
    void testEncryptAndDecrypt_EmptyString() throws Exception {
        String originalValue = "";
        String encryptedValue = cryptoUtil.encrypt(originalValue);
        assertNotNull(encryptedValue);
        String decryptedValue = cryptoUtil.decrypt(encryptedValue);
        assertEquals(originalValue, decryptedValue);
    }
    @Test
    void testEncryptAndDecrypt_StringRequiringPadding() throws Exception {
        // Test with a string that needs 1 byte padding (length 15)
        String originalValue1 = "Short string 15";
        String encryptedValue1 = cryptoUtil.encrypt(originalValue1);
        assertNotNull(encryptedValue1);
        String decryptedValue1 = cryptoUtil.decrypt(encryptedValue1);
        assertEquals(originalValue1, decryptedValue1);
        // Test with a string that needs 15 bytes padding (length 1)
        String originalValue2 = "A";
        String encryptedValue2 = cryptoUtil.encrypt(originalValue2);
        assertNotNull(encryptedValue2);
        String decryptedValue2 = cryptoUtil.decrypt(encryptedValue2);
        assertEquals(originalValue2, decryptedValue2);
        // Test with a string that is exactly 16 characters (should get a full block of padding)
        String originalValue3 = "Exactly16chars!!";
        String encryptedValue3 = cryptoUtil.encrypt(originalValue3);
        assertNotNull(encryptedValue3);
        String decryptedValue3 = cryptoUtil.decrypt(encryptedValue3);
        assertEquals(originalValue3, decryptedValue3);
        // Test with a string that is 17 characters (needs padding for the next block)
        String originalValue4 = "This is 17 chars.";
        String encryptedValue4 = cryptoUtil.encrypt(originalValue4);
        assertNotNull(encryptedValue4);
        String decryptedValue4 = cryptoUtil.decrypt(encryptedValue4);
        assertEquals(originalValue4, decryptedValue4);
    }
    @Test
    void testDecrypt_InvalidBase64InputReturnsNull() {
        String invalidEncryptedValue = "ThisIsNotValidBase64!";
        String decryptedValue = cryptoUtil.decrypt(invalidEncryptedValue);
        assertNull(decryptedValue);
    }
    @Test
    void testDecrypt_ValidBase64ButInvalidCipherDataReturnsNull() {
        // This is a valid Base64 string, but the underlying bytes are not valid AES encrypted data
        String invalidCipherData = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=";
        String decryptedValue = cryptoUtil.decrypt(invalidCipherData);
        assertNull(decryptedValue);
    }
}