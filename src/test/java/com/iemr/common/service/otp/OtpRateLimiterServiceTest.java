package com.iemr.common.service.otp;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import com.iemr.common.exception.OtpRateLimitException;

/**
 * Covers the per-minute, per-hour and per-day OTP request limits.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OtpRateLimiterServiceTest {

	private static final String MOBILE = "9876500000";

	@Mock
	private StringRedisTemplate redis;
	@Mock
	private ValueOperations<String, String> valueOperations;

	private OtpRateLimiterService service;

	@BeforeEach
	void setUp() {
		service = new OtpRateLimiterService(redis);
		ReflectionTestUtils.setField(service, "enabled", true);
		ReflectionTestUtils.setField(service, "minuteLimit", 3);
		ReflectionTestUtils.setField(service, "hourLimit", 10);
		ReflectionTestUtils.setField(service, "dayLimit", 20);
		when(redis.opsForValue()).thenReturn(valueOperations);
	}

	private void counterReturns(String keyPrefix, long count) {
		when(valueOperations.increment(anyString(), anyLong())).thenAnswer(invocation -> {
			String key = invocation.getArgument(0);
			return key.startsWith(keyPrefix) ? count : 1L;
		});
	}

	@Nested
	@DisplayName("within the limits")
	class WithinLimits {

		@Test
		@DisplayName("a request under every limit is allowed")
		void allowsRequest() {
			when(valueOperations.increment(anyString(), anyLong())).thenReturn(1L);

			assertThatCode(() -> service.checkRateLimit(MOBILE)).doesNotThrowAnyException();
		}

		@Test
		@DisplayName("the first request in each window sets that window's expiry")
		void setsExpiryPerWindow() {
			when(valueOperations.increment(anyString(), anyLong())).thenReturn(1L);

			service.checkRateLimit(MOBILE);

			verify(redis).expire(anyString(), eq(60L), eq(TimeUnit.SECONDS));
			verify(redis).expire(anyString(), eq(3600L), eq(TimeUnit.SECONDS));
			verify(redis).expire(anyString(), eq(86400L), eq(TimeUnit.SECONDS));
		}

		@Test
		@DisplayName("a later request in the same window does not reset the expiry")
		void doesNotResetExpiry() {
			when(valueOperations.increment(anyString(), anyLong())).thenReturn(2L);

			service.checkRateLimit(MOBILE);

			verify(redis, never()).expire(anyString(), anyLong(), eq(TimeUnit.SECONDS));
		}

		@Test
		@DisplayName("a request at exactly the limit is still allowed")
		void allowsRequestAtTheLimit() {
			counterReturns("rl:otp:min:", 3L);

			assertThatCode(() -> service.checkRateLimit(MOBILE)).doesNotThrowAnyException();
		}

		@Test
		@DisplayName("a Redis counter that returns nothing is treated as zero")
		void nullCounterIsTreatedAsZero() {
			when(valueOperations.increment(anyString(), anyLong())).thenReturn(null);

			assertThatCode(() -> service.checkRateLimit(MOBILE)).doesNotThrowAnyException();
		}
	}

	@Nested
	@DisplayName("over the limits")
	class OverLimits {

		@Test
		@DisplayName("exceeding the per-minute limit is refused")
		void refusesOverMinuteLimit() {
			counterReturns("rl:otp:min:", 4L);

			assertThatExceptionOfType(OtpRateLimitException.class).isThrownBy(() -> service.checkRateLimit(MOBILE))
					.withMessageContaining("Maximum 3 OTPs allowed per minute");
		}

		@Test
		@DisplayName("exceeding the per-hour limit is refused")
		void refusesOverHourLimit() {
			counterReturns("rl:otp:hr:", 11L);

			assertThatExceptionOfType(OtpRateLimitException.class).isThrownBy(() -> service.checkRateLimit(MOBILE))
					.withMessageContaining("Maximum 10 OTPs allowed per hour");
		}

		@Test
		@DisplayName("exceeding the per-day limit is refused")
		void refusesOverDayLimit() {
			counterReturns("rl:otp:day:", 21L);

			assertThatExceptionOfType(OtpRateLimitException.class).isThrownBy(() -> service.checkRateLimit(MOBILE))
					.withMessageContaining("Maximum 20 OTPs allowed per day");
		}
	}

	@Test
	@DisplayName("no counting happens when rate limiting is switched off")
	void disabledSkipsCounting() {
		ReflectionTestUtils.setField(service, "enabled", false);

		assertThatCode(() -> service.checkRateLimit(MOBILE)).doesNotThrowAnyException();
		verifyNoInteractions(valueOperations);
	}

	@Test
	@DisplayName("two mobile numbers are counted separately")
	void countsPerMobileNumber() {
		when(valueOperations.increment(anyString(), anyLong())).thenAnswer(invocation -> {
			String key = invocation.getArgument(0);
			return key.contains(MOBILE) ? 4L : 1L;
		});

		assertThatExceptionOfType(OtpRateLimitException.class).isThrownBy(() -> service.checkRateLimit(MOBILE));
		assertThatCode(() -> service.checkRateLimit("9000000000")).doesNotThrowAnyException();
	}
}
