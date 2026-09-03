package com.iemr.common.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import com.iemr.common.data.users.User;
import com.iemr.common.repository.users.IEMRUserRepositoryCustom;
import com.iemr.common.utils.exception.IEMRException;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Covers JWT validation against the user cache and the user table.
 *
 * <p>A token is only accepted once its user id resolves to a real user, either from the
 * Redis cache or, failing that, from the database — where it is then cached. Both paths and
 * every rejection are asserted.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JwtAuthenticationUtilTest {

	private static final String TOKEN = "a-jwt-token";

	@Mock
	private CookieUtil cookieUtil;
	@Mock
	private JwtUtil jwtUtil;
	@Mock
	private RedisTemplate<String, Object> redisTemplate;
	@Mock
	private ValueOperations<String, Object> valueOperations;
	@Mock
	private IEMRUserRepositoryCustom userRepository;
	@Mock
	private HttpServletRequest request;
	@Mock
	private Claims claims;

	private JwtAuthenticationUtil util;

	@BeforeEach
	void setUp() {
		util = new JwtAuthenticationUtil(cookieUtil, jwtUtil);
		ReflectionTestUtils.setField(util, "redisTemplate", redisTemplate);
		ReflectionTestUtils.setField(util, "iEMRUserRepositoryCustom", userRepository);
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
	}

	private static User user(long id, String name) {
		User user = new User();
		user.setUserID(id);
		user.setUserName(name);
		return user;
	}

	@Nested
	@DisplayName("validateJwtToken")
	class ValidateJwtToken {

		@Test
		@DisplayName("returns the user name carried by a valid cookie token")
		void returnsUserNameForValidToken() {
			when(cookieUtil.getCookieValue(request, "Jwttoken")).thenReturn(Optional.of(TOKEN));
			when(jwtUtil.validateToken(TOKEN)).thenReturn(claims);
			when(claims.getSubject()).thenReturn("asha");

			ResponseEntity<String> response = util.validateJwtToken(request);

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(response.getBody()).isEqualTo("asha");
		}

		@Test
		@DisplayName("refuses a request with no token cookie")
		void refusesMissingCookie() {
			when(cookieUtil.getCookieValue(request, "Jwttoken")).thenReturn(Optional.empty());

			ResponseEntity<String> response = util.validateJwtToken(request);

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
			assertThat(response.getBody()).contains("JWT Token is not set");
		}

		@Test
		@DisplayName("refuses a token that does not validate")
		void refusesInvalidToken() {
			when(cookieUtil.getCookieValue(request, "Jwttoken")).thenReturn(Optional.of(TOKEN));
			when(jwtUtil.validateToken(TOKEN)).thenReturn(null);

			ResponseEntity<String> response = util.validateJwtToken(request);

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
			assertThat(response.getBody()).contains("Invalid JWT Token");
		}

		@Test
		@DisplayName("refuses a token carrying no subject")
		void refusesTokenWithoutSubject() {
			when(cookieUtil.getCookieValue(request, "Jwttoken")).thenReturn(Optional.of(TOKEN));
			when(jwtUtil.validateToken(TOKEN)).thenReturn(claims);
			when(claims.getSubject()).thenReturn(null);

			ResponseEntity<String> response = util.validateJwtToken(request);

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
			assertThat(response.getBody()).contains("Username is missing");
		}

		@Test
		@DisplayName("refuses a token carrying an empty subject")
		void refusesTokenWithEmptySubject() {
			when(cookieUtil.getCookieValue(request, "Jwttoken")).thenReturn(Optional.of(TOKEN));
			when(jwtUtil.validateToken(TOKEN)).thenReturn(claims);
			when(claims.getSubject()).thenReturn("");

			assertThat(util.validateJwtToken(request).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
		}
	}

	@Nested
	@DisplayName("validateUserIdAndJwtToken")
	class ValidateUserIdAndJwtToken {

		@Test
		@DisplayName("accepts a token whose user is already cached")
		void acceptsCachedUser() throws Exception {
			when(jwtUtil.validateToken(TOKEN)).thenReturn(claims);
			when(claims.get("userId", String.class)).thenReturn("42");
			when(valueOperations.get("user_42")).thenReturn(user(42L, "asha"));

			assertThat(util.validateUserIdAndJwtToken(TOKEN)).isTrue();
			verify(userRepository, never()).findByUserID(anyLong());
		}

		@Test
		@DisplayName("falls back to the user table and caches what it finds")
		void fallsBackToTheDatabase() throws Exception {
			when(jwtUtil.validateToken(TOKEN)).thenReturn(claims);
			when(claims.get("userId", String.class)).thenReturn("42");
			when(valueOperations.get("user_42")).thenReturn(null);
			when(userRepository.findByUserID(42L)).thenReturn(user(42L, "asha"));

			assertThat(util.validateUserIdAndJwtToken(TOKEN)).isTrue();
			verify(valueOperations).set(eq("user_42"), any(), eq(30L), eq(TimeUnit.MINUTES));
		}

		@Test
		@DisplayName("rejects a token that does not validate")
		void rejectsInvalidToken() {
			when(jwtUtil.validateToken(TOKEN)).thenReturn(null);

			assertThatExceptionOfType(IEMRException.class)
					.isThrownBy(() -> util.validateUserIdAndJwtToken(TOKEN))
					.withMessageContaining("Invalid JWT token");
		}

		@Test
		@DisplayName("rejects a token whose user id matches no user")
		void rejectsUnknownUser() {
			when(jwtUtil.validateToken(TOKEN)).thenReturn(claims);
			when(claims.get("userId", String.class)).thenReturn("42");
			when(valueOperations.get("user_42")).thenReturn(null);
			when(userRepository.findByUserID(42L)).thenReturn(null);

			assertThatExceptionOfType(IEMRException.class)
					.isThrownBy(() -> util.validateUserIdAndJwtToken(TOKEN))
					.withMessageContaining("Invalid User ID");
		}

		@Test
		@DisplayName("rejects a token whose user id is not a number")
		void rejectsNonNumericUserId() {
			when(jwtUtil.validateToken(TOKEN)).thenReturn(claims);
			when(claims.get("userId", String.class)).thenReturn("not-a-number");
			when(valueOperations.get(anyString())).thenReturn(null);

			assertThatExceptionOfType(IEMRException.class)
					.isThrownBy(() -> util.validateUserIdAndJwtToken(TOKEN))
					.withMessageContaining("Validation error");
		}

		@Test
		@DisplayName("rejects a token when the cache lookup itself fails")
		void rejectsWhenCacheFails() {
			when(jwtUtil.validateToken(TOKEN)).thenReturn(claims);
			when(claims.get("userId", String.class)).thenReturn("42");
			when(valueOperations.get("user_42")).thenThrow(new IllegalStateException("redis down"));

			assertThatExceptionOfType(IEMRException.class)
					.isThrownBy(() -> util.validateUserIdAndJwtToken(TOKEN));
		}
	}
}
