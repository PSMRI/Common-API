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

import java.sql.Timestamp;
import java.time.Clock;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.iemr.common.data.customization.ProjectConsentConfigEntity;
import com.iemr.common.data.customization.ProjectConsentConfigHistory;
import com.iemr.common.data.customization.ProjectCustomization;
import com.iemr.common.data.customization.ServicelineCustomization;
import com.iemr.common.data.customization.consent.ConsentConfigLoadRequest;
import com.iemr.common.data.customization.consent.ConsentConfigLoadResponse;
import com.iemr.common.data.customization.consent.ConsentConfigMasters;
import com.iemr.common.data.customization.consent.ConsentConfigMasters.ModeMaster;
import com.iemr.common.data.customization.consent.ConsentConfigMasters.ProjectVersion;
import com.iemr.common.data.customization.consent.ConsentContext;
import com.iemr.common.data.customization.consent.ConsentMode;
import com.iemr.common.data.customization.consent.ContextConfig;
import com.iemr.common.data.customization.consent.ProjectConsentConfig;
import com.iemr.common.data.customization.consent.ProjectConsentConfigRequest;
import com.iemr.common.repo.customization.ProjectConsentConfigHistoryRepo;
import com.iemr.common.repo.customization.ProjectConsentConfigRepo;
import com.iemr.common.repo.customization.ServicelineCustomizationRepo;
import com.iemr.common.service.customization.ConsentMastersClient.CentralLists;
import com.iemr.common.utils.InputSanitizer;
import com.iemr.common.utils.exception.ConsentConfigConflictException;
import com.iemr.common.utils.exception.ConsentServiceUnavailableException;
import com.iemr.common.utils.exception.IEMRException;
import com.iemr.common.utils.mapper.InputMapper;

/**
 * Per-project consent configuration: API 1 for the MMU consent dialog and A1–A5 for Admin. Stored configurations
 * are cleaned before they are returned ({@link ConsentConfigCleaner}, feature 005); saves are checked by
 * {@link ConsentConfigValidator}.
 */
@Service
public class ConsentConfigServiceImpl implements ConsentConfigService {

	static final String SAVE = "saveConfig";
	static final String UPDATE = "updateConfig";

	private static final Gson GSON = new Gson();

	private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	private final ProjectConsentConfigRepo configRepo;
	private final ProjectConsentConfigHistoryRepo historyRepo;
	private final ServicelineCustomizationRepo servicelineCustomizationRepo;
	private final ConsentProjectAccess access;
	private final ConsentMastersClient mastersClient;
	private final ConsentConfigValidator validator;
	private final ConsentConfigIdempotency idempotency;
	private final ConsentConfigCleaner cleaner;
	private final Clock clock;

	public ConsentConfigServiceImpl(ProjectConsentConfigRepo configRepo, ProjectConsentConfigHistoryRepo historyRepo,
			ServicelineCustomizationRepo servicelineCustomizationRepo, ConsentProjectAccess access,
			ConsentMastersClient mastersClient,
			ConsentConfigValidator validator, ConsentConfigIdempotency idempotency,
			ConsentConfigCleaner cleaner, Clock clock) {
		this.configRepo = configRepo;
		this.historyRepo = historyRepo;
		this.servicelineCustomizationRepo = servicelineCustomizationRepo;
		this.access = access;
		this.mastersClient = mastersClient;
		this.validator = validator;
		this.idempotency = idempotency;
		this.cleaner = cleaner;
		this.clock = clock;
	}

	@Override
	public String fetchConsentConfig(String request, Integer userId) throws Exception {
		ConsentConfigLoadRequest load = parse(request, ConsentConfigLoadRequest.class);
		ConsentContext context = parseContext(load.getContext());
		if (load.getServiceProviderId() != null) {
			access.requireProviderMember(userId, load.getServiceProviderId());
		}
		ServicelineCustomization mapping;
		try {
			mapping = servicelineCustomizationRepo.checkExistingData(load.getServiceLineId(), load.getServiceLine(),
					load.getStateId(), load.getDistrictId(), load.getBlockId(), load.getServiceProviderId());
		} catch (IncorrectResultSizeDataAccessException e) {
			logger.warn("More than one project is mapped to serviceLineId={} stateId={} districtId={} blockId={} "
					+ "serviceProviderId={}; no consent configuration returned", load.getServiceLineId(),
					load.getStateId(), load.getDistrictId(), load.getBlockId(), load.getServiceProviderId());
			return null;
		}
		if (mapping == null) {
			return null;
		}
		Optional<ProjectConsentConfigEntity> saved = configRepo.findByProjectIdAndDeletedFalse(mapping.getProjectId());
		if (saved.isEmpty()) {
			return null;
		}
		ProjectConsentConfigEntity entity = saved.get();
		ConsentConfigLoadResponse response = new ConsentConfigLoadResponse();
		response.setProjectId(entity.getProjectId());
		response.setVersion(entity.getVersion());
		response.setContext(context.name());
		String json = context == ConsentContext.REVISIT && entity.getRevisitConfig() != null
				? entity.getRevisitConfig()
				: entity.getRegistrationConfig();
		response.setConfig(cleaner.clean(GSON.fromJson(json, ContextConfig.class)));
		return GSON.toJson(response);
	}

