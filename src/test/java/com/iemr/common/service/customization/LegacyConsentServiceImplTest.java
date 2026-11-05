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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.iemr.common.dto.identity.BeneficiariesDTO;
import com.iemr.common.service.beneficiary.IdentityBeneficiaryService;
import com.iemr.common.utils.UserAgentContext;
import com.iemr.common.utils.exception.ConsentServiceUnavailableException;
import com.iemr.common.utils.exception.IEMRException;

/** Consent-API feature 008: A9–A10 in Common-API: fields, membership, Identity-API lookups, forwarding. */
class LegacyConsentServiceImplTest {

	private static final int USER = 501;
	private static final String AUTH = "Bearer token";
	private static final String IDEM = "3f2504e0-4f89-11d3-9a0c-0305e82c3301";
	private static final String BATCH = "8a1c3e2b-1111-4222-8333-944455556666";
	private static final String ID_A = "100245678901";
	private static final String ID_B = "100245678902";

	private ConsentNoticeClient client;
	private ConsentProjectAccess access;
	private IdentityBeneficiaryService identity;
	private LegacyConsentServiceImpl service;

	@BeforeEach
	void setUp() {
		client = mock(ConsentNoticeClient.class);
		access = mock(ConsentProjectAccess.class);
		identity = mock(IdentityBeneficiaryService.class);
		service = new LegacyConsentServiceImpl(client, access, identity, 4);
	}

	@AfterEach
	void tearDown() {
		RequestContextHolder.resetRequestAttributes();
		UserAgentContext.clear();
	}

	private static List<BeneficiariesDTO> found(long regId) {
		BeneficiariesDTO dto = new BeneficiariesDTO();
		dto.setBenRegId(BigInteger.valueOf(regId));
		return List.of(dto);
	}

	private static String file(String... ids) {
		StringBuilder rows = new StringBuilder();
		for (int i = 0; i < ids.length; i++) {
			rows.append(i == 0 ? "" : ",").append("{\"rowNo\":").append(i + 2).append(",\"beneficiaryId\":")
					.append(ids[i] == null ? "null" : "\"" + ids[i] + "\"")
					.append(",\"consentDate\":\"2025-06-12\",\"mode\":\"PAPER\",\"representativeName\":\"Gita\","
							+ "\"extra\":\"x\"}");
		}
		return "{\"serviceProviderId\":1,\"fileName\":\"old.csv\",\"uploadedBy\":\"someone\",\"rows\":[" + rows
				+ "]}";
	}

	private JsonObject forwarded() throws IEMRException {
		ArgumentCaptor<String> body = ArgumentCaptor.forClass(String.class);
		verify(client).checkLegacy(body.capture(), eq(IDEM), eq(AUTH));
		return JsonParser.parseString(body.getValue()).getAsJsonObject();
	}

	@Test
	void eachDistinctIdIsLookedUpOnceAndAddedToTheRows() throws Exception {
		when(identity.getBeneficiaryListByBenID(ID_A, AUTH, false)).thenReturn(found(7001L));
		when(identity.getBeneficiaryListByBenID(ID_B, AUTH, false)).thenReturn(List.of());
		when(client.checkLegacy(anyString(), eq(IDEM), eq(AUTH)))
				.thenReturn(JsonParser.parseString("{\"batchId\":\"b-1\"}"));

		String answer = service.validate(file(ID_A, " " + ID_A + " ", ID_B, "12345", null), USER, IDEM, AUTH);

		assertThat(answer).isEqualTo("{\"batchId\":\"b-1\"}");
		verify(access).requireProviderMember(USER, 1);
		verify(identity, times(1)).getBeneficiaryListByBenID(ID_A, AUTH, false);
		verify(identity, times(1)).getBeneficiaryListByBenID(ID_B, AUTH, false);
		verify(identity, times(2)).getBeneficiaryListByBenID(anyString(), any(), any());
		JsonObject body = forwarded();
		assertThat(body.has("uploadedBy")).isFalse();
		assertThat(body.get("serviceProviderId").getAsInt()).isEqualTo(1);
		assertThat(body.get("fileName").getAsString()).isEqualTo("old.csv");
		List<JsonObject> rows = body.getAsJsonArray("rows").asList().stream().map(e -> e.getAsJsonObject()).toList();
		assertThat(rows.get(0).get("beneficiaryRegId").getAsLong()).isEqualTo(7001L);
		assertThat(rows.get(1).get("beneficiaryRegId").getAsLong()).as("same ID, spaces").isEqualTo(7001L);
		assertThat(rows.get(2).get("beneficiaryRegId").isJsonNull()).as("not found").isTrue();
		assertThat(rows.get(3).get("beneficiaryRegId").isJsonNull()).as("not 12 digits").isTrue();
		assertThat(rows.get(4).get("beneficiaryRegId").isJsonNull()).as("missing").isTrue();
		assertThat(rows.get(0).has("extra")).isFalse();
		assertThat(rows.get(0).get("representativeName").getAsString()).isEqualTo("Gita");
		assertThat(rows.get(0).get("rowNo").getAsInt()).isEqualTo(2);
	}

