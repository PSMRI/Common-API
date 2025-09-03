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
package com.iemr.common.config.encryption;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import org.junit.jupiter.api.Test;
import java.security.spec.KeySpec;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import static org.junit.jupiter.api.Assertions.*;

class SecurePasswordTest {

    private final SecurePassword securePassword = new SecurePassword();

    @Test
    void testValidatePassword_MalformedStoredPassword_TooFewParts() {
        String originalPassword = "myPassword";
        String storedPassword = "1001:someSalt"; // Missing hash part

        NumberFormatException ex = assertThrows(NumberFormatException.class, () -> {
    securePassword.validatePassword(originalPassword, storedPassword);
});
        assertNotNull(ex);
    }

    @Test
    void testValidatePassword_MalformedStoredPassword_InvalidIterationsFormat() {
        String originalPassword = "myPassword";
        String storedPassword = "abc:someSaltHex:someHashHex"; // Invalid iterations format

        NumberFormatException ex = assertThrows(NumberFormatException.class, () -> {
            securePassword.validatePassword(originalPassword, storedPassword);
        });
        assertNotNull(ex);
    }

    @Test
    void testValidatePassword_MalformedStoredPassword_InvalidHexFormat() {
        String originalPassword = "myPassword";
        // Valid length but includes non-hex characters 'G'
        String storedPassword = "1001:0123456789ABCDEF0123456789ABCDEF:0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEG";

        NumberFormatException ex = assertThrows(NumberFormatException.class, () -> {
            securePassword.validatePassword(originalPassword, storedPassword);
        });
        assertNotNull(ex);
    }

    @Test
    void testValidatePasswordExisting_MalformedStoredPassword_TooFewParts() {
        String originalPassword = "myPassword";
        String storedPassword = "1000:someSalt"; // Missing hash part
NumberFormatException ex = assertThrows(NumberFormatException.class, () -> {
    securePassword.validatePassword(originalPassword, storedPassword);
});
        assertNotNull(ex);
    }

    @Test
    void testValidatePasswordExisting_MalformedStoredPassword_InvalidIterationsFormat() {
        String originalPassword = "myPassword";
        String storedPassword = "abc:someSaltHex:someHashHex"; // Invalid iterations format

        NumberFormatException ex = assertThrows(NumberFormatException.class, () -> {
            securePassword.validatePasswordExisting(originalPassword, storedPassword);
        });
        assertNotNull(ex);
    }

    @Test
    void testValidatePasswordExisting_MalformedStoredPassword_InvalidHexFormat() {
        String originalPassword = "myPassword";
        // Valid length but includes non-hex characters 'G'
        String storedPassword = "1000:0123456789ABCDEF0123456789ABCDEF:0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEG";

        NumberFormatException ex = assertThrows(NumberFormatException.class, () -> {
            securePassword.validatePasswordExisting(originalPassword, storedPassword);
        });
        assertNotNull(ex);
    }

    @Test
    void testGenerateStrongPassword_ValidInput() throws NoSuchAlgorithmException, InvalidKeySpecException {
        String password = "testPassword123!@#";
        String strongPassword = securePassword.generateStrongPassword(password);

        assertNotNull(strongPassword);
        assertFalse(strongPassword.isEmpty());

        // Expected format: iterations:salt:hash
        String[] parts = strongPassword.split(":");
        assertEquals(3, parts.length, "Strong password should have 3 parts separated by colons.");

        // Verify iterations are 1001 as per generateStrongPassword implementation
        assertEquals("1001", parts[0], "Iterations should be 1001.");

        // Verify salt and hash are valid hex strings
        assertTrue(parts[1].matches("[0-9a-fA-F]+"), "Salt should be a hex string.");
        assertTrue(parts[2].matches("[0-9a-fA-F]+"), "Hash should be a hex string.");

        // Verify expected lengths (salt: 16 bytes = 32 hex chars, hash: 512 bits = 64 bytes = 128 hex chars)
        assertEquals(32, parts[1].length(), "Salt hex string should be 32 characters long.");
        assertEquals(128, parts[2].length(), "Hash hex string should be 128 characters long.");
    }

    @Test
    void testGenerateStrongPassword_EmptyString() throws NoSuchAlgorithmException, InvalidKeySpecException {
        String password = "";
        String strongPassword = securePassword.generateStrongPassword(password);

        assertNotNull(strongPassword);
        assertFalse(strongPassword.isEmpty());
        String[] parts = strongPassword.split(":");
        assertEquals(3, parts.length);
        assertEquals("1001", parts[0]);
        assertEquals(32, parts[1].length());
        assertEquals(128, parts[2].length());
    }

