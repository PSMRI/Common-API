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
package com.iemr.common.mapper;

import org.springframework.beans.factory.annotation.Autowired;

import com.iemr.common.dto.identity.Address;
import com.iemr.common.dto.identity.CommonIdentityDTO;
import com.iemr.common.dto.identity.Contact;
import com.iemr.common.dto.identity.Identity;
import com.iemr.common.model.beneficiary.BeneficiaryDemographicsModel;
import com.iemr.common.model.beneficiary.BeneficiaryModel;

public abstract class CommonIdentityMapperDecorator implements CommonIdentityMapper {
	@Autowired
	StateMapper stateMapper;
	@Autowired
	DistrictMapper districtMapper;
	@Autowired
	DistrictBlockMapper blockMapper;
	@Autowired
	DistrictBranchMapper branchMapper;
	@Autowired
	EducationMapper educationMapper;
	@Autowired
	CommunityMapper communityMapper;
	@Autowired
	LanguageMapper languageMapper;
	@Autowired
	OccupationMapper occupationMapper;
	@Autowired
	StatusMapper statusMapper;
	@Autowired
	HealthCareWorkerMapper healthCareWorkerMapper;
	@Autowired
	SexualOrientationMapper sexualOrientationMapper;
	@Autowired
	GenderMapper genderMapper;
	@Autowired
	ReligionMapper religionMapper;
	@Autowired
	TitleMapper titleMapper;
	@Autowired
	IncomeStatusMapper incomeStatusMapper;
	@Autowired
	MaritalStatusMapper maritalStatusMapper;

