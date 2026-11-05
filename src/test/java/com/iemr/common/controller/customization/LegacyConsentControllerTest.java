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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.iemr.common.service.customization.LegacyConsentService;
import com.iemr.common.utils.JwtUtil;
import com.iemr.common.utils.exception.IEMRException;

/** Consent-API feature 008: the A9–A10 endpoints and their OutputResponse envelope (contract common-api-legacy). */
class LegacyConsentControllerTest {

	private static final String BASE = "/customization/consent/legacy";
	private static final String AUTH = "Bearer token";
	private static final String IDEM = "3f2504e0-4f89-11d3-9a0c-0305e82c3301";
	private static final String BODY = "{\"serviceProviderId\":1}";

	private LegacyConsentService service;
	private MockMvc mvc;

	@BeforeEach
	void setUp() {
		service = mock(LegacyConsentService.class);
		JwtUtil jwtUtil = mock(JwtUtil.class);
		when(jwtUtil.getUserIdFromRequest(any())).thenReturn(501);
		mvc = MockMvcBuilders.standaloneSetup(new LegacyConsentController(service, jwtUtil)).build();
	}

	private static MockHttpServletRequestBuilder postJson(String path) {
		return post(BASE + path).header("Authorization", AUTH).header("Idempotency-Key", IDEM)
				.contentType(MediaType.APPLICATION_JSON).content(BODY);
	}

	@Test
	void validateReturnsTheBatch() throws Exception {
		when(service.validate(BODY, 501, IDEM, AUTH)).thenReturn("{\"batchId\":\"b-1\",\"total\":2}");

		mvc.perform(postJson("/validate")).andExpect(status().isOk()).andExpect(jsonPath("$.statusCode").value(200))
				.andExpect(jsonPath("$.data.batchId").value("b-1"));
	}

	@Test
	void importReturnsTheBatch() throws Exception {
		when(service.importBatch(BODY, 501, IDEM, AUTH)).thenReturn("{\"status\":\"IMPORTED\"}");

		mvc.perform(postJson("/import")).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.status").value("IMPORTED"));
	}

	@Test
	void refusalsAre5000() throws Exception {
		when(service.validate(BODY, 501, IDEM, AUTH))
				.thenThrow(new IEMRException("Beneficiary lookup unavailable, try again"));
		when(service.importBatch(BODY, 501, IDEM, AUTH)).thenThrow(new IEMRException("Batch already imported"));

		mvc.perform(postJson("/validate")).andExpect(status().isOk()).andExpect(jsonPath("$.statusCode").value(5000))
				.andExpect(jsonPath("$.errorMessage").value("Beneficiary lookup unavailable, try again"));
		mvc.perform(postJson("/import")).andExpect(status().isOk()).andExpect(jsonPath("$.statusCode").value(5000))
				.andExpect(jsonPath("$.errorMessage").value("Batch already imported"));
	}
}
