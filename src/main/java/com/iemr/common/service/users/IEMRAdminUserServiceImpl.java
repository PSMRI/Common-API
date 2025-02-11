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

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.LongSerializationPolicy;
import com.iemr.common.config.encryption.SecurePassword;
import com.iemr.common.data.cti.CampaignRole;
import com.iemr.common.data.users.LoginSecurityQuestions;
import com.iemr.common.data.users.M_Role;
import com.iemr.common.data.users.ProviderServiceAddressMapping;
import com.iemr.common.data.users.ProviderServiceMapping;
import com.iemr.common.data.users.Role;
import com.iemr.common.data.users.Screen;
import com.iemr.common.data.users.ServiceRoleScreenMapping;
import com.iemr.common.data.users.Servicepointvillagemap;
import com.iemr.common.data.users.User;
import com.iemr.common.data.users.UserSecurityQMapping;
import com.iemr.common.data.users.UserServiceRoleMapping;
import com.iemr.common.mapper.RoleMapper;
// import com.iemr.common.mapper.RoleMapper;
import com.iemr.common.mapper.UserMapper;
import com.iemr.common.mapper.UserServiceRoleMapper;
import com.iemr.common.model.user.ForceLogoutRequestModel;
import com.iemr.common.model.user.LoginRequestModel;
import com.iemr.common.model.user.LoginResponseModel;
import com.iemr.common.model.user.QuestionAnswerValidateModel;
import com.iemr.common.model.user.RoleFeatureOutputModel;
import com.iemr.common.repository.users.IEMRUserLoginSecurityRepository;
import com.iemr.common.repository.users.IEMRUserRepositoryCustom;
import com.iemr.common.repository.users.IEMRUserSecurityQuesAnsRepository;
import com.iemr.common.repository.users.MasterServicePointRepo;
import com.iemr.common.repository.users.MasterVanRepo;
import com.iemr.common.repository.users.ProviderServiceMapRepository;
import com.iemr.common.repository.users.RoleRepo;
import com.iemr.common.repository.users.ServicePointVillageMappingRepo;
import com.iemr.common.repository.users.ServiceRoleScreenMappingRepository;
import com.iemr.common.repository.users.UserParkingplaceMappingRepo;
import com.iemr.common.repository.users.UserRoleMappingRepository;
import com.iemr.common.repository.users.VanServicepointMappingRepo;
import com.iemr.common.service.cti.CTIService;
import com.iemr.common.utils.config.ConfigProperties;
import com.iemr.common.utils.encryption.AESUtil;
import com.iemr.common.utils.exception.IEMRException;
import com.iemr.common.utils.mapper.InputMapper;
import com.iemr.common.utils.mapper.OutputMapper;
import com.iemr.common.utils.response.OutputResponse;
import com.iemr.common.utils.rsa.RSAUtil;
import com.iemr.common.utils.sessionobject.SessionObject;
import com.iemr.common.utils.validator.Validator;

/**
 * 
 */
@Service
@PropertySource("classpath:application.properties")
public class IEMRAdminUserServiceImpl implements IEMRAdminUserService {
	/* Below are the mapper initializations */
	// @Autowired
	// private RoleMapper roleMapper;
	@Autowired
	private UserMapper userMapper;
	@Autowired
	private UserServiceRoleMapper userServiceRoleMapper;
	@Autowired
	private SecurePassword securePassword;

	@Autowired
	private RoleMapper roleMapper;
	@Autowired
	private AESUtil aesUtil;

	@Autowired
	private SessionObject sessionObject;
	@Value("${failedLoginAttempt}")
	private String failedLoginAttempt;
	// @Autowired
	// private ServiceRoleScreenMappingRepository ;

	private IEMRUserRepositoryCustom iEMRUserRepositoryCustom;
	private IEMRUserSecurityQuesAnsRepository iEMRUserSecurityQuesAnsRepository;
	private IEMRUserLoginSecurityRepository iEMRUserLoginSecurityRepository;
	private UserRoleMappingRepository userRoleMappingRepository;
	private ProviderServiceMapRepository providerServiceMapRepository;
	// private InputMapper inputMapper = new InputMapper();

	private UserParkingplaceMappingRepo userParkingplaceMappingRepo;
	private MasterVanRepo masterVanRepo;
	private VanServicepointMappingRepo vanServicepointMappingRepo;
	private ServicePointVillageMappingRepo servicePointVillageMappingRepo;

	private CTIService ctiService;

	private Validator validator;

	private Logger logger = LoggerFactory.getLogger(IEMRAdminUserServiceImpl.class);

	private static final String SEPERATOR = "%%%%";

	@Autowired
	public void setVanServicepointMappingRepo(VanServicepointMappingRepo vanServicepointMappingRepo) {
		this.vanServicepointMappingRepo = vanServicepointMappingRepo;
	}

	@Autowired
	public void setMasterVanRepo(MasterVanRepo masterVanRepo) {
		this.masterVanRepo = masterVanRepo;
	}

	@Autowired
	public void setUserParkingplaceMappingRepo(UserParkingplaceMappingRepo userParkingplaceMappingRepo) {
		this.userParkingplaceMappingRepo = userParkingplaceMappingRepo;
	}

	@Autowired
	public void setMasterServicePointRepo(MasterServicePointRepo masterServicePointRepo) {
	}

	@Autowired
	public void setIEMRUserLoginSecurityRepository(IEMRUserLoginSecurityRepository iEMRUserLoginSecurityRepository) {

		this.iEMRUserLoginSecurityRepository = iEMRUserLoginSecurityRepository;
	}

	@Autowired
	public void setIemrUserRepositoryImpl(IEMRUserRepositoryCustom iEMRUserRepositoryCustom) {
		this.iEMRUserRepositoryCustom = iEMRUserRepositoryCustom;
	}

	@Autowired
	public void setIemrUserRepositoryImpl(IEMRUserSecurityQuesAnsRepository iemrLoginSecurityQuesAnsRepository) {
		this.iEMRUserSecurityQuesAnsRepository = iemrLoginSecurityQuesAnsRepository;
	}