	@Override
	public String getMasters(Integer serviceProviderId, Integer userId, String authorization) throws Exception {
		if (serviceProviderId == null) {
			throw new IEMRException("serviceProviderId is required");
		}
		access.requireProviderMember(userId, serviceProviderId);
		CentralLists central = loadCentral(authorization);
		ConsentConfigMasters masters = new ConsentConfigMasters();
		masters.setModes(Arrays.stream(ConsentMode.values()).filter(ConsentMode::isOffered)
				.map(m -> new ModeMaster(m, m.getDisplayName())).collect(Collectors.toList()));
		masters.setPurposes(central.getPurposes());
		masters.setLanguages(central.getLanguages());
		masters.setProjects(configRepo.findByServiceProviderIdAndDeletedFalse(serviceProviderId).stream()
				.map(e -> new ProjectVersion(e.getProjectId(), e.getVersion())).collect(Collectors.toList()));
		return GSON.toJson(masters);
	}

	@Override
	public String fetchAllConfigs(String request, Integer userId) throws Exception {
		ProjectConsentConfigRequest ref = parse(request, ProjectConsentConfigRequest.class);
		if (ref.getServiceProviderId() == null) {
			throw new IEMRException("serviceProviderId is required");
		}
		access.requireProviderMember(userId, ref.getServiceProviderId());
		List<ProjectConsentConfig> configs = configRepo.findByServiceProviderIdAndDeletedFalse(ref.getServiceProviderId())
				.stream().map(this::toDto).collect(Collectors.toList());
		return GSON.toJson(configs);
	}

