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

import com.iemr.common.data.customization.consent.ConsentConfigMasters.LanguageMaster;
import com.iemr.common.data.customization.consent.ConsentConfigMasters.PurposeMaster;
import com.iemr.common.data.customization.consent.ConsentMode;
import com.iemr.common.data.customization.consent.ContextConfig;
import com.iemr.common.data.customization.consent.GuardianRule;
import com.iemr.common.data.customization.consent.ModeEntry;
import com.iemr.common.data.customization.consent.ProjectConsentConfigRequest;
import com.iemr.common.data.customization.consent.PurposeEntry;
import com.iemr.common.service.customization.ConsentMastersClient.CentralLists;

/** Synthetic consent configuration data shared by the consent configuration tests. */
final class ConsentConfigFixtures {

	static final int PROJECT_ID = 12;
	static final int PROVIDER_ID = 1;
	static final int USER_ID = 501;
	static final String USER_NAME = "padmin.test";

	private ConsentConfigFixtures() {
	}

	/** P-REG is centrally mandatory, P-SMS optional; notices are published in en and hi. */
	static CentralLists central() {
		List<PurposeMaster> purposes = new ArrayList<>();
		purposes.add(new PurposeMaster("P-REG", "Registration", true));
		purposes.add(new PurposeMaster("P-SMS", "SMS reminders", false));
		List<LanguageMaster> languages = new ArrayList<>();
		languages.add(new LanguageMaster("en", "English", true));
		languages.add(new LanguageMaster("hi", "Hindi", true));
		return new CentralLists(purposes, languages);
	}

	static ModeEntry mode(ConsentMode mode, Integer rank) {
		ModeEntry entry = new ModeEntry();
		entry.setMode(mode);
		entry.setRank(rank);
		return entry;
	}

	static PurposeEntry purpose(String code, Boolean shown, Integer rank, Boolean mandatory) {
		PurposeEntry entry = new PurposeEntry();
		entry.setPurposeCode(code);
		entry.setShown(shown);
		entry.setRank(rank);
		entry.setIsMandatory(mandatory);
		return entry;
	}

	static GuardianRule guardian(Boolean enabled, Integer age) {
		GuardianRule rule = new GuardianRule();
		rule.setEnabled(enabled);
		rule.setAgeThreshold(age);
		return rule;
	}

	/** Enabled, mandatory, SELF_READ + OTP, both purposes shown, both languages, guardian at 18. */
	static ContextConfig validContext() {
		ContextConfig config = new ContextConfig();
		config.setEnabled(true);
		config.setMandatory(true);
		config.setModes(new ArrayList<>(List.of(mode(ConsentMode.SELF_READ, 1), mode(ConsentMode.OTP, 2))));
		config.setDefaultMode(ConsentMode.SELF_READ);
		config.setPurposes(new ArrayList<>(List.of(purpose("P-REG", true, 1, true), purpose("P-SMS", true, 2, false))));
		config.setLanguages(new ArrayList<>(List.of("en", "hi")));
		config.setGuardian(guardian(true, 18));
		return config;
	}

	static ProjectConsentConfigRequest validRequest() {
		ProjectConsentConfigRequest request = new ProjectConsentConfigRequest();
		request.setProjectId(PROJECT_ID);
		request.setProjectName("MMU Project");
		request.setServiceProviderId(PROVIDER_ID);
		request.setRegistration(validContext());
		request.setRevisit(null);
		return request;
	}
}
