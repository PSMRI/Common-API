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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.iemr.common.service.customization.ConsentConfigService;
import com.iemr.common.utils.JwtUtil;
import com.iemr.common.utils.exception.ConsentConfigConflictException;
import com.iemr.common.utils.exception.IEMRException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ConsentConfigControllerTest {

	private static final String BASE = "/customization/consent";
	private static final String AUTH = "Bearer token";
	private static final String BODY = "{\"projectId\":12,\"serviceProviderId\":1}";

	@Mock
	private ConsentConfigService service;
	@Mock
	private JwtUtil jwtUtil;
	@InjectMocks
	private ConsentConfigController controller;

	private MockMvc mvc;

	@BeforeEach
	void setUp() {
		mvc = MockMvcBuilders.standaloneSetup(controller).build();
		when(jwtUtil.getUserIdFromRequest(any())).thenReturn(501);
		when(jwtUtil.getUsernameFromRequest(any())).thenReturn("padmin.test");
	}

	private MockHttpServletRequestBuilder postJson(String path) {
		return post(BASE + path).header("Authorization", AUTH).contentType(MediaType.APPLICATION_JSON).content(BODY);
	}

	@Nested
	@DisplayName("reads")
	class Reads {

		@Test
		@DisplayName("fetchConsentConfig wraps the configuration in a 200 response with the caller's user ID")
		void fetchConsentConfig() throws Exception {
			when(service.fetchConsentConfig(BODY, 501)).thenReturn("{\"projectId\":12,\"version\":3}");

			mvc.perform(postJson("/fetchConsentConfig")).andExpect(status().isOk())
					.andExpect(jsonPath("$.statusCode").value(200)).andExpect(jsonPath("$.data.version").value(3));
		}

		@Test
		@DisplayName("fetchConsentConfig with nothing configured returns 200 without data")
		void fetchConsentConfigNone() throws Exception {
			when(service.fetchConsentConfig(BODY, 501)).thenReturn(null);

			mvc.perform(postJson("/fetchConsentConfig")).andExpect(status().isOk())
					.andExpect(jsonPath("$.statusCode").value(200)).andExpect(jsonPath("$.data").doesNotExist());
		}

		@Test
		@DisplayName("a service failure is statusCode 5000 with its message, still HTTP 200")
		void fetchConsentConfigFailure() throws Exception {
			when(service.fetchConsentConfig(BODY, 501)).thenThrow(new IEMRException("context must be REGISTRATION or REVISIT"));

			mvc.perform(postJson("/fetchConsentConfig")).andExpect(status().isOk())
					.andExpect(jsonPath("$.statusCode").value(5000))
					.andExpect(jsonPath("$.errorMessage").value("context must be REGISTRATION or REVISIT"));
		}

		@Test
		@DisplayName("masters binds serviceProviderId from the query and forwards the Authorization header")
		void masters() throws Exception {
			when(service.getMasters(1, 501, AUTH)).thenReturn("{\"modes\":[]}");

			mvc.perform(get(BASE + "/masters").param("serviceProviderId", "1").header("Authorization", AUTH))
					.andExpect(status().isOk()).andExpect(jsonPath("$.statusCode").value(200))
					.andExpect(jsonPath("$.data.modes").isArray());
		}

		@Test
		@DisplayName("masters without serviceProviderId is a 400 from request binding")
		void mastersMissingProvider() throws Exception {
			mvc.perform(get(BASE + "/masters").header("Authorization", AUTH)).andExpect(status().isBadRequest());
			verifyNoInteractions(service);
		}

		@Test
		@DisplayName("masters failure is statusCode 5000")
		void mastersFailure() throws Exception {
			when(service.getMasters(1, 501, AUTH)).thenThrow(new IEMRException("Consent service unavailable, try again"));

			mvc.perform(get(BASE + "/masters").param("serviceProviderId", "1").header("Authorization", AUTH))
					.andExpect(status().isOk()).andExpect(jsonPath("$.statusCode").value(5000))
					.andExpect(jsonPath("$.errorMessage").value("Consent service unavailable, try again"));
		}

		@Test
		@DisplayName("fetchAllConfigs and fetchConfigByProject return the service result or statusCode 5000")
		void adminReads() throws Exception {
			when(service.fetchAllConfigs(BODY, 501)).thenReturn("[{\"projectId\":12}]");
			when(service.fetchConfigByProject(BODY, 501)).thenReturn("{\"projectId\":12,\"version\":0}");

			mvc.perform(postJson("/fetchAllConfigs")).andExpect(status().isOk())
					.andExpect(jsonPath("$.data[0].projectId").value(12));
			mvc.perform(postJson("/fetchConfigByProject")).andExpect(status().isOk())
					.andExpect(jsonPath("$.data.version").value(0));

			when(service.fetchAllConfigs(BODY, 501)).thenThrow(new IEMRException("project not found"));
			when(service.fetchConfigByProject(BODY, 501)).thenThrow(new IEMRException("project not found"));
			mvc.perform(postJson("/fetchAllConfigs")).andExpect(jsonPath("$.statusCode").value(5000));
			mvc.perform(postJson("/fetchConfigByProject")).andExpect(jsonPath("$.statusCode").value(5000))
					.andExpect(jsonPath("$.errorMessage").value("project not found"));
		}
	}

	@Nested
	@DisplayName("writes")
	class Writes {

		@Test
		@DisplayName("saveConfig passes the session user and the Idempotency-Key header")
		void save() throws Exception {
			when(service.saveConfig(BODY, 501, "padmin.test", "9b2f", AUTH)).thenReturn("{\"version\":1}");

			mvc.perform(postJson("/saveConfig").header("Idempotency-Key", "9b2f")).andExpect(status().isOk())
					.andExpect(jsonPath("$.statusCode").value(200)).andExpect(jsonPath("$.data.version").value(1));
		}

		@Test
		@DisplayName("saveConfig without an Idempotency-Key passes null")
		void saveWithoutKey() throws Exception {
			when(service.saveConfig(eq(BODY), eq(501), eq("padmin.test"), isNull(), eq(AUTH))).thenReturn("{\"version\":1}");

			mvc.perform(postJson("/saveConfig")).andExpect(status().isOk()).andExpect(jsonPath("$.data.version").value(1));
		}

		@Test
		@DisplayName("C20 – a stale save is HTTP 409 with data.reason STALE_VERSION")
		void saveConflict() throws Exception {
			when(service.saveConfig(anyString(), any(), anyString(), any(), anyString()))
					.thenThrow(new ConsentConfigConflictException());

			mvc.perform(postJson("/saveConfig")).andExpect(status().isConflict())
					.andExpect(jsonPath("$.statusCode").value(409))
					.andExpect(jsonPath("$.errorMessage").value("Changed by someone else, reload"))
					.andExpect(jsonPath("$.data.reason").value("STALE_VERSION"));
		}

		@Test
		@DisplayName("a validation failure is HTTP 200 with statusCode 5000")
		void saveInvalid() throws Exception {
			when(service.saveConfig(anyString(), any(), anyString(), any(), anyString()))
					.thenThrow(new IEMRException("registration.guardian.ageThreshold must be 18"));

			mvc.perform(postJson("/saveConfig")).andExpect(status().isOk())
					.andExpect(jsonPath("$.statusCode").value(5000))
					.andExpect(jsonPath("$.errorMessage").value("registration.guardian.ageThreshold must be 18"));
		}

		@Test
		@DisplayName("without a session user nothing is saved")
		void noSessionUser() throws Exception {
			when(jwtUtil.getUserIdFromRequest(any())).thenReturn(null);
			mvc.perform(postJson("/saveConfig")).andExpect(status().isOk())
					.andExpect(jsonPath("$.statusCode").value(5000))
					.andExpect(jsonPath("$.errorMessage").value("session user not found"));

			when(jwtUtil.getUserIdFromRequest(any())).thenReturn(501);
			when(jwtUtil.getUsernameFromRequest(any())).thenReturn(null);
			mvc.perform(postJson("/updateConfig")).andExpect(jsonPath("$.statusCode").value(5000));

			verify(service, never()).saveConfig(any(), any(), any(), any(), any());
			verify(service, never()).updateConfig(any(), any(), any(), any(), any());
		}

		@Test
		@DisplayName("updateConfig returns the new version, 409 when stale and 5000 on failure")
		void update() throws Exception {
			when(service.updateConfig(BODY, 501, "padmin.test", "k1", AUTH)).thenReturn("{\"version\":4}");
			mvc.perform(postJson("/updateConfig").header("Idempotency-Key", "k1")).andExpect(status().isOk())
					.andExpect(jsonPath("$.data.version").value(4));

			when(service.updateConfig(BODY, 501, "padmin.test", "k2", AUTH)).thenThrow(new ConsentConfigConflictException());
			mvc.perform(postJson("/updateConfig").header("Idempotency-Key", "k2")).andExpect(status().isConflict())
					.andExpect(jsonPath("$.data.reason").value("STALE_VERSION"));

			when(service.updateConfig(BODY, 501, "padmin.test", "k3", AUTH))
					.thenThrow(new IEMRException("No configuration to update; save it first"));
			mvc.perform(postJson("/updateConfig").header("Idempotency-Key", "k3")).andExpect(status().isOk())
					.andExpect(jsonPath("$.statusCode").value(5000))
					.andExpect(jsonPath("$.errorMessage").value("No configuration to update; save it first"));
		}
	}
}
