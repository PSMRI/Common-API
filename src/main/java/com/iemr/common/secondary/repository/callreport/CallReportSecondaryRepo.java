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
package com.iemr.common.secondary.repository.callreport;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import common.iemr.common.secondary.data.report.SecondaryCallReport;

@Profile("!swagger")
@Repository
public interface CallReportSecondaryRepo  extends CrudRepository<SecondaryCallReport, Long> {
	@Query(value="call Pr_104QAReport(:startDateTime,:endDateTime,:receivedRoleName,:agentID,:providerServiceMapID)", nativeQuery=true)
	public List<Object[]> get104QAReport(@Param("startDateTime") Timestamp startDateTime,
			@Param("endDateTime") Timestamp endDateTime,
			@Param("receivedRoleName") String receivedRoleName,@Param("agentID") String agentID,
			@Param("providerServiceMapID") Integer providerServiceMapID);
	
	@Query(value="call Pr_104LAHTAlgorithmCalls(:startDateTime,:endDateTime,:callTypeName,:receivedRoleName,:agentID,:providerServiceMapID)", nativeQuery=true)
	public List<Object[]> getLAHTAlgorithmCalls(@Param("startDateTime") Timestamp startDateTime,
			@Param("endDateTime") Timestamp endDateTime,@Param("callTypeName") String callTypeName,
			@Param("receivedRoleName") String receivedRoleName,@Param("agentID") String agentID,
			@Param("providerServiceMapID") Integer providerServiceMapID);
	
	@Query(value="call Pr_104DSusedValidCallAtHAO(:startDateTime,:endDateTime,:callTypeName,:receivedRoleName,:agentID,:providerServiceMapID)", nativeQuery=true)
	public List<Object[]> getDSUsedValidCalls(@Param("startDateTime") Timestamp startDateTime,
			@Param("endDateTime") Timestamp endDateTime,@Param("callTypeName") String callTypeName,
			@Param("receivedRoleName") String receivedRoleName,@Param("agentID") String agentID,
			@Param("providerServiceMapID") Integer providerServiceMapID);
	
	@Query(value="call Pr_104LAHTTransferCallsMO(:startDateTime,:endDateTime,:callTypeName,:receivedRoleName,:agentID,:providerServiceMapID)", nativeQuery=true)
	public List<Object[]> getLAHTTransferCallsToMO(@Param("startDateTime") Timestamp startDateTime,
			@Param("endDateTime") Timestamp endDateTime,@Param("callTypeName") String callTypeName,
			@Param("receivedRoleName") String receivedRoleName,@Param("agentID") String agentID,
			@Param("providerServiceMapID") Integer providerServiceMapID);
	
	@Query(value="call Pr_104HAHTDisconnectedCalls(:startDateTime,:endDateTime,:callTypeName,:receivedRoleName,:agentID,:providerServiceMapID)", nativeQuery=true)
	public List<Object[]> getHAHTDisconnectedCalls(@Param("startDateTime") Timestamp startDateTime,
			@Param("endDateTime") Timestamp endDateTime,@Param("callTypeName") String callTypeName,
			@Param("receivedRoleName") String receivedRoleName,@Param("agentID") String agentID,
			@Param("providerServiceMapID") Integer providerServiceMapID);
	
	@Query(value="call Pr_104HAHTvalidcallsclosedatHAO(:startDateTime,:endDateTime,:callTypeName,:receivedRoleName,:agentID,:providerServiceMapID)", nativeQuery=true)
	public List<Object[]> getHAHTvalidCallsClosedAtHAO(@Param("startDateTime") Timestamp startDateTime,
			@Param("endDateTime") Timestamp endDateTime,@Param("callTypeName") String callTypeName,
			@Param("receivedRoleName") String receivedRoleName,@Param("agentID") String agentID,
			@Param("providerServiceMapID") Integer providerServiceMapID);
	
