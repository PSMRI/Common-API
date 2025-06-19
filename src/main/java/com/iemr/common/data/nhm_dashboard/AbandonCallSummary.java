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
package com.iemr.common.data.nhm_dashboard;

import java.math.BigInteger;
import java.sql.Timestamp;

import com.google.gson.annotations.Expose;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "t_abandonsummary")
public class AbandonCallSummary {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Expose
	@Column(name = "id", insertable = false)
	private Long id;
	@Expose
	@Column(name = "CustomerPhoneNumber")
	private String customerPhoneNumber;
	@Expose
	@Column(name = "UniqueID")
	private String uniqueId;
	@Expose
	@Column(name = "QueueEnterTime")
	private Timestamp queueEnterTime;
	@Expose
	@Column(name = "QueueTime")
	private Integer queueTime;
	@Expose
	@Column(name = "DIDNumber")
	private BigInteger didNumber;
	@Expose
	@Column(name = "Skills")
	private String skills;
	@Expose
	@Column(name = "CampaignName")
	private String campaignName;
	@Expose
	@Column(name = "RedialFlag")
	private String redicalFlag;
	@Expose
	@Column(name = "CallType")
	private String callType;
	@Expose
	@Column(name = "Deleted", insertable = false, updatable = true)
	private Boolean deleted;
	@Expose
	@Column(name = "Processed", insertable = false)
	private String processed;
	@Expose
	@Column(name = "CreatedBy", updatable = false)
	private String createdBy;
	@Expose
	@Column(name = "CreatedDate", insertable = false, updatable = false)
	private Timestamp createdDate;
	@Expose
	@Column(name = "ModifiedBy", insertable = false, updatable = true)
	private String modifiedBy;
	@Expose
	@Column(name = "LastModDate", insertable = false, updatable = false)
	private Timestamp lastModDate;

}
