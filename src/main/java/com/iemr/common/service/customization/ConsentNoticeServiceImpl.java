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
import java.util.List;

import org.springframework.stereotype.Service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.iemr.common.utils.exception.ConsentServiceUnavailableException;
import com.iemr.common.utils.exception.IEMRException;

/**
 * Forwards the Admin "Consent message" tab to Consent-API (A6–A8, feature 006, research R1, R2, R11). Here only:
 * the request fields the front end sends and that the project belongs to the caller's service provider. The actor is never taken from the body ({@code savedBy},
 * {@code publishedBy} are dropped); Consent-API uses the logged-in user.
 */
@Service
public class ConsentNoticeServiceImpl implements ConsentNoticeService {

	static final String ENGLISH_ONLY = "Only English can be edited; translations are generated on publish";

	private final ConsentNoticeClient client;

	private final ConsentProjectAccess access;

	public ConsentNoticeServiceImpl(ConsentNoticeClient client, ConsentProjectAccess access) {
		this.client = client;
		this.access = access;
	}

	@Override
	public String fetchByProject(String request, Integer userId, String authorization) throws Exception {
		JsonObject body = parse(request);
		Integer projectId = integer(body, "projectId");
		Integer serviceProviderId = integer(body, "serviceProviderId");
		if (projectId == null || serviceProviderId == null) {
			throw new IEMRException("projectId and serviceProviderId are required");
		}
		access.requireOwnedProject(projectId, serviceProviderId, userId);
		return forward(() -> client.list(projectId, authorization));
	}

	@Override
	public String saveDraft(String request, Integer userId, String idempotencyKey, String authorization)
			throws Exception {
		JsonObject body = parse(request);
		Target target = target(body, true);
		access.requireOwnedProject(target.projectId(), target.serviceProviderId(), userId);
		JsonObject message = body.deepCopy();
		for (String field : List.of("projectId", "serviceProviderId", "context", "savedBy", "createdBy")) {
			message.remove(field);
		}
		message.addProperty("languageCode", "en");
		return forward(() -> client.saveDraft(target.projectId(), target.context(), message.toString(),
				idempotencyKey, authorization));
	}

	@Override
	public String publish(String request, Integer userId, String idempotencyKey, String authorization)
			throws Exception {
		JsonObject body = parse(request);
		Target target = target(body, false);
		access.requireOwnedProject(target.projectId(), target.serviceProviderId(), userId);
		JsonObject options = new JsonObject();
		copy(body, options, "requireReconsent");
		copy(body, options, "translateTo");
		return forward(() -> client.publish(target.projectId(), target.context(), options.toString(),
				idempotencyKey, authorization));
	}

	private record Target(Integer projectId, Integer serviceProviderId, String context) {
	}

	/** The project, provider and context of a draft or publish; all problems are reported together. */
	private static Target target(JsonObject body, boolean needsBody) throws IEMRException {
		List<String> errors = new ArrayList<>();
		Integer projectId = integer(body, "projectId");
		Integer serviceProviderId = integer(body, "serviceProviderId");
		String context = string(body, "context");
		if (projectId == null) {
			errors.add("projectId is required");
		}
		if (serviceProviderId == null) {
			errors.add("serviceProviderId is required");
		}
		if (context == null) {
			errors.add("context is required");
		} else if (!"REGISTRATION".equals(context) && !"REVISIT".equals(context)) {
			errors.add("context must be REGISTRATION or REVISIT");
		}
		String text = string(body, "body");
		if (needsBody && (text == null || text.isBlank())) {
			errors.add("body is required");
		}
		String language = string(body, "languageCode");
		if (language != null && !"en".equals(language)) {
			errors.add(ENGLISH_ONLY);
		}
		if (!errors.isEmpty()) {
			throw new IEMRException(String.join("; ", errors));
		}
		return new Target(projectId, serviceProviderId, context);
	}

	private interface Call {
		JsonElement run() throws IEMRException;
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
}
