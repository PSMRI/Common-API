package com.iemr.common.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

/** Covers the Redis-backed denylist of revoked JWT ids. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TokenDenylistTest {

	@Mock
	private RedisTemplate<String, Object> redisTemplate;
	@Mock
	private ValueOperations<String, Object> valueOperations;

	@InjectMocks
	private TokenDenylist denylist;

	@Test
	@DisplayName("a denied jti is stored under the denied_ prefix with its expiry")
	void addStoresPrefixedKeyWithExpiry() {
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);

		denylist.addTokenToDenylist("abc-123", 60_000L);

		verify(valueOperations).set("denied_abc-123", " ", 60_000L, TimeUnit.MILLISECONDS);
	}

	@ParameterizedTest
	@NullSource
	@ValueSource(strings = { "", "   " })
	@DisplayName("a blank jti is ignored rather than written to Redis")
	void addIgnoresBlankJti(String jti) {
		denylist.addTokenToDenylist(jti, 60_000L);

		verifyNoInteractions(redisTemplate);
	}

	@ParameterizedTest
	@NullSource
	@ValueSource(longs = { 0L, -1L })
	@DisplayName("a non-positive expiry is rejected")
	void addRejectsNonPositiveExpiry(Long expiry) {
		assertThatThrownBy(() -> denylist.addTokenToDenylist("abc-123", expiry))
				.isInstanceOf(IllegalArgumentException.class).hasMessage("Expiration time must be positive");
	}

	@Test
	@DisplayName("a Redis failure while denylisting is surfaced, never silently dropped")
	void addSurfacesRedisFailure() {
		when(redisTemplate.opsForValue()).thenThrow(new IllegalStateException("redis down"));

		assertThatThrownBy(() -> denylist.addTokenToDenylist("abc-123", 60_000L))
				.isInstanceOf(RuntimeException.class).hasMessage("Failed to denylist token");
	}

	@Test
	@DisplayName("a known jti is reported as denylisted")
	void knownJtiIsDenylisted() {
		when(redisTemplate.hasKey("denied_abc-123")).thenReturn(true);

		assertThat(denylist.isTokenDenylisted("abc-123")).isTrue();
	}

	@Test
	@DisplayName("an unknown jti is not denylisted")
	void unknownJtiIsNotDenylisted() {
		when(redisTemplate.hasKey(anyString())).thenReturn(false);

		assertThat(denylist.isTokenDenylisted("abc-123")).isFalse();
	}

	@ParameterizedTest
	@NullSource
	@ValueSource(strings = { "", "   " })
	@DisplayName("a blank jti is never looked up")
	void blankJtiIsNotLookedUp(String jti) {
		assertThat(denylist.isTokenDenylisted(jti)).isFalse();

		verifyNoInteractions(redisTemplate);
	}

	@Test
	@DisplayName("a Redis outage lets requests through rather than blocking every caller")
	void redisOutageFailsOpen() {
		when(redisTemplate.hasKey(anyString())).thenThrow(new IllegalStateException("redis down"));

		assertThat(denylist.isTokenDenylisted("abc-123")).isFalse();
	}

	@Test
	@DisplayName("removing a jti deletes its prefixed key")
	void removeDeletesPrefixedKey() {
		denylist.removeTokenFromDenylist("abc-123");

		verify(redisTemplate).delete("denied_abc-123");
	}

	@ParameterizedTest
	@NullSource
	@ValueSource(strings = { "", "   " })
	@DisplayName("removing a blank jti is a no-op")
	void removeIgnoresBlankJti(String jti) {
		assertThatCode(() -> denylist.removeTokenFromDenylist(jti)).doesNotThrowAnyException();

		verify(redisTemplate, never()).delete(anyString());
	}
}
