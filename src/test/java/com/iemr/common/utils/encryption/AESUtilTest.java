package com.iemr.common.utils.encryption;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.nio.charset.StandardCharsets;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.HexFormat;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Covers the CryptoJS-compatible AES decryption used for password handling.
 *
 * <p>The class only decrypts, so the fixtures are encrypted here with the same
 * PBKDF2 + AES/CBC/ISO10126Padding scheme the production code expects. That both
 * pins the wire format and exercises the real cipher rather than a stub.
 */
class AESUtilTest {

	private static final String PASS_PHRASE = "a-pass-phrase";
	private static final int ITERATION_COUNT = 1989;
	private static final int KEY_SIZE = 256;
	/** The class derives its salt length from the key size: 256 / 4 hex characters. */
	private static final int SALT_HEX_LENGTH = KEY_SIZE / 4;
	/** The IV is a fixed 128 bits, carried as 128 / 4 hex characters. */
	private static final int IV_HEX_LENGTH = 32;

	private final AESUtil aesUtil = new AESUtil();

	private static String hex(int lengthInHexChars) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < lengthInHexChars; i++) {
			builder.append("0123456789abcdef".charAt(i % 16));
		}
		return builder.toString();
	}

	private static SecretKey key(String saltHex, String algorithm) throws Exception {
		SecretKeyFactory factory = SecretKeyFactory.getInstance(algorithm);
		KeySpec keySpec = new PBEKeySpec(PASS_PHRASE.toCharArray(), HexFormat.of().parseHex(saltHex), ITERATION_COUNT,
				KEY_SIZE);
		return new SecretKeySpec(factory.generateSecret(keySpec).getEncoded(), "AES");
	}

	private static String encrypt(String plainText, String saltHex, String ivHex, String algorithm) throws Exception {
		Cipher cipher = Cipher.getInstance("AES/CBC/ISO10126Padding");
		cipher.init(Cipher.ENCRYPT_MODE, key(saltHex, algorithm),
				new IvParameterSpec(HexFormat.of().parseHex(ivHex)));
		return Base64.getEncoder().encodeToString(cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8)));
	}

	@Nested
	@DisplayName("decrypt")
	class Decrypt {

		@Test
		@DisplayName("recovers the plain text from an explicit salt and iv")
		void recoversPlainText() throws Exception {
			String salt = hex(SALT_HEX_LENGTH);
			String iv = hex(IV_HEX_LENGTH);
			String cipherText = encrypt("Secret@123", salt, iv, "PBKDF2WithHmacSHA512");

			assertThat(aesUtil.decrypt(salt, iv, PASS_PHRASE, cipherText)).isEqualTo("Secret@123");
		}

		@Test
		@DisplayName("recovers the plain text when the salt and iv are prefixed onto the cipher text")
		void recoversPlainTextFromCombinedPayload() throws Exception {
			String salt = hex(SALT_HEX_LENGTH);
			String iv = hex(IV_HEX_LENGTH);
			String cipherText = encrypt("Secret@123", salt, iv, "PBKDF2WithHmacSHA512");

			assertThat(aesUtil.decrypt(PASS_PHRASE, salt + iv + cipherText)).isEqualTo("Secret@123");
		}

		@Test
		@DisplayName("returns null when the pass phrase is wrong")
		void returnsNullForWrongPassPhrase() throws Exception {
			String salt = hex(SALT_HEX_LENGTH);
			String iv = hex(IV_HEX_LENGTH);
			String cipherText = encrypt("Secret@123", salt, iv, "PBKDF2WithHmacSHA512");

			assertThat(aesUtil.decrypt(salt, iv, "the-wrong-phrase", cipherText)).isNotEqualTo("Secret@123");
		}

		@ParameterizedTest(name = "cipher text \"{0}\"")
		@ValueSource(strings = { "", "not-base64-!!", "short" })
		@DisplayName("returns null for a cipher text it cannot make sense of")
		void returnsNullForUnusableCipherText(String cipherText) {
			assertThat(aesUtil.decrypt(PASS_PHRASE, cipherText)).isNull();
		}

		@Test
		@DisplayName("returns null when the salt is not valid hex")
		void returnsNullForNonHexSalt() {
			assertThat(aesUtil.decrypt("not-hex", hex(IV_HEX_LENGTH), PASS_PHRASE, "AAAA")).isNull();
		}
	}

	@Nested
	@DisplayName("decryptExistingPwd")
	class DecryptExistingPassword {

		@Test
		@DisplayName("recovers a legacy SHA-1 derived password from an explicit salt and iv")
		void recoversLegacyPassword() throws Exception {
			String salt = hex(SALT_HEX_LENGTH);
			String iv = hex(IV_HEX_LENGTH);
			String cipherText = encrypt("Legacy@1", salt, iv, "PBKDF2WithHmacSHA1");

			assertThat(aesUtil.decryptExistingPwd(salt, iv, PASS_PHRASE, cipherText)).isEqualTo("Legacy@1");
		}

		@Test
		@DisplayName("recovers a legacy password from a combined payload")
		void recoversLegacyPasswordFromCombinedPayload() throws Exception {
			String salt = hex(SALT_HEX_LENGTH);
			String iv = hex(IV_HEX_LENGTH);
			String cipherText = encrypt("Legacy@1", salt, iv, "PBKDF2WithHmacSHA1");

			assertThat(aesUtil.decryptExistingPwd(PASS_PHRASE, salt + iv + cipherText)).isEqualTo("Legacy@1");
		}

		@Test
		@DisplayName("returns null for a payload shorter than the salt and iv it should carry")
		void returnsNullForTruncatedPayload() {
			assertThat(aesUtil.decryptExistingPwd(PASS_PHRASE, "tooshort")).isNull();
		}
	}

	@Nested
	@DisplayName("decryptExistingPwdNew")
	class DecryptExistingPasswordNew {

		@Test
		@DisplayName("re-derives the hash for an iterations:salt:hash encoded password")
		void reDerivesTheHash() {
			String salt = hex(32);
			String hash = hex(64);

			String derived = aesUtil.decryptExistingPwdNew(PASS_PHRASE, "1989:" + salt + ":" + hash);

			assertThat(derived).isNotNull().hasSize(hash.length()).matches("[0-9A-F]+");
		}

		@Test
		@DisplayName("is deterministic for the same pass phrase and salt")
		void isDeterministic() {
			String password = "1989:" + hex(32) + ":" + hex(64);

			assertThat(aesUtil.decryptExistingPwdNew(PASS_PHRASE, password))
					.isEqualTo(aesUtil.decryptExistingPwdNew(PASS_PHRASE, password));
		}

		@Test
		@DisplayName("derives a different hash for a different pass phrase")
		void differsByPassPhrase() {
			String password = "1989:" + hex(32) + ":" + hex(64);

			assertThat(aesUtil.decryptExistingPwdNew(PASS_PHRASE, password))
					.isNotEqualTo(aesUtil.decryptExistingPwdNew("another-phrase", password));
		}

		@ParameterizedTest(name = "password \"{0}\"")
		@ValueSource(strings = { "", "no-colons", "1989:onlysalt", "notanumber:abcd:ef01" })
		@DisplayName("rejects a password that is not in the expected encoding")
		void rejectsMalformedPassword(String password) {
			assertThatExceptionOfType(RuntimeException.class)
					.isThrownBy(() -> aesUtil.decryptExistingPwdNew(PASS_PHRASE, password))
					.withMessageContaining("Error decrypting password");
		}
	}

	@Test
	@DisplayName("an explicitly sized instance decrypts payloads salted for that key size")
	void honoursAnExplicitKeySize() throws Exception {
		AESUtil sized = new AESUtil(KEY_SIZE, ITERATION_COUNT);
		String salt = hex(SALT_HEX_LENGTH);
		String iv = hex(IV_HEX_LENGTH);
		String cipherText = encrypt("Sized@1", salt, iv, "PBKDF2WithHmacSHA512");

		assertThat(sized.decrypt(PASS_PHRASE, salt + iv + cipherText)).isEqualTo("Sized@1");
	}

	@Test
	@DisplayName("the data type enum exposes the two supported encodings")
	void dataTypeEnumValues() {
		assertThat(AESUtil.DataType.values()).containsExactly(AESUtil.DataType.HEX, AESUtil.DataType.BASE64);
		assertThat(AESUtil.DataType.valueOf("BASE64")).isEqualTo(AESUtil.DataType.BASE64);
	}
}
