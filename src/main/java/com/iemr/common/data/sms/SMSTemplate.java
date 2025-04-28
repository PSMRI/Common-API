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
package com.iemr.common.data.sms;

import java.sql.Timestamp;
import java.util.List;

import com.iemr.common.utils.mapper.OutputMapper;

import com.google.gson.GsonBuilder;
import com.google.gson.LongSerializationPolicy;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "m_smstemplate")
@Data
public class SMSTemplate
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "SMSTemplateID")
	Integer smsTemplateID;
	@OneToMany(fetch=FetchType.LAZY)
	@JoinColumn(name="SMSTemplateID", insertable=false, updatable=false)
	List<SMSParametersMap> smsParameterMaps;
	@Column(name = "SMSTemplateName")
	String smsTemplateName;
	@Column(name = "SMSTemplate")
	String smsTemplate;
	@Column(name = "dltTemplateId")
	String dltTemplateId;
	@Column(name = "SMSSenderName")
	String smsSenderID;
	@Column(name = "SMSTypeID")
	Integer smsTypeID;
	@OneToOne
	@JoinColumn(name = "SMSTypeID", insertable = false, updatable = false)
	SMSType smsType;
	@Column(name = "ProviderServiceMapID")
	Integer providerServiceMapID;
	@Column(name = "Deleted", insertable = false, updatable = true)
	Boolean deleted;
	@Column(name = "CreatedBy", insertable = true, updatable = false)
	String createdBy;
	@Column(name = "CreatedDate", insertable = false, updatable = false)
	Timestamp createdDate;
	@Column(name = "ModifiedBy", insertable = false, updatable = true)
	String modifiedBy;
	@Column(name = "LastModDate", insertable = false, updatable = false)
	Timestamp lastModDate;

	// @Override
	// public String toString()
	// {
	// 	return OutputMapper.gsonWithoutExposeRestriction().toJson(this);
	// }

@Override
	public String toString() {

		return new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setLongSerializationPolicy(LongSerializationPolicy.STRING).serializeNulls()
				.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create().toJson(this);
	}

}
