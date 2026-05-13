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

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.iemr.common.model.beneficiary.AbhaAddressDTO;

import lombok.Data;

@Data
public class BeneficiariesDTO {
	private BigInteger benMapId; //
	private String benId;
	private BigInteger benRegId;
	private String createdBy; //
	private Timestamp createdDate; //
	// private Boolean deleted; //
	private Timestamp lastModDate; //
	private String modifiedBy; //
	private Address currentAddress; //
	private Address permanentAddress; //
	private Address emergencyAddress; //
	private String preferredPhoneNum; //
	private String preferredPhoneTyp; //
	private String preferredSMSPhoneNum; //
	private String preferredSMSPhoneTyp; //
	private String emergencyContactNum; //
	private String emergencyContactTyp; //
	private String preferredEmailId; //
	private BenDetailDTO beneficiaryDetails;
	private List<BenFamilyDTO> beneficiaryFamilyTags;
	// private List<BenFamilyDTO> benFamilyDTOs;
	private List<BenIdentityDTO> beneficiaryIdentites;
	private List<BenServiceDTO> beneficiaryServiceMap;
	private List<Phone> contacts;

	// Start Outreach
	// New columns added for MMU integration 09-04-2018
	private Timestamp marriageDate;
	private Integer ageAtMarriage;
	private String literacyStatus;
	private String motherName;
	private String email;
	private String bankName;
	private String branchName;
	private String ifscCode;
	private String accountNo;
	private Long benAccountID;
	private Long benImageID;
	private Integer occupationId;
	private String occupationName;
	private String incomeStatus;
	private BigInteger religionId;
	private String religion;
	private String monthlyFamilyIncome;
	private String otherFields;
	private String genderName;
	private String maritalStatusName;
	private String community;
	// End Outreach

	// Start 1097
	private Integer beneficiaryAge;
	// end 1097

	// ABHA address
	List<AbhaAddressDTO> abhaDetails;

	private String economicStatus;

	private Integer economicStatusId;

	private String  residentialArea;

	private Integer  residentialAreaId;

	private String address;
}
