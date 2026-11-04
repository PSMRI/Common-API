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
import static com.iemr.common.service.customization.ConsentConfigFixtures.guardian;
import static com.iemr.common.service.customization.ConsentConfigFixtures.mode;
import static com.iemr.common.service.customization.ConsentConfigFixtures.purpose;
import static com.iemr.common.service.customization.ConsentConfigFixtures.validContext;
import static com.iemr.common.service.customization.ConsentConfigFixtures.validRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.iemr.common.data.customization.consent.ConsentMode;
import com.iemr.common.data.customization.consent.ContextConfig;
import com.iemr.common.data.customization.consent.ModeEntry;
import com.iemr.common.data.customization.consent.ModeSettings;
import com.iemr.common.data.customization.consent.OtpTarget;
import com.iemr.common.data.customization.consent.ProjectConsentConfigRequest;
import com.iemr.common.utils.exception.IEMRException;
import com.iemr.common.utils.mapper.InputMapper;

class ConsentConfigValidatorTest {

	private final ConsentConfigValidator validator = new ConsentConfigValidator();

	private ProjectConsentConfigRequest withRegistration(ContextConfig config) {
		ProjectConsentConfigRequest request = validRequest();
		request.setRegistration(config);
		return request;
	}

	private String errorsFor(ProjectConsentConfigRequest request) {
		try {
			validator.validateAndNormalise(request, central());
		} catch (IEMRException e) {
			return e.getMessage();
		}
		return "";
	}

	private String errorsFor(ContextConfig config) {
		return errorsFor(withRegistration(config));
	}

	@Nested
	@DisplayName("identity fields (FR-021)")
	class Identity {

		@Test
		@DisplayName("a complete first save passes")
		void completeSavePasses() {
			assertThatCode(() -> validator.validateIdentity(validRequest(), false)).doesNotThrowAnyException();
		}

		@Test
		@DisplayName("missing project, name and provider are all reported together")
		void missingFieldsReported() {
			ProjectConsentConfigRequest request = new ProjectConsentConfigRequest();
			assertThatThrownBy(() -> validator.validateIdentity(request, false)).isInstanceOf(IEMRException.class)
					.hasMessage("projectId is required; projectName is required; serviceProviderId is required");
		}

		@Test
		@DisplayName("a blank project name is refused")
		void blankNameRefused() {
			ProjectConsentConfigRequest request = validRequest();
			request.setProjectName("  ");
			assertThatThrownBy(() -> validator.validateIdentity(request, false)).hasMessage("projectName is required");
		}

		@Test
		@DisplayName("a project name over 100 characters is refused")
		void longNameRefused() {
			ProjectConsentConfigRequest request = validRequest();
			request.setProjectName("x".repeat(101));
			assertThatThrownBy(() -> validator.validateIdentity(request, false))
					.hasMessage("projectName must be at most 100 characters");
		}

		@Test
		@DisplayName("an update needs a base version of 1 or more")
		void updateNeedsBaseVersion() {
			ProjectConsentConfigRequest request = validRequest();
			assertThatThrownBy(() -> validator.validateIdentity(request, true)).hasMessage("baseVersion must be 1 or more");
			request.setBaseVersion(0);
			assertThatThrownBy(() -> validator.validateIdentity(request, true)).hasMessage("baseVersion must be 1 or more");
			request.setBaseVersion(3);
			assertThatCode(() -> validator.validateIdentity(request, true)).doesNotThrowAnyException();
		}
	}

	@Nested
	@DisplayName("contexts (FR-010, FR-011)")
	class Contexts {

		@Test
		@DisplayName("a valid registration with revisit = same as registration passes")
		void validPasses() {
			assertThat(errorsFor(validRequest())).isEmpty();
		}

		@Test
		@DisplayName("registration is required")
		void registrationRequired() {
			assertThat(errorsFor(withRegistration(null))).isEqualTo("registration is required");
		}

		@Test
		@DisplayName("every context field is required")
		void everyFieldRequired() {
			String errors = errorsFor(new ContextConfig());
			assertThat(errors).contains("registration.enabled is required", "registration.mandatory is required",
					"registration.modes is required", "registration.purposes is required",
					"registration.languages is required", "registration.guardian.enabled is required");
		}

		@Test
		@DisplayName("revisit, when given, is checked with the same rules")
		void revisitChecked() {
			ProjectConsentConfigRequest request = validRequest();
			ContextConfig revisit = validContext();
			revisit.setLanguages(new ArrayList<>(List.of("fr")));
			request.setRevisit(revisit);
			assertThat(errorsFor(request)).isEqualTo("revisit.languages: unknown language code fr");
		}

