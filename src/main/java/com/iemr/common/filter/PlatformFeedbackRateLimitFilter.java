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
package com.iemr.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Base64;
import java.util.concurrent.TimeUnit;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;


@Component
@ConditionalOnProperty(prefix = "platform.feedback.ratelimit", name = "enabled", havingValue = "true", matchIfMissing = false)
@Order(Ordered.HIGHEST_PRECEDENCE + 10) // run early (adjust order as needed)
public class PlatformFeedbackRateLimitFilter extends OncePerRequestFilter {

    private final StringRedisTemplate redis;
    private final String pepper;
    private final boolean trustForwardedFor;
    private final String forwardedForHeader;

    // Limits & TTLs (tweak if needed)
    private static final int MINUTE_LIMIT = 10;
    private static final int DAY_LIMIT = 100;
    private static final int USER_DAY_LIMIT = 50; // for identified users
    private static final Duration MINUTE_WINDOW = Duration.ofMinutes(1);
    private static final Duration DAY_WINDOW = Duration.ofHours(48); // keep key TTL ~48h
    private static final Duration FAIL_COUNT_WINDOW = Duration.ofMinutes(5);
    private static final int FAILS_TO_BACKOFF = 3;
    private static final Duration BACKOFF_WINDOW = Duration.ofMinutes(15);

    public PlatformFeedbackRateLimitFilter(StringRedisTemplate redis,
                                           org.springframework.core.env.Environment env) {
        this.redis = redis;
        this.pepper = env.getProperty("platform.feedback.pepper", "");
        this.trustForwardedFor = Boolean.parseBoolean(env.getProperty("platform.feedback.trust-forwarded-for", "true"));
        this.forwardedForHeader = env.getProperty("platform.feedback.forwarded-for-header", "X-Forwarded-For");
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        // Only filter specific endpoints (POST to platform-feedback). Keep it narrow.
        String path = request.getRequestURI();
        String method = request.getMethod();
        // adjust path as needed (supports /common-api/platform-feedback and subpaths)
        return !("POST".equalsIgnoreCase(method) && path != null && path.matches("^/platform-feedback(?:/.*)?$"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // compute day key
        String clientIp = extractClientIp(request);
        if (clientIp == null || clientIp.isBlank()) {
            // If we can't identify an IP, be conservative and allow but log (or optionally block)
            filterChain.doFilter(request, response);
            return;
        }

        String today = LocalDate.now(ZoneId.of("Asia/Kolkata")).toString().replaceAll("-", ""); // yyyyMMdd
        String ipHash = sha256Base64(clientIp + pepper + today); // base64 shorter storage; not reversible without pepper
        String minKey = "rl:fb:min:" + ipHash + ":" + (System.currentTimeMillis() / 60000L); // minute-slotted
        String dayKey = "rl:fb:day:" + today + ":" + ipHash;
        String failKey = "rl:fb:fail:" + ipHash;
        String backoffKey = "rl:fb:backoff:" + ipHash;

        // If under backoff -> respond 429 with Retry-After = TTL
        Long backoffTtl = getTtlSeconds(backoffKey);
        if (backoffTtl != null && backoffTtl > 0) {
            sendTooMany(response, backoffTtl);
            return;
        }

        // Minute window check (INCR + TTL if first)
        long minuteCount = incrementWithExpire(minKey, 1, MINUTE_WINDOW.getSeconds());
        if (minuteCount > MINUTE_LIMIT) {
            handleFailureAndMaybeBackoff(failKey, backoffKey, response, minKey, dayKey);
            return;
        }

        // Day window check
        long dayCount = incrementWithExpire(dayKey, 1, DAY_WINDOW.getSeconds());
        if (dayCount > DAY_LIMIT) {
            handleFailureAndMaybeBackoff(failKey, backoffKey, response, minKey, dayKey);
            return;
        }

        // Optional: per-user daily cap if we can extract an authenticated user id from header/jwt
        Integer userId = extractUserIdFromRequest(request); // implement extraction as per your JWT scheme
        if (userId != null) {
            String userDayKey = "rl:fb:user:" + today + ":" + userId;
            long ucount = incrementWithExpire(userDayKey, 1, DAY_WINDOW.getSeconds());
            if (ucount > USER_DAY_LIMIT) {
                handleFailureAndMaybeBackoff(failKey, backoffKey, response, minKey, userDayKey);
                return;
            }
        }

        // All checks passed — proceed to controller
        filterChain.doFilter(request, response);
    }

    // increments key by delta; sets TTL when key is new (INCR returns 1)
    private long incrementWithExpire(String key, long delta, long ttlSeconds) {
        Long value = redis.opsForValue().increment(key, delta);
        if (value != null && value == 1L) {
            redis.expire(key, ttlSeconds, TimeUnit.SECONDS);
        }
        return value == null ? 0L : value;
    }

    private void handleFailureAndMaybeBackoff(String failKey, String backoffKey, HttpServletResponse response, String trigKey, String dayKey) throws IOException {
        // increment fail counter and possibly set backoff
        Long fails = redis.opsForValue().increment(failKey, 1);
        if (fails != null && fails == 1L) {
            redis.expire(failKey, FAIL_COUNT_WINDOW.getSeconds(), TimeUnit.SECONDS);
        }
        if (fails != null && fails >= FAILS_TO_BACKOFF) {
            // set backoff flag
            redis.opsForValue().set(backoffKey, "1", BACKOFF_WINDOW.getSeconds(), TimeUnit.SECONDS);
            sendTooMany(response, BACKOFF_WINDOW.getSeconds());
            return;
        }

        // otherwise respond with Retry-After for the triggering key TTL (minute/day)
        Long retryAfter = getTtlSeconds(trigKey);
        if (retryAfter == null || retryAfter <= 0) retryAfter = 60L;
        sendTooMany(response, retryAfter);
    }

    private void sendTooMany(HttpServletResponse response, long retryAfterSeconds) throws IOException {
        response.setStatus(429);
        response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        String body = String.format("{\"code\":\"RATE_LIMITED\",\"message\":\"Too many requests\",\"retryAfter\":%d}", retryAfterSeconds);
        response.getWriter().write(body);
    }

    private Long getTtlSeconds(String key) {
        Long ttl = redis.getExpire(key, TimeUnit.SECONDS);
        return ttl == null || ttl < 0 ? null : ttl;
    }

    private String extractClientIp(HttpServletRequest request) {
        if (trustForwardedFor) {
            String header = request.getHeader(forwardedForHeader);
            if (StringUtils.hasText(header)) {
                // X-Forwarded-For may contain comma-separated list; take the first (client) entry
                String[] parts = header.split(",");
                if (parts.length > 0) {
                    String ip = parts[0].trim();
                    if (StringUtils.hasText(ip)) return ip;
                }
            }
        }
        return request.getRemoteAddr();
    }

    private Integer extractUserIdFromRequest(HttpServletRequest request) {
        // implement based on how you propagate JWT or user info.
        // Example: if your gateway injects header X-User-Id for authenticated requests:
        String s = request.getHeader("X-User-Id");
        if (StringUtils.hasText(s)) {
            try { return Integer.valueOf(s); } catch (NumberFormatException ignored) {}
        }
        // If JWT parsing required, do it here, but keep this filter light — prefer upstream auth filter to populate a header.
        return null;
    }

    private static String sha256Base64(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            // base64 url-safe or normal base64 — either is fine; base64 is shorter than hex
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hashed);
        } catch (Exception ex) {
            throw new RuntimeException("sha256 failure", ex);
        }
    }
}