	@Query(value="call Pr_104RandomPickup(:startDateTime,:endDateTime,:callTypeName,:receivedRoleName,:agentID,:providerServiceMapID)", nativeQuery=true)
	public List<Object[]> getRandomCalls(@Param("startDateTime") Timestamp startDateTime,
			@Param("endDateTime") Timestamp endDateTime,@Param("callTypeName") String callTypeName,
			@Param("receivedRoleName") String receivedRoleName,@Param("agentID") String agentID,
			@Param("providerServiceMapID") Integer providerServiceMapID);
	
	@Query(value="call Pr_104OtherAdviceCalls(:startDateTime,:endDateTime,:callTypeName,:receivedRoleName,:agentID,:providerServiceMapID)", nativeQuery=true)
	public List<Object[]> getOtherAdviceCalls(@Param("startDateTime") Timestamp startDateTime,
			@Param("endDateTime") Timestamp endDateTime,@Param("callTypeName") String callTypeName,
			@Param("receivedRoleName") String receivedRoleName,@Param("agentID") String agentID,
			@Param("providerServiceMapID") Integer providerServiceMapID);
	
	@Query(value="call Pr_104GrievanceDetailsReport(:startDateTime,:endDateTime,:feedbackNatureID,:feedbackTypeID,:feedbackTypeName,:providerServiceMapID)", nativeQuery=true)
	public List<Object[]> getGrievanceDetailsReport(@Param("startDateTime") Timestamp startDateTime,
			@Param("endDateTime") Timestamp endDateTime,@Param("feedbackTypeName") String feedbackTypeName,
			@Param("providerServiceMapID") Integer providerServiceMapID,@Param("feedbackTypeID") Integer feedbackTypeID,
			@Param("feedbackNatureID") Integer feedbackNatureID);
	
	@Query(value="call Pr_104CallSummaryReport(:startDateTime,:endDateTime,:roleName,:callTypeID,:agentID,:providerServiceMapID)", nativeQuery=true)
	public List<Object[]> getCallSummaryReport(@Param("startDateTime") Timestamp startDateTime,
	@Param("endDateTime") Timestamp endDateTime,@Param("roleName") String roleName,
	@Param("callTypeID") Integer callTypeID,@Param("agentID") String agentID,
	@Param("providerServiceMapID") Integer providerServiceMapID);
	
	@Query(value="call Pr_DistrictwisecallvolumeReport_pivot(:startDateTime,:endDateTime,:providerServiceMapID,:districtID)", nativeQuery=true)
	public List<Object[]> getDistrictWiseCallReport(@Param("startDateTime") Timestamp startDateTime,
	@Param("endDateTime") Timestamp endDateTime, @Param("providerServiceMapID") Integer providerServiceMapID, @Param("districtID") Integer districtID);

	@Query(value="call Pr_104UnblockUserReport(:startDateTime,:endDateTime,:providerServiceMapID)", nativeQuery=true)
	public List<Object[]> getUnblockedUserReport(@Param("startDateTime") Timestamp startDateTime,
	@Param("endDateTime") Timestamp endDateTime,@Param("providerServiceMapID") Integer providerServiceMapID);
	
	@Query(value="call Pr_1097Sexualorientation(:startDateTime,:endDateTime,:state,:district,:sexualOrientation,:providerServiceMapID)", nativeQuery=true)
	public List<Object[]> getAllBySexualOrientationReport(@Param("startDateTime") Timestamp startTimestamp,
	@Param("endDateTime") Timestamp endTimestamp,@Param("state") String state,
	@Param("district") String district,@Param("sexualOrientation") String sexual_Orientation,
	@Param("providerServiceMapID") Integer providerServiceMapID);
	
	
	@Query(value="call Pr_104CallQuality_CallTypeWise(:startDateTime,:endDateTime,:callTypeID,:providerServiceMapID)", nativeQuery=true)
	public List<Object[]> getcallTypeWise(@Param("startDateTime") Timestamp startDateTime,
	@Param("endDateTime") Timestamp endDateTime,@Param("callTypeID") Integer callTypeID,
	@Param("providerServiceMapID") Integer providerServiceMapID);
	
