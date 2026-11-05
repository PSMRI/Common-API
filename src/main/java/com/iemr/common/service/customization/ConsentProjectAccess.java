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

import org.springframework.stereotype.Component;

import com.iemr.common.data.customization.ProjectCustomization;
import com.iemr.common.repo.customization.ProjectCustomizationRepo;
import com.iemr.common.repository.users.UserRoleMappingRepository;
import com.iemr.common.utils.exception.IEMRException;

/**
 * Who may manage a project's consent settings: the caller must belong to the project's service provider
 * (feature 006, research R2). Shared by the
 * consent configuration (A1–A5) and consent message (A6–A8) services. Unknown, deleted and other providers'
 * projects all answer "project not found".
 */
@Component
public class ConsentProjectAccess {

	static final String NOT_FOUND = "project not found";

	private final ProjectCustomizationRepo projectCustomizationRepo;

	private final UserRoleMappingRepository userRoleMappingRepository;

	public ConsentProjectAccess(ProjectCustomizationRepo projectCustomizationRepo,
			UserRoleMappingRepository userRoleMappingRepository) {
		this.projectCustomizationRepo = projectCustomizationRepo;
		this.userRoleMappingRepository = userRoleMappingRepository;
	}

	/** The caller must have an active role mapping under the service provider (callers check it is not null). */
	public void requireProviderMember(Integer userId, Integer serviceProviderId) throws IEMRException {
		if (userId == null || outOfRange(serviceProviderId)) {
			throw new IEMRException(NOT_FOUND);
		}
		Long mappings = userRoleMappingRepository.countActiveMappingsForProvider(userId.longValue(),
				serviceProviderId.shortValue());
		if (mappings == null || mappings == 0) {
			throw new IEMRException(NOT_FOUND);
		}
	}

	/** The project, when it exists, is not deleted and belongs to the caller's service provider. */
	public ProjectCustomization requireOwnedProject(Integer projectId, Integer serviceProviderId, Integer userId)
			throws IEMRException {
		requireProviderMember(userId, serviceProviderId);
		ProjectCustomization project = projectCustomizationRepo.findById(projectId).orElse(null);
		if (project == null || Boolean.TRUE.equals(project.getDeleted())
				|| !serviceProviderId.equals(project.getServiceProviderId())) {
			throw new IEMRException(NOT_FOUND);
		}
		return project;
	}

	private static boolean outOfRange(Integer serviceProviderId) {
		return serviceProviderId > Short.MAX_VALUE || serviceProviderId < Short.MIN_VALUE;
	}
}
