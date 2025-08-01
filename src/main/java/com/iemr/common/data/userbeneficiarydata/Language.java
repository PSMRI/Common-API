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
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see https://www.gnu.org/licenses/.
*/
package com.iemr.common.data.userbeneficiarydata;

import java.sql.Timestamp;
import java.util.Set;

import com.google.gson.annotations.Expose;
import com.iemr.common.data.beneficiary.BenDemographics;
import com.iemr.common.data.notification.Notification;
import com.iemr.common.data.users.UserLangMapping;
import com.iemr.common.utils.mapper.OutputMapper;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;

@Entity
@Table(name = "M_Language")
@Data
public class Language
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "LanguageID")
	@Expose
	private Integer languageID;

	@Transient
	private Set<BenDemographics> i_bendemographics;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "m_language")
	@Transient
	private Set<UserLangMapping> m_UserLangMappings;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "language")
	@Transient
	private Set<Notification> notifications;

	@Column(name = "LanguageName")
	@Expose
	private String languageName;
	@Column(name = "LanguageDesc")
	@Expose
	private String languageDesc;
	@Column(name = "PropertyFilePath")
	@Expose
	private String propertyFilePath;
	@Column(name = "IVRFilePath")
	@Expose
	private String ivrFilePath;
	@Column(name = "Deleted", insertable = false, updatable = true)
	@Expose
	private Boolean deleted;
	@Column(name = "CreatedBy")
	@Expose
	private String createdBy;
	@Column(name = "CreatedDate", insertable = false, updatable = false)
	private Timestamp createdDate;
	@Column(name = "ModifiedBy")
	private String modifiedBy;
	@Column(name = "LastModDate", insertable = false, updatable = false)
	private Timestamp lastModDate;

	@Transient
	private OutputMapper outputMapper = new OutputMapper();

	public Language()
	{
	}

	public Language(int languageId, String languageName, String languageDesc, String propertyFilePath,
			String IVRFilePath)
	{
		this.languageID = Integer.valueOf(languageId);
		this.languageName = languageName;
		this.languageDesc = languageDesc;
		this.propertyFilePath = propertyFilePath;
		this.ivrFilePath = IVRFilePath;
	}

	public Language(int languageId, String languageName)
	{
		this.languageID = Integer.valueOf(languageId);
		this.languageName = languageName;
	}

	@Override
	public String toString()
	{
		return outputMapper.gson().toJson(this);
	}
}
