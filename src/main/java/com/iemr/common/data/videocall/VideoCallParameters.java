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

package com.iemr.common.data.videocall;

import java.sql.Timestamp;

import com.iemr.common.utils.mapper.OutputMapper;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "t_videocallparameter") 
@Data
public class VideoCallParameters {
		@Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    @Column(name = "MeetingID")
	    private Integer meetingID;

	    @Column(name = "DateOfCall")
	    private Timestamp dateOfCall;

	    @Column(name = "CallerPhoneNumber")
	    private String callerPhoneNumber;

	    @Column(name = "AgentID")
	    private String agentID;

	    @Column(name = "AgentName")
	    private String agentName;

	    @Column(name = "MeetingLink")
	    private String meetingLink;

	    @Column(name = "CallStatus")
	    private String callStatus;

	    @Column(name = "CallDuration")
	    private String callDuration;

	    @Column(name = "ProviderServiceMapID")
	    private Integer providerServiceMapID;

	    @Column(name = "BeneficiaryRegID")
    	private Long beneficiaryRegID;

	    @Column(name = "ClosureRemark")
	    private String closureRemark;
	    
		@Column(name = "LinkGeneratedAt")
		private Timestamp linkGeneratedAt;

		@Column(name = "IsLinkUsed")
		private boolean linkUsed;

	    @Column(name = "Deleted", insertable = false, updatable = true)
		private Boolean deleted;

		@Column(name = "CreatedBy", insertable = true, updatable = false)
		private String createdBy;

		@Column(name = "CreatedDate", insertable = false, updatable = false)
		private Timestamp createdDate;

		@Column(name = "ModifiedBy", insertable = false, updatable = true)
		private String modifiedBy;

		@Column(name = "LastModDate", insertable = false, updatable = false)
		private Timestamp lastModDate;
		
		@Override
		public String toString()
		{
			return OutputMapper.gsonWithoutExposeRestriction().toJson(this);
		}
}
