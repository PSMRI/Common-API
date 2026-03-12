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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see https://www.gnu.org/licenses/.
 */
package com.iemr.common.service.otp;

import com.iemr.common.exception.OtpRateLimitException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

/**
 * Rate-limits OTP send/resend requests per mobile number using Redis counters.
 *
 * Limits (configurable via properties):
 *   otp.ratelimit.minute-limit  – max OTPs per minute   (default 3)
 *   otp.ratelimit.hour-limit    – max OTPs per hour      (default 10)
 *   otp.ratelimit.day-limit     – max OTPs per day       (default 20)
 *
 * Redis key pattern:
 *   rl:otp:min:{mobNo}:{minuteSlot}   TTL 60 s
 *   rl:otp:hr:{mobNo}:{hourSlot}      TTL 3600 s
 *   rl:otp:day:{mobNo}:{yyyyMMdd}     TTL 86400 s
 */
@Component
public class OtpRateLimiterService {

    private final StringRedisTemplate redis;

    @Value("${otp.ratelimit.enabled:true}")
    private boolean enabled;

    @Value("${otp.ratelimit.minute-limit:3}")
    private int minuteLimit;

    @Value("${otp.ratelimit.hour-limit:10}")
    private int hourLimit;

    @Value("${otp.ratelimit.day-limit:20}")
    private int dayLimit;

    public OtpRateLimiterService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    /**
     * Checks all three rate-limit windows for the given mobile number.
     * Throws {@link OtpRateLimitException} if any limit is exceeded.
     * No-op when otp.ratelimit.enabled=false.
     */
    public void checkRateLimit(String mobNo) {
        if (!enabled) return;
        String today = LocalDate.now(ZoneId.of("Asia/Kolkata"))
                .toString().replaceAll("-", "");                 // yyyyMMdd
        long minuteSlot = System.currentTimeMillis() / 60_000L;
        long hourSlot   = System.currentTimeMillis() / 3_600_000L;

        String minKey  = "rl:otp:min:" + mobNo + ":" + minuteSlot;
        String hourKey = "rl:otp:hr:"  + mobNo + ":" + hourSlot;
        String dayKey  = "rl:otp:day:" + mobNo + ":" + today;

        if (incrementWithExpire(minKey, 60L) > minuteLimit) {
            throw new OtpRateLimitException(
                    "OTP request limit exceeded. Maximum " + minuteLimit + " OTPs allowed per minute. Please try again later.");
        }
        if (incrementWithExpire(hourKey, 3600L) > hourLimit) {
            throw new OtpRateLimitException(
                    "OTP request limit exceeded. Maximum " + hourLimit + " OTPs allowed per hour. Please try again later.");
        }
        if (incrementWithExpire(dayKey, 86400L) > dayLimit) {
            throw new OtpRateLimitException(
                    "OTP request limit exceeded. Maximum " + dayLimit + " OTPs allowed per day. Please try again tomorrow.");
        }
    }

    private long incrementWithExpire(String key, long ttlSeconds) {
        Long value = redis.opsForValue().increment(key, 1L);
        if (value != null && value == 1L) {
            redis.expire(key, ttlSeconds, TimeUnit.SECONDS);
        }
        return value == null ? 0L : value;
    }
}
