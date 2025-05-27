package com.iemr.common.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;

public class TokenBlacklist {
	@Value("${jwt.blacklist.expiration}")
	private static long BLACK_LIST_EXPIRATION_TIME;
	
	// Store blacklisted tokens (in-memory)
	private static Map<String, Long> blacklistedTokens = new HashMap<>();


    // Add a token to the blacklist
    public static void blacklistToken(String token) {
    	blacklistedTokens.put(token, BLACK_LIST_EXPIRATION_TIME);
    }

    // Check if a token is blacklisted
   
    public static boolean isTokenBlacklisted(String token) {
        Long expiry = blacklistedTokens.get(token);
        if (expiry == null) return false;
        // If token is expired, remove it from blacklist and treat as not blacklisted
        if (System.currentTimeMillis() > expiry) {
            blacklistedTokens.remove(token);
            return false;
        }
        return true;
    }

}
