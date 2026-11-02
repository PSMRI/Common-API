package com.iemr.common.utils.rsa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Covers the RSA helper used for the encrypted-login handshake.
 *
 * <p>A key pair is generated once for the class so the tests exercise the real cipher
 * rather than relying on the keys baked into the helper.
 */
class RSAUtilTest {

	private static String publicKeyBase64;
	private static String privateKeyBase64;

	@BeforeAll
	static void generateKeyPair() throws Exception {
		KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
		generator.initialize(2048);
		KeyPair pair = generator.generateKeyPair();
		publicKeyBase64 = Base64.getEncoder().encodeToString(pair.getPublic().getEncoded());
		privateKeyBase64 = Base64.getEncoder().encodeToString(pair.getPrivate().getEncoded());
	}

	@Test
	@DisplayName("reads a base64 encoded public key")
	void readsPublicKey() {
		assertThat(RSAUtil.getPublicKey(publicKeyBase64)).isNotNull()
				.extracting(java.security.PublicKey::getAlgorithm).isEqualTo("RSA");
	}

	@Test
	@DisplayName("reads a base64 encoded private key")
	void readsPrivateKey() {
		assertThat(RSAUtil.getPrivateKey(privateKeyBase64)).isNotNull()
				.extracting(java.security.PrivateKey::getAlgorithm).isEqualTo("RSA");
	}

	@Test
	@DisplayName("returns nothing for a public key that is not a valid key")
	void rejectsInvalidPublicKey() {
		assertThat(RSAUtil.getPublicKey(Base64.getEncoder().encodeToString("not a key".getBytes()))).isNull();
	}

	@Test
	@DisplayName("returns nothing for a private key that is not a valid key")
	void rejectsInvalidPrivateKey() {
		assertThat(RSAUtil.getPrivateKey(Base64.getEncoder().encodeToString("not a key".getBytes()))).isNull();
	}

	@ParameterizedTest(name = "\"{0}\"")
	@ValueSource(strings = { "Secret@123", "a", "a value with spaces and punctuation!" })
	@DisplayName("a value survives a round trip through the key pair")
	void roundTripsAValue(String plainText) throws Exception {
		byte[] cipherBytes = RSAUtil.encrypt(plainText, publicKeyBase64);

		assertThat(RSAUtil.decrypt(cipherBytes, RSAUtil.getPrivateKey(privateKeyBase64))).isEqualTo(plainText);
	}

	@Test
	@DisplayName("a base64 cipher text can be decrypted with a base64 private key")
	void roundTripsThroughBase64() throws Exception {
		String cipherText = Base64.getEncoder().encodeToString(RSAUtil.encrypt("Secret@123", publicKeyBase64));

		assertThat(RSAUtil.decrypt(cipherText, privateKeyBase64)).isEqualTo("Secret@123");
	}

	@Test
	@DisplayName("encrypting the same value twice yields different cipher text")
	void encryptionIsRandomised() throws Exception {
		// OAEP padding is randomised, so the same input must not encrypt identically.
		assertThat(RSAUtil.encrypt("Secret@123", publicKeyBase64))
				.isNotEqualTo(RSAUtil.encrypt("Secret@123", publicKeyBase64));
	}

	@Test
	@DisplayName("decryption rejects cipher text encrypted for another key")
	void rejectsForeignCipherText() throws Exception {
		KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
		generator.initialize(2048);
		KeyPair other = generator.generateKeyPair();
		byte[] cipherBytes = RSAUtil.encrypt("Secret@123",
				Base64.getEncoder().encodeToString(other.getPublic().getEncoded()));

		assertThatExceptionOfType(Exception.class)
				.isThrownBy(() -> RSAUtil.decrypt(cipherBytes, RSAUtil.getPrivateKey(privateKeyBase64)));
	}

	@Test
	@DisplayName("encryption fails when the public key cannot be read")
	void encryptionFailsForUnreadableKey() {
		assertThatExceptionOfType(Exception.class)
				.isThrownBy(() -> RSAUtil.encrypt("Secret@123",
						Base64.getEncoder().encodeToString("not a key".getBytes())));
	}

	@Test
	@DisplayName("the helper can be constructed")
	void isConstructible() {
		assertThat(new RSAUtil()).isNotNull();
	}
}
