package com.iemr.common.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Date;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Covers issuing, validating and reading back the JWTs used for session handling.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JwtUtilTest {

	private static final String SECRET = "a-test-signing-secret-that-is-long-enough-for-hs512-abcdefghijklmnop";
	private static final long ACCESS_EXPIRATION = 60_000L;
	private static final long REFRESH_EXPIRATION = 600_000L;

	@Mock
	private TokenDenylist tokenDenylist;

	@Mock
	private HttpServletRequest request;

	private JwtUtil jwtUtil;

	@BeforeEach
	void setUp() {
		jwtUtil = new JwtUtil();
		ReflectionTestUtils.setField(jwtUtil, "SECRET_KEY", SECRET);
		ReflectionTestUtils.setField(jwtUtil, "ACCESS_EXPIRATION_TIME", ACCESS_EXPIRATION);
		ReflectionTestUtils.setField(jwtUtil, "REFRESH_EXPIRATION_TIME", REFRESH_EXPIRATION);
		ReflectionTestUtils.setField(jwtUtil, "tokenDenylist", tokenDenylist);
	}

	private static SecretKey signingKey() {
		return Keys.hmacShaKeyFor(SECRET.getBytes());
	}

	/** Signs a token that has already expired, to exercise the expiry branch. */
	private static String expiredToken() {
		return Jwts.builder().subject("asha").claim("userId", "42").claim("token_type", "access")
				.id("expired-jti").issuedAt(new Date(System.currentTimeMillis() - 120_000L))
				.expiration(new Date(System.currentTimeMillis() - 60_000L)).signWith(signingKey()).compact();
	}

	@Nested
	@DisplayName("token issuing")
	class Issuing {

		@Test
		@DisplayName("an access token carries the username, user id and access token type")
		void accessTokenCarriesItsClaims() {
			String token = jwtUtil.generateToken("asha", "42");

			Claims claims = jwtUtil.getAllClaimsFromToken(token);
			assertThat(claims.getSubject()).isEqualTo("asha");
			assertThat(claims.get("userId", String.class)).isEqualTo("42");
			assertThat(claims.get("token_type", String.class)).isEqualTo("access");
			assertThat(claims.getId()).isNotBlank();
			assertThat(claims.getExpiration()).isAfter(new Date());
		}

		@Test
		@DisplayName("a refresh token is marked as a refresh token")
		void refreshTokenIsMarked() {
			Claims claims = jwtUtil.getAllClaimsFromToken(jwtUtil.generateRefreshToken("asha", "42"));

			assertThat(claims.get("token_type", String.class)).isEqualTo("refresh");
		}

		@Test
		@DisplayName("a secure token uses the user id as its subject")
		void secureTokenSubjectIsTheUserId() {
			Claims claims = jwtUtil.getAllClaimsFromToken(jwtUtil.generateSecureToken("42"));

			assertThat(claims.getSubject()).isEqualTo("42");
			assertThat(claims.get("token_type", String.class)).isEqualTo("access");
		}

		@Test
		@DisplayName("a secure refresh token uses the user id as its subject")
		void secureRefreshTokenSubjectIsTheUserId() {
			Claims claims = jwtUtil.getAllClaimsFromToken(jwtUtil.generateSecureRefreshToken("42"));

			assertThat(claims.getSubject()).isEqualTo("42");
			assertThat(claims.get("token_type", String.class)).isEqualTo("refresh");
		}

		@Test
		@DisplayName("each issued token gets its own id")
		void tokenIdsAreUnique() {
			assertThat(jwtUtil.getJtiFromToken(jwtUtil.generateToken("asha", "42")))
					.isNotEqualTo(jwtUtil.getJtiFromToken(jwtUtil.generateToken("asha", "42")));
		}

		@Test
		@DisplayName("issuing fails when no signing secret is configured")
		void failsWithoutASigningSecret() {
			ReflectionTestUtils.setField(jwtUtil, "SECRET_KEY", null);

			assertThatExceptionOfType(IllegalStateException.class)
					.isThrownBy(() -> jwtUtil.generateToken("asha", "42"))
					.withMessageContaining("JWT secret key is not set");
		}

		@Test
		@DisplayName("issuing fails when the signing secret is blank")
		void failsWithABlankSigningSecret() {
			ReflectionTestUtils.setField(jwtUtil, "SECRET_KEY", "");

			assertThatExceptionOfType(IllegalStateException.class)
					.isThrownBy(() -> jwtUtil.generateToken("asha", "42"));
		}

		@Test
		@DisplayName("the configured expirations are reported back")
		void reportsConfiguredExpirations() {
			assertThat(jwtUtil.getAccessTokenExpiration()).isEqualTo(ACCESS_EXPIRATION);
			assertThat(jwtUtil.getRefreshTokenExpiration()).isEqualTo(REFRESH_EXPIRATION);
		}
	}

	@Nested
	@DisplayName("validateToken")
	class Validate {

		@Test
		@DisplayName("accepts a freshly issued token that is not denylisted")
		void acceptsAFreshToken() {
			when(tokenDenylist.isTokenDenylisted(anyString())).thenReturn(false);

			Claims claims = jwtUtil.validateToken(jwtUtil.generateToken("asha", "42"));

			assertThat(claims).isNotNull();
			assertThat(claims.getSubject()).isEqualTo("asha");
		}

		@Test
		@DisplayName("rejects a token whose id has been denylisted")
		void rejectsADenylistedToken() {
			when(tokenDenylist.isTokenDenylisted(anyString())).thenReturn(true);

			assertThat(jwtUtil.validateToken(jwtUtil.generateToken("asha", "42"))).isNull();
		}

		@Test
		@DisplayName("rejects an expired token")
		void rejectsAnExpiredToken() {
			assertThat(jwtUtil.validateToken(expiredToken())).isNull();
		}

		@Test
		@DisplayName("rejects a token signed with a different key")
		void rejectsAForeignSignature() {
			String foreign = Jwts.builder().subject("asha").id("jti")
					.expiration(new Date(System.currentTimeMillis() + 60_000L))
					.signWith(Keys.hmacShaKeyFor("a-completely-different-secret-of-sufficient-length!!".getBytes()))
					.compact();

			assertThat(jwtUtil.validateToken(foreign)).isNull();
		}

		@ParameterizedTest(name = "token \"{0}\"")
		@ValueSource(strings = { "", "not-a-jwt", "a.b.c" })
		@DisplayName("rejects a malformed token")
		void rejectsAMalformedToken(String token) {
			assertThat(jwtUtil.validateToken(token)).isNull();
		}
	}

	@Nested
	@DisplayName("reading claims")
	class ReadingClaims {

		@Test
		@DisplayName("exposes the username, user id and token id")
		void exposesNamedClaims() {
			String token = jwtUtil.generateToken("asha", "42");

			assertThat(jwtUtil.getUsernameFromToken(token)).isEqualTo("asha");
			assertThat(jwtUtil.getUserIdFromToken(token)).isEqualTo("42");
			assertThat(jwtUtil.getJtiFromToken(token)).isNotBlank();
		}

		@Test
		@DisplayName("resolves an arbitrary claim through a resolver function")
		void resolvesAnArbitraryClaim() {
			String token = jwtUtil.generateToken("asha", "42");

			assertThat(jwtUtil.getClaimFromToken(token, Claims::getSubject)).isEqualTo("asha");
		}

		@Test
		@DisplayName("reading claims from an invalid token fails loudly")
		void readingAnInvalidTokenFails() {
			assertThatExceptionOfType(RuntimeException.class)
					.isThrownBy(() -> jwtUtil.getAllClaimsFromToken("not-a-jwt"));
		}
	}

	@Nested
	@DisplayName("reading the caller from a request")
	class FromRequest {

		@Test
		@DisplayName("reads the user id from the Jwttoken header")
		void readsUserIdFromHeader() {
			when(tokenDenylist.isTokenDenylisted(anyString())).thenReturn(false);
			when(request.getHeader("Jwttoken")).thenReturn(jwtUtil.generateToken("asha", "42"));

			assertThat(jwtUtil.getUserIdFromRequest(request)).isEqualTo(42);
		}

		@Test
		@DisplayName("falls back to the Jwttoken cookie when the header is absent")
		void fallsBackToTheCookie() {
			when(tokenDenylist.isTokenDenylisted(anyString())).thenReturn(false);
			when(request.getHeader("Jwttoken")).thenReturn(null);
			when(request.getCookies())
					.thenReturn(new Cookie[] { new Cookie("Jwttoken", jwtUtil.generateToken("asha", "42")) });

			assertThat(jwtUtil.getUserIdFromRequest(request)).isEqualTo(42);
		}

		@Test
		@DisplayName("falls back to the cookie when the header is present but empty")
		void fallsBackWhenHeaderIsEmpty() {
			when(tokenDenylist.isTokenDenylisted(anyString())).thenReturn(false);
			when(request.getHeader("Jwttoken")).thenReturn("");
			when(request.getCookies())
					.thenReturn(new Cookie[] { new Cookie("Jwttoken", jwtUtil.generateToken("asha", "42")) });

			assertThat(jwtUtil.getUserIdFromRequest(request)).isEqualTo(42);
		}

		@Test
		@DisplayName("reads the username from a request")
		void readsUsernameFromRequest() {
			when(tokenDenylist.isTokenDenylisted(anyString())).thenReturn(false);
			when(request.getHeader("Jwttoken")).thenReturn(jwtUtil.generateToken("asha", "42"));

			assertThat(jwtUtil.getUsernameFromRequest(request)).isEqualTo("asha");
		}

		@Test
		@DisplayName("returns nothing when the request carries no token at all")
		void noTokenOnTheRequest() {
			when(request.getHeader("Jwttoken")).thenReturn(null);
			when(request.getCookies()).thenReturn(null);

			assertThat(jwtUtil.getUserIdFromRequest(request)).isNull();
			assertThat(jwtUtil.getUsernameFromRequest(request)).isNull();
		}

		@Test
		@DisplayName("returns nothing when the token on the request is expired")
		void expiredTokenOnTheRequest() {
			when(request.getHeader("Jwttoken")).thenReturn(expiredToken());

			assertThat(jwtUtil.getUserIdFromRequest(request)).isNull();
			assertThat(jwtUtil.getUsernameFromRequest(request)).isNull();
		}

		@Test
		@DisplayName("returns nothing when the token carries no user id")
		void tokenWithoutAUserId() {
			when(tokenDenylist.isTokenDenylisted(anyString())).thenReturn(false);
			String withoutUserId = Jwts.builder().subject("asha").id("jti")
					.expiration(new Date(System.currentTimeMillis() + 60_000L)).signWith(signingKey()).compact();
			when(request.getHeader("Jwttoken")).thenReturn(withoutUserId);

			assertThat(jwtUtil.getUserIdFromRequest(request)).isNull();
		}

		@Test
		@DisplayName("returns nothing when the user id claim is not a number")
		void nonNumericUserId() {
			when(tokenDenylist.isTokenDenylisted(anyString())).thenReturn(false);
			when(request.getHeader("Jwttoken")).thenReturn(jwtUtil.generateToken("asha", "not-a-number"));

			assertThat(jwtUtil.getUserIdFromRequest(request)).isNull();
		}
	}
}
