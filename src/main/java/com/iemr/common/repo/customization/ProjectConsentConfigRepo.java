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
package com.iemr.common.repo.customization;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.iemr.common.data.customization.ProjectConsentConfigEntity;

@Repository
public interface ProjectConsentConfigRepo extends JpaRepository<ProjectConsentConfigEntity, Long> {

	Optional<ProjectConsentConfigEntity> findByProjectIdAndDeletedFalse(Integer projectId);

	List<ProjectConsentConfigEntity> findByServiceProviderIdAndDeletedFalse(Integer serviceProviderId);

	/**
	 * Saves a new version only if the stored version is still the one the caller loaded. Returns 0 when
	 * someone else saved first (or there is no configuration).
	 */
	@Modifying(clearAutomatically = true)
	@Query("UPDATE ProjectConsentConfigEntity c SET c.version = c.version + 1, c.registrationConfig = :reg, "
			+ "c.revisitConfig = :rev, c.projectName = :projectName, c.modifiedBy = :modifiedBy, "
			+ "c.lastModDate = :modifiedAt WHERE c.projectId = :projectId AND c.version = :baseVersion "
			+ "AND c.deleted = false")
	int updateIfVersion(@Param("projectId") Integer projectId, @Param("baseVersion") Integer baseVersion,
			@Param("reg") String registrationConfig, @Param("rev") String revisitConfig,
			@Param("projectName") String projectName, @Param("modifiedBy") String modifiedBy,
			@Param("modifiedAt") Timestamp modifiedAt);
}
