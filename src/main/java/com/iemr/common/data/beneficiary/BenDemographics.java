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
package com.iemr.common.data.beneficiary;



import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.annotations.Expose;
import com.iemr.common.data.healthCareWorkerType.HealthCareWorker;
import com.iemr.common.data.location.CityDetails;
import com.iemr.common.data.location.Country;
import com.iemr.common.data.location.DistrictBlock;
import com.iemr.common.data.location.DistrictBranchMapping;
import com.iemr.common.data.location.Districts;
import com.iemr.common.data.location.States;
import com.iemr.common.data.userbeneficiarydata.Community;
import com.iemr.common.data.userbeneficiarydata.Language;
import com.iemr.common.data.userbeneficiarydata.Religion;
import com.iemr.common.utils.mapper.OutputMapper;

import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Transient;
import lombok.Data;

@Data
public class BenDemographics
{
	// @Id
	// // @Column(name = "BenDemographicsID")
	@Expose
	private Long benDemographicsID;

	// @Column(name = "BeneficiaryRegID")
	@Expose
	private Long beneficiaryRegID;

	@JsonIgnore
	// @Expose
	@Transient
	private Beneficiary i_beneficiary;

	// @Column(name = "EducationID")
	@Expose
	private Integer educationID;
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(updatable = false, insertable = false, name = "EducationID")
	@JsonIgnore
	@Expose
	private BeneficiaryEducation i_beneficiaryeducation;

	// @Column(name = "OccupationID")
	@Expose
	private Integer occupationID;
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(updatable = false, insertable = false, name = "OccupationID")
	@JsonIgnore
	@Expose
	private BeneficiaryOccupation i_beneficiaryoccupation;

	// @Column(name = "HealthCareWorkerID")
	@Expose
	private Short healthCareWorkerID;
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(updatable = false, insertable = false, name = "healthCareWorkerID")
	@JsonIgnore
	@Expose
	private HealthCareWorker healthCareWorkerType;

	// @Column(name = "IncomeStatusID")
	@Expose
	private Integer incomeStatusID;
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(updatable = false, insertable = false, name = "IncomeStatusID")
	@JsonIgnore
	@Expose
	private BeneficiaryIncomeStatus i_beneficiaryincomestatus;

	// @Column(name = "CommunityID")
	@Expose
	private Integer communityID;
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(updatable = false, insertable = false, name = "CommunityID")
	@JsonIgnore
	@Expose
	private Community m_community;

	// @Column(name = "PreferredLangID")
	@Expose
	private Integer preferredLangID;
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(updatable = false, insertable = false, name = "PreferredLangID", referencedColumnName = "LanguageID")
	@JsonIgnore
	@Expose
	private Language m_language;

	// @Column(name = "ReligionID")
	@Expose
	private Integer religionID;
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(updatable = false, insertable = false, name = "ReligionID")
	@JsonIgnore
	@Expose
	private Religion m_religion;

	// @Column(name = "IsPhoto")
	@Expose
	private Boolean isPhoto;
	// @Column(name = "PhotoFilePath")
	@Expose
	private String photoFilePath;
	// @Column(name = "IsBiometric")
	@Expose
	private Boolean isBiometric;
	// @Column(name = "BiometricFilePath")
	@Expose
	private String biometricFilePath;
	// @Column(name = "AddressLine1")
	@Expose
	private String addressLine1;
	// @Column(name = "AddressLine2")
	@Expose
	private String addressLine2;
	// @Column(name = "AddressLine3")
	@Expose
	private String addressLine3;
	// @Column(name = "AddressLine4")
	@Expose
	private String addressLine4;
	// @Column(name = "AddressLine5")
	@Expose
	private String addressLine5;

	// @Column(name = "CityID")
	@Expose
	private Integer cityID;
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(updatable = false, insertable = false, name = "CityID")
	@JsonIgnore
	@Expose
	private CityDetails m_city;

	// @Column(name = "StateID")
	@Expose
	private Integer stateID;
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(updatable = false, insertable = false, name = "StateID")
	@JsonIgnore
	@Expose
	private States m_state;