	@Autowired
	public void setUserRoleMappingRepository(UserRoleMappingRepository userRoleMappingRepository) {
		this.userRoleMappingRepository = userRoleMappingRepository;
	}

	@Autowired
	public void setProviderServiceMapRepository(ProviderServiceMapRepository providerServiceMapRepository) {
		this.providerServiceMapRepository = providerServiceMapRepository;
	}

	private ServiceRoleScreenMappingRepository serviceRoleScreenMappingRepository;

	@Autowired
	public void setServiceRoleScreenMappingRepository(
			ServiceRoleScreenMappingRepository serviceRoleScreenMappingRepository) {
		this.serviceRoleScreenMappingRepository = serviceRoleScreenMappingRepository;
	}

	@Autowired
	public void setServicePointVillageMappingRepo(ServicePointVillageMappingRepo servicePointVillageMappingRepo) {
		this.servicePointVillageMappingRepo = servicePointVillageMappingRepo;
	}

	@Autowired
	public void setCtiService(CTIService ctiService) {
		this.ctiService = ctiService;
	}

	@Autowired
	public void setValidator(Validator validator) {
		this.validator = validator;
	}

	@Override
	public List<User> userAuthenticate(String userName, String password) throws Exception {
		List<User> users = iEMRUserRepositoryCustom.findByUserNameNew(userName);
		if (users.size() != 1) {
			throw new IEMRException("User login failed due to incorrect username/password");
		} else {
			if (users.get(0).getDeleted())
				throw new IEMRException("Your account is locked or de-activated. Please contact administrator");
			else if (users.get(0).getStatusID() > 2)
				throw new IEMRException("Your account is not active. Please contact administrator");
		}
		int failedAttempt = 0;
		if (failedLoginAttempt != null)
			failedAttempt = Integer.parseInt(failedLoginAttempt);
		else
			failedAttempt = 5;
		User user = users.get(0);
		try {
			int validatePassword;
			validatePassword = securePassword.validatePassword(password, user.getPassword());
			if (validatePassword == 1) {
				int iterations = 1001;
				char[] chars = password.toCharArray();
				byte[] salt = getSalt();

				PBEKeySpec spec = new PBEKeySpec(chars, salt, iterations, 512);
				SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
				byte[] hash = skf.generateSecret(spec).getEncoded();
				String updatedPassword = iterations + ":" + toHex(salt) + ":" + toHex(hash);
				// save operation
				user.setPassword(updatedPassword);
				iEMRUserRepositoryCustom.save(user);

			} else if (validatePassword == 2) {
				iEMRUserRepositoryCustom.save(user);

			} else if (validatePassword == 3) {
				iEMRUserRepositoryCustom.save(user);
			} else if (validatePassword == 0) {
				if (user.getFailedAttempt() + 1 >= failedAttempt) {
					user.setFailedAttempt(user.getFailedAttempt() + 1);
					user.setDeleted(true);
					user = iEMRUserRepositoryCustom.save(user);
					throw new IEMRException(
							"User login failed due to incorrect username/password. Your account is locked due to "
									+ ConfigProperties.getInteger("failedLoginAttempt")
									+ " failed attempts. Please contact administrator.");
				} else {
					user.setFailedAttempt(user.getFailedAttempt() + 1);
					user = iEMRUserRepositoryCustom.save(user);
					throw new IEMRException("User login failed due to incorrect username/password. "
							+ (ConfigProperties.getInteger("failedLoginAttempt") - user.getFailedAttempt())
							+ " more attempt left.");
				}
			} else {
				if (user.getFailedAttempt() != 0) {
					user.setFailedAttempt(0);
					user = iEMRUserRepositoryCustom.save(user);
				}
			}
		} catch (Exception e) {
			throw new IEMRException(e.getMessage());
		}
		user.setM_UserServiceRoleMapping(getUserServiceRoleMapping(user.getUserID()));
		return users;
	}

	private void checkUserLoginFailedAttempt(User user) throws IEMRException {

	}

	private void updateUserLoginFailedAttempt(User user) throws IEMRException {

	}

	private void resetUserLoginFailedAttempt(User user) throws IEMRException {

	}

	/**
	 * Super Admin login
	 */
	@Override
	public User superUserAuthenticate(String userName, String password) throws Exception {
		List<User> users = iEMRUserRepositoryCustom.findByUserName(userName);

		if (users.size() != 1) {
			throw new IEMRException("User login failed due to incorrect username/password");
		} else {
			if (users.get(0).getDeleted())
				throw new IEMRException("Your account is locked or de-activated. Please contact administrator");
			else if (users.get(0).getStatusID() > 2)
				throw new IEMRException("Your account is not active. Please contact administrator");
		}
		int failedAttempt = 0;
		if (failedLoginAttempt != null)
			failedAttempt = Integer.parseInt(failedLoginAttempt);
		else
			failedAttempt = 5;
		User user = users.get(0);
		try {
			int validatePassword;
			validatePassword = securePassword.validatePassword(password, user.getPassword());
			if (validatePassword == 1) {
				int iterations = 1001;
				char[] chars = password.toCharArray();
				byte[] salt = getSalt();

				PBEKeySpec spec = new PBEKeySpec(chars, salt, iterations, 512);
				SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
				byte[] hash = skf.generateSecret(spec).getEncoded();
				String updatedPassword = iterations + ":" + toHex(salt) + ":" + toHex(hash);
				// save operation
				user.setPassword(updatedPassword);
				iEMRUserRepositoryCustom.save(user);

			} else if (validatePassword == 2) {
				iEMRUserRepositoryCustom.save(user);

			} else if (validatePassword == 0) {
				if (user.getFailedAttempt() + 1 >= failedAttempt) {
					user.setFailedAttempt(user.getFailedAttempt() + 1);
					user.setDeleted(true);
					user = iEMRUserRepositoryCustom.save(user);
					throw new IEMRException(
							"User login failed due to incorrect username/password. Your account is locked due to "
									+ ConfigProperties.getInteger("failedLoginAttempt")
									+ " failed attempts. Please contact administrator.");
				} else {
					user.setFailedAttempt(user.getFailedAttempt() + 1);
					user = iEMRUserRepositoryCustom.save(user);
					throw new IEMRException("User login failed due to incorrect username/password. "
							+ (ConfigProperties.getInteger("failedLoginAttempt") - user.getFailedAttempt())
							+ " more attempt left.");
				}
			} else {
				if (user.getFailedAttempt() != 0) {
					user.setFailedAttempt(0);
					user = iEMRUserRepositoryCustom.save(user);
				}
			}
		} catch (Exception e) {
			throw new IEMRException(e.getMessage());
		}
		return users.get(0);
	}

