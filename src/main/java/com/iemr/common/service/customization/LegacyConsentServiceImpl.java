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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.iemr.common.dto.identity.BeneficiariesDTO;
import com.iemr.common.service.beneficiary.IdentityBeneficiaryService;
import com.iemr.common.utils.UserAgentContext;
import com.iemr.common.utils.exception.ConsentServiceUnavailableException;
import com.iemr.common.utils.exception.IEMRException;

/**
 * Forwards the Admin legacy consent upload to Consent-API (A9–A10, Consent-API feature 008, research R1, R2). Here
 * only: the request fields, that the caller belongs to the service provider (any user of the provider, as for A8),
 * and, for A9, the Identity-API lookup of each distinct 12-digit Beneficiary ID. Lookups run in parallel on worker
 * threads that carry the caller's request context, so the user's {@code Jwttoken} reaches Identity-API. An ID that
 * is not 12 digits or not found is sent as {@code beneficiaryRegId: null}; a failed lookup fails the whole check.
 * The uploader and importer are never taken from the body. No ID, row or name is logged.
 */
@Service
public class LegacyConsentServiceImpl implements LegacyConsentService {

	static final String NOT_ALLOWED = "You are not allowed to import legacy consents";

	static final String LOOKUP_UNAVAILABLE = "Beneficiary lookup unavailable, try again";

	static final int MAX_ROWS = 5000;

	static final long LOOKUP_TIMEOUT_SECONDS = 120;

	private static final Pattern BENEFICIARY_ID = Pattern.compile("^\\d{12}$");

	private static final Pattern UUID_TEXT = Pattern
			.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

	/** The row columns forwarded to Consent-API; anything else in a row is dropped. */
	private static final List<String> ROW_FIELDS = List.of("rowNo", "beneficiaryId", "consentDate",
			"purposesGranted", "languageCode", "mode", "representativeName", "representativeRelationship",
			"remarks");

	private final ConsentNoticeClient client;

	private final ConsentProjectAccess access;

	private final IdentityBeneficiaryService identity;

	private final int lookupThreads;

	public LegacyConsentServiceImpl(ConsentNoticeClient client, ConsentProjectAccess access,
			IdentityBeneficiaryService identity,
			@Value("${consent.legacy.lookup-threads:8}") int lookupThreads) {
		this.client = client;
		this.access = access;
		this.identity = identity;
		this.lookupThreads = Math.max(1, lookupThreads);
	}

	@Override
	public String validate(String request, Integer userId, String idempotencyKey, String authorization)
			throws IEMRException {
		JsonObject body = parse(request);
		Integer serviceProviderId = integer(body, "serviceProviderId");
		JsonElement rows = body.get("rows");
		if (serviceProviderId == null || rows == null || !rows.isJsonArray()) {
			throw new IEMRException("serviceProviderId and rows are required");
		}
		if (rows.getAsJsonArray().size() > MAX_ROWS) {
			throw new IEMRException("rows: at most " + MAX_ROWS + " rows");
		}
		List<JsonObject> input = new ArrayList<>();
		for (int i = 0; i < rows.getAsJsonArray().size(); i++) {
			JsonElement row = rows.getAsJsonArray().get(i);
			if (!row.isJsonObject()) {
				throw new IEMRException("rows[" + i + "] must be an object");
			}
			input.add(row.getAsJsonObject());
		}
		requireMember(userId, serviceProviderId);
		Map<String, Long> regIds = lookUp(input, authorization);
		JsonObject forward = new JsonObject();
		forward.addProperty("serviceProviderId", serviceProviderId);
		copy(body, forward, "fileName");
		JsonArray forwardRows = new JsonArray();
		for (JsonObject row : input) {
			JsonObject out = new JsonObject();
			ROW_FIELDS.forEach(field -> copy(row, out, field));
			out.addProperty("beneficiaryRegId", regIds.get(beneficiaryId(row)));
			forwardRows.add(out);
		}
		forward.add("rows", forwardRows);
		return forward(() -> client.checkLegacy(forward.toString(), idempotencyKey, authorization));
	}

	@Override
	public String importBatch(String request, Integer userId, String idempotencyKey, String authorization)
			throws IEMRException {
		JsonObject body = parse(request);
		Integer serviceProviderId = integer(body, "serviceProviderId");
		String batchId = string(body, "batchId");
		if (serviceProviderId == null || batchId == null) {
			throw new IEMRException("batchId and serviceProviderId are required");
		}
		if (!UUID_TEXT.matcher(batchId).matches()) {
			throw new IEMRException("batchId must be a UUID");
		}
		requireMember(userId, serviceProviderId);
		return forward(() -> client.importLegacy(batchId, idempotencyKey, authorization));
	}

