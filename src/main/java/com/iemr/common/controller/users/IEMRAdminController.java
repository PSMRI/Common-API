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
package com.iemr.common.controller.users;

import java.util.*;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.MediaType;

import com.iemr.common.utils.UserAgentUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.iemr.common.config.encryption.SecurePassword;
import com.iemr.common.constant.Constants;
import com.iemr.common.data.users.LoginSecurityQuestions;
import com.iemr.common.data.users.M_Role;
import com.iemr.common.data.users.ServiceRoleScreenMapping;
import com.iemr.common.data.users.User;
import com.iemr.common.data.users.UserSecurityQMapping;
import com.iemr.common.data.users.UserServiceRoleMapping;
import com.iemr.common.model.user.ChangePasswordModel;
import com.iemr.common.model.user.ForceLogoutRequestModel;
import com.iemr.common.model.user.LoginRequestModel;
import com.iemr.common.service.recaptcha.CaptchaValidationService;
import com.iemr.common.service.users.IEMRAdminUserService;
import com.iemr.common.utils.CookieUtil;
import com.iemr.common.utils.JwtUtil;
import com.iemr.common.utils.TokenDenylist;
import com.iemr.common.utils.encryption.AESUtil;
import com.iemr.common.utils.exception.IEMRException;
import com.iemr.common.utils.mapper.InputMapper;
import com.iemr.common.utils.mapper.OutputMapper;
import com.iemr.common.utils.redis.RedisSessionException;
import com.iemr.common.utils.response.OutputResponse;
import com.iemr.common.utils.sessionobject.SessionObject;

import io.lettuce.core.dynamic.annotation.Param;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RequestMapping("/user")
@RestController
public class IEMRAdminController {
	private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	private InputMapper inputMapper = new InputMapper();

	@Value("${captcha.enable-captcha}")
	private boolean enableCaptcha;

	@Autowired
	private CaptchaValidationService captchaValidatorService;
	private IEMRAdminUserService iemrAdminUserServiceImpl;
	@Autowired
	private JwtUtil jwtUtil;
	@Autowired
	private TokenDenylist tokenDenylist;
	@Autowired
	private CookieUtil cookieUtil;
	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	private AESUtil aesUtil;

	@Autowired
	public void setAesUtil(AESUtil aesUtil) {
		this.aesUtil = aesUtil;
	}

	@Autowired
	public void setIemrAdminUserService(IEMRAdminUserService iemrAdminUserService) {
		this.iemrAdminUserServiceImpl = iemrAdminUserService;
	}

	private SessionObject sessionObject;

	@Autowired
	public void setSessionObject(SessionObject sessionObject) {
		this.sessionObject = sessionObject;
	}

	@Autowired
	SecurePassword securePassword;

	@Operation(summary = "New user authentication")
	@RequestMapping(value = "/userAuthenticateNew", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON)
	public String userAuthenticateNew(
			@Param(value = "\"{\\\"userName\\\":\\\"String\\\",\\\"password\\\":\\\"String\\\"}\"") @RequestBody String jsonRequest) {
		OutputResponse response = new OutputResponse();
		try {
			User m_user = inputMapper.gson().fromJson(jsonRequest, User.class);
			response.setResponse("hello.....");
		} catch (Exception e) {
			response.setError(e);
		}
		return response.toString();
	}

	@Operation(summary = "User authentication")
	@RequestMapping(value = "/userAuthenticate", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON)
	public String userAuthenticate(
			@Param(value = "\"{\\\"userName\\\":\\\"String\\\",\\\"password\\\":\\\"String\\\"}\"") @RequestBody LoginRequestModel m_User,
			HttpServletRequest request, HttpServletResponse httpResponse) {
		OutputResponse response = new OutputResponse();
		logger.info("userAuthenticate request - " + m_User + " " + m_User.getUserName() + " " + m_User.getPassword());
		try {

			boolean isMobile = false;
			String userAgent = request.getHeader("User-Agent");
			isMobile = UserAgentUtil.isMobileDevice(userAgent);
			logger.info("UserAgentUtil isMobile : " + isMobile);

			String captchaToken = m_User.getCaptchaToken();
			if (enableCaptcha && !isMobile) {
				if (captchaToken != null && !captchaToken.trim().isEmpty()) {
					if (!captchaValidatorService.validateCaptcha(captchaToken)) {
						logger.warn("CAPTCHA validation failed for user: {}", m_User.getUserName());
						response.setError(new IEMRException("CAPTCHA validation failed"));
						return response.toString();
					}
					logger.info("CAPTCHA validated successfully for user: {}", m_User.getUserName());
				} else {
					logger.warn("CAPTCHA token missing for user: {}", m_User.getUserName());
					response.setError(new IEMRException("CAPTCHA validation failed. Please try again."));
					return response.toString();
				}
			} else {
				logger.info("CAPTCHA validation skipped");
			}

			String decryptPassword = aesUtil.decrypt("Piramal12Piramal", m_User.getPassword());
			List<User> mUser = iemrAdminUserServiceImpl.userAuthenticate(m_User.getUserName(), decryptPassword);
			JSONObject resMap = new JSONObject();
			JSONObject serviceRoleMultiMap = new JSONObject();
			JSONObject serviceRoleMap = new JSONObject();
			JSONArray serviceRoleList = new JSONArray();
			JSONObject previlegeObj = new JSONObject();
			if (m_User.getUserName() != null
					    && (m_User.getDoLogout() == null || !m_User.getDoLogout())
					    && (m_User.getWithCredentials() != null && m_User.getWithCredentials())) {
				String tokenFromRedis = getConcurrentCheckSessionObjectAgainstUser(
						m_User.getUserName().trim().toLowerCase());
				if (tokenFromRedis != null) {
					throw new IEMRException(
							"You are already logged in,please confirm to logout from other device and login again");
				}
			} else if (m_User.getUserName() != null && m_User.getDoLogout() != null && m_User.getDoLogout() == true) {
				deleteSessionObject(m_User.getUserName().trim().toLowerCase());
			}

			String jwtToken = null;
			String refreshToken = null;
			if (mUser.size() == 1) {
				jwtToken = jwtUtil.generateToken(m_User.getUserName(), mUser.get(0).getUserID().toString());

				User user = new User(); // Assuming the Users class exists
				user.setUserID(mUser.get(0).getUserID());
				user.setUserName(mUser.get(0).getUserName());
				logger.info("UserAgentUtil isMobile : " + isMobile);

				if (isMobile) {
					refreshToken = jwtUtil.generateRefreshToken(m_User.getUserName(), user.getUserID().toString());
					logger.debug("Refresh token generated successfully for user: {}", user.getUserName());
					String jti = jwtUtil.getJtiFromToken(refreshToken);
					redisTemplate.opsForValue().set(
							"refresh:" + jti,
							user.getUserID().toString(),
							jwtUtil.getRefreshTokenExpiration(),
							TimeUnit.MILLISECONDS
					);
				} else {
					cookieUtil.addJwtTokenToCookie(jwtToken, httpResponse, request);
				}

				String redisKey = "user_" + mUser.get(0).getUserID(); // Use user ID to create a unique key

				// Store the user in Redis (set a TTL of 30 minutes)
				redisTemplate.opsForValue().set(redisKey, user, 30, TimeUnit.MINUTES);

				createUserMapping(mUser.get(0), resMap, serviceRoleMultiMap, serviceRoleMap, serviceRoleList,
						previlegeObj);
			} else {
				resMap.put("isAuthenticated", /* Boolean.valueOf(false) */false);
			}
			JSONObject responseObj = new JSONObject(resMap.toString());
			JSONArray previlageObjs = new JSONArray();
			Iterator<?> services = previlegeObj.keys();
			while (services.hasNext()) {
				String service = (String) services.next();
				previlageObjs.put(previlegeObj.getJSONObject(service));
			}
			responseObj.put("previlegeObj", previlageObjs);
			String remoteAddress = request.getHeader("X-FORWARDED-FOR");
			if (remoteAddress == null || remoteAddress.trim().length() == 0) {
				remoteAddress = request.getRemoteAddr();
			}
			responseObj = iemrAdminUserServiceImpl.generateKeyAndValidateIP(responseObj, remoteAddress,
					request.getRemoteHost());

			// Add tokens to response for mobile
			if (isMobile && !mUser.isEmpty()) {
				responseObj.put("jwtToken", jwtToken);
				responseObj.put("refreshToken", refreshToken);
			}

			response.setResponse(responseObj.toString());
		} catch (Exception e) {
			logger.error("userAuthenticate failed with error " + e.getMessage(), e);
			response.setError(e);
		}
		logger.info("userAuthenticate response " + response.toString());
		return response.toString();
	}

