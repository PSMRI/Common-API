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
package common.iemr.common.secondary.data.report;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import com.google.gson.annotations.Expose;
import com.iemr.common.utils.mapper.OutputMapper;

import jakarta.persistence.Column;
import org.springframework.context.annotation.Profile;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Profile("!swagger")
@Entity
@Table(name = "fact_bencall", schema = "db_reporting")
public class SecondaryCallReport implements Serializable 
{

	@Transient
	SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
	
	@Transient
	String callDuartion ;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "Fact_BenCallID")
	private Long fact_BenCallID;

	@Expose
	@Column(name = "BenCallID")
	private Long benCallID;

	
	@Column(name = "BeneficiaryRegID")
	private Long beneficiaryRegID;

	@Expose
	@Column(name = "CallID")
	private String callID;

	@Expose
	@Column(name = "SessionID")
	private String sessionID;

	@Expose
	@Column(name = "PhoneNo")
	private String phoneNo;

	@Column(name = "ProviderServiceMapID")
	private Integer providerServiceMapID;

	@Column(name = "CallTypeID")
	private Integer callTypeID;

	@Expose
	@Column(name = "CallTypeName")
	private String callTypeName;

	@Expose
	@Column(name = "CallSubTypeName")
	private String callSubTypeName;

	@Expose
	@Column(name = "CallTime")
	private Timestamp callTime;

	@Expose
	@Column(name = "Remarks")
	private String remarks;

	@Expose
	@Column(name = "CallClosureType")
	private String callClosureType;

	@Expose
	@Column(name = "CallReceivedUserID")
	private Integer callReceivedUserID;

	@Expose
	@Column(name = "ReceivedRoleName")
	private String receivedRoleName;

	@Expose
	@Column(name = "ReceivedAgentID")
	private String agentID;	

	@Expose
	@Column(name = "CallEndUserID")
	private Integer callEndUserID;

	@Expose
	@Column(name = "Category")
	private String category;

	@Expose
	@Column(name = "SubCategory")
	private String subCategory;

	@Expose
	@Column(name = "CDICallStatus")
	private String cdiCallStatus;

	@Expose
	@Column(name = "IsOutbound")
	private Boolean isOutbound;

	@Expose
	@Column(name = "IsCalledEarlier")
	private Boolean isCalledEarlier;

	@Column(name = "CreatedBy")
	private String createdBy;

	
	@Column(name = "CreatedDate")
	private Timestamp createdDate;
	@Expose
	@Column(name = "CallDuration")
	private String CallDuration;
	@Expose
	@Column(name = "CZCallDuration")
	private Integer czCallDuration;

//	@OneToOne
//	@JoinColumn(updatable = false, insertable = false, name = "beneficiaryRegID", referencedColumnName = "beneficiaryRegID")
//	private BenDetails beneficiaryReport;

	// search params
	@Transient
	private Timestamp startDateTime;

	@Transient
	private Timestamp endDateTime;
	
//	@OneToOne(fetch = FetchType.EAGER)
//	@JoinColumn(name = "CallReceivedUserID", referencedColumnName = "UserID", insertable = false, updatable = false)
//	private DimUserReport userReportObj;

	
//	public CallReport()
//	{
//		super();
//	}

	@Transient
	private OutputMapper outputMapper = new OutputMapper();

	@Override
	public String toString()
	{
		return outputMapper.gson().toJson(this);
	}

	public Timestamp getStartDateTime()
	{
		return startDateTime;
	}

	public Timestamp getEndDateTime()
	{
		return endDateTime;
	}

	public Integer getProviderServiceMapID()
	{
		return providerServiceMapID;
	}
	
	public String getAgentID()
	{
		return agentID;
	}

	public String getPhoneNo() {
		return phoneNo;
	}

	public void setPhoneNo(String phoneNo) {
		this.phoneNo = phoneNo;
	}

	public String getCallTypeName() {
		return callTypeName;
	}

	public void setCallTypeName(String callTypeName) {
		this.callTypeName = callTypeName;
	}

	public String getCallSubTypeName() {
		return callSubTypeName;
	}

	public String getCallDuration() {
		return CallDuration;
	}

	public void setCallDuration(String callDuration) {
		this.CallDuration = callDuration;
	}

	public void setCallSubTypeName(String callSubTypeName) {
		this.callSubTypeName = callSubTypeName;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public Timestamp getCreatedDate() {
		return createdDate;
	}

//	public BenDetails getBeneficiaryReport() {
//		return beneficiaryReport;
//	}
//
//	public void setBeneficiaryReport(BenDetails beneficiaryReport) {
//		this.beneficiaryReport = beneficiaryReport;
//	}

	public void setCreatedDate(Timestamp createdDate) {
		this.createdDate = createdDate;
	}

	public void setAgentID(String agentID) {
		this.agentID = agentID;
	}

	public String getReceivedRoleName() {
		return receivedRoleName;
	}

	public void setReceivedRoleName(String receivedRoleName) {
		this.receivedRoleName = receivedRoleName;
	}
	
	@Column(name="RecordingFilePath")
	private String recordingFilePath;


	public String getRecordingFilePath() {
		return recordingFilePath;
	}

	public void setRecordingFilePath(String recordingFilePath) {
		this.recordingFilePath = recordingFilePath;
	}

	public Long getBenCallID() {
		return benCallID;
	}

	public String getCallID() {
		return callID;
	}

	public void setCallID(String callID) {
		this.callID = callID;
	}



	public String getCallDuartion() {
		return callDuartion;
	}

	public void setCallDuartion(String callDuartion) {
		this.callDuartion = callDuartion;
	}

//	public DimUserReport getUserReportObj() {
//		return userReportObj;
//	}
//
//	public void setUserReportObj(DimUserReport userReportObj) {
//		this.userReportObj = userReportObj;
//	}
	
	

}