	@Override
	public LoginResponseModel userAuthenticateV1(LoginRequestModel loginRequest, String ipAddress, String hostName)
			throws Exception {
		LoginResponseModel loginResponseModel = null;
		List<User> users = iEMRUserRepositoryCustom.findByUserName(loginRequest.getUserName());
		if (users.size() == 1) {
			User user = users.get(0);
			try {
				if (!securePassword.validatePasswordExisting(loginRequest.getPassword(), user.getPassword())) {
					throw new IEMRException("User login failed due to incorrect username/password");
				}
			} catch (Exception e) {
				throw new IEMRException("User login failed due to incorrect username/password");
			}
			loginResponseModel = userMapper.userDataToLoginResponse(user);
			logger.info("Login response is " + loginResponseModel.toString());
			List<UserServiceRoleMapping> userServiceRoleMappings = getUserServiceRoleMapping(
					loginResponseModel.getUserID());
			loginResponseModel
					.setUserServiceRoleMappings(userServiceRoleMapper.userRoleToLoginUserRole(userServiceRoleMappings));

			// loginResponseModel.setHostName(hostName);
			// loginResponseModel.setIpAddress(ipAddress);
		} else {
			throw new IEMRException("User login failed due to incorrect username/password");
		}

		return loginResponseModel;
	}

	@Override
	public List<User> getUserDetails(String request) throws Exception {
		// List<User> users = new ArrayList<User>();
		// User userRequest = inputMapper.gson().fromJson(request, User.class);
		// users.add(iEMRUserRepositoryCustom.findUserByUserID(userRequest.getUserID()));
		// if (users.size() == 1)
		// {
		// for (User user : users)
		// {
		// user.setM_UserServiceRoleMapping(getUserServiceRoleMapping(user.getUserID()));
		// }
		// } else
		// {
		// throw new IEMRException("User login failed due to incorrect username/password
		// or both");
		// }
		return null;
	}

	@Override
	public List<User> userExitsCheck(String userName) {
		return iEMRUserRepositoryCustom.findByUserName(userName);
	}

	@Override
	public List<UserSecurityQMapping> userSecurityQuestion(Long userId) {
		return iEMRUserRepositoryCustom.getUserSecurityQues(userId);
	}

	@Override
	public int setForgetPassword(User user, String loginpass, String transactionId, Boolean isAdmin)
			throws IEMRException {

		int count = 0;
		try {
			if (isAdmin != null && isAdmin == true) {
				updateCTIPasswordForUserV1(user.getUserID(), loginpass);
				loginpass = securePassword.generateStrongPassword(loginpass);
				count = iEMRUserRepositoryCustom.updateSetForgetPassword(user.getUserID(), loginpass);
			} else {
				String tokenFromRedis = sessionObject
						.getSessionObjectForChangePassword((user.getUserID().toString() + user.getUserName()));
				if (tokenFromRedis.equalsIgnoreCase(transactionId)) {

					updateCTIPasswordForUserV1(user.getUserID(), loginpass);
					loginpass = securePassword.generateStrongPassword(loginpass);
					count = iEMRUserRepositoryCustom.updateSetForgetPassword(user.getUserID(), loginpass);
					// Deleting transaction Id
					if (count > 0)
						sessionObject.deleteSessionObject((user.getUserID().toString() + user.getUserName()));
					else
						throw new IEMRException("error while updating new password");
				} else {
					throw new IEMRException("Unable to fetch transaction Id or transaction Id is expired");
				}
			}
		} catch (Exception e) {
			logger.error("Error while changing the password: " + e.getMessage(), e);
			throw new IEMRException("Error while changing the password: " + e.getMessage());
		}
		return count;
	}

