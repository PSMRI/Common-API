package com.iemr.common.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Covers the Redis-backed rate limiter in front of the public platform feedback endpoint.
 *
 * <p>The limiter counts per-minute and per-day requests for a hashed client IP, counts
 * per-day requests for an identified user, and escalates repeat offenders into a fixed
 * backoff window. Counter values are driven directly through the Redis mock so each
 * threshold can be crossed deterministically.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PlatformFeedbackRateLimitFilterTest {

	private static final int MINUTE_LIMIT = 10;
	private static final int DAY_LIMIT = 100;
	private static final int USER_DAY_LIMIT = 50;
	private static final long BACKOFF_SECONDS = 15 * 60L;

	@Mock
	private StringRedisTemplate redis;
	@Mock
	private ValueOperations<String, String> valueOperations;
	@Mock
	private Environment environment;
	@Mock
	private HttpServletRequest request;
	@Mock
	private HttpServletResponse response;
	@Mock
	private FilterChain filterChain;

	private StringWriter responseBody;
	private PlatformFeedbackRateLimitFilter filter;

	@BeforeEach
	void setUp() throws Exception {
		when(environment.getProperty("platform.feedback.pepper", "")).thenReturn("a-pepper");
		when(environment.getProperty("platform.feedback.trust-forwarded-for", "true")).thenReturn("true");
		when(environment.getProperty("platform.feedback.forwarded-for-header", "X-Forwarded-For"))
				.thenReturn("X-Forwarded-For");
		when(redis.opsForValue()).thenReturn(valueOperations);

		responseBody = new StringWriter();
		when(response.getWriter()).thenReturn(new PrintWriter(responseBody));

		when(request.getMethod()).thenReturn("POST");
		when(request.getRequestURI()).thenReturn("/platform-feedback");
		when(request.getRemoteAddr()).thenReturn("203.0.113.7");

		filter = new PlatformFeedbackRateLimitFilter(redis, environment);
	}

	/** Counters return 1 (first hit in the window) unless a specific key is told otherwise. */
	private void countersReturn(long defaultCount) {
		when(valueOperations.increment(anyString(), anyLong())).thenReturn(defaultCount);
	}

	private void counterReturns(String keyPrefix, long count) {
		when(valueOperations.increment(anyString(), anyLong())).thenAnswer(invocation -> {
			String key = invocation.getArgument(0);
			return key.startsWith(keyPrefix) ? count : 1L;
		});
	}

	private void invoke() throws Exception {
		ReflectionTestUtils.invokeMethod(filter, "doFilterInternal", request, response, filterChain);
	}

	@Nested
	@DisplayName("which requests are filtered")
	class Applicability {

		@Test
		@DisplayName("a POST to the feedback endpoint is rate limited")
		void filtersFeedbackPost() {
			assertThat(shouldNotFilter()).isFalse();
		}

		@Test
		@DisplayName("a POST to a feedback sub-path is rate limited")
		void filtersFeedbackSubPath() {
			when(request.getRequestURI()).thenReturn("/platform-feedback/submit");

			assertThat(shouldNotFilter()).isFalse();
		}

		@ParameterizedTest(name = "{0} is not rate limited")
		@ValueSource(strings = { "GET", "PUT", "DELETE" })
		@DisplayName("only POST requests are rate limited")
		void ignoresOtherMethods(String method) {
			when(request.getMethod()).thenReturn(method);

			assertThat(shouldNotFilter()).isTrue();
		}

		@ParameterizedTest(name = "{0} is not rate limited")
		@ValueSource(strings = { "/user/userAuthenticate", "/platform-feedbackish", "/health" })
		@DisplayName("other endpoints are not rate limited")
		void ignoresOtherPaths(String path) {
			when(request.getRequestURI()).thenReturn(path);

			assertThat(shouldNotFilter()).isTrue();
		}

		@Test
		@DisplayName("a request with no URI is not rate limited")
		void ignoresMissingUri() {
			when(request.getRequestURI()).thenReturn(null);

			assertThat(shouldNotFilter()).isTrue();
		}

		private boolean shouldNotFilter() {
			return Boolean.TRUE.equals(ReflectionTestUtils.invokeMethod(filter, "shouldNotFilter", request));
		}
	}

	@Nested
	@DisplayName("within the limits")
	class WithinLimits {

		@Test
		@DisplayName("a request under every limit is passed on")
		void passesRequestOn() throws Exception {
			countersReturn(1L);

			invoke();

			verify(filterChain).doFilter(request, response);
			verify(response, never()).setStatus(429);
		}

		@Test
		@DisplayName("the first hit in a window sets the key expiry")
		void setsExpiryOnFirstHit() throws Exception {
			countersReturn(1L);

			invoke();

			verify(redis).expire(anyString(), eq(60L), eq(TimeUnit.SECONDS));
		}

		@Test
		@DisplayName("a later hit in the same window does not reset the expiry")
		void doesNotResetExpiry() throws Exception {
			countersReturn(2L);

			invoke();

			verify(redis, never()).expire(anyString(), eq(60L), eq(TimeUnit.SECONDS));
			verify(filterChain).doFilter(request, response);
		}

		@Test
		@DisplayName("an identified user under the user limit is passed on")
		void passesIdentifiedUserOn() throws Exception {
			countersReturn(1L);
			when(request.getHeader("X-User-Id")).thenReturn("42");

			invoke();

			verify(filterChain).doFilter(request, response);
		}

		@Test
		@DisplayName("a request with no resolvable client IP is passed on unchecked")
		void passesOnWhenIpUnknown() throws Exception {
			when(request.getRemoteAddr()).thenReturn("");

			invoke();

			verify(filterChain).doFilter(request, response);
			verify(valueOperations, never()).increment(anyString(), anyLong());
		}
	}

	@Nested
	@DisplayName("over the limits")
	class OverLimits {

		@Test
		@DisplayName("exceeding the per-minute limit is refused with 429")
		void refusesOverMinuteLimit() throws Exception {
			counterReturns("rl:fb:min:", MINUTE_LIMIT + 1);
			when(redis.getExpire(anyString(), eq(TimeUnit.SECONDS))).thenReturn(30L);

			invoke();

			verify(response).setStatus(429);
			verify(response).setHeader("Retry-After", "30");
			verify(filterChain, never()).doFilter(any(), any());
			assertThat(responseBody.toString()).contains("RATE_LIMITED").contains("\"retryAfter\":30");
		}

		@Test
		@DisplayName("exceeding the per-day limit is refused with 429")
		void refusesOverDayLimit() throws Exception {
			counterReturns("rl:fb:day:", DAY_LIMIT + 1);
			when(redis.getExpire(anyString(), eq(TimeUnit.SECONDS))).thenReturn(-1L);

			invoke();

			verify(response).setStatus(429);
			// With no readable TTL the caller is asked to retry after the default minute.
			verify(response).setHeader("Retry-After", "60");
		}

		@Test
		@DisplayName("exceeding the per-user day limit is refused with 429")
		void refusesOverUserDayLimit() throws Exception {
			counterReturns("rl:fb:user:", USER_DAY_LIMIT + 1);
			when(request.getHeader("X-User-Id")).thenReturn("42");

			invoke();

			verify(response).setStatus(429);
			verify(filterChain, never()).doFilter(any(), any());
		}

		@Test
		@DisplayName("a non-numeric user id is ignored and only the IP limits apply")
		void ignoresNonNumericUserId() throws Exception {
			countersReturn(1L);
			when(request.getHeader("X-User-Id")).thenReturn("not-a-number");

			invoke();

			verify(filterChain).doFilter(request, response);
		}
	}

	@Nested
	@DisplayName("backoff for repeat offenders")
	class Backoff {

		@Test
		@DisplayName("repeated breaches put the client into a backoff window")
		void entersBackoffAfterRepeatedBreaches() throws Exception {
			when(valueOperations.increment(anyString(), anyLong())).thenAnswer(invocation -> {
				String key = invocation.getArgument(0);
				if (key.startsWith("rl:fb:min:")) {
					return (long) MINUTE_LIMIT + 1;
				}
				return key.startsWith("rl:fb:fail:") ? 3L : 1L;
			});

			invoke();

			verify(valueOperations).set(anyString(), eq("1"), eq(BACKOFF_SECONDS), eq(TimeUnit.SECONDS));
			verify(response).setHeader("Retry-After", String.valueOf(BACKOFF_SECONDS));
		}

		@Test
		@DisplayName("the first breach starts the failure counter window")
		void firstBreachStartsFailureWindow() throws Exception {
			when(valueOperations.increment(anyString(), anyLong())).thenAnswer(invocation -> {
				String key = invocation.getArgument(0);
				return key.startsWith("rl:fb:min:") ? (long) MINUTE_LIMIT + 1 : 1L;
			});

			invoke();

			verify(redis).expire(anyString(), eq(300L), eq(TimeUnit.SECONDS));
		}

		@Test
		@DisplayName("a client already in backoff is refused without touching the counters")
		void refusesClientAlreadyInBackoff() throws Exception {
			when(redis.getExpire(anyString(), eq(TimeUnit.SECONDS))).thenAnswer(invocation -> {
				String key = invocation.getArgument(0);
				return key.startsWith("rl:fb:backoff:") ? 600L : -1L;
			});

			invoke();

			verify(response).setStatus(429);
			verify(response).setHeader("Retry-After", "600");
			verify(valueOperations, never()).increment(anyString(), anyLong());
			verify(filterChain, never()).doFilter(any(), any());
		}
	}

	@Nested
	@DisplayName("client IP resolution")
	class ClientIpResolution {

		@Test
		@DisplayName("the first entry of the forwarded-for header is used when trusted")
		void usesForwardedForWhenTrusted() throws Exception {
			countersReturn(1L);
			when(request.getHeader("X-Forwarded-For")).thenReturn("198.51.100.9, 203.0.113.7");

			invoke();

			verify(filterChain).doFilter(request, response);
			verify(request, never()).getRemoteAddr();
		}

		@Test
		@DisplayName("the socket address is used when the forwarded-for header is not trusted")
		void usesRemoteAddressWhenNotTrusted() throws Exception {
			when(environment.getProperty("platform.feedback.trust-forwarded-for", "true")).thenReturn("false");
			filter = new PlatformFeedbackRateLimitFilter(redis, environment);
			countersReturn(1L);

			invoke();

			verify(request).getRemoteAddr();
			verify(filterChain).doFilter(request, response);
		}

		@Test
		@DisplayName("the socket address is used when the forwarded-for header is blank")
		void fallsBackForBlankHeader() throws Exception {
			countersReturn(1L);
			when(request.getHeader("X-Forwarded-For")).thenReturn("   ");

			invoke();

			verify(request).getRemoteAddr();
		}

		@Test
		@DisplayName("two different client addresses are counted separately")
		void differentAddressesUseDifferentKeys() throws Exception {
			countersReturn(1L);
			when(request.getHeader("X-Forwarded-For")).thenReturn("198.51.100.9");
			invoke();

			org.mockito.ArgumentCaptor<String> keys = org.mockito.ArgumentCaptor.forClass(String.class);
			verify(valueOperations, org.mockito.Mockito.atLeastOnce()).increment(keys.capture(), anyLong());
			String firstMinuteKey = keys.getAllValues().stream().filter(k -> k.startsWith("rl:fb:min:")).findFirst()
					.orElseThrow();

			org.mockito.Mockito.reset(valueOperations);
			when(valueOperations.increment(anyString(), anyLong())).thenReturn(1L);
			when(request.getHeader("X-Forwarded-For")).thenReturn("198.51.100.10");
			invoke();

			org.mockito.ArgumentCaptor<String> otherKeys = org.mockito.ArgumentCaptor.forClass(String.class);
			verify(valueOperations, org.mockito.Mockito.atLeastOnce()).increment(otherKeys.capture(), anyLong());
			String secondMinuteKey = otherKeys.getAllValues().stream().filter(k -> k.startsWith("rl:fb:min:"))
					.findFirst().orElseThrow();

			assertThat(secondMinuteKey).isNotEqualTo(firstMinuteKey);
		}
	}

	@Test
	@DisplayName("a Redis counter that returns nothing is treated as zero")
	void nullCounterIsTreatedAsZero() throws Exception {
		when(valueOperations.increment(anyString(), anyLong())).thenReturn(null);

		invoke();

		verify(filterChain).doFilter(request, response);
	}
}
