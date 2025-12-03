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
package com.iemr.common.utils;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.iemr.common.utils.http.AuthorizationHeaderRequestWrapper;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtUserIdValidationFilter implements Filter {

	private final JwtAuthenticationUtil jwtAuthenticationUtil;
	private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	private final String allowedOrigins;


	public JwtUserIdValidationFilter(JwtAuthenticationUtil jwtAuthenticationUtil,
			String allowedOrigins) {
		this.jwtAuthenticationUtil = jwtAuthenticationUtil;
		this.allowedOrigins = allowedOrigins;
	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) servletRequest;
		HttpServletResponse response = (HttpServletResponse) servletResponse;

		String origin = request.getHeader("Origin");
		String method = request.getMethod();
		String uri = request.getRequestURI();

		logger.debug("Incoming Origin: {}", origin);
		logger.debug("Request Method: {}", method);
		logger.debug("Request URI: {}", uri);
		logger.debug("Allowed Origins Configured: {}", allowedOrigins);

		// STEP 1: STRICT Origin Validation - Block unauthorized origins immediately
		// For OPTIONS requests, Origin header is required (CORS preflight)
		if ("OPTIONS".equalsIgnoreCase(method)) {
			if (origin == null) {
				logger.warn("BLOCKED - OPTIONS request without Origin header | Method: {} | URI: {}", method, uri);
				response.sendError(HttpServletResponse.SC_FORBIDDEN, "OPTIONS request requires Origin header");
				return;
			}
			if (!isOriginAllowed(origin)) {
				logger.warn("BLOCKED - Unauthorized Origin | Origin: {} | Method: {} | URI: {}", origin, method, uri);
				response.sendError(HttpServletResponse.SC_FORBIDDEN, "Origin not allowed");
				return;
			}
		} else {
			// For non-OPTIONS requests, validate origin if present
			if (origin != null && !isOriginAllowed(origin)) {
				logger.warn("BLOCKED - Unauthorized Origin | Origin: {} | Method: {} | URI: {}", origin, method, uri);
				response.sendError(HttpServletResponse.SC_FORBIDDEN, "Origin not allowed");
				return;
			}
		}

		// Determine request path/context for later checks
		String path = request.getRequestURI();
		String contextPath = request.getContextPath();

		// STEP 3: Add CORS Headers (only for validated origins)
		if (origin != null && isOriginAllowed(origin)) {
			response.setHeader("Access-Control-Allow-Origin", origin); // Never use wildcard
			response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, PATCH, DELETE, OPTIONS");
			response.setHeader("Access-Control-Allow-Headers", 
					"Authorization, Content-Type, Accept, Jwttoken, serverAuthorization, ServerAuthorization, serverauthorization, Serverauthorization");
			response.setHeader("Access-Control-Allow-Credentials", "true");
			response.setHeader("Access-Control-Max-Age", "3600");
			logger.info("Origin Validated | Origin: {} | Method: {} | URI: {}", origin, method, uri);
		}

		// STEP 4: Handle OPTIONS Preflight Request
		if ("OPTIONS".equalsIgnoreCase(method)) {
			// OPTIONS (preflight) - respond with full allowed methods
			response.setHeader("Access-Control-Allow-Origin", origin);
			response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, PATCH, DELETE, OPTIONS");
			response.setHeader("Access-Control-Allow-Headers",
					"Authorization, Content-Type, Accept, Jwttoken, serverAuthorization, ServerAuthorization, serverauthorization, Serverauthorization");
			response.setHeader("Access-Control-Allow-Credentials", "true");
			response.setStatus(HttpServletResponse.SC_OK);
			return;
		}

		logger.info("JwtUserIdValidationFilter invoked for path: " + path);

        // NEW: if this is a platform-feedback endpoint, treat it as public (skip auth)
        // and also ensure we don't clear any user cookies for these requests.
        if (isPlatformFeedbackPath(path, contextPath)) {
            logger.debug("Platform-feedback path detected - skipping authentication and leaving cookies intact: {}", path);
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
		// Log cookies for debugging
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if ("userId".equalsIgnoreCase(cookie.getName())) {
					logger.warn("userId found in cookies! Clearing it...");
					clearUserIdCookie(response); // Explicitly remove userId cookie
				}
			}
		} else {
			logger.info("No cookies found in the request");
		}

		// Log headers for debugging
		logger.debug("JWT token from header: {}", request.getHeader("Jwttoken") != null ? "present" : "not present");

		// Skip authentication for public endpoints
		if (shouldSkipAuthentication(path, contextPath)) {
			logger.info("Skipping filter for path: {}", path);
			filterChain.doFilter(servletRequest, servletResponse);
			return;
		}

		try {
			String jwtFromCookie = getJwtTokenFromCookies(request);
			String jwtFromHeader = request.getHeader("JwtToken");
			String authHeader = request.getHeader("Authorization");

			if (jwtFromCookie != null) {
				logger.info("Validating JWT token from cookie");
				if (jwtAuthenticationUtil.validateUserIdAndJwtToken(jwtFromCookie)) {
					AuthorizationHeaderRequestWrapper authorizationHeaderRequestWrapper = new AuthorizationHeaderRequestWrapper(
							request, "");
					filterChain.doFilter(authorizationHeaderRequestWrapper, servletResponse);
					return;
				}
			} else if (jwtFromHeader != null) {
				logger.info("Validating JWT token from header");
				if (jwtAuthenticationUtil.validateUserIdAndJwtToken(jwtFromHeader)) {
					AuthorizationHeaderRequestWrapper authorizationHeaderRequestWrapper = new AuthorizationHeaderRequestWrapper(
							request, "");
					filterChain.doFilter(authorizationHeaderRequestWrapper, servletResponse);
					return;
				}
			} else {
				String userAgent = request.getHeader("User-Agent");
				logger.info("User-Agent: " + userAgent);
				if (userAgent != null && isMobileClient(userAgent) && authHeader != null) {
					try {
						logger.info("Common-API incoming userAget : " + userAgent);
						UserAgentContext.setUserAgent(userAgent);
						filterChain.doFilter(servletRequest, servletResponse);
					} finally {
						UserAgentContext.clear();
					}
					return;
				}
			}

			logger.warn("No valid authentication token found");
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: Invalid or missing token");
		} catch (Exception e) {
			logger.error("Authorization error: ", e);
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authorization error: " + e.getMessage());
		}
	}

    /**
     * New helper: identifies platform-feedback endpoints so we can treat them
     * specially (public + preserve cookies).
     */
    private boolean isPlatformFeedbackPath(String path, String contextPath) {
        if (path == null) return false;
        String normalized = path.toLowerCase();
        String base = (contextPath == null ? "" : contextPath).toLowerCase();
        // match /platform-feedback and anything under it
        return normalized.startsWith(base + "/platform-feedback");
    }


	private boolean isOriginAllowed(String origin) {
		if (origin == null || allowedOrigins == null || allowedOrigins.trim().isEmpty()) {
			logger.warn("No allowed origins configured or origin is null");
			return false;
		}

		return Arrays.stream(allowedOrigins.split(","))
				.map(String::trim)
				.anyMatch(pattern -> {
					String regex = pattern
							.replace(".", "\\.")
						.replace("*", ".*");

					boolean matched = origin.matches(regex);
					return matched;
				});
	}



	private boolean isMobileClient(String userAgent) {
		if (userAgent == null)
			return false;
		userAgent = userAgent.toLowerCase();
		logger.info(userAgent);
		// return userAgent.contains("okhttp"); // iOS (custom clients)
		return userAgent.contains("okhttp") || userAgent.contains("java/"); // iOS (custom clients)
	}

	private boolean shouldSkipAuthentication(String path, String contextPath) {
		return path.equals(contextPath + "/user/userAuthenticate")
				|| path.equalsIgnoreCase(contextPath + "/user/logOutUserFromConcurrentSession")
				|| path.startsWith(contextPath + "/swagger-ui") || path.startsWith(contextPath + "/v3/api-docs")
				|| path.startsWith(contextPath + "/public") || path.equals(contextPath + "/user/refreshToken")
				|| path.startsWith(contextPath + "/user/superUserAuthenticate")
				|| path.startsWith(contextPath + "/user/user/userAuthenticateNew")
				|| path.startsWith(contextPath + "/user/userAuthenticateV1")
				|| path.startsWith(contextPath + "/user/forgetPassword")
				|| path.startsWith(contextPath + "/user/setForgetPassword")
				|| path.startsWith(contextPath + "/user/changePassword")
				|| path.startsWith(contextPath + "/user/saveUserSecurityQuesAns")
				|| path.startsWith(contextPath + "/user/userLogout")
				|| path.startsWith(contextPath + "/user/validateSecurityQuestionAndAnswer")
				|| path.startsWith(contextPath + "/user/logOutUserFromConcurrentSession")
				|| path.startsWith(contextPath + "/user/refreshToken");
	}

	private String getJwtTokenFromCookies(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equalsIgnoreCase("Jwttoken")) {
					return cookie.getValue();
				}
			}
		}
		return null;
	}

	private void clearUserIdCookie(HttpServletResponse response) {
		Cookie cookie = new Cookie("userId", null);
		cookie.setPath("/");
		cookie.setHttpOnly(true);
		cookie.setSecure(true);
		cookie.setMaxAge(0); // Invalidate the cookie
		response.addCookie(cookie);
	}
}