		@Test
		@DisplayName("a disabled context with no modes, purposes or languages is accepted")
		void disabledEmptyAccepted() {
			ContextConfig config = validContext();
			config.setEnabled(false);
			config.setModes(new ArrayList<>());
			config.setDefaultMode(null);
			config.setPurposes(new ArrayList<>());
			config.setLanguages(new ArrayList<>());
			config.setGuardian(guardian(false, null));
			assertThat(errorsFor(config)).isEmpty();
		}
	}

	@Nested
	@DisplayName("modes (FR-012, FR-013)")
	class Modes {

		@Test
		@DisplayName("enabled consent needs at least one mode")
		void enabledNeedsMode() {
			ContextConfig config = validContext();
			config.setModes(new ArrayList<>());
			config.setDefaultMode(null);
			assertThat(errorsFor(config))
					.isEqualTo("registration: at least one mode must be allowed when consent is enabled");
		}

		@Test
		@DisplayName("a missing or invalid mode is reported")
		void invalidModeReported() {
			ContextConfig config = validContext();
			config.getModes().add(null);
			config.getModes().add(mode(null, 3));
			assertThat(errorsFor(config)).isEqualTo("registration.modes[2].mode is missing or not a valid mode; "
					+ "registration.modes[3].mode is missing or not a valid mode");
		}

		@Test
		@DisplayName("a mode allowed twice is refused")
		void duplicateModeRefused() {
			ContextConfig config = validContext();
			config.getModes().add(mode(ConsentMode.OTP, 3));
			assertThat(errorsFor(config)).isEqualTo("registration.modes: OTP appears more than once");
		}

		@Test
		@DisplayName("ranks must be 1 or more and unique; gaps are allowed")
		void ranksChecked() {
			ContextConfig config = validContext();
			config.setModes(new ArrayList<>(List.of(mode(ConsentMode.SELF_READ, 0), mode(ConsentMode.OTP, null),
					mode(ConsentMode.WITNESSED, 4), mode(ConsentMode.SIGNATURE_PHOTO, 4))));
			assertThat(errorsFor(config)).isEqualTo("registration.modes[0].rank must be 1 or more; "
					+ "registration.modes[1].rank must be 1 or more; registration.modes: rank 4 is used more than once");
		}

		@Test
		@DisplayName("the default mode must be one of the allowed modes")
		void defaultModeMustBeAllowed() {
			ContextConfig config = validContext();
			config.setDefaultMode(ConsentMode.WITNESSED);
			assertThat(errorsFor(config)).isEqualTo("registration.defaultMode must be one of the allowed modes");
		}

		@Test
		@DisplayName("no default mode is accepted")
		void noDefaultModeAccepted() {
			ContextConfig config = validContext();
			config.setDefaultMode(null);
			assertThat(errorsFor(config)).isEmpty();
		}

		@Test
		@DisplayName("feature 005: VOICE_CLIP as an allowed mode is refused, naming the field")
		void voiceClipModeRefused() {
			ContextConfig config = validContext();
			config.getModes().add(mode(ConsentMode.VOICE_CLIP, 3));
			assertThat(errorsFor(config)).isEqualTo("registration.modes[2].mode VOICE_CLIP is no longer allowed");
		}

		@Test
		@DisplayName("feature 005: VOICE_CLIP in the revisit modes is refused with the revisit prefix")
		void voiceClipRevisitRefused() {
			ProjectConsentConfigRequest request = validRequest();
			ContextConfig revisit = validContext();
			revisit.getModes().add(mode(ConsentMode.VOICE_CLIP, 3));
			request.setRevisit(revisit);
			assertThat(errorsFor(request)).isEqualTo("revisit.modes[2].mode VOICE_CLIP is no longer allowed");
		}

		@Test
		@DisplayName("feature 005: a VOICE_CLIP default mode is refused instead of 'must be one of the allowed modes'")
		void voiceClipDefaultRefused() {
			ContextConfig config = validContext();
			config.setDefaultMode(ConsentMode.VOICE_CLIP);
			assertThat(errorsFor(config)).isEqualTo("registration.defaultMode VOICE_CLIP is no longer allowed");
		}

		@Test
		@DisplayName("feature 005: VOICE_CLIP as mode and default gives both messages, the mode first")
		void voiceClipModeAndDefaultRefused() {
			ContextConfig config = validContext();
			config.getModes().add(mode(ConsentMode.VOICE_CLIP, 3));
			config.setDefaultMode(ConsentMode.VOICE_CLIP);
			assertThat(errorsFor(config)).isEqualTo("registration.modes[2].mode VOICE_CLIP is no longer allowed; "
					+ "registration.defaultMode VOICE_CLIP is no longer allowed");
		}

