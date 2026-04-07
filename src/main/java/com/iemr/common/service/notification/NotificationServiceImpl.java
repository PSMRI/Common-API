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
package com.iemr.common.service.notification;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iemr.common.data.institute.Designation;
import com.iemr.common.data.kmfilemanager.KMFileManager;
import com.iemr.common.data.notification.EmergencyContacts;
import com.iemr.common.data.notification.Notification;
import com.iemr.common.data.notification.NotificationType;
import com.iemr.common.data.userbeneficiarydata.Language;
import com.iemr.common.data.users.ProviderServiceMapping;
import com.iemr.common.data.users.Role;
import com.iemr.common.data.users.User;
import com.iemr.common.data.users.WorkLocation;
import com.iemr.common.repository.notification.EmergencyContactsRepository;
import com.iemr.common.repository.notification.NotificationRepository;
import com.iemr.common.repository.notification.NotificationTypeRepository;
import com.iemr.common.service.kmfilemanager.KMFileManagerService;
import com.iemr.common.utils.config.ConfigProperties;
import com.iemr.common.utils.exception.IEMRException;
import com.iemr.common.utils.gateway.email.EmailService;
import com.iemr.common.utils.mapper.InputMapper;

@Service
public class NotificationServiceImpl implements NotificationService
{
	private InputMapper inputMapper = new InputMapper();

	private EmailService emailService;


	@Value("${km-base-path}")
	private String dmsPath;
	
	@Value("${km-guest-user}")
	private String userName;
	
	@Value("${km-guest-password}")
	private String userPassword;
	
	@Value("${km-base-protocol}")
	private String dmsProtocol;	
	
	@Autowired
	public void setEmailService(EmailService emailService)
	{
		this.emailService = emailService;
	}
	// private ConfigProperties configProperties;
	//
	// @Autowired
	// public void setConfigProperties(ConfigProperties configProperties)
	// {
	// this.configProperties = configProperties;
	// }

	private Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);

	private NotificationRepository notificationRepository;

	@Autowired
	public void setNotificationRepository(NotificationRepository notificationRepository)
	{
		this.notificationRepository = notificationRepository;
	}

	private NotificationTypeRepository notificationTypeRepository;

	@Autowired
	public void setNotificationTypeRepository(NotificationTypeRepository notificationTypeRepository)
	{
		this.notificationTypeRepository = notificationTypeRepository;
	}

	private KMFileManagerService kmFileManagerService;

	@Autowired
	public void setKmFileManagerService(KMFileManagerService kmFileManagerService)
	{
		this.kmFileManagerService = kmFileManagerService;
	}
	@Autowired
	private EmergencyContactsRepository emergencyContactsRepository;

