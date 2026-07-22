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

import java.math.BigInteger;

import org.springframework.beans.factory.annotation.Autowired;

import com.iemr.common.dto.identity.Address;
import com.iemr.common.dto.identity.BenFamilyDTO;
import com.iemr.common.dto.identity.Contact;
import com.iemr.common.dto.identity.Identity;
import com.iemr.common.dto.identity.IdentityEditDTO;
import com.iemr.common.model.beneficiary.BenPhoneMapModel;
import com.iemr.common.model.beneficiary.BeneficiaryDemographicsModel;
import com.iemr.common.model.beneficiary.BeneficiaryModel;

public abstract class IdentityBenEditMapperDecorator implements IdentityBenEditMapper {
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
		address.setGpsLatitude(beneficiaryDemographicsModel.getLatitude());
		address.setGpsLongitude(beneficiaryDemographicsModel.getLongitude());
		address.setDigipin(beneficiaryDemographicsModel.getDigipin());
		address.setGpsTimestamp(beneficiaryDemographicsModel.getGpsTimestamp());
		address.setIsGpsUnavailable(beneficiaryDemographicsModel.getIsGpsUnavailable());
		address.setGpsUnavailableReason(beneficiaryDemographicsModel.getGpsUnavailableReason());

		return address;
	}

	@Override
	public IdentityEditDTO BenToIdentityEditMapper(BeneficiaryModel beneficiary) {
		if (beneficiary == null) {
			return null;
		}

		IdentityEditDTO identityEditDTO = new IdentityEditDTO();
		identityEditDTO.setPermanentAddress(beneficiaryDemographicsModelToAddress(beneficiary.getI_bendemographics()));
		identityEditDTO.setCurrentAddress(beneficiaryDemographicsModelToAddress(beneficiary.getI_bendemographics()));
		identityEditDTO.setEmergencyAddress(beneficiaryDemographicsModelToAddress(beneficiary.getI_bendemographics()));
		identityEditDTO.setPlaceOfWork(beneficiary.getPlaceOfWork());
		identityEditDTO.setMarriageDate(beneficiary.getMarriageDate());
		identityEditDTO.setIsHIVPositive(beneficiary.getIsHIVPos());
		identityEditDTO.setLastName(beneficiary.getLastName());
		identityEditDTO.setChangeInSelfDetails(beneficiary.getChangeInSelfDetails());
		identityEditDTO.setChangeInAddress(beneficiary.getChangeInAddress());
		identityEditDTO.setChangeInContacts(beneficiary.getChangeInContacts());
		identityEditDTO.setChangeInIdentities(beneficiary.getChangeInIdentities());
		identityEditDTO.setChangeInOtherDetails(beneficiary.getChangeInOtherDetails());
		identityEditDTO.setChangeInFamilyDetails(beneficiary.getChangeInFamilyDetails());
		identityEditDTO.setChangeInAssociations(beneficiary.getChangeInAssociations());
		identityEditDTO.setChangeInBankDetails(beneficiary.getChangeInBankDetails());
		identityEditDTO.setChangeInBenImage(beneficiary.getChangeInBenImage());
		identityEditDTO.setSourceOfInformation(beneficiary.getSourceOfInformation());
		identityEditDTO.setBenFamilyDTOs(BenFamilyDTO.benFamilyFromBenPhoneMapList(beneficiary.getBenPhoneMaps()));
		identityEditDTO.setDob(BeneficiaryModel.getTimestampData(beneficiary.getDOB()));
		identityEditDTO.setIdentities(
				Identity.createIdentity(beneficiary.getBeneficiaryIdentities(), beneficiary.getCreatedBy()));
		identityEditDTO.setContact(Contact.addContacts(beneficiary.getBenPhoneMaps()));
		identityEditDTO.setSourceOfInfo(beneficiary.getSourceOfInformation());
		identityEditDTO.setIfscCode(beneficiary.getIfscCode());
		identityEditDTO.setBenImage(beneficiary.getBenImage());
		identityEditDTO.setSpouseName(beneficiary.getSpouseName());
		identityEditDTO.setPreferredEmailId(beneficiary.getEmail());
		identityEditDTO.setFatherName(beneficiary.getFatherName());
		identityEditDTO.setAgeAtMarriage(beneficiary.getAgeAtMarriage());
		identityEditDTO.setLiteracyStatus(beneficiary.getLiteracyStatus());
		identityEditDTO.setMotherName(beneficiary.getMotherName());
		identityEditDTO.setAgentName(beneficiary.getCreatedBy());
		identityEditDTO.setBenImageId(beneficiary.getBenImageId());
		identityEditDTO.setBankName(beneficiary.getBankName());
		identityEditDTO.setBranchName(beneficiary.getBranchName());
		identityEditDTO.setMiddleName(beneficiary.getMiddleName());
		identityEditDTO.setRemarks(beneficiary.getRemarks());
		identityEditDTO.setFirstName(beneficiary.getFirstName());
		identityEditDTO.setAccountNo(beneficiary.getAccountNo());
		if (beneficiary.getBenAccountID() != null) {
			identityEditDTO.setBenAccountID(BigInteger.valueOf(beneficiary.getBenAccountID()));
		}
		if (beneficiary.getBeneficiaryID() != null) {
			identityEditDTO.setBeneficiaryId(new BigInteger(beneficiary.getBeneficiaryID()));
		}
		if (beneficiary.getBeneficiaryRegID() != null) {
			identityEditDTO.setBeneficiaryRegId(BigInteger.valueOf(beneficiary.getBeneficiaryRegID()));
		}

		Integer educationID = beneficiary.getI_bendemographics().getEducationID();
		if (educationID != null) {
			identityEditDTO.setEducationId(educationID);
			identityEditDTO
					.setEducation(educationMapper.educationToModelByID(educationID.longValue()).getEducationType());
		}
		Integer occupationID = beneficiary.getI_bendemographics().getOccupationID();
		if (occupationID != null) {
			identityEditDTO.setOccupationId(occupationID);
			identityEditDTO.setOccupationName(occupationMapper.occupationToModelByID(occupationID).getOccupationType());
		}
		Integer communityID = beneficiary.getI_bendemographics().getCommunityID();
		if (communityID != null) {
			identityEditDTO.setCommunityId(communityID);
			identityEditDTO.setCommunity(communityMapper.communityToLoginResponseByID(communityID).getCommunityType());
		}
		if (beneficiary.getStatusID() != null) {
			identityEditDTO.setStatusId(beneficiary.getStatusID().intValue());
			identityEditDTO.setStatus(statusMapper.statusToModelByID(beneficiary.getStatusID().intValue()).getStatus());
		}
		Short healthCareWorkerID = beneficiary.getI_bendemographics().getHealthCareWorkerID();
		if (healthCareWorkerID != null) {
			identityEditDTO.setHealthCareWorkerId(healthCareWorkerID.intValue());
			identityEditDTO.setHealthCareWorkerType(
					healthCareWorkerMapper.getModelByWorkerID(healthCareWorkerID).getHealthCareWorkerType());
		}
		if (beneficiary.getSexualOrientationID() != null) {
			identityEditDTO.setSexualOrientationID(beneficiary.getSexualOrientationID().intValue());
			identityEditDTO.setSexualOrientationType(sexualOrientationMapper
					.sexualOrientationByIDToModel(beneficiary.getSexualOrientationID()).getSexualOrientation());
		}
		Integer genderID = beneficiary.getGenderID();
		if (genderID != null) {
			identityEditDTO.setGenderId(genderID);
			identityEditDTO.setGender(genderMapper.genderByIDToLoginResponse(genderID).getGenderName());
		}
		Integer relegionID = beneficiary.getI_bendemographics().getReligionID();
		if (relegionID != null) {
			identityEditDTO.setReligionId(relegionID);
			if (beneficiary.getI_bendemographics().getReligionName() != null)
				identityEditDTO.setReligion(beneficiary.getI_bendemographics().getReligionName());
			else
				identityEditDTO.setReligion(religionMapper.relegionToModelByID(relegionID).getReligionType());
		}
		Short titleID = beneficiary.getTitleId();
		if (titleID != null) {
			identityEditDTO.setTitleId(titleID.intValue());
			identityEditDTO.setTitle(titleMapper.titleByIDToResponse(titleID.intValue()).getTitleName());
		}
		Integer incomeStatusID = beneficiary.getI_bendemographics().getIncomeStatusID();
		if (incomeStatusID != null) {
			identityEditDTO.setIncomeStatusId(incomeStatusID);
			identityEditDTO
					.setIncomeStatus(incomeStatusMapper.incomeStatusByIDToModel(incomeStatusID).getIncomeStatus());
		}
		Short maritalStatusID = beneficiary.getMaritalStatusID();
		if (maritalStatusID != null) {
			identityEditDTO.setMaritalStatusId(beneficiary.getMaritalStatusID().intValue());
			identityEditDTO.setMaritalStatus(
					maritalStatusMapper.maritalStatusByIDToResponse(maritalStatusID.intValue()).getStatus());
		}

		Integer preferredLangID = beneficiary.getI_bendemographics().getPreferredLangID();
		if (preferredLangID != null) {
			identityEditDTO.setPreferredLanguageId(preferredLangID);
			identityEditDTO.setPreferredLanguage(languageMapper.languageToModelByID(preferredLangID).getLanguageName());
		}

		if (beneficiary.getVanID() != null)
			identityEditDTO.setVanID(beneficiary.getVanID());
		if (beneficiary.getParkingPlaceID() != null)
			identityEditDTO.setParkingPlaceId(beneficiary.getParkingPlaceID());
		if (beneficiary.getI_bendemographics() != null
				&& beneficiary.getI_bendemographics().getMonthlyFamilyIncome() != null)
			identityEditDTO.setMonthlyFamilyIncome(beneficiary.getI_bendemographics().getMonthlyFamilyIncome());
		if(beneficiary.getOtherFields() != null)
			identityEditDTO.setOtherFields(beneficiary.getOtherFields());
		if(beneficiary.getMaritalStatusName() != null)
			identityEditDTO.setMaritalStatus(beneficiary.getMaritalStatusName());
		if(beneficiary.getI_bendemographics() != null && beneficiary.getI_bendemographics().getOccupationName() != null)
			identityEditDTO.setOccupationName(beneficiary.getI_bendemographics().getOccupationName());
		return identityEditDTO;
	}

}
