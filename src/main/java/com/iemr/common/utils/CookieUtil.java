package com.iemr.common.utils;

import java.util.Arrays;
import java.util.Optional;

import org.springframework.stereotype.Service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class CookieUtil {

	public Optional<String> getCookieValue(HttpServletRequest request, String cookieName) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookieName.equals(cookie.getName())) {
					return Optional.of(cookie.getValue());
				}
			}
		}
		return Optional.empty();
	}

	public void addJwtTokenToCookie(String Jwttoken, HttpServletResponse response, HttpServletRequest request) {
	    // Create a new cookie with the JWT token
	    Cookie cookie = new Cookie("Jwttoken", Jwttoken);
	 
	    // Make the cookie HttpOnly to prevent JavaScript access for security
	    cookie.setHttpOnly(true);
	 
	    // Set the Max-Age (expiry time) in seconds (1 day)
	    cookie.setMaxAge(60 * 60 * 24); // 1 day expiration
	 
	    // Set the path to "/" so the cookie is available across the entire application
	    cookie.setPath("/");
	 
	    // Set the SameSite attribute for cross-site request handling (if needed)
	    String sameSite = "None"; // Allow cross-site cookies (can be 'Strict', 'Lax', or 'None')
	    cookie.setSecure(true);
	 
	    // Build the Set-Cookie header manually (to add SameSite attribute support)
	    StringBuilder cookieHeader = new StringBuilder();
	    cookieHeader.append(cookie.getName()).append("=").append(cookie.getValue())
	                .append("; Path=").append(cookie.getPath())
	                .append("; Max-Age=").append(cookie.getMaxAge())
	                .append("; HttpOnly");
	 
	    // Add SameSite and Secure attributes manually if needed
	    cookieHeader.append("; SameSite=").append(sameSite);
	    if (cookie.getSecure()) {
	        cookieHeader.append("; Secure");
	    }
	 
	    // Set the custom Set-Cookie header
	    response.addHeader("Set-Cookie", cookieHeader.toString());
	}

	public String getJwtTokenFromCookie(HttpServletRequest request) {
		if (request.getCookies() == null) {
	        return null;  // If cookies are null, return null safely.
	    }
		return Arrays.stream(request.getCookies()).filter(cookie -> "Jwttoken".equals(cookie.getName()))
				.map(Cookie::getValue).findFirst().orElse(null);
	}
}
