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
package com.iemr.common.data.users;

import java.sql.Timestamp;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.annotations.Expose;
import com.iemr.common.data.callhandling.OutboundCallRequest;
import com.iemr.common.data.feedback.FeedbackDetails;
import com.iemr.common.data.institute.Designation;
import com.iemr.common.data.userbeneficiarydata.Gender;
import com.iemr.common.data.userbeneficiarydata.MaritalStatus;
import com.iemr.common.data.userbeneficiarydata.Status;
import com.iemr.common.data.userbeneficiarydata.Title;
import com.iemr.common.utils.mapper.OutputMapper;

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
import jakarta.persistence.Transient;
import lombok.Data;

@Entity
@Table(name = "m_user")
@Data
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Expose
	@Column(name = "UserID")
	private Long userID;
	@OneToMany(mappedBy = "m_user", fetch = FetchType.EAGER)
	@Expose
	@Transient
	private List<UserServiceRoleMapping> m_UserServiceRoleMapping;

	@OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
	@Transient
	@Expose
	private Set<OutboundCallRequest> outboundCallRequests;

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "language")
	@Transient
	private Set<UserServiceRoleMapping> roleMappings;

	@Expose
	// @Transient
	@OneToMany(/* mappedBy = "m_user", fetch = FetchType.EAGER */)
	@JoinColumn(updatable = false, insertable = false, name = "userID", referencedColumnName = "userID")
	private Set<UserLangMapping> m_UserLangMappings;

	@OneToMany(mappedBy = "mUser", fetch = FetchType.EAGER)
	@Transient
	@Expose
	private Set<FeedbackDetails> feedbackDetails;

	@Expose
	@Column(name = "TitleID")
	private Integer titleID;
	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "TitleID", insertable = false, updatable = false)
	@JsonIgnore
	@Expose
	private Title m_title;
	@Expose
	@Column(name = "FirstName")
	private String firstName;
	@Expose
	@Column(name = "MiddleName")
	private String middleName;
	@Expose
	@Column(name = "LastName")
	private String lastName;

	@Expose
	@Column(name = "GenderID")
	private Integer genderID;
	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "GenderID", insertable = false, updatable = false)
	@JsonIgnore
	@Expose
	private Gender m_gender;

	@Expose
	@Column(name = "MaritalStatusID")
	private Integer maritalStatusID;
	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "maritalStatusID", insertable = false, updatable = false)
	@JsonIgnore
	@Expose
	private MaritalStatus m_maritalstatus;

	@Expose
	@Column(name = "StatusID")
	private Integer statusID;
	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "StatusID", insertable = false, updatable = false)
	@JsonIgnore
	@Expose
	private Status m_status;

	@Expose
	@Column(name = "AadhaarNo")
	private String aadhaarNo;
	@Expose
	@Column(name = "PAN")
	private String pAN;
	@Expose
	@Column(name = "DOB")
	private Timestamp dOB;
	@Expose
	@Column(name = "DOJ")
	private Timestamp dOJ;
	@Expose
	@Column(name = "QualificationID")
	private Integer qualificationID;
	@Expose
	@Column(name = "userName")
	private String userName;
	// @Expose
	@Column(name = "Password")
	private String password;
	@Expose
	@Column(name = "EmailID")
	private String emailID;

	@Expose
	@Column(name = "EmergencyContactPerson")
	private String emergencyContactPerson;
	@Expose
	@Column(name = "EmergencyContactNo")
	private String emergencyContactNo;
	@Expose
	@Column(name = "IsSupervisor")
	private Boolean isSupervisor;
	@Expose
	@Column(name = "Deleted", insertable = false, updatable = true)
	private Boolean deleted;
	@Expose
	@Column(name = "CreatedBy")
	private String createdBy;
	@Column(name = "CreatedDate", insertable = false, updatable = false)
	private Timestamp createdDate;
	@Column(name = "ModifiedBy")
	private String modifiedBy;
	@Column(name = "LastModDate", insertable = false, updatable = false)
	private Timestamp lastModDate;
	@Expose
	@Column(name = "AgentID")
	private String agentID;

	@Expose
	@Column(name = "DesignationID")
	private Integer designationID;
	@JoinColumn(name = "DesignationID", insertable = false, updatable = false)
	@Expose
	@OneToOne(fetch = FetchType.EAGER)
	private Designation designation;

	@Expose
	@Column(name = "AgentPassword")
	private String agentPassword;

	@Transient
	private String newPassword = null;

	@Transient
	private OutputMapper outPutMapper = new OutputMapper();

	// new field for rate-limit, failed authentication
	@Expose
	@Column(name = "failed_attempt")
	private Integer failedAttempt;

	/*
	 * protected User() { }
	 */

	public static User initializeUsers(Long userID, Integer titleID, String firstName, String middleName,
			String lastName, Integer genderID, Integer maritalStatusID, String aadhaarNo, String pAN, Timestamp dOB,
			Timestamp dOJ, Integer qualificationID, String userName, String password, String emailID, Status m_Status,
			List<UserServiceRoleMapping> m_UserServiceRoleMapping, String emergencyContactPerson,
			String emergencyContactNo, Boolean isSupervisor, Boolean deleted, String createdBy, Timestamp createdDate,
			String modifiedBy, Timestamp lastModDate, String newPassword) {
		User user = new User();
		user.userID = userID;
		user.titleID = titleID;
		user.firstName = firstName;
		user.middleName = middleName;
		user.lastName = lastName;
		user.genderID = genderID;
		user.maritalStatusID = maritalStatusID;
		user.aadhaarNo = aadhaarNo;
		user.pAN = pAN;
		user.dOB = dOB;
		user.dOJ = dOJ;
		user.qualificationID = qualificationID;
		user.userName = userName;
		user.password = password;
		user.emailID = emailID;
		user.m_UserServiceRoleMapping = m_UserServiceRoleMapping;
		user.emergencyContactPerson = emergencyContactPerson;
		user.emergencyContactNo = emergencyContactNo;
		user.isSupervisor = isSupervisor;
		user.deleted = deleted;
		user.createdBy = createdBy;
		user.createdDate = createdDate;
		user.modifiedBy = modifiedBy;
		user.lastModDate = lastModDate;
		user.newPassword = newPassword;
		return user;
	}

	public Long getUserID() {
		return userID;
	}

	public void setUserID(Long userID) {
		this.userID = userID;
	}

