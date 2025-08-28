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

import org.json.JSONException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.iemr.common.utils.exception.IEMRException;

public interface NotificationService {
	String getNotification(String request) throws IEMRException, JsonMappingException, JsonProcessingException;

	String getSupervisorNotification(String request) throws IEMRException, JsonMappingException, JsonProcessingException;

	String createNotification(String request) throws NoSuchAlgorithmException, IOException, IEMRException, Exception;

	String updateNotification(String request)
			throws JSONException, NoSuchAlgorithmException, IOException, IEMRException, Exception;

	String getNotificationType(String request) throws IEMRException;

	String createNotificationType(String request) throws IEMRException, JsonMappingException, JsonProcessingException;

	String updateNotificationType(String request) throws JSONException, IEMRException;

	String getEmergencyContacts(String request) throws IEMRException;

	String getSupervisorEmergencyContacts(String request) throws IEMRException, JsonMappingException, JsonProcessingException;

	String createEmergencyContacts(String request)
			throws NoSuchAlgorithmException, IOException, IEMRException, Exception;

	String updateEmergencyContacts(String request)
			throws JSONException, NoSuchAlgorithmException, IOException, IEMRException, Exception;
}
