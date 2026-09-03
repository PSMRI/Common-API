package com.iemr.common.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.Cookie;

/**
 * Covers the outbound request header builder that forwards the caller's credentials.
 *
 * <p>The helper reads the inbound request off the thread-local request context, so each
 * test installs its own request and clears it afterwards.
 */
class RestTemplateUtilTest {

	private static final String AUTHORIZATION = "a-session-key";
	private static final String JWT_HEADER = "Jwttoken";

	@AfterEach
	void clearRequestContext() {
		RequestContextHolder.resetRequestAttributes();
		UserAgentContext.clear();
	}

	private MockHttpServletRequest installRequest() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
		return request;
	}

	@Nested
	@DisplayName("createRequestEntity")
	class CreateRequestEntity {

		@Test
		@DisplayName("carries the body and credentials when there is no inbound request")
		void worksOutsideARequest() {
			HttpEntity<Object> entity = RestTemplateUtil.createRequestEntity("{\"a\":1}", AUTHORIZATION);

			assertThat(entity.getBody()).isEqualTo("{\"a\":1}");
			assertThat(entity.getHeaders().getFirst(HttpHeaders.AUTHORIZATION)).isEqualTo(AUTHORIZATION);
			assertThat(entity.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE))
					.isEqualTo("application/json;charset=utf-8");
		}

		@Test
		@DisplayName("forwards the inbound JWT header as both a header and a cookie")
		void forwardsJwtHeader() {
			installRequest().addHeader(JWT_HEADER, "a-jwt-token");

			HttpEntity<Object> entity = RestTemplateUtil.createRequestEntity("body", AUTHORIZATION);

			assertThat(entity.getHeaders().getFirst(JWT_HEADER)).isEqualTo("a-jwt-token");
			assertThat(entity.getHeaders().get(HttpHeaders.COOKIE)).contains("Jwttoken=a-jwt-token");
		}

		@Test
		@DisplayName("forwards the inbound JWT cookie")
		void forwardsJwtCookie() {
			installRequest().setCookies(new Cookie(JWT_HEADER, "cookie-token"));

			HttpEntity<Object> entity = RestTemplateUtil.createRequestEntity("body", AUTHORIZATION);

			assertThat(entity.getHeaders().get(HttpHeaders.COOKIE)).contains("Jwttoken=cookie-token");
		}

		@Test
		@DisplayName("forwards the recorded user agent")
		void forwardsUserAgent() {
			installRequest();
			UserAgentContext.setUserAgent("okhttp/4.9.0");

			HttpEntity<Object> entity = RestTemplateUtil.createRequestEntity("body", AUTHORIZATION);

			assertThat(entity.getHeaders().getFirst(HttpHeaders.USER_AGENT)).isEqualTo("okhttp/4.9.0");
		}

		@Test
		@DisplayName("sends no JWT credentials when the caller presented none")
		void noJwtCredentialsToForward() {
			installRequest();

			HttpEntity<Object> entity = RestTemplateUtil.createRequestEntity("body", AUTHORIZATION);

			assertThat(entity.getHeaders().get(HttpHeaders.COOKIE)).isNull();
			assertThat(entity.getHeaders().getFirst(JWT_HEADER)).isNull();
		}
	}

	@Nested
	@DisplayName("getJwttokenFromHeaders")
	class GetJwtTokenFromHeaders {

		@Test
		@DisplayName("leaves the headers untouched when there is no inbound request")
		void noOpOutsideARequest() {
			HttpHeaders headers = new HttpHeaders();

			RestTemplateUtil.getJwttokenFromHeaders(headers);

			assertThat(headers).isEmpty();
		}

		@Test
		@DisplayName("adds a JSON content type when none is set")
		void addsContentType() {
			installRequest();
			HttpHeaders headers = new HttpHeaders();

			RestTemplateUtil.getJwttokenFromHeaders(headers);

			assertThat(headers.getFirst(HttpHeaders.CONTENT_TYPE)).isEqualTo("application/json;charset=utf-8");
		}

		@Test
		@DisplayName("leaves an existing content type alone")
		void keepsExistingContentType() {
			installRequest();
			HttpHeaders headers = new HttpHeaders();
			headers.add(HttpHeaders.CONTENT_TYPE, "application/xml");

			RestTemplateUtil.getJwttokenFromHeaders(headers);

			assertThat(headers.get(HttpHeaders.CONTENT_TYPE)).containsExactly("application/xml");
		}

		@Test
		@DisplayName("prefers the JWT cookie over the JWT header")
		void prefersTheCookie() {
			MockHttpServletRequest request = installRequest();
			request.setCookies(new Cookie(JWT_HEADER, "cookie-token"));
			request.addHeader(JWT_HEADER, "header-token");
			HttpHeaders headers = new HttpHeaders();

			RestTemplateUtil.getJwttokenFromHeaders(headers);

			assertThat(headers.get(HttpHeaders.COOKIE)).containsExactly("Jwttoken=cookie-token");
			assertThat(headers.getFirst(JWT_HEADER)).isNull();
		}

		@Test
		@DisplayName("falls back to the JWT header when there is no cookie")
		void fallsBackToTheHeader() {
			installRequest().addHeader(JWT_HEADER, "header-token");
			HttpHeaders headers = new HttpHeaders();

			RestTemplateUtil.getJwttokenFromHeaders(headers);

			assertThat(headers.getFirst(JWT_HEADER)).isEqualTo("header-token");
			assertThat(headers.get(HttpHeaders.COOKIE)).containsExactly("Jwttoken=header-token");
		}

		@Test
		@DisplayName("adds the recorded user agent once")
		void addsUserAgentOnce() {
			installRequest();
			UserAgentContext.setUserAgent("okhttp/4.9.0");
			HttpHeaders headers = new HttpHeaders();
			headers.add(HttpHeaders.USER_AGENT, "already-set");

			RestTemplateUtil.getJwttokenFromHeaders(headers);

			assertThat(headers.get(HttpHeaders.USER_AGENT)).containsExactly("already-set");
		}

		@Test
		@DisplayName("adds the recorded user agent when none is set")
		void addsUserAgent() {
			installRequest();
			UserAgentContext.setUserAgent("okhttp/4.9.0");
			HttpHeaders headers = new HttpHeaders();

			RestTemplateUtil.getJwttokenFromHeaders(headers);

			assertThat(headers.getFirst(HttpHeaders.USER_AGENT)).isEqualTo("okhttp/4.9.0");
		}
	}

	@Test
	@DisplayName("the helper can be constructed")
	void isConstructible() {
		assertThat(new RestTemplateUtil()).isNotNull();
	}
}