	// @Column(name = "DistrictID")
	@Expose
	private Integer districtID;
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(updatable = false, insertable = false, name = "DistrictID")
	@JsonIgnore
	@Expose
	private Districts m_district;

	// @Column(name = "BlockID")
	@Expose
	private Integer blockID;
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(updatable = false, insertable = false, name = "BlockID")
	@JsonIgnore
	@Expose
	private DistrictBlock m_districtblock;

	// @Column(name = "DistrictBranchID")
	@Expose
	private Integer districtBranchID;
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(updatable = false, insertable = false, name = "DistrictBranchID")
	@JsonIgnore
	@Expose
	private DistrictBranchMapping m_districtbranchmapping;

	// @Column(name = "CountryID")
	@Expose
	private Integer countryID;
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(updatable = false, insertable = false, name = "CountryID")
	@JsonIgnore
	@Expose
	private Country m_country;

	// @Column(name = "PinCode")
	@Expose
	private String pinCode;
	// @Column(name = "IsAddPresent")
	@Expose
	private Boolean isAddPresent;
	// @Column(name = "IsAddPermanent")
	@Expose
	private Boolean isAddPermanent;
	// @Column(name = "Deleted", insertable = false, updatable = true)
	@Expose
	private Boolean deleted;
	// @Column(name = "CreatedBy")
	@Expose
	private String createdBy;
	// @Column(name = "ModifiedBy")
	private String modifiedBy;

	@Expose
	private String economicStatus;

	@Expose
	private Integer economicStatusId;

	@Expose
	private String  residentialArea;

	@Expose
	private Integer  residentialAreaId;

	@Expose
	private String address;

	@Transient
	private OutputMapper outputMapper = new OutputMapper();

	public BenDemographics()
	{
	}

	public BenDemographics(long benDemographicsID, Long beneficiaryRegID, Integer educationID, Integer occupationID,
			Integer incomeStatusID, Integer communityID, Integer preferredLangID, Integer religionID, Boolean isPhoto,
			String photoFilePath, Boolean isBiometric, String biometricFilePath, String addressLine1,
			String addressLine2, String addressLine3, String addressLine4, String addressLine5, Integer cityID,
			Integer stateID, Integer countryID, String pinCode, Boolean isAddPresent, Boolean isAddPermanent,
			Boolean deleted, String createdBy, String modifiedBy)
	{
		this.benDemographicsID = Long.valueOf(benDemographicsID);
		this.beneficiaryRegID = beneficiaryRegID;
		this.educationID = educationID;
		this.occupationID = occupationID;
		this.incomeStatusID = incomeStatusID;
		this.communityID = communityID;
		this.preferredLangID = preferredLangID;
		this.religionID = religionID;
		this.isPhoto = isPhoto;
		this.photoFilePath = photoFilePath;
		this.isBiometric = isBiometric;
		this.biometricFilePath = biometricFilePath;
		this.addressLine1 = addressLine1;
		this.addressLine2 = addressLine2;
		this.addressLine3 = addressLine3;
		this.addressLine4 = addressLine4;
		this.addressLine5 = addressLine5;
		this.cityID = cityID;
		this.stateID = stateID;
		this.countryID = countryID;
		this.pinCode = pinCode;
		this.isAddPresent = isAddPresent;
		this.isAddPermanent = isAddPermanent;
		this.deleted = deleted;
		this.createdBy = createdBy;
		this.modifiedBy = modifiedBy;
	}

