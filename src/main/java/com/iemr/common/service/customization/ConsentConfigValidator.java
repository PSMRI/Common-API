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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.iemr.common.data.customization.consent.ConsentMode;
import com.iemr.common.data.customization.consent.ContextConfig;
import com.iemr.common.data.customization.consent.GuardianRule;
import com.iemr.common.data.customization.consent.ModeEntry;
import com.iemr.common.data.customization.consent.ModeSettings;
import com.iemr.common.data.customization.consent.OtpTarget;
import com.iemr.common.data.customization.consent.PhotoRequirement;
import com.iemr.common.data.customization.consent.ProjectConsentConfigRequest;
import com.iemr.common.data.customization.consent.PurposeEntry;
import com.iemr.common.service.customization.ConsentMastersClient.CentralLists;
import com.iemr.common.utils.exception.IEMRException;

/**
 * Checks a consent configuration before it is saved, fills in defaults and drops settings that do not
 * belong to the entry's mode. All problems are reported together in one message.
 */
@Component
public class ConsentConfigValidator {

	/** Proposal §3.1 rule 7 and DPDP Rule 10: patients under 18 get the guardian step. */
	static final int GUARDIAN_AGE = 18;
	/** Proposal §3.1 rule 6: a voice clip is a 30–60 s recording. */
	static final int VOICE_MIN = 30;
	static final int VOICE_MAX = 60;
	static final int VOICE_DEFAULT = VOICE_MIN;
	static final int PROJECT_NAME_MAX = 100;

	/** The fields needed before anything is read: project, provider and (for an update) the base version. */
	public void validateIdentity(ProjectConsentConfigRequest request, boolean update) throws IEMRException {
		List<String> errors = new ArrayList<>();
		if (request.getProjectId() == null) {
			errors.add("projectId is required");
		}
		if (request.getProjectName() == null || request.getProjectName().isBlank()) {
			errors.add("projectName is required");
		} else if (request.getProjectName().length() > PROJECT_NAME_MAX) {
			errors.add("projectName must be at most " + PROJECT_NAME_MAX + " characters");
		}
		if (request.getServiceProviderId() == null) {
			errors.add("serviceProviderId is required");
		}
		if (update && (request.getBaseVersion() == null || request.getBaseVersion() < 1)) {
			errors.add("baseVersion must be 1 or more");
		}
		throwIfAny(errors);
	}

	/** Validates and normalises both contexts against Consent-API's purpose and language lists. */
	public void validateAndNormalise(ProjectConsentConfigRequest request, CentralLists central) throws IEMRException {
		List<String> errors = new ArrayList<>();
		if (request.getRegistration() == null) {
			errors.add("registration is required");
		} else {
			validateContext("registration", request.getRegistration(), central, errors);
		}
		if (request.getRevisit() != null) {
			validateContext("revisit", request.getRevisit(), central, errors);
		}
		throwIfAny(errors);
	}

	private void validateContext(String prefix, ContextConfig config, CentralLists central, List<String> errors) {
		if (config.getEnabled() == null) {
			errors.add(prefix + ".enabled is required");
		}
		if (config.getMandatory() == null) {
			errors.add(prefix + ".mandatory is required");
		}
		boolean enabled = Boolean.TRUE.equals(config.getEnabled());
		Set<ConsentMode> modes = validateModes(prefix, config, errors);
		if (enabled && modes.isEmpty()) {
			errors.add(prefix + ": at least one mode must be allowed when consent is enabled");
		}
		if (config.getDefaultMode() != null && !modes.contains(config.getDefaultMode())) {
			errors.add(prefix + ".defaultMode must be one of the allowed modes");
		}
		validatePurposes(prefix, config.getPurposes(), central, errors);
		validateLanguages(prefix, config.getLanguages(), central, errors);
		validateGuardian(prefix, config.getGuardian(), enabled, errors);
	}

	private Set<ConsentMode> validateModes(String prefix, ContextConfig config, List<String> errors) {
		Set<ConsentMode> modes = new HashSet<>();
		if (config.getModes() == null) {
			errors.add(prefix + ".modes is required");
			return modes;
		}
		Set<Integer> ranks = new HashSet<>();
		for (int i = 0; i < config.getModes().size(); i++) {
			ModeEntry entry = config.getModes().get(i);
			String at = prefix + ".modes[" + i + "]";
			if (entry == null || entry.getMode() == null) {
				errors.add(at + ".mode is missing or not a valid mode");
				continue;
			}
			if (!modes.add(entry.getMode())) {
				errors.add(prefix + ".modes: " + entry.getMode() + " appears more than once");
			}
			checkRank(at, prefix + ".modes", entry.getRank(), ranks, errors);
			entry.setSettings(normaliseSettings(at, entry.getMode(), entry.getSettings(), errors));
		}
		return modes;
	}