	@Query(value="call Pr_104CallQuality_AgentWise(:startDateTime,:endDateTime,:userID,:providerServiceMapID)", nativeQuery=true)
	public List<Object[]> getAgentWiseReport(@Param("startDateTime") Timestamp startDateTime,
	@Param("endDateTime") Timestamp endDateTime,@Param("userID") Long userID,
	@Param("providerServiceMapID") Integer providerServiceMapID);
	
	@Query(value="call Pr_104CallQuality_SkillsetWise(:startDateTime,:endDateTime,:roleID,:providerServiceMapID)", nativeQuery=true)
	public List<Object[]> getSkillsetWiseReport(@Param("startDateTime") Timestamp startDateTime,
	@Param("endDateTime") Timestamp endDateTime,@Param("roleID") Long roleID,
	@Param("providerServiceMapID") Integer providerServiceMapID);
	
	@Query(value="call Pr_104CallQuality_DateWise(:startDateTime,:endDateTime,:providerServiceMapID)", nativeQuery=true)
	public List<Object[]> getDateWiseReport(@Param("startDateTime") Timestamp startDateTime,
	@Param("endDateTime") Timestamp endDateTime,@Param("providerServiceMapID") Integer providerServiceMapID);
	
	@Query(value="call Pr_104CallQuality_LocationWise(:startDateTime,:endDateTime,:locationID,:providerServiceMapID)", nativeQuery=true)
	public List<Object[]> getLocationWiseReport(@Param("startDateTime") Timestamp startDateTime,
	@Param("endDateTime") Timestamp endDateTime,@Param("locationID") Long locationID,
	@Param("providerServiceMapID") Integer providerServiceMapID);

	@Query(value="call Pr_1097LanguageDistribution(:startDateTime,:endDateTime,:state,:district,:beneficiaryPreferredLanguage,:providerServiceMapID)", nativeQuery=true)
	public List<Object[]> getLanguageDistributionReport(@Param("startDateTime") Timestamp startDateTime,
	@Param("endDateTime") Timestamp endDateTime,@Param("state") String state,
	@Param("district") String district,@Param("beneficiaryPreferredLanguage") String beneficiaryPreferredLanguage,
	@Param("providerServiceMapID") Integer providerServiceMapID);


	@Query(value="call Pr_1097AgegroupDistribution(:startDateTime,:endDateTime,:state,:district,:minAge,:maxAge,:providerServiceMapID)", nativeQuery=true)
	public List<Object[]> getAllByAgeGroup(@Param("startDateTime") Timestamp startTimestamp,
	@Param("endDateTime") Timestamp endTimestamp, @Param("state") String state, @Param("district") String district, 
	@Param("minAge") Integer minAge, @Param("maxAge") Integer maxAge, @Param("providerServiceMapID") Integer providerServiceMapID);

	@Query(value="call db_reporting.Pr_1097calltypeReport(:startDateTime,:endDateTime,:state,:district,:beneficiaryCallType,:beneficiaryCallSubType,:beneficiaryPreferredLanguage,:gender,:beneficiarySexualOrientation,:providerServiceMapID)", nativeQuery=true)
	public List<Object[]> getAllReportsByDate(@Param("startDateTime") Timestamp startTimestamp,
	@Param("endDateTime") Timestamp endTimestamp, @Param("state") String state, @Param("district") String district,
	@Param("beneficiaryCallType") String beneficiaryCallType,@Param("beneficiaryCallSubType") String beneficiaryCallSubType, @Param("beneficiaryPreferredLanguage") String beneficiaryPreferredLanguage, @Param("gender") String gender,
	@Param("beneficiarySexualOrientation") String beneficiarySexualOrientation, @Param("providerServiceMapID") Integer providerServiceMapID);

	@Query(value="call Pr_1097GenderDistribution(:startDateTime,:endDateTime,:state,:district,:gender,:providerServiceMapID)", nativeQuery=true)
	public List<Object[]> getAllByGender(@Param("startDateTime") Timestamp startDateTime,
	@Param("endDateTime") Timestamp endDateTime, @Param("state") String state, @Param("district") String district,
	@Param("gender") String gender, @Param("providerServiceMapID") Integer providerServiceMapID);


}