	// public Long getBenDemographicsID()
	// {
	// return this.benDemographicsID;
	// }
	//
	// public void setBenDemographicsID(long benDemographicsID)
	// {
	// this.benDemographicsID = Long.valueOf(benDemographicsID);
	// }
	//
	// public Long getBeneficiaryRegID()
	// {
	// return this.beneficiaryRegID;
	// }
	//
	// public void setBeneficiaryRegID(long beneficiaryRegID)
	// {
	// this.beneficiaryRegID = Long.valueOf(beneficiaryRegID);
	// }
	//
	// public Integer getEducationID()
	// {
	// return this.educationID;
	// }
	//
	// public void setEducationID(Integer educationID)
	// {
	// this.educationID = educationID;
	// }
	//
	// public Integer getOccupationID()
	// {
	// return this.occupationID;
	// }
	//
	// public void setOccupationID(Integer occupationID)
	// {
	// this.occupationID = occupationID;
	// }
	//
	// public Integer getIncomeStatusID()
	// {
	// return this.incomeStatusID;
	// }
	//
	// public void setIncomeStatusID(Integer incomeStatusID)
	// {
	// this.incomeStatusID = incomeStatusID;
	// }
	//
	// public Integer getCommunityID()
	// {
	// return this.communityID;
	// }
	//
	// public void setCommunityID(Integer communityID)
	// {
	// this.communityID = communityID;
	// }
	//
	// public Integer getPreferredLangID()
	// {
	// return this.preferredLangID;
	// }
	//
	// public void setPreferredLangID(Integer preferredLangID)
	// {
	// this.preferredLangID = preferredLangID;
	// }
	//
	// public Integer getReligionID()
	// {
	// return this.religionID;
	// }
	//
	// public void setReligionID(Integer religionID)
	// {
	// this.religionID = religionID;
	// }
	//
	// public Boolean getIsPhoto()
	// {
	// return this.isPhoto;
	// }
	//
	// public void setIsPhoto(Boolean isPhoto)
	// {
	// this.isPhoto = isPhoto;
	// }
	//
	// public String getPhotoFilePath()
	// {
	// return this.photoFilePath;
	// }
	//
	// public void setPhotoFilePath(String photoFilePath)
	// {
	// this.photoFilePath = photoFilePath;
	// }
	//
	// public Boolean getIsBiometric()
	// {
	// return this.isBiometric;
	// }
	//
	// public void setIsBiometric(Boolean isBiometric)
	// {
	// this.isBiometric = isBiometric;
	// }
	//
	// public String getBiometricFilePath()
	// {
	// return this.biometricFilePath;
	// }
	//
	// public void setBiometricFilePath(String biometricFilePath)
	// {
	// this.biometricFilePath = biometricFilePath;
	// }
	//
	// public String getAddressLine1()
	// {
	// return this.addressLine1;
	// }
	//
	// public void setAddressLine1(String addressLine1)
	// {
	// this.addressLine1 = addressLine1;
	// }
	//
	// public String getAddressLine2()
	// {
	// return this.addressLine2;
	// }
	//
	// public void setAddressLine2(String addressLine2)
	// {
	// this.addressLine2 = addressLine2;
	// }
	//
	// public String getAddressLine3()
	// {
	// return this.addressLine3;
	// }
	//
	// public void setAddressLine3(String addressLine3)
	// {
	// this.addressLine3 = addressLine3;
	// }
	//
	// public String getAddressLine4()
	// {
	// return this.addressLine4;
	// }
	//
	// public void setAddressLine4(String addressLine4)
	// {
	// this.addressLine4 = addressLine4;
	// }
	//
	// public String getAddressLine5()
	// {
	// return this.addressLine5;
	// }
	//
	// public void setAddressLine5(String addressLine5)
	// {
	// this.addressLine5 = addressLine5;
	// }
	//
	// public Integer getCityID()
	// {
	// return this.cityID;
	// }
	//
	// public void setCityID(Integer cityID)
	// {
	// this.cityID = cityID;
	// }
	//
	// public Integer getStateID()
	// {
	// return this.stateID;
	// }
	//
	// public void setStateID(Integer stateID)
	// {
	// this.stateID = stateID;
	// }
	//
	// public Integer getCountryID()
	// {
	// return this.countryID;
	// }
	//
	// public void setCountryID(Integer countryID)
	// {
	// this.countryID = countryID;
	// }
	//
	// public String getPinCode()
	// {
	// return this.pinCode;
	// }
	//
	// public void setPinCode(String pinCode)
	// {
	// this.pinCode = pinCode;
	// }
	//
	// public Boolean getIsAddPresent()
	// {
	// return this.isAddPresent;
	// }
	//
	// public void setIsAddPresent(Boolean isAddPresent)
	// {
	// this.isAddPresent = isAddPresent;
	// }
	//
	// public Boolean getIsAddPermanent()
	// {
	// return this.isAddPermanent;
	// }
	//
	// public void setIsAddPermanent(Boolean isAddPermanent)
	// {
	// this.isAddPermanent = isAddPermanent;
	// }
	//
	// public Boolean getDeleted()
	// {
	// return this.deleted;
	// }
	//
	// public void setDeleted(Boolean deleted)
	// {
	// this.deleted = deleted;
	// }
	//
	// public String getCreatedBy()
	// {
	// return this.createdBy;
	// }
	//
	// public void setCreatedBy(String createdBy)
	// {
	// this.createdBy = createdBy;
	// }
	//
	// public String getModifiedBy()
	// {
	// return this.modifiedBy;
	// }
	//
	// public void setModifiedBy(String modifiedBy)
	// {
	// this.modifiedBy = modifiedBy;
	// }
	//
	// public Beneficiary getI_beneficiary()
	// {
	// return i_beneficiary;
	// }
	//
	// public BeneficiaryEducation getI_beneficiaryeducation()
	// {
	// return i_beneficiaryeducation;
	// }
	//
	// public BeneficiaryOccupation getI_beneficiaryoccupation()
	// {
	// return i_beneficiaryoccupation;
	// }
	//
	// public BeneficiaryIncomeStatus getI_beneficiaryincomestatus()
	// {
	// return i_beneficiaryincomestatus;
	// }
	//
	// public Community getM_community()
	// {
	// return m_community;
	// }
	//
	// public Language getM_language()
	// {
	// return m_language;
	// }
	//
	// public Religion getM_religion()
	// {
	// return m_religion;
	// }
	//
	// public CityDetails getM_city()
	// {
	// return m_city;
	// }
	//
	// public States getM_state()
	// {
	// return m_state;
	// }
	//
	// public Integer getDistrictID()
	// {
	// return districtID;
	// }
	//
	// public Districts getM_district()
	// {
	// return m_district;
	// }
	//
	// public Integer getBlockID()
	// {
	// return blockID;
	// }
	//
	// public DistrictBlock getM_districtblock()
	// {
	// return m_districtblock;
	// }
	//
	// public Integer getDistrictBranchID()
	// {
	// return districtBranchID;
	// }
	//
	// public DistrictBranchMapping getM_districtbranchmapping()
	// {
	// return m_districtbranchmapping;
	// }
	//
	// public Country getM_country()
	// {
	// return m_country;
	// }

