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
package com.iemr.common.service.users;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.gson.JsonObject;
import com.iemr.common.data.users.LoginSecurityQuestions;
import com.iemr.common.data.users.M_Role;
import com.iemr.common.data.users.User;
import com.iemr.common.data.users.UserSecurityQMapping;
import com.iemr.common.data.users.UserServiceRoleMapping;
import com.iemr.common.data.users.ServiceRoleScreenMapping;
import com.iemr.common.model.user.ForceLogoutRequestModel;
import com.iemr.common.model.user.LoginRequestModel;
import com.iemr.common.model.user.LoginResponseModel;
import com.iemr.common.utils.exception.IEMRException;
import com.iemr.common.utils.redis.RedisSessionException;

public interface IEMRAdminUserService
{
	List<User> userAuthenticate(String userName, String password) throws Exception;

	List<User> userExitsCheck(String userName);

	List<UserSecurityQMapping> userSecurityQuestion(Long userId);


	User userWithOldPassExitsCheck(String userName, String Password);

	String saveUserSecurityQuesAns(Iterable<UserSecurityQMapping> m_UserSecurityQMapping) throws IEMRException;

	// Iterable<UserSecurityQMapping>
	// saveUserSecurityQuesAns(Iterable<UserSecurityQMapping>
	// m_UserSecurityQMapping);

	/**
	 * 
	 * @return login security questions
	 */
	ArrayList<LoginSecurityQuestions> getAllLoginSecurityQuestions();

	String getRolesByProviderID(String request) throws IEMRException, JsonMappingException, JsonProcessingException;

	String getUsersByProviderID(String request) throws IEMRException, JsonMappingException, JsonProcessingException;

	List<User> getUserDetails(String request) throws Exception;

	public String getUserServicePointVanDetails(Integer userID);

	public String getServicepointVillages(Integer servicePointID);

	LoginResponseModel userAuthenticateV1(LoginRequestModel loginRequest, String ipAddress, String hostName)
			throws Exception;

	JSONObject generateKeyAndValidateIP(JSONObject responseObj, String ipAddress, String hostName)
			throws JSONException, NoSuchAlgorithmException, RedisSessionException, IEMRException;

	public List<ServiceRoleScreenMapping> getUserServiceRoleMappingForProvider(Integer providerServiceMapID)
			throws IEMRException;

	String getLocationsByProviderID(String request) throws Exception;

	/**
	 * for admin authentication
	 * 
	 * @param userName
	 * @param password
	 * @return
	 */
	User superUserAuthenticate(String userName, String password) throws Exception;

	void forceLogout(ForceLogoutRequestModel request) throws Exception;

	void userForceLogout(ForceLogoutRequestModel request) throws Exception;

	String getAgentByRoleID(String request) throws IEMRException, JsonMappingException, JsonProcessingException;

	List<User> userAuthenticateByEncryption(String req) throws Exception;

	public M_Role getrolewrapuptime(Integer roleID);

	String validateQuestionAndAnswersForPasswordChange(JsonObject requestObj) throws Exception;

//	String generateTransactionIdForPasswordChange(Long userId) throws Exception;

//	int setForgetPassword(Long userId, String loginpass, String transactionId) throws IEMRException;

	int setForgetPassword(User user, String loginpass, String transactionId, Boolean isAdmin) throws IEMRException;

	String generateTransactionIdForPasswordChange(User user) throws Exception;

	User getUserById(Long userId) throws IEMRException;

	List<UserServiceRoleMapping> getUserServiceRoleMapping(Long userID) throws IEMRException;

	List<User> getUserIdbyUserName(String userName) throws IEMRException;

	boolean unlockUserAccount(Long userId) throws IEMRException;

	String getUserLockStatusJson(Long userId) throws IEMRException;

	boolean hasAdminPrivileges(Long userId) throws IEMRException;

}
