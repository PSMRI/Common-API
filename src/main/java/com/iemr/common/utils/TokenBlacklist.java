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
        Long expiryTime = blacklistedTokens.get(token);
        if (expiryTime == null) return false;
        if (System.currentTimeMillis() > expiryTime) {
            blacklistedTokens.remove(token);
            return false;
        }
        return true;
    }

}
