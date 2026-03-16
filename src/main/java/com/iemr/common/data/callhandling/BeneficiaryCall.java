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
package com.iemr.common.data.callhandling;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.Expose;
import com.iemr.common.data.beneficiary.Beneficiary;
import com.iemr.common.utils.config.ConfigProperties;
import com.iemr.common.utils.mapper.OutputMapper;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(name = "t_bencall")
@Data
public class BeneficiaryCall {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "BenCallID", insertable = false, updatable = false)
	@Expose
	private Long benCallID;
	// @OneToOne(fetch = FetchType.LAZY, mappedBy = "beneficiaryCall")
	// @Expose
	// private OutboundCallRequest outboundCallRequest;

	@Column(name = "BeneficiaryRegID")
	@Expose
	private Long beneficiaryRegID;

	@Transient
	@Expose
	private Beneficiary beneficiary;

	@Column(name = "CallID")
	@Expose
	private String callID;
	@Expose
	@Column(name = "CalledServiceID")
	private Integer calledServiceID;
	@Expose
	@Column(name = "is1097")
	private Boolean is1097 = false;
	@Expose
	@Column(name = "CallTime")
	private Timestamp callTime;
	@Expose
	@Column(name = "Remarks")
	private String remarks;

	@Column(name = "ServicesProvided")
	private String servicesProvided;

	@Column(name = "CallTypeID")
	@Expose
	private Integer callTypeID;

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "CallTypeID", insertable = false, updatable = false)
	private CallType callTypeObj;

	@Expose
	@Column(name = "CallClosureType")
	private String callClosureType;

	@Expose
	@Column(name = "DispositionStatusID")
	private Integer dispositionStatusID;

	@Expose
	@Column(name = "CallReceivedUserID")
	private Integer callReceivedUserID;

	@Expose
	@Column(name = "CallEndUserID")
	private Integer callEndUserID;
	@Column(name = "Category")
	private String category;
	@Column(name = "SubCategory")
	private String subCategory;

	//@Expose
	@Column(name = "CDICallStatus", insertable = false, updatable = true)
	private String cDICallStatus;

	@Expose
	@Column(name = "ReceivedAgentID")
	private String agentID;

	@Expose
	@Column(name = "IsOutbound")
	private Boolean isOutbound;

	@Expose
	@Column(name = "IsCalledEarlier")
	private Boolean isCalledEarlier = false;

	@Column(name = "Deleted", insertable = false, updatable = true)
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

	@Expose
	@Transient
	private Long informationServices;

	@Expose
	@Transient
	private Long feedbackServices;

	@Expose
	@Transient
	private Long referralServices;

	@Expose
	@Transient
	private Long counsellingServices;

	@Transient
	private Timestamp filterStartDate;
	@Transient
	private Timestamp filterEndDate;
	@Transient
	@Expose
	private Boolean fitToBlock = false;

	@Transient
	private Boolean isFeedback = false;

	@Expose
	@Column(name = "PhoneNo")
	private String phoneNo;

	@Expose
	@Column(name = "ReceivedRoleName")
	private String receivedRoleName;

	@Transient
	private Boolean endCall = false;

	@Transient
	private String agentIPAddress;

	@Transient
	@Expose
	private Long maxCount = 0l;

	@Column(name = "RecordingFilePath", length = 300)
	@Expose
	private String recordingPath;

	@Column(name = "ArchiveFilePath", length = 300)
	@Expose
	private String archivePath;

	@Column(name = "EmergencyType")
	@Expose
	private Short emergencyType;

	@Transient
	private Integer count;
	@Transient
	private String inboundOutbound;
	@Transient
	private String benCallIDs;

	@Transient
	private Integer pageNo;
	@Transient
	private Integer pageSize;

	@Expose
	@Column(name = "CallEndTime")
	private Timestamp callEndTime;
	@Expose
	@Column(name = "CallDuration")
	private String callDuration;
	@Expose
	@Column(name = "CZcallStartTime")
	private String cZcallStartTime;
	@Expose
	@Column(name = "CZcallEndTime")
	private String cZcallEndTime;
	@Expose
	@Column(name = "CZcallDuration")
	private Integer cZcallDuration;

	// referral
	@Expose
	@Column(name = "ExternalReferral")
	private String externalReferral;
	@Expose
	@Column(name = "InsTypeid")
	private Integer instTypeId;
	@Expose
	@Column(name = "InsName")
	private String instName;

	@Transient
	@Expose
	private String[] instNames;

	@Transient
	@Expose
	private Boolean isOutboundManualDial;

	@Transient
	private Boolean isTransfered;

	@Column(name = "IsDispositionSentToCTI")
	private Boolean isDispositionSentToCTI = false;

	public BeneficiaryCall() {

	}

	public BeneficiaryCall(Long benCallID, Integer calledServiceID, Boolean is1097, Timestamp callTime, String remarks,
			String callClosureType, Integer dispositionStatusID) {
		this.benCallID = benCallID;
		this.calledServiceID = calledServiceID;
		this.is1097 = is1097;
		this.callTime = callTime;
		this.remarks = remarks;
		this.callClosureType = callClosureType;
		this.dispositionStatusID = dispositionStatusID;
	}

	public BeneficiaryCall(Long benCallID, Timestamp callTime, String remarks, Long informationServices,
			Long feedbackServices, Long referralServices, Timestamp createdDate, Long counsellingServices) {
		this.benCallID = benCallID;
		this.callTime = callTime;
		this.remarks = remarks;
		this.informationServices = informationServices;
		this.feedbackServices = feedbackServices;
		this.referralServices = referralServices;
		this.counsellingServices = counsellingServices;
		this.createdDate = createdDate;
	}

	public BeneficiaryCall(Long beneficiaryRegID, Boolean is1097, String createdBy) {
		this.beneficiaryRegID = beneficiaryRegID;
		this.is1097 = is1097;
		this.createdBy = createdBy;
	}

	public BeneficiaryCall(Long benCallID, Timestamp createdDate, String agentID, String callID, String recordingPath,
			String archivePath) {
		String loggerBaseURL = ConfigProperties.getPropertyByName("cti-logger_base_url");
		this.benCallID = benCallID;
		this.createdDate = createdDate;
		this.agentID = agentID;
		this.callID = callID;
		this.recordingPath = (recordingPath != null) ? (loggerBaseURL + "/" + recordingPath) : recordingPath;
		this.archivePath = (archivePath != null) ? (loggerBaseURL + "/" + archivePath) : archivePath;
	}

	@Transient
	private OutputMapper outputMapper = new OutputMapper();

	@Override
	public String toString() {
		return outputMapper.gson().toJson(this);
	}

}
