package com.iemr.common.utils.redis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands.SetOption;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Covers the raw Redis session store behind the session object.
 *
 * <p>All four operations read the key first and then decide what to do, so the tests drive
 * the "key present" and "key absent" cases for each and assert both the returned value and
 * the command actually issued to Redis.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RedisStorageTest {

	private static final String KEY = "session-key";
	private static final String VALUE = "{\"userName\":\"asha\"}";
	private static final int EXPIRY_SECONDS = 1620;

	@Mock
	private LettuceConnectionFactory connectionFactory;
	@Mock
	private RedisConnection connection;

	private RedisStorage storage;

	@BeforeEach
	void setUp() {
		storage = new RedisStorage();
		ReflectionTestUtils.setField(storage, "connection", connectionFactory);
		when(connectionFactory.getConnection()).thenReturn(connection);
	}

	private void keyHolds(String value) {
		when(connection.get(KEY.getBytes())).thenReturn(value == null ? null : value.getBytes(StandardCharsets.UTF_8));
	}

	@Nested
	@DisplayName("setObject")
	class SetObject {

		@Test
		@DisplayName("writes the value with an expiry when the key is not yet held")
		void writesWhenKeyAbsent() throws Exception {
			keyHolds(null);

			assertThat(storage.setObject(KEY, VALUE, EXPIRY_SECONDS)).isEqualTo(KEY);
			verify(connection).set(eq(KEY.getBytes()), eq(VALUE.getBytes()),
					eq(Expiration.seconds(EXPIRY_SECONDS)), eq(SetOption.UPSERT));
		}

		@Test
		@DisplayName("leaves an existing session untouched")
		void leavesExistingSessionAlone() throws Exception {
			keyHolds("an existing session");

			assertThat(storage.setObject(KEY, VALUE, EXPIRY_SECONDS)).isEqualTo(KEY);
			verify(connection, never()).set(any(), any(), any(Expiration.class), any(SetOption.class));
		}

		@Test
		@DisplayName("treats an empty stored value as absent and writes over it")
		void writesOverAnEmptyValue() throws Exception {
			keyHolds("");

			assertThat(storage.setObject(KEY, VALUE, EXPIRY_SECONDS)).isEqualTo(KEY);
			verify(connection).set(any(), any(), any(Expiration.class), any(SetOption.class));
		}
	}

	@Nested
	@DisplayName("getObject")
	class GetObject {

		@Test
		@DisplayName("returns the stored session and refreshes its expiry")
		void returnsAndRefreshes() throws Exception {
			keyHolds(VALUE);

			assertThat(storage.getObject(KEY, true, EXPIRY_SECONDS)).isEqualTo(VALUE);
			verify(connection).expire(KEY.getBytes(), EXPIRY_SECONDS);
		}

		@Test
		@DisplayName("fails when the key holds nothing")
		void failsWhenKeyAbsent() {
			keyHolds(null);

			assertThatExceptionOfType(RedisSessionException.class)
					.isThrownBy(() -> storage.getObject(KEY, true, EXPIRY_SECONDS))
					.withMessageContaining("Unable to fetch session object from Redis server");
		}

		@Test
		@DisplayName("fails when the key holds only whitespace")
		void failsWhenKeyBlank() {
			keyHolds("   ");

			assertThatExceptionOfType(RedisSessionException.class)
					.isThrownBy(() -> storage.getObject(KEY, false, EXPIRY_SECONDS));
		}
	}

	@Nested
	@DisplayName("updateObject")
	class UpdateObject {

		@Test
		@DisplayName("overwrites an existing session and resets its expiry")
		void overwritesExistingSession() throws Exception {
			keyHolds("an existing session");

			assertThat(storage.updateObject(KEY, VALUE, true, EXPIRY_SECONDS)).isEqualTo(KEY);
			verify(connection).set(eq(KEY.getBytes()), eq(VALUE.getBytes()),
					eq(Expiration.seconds(EXPIRY_SECONDS)), eq(SetOption.UPSERT));
		}

		@Test
		@DisplayName("fails when there is no session to update")
		void failsWhenNoSession() {
			keyHolds(null);

			assertThatExceptionOfType(RedisSessionException.class)
					.isThrownBy(() -> storage.updateObject(KEY, VALUE, true, EXPIRY_SECONDS));
		}
	}

	@Nested
	@DisplayName("deleteObject")
	class DeleteObject {

		@Test
		@DisplayName("reports how many keys were removed")
		void reportsRemovedCount() throws Exception {
			when(connection.del(KEY.getBytes())).thenReturn(1L);

			assertThat(storage.deleteObject(KEY)).isEqualTo(1L);
		}

		@Test
		@DisplayName("reports zero when the key was not held")
		void reportsZeroWhenAbsent() throws Exception {
			when(connection.del(KEY.getBytes())).thenReturn(0L);

			assertThat(storage.deleteObject(KEY)).isZero();
		}
	}
}
