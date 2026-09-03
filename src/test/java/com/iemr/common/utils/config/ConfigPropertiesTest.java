package com.iemr.common.utils.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

/**
 * Covers the static property accessor shared across the service.
 *
 * <p>Its caches and its environment reference are static, so each test snapshots and
 * restores them to keep the class in the state the rest of the suite found it in.
 */
class ConfigPropertiesTest {

	private static final String[] CACHED_FIELDS = { "environment", "redisurl", "redisport", "extendExpiryTime",
			"sessionExpiryTime", "sessionExpiryTimeForChangePassword", "extendExpiryTimeForChangePassword" };

	private final Map<String, Object> originalValues = new HashMap<>();
	private MockEnvironment environment;

	@BeforeEach
	void snapshotStaticState() throws Exception {
		for (String name : CACHED_FIELDS) {
			originalValues.put(name, read(name));
			write(name, null);
		}
		environment = new MockEnvironment();
		new ConfigProperties().setEnvironment(environment);
	}

	@AfterEach
	void restoreStaticState() throws Exception {
		for (String name : CACHED_FIELDS) {
			write(name, originalValues.get(name));
		}
	}

	private static Field field(String name) throws Exception {
		Field field = ConfigProperties.class.getDeclaredField(name);
		field.setAccessible(true);
		return field;
	}

	private static Object read(String name) throws Exception {
		return field(name).get(null);
	}

	private static void write(String name, Object value) throws Exception {
		field(name).set(null, value);
	}

	@Test
	@DisplayName("a property is read from the environment with its surrounding whitespace trimmed")
	void propertyIsReadFromEnvironmentAndTrimmed() {
		environment.withProperty("some.key", "  a value  ");

		assertThat(ConfigProperties.getPropertyByName("some.key")).isEqualTo("a value");
	}

	@Test
	@DisplayName("a property absent from every source is reported as missing rather than throwing")
	void missingPropertyIsNull() {
		assertThat(ConfigProperties.getPropertyByName("no.such.key.anywhere")).isNull();
	}

	@Test
	@DisplayName("a boolean property is parsed, and anything unparseable reads as false")
	void booleanPropertiesAreParsed() {
		environment.withProperty("flag.on", "true").withProperty("flag.junk", "yes");

		assertThat(ConfigProperties.getBoolean("flag.on")).isTrue();
		assertThat(ConfigProperties.getBoolean("flag.junk")).isFalse();
		assertThat(ConfigProperties.getBoolean("flag.missing")).isFalse();
	}

	@Test
	@DisplayName("numeric properties are parsed, and anything unparseable falls back to zero")
	void numericPropertiesAreParsed() {
		environment.withProperty("num.int", "42").withProperty("num.long", "9999999999")
				.withProperty("num.float", "1.5").withProperty("num.junk", "not-a-number");

		assertThat(ConfigProperties.getInteger("num.int")).isEqualTo(42);
		assertThat(ConfigProperties.getLong("num.long")).isEqualTo(9999999999L);
		assertThat(ConfigProperties.getFloat("num.float")).isEqualTo(1.5F);
		assertThat(ConfigProperties.getInteger("num.junk")).isZero();
		assertThat(ConfigProperties.getLong("num.junk")).isZero();
		assertThat(ConfigProperties.getFloat("num.junk")).isZero();
	}

	@Test
	@DisplayName("an obfuscated password is decoded, a plain one is returned as it stands")
	void obfuscatedPasswordIsDecoded() {
		String encoded = Base64.getEncoder().encodeToString("s3cret".getBytes());
		environment.withProperty("db.password", "0X10:" + encoded).withProperty("db.plain", "plaintext");

		assertThat(ConfigProperties.getPassword("db.password")).isEqualTo("s3cret");
		assertThat(ConfigProperties.getPassword("db.plain")).isEqualTo("plaintext");
		assertThat(ConfigProperties.getPassword("db.missing")).isNull();
	}

	@Test
	@DisplayName("the Redis connection details are resolved once and then cached")
	void redisSettingsAreResolvedAndCached() {
		environment.withProperty("iemr.redis.url", "redis.internal").withProperty("iemr.redis.port", "6380");

		assertThat(ConfigProperties.getRedisUrl()).isEqualTo("redis.internal");
		assertThat(ConfigProperties.getRedisPort()).isEqualTo(6380);

		environment.withProperty("iemr.redis.url", "changed.internal");
		assertThat(ConfigProperties.getRedisUrl()).isEqualTo("redis.internal");
	}

	@Test
	@DisplayName("the session expiry settings are resolved from configuration")
	void sessionExpirySettingsAreResolved() {
		environment.withProperty("iemr.session.expiry.time", "27")
				.withProperty("iemr.session.expiry.time.changePassword", "5");

		assertThat(ConfigProperties.getSessionExpiryTime()).isEqualTo(27);
		assertThat(ConfigProperties.getExtendExpiryTime()).isFalse();
		assertThat(ConfigProperties.getSessionExpiryTimeForChangePassword()).isEqualTo(5);
		assertThat(ConfigProperties.getExtendExpiryTimeForChangePassword()).isFalse();
	}

	@Test
	@DisplayName("an expiry flag set to true is honoured")
	void extendExpiryFlagIsHonoured() {
		environment.withProperty("iemr.session.expiry.time", "true")
				.withProperty("iemr.session.expiry.time.changePassword", "true");

		assertThat(ConfigProperties.getExtendExpiryTime()).isTrue();
		assertThat(ConfigProperties.getExtendExpiryTimeForChangePassword()).isTrue();
	}
}
