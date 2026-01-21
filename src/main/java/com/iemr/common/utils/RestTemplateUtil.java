package com.iemr.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.iemr.common.constant.Constants;

import jakarta.servlet.http.HttpServletRequest;

public class RestTemplateUtil {
	private final static Logger logger = LoggerFactory.getLogger(RestTemplateUtil.class);

	public static HttpEntity<Object> createRequestEntity(Object body, String authorization) {

		ServletRequestAttributes servletRequestAttributes = ((ServletRequestAttributes) RequestContextHolder
				.getRequestAttributes());
		if (servletRequestAttributes == null) {
			MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
			headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE + ";charset=utf-8");
			headers.add(HttpHeaders.AUTHORIZATION, authorization);
			return new HttpEntity<>(body, headers);
		}
		HttpServletRequest requestHeader = servletRequestAttributes.getRequest();

		String jwtTokenFromCookie = extractJwttoken(requestHeader);

		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE + ";charset=utf-8");
		if (null != UserAgentContext.getUserAgent()) {
			headers.add(HttpHeaders.USER_AGENT, UserAgentContext.getUserAgent());
		}
		headers.add(HttpHeaders.AUTHORIZATION, authorization);
		if (null != requestHeader.getHeader(Constants.JWT_TOKEN)) {
			headers.add(Constants.JWT_TOKEN, requestHeader.getHeader(Constants.JWT_TOKEN));
			headers.add(HttpHeaders.COOKIE, "Jwttoken=" + requestHeader.getHeader(Constants.JWT_TOKEN));

		}
		if (null != jwtTokenFromCookie) {
			headers.add(HttpHeaders.COOKIE, "Jwttoken=" + jwtTokenFromCookie);
		}

		return new HttpEntity<>(body, headers);
	}

	private static String extractJwttoken(HttpServletRequest requestHeader) {
		String jwtTokenFromCookie = null;
		try {
			jwtTokenFromCookie = CookieUtil.getJwtTokenFromCookie(requestHeader);

		} catch (Exception e) {
			logger.error("Error while getting jwtToken from Cookie" + e.getMessage());
		}
		return jwtTokenFromCookie;
	}

	public static void getJwttokenFromHeaders(HttpHeaders headers) {
		ServletRequestAttributes servletRequestAttributes = ((ServletRequestAttributes) RequestContextHolder
				.getRequestAttributes());
		if (servletRequestAttributes == null) {
			return;
		}
		HttpServletRequest requestHeader = servletRequestAttributes.getRequest();
		String jwtTokenFromCookie = extractJwttoken(requestHeader);
		if (!headers.containsKey(HttpHeaders.CONTENT_TYPE)) {
			headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE + ";charset=utf-8");
		}
		if (null != UserAgentContext.getUserAgent()) {
			if (!headers.containsKey(HttpHeaders.USER_AGENT)) {
				headers.add(HttpHeaders.USER_AGENT, UserAgentContext.getUserAgent());
			}
		}
		if (null != jwtTokenFromCookie) {
			headers.add(HttpHeaders.COOKIE, Constants.JWT_TOKEN + "=" + jwtTokenFromCookie);
		} else if (null != requestHeader.getHeader(Constants.JWT_TOKEN)) {
			 headers.add(Constants.JWT_TOKEN, requestHeader.getHeader(Constants.JWT_TOKEN));
			 headers.add(HttpHeaders.COOKIE, Constants.JWT_TOKEN + "=" + requestHeader.getHeader(Constants.JWT_TOKEN));

		}
	}

}
