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

import static com.iemr.common.service.customization.ConsentConfigFixtures.central;
import static com.iemr.common.service.customization.ConsentConfigFixtures.mode;
import static com.iemr.common.service.customization.ConsentConfigFixtures.validContext;
import static com.iemr.common.service.customization.ConsentConfigFixtures.validRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.iemr.common.data.customization.consent.ConsentMode;
import com.iemr.common.data.customization.consent.ContextConfig;
import com.iemr.common.data.customization.consent.ModeEntry;
import com.iemr.common.data.customization.consent.ModeSettings;
import com.iemr.common.data.customization.consent.OtpTarget;
import com.iemr.common.data.customization.consent.ProjectConsentConfigRequest;

/** Feature 005 (FR-005, research R5): stored configurations lose retired modes and settings on load. */
class ConsentConfigCleanerTest {

	private static final Gson GSON = new Gson();

	/** The "stored" example of data-model.md. */
	private static final String LEGACY = "{\"enabled\":true,\"mandatory\":true,\"modes\":["
			+ "{\"mode\":\"VOICE_CLIP\",\"rank\":1,\"settings\":{\"voiceMinSeconds\":30}},"
			+ "{\"mode\":\"READ_ALOUD\",\"rank\":2},"
			+ "{\"mode\":\"SIGNATURE_PHOTO\",\"rank\":3,\"settings\":{\"photos\":\"BOTH\"}}],"
			+ "\"defaultMode\":\"VOICE_CLIP\",\"purposes\":[],\"languages\":[],"
			+ "\"guardian\":{\"enabled\":true,\"ageThreshold\":18}}";

	private final ConsentConfigCleaner cleaner = new ConsentConfigCleaner();

	private static ContextConfig parse(String json) {
		return GSON.fromJson(json, ContextConfig.class);
	}

	private static List<ConsentMode> modesOf(ContextConfig config) {
		return config.getModes().stream().map(ModeEntry::getMode).toList();
	}

	@Test
	@DisplayName("null stays null")
	void nullInput() {
		assertThat(cleaner.clean(null)).isNull();
	}

	@Test
	@DisplayName("a context without modes is returned unchanged")
	void noModes() {
		ContextConfig config = validContext();
		config.setModes(null);
		config.setDefaultMode(ConsentMode.VOICE_CLIP);

		assertThat(cleaner.clean(config)).isSameAs(config);
		assertThat(config.getDefaultMode()).isEqualTo(ConsentMode.VOICE_CLIP);
		assertThat(config.getPurposes()).hasSize(2);
	}

	@Test
	@DisplayName("the data-model example: VOICE_CLIP removed, ranks kept, photo setting dropped, default moved")
	void dataModelExample() {
		ContextConfig config = cleaner.clean(parse(LEGACY));

		assertThat(modesOf(config)).containsExactly(ConsentMode.READ_ALOUD, ConsentMode.SIGNATURE_PHOTO);
		assertThat(config.getModes()).extracting(ModeEntry::getRank).containsExactly(2, 3);
		assertThat(config.getModes()).extracting(ModeEntry::getSettings).containsOnlyNulls();
		assertThat(config.getDefaultMode()).isEqualTo(ConsentMode.READ_ALOUD);
		assertThat(GSON.toJson(config)).doesNotContain("VOICE_CLIP").doesNotContain("photos")
				.doesNotContain("voiceMinSeconds");
	}

	@Test
	@DisplayName("an unknown stored mode (null after parsing) and a null entry are removed")
	void unknownModeRemoved() {
		ContextConfig config = parse("{\"enabled\":true,\"modes\":[{\"mode\":\"SMOKE_SIGNAL\",\"rank\":1},"
				+ "{\"mode\":\"OTP\",\"rank\":2}],\"defaultMode\":\"OTP\"}");
		config.getModes().add(null);

		cleaner.clean(config);

		assertThat(modesOf(config)).containsExactly(ConsentMode.OTP);
		assertThat(config.getDefaultMode()).isEqualTo(ConsentMode.OTP);
	}