	private ModeSettings normaliseSettings(String at, ConsentMode mode, ModeSettings given, List<String> errors) {
		ModeSettings in = given == null ? new ModeSettings() : given;
		ModeSettings out = new ModeSettings();
		switch (mode) {
		case VOICE_CLIP:
			Integer seconds = in.getVoiceMinSeconds();
			if (seconds == null) {
				seconds = VOICE_DEFAULT;
			} else if (seconds < VOICE_MIN || seconds > VOICE_MAX) {
				errors.add(at + ".settings.voiceMinSeconds must be between " + VOICE_MIN + " and " + VOICE_MAX);
			}
			out.setVoiceMinSeconds(seconds);
			return out;
		case OTP:
			out.setOtpTarget(in.getOtpTarget() == null ? OtpTarget.PATIENT : in.getOtpTarget());
			return out;
		case WITNESSED:
			out.setWitnessRelationshipRequired(
					in.getWitnessRelationshipRequired() == null || in.getWitnessRelationshipRequired());
			return out;
		case SIGNATURE_PHOTO:
			out.setPhotos(in.getPhotos() == null ? PhotoRequirement.BOTH : in.getPhotos());
			return out;
		default:
			return null;
		}
	}

	private void validatePurposes(String prefix, List<PurposeEntry> purposes, CentralLists central,
			List<String> errors) {
		if (purposes == null) {
			errors.add(prefix + ".purposes is required");
			return;
		}
		Set<String> codes = new HashSet<>();
		Set<Integer> ranks = new HashSet<>();
		for (int i = 0; i < purposes.size(); i++) {
			PurposeEntry purpose = purposes.get(i);
			String at = prefix + ".purposes[" + i + "]";
			if (purpose == null || purpose.getPurposeCode() == null || purpose.getPurposeCode().isBlank()) {
				errors.add(at + ".purposeCode is required");
				continue;
			}
			String code = purpose.getPurposeCode();
			if (!central.hasPurpose(code)) {
				errors.add(prefix + ".purposes: unknown purpose code " + code);
			}
			if (!codes.add(code)) {
				errors.add(prefix + ".purposes: " + code + " appears more than once");
			}
			if (purpose.getShown() == null) {
				errors.add(at + ".shown is required");
			}
			if (purpose.getIsMandatory() == null) {
				errors.add(at + ".isMandatory is required");
			}
			checkRank(at, prefix + ".purposes", purpose.getRank(), ranks, errors);
			boolean centrallyMandatory = central.isCentrallyMandatory(code);
			if (centrallyMandatory && Boolean.FALSE.equals(purpose.getIsMandatory())) {
				errors.add(prefix + ".purposes: " + code + " is mandatory in the central list and cannot be made optional");
			}
			// Proposal §3.1 rule 4: mandatory purposes are shown first, so they cannot be hidden.
			if ((centrallyMandatory || Boolean.TRUE.equals(purpose.getIsMandatory()))
					&& Boolean.FALSE.equals(purpose.getShown())) {
				errors.add(prefix + ".purposes: " + code + " is mandatory and cannot be hidden");
			}
		}
	}

	private void validateLanguages(String prefix, List<String> languages, CentralLists central,
			List<String> errors) {
		if (languages == null) {
			errors.add(prefix + ".languages is required");
			return;
		}
		Set<String> seen = new HashSet<>();
		for (String code : languages) {
			if (code == null || code.isBlank()) {
				errors.add(prefix + ".languages: a language code is empty");
				continue;
			}
			if (!central.hasLanguage(code)) {
				errors.add(prefix + ".languages: unknown language code " + code);
			}
			if (!seen.add(code)) {
				errors.add(prefix + ".languages: " + code + " appears more than once");
			}
		}
	}

	private void validateGuardian(String prefix, GuardianRule guardian, boolean consentEnabled, List<String> errors) {
		if (guardian == null || guardian.getEnabled() == null) {
			errors.add(prefix + ".guardian.enabled is required");
			return;
		}
		if (consentEnabled && !guardian.getEnabled()) {
			errors.add(prefix + ".guardian must be enabled while consent is enabled");
		}
		if (guardian.getAgeThreshold() == null) {
			guardian.setAgeThreshold(GUARDIAN_AGE);
		} else if (guardian.getAgeThreshold() != GUARDIAN_AGE) {
			errors.add(prefix + ".guardian.ageThreshold must be " + GUARDIAN_AGE);
		}
	}

	private void checkRank(String at, String list, Integer rank, Set<Integer> ranks, List<String> errors) {
		if (rank == null || rank < 1) {
			errors.add(at + ".rank must be 1 or more");
		} else if (!ranks.add(rank)) {
			errors.add(list + ": rank " + rank + " is used more than once");
		}
	}

	private static void throwIfAny(List<String> errors) throws IEMRException {
		if (!errors.isEmpty()) {
			throw new IEMRException(String.join("; ", errors));
		}
	}
}