	private void requireMember(Integer userId, Integer serviceProviderId) throws IEMRException {
		try {
			access.requireProviderMember(userId, serviceProviderId);
		} catch (IEMRException e) {
			throw new IEMRException(NOT_ALLOWED);
		}
	}

	/** Each distinct 12-digit ID looked up once, in parallel; IDs not found map to null. */
	private Map<String, Long> lookUp(List<JsonObject> rows, String authorization) throws IEMRException {
		Set<String> ids = new LinkedHashSet<>();
		rows.stream().map(LegacyConsentServiceImpl::beneficiaryId).filter(id -> id != null
				&& BENEFICIARY_ID.matcher(id).matches()).forEach(ids::add);
		Map<String, Long> found = new HashMap<>();
		if (ids.isEmpty()) {
			return found;
		}
		RequestAttributes context = RequestContextHolder.getRequestAttributes();
		String userAgent = UserAgentContext.getUserAgent();
		List<String> order = new ArrayList<>(ids);
		List<Callable<Long>> tasks = order.stream()
				.<Callable<Long>>map(id -> () -> inContext(context, userAgent,
						() -> regId(id, authorization)))
				.toList();
		ExecutorService pool = Executors.newFixedThreadPool(Math.min(lookupThreads, tasks.size()));
		try {
			List<Future<Long>> results = pool.invokeAll(tasks, LOOKUP_TIMEOUT_SECONDS, TimeUnit.SECONDS);
			for (int i = 0; i < order.size(); i++) {
				found.put(order.get(i), results.get(i).get());
			}
			return found;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IEMRException(LOOKUP_UNAVAILABLE);
		} catch (Exception e) {
			throw new IEMRException(LOOKUP_UNAVAILABLE);
		} finally {
			pool.shutdownNow();
		}
	}

	/** Runs a lookup with the caller's request context and user agent, then clears them from the worker. */
	static <T> T inContext(RequestAttributes context, String userAgent, Callable<T> lookup) throws Exception {
		RequestContextHolder.setRequestAttributes(context);
		UserAgentContext.setUserAgent(userAgent);
		try {
			return lookup.call();
		} finally {
			RequestContextHolder.resetRequestAttributes();
			UserAgentContext.clear();
		}
	}

	private Long regId(String beneficiaryId, String authorization) throws IEMRException {
		List<BeneficiariesDTO> matches = identity.getBeneficiaryListByBenID(beneficiaryId, authorization,
				false);
		BigInteger regId = matches == null || matches.isEmpty() ? null : matches.get(0).getBenRegId();
		return regId == null ? null : regId.longValueExact();
	}

	private static String beneficiaryId(JsonObject row) {
		String id = string(row, "beneficiaryId");
		return id == null ? null : id.trim();
	}

	private static String forward(Call call) throws IEMRException {
		try {
			JsonElement data = call.run();
			return data == null ? null : data.toString();
		} catch (ConsentServiceUnavailableException e) {
			throw new IEMRException(e.getMessage());
		}
	}

	private static JsonObject parse(String request) throws IEMRException {
		JsonElement element;
		try {
			element = request == null ? null : JsonParser.parseString(request);
		} catch (JsonParseException e) {
			throw new IEMRException("Invalid request body");
		}
		if (element == null || !element.isJsonObject()) {
			throw new IEMRException("Request body is required");
		}
		return element.getAsJsonObject();
	}

	private static Integer integer(JsonObject body, String field) throws IEMRException {
		if (!body.has(field) || body.get(field).isJsonNull()) {
			return null;
		}
		try {
			return body.get(field).getAsInt();
		} catch (RuntimeException e) {
			throw new IEMRException(field + " must be a number");
		}
	}

	private static String string(JsonObject body, String field) {
		return body.has(field) && body.get(field).isJsonPrimitive() ? body.get(field).getAsString() : null;
	}

	private static void copy(JsonObject from, JsonObject to, String field) {
		if (from.has(field)) {
			to.add(field, from.get(field));
		}
	}

	/** A Consent-API call. */
	@FunctionalInterface
	private interface Call {
		JsonElement run() throws IEMRException;
	}
}
