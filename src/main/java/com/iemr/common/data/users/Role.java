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

import java.sql.Timestamp;
import java.util.List;
import java.util.Set;

import com.google.gson.annotations.Expose;
import com.iemr.common.data.notification.Notification;
import com.iemr.common.utils.mapper.OutputMapper;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;

@Entity
@Table(name = "m_role")
@Data
public class Role
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Expose
	private Integer RoleID;
	@Expose
	private String RoleName;
	@Expose
	private String RoleDesc;
	@Transient
	@Expose
	private Integer roleID;
	@Transient
	@Expose
	private String roleName;
	@Transient
	@Expose
	private String roleDesc;
	@Expose
	private Boolean Deleted;
	@Expose
	private String CreatedBy;
	@Expose
    @Transient
    private Boolean inbound;
   
    @Expose
    @Transient
    private Boolean outbound;
	private Timestamp CreatedDate;
	private String ModifiedBy;
	private Timestamp LastModDate;
	@OneToMany(mappedBy = "m_Role")
	@Transient
	private Set<UserServiceRoleMapping> m_UserServiceRoleMapping;
	@OneToMany(mappedBy = "role", fetch = FetchType.LAZY)
	@Transient
	private Set<Notification> notifications;
	@Expose
	@OneToMany(mappedBy = "role", fetch = FetchType.EAGER)
	@Transient
	private List<ServiceRoleScreenMapping> serviceRoleScreenMappings;

	@Transient
	private OutputMapper outputMapper = new OutputMapper();

	@Expose
	@Transient
	private Integer workingLocationID;

	@Expose
	@Transient
	private String agentID;
	
	@Expose
	@Transient
	private String teleConsultation;

	public Role createRole(Integer roleID, String roleName, String roleDesc, Boolean deleted, String createdBy,
			Timestamp createdDate, String modifiedBy, Timestamp lastModDate,
			Set<UserServiceRoleMapping> m_UserServiceRoleMapping)
	{
		this.RoleID = roleID;
		this.RoleName = roleName;
		this.RoleDesc = roleDesc;
		this.roleID = roleID;
		this.roleName = roleName;
		this.roleDesc = roleDesc;

		this.Deleted = deleted;
		this.CreatedBy = createdBy;
		this.CreatedDate = createdDate;
		this.ModifiedBy = modifiedBy;
		this.LastModDate = lastModDate;
		this.m_UserServiceRoleMapping = m_UserServiceRoleMapping;
		return this;
	}



	public boolean isDeleted()
	{
		return this.Deleted.booleanValue();
	}

	public void setDeleted(boolean deleted)
	{
		this.Deleted = Boolean.valueOf(deleted);
	}

	public String getCreatedBy()
	{
		return this.CreatedBy;
	}

	public void setCreatedBy(String createdBy)
	{
		this.CreatedBy = createdBy;
	}

	public Timestamp getCreatedDate()
	{
		return this.CreatedDate;
	}

	public void setCreatedDate(Timestamp createdDate)
	{
		this.CreatedDate = createdDate;
	}

	public String getModifiedBy()
	{
		return this.ModifiedBy;
	}

	public void setModifiedBy(String modifiedBy)
	{
		this.ModifiedBy = modifiedBy;
	}

	public Timestamp getLastModDate()
	{
		return this.LastModDate;
	}

	public void setLastModDate(Timestamp lastModDate)
	{
		this.LastModDate = lastModDate;
	}


	public void setServiceRoleScreenMappings(List<ServiceRoleScreenMapping> serviceRoleScreenMappings)
	{
		this.serviceRoleScreenMappings = serviceRoleScreenMappings;
	}

	@Override
	public String toString()
	{
		return OutputMapper.gson().toJson(this);
	}

	public static Role initializeRole(Integer roleID, String roleName)
	{
		Role role = new Role();
		role.RoleID = roleID;
		role.RoleName = roleName;
		role.roleID = roleID;
		role.roleName = roleName;
		return role;
	}

	public void setWorkingLocationID(Integer workingLocationID)
	{
		this.workingLocationID = workingLocationID;
	}

	public void setAgentID(String agentID)
	{
		this.agentID = agentID;
	}
	public void setInbound(Boolean inbound)
	{
	this.inbound = inbound;
	}
	public void setOutbound(Boolean outbound)
	{
	this.outbound = outbound;
	}
}