//	@Autowired
//	public void setKmFileManagerService(EmergencyContactsRepository emergencyContactsRepository)
//	{
//		this.emergencyContactsRepository = emergencyContactsRepository;
//	}

	@Override
	public String getNotification(String request) throws IEMRException, JsonMappingException, JsonProcessingException
	{
		ArrayList<Notification> notifications = new ArrayList<Notification>();
		ObjectMapper objectMapper = new ObjectMapper();
		Notification notificationRequest = objectMapper.readValue(request, Notification.class);
		Integer providerServiceMapID = notificationRequest.getProviderServiceMapID();
		Integer notificationTypeID = notificationRequest.getNotificationTypeID();
		List<Integer> roleIDs = notificationRequest.getRoleIDs();
		Timestamp validFrom = notificationRequest.getValidFrom();
		Timestamp validTill = notificationRequest.getValidTill();
		List<Integer> userIDs = notificationRequest.getUserIDs();
		List<Integer> languageIDs = notificationRequest.getLanguageIDs();
		List<Integer> workLocationIDs = notificationRequest.getWorkingLocationIDs();
		Set<Object[]> resultSet = null;
		if (userIDs != null)
		{
			resultSet = notificationRepository.getUserNotifications(providerServiceMapID, notificationTypeID,
					notificationRequest.getValidFrom(), notificationRequest.getValidTill(), userIDs);
		} else if (languageIDs != null)
		{
			resultSet = notificationRepository.getLanguageNotifications(providerServiceMapID, notificationTypeID,
					validFrom, validTill, languageIDs);
		} else if (workLocationIDs != null && roleIDs == null)
		{
			resultSet = notificationRepository.getLocationNotifications(providerServiceMapID, notificationTypeID,
					validFrom, validTill, workLocationIDs);
		} else
		{
			resultSet = notificationRepository.getRoleNotifications(providerServiceMapID, notificationTypeID, roleIDs,
					workLocationIDs, validFrom, notificationRequest.getValidTill());
		}
		// notification.workingLocationID, notification.workingLocation, notification.languageID, notification.language,
		// notification.userID, notification.user
		for (Object[] notificationItems : resultSet)
		{
			if (notificationItems != null && notificationItems.length >= 19)
			{
				Notification notification = Notification.initializeNotification((Integer) notificationItems[0],
						(String) notificationItems[1], (String) notificationItems[2], (Integer) notificationItems[3],
						(NotificationType) notificationItems[4], (Integer) notificationItems[5],
						(Role) notificationItems[6], (Integer) notificationItems[7], (Timestamp) notificationItems[8],
						(Timestamp) notificationItems[9], (Boolean) notificationItems[10],
						(Integer) notificationItems[11], (KMFileManager) notificationItems[12],
						(Integer) notificationItems[13], (WorkLocation) notificationItems[14],
						(Integer) notificationItems[15], (Language) notificationItems[16],
						(Integer) notificationItems[17], (User) notificationItems[18]);
				notification.setKMFilePath(getFilePath((KMFileManager) notificationItems[12]));
				notifications.add(notification);
			}
		}
		return notifications.toString();
	}

	@Override
	public String getSupervisorNotification(String request) throws IEMRException, JsonMappingException, JsonProcessingException
	{
		ArrayList<Notification> notifications = new ArrayList<Notification>();
		ObjectMapper objectMapper = new ObjectMapper();
		Notification notificationRequest = objectMapper.readValue(request, Notification.class);
		Set<Object[]> resultSet = null;
		Calendar cal = Calendar.getInstance();
		Timestamp validStartDate = new Timestamp(cal.getTimeInMillis());
		cal.add(Calendar.DATE, 7);
		Timestamp validEndDate = new Timestamp(cal.getTimeInMillis());
		if (notificationRequest.getValidStartDate() != null)
		{
			validStartDate = notificationRequest.getValidStartDate();
		}
		if (notificationRequest.getValidEndDate() != null)
		{
			validEndDate = notificationRequest.getValidEndDate();
		}

		List<Integer> userIDs = notificationRequest.getUserIDs();
		List<Integer> languageIDs = notificationRequest.getLanguageIDs();
		List<Integer> locationIDs = notificationRequest.getWorkingLocationIDs();
		List<Integer> roleIDs = notificationRequest.getRoleIDs();
		if (userIDs != null)
		{
			resultSet = notificationRepository.getSupervisorNotificationsByUser(
					notificationRequest.getProviderServiceMapID(), validStartDate, validEndDate, userIDs,
					notificationRequest.getNotificationTypeID());
		} else if (languageIDs != null)
		{
			resultSet = notificationRepository.getSupervisorNotificationsByLanguage(
					notificationRequest.getProviderServiceMapID(), validStartDate, validEndDate, languageIDs,
					notificationRequest.getNotificationTypeID());
		} else if (locationIDs != null && roleIDs == null)
		{
			resultSet = notificationRepository.getSupervisorNotificationsByLocation(
					notificationRequest.getProviderServiceMapID(), validStartDate, validEndDate, locationIDs,
					notificationRequest.getNotificationTypeID());
		} else if (roleIDs == null || roleIDs.size() == 0)
		{
			resultSet = notificationRepository.getSupervisorNotifications(notificationRequest.getProviderServiceMapID(),
					validStartDate, validEndDate, notificationRequest.getNotificationTypeID());
		} else
		{
			// resultSet = notificationRepository.getSupervisorNotificationsByRole(
			// notificationRequest.getProviderServiceMapID(), validStartDate, validEndDate, roleIDs,
			// notificationRequest.getNotificationTypeID(), locationIDs);
			resultSet = notificationRepository.getSupervisorNotificationsByRole(
					notificationRequest.getProviderServiceMapID(), validStartDate, validEndDate, roleIDs,
					notificationRequest.getNotificationTypeID());
		}
		for (Object[] notificationItems : resultSet)
		{
			if (notificationItems != null && notificationItems.length >= 19)
			{
				Notification notification = Notification.initializeNotification((Integer) notificationItems[0],
						(String) notificationItems[1], (String) notificationItems[2], (Integer) notificationItems[3],
						(NotificationType) notificationItems[4], (Integer) notificationItems[5],
						(Role) notificationItems[6], (Integer) notificationItems[7], (Timestamp) notificationItems[8],
						(Timestamp) notificationItems[9], (Boolean) notificationItems[10],
						(Integer) notificationItems[11], (KMFileManager) notificationItems[12],
						(Integer) notificationItems[13], (WorkLocation) notificationItems[14],
						(Integer) notificationItems[15], (Language) notificationItems[16],
						(Integer) notificationItems[17], (User) notificationItems[18]);
				notification.setKMFilePath(getFilePath((KMFileManager) notificationItems[12]));
				notifications.add(notification);
			}
		}
		return notifications.toString();
	}

	@Override
	public String createNotification(String request) throws Exception
	{
		ArrayList<Notification> savedNotifications = new ArrayList<Notification>();
		ObjectMapper objectMapper = new ObjectMapper();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata")); // Set the timezone to IST
		objectMapper.setDateFormat(dateFormat);
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		Notification[] notificationRequests = objectMapper.readValue(request, Notification[].class);
		for (Notification notificationRequest : notificationRequests)
		{
			Notification notification = addNewNotification(notificationRequest);
			savedNotifications.add(notification);
		}
		return savedNotifications.toString();
	}

	private Notification addNewNotification(Notification notificationRequest) throws Exception
	{
		ObjectMapper objectMapper = new ObjectMapper();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata")); // Set the timezone to IST
		objectMapper.setDateFormat(dateFormat);
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		Notification notification = objectMapper.readValue(notificationRequest.toString(), Notification.class);
		notification.setKmFileManager(null);
		notification = notificationRepository.save(notification);
		notificationRequest.setNotificationID(notification.getNotificationID());
		Integer kmFileManagerID = uploadNotificationFileInKM(notificationRequest);
		if (notificationRequest.getDeleted() == null)
		{
			notificationRequest.setDeleted(false);
		}
		if (kmFileManagerID > 0)
		{
			notificationRequest.setKmFileManagerID(kmFileManagerID);
			updateKMFileIDInNotification(notificationRequest);
		}
		return notification;
	}

	private Integer uploadNotificationFileInKM(Notification notificationRequest) throws Exception
	{
		Integer kmFileManagerID = 0;
		notificationRequest = updateNotificationFile(notificationRequest);
		if (notificationRequest != null)
		{
			kmFileManagerID = notificationRequest.getKmFileManagerID();
			notificationRequest.setKmFileManagerID(kmFileManagerID);
		}
		return kmFileManagerID;
	}

	// @Transactional(propagation = Propagation.REQUIRES_NEW)
	private Notification updateNotificationFile(Notification notificationRequest) throws Exception
	{
		Notification notificationResponse = null;
		if (notificationRequest.getKmFileManager() != null
				&& notificationRequest.getKmFileManager().getFileContent() != null
				&& notificationRequest.getKmFileManager().getFileExtension() != null
				&& notificationRequest.getKmFileManager().getFileName() != null)
		{
			// try
			// {
			List<KMFileManager> list = new ArrayList<KMFileManager>();
			list.add(notificationRequest.getKmFileManager());
			String kmFileManagerResp = kmFileManagerService.addKMFile(list.toString());
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			KMFileManager[] kmFileManagerArray = objectMapper.readValue(kmFileManagerResp, KMFileManager[].class);
			//KMFileManager[] kmFileManagerArray = inputMapper.gson().fromJson(kmFileManagerResp, KMFileManager[].class);
			for (KMFileManager kmFileManager : kmFileManagerArray)
			{
				notificationRequest.setKmFileManagerID(kmFileManager.getKmFileManagerID());
			}
			if (kmFileManagerArray.length > 0)
			{
				notificationResponse = objectMapper.readValue(notificationRequest.toString(), Notification.class);
			}
			// } catch (Exception e)
			// {
			// logger.error("Adding file to KM Failed with exception " +
			// e.getMessage(), e);
			// }
		}
		return notificationResponse;
	}

	// @Transactional(propagation = Propagation.REQUIRES_NEW)
	private void updateKMFileIDInNotification(Notification notificationRequest)
	{
		notificationRepository.updateNotification(notificationRequest.getNotificationID(),
				notificationRequest.getKmFileManagerID(), notificationRequest.getDeleted());
	}

	@Override
	// @Transactional(propagation = Propagation.REQUIRES_NEW)
	public String updateNotification(String request) throws Exception
	{
		ObjectMapper objectMapper = new ObjectMapper();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata")); // Set the timezone to IST
		objectMapper.setDateFormat(dateFormat);
		Notification notificationRequest = objectMapper.readValue(request, Notification.class);
		JSONObject response = new JSONObject(notificationRequest.toString());
		int count = notificationRepository.updateNotification(notificationRequest.getNotificationID(),
				notificationRequest.getNotification(), notificationRequest.getNotificationDesc(),
				notificationRequest.getNotificationTypeID(), notificationRequest.getRoleID(),
				notificationRequest.getValidFrom(), notificationRequest.getValidTill(),
				notificationRequest.getDeleted(), notificationRequest.getModifiedBy());

		if (notificationRequest.getKmFileManager() != null)
		{
			notificationRequest.setKmFileManagerID(uploadNotificationFileInKM(notificationRequest));
			updateKMFileIDInNotification(notificationRequest);
		}
		response.put("updatecount", count);
		return response.toString();
	}

	@Override
	public String getNotificationType(String request) throws IEMRException
	{
		ArrayList<NotificationType> notificationTypes = new ArrayList<NotificationType>();
		//NotificationType notificationType = inputMapper.gson().fromJson(request, NotificationType.class);
		// Set<Object[]> resultSet = notificationTypeRepository
		// .getNotificationTypes(notificationType.getProviderServiceMapID());
		Set<Object[]> resultSet = notificationTypeRepository.getNotificationTypes();
		for (Object[] notificationTypeObjs : resultSet)
		{
			if (notificationTypeObjs != null && notificationTypeObjs.length >= 4)
			{
				notificationTypes.add(NotificationType.initializeNotificationTypes((Integer) notificationTypeObjs[0],
						(String) notificationTypeObjs[1], (String) notificationTypeObjs[2],
						(Boolean) notificationTypeObjs[3]));
			}
		}
		return notificationTypes.toString();
	}

	@Override
	// @Transactional(propagation = Propagation.REQUIRES_NEW)
	public String createNotificationType(String request) throws IEMRException, JsonMappingException, JsonProcessingException
	{
		ArrayList<NotificationType> savedNotificationTypes = new ArrayList<NotificationType>();
		ObjectMapper objectMapper = new ObjectMapper();
		NotificationType[] notificationTypeRequest = objectMapper.readValue(request, NotificationType[].class);
		savedNotificationTypes =
				(ArrayList<NotificationType>) notificationTypeRepository.saveAll(Arrays.asList(notificationTypeRequest));
		return savedNotificationTypes.toString();
	}

	@Override
	// @Transactional(propagation = Propagation.REQUIRES_NEW)
	public String updateNotificationType(String request) throws JSONException, IEMRException
	{
		NotificationType notificationType = inputMapper.gson().fromJson(request, NotificationType.class);
		JSONObject response = new JSONObject(notificationType.toString());
		int count = notificationTypeRepository.updateNotificationType(notificationType.getNotificationTypeID(),
				notificationType.getNotificationType(), notificationType.getNotificationTypeDesc(),
				notificationType.getDeleted(), notificationType.getModifiedBy());
		response.put("updatecount", count);
		return response.toString();
	}

	private String getFilePath(KMFileManager kmFileManager)
	{
		String fileUIDAsURI = null;
		if (kmFileManager != null && kmFileManager.getFileUID() != null)
		{
			String fileUID = kmFileManager.getFileUID();
			// String dmsPath = ConfigProperties.getPropertyByName("km-base-path");
			// String dmsProtocol = ConfigProperties.getPropertyByName("km-base-protocol");
			// String userName = ConfigProperties.getPropertyByName("km-guest-user");
			// String userPassword = ConfigProperties.getPassword("km-guest-user");
			fileUIDAsURI =
					dmsProtocol + "://" + userName + ":" + userPassword + "@" + dmsPath + "/Download?uuid=" + fileUID;
		}
		return fileUIDAsURI;
	}

	@Override
	public String getEmergencyContacts(String request) throws IEMRException
	{
		List<EmergencyContacts> emergencyContacts = new ArrayList<EmergencyContacts>();
		EmergencyContacts emergencyContactRequest = inputMapper.gson().fromJson(request, EmergencyContacts.class);
		Integer providerServiceMapID = emergencyContactRequest.getProviderServiceMapID();
		Integer notificationTypeID = emergencyContactRequest.getNotificationTypeID();
		// emergencyContacts = emergencyContactsRepository.getEmergencyContacts(providerServiceMapID,
		// notificationTypeID);
		Set<Object[]> resultSet = null;

		resultSet = emergencyContactsRepository.getEmergencyContacts(providerServiceMapID, notificationTypeID);
		for (Object[] emergencyContact : resultSet)
		{
			if (emergencyContact != null && emergencyContact.length >= 13)
			{
				EmergencyContacts notification = EmergencyContacts.initializeEmergencyContacts(
						(Integer) emergencyContact[0], (Integer) emergencyContact[1], (String) emergencyContact[2],
						(String) emergencyContact[3], (String) emergencyContact[4], (Integer) emergencyContact[5],
						(ProviderServiceMapping) emergencyContact[6], (Integer) emergencyContact[7],
						(NotificationType) emergencyContact[8], (String) emergencyContact[9],
						(Boolean) emergencyContact[10], (Timestamp) emergencyContact[11],
						(Designation) emergencyContact[12]);
				emergencyContacts.add(notification);
			}
		}
		return emergencyContacts.toString();
	}

	@Override
	public String getSupervisorEmergencyContacts(String request) throws IEMRException, JsonMappingException, JsonProcessingException
	{
		List<EmergencyContacts> emergencyContacts = new ArrayList<EmergencyContacts>();
		ObjectMapper objectMapper = new ObjectMapper();	
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		EmergencyContacts emergencyContactRequest =objectMapper.readValue(request, EmergencyContacts.class);
		//EmergencyContacts emergencyContactRequest = inputMapper.gson().fromJson(request, EmergencyContacts.class);
		Integer providerServiceMapID = emergencyContactRequest.getProviderServiceMapID();
		Integer notificationTypeID = emergencyContactRequest.getNotificationTypeID();
		// emergencyContacts = emergencyContactsRepository.getSupervisorEmergencyContacts(providerServiceMapID,
		// notificationTypeID);
		Set<Object[]> resultSet = null;

		resultSet =
				emergencyContactsRepository.getSupervisorEmergencyContacts(providerServiceMapID, notificationTypeID);
		for (Object[] emergencyContact : resultSet)
		{
			if (emergencyContact != null && emergencyContact.length >= 13)
			{
				EmergencyContacts notification = EmergencyContacts.initializeEmergencyContacts(
						(Integer) emergencyContact[0], (Integer) emergencyContact[1], (String) emergencyContact[2],
						(String) emergencyContact[3], (String) emergencyContact[4], (Integer) emergencyContact[5],
						(ProviderServiceMapping) emergencyContact[6], (Integer) emergencyContact[7],
						(NotificationType) emergencyContact[8], (String) emergencyContact[9],
						(Boolean) emergencyContact[10], (Timestamp) emergencyContact[11],
						(Designation) emergencyContact[12]);
				emergencyContacts.add(notification);
			}
		}
		return emergencyContacts.toString();
	}

	@Override
	public String createEmergencyContacts(String request)
			throws NoSuchAlgorithmException, IOException, IEMRException, Exception
	{
		List<EmergencyContacts> emergencyContacts = new ArrayList<EmergencyContacts>();
		ObjectMapper objectMapper = new ObjectMapper();
		EmergencyContacts[] emergencyContactRequest = objectMapper.readValue(request, EmergencyContacts[].class);
		for (EmergencyContacts emergencyContact : emergencyContactRequest)
		{
			emergencyContacts.add(emergencyContactsRepository.save(emergencyContact));
		}
		return emergencyContacts.toString();
	}

	@Override
	public String updateEmergencyContacts(String request)
			throws JSONException, NoSuchAlgorithmException, IOException, IEMRException, Exception
	{
		Integer updateCount = 0;
		String result = "Failed to update the given request.";
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		EmergencyContacts emergencyContactRequest = objectMapper.readValue(request, EmergencyContacts.class);
		updateCount = emergencyContactsRepository.updateEmergencyContacts(emergencyContactRequest.getEmergContactID(),
				emergencyContactRequest.getDesignationID(), emergencyContactRequest.getEmergContactNo(),
				emergencyContactRequest.getEmergContactDesc(), emergencyContactRequest.getProviderServiceMapID(),
				emergencyContactRequest.getNotificationTypeID(), emergencyContactRequest.getDeleted(),
				emergencyContactRequest.getModifiedBy(), emergencyContactRequest.getEmergContactName(),
				emergencyContactRequest.getLocation());
		if (updateCount > 0)
		{
			result = "Updated the " + updateCount + " rows for given request.";
		}
		return result;
	}

}