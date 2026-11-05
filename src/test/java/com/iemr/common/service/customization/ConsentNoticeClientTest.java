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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.iemr.common.utils.exception.ConsentConfigConflictException;
import com.iemr.common.utils.exception.ConsentServiceUnavailableException;
import com.iemr.common.utils.exception.IEMRException;

/** Feature 006: Common-API's calls to Consent-API's admin notice endpoints (research R11). */
class ConsentNoticeClientTest {

	private static final String BASE = "http://consent.test";
	private static final String KEY = "svc-key";
	private static final String AUTH = "Bearer token";
	private static final String IDEM = "3f2504e0-4f89-11d3-9a0c-0305e82c3301";

	private RestTemplate restTemplate;
	private ConsentNoticeClient client;

	@BeforeEach
	void setUp() {
		restTemplate = mock(RestTemplate.class);
		client = new ConsentNoticeClient(restTemplate, BASE + "/", KEY);
	}

	private void answer(String url, HttpMethod method, String body) {
		when(restTemplate.exchange(eq(BASE + url), eq(method), any(HttpEntity.class), eq(String.class),
				any(Object[].class))).thenReturn(ResponseEntity.ok(body));
	}

	private void refuse(Exception e) {
		when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), eq(String.class),
				any(Object[].class))).thenThrow(e);
	}

	private static HttpClientErrorException error(HttpStatus status, String body) {
		return HttpClientErrorException.create(status, "", null, body.getBytes(StandardCharsets.UTF_8),
				StandardCharsets.UTF_8);
	}

	@Test
	void listReturnsTheDataAndSendsTheServiceKey() throws Exception {
		answer(ConsentNoticeClient.LIST_PATH, HttpMethod.GET, "{\"success\":true,\"data\":[{\"languageCode\":\"en\"}]}");

		assertThat(client.list(3, AUTH).toString()).isEqualTo("[{\"languageCode\":\"en\"}]");

		@SuppressWarnings("unchecked")
		ArgumentCaptor<HttpEntity<Object>> entity = ArgumentCaptor.forClass(HttpEntity.class);
		verify(restTemplate).exchange(eq(BASE + ConsentNoticeClient.LIST_PATH), eq(HttpMethod.GET), entity.capture(),
				eq(String.class), eq(3));
		assertThat(entity.getValue().getHeaders().getFirst(ConsentNoticeClient.SERVICE_KEY_HEADER)).isEqualTo(KEY);
		assertThat(entity.getValue().getHeaders().getFirst("Authorization")).isEqualTo(AUTH);
		assertThat(entity.getValue().getHeaders().containsKey("Idempotency-Key")).isFalse();
	}

	@Test
	void draftAndPublishForwardTheBodyAndIdempotencyKey() throws Exception {
		answer(ConsentNoticeClient.DRAFT_PATH, HttpMethod.PUT, "{\"data\":{\"languageCode\":\"en\"}}");
		answer(ConsentNoticeClient.PUBLISH_PATH, HttpMethod.POST, "{\"data\":null}");

		assertThat(client.saveDraft(3, "REGISTRATION", "{\"body\":\"B\"}", IDEM, AUTH).getAsJsonObject()
				.get("languageCode").getAsString()).isEqualTo("en");
		assertThat(client.publish(3, "REVISIT", "{}", IDEM, AUTH).isJsonNull()).isTrue();

		@SuppressWarnings("unchecked")
		ArgumentCaptor<HttpEntity<Object>> entity = ArgumentCaptor.forClass(HttpEntity.class);
		verify(restTemplate).exchange(eq(BASE + ConsentNoticeClient.DRAFT_PATH), eq(HttpMethod.PUT), entity.capture(),
				eq(String.class), eq(3), eq("REGISTRATION"));
		assertThat(entity.getValue().getBody()).isEqualTo("{\"body\":\"B\"}");
		assertThat(entity.getValue().getHeaders().getFirst("Idempotency-Key")).isEqualTo(IDEM);
	}

	@Test
	void legacyCheckAndImportUseTheirPathsAndHeaders() throws Exception {
		answer(ConsentNoticeClient.LEGACY_BATCHES_PATH, HttpMethod.POST, "{\"data\":{\"batchId\":\"b-1\"}}");
		answer(ConsentNoticeClient.LEGACY_IMPORT_PATH, HttpMethod.POST, "{\"data\":{\"status\":\"IMPORTED\"}}");

		assertThat(client.checkLegacy("{\"rows\":[]}", IDEM, AUTH).getAsJsonObject().get("batchId").getAsString())
				.isEqualTo("b-1");
		assertThat(client.importLegacy("b-1", IDEM, AUTH).getAsJsonObject().get("status").getAsString())
				.isEqualTo("IMPORTED");

		@SuppressWarnings("unchecked")
		ArgumentCaptor<HttpEntity<Object>> check = ArgumentCaptor.forClass(HttpEntity.class);
		verify(restTemplate).exchange(eq(BASE + ConsentNoticeClient.LEGACY_BATCHES_PATH), eq(HttpMethod.POST),
				check.capture(), eq(String.class), eq(new Object[0]));
		assertThat(check.getValue().getBody()).isEqualTo("{\"rows\":[]}");
		assertThat(check.getValue().getHeaders().getFirst(ConsentNoticeClient.SERVICE_KEY_HEADER)).isEqualTo(KEY);
		assertThat(check.getValue().getHeaders().getFirst("Idempotency-Key")).isEqualTo(IDEM);
		assertThat(check.getValue().getHeaders().getFirst("Authorization")).isEqualTo(AUTH);

		@SuppressWarnings("unchecked")
		ArgumentCaptor<HttpEntity<Object>> imported = ArgumentCaptor.forClass(HttpEntity.class);
		verify(restTemplate).exchange(eq(BASE + ConsentNoticeClient.LEGACY_IMPORT_PATH), eq(HttpMethod.POST),
				imported.capture(), eq(String.class), eq("b-1"));
		assertThat(imported.getValue().getBody()).isEqualTo("{}");
		assertThat(imported.getValue().getHeaders().getFirst("Idempotency-Key")).isEqualTo(IDEM);
	}

	@Test
	void batchAlreadyImportedComesBackWithConsentApisMessage() {
		refuse(error(HttpStatus.CONFLICT,
				"{\"message\":\"Batch already imported\",\"error\":{\"code\":\"STATE_CONFLICT\"}}"));
		assertThatThrownBy(() -> client.importLegacy("b-1", IDEM, AUTH)).isInstanceOf(IEMRException.class)
				.isNotInstanceOf(ConsentConfigConflictException.class).hasMessage("Batch already imported");
	}

	@Test
	void anAnswerWithoutDataIsNull() throws Exception {
		answer(ConsentNoticeClient.LIST_PATH, HttpMethod.GET, "{\"success\":true}");
		assertThat(client.list(3, AUTH)).isNull();
		answer(ConsentNoticeClient.LIST_PATH, HttpMethod.GET, "");
		assertThat(client.list(3, AUTH)).isNull();
		answer(ConsentNoticeClient.LIST_PATH, HttpMethod.GET, "[1]");
		assertThat(client.list(3, AUTH)).isNull();
		answer(ConsentNoticeClient.LIST_PATH, HttpMethod.GET, null);
		assertThat(client.list(3, AUTH)).isNull();
	}

	@Test
	void staleVersionIsAConflict() {
		refuse(error(HttpStatus.CONFLICT, "{\"message\":\"changed\",\"error\":{\"code\":\"STALE_VERSION\"}}"));
		assertThatThrownBy(() -> client.publish(3, "REGISTRATION", "{}", IDEM, AUTH))
				.isInstanceOf(ConsentConfigConflictException.class);
	}

	@Test
	void otherRefusalsCarryConsentApisMessageAndFirstField() {
		refuse(error(HttpStatus.BAD_REQUEST, "{\"message\":\"One or more fields are invalid\",\"error\":{\"code\":"
				+ "\"VALIDATION_FAILED\",\"details\":[{\"field\":\"body\",\"message\":\"must not be blank\"}]}}"));
		assertThatThrownBy(() -> client.saveDraft(3, "REGISTRATION", "{}", IDEM, AUTH))
				.isInstanceOf(IEMRException.class).hasMessage("body: must not be blank");

		refuse(error(HttpStatus.UNPROCESSABLE_ENTITY,
				"{\"message\":\"Enter the patient declaration\",\"error\":{\"code\":\"BUSINESS_RULE_VIOLATION\","
						+ "\"details\":[]}}"));
		assertThatThrownBy(() -> client.publish(3, "REGISTRATION", "{}", IDEM, AUTH))
				.hasMessage("Enter the patient declaration");

		refuse(error(HttpStatus.CONFLICT, "{\"message\":\"key reused\",\"error\":{\"code\":\"STATE_CONFLICT\"}}"));
		assertThatThrownBy(() -> client.publish(3, "REGISTRATION", "{}", IDEM, AUTH)).hasMessage("key reused");

		refuse(error(HttpStatus.FORBIDDEN, "{\"error\":null,\"message\":null}"));
		assertThatThrownBy(() -> client.list(3, AUTH)).hasMessage("Consent service refused the request");

		refuse(error(HttpStatus.FORBIDDEN, "not json {"));
		assertThatThrownBy(() -> client.list(3, AUTH)).hasMessage("Consent service refused the request");

		refuse(error(HttpStatus.FORBIDDEN, "{\"message\":\"m\",\"error\":{\"details\":\"x\",\"code\":null}}"));
		assertThatThrownBy(() -> client.list(3, AUTH)).hasMessage("m");

		refuse(error(HttpStatus.FORBIDDEN, "{\"message\":\"m\",\"error\":\"text\"}"));
		assertThatThrownBy(() -> client.list(3, AUTH)).hasMessage("m");

		refuse(error(HttpStatus.BAD_REQUEST, "{\"message\":\"m\",\"error\":{\"details\":[\"s\"]}}"));
		assertThatThrownBy(() -> client.list(3, AUTH)).hasMessage("m");

		refuse(error(HttpStatus.BAD_REQUEST, "{\"message\":\"m\",\"error\":{\"code\":{}}}"));
		assertThatThrownBy(() -> client.list(3, AUTH)).hasMessage("m");

		refuse(error(HttpStatus.CONFLICT, "{\"message\":\"m\"}"));
		assertThatThrownBy(() -> client.publish(3, "REGISTRATION", "{}", IDEM, AUTH)).hasMessage("m");

		refuse(error(HttpStatus.UNPROCESSABLE_ENTITY, "{\"message\":\"m\",\"error\":{\"code\":\"STALE_VERSION\"}}"));
		assertThatThrownBy(() -> client.publish(3, "REGISTRATION", "{}", IDEM, AUTH))
				.isInstanceOf(IEMRException.class).hasMessage("m");
	}

	@Test
	void serverErrorsAndUnreachableAreUnavailable() {
		refuse(HttpServerErrorException.create(HttpStatus.SERVICE_UNAVAILABLE, "", null, null, null));
		assertThatThrownBy(() -> client.list(3, AUTH)).isInstanceOf(ConsentServiceUnavailableException.class);
		refuse(new ResourceAccessException("timeout"));
		assertThatThrownBy(() -> client.list(3, AUTH)).isInstanceOf(ConsentServiceUnavailableException.class);
	}

	@Test
	void withoutUrlOrKeyNothingIsCalled() {
		assertThatThrownBy(() -> new ConsentNoticeClient(restTemplate, " ", KEY).list(3, AUTH))
				.isInstanceOf(ConsentServiceUnavailableException.class);
		assertThatThrownBy(() -> new ConsentNoticeClient(restTemplate, null, KEY).list(3, AUTH))
				.isInstanceOf(ConsentServiceUnavailableException.class);
		assertThatThrownBy(() -> new ConsentNoticeClient(restTemplate, BASE, " ").list(3, AUTH))
				.isInstanceOf(ConsentServiceUnavailableException.class);
		assertThatThrownBy(() -> new ConsentNoticeClient(restTemplate, BASE, null).list(3, AUTH))
				.isInstanceOf(ConsentServiceUnavailableException.class);
		verifyNoInteractions(restTemplate);
		assertThat(new ConsentNoticeClient(BASE, KEY, 1000)).isNotNull();
	}
}