	@Async
	public void updateCTIPasswordForUser(Long userID, String password) {
		try {
			List<UserServiceRoleMapping> userRoles = new ArrayList<UserServiceRoleMapping>();
			User userDetails = iEMRUserRepositoryCustom.findUserByUserID(userID);
			if (userDetails.getUserID() != null) {
				Set<Object[]> resultSet = userRoleMappingRepository.getUserRoleMappingForUser(userID);
				for (Object[] object : resultSet) {
					if (object != null && object.length >= 11) {
						UserServiceRoleMapping userServiceRoleMapping = UserServiceRoleMapping
								.initializeUserRoleMappingObjs((Long) object[0], (Long) object[1], (Integer) object[2],
										(Role) object[3], (Integer) object[4],
										getUserProviderServiceRoleMapping((Integer) object[4]), (String) object[5],
										(Boolean) object[6], (Boolean) object[7], (Boolean) object[8], (String) object[9],
										(Integer) object[10], (ProviderServiceAddressMapping) object[11]);
						userRoles.add(userServiceRoleMapping);
					}
				}
				userRoles.removeIf(Objects::isNull);
			}
			JSONObject rolesToUpdate = new JSONObject();
			for (UserServiceRoleMapping m_UserServiceRoleMapping : userRoles) {
				JSONArray serviceLines;
				String roleName = m_UserServiceRoleMapping.getM_Role().getRoleName();
				String campaignName = m_UserServiceRoleMapping.getM_ProviderServiceMapping().getCtiCampaignName();
				if (campaignName == null) {
					continue;
				}
				String roleCampaignKey = roleName + SEPERATOR + campaignName;
				if (rolesToUpdate.has(roleCampaignKey)) {
					serviceLines = rolesToUpdate.getJSONArray(roleCampaignKey);
				} else {
					serviceLines = new JSONArray();
					rolesToUpdate.put(roleCampaignKey, serviceLines);
				}
				String newServiceName = m_UserServiceRoleMapping.getM_ProviderServiceMapping().getM_ServiceMaster()
						.getServiceName();
				JSONArray oldServices = new JSONArray(serviceLines.toString());
				int indexToInsert = oldServices.length();
				for (int i = 0; i < oldServices.length(); i++) {
					String string = oldServices.getString(i);
					if (indexToInsert == oldServices.length() && string.contains("_")) {
						indexToInsert = i;
					}
					serviceLines.put(string + "_" + newServiceName);
				}
				serviceLines.put(indexToInsert, newServiceName);
			}

			JSONObject request = new JSONObject();
			request.put("username", userDetails.getUserName());
			// request.put("password", userDetails.getPassword());
			request.put("password", password);
			request.put("firstname", userDetails.getFirstName());
			request.put("lastname", userDetails.getLastName());
			request.put("phone", userDetails.getEmergencyContactNo());
			request.put("email", userDetails.getEmailID());
			Iterator<String> keys = rolesToUpdate.keys();
			while (keys.hasNext()) {
				String keyValue = keys.next();
				String[] keyCampaign = keyValue.split(SEPERATOR);
				if (keyCampaign.length >= 2) {
					String key = keyCampaign[0];
					String campaign = keyCampaign[1];
					if (campaign == null) {
						continue;
					}
					HashMap<String, String> availableDesignations = null;
					availableDesignations = getAvailableCTIDesignations(campaign);
					JSONArray roles = rolesToUpdate.getJSONArray(keyValue);
					for (int index = 0; index < roles.length(); index++) {
						String role = roles.getString(index);
						try {
							String designation = key + "_" + role;
							// String ctiRole = availableDesignations.get(designation);
							// if (ctiRole.equalsIgnoreCase(designation))
							Set<String> ctiRoles = availableDesignations.keySet();
							for (String ctiRole : ctiRoles) {
								System.out.println(
										ctiRole.replace("\"", "").replace("\'", "") + " ++++++++++++++ " + designation);
								if (ctiRole.replace("\"", "").replace("\'", "").equalsIgnoreCase(designation)) {
									request.put("role", designation);
									request.put("designation", designation);
									ctiService.addUpdateUserData(request.toString(), "127.0.0.1");
								}
							}
							// if (availableDesignations.containsValue(designation))
							// {
							// request.put("role", designation);
							// request.put("designation", designation);
							// ctiService.addUpdateUserData(request.toString(), "127.0.0.1");
							// }
						} catch (Exception e) {
							logger.error("Update failed with error ", e);
						}
					}
				}
			}

		} catch (Exception e) {
			logger.error("CTI updating user details failed with error " + e.getMessage(), e);
		}
	}

	@Async
	public void updateCTIPasswordForUserV1(Long userID, String password) {
		try {
			// List<UserServiceRoleMapping> userRoles = new
			// ArrayList<UserServiceRoleMapping>();
			User userDetails = iEMRUserRepositoryCustom.findUserByUserID(userID);
			if (userDetails.getUserID() != null) {
				JSONObject request = new JSONObject();
				request.put("username", userDetails.getUserName());
				request.put("password", password);
				ctiService.addUpdateUserData(request.toString(), "127.0.0.1");
			}
		} catch (Exception e) {
			logger.error("CTI updating user details failed with error " + e.getMessage(), e);
		}
	}

	private HashMap<String, String> getAvailableCTIDesignations(String campaign) throws JsonMappingException, JsonProcessingException {
		HashMap<String, String> result = new HashMap<String, String>();
		try {
			JSONObject request = new JSONObject();
			request.put("campaign", campaign);
			OutputResponse response = ctiService.getCampaignRoles(request.toString(), "127.0.0.1");
			CampaignRole campaignRoles = InputMapper.gson().fromJson(response.getData(), CampaignRole.class);
			JsonArray ctiRoles = campaignRoles.getRoles();
			if (ctiRoles.size() > 0) {
				for (JsonElement role : ctiRoles) {
					// result.add(role.toString().trim());
					result.put(role.toString().trim(), role.toString().trim());
				}
			}
		} catch (JSONException | IEMRException e) {
			logger.error("getAvailableCTIDesignations failed with error " + e.getMessage(), e);
		}
		return result;
	}

	@Override
	public User userWithOldPassExitsCheck(String userName, String password) {
		return iEMRUserRepositoryCustom.findUserForChangePass(userName, password);
	}

