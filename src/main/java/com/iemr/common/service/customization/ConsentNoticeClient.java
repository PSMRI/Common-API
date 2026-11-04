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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.iemr.common.utils.RestTemplateUtil;
import com.iemr.common.utils.exception.ConsentConfigConflictException;
import com.iemr.common.utils.exception.ConsentServiceUnavailableException;
import com.iemr.common.utils.exception.IEMRException;

/**
 * Calls Consent-API's admin notice endpoints for the Admin "Consent message" tab (A6–A8, feature 006). Sends the
 * caller's Authorization (as {@link ConsentMastersClient}), the {@code Idempotency-Key} and the service key that
 * Consent-API requires on {@code /consent/v1/admin/**}. Neither the key nor any body is logged.
 */
@Component
public class ConsentNoticeClient {

	static final String SERVICE_KEY_HEADER = "X-Consent-Service-Key";

	static final String LIST_PATH = "/consent/v1/admin/projects/{projectId}/notices";

	static final String DRAFT_PATH = LIST_PATH + "/{context}/draft";

	static final String PUBLISH_PATH = LIST_PATH + "/{context}/publish";

	private static final String STALE_VERSION = "STALE_VERSION";

	private final RestTemplate restTemplate;

	private final String baseUrl;

	private final String serviceKey;

	@Autowired
	public ConsentNoticeClient(@Value("${consent-api-url:}") String baseUrl,
			@Value("${consent-api.service-key:}") String serviceKey,
			@Value("${consent-api.timeout-ms:5000}") int timeoutMs) {
		this(buildRestTemplate(timeoutMs), baseUrl, serviceKey);
	}

	ConsentNoticeClient(RestTemplate restTemplate, String baseUrl, String serviceKey) {
		this.restTemplate = restTemplate;
		String trimmed = baseUrl == null ? "" : baseUrl.trim();
		this.baseUrl = trimmed.endsWith("/") ? trimmed.substring(0, trimmed.length() - 1) : trimmed;
		this.serviceKey = serviceKey == null ? "" : serviceKey;
	}

	private static RestTemplate buildRestTemplate(int timeoutMs) {
		SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		factory.setConnectTimeout(timeoutMs);
		factory.setReadTimeout(timeoutMs);
		return new RestTemplate(factory);
	}

	/** A6: the project's message entries. */
	public JsonElement list(Integer projectId, String authorization) throws IEMRException {
		return call(HttpMethod.GET, LIST_PATH, null, null, authorization, projectId);
	}

	/** A7: saves the English draft. */
	public JsonElement saveDraft(Integer projectId, String context, String body, String idempotencyKey,
			String authorization) throws IEMRException {
		return call(HttpMethod.PUT, DRAFT_PATH, body, idempotencyKey, authorization, projectId, context);
	}

	/** A8: publishes the draft. */
	public JsonElement publish(Integer projectId, String context, String body, String idempotencyKey,
			String authorization) throws IEMRException {
		return call(HttpMethod.POST, PUBLISH_PATH, body, idempotencyKey, authorization, projectId, context);
	}

	/**
	 * @return Consent-API's {@code data}
	 * @throws ConsentConfigConflictException     on 409 STALE_VERSION
	 * @throws IEMRException                      on any other refusal, with Consent-API's message
	 * @throws ConsentServiceUnavailableException when Consent-API is not configured, unreachable or failing
	 */
	private JsonElement call(HttpMethod method, String path, String body, String idempotencyKey,
			String authorization, Object... variables) throws IEMRException {
		if (baseUrl.isEmpty() || serviceKey.isBlank()) {
			throw new ConsentServiceUnavailableException();
		}
		HttpHeaders headers = new HttpHeaders();
		headers.addAll(RestTemplateUtil.createRequestEntity(null, authorization).getHeaders());
		headers.set(SERVICE_KEY_HEADER, serviceKey);
		if (idempotencyKey != null) {
			headers.set("Idempotency-Key", idempotencyKey);
		}
		try {
			ResponseEntity<String> response = restTemplate.exchange(baseUrl + path, method,
					new HttpEntity<>(body, headers), String.class, variables);
			JsonObject answer = parse(response.getBody());
			return answer == null ? null : answer.get("data");
		} catch (HttpStatusCodeException e) {
			if (e.getStatusCode().is5xxServerError()) {
				throw new ConsentServiceUnavailableException(e);
			}
			JsonObject answer = parse(e.getResponseBodyAsString());
			JsonObject error = object(answer, "error");
			boolean conflict = e.getStatusCode().value() == HttpStatus.CONFLICT.value();
			if (conflict && STALE_VERSION.equals(text(error, "code"))) {
				throw new ConsentConfigConflictException();
			}
			throw new IEMRException(message(answer, error));
		} catch (RestClientException e) {
			throw new ConsentServiceUnavailableException(e);
		}
	}

	/** Consent-API's message, or its first field detail ("body: must not be blank") when there is one. */
	private static String message(JsonObject answer, JsonObject error) {
		JsonElement details = error == null ? null : error.get("details");
		if (details != null && details.isJsonArray() && !details.getAsJsonArray().isEmpty()
				&& details.getAsJsonArray().get(0).isJsonObject()) {
			JsonObject detail = details.getAsJsonArray().get(0).getAsJsonObject();
			return text(detail, "field") + ": " + text(detail, "message");
		}
		String message = text(answer, "message");
		return message != null ? message : "Consent service refused the request";
	}

	private static JsonObject object(JsonObject parent, String key) {
		JsonElement value = parent == null ? null : parent.get(key);
		return value != null && value.isJsonObject() ? value.getAsJsonObject() : null;
	}

	private static String text(JsonObject parent, String key) {
		JsonElement value = parent == null ? null : parent.get(key);
		return value != null && value.isJsonPrimitive() ? value.getAsString() : null;
	}

	private static JsonObject parse(String body) {
		try {
			JsonElement element = body == null || body.isBlank() ? null : JsonParser.parseString(body);
			return element != null && element.isJsonObject() ? element.getAsJsonObject() : null;
		} catch (JsonParseException e) {
			return null;
		}
	}
}
