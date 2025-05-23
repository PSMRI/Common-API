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
package com.iemr.common.utils.sessionobject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.iemr.common.utils.config.ConfigProperties;
import com.iemr.common.utils.redis.RedisSessionException;
import com.iemr.common.utils.redis.RedisStorage;

@Component
public class SessionObject {

	Logger logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());

	private RedisStorage objectStore;

	@Autowired
	public void setObjectStore(RedisStorage objectStore) {

		this.objectStore = objectStore;
	}

	public SessionObject() {

		extendExpirationTime = ConfigProperties.getExtendExpiryTime();
		sessionExpiryTime = ConfigProperties.getSessionExpiryTime();
	}

	private boolean extendExpirationTime;// =
											// configProperties.getExtendExpiryTime();
	private int sessionExpiryTime;// = configProperties.getSessionExpiryTime();

	public String getSessionObject(String key) throws RedisSessionException {
		Boolean extendExpirationTime = ConfigProperties.getExtendExpiryTime();
		Integer sessionExpiryTime = ConfigProperties.getSessionExpiryTime();
		if(null != key && !key.isEmpty()) {
			return objectStore.getObject(key, extendExpirationTime, sessionExpiryTime);
		}
		return null;
	}

	public String setSessionObject(String key, String value) throws RedisSessionException {
		Integer sessionExpiryTime = ConfigProperties.getSessionExpiryTime();
		if(null != key && !key.isEmpty()) {
			return objectStore.setObject(key, value, sessionExpiryTime);
		}
		return null;
	}

	public String updateSessionObject(String key, String value) throws RedisSessionException {
		Boolean extendExpirationTime = ConfigProperties.getExtendExpiryTime();
		Integer sessionExpiryTime = ConfigProperties.getSessionExpiryTime();
		if(null != key && !key.isEmpty()) {
			updateConcurrentSessionObject(key, value, extendExpirationTime, sessionExpiryTime);
			return objectStore.updateObject(key, value, extendExpirationTime, sessionExpiryTime);
		}
		return null;
	}

	private void updateConcurrentSessionObject(String key, String value, Boolean extendExpirationTime,
			Integer sessionExpiryTime) {
		try {
			JsonObject jsnOBJ = new JsonObject();
			JsonParser jsnParser = new JsonParser();
			JsonElement jsnElmnt = jsnParser.parse(value);
			jsnOBJ = jsnElmnt.getAsJsonObject();
			logger.info("updating key:" + jsnOBJ.get("userName"));
			if (jsnOBJ.has("userName") && jsnOBJ.get("userName") != null) {
				objectStore.updateObject(jsnOBJ.get("userName").getAsString().trim().toLowerCase(), key,
						extendExpirationTime, sessionExpiryTime);
			}
		} catch (Exception e) {

		}
	}

	public void deleteSessionObject(String key) {
		try {
			logger.info("Deleting key " + key);
			if(null != key && !key.isEmpty()) {
				objectStore.deleteObject(key);
			}
			logger.info("Deleted key " + key);
		} catch (Exception e) {
			logger.error("deleteSessionObject failed with error " + e.getMessage(), e);
		}
	}

	public String setSessionObjectForChangePassword(String key, String value) throws RedisSessionException {
		Integer sessionExpiryTime = ConfigProperties.getSessionExpiryTimeForChangePassword();
		if(null != key && !key.isEmpty()) {
			return objectStore.setObject(key, value, sessionExpiryTime);
		}
		return null;
	}

	public String getSessionObjectForChangePassword(String key) throws RedisSessionException {
		Boolean extendExpirationTime = ConfigProperties.getExtendExpiryTimeForChangePassword();
		Integer sessionExpiryTime = ConfigProperties.getSessionExpiryTimeForChangePassword();
		if(null != key && !key.isEmpty()) {
			return objectStore.getObject(key, extendExpirationTime, sessionExpiryTime);
		}
		return null;
	}
}
