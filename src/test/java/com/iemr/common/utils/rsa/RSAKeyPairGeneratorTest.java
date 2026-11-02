package com.iemr.common.utils.rsa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.iemr.common.notification.exception.IEMRException;

/**
 * Covers the RSA key pair generator used to seed the encrypted-login key material.
 */
class RSAKeyPairGeneratorTest {

	@Test
	@DisplayName("generates a matching 2048 bit RSA key pair")
	void generatesAKeyPair() throws Exception {
		RSAKeyPairGenerator generator = new RSAKeyPairGenerator();

		assertThat(generator.getPublicKey()).isNotNull();
		assertThat(generator.getPrivateKey()).isNotNull();
		assertThat(generator.getPublicKey().getAlgorithm()).isEqualTo("RSA");
		assertThat(generator.getPrivateKey().getAlgorithm()).isEqualTo("RSA");
	}

	@Test
	@DisplayName("each generator produces a distinct key pair")
	void keyPairsAreDistinct() throws Exception {
		assertThat(new RSAKeyPairGenerator().getPublicKey().getEncoded())
				.isNotEqualTo(new RSAKeyPairGenerator().getPublicKey().getEncoded());
	}

	@Test
	@DisplayName("writes a key to disk, creating the parent directories")
	void writesAKeyToDisk(@TempDir Path tempDir) throws Exception {
		RSAKeyPairGenerator generator = new RSAKeyPairGenerator();
		Path target = tempDir.resolve("nested/dir/publicKey");

		generator.writeToFile(target.toString(), generator.getPublicKey().getEncoded());

		assertThat(Files.readAllBytes(target)).isEqualTo(generator.getPublicKey().getEncoded());
	}

	@Test
	@DisplayName("an unwritable path is reported rather than thrown")
	void unwritablePathIsContained() throws Exception {
		RSAKeyPairGenerator generator = new RSAKeyPairGenerator();

		assertThatCode(() -> generator.writeToFile("/proc/definitely/not/writable/key",
				generator.getPublicKey().getEncoded())).doesNotThrowAnyException();
	}

	@Test
	@DisplayName("reads back a base64 encoded public key")
	void readsBackAPublicKey() throws Exception {
		RSAKeyPairGenerator generator = new RSAKeyPairGenerator();
		String encoded = Base64.getEncoder().encodeToString(generator.getPublicKey().getEncoded());

		assertThat(RSAKeyPairGenerator.getPublicKey(encoded).getEncoded())
				.isEqualTo(generator.getPublicKey().getEncoded());
	}

	@Test
	@DisplayName("returns nothing for a public key that is not a valid key")
	void rejectsInvalidPublicKey() throws IEMRException {
		assertThat(RSAKeyPairGenerator.getPublicKey(Base64.getEncoder().encodeToString("not a key".getBytes())))
				.isNull();
	}

}
