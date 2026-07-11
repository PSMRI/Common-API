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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.Expose;
import com.iemr.common.utils.mapper.OutputMapper;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;

@Data
@Entity
@Table(name = "m_userservicerolemapping")
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserServiceRoleMapping
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Expose
	private Long USRMappingID;

	@Expose
	private Long UserID;
	@ManyToOne /* (fetch = FetchType.LAZY) */
	@JoinColumn(name = "UserID", updatable = false, insertable = false)
	@JsonIgnore
	@Expose
	// @Transient
	private User m_user;

	@Expose
	private Integer RoleID;
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "RoleID", updatable = false, insertable = false)
	@JsonIgnore
	@Expose
	private Role m_Role;
	@Expose
	private Integer roleID;
	// @Expose
	// private Integer RoleID;
	@Expose
	@Transient
	private Integer ServiceID;
	@Expose
	@Column(name = "Deleted", insertable = false, updatable = true)
	private Boolean Deleted;
	@Expose
	private String CreatedBy;
	private Timestamp CreatedDate;
	private String ModifiedBy;
	private Timestamp LastModDate;

	@Expose
	@Column(name = "ProviderServiceMapID")
	private Integer providerServiceMapID;
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ProviderServiceMapID", updatable = false, insertable = false)
	@JsonIgnore
	@Expose
	private ProviderServiceMapping m_ProviderServiceMapping;

	@Expose
	@Column(name = "AgentID")
	private String agentID;

	@Expose
	@Column(name = "AgentPassword")
	private String agentPassword;
	

    @Expose
	@Column(name = "isInbound")
	private Boolean inbound;
	@Expose
	@Column(name = "isOutbound")
	private Boolean outbound;
	
	@Expose
	@Column(name = "teleConsultation")
	private String teleConsultation;


	// @Expose
	// @Column(name = "StatusID")
	// private Integer statusID;
	//
	// @Expose
	// @OneToOne(fetch = FetchType.LAZY)
	// @JoinColumn(name = "StatusID", insertable = false, updatable = false)
	// private Status status;

	@Expose
	@Column(name = "WorkingLocationID")
	private Integer workingLocationID;
	@Expose
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "workingLocationID", insertable = false, updatable = false, referencedColumnName = "pSAddMapID")
	private ProviderServiceAddressMapping providerServiceAddressMapping;

	@Transient
	private OutputMapper outputMapper = new OutputMapper();

	@Transient
	private String languageName;

	// protected UserServiceRoleMapping() {
	// }

	public UserServiceRoleMapping createUserRoleMappingByID(Long uSRMappingID)
	{
		this.USRMappingID = uSRMappingID;
		return this;
	}

	public UserServiceRoleMapping createUserServiceRoleMapping(Long uSRMappingID, User m_user, Role m_Role,
			ProviderServiceMapping m_ProviderServiceMapping, Boolean deleted, String createdBy, Timestamp createdDate,
			String modifiedBy, Timestamp lastModDate, Integer workingLocationID)
	{
		this.USRMappingID = uSRMappingID;
		this.m_user = m_user;
		this.m_Role = m_Role;
		this.m_ProviderServiceMapping = m_ProviderServiceMapping;
		this.Deleted = deleted;
		this.CreatedBy = createdBy;
		this.CreatedDate = createdDate;
		this.ModifiedBy = modifiedBy;
		this.LastModDate = lastModDate;
		this.workingLocationID = workingLocationID;
		this.m_Role.setWorkingLocationID(workingLocationID);
		return this;
	}

	public Long getUSRMappingID()
	{
		return this.USRMappingID;
	}

	public void setUSRMappingID(Long uSRMappingID)
	{
		this.USRMappingID = uSRMappingID;
	}

//	public User getM_user()
//	{
//		return this.m_user;
//	}
//
//	public void setM_user(User m_user)
//	{
//		this.m_user = m_user;
//	}
//
//	public Role getM_Role()
//	{
//		return this.m_Role;
//	}
//
//	public void setM_Role(Role m_Role)
//	{
//		this.m_Role = m_Role;
//	}
//
//	public ProviderServiceMapping getM_ProviderServiceMapping()
//	{
//		return this.m_ProviderServiceMapping;
//	}
//
//	public void setM_ProviderServiceMapping(ProviderServiceMapping m_ProviderServiceMapping)
//	{
//		this.m_ProviderServiceMapping = m_ProviderServiceMapping;
//	}

	public Boolean isDeleted()
	{
		return Deleted;
	}

	public void setDeleted(Boolean deleted)
	{
		Deleted = deleted;
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

	public Long getUserID()
	{
		return UserID;
	}

	public void setUserID(Long userID)
	{
		UserID = userID;
	}

//	public Integer getRoleID()
//	{
//		if (roleID != null)
//		{
//			RoleID = roleID;
//		}
//		return RoleID;
//	}
//
//	public void setRoleID(Integer roleID)
//	{
//		this.RoleID = roleID;
//	}

	public Integer getServiceID()
	{
		return ServiceID;
	}

	public void setServiceID(Integer serviceID)
	{
		ServiceID = serviceID;
		providerServiceMapID = serviceID;
	}

	public Boolean getDeleted()
	{
		return Deleted;
	}

	@Override
	public String toString()
	{
		return outputMapper.gson().toJson(this);
	}

	public static UserServiceRoleMapping initializeUserRoleMappingObjs(Long USRMappingID, Long UserID, Integer roleID,
			Role m_Role, Integer providerServiceMapID, ProviderServiceMapping m_ProviderServiceMapping,
			String agentID,Boolean inbound, Boolean outbound, String teleConsultation, String agentPassword, Integer workingLocationID,
			ProviderServiceAddressMapping providerServiceAddressMapping)
	{
		UserServiceRoleMapping userRoleMapping = new UserServiceRoleMapping();
		userRoleMapping.USRMappingID = USRMappingID;
		userRoleMapping.UserID = UserID;
		userRoleMapping.RoleID = roleID;
		userRoleMapping.roleID = roleID;
		userRoleMapping.m_Role = m_Role;
		userRoleMapping.ServiceID = providerServiceMapID;
		userRoleMapping.providerServiceMapID = providerServiceMapID;
		userRoleMapping.m_ProviderServiceMapping = m_ProviderServiceMapping;
		userRoleMapping.agentID = agentID;
		userRoleMapping.inbound = inbound;
		userRoleMapping.outbound = outbound;
		userRoleMapping.teleConsultation = teleConsultation;
		userRoleMapping.agentPassword = agentPassword;
		userRoleMapping.workingLocationID = workingLocationID;
		userRoleMapping.m_Role.setWorkingLocationID(workingLocationID);
		userRoleMapping.m_Role.setAgentID(agentID);
		userRoleMapping.m_Role.setInbound(inbound);
		userRoleMapping.m_Role.setOutbound(outbound);
		userRoleMapping.setProviderServiceAddressMapping(providerServiceAddressMapping);
		return userRoleMapping;
	}

	public Integer getProviderServiceMapID()
	{
		return providerServiceMapID;
	}

	public String getAgentID()
	{
		return agentID;
	}
	
	public Boolean getInbound()
	{
	return inbound;
	}
	public Boolean getOutbound() {
	return outbound;
	}

	public String getAgentPassword()
	{
		return agentPassword;
	}

	public Integer getWorkingLocationID()
	{
		return workingLocationID;
	}

	public String getLanguageName()
	{
		return languageName;
	}
}