		@Test
		@DisplayName("feature 005: VOICE_CLIP as the only mode of an enabled context gives all three messages in order")
		void voiceClipOnlyModeRefused() {
			ContextConfig config = validContext();
			config.setModes(new ArrayList<>(List.of(mode(ConsentMode.VOICE_CLIP, 1))));
			config.setDefaultMode(ConsentMode.VOICE_CLIP);
			assertThat(errorsFor(config)).isEqualTo("registration.modes[0].mode VOICE_CLIP is no longer allowed; "
					+ "registration: at least one mode must be allowed when consent is enabled; "
					+ "registration.defaultMode VOICE_CLIP is no longer allowed");
		}
	}

	@Nested
	@DisplayName("mode settings (FR-017, FR-020)")
	class Settings {

		private ModeSettings settingsAfterValidation(ConsentMode mode, ModeSettings given) throws IEMRException {
			ContextConfig config = validContext();
			ModeEntry entry = mode(mode, 3);
			entry.setSettings(given);
			config.getModes().add(entry);
			validator.validateAndNormalise(withRegistration(config), central());
			return entry.getSettings();
		}

		@Test
		@DisplayName("missing settings get their defaults; modes without settings get none")
		void defaultsApplied() throws IEMRException {
			assertThat(settingsAfterValidation(ConsentMode.WITNESSED, null).getWitnessRelationshipRequired()).isTrue();
			assertThat(settingsAfterValidation(ConsentMode.SIGNATURE_PHOTO, null)).isNull();
			assertThat(settingsAfterValidation(ConsentMode.READ_ALOUD, null)).isNull();
			ContextConfig config = validContext();
			validator.validateAndNormalise(withRegistration(config), central());
			assertThat(config.getModes().get(1).getSettings().getOtpTarget()).isEqualTo(OtpTarget.PATIENT);
			assertThat(config.getModes().get(0).getSettings()).isNull();
		}

		@Test
		@DisplayName("given settings are kept and keys of other modes are dropped")
		void givenSettingsKept() throws IEMRException {
			ModeSettings given = new ModeSettings();
			given.setOtpTarget(OtpTarget.GUARDIAN);
			given.setWitnessRelationshipRequired(false);
			ModeSettings witnessed = settingsAfterValidation(ConsentMode.WITNESSED, given);
			assertThat(witnessed.getWitnessRelationshipRequired()).isFalse();
			assertThat(witnessed.getOtpTarget()).isNull();
			given.setWitnessRelationshipRequired(true);
			assertThat(settingsAfterValidation(ConsentMode.WITNESSED, given).getWitnessRelationshipRequired()).isTrue();
			assertThat(settingsAfterValidation(ConsentMode.SIGNATURE_PHOTO, given)).isNull();
			assertThat(settingsAfterValidation(ConsentMode.READ_ALOUD, given)).isNull();

			ContextConfig config = validContext();
			config.getModes().get(1).setSettings(given);
			validator.validateAndNormalise(withRegistration(config), central());
			assertThat(config.getModes().get(1).getSettings().getOtpTarget()).isEqualTo(OtpTarget.GUARDIAN);
			assertThat(config.getModes().get(1).getSettings().getWitnessRelationshipRequired()).isNull();
		}

		@Test
		@DisplayName("feature 005: photos and voiceMinSeconds sent in JSON are dropped, and the save is accepted")
		void retiredSettingsDropped() throws IEMRException {
			ModeEntry entry = InputMapper.gson().fromJson(
					"{\"mode\":\"SIGNATURE_PHOTO\",\"rank\":3,\"settings\":{\"photos\":\"BOTH\",\"voiceMinSeconds\":45}}",
					ModeEntry.class);
			ContextConfig config = validContext();
			config.getModes().add(entry);

			validator.validateAndNormalise(withRegistration(config), central());

			assertThat(entry.getSettings()).isNull();
			String stored = new Gson().toJson(config);
			assertThat(stored).doesNotContain("photos").doesNotContain("voiceMinSeconds");
		}
	}

	@Nested
	@DisplayName("purposes (FR-014, FR-015)")
	class Purposes {

		@Test
		@DisplayName("an empty purpose list (central list as it is) is accepted")
		void emptyAccepted() {
			ContextConfig config = validContext();
			config.setPurposes(new ArrayList<>());
			assertThat(errorsFor(config)).isEmpty();
		}

		@Test
		@DisplayName("a missing purpose code is reported")
		void missingCodeReported() {
			ContextConfig config = validContext();
			config.getPurposes().add(null);
			config.getPurposes().add(purpose(" ", true, 4, false));
			config.getPurposes().add(purpose(null, true, 5, false));
			assertThat(errorsFor(config)).isEqualTo("registration.purposes[2].purposeCode is required; "
					+ "registration.purposes[3].purposeCode is required; registration.purposes[4].purposeCode is required");
		}