    @Test
    void testValidatePassword_CorrectPasswordGeneratedByStrongPassword() throws NoSuchAlgorithmException, InvalidKeySpecException {
        String originalPassword = "mySuperSecretPassword123";
        String storedPassword = securePassword.generateStrongPassword(originalPassword);

        // Expecting 3 because generateStrongPassword uses 1001 iterations and PBKDF2WithHmacSHA512,
        // which corresponds to the 'iterations == 1001' block in validatePassword.
        int result = securePassword.validatePassword(originalPassword, storedPassword);
        assertEquals(3, result, "Validation should return 3 for a correct password generated by generateStrongPassword.");
    }

    @Test
    void testValidatePassword_IncorrectPassword() throws NoSuchAlgorithmException, InvalidKeySpecException {
        String originalPassword = "mySuperSecretPassword123";
        String storedPassword = securePassword.generateStrongPassword(originalPassword);

        int result = securePassword.validatePassword("wrongPassword", storedPassword);
        assertEquals(0, result, "Validation should return 0 for an incorrect password.");
    }

    @Test
    void testValidatePasswordExisting_AlwaysFalseForStrongPassword() throws NoSuchAlgorithmException, InvalidKeySpecException {
        String originalPassword = "somePassword";
        String storedPassword = securePassword.generateStrongPassword(originalPassword);

        // generateStrongPassword uses PBKDF2WithHmacSHA512, while validatePasswordExisting uses PBKDF2WithHmacSHA1.
        // Therefore, a password generated by generateStrongPassword will not be validated by validatePasswordExisting.
        boolean result = securePassword.validatePasswordExisting(originalPassword, storedPassword);
        assertFalse(result, "validatePasswordExisting should return false for passwords generated by generateStrongPassword (different algorithm).");
    }

    @Test
    void testValidatePasswordExisting_IncorrectPassword() throws NoSuchAlgorithmException, InvalidKeySpecException {
        String originalPassword = "testPassword";
        String storedPassword = securePassword.generateStrongPassword(originalPassword);

        boolean result = securePassword.validatePasswordExisting("wrongPassword", storedPassword);
        assertFalse(result, "validatePasswordExisting should return false for an incorrect password, even if algorithms matched.");
    }

    @Test
    void testValidatePassword_InvalidStoredPasswordFormat_ThrowsException() {
        String originalPassword = "AnyPassword";

        // Test with too few parts
        String invalidFormat1 = "1001:salt";
        // Assign the thrown exception to a variable to avoid 'Throwable method result is ignored' warning
        NumberFormatException ex1 = assertThrows(NumberFormatException.class, () -> securePassword.validatePassword(originalPassword, invalidFormat1));
        assertNotNull(ex1);

        // Test with non-integer iterations
        String invalidFormat2 = "abc:salt:hash";
        NumberFormatException ex2 = assertThrows(NumberFormatException.class, () -> securePassword.validatePassword(originalPassword, invalidFormat2));
        assertNotNull(ex2);

        // Test with non-hex salt/hash (fromHex will throw NumberFormatException)
        String invalidFormat3 = "1001:nothex:morenothex";
        NumberFormatException ex3 = assertThrows(NumberFormatException.class, () -> securePassword.validatePassword(originalPassword, invalidFormat3));
        assertNotNull(ex3);
    }

    @Test
    void testGenerateStrongPassword() throws NoSuchAlgorithmException, InvalidKeySpecException {
        String originalPassword = "MySecurePassword123!";
        String generatedPassword = securePassword.generateStrongPassword(originalPassword);

        assertNotNull(generatedPassword);
        assertFalse(generatedPassword.isEmpty());

        String[] parts = generatedPassword.split(":");
        assertEquals(3, parts.length, "Generated password should have 3 parts: iterations:salt:hash");

        int iterations = Integer.parseInt(parts[0]);
        assertEquals(1001, iterations, "Generated password should use 1001 iterations");

        // Verify that the generated password can be validated by the validatePassword method
        int validationResult = securePassword.validatePassword(originalPassword, generatedPassword);
        assertEquals(3, validationResult, "Generated password should be valid (result 3)");
    }

    @Test
    void testValidatePassword_GeneratedPassword_Returns3() throws NoSuchAlgorithmException, InvalidKeySpecException {
        String originalPassword = "TestPasswordForValidation";
        String storedPassword = securePassword.generateStrongPassword(originalPassword);

        int result = securePassword.validatePassword(originalPassword, storedPassword);
        assertEquals(3, result, "Should return 3 for a valid password generated with current schema (1001 iterations, SHA512)");
    }

