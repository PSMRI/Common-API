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

package com.iemr.common.controller.customization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.iemr.common.service.customization.LegacyConsentService;
import com.iemr.common.utils.JwtUtil;
import com.iemr.common.utils.response.OutputResponse;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;

/**
 * The Admin legacy consent upload (A9–A10, Consent-API feature 008): check a file's rows, then import the valid ones
 * once. Responses use OutputResponse; per-row problems are inside the returned batch, refusals are statusCode 5000.
 * The batches live in Consent-API. Only the exception type is logged, never IDs, rows or names.
 */
@RequestMapping(value = "/customization/consent/legacy")
@RestController
public class LegacyConsentController {

	private static final String JSON = MediaType.APPLICATION_JSON_VALUE;

	private final LegacyConsentService service;

	private final JwtUtil jwtUtil;

	private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	public LegacyConsentController(LegacyConsentService service, JwtUtil jwtUtil) {
		this.service = service;
		this.jwtUtil = jwtUtil;
	}

	@Operation(summary = "A9: check the rows of a legacy consent file (Beneficiary IDs looked up in Identity-API)")
	@RequestMapping(value = "/validate", method = RequestMethod.POST, consumes = JSON, produces = JSON,
			headers = "Authorization")
	public String validate(@RequestBody String request,
			@RequestHeader(value = "Authorization") String authorization,
			@RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
			HttpServletRequest httpRequest) {
		return run(true, request, authorization, idempotencyKey, httpRequest);
	}

	@Operation(summary = "A10: import the valid rows of a checked legacy batch, once")
	@RequestMapping(value = "/import", method = RequestMethod.POST, consumes = JSON, produces = JSON,
			headers = "Authorization")
	public String importBatch(@RequestBody String request,
			@RequestHeader(value = "Authorization") String authorization,
			@RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
			HttpServletRequest httpRequest) {
		return run(false, request, authorization, idempotencyKey, httpRequest);
	}

	private String run(boolean validate, String request, String authorization, String idempotencyKey,
			HttpServletRequest httpRequest) {
		OutputResponse response = new OutputResponse();
		try {
			Integer userId = jwtUtil.getUserIdFromRequest(httpRequest);
			response.setResponse(validate ? service.validate(request, userId, idempotencyKey, authorization)
					: service.importBatch(request, userId, idempotencyKey, authorization));
		} catch (Exception e) {
			logger.error("Legacy consent {} failed: {}", validate ? "validate" : "import",
					e.getClass().getSimpleName());
			response.setError(OutputResponse.GENERIC_FAILURE, e.getMessage());
		}
		return response.toString();
	}
}
