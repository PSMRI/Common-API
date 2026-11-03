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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.iemr.common.data.customization.consent.ConsentConfigMasters.LanguageMaster;
import com.iemr.common.data.customization.consent.ConsentConfigMasters.PurposeMaster;
import com.iemr.common.data.customization.consent.ConsentContext;
import com.iemr.common.utils.RestTemplateUtil;
import com.iemr.common.utils.exception.ConsentServiceUnavailableException;

/**
 * Reads the purposes and languages a project can choose from out of Consent-API. Consent-API has no purpose
 * list endpoint yet, so the purposes are those of the active notices (REGISTRATION and REVISIT) in the
 * reference language, and the languages are those the active notices are published in.
 */
@Component
public class ConsentMastersClient {

	private static final String LANGUAGES_PATH = "/consent/v1/notices/active/languages?context={context}";
	private static final String NOTICE_PATH = "/consent/v1/notices/active?context={context}&lang={lang}";

	private final RestTemplate restTemplate;
	private final String baseUrl;
	private final String referenceLanguage;

	@Autowired
	public ConsentMastersClient(@Value("${consent-api-url:}") String baseUrl,
			@Value("${consent-api.reference-language:en}") String referenceLanguage,
			@Value("${consent-api.timeout-ms:5000}") int timeoutMs) {
		this(buildRestTemplate(timeoutMs), baseUrl, referenceLanguage);
	}

	ConsentMastersClient(RestTemplate restTemplate, String baseUrl, String referenceLanguage) {
		this.restTemplate = restTemplate;
		this.baseUrl = baseUrl == null ? "" : baseUrl.trim();
		this.referenceLanguage = referenceLanguage;
	}

	private static RestTemplate buildRestTemplate(int timeoutMs) {
		SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		factory.setConnectTimeout(timeoutMs);
		factory.setReadTimeout(timeoutMs);
		return new RestTemplate(factory);
	}

	/**
	 * @throws ConsentServiceUnavailableException when Consent-API is not configured or cannot be reached
	 */
	public CentralLists load(String authorization) {
		if (baseUrl.isEmpty()) {
			throw new ConsentServiceUnavailableException();
		}
		HttpEntity<Object> entity = RestTemplateUtil.createRequestEntity(null, authorization);
		Map<String, LanguageMaster> languages = new LinkedHashMap<>();
		Map<String, PurposeMaster> purposes = new LinkedHashMap<>();
		for (ConsentContext context : ConsentContext.values()) {
			JsonElement languageData = get(LANGUAGES_PATH, entity, context.name(), null);
			if (languageData != null && languageData.isJsonArray()) {
				for (JsonElement e : languageData.getAsJsonArray()) {
					JsonObject o = e.getAsJsonObject();
					String code = string(o, "languageCode");
					if (code != null) {
						languages.putIfAbsent(code, new LanguageMaster(code, string(o, "languageName"), true));
					}
				}
			}
			JsonElement notice = get(NOTICE_PATH, entity, context.name(), referenceLanguage);
			if (notice != null && notice.isJsonObject() && notice.getAsJsonObject().has("purposes")
					&& notice.getAsJsonObject().get("purposes").isJsonArray()) {
				JsonArray noticePurposes = notice.getAsJsonObject().getAsJsonArray("purposes");
				for (JsonElement e : noticePurposes) {
					JsonObject o = e.getAsJsonObject();
					String code = string(o, "purposeCode");
					if (code == null) {
						continue;
					}
					boolean mandatory = o.has("isMandatory") && !o.get("isMandatory").isJsonNull()
							&& o.get("isMandatory").getAsBoolean();
					PurposeMaster existing = purposes.get(code);
					if (existing == null) {
						purposes.put(code, new PurposeMaster(code, string(o, "name"), mandatory));
					} else if (mandatory) {
						existing.setIsMandatory(true);
					}
				}
			}
		}
		return new CentralLists(new ArrayList<>(purposes.values()), new ArrayList<>(languages.values()));
	}

	/** Returns the "data" of Consent-API's response, or null when Consent-API answers 404 (nothing active). */
	private JsonElement get(String path, HttpEntity<Object> entity, String context, String lang) {
		try {
			ResponseEntity<String> response = lang == null
					? restTemplate.exchange(baseUrl + path, HttpMethod.GET, entity, String.class, context)
					: restTemplate.exchange(baseUrl + path, HttpMethod.GET, entity, String.class, context, lang);
			String body = response.getBody();
			if (body == null || body.isBlank()) {
				return null;
			}
			JsonElement root = JsonParser.parseString(body);
			if (!root.isJsonObject() || !root.getAsJsonObject().has("data")) {
				return null;
			}
			return root.getAsJsonObject().get("data");
		} catch (HttpClientErrorException.NotFound e) {
			return null;
		} catch (RestClientException | IllegalStateException | JsonParseException e) {
			throw new ConsentServiceUnavailableException(e);
		}
	}

	private static String string(JsonObject o, String key) {
		return o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsString() : null;
	}

	/** The central purpose list and the published languages, as offered to the admin. */
	public static final class CentralLists {

		private final List<PurposeMaster> purposes;
		private final List<LanguageMaster> languages;

		public CentralLists(List<PurposeMaster> purposes, List<LanguageMaster> languages) {
			this.purposes = Collections.unmodifiableList(purposes);
			this.languages = Collections.unmodifiableList(languages);
		}

		public List<PurposeMaster> getPurposes() {
			return purposes;
		}

		public List<LanguageMaster> getLanguages() {
			return languages;
		}

		public boolean hasPurpose(String code) {
			return purposes.stream().anyMatch(p -> p.getPurposeCode().equals(code));
		}

		public boolean isCentrallyMandatory(String code) {
			return purposes.stream().anyMatch(p -> p.getPurposeCode().equals(code) && Boolean.TRUE.equals(p.getIsMandatory()));
		}

		public boolean hasLanguage(String code) {
			return languages.stream().anyMatch(l -> l.getLanguageCode().equals(code));
		}
	}
}