	public BenDemographics getBenDemograhics(long benDemographicsID, Integer educationID, String educationType,
			Integer occupationID, String occupationType, Integer incomeStatusID, String incomeStatus,
			Integer communityID, String communityType, Integer preferredLangID, String languageName, Integer religionID,
			String relegionType, Boolean isPhoto, String photoFilePath, String addressLine1, String addressLine2,
			String addressLine3, String addressLine4, String addressLine5, Integer cityID, String cityName,
			Integer stateID, String stateName, Integer countryID, String countryName, String pinCode,
			Boolean isAddPresent, Boolean isAddPermanent)
	{
		this.benDemographicsID = Long.valueOf(benDemographicsID);
		this.i_beneficiaryeducation = new BeneficiaryEducation(educationID, educationType);
		this.i_beneficiaryoccupation = new BeneficiaryOccupation()
				.getOccupation(/* new Long(occupationID) */occupationID.longValue(), occupationType);
		this.i_beneficiaryincomestatus = new BeneficiaryIncomeStatus(incomeStatusID.intValue(), incomeStatus);
		this.m_community = new Community().getCommunity(communityID.intValue(), communityType);
		this.m_language = new Language(preferredLangID.intValue(), languageName);
		this.m_religion = new Religion(religionID.intValue(), relegionType, "");
		this.addressLine1 = addressLine1;
		this.addressLine2 = addressLine2;
		this.addressLine3 = addressLine3;
		this.addressLine4 = addressLine4;
		this.addressLine5 = addressLine5;
		this.isPhoto = isPhoto;
		this.m_city = new CityDetails(cityID, cityName);
		this.m_state = new States();
		this.m_state = this.m_state.getStates(stateID.intValue(), stateName);
		this.m_country = new Country();
		this.m_country = m_country.createCountry(countryID.intValue(), countryName);
		this.pinCode = pinCode;
		this.isAddPresent = isAddPresent;
		this.isAddPermanent = isAddPermanent;
		return this;
	}