	@Override
	public String fetchConfigByProject(String request, Integer userId) throws Exception {
		ProjectConsentConfigRequest ref = parse(request, ProjectConsentConfigRequest.class);
		if (ref.getProjectId() == null || ref.getServiceProviderId() == null) {
			throw new IEMRException("projectId and serviceProviderId are required");
		}
		ProjectCustomization project = access.requireOwnedProject(ref.getProjectId(), ref.getServiceProviderId(), userId);
		ProjectConsentConfig config = configRepo.findByProjectIdAndDeletedFalse(ref.getProjectId()).map(this::toDto)
				.orElseGet(() -> neverSaved(project));
		return GSON.toJson(config);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public String saveConfig(String request, Integer userId, String userName, String idempotencyKey,
			String authorization) throws Exception {
		return write(SAVE, request, userId, userName, idempotencyKey, authorization);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public String updateConfig(String request, Integer userId, String userName, String idempotencyKey,
			String authorization) throws Exception {
		return write(UPDATE, request, userId, userName, idempotencyKey, authorization);
	}

	private String write(String endpoint, String request, Integer userId, String userName, String idempotencyKey,
			String authorization) throws Exception {
		boolean update = UPDATE.equals(endpoint);
		ProjectConsentConfigRequest config = parse(request, ProjectConsentConfigRequest.class);
		validator.validateIdentity(config, update);
		config.setProjectName(InputSanitizer.sanitize(config.getProjectName()));
		Optional<String> replay = idempotency.begin(userId, endpoint, idempotencyKey);
		if (replay.isPresent()) {
			return replay.get();
		}
		String response;
		try {
			access.requireOwnedProject(config.getProjectId(), config.getServiceProviderId(), userId);
			validator.validateAndNormalise(config, loadCentral(authorization));
			response = GSON.toJson(update ? applyUpdate(config, userName) : applyCreate(config, userName));
		} catch (Exception e) {
			idempotency.release(userId, endpoint, idempotencyKey);
			throw e;
		}
		storeAfterCommit(userId, endpoint, idempotencyKey, response);
		return response;
	}

	private ProjectConsentConfig applyCreate(ProjectConsentConfigRequest config, String userName) {
		if (configRepo.findByProjectIdAndDeletedFalse(config.getProjectId()).isPresent()) {
			throw new ConsentConfigConflictException();
		}
		Timestamp now = Timestamp.from(clock.instant());
		ProjectConsentConfigEntity entity = new ProjectConsentConfigEntity();
		entity.setProjectId(config.getProjectId());
		entity.setProjectName(config.getProjectName());
		entity.setServiceProviderId(config.getServiceProviderId());
		entity.setVersion(1);
		entity.setRegistrationConfig(GSON.toJson(config.getRegistration()));
		entity.setRevisitConfig(config.getRevisit() == null ? null : GSON.toJson(config.getRevisit()));
		entity.setDeleted(false);
		entity.setCreatedBy(userName);
		entity.setLastModDate(now);
		try {
			// Flush now so a concurrent first save fails on the unique ProjectID key here, not at commit.
			configRepo.saveAndFlush(entity);
		} catch (DataIntegrityViolationException e) {
			throw new ConsentConfigConflictException();
		}
		return recordVersion(config, "CREATE", 0, userName, now);
	}

	private ProjectConsentConfig applyUpdate(ProjectConsentConfigRequest config, String userName)
			throws IEMRException {
		Timestamp now = Timestamp.from(clock.instant());
		int updated = configRepo.updateIfVersion(config.getProjectId(), config.getBaseVersion(),
				GSON.toJson(config.getRegistration()),
				config.getRevisit() == null ? null : GSON.toJson(config.getRevisit()), config.getProjectName(),
				userName, now);
		if (updated == 0) {
			if (configRepo.findByProjectIdAndDeletedFalse(config.getProjectId()).isPresent()) {
				throw new ConsentConfigConflictException();
			}
			throw new IEMRException("No configuration to update; save it first");
		}
		return recordVersion(config, "UPDATE", config.getBaseVersion(), userName, now);
	}

	/** Writes the history (audit) row of a save and returns the saved configuration. */
	private ProjectConsentConfig recordVersion(ProjectConsentConfigRequest config, String action, int baseVersion,
			String userName, Timestamp now) {
		int version = baseVersion + 1;
		ProjectConsentConfigHistory history = new ProjectConsentConfigHistory();
		history.setProjectId(config.getProjectId());
		history.setServiceProviderId(config.getServiceProviderId());
		history.setVersion(version);
		history.setBaseVersion(baseVersion);
		history.setAction(action);
		history.setRegistrationConfig(GSON.toJson(config.getRegistration()));
		history.setRevisitConfig(config.getRevisit() == null ? null : GSON.toJson(config.getRevisit()));
		history.setCreatedBy(userName);
		history.setCreatedDate(now);
		historyRepo.save(history);
		logger.info("Consent configuration saved: projectId={} serviceProviderId={} version={}",
				config.getProjectId(), config.getServiceProviderId(), version);

		ProjectConsentConfig saved = new ProjectConsentConfig();
		saved.setProjectId(config.getProjectId());
		saved.setProjectName(config.getProjectName());
		saved.setServiceProviderId(config.getServiceProviderId());
		saved.setVersion(version);
		saved.setSavedBy(userName);
		saved.setSavedAt(now.toInstant().toString());
		saved.setRegistration(config.getRegistration());
		saved.setRevisit(config.getRevisit());
		return saved;
	}

	/** Stores the response for idempotent replay only once the save has committed; releases the key on rollback. */
	private void storeAfterCommit(Integer userId, String endpoint, String key, String response) {
		if (!TransactionSynchronizationManager.isSynchronizationActive()) {
			idempotency.complete(userId, endpoint, key, response);
			return;
		}
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				idempotency.complete(userId, endpoint, key, response);
			}

			@Override
			public void afterCompletion(int status) {
				if (status != STATUS_COMMITTED) {
					idempotency.release(userId, endpoint, key);
				}
			}
		});
	}

	private CentralLists loadCentral(String authorization) throws IEMRException {
		try {
			return mastersClient.load(authorization);
		} catch (ConsentServiceUnavailableException e) {
			logger.warn("Consent-API unavailable while loading consent configuration masters");
			throw new IEMRException(e.getMessage());
		}
	}

	ProjectConsentConfig toDto(ProjectConsentConfigEntity entity) {
		ProjectConsentConfig dto = new ProjectConsentConfig();
		dto.setProjectId(entity.getProjectId());
		dto.setProjectName(entity.getProjectName());
		dto.setServiceProviderId(entity.getServiceProviderId());
		dto.setVersion(entity.getVersion());
		dto.setSavedBy(entity.getModifiedBy() != null ? entity.getModifiedBy() : entity.getCreatedBy());
		dto.setSavedAt(entity.getLastModDate() == null ? null : entity.getLastModDate().toInstant().toString());
		dto.setRegistration(cleaner.clean(GSON.fromJson(entity.getRegistrationConfig(), ContextConfig.class)));
		dto.setRevisit(entity.getRevisitConfig() == null ? null
				: cleaner.clean(GSON.fromJson(entity.getRevisitConfig(), ContextConfig.class)));
		return dto;
	}

	ProjectConsentConfig neverSaved(ProjectCustomization project) {
		ProjectConsentConfig dto = new ProjectConsentConfig();
		dto.setProjectId(project.getProjectId());
		dto.setProjectName(project.getProjectName());
		dto.setServiceProviderId(project.getServiceProviderId());
		dto.setVersion(0);
		return dto;
	}

	private static <T> T parse(String request, Class<T> type) throws IEMRException {
		T parsed;
		try {
			parsed = InputMapper.gson().fromJson(request, type);
		} catch (JsonParseException e) {
			throw new IEMRException("Invalid request body");
		}
		if (parsed == null) {
			throw new IEMRException("Request body is required");
		}
		return parsed;
	}

	private static ConsentContext parseContext(String context) throws IEMRException {
		try {
			return ConsentContext.valueOf(context);
		} catch (IllegalArgumentException | NullPointerException e) {
			throw new IEMRException("context must be REGISTRATION or REVISIT");
		}
	}
}