	@Test
	void lookupsRunWithTheCallersRequestContextAndLeaveTheWorkerClean() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader("Jwttoken", "user-jwt");
		ServletRequestAttributes caller = new ServletRequestAttributes(request);
		RequestContextHolder.setRequestAttributes(caller);
		UserAgentContext.setUserAgent("admin-ui");
		AtomicReference<RequestAttributes> seen = new AtomicReference<>();
		AtomicReference<String> agent = new AtomicReference<>();
		when(identity.getBeneficiaryListByBenID(ID_A, AUTH, false)).thenAnswer(i -> {
			seen.set(RequestContextHolder.getRequestAttributes());
			agent.set(UserAgentContext.getUserAgent());
			return found(7001L);
		});

		service.validate(file(ID_A), USER, IDEM, AUTH);

		assertThat(seen.get()).isSameAs(caller);
		assertThat(agent.get()).isEqualTo("admin-ui");
		assertThat(RequestContextHolder.getRequestAttributes()).as("caller keeps its context").isSameAs(caller);
	}

	@Test
	void workerContextIsResetAfterALookup() throws Exception {
		RequestAttributes caller = new ServletRequestAttributes(new MockHttpServletRequest());
		assertThat(LegacyConsentServiceImpl.inContext(caller, "ua", () -> RequestContextHolder.getRequestAttributes()))
				.isSameAs(caller);
		assertThat(RequestContextHolder.getRequestAttributes()).isNull();
		assertThat(UserAgentContext.getUserAgent()).isNull();
		assertThatThrownBy(() -> LegacyConsentServiceImpl.inContext(caller, "ua", () -> {
			throw new IllegalStateException("boom");
		})).isInstanceOf(IllegalStateException.class);
		assertThat(RequestContextHolder.getRequestAttributes()).isNull();
	}

	@Test
	void aFailedLookupFailsTheWholeCheck() throws Exception {
		when(identity.getBeneficiaryListByBenID(ID_A, AUTH, false)).thenReturn(found(7001L));
		when(identity.getBeneficiaryListByBenID(ID_B, AUTH, false)).thenThrow(new IEMRException("identity down"));

		assertThatThrownBy(() -> service.validate(file(ID_A, ID_B), USER, IDEM, AUTH))
				.hasMessage(LegacyConsentServiceImpl.LOOKUP_UNAVAILABLE);
		verifyNoInteractions(client);
	}

	@Test
	void anInterruptedLookupFailsAndKeepsTheInterrupt() throws Exception {
		when(identity.getBeneficiaryListByBenID(ID_A, AUTH, false)).thenReturn(found(7001L));
		Thread.currentThread().interrupt();
		try {
			assertThatThrownBy(() -> service.validate(file(ID_A), USER, IDEM, AUTH))
					.hasMessage(LegacyConsentServiceImpl.LOOKUP_UNAVAILABLE);
			assertThat(Thread.currentThread().isInterrupted()).isTrue();
		} finally {
			Thread.interrupted();
		}
		verifyNoInteractions(client);
	}

	@Test
	void aFileWithoutValidIdsNeedsNoLookup() throws Exception {
		when(client.checkLegacy(anyString(), eq(IDEM), eq(AUTH))).thenReturn(null);

		assertThat(service.validate("{\"serviceProviderId\":1,\"rows\":[{\"rowNo\":1}]}", USER, IDEM, AUTH))
				.isNull();
		verifyNoInteractions(identity);
		assertThat(forwarded().has("fileName")).isFalse();
	}

	@Test
	void requestFieldsAreChecked() {
		assertThatThrownBy(() -> service.validate("{\"rows\":[]}", USER, IDEM, AUTH))
				.hasMessage("serviceProviderId and rows are required");
		assertThatThrownBy(() -> service.validate("{\"serviceProviderId\":1,\"rows\":{}}", USER, IDEM, AUTH))
				.hasMessage("serviceProviderId and rows are required");
		assertThatThrownBy(() -> service.validate("{\"serviceProviderId\":1}", USER, IDEM, AUTH))
				.hasMessage("serviceProviderId and rows are required");
		assertThatThrownBy(() -> service.validate("{\"serviceProviderId\":\"x\",\"rows\":[]}", USER, IDEM, AUTH))
				.hasMessage("serviceProviderId must be a number");
		assertThatThrownBy(() -> service.validate("{\"serviceProviderId\":null,\"rows\":[]}", USER, IDEM, AUTH))
				.hasMessage("serviceProviderId and rows are required");
		assertThatThrownBy(() -> service.validate("{\"serviceProviderId\":1,\"rows\":[1]}", USER, IDEM, AUTH))
				.hasMessage("rows[0] must be an object");
		assertThatThrownBy(() -> service.validate("[1]", USER, IDEM, AUTH)).hasMessage("Request body is required");
		assertThatThrownBy(() -> service.validate(null, USER, IDEM, AUTH)).hasMessage("Request body is required");
		assertThatThrownBy(() -> service.validate("{bad", USER, IDEM, AUTH)).hasMessage("Invalid request body");
		String tooMany = "{\"serviceProviderId\":1,\"rows\":[" + "{},".repeat(5000) + "{}]}";
		assertThatThrownBy(() -> service.validate(tooMany, USER, IDEM, AUTH)).hasMessage("rows: at most 5000 rows");
		verifyNoInteractions(client, identity, access);
	}

	@Test
	void onlyMembersOfTheServiceProviderMayUpload() throws Exception {
		doThrow(new IEMRException("project not found")).when(access).requireProviderMember(USER, 2);

		assertThatThrownBy(() -> service.validate("{\"serviceProviderId\":2,\"rows\":[]}", USER, IDEM, AUTH))
				.hasMessage(LegacyConsentServiceImpl.NOT_ALLOWED);
		assertThatThrownBy(() -> service.importBatch("{\"serviceProviderId\":2,\"batchId\":\"" + BATCH + "\"}", USER,
				IDEM, AUTH)).hasMessage(LegacyConsentServiceImpl.NOT_ALLOWED);
		verifyNoInteractions(client, identity);
	}

	@Test
	void importForwardsOnlyTheBatchId() throws Exception {
		when(client.importLegacy(BATCH, IDEM, AUTH)).thenReturn(JsonParser.parseString("{\"status\":\"IMPORTED\"}"));

		assertThat(service.importBatch("{\"serviceProviderId\":1,\"batchId\":\"" + BATCH + "\",\"importedBy\":\"x\"}",
				USER, IDEM, AUTH)).isEqualTo("{\"status\":\"IMPORTED\"}");
		verify(access).requireProviderMember(USER, 1);
	}

	@Test
	void importFieldsAreChecked() throws Exception {
		assertThatThrownBy(() -> service.importBatch("{\"serviceProviderId\":1}", USER, IDEM, AUTH))
				.hasMessage("batchId and serviceProviderId are required");
		assertThatThrownBy(() -> service.importBatch("{\"batchId\":\"" + BATCH + "\"}", USER, IDEM, AUTH))
				.hasMessage("batchId and serviceProviderId are required");
		assertThatThrownBy(() -> service.importBatch("{\"serviceProviderId\":1,\"batchId\":\"../x\"}", USER, IDEM,
				AUTH)).hasMessage("batchId must be a UUID");
		verify(client, never()).importLegacy(any(), any(), any());
	}

	@Test
	void consentApiRefusalsAndOutagesAre5000Messages() throws Exception {
		when(client.importLegacy(BATCH, IDEM, AUTH)).thenThrow(new IEMRException("Batch already imported"))
				.thenThrow(new ConsentServiceUnavailableException());
		String request = "{\"serviceProviderId\":1,\"batchId\":\"" + BATCH + "\"}";

		assertThatThrownBy(() -> service.importBatch(request, USER, IDEM, AUTH)).hasMessage("Batch already imported");
		assertThatThrownBy(() -> service.importBatch(request, USER, IDEM, AUTH)).isInstanceOf(IEMRException.class)
				.hasMessage(new ConsentServiceUnavailableException().getMessage());
	}

	@Test
	void aZeroThreadSettingStillLooksUp() throws Exception {
		LegacyConsentServiceImpl single = new LegacyConsentServiceImpl(client, access, identity, 0);
		when(identity.getBeneficiaryListByBenID(ID_A, AUTH, false)).thenReturn(null);

		single.validate(file(ID_A), USER, IDEM, AUTH);

		assertThat(forwarded().getAsJsonArray("rows").get(0).getAsJsonObject().get("beneficiaryRegId").isJsonNull())
				.isTrue();
	}
}
