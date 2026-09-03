package com.iemr.common.utils.sessionobject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.iemr.common.utils.redis.RedisSessionException;
import com.iemr.common.utils.redis.RedisStorage;

/**
 * Covers the session facade used for login sessions and the change-password flow.
 *
 * <p>Every operation is a no-op for a missing key, so that guard is asserted for each, and
 * an update additionally mirrors the session under the user name so a concurrent login can
 * be detected.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SessionObjectTest {

	private static final String KEY = "a-session-key";

	@Mock
	private RedisStorage objectStore;

	private SessionObject sessionObject;

	@BeforeEach
	void setUp() {
		sessionObject = new SessionObject();
		sessionObject.setObjectStore(objectStore);
	}

	@Nested
	@DisplayName("login sessions")
	class LoginSessions {

		@Test
		@DisplayName("reads a session through the store")
		void readsASession() throws Exception {
			when(objectStore.getObject(eq(KEY), any(), anyInt())).thenReturn("the-session");

			assertThat(sessionObject.getSessionObject(KEY)).isEqualTo("the-session");
		}

		@Test
		@DisplayName("writes a session through the store")
		void writesASession() throws Exception {
			when(objectStore.setObject(eq(KEY), anyString(), anyInt())).thenReturn(KEY);

			assertThat(sessionObject.setSessionObject(KEY, "the-session")).isEqualTo(KEY);
		}

		@Test
		@DisplayName("updates a session through the store")
		void updatesASession() throws Exception {
			when(objectStore.updateObject(eq(KEY), anyString(), any(), anyInt())).thenReturn(KEY);

			assertThat(sessionObject.updateSessionObject(KEY, "{}")).isEqualTo(KEY);
		}

		@Test
		@DisplayName("an update also indexes the session under the user name")
		void updateIndexesByUserName() throws Exception {
			sessionObject.updateSessionObject(KEY, "{\"userName\":\"  Asha  \"}");

			// The user name is normalised so a concurrent login can find the session.
			verify(objectStore).updateObject(eq("asha"), eq(KEY), any(), anyInt());
		}

		@Test
		@DisplayName("an update with no user name only touches the session key")
		void updateWithoutUserName() throws Exception {
			sessionObject.updateSessionObject(KEY, "{\"other\":\"value\"}");

			verify(objectStore, never()).updateObject(eq("asha"), anyString(), any(), anyInt());
			verify(objectStore).updateObject(eq(KEY), anyString(), any(), anyInt());
		}

		@Test
		@DisplayName("an update with an unparseable payload still updates the session key")
		void updateWithUnparseablePayload() throws Exception {
			sessionObject.updateSessionObject(KEY, "not json at all");

			verify(objectStore).updateObject(eq(KEY), anyString(), any(), anyInt());
		}
	}

	@Nested
	@DisplayName("change password sessions")
	class ChangePasswordSessions {

		@Test
		@DisplayName("writes a change password session through the store")
		void writesASession() throws Exception {
			when(objectStore.setObject(eq(KEY), anyString(), anyInt())).thenReturn(KEY);

			assertThat(sessionObject.setSessionObjectForChangePassword(KEY, "the-session")).isEqualTo(KEY);
		}

		@Test
		@DisplayName("reads a change password session through the store")
		void readsASession() throws Exception {
			when(objectStore.getObject(eq(KEY), any(), anyInt())).thenReturn("the-session");

			assertThat(sessionObject.getSessionObjectForChangePassword(KEY)).isEqualTo("the-session");
		}
	}

	@Nested
	@DisplayName("deleteSessionObject")
	class Delete {

		@Test
		@DisplayName("removes the session from the store")
		void removesTheSession() throws Exception {
			sessionObject.deleteSessionObject(KEY);

			verify(objectStore).deleteObject(KEY);
		}

		@Test
		@DisplayName("a store failure is contained rather than propagated")
		void containsStoreFailures() throws Exception {
			doThrow(new RedisSessionException("redis down")).when(objectStore).deleteObject(KEY);

			assertThatCode(() -> sessionObject.deleteSessionObject(KEY)).doesNotThrowAnyException();
		}

		@ParameterizedTest(name = "key \"{0}\"")
		@NullAndEmptySource
		@DisplayName("does nothing without a key")
		void doesNothingWithoutAKey(String key) {
			sessionObject.deleteSessionObject(key);

			verifyNoInteractions(objectStore);
		}
	}

	@Nested
	@DisplayName("missing keys")
	class MissingKeys {

		@ParameterizedTest(name = "key \"{0}\"")
		@NullAndEmptySource
		@DisplayName("every session operation is a no-op without a key")
		void everyOperationIsANoOp(String key) throws Exception {
			assertThat(sessionObject.getSessionObject(key)).isNull();
			assertThat(sessionObject.setSessionObject(key, "value")).isNull();
			assertThat(sessionObject.updateSessionObject(key, "{}")).isNull();
			assertThat(sessionObject.setSessionObjectForChangePassword(key, "value")).isNull();
			assertThat(sessionObject.getSessionObjectForChangePassword(key)).isNull();

			verifyNoInteractions(objectStore);
		}
	}
}
