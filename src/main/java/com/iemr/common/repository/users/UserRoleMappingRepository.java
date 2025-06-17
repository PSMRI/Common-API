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
package com.iemr.common.repository.users;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.iemr.common.data.users.UserServiceRoleMapping;

@Repository
public interface UserRoleMappingRepository extends CrudRepository<UserServiceRoleMapping, Long>
{
	@Query("select map.USRMappingID, map.UserID, map.RoleID, map.m_Role, map.providerServiceMapID, "
			+ "map.agentID, map.inbound, map.outbound, map.teleConsultation, map.agentPassword, map.workingLocationID, map.providerServiceAddressMapping from "
			+ "UserServiceRoleMapping map left join map.m_ProviderServiceMapping psm left join psm.serviceProvider sp "
			+ "left join map.m_Role left join map.providerServiceAddressMapping "
			+ "where map.Deleted = false and UserID = :UserID and sp.statusID in (1,2) "
			+ "and sp.deleted = false and psm.statusID in (1,2) "
			+ "and sp.validTill > current_timestamp() and sp.validFrom < current_timestamp() "
			+ "order by map.USRMappingID ")
	Set<Object[]> getUserRoleMappingForUser(@Param("UserID") Long UserID);

	@Query("select map.RoleID, map.m_Role from UserServiceRoleMapping map join map.m_Role "
			+ "where map.Deleted = false and map.providerServiceMapID = :providerServiceMapID group by map.RoleID "
			+ "order by map.RoleID ")
	Set<Object[]> getRolesByProviderServiceMapID(@Param("providerServiceMapID") Integer providerServiceMapID);

	@Query("select distinct map.workingLocationID, map.providerServiceAddressMapping "
			+ "from UserServiceRoleMapping map join map.providerServiceAddressMapping "
			+ "where map.Deleted = false and map.providerServiceMapID = :providerServiceMapID "
			+ "group by map.workingLocationID order by map.workingLocationID")
	Set<Object[]> getLocationsByProviderID(@Param("providerServiceMapID") Integer providerServiceMapID);

	@Query("select distinct map.workingLocationID, map.providerServiceAddressMapping "
			+ "from UserServiceRoleMapping map join map.providerServiceAddressMapping "
			+ "where map.Deleted = false and map.providerServiceMapID = :providerServiceMapID and "
			+ "map.RoleID = :RoleID group by map.workingLocationID order by map.workingLocationID")
	Set<Object[]> getLocationsByProviderID(@Param("providerServiceMapID") Integer providerServiceMapID,
			@Param("RoleID") Integer RoleID);

	@Query("select distinct UserID from UserServiceRoleMapping where Deleted = false and "
			+ "providerServiceMapID = :providerServiceMapID")
	List<Long> getUsersByProviderServiceMapID(@Param("providerServiceMapID") Integer providerServiceMapID);

	@Query("select distinct usr.UserID from UserServiceRoleMapping usr join usr.m_Role role "
			+ "where usr.Deleted = false and usr.providerServiceMapID = :providerServiceMapID and role.RoleID = :roleID ")
	List<Long> getUsersByProviderServiceMapID(@Param("providerServiceMapID") Integer providerServiceMapID,
			@Param("roleID") Integer roleID);

	@Query("select distinct usr.UserID from UserServiceRoleMapping usr join usr.m_user user "
			+ "join user.m_UserLangMappings langs join langs.m_language lang "
			+ "where usr.Deleted = false and usr.providerServiceMapID = :providerServiceMapID "
			+ "and langs.canSpeak = true and lang.languageName = :languageName order by usr.UserID")
	List<Long> getUsersByProviderServiceMapLang(@Param("providerServiceMapID") Integer providerServiceMapID,
			@Param("languageName") String languageName);

	@Query("select distinct usr.UserID from UserServiceRoleMapping usr join usr.m_Role role join usr.m_user user "
			+ "join user.m_UserLangMappings langs join langs.m_language lang "
			+ "where usr.Deleted = false and usr.providerServiceMapID = :providerServiceMapID and role.RoleID = :roleID "
			+ "and langs.canSpeak = true and lang.languageName = :languageName order by usr.UserID")
	List<Long> getUsersByProviderServiceMapRoleLang(@Param("providerServiceMapID") Integer providerServiceMapID,
			@Param("roleID") Integer roleID, @Param("languageName") String languageName);

	@Query("select count(usr) from UserServiceRoleMapping usr "
			+ "where usr.UserID = :userID and usr.providerServiceMapID = :providerServiceMapID")
	long getCountByUserIDAndProviderServiceMapID(@Param("userID") Long userID,
			@Param("providerServiceMapID") Integer providerServiceMapID);

	@Query("select usr from UserServiceRoleMapping usr "
			+ "where usr.UserID = :userID and usr.providerServiceMapID = :providerServiceMapID")
	List<UserServiceRoleMapping> getMappingsByUserIDAndProviderServiceMapID(@Param("userID") Long userID,
			@Param("providerServiceMapID") Integer providerServiceMapID);

	@Query("select distinct user.agentID from UserServiceRoleMapping user where user.Deleted = false and "
			+ "user.providerServiceMapID = :providerServiceMapID and user.agentID is not null order by user.agentID")
	List<String> getAgentByProviderServiceMapID(@Param("providerServiceMapID") Integer providerServiceMapID);

	@Query("select distinct user.agentID from UserServiceRoleMapping user join user.m_Role role "
			+ "where user.Deleted = false and user.providerServiceMapID = :providerServiceMapID and role.RoleID = :roleID and user.agentID is not null order by user.agentID")
	List<String> getAgentByRoleID(@Param("providerServiceMapID") Integer providerServiceMapID,
			@Param("roleID") Integer roleID);
}
