/*
 * AMRIT – Accessible Medical Records via Integrated Technology
 * Integrated EHR (Electronic Health Records) Solution
 *
 * Copyright (C) "Piramal Swasthya Management and Research Institute"
 *
 * This file is part of AMRIT.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */
package com.iemr.common.service.customization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.iemr.common.utils.exception.IEMRException;

@ExtendWith(MockitoExtension.class)
class ConsentConfigIdempotencyTest {

	private static final String KEY = "consent-config:idem:501:saveConfig:9b2f";
	private static final Duration TTL = Duration.ofHours(24);

	@Mock
	private StringRedisTemplate redis;
	@Mock
	private ValueOperations<String, String> ops;

	private ConsentConfigIdempotency idempotency;

	@BeforeEach
	void setUp() {
		idempotency = new ConsentConfigIdempotency(redis, 24);
	}

	@Test
	@DisplayName("the first request claims the key and runs")
	void firstRequestRuns() throws IEMRException {
		when(redis.opsForValue()).thenReturn(ops);
		when(ops.setIfAbsent(KEY, ConsentConfigIdempotency.IN_PROGRESS, TTL)).thenReturn(true);

		assertThat(idempotency.begin(501, "saveConfig", "9b2f")).isEmpty();
	}

	@Test
	@DisplayName("C18 – same Idempotency-Key returns the original result, no second save")
	void repeatReturnsStoredResult() throws IEMRException {
		when(redis.opsForValue()).thenReturn(ops);
		when(ops.setIfAbsent(KEY, ConsentConfigIdempotency.IN_PROGRESS, TTL)).thenReturn(false);
		when(ops.get(KEY)).thenReturn("{\"version\":1}");

		assertThat(idempotency.begin(501, "saveConfig", "9b2f")).contains("{\"version\":1}");
	}

	@Test
	@DisplayName("a repeat while the first request is still running is refused")
	void repeatInProgressRefused() {
		when(redis.opsForValue()).thenReturn(ops);
		when(ops.setIfAbsent(KEY, ConsentConfigIdempotency.IN_PROGRESS, TTL)).thenReturn(false);
		when(ops.get(KEY)).thenReturn(ConsentConfigIdempotency.IN_PROGRESS);

		assertThatThrownBy(() -> idempotency.begin(501, "saveConfig", "9b2f")).isInstanceOf(IEMRException.class)
				.hasMessage("Request is already being processed");
	}

	@Test
	@DisplayName("a key that expired between the two Redis calls is treated as in progress")
	void vanishedKeyRefused() {
		when(redis.opsForValue()).thenReturn(ops);
		when(ops.setIfAbsent(KEY, ConsentConfigIdempotency.IN_PROGRESS, TTL)).thenReturn(null);
		when(ops.get(KEY)).thenReturn(null);

		assertThatThrownBy(() -> idempotency.begin(501, "saveConfig", "9b2f"))
				.hasMessage("Request is already being processed");
	}

	@Test
	@DisplayName("complete stores the response with the same lifetime; release deletes the key")
	void completeAndRelease() {
		when(redis.opsForValue()).thenReturn(ops);

		idempotency.complete(501, "saveConfig", "9b2f", "{}");
		idempotency.release(501, "saveConfig", "9b2f");

		verify(ops).set(KEY, "{}", TTL);
		verify(redis).delete(KEY);
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = { "   " })
	@DisplayName("without a key nothing touches Redis")
	void noKeyNoRedis(String key) throws IEMRException {
		assertThat(idempotency.begin(501, "saveConfig", key)).isEmpty();
		idempotency.complete(501, "saveConfig", key, "{}");
		idempotency.release(501, "saveConfig", key);

		verifyNoInteractions(redis);
		verify(ops, never()).set(anyString(), anyString(), any(Duration.class));
	}
}
