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
package com.iemr.common.utils.http;


import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.iemr.common.utils.response.OutputResponse;
import com.iemr.common.utils.sessionobject.SessionObject;
import com.iemr.common.utils.validator.Validator;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
@Configuration
@Component
public class HTTPRequestInterceptor implements HandlerInterceptor {
	private Validator validator;

	Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

	@Autowired
	public void setValidator(Validator validator) {
		this.validator = validator;
	}

	private SessionObject sessionObject;

	@Autowired
	public void setSessionObject(SessionObject sessionObject) {
		this.sessionObject = sessionObject;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object object) throws Exception {
		boolean status = true;
		logger.info("In info preHandle we are Intercepting the Request");
		logger.debug("In preHandle we are Intercepting the Request");
	//	String authorization = request.getHeader("Authorization");
		String authorization = null;
		String preAuth = request.getHeader("Authorization");
		if(null != preAuth && preAuth.contains("Bearer "))
			authorization=preAuth.replace("Bearer ", "");
		else
			authorization = preAuth;
		
		if (authorization == null || authorization.isEmpty()) {
	        logger.info("Authorization header is null or empty. Skipping HTTPRequestInterceptor.");
	        return true; // Allow the request to proceed without validation
	    }
		logger.debug("RequestURI::" + request.getRequestURI() + " || Authorization ::" + authorization
				+ " || method :: " + request.getMethod());
		if (!request.getMethod().equalsIgnoreCase("OPTIONS")) {
			try {
				String[] requestURIParts = request.getRequestURI().split("/");
				String requestAPI = requestURIParts[requestURIParts.length - 1];
				switch (requestAPI) {
				case "userAuthenticate":
				case "superUserAuthenticate":
				case "userAuthenticateNew":
				case "userAuthenticateV1":
				case "forgetPassword":
				case "setForgetPassword":
				case "changePassword":
				case "saveUserSecurityQuesAns":
				case "doAgentLogout":
				case "userLogout":
				case "swagger-ui.html":
				case "index.html":
				case "index.css":
				case "swagger-initializer.js":
				case "swagger-config":
				case "swagger-ui-bundle.js":
				case "swagger-ui.css":
				case "ui":
				case "swagger-ui-standalone-preset.js":
				case "favicon-32x32.png":
				case "favicon-16x16.png":
				case "swagger-resources":
				case "api-docs":
				case "updateBenCallIdsInPhoneBlock":
				case "userAuthenticateByEncryption":
				case "sendOTP":
				case "validateOTP":
				case "resendOTP":
				case "validateSecurityQuestionAndAnswer":
				case "logOutUserFromConcurrentSession":
				case "refreshToken":
					break;
				case "error":
					status = false;
					break;
				default:
					String remoteAddress = request.getHeader("X-FORWARDED-FOR");
					if (remoteAddress == null || remoteAddress.trim().length() == 0) {
						remoteAddress = request.getRemoteAddr();
					}
					validator.checkKeyExists(authorization, remoteAddress);
					break;
				}
			} catch (Exception e) {
				OutputResponse output = new OutputResponse();
				output.setError(e);
				response.getOutputStream().print(output.toString());
				response.setContentType(MediaType.APPLICATION_JSON);
				response.setContentLength(output.toString().length());
				response.setHeader("Access-Control-Allow-Origin", "*");
				status = false;
			}
		}
		return status;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object object, ModelAndView model)
			throws Exception {
		try {
			logger.debug("In postHandle we are Intercepting the Request");
		//	String authorization = request.getHeader("Authorization");
			String authorization = null;
			String postAuth = request.getHeader("Authorization");
			if(null != postAuth && postAuth.contains("Bearer "))
				authorization=postAuth.replace("Bearer ", "");
			else
				authorization = postAuth;
			logger.debug("RequestURI::" + request.getRequestURI() + " || Authorization ::" + authorization);
			
			if (authorization != null && !authorization.equals("")) {
				sessionObject.updateSessionObject(authorization, sessionObject.getSessionObject(authorization));
			}
		} catch (Exception e) {
			logger.debug("postHandle failed with error " + e.getMessage());
		}
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object object, Exception arg3)
			throws Exception {
		logger.debug("In afterCompletion Request Completed");
	}
}