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
package com.iemr.common.controller.notification;

import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.iemr.common.service.notification.NotificationService;
import com.iemr.common.utils.response.OutputResponse;
import io.lettuce.core.dynamic.annotation.Param;
import io.swagger.v3.oas.annotations.Operation;



@RequestMapping({ "/notification" })
@RestController
public class NotificationController {
	private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	private NotificationService notificationService;

	@Autowired
	public void setNotificationService(NotificationService notificationService) {
		this.notificationService = notificationService;
	}

	
	@Operation(summary = "Get notification")
	@RequestMapping(value = "/getNotification", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String getNotification(@Param(value = "{\"providerServiceMapID\": \"integer-provider service map id\", "
			+ "\"notificationTypeID\": \"integer-notification type id\", \"userIDs\": \"[integer-user id]\", "
			+ "\"workingLocationIDs\": \"[integer-working Location id]\", \"languageIDs\": \"[integer-language ids]\", "
			+ "\"roleIDs\":\"[ integer array- multiple roles available for the selected service]\", "
			+ "\"validFrom\": \"EPOC date\", validTill:\"EPOC date\"}") @RequestBody String request) {
		OutputResponse response = new OutputResponse();
		logger.info("getNotification request " + request);
		try {
			response.setResponse(notificationService.getNotification(request));
		} catch (Exception e) {
			logger.info("getNotification failed with error " + e.getMessage(), e);
			response.setError(e);
		}
		logger.info("getNotification response " + response.toString());
		return response.toString();
	}

	
	@Operation(summary = "Get supervisor notification")
	@RequestMapping(value = "/getSupervisorNotification", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String getSupervisorNotification(
			@Param(value = "{\"providerServiceMapID\": \"integer-provider service map id\", \"notificationTypeID\":\"Integer\", "
					+ "\"userIDs\": \"Optional - [integer-user id]\", \"workingLocationIDs\": \"Optional - [integer-working Location id]\", "
					+ "\"languageIDs\": \"Optional - [integer-language ids]\", \"validStartDate\":\"Optional - timestamp\", "
					+ "\"validEndDate\":\"Optional - timestamp\", \"roleIDs\":\"Optional - [Array of role ids]\"}") @RequestBody String request) {
		OutputResponse response = new OutputResponse();
		logger.info("getSupervisorNotification request " + request);
		try {
			response.setResponse(notificationService.getSupervisorNotification(request));
		} catch (Exception e) {
			logger.info("getSupervisorNotification failed with error " + e.getMessage(), e);
			response.setError(e);
		}
		logger.info("getSupervisorNotification response " + response.toString());
		return response.toString();
	}

	
	@Operation(summary = "Create notification")
	@RequestMapping(value = "/createNotification", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String createNotification(
			@Param(value = "[{\"providerServiceMapID\": \"integer-provider service map id\", "
					+ "\"notificationTypeID\": \"integer-notification type id\", \"roleID\": \"integer-role id of the user role\", "
					+ "\"userID\":\"Integer: user ID of the user to whom notification will be sent\", "
					+ "\"workingLocationID\":\"Integer: office location to which notification will be sent\", "
					+ "\"languageID\":\"Integer: language to which notification will be sent\", "
					+ "\"createdBy\": \"User name of the supervisor creating notification\", \"notification\":\"String - notification subject\", "
					+ "\"notificationDesc\":\"String- notificationDesc\", \"validFrom\": \"EPOC date\", \"validTill\":\"EPOC date\", "
					+ "\"kmFileManager\":{\"fileName\":\"String: name of file\", \"fileExtension\":\"String: file extension\", "
					+ "\"providerServiceMapID\":\"Integer: service provider map ID\", \"validFrom\":\"Epoch date time: Document validity start time\", "
					+ "\"validUpto\":\"Epoch date time: Document validity end time\", \"fileContent\":\"String: base64encoded binary file content\", "
					+ "\"createdBy\":\"String: username of supervisor\", \"categoryID\":\"Optional: Integer: category ID selected\", "
					+ "\"subCategoryID\":\"Optional: Integer: sub-category ID selected\"}}...]") @RequestBody String request) {
		OutputResponse response = new OutputResponse();
		logger.info("createNotification request " + request);
		try {
			response.setResponse(notificationService.createNotification(request));
		} catch (Exception e) {
			logger.info("createNotification failed with error " + e.getMessage(), e);
			response.setError(e);
		}
		logger.info("createNotification response " + response.toString());
		return response.toString();
	}

	
	@Operation(summary = "Update notification")
	@RequestMapping(value = "/updateNotification", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String updateNotification(
			@Param(value = "{\"notificationID\" : \"Integer - Notification ID of the notification that needs to be updated\", "
					+ "\"notification\":\"String - notification name\", \"notificationDesc\":\"String - notificationDesc\", "
					+ "\"notificationTypeID\":\"Integer - notificationTypeID\", \"roleID\":\"Integer - roleID\", "
					+ "\"validFrom\":\"Epoch date - validFrom\", \"validTill\":\"Epoch date - validTill\", \"deleted\":\"Boolean - deleted\", "
					+ "\"modifiedBy\":\"String - username of the modifying supervisor\", \"kmFileManager\":{\"fileName\":\"String: name of file\", "
					+ "\"fileExtension\":\"String: file extension\", \"providerServiceMapID\":\"Integer: service provider map ID\", "
					+ "\"userID\":\"Integer: user ID of the supervisor\", \"validFrom\":\"Epoch date time: Document validity start time\", "
					+ "\"validUpto\":\"Epoch date time: Document validity end time\", \"fileContent\":\"String: base64encoded binary file content\", "
					+ "\"createdBy\":\"String: username of supervisor\", \"categoryID\":\"Optional: Integer: category ID selected\", "
					+ "\"subCategoryID\":\"Optional: Integer: sub-category ID selected\"}}") @RequestBody String request) {
		OutputResponse response = new OutputResponse();
		logger.info("updateNotification request " + request);
		try {
			response.setResponse(notificationService.updateNotification(request));
		} catch (Exception e) {
			logger.info("updateNotification failed with error " + e.getMessage(), e);
			response.setError(e);
		}
		logger.info("updateNotification response " + response.toString());
		return response.toString();
	}

	
	@Operation(summary = "Get notification type")
	@RequestMapping(value = "/getNotificationType", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String getNotificationType(
			@Param(value = "{\"providerServiceMapID\" : \"Integer - providerServiceMapID\"}") @RequestBody String request) {
		OutputResponse response = new OutputResponse();
		logger.info("getNotificationType request " + request);
		try {
			response.setResponse(notificationService.getNotificationType(request));
		} catch (Exception e) {
			logger.info("getNotificationType failed with error " + e.getMessage(), e);
			response.setError(e);
		}
		logger.info("getNotificationType response " + response.toString());
		return response.toString();
	}

	
	@Operation(summary = "Create notification type")
	@RequestMapping(value = "/createNotificationType", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String createNotificationType(
			@Param(value = "{\"providerServiceMapID\" : \"Integer - providerServiceMapID\", "
					+ "\"notificationType\":\"String - notificationType name\", \"notificationTypeDesc\":\"String - notificationTypeDesc\", "
					+ "\"createdBy\":\"String - username of the supervisor\"}") @RequestBody String request) {
		OutputResponse response = new OutputResponse();
		logger.info("createNotificationType request " + request);
		try {
			response.setResponse(notificationService.createNotificationType(request));
		} catch (Exception e) {
			logger.info("createNotificationType failed with error " + e.getMessage(), e);
			response.setError(e);
		}
		logger.info("createNotificationType response " + response.toString());
		return response.toString();
	}

	
	@Operation(summary = "Update notification type")
	@RequestMapping(value = "/updateNotificationType", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String updateNotificationType(
			@Param(value = "{\"notificationTypeID\" : \"Integer - notificationTypeID\", \"notificationType\":\"String - notificationType name\", "
					+ "\"notificationTypeDesc\":\"String - notificationTypeDesc\", \"deleted\":\"Boolean\""
					+ "\"modifiedBy\":\"String - username of the supervisor\"}") @RequestBody String request) {
		OutputResponse response = new OutputResponse();
		logger.info("updateNotificationType request " + request);
		try {
			response.setResponse(notificationService.updateNotificationType(request));
		} catch (Exception e) {
			logger.info("updateNotificationType failed with error " + e.getMessage(), e);
			response.setError(e);
		}
		logger.info("updateNotificationType response " + response.toString());
		return response.toString();
	}

	
	@Operation(summary = "Get emergency contacts")
	@RequestMapping(value = "/getEmergencyContacts", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String getEmergencyContacts(
			@Param(value = "{\"providerServiceMapID\": \"integer-provider service map id\", "
					+ "\"notificationTypeID\": \"integer-notification type id\"}") @RequestBody String request) {
		OutputResponse response = new OutputResponse();
		logger.info("getEmergencyContacts request " + request);
		try {
			response.setResponse(notificationService.getEmergencyContacts(request));
		} catch (Exception e) {
			logger.info("getEmergencyContacts failed with error " + e.getMessage(), e);
			response.setError(e);
		}
		logger.info("getEmergencyContacts response " + response.toString());
		return response.toString();
	}

	
	@Operation(summary = "Get supervisor emergency contacts")
	@RequestMapping(value = "/getSupervisorEmergencyContacts", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String getSupervisorEmergencyContacts(
			@Param(value = "{\"providerServiceMapID\": \"integer-provider service map id\", \"notificationTypeID\":\"Integer\"}") @RequestBody String request) {
		OutputResponse response = new OutputResponse();
		logger.info("getSupervisorEmergencyContacts request " + request);
		try {
			response.setResponse(notificationService.getSupervisorEmergencyContacts(request));
		} catch (Exception e) {
			logger.info("getSupervisorEmergencyContacts failed with error " + e.getMessage(), e);
			response.setError(e);
		}
		logger.info("getSupervisorEmergencyContacts response " + response.toString());
		return response.toString();
	}

	
	@Operation(summary = "Create emergency contact")
	@RequestMapping(value = "/createEmergencyContacts", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String createEmergencyContacts(
			@Param(value = "[{\"providerServiceMapID\": \"integer-provider service map id\", "
					+ "\"notificationTypeID\": \"integer-notification type id\", \"createdBy\": \"User name of the supervisor creating notification\", "
					+ "\"designationID\":\"Integer - designation ID\", \"emergContactName\":\"String - emergency Contact name\", "
					+ "\"location\":\"String - emergency Contact location\", \"emergContactNo\":\"String - emergency Contact No\", "
					+ "\"emergContactDesc\": \"String - emergency Contact Desc\", \"notificationTypeID\":\"Integer: notification Type ID\", "
					+ "\"createdBy\":\"String: username of supervisor\"}...]") @RequestBody String request) {
		OutputResponse response = new OutputResponse();
		logger.info("createEmergencyContacts request " + request);
		try {
			response.setResponse(notificationService.createEmergencyContacts(request));
		} catch (Exception e) {
			logger.info("createEmergencyContacts failed with error " + e.getMessage(), e);
			response.setError(e);
		}
		logger.info("createEmergencyContacts response " + response.toString());
		return response.toString();
	}

	
	@Operation(summary = "Update emergency contacts")
	@RequestMapping(value = "/updateEmergencyContacts", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String updateEmergencyContacts(
			@Param(value = "{\"providerServiceMapID\": \"integer-provider service map id\", "
					+ "\"notificationTypeID\": \"integer-notification type id\", \"createdBy\": \"User name of the supervisor creating notification\", "
					+ "\"designationID\":\"Integer - designation ID\", \"emergContactName\":\"String - emergency Contact name\", "
					+ "\"location\":\"String - emergency Contact location\", \"emergContactNo\":\"String - emergency Contact No\", "
					+ "\"emergContactDesc\": \"String - emergency Contact Desc\", \"notificationTypeID\":\"Integer: notification Type ID\", "
					+ "\"createdBy\":\"String: username of supervisor\"}") @RequestBody String request) {
		OutputResponse response = new OutputResponse();
		logger.info("updateEmergencyContacts request " + request);
		try {
			response.setResponse(notificationService.updateEmergencyContacts(request));
		} catch (Exception e) {
			logger.info("updateEmergencyContacts failed with error " + e.getMessage(), e);
			response.setError(e);
		}
		logger.info("updateEmergencyContacts response " + response.toString());
		return response.toString();
	}

}
