package com.iemr.common.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class TokenDenylist {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // Add a token's jti to the denylist with expiration time
    public void denylistToken(String jti, Long expirationTime) {
        if (jti == null || jti.trim().isEmpty()) {
            return;
        }
        // Store the jti in Redis with expiration time set to the token's exp time (in milliseconds)
        redisTemplate.opsForValue().set(jti, "denied", expirationTime, TimeUnit.MILLISECONDS);
    }

    // Check if a token's jti is in the denylist
    public boolean isTokenDenylisted(String jti) {
        if (jti == null || jti.trim().isEmpty()) {
            return false;
        }
        return redisTemplate.hasKey(jti);
    }

    // Remove a token's jti from the denylist (Redis)
    public void removeTokenFromDenylist(String jti) {
        if (jti != null && !jti.trim().isEmpty()) {
            redisTemplate.delete(jti);
        }
    }
}
