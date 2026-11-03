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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.iemr.common.service.customization.ConsentMastersClient.CentralLists;
import com.iemr.common.utils.exception.ConsentServiceUnavailableException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ConsentMastersClientTest {

	private static final String BASE = "http://consent.test";
	private static final String LANGUAGES = BASE + "/consent/v1/notices/active/languages?context={context}";
	private static final String NOTICE = BASE + "/consent/v1/notices/active?context={context}&lang={lang}";

	@Mock
	private RestTemplate restTemplate;

	private ConsentMastersClient client() {
		return new ConsentMastersClient(restTemplate, BASE, "en");
	}

	private void languages(String context, String body) {
		when(restTemplate.exchange(eq(LANGUAGES), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class),
				eq(context))).thenReturn(ResponseEntity.ok(body));
	}

	private void notice(String context, String body) {
		when(restTemplate.exchange(eq(NOTICE), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class),
				eq(context), eq("en"))).thenReturn(ResponseEntity.ok(body));
	}

	@Test
	@DisplayName("a trailing slash on the configured URL does not produce a double slash")
	void trailingSlashIsIgnored() {
		languages("REGISTRATION", "{\"data\":[]}");
		languages("REVISIT", "{\"data\":[]}");
		notice("REGISTRATION", "{\"data\":{\"purposes\":[]}}");
		notice("REVISIT", "{\"data\":{\"purposes\":[]}}");

		assertThat(new ConsentMastersClient(restTemplate, BASE + "/", "en").load("Bearer t").getLanguages()).isEmpty();
	}

	@Test
	@DisplayName("languages and purposes are the union of both contexts; mandatory if either notice says so")
	void unionOfContexts() {
		languages("REGISTRATION", "{\"data\":[{\"languageCode\":\"en\",\"languageName\":\"English\",\"hasAudio\":true},"
				+ "{\"languageCode\":\"hi\",\"languageName\":\"Hindi\"},{\"languageName\":\"No code\"}]}");
		languages("REVISIT", "{\"data\":[{\"languageCode\":\"hi\",\"languageName\":\"Hindi\"},"
				+ "{\"languageCode\":\"as\",\"languageName\":\"Assamese\"}]}");
		notice("REGISTRATION", "{\"data\":{\"purposes\":[{\"purposeCode\":\"P-REG\",\"name\":\"Registration\",\"isMandatory\":true},"
				+ "{\"purposeCode\":\"P-SMS\",\"name\":\"SMS\",\"isMandatory\":false},{\"name\":\"No code\"}]}}");
		notice("REVISIT", "{\"data\":{\"purposes\":[{\"purposeCode\":\"P-SMS\",\"name\":\"SMS\",\"isMandatory\":true},"
				+ "{\"purposeCode\":\"P-RES\",\"name\":\"Research\",\"isMandatory\":null}]}}");

		CentralLists lists = client().load("Bearer t");

		assertThat(lists.getLanguages()).extracting("languageCode").containsExactly("en", "hi", "as");
		assertThat(lists.getLanguages()).allMatch(l -> l.getHasNotice());
		assertThat(lists.getPurposes()).extracting("purposeCode").containsExactly("P-REG", "P-SMS", "P-RES");
		assertThat(lists.isCentrallyMandatory("P-REG")).isTrue();
		assertThat(lists.isCentrallyMandatory("P-SMS")).isTrue();
		assertThat(lists.isCentrallyMandatory("P-RES")).isFalse();
		assertThat(lists.isCentrallyMandatory("P-XYZ")).isFalse();
		assertThat(lists.hasPurpose("P-RES")).isTrue();
		assertThat(lists.hasPurpose("P-XYZ")).isFalse();
		assertThat(lists.hasLanguage("as")).isTrue();
		assertThat(lists.hasLanguage("fr")).isFalse();
	}

	@Test
	@DisplayName("a purpose stays mandatory when a later notice lists it as optional; missing fields are tolerated")
	void laterOptionalKeepsMandatory() {
		languages("REGISTRATION", "{\"data\":[{\"languageCode\":\"en\",\"languageName\":null}]}");
		languages("REVISIT", "{\"data\":[]}");
		notice("REGISTRATION", "{\"data\":{\"purposes\":[{\"purposeCode\":\"P-REG\",\"name\":\"Registration\",\"isMandatory\":true},"
				+ "{\"purposeCode\":\"P-SMS\",\"name\":\"SMS\"}]}}");
		notice("REVISIT", "{\"data\":{\"purposes\":[{\"purposeCode\":\"P-REG\",\"name\":\"Registration\",\"isMandatory\":false}]}}");

		CentralLists lists = client().load("Bearer t");

		assertThat(lists.isCentrallyMandatory("P-REG")).isTrue();
		assertThat(lists.isCentrallyMandatory("P-SMS")).isFalse();
		assertThat(lists.getLanguages().get(0).getLanguageName()).isNull();
	}

	@Test
	@DisplayName("notice data that is not an object adds no purposes")
	void noticeDataNotObject() {
		languages("REGISTRATION", "{\"data\":[]}");
		languages("REVISIT", "{\"data\":[]}");
		notice("REGISTRATION", "{\"data\":[]}");
		notice("REVISIT", "{\"data\":null}");

		assertThat(client().load("Bearer t").getPurposes()).isEmpty();
	}

	@Test
	@DisplayName("a context with no active notice (404) or an empty answer adds nothing")
	void notFoundAndEmptyAddNothing() {
		when(restTemplate.exchange(eq(LANGUAGES), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class),
				eq("REGISTRATION"))).thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "", null, null, null));
		languages("REVISIT", "");
		notice("REGISTRATION", "{\"data\":{\"body\":\"no purposes\"}}");
		when(restTemplate.exchange(eq(NOTICE), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class),
				eq("REVISIT"), eq("en"))).thenReturn(ResponseEntity.ok((String) null));

		CentralLists lists = client().load("Bearer t");

		assertThat(lists.getLanguages()).isEmpty();
		assertThat(lists.getPurposes()).isEmpty();
	}

	@Test
	@DisplayName("answers without data, or with data of the wrong shape, add nothing")
	void unexpectedShapesIgnored() {
		languages("REGISTRATION", "[]");
		languages("REVISIT", "{\"data\":{}}");
		notice("REGISTRATION", "{\"success\":true}");
		notice("REVISIT", "{\"data\":{\"purposes\":{}}}");

		CentralLists lists = client().load("Bearer t");

		assertThat(lists.getLanguages()).isEmpty();
		assertThat(lists.getPurposes()).isEmpty();
	}

	@Test
	@DisplayName("Consent-API unreachable or failing means the service is unavailable")
	void unreachableIsUnavailable() {
		when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class),
				eq("REGISTRATION"))).thenThrow(new ResourceAccessException("refused"));

		assertThatThrownBy(() -> client().load("Bearer t")).isInstanceOf(ConsentServiceUnavailableException.class)
				.hasMessage("Consent service unavailable, try again");
	}

	@Test
	@DisplayName("a body that is not JSON means the service is unavailable")
	void malformedBodyIsUnavailable() {
		languages("REGISTRATION", "{not json");

		assertThatThrownBy(() -> client().load("Bearer t")).isInstanceOf(ConsentServiceUnavailableException.class);
	}

	@Test
	@DisplayName("without a configured Consent-API URL nothing is called")
	void noUrlIsUnavailable() {
		ConsentMastersClient client = new ConsentMastersClient(restTemplate, "  ", "en");

		assertThatThrownBy(() -> client.load("Bearer t")).isInstanceOf(ConsentServiceUnavailableException.class);
		assertThatThrownBy(() -> new ConsentMastersClient(restTemplate, null, "en").load("Bearer t"))
				.isInstanceOf(ConsentServiceUnavailableException.class);
		verifyNoInteractions(restTemplate);
	}

	@Test
	@DisplayName("the Spring constructor builds a client with timeouts")
	void springConstructor() {
		assertThatThrownBy(() -> new ConsentMastersClient("", "en", 1000).load("Bearer t"))
				.isInstanceOf(ConsentServiceUnavailableException.class);
	}
}
