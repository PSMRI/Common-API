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

import com.google.gson.annotations.Expose;
import com.iemr.common.model.user.HealthCareWorkerModel;
import com.iemr.common.model.userbeneficiary.CityModel;
import com.iemr.common.model.userbeneficiary.CommunityModel;
import com.iemr.common.model.userbeneficiary.CountryModel;
import com.iemr.common.model.userbeneficiary.DistrictBlockModel;
import com.iemr.common.model.userbeneficiary.DistrictBranchModel;
import com.iemr.common.model.userbeneficiary.DistrictModel;
import com.iemr.common.model.userbeneficiary.LanguageModel;
import com.iemr.common.model.userbeneficiary.ReligionModel;
import com.iemr.common.model.userbeneficiary.StateModel;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BeneficiaryDemographicsModel {
	@Expose
	private Long benDemographicsID;
	@Expose
	private Long beneficiaryRegID;
	// private BeneficiaryModel beneficiaryModel;
	@Expose
	private Integer educationID;
	@Expose
	private String educationName;
	@Expose
	private BeneficiaryEducationModel i_beneficiaryeducation;
	@Expose
	private Integer occupationID;
	@Expose
	private String occupationName;
	@Expose
	private BeneficiaryOccupationModel beneficiaryoccupation;
	@Expose
	private Short healthCareWorkerID;
	@Expose
	private String healthCareWorkerName;
	@Expose
	private HealthCareWorkerModel healthCareWorkerType;
	@Expose
	private Integer incomeStatusID;
	@Expose
	private String incomeStatusName;
	@Expose
	private BeneficiaryIncomeStatusModel i_beneficiaryincomestatus;
	@Expose
	private Integer communityID;
	@Expose
	private String communityName;
	@Expose
	private CommunityModel communityModel;
	@Expose
	private CommunityModel m_community;
	@Expose
	private Integer preferredLangID;
	@Expose
	private String preferredLangName;
	@Expose
	private LanguageModel m_language;
	@Expose
	private Integer religionID;
	@Expose
	private String religionName;
	@Expose
	private ReligionModel m_religion;
	@Expose
	private Boolean isPhoto;
	@Expose
	private String photoFilePath;
	@Expose
	private Boolean isBiometric;
	@Expose
	private String biometricFilePath;
	@Expose
	private String addressLine1;
	@Expose
	private String addressLine2;
	@Expose
	private String addressLine3;
	@Expose
	private String addressLine4;
	@Expose
	private String addressLine5;
	@Expose
	private Integer cityID;
	@Expose
	private String cityName;
	@Expose
	private CityModel m_city;
	@Expose
	private Integer stateID;
	@Expose
	private String stateName;
	@Expose
	private StateModel m_state;
	@Expose
	private Integer districtID;
	@Expose
	private String districtName;
	@Expose
	private DistrictModel m_district;
	@Expose
	private Integer blockID;
	@Expose
	private String blockName;
	@Expose
	private DistrictBlockModel m_districtblock;
	@Expose
	private Integer districtBranchID;
	@Expose
	private String districtBranchName;
	@Expose
	private DistrictBranchModel m_districtbranchmapping;
	@Expose
	private Integer countryID;
	@Expose
	private String countryName;
	@Expose
	private CountryModel m_country;
	@Expose
	private String pinCode;
	@Expose
	private Boolean isAddPresent;
	@Expose
	private Boolean isAddPermanent;
	@Expose
	private Boolean deleted;
	@Expose
	private String createdBy;
	@Expose
	private String modifiedBy;

	/*
	 * New columns added for MMU integration 09-04-2018
	 */
	@Expose
	private Integer zoneID;
	@Expose
	private String zoneName;
	@Expose
	private Integer parkingPlaceID;
	@Expose
	private String parkingPlaceName;
	@Expose
	private Integer servicePointID;
	@Expose
	private String servicePointName;
	@Expose
	private String habitation;

	@Expose
	private Double latitude;
	@Expose
	private Double longitude;
	@Expose
	private String digipin;
	@Expose
	private Long gpsTimestamp;
	@Expose
	private Boolean isGpsUnavailable = false;
	@Expose
	private String gpsUnavailableReason;

	@Expose
	private String occupation;
	@Expose
	private String incomeStatus;
	@Expose
	private String religion;
	@Expose
	private String monthlyFamilyIncome;

	public BeneficiaryDemographicsModel() {

	}

	// for everWell registration
	public BeneficiaryDemographicsModel(Integer communityID, Integer stateID, Integer districtID, String pinCode,
			boolean deleted) {
		this.communityID = communityID;
		this.stateID = stateID;
		this.districtID = districtID;
		this.pinCode = pinCode;
		this.deleted = deleted;
	}

	// public static BeneficiaryDemographicsModel createDemographics(Address
	// address, BigInteger benRegID,
	// String createdBy, BenDetailDTO benDetails, LocationStateRepository
	// locationStateRepo,
	// StateMapper stateMapper, LocationDistrictRepository districtRepository,
	// DistrictMapper districtMapper,
	// LocationDistrictBlockRepository blockRepository, DistrictBlockMapper
	// blockMapper,
	// LocationDistrilctBranchRepository branchRepository, DistrictBranchMapper
	// branchMapper,
	// EducationRepository educationRepository, EducationMapper educationMapper,
	// CommunityRepository communityRepository, CommunityMapper communityMapper)
	// {
	// BeneficiaryDemographicsModel demographicsModel = new
	// BeneficiaryDemographicsModel();
	// demographicsModel.beneficiaryRegID = Long.parseLong(benRegID.toString());
	// demographicsModel.stateID = address.getStateId();
	// if (address.getStateId() != null)
	// {
	// demographicsModel.m_state = StateModel.getState(address.getStateId(),
	// locationStateRepo, stateMapper);
	// demographicsModel.stateName = demographicsModel.m_state.getStateName();
	// }
	// demographicsModel.districtID = address.getDistrictId();
	// if (address.getDistrictId() != null)
	// {
	// demographicsModel.m_district =
	// DistrictModel.createDistrict(address.getDistrictId(), districtRepository,
	// districtMapper);
	// demographicsModel.districtName =
	// demographicsModel.m_district.getDistrictName();
	// }
	// demographicsModel.blockID = address.getSubDistrictId();
	// if (address.getSubDistrictId() != null)
	// {
	// demographicsModel.m_districtblock =
	// DistrictBlockModel.createDistrictBlock(address.getSubDistrictId(),
	// blockRepository, blockMapper);
	// demographicsModel.blockName =
	// demographicsModel.m_districtblock.getBlockName();
	// }
	// demographicsModel.districtBranchID = address.getVillageId();
	// if (address.getVillageId() != null)
	// {
	// demographicsModel.m_districtbranchmapping =
	// DistrictBranchModel.createDistrictBranch(address.getVillageId(),
	// branchRepository, branchMapper);
	// demographicsModel.districtBranchName =
	// demographicsModel.m_districtbranchmapping.getVillageName();
	// }
	// demographicsModel.educationID = benDetails.getEducationId();
	// if (benDetails.getEducationId() != null)
	// {
	// demographicsModel.i_beneficiaryeducation = BeneficiaryEducationModel
	// .createEducationModel(benDetails.getEducationId(), educationRepository,
	// educationMapper);
	// demographicsModel.educationName =
	// demographicsModel.i_beneficiaryeducation.getEducationType();
	// }
	// demographicsModel.communityID = benDetails.getCommunityId();
	// if (benDetails.getCommunityId() != null)
	// {
	// demographicsModel.m_community =
	// CommunityModel.createCommunityModel(benDetails.getCommunityId(),
	// communityRepository, communityMapper);
	// demographicsModel.communityName =
	// demographicsModel.m_community.getCommunityType();
	// }
	// demographicsModel.createdBy = createdBy;
	// demographicsModel.pinCode = address.getPinCode();
	// demographicsModel.addressLine1 = address.getAddrLine1();
	// demographicsModel.addressLine2 = address.getAddrLine2();
	// demographicsModel.addressLine3 = address.getAddrLine3();
	// demographicsModel.healthCareWorkerID =
	// benDetails.getHealthCareWorkerId();
	//
	// return demographicsModel;
	// }
}
