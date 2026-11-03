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

import java.time.Duration;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.iemr.common.utils.exception.IEMRException;

/**
 * Makes consent configuration saves safe to retry: a repeat with the same Idempotency-Key (same user, same
 * endpoint) gets the original response instead of saving again.
 */
@Component
public class ConsentConfigIdempotency {

	static final String IN_PROGRESS = "IN_PROGRESS";
	private static final String PREFIX = "consent-config:idem:";

	private final StringRedisTemplate redis;
	private final Duration ttl;

	public ConsentConfigIdempotency(StringRedisTemplate redis,
			@Value("${consent-config.idempotency-ttl-hours:24}") long ttlHours) {
		this.redis = redis;
		this.ttl = Duration.ofHours(ttlHours);
	}

	/**
	 * Claims the key. Returns the stored response for a repeat, or empty when the request should run.
	 *
	 * @throws IEMRException when the same request is still being processed
	 */
	public Optional<String> begin(Integer userId, String endpoint, String key) throws IEMRException {
		if (isBlank(key)) {
			return Optional.empty();
		}
		String redisKey = redisKey(userId, endpoint, key);
		if (Boolean.TRUE.equals(redis.opsForValue().setIfAbsent(redisKey, IN_PROGRESS, ttl))) {
			return Optional.empty();
		}
		String stored = redis.opsForValue().get(redisKey);
		if (stored == null || IN_PROGRESS.equals(stored)) {
			throw new IEMRException("Request is already being processed");
		}
		return Optional.of(stored);
	}

	public void complete(Integer userId, String endpoint, String key, String response) {
		if (!isBlank(key)) {
			redis.opsForValue().set(redisKey(userId, endpoint, key), response, ttl);
		}
	}

	public void release(Integer userId, String endpoint, String key) {
		if (!isBlank(key)) {
			redis.delete(redisKey(userId, endpoint, key));
		}
	}

	private static String redisKey(Integer userId, String endpoint, String key) {
		return PREFIX + userId + ":" + endpoint + ":" + key;
	}

	private static boolean isBlank(String key) {
		return key == null || key.isBlank();
	}
}
