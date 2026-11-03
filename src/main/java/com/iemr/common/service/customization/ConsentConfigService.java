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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */
package com.iemr.common.service.customization;

/**
 * Per-project consent configuration: read by the MMU consent dialog, set by Admin-UI's Provider Admin.
 * Methods return JSON for OutputResponse.setResponse, or null when there is nothing to return.
 */
public interface ConsentConfigService {

	String fetchConsentConfig(String request, Integer userId) throws Exception;

	String getMasters(Integer serviceProviderId, Integer userId, String authorization) throws Exception;

	String fetchAllConfigs(String request, Integer userId) throws Exception;

	String fetchConfigByProject(String request, Integer userId) throws Exception;

	String saveConfig(String request, Integer userId, String userName, String idempotencyKey,
			String authorization) throws Exception;

	String updateConfig(String request, Integer userId, String userName, String idempotencyKey,
			String authorization) throws Exception;
}