	public CommonIdentityDTO beneficiaryModelCommonIdentityDTO(BeneficiaryModel beneficiary) {
		if (beneficiary == null) {
			return null;
		}
		CommonIdentityDTO commonIdentityDTO = new CommonIdentityDTO();
		commonIdentityDTO
				.setPermanentAddress(beneficiaryDemographicsModelToAddress(beneficiary.getI_bendemographics()));
		commonIdentityDTO.setCurrentAddress(beneficiaryDemographicsModelToAddress(beneficiary.getI_bendemographics()));
		commonIdentityDTO
				.setEmergencyAddress(beneficiaryDemographicsModelToAddress(beneficiary.getI_bendemographics()));
		commonIdentityDTO.setPlaceOfWork(beneficiary.getPlaceOfWork());
		commonIdentityDTO.setMarriageDate(beneficiary.getMarriageDate());
		commonIdentityDTO.setIsHIVPositive(beneficiary.getIsHIVPos());
		commonIdentityDTO.setLastName(beneficiary.getLastName());
		commonIdentityDTO.setSourceOfInfo(beneficiary.getSourceOfInformation());
		commonIdentityDTO.setIfscCode(beneficiary.getIfscCode());
		commonIdentityDTO.setBenImage(beneficiary.getBenImage());
		commonIdentityDTO.setSpouseName(beneficiary.getSpouseName());
		commonIdentityDTO.setCreatedDate(beneficiary.getCreatedDate());
		commonIdentityDTO.setPreferredEmailId(beneficiary.getEmail());
		commonIdentityDTO.setFatherName(beneficiary.getFatherName());
		commonIdentityDTO.setAgeAtMarriage(beneficiary.getAgeAtMarriage());
		if (beneficiary.getBeneficiaryRegID() != null) {
			commonIdentityDTO.setBeneficiaryRegId(beneficiary.getBeneficiaryRegID().intValue());
		}
		commonIdentityDTO.setLiteracyStatus(beneficiary.getLiteracyStatus());
		commonIdentityDTO.setMotherName(beneficiary.getMotherName());
		commonIdentityDTO.setAgentName(beneficiary.getCreatedBy());
		commonIdentityDTO.setFirstName(beneficiary.getFirstName());
		commonIdentityDTO.setBankName(beneficiary.getBankName());
		commonIdentityDTO.setAccountNo(beneficiary.getAccountNo());
		if (beneficiary.getBeneficiaryID() != null) {
			commonIdentityDTO.setBeneficiaryId(Integer.parseInt(beneficiary.getBeneficiaryID()));
		}
		commonIdentityDTO.setBranchName(beneficiary.getBranchName());
		commonIdentityDTO.setMiddleName(beneficiary.getMiddleName());
		commonIdentityDTO.setRemarks(beneficiary.getRemarks());
		commonIdentityDTO.setDob(beneficiary.getDOB());
		commonIdentityDTO.setDobFromAge(beneficiary.getAge());
		commonIdentityDTO.setIdentities(
				Identity.createIdentity(beneficiary.getBeneficiaryIdentities(), beneficiary.getCreatedBy()));
		commonIdentityDTO.setContact(Contact.addContacts(beneficiary.getBenPhoneMaps()));

		Integer educationID = beneficiary.getI_bendemographics().getEducationID();
		if (educationID != null) {
			commonIdentityDTO.setEducationId(educationID);
			commonIdentityDTO.setEducation(
					educationMapper.educationToModelByID(Long.parseLong(educationID + "")).getEducationType());
		}
		Integer occupationID = beneficiary.getI_bendemographics().getOccupationID();
		if (occupationID != null) {
			commonIdentityDTO.setOccupationId(occupationID);
			commonIdentityDTO.setOccupationName(occupationMapper.occupationToModelByID(occupationID).getOccupationType());
		}
		Integer communityID = beneficiary.getI_bendemographics().getCommunityID();
		if (communityID != null) {
			commonIdentityDTO.setCommunityId(communityID);
			commonIdentityDTO
					.setCommunity(communityMapper.communityToLoginResponseByID(communityID).getCommunityType());
		}
		Short statusID = beneficiary.getStatusID();
		if (beneficiary.getStatusID() != null) {
			commonIdentityDTO.setStatusId(beneficiary.getStatusID().intValue());
			commonIdentityDTO.setStatus(statusMapper.statusToModelByID(Integer.parseInt(statusID + "")).getStatus());
		}
		Short healthCareWorkerID = beneficiary.getI_bendemographics().getHealthCareWorkerID();
		if (healthCareWorkerID != null) {
			commonIdentityDTO.setHealthCareWorkerId(healthCareWorkerID.intValue());
			commonIdentityDTO.setHealthCareWorker(
					healthCareWorkerMapper.getModelByWorkerID(healthCareWorkerID).getHealthCareWorkerType());
		}
		Short sexualOrientationID = beneficiary.getSexualOrientationID();
		if (sexualOrientationID != null) {
			commonIdentityDTO.setSexualOrientationID(Integer.parseInt(sexualOrientationID + ""));
			commonIdentityDTO.setSexualOrientationType(
					sexualOrientationMapper.sexualOrientationByIDToModel(sexualOrientationID).getSexualOrientation());
		}
		Integer genderID = beneficiary.getGenderID();
		if (genderID != null) {
			commonIdentityDTO.setGenderId(genderID);
			commonIdentityDTO.setGender(genderMapper.genderByIDToLoginResponse(genderID).getGenderName());
		}
		Integer relegionID = beneficiary.getI_bendemographics().getReligionID();
		if (relegionID != null) {
			commonIdentityDTO.setReligionId(relegionID);
			if (beneficiary.getI_bendemographics().getReligionName() != null)
				commonIdentityDTO.setReligion(beneficiary.getI_bendemographics().getReligionName());
			else
				commonIdentityDTO.setReligion(religionMapper.relegionToModelByID(relegionID).getReligionType());
		}
		Short titleID = beneficiary.getTitleId();
		if (titleID != null) {
			commonIdentityDTO.setTitleId(titleID.intValue());
			commonIdentityDTO.setTitle(titleMapper.titleByIDToResponse(titleID.intValue()).getTitleName());
		}
		Integer incomeStatusID = beneficiary.getI_bendemographics().getIncomeStatusID();
		if (incomeStatusID != null) {
			commonIdentityDTO.setIncomeStatusId(incomeStatusID);
			commonIdentityDTO
					.setIncomeStatus(incomeStatusMapper.incomeStatusByIDToModel(incomeStatusID).getIncomeStatus());
		}
		Short maritalStatusID = beneficiary.getMaritalStatusID();
		if (maritalStatusID != null) {
			commonIdentityDTO.setMaritalStatusId(maritalStatusID.intValue());
			var model = maritalStatusMapper.maritalStatusByIDToResponse(maritalStatusID.intValue());
			if (model != null) {
				commonIdentityDTO.setMaritalStatus(model.getStatus());
			} else {
				// Option 1: Return null
				commonIdentityDTO.setMaritalStatus(null);
				// Option 2: Or set a default
				// commonIdentityDTO.setMaritalStatus("Unknown");
				// Option 3: Or log a warning
				// logger.warn("Invalid maritalStatusID: {}", maritalStatusID);
			}
		}
		commonIdentityDTO.setMaritalStatus(beneficiary.getMaritalStatusName());
		commonIdentityDTO.setGender(beneficiary.getGenderName());
		if(beneficiary.getI_bendemographics() != null && beneficiary.getI_bendemographics().getOccupationName() != null)
			commonIdentityDTO.setOccupationName(beneficiary.getI_bendemographics().getOccupationName());
		Integer preferredLangID = beneficiary.getI_bendemographics().getPreferredLangID();
		if (preferredLangID != null) {
			commonIdentityDTO.setPreferredLanguageId(preferredLangID);
			commonIdentityDTO
					.setPreferredLanguage(languageMapper.languageToModelByID(preferredLangID).getLanguageName());
		}
		commonIdentityDTO.setProviderServiceMapId(beneficiary.getProviderServiceMapID());

		// new property mapping for data sync
		// 17-06-2018
		commonIdentityDTO.setVanID(beneficiary.getVanID());
		commonIdentityDTO.setParkingPlaceId(beneficiary.getParkingPlaceID());

		commonIdentityDTO.setBeneficiaryConsent(beneficiary.getBeneficiaryConsent());
		if (beneficiary.getI_bendemographics() != null
				&& beneficiary.getI_bendemographics().getMonthlyFamilyIncome() != null) {
			commonIdentityDTO.setMonthlyFamilyIncome(beneficiary.getI_bendemographics().getMonthlyFamilyIncome());
		}
		if (beneficiary.getOtherFields() != null) {
			commonIdentityDTO.setOtherFields(beneficiary.getOtherFields());
		}
		// End
		return commonIdentityDTO;
	}

