package com.iemr.common.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;

public class TokenBlacklist {
	
	
	// Store blacklisted tokens (in-memory)
	private static final Map<String, Long> blacklistedTokens = new ConcurrentHashMap<>();


    // Add a token to the blacklist
    public static void blacklistToken(String token ,Long blackListExpirationTime) {
    	if(token == null || token.trim().isEmpty()) {
    		return;
    	}
    	blacklistedTokens.put(token, System.currentTimeMillis()+ blackListExpirationTime);
    }

    // Check if a token is blacklisted
   
    public static boolean isTokenBlacklisted(String token) {
    	if(token == null || token.trim().isEmpty()) {
    		return false;
    	}
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
