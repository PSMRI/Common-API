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
import static org.mockito.ArgumentMatchers.eq;
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

import com.iemr.common.service.customization.ConsentNoticeService;
import com.iemr.common.utils.JwtUtil;
import com.iemr.common.utils.exception.ConsentConfigConflictException;
import com.iemr.common.utils.exception.IEMRException;

/** Feature 006: the A6–A8 endpoints and their OutputResponse envelope (contract common-api-consent-notices). */
class ConsentNoticeControllerTest {

	private static final String BASE = "/customization/consent/notices";
	private static final String AUTH = "Bearer token";
	private static final String IDEM = "3f2504e0-4f89-11d3-9a0c-0305e82c3301";
	private static final String BODY = "{\"projectId\":3,\"serviceProviderId\":1}";

	private ConsentNoticeService service;
	private MockMvc mvc;

	@BeforeEach
	void setUp() {
		service = mock(ConsentNoticeService.class);
		JwtUtil jwtUtil = mock(JwtUtil.class);
		when(jwtUtil.getUserIdFromRequest(any())).thenReturn(501);
		mvc = MockMvcBuilders.standaloneSetup(new ConsentNoticeController(service, jwtUtil)).build();
	}

	private static MockHttpServletRequestBuilder postJson(String path) {
		return post(BASE + path).header("Authorization", AUTH).header("Idempotency-Key", IDEM)
				.contentType(MediaType.APPLICATION_JSON).content(BODY);
	}

	@Test
	void fetchByProjectReturnsTheEntries() throws Exception {
		when(service.fetchByProject(BODY, 501, AUTH)).thenReturn("[{\"languageCode\":\"en\"}]");

		mvc.perform(postJson("/fetchByProject")).andExpect(status().isOk())
				.andExpect(jsonPath("$.statusCode").value(200)).andExpect(jsonPath("$.data[0].languageCode").value("en"));
	}

	@Test
	void fetchByProjectFailureIs5000() throws Exception {
		when(service.fetchByProject(BODY, 501, AUTH)).thenThrow(new IEMRException("project not found"));

		mvc.perform(postJson("/fetchByProject")).andExpect(status().isOk())
				.andExpect(jsonPath("$.statusCode").value(5000))
				.andExpect(jsonPath("$.errorMessage").value("project not found"));
	}

	@Test
	void saveDraftAndPublishSucceed() throws Exception {
		when(service.saveDraft(BODY, 501, IDEM, AUTH)).thenReturn("{\"languageCode\":\"en\"}");
		when(service.publish(BODY, 501, IDEM, AUTH)).thenReturn("{\"languageCode\":\"en\"}");

		mvc.perform(postJson("/saveDraft")).andExpect(status().isOk()).andExpect(jsonPath("$.statusCode").value(200));
		mvc.perform(postJson("/publish")).andExpect(status().isOk()).andExpect(jsonPath("$.data.languageCode").value("en"));
	}

	@Test
	void aStaleWriteIsHttp409() throws Exception {
		when(service.publish(BODY, 501, IDEM, AUTH)).thenThrow(new ConsentConfigConflictException());

		mvc.perform(postJson("/publish")).andExpect(status().isConflict()).andExpect(jsonPath("$.statusCode").value(409))
				.andExpect(jsonPath("$.data.reason").value("STALE_VERSION"))
				.andExpect(jsonPath("$.errorMessage").value("Changed by someone else, reload"));
	}

	@Test
	void refusalsAre5000() throws Exception {
		when(service.publish(eq(BODY), eq(501), eq(IDEM), eq(AUTH)))
				.thenThrow(new IEMRException("You are not allowed to publish consent messages"));
		when(service.saveDraft(eq(BODY), eq(501), eq(IDEM), eq(AUTH))).thenThrow(new IEMRException("body is required"));

		mvc.perform(postJson("/publish")).andExpect(status().isOk()).andExpect(jsonPath("$.statusCode").value(5000))
				.andExpect(jsonPath("$.errorMessage").value("You are not allowed to publish consent messages"));
		mvc.perform(postJson("/saveDraft")).andExpect(status().isOk())
				.andExpect(jsonPath("$.errorMessage").value("body is required"));
	}
}