	@Operation(summary = "generating a auth token with the refreshToken.")
	@RequestMapping(value = "/refreshToken", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON)
	public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
		String refreshToken = request.get("refreshToken");

		try {
			if (jwtUtil.validateToken(refreshToken) == null) {
				logger.warn("Token validation failed: invalid token provided.");
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized.");
			}

			Claims claims = jwtUtil.getAllClaimsFromToken(refreshToken);

			// Verify token type
			if (!"refresh".equals(claims.get("token_type", String.class))) {
				logger.warn("Token validation failed: incorrect token type in refresh request.");
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized.");

			}

			// Check revocation using JTI
			String jti = claims.getId();
			if (!redisTemplate.hasKey("refresh:" + jti)) {
				logger.warn("Token validation failed: refresh token is revoked or not found in store.");
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized.");
			}

			// Get user details
			// Get user details
			String userId = claims.get("userId", String.class);
			User user = iemrAdminUserServiceImpl.getUserById(Long.parseLong(userId));

			// Validate that the user still exists and is active
			if (user == null) {
				logger.warn("Token validation failed: user not found for userId in token.");
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized.");
			}

			if (user.getM_status() == null || !("Active".equalsIgnoreCase(user.getM_status().getStatus())
					|| "New".equalsIgnoreCase(user.getM_status().getStatus()))) {
				logger.warn("Token validation failed: user account is neither 'Active' nor 'New'.");
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized.");
			}
			// Generate new tokens
			String newJwt = jwtUtil.generateToken(user.getUserName(), userId);

			Map<String, String> tokens = new HashMap<>();
			tokens.put("jwtToken", newJwt);

			// Generate and store a new refresh token (token rotation)
			String newRefreshToken = jwtUtil.generateRefreshToken(user.getUserName(), userId);
			String newJti = jwtUtil.getJtiFromToken(newRefreshToken);
			redisTemplate.opsForValue().set(
				"refresh:" + newJti,
				userId,
				jwtUtil.getRefreshTokenExpiration(),
				TimeUnit.MILLISECONDS
			);
			tokens.put("refreshToken", newRefreshToken);

			return ResponseEntity.ok(tokens);
		} catch (ExpiredJwtException ex) {
			logger.warn("Token validation failed: token has expired.");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body("Authentication failed. Please log in again.");
		} catch (Exception e) {
			logger.error("Refresh failed: ", e);
			logger.error("Token refresh failed due to unexpected server error.");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("An unexpected error occurred. Please try again later.");
		}
	}

	@Operation(summary = "Log out user from concurrent session")
	@RequestMapping(value = "/logOutUserFromConcurrentSession", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON)
	public String logOutUserFromConcurrentSession(
			@Param(value = "\"{\\\"userName\\\":\\\"String\\\"}\"") @RequestBody LoginRequestModel m_User,
			HttpServletRequest request) {
		OutputResponse response = new OutputResponse();
		logger.info("logOutUserFromConcurrentSession request - " + m_User);
		try {
			if (m_User != null && m_User.getUserName() != null) {
				List<User> mUsers = iemrAdminUserServiceImpl.userExitsCheck(m_User.getUserName());

				if (mUsers == null || mUsers.size() <= 0) {
					logger.error("User not found");
					throw new IEMRException("Logout request failed, please try again later");
				} else if (mUsers.size() > 1) {
					logger.error("More than 1 user found");
					throw new IEMRException("Logout failed. Please retry or contact administrator");
				} else if (mUsers.size() == 1) {
					String previousTokenFromRedis = sessionObject
							.getSessionObject((mUsers.get(0).getUserName().toString().trim().toLowerCase()));
					if (previousTokenFromRedis != null) {
						deleteSessionObjectByGettingSessionDetails(previousTokenFromRedis);
						sessionObject.deleteSessionObject(previousTokenFromRedis);
						response.setResponse("User successfully logged out");
					} else{
						logger.error("Unable to fetch session from redis");
					throw new IEMRException("Session error. Please try again later");
					}
				}
			} else {
				throw new IEMRException("Invalid request object");
			}

		} catch (Exception e) {
			logger.error("logOutUserFromConcurrentSession failed with error " + e.getMessage(), e);
			response.setError(e);
		}
		logger.info("logOutUserFromConcurrentSession response " + response.toString());
		return response.toString();
	}

	/**
	 *
	 * function to return session object against userName
	 */
	private String getConcurrentCheckSessionObjectAgainstUser(String userName) {
		String response = null;
		try {
			response = sessionObject.getSessionObject(userName);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return response;
	}

	private void createUserMapping(User mUser, JSONObject resMap, JSONObject serviceRoleMultiMap,
			JSONObject serviceRoleMap, JSONArray serviceRoleList, JSONObject previlegeObj) {
		System.out.println(mUser);
		String fName = mUser.getFirstName();
		String lName = mUser.getLastName();
		String mName = mUser.getMiddleName();
		String uName = (fName == null ? "" : fName) + " " + (mName == null ? "" : mName) + " "
				+ (lName == null ? "" : lName);
		resMap.put("userID", mUser.getUserID());
		resMap.put("isAuthenticated", /* Boolean.valueOf(true) */true);
		resMap.put("userName", mUser.getUserName());
		resMap.put("fullName", uName);
		resMap.put("Status", mUser.getM_status().getStatus());
		resMap.put("agentID", mUser.getAgentID());
		resMap.put("agentPassword", mUser.getAgentPassword());
		resMap.put("m_UserLangMappings", new JSONArray(mUser.getM_UserLangMappings().toString()));
		resMap.put("designationID", mUser.getDesignationID());
		if (mUser.getDesignation() != null) {
			resMap.put("designation", new JSONObject(mUser.getDesignation().toString()));
		}
		if (null != mUser.getM_UserServiceRoleMapping()) {
			for (UserServiceRoleMapping m_UserServiceRoleMapping : mUser.getM_UserServiceRoleMapping()) {
				serviceRoleMultiMap.put(
						m_UserServiceRoleMapping.getM_ProviderServiceMapping().getM_ServiceMaster().getServiceName(),
						m_UserServiceRoleMapping.getM_Role().getRoleName());
				String serv = m_UserServiceRoleMapping.getM_ProviderServiceMapping().getM_ServiceMaster()
						.getServiceName();
				if (!previlegeObj.has(serv)) {
					previlegeObj.put(serv, new JSONObject(
							m_UserServiceRoleMapping.getM_ProviderServiceMapping().getM_ServiceMaster().toString()));
					previlegeObj.getJSONObject(serv).put("serviceName", serv);
					previlegeObj.getJSONObject(serv).put("serviceID",
							m_UserServiceRoleMapping.getM_ProviderServiceMapping().getProviderServiceMapID());
					previlegeObj.getJSONObject(serv).put("providerServiceMapID",
							m_UserServiceRoleMapping.getM_ProviderServiceMapping().getProviderServiceMapID());
					previlegeObj.getJSONObject(serv).put("apimanClientKey",
							m_UserServiceRoleMapping.getM_ProviderServiceMapping().getAPIMANClientKey());
					previlegeObj.getJSONObject(serv).put("roles", new JSONArray());
					previlegeObj.getJSONObject(serv).put("stateID",
							m_UserServiceRoleMapping.getM_ProviderServiceMapping().getStateID());
					previlegeObj.getJSONObject(serv).put("agentID", m_UserServiceRoleMapping.getAgentID());
					previlegeObj.getJSONObject(serv).put("agentPassword", m_UserServiceRoleMapping.getAgentPassword());
				}
				JSONArray roles = previlegeObj.getJSONObject(serv).getJSONArray("roles");
				// roles.put(new JSONObject(m_UserServiceRoleMapping.getM_Role().toString()));
				JSONObject roleObject = new JSONObject(m_UserServiceRoleMapping.getM_Role().toString());
				roleObject.put("teleConsultation", m_UserServiceRoleMapping.getTeleConsultation());
				roles.put(roleObject);
			}
		}
		Iterator<String> keySet = serviceRoleMultiMap.keys();
		while (keySet.hasNext()) {
			String s = keySet.next();
			serviceRoleMap.put("Service", s);
			serviceRoleMap.put("Role", serviceRoleMultiMap.get(s));
			serviceRoleList.put(serviceRoleMap);
		}
		resMap.put("Previlege", serviceRoleList);
	}

	@RequestMapping(value = "/superUserAuthenticate", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON)
	public String superUserAuthenticate(
			@Param(value = "\"{\\\"userName\\\":\\\"String\\\",\\\"doLogout\\\":\\\"Boolean\\\"}\"") @RequestBody LoginRequestModel m_User,
			HttpServletRequest request,HttpServletResponse httpResponse) {
		OutputResponse response = new OutputResponse();
		logger.info("userAuthenticate request ");
		try {

			if (!m_User.getUserName().equalsIgnoreCase("SuperAdmin")) {
				throw new IEMRException("Please log with admin credentials");
			}
			String decryptPassword = aesUtil.decrypt("Piramal12Piramal", m_User.getPassword());
			User mUser = iemrAdminUserServiceImpl.superUserAuthenticate(m_User.getUserName(), decryptPassword);
			JSONObject resMap = new JSONObject();
			JSONObject previlegeObj = new JSONObject();
			String jwtToken = null;
			String refreshToken = null;
			boolean isMobile = false;
			if (m_User.getUserName() != null && (m_User.getDoLogout() == null || m_User.getDoLogout() == false)) {
				String tokenFromRedis = getConcurrentCheckSessionObjectAgainstUser(
						m_User.getUserName().trim().toLowerCase());
				if (tokenFromRedis != null) {
					throw new IEMRException(
							"You are already logged in,please confirm to logout from other device and login again");
				}
			} else if (m_User.getUserName() != null && m_User.getDoLogout() != null && m_User.getDoLogout() == true) {
				deleteSessionObject(m_User.getUserName().trim().toLowerCase());
			}
			if (mUser != null) {
				resMap.put("userID", mUser.getUserID());
				resMap.put("isAuthenticated", /* Boolean.valueOf(true) */true);
				resMap.put("userName", mUser.getUserName());
				jwtToken = jwtUtil.generateToken(m_User.getUserName(), mUser.getUserID().toString());

				User user = new User(); // Assuming the Users class exists
	            user.setUserID(mUser.getUserID());
	            user.setUserName(mUser.getUserName());

				String userAgent = request.getHeader("User-Agent");
				isMobile = UserAgentUtil.isMobileDevice(userAgent);
				logger.info("UserAgentUtil isMobile : " + isMobile);

				if (isMobile) {
					refreshToken = jwtUtil.generateRefreshToken(m_User.getUserName(), user.getUserID().toString());
					logger.debug("Refresh token generated successfully for user: {}", user.getUserName());
					String jti = jwtUtil.getJtiFromToken(refreshToken);
					redisTemplate.opsForValue().set(
							"refresh:" + jti,
							user.getUserID().toString(),
							jwtUtil.getRefreshTokenExpiration(),
							TimeUnit.MILLISECONDS
					);
				} else {
					cookieUtil.addJwtTokenToCookie(jwtToken, httpResponse, request);
				}

				String redisKey = "user_" + mUser.getUserID(); // Use user ID to create a unique key

				// Store the user in Redis (set a TTL of 30 minutes)
				redisTemplate.opsForValue().set(redisKey, user, 30, TimeUnit.MINUTES);

			} else {
				resMap.put("isAuthenticated", /* Boolean.valueOf(false) */false);
			}
			JSONObject responseObj = new JSONObject(resMap.toString());
			JSONArray previlageObjs = new JSONArray();
			Iterator<?> services = previlegeObj.keys();
			while (services.hasNext()) {
				String service = (String) services.next();
				previlageObjs.put(previlegeObj.getJSONObject(service));
			}
			responseObj.put("previlegeObj", previlageObjs);

			String remoteAddress = request.getHeader("X-FORWARDED-FOR");
			if (remoteAddress == null || remoteAddress.trim().length() == 0) {
				remoteAddress = request.getRemoteAddr();
			}
			if (isMobile && null != mUser) {
				responseObj.put("jwtToken", jwtToken);
				responseObj.put("refreshToken", refreshToken);
			}
			responseObj = iemrAdminUserServiceImpl.generateKeyAndValidateIP(responseObj, remoteAddress,
					request.getRemoteHost());
			response.setResponse(responseObj.toString());
		} catch (Exception e) {
			logger.error("userAuthenticate failed with error " + e.getMessage(), e);
			response.setError(e);
		}
		logger.info("userAuthenticate response " + response.toString());
		return response.toString();
	}

//	@Operation(summary = "User authentication V1")
//	@RequestMapping(value = "/userAuthenticateV1", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON)
//	public String userAuthenticateV1(
//			@Param(value = "\"{\\\"userName\\\":\\\"String\\\",\\\"password\\\":\\\"String\\\"}\"") @RequestBody LoginRequestModel loginRequest,
//			HttpServletRequest request) {
//		OutputResponse response = new OutputResponse();
//		logger.info("userAuthenticate request ");
//		try {
//
//			String remoteAddress = request.getHeader("X-FORWARDED-FOR");
//			if (remoteAddress == null || remoteAddress.trim().length() == 0) {
//				remoteAddress = request.getRemoteAddr();
//			}
//			LoginResponseModel resp = iemrAdminUserServiceImpl.userAuthenticateV1(loginRequest, remoteAddress,
//					request.getRemoteHost());
//			JSONObject responseObj = new JSONObject(OutputMapper.gsonWithoutExposeRestriction().toJson(resp));
//			responseObj = iemrAdminUserServiceImpl.generateKeyAndValidateIP(responseObj, remoteAddress,
//					request.getRemoteHost());
//			response.setResponse(responseObj.toString());
//		} catch (Exception e) {
//			logger.error("userAuthenticate failed with error " + e.getMessage(), e);
//			response.setError(e);
//		}
//		logger.info("userAuthenticate response " + response.toString());
//		return response.toString();
//	}

	@Operation(summary = "Get login response")
	@RequestMapping(value = "/getLoginResponse", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String getLoginResponse(HttpServletRequest request) {
		OutputResponse response = new OutputResponse();
		try {
			String authHeader = request.getHeader("Authorization");
			if (authHeader.isEmpty()) {
				// Try JWT token from header first
				String jwtToken = request.getHeader("Jwttoken");

				// If not in header, try cookie
				if (jwtToken == null) {
					Cookie[] cookies = request.getCookies();
					if (cookies != null) {
						for (Cookie cookie : cookies) {
							if ("Jwttoken".equalsIgnoreCase(cookie.getName())) {
								jwtToken = cookie.getValue();
								break;
							}
						}
					}
				}

				if (jwtToken == null) {
					logger.warn("Authentication failed: no token found in header or cookies.");
					throw new IEMRException("Authentication failed. Please log in again.");
				}

				// Extract user ID from the JWT token
				String userId = jwtUtil.getUserIdFromToken(jwtToken);

				// Get user details and prepare response
				User user = iemrAdminUserServiceImpl.getUserById(Long.parseLong(userId));
				if (user == null) {
					logger.warn("User lookup failed for provided userId.");
					throw new IEMRException("Authentication failed. Please try again.");

				}

				String remoteAddress = request.getHeader("X-FORWARDED-FOR");
				if (remoteAddress == null || remoteAddress.trim().length() == 0) {
					remoteAddress = request.getRemoteAddr();
				}

				// Use the helper method to prepare response
				JSONObject responseObj = prepareAuthenticationResponse(user, remoteAddress, request.getRemoteHost());
				response.setResponse(responseObj.toString());
			} else {
				response.setResponse(sessionObject.getSessionObject(authHeader));
			}
		} catch (Exception e) {
			logger.error("getLoginResponse failed with error " + e.getMessage(), e);
			response.setError(e);
		}
		return response.toString();
	}

	@Operation(summary = "Forget password")
	@RequestMapping(value = "/forgetPassword", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON)
	public String forgetPassword(
			@Param(value = "\"{\\\"userName\\\":\\\"String\\\"}\"") @RequestBody ChangePasswordModel m_User) {
		OutputResponse response = new OutputResponse();
		logger.info("forgetPassword request " + m_User);
		try {
			List<User> mUsers = iemrAdminUserServiceImpl.userExitsCheck(m_User.getUserName());

			if (mUsers == null || mUsers.size() <= 0) {
				logger.error("User not found");
				throw new IEMRException("If the username is registered, you will be asked a security question");
			} else if (mUsers.size() > 1) {
				logger.error("More than 1 user found");
				throw new IEMRException("If the username is registered, you will be asked a security question");

			} else if (mUsers.size() == 1) {
				List<Map<String, String>> quesAnsList = new ArrayList<>();
				Map<String, String> quesAnsMap;
				Map<Object, Object> resMap = new HashMap<>();
				List<UserSecurityQMapping> mUserSecQuesMapping = iemrAdminUserServiceImpl
						.userSecurityQuestion(mUsers.get(0).getUserID());
				if (mUserSecQuesMapping != null) {
					for (UserSecurityQMapping element : mUserSecQuesMapping) {
						quesAnsMap = new HashMap<>();
						quesAnsMap.put("questionId", element.getQuestionID());
						quesAnsMap.put("question", element.getM_LoginSecurityQuestions().getQuestion());

						quesAnsList.add(quesAnsMap);
					}
					resMap.put("SecurityQuesAns", quesAnsList);
				}
				response.setResponse(OutputMapper.gsonWithoutExposeRestriction().toJson(resMap));
			}
		} catch (Exception e) {
			logger.error("forgetPassword failed with error " + e.getMessage(), e);
			response.setError(e);
		}
		logger.info("forgetPassword response " + response.toString());
		return response.toString();
	}

	@Operation(summary = "Set forget password")
	@RequestMapping(value = "/setForgetPassword", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON)
	public String setPassword(
			@Param(value = "\"{\"userName\":\"String\",\"password\":\"String\",\"transactionId\":\"String\"}\"") @RequestBody ChangePasswordModel m_user) {
		OutputResponse response = new OutputResponse();
		logger.info("setForgetPassword request " + m_user);
		try {
			int noOfRowModified = 0;
			List<User> mUsers = iemrAdminUserServiceImpl.userExitsCheck(m_user.getUserName());
			if (mUsers.size() != 1) {
				logger.warn(
						"Password reset failed for username '{}'. Reason: user not found, inactive, or multiple matches.",
						m_user.getUserName());

				throw new IEMRException("Unable to process your request. Please try again or contact support.");
			}
			User mUser = mUsers.get(0);
			String setStatus;
			String decryptPassword = aesUtil.decrypt("Piramal12Piramal", m_user.getPassword());
			noOfRowModified = iemrAdminUserServiceImpl.setForgetPassword(mUser, decryptPassword,
					m_user.getTransactionId(), m_user.getIsAdmin());
			if (noOfRowModified > 0) {
				setStatus = "Password Changed";
			} else {
				setStatus = "Something Wrong..!!!";
			}
			logger.info("Set forgot password changed " + noOfRowModified + " rows of data.");
			response.setResponse(setStatus);
		} catch (Exception e) {
			logger.error("setForgetPassword failed with error " + e.getMessage(), e);
			if (e.getMessage().equals(
					"Unable to process your request. Please try again or contact support."))
				response.setError(e);
			else
				response.setError(5000, e.getMessage());
		}

		logger.info("setForgetPassword response " + response.toString());
		return response.toString();
	}

	@Operation(summary = "Change password")
	@RequestMapping(value = "/changePassword", method = RequestMethod.POST, produces = "application/json")
	public String changePassword(
			@Param(value = "\"{\\\"userName\\\":\\\"String\\\",\\\"password\\\":\\\"String\\\",\\\"transactionId\\\":\\\"String\\\"}\"") @RequestBody ChangePasswordModel changePassword) {
		OutputResponse response = new OutputResponse();
		logger.info("changePassword request " + changePassword);
		try {
			int noOfRowUpdated = 0;
			List<User> mUsers = iemrAdminUserServiceImpl.userExitsCheck(changePassword.getUserName());
			String changeReqResult;
			if (mUsers.size() != 1) {
				logger.warn("Change password attempt failed. User not found or not available.");

				throw new IEMRException("Unable to change password. Please try again later");
			}
			try {
				int validatePassword;
				validatePassword = securePassword.validatePassword(changePassword.getPassword(),
						mUsers.get(0).getPassword());
				if (validatePassword == 1) {
					User mUser = mUsers.get(0);
					noOfRowUpdated = iemrAdminUserServiceImpl.setForgetPassword(mUser, changePassword.getNewPassword(),
							changePassword.getTransactionId(), changePassword.getIsAdmin());

				} else if (validatePassword == 2) {
					User mUser = mUsers.get(0);
					noOfRowUpdated = iemrAdminUserServiceImpl.setForgetPassword(mUser, changePassword.getNewPassword(),
							changePassword.getTransactionId(), changePassword.getIsAdmin());

				}
			} catch (Exception e) {
				throw new IEMRException(e.getMessage());
			}

			if (noOfRowUpdated > 0) {
				changeReqResult = "Password SuccessFully Change";
			} else {
				changeReqResult = "Something WentWrong.....Please Contact Administrator..!!!";
			}
			response.setResponse(changeReqResult);
		} catch (Exception e) {
			logger.error("changePassword failed with error " + e.getMessage(), e);
			response.setError(e);
		}
		logger.info("changePassword response " + response.toString());
		return response.toString();
	}

	@Operation(summary = "Save user security questions & answers")
	@RequestMapping(value = "/saveUserSecurityQuesAns", method = RequestMethod.POST, produces = "application/json")
	public String saveUserSecurityQuesAns(
			@Param(value = "\"[{\\\"userID\\\":\\\"Integer\\\",\\\"questionID\\\":\\\"Integer\\\",\\\"answers\\\":\\\"String\\\","
					+ "\\\"mobileNumber\\\":\\\"String\\\",\\\"createdBy\\\":\\\"String\\\"}]\"") @RequestBody Iterable<UserSecurityQMapping> m_UserSecurityQMapping) {
		OutputResponse response = new OutputResponse();
		logger.info("saveUserSecurityQuesAns request " + m_UserSecurityQMapping);
		try {
			String responseData = iemrAdminUserServiceImpl.saveUserSecurityQuesAns(m_UserSecurityQMapping);
			response.setResponse(responseData);
		} catch (Exception e) {
			logger.error("saveUserSecurityQuesAns failed with error " + e.getMessage(), e);
			response.setError(e);
		}
		logger.info("saveUserSecurityQuesAns response " + response.toString());
		return response.toString();
	}

	/**
	 *
	 * @return security qtns
	 */
	@Operation(summary = "Get security quetions")
	@RequestMapping(value = "/getsecurityquetions", method = RequestMethod.GET)
	public String getSecurityts() {
		OutputResponse response = new OutputResponse();
		logger.info("getsecurityquetions request ");
		try {
			ArrayList<LoginSecurityQuestions> test = iemrAdminUserServiceImpl.getAllLoginSecurityQuestions();
			response.setResponse(test.toString());
		} catch (Exception e) {
			logger.error("getsecurityquetions failed with error " + e.getMessage(), e);
			response.setError(5000, "Unable to fetch security questions");
		}
		logger.info("getsecurityquetions response " + response.toString());
		return response.toString();
	}

	@Operation(summary = "Get roles by provider id")
	@RequestMapping(value = "/getRolesByProviderID", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String getRolesByProviderID(
			@Param(value = "{\"providerServiceMapID\":\"Integer - providerServiceMapID\"}") @RequestBody String request) {
		OutputResponse response = new OutputResponse();
		logger.info("getRolesByProviderID request ");
		try {
			response.setResponse(iemrAdminUserServiceImpl.getRolesByProviderID(request));
		} catch (Exception e) {
			logger.error("getRolesByProviderID failed with error " + e.getMessage(), e);
			response.setError(e);
		}
		logger.info("getRolesByProviderID response " + response.toString());
		return response.toString();
	}

	@Operation(summary = "Get role screen mapping by provider id")
	@RequestMapping(value = "/getRoleScreenMappingByProviderID", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String getRoleScreenMappingByProviderID(
			@Param(value = "{\"providerServiceMapID\":\"Integer - providerServiceMapID\"}") @RequestBody String request) {

		OutputResponse response = new OutputResponse();
		logger.info("getRoleScreenMappingByProviderID");
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			ServiceRoleScreenMapping serviceRoleScreenMapping = objectMapper.readValue(request,
					ServiceRoleScreenMapping.class);

			List<ServiceRoleScreenMapping> mapping = iemrAdminUserServiceImpl
					.getUserServiceRoleMappingForProvider(serviceRoleScreenMapping.getProviderServiceMapID());

			response.setResponse(mapping.toString());
		} catch (Exception e) {
			logger.error("getRoleScreenMappingByProviderID failed with error " + e.getMessage(), e);
			response.setError(e);
		}
		logger.info("getRoleScreenMappingByProviderID response " + response.toString());
		return response.toString();
	}

	@Operation(summary = "Get users by provider id")
	@RequestMapping(value = "/getUsersByProviderID", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String getUsersByProviderID(@Param(value = "{\"providerServiceMapID\":\"Integer - providerServiceMapID\", "
			+ "\"RoleID\":\"Optional: Integer - role ID to be filtered\", "
			+ "\"languageName\":\"Optional: String - languageName\"}") @RequestBody String request) {
		OutputResponse response = new OutputResponse();
		logger.info("getRolesByProviderID request ");
		try {
			response.setResponse(iemrAdminUserServiceImpl.getUsersByProviderID(request));
		} catch (Exception e) {
			logger.error("getRolesByProviderID failed with error " + e.getMessage(), e);
			response.setError(e);
		}
		logger.info("getRolesByProviderID response " + response.toString());
		return response.toString();
	}

	@Operation(summary = "Get user service point van details")
	@RequestMapping(value = "/getUserServicePointVanDetails", method = RequestMethod.POST, produces = "application/json", headers = "Authorization")
	public String getUserServicePointVanDetails(
			@Param(value = "\"{\\\"userID\\\":\\\"Integer\\\",\"providerServiceMapID\":\"Integer\"}\"") @RequestBody String comingRequest) {
		OutputResponse response = new OutputResponse();
		try {

			JSONObject obj = new JSONObject(comingRequest);
			logger.info("getUserServicePointVanDetails request " + comingRequest);
			String responseData = iemrAdminUserServiceImpl.getUserServicePointVanDetails(obj.getInt("userID"));
			response.setResponse(responseData);
		} catch (Exception e) {
			response.setError(e);
			logger.error("get User SP and van details failed with " + e.getMessage(), e);

		}
		logger.info("getUserServicePointVanDetails response " + response.toString());
		return response.toString();
	}

	@Operation(summary = "Get service point villages")
	@RequestMapping(value = "/getServicepointVillages", method = RequestMethod.POST, produces = "application/json", headers = "Authorization")
	public String getServicepointVillages(
			@Param(value = "\"{\\\"servicePointID\\\":\\\"Integer\\\"}\"") @RequestBody String comingRequest) {
		OutputResponse response = new OutputResponse();
		try {

			JSONObject obj = new JSONObject(comingRequest);
			logger.info("getServicepointVillages request " + comingRequest);
			String responseData = iemrAdminUserServiceImpl.getServicepointVillages(obj.getInt("servicePointID"));
			response.setResponse(responseData);
		} catch (Exception e) {
			response.setError(e);
			logger.error("get villages with servicepoint failed with " + e.getMessage(), e);

		}
		logger.info("getServicepointVillages response " + response.toString());
		return response.toString();
	}

	@Operation(summary = "Get locations by provider id")
	@RequestMapping(value = "/getLocationsByProviderID", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String getLocationsByProviderID(
			@Param(value = "{\"providerServiceMapID\":\"Integer - providerServiceMapID\", "
					+ "\"roleID\":\"Integer - roleID\"}") @RequestBody String request) {
		OutputResponse response = new OutputResponse();
		logger.info("getLocationsByProviderID request ");
		try {
			response.setResponse(iemrAdminUserServiceImpl.getLocationsByProviderID(request));
		} catch (Exception e) {
			logger.error("getLocationsByProviderID failed with error " + e.getMessage(), e);
			response.setError(e);
		}
		logger.info("getLocationsByProviderID response " + response.toString());
		return response.toString();
	}

	@Operation(summary = "User log out")
	@RequestMapping(value = "/userLogout", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String userLogout(HttpServletRequest request) {
		OutputResponse response = new OutputResponse();
		logger.info("userLogout");
		try {
			deleteSessionObjectByGettingSessionDetails(request.getHeader("Authorization"));
			sessionObject.deleteSessionObject(request.getHeader("Authorization"));
			response.setResponse("Success");
		} catch (Exception e) {
			response.setError(e);
		}
		return response.toString();
	}

	/**
	 *
	 * @param key
	 * @return
	 */
	private void deleteSessionObjectByGettingSessionDetails(String key) {
		String sessionDetails = null;
		try {
			logger.info("inside delete child:" + key);
			sessionDetails = sessionObject.getSessionObject(key);
			logger.info("isessionDetails:" + sessionDetails);
			JsonObject jsnOBJ = new JsonObject();
			JsonParser jsnParser = new JsonParser();
			JsonElement jsnElmnt = jsnParser.parse(sessionDetails);
			jsnOBJ = jsnElmnt.getAsJsonObject();
			if (jsnOBJ.has("userName") && jsnOBJ.get("userName") != null) {
				logger.info("deleting key:" + jsnOBJ.get("userName").getAsString().trim().toLowerCase());
				sessionObject.deleteSessionObject(jsnOBJ.get("userName").getAsString().trim().toLowerCase());
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

	private void deleteSessionObject(String key) {
		try {
			sessionObject.deleteSessionObject(key);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

	@Operation(summary = "Force log out")
	@RequestMapping(value = "/forceLogout", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String forceLogout(@RequestBody ForceLogoutRequestModel request, HttpServletRequest httpRequest, HttpServletResponse response) {
	    OutputResponse outputResponse = new OutputResponse();
	    try {
	        // Perform the force logout logic
	        iemrAdminUserServiceImpl.forceLogout(request);

	        // Extract token from cookies or headers
	        String token = getJwtTokenFromCookies(httpRequest);
	        if (token == null) {
	        	 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
	             outputResponse.setError(new RuntimeException("No JWT token found in request"));
	        	 return outputResponse.toString();
	        	 }

	        // Validate the token: Check if it is expired or in the deny list
	        Claims claims = jwtUtil.validateToken(token);
	        if (claims.isEmpty() || claims.getExpiration() == null || claims.getId() == null) {	            // If token is either expired or in the deny list, return 401 Unauthorized
	        	response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
	        	outputResponse.setError(new RuntimeException("Token is expired or has been logged out"));
	        	return outputResponse.toString();
	        }

	        // Extract the jti (JWT ID) and expiration time from the validated claims
	        String jti = claims.getId();  // jti is in the 'id' field of claims
	        long expirationTime = claims.getExpiration().getTime(); // Use expiration from claims
	        long ttlMillis = expirationTime - System.currentTimeMillis();
	        tokenDenylist.addTokenToDenylist(jti, ttlMillis);

	        // Set the response message
	        outputResponse.setResponse("Success");
	    } catch (Exception e) {
	        outputResponse.setError(e);
	    }
	    return outputResponse.toString();
	}

	private String getJwtTokenFromCookies(HttpServletRequest request) {
	    Cookie[] cookies = request.getCookies();
	    if (cookies != null) {
	        for (Cookie cookie : cookies) {
	        	 if (cookie.getName().equalsIgnoreCase(Constants.JWT_TOKEN)) {
	        		 return cookie.getValue();
	            }
	        }
	    }
	    return null;
	}


	@Operation(summary = "User force log out")
	@RequestMapping(value = "/userForceLogout", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String userForceLogout(
			@Param(value = "\"{\\\"userName\\\":\\\"String\\\"}\"") @RequestBody ForceLogoutRequestModel request) {
		OutputResponse response = new OutputResponse();
		try {
			iemrAdminUserServiceImpl.userForceLogout(request);
			response.setResponse("Success");
		} catch (Exception e) {
			response.setError(e);
		}
		return response.toString();
	}

	@Operation(summary = "Get agent by role id")
	@RequestMapping(value = "/getAgentByRoleID", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String getAgentByRoleID(@Param(value = "{\"providerServiceMapID\":\"Integer - providerServiceMapID\", "
			+ "\"RoleID\":\"Optional: Integer - role ID to be filtered\"}") @RequestBody String request) {
		OutputResponse response = new OutputResponse();
		logger.info("getAgentByRoleID request " + request.toString());
		try {
			response.setResponse(iemrAdminUserServiceImpl.getAgentByRoleID(request));
		} catch (Exception e) {
			logger.error("getAgentByRoleID failed with error " + e.getMessage(), e);
			response.setError(e);
		}
		logger.info("getAgentByRoleID response " + response.toString());
		return response.toString();
	}

	@Operation(summary = "User authenticate by encryption")
	@RequestMapping(value = "/userAuthenticateByEncryption", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON)
	public String userAuthenticateByEncryption(
			@Param(value = "\"{\\\"userName\\\":\\\"String\\\",\\\"password\\\":\\\"String\\\"}\"") @RequestBody String req,
			HttpServletRequest request) {
		OutputResponse response = new OutputResponse();
		logger.info("userAuthenticateByEncryption request ");
		try {
			List<User> mUser = iemrAdminUserServiceImpl.userAuthenticateByEncryption(req);
			JSONObject resMap = new JSONObject();
			JSONObject serviceRoleMultiMap = new JSONObject();
			JSONObject serviceRoleMap = new JSONObject();
			JSONArray serviceRoleList = new JSONArray();
			JSONObject previlegeObj = new JSONObject();

			if (mUser.size() == 1) {
				createUserMapping(mUser.get(0), resMap, serviceRoleMultiMap, serviceRoleMap, serviceRoleList,
						previlegeObj);
			} else {
				resMap.put("isAuthenticated", /* Boolean.valueOf(false) */false);
			}
			JSONObject responseObj = new JSONObject(resMap.toString());
			JSONArray previlageObjs = new JSONArray();
			Iterator<?> services = previlegeObj.keys();
			while (services.hasNext()) {
				String service = (String) services.next();
				previlageObjs.put(previlegeObj.getJSONObject(service));
			}
			responseObj.put("previlegeObj", previlageObjs);
			String remoteAddress = request.getHeader("X-FORWARDED-FOR");
			if (remoteAddress == null || remoteAddress.trim().length() == 0) {
				remoteAddress = request.getRemoteAddr();
			}
			responseObj = iemrAdminUserServiceImpl.generateKeyAndValidateIP(responseObj, remoteAddress,
					request.getRemoteHost());
			response.setResponse(responseObj.toString());
		} catch (Exception e) {
			logger.error("userAuthenticateByEncryption failed with error " + e.getMessage(), e);
			response.setError(e);
		}
		logger.info("userAuthenticateByEncryption response " + response.toString());
		return response.toString();
	}

	@Operation(summary = "Get role wrap up time")
	@RequestMapping(value = "/role/{roleID}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String getrolewrapuptime(@PathVariable("roleID") Integer roleID) {

		OutputResponse response = new OutputResponse();
		try {
			M_Role test = iemrAdminUserServiceImpl.getrolewrapuptime(roleID);
			if (test == null) {
				throw new Exception("RoleID Not Found");
			}
			response.setResponse(test.toString());
		} catch (Exception e) {
			response.setError(e);
		}
		return response.toString();
	}

	/**
	 * @param request
	 * @return transaction Id for password change
	 */
	@Operation(summary = "Validating security question and answers for password change")
	@RequestMapping(value = { "/validateSecurityQuestionAndAnswer" }, method = { RequestMethod.POST })
	public String validateSecurityQuestionAndAnswer(
			@Param(value = "{\"SecurityQuesAns\": [{\"questionId\":\"String\",\"answer\":\"String\"}],\"userName\":\"String\"}") @RequestBody String request) {

		OutputResponse response = new OutputResponse();
		logger.info("validateSecurityQuestionAndAnswer API request" + request);
		try {
			if (request != null) {
				JsonObject requestObj = new JsonObject();
				JsonParser jsnParser = new JsonParser();
				JsonElement jsnElmnt = jsnParser.parse(request);
				requestObj = jsnElmnt.getAsJsonObject();
				String resp = iemrAdminUserServiceImpl.validateQuestionAndAnswersForPasswordChange(requestObj);
				response.setResponse(resp);
			} else
				throw new IEMRException("Invalid Request");
		} catch (Exception e) {
			logger.error("validateSecurityQuestionAndAnswer failed: {}", e.toString());
			response.setError(5000, e.getMessage());
		}
		logger.info("validateSecurityQuestionAndAnswer API response" + response.toString());
		return response.toString();
	}

	@Operation(summary = "User authentication")
	@RequestMapping(value = "/bhavya/userAuthenticate", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON)
	public String userAuthenticateBhavya(
			@Param(value = "\"{\\\"userName\\\":\\\"String\\\",\\\"password\\\":\\\"String\\\"}\"") @RequestBody LoginRequestModel m_User,
			HttpServletRequest request) {
		OutputResponse response = new OutputResponse();
		logger.info("userAuthenticate request - " + m_User + " " + m_User.getUserName() + " " + m_User.getPassword());
		try {
			// String decryptPassword = aesUtil.decrypt("Piramal12Piramal",
			// m_User.getPassword());
			// logger.info("decryptPassword : " + m_User.getPassword());
			List<User> mUser = iemrAdminUserServiceImpl.userAuthenticate(m_User.getUserName(), m_User.getPassword());
			JSONObject resMap = new JSONObject();
			JSONObject serviceRoleMultiMap = new JSONObject();
			JSONObject serviceRoleMap = new JSONObject();
			JSONArray serviceRoleList = new JSONArray();
			JSONObject previlegeObj = new JSONObject();
			if (m_User.getUserName() != null && (m_User.getDoLogout() == null || m_User.getDoLogout() == false)) {
				String tokenFromRedis = getConcurrentCheckSessionObjectAgainstUser(
						m_User.getUserName().trim().toLowerCase());
				if (tokenFromRedis != null) {
					throw new IEMRException(
							"You are already logged in,please confirm to logout from other device and login again");
				}
			} else if (m_User.getUserName() != null && m_User.getDoLogout() != null && m_User.getDoLogout() == true) {
				deleteSessionObject(m_User.getUserName().trim().toLowerCase());
			}
			if (mUser.size() == 1) {
				createUserMapping(mUser.get(0), resMap, serviceRoleMultiMap, serviceRoleMap, serviceRoleList,
						previlegeObj);
			} else {
				resMap.put("isAuthenticated", /* Boolean.valueOf(false) */false);
			}
			JSONObject responseObj = new JSONObject(resMap.toString());
			JSONArray previlageObjs = new JSONArray();
			Iterator<?> services = previlegeObj.keys();
			while (services.hasNext()) {
				String service = (String) services.next();
				previlageObjs.put(previlegeObj.getJSONObject(service));
			}
			responseObj.put("previlegeObj", previlageObjs);
			String remoteAddress = request.getHeader("X-FORWARDED-FOR");
			if (remoteAddress == null || remoteAddress.trim().length() == 0) {
				remoteAddress = request.getRemoteAddr();
			}
			responseObj = iemrAdminUserServiceImpl.generateKeyAndValidateIP(responseObj, remoteAddress,
					request.getRemoteHost());
			response.setResponse(responseObj.toString());
		} catch (Exception e) {
			logger.error("userAuthenticate failed with error " + e.getMessage(), e);
			response.setError(e);
		}
		logger.info("userAuthenticate response " + response.toString());
		return response.toString();
	}

	@GetMapping("/get-jwt-token")
	public ResponseEntity<String> getJwtTokenFromCookie(HttpServletRequest httpRequest) {
		// Retrieve the cookie named 'jwtToken'
		Cookie[] cookies = httpRequest.getCookies();

		if (cookies != null) {
			for (Cookie cookie : cookies) {

				if ("Jwttoken".equalsIgnoreCase(cookie.getName())) {

					String jwtToken = cookie.getValue();
					// Return the JWT token in the response
					return ResponseEntity.ok(jwtToken);
				}
			}
		}
		// Return 404 if the token is not found in the cookies
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body("JWT token not found");
	}

	private JSONObject prepareAuthenticationResponse(User mUser, String remoteAddress, String remoteHost) throws Exception {
		JSONObject resMap = new JSONObject();
		JSONObject serviceRoleMultiMap = new JSONObject();
		JSONObject serviceRoleMap = new JSONObject();
		JSONArray serviceRoleList = new JSONArray();
		JSONObject previlegeObj = new JSONObject();

		if (mUser != null) {
			mUser.setM_UserServiceRoleMapping(iemrAdminUserServiceImpl.getUserServiceRoleMapping(mUser.getUserID()));
			createUserMapping(mUser, resMap, serviceRoleMultiMap, serviceRoleMap, serviceRoleList, previlegeObj);
		} else {
			resMap.put("isAuthenticated", false);
		}

		JSONObject responseObj = new JSONObject(resMap.toString());
		JSONArray previlageObjs = new JSONArray();
		Iterator<?> services = previlegeObj.keys();
		while (services.hasNext()) {
			String service = (String) services.next();
			previlageObjs.put(previlegeObj.getJSONObject(service));
		}
		responseObj.put("previlegeObj", previlageObjs);

		return iemrAdminUserServiceImpl.generateKeyAndValidateIP(responseObj, remoteAddress, remoteHost);
	}

}