		@Test
		@DisplayName("unknown and repeated purpose codes are refused")
		void unknownAndDuplicateRefused() {
			ContextConfig config = validContext();
			config.getPurposes().add(purpose("P-XYZ", true, 3, false));
			config.getPurposes().add(purpose("P-SMS", true, 4, false));
			assertThat(errorsFor(config)).isEqualTo("registration.purposes: unknown purpose code P-XYZ; "
					+ "registration.purposes: P-SMS appears more than once");
		}

		@Test
		@DisplayName("shown, isMandatory and rank are required, and ranks are unique")
		void fieldsRequired() {
			ContextConfig config = validContext();
			config.setPurposes(new ArrayList<>(Arrays.asList(purpose("P-SMS", null, null, null),
					purpose("P-REG", true, 2, true))));
			config.getPurposes().get(0).setRank(2);
			assertThat(errorsFor(config)).isEqualTo("registration.purposes[0].shown is required; "
					+ "registration.purposes[0].isMandatory is required; registration.purposes: rank 2 is used more than once");
		}

		@Test
		@DisplayName("a centrally mandatory purpose cannot be made optional")
		void centralMandatoryKept() {
			ContextConfig config = validContext();
			config.getPurposes().get(0).setIsMandatory(false);
			assertThat(errorsFor(config)).isEqualTo(
					"registration.purposes: P-REG is mandatory in the central list and cannot be made optional");
		}

		@Test
		@DisplayName("an optional purpose can be made mandatory")
		void optionalCanBecomeMandatory() {
			ContextConfig config = validContext();
			config.getPurposes().get(1).setIsMandatory(true);
			assertThat(errorsFor(config)).isEmpty();
		}

		@Test
		@DisplayName("a mandatory purpose cannot be hidden, whether mandatory centrally or by the project")
		void mandatoryCannotBeHidden() {
			ContextConfig config = validContext();
			config.getPurposes().get(0).setShown(false);
			config.getPurposes().get(1).setIsMandatory(true);
			config.getPurposes().get(1).setShown(false);
			assertThat(errorsFor(config)).isEqualTo("registration.purposes: P-REG is mandatory and cannot be hidden; "
					+ "registration.purposes: P-SMS is mandatory and cannot be hidden");
		}

		@Test
		@DisplayName("an optional purpose can be hidden")
		void optionalCanBeHidden() {
			ContextConfig config = validContext();
			config.getPurposes().get(1).setShown(false);
			assertThat(errorsFor(config)).isEmpty();
		}
	}

	@Nested
	@DisplayName("languages (FR-016)")
	class Languages {

		@Test
		@DisplayName("an empty language list (every published language) is accepted")
		void emptyAccepted() {
			ContextConfig config = validContext();
			config.setLanguages(new ArrayList<>());
			assertThat(errorsFor(config)).isEmpty();
		}

		@Test
		@DisplayName("empty, unknown and repeated language codes are refused")
		void badCodesRefused() {
			ContextConfig config = validContext();
			config.setLanguages(new ArrayList<>(Arrays.asList("en", null, "", "xx", "en")));
			assertThat(errorsFor(config)).isEqualTo("registration.languages: a language code is empty; "
					+ "registration.languages: a language code is empty; registration.languages: unknown language code xx; "
					+ "registration.languages: en appears more than once");
		}
	}

	@Nested
	@DisplayName("guardian rule (FR-018, FR-019)")
	class Guardian {

		@Test
		@DisplayName("the guardian step cannot be switched off while consent is enabled")
		void guardianRequiredWhenEnabled() {
			ContextConfig config = validContext();
			config.setGuardian(guardian(false, 18));
			assertThat(errorsFor(config)).isEqualTo("registration.guardian must be enabled while consent is enabled");
		}

		@Test
		@DisplayName("a guardian rule without enabled is refused")
		void guardianEnabledRequired() {
			ContextConfig config = validContext();
			config.setGuardian(guardian(null, 18));
			assertThat(errorsFor(config)).isEqualTo("registration.guardian.enabled is required");
		}

		@Test
		@DisplayName("a missing age threshold becomes 18")
		void missingAgeDefaults() throws IEMRException {
			ContextConfig config = validContext();
			config.setGuardian(guardian(true, null));
			validator.validateAndNormalise(withRegistration(config), central());
			assertThat(config.getGuardian().getAgeThreshold()).isEqualTo(18);
		}

		@ParameterizedTest(name = "age {0} is refused")
		@ValueSource(ints = { 16, 17, 19, 21 })
		@DisplayName("an age threshold other than 18 is refused (proposal §3.1 rule 7)")
		void otherAgesRefused(int age) {
			ContextConfig config = validContext();
			config.setGuardian(guardian(true, age));
			assertThat(errorsFor(config)).isEqualTo("registration.guardian.ageThreshold must be 18");
		}
	}
}
