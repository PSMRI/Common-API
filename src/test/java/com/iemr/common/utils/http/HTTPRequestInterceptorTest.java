package com.iemr.common.utils.http;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import com.iemr.common.utils.JwtUtil;
import com.iemr.common.utils.exception.IEMRException;
import com.iemr.common.utils.sessionobject.SessionObject;
import com.iemr.common.utils.validator.Validator;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Covers the interceptor that validates the session key on every authenticated request.
 *
 * <p>Requests with no Authorization header, preflights and the public endpoints listed in
 * the interceptor all pass straight through; anything else has its key checked and, on
 * failure, receives a 401 JSON body with CORS headers only for a configured origin.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class HTTPRequestInterceptorTest {

	private static final String ALLOWED_ORIGINS = "https://amrit.example.org, https://*.trusted.example";

	@Mock
	private Validator validator;
	@Mock
	private SessionObject sessionObject;
	@Mock
	private JwtUtil jwtUtil;
	@Mock
	private HttpServletRequest request;
	@Mock
	private HttpServletResponse response;

	private ByteArrayOutputStream writtenBody;
	private HTTPRequestInterceptor interceptor;

	@BeforeEach
	void setUp() throws IOException {
		interceptor = new HTTPRequestInterceptor();
		interceptor.setValidator(validator);
		interceptor.setSessionObject(sessionObject);
		ReflectionTestUtils.setField(interceptor, "jwtUtil", jwtUtil);
		ReflectionTestUtils.setField(interceptor, "allowedOrigins", ALLOWED_ORIGINS);

		writtenBody = new ByteArrayOutputStream();
		when(response.getOutputStream()).thenReturn(new ServletOutputStream() {
			@Override
			public void write(int b) {
				writtenBody.write(b);
			}

			@Override
			public boolean isReady() {
				return true;
			}

			@Override
			public void setWriteListener(WriteListener listener) {
				// not used by the interceptor
			}
		});

		when(request.getMethod()).thenReturn("POST");
		when(request.getRequestURI()).thenReturn("/commonapi/beneficiary/searchUser");
		when(request.getRemoteAddr()).thenReturn("203.0.113.7");
	}

	@Nested
	@DisplayName("requests that bypass validation")
	class Bypass {

		@ParameterizedTest(name = "Authorization \"{0}\"")
		@NullAndEmptySource
		@DisplayName("a request without an Authorization header passes through")
		void passesThroughWithoutAuthorization(String authorization) throws Exception {
			when(request.getHeader("Authorization")).thenReturn(authorization);

			assertThat(interceptor.preHandle(request, response, new Object())).isTrue();
			verifyNoInteractions(validator);
		}

		@Test
		@DisplayName("a preflight passes through without a key check")
		void passesThroughPreflight() throws Exception {
			when(request.getHeader("Authorization")).thenReturn("a-session-key");
			when(request.getMethod()).thenReturn("OPTIONS");

			assertThat(interceptor.preHandle(request, response, new Object())).isTrue();
			verifyNoInteractions(validator);
		}

		@ParameterizedTest(name = "{0}")
		@ValueSource(strings = { "userAuthenticate", "superUserAuthenticate", "userAuthenticateNew",
				"userAuthenticateV1", "forgetPassword", "setForgetPassword", "changePassword",
				"saveUserSecurityQuesAns", "doAgentLogout", "userLogout", "swagger-ui.html", "index.html",
				"index.css", "swagger-initializer.js", "swagger-config", "swagger-ui-bundle.js", "swagger-ui.css",
				"ui", "swagger-ui-standalone-preset.js", "favicon-32x32.png", "favicon-16x16.png",
				"swagger-resources", "api-docs", "updateBenCallIdsInPhoneBlock", "userAuthenticateByEncryption",
				"sendOTP", "validateOTP", "resendOTP", "validateSecurityQuestionAndAnswer",
				"logOutUserFromConcurrentSession", "refreshToken", "resolve" })
		@DisplayName("a public endpoint passes through without a key check")
		void passesThroughPublicEndpoints(String endpoint) throws Exception {
			when(request.getHeader("Authorization")).thenReturn("a-session-key");
			when(request.getRequestURI()).thenReturn("/commonapi/user/" + endpoint);

			assertThat(interceptor.preHandle(request, response, new Object())).isTrue();
			verify(validator, never()).checkKeyExists(anyString(), anyString());
		}

		@Test
		@DisplayName("the error endpoint is stopped without a key check")
		void stopsTheErrorEndpoint() throws Exception {
			when(request.getHeader("Authorization")).thenReturn("a-session-key");
			when(request.getRequestURI()).thenReturn("/commonapi/error");

			assertThat(interceptor.preHandle(request, response, new Object())).isFalse();
			verify(validator, never()).checkKeyExists(anyString(), anyString());
		}
	}

	@Nested
	@DisplayName("key validation")
	class KeyValidation {

		@Test
		@DisplayName("a valid key lets the request continue")
		void validKeyContinues() throws Exception {
			when(request.getHeader("Authorization")).thenReturn("a-session-key");

			assertThat(interceptor.preHandle(request, response, new Object())).isTrue();
			verify(validator).checkKeyExists("a-session-key", "203.0.113.7");
		}

		@Test
		@DisplayName("a Bearer prefix is stripped before the key is checked")
		void stripsBearerPrefix() throws Exception {
			when(request.getHeader("Authorization")).thenReturn("Bearer a-session-key");

			interceptor.preHandle(request, response, new Object());

			verify(validator).checkKeyExists("a-session-key", "203.0.113.7");
		}

		@Test
		@DisplayName("the forwarded-for address is preferred over the socket address")
		void prefersForwardedForAddress() throws Exception {
			when(request.getHeader("Authorization")).thenReturn("a-session-key");
			when(request.getHeader("X-FORWARDED-FOR")).thenReturn("198.51.100.9");

			interceptor.preHandle(request, response, new Object());

			verify(validator).checkKeyExists("a-session-key", "198.51.100.9");
		}

		@Test
		@DisplayName("a blank forwarded-for header falls back to the socket address")
		void blankForwardedForFallsBack() throws Exception {
			when(request.getHeader("Authorization")).thenReturn("a-session-key");
			when(request.getHeader("X-FORWARDED-FOR")).thenReturn("   ");

			interceptor.preHandle(request, response, new Object());

			verify(validator).checkKeyExists("a-session-key", "203.0.113.7");
		}
	}

	@Nested
	@DisplayName("rejected requests")
	class Rejection {

		@BeforeEach
		void keyCheckFails() throws Exception {
			when(request.getHeader("Authorization")).thenReturn("a-session-key");
			doThrow(new IEMRException("Session expired")).when(validator).checkKeyExists(anyString(), anyString());
		}

		@Test
		@DisplayName("answers 401 with a JSON body naming the failure")
		void answersUnauthorized() throws Exception {
			assertThat(interceptor.preHandle(request, response, new Object())).isFalse();

			verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			assertThat(writtenBody.toString()).contains("\"statusCode\": 401").contains("Session expired");
		}

		@Test
		@DisplayName("adds CORS headers for a configured origin")
		void addsCorsHeadersForConfiguredOrigin() throws Exception {
			when(request.getHeader("Origin")).thenReturn("https://amrit.example.org");

			interceptor.preHandle(request, response, new Object());

			verify(response).setHeader("Access-Control-Allow-Origin", "https://amrit.example.org");
			verify(response).setHeader("Access-Control-Allow-Credentials", "true");
		}

		@Test
		@DisplayName("adds CORS headers for a wildcard subdomain")
		void addsCorsHeadersForWildcardSubdomain() throws Exception {
			when(request.getHeader("Origin")).thenReturn("https://ui.trusted.example");

			interceptor.preHandle(request, response, new Object());

			verify(response).setHeader("Access-Control-Allow-Origin", "https://ui.trusted.example");
		}

		@Test
		@DisplayName("withholds CORS headers from an unconfigured origin")
		void withholdsCorsHeadersFromUnknownOrigin() throws Exception {
			when(request.getHeader("Origin")).thenReturn("https://evil.example");

			interceptor.preHandle(request, response, new Object());

			verify(response, never()).setHeader(anyString(), anyString());
			verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		}

		@Test
		@DisplayName("falls back to a generic message when the failure has none")
		void fallsBackToAGenericMessage() throws Exception {
			doThrow(new IEMRException("")).when(validator).checkKeyExists(anyString(), anyString());

			interceptor.preHandle(request, response, new Object());

			assertThat(writtenBody.toString()).contains("Unauthorized access or session expired.");
		}

		@Test
		@DisplayName("escapes quotes in the failure message")
		void escapesQuotesInTheMessage() throws Exception {
			doThrow(new IEMRException("bad \"key\"")).when(validator).checkKeyExists(anyString(), anyString());

			interceptor.preHandle(request, response, new Object());

			assertThat(writtenBody.toString()).contains("bad \\\"key\\\"");
		}
	}

	@Nested
	@DisplayName("postHandle")
	class PostHandle {

		@Test
		@DisplayName("refreshes the session for an authenticated request")
		void refreshesTheSession() throws Exception {
			when(request.getHeader("Authorization")).thenReturn("a-session-key");
			when(sessionObject.getSessionObject("a-session-key")).thenReturn("the-session");

			interceptor.postHandle(request, response, new Object(), null);

			verify(sessionObject).updateSessionObject("a-session-key", "the-session");
		}

		@Test
		@DisplayName("strips a Bearer prefix before refreshing")
		void stripsBearerPrefix() throws Exception {
			when(request.getHeader("Authorization")).thenReturn("Bearer a-session-key");

			interceptor.postHandle(request, response, new Object(), null);

			verify(sessionObject).updateSessionObject(anyString(), org.mockito.ArgumentMatchers.nullable(String.class));
		}

		@ParameterizedTest(name = "Authorization \"{0}\"")
		@NullAndEmptySource
		@DisplayName("does nothing for a request without an Authorization header")
		void doesNothingWithoutAuthorization(String authorization) throws Exception {
			when(request.getHeader("Authorization")).thenReturn(authorization);

			interceptor.postHandle(request, response, new Object(), null);

			verify(sessionObject, never()).updateSessionObject(anyString(), anyString());
		}

		@Test
		@DisplayName("a session store failure is contained")
		void containsSessionStoreFailures() throws Exception {
			when(request.getHeader("Authorization")).thenReturn("a-session-key");
			when(sessionObject.getSessionObject(anyString())).thenThrow(new IllegalStateException("redis down"));

			interceptor.postHandle(request, response, new Object(), null);

			verify(sessionObject, never()).updateSessionObject(anyString(), anyString());
		}
	}

	@Test
	@DisplayName("afterCompletion does no further work")
	void afterCompletionDoesNothing() throws Exception {
		interceptor.afterCompletion(request, response, new Object(), null);

		verifyNoInteractions(validator, sessionObject);
	}
}
