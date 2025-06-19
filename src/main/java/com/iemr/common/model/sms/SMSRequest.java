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
package com.iemr.common.model.sms;

import java.sql.Timestamp;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.iemr.common.data.telemedicine.PrescribedDrugDetail;

import lombok.Data;

@Data
public class SMSRequest {
	private Integer providerServiceMapID;
	private Integer smsTemplateTypeID;
	private Integer smsTemplateID;
	private Long beneficiaryRegID;
	private Integer stateID;
	private Integer districtID;
	private Integer blockID;
	private Boolean is1097 = false;
	private Long userID;
	private Integer instituteID;
	private Long feedbackID;
	private String createdBy;
	private String alternateNo;
	private String smsText;
	private Long prescribedDrugID;
	private Long bloodReqID;
	private Long directoryServiceID;
	private Long fSComplaintID;
	private String requestID;
	private Long outbreakComplaintID;
	private Long obCallID;
	private Boolean isBloodBankSMS = false;
	private String moAdvice;
	private Long organDonationID;
	private Long requestedBloodBankID;
	private Long requestedInstitutionID;

	private Integer specializationID;
	private String smsTypeTM;
	private String tcDate;
	private String tcPreviousDate;
	private List<PrescribedDrugDetail> presObj;
	private List<Object> diagnosis;
	private Long benHistoryID;	
	private String imrDate;
	private String imrID;
	private String nodalNumber;
	private String informerName;
	private String choName;
	private String facilityPhoneNo;
	private String benPhoneNo;
	private String employeeCode;
	private String facilityName;
	private String hfrId;
	private String beneficiaryName;
	private Long beneficiaryId;
	private String appointmentDate;
	private String appointmentTime;
	@JsonProperty("sms_Advice")
	private String smsAdvice;  
}