	@Override
	public String saveUserSecurityQuesAns(Iterable<UserSecurityQMapping> m_UserSecurityQMapping) throws IEMRException {
		int x = 0;
		try {
			if (m_UserSecurityQMapping != null && m_UserSecurityQMapping.iterator().hasNext()) {
				User users = iEMRUserRepositoryCustom
						.findUserByUserID(m_UserSecurityQMapping.iterator().next().getUserID());
				if (users == null) {
					throw new IEMRException("User does not exist or is not active");
				}

				Iterable<UserSecurityQMapping> obj = iEMRUserSecurityQuesAnsRepository.saveAll(m_UserSecurityQMapping);

				if (obj.iterator().hasNext()) {
					x = iEMRUserRepositoryCustom.updateSetUserStatusActive(obj.iterator().next().getUserID());
				}

				if (x > 0) {
					sessionObject.deleteSessionObject((users.getUserID().toString() + users.getUserName()));
					return generateTransactionIdForPasswordChange(users);
				} else {
					throw new IEMRException("Failed to save security question and answers, Please try again");
				}
			} else
				throw new IEMRException("Invalid user, please contact administrator");
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new IEMRException(e.getMessage());
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.iemr.common.service.users.IEMRAdminUserService#
	 * getAllLoginSecurityQuestions()
	 */
	@Override
	public ArrayList<LoginSecurityQuestions> getAllLoginSecurityQuestions() {

		ArrayList<LoginSecurityQuestions> result = new ArrayList<LoginSecurityQuestions>();
		ArrayList<Object[]> lists = iEMRUserLoginSecurityRepository.getAllLoginSecurityQuestions();
		for (Object[] objects : lists) {
			if (objects != null && objects.length > 0) {
				LoginSecurityQuestions question = new LoginSecurityQuestions();
				result.add(question.getSecurityQuestions((Integer) objects[0], (String) objects[1]));
			}
			result.removeIf(Objects::isNull);
		}
		return result;
	}

	private List<UserServiceRoleMapping> getUserServiceRoleMapping(Long userID) throws IEMRException {
		List<UserServiceRoleMapping> userServiceRoleMappings = new ArrayList<UserServiceRoleMapping>();
		Set<Object[]> resultSet = userRoleMappingRepository.getUserRoleMappingForUser(userID);
		if (resultSet.size() == 0) {
			throw new IEMRException("Contact Administrator as either provider or user or both are inactive");
		}
		for (Object[] object : resultSet) {
			if (object != null && object.length >= 11) {
				UserServiceRoleMapping userServiceRoleMapping = UserServiceRoleMapping.initializeUserRoleMappingObjs(
						(Long) object[0], (Long) object[1], (Integer) object[2], (Role) object[3], (Integer) object[4],
						getUserProviderServiceRoleMapping((Integer) object[4]), (String) object[5], (Boolean) object[6],
						(Boolean) object[7], (Boolean) object[8], (String) object[9], (Integer) object[10], 
						(ProviderServiceAddressMapping) object[11]);
				List<ServiceRoleScreenMapping> serviceRoleScreenMappings = getActiveScreenMappings(
						userServiceRoleMapping.getProviderServiceMapID(), userServiceRoleMapping.getRoleID());
				userServiceRoleMapping.getM_Role().setServiceRoleScreenMappings(serviceRoleScreenMappings);
				userServiceRoleMappings.add(userServiceRoleMapping);
			}
		}
		userServiceRoleMappings.removeIf(Objects::isNull);
		return userServiceRoleMappings;
	}

	private List<ServiceRoleScreenMapping> getActiveScreenMappings(Integer serviceProviderMapID, Integer roleID) {
		List<ServiceRoleScreenMapping> screenMappings = new ArrayList<ServiceRoleScreenMapping>();
		List<Object[]> resultSet = serviceRoleScreenMappingRepository.getActiveScreenMappings(serviceProviderMapID,
				roleID);
		for (Object[] objects : resultSet) {
			ServiceRoleScreenMapping screenMapping = new ServiceRoleScreenMapping();
			screenMappings.add(screenMapping.createServiceRoleScreenMapping((Integer) objects[0], (Integer) objects[1],
					(Screen) objects[2], (Integer) objects[3], (ProviderServiceMapping) objects[4],
					(Integer) objects[5], /* (Role) objects[6], */(Boolean) objects[6], (String) objects[7]));
		}
		return screenMappings;
	}

	public List<ServiceRoleScreenMapping> getUserServiceRoleMappingForProvider(Integer providerServiceMapID)
			throws IEMRException {

		List<ServiceRoleScreenMapping> screenMappings = new ArrayList<ServiceRoleScreenMapping>();
		List<Object[]> resultSet = serviceRoleScreenMappingRepository
				.getActiveScreenMappingsForProvider(providerServiceMapID);
		for (Object[] objects : resultSet) {
			ServiceRoleScreenMapping screenMapping = new ServiceRoleScreenMapping();
			screenMappings.add(screenMapping.createServiceRoleScreenMapping((Integer) objects[0], (Integer) objects[1],
					(Screen) objects[2], (Integer) objects[3], (ProviderServiceMapping) objects[4],
					(Integer) objects[5], /* (Role) objects[6], */ (Boolean) objects[6], (String) objects[7]));
		}
		screenMappings.removeIf(Objects::isNull);
		return screenMappings;
	}

	private ProviderServiceMapping getUserProviderServiceRoleMapping(Integer providerServiceMapID) {
		return providerServiceMapRepository.findByID(providerServiceMapID);
	}

	@Override
	public String getRolesByProviderID(String request) throws IEMRException, JsonMappingException, JsonProcessingException {
		// ArrayList<Role> rolesList = new ArrayList<Role>();
		ArrayList<RoleFeatureOutputModel> rolesList = new ArrayList<RoleFeatureOutputModel>();
		ObjectMapper objectMapper = new ObjectMapper();
		UserServiceRoleMapping userRoles = objectMapper.readValue(request, UserServiceRoleMapping.class);
		Set<Object[]> resultSet = userRoleMappingRepository
				.getRolesByProviderServiceMapID(userRoles.getProviderServiceMapID());
		for (Object[] roleObj : resultSet) {
			if (roleObj != null && roleObj.length >= 2) {
				Role role = (Role) roleObj[1];
				role.setServiceRoleScreenMappings(
						getRoleScreenMapping(userRoles.getProviderServiceMapID(), role.getRoleID()));
				RoleFeatureOutputModel model = roleMapper.roleFeatureMapping(role);
				rolesList.add(model);
			}
			rolesList.removeIf(Objects::isNull);
		}
		return OutputMapper.gsonWithoutExposeRestriction().toJson(rolesList);
	}

	private List<ServiceRoleScreenMapping> getRoleScreenMapping(Integer providerServiceMapID, Integer roleID) {
		List<ServiceRoleScreenMapping> screenMappings = new ArrayList<ServiceRoleScreenMapping>();
		List<Object[]> resultSet = serviceRoleScreenMappingRepository.getRoleScreenMappings(providerServiceMapID,
				roleID);
		if (resultSet != null && resultSet.size() > 0) {
			for (Object[] objects : resultSet) {
				if (objects.length >= 5) {
					screenMappings.add(new ServiceRoleScreenMapping().createServiceRoleScreenMapping(
							(Integer) objects[0], (Integer) objects[1], (Screen) objects[2], (Boolean) objects[3],
							(String) objects[4]));
				}
			}
			screenMappings.removeIf(Objects::isNull);
		}
		return screenMappings;
	}

	@Override
	public String getUsersByProviderID(String request) throws IEMRException, JsonMappingException, JsonProcessingException {
		ArrayList<User> usersList = new ArrayList<User>();
		ObjectMapper objectMapper = new ObjectMapper();
		UserServiceRoleMapping userRoles = objectMapper.readValue(request, UserServiceRoleMapping.class);
		List<Long> resultSet = null;
		if (userRoles.getRoleID() != null) {
			if (userRoles.getLanguageName() != null) {
				resultSet = userRoleMappingRepository.getUsersByProviderServiceMapRoleLang(
						userRoles.getProviderServiceMapID(), userRoles.getRoleID(), userRoles.getLanguageName());
			} else {
				resultSet = userRoleMappingRepository
						.getUsersByProviderServiceMapID(userRoles.getProviderServiceMapID(), userRoles.getRoleID());
			}
		} else {
			if (userRoles.getLanguageName() != null) {
				resultSet = userRoleMappingRepository.getUsersByProviderServiceMapLang(
						userRoles.getProviderServiceMapID(), userRoles.getLanguageName());
			} else {
				resultSet = userRoleMappingRepository
						.getUsersByProviderServiceMapID(userRoles.getProviderServiceMapID());

			}
		}
		for (Long roleObj : resultSet) {
			if (roleObj != null) {
				usersList.add(iEMRUserRepositoryCustom.findUserByUserID(roleObj));
			}
		}
		usersList.removeIf(Objects::isNull);
		return usersList.toString();
	}

	@Override
	public String getUserServicePointVanDetails(Integer userID) {
		// System.out.println("hello");
		Map<String, ArrayList<Map<String, Object>>> responseMap = new HashMap<>();
		List<Object[]> parkingPlaceList = userParkingplaceMappingRepo.getUserParkingPlce(userID);
		Set<Integer> ppS = new HashSet<>();
		ArrayList<Map<String, Object>> parkingPlaceLocationList = new ArrayList<>();
		if (parkingPlaceList.size() > 0) {
			Map<String, Object> parkingPlaceLocationMap;
			for (Object[] obj : parkingPlaceList) {
				ppS.add((Integer) obj[0]);
				parkingPlaceLocationMap = new HashMap<String, Object>();
				parkingPlaceLocationMap.put("stateID", obj[1]);
				parkingPlaceLocationMap.put("stateName", obj[2]);
				parkingPlaceLocationMap.put("districtID", obj[3]);
				parkingPlaceLocationMap.put("districtName", obj[4]);
				parkingPlaceLocationMap.put("blockID", obj[5]);
				parkingPlaceLocationMap.put("blockName", obj[6]);
				parkingPlaceLocationList.add(parkingPlaceLocationMap);
			}
			// System.out.println("hello");
			List<Object[]> vanList = masterVanRepo.getUserVanDatails(ppS);
			Map<String, Object> vMap;
			ArrayList<Map<String, Object>> vanListResponse = new ArrayList<>();
			if (vanList.size() > 0) {
				for (Object[] obj : vanList) {
					vMap = new HashMap<String, Object>();
					vMap.put("vanID", obj[0]);
					vMap.put("vanNO", obj[1]);
					vanListResponse.add(vMap);
				}
			} else {
				vMap = new HashMap<String, Object>();
				vanListResponse.add(vMap);
			}
			// System.out.println("hello");

			List<Object[]> servicePointList = vanServicepointMappingRepo.getuserSpSessionDetails(ppS);
			Map<String, Object> spMap;
			ArrayList<Map<String, Object>> servicePointListResponse = new ArrayList<>();
			if (servicePointList.size() > 0) {
				for (Object[] obj : servicePointList) {
					spMap = new HashMap<String, Object>();
					spMap.put("servicePointID", obj[0]);
					spMap.put("servicePointName", obj[1]);
					spMap.put("sessionType", obj[2]);
					servicePointListResponse.add(spMap);
				}
			} else {
				spMap = new HashMap<String, Object>();
				servicePointListResponse.add(spMap);
			}
			responseMap.put("userVanDetails", vanListResponse);
			responseMap.put("userSpDetails", servicePointListResponse);
			responseMap.put("parkingPlaceLocationList", parkingPlaceLocationList);
		}
		return new GsonBuilder().setLongSerializationPolicy(LongSerializationPolicy.STRING)
				.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create().toJson(responseMap);
	}

	@Override
	public String getServicepointVillages(Integer servicePointID) {
		List<Object[]> servicePointVillageList = servicePointVillageMappingRepo.getServicePointVillages(servicePointID);

		ArrayList<Servicepointvillagemap> villageList = new ArrayList<Servicepointvillagemap>();
		if (servicePointVillageList.size() > 0) {
			Servicepointvillagemap VillageMap;
			for (Object[] obj : servicePointVillageList) {
				VillageMap = new Servicepointvillagemap((Integer) obj[0], (String) obj[1]);
				villageList.add(VillageMap);
			}
		}

		return OutputMapper.gsonWithoutExposeRestriction().toJson(villageList);
	}

	@Override
	public JSONObject generateKeyAndValidateIP(JSONObject responseObj, String ipAddress, String hostName)
			throws JSONException, NoSuchAlgorithmException, IEMRException {
		String key = generateKey(responseObj);
		// commented the below code to restrict IP address and hostname to be sent on UI
//		responseObj.put("loginIPAddress", ipAddress);
//		responseObj.put("ipAddress", ipAddress);
//		responseObj.put("hostName", hostName);
		responseObj = validator.updateCacheObj(responseObj, key, "");
		setConcurrentCheckSessionObject(responseObj, key);
		return responseObj;
	}

	/**
	 * SH20094090,19-04-2022
	 * 
	 * @param responseObj,key
	 * @param key             Function to set new session object whenever a user
	 *                        logs in
	 */
	public void setConcurrentCheckSessionObject(JSONObject responseObj, String key) {
		try {
			if ((responseObj.get("userName")) != null && (responseObj.get("userName").toString()) != null) {
				logger.info("setting key:" + (responseObj.get("userName").toString().trim().toLowerCase()));
				sessionObject.setSessionObject((responseObj.get("userName").toString().trim().toLowerCase()), key);
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}

	}

	// 10-08-2020
	// new service to generate key once OTP validaiton is done
	public JSONObject generateKeyPostOTPValidation(JSONObject responseObj)
			throws JSONException, NoSuchAlgorithmException, IEMRException {
		String key = generateKey(responseObj);
		responseObj = validator.updateCacheObj(responseObj, key, "");
		return responseObj;

	}

	private String generateKey(JSONObject responseObj) throws NoSuchAlgorithmException, JSONException {

		MessageDigest md = MessageDigest.getInstance("SHA-256");
		String key = null;
		JSONObject keyObj = new JSONObject();
		keyObj.put("userName", responseObj.get("userName").toString() + System.currentTimeMillis());
		keyObj.put("userID", responseObj.get("userID").toString() + System.currentTimeMillis());
		// byte[] bytes = md.digest(responseObj.toString().getBytes());
		byte[] bytes = md.digest(keyObj.toString().getBytes());
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
		}
		key = sb.toString();
		return key;
	}

	public String getLocationsByProviderID(String request) throws Exception {
		ArrayList<ProviderServiceAddressMapping> rolesList = new ArrayList<ProviderServiceAddressMapping>();
		ObjectMapper objectMapper = new ObjectMapper();
		UserServiceRoleMapping userRoles = objectMapper.readValue(request, UserServiceRoleMapping.class);
		Set<Object[]> resultSet = null;
		if (userRoles.getRoleID() != null) {
			resultSet = userRoleMappingRepository.getLocationsByProviderID(userRoles.getProviderServiceMapID(),
					userRoles.getRoleID());
		} else {
			resultSet = userRoleMappingRepository.getLocationsByProviderID(userRoles.getProviderServiceMapID());
		}
		for (Object[] locationObject : resultSet) {
			if (locationObject != null && locationObject.length >= 2) {
				rolesList.add((ProviderServiceAddressMapping) locationObject[1]);
			}
		}
		rolesList.removeIf(Objects::isNull);
		return rolesList.toString();
	}

	@Override
	public void forceLogout(ForceLogoutRequestModel request) throws Exception {
		List<User> users = iEMRUserRepositoryCustom.findByUserName(request.getUserName());
		if (users.size() != 1) {
			throw new Exception("Force logout failed due to incorrect username");
		}
		userForceLogout(request, users.get(0));
	}

	@Override
	public void userForceLogout(ForceLogoutRequestModel request) throws Exception {
		List<User> users = iEMRUserRepositoryCustom.findByUserName(request.getUserName());
		if (users.size() != 1) {
			throw new Exception("Force logout failed due to incorrect username");
		}
		try {
			if (!securePassword.validatePasswordExisting(request.getPassword(), users.get(0).getPassword())) {
				throw new Exception("Force logout failed due to incorrect password");
			}
		} catch (Exception e) {
			throw new Exception("Force logout failed due to incorrect password");
		}
		userForceLogout(request, users.get(0));
	}

	private void userForceLogout(ForceLogoutRequestModel request, User user)
			throws NoSuchAlgorithmException, Exception {
		List<UserServiceRoleMapping> usrMappings = userRoleMappingRepository
				.getMappingsByUserIDAndProviderServiceMapID(user.getUserID(), request.getProviderServiceMapID());
		if (usrMappings.size() > 0) {
			for (UserServiceRoleMapping usrMapping : usrMappings) {
				if ((usrMapping.getAgentID() != null) && !usrMapping.getDeleted()) {
					try {
						JSONObject agentLogoutRequest = new JSONObject();
						agentLogoutRequest.put("agent_id", usrMapping.getAgentID());
						OutputResponse response = ctiService.agentLogout(agentLogoutRequest.toString(), "");
						if (!response.isSuccess()
								&& !(response.getErrorMessage().contains("not complete as per the request"))) {
							throw new Exception(response.getErrorMessage());
						}
					} catch (Exception e) {
						// logger.error("Agent logout for agent " + usrMapping.getAgentID() + " failed
						// with error "
						// + e.getMessage(), e);
						throw new Exception("Agent " + usrMapping.getAgentID() + " force logout failed due to error "
								+ e.getMessage());
					}
				}
			}
			if (user.getUserName() != null) {
				String key = deleteConcurrentCheckSessionObject(user.getUserName());
				if (key != null)
					sessionObject.deleteSessionObject(key);
			}
			// sessionObject.deleteSessionObject(generateKey(new
			// JSONObject(user.toString())));
		} else {
			throw new Exception("Force logout failed due to insuffient privilege");
		}
	}

	/**
	 * 
	 * SH20094090,19-04-2022,function added to check and delete session object
	 */
	private String deleteConcurrentCheckSessionObject(String userName) {
		String tokenFromRedis = null;
		try {
			tokenFromRedis = sessionObject.getSessionObject(userName.toLowerCase());
			if (tokenFromRedis != null) {
				sessionObject.deleteSessionObject(userName.toLowerCase());
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return tokenFromRedis;
	}

	@Override
	public String getAgentByRoleID(String request) throws IEMRException, JsonMappingException, JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		UserServiceRoleMapping userRoles = objectMapper.readValue(request, UserServiceRoleMapping.class);
		List<String> resultSet = null;
		List<UserServiceRoleMapping> list = new ArrayList<UserServiceRoleMapping>();

		if (userRoles.getRoleID() != null) {
			resultSet = userRoleMappingRepository.getAgentByRoleID(userRoles.getProviderServiceMapID(),
					userRoles.getRoleID());
		} else {
			resultSet = userRoleMappingRepository.getAgentByProviderServiceMapID(userRoles.getProviderServiceMapID());
		}

		for (String agentID : resultSet) {
			userRoles = new UserServiceRoleMapping();
			userRoles.setAgentID(agentID);
			list.add(userRoles);
		}

		return list.toString();
	}

	@Override
	public List<User> userAuthenticateByEncryption(String req) throws Exception {
		LoginRequestModel m_user2 = InputMapper.gson().fromJson(req, LoginRequestModel.class);
		String jsonreq = RSAUtil.encryptUserDetails(m_user2.getUserName());
		LoginRequestModel m_user = InputMapper.gson().fromJson(jsonreq, LoginRequestModel.class);
		List<User> users = iEMRUserRepositoryCustom.findByUserName(m_user.getUserName());
		if (users.size() != 1) {
			throw new IEMRException("User login failed due to incorrect username/password");
		}
		User user = users.get(0);
		try {
			if (!securePassword.validatePasswordExisting(m_user.getPassword(), user.getPassword())) {
				throw new IEMRException("User login failed due to incorrect username/password");
			}
		} catch (Exception e) {
			throw new IEMRException("User login failed due to incorrect username/password");
		}
		user.setM_UserServiceRoleMapping(getUserServiceRoleMapping(user.getUserID()));
		return users;
	}

	@Autowired
	private RoleRepo roleRepo;

	@Override
	public M_Role getrolewrapuptime(Integer roleID) {
		// TODO Auto-generated method stub
		return roleRepo.findByRoleID(roleID);
	}

	@Override
	public String generateTransactionIdForPasswordChange(User user) throws Exception {

		String token = null;
		Map<String, Object> userMap = new HashMap<String, Object>();
		try {
			token = UUID.randomUUID().toString();
			sessionObject.setSessionObjectForChangePassword((user.getUserID().toString() + user.getUserName()), token);
			userMap.put("transactionId", token);

		} catch (Exception e) {
			logger.error("Error while changing the password: " + e.getMessage(), e);
			throw new IEMRException("Error while changing the password: " + e.getMessage());
		}
		if (token != null)

			return new Gson().toJson(userMap);

		else
			throw new IEMRException("Error while changing the password");

	}

	@Override
	public String validateQuestionAndAnswersForPasswordChange(JsonObject request) throws Exception {

		QuestionAnswerValidateModel[] userRequestArr = InputMapper.gson()
				.fromJson(request.get("SecurityQuesAns").toString(), QuestionAnswerValidateModel[].class);

		try {
			if (request.has("userName") && request.get("userName") != null) {
				List<User> users = iEMRUserRepositoryCustom.findByUserName(request.get("userName").getAsString());
				if (users.size() != 1) {
					throw new IEMRException("User does not exist or is not active or more than 1 user found");
				}
				User user = users.get(0);
				sessionObject.deleteSessionObject((user.getUserID().toString() + user.getUserName()));

				if (userRequestArr != null) {
					int pointer = 0;
					for (QuestionAnswerValidateModel securityAnswers : userRequestArr) {
						UserSecurityQMapping userSecurityQuestionAnswers = null;
						userSecurityQuestionAnswers = iEMRUserRepositoryCustom.verifySecurityQuestionAnswers(
								user.getUserID(), securityAnswers.getQuestionId(), securityAnswers.getAnswer());

						if (userSecurityQuestionAnswers == null
								|| userSecurityQuestionAnswers.getUserSecurityQAID() == null)
							throw new IEMRException("Security answers does not match");

						pointer++;
					}

					if (pointer == 3)
						return generateTransactionIdForPasswordChange(user);
					else
						throw new IEMRException("Invalid questions, validation failed, please contact administrator");
				} else
					throw new IEMRException("Invalid questions, validation failed, please contact administrator");

			} else
				throw new IEMRException("Invalid/NULL user name");
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new IEMRException(e.getMessage());
		}

	}

	

	public String generateStrongPasswordForExistingUser(String password)
			throws NoSuchAlgorithmException, InvalidKeySpecException {
		int iterations = 1000;
		char[] chars = password.toCharArray();
		byte[] salt = getSalt();

		PBEKeySpec spec = new PBEKeySpec(chars, salt, iterations, 512);
		SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
		byte[] hash = skf.generateSecret(spec).getEncoded();
		return iterations + ":" + toHex(salt) + ":" + toHex(hash);
	}

	private byte[] getSalt() throws NoSuchAlgorithmException {
		SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
		byte[] salt = new byte[16];
		sr.nextBytes(salt);
		return salt;
	}

	private String toHex(byte[] array) throws NoSuchAlgorithmException {
		BigInteger bi = new BigInteger(1, array);
		String hex = bi.toString(16);
		int paddingLength = array.length * 2 - hex.length();
		if (paddingLength > 0) {
			return String.format(new StringBuilder().append("%0").append(paddingLength).append("d").toString(),
					new Object[] { Integer.valueOf(0) }) + hex;
		}
		return hex;
	}
	
	public User getUserById(Long userId) throws IEMRException {
	    try {
	        // Fetch user from custom repository by userId
	        User user = iEMRUserRepositoryCustom.findByUserID(userId);
	        
	        // Check if user is found
	        if (user == null) {
	            throw new IEMRException("User not found with ID: " + userId);
	        }
	        
	        return user;
	    } catch (Exception e) {
	        // Log and throw custom exception in case of errors
	        logger.error("Error fetching user with ID: " + userId, e);
	        throw new IEMRException("Error fetching user with ID: " + userId, e);
	    }
	}
}
