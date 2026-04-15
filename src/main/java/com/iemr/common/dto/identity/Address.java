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

import com.iemr.common.model.beneficiary.BeneficiaryDemographicsModel;

import lombok.Data;

public @Data class Address {
	private String addrLine1;
	private String addrLine2;
	private String addrLine3;
	private Integer countryId;
	private String country;
	private Integer stateId;
	private String state;
	private Integer districtId;
	private String district;
	private Integer subDistrictId;
	private String subDistrict;
	private Integer villageId;
	private String village;
	private String habitation;
	private String addressValue;
	private String pinCode;

	/*
	 * New columns added for MMU integration 09-04-2018
	 */
	private Integer zoneID;
	private String zoneName;
	private Integer parkingPlaceID;
	private String parkingPlaceName;
	private Integer servicePointID;
	private String servicePointName;

	private Integer areaId;
	private String area;
	private String otherResidentialArea;
	private String coordinate;

	public static Address bendemographicsAddressMapper(BeneficiaryDemographicsModel i_bendemographics) {
		Address address = new Address();
		if (i_bendemographics == null) {
			return null;
		}
		address.setAddrLine1(i_bendemographics.getAddressLine1());
		address.setAddrLine2(i_bendemographics.getAddressLine2());
		address.setAddrLine3(i_bendemographics.getAddressLine3());
		address.setCountryId(i_bendemographics.getCountryID());
		if (i_bendemographics.getM_state() != null) {

			address.setState(i_bendemographics.getM_state().getStateName());
		}
		if (i_bendemographics.getM_country() != null) {
			address.setCountry(i_bendemographics.getM_country().getCountryName());
		}
		if (i_bendemographics.getM_district() != null) {

			address.setDistrict(i_bendemographics.getM_district().getDistrictName());
		}
		if (i_bendemographics.getM_districtbranchmapping() != null) {

			address.setVillage(i_bendemographics.getM_districtbranchmapping().getVillageName());
		}
		if (i_bendemographics.getM_districtblock() != null) {

			address.setVillage(i_bendemographics.getM_districtblock().getBlockName());
		}

		address.setStateId(i_bendemographics.getStateID());
		address.setDistrictId(i_bendemographics.getDistrictID());
		address.setSubDistrictId(i_bendemographics.getBlockID());
		address.setVillageId(i_bendemographics.getDistrictBranchID());
		address.setPinCode(i_bendemographics.getPinCode());

		address.setAreaId(i_bendemographics.getResidentialAreaId());
		address.setArea(i_bendemographics.getResidentialArea());
		address.setOtherResidentialArea(i_bendemographics.getOtherResidentialArea());
		if (i_bendemographics.getLatitude() != null && i_bendemographics.getLongitude() != null
				&& !(i_bendemographics.getLatitude() == 0.0 && i_bendemographics.getLongitude() == 0.0)) {
			address.setCoordinate(i_bendemographics.getLatitude() + "," + i_bendemographics.getLongitude());
		}

		return address;
	}
}
