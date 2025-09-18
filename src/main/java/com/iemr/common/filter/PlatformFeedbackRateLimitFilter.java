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

import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.beans.factory.annotation.Value;

@Component
@ConditionalOnProperty(prefix = "platform.feedback.ratelimit", name = "enabled", havingValue = "true", matchIfMissing = false)
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class PlatformFeedbackRateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(PlatformFeedbackRateLimitFilter.class);

    private final StringRedisTemplate redis;

    @Value("${platform.feedback.ratelimit.pepper:}")
    private String pepper;

    @Value("${platform.feedback.ratelimit.trust-forwarded-for:false}")
    private boolean trustForwardedFor;

    @Value("${platform.feedback.ratelimit.forwarded-for-header:X-Forwarded-For}")
    private String forwardedForHeader;

    @Value("${platform.feedback.ratelimit.minute-limit:10}")
    private int minuteLimit;

    @Value("${platform.feedback.ratelimit.day-limit:100}")
    private int dayLimit;

    @Value("${platform.feedback.ratelimit.user-day-limit:50}")
    private int userDayLimit;

    private final Duration MINUTE_WINDOW = Duration.ofMinutes(1);
    private final Duration DAY_WINDOW = Duration.ofHours(48); 

    @Value("${platform.feedback.ratelimit.fail-window-minutes:5}")
    private long failCountWindowMinutes;

    @Value("${platform.feedback.ratelimit.backoff-minutes:15}")
    private long backoffWindowMinutes;

    @Value("${platform.feedback.ratelimit.fails-to-backoff:3}")
    private int failsToBackoff;

    public PlatformFeedbackRateLimitFilter(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @PostConstruct
    public void validateConfig() {
        if (!StringUtils.hasText(pepper)) {
            throw new IllegalStateException("platform.feedback.ratelimit.pepper must be set");
        }
        if (failCountWindowMinutes <= 0) {
            throw new IllegalStateException("platform.feedback.ratelimit.fail-window-minutes must be > 0");
        }
        if (backoffWindowMinutes <= 0) {
            throw new IllegalStateException("platform.feedback.ratelimit.backoff-minutes must be > 0");
        }
        if (minuteLimit <= 0 || dayLimit <= 0 || userDayLimit <= 0) {
            log.warn("One of the rate limits is non-positive; please check configuration");
        }
        log.info("PlatformFeedbackRateLimitFilter initialized (minuteLimit={}, dayLimit={}, userDayLimit={}, failWindowMinutes={}, backoffMinutes={}, failsToBackoff={})",
                minuteLimit, dayLimit, userDayLimit, failCountWindowMinutes, backoffWindowMinutes, failsToBackoff);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        // Only filter specific endpoints (POST to platform-feedback). Keep it narrow.
        String path = request.getRequestURI();
        String method = request.getMethod();
        // Allow context path prefixes, e.g. /common-api/platform-feedback
        return !("POST".equalsIgnoreCase(method) && path != null && path.matches(".*/platform-feedback(?:/.*)?$"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String clientIp = extractClientIp(request);
        if (clientIp == null || clientIp.isBlank()) {
            log.debug("Client IP could not be determined; allowing request (fail-open). RequestURI={}", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        String today = LocalDate.now(ZoneId.of("Asia/Kolkata")).toString().replaceAll("-", ""); // yyyyMMdd
        String ipHash = sha256Base64(clientIp + pepper + today); // base64 shorter storage; not reversible without pepper
        String minKey = "rl:fb:min:" + ipHash + ":" + (System.currentTimeMillis() / 60000L); // minute-slotted
        String dayKey = "rl:fb:day:" + today + ":" + ipHash;
        String failKey = "rl:fb:fail:" + ipHash;
        String backoffKey = "rl:fb:backoff:" + ipHash;

        Long backoffTtl = getTtlSeconds(backoffKey);
        if (backoffTtl != null && backoffTtl > 0) {
            log.debug("IP in backoff (ipHash={}, ttl={})", ipHash, backoffTtl);
            sendTooMany(response, backoffTtl);
            return;
        }

        long minuteCount = incrementWithExpire(minKey, 1, MINUTE_WINDOW.getSeconds());
        if (minuteCount > minuteLimit) {
            log.info("Minute limit hit for ipHash={} minuteCount={}", ipHash, minuteCount);
            handleFailureAndMaybeBackoff(failKey, backoffKey, response, minKey, dayKey);
            return;
        }

        long dayCount = incrementWithExpire(dayKey, 1, DAY_WINDOW.getSeconds());
        if (dayCount > dayLimit) {
            log.info("Day limit hit for ipHash={} dayCount={}", ipHash, dayCount);
            handleFailureAndMaybeBackoff(failKey, backoffKey, response, minKey, dayKey);
            return;
        }

        Integer userId = extractUserIdFromRequest(request); // implement extraction as per your JWT scheme
        if (userId != null) {
            String userDayKey = "rl:fb:user:" + today + ":" + userId;
            long ucount = incrementWithExpire(userDayKey, 1, DAY_WINDOW.getSeconds());
            if (ucount > userDayLimit) {
                log.info("User day limit hit for userId={} count={}", userId, ucount);
                handleFailureAndMaybeBackoff(failKey, backoffKey, response, minKey, userDayKey);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private long incrementWithExpire(String key, long delta, long ttlSeconds) {
        try {
            Long value = redis.opsForValue().increment(key, delta);
            if (value != null && value == 1L) {
                redis.expire(key, ttlSeconds, TimeUnit.SECONDS);
            }
            return value == null ? 0L : value;
        } catch (Exception ex) {
            log.error("Redis increment failed for key={} delta={} - failing open (allow request). Exception: {}", key, delta, ex.toString());
            return 0L;
        }
    }

    private void handleFailureAndMaybeBackoff(String failKey, String backoffKey, HttpServletResponse response, String trigKey, String dayKey) throws IOException {
        try {
            Long fails = redis.opsForValue().increment(failKey, 1);
            if (fails != null && fails == 1L) {
                redis.expire(failKey, getFailCountWindowSeconds(), TimeUnit.SECONDS);
            }
            log.debug("Fail counter for key {} is {}", failKey, fails);

            if (fails != null && fails >= failsToBackoff) {
                long backoffSeconds = getBackoffWindowSeconds();
                redis.opsForValue().set(backoffKey, "1", backoffSeconds, TimeUnit.SECONDS);
                log.info("Entering backoff for ip (backoffKey={}, backoffSeconds={})", backoffKey, backoffSeconds);
                sendTooMany(response, backoffSeconds);
                return;
            }

            Long retryAfter = getTtlSeconds(trigKey);
            if (retryAfter == null || retryAfter <= 0) retryAfter = 60L;
            log.debug("Responding rate-limited with Retry-After={} for key={}", retryAfter, trigKey);
            sendTooMany(response, retryAfter);
        } catch (Exception ex) {
            log.error("Error while handling failure/backoff; failing open and allowing request. Exception: {}", ex.toString());
        }
    }

    private void sendTooMany(HttpServletResponse response, long retryAfterSeconds) throws IOException {
        response.setStatus(429);
        response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        String body = String.format("{\"code\":\"RATE_LIMITED\",\"message\":\"Too many requests\",\"retryAfter\":%d}", retryAfterSeconds);
        response.getWriter().write(body);
    }

    private Long getTtlSeconds(String key) {
        try {
            Long ttl = redis.getExpire(key, TimeUnit.SECONDS);
            return ttl == null || ttl < 0 ? null : ttl;
        } catch (Exception ex) {
            log.warn("Redis getExpire failed for key={} - treating as no TTL. Exception: {}", key, ex.toString());
            return null;
        }
    }

    private String extractClientIp(HttpServletRequest request) {
        if (trustForwardedFor) {
            String header = request.getHeader(forwardedForHeader);
            if (StringUtils.hasText(header)) {
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
        String s = request.getHeader("X-User-Id");
        if (StringUtils.hasText(s)) {
            try { return Integer.valueOf(s); } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    private long getFailCountWindowSeconds() {
        return Duration.ofMinutes(failCountWindowMinutes).getSeconds();
    }

    private long getBackoffWindowSeconds() {
        return Duration.ofMinutes(backoffWindowMinutes).getSeconds();
    }

    private static String sha256Base64(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hashed);
        } catch (Exception ex) {
            throw new RuntimeException("sha256 failure", ex);
        }
    }
}
