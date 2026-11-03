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
package com.iemr.common.data.customization;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * Current consent configuration of a project. No row means version 0 (never saved).
 */
@Entity
@Table(name = "t_projectconsentconfig")
@Data
public class ProjectConsentConfigEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID")
	private Long id;

	@Column(name = "ProjectID")
	private Integer projectId;

	@Column(name = "ProjectName")
	private String projectName;

	@Column(name = "ServiceProviderID")
	private Integer serviceProviderId;

	@Column(name = "Version")
	private Integer version;

	@Column(name = "RegistrationConfig")
	private String registrationConfig;

	@Column(name = "RevisitConfig")
	private String revisitConfig;

	@Column(name = "Deleted")
	private Boolean deleted;

	@Column(name = "CreatedBy")
	private String createdBy;

	@Column(name = "CreatedDate", insertable = false, updatable = false)
	private Timestamp createdDate;

	@Column(name = "ModifiedBy")
	private String modifiedBy;

	// Set by the application clock on every save, so saved-at is the same in every response.
	@Column(name = "LastModDate")
	private Timestamp lastModDate;
}
