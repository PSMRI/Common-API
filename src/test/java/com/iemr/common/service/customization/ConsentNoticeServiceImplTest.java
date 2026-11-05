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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.google.gson.JsonParser;
import com.iemr.common.utils.exception.ConsentConfigConflictException;
import com.iemr.common.utils.exception.ConsentServiceUnavailableException;
import com.iemr.common.utils.exception.IEMRException;

/** Feature 006: A6–A8 in Common-API: fields, ownership, forwarding (research R2, R11). */
class ConsentNoticeServiceImplTest {

	private static final int USER = 501;
	private static final String AUTH = "Bearer token";
	private static final String IDEM = "3f2504e0-4f89-11d3-9a0c-0305e82c3301";

	private ConsentNoticeClient client;
	private ConsentProjectAccess access;
	private ConsentNoticeServiceImpl service;

	@BeforeEach
	void setUp() {
		client = mock(ConsentNoticeClient.class);
		access = mock(ConsentProjectAccess.class);
		service = new ConsentNoticeServiceImpl(client, access);
	}

	@Test
	void fetchByProjectChecksOwnershipAndReturnsTheData() throws Exception {
		when(client.list(3, AUTH)).thenReturn(JsonParser.parseString("[{\"languageCode\":\"en\"}]"));

		assertThat(service.fetchByProject("{\"projectId\":3,\"serviceProviderId\":1}", USER, AUTH))
				.isEqualTo("[{\"languageCode\":\"en\"}]");
		verify(access).requireOwnedProject(3, 1, USER);
	}

	@Test
	void fetchByProjectNeedsBothIds() {
		assertThatThrownBy(() -> service.fetchByProject("{\"projectId\":3}", USER, AUTH))
				.hasMessage("projectId and serviceProviderId are required");
		assertThatThrownBy(() -> service.fetchByProject("{\"serviceProviderId\":1,\"projectId\":null}", USER, AUTH))
				.hasMessage("projectId and serviceProviderId are required");
		assertThatThrownBy(() -> service.fetchByProject("{\"projectId\":\"x\",\"serviceProviderId\":1}", USER, AUTH))
				.hasMessage("projectId must be a number");
		assertThatThrownBy(() -> service.fetchByProject("not json {", USER, AUTH)).hasMessage("Invalid request body");
		assertThatThrownBy(() -> service.fetchByProject("[1]", USER, AUTH)).hasMessage("Request body is required");
		assertThatThrownBy(() -> service.fetchByProject(null, USER, AUTH)).hasMessage("Request body is required");
		verifyNoInteractions(client);
	}

	@Test
	void anotherProvidersProjectIsNotForwarded() throws Exception {
		doThrow(new IEMRException("project not found")).when(access).requireOwnedProject(3, 2, USER);
		assertThatThrownBy(() -> service.fetchByProject("{\"projectId\":3,\"serviceProviderId\":2}", USER, AUTH))
				.hasMessage("project not found");
		verifyNoInteractions(client);
	}

	@Test
	void saveDraftForwardsOnlyTheMessageInEnglish() throws Exception {
		when(client.saveDraft(eq(3), eq("REGISTRATION"), anyString(), eq(IDEM), eq(AUTH)))
				.thenReturn(JsonParser.parseString("{\"languageCode\":\"en\"}"));

		String data = service.saveDraft("{\"projectId\":3,\"serviceProviderId\":1,\"context\":\"REGISTRATION\","
				+ "\"body\":\"B\",\"declaration\":\"I agree\",\"savedBy\":\"someone.else\"}", USER, IDEM, AUTH);

		assertThat(data).isEqualTo("{\"languageCode\":\"en\"}");
		ArgumentCaptor<String> body = ArgumentCaptor.forClass(String.class);
		verify(client).saveDraft(eq(3), eq("REGISTRATION"), body.capture(), eq(IDEM), eq(AUTH));
		assertThat(JsonParser.parseString(body.getValue())).isEqualTo(JsonParser
				.parseString("{\"body\":\"B\",\"declaration\":\"I agree\",\"languageCode\":\"en\"}"));
		verify(access).requireOwnedProject(3, 1, USER);
	}

	@Test
	void saveDraftReportsEveryMissingField() {
		assertThatThrownBy(() -> service.saveDraft("{}", USER, IDEM, AUTH)).hasMessage("projectId is required; "
				+ "serviceProviderId is required; context is required; body is required");
		assertThatThrownBy(() -> service.saveDraft("{\"projectId\":3,\"serviceProviderId\":1,\"context\":\"HELPLINE\","
				+ "\"body\":\" \",\"languageCode\":\"hi\"}", USER, IDEM, AUTH))
				.hasMessage("context must be REGISTRATION or REVISIT; body is required; "
						+ ConsentNoticeServiceImpl.ENGLISH_ONLY);
		assertThatThrownBy(() -> service.saveDraft("{\"projectId\":3,\"serviceProviderId\":1,\"context\":{},"
				+ "\"body\":[\"B\"]}", USER, IDEM, AUTH)).hasMessage("context is required; body is required");
		verifyNoInteractions(client);
	}

	@Test
	void publishChecksOwnershipAndForwardsOnlyTheOptions() throws Exception {
		String request = "{\"projectId\":3,\"serviceProviderId\":1,\"context\":\"REVISIT\",\"languageCode\":\"en\","
				+ "\"requireReconsent\":true,\"translateTo\":[\"hi\"],\"publishedBy\":\"x\"}";
		when(client.publish(eq(3), eq("REVISIT"), anyString(), eq(IDEM), eq(AUTH))).thenReturn(null);

		assertThat(service.publish(request, USER, IDEM, AUTH)).isNull();
		verify(access).requireOwnedProject(3, 1, USER);
		ArgumentCaptor<String> body = ArgumentCaptor.forClass(String.class);
		verify(client).publish(eq(3), eq("REVISIT"), body.capture(), eq(IDEM), eq(AUTH));
		assertThat(JsonParser.parseString(body.getValue()))
				.isEqualTo(JsonParser.parseString("{\"requireReconsent\":true,\"translateTo\":[\"hi\"]}"));
	}

	@Test
	void publishOfAnotherProvidersProjectIsNotForwarded() throws Exception {
		doThrow(new IEMRException("project not found")).when(access).requireOwnedProject(3, 2, USER);
		assertThatThrownBy(() -> service.publish("{\"projectId\":3,\"serviceProviderId\":2,\"context\":\"REVISIT\"}",
				USER, IDEM, AUTH)).hasMessage("project not found");
		verifyNoInteractions(client);
	}

	@Test
	void consentApiOutcomesPassThrough() throws Exception {
		String request = "{\"projectId\":3,\"serviceProviderId\":1,\"context\":\"REGISTRATION\",\"body\":\"B\"}";
		when(client.saveDraft(any(), any(), any(), any(), any())).thenThrow(new ConsentConfigConflictException())
				.thenThrow(new IEMRException("Enter the patient declaration"))
				.thenThrow(new ConsentServiceUnavailableException());

		assertThatThrownBy(() -> service.saveDraft(request, USER, IDEM, AUTH))
				.isInstanceOf(ConsentConfigConflictException.class);
		assertThatThrownBy(() -> service.saveDraft(request, USER, IDEM, AUTH))
				.hasMessage("Enter the patient declaration");
		assertThatThrownBy(() -> service.saveDraft(request, USER, IDEM, AUTH)).isInstanceOf(IEMRException.class)
				.hasMessage("Consent service unavailable, try again");
	}
}
