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
package com.iemr.common.data.users;

import java.sql.Date;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.iemr.common.utils.mapper.OutputMapper;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;

@Data
@Entity
@Table(name = "M_ProviderServiceAddMapping")
public class ProviderServiceAddressMapping
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Expose
	@Column(name = "PSAddMapID")
	private Integer pSAddMapID;
	@Expose
	@OneToMany(mappedBy = "providerServiceAddressMapping", fetch = FetchType.LAZY)
	@Transient
	private List<UserServiceRoleMapping> userServiceRoleMappings;

	@Expose
	@Column(name = "ProviderServiceMapID")
	private Integer providerServiceMapID;
	@Expose
	@Column(name = "DistrictID")
	private Integer districtID;
	@Expose
	@Column(name = "Address")
	private String address;
	@Expose
	@Column(name = "Deleted", insertable = false, updatable = true)
	private Boolean deleted;
	@Expose
	@Column(name = "CreatedBy")
	private String createdBy;
	@Expose
	@Column(name = "CreatedDate", insertable = false, updatable = false)
	private Date createdDate;
	@Expose
	@Column(name = "ModifiedBy")
	private String modifiedBy;
	@Expose
	@Column(name = "LastModDate", insertable = false, updatable = false)
	private Date lastModDate;

	@Expose
	@Column(name = "LocationName")
	private String locationName;
	
	@Expose
	@Column(name = "AbdmFacilityID")
	private String abdmFacilityID;
	
	@Expose
	@Column(name = "AbdmFacilityName")
	private String abdmFacilityName;

	// public ProviderServiceAddressMapping()
	// {
	// }

	@Transient
	private OutputMapper mapper = new OutputMapper();

	@Override
	public String toString()
	{
		return mapper.gson().toJson(this);
	}

	public Integer getpSAddMapID()
	{
		return pSAddMapID;
	}

	public List<UserServiceRoleMapping> getUserServiceRoleMappings()
	{
		return userServiceRoleMappings;
	}

	public Integer getProviderServiceMapID()
	{
		return providerServiceMapID;
	}

	public Integer getDistrictID()
	{
		return districtID;
	}

	public String getAddress()
	{
		return address;
	}

	public Boolean getDeleted()
	{
		return deleted;
	}

	public String getCreatedBy()
	{
		return createdBy;
	}

	public Date getCreatedDate()
	{
		return createdDate;
	}

	public String getModifiedBy()
	{
		return modifiedBy;
	}

	public Date getLastModDate()
	{
		return lastModDate;
	}

	public String getLocationName()
	{
		return locationName;
	}
	
}
