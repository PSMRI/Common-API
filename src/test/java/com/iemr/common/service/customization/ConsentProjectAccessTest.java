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
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.iemr.common.data.customization.ProjectCustomization;
import com.iemr.common.repo.customization.ProjectCustomizationRepo;
import com.iemr.common.repository.users.UserRoleMappingRepository;

/** Feature 006: project ownership (research R2). */
class ConsentProjectAccessTest {

	private ProjectCustomizationRepo projects;
	private UserRoleMappingRepository roles;
	private ConsentProjectAccess access;

	@BeforeEach
	void setUp() {
		projects = mock(ProjectCustomizationRepo.class);
		roles = mock(UserRoleMappingRepository.class);
		access = new ConsentProjectAccess(projects, roles);
		when(roles.countActiveMappingsForProvider((long) USER_ID, (short) PROVIDER_ID)).thenReturn(1L);
	}

	private static ProjectCustomization project(int providerId, Boolean deleted) {
		ProjectCustomization project = new ProjectCustomization();
		project.setProjectId(PROJECT_ID);
		project.setServiceProviderId(providerId);
		project.setDeleted(deleted);
		return project;
	}

	@Test
	void ownedProjectIsReturned() throws Exception {
		ProjectCustomization project = project(PROVIDER_ID, null);
		when(projects.findById(PROJECT_ID)).thenReturn(Optional.of(project));
		assertThat(access.requireOwnedProject(PROJECT_ID, PROVIDER_ID, USER_ID)).isSameAs(project);
	}

	@Test
	void unknownDeletedAndOtherProvidersProjectsAreNotFound() {
		when(projects.findById(PROJECT_ID)).thenReturn(Optional.empty(), Optional.of(project(PROVIDER_ID, true)),
				Optional.of(project(2, false)));
		for (int i = 0; i < 3; i++) {
			assertThatThrownBy(() -> access.requireOwnedProject(PROJECT_ID, PROVIDER_ID, USER_ID))
					.hasMessage("project not found");
		}
	}

	@Test
	void membersOnly() {
		assertThatThrownBy(() -> access.requireProviderMember(null, PROVIDER_ID)).hasMessage("project not found");
		assertThatThrownBy(() -> access.requireProviderMember(USER_ID, 40000)).hasMessage("project not found");
		assertThatThrownBy(() -> access.requireProviderMember(USER_ID, -40000)).hasMessage("project not found");
		when(roles.countActiveMappingsForProvider((long) USER_ID, (short) PROVIDER_ID)).thenReturn(0L, (Long) null);
		assertThatThrownBy(() -> access.requireProviderMember(USER_ID, PROVIDER_ID)).hasMessage("project not found");
		assertThatThrownBy(() -> access.requireProviderMember(USER_ID, PROVIDER_ID)).hasMessage("project not found");
	}
}