	protected Address beneficiaryDemographicsModelToAddress(BeneficiaryDemographicsModel beneficiaryDemographicsModel) {
		if (beneficiaryDemographicsModel == null) {
			return null;
		}

		Address address = new Address();

		address.setVillageId(beneficiaryDemographicsModel.getDistrictBranchID());
		address.setZoneID(beneficiaryDemographicsModel.getZoneID());
		address.setParkingPlaceName(beneficiaryDemographicsModel.getParkingPlaceName());
		address.setStateId(beneficiaryDemographicsModel.getStateID());
		address.setDistrictId(beneficiaryDemographicsModel.getDistrictID());
		if (beneficiaryDemographicsModel.getDistrictID() != null) {
			address.setDistrict(
					districtMapper.districtToModelByID(beneficiaryDemographicsModel.getDistrictID()).getDistrictName());
		}
		address.setServicePointName(beneficiaryDemographicsModel.getServicePointName());
		address.setCountry(beneficiaryDemographicsModel.getCountryName());
		if (beneficiaryDemographicsModel.getStateID() != null) {
			address.setState(stateMapper.StateToModelByID(beneficiaryDemographicsModel.getStateID()).getStateName());
		}
		address.setZoneName(beneficiaryDemographicsModel.getZoneName());
		address.setHabitation(beneficiaryDemographicsModel.getHabitation());
		address.setAddrLine1(beneficiaryDemographicsModel.getAddressLine1());
		address.setAddrLine2(beneficiaryDemographicsModel.getAddressLine2());
		address.setAddrLine3(beneficiaryDemographicsModel.getAddressLine3());
		address.setPinCode(beneficiaryDemographicsModel.getPinCode());
		address.setParkingPlaceID(beneficiaryDemographicsModel.getParkingPlaceID());
		address.setServicePointID(beneficiaryDemographicsModel.getServicePointID());
		if (beneficiaryDemographicsModel.getDistrictBranchID() != null) {
			address.setVillage(branchMapper
					.districtBranchToModelByID(beneficiaryDemographicsModel.getDistrictBranchID()).getVillageName());
		}
		address.setSubDistrictId(beneficiaryDemographicsModel.getBlockID());
		address.setCountryId(beneficiaryDemographicsModel.getCountryID());
		if (beneficiaryDemographicsModel.getBlockID() != null) {
			address.setSubDistrict(
					blockMapper.districtBlockToModelByID(beneficiaryDemographicsModel.getBlockID()).getBlockName());
		}

		return address;
	}
}
