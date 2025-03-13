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
package com.iemr.common.repository.callhandling;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.iemr.common.data.callhandling.CallType;

@Repository
public interface IEMRCalltypeRepositoryImplCustom extends CrudRepository<CallType, Long>
{

	@Query("select callType, callTypeID, callTypeDesc, callGroupType, fitToBlock, fitForFollowUp from CallType "
			+ "where deleted=false order by callType asc")
	Iterable<CallType> readData();

	// @Transactional
	// @Modifying(clearAutomatically = true)
	// @Query("UPDATE CallType c set c.remarks =:remarks, c.callType =:callType
	// where c.id = :id")
	// int updateCallType(@Param("id") Long id,@Param("callType") String
	// callType, @Param("remarks") String remarks);

	/*
	 * @Query("UPDATE CallType c set c.remarks =:"+m_Calltype.getId()+
	 * ", c.callType =:m_Calltype.getId() where c.id = :m_Calltype.getId()") int updateCallType(CallType m_Calltype);
	 */

	@Query("select callType, callTypeID, callTypeDesc, callGroupType, fitToBlock, fitForFollowUp, "
			+ "isInbound, isOutbound from CallType "
			+ "where deleted=false and providerServiceMapID = :providerServiceMapID order by callType asc")
	Set<Object[]> getCallTypes(@Param("providerServiceMapID") Integer providerServiceMapID);

	@Query("select callType, callTypeID, callTypeDesc, callGroupType, fitToBlock, fitForFollowUp, "
			+ "isInbound, isOutbound from CallType "
			+ "where deleted=false and providerServiceMapID = :providerServiceMapID and "
			+ "(isInbound is null or isInbound = :isInbound )and "
			+ "(isOutbound is null or isOutbound = :isOutbound) order by callType asc")
	Set<Object[]> getCallTypes(@Param("providerServiceMapID") Integer providerServiceMapID,
			@Param("isInbound") Boolean isInbound, @Param("isOutbound") Boolean isOutbound);

	@Query("select callType, callTypeID, callTypeDesc, callGroupType, fitToBlock, fitForFollowUp, "
			+ "isInbound, isOutbound from CallType "
			+ "where deleted=false and providerServiceMapID = :providerServiceMapID and "
			+ "(isInbound is null or isInbound = :isInbound ) order by callType asc")
	Set<Object[]> getInboundCallTypes(@Param("providerServiceMapID") Integer providerServiceMapID,
			@Param("isInbound") Boolean isInbound);

	@Query("select callType, callTypeID, callTypeDesc, callGroupType, fitToBlock, fitForFollowUp, "
			+ "isInbound, isOutbound from CallType "
			+ "where deleted=false and providerServiceMapID = :providerServiceMapID and "
			+ "(isOutbound is null or isOutbound = :isOutbound) order by callType asc")
	Set<Object[]> getOutboundCallTypes(@Param("providerServiceMapID") Integer providerServiceMapID,
			@Param("isOutbound") Boolean isOutbound);

	@Query("select maxRedial from CallType where callTypeID = :callTypeID")
	Integer getMaxRedialByCallTypeID(@Param("callTypeID") Integer callTypeID);

	@Query("select callType from CallType callType where deleted = false and fitToBlock = true")
	List<CallType> getFitToBlockCallTypes();
	
	@Query("select callType from CallType callType where deleted = false and callTypeID = :callTypeID")
	CallType getCallTypeDetails(@Param("callTypeID") Integer callTypeID);
	
	@Query("select callGroupType, callType from CallType "
			+ "where callTypeID = :callTypeID")
	Set<Object[]> getCallDetails(@Param("callTypeID") Integer callTypeID);
}