	@Test
	@DisplayName("settings are kept on OTP and WITNESSED and cleared on every other mode")
	void settingsKeptOnlyWhereTheyBelong() {
		ContextConfig config = validContext();
		ModeSettings otp = new ModeSettings();
		otp.setOtpTarget(OtpTarget.GUARDIAN);
		ModeSettings witness = new ModeSettings();
		witness.setWitnessRelationshipRequired(false);
		ModeSettings stray = new ModeSettings();
		stray.setOtpTarget(OtpTarget.PATIENT);
		config.setModes(new ArrayList<>(List.of(withSettings(ConsentMode.OTP, 1, otp),
				withSettings(ConsentMode.WITNESSED, 2, witness), withSettings(ConsentMode.SELF_READ, 3, stray),
				withSettings(ConsentMode.READ_ALOUD, 4, stray), withSettings(ConsentMode.SIGNATURE_PHOTO, 5, stray))));

		cleaner.clean(config);

		assertThat(config.getModes().get(0).getSettings().getOtpTarget()).isEqualTo(OtpTarget.GUARDIAN);
		assertThat(config.getModes().get(1).getSettings().getWitnessRelationshipRequired()).isFalse();
		assertThat(config.getModes().subList(2, 5)).extracting(ModeEntry::getSettings).containsOnlyNulls();
	}

	@Test
	@DisplayName("an enabled context whose only mode was VOICE_CLIP falls back to READ_ALOUD, rank 1")
	void onlyVoiceClipEnabled() {
		ContextConfig config = parse("{\"enabled\":true,\"modes\":[{\"mode\":\"VOICE_CLIP\",\"rank\":4}],"
				+ "\"defaultMode\":\"VOICE_CLIP\"}");

		cleaner.clean(config);

		assertThat(modesOf(config)).containsExactly(ConsentMode.READ_ALOUD);
		assertThat(config.getModes().get(0).getRank()).isEqualTo(1);
		assertThat(config.getModes().get(0).getSettings()).isNull();
		assertThat(config.getDefaultMode()).isEqualTo(ConsentMode.READ_ALOUD);
	}

	@Test
	@DisplayName("a disabled context whose only mode was VOICE_CLIP has no modes and no default")
	void onlyVoiceClipDisabled() {
		ContextConfig config = parse("{\"enabled\":false,\"modes\":[{\"mode\":\"VOICE_CLIP\",\"rank\":1}],"
				+ "\"defaultMode\":\"VOICE_CLIP\"}");

		cleaner.clean(config);

		assertThat(config.getModes()).isEmpty();
		assertThat(config.getDefaultMode()).isNull();
	}

	@Test
	@DisplayName("a default that is not among the remaining modes moves to the lowest rank")
	void defaultNotAllowedMoves() {
		ContextConfig config = validContext();
		config.setModes(new ArrayList<>(Arrays.asList(mode(ConsentMode.OTP, 5), mode(ConsentMode.SELF_READ, null),
				mode(ConsentMode.READ_ALOUD, 2))));
		config.setDefaultMode(ConsentMode.WITNESSED);

		cleaner.clean(config);

		assertThat(config.getDefaultMode()).isEqualTo(ConsentMode.READ_ALOUD);
	}

	@Test
	@DisplayName("a valid default and a missing default are left alone")
	void validOrMissingDefaultKept() {
		ContextConfig valid = validContext();
		valid.setDefaultMode(ConsentMode.OTP);
		assertThat(cleaner.clean(valid).getDefaultMode()).isEqualTo(ConsentMode.OTP);

		ContextConfig none = validContext();
		none.setDefaultMode(null);
		assertThat(cleaner.clean(none).getDefaultMode()).isNull();
	}

	@Test
	@DisplayName("purposes, languages, guardian, enabled and mandatory are untouched")
	void otherFieldsUntouched() {
		ContextConfig config = validContext();
		String before = GSON.toJson(config);

		cleaner.clean(config);

		assertThat(GSON.toJson(config)).isEqualTo(before);
	}

	@Test
	@DisplayName("FR-007: a cleaned configuration passes the save rules unchanged")
	void cleanedConfigurationSavesBack() {
		ContextConfig cleaned = cleaner.clean(parse(LEGACY));
		cleaned.setPurposes(validContext().getPurposes());
		cleaned.setLanguages(validContext().getLanguages());
		ProjectConsentConfigRequest request = validRequest();
		request.setRegistration(cleaned);

		assertThatCode(() -> new ConsentConfigValidator().validateAndNormalise(request, central()))
				.doesNotThrowAnyException();
	}

	private static ModeEntry withSettings(ConsentMode mode, int rank, ModeSettings settings) {
		ModeEntry entry = mode(mode, rank);
		entry.setSettings(settings);
		return entry;
	}
}
