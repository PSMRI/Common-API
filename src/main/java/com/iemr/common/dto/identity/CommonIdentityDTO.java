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
package com.iemr.common.dto.identity;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

import com.google.gson.annotations.Expose;

import lombok.Data;

@Data
public class CommonIdentityDTO {

	private String eventTypeName; // created, reserved, registered, modified,
									// deleted
	private Timestamp eventTypeDate;
	private Integer agentId;
	private String agentName;
	private Integer agentPSMapId; // ProviderServiceMapId
	private String agentComment;
	private Integer serviceId;
	private String serviceName;
	private Integer serviceProviderId;
	private String serviceProviderName;
	private Integer stateId;
	private String stateName;
	private Integer providerServiceMapId;

	// details
	private Integer areaId;
	private Integer beneficiaryId;
	private Integer beneficiaryRegId;
	private Integer communityId;
	private String community;
	private Timestamp dob;
	private Integer ageAtMarriage;
	private Integer educationId;
	private String education;
	private Boolean emergencyRegistration = false;
	private Integer healthCareWorkerId;
	private String healthCareWorker;
	private String fatherName;
	private String firstName;
	private Integer genderId;
	private String gender;
	private Integer incomeStatusId;
	private String incomeStatus;
	private String lastName;
	private Integer maritalStatusId;
	private String middleName;
	private Integer occupationId;
	private String occupationName;
	private Integer phcId;
	private Integer parkingPlaceId;
	private String placeOfWork;
	private Integer preferredLanguageId;
	private String preferredLanguage;
	private Integer religionId;
	private String religion;
	private String remarks;
	private Integer servicePointId;
	private String sourceOfInfo;
	private String spouseName;
	private Integer statusId;
	private String status;
	private Integer titleId;
	private String title;
	private Integer zoneId;
	private String monthlyFamilyIncome;
	private List<Float> faceEmbedding;

	// address
	private Address currentAddress;
	private Address permanentAddress;
	private Address emergencyAddress;
	private Boolean isPermAddrSameAsCurrAddr;
	private Boolean isPermAddrSameAsEmerAddr;
	private Boolean isEmerAddrSameAsCurrAddr;
	private Boolean isEmerAddrSameAsPermAddr;
	private String addressType;

	// contacts
	private String preferredEmailId;
	private Contact contact;

	// family tagging -- changed to List for considering more than one family
	// member during creation
	Set<BenFamilyDTO> benFamilyDTOs;
	// private Boolean isEmergencyContact;
	// private String relationshipToSelf;
	// private Integer associatedBenRegId;
	// private Integer relationshipId;

	// identities
	private List<Identity> identities;

	// Start Outreach
	// New columns added for MMU integration 09-04-2018

	private String bankName;
	private String branchName;
	private String ifscCode;
	private String accountNo;
	private String benImage;

	private String motherName;
	private String maritalStatus;
	private Timestamp marriageDate;
	private String literacyStatus;
	// End Outreach

	// Start 1097
	private String isHIVPositive;
	private Integer sexualOrientationID;
	private String sexualOrientationType;

	// End 1097

	// new column added for data sync
	// 17-06-2018
	@Expose
	private Integer vanID;

	@Expose
	private Timestamp createdDate;

	// END OF new column added for data sync

	@Expose
	private Boolean beneficiaryConsent;
	private String otherFields;
	private String religionName;

	// private Object otherFields;

	public void setDobFromAge(Integer age) {
		if (dob == null && age != null) {
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.YEAR, -1 * age);
			dob = new Timestamp(cal.getTime().getTime());
		}
	}
}
