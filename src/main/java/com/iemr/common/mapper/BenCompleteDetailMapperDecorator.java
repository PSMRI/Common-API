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
import com.iemr.common.dto.identity.BenDetailDTO;
import com.iemr.common.dto.identity.BeneficiariesDTO;
import com.iemr.common.model.beneficiary.BeneficiaryDemographicsModel;
import com.iemr.common.model.userbeneficiary.LanguageModel;

public abstract class BenCompleteDetailMapperDecorator implements BenCompleteDetailMapper
{
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

	@Override
	public BeneficiaryDemographicsModel createBenDemographicsModel(BeneficiariesDTO beneficiariesDTO)
	{
		Address address = beneficiariesDTO.getCurrentAddress();
		BigInteger benRegID = beneficiariesDTO.getBenRegId();
		String createdBy = beneficiariesDTO.getCreatedBy();
		BenDetailDTO benDetails = beneficiariesDTO.getBeneficiaryDetails();

		BeneficiaryDemographicsModel demographicsModel = new BeneficiaryDemographicsModel();
		if(null != benRegID)
		demographicsModel.setBeneficiaryRegID(Long.parseLong(benRegID.toString()));
		
		
		if (null != address && address.getStateId() != null)
		{
			demographicsModel.setStateID(address.getStateId());
			demographicsModel.setM_state(stateMapper.StateToModelByID(address.getStateId()));
			demographicsModel.setStateName(demographicsModel.getM_state().getStateName());
		}
		
		if (null != address && address.getDistrictId() != null)
		{
			demographicsModel.setDistrictID(address.getDistrictId());
			demographicsModel.setM_district(districtMapper.districtToModelByID(address.getDistrictId()));
			demographicsModel.setDistrictName(demographicsModel.getM_district().getDistrictName());
		}
		
		if (null != address && address.getSubDistrictId() != null)
		{
			demographicsModel.setBlockID(address.getSubDistrictId());
			demographicsModel.setM_districtblock(blockMapper.districtBlockToModelByID(address.getSubDistrictId()));
			demographicsModel.setBlockName(demographicsModel.getM_districtblock().getBlockName());
		}
		
		if (null != address && address.getVillageId() != null)
		{
			demographicsModel.setDistrictBranchID(address.getVillageId());
			demographicsModel
					.setM_districtbranchmapping(branchMapper.districtBranchToModelByID(address.getVillageId()));
			demographicsModel.setDistrictBranchName(demographicsModel.getM_districtbranchmapping().getVillageName());
		}
		demographicsModel.setEducationID(benDetails.getEducationId());
		if (benDetails.getEducationId() != null)
		{
			demographicsModel.setI_beneficiaryeducation(
					educationMapper.educationToModelByID(Long.parseLong(benDetails.getEducationId().toString())));
			demographicsModel.setEducationName(demographicsModel.getI_beneficiaryeducation().getEducationType());
		}
		demographicsModel.setCommunityID(benDetails.getCommunityId());
		if (benDetails.getCommunityId() != null)
		{
			demographicsModel.setM_community(communityMapper.communityToLoginResponseByID(benDetails.getCommunityId()));
			demographicsModel.setCommunityName(demographicsModel.getM_community().getCommunityType());
		}
		if (benDetails.getOccupationId() != null)
		{
			demographicsModel.setOccupationID(benDetails.getOccupationId());
			demographicsModel.setOccupationName(benDetails.getOccupation());
		}
		// if (benDetails.getIncomeStatusId()!=null) {
		demographicsModel.setIncomeStatusID(benDetails.getIncomeStatusId());
		demographicsModel.setIncomeStatus(benDetails.getIncomeStatus());
		demographicsModel.setIncomeStatusName(benDetails.getIncomeStatus());
		// }
		if (benDetails.getReligionId() != null)
		{
			demographicsModel.setReligionID(benDetails.getReligionId());
			demographicsModel.setReligionName(benDetails.getReligion());
		}
		if (benDetails.getIncomeStatusId() != null)
		{
			demographicsModel.setIncomeStatusID(benDetails.getIncomeStatusId());
			demographicsModel.setIncomeStatusName(benDetails.getIncomeStatus());
			demographicsModel.setIncomeStatus(benDetails.getIncomeStatus());
		}
		if (benDetails.getMonthlyFamilyIncome() != null) {
			demographicsModel.setMonthlyFamilyIncome(benDetails.getMonthlyFamilyIncome());
		}
		demographicsModel.setCreatedBy(createdBy);
		if(null != address) {
		demographicsModel.setPinCode(address.getPinCode());
		demographicsModel.setAddressLine1(address.getAddrLine1());
		demographicsModel.setAddressLine2(address.getAddrLine2());
		demographicsModel.setAddressLine3(address.getAddrLine3());
		
		demographicsModel.setZoneID(address.getZoneID());
		demographicsModel.setZoneName(address.getZoneName());
		demographicsModel.setParkingPlaceID(address.getParkingPlaceID());
		demographicsModel.setParkingPlaceName(address.getParkingPlaceName());
		demographicsModel.setServicePointID(address.getServicePointID());
		demographicsModel.setServicePointName(address.getServicePointName());
		demographicsModel.setHabitation(address.getHabitation());
		demographicsModel.setLatitude(address.getGpsLatitude());
		demographicsModel.setLongitude(address.getGpsLongitude());
		demographicsModel.setDigipin(address.getDigipin());
		demographicsModel.setGpsTimestamp(address.getGpsTimestamp());
		demographicsModel.setIsGpsUnavailable(address.getIsGpsUnavailable());
		demographicsModel.setGpsUnavailableReason(address.getGpsUnavailableReason());
		}
		demographicsModel.setHealthCareWorkerID(benDetails.getHealthCareWorkerId());
		demographicsModel.setPreferredLangID(beneficiariesDTO.getBeneficiaryDetails().getPreferredLanguageId());
		LanguageModel lang =
				languageMapper.languageToModelByID(beneficiariesDTO.getBeneficiaryDetails().getPreferredLanguageId());
		demographicsModel.setM_language(lang);
		if (lang != null)
		{
			demographicsModel.setPreferredLangName(lang.getLanguageName());
		}
		return demographicsModel;
	}

