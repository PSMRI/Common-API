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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.iemr.common.service.customization.ConsentConfigService;
import com.iemr.common.utils.JwtUtil;
import com.iemr.common.utils.exception.ConsentConfigConflictException;
import com.iemr.common.utils.response.OutputResponse;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Per-project consent configuration (API 1 for the MMU consent dialog, A1–A5 for Admin-UI). Responses use
 * OutputResponse; a stale save is the one exception and returns HTTP 409 with data.reason = STALE_VERSION.
 */
@RequestMapping(value = "/customization/consent")
@RestController
public class ConsentConfigController {

	private final ConsentConfigService consentConfigService;
	private final JwtUtil jwtUtil;

	private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	public ConsentConfigController(ConsentConfigService consentConfigService, JwtUtil jwtUtil) {
		this.consentConfigService = consentConfigService;
		this.jwtUtil = jwtUtil;
	}

	@Operation(summary = "Load the consent configuration for the consent dialog (null when none)")
	@RequestMapping(value = "/fetchConsentConfig", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, headers = "Authorization")
	public String fetchConsentConfig(@RequestBody String request,
			@RequestHeader(value = "Authorization") String authorization, HttpServletRequest httpRequest) {
		OutputResponse response = new OutputResponse();
		try {
			response.setResponse(
					consentConfigService.fetchConsentConfig(request, jwtUtil.getUserIdFromRequest(httpRequest)));
		} catch (Exception e) {
			fail(response, "fetchConsentConfig", e);
		}
		return response.toString();
	}

	@Operation(summary = "Consent configuration masters: modes, central purposes, languages, configured projects")
	@RequestMapping(value = "/masters", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, headers = "Authorization")
	public String getMasters(@RequestParam Integer serviceProviderId,
			@RequestHeader(value = "Authorization") String authorization, HttpServletRequest httpRequest) {
		OutputResponse response = new OutputResponse();
		try {
			response.setResponse(consentConfigService.getMasters(serviceProviderId,
					jwtUtil.getUserIdFromRequest(httpRequest), authorization));
		} catch (Exception e) {
			fail(response, "masters", e);
		}
		return response.toString();
	}

	@Operation(summary = "List the consent configurations of a service provider")
	@RequestMapping(value = "/fetchAllConfigs", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, headers = "Authorization")
	public String fetchAllConfigs(@RequestBody String request,
			@RequestHeader(value = "Authorization") String authorization, HttpServletRequest httpRequest) {
		OutputResponse response = new OutputResponse();
		try {
			response.setResponse(
					consentConfigService.fetchAllConfigs(request, jwtUtil.getUserIdFromRequest(httpRequest)));
		} catch (Exception e) {
			fail(response, "fetchAllConfigs", e);
		}
		return response.toString();
	}

	@Operation(summary = "Get one project's consent configuration (version 0 = never saved)")
	@RequestMapping(value = "/fetchConfigByProject", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, headers = "Authorization")
	public String fetchConfigByProject(@RequestBody String request,
			@RequestHeader(value = "Authorization") String authorization, HttpServletRequest httpRequest) {
		OutputResponse response = new OutputResponse();
		try {
			response.setResponse(
					consentConfigService.fetchConfigByProject(request, jwtUtil.getUserIdFromRequest(httpRequest)));
		} catch (Exception e) {
			fail(response, "fetchConfigByProject", e);
		}
		return response.toString();
	}

	@Operation(summary = "Save a project's first consent configuration")
	@RequestMapping(value = "/saveConfig", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, headers = "Authorization")
	public ResponseEntity<String> saveConfig(@RequestBody String request,
			@RequestHeader(value = "Authorization") String authorization,
			@RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
			HttpServletRequest httpRequest) {
		return write(false, request, authorization, idempotencyKey, httpRequest);
	}

	@Operation(summary = "Update a project's consent configuration from the version that was loaded")
	@RequestMapping(value = "/updateConfig", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, headers = "Authorization")
	public ResponseEntity<String> updateConfig(@RequestBody String request,
			@RequestHeader(value = "Authorization") String authorization,
			@RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
			HttpServletRequest httpRequest) {
		return write(true, request, authorization, idempotencyKey, httpRequest);
	}

	private ResponseEntity<String> write(boolean update, String request, String authorization,
			String idempotencyKey, HttpServletRequest httpRequest) {
		OutputResponse response = new OutputResponse();
		Integer userId = jwtUtil.getUserIdFromRequest(httpRequest);
		String userName = jwtUtil.getUsernameFromRequest(httpRequest);
		if (userId == null || userName == null) {
			response.setError(OutputResponse.GENERIC_FAILURE, "session user not found");
			return ResponseEntity.ok(response.toString());
		}
		try {
			response.setResponse(update
					? consentConfigService.updateConfig(request, userId, userName, idempotencyKey, authorization)
					: consentConfigService.saveConfig(request, userId, userName, idempotencyKey, authorization));
		} catch (ConsentConfigConflictException e) {
			response.setResponse("{\"reason\":\"" + e.getReason() + "\"}");
			response.setError(HttpStatus.CONFLICT.value(), e.getMessage());
			return ResponseEntity.status(HttpStatus.CONFLICT).body(response.toString());
		} catch (Exception e) {
			fail(response, update ? "updateConfig" : "saveConfig", e);
		}
		return ResponseEntity.ok(response.toString());
	}

	private void fail(OutputResponse response, String endpoint, Exception e) {
		logger.error("Consent configuration {} failed: {}", endpoint, e.getMessage());
		response.setError(OutputResponse.GENERIC_FAILURE, e.getMessage());
	}
}
