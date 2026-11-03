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
package com.iemr.common.data.customization.consent;

import java.util.List;

import com.google.gson.annotations.Expose;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class ConsentConfigMasters {

	@Expose
	private List<ModeMaster> modes;
	@Expose
	private List<PurposeMaster> purposes;
	@Expose
	private List<LanguageMaster> languages;
	@Expose
	private List<ProjectVersion> projects;

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class ModeMaster {
		@Expose
		private ConsentMode mode;
		@Expose
		private String name;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class PurposeMaster {
		@Expose
		private String purposeCode;
		@Expose
		private String name;
		@Expose
		private Boolean isMandatory;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class LanguageMaster {
		@Expose
		private String languageCode;
		@Expose
		private String languageName;
		@Expose
		private Boolean hasNotice;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class ProjectVersion {
		@Expose
		private Integer projectId;
		@Expose
		private Integer version;
	}
}