	protected Address beneficiaryDemographicsModelToAddress(BeneficiaryDemographicsModel beneficiaryDemographicsModel)
	{
		if (beneficiaryDemographicsModel == null)
		{
			return null;
		}

		Address address = new Address();

		address.setVillageId(beneficiaryDemographicsModel.getDistrictBranchID());
		address.setZoneID(beneficiaryDemographicsModel.getZoneID());
		address.setParkingPlaceName(beneficiaryDemographicsModel.getParkingPlaceName());
		address.setStateId(beneficiaryDemographicsModel.getStateID());
		address.setDistrictId(beneficiaryDemographicsModel.getDistrictID());
		if (beneficiaryDemographicsModel.getDistrictID() != null)
		{
			address.setDistrict(
					districtMapper.districtToModelByID(beneficiaryDemographicsModel.getDistrictID()).getDistrictName());
		}
		address.setServicePointName(beneficiaryDemographicsModel.getServicePointName());
		address.setCountry(beneficiaryDemographicsModel.getCountryName());
		if (beneficiaryDemographicsModel.getStateID() != null)
		{
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
		if (beneficiaryDemographicsModel.getDistrictBranchID() != null)
		{
			address.setVillage(branchMapper
					.districtBranchToModelByID(beneficiaryDemographicsModel.getDistrictBranchID()).getVillageName());
		}
		address.setSubDistrictId(beneficiaryDemographicsModel.getBlockID());
		address.setCountryId(beneficiaryDemographicsModel.getCountryID());
		if (beneficiaryDemographicsModel.getBlockID() != null)
		{
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

}
