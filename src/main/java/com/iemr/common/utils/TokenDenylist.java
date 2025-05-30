package com.iemr.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class TokenDenylist {
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    
    private static final String PREFIX = "denied_";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // Add a token's jti to the denylist with expiration time
    public void addTokenToDenylist(String jti, Long expirationTime) {
        if (jti == null || jti.trim().isEmpty()) {
            return;
        }
        if (expirationTime == null || expirationTime <= 0) {
            throw new IllegalArgumentException("Expiration time must be positive");
        }

        // Store the jti in Redis with expiration time set to the token's exp time (in milliseconds)
        try {
            String key = PREFIX + jti;
            redisTemplate.opsForValue().set(key, " ", expirationTime * 1000, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            logger.error("Failed to denylist token with jti: " + jti, e);
            throw new RuntimeException("Failed to denylist token", e);
        }
    }

    // Check if a token's jti is in the denylist
    public boolean isTokenDenylisted(String jti) {
        if (jti == null || jti.trim().isEmpty()) {
            return false;
        }
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(jti));
        } catch (Exception e) {
            logger.error("Failed to check denylist status for jti: " + jti, e);
            // In case of Redis failure, consider the token as not denylisted to avoid blocking all requests
            return false;
        }
    }

    // Remove a token's jti from the denylist (Redis)
    public void removeTokenFromDenylist(String jti) {
        if (jti != null && !jti.trim().isEmpty()) {
            redisTemplate.delete(jti);
        }
    }
}