//	public List<UserServiceRoleMapping> getM_UserServiceRoleMapping()
//	{
//		return m_UserServiceRoleMapping;
//	}
//
//	public void setM_UserServiceRoleMapping(List<UserServiceRoleMapping> m_UserServiceRoleMapping)
//	{
//		this.m_UserServiceRoleMapping = m_UserServiceRoleMapping;
//	}

	public Integer getTitleID() {
		return titleID;
	}

	public void setTitleID(Integer titleID) {
		this.titleID = titleID;
	}

//	public Title getM_title()
//	{
//		return m_title;
//	}
//
//	public void setM_title(Title m_title)
//	{
//		this.m_title = m_title;
//	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getMiddleName() {
		return middleName;
	}

	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public Integer getGenderID() {
		return genderID;
	}

	public void setGenderID(Integer genderID) {
		this.genderID = genderID;
	}

//	public Gender getM_gender()
//	{
//		return m_gender;
//	}
//
//	public void setM_gender(Gender m_gender)
//	{
//		this.m_gender = m_gender;
//	}

	public Integer getMaritalStatusID() {
		return maritalStatusID;
	}

	public void setMaritalStatusID(Integer maritalStatusID) {
		this.maritalStatusID = maritalStatusID;
	}

//	public MaritalStatus getM_maritalstatus()
//	{
//		return m_maritalstatus;
//	}
//
//	public void setM_maritalstatus(MaritalStatus m_maritalstatus)
//	{
//		this.m_maritalstatus = m_maritalstatus;
//	}

	public Integer getStatusID() {
		return statusID;
	}

	public void setStatusID(Integer statusID) {
		this.statusID = statusID;
	}

//	public Status getM_status()
//	{
//		return m_status;
//	}
//
//	public void setM_status(Status m_status)
//	{
//		this.m_status = m_status;
//	}

	public String getAadhaarNo() {
		return aadhaarNo;
	}

	public void setAadhaarNo(String aadhaarNo) {
		this.aadhaarNo = aadhaarNo;
	}

	public String getpAN() {
		return pAN;
	}

	public void setpAN(String pAN) {
		this.pAN = pAN;
	}

	public Timestamp getdOB() {
		return dOB;
	}

	public void setdOB(Timestamp dOB) {
		this.dOB = dOB;
	}

	public Timestamp getdOJ() {
		return dOJ;
	}

	public void setdOJ(Timestamp dOJ) {
		this.dOJ = dOJ;
	}

	public Integer getQualificationID() {
		return qualificationID;
	}

	public void setQualificationID(Integer qualificationID) {
		this.qualificationID = qualificationID;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmailID() {
		return emailID;
	}

	public void setEmailID(String emailID) {
		this.emailID = emailID;
	}

	public String getEmergencyContactPerson() {
		return emergencyContactPerson;
	}

	public void setEmergencyContactPerson(String emergencyContactPerson) {
		this.emergencyContactPerson = emergencyContactPerson;
	}

	public String getEmergencyContactNo() {
		return emergencyContactNo;
	}

	public void setEmergencyContactNo(String emergencyContactNo) {
		this.emergencyContactNo = emergencyContactNo;
	}

	public Boolean getIsSupervisor() {
		return isSupervisor;
	}

	public void setIsSupervisor(Boolean isSupervisor) {
		this.isSupervisor = isSupervisor;
	}

	public Boolean getDeleted() {
		return deleted;
	}

	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public Timestamp getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Timestamp createdDate) {
		this.createdDate = createdDate;
	}

	public String getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public Timestamp getLastModDate() {
		return lastModDate;
	}

	public void setLastModDate(Timestamp lastModDate) {
		this.lastModDate = lastModDate;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

	public OutputMapper getOutPutMapper() {
		return outPutMapper;
	}

	public void setOutPutMapper(OutputMapper outPutMapper) {
		this.outPutMapper = outPutMapper;
	}

	public String getAgentID() {
		return agentID;
	}

	public String getAgentPassword() {
		return agentPassword;
	}

//	public Set<UserLangMapping> getM_UserLangMappings()
//	{
//		return m_UserLangMappings;
//	}

	@Override
	public String toString() {
		return OutputMapper.gson().toJson(this);
	}

	public Integer getDesignationID() {
		return designationID;
	}

	public Designation getDesignation() {
		return designation;
	}

	/*
	 * public User(String userName, String password) { this.userName = userName;
	 * this.password = password; }
	 */
}
