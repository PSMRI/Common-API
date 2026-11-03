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
 * One saved version of a project's consent configuration. Append-only: rows are never updated or deleted,
 * so this is both the version history and the audit trail.
 */
@Entity
@Table(name = "t_projectconsentconfighistory")
@Data
public class ProjectConsentConfigHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", updatable = false)
	private Long id;

	@Column(name = "ProjectID", updatable = false)
	private Integer projectId;

	@Column(name = "ServiceProviderID", updatable = false)
	private Integer serviceProviderId;

	@Column(name = "Version", updatable = false)
	private Integer version;

	@Column(name = "BaseVersion", updatable = false)
	private Integer baseVersion;

	@Column(name = "Action", updatable = false)
	private String action;

	@Column(name = "RegistrationConfig", updatable = false)
	private String registrationConfig;

	@Column(name = "RevisitConfig", updatable = false)
	private String revisitConfig;

	@Column(name = "CreatedBy", updatable = false)
	private String createdBy;

	@Column(name = "CreatedDate", updatable = false)
	private Timestamp createdDate;
}
