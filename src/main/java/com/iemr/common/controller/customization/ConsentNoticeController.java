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
import org.springframework.web.bind.annotation.RestController;

import com.iemr.common.service.customization.ConsentNoticeService;
import com.iemr.common.utils.JwtUtil;
import com.iemr.common.utils.exception.ConsentConfigConflictException;
import com.iemr.common.utils.response.OutputResponse;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;

/**
 * The Admin "Consent message" tab (A6–A8, feature 006): list, save the English draft, publish. Responses use
 * OutputResponse; a draft or publish that someone else changed first is HTTP 409 with data.reason = STALE_VERSION,
 * as in A4/A5. The notices themselves live in Consent-API.
 */
@RequestMapping(value = "/customization/consent/notices")
@RestController
public class ConsentNoticeController {

	private static final String JSON = MediaType.APPLICATION_JSON_VALUE;

	private final ConsentNoticeService service;

	private final JwtUtil jwtUtil;

	private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	public ConsentNoticeController(ConsentNoticeService service, JwtUtil jwtUtil) {
		this.service = service;
		this.jwtUtil = jwtUtil;
	}

	@Operation(summary = "A6: a project's consent messages, one entry per context and language")
	@RequestMapping(value = "/fetchByProject", method = RequestMethod.POST, consumes = JSON, produces = JSON,
			headers = "Authorization")
	public String fetchByProject(@RequestBody String request,
			@RequestHeader(value = "Authorization") String authorization, HttpServletRequest httpRequest) {
		OutputResponse response = new OutputResponse();
		try {
			Integer userId = jwtUtil.getUserIdFromRequest(httpRequest);
			response.setResponse(service.fetchByProject(request, userId, authorization));
		} catch (Exception e) {
			fail(response, "fetchByProject", e);
		}
		return response.toString();
	}

	@Operation(summary = "A7: save the English consent message draft")
	@RequestMapping(value = "/saveDraft", method = RequestMethod.POST, consumes = JSON, produces = JSON,
			headers = "Authorization")
	public ResponseEntity<String> saveDraft(@RequestBody String request,
			@RequestHeader(value = "Authorization") String authorization,
			@RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
			HttpServletRequest httpRequest) {
		return write(false, request, authorization, idempotencyKey, httpRequest);
	}

	@Operation(summary = "A8: publish the consent message (any user of the project's service provider)")
	@RequestMapping(value = "/publish", method = RequestMethod.POST, consumes = JSON, produces = JSON,
			headers = "Authorization")
	public ResponseEntity<String> publish(@RequestBody String request,
			@RequestHeader(value = "Authorization") String authorization,
			@RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
			HttpServletRequest httpRequest) {
		return write(true, request, authorization, idempotencyKey, httpRequest);
	}

	private ResponseEntity<String> write(boolean publish, String request, String authorization,
			String idempotencyKey, HttpServletRequest httpRequest) {
		OutputResponse response = new OutputResponse();
		Integer userId = jwtUtil.getUserIdFromRequest(httpRequest);
		try {
			response.setResponse(publish ? service.publish(request, userId, idempotencyKey, authorization)
					: service.saveDraft(request, userId, idempotencyKey, authorization));
		} catch (ConsentConfigConflictException e) {
			response.setResponse("{\"reason\":\"" + e.getReason() + "\"}");
			response.setError(HttpStatus.CONFLICT.value(), e.getMessage());
			return ResponseEntity.status(HttpStatus.CONFLICT).body(response.toString());
		} catch (Exception e) {
			fail(response, publish ? "publish" : "saveDraft", e);
		}
		return ResponseEntity.ok(response.toString());
	}

	private void fail(OutputResponse response, String endpoint, Exception e) {
		logger.error("Consent message {} failed: {}", endpoint, e.getMessage());
		response.setError(OutputResponse.GENERIC_FAILURE, e.getMessage());
	}
}
