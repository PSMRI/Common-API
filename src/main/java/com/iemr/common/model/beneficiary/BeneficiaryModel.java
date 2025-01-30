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
package com.iemr.common.model.beneficiary;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import org.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.iemr.common.model.user.TitleModel;
import com.iemr.common.model.userbeneficiary.BeneficiaryIdentityModel;
import com.iemr.common.model.userbeneficiary.GenderModel;
import com.iemr.common.model.userbeneficiary.GovtIdentityTypeModel;
import com.iemr.common.model.userbeneficiary.MaritalStatusModel;
import com.iemr.common.model.userbeneficiary.SexualOrientationModel;
import com.iemr.common.model.userbeneficiary.StatusModel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BeneficiaryModel implements Comparable<BeneficiaryModel> {
	private static final Integer DAYS_IN_YEAR = 365;
	private static final Integer DAYS_IN_MONTH = 30;
	private static final Integer HOURS_IN_DAY = 24;
	private static final Integer MINUTES_IN_HOUR = 60;
	private static final Integer SECONDS_IN_MINUTE = 60;
	private static final Integer MILLIS_IN_SECOND = 1000;
	private static final Long DAY_IN_MILLIS = HOURS_IN_DAY * MINUTES_IN_HOUR * SECONDS_IN_MINUTE
			* MILLIS_IN_SECOND.longValue();
	private static final Long MONTH_IN_MILLIS = DAYS_IN_MONTH * HOURS_IN_DAY * MINUTES_IN_HOUR * SECONDS_IN_MINUTE
			* MILLIS_IN_SECOND.longValue();
	private static final Long YEAR_IN_MILLIS = DAYS_IN_YEAR * HOURS_IN_DAY * MINUTES_IN_HOUR * SECONDS_IN_MINUTE
			* MILLIS_IN_SECOND.longValue();
	@Expose
	private Long beneficiaryRegID;
	@Expose
	private BeneficiaryDemographicsModel i_bendemographics;
	// private List<BenMedHistoryModel> t_benmedhistory;
	@Expose
	private List<BenPhoneMapModel> benPhoneMaps;
	@Expose
	private List<BenPhoneMapModel> parentBenPhoneMaps;
	// private List<OutboundCallRequestModel> outboundCallRequests;
	// private List<BeneficiaryCallModel> beneficiaryCalls;
	// private List<FeedbackDetailsModel> feedbacks;
	@Expose
	private String beneficiaryID;
	@Expose
	private Short titleId;
	@Expose
	private String titleName;
	@Expose
	private TitleModel m_title;
	@Expose
	private String firstName;
	@Expose
	private String middleName;
	@Expose
	private String lastName;
	@Expose
	private Short statusID;
	@Expose
	private String statusName;
	@Expose
	private StatusModel status;
	@Expose
	private Integer genderID;
	@Expose
	private String genderName;
	@Expose
	private GenderModel m_gender;
	@Expose
	private Short maritalStatusID;
	@Expose
	private String maritalStatusName;
	@Expose
	private MaritalStatusModel maritalStatus;
	@Expose
	private Short sexualOrientationID;
	@Expose
	private String sexualOrientationName;
	@Expose
	private SexualOrientationModel sexualOrientation;
//	@Expose
//	private Timestamp dob;
	@Expose
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private Timestamp dOB;
	@Expose
	private Integer age;
	@Expose
	private String fatherName;
	@Expose
	private String spouseName;
	private SimpleDateFormat formatYear/* = new SimpleDateFormat("yyyy") */;
	@Expose
	private String govtIdentityNo;
	@Expose
	private Integer govtIdentityTypeID;
	@Expose
	private GovtIdentityTypeModel govtIdentityType;
	@Expose
	private Short registeredServiceID;
	@Expose
	private String sourceOfInformation;
	@Expose
	private String isHIVPos;
	@Expose
	private String placeOfWork;
	@Expose
	private String remarks;
	@Expose
	private Boolean changeInSelfDetails = false;
	@Expose
	private Boolean changeInAddress = false;
	@Expose
	private Boolean changeInContacts = false;
	@Expose
	private Boolean changeInIdentities = false;
	@Expose
	private Boolean changeInOtherDetails = false;
	@Expose
	private Boolean changeInFamilyDetails = false;
	@Expose
	private Boolean changeInAssociations = false;
	@Expose
	private Boolean is1097 = false;

	@Expose
	private Boolean deleted;
	@Expose
	private String createdBy;
	@Expose
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private Timestamp createdDate;
	@Expose
	private String modifiedBy;
	@Expose
	private Timestamp lastModDate;

	// Start Outreach
	// New columns added for MMU integration 09-04-2018
	@Expose
	private Boolean changeInBankDetails = false;
	@Expose
	private List<BeneficiaryIdentityModel> beneficiaryIdentities;
	@Expose
	private Timestamp marriageDate;
	@Expose
	private Integer ageAtMarriage;
	@Expose
	private String literacyStatus;
	@Expose
	private String motherName;
	@Expose
	private String email;
	@Expose
	private String bankName;
	@Expose
	private String branchName;
	@Expose
	private String ifscCode;
	@Expose
	private String accountNo;
	@Expose
	private Long benAccountID;

	@Expose
	private Boolean changeInBenImage = false;
	@Expose
	private String benImage;
	@Expose
	private Long benImageId;
	@Expose
	private String phoneNo;

	@Expose
	private Integer occupationId;
	@Expose
	private String occupationName;
	@Expose
	private String occupation;
	@Expose
	private String incomeStatus;
	@Expose
	private BigInteger religionId;
	@Expose
	private String religion;
	@Expose
	private String education;
	@Expose
	private Integer providerServiceMapID;

	@Expose
	private Integer actualAge;
	@Expose
	private String ageUnits;

	// End Outreach

	// new column added for data sync
	// 17-06-2018
	@Expose
	private Integer vanID;
	@Expose
	private Integer parkingPlaceID;

	// new column added for D2D, 21-01-2021
	@Expose
	private Long houseHoldID;
	@Expose
	private String guideLineID;
	@Expose
	private String rchID;

	@Expose
	private Boolean isD2D;

	// END OF new column added for data sync

	// new column added for face recognitiom
	@Expose
	private List<Float> faceEmbedding;
	// END OF new column added for face recognitiom

	// ABHA address
	List<AbhaAddressDTO> abhaDetails;

	@Expose
	private String HealthID;
	@Expose
	private String HealthIDNumber;

	@Expose
	private String familyId;
	@Expose
	private String identity;

	@Expose
	private Integer headOfFamily_RelationID;
	@Expose
	private String other;
	@Expose
	private String headOfFamily_Relation;

	@Expose
	private Boolean beneficiaryConsent;
	
	@Expose
	private String monthlyFamilyIncome;
	@Expose
	private boolean emergencyRegistration;
	@Expose
	private String communityName;
	@Expose
	private Boolean passToNurse = false;
	private String otherFields;
	@Expose
	private String religionName;
	
//	private Object otherFields;

	public static Timestamp getTimestampData(Timestamp timestamp) {
		return timestamp;
	}

	public static Integer setAge(Integer ageFromBenDetails, Integer ageFromBenDTO) {
		if (ageFromBenDetails != null) {
			return ageFromBenDetails;
		} else {
			return ageFromBenDTO;
		}
	}

	public static String ageUnits(Timestamp dob) {
		String beneficiaryAgeUnit = null;
		Date current = new Date();
		if (dob != null) {

			ZoneId defaultZoneId = ZoneId.systemDefault();
			Instant instant1 = dob.toInstant();
			LocalDate localeDateMyDob = instant1.atZone(defaultZoneId).toLocalDate();
			Instant instant2 = current.toInstant();
			LocalDate localeDateCurrent = instant2.atZone(defaultZoneId).toLocalDate();

			Period period = Period.between(localeDateMyDob, localeDateCurrent);
			if (localeDateMyDob.equals(localeDateCurrent)) {
				beneficiaryAgeUnit = "Day";
			} else if (period.getYears() > 0) {
				beneficiaryAgeUnit = "Year" + (period.getYears() > 1 ? "s" : "");
			} else if (period.getMonths() > 0) {
				beneficiaryAgeUnit = "Month" + (period.getMonths() > 1 ? "s" : "");
			} else if (period.getDays() > 0) {
				beneficiaryAgeUnit = "Day" + (period.getDays() > 1 ? "s" : "");
			}
		}
		return beneficiaryAgeUnit;
	}

//	public static Long actualAge(Timestamp dob) {
//		Long beneficiaryAge = null;
//		Date current = new Date();
//		if (dob != null) {
//
//			ZoneId defaultZoneId = ZoneId.systemDefault();
//			Instant instant1 = dob.toInstant();
//			LocalDate localeDateMyDob = instant1.atZone(defaultZoneId).toLocalDate();
//			Instant instant2 = current.toInstant();
//			LocalDate localeDateCurrent = instant2.atZone(defaultZoneId).toLocalDate();
//
//			Period period = Period.between(localeDateMyDob, localeDateCurrent);
//			if (localeDateMyDob.equals(localeDateCurrent)) {
//				beneficiaryAge = 1L;
//			} else if (period.getYears() > 0) {
//				beneficiaryAge = (long) period.getYears();
//			} else if (period.getMonths() > 0) {
//				beneficiaryAge = (long) period.getMonths();
//			} else if (period.getDays() > 0) {
//				beneficiaryAge = (long) period.getDays();
//			}
//
//		}
//		return beneficiaryAge;
//	}
	
	public static Integer actualAge(Timestamp dob) {
	    Integer beneficiaryAge = null;
	    Date current = new Date();
	    if (dob != null) {
	        ZoneId defaultZoneId = ZoneId.systemDefault();
	        Instant instant1 = dob.toInstant();
	        LocalDate localeDateMyDob = instant1.atZone(defaultZoneId).toLocalDate();
	        Instant instant2 = current.toInstant();
	        LocalDate localeDateCurrent = instant2.atZone(defaultZoneId).toLocalDate();

	        Period period = Period.between(localeDateMyDob, localeDateCurrent);
	        if (localeDateMyDob.equals(localeDateCurrent)) {
	            beneficiaryAge = 1;
	        } else if (period.getYears() > 0) {
	            beneficiaryAge = period.getYears();
	        } else if (period.getMonths() > 0) {
	            beneficiaryAge = period.getMonths();
	        } else if (period.getDays() > 0) {
	            beneficiaryAge = period.getDays();
	        }
	    }
	    return beneficiaryAge;
	}

//Age Calculation-Old

//	public static String ageUnits(Timestamp dOB) {
//		String ageUnits = null;
//		if (dOB != null) {
//			Calendar cal = Calendar.getInstance();
//			Long ageInMillis = cal.getTime().getTime() - dOB.getTime();
//			if (ageInMillis > YEAR_IN_MILLIS) {
//				ageUnits = "year" + ((ageInMillis / YEAR_IN_MILLIS) > 1 ? "s" : "");
//			} else if (ageInMillis > MONTH_IN_MILLIS) {
//				ageUnits = "month" + ((ageInMillis / MONTH_IN_MILLIS) > 1 ? "s" : "");
//			} else if (ageInMillis > DAY_IN_MILLIS) {
//				ageUnits = "day" + ((ageInMillis / DAY_IN_MILLIS) > 1 ? "s" : "");
//			}
//		}
//		return ageUnits;
//	}
//	

//	public static Long actualAge(Timestamp dOB) {
//		Long actualAge = null;
//		if (dOB != null) {
//			Calendar cal = Calendar.getInstance();
//			Long ageInMillis = cal.getTime().getTime() - dOB.getTime();
//			if (ageInMillis > YEAR_IN_MILLIS) {
//				actualAge = ageInMillis / YEAR_IN_MILLIS;
//			} else if (ageInMillis > MONTH_IN_MILLIS) {
//				actualAge = ageInMillis / MONTH_IN_MILLIS;
//			} else if (ageInMillis > DAY_IN_MILLIS) {
//				actualAge = ageInMillis / DAY_IN_MILLIS;
//			}
//		}
//		return actualAge;
//	}

	@Override
	public int compareTo(BeneficiaryModel ben) {
		return this.beneficiaryRegID.compareTo(ben.beneficiaryRegID);
	}
}