	public BenDemographics getBenDemograhics(Long benDemographicsID,
			Long beneficiaryRegID, /* Beneficiary i_beneficiary, */
			Integer educationID, BeneficiaryEducation i_beneficiaryeducation, Integer occupationID,
			BeneficiaryOccupation i_beneficiaryoccupation, Integer incomeStatusID,
			BeneficiaryIncomeStatus i_beneficiaryincomestatus, Integer communityID, Community m_community,
			Integer preferredLangID, Language m_language, Integer religionID, Religion m_religion, Boolean isPhoto,
			String photoFilePath, Boolean isBiometric, String biometricFilePath, String addressLine1,
			String addressLine2, String addressLine3, String addressLine4, String addressLine5, Integer cityID,
			CityDetails m_city, Integer stateID, States m_state, Integer countryID, Country m_country, String pinCode,
			Boolean isAddPresent, Boolean isAddPermanent, Boolean deleted, Integer districtBranchID,
			DistrictBranchMapping m_districtbranchmapping, Integer blockID, DistrictBlock m_districtblock,
			Integer districtID, Districts m_district, Short healthCareWorkerID, HealthCareWorker healthcareWorkerType)
	{

		this.benDemographicsID = benDemographicsID;
		this.beneficiaryRegID = beneficiaryRegID;
		// this.i_beneficiary = i_beneficiary;
		this.educationID = educationID;
		this.i_beneficiaryeducation = i_beneficiaryeducation;
		this.occupationID = occupationID;
		this.i_beneficiaryoccupation = i_beneficiaryoccupation;
		this.incomeStatusID = incomeStatusID;
		this.i_beneficiaryincomestatus = i_beneficiaryincomestatus;
		this.communityID = communityID;
		this.m_community = m_community;
		this.preferredLangID = preferredLangID;
		this.m_language = m_language;
		this.religionID = religionID;
		this.m_religion = m_religion;
		this.isPhoto = isPhoto;
		this.photoFilePath = photoFilePath;
		this.isBiometric = isBiometric;
		this.biometricFilePath = biometricFilePath;
		this.addressLine1 = addressLine1;
		this.addressLine2 = addressLine2;
		this.addressLine3 = addressLine3;
		this.addressLine4 = addressLine4;
		this.addressLine5 = addressLine5;
		this.cityID = cityID;
		this.m_city = m_city;
		this.districtBranchID = districtBranchID;
		this.m_districtbranchmapping = m_districtbranchmapping;
		this.blockID = blockID;
		this.m_districtblock = m_districtblock;
		this.districtID = districtID;
		this.m_district = m_district;
		this.stateID = stateID;
		this.m_state = m_state;
		this.countryID = countryID;
		this.m_country = m_country;
		this.pinCode = pinCode;
		this.isAddPresent = isAddPresent;
		this.isAddPermanent = isAddPermanent;
		this.deleted = deleted;
		this.healthCareWorkerID = healthCareWorkerID;
		this.healthCareWorkerType = healthcareWorkerType;
		return this;
	}

	// public Short getHealthCareWorkerID()
	// {
	// return healthCareWorkerID;
	// }
	//
	// public void setHealthCareWorkerID(Short healthCareWorkerID)
	// {
	// this.healthCareWorkerID = healthCareWorkerID;
	// }
	//
	// public HealthCareWorker getHealthCareWorkerType()
	// {
	// return healthCareWorkerType;
	// }
	//
	// public void setHealthCareWorkerType(HealthCareWorker healthCareWorkerType)
	// {
	// this.healthCareWorkerType = healthCareWorkerType;
	// }

	@Override
	public String toString()
	{
		return OutputMapper.gson().toJson(this);
	}

	// public void setDistrictID(Integer districtID)
	// {
	// this.districtID = districtID;
	// }

}