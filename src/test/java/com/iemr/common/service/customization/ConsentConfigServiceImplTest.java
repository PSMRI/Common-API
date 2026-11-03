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

import static com.iemr.common.service.customization.ConsentConfigFixtures.PROJECT_ID;
import static com.iemr.common.service.customization.ConsentConfigFixtures.PROVIDER_ID;
import static com.iemr.common.service.customization.ConsentConfigFixtures.USER_ID;
import static com.iemr.common.service.customization.ConsentConfigFixtures.USER_NAME;
import static com.iemr.common.service.customization.ConsentConfigFixtures.central;
import static com.iemr.common.service.customization.ConsentConfigFixtures.validContext;
import static com.iemr.common.service.customization.ConsentConfigFixtures.validRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyShort;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.iemr.common.data.customization.ProjectConsentConfigEntity;
import com.iemr.common.data.customization.ProjectConsentConfigHistory;
import com.iemr.common.data.customization.ProjectCustomization;
import com.iemr.common.data.customization.ServicelineCustomization;
import com.iemr.common.data.customization.consent.ConsentMode;
import com.iemr.common.data.customization.consent.ContextConfig;
import com.iemr.common.data.customization.consent.ProjectConsentConfigRequest;
import com.iemr.common.repo.customization.ProjectConsentConfigHistoryRepo;
import com.iemr.common.repo.customization.ProjectConsentConfigRepo;
import com.iemr.common.repo.customization.ProjectCustomizationRepo;
import com.iemr.common.repo.customization.ServicelineCustomizationRepo;
import com.iemr.common.repository.users.UserRoleMappingRepository;
import com.iemr.common.utils.InputSanitizer;
import com.iemr.common.utils.exception.ConsentConfigConflictException;
import com.iemr.common.utils.exception.ConsentServiceUnavailableException;
import com.iemr.common.utils.exception.IEMRException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ConsentConfigServiceImplTest {

	private static final Instant NOW = Instant.parse("2026-11-02T10:00:00Z");
	private static final String AUTH = "Bearer token";
	private static final String KEY = "9b2f";
	private static final Gson GSON = new Gson();

	@Mock
	private ProjectConsentConfigRepo configRepo;
	@Mock
	private ProjectConsentConfigHistoryRepo historyRepo;
	@Mock
	private ProjectCustomizationRepo projectCustomizationRepo;
	@Mock
	private ServicelineCustomizationRepo servicelineCustomizationRepo;
	@Mock
	private UserRoleMappingRepository userRoleMappingRepository;
	@Mock
	private ConsentMastersClient mastersClient;
	@Mock
	private ConsentConfigIdempotency idempotency;

	private ConsentConfigServiceImpl service;

	@BeforeEach
	void setUp() throws IEMRException {
		service = new ConsentConfigServiceImpl(configRepo, historyRepo, projectCustomizationRepo,
				servicelineCustomizationRepo, userRoleMappingRepository, mastersClient, new ConsentConfigValidator(),
				idempotency, Clock.fixed(NOW, ZoneOffset.UTC));
		when(userRoleMappingRepository.countActiveMappingsForProvider((long) USER_ID, (short) PROVIDER_ID))
				.thenReturn(2L);
		when(projectCustomizationRepo.findById(PROJECT_ID)).thenReturn(Optional.of(project(PROVIDER_ID, false)));
		when(mastersClient.load(AUTH)).thenReturn(central());
		when(idempotency.begin(any(), anyString(), any())).thenReturn(Optional.empty());
	}

	@AfterEach
	void clearSynchronization() {
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.clearSynchronization();
		}
	}

	private static ProjectCustomization project(int providerId, boolean deleted) {
		ProjectCustomization project = new ProjectCustomization();
		project.setProjectId(PROJECT_ID);
		project.setProjectName("MMU Project");
		project.setServiceProviderId(providerId);
		project.setDeleted(deleted);
		return project;
	}

	private static ProjectConsentConfigEntity saved(int version, ContextConfig revisit) {
		ProjectConsentConfigEntity entity = new ProjectConsentConfigEntity();
		entity.setProjectId(PROJECT_ID);
		entity.setProjectName("MMU Project");
		entity.setServiceProviderId(PROVIDER_ID);
		entity.setVersion(version);
		entity.setRegistrationConfig(GSON.toJson(validContext()));
		entity.setRevisitConfig(revisit == null ? null : GSON.toJson(revisit));
		entity.setDeleted(false);
		entity.setCreatedBy("first.admin");
		entity.setLastModDate(Timestamp.from(NOW));
		return entity;
	}

	private static JsonObject json(String body) {
		return JsonParser.parseString(body).getAsJsonObject();
	}

	@Nested
	@DisplayName("fetchConsentConfig — API 1 (US1)")
	class Load {

		private ServicelineCustomization mapping;

		@BeforeEach
		void mapped() {
			mapping = new ServicelineCustomization();
			mapping.setProjectId(PROJECT_ID);
			when(servicelineCustomizationRepo.checkExistingData(2, "MMU", 5, 41, 312, PROVIDER_ID)).thenReturn(mapping);
		}

		private String load(String context) throws Exception {
			return service.fetchConsentConfig("{\"serviceLine\":\"MMU\",\"serviceLineId\":2,\"stateId\":5,"
					+ "\"districtId\":41,\"blockId\":312,\"serviceProviderId\":1,\"context\":\"" + context + "\"}",
					USER_ID);
		}

		@Test
		@DisplayName("returns the registration settings of the mapped project")
		void registration() throws Exception {
			when(configRepo.findByProjectIdAndDeletedFalse(PROJECT_ID)).thenReturn(Optional.of(saved(3, null)));

			JsonObject result = json(load("REGISTRATION"));

			assertThat(result.get("projectId").getAsInt()).isEqualTo(PROJECT_ID);
			assertThat(result.get("version").getAsInt()).isEqualTo(3);
			assertThat(result.get("context").getAsString()).isEqualTo("REGISTRATION");
			assertThat(result.getAsJsonObject("config").get("enabled").getAsBoolean()).isTrue();
			verifyNoInteractions(mastersClient);
		}

		@Test
		@DisplayName("REVISIT with revisit = same as registration returns the registration settings")
		void revisitSameAsRegistration() throws Exception {
			when(configRepo.findByProjectIdAndDeletedFalse(PROJECT_ID)).thenReturn(Optional.of(saved(3, null)));

			JsonObject result = json(load("REVISIT"));

			assertThat(result.get("context").getAsString()).isEqualTo("REVISIT");
			assertThat(result.getAsJsonObject("config").getAsJsonArray("modes")).hasSize(2);
		}

		@Test
		@DisplayName("REVISIT with its own rules returns the revisit settings")
		void revisitOwnRules() throws Exception {
			ContextConfig revisit = validContext();
			revisit.setMandatory(false);
			when(configRepo.findByProjectIdAndDeletedFalse(PROJECT_ID)).thenReturn(Optional.of(saved(3, revisit)));

			assertThat(json(load("REVISIT")).getAsJsonObject("config").get("mandatory").getAsBoolean()).isFalse();
			assertThat(json(load("REGISTRATION")).getAsJsonObject("config").get("mandatory").getAsBoolean()).isTrue();
		}

		@Test
		@DisplayName("no mapped project, no saved configuration or an ambiguous mapping return no data")
		void nothingToReturn() throws Exception {
			when(configRepo.findByProjectIdAndDeletedFalse(PROJECT_ID)).thenReturn(Optional.empty());
			assertThat(load("REGISTRATION")).isNull();

			when(servicelineCustomizationRepo.checkExistingData(2, "MMU", 5, 41, 312, PROVIDER_ID)).thenReturn(null);
			assertThat(load("REGISTRATION")).isNull();

			when(servicelineCustomizationRepo.checkExistingData(2, "MMU", 5, 41, 312, PROVIDER_ID))
					.thenThrow(new IncorrectResultSizeDataAccessException(1, 2));
			assertThat(load("REGISTRATION")).isNull();
		}

		@Test
		@DisplayName("a missing or unknown context is refused")
		void badContextRefused() {
			assertThatThrownBy(() -> load("OTHER")).isInstanceOf(IEMRException.class)
					.hasMessage("context must be REGISTRATION or REVISIT");
			assertThatThrownBy(() -> service.fetchConsentConfig("{}", USER_ID))
					.hasMessage("context must be REGISTRATION or REVISIT");
		}

		@Test
		@DisplayName("a caller outside the requested service provider gets project not found")
		void notMemberRefused() {
			when(userRoleMappingRepository.countActiveMappingsForProvider((long) USER_ID, (short) PROVIDER_ID))
					.thenReturn(0L);

			assertThatThrownBy(() -> load("REGISTRATION")).hasMessage("project not found");
			verifyNoInteractions(servicelineCustomizationRepo);
		}

		@Test
		@DisplayName("without a service provider the membership check is skipped")
		void noProviderNoMembershipCheck() throws Exception {
			assertThat(service.fetchConsentConfig("{\"context\":\"REGISTRATION\"}", USER_ID)).isNull();
			verifyNoInteractions(userRoleMappingRepository);
		}

		@Test
		@DisplayName("an empty or malformed body is refused")
		void badBodyRefused() {
			assertThatThrownBy(() -> service.fetchConsentConfig("", USER_ID)).hasMessage("Request body is required");
			assertThatThrownBy(() -> service.fetchConsentConfig("{not json", USER_ID))
					.hasMessage("Invalid request body");
		}
	}

	@Nested
	@DisplayName("admin reads — A1 to A3 (US2)")
	class AdminReads {

		@Test
		@DisplayName("masters list the six modes, central purposes, languages and configured projects")
		void masters() throws Exception {
			when(configRepo.findByServiceProviderIdAndDeletedFalse(PROVIDER_ID)).thenReturn(List.of(saved(3, null)));

			JsonObject masters = json(service.getMasters(PROVIDER_ID, USER_ID, AUTH));

			assertThat(masters.getAsJsonArray("modes")).hasSize(6);
			assertThat(masters.getAsJsonArray("modes").get(0).getAsJsonObject().get("mode").getAsString())
					.isEqualTo("SELF_READ");
			assertThat(masters.getAsJsonArray("modes").get(0).getAsJsonObject().get("name").getAsString())
					.isEqualTo("Self-read");
			assertThat(masters.getAsJsonArray("purposes")).hasSize(2);
			assertThat(masters.getAsJsonArray("languages")).hasSize(2);
			assertThat(masters.getAsJsonArray("projects").get(0).getAsJsonObject().get("version").getAsInt())
					.isEqualTo(3);
		}

		@Test
		@DisplayName("masters need a service provider the caller belongs to")
		void mastersAccess() {
			assertThatThrownBy(() -> service.getMasters(null, USER_ID, AUTH))
					.hasMessage("serviceProviderId is required");
			assertThatThrownBy(() -> service.getMasters(2, USER_ID, AUTH)).hasMessage("project not found");
			assertThatThrownBy(() -> service.getMasters(PROVIDER_ID, null, AUTH)).hasMessage("project not found");
			assertThatThrownBy(() -> service.getMasters(40000, USER_ID, AUTH)).hasMessage("project not found");
			assertThatThrownBy(() -> service.getMasters(-40000, USER_ID, AUTH)).hasMessage("project not found");
			when(userRoleMappingRepository.countActiveMappingsForProvider((long) USER_ID, (short) PROVIDER_ID))
					.thenReturn(null);
			assertThatThrownBy(() -> service.getMasters(PROVIDER_ID, USER_ID, AUTH)).hasMessage("project not found");
			verifyNoInteractions(mastersClient);
		}

		@Test
		@DisplayName("masters report Consent-API being unavailable")
		void mastersUnavailable() {
			when(mastersClient.load(AUTH)).thenThrow(new ConsentServiceUnavailableException());

			assertThatThrownBy(() -> service.getMasters(PROVIDER_ID, USER_ID, AUTH))
					.isInstanceOf(IEMRException.class).hasMessage("Consent service unavailable, try again");
		}

		@Test
		@DisplayName("fetchAllConfigs returns one entry per configured project with saved-by and saved-at")
		void fetchAll() throws Exception {
			ProjectConsentConfigEntity updated = saved(4, validContext());
			updated.setModifiedBy("second.admin");
			ProjectConsentConfigEntity noDate = saved(1, null);
			noDate.setLastModDate(null);
			when(configRepo.findByServiceProviderIdAndDeletedFalse(PROVIDER_ID)).thenReturn(List.of(updated, noDate));

			var list = JsonParser.parseString(service.fetchAllConfigs("{\"serviceProviderId\":1}", USER_ID))
					.getAsJsonArray();

			assertThat(list).hasSize(2);
			assertThat(list.get(0).getAsJsonObject().get("savedBy").getAsString()).isEqualTo("second.admin");
			assertThat(list.get(0).getAsJsonObject().get("savedAt").getAsString()).isEqualTo("2026-11-02T10:00:00Z");
			assertThat(list.get(0).getAsJsonObject().has("revisit")).isTrue();
			assertThat(list.get(1).getAsJsonObject().get("savedBy").getAsString()).isEqualTo("first.admin");
			assertThat(list.get(1).getAsJsonObject().has("savedAt")).isFalse();
			assertThat(list.get(1).getAsJsonObject().has("revisit")).isFalse();
		}

		@Test
		@DisplayName("fetchAllConfigs with nothing configured returns an empty list, and needs a provider")
		void fetchAllEmptyAndAccess() throws Exception {
			when(configRepo.findByServiceProviderIdAndDeletedFalse(PROVIDER_ID)).thenReturn(List.of());
			assertThat(service.fetchAllConfigs("{\"serviceProviderId\":1}", USER_ID)).isEqualTo("[]");
			assertThatThrownBy(() -> service.fetchAllConfigs("{}", USER_ID)).hasMessage("serviceProviderId is required");
			assertThatThrownBy(() -> service.fetchAllConfigs("{\"serviceProviderId\":2}", USER_ID))
					.hasMessage("project not found");
		}

		@Test
		@DisplayName("fetchConfigByProject returns the saved configuration, or version 0 when never saved")
		void fetchByProject() throws Exception {
			String body = "{\"projectId\":12,\"serviceProviderId\":1}";
			when(configRepo.findByProjectIdAndDeletedFalse(PROJECT_ID)).thenReturn(Optional.of(saved(3, null)));
			assertThat(json(service.fetchConfigByProject(body, USER_ID)).get("version").getAsInt()).isEqualTo(3);

			when(configRepo.findByProjectIdAndDeletedFalse(PROJECT_ID)).thenReturn(Optional.empty());
			JsonObject never = json(service.fetchConfigByProject(body, USER_ID));
			assertThat(never.get("version").getAsInt()).isZero();
			assertThat(never.get("projectName").getAsString()).isEqualTo("MMU Project");
			assertThat(never.has("savedBy")).isFalse();
			assertThat(never.has("savedAt")).isFalse();
			assertThat(never.has("registration")).isFalse();
		}

		@Test
		@DisplayName("fetchConfigByProject refuses unknown, deleted and other providers' projects")
		void fetchByProjectAccess() {
			String body = "{\"projectId\":12,\"serviceProviderId\":1}";
			assertThatThrownBy(() -> service.fetchConfigByProject("{\"projectId\":12}", USER_ID))
					.hasMessage("projectId and serviceProviderId are required");
			assertThatThrownBy(() -> service.fetchConfigByProject("{\"serviceProviderId\":1}", USER_ID))
					.hasMessage("projectId and serviceProviderId are required");
			when(projectCustomizationRepo.findById(PROJECT_ID)).thenReturn(Optional.of(project(2, false)));
			assertThatThrownBy(() -> service.fetchConfigByProject(body, USER_ID)).hasMessage("project not found");
			when(projectCustomizationRepo.findById(PROJECT_ID)).thenReturn(Optional.of(project(PROVIDER_ID, true)));
			assertThatThrownBy(() -> service.fetchConfigByProject(body, USER_ID)).hasMessage("project not found");
			when(projectCustomizationRepo.findById(PROJECT_ID)).thenReturn(Optional.empty());
			assertThatThrownBy(() -> service.fetchConfigByProject(body, USER_ID)).hasMessage("project not found");
		}
	}

	@Nested
	@DisplayName("saveConfig — A4 (US3)")
	class Save {

		private String request() {
			ProjectConsentConfigRequest request = validRequest();
			request.setCreatedBy("someone.else");
			return GSON.toJson(request);
		}

		private String save(String body) throws Exception {
			return service.saveConfig(body, USER_ID, USER_NAME, KEY, AUTH);
		}

		@Test
		@DisplayName("a never-saved project is stored as version 1 by the session user, with a CREATE history row")
		void firstSave() throws Exception {
			when(configRepo.findByProjectIdAndDeletedFalse(PROJECT_ID)).thenReturn(Optional.empty());

			String body = save(request());
			JsonObject result = json(body);

			ArgumentCaptor<ProjectConsentConfigEntity> entity = ArgumentCaptor.forClass(ProjectConsentConfigEntity.class);
			verify(configRepo).saveAndFlush(entity.capture());
			assertThat(entity.getValue().getVersion()).isEqualTo(1);
			assertThat(entity.getValue().getCreatedBy()).isEqualTo(USER_NAME);
			assertThat(entity.getValue().getDeleted()).isFalse();
			assertThat(entity.getValue().getRevisitConfig()).isNull();
			assertThat(entity.getValue().getLastModDate()).isEqualTo(Timestamp.from(NOW));
			ArgumentCaptor<ProjectConsentConfigHistory> history = ArgumentCaptor.forClass(ProjectConsentConfigHistory.class);
			verify(historyRepo).save(history.capture());
			assertThat(history.getValue().getAction()).isEqualTo("CREATE");
			assertThat(history.getValue().getBaseVersion()).isZero();
			assertThat(history.getValue().getVersion()).isEqualTo(1);
			assertThat(history.getValue().getCreatedBy()).isEqualTo(USER_NAME);
			assertThat(history.getValue().getCreatedDate()).isEqualTo(Timestamp.from(NOW));
			assertThat(result.get("version").getAsInt()).isEqualTo(1);
			assertThat(result.get("savedBy").getAsString()).isEqualTo(USER_NAME);
			assertThat(result.get("savedAt").getAsString()).isEqualTo("2026-11-02T10:00:00Z");
			assertThat(result.getAsJsonObject("registration").getAsJsonObject("guardian").get("ageThreshold").getAsInt())
					.isEqualTo(18);
			verify(idempotency).complete(USER_ID, "saveConfig", KEY, body);
		}

		@Test
		@DisplayName("separate revisit rules are stored")
		void revisitStored() throws Exception {
			ProjectConsentConfigRequest request = validRequest();
			request.setRevisit(validContext());
			when(configRepo.findByProjectIdAndDeletedFalse(PROJECT_ID)).thenReturn(Optional.empty());

			assertThat(json(save(GSON.toJson(request))).has("revisit")).isTrue();
			ArgumentCaptor<ProjectConsentConfigEntity> entity = ArgumentCaptor.forClass(ProjectConsentConfigEntity.class);
			verify(configRepo).saveAndFlush(entity.capture());
			assertThat(entity.getValue().getRevisitConfig()).isNotNull();
		}

		@Test
		@DisplayName("the project name is sanitised before it is stored")
		void nameSanitised() throws Exception {
			ProjectConsentConfigRequest request = validRequest();
			request.setProjectName("<script>x</script>MMU");
			when(configRepo.findByProjectIdAndDeletedFalse(PROJECT_ID)).thenReturn(Optional.empty());

			save(GSON.toJson(request));

			ArgumentCaptor<ProjectConsentConfigEntity> entity = ArgumentCaptor.forClass(ProjectConsentConfigEntity.class);
			verify(configRepo).saveAndFlush(entity.capture());
			assertThat(entity.getValue().getProjectName()).isEqualTo(InputSanitizer.sanitize("<script>x</script>MMU"));
		}

		@Test
		@DisplayName("C20 – saveConfig on a configured project is rejected as STALE_VERSION")
		void alreadyConfigured() {
			when(configRepo.findByProjectIdAndDeletedFalse(PROJECT_ID)).thenReturn(Optional.of(saved(1, null)));

			assertThatThrownBy(() -> save(request())).isInstanceOf(ConsentConfigConflictException.class);
			verify(configRepo, never()).saveAndFlush(any());
			verify(idempotency).release(USER_ID, "saveConfig", KEY);
		}

		@Test
		@DisplayName("C20 – a first save that loses the race on the unique project key is rejected as STALE_VERSION")
		void lostRace() {
			when(configRepo.findByProjectIdAndDeletedFalse(PROJECT_ID)).thenReturn(Optional.empty());
			when(configRepo.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("UQ_ProjectConsentConfig_ProjectID"));

			assertThatThrownBy(() -> save(request())).isInstanceOf(ConsentConfigConflictException.class);
			verify(historyRepo, never()).save(any());
			verify(idempotency).release(USER_ID, "saveConfig", KEY);
		}

		@Test
		@DisplayName("C18 – replayed saveConfig creates one version and one history row")
		void replay() throws Exception {
			when(idempotency.begin(USER_ID, "saveConfig", KEY)).thenReturn(Optional.of("{\"version\":1}"));

			assertThat(save(request())).isEqualTo("{\"version\":1}");
			verifyNoInteractions(configRepo, historyRepo, mastersClient);
		}

		@Test
		@DisplayName("an invalid configuration is refused with every problem and nothing is stored")
		void invalidRefused() {
			ProjectConsentConfigRequest request = validRequest();
			request.getRegistration().getGuardian().setAgeThreshold(16);
			request.getRegistration().setDefaultMode(ConsentMode.VOICE_CLIP);

			assertThatThrownBy(() -> save(GSON.toJson(request))).isInstanceOf(IEMRException.class)
					.hasMessage("registration.defaultMode must be one of the allowed modes; "
							+ "registration.guardian.ageThreshold must be 18");
			verifyNoInteractions(configRepo, historyRepo);
			verify(idempotency).release(USER_ID, "saveConfig", KEY);
		}

		@Test
		@DisplayName("missing identity fields are refused before the idempotency key is claimed")
		void identityRefusedFirst() throws IEMRException {
			assertThatThrownBy(() -> save("{\"projectId\":12}")).isInstanceOf(IEMRException.class);
			verify(idempotency, never()).begin(any(), anyString(), any());
		}

		@Test
		@DisplayName("a caller outside the provider, or another provider's project, gets project not found")
		void accessRefused() {
			when(userRoleMappingRepository.countActiveMappingsForProvider(anyLong(), anyShort())).thenReturn(0L);
			assertThatThrownBy(() -> save(request())).hasMessage("project not found");
			verifyNoInteractions(configRepo, historyRepo);
		}

		@Test
		@DisplayName("Consent-API being unavailable refuses the save")
		void unavailableRefused() {
			when(mastersClient.load(AUTH)).thenThrow(new ConsentServiceUnavailableException());

			assertThatThrownBy(() -> save(request())).hasMessage("Consent service unavailable, try again");
			verifyNoInteractions(configRepo, historyRepo);
		}

		@Test
		@DisplayName("inside a transaction the result is stored after commit, and the key is released on rollback")
		void storedAfterCommitOnly() throws Exception {
			when(configRepo.findByProjectIdAndDeletedFalse(PROJECT_ID)).thenReturn(Optional.empty());
			TransactionSynchronizationManager.initSynchronization();

			String result = save(request());

			verify(idempotency, never()).complete(any(), anyString(), anyString(), anyString());
			List<TransactionSynchronization> syncs = TransactionSynchronizationManager.getSynchronizations();
			assertThat(syncs).hasSize(1);
			syncs.get(0).afterCommit();
			syncs.get(0).afterCompletion(TransactionSynchronization.STATUS_COMMITTED);
			verify(idempotency).complete(USER_ID, "saveConfig", KEY, result);
			verify(idempotency, never()).release(any(), anyString(), anyString());
			syncs.get(0).afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);
			verify(idempotency).release(USER_ID, "saveConfig", KEY);
		}
	}

	@Nested
	@DisplayName("updateConfig — A5 (US4)")
	class Update {

		private String request(Integer baseVersion) {
			ProjectConsentConfigRequest request = validRequest();
			request.setBaseVersion(baseVersion);
			request.setModifiedBy("someone.else");
			return GSON.toJson(request);
		}

		private String update(String body) throws Exception {
			return service.updateConfig(body, USER_ID, USER_NAME, KEY, AUTH);
		}

		@Test
		@DisplayName("an update from the current version stores base + 1 with an UPDATE history row")
		void update() throws Exception {
			when(configRepo.updateIfVersion(eq(PROJECT_ID), eq(3), anyString(), isNull(), eq("MMU Project"),
					eq(USER_NAME), eq(Timestamp.from(NOW)))).thenReturn(1);

			String body = update(request(3));
			JsonObject result = json(body);

			ArgumentCaptor<ProjectConsentConfigHistory> history = ArgumentCaptor.forClass(ProjectConsentConfigHistory.class);
			verify(historyRepo).save(history.capture());
			assertThat(history.getValue().getAction()).isEqualTo("UPDATE");
			assertThat(history.getValue().getBaseVersion()).isEqualTo(3);
			assertThat(history.getValue().getVersion()).isEqualTo(4);
			assertThat(history.getValue().getCreatedBy()).isEqualTo(USER_NAME);
			assertThat(result.get("version").getAsInt()).isEqualTo(4);
			assertThat(result.get("savedBy").getAsString()).isEqualTo(USER_NAME);
			verify(idempotency).complete(USER_ID, "updateConfig", KEY, body);
		}

		@Test
		@DisplayName("separate revisit rules are passed to the update")
		void revisitPassed() throws Exception {
			ProjectConsentConfigRequest request = validRequest();
			request.setBaseVersion(3);
			request.setRevisit(validContext());
			when(configRepo.updateIfVersion(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString(),
					any())).thenReturn(1);

			assertThat(json(update(GSON.toJson(request))).has("revisit")).isTrue();
		}

		@Test
		@DisplayName("C20 – updateConfig from a stale baseVersion is rejected; current version unchanged")
		void stale() {
			when(configRepo.updateIfVersion(anyInt(), anyInt(), anyString(), any(), anyString(), anyString(), any()))
					.thenReturn(0);
			when(configRepo.findByProjectIdAndDeletedFalse(PROJECT_ID)).thenReturn(Optional.of(saved(4, null)));

			assertThatThrownBy(() -> update(request(3))).isInstanceOf(ConsentConfigConflictException.class);
			verify(historyRepo, never()).save(any());
			verify(idempotency).release(USER_ID, "updateConfig", KEY);
		}

		@Test
		@DisplayName("updating a project that has never been saved is refused")
		void neverSaved() {
			when(configRepo.updateIfVersion(anyInt(), anyInt(), anyString(), any(), anyString(), anyString(), any()))
					.thenReturn(0);
			when(configRepo.findByProjectIdAndDeletedFalse(PROJECT_ID)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> update(request(1))).isInstanceOf(IEMRException.class)
					.hasMessage("No configuration to update; save it first");
		}

		@Test
		@DisplayName("an update without a base version is refused")
		void baseVersionRequired() {
			assertThatThrownBy(() -> update(request(null))).hasMessage("baseVersion must be 1 or more");
			verifyNoInteractions(configRepo);
		}

		@Test
		@DisplayName("rollback after the update releases the key and stores no result")
		void rollbackReleases() throws Exception {
			when(configRepo.updateIfVersion(anyInt(), anyInt(), anyString(), any(), anyString(), anyString(), any()))
					.thenReturn(1);
			TransactionSynchronizationManager.initSynchronization();

			update(request(3));

			TransactionSynchronizationManager.getSynchronizations().get(0)
					.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);
			verify(idempotency).release(USER_ID, "updateConfig", KEY);
			verify(idempotency, never()).complete(any(), anyString(), anyString(), anyString());
		}
	}
}