    @Test
    void testValidatePassword_OldSchemaSHA1_Returns1() throws NoSuchAlgorithmException, InvalidKeySpecException {
        String originalPassword = "OldPasswordSHA1";
        // Simulate an old password generated with 1000 iterations and PBKDF2WithHmacSHA1
        String storedPasswordOldSHA1 = generateTestStoredPassword(originalPassword, 1000, "PBKDF2WithHmacSHA1", 160); // 160 bits for SHA1

        int result = securePassword.validatePassword(originalPassword, storedPasswordOldSHA1);
        assertEquals(1, result, "Should return 1 for a valid password from old SHA1 schema (1000 iterations)");
    }

    @Test
    void testValidatePassword_OldSchemaSHA512_Returns2() throws NoSuchAlgorithmException, InvalidKeySpecException {
        String originalPassword = "OldPasswordSHA512";
        // Simulate an old password generated with 1000 iterations and PBKDF2WithHmacSHA512
        String storedPasswordOldSHA512 = generateTestStoredPassword(originalPassword, 1000, "PBKDF2WithHmacSHA512", 512); // 512 bits for SHA512

        int result = securePassword.validatePassword(originalPassword, storedPasswordOldSHA512);
        assertEquals(2, result, "Should return 2 for a valid password from old SHA512 schema (1000 iterations)");
    }

    @Test
    void testValidatePassword_IncorrectPassword_Returns0() throws NoSuchAlgorithmException, InvalidKeySpecException {
        String originalPassword = "CorrectPassword";
        String wrongPassword = "WrongPassword";
        String storedPassword = securePassword.generateStrongPassword(originalPassword);

        int result = securePassword.validatePassword(wrongPassword, storedPassword);
        assertEquals(0, result, "Should return 0 for an incorrect password");
    }

    @Test
    void testValidatePasswordExisting_CorrectOldSchemaSHA1_ReturnsTrue() throws NoSuchAlgorithmException, InvalidKeySpecException {
        String originalPassword = "ExistingUserPassword";
        // generateTestStoredPassword uses a fixed salt for deterministic output
        String storedPasswordForExisting = generateTestStoredPassword(originalPassword, 1000, "PBKDF2WithHmacSHA1", 160);

        boolean result = securePassword.validatePasswordExisting(originalPassword, storedPasswordForExisting);
        assertTrue(result, "Should return true for a valid password against the existing SHA1 schema");
    }

    @Test
    void testValidatePasswordExisting_IncorrectPassword_ReturnsFalse() throws NoSuchAlgorithmException, InvalidKeySpecException {
        String originalPassword = "ExistingUserPassword";
        String wrongPassword = "IncorrectPassword";
        String storedPasswordForExisting = generateTestStoredPassword(originalPassword, 1000, "PBKDF2WithHmacSHA1", 160);

        boolean result = securePassword.validatePasswordExisting(wrongPassword, storedPasswordForExisting);
        assertFalse(result, "Should return false for an incorrect password against the existing SHA1 schema");
    }

    @Test
    void testValidatePasswordExisting_NewSchemaPassword_ReturnsFalse() throws NoSuchAlgorithmException, InvalidKeySpecException {
        String originalPassword = "NewUserPassword";
        // Password generated by generateStrongPassword uses 1001 iterations and PBKDF2WithHmacSHA512
        String storedPasswordNewSchema = securePassword.generateStrongPassword(originalPassword);

        // validatePasswordExisting method explicitly uses PBKDF2WithHmacSHA1
        boolean result = securePassword.validatePasswordExisting(originalPassword, storedPasswordNewSchema);
        assertFalse(result, "Should return false when validating a new schema password (SHA512) against the existing SHA1 schema");
    }

    // Helper method for generating test passwords for old schemas
    private String generateTestStoredPassword(String password, int iterations, String algorithm, int keyLengthBits)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] salt = new byte[16];
        // Use a fixed salt for deterministic output in tests
        for (int i = 0; i < salt.length; i++) {
            salt[i] = (byte) i;
        }
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, keyLengthBits);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(algorithm);
        byte[] hash = factory.generateSecret(spec).getEncoded();
        return iterations + ":" + toHex(salt) + ":" + toHex(hash);
    }

    // Helper method to convert byte array to hex string
    private static String toHex(byte[] array) {
        StringBuilder sb = new StringBuilder(array.length * 2);
        for (byte b : array) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}