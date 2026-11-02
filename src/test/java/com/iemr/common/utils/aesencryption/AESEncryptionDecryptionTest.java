package com.iemr.common.utils.aesencryption;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.Base64;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Covers the AES-GCM helper used for at-rest encryption of sensitive values.
 */
class AESEncryptionDecryptionTest {

	private final AESEncryptionDecryption crypto = new AESEncryptionDecryption();

	@ParameterizedTest(name = "\"{0}\"")
	@ValueSource(strings = { "a-secret-value", "", " ", "Beneficiary 4242 / Ward 3",
			"a much longer value that spans more than a single AES block to exercise the padding path" })
	@DisplayName("a value survives a round trip through encryption and decryption")
	void roundTripsAValue(String plainText) throws Exception {
		assertThat(crypto.decrypt(crypto.encrypt(plainText))).isEqualTo(plainText);
	}

	@Test
	@DisplayName("encrypting the same value twice yields different cipher text")
	void usesAFreshInitialisationVector() throws Exception {
		// GCM prefixes a random 12 byte IV, so the same input must not encrypt identically.
		assertThat(crypto.encrypt("a-secret-value")).isNotEqualTo(crypto.encrypt("a-secret-value"));
	}

	@Test
	@DisplayName("the cipher text is base64 and longer than the initialisation vector")
	void cipherTextIsBase64() throws Exception {
		String cipherText = crypto.encrypt("a-secret-value");

		assertThat(Base64.getDecoder().decode(cipherText)).hasSizeGreaterThan(12);
	}

	@Test
	@DisplayName("decryption rejects cipher text that has been tampered with")
	void rejectsTamperedCipherText() throws Exception {
		byte[] cipherBytes = Base64.getDecoder().decode(crypto.encrypt("a-secret-value"));
		cipherBytes[cipherBytes.length - 1] ^= 0x01;
		String tampered = Base64.getEncoder().encodeToString(cipherBytes);

		assertThatExceptionOfType(Exception.class).isThrownBy(() -> crypto.decrypt(tampered));
	}

	@Test
	@DisplayName("decryption rejects a value that is not valid base64")
	void rejectsNonBase64() {
		assertThatExceptionOfType(Exception.class).isThrownBy(() -> crypto.decrypt("not base64 !!"));
	}

	@Test
	@DisplayName("decryption rejects a value too short to carry an initialisation vector")
	void rejectsTruncatedValue() {
		assertThatExceptionOfType(Exception.class)
				.isThrownBy(() -> crypto.decrypt(Base64.getEncoder().encodeToString(new byte[4])));
	}

	@Test
	@DisplayName("an explicitly set key is used for subsequent operations")
	void honoursAnExplicitKey() throws Exception {
		AESEncryptionDecryption.setKey("a-different-passphrase");

		assertThat(crypto.decrypt(crypto.encrypt("a-secret-value"))).isEqualTo("a-secret-value");

		// Restore the default so the ordering of other tests does not matter.
		AESEncryptionDecryption.setKey("amrith$%2022@&*piramal@@swasthya!#");
	}
}
