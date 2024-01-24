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
package com.iemr.common.data.beneficiary;

import java.sql.Timestamp;
import java.util.Set;

import com.google.gson.annotations.Expose;
import com.iemr.common.utils.mapper.OutputMapper;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;

@Entity
@Table(name = "m_beneficiaryincomestatus")
@Data
public class BeneficiaryIncomeStatus
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "IncomeStatusID")
	@Expose
	private Integer incomeStatusID;

	@Transient
	private Set<BenDemographics> i_bendemographics;

	@Column(name = "IncomeStatus")
	@Expose
	private String incomeStatus;
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

	public BeneficiaryIncomeStatus()
	{
	}

	public BeneficiaryIncomeStatus(Integer IncomeStatusID, String IncomeStatus)
	{
		this.incomeStatusID = IncomeStatusID;
		this.incomeStatus = IncomeStatus;
	}

	@Override
	public String toString()
	{
		return outputMapper.gson().toJson(this);
	}
}
