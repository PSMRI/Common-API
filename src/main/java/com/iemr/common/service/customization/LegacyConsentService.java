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
 * Admin legacy consent upload (A9–A10, Consent-API feature 008). Every answer is the {@code data} JSON for the
 * {@code OutputResponse}; refusals are {@code IEMRException} (statusCode 5000). The batches live in Consent-API.
 */
public interface LegacyConsentService {

	/** A9: looks up the Beneficiary IDs in Identity-API; Consent-API then checks the rows and stores the batch. */
	String validate(String request, Integer userId, String idempotencyKey, String authorization) throws Exception;

	/** A10: has Consent-API import the valid rows of a checked batch, once. */
	String importBatch(String request, Integer userId, String idempotencyKey, String authorization)
			throws Exception;
}
