package com.iemr.common.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

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

import com.iemr.common.utils.exception.IEMRException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Covers the request filter that enforces CORS origins and JWT authentication.
 *
 * <p>The filter has three jobs: reject requests from origins that are not configured,
 * answer CORS preflights, and require a valid token on everything except an explicit
 * allow-list of public paths. Each of those decisions is asserted through the effect on
 * the response and on whether the chain was allowed to continue.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JwtUserIdValidationFilterTest {

	private static final String ALLOWED_ORIGINS = "https://amrit.example.org, https://*.trusted.example";
	private static final String CONTEXT_PATH = "/commonapi";

	@Mock
	private JwtAuthenticationUtil jwtAuthenticationUtil;
	@Mock
	private HttpServletRequest request;
	@Mock
	private HttpServletResponse response;
	@Mock
	private FilterChain filterChain;

	private JwtUserIdValidationFilter filter;

	@BeforeEach
	void setUp() {
		filter = new JwtUserIdValidationFilter(jwtAuthenticationUtil, ALLOWED_ORIGINS);
		when(request.getContextPath()).thenReturn(CONTEXT_PATH);
		when(request.getMethod()).thenReturn("POST");
	}

	private void requestFor(String path) {
		when(request.getRequestURI()).thenReturn(CONTEXT_PATH + path);
	}

	@Nested
	@DisplayName("origin checks")
	class OriginChecks {

		@Test
		@DisplayName("a request from a configured origin is allowed through with CORS headers")
		void allowsConfiguredOrigin() throws Exception {
			requestFor("/health");
			when(request.getHeader("Origin")).thenReturn("https://amrit.example.org");

			filter.doFilter(request, response, filterChain);

			verify(response).setHeader("Access-Control-Allow-Origin", "https://amrit.example.org");
			verify(response).setHeader("Access-Control-Allow-Credentials", "true");
			verify(filterChain).doFilter(request, response);
		}

		@Test
		@DisplayName("a wildcard pattern matches its subdomains")
		void allowsWildcardSubdomain() throws Exception {
			requestFor("/health");
			when(request.getHeader("Origin")).thenReturn("https://ui.trusted.example");

			filter.doFilter(request, response, filterChain);

			verify(response).setHeader("Access-Control-Allow-Origin", "https://ui.trusted.example");
			verify(filterChain).doFilter(request, response);
		}

		@Test
		@DisplayName("a request from an unconfigured origin is refused")
		void refusesUnknownOrigin() throws Exception {
			requestFor("/health");
			when(request.getHeader("Origin")).thenReturn("https://evil.example");

			filter.doFilter(request, response, filterChain);

			verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "Origin not allowed");
			verify(filterChain, never()).doFilter(any(), any());
		}

		@Test
		@DisplayName("a request with no Origin header is allowed to proceed")
		void allowsMissingOrigin() throws Exception {
			requestFor("/health");
			when(request.getHeader("Origin")).thenReturn(null);

			filter.doFilter(request, response, filterChain);

			verify(filterChain).doFilter(request, response);
		}

		@Test
		@DisplayName("every origin is refused when none is configured")
		void refusesEverythingWhenNoOriginsConfigured() throws Exception {
			filter = new JwtUserIdValidationFilter(jwtAuthenticationUtil, "   ");
			requestFor("/health");
			when(request.getHeader("Origin")).thenReturn("https://amrit.example.org");

			filter.doFilter(request, response, filterChain);

			verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "Origin not allowed");
		}
	}

	@Nested
	@DisplayName("CORS preflight")
	class Preflight {

		@BeforeEach
		void preflight() {
			when(request.getMethod()).thenReturn("OPTIONS");
			requestFor("/user/getUser");
		}

		@Test
		@DisplayName("a preflight from a configured origin is answered without running the chain")
		void answersPreflight() throws Exception {
			when(request.getHeader("Origin")).thenReturn("https://amrit.example.org");

			filter.doFilter(request, response, filterChain);

			verify(response).setStatus(HttpServletResponse.SC_OK);
			verify(filterChain, never()).doFilter(any(), any());
		}

		@Test
		@DisplayName("a preflight without an Origin header is refused")
		void refusesPreflightWithoutOrigin() throws Exception {
			when(request.getHeader("Origin")).thenReturn(null);

			filter.doFilter(request, response, filterChain);

			verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "OPTIONS request requires Origin header");
		}

		@Test
		@DisplayName("a preflight from an unconfigured origin is refused")
		void refusesPreflightFromUnknownOrigin() throws Exception {
			when(request.getHeader("Origin")).thenReturn("https://evil.example");

			filter.doFilter(request, response, filterChain);

			verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "Origin not allowed");
		}
	}

	@Nested
	@DisplayName("paths that bypass authentication")
	class PublicPaths {

		@ParameterizedTest(name = "{0}")
		@ValueSource(strings = { "/user/userAuthenticate", "/user/logOutUserFromConcurrentSession",
				"/user/refreshToken", "/user/superUserAuthenticate", "/user/userAuthenticateV1",
				"/user/forgetPassword", "/user/setForgetPassword", "/user/changePassword",
				"/user/saveUserSecurityQuesAns", "/user/userLogout", "/user/validateSecurityQuestionAndAnswer",
				"/swagger-ui/index.html", "/v3/api-docs", "/public/anything", "/health", "/version" })
		@DisplayName("is served without a token")
		void bypassesAuthentication(String path) throws Exception {
			requestFor(path);

			filter.doFilter(request, response, filterChain);

			verify(filterChain).doFilter(request, response);
			verify(response, never()).sendError(anyInt(), anyString());
		}

		@Test
		@DisplayName("the video consultation resolve path is served without a token")
		void videoConsultationResolveBypasses() throws Exception {
			requestFor("/video-consultation/resolve");

			filter.doFilter(request, response, filterChain);

			verify(filterChain).doFilter(request, response);
		}

		@Test
		@DisplayName("the platform feedback paths are served without a token and keep their cookies")
		void platformFeedbackBypasses() throws Exception {
			requestFor("/platform-feedback/submit");

			filter.doFilter(request, response, filterChain);

			verify(filterChain).doFilter(request, response);
			verify(response, never()).addCookie(any(Cookie.class));
		}
	}

	@Nested
	@DisplayName("token validation")
	class TokenValidation {

		@Test
		@DisplayName("a valid token in a cookie lets the request continue")
		void acceptsTokenFromCookie() throws Exception {
			requestFor("/user/getUser");
			when(request.getCookies()).thenReturn(new Cookie[] { new Cookie("Jwttoken", "a-token") });
			when(jwtAuthenticationUtil.validateUserIdAndJwtToken("a-token")).thenReturn(true);

			filter.doFilter(request, response, filterChain);

			verify(filterChain).doFilter(any(ServletRequest.class), any());
			verify(response, never()).sendError(anyInt(), anyString());
		}

		@Test
		@DisplayName("a valid token in the JwtToken header lets the request continue")
		void acceptsTokenFromHeader() throws Exception {
			requestFor("/user/getUser");
			when(request.getHeader("JwtToken")).thenReturn("a-token");
			when(jwtAuthenticationUtil.validateUserIdAndJwtToken("a-token")).thenReturn(true);

			filter.doFilter(request, response, filterChain);

			verify(filterChain).doFilter(any(ServletRequest.class), any());
		}

		@Test
		@DisplayName("a request with no token at all is rejected as unauthorized")
		void rejectsMissingToken() throws Exception {
			requestFor("/user/getUser");

			filter.doFilter(request, response, filterChain);

			verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED,
					"Unauthorized: Invalid or missing token");
			verify(filterChain, never()).doFilter(any(), any());
		}

		@Test
		@DisplayName("a cookie token that does not validate is rejected")
		void rejectsInvalidCookieToken() throws Exception {
			requestFor("/user/getUser");
			when(request.getCookies()).thenReturn(new Cookie[] { new Cookie("Jwttoken", "a-token") });
			when(jwtAuthenticationUtil.validateUserIdAndJwtToken("a-token")).thenReturn(false);

			filter.doFilter(request, response, filterChain);

			verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED,
					"Unauthorized: Invalid or missing token");
		}

		@Test
		@DisplayName("a validation failure is reported as an authorization error")
		void reportsValidationFailure() throws Exception {
			requestFor("/user/getUser");
			when(request.getCookies()).thenReturn(new Cookie[] { new Cookie("Jwttoken", "a-token") });
			when(jwtAuthenticationUtil.validateUserIdAndJwtToken("a-token"))
					.thenThrow(new IEMRException("Invalid User ID."));

			filter.doFilter(request, response, filterChain);

			verify(response).sendError(anyInt(), anyString());
		}

		@ParameterizedTest(name = "User-Agent {0}")
		@ValueSource(strings = { "okhttp/4.9.0", "Java/17.0.1" })
		@DisplayName("a mobile client presenting an Authorization header is allowed through")
		void allowsMobileClientWithAuthorizationHeader(String userAgent) throws Exception {
			requestFor("/user/getUser");
			when(request.getHeader("User-Agent")).thenReturn(userAgent);
			when(request.getHeader("Authorization")).thenReturn("Bearer server-token");

			filter.doFilter(request, response, filterChain);

			verify(filterChain).doFilter(request, response);
			verify(response, never()).sendError(anyInt(), anyString());
		}

		@Test
		@DisplayName("a mobile client without an Authorization header is rejected")
		void rejectsMobileClientWithoutAuthorizationHeader() throws Exception {
			requestFor("/user/getUser");
			when(request.getHeader("User-Agent")).thenReturn("okhttp/4.9.0");

			filter.doFilter(request, response, filterChain);

			verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED,
					"Unauthorized: Invalid or missing token");
		}

		@Test
		@DisplayName("a browser client without a token is rejected")
		void rejectsBrowserClientWithoutToken() throws Exception {
			requestFor("/user/getUser");
			when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0");
			when(request.getHeader("Authorization")).thenReturn("Bearer server-token");

			filter.doFilter(request, response, filterChain);

			verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED,
					"Unauthorized: Invalid or missing token");
		}

		@Test
		@DisplayName("the user agent context is cleared after a mobile request")
		void clearsUserAgentContext() throws Exception {
			requestFor("/user/getUser");
			when(request.getHeader("User-Agent")).thenReturn("okhttp/4.9.0");
			when(request.getHeader("Authorization")).thenReturn("Bearer server-token");

			filter.doFilter(request, response, filterChain);

			assertThat(UserAgentContext.getUserAgent()).isNull();
		}
	}

	@Nested
	@DisplayName("userId cookie hygiene")
	class CookieHygiene {

		@Test
		@DisplayName("a userId cookie sent by the client is expired")
		void clearsUserIdCookie() throws Exception {
			requestFor("/user/getUser");
			when(request.getCookies()).thenReturn(new Cookie[] { new Cookie("userId", "42") });

			filter.doFilter(request, response, filterChain);

			verify(response).addCookie(any(Cookie.class));
		}

		@Test
		@DisplayName("no cookie is written when the client sent none")
		void noCookiesToClear() throws Exception {
			requestFor("/user/getUser");
			when(request.getCookies()).thenReturn(null);

			filter.doFilter(request, response, filterChain);

			verify(response, never()).addCookie(any(Cookie.class));
		}

		@Test
		@DisplayName("an unrelated cookie is left alone")
		void leavesOtherCookiesAlone() throws Exception {
			requestFor("/user/getUser");
			when(request.getCookies()).thenReturn(new Cookie[] { new Cookie("theme", "dark") });

			filter.doFilter(request, response, filterChain);

			verify(response, never()).addCookie(any(Cookie.class));
		}
	}
}
