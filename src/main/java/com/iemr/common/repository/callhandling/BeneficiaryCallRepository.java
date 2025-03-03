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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
// import javax.transaction.Transactional;
import org.springframework.transaction.annotation.Transactional;

import com.iemr.common.data.callhandling.BeneficiaryCall;

@Repository
public interface BeneficiaryCallRepository extends CrudRepository<BeneficiaryCall, Long> {
	// 29-10-2020 shubham and Neeraj
	// @Transactional(propagation = Propagation.REQUIRES_NEW)
	@Transactional
	@Modifying
	@Query("update BeneficiaryCall set callEndTime = :callEndTime, remarks = :remarks, "
			+ "callClosureType = :callClosureType, " + "dispositionStatusID = :dispositionStatusID, "
			+ "callTypeID = :callTypeID, emergencyType = :emergencyType where benCallID = :benCallID")
	public int closeCallOld(@Param("benCallID") Long benCallID, @Param("remarks") String remarks,
			@Param("callEndTime") Timestamp callEndTime, @Param("callClosureType") String callClosureType,
			@Param("callTypeID") Integer callTypeID, @Param("dispositionStatusID") Integer dispositionStatusID,
			@Param("emergencyType") Short emergencyType);

	@Transactional
	@Modifying
	@Query("update BeneficiaryCall set callEndTime = :callEndTime, remarks = :remarks, "
			+ "callClosureType = :callClosureType, " + "dispositionStatusID = :dispositionStatusID, "
			+ "callTypeID = :callTypeID, emergencyType = :emergencyType,"
			+ " externalReferral=:externalReferral,instTypeId=:instTypeId,instName=:instName, isOutbound=:isOutbound"
			+ "   where benCallID = :benCallID")
	public int closeCall(@Param("benCallID") Long benCallID, @Param("remarks") String remarks,
			@Param("callEndTime") Timestamp callEndTime, @Param("callClosureType") String callClosureType,
			@Param("callTypeID") Integer callTypeID, @Param("dispositionStatusID") Integer dispositionStatusID,
			@Param("emergencyType") Short emergencyType, @Param("externalReferral") String externalReferral,
			@Param("instTypeId") Integer instTypeId, @Param("instName") String instName,  @Param("isOutbound") Boolean isOutbound);

	// @Transactional(propagation = Propagation.REQUIRES_NEW)
	@Transactional
	@Modifying
	@Query("update BeneficiaryCall set beneficiaryRegID = :beneficiaryRegID, isCalledEarlier = :isCalledEarlier"
			+ " where benCallID = :benCallID")
	public int updateBeneficiaryIDInCall(@Param("benCallID") Long benCallID,
			@Param("beneficiaryRegID") Long beneficiaryRegID, @Param("isCalledEarlier") Boolean isCalledEarlier);

	// @Transactional(propagation = Propagation.REQUIRES_NEW)
	@Transactional
	@Modifying
	@Query("update BeneficiaryCall set cDICallStatus = :cDICallStatus where benCallID = :benCallID")
	public int updateBeneficiaryCallCDIStatus(@Param("benCallID") Long benCallID,
			@Param("cDICallStatus") String cDICallStatus);

	@Query("select call.benCallID, " + "call.remarks, type.callType, call.callTime " + "from BeneficiaryCall call "
			+ "join call.callTypeObj type where call.calledServiceID= :calledServiceID and call.callTypeID = :callTypeID "
			+ "and call.beneficiaryRegID is not null order by call.benCallID desc")
	public Set<Object[]> getCallsByCallType(@Param("calledServiceID") Integer calledServiceID,
			@Param("callTypeID") Integer callTypeID);

	@Query("select call.benCallID, call.beneficiaryRegID, " + "call.remarks, type.callType, call.callTime, "
			+ "call.phoneNo, call.cDICallStatus from BeneficiaryCall call "
			+ "join call.callTypeObj type where call.calledServiceID= :calledServiceID and call.callTypeID = :callTypeID "
			+ "and call.createdDate >= :filterStartDate and call.createdDate <= :filterEndDate "
			+ "and call.beneficiaryRegID is not null order by call.benCallID desc")
	public Set<Object[]> getCallsByCallTypeBetweenDates(@Param("calledServiceID") Integer calledServiceID,
			@Param("callTypeID") Integer callTypeID, @Param("filterStartDate") Timestamp filterStartDate,
			@Param("filterEndDate") Timestamp filterEndDate);

	@Query("select call from BeneficiaryCall call "
			+ "where call.calledServiceID= :calledServiceID and call.callTypeID = :callTypeID "
			+ "and call.createdDate >= :filterStartDate and call.createdDate <= :filterEndDate "
			+ "and call.beneficiaryRegID is not null and call.receivedRoleName like :receivedRoleName "
			+ "order by call.benCallID desc")
	public List<BeneficiaryCall> getCallsByCallTypeBetweenDatesV1(@Param("calledServiceID") Integer calledServiceID,
			@Param("callTypeID") Integer callTypeID, @Param("filterStartDate") Timestamp filterStartDate,
			@Param("filterEndDate") Timestamp filterEndDate, @Param("receivedRoleName") String receivedRoleName);

	@Query("select call from BeneficiaryCall call "
			+ "where call.calledServiceID= :calledServiceID and call.callTypeID = :callTypeID "
			+ "and call.createdDate >= :filterStartDate and call.createdDate <= :filterEndDate "
			+ "and call.beneficiaryRegID is not null and call.receivedRoleName = :receivedRoleName "
			+ "and call.isOutbound = :isOutbound order by call.benCallID desc")
	public List<BeneficiaryCall> getCallsByCallTypeBetweenDatesV1(@Param("calledServiceID") Integer calledServiceID,
			@Param("callTypeID") Integer callTypeID, @Param("filterStartDate") Timestamp filterStartDate,
			@Param("filterEndDate") Timestamp filterEndDate, @Param("receivedRoleName") String receivedRoleName,
			@Param("isOutbound") Boolean isOutbound);

	@Query("select phoneNo from BeneficiaryCall where benCallID = :benCallID")
	public String findPhoneCallByID(@Param("benCallID") Long benCallID);

	@Query("select call from BeneficiaryCall call where call.benCallID = :benCallID")
	public BeneficiaryCall findCallDetails(@Param("benCallID") Long benCallID);

	@Query("select call from BeneficiaryCall call where call.callID = :callID")
	public List<BeneficiaryCall> findCallDetails(@Param("callID") String callID);

	@Query("select call from BeneficiaryCall call where call.callID = :callID and call.beneficiaryRegID is not null ")
	public List<BeneficiaryCall> getCallHistoryByCallID(@Param("callID") String callID);

	@Query("select call.benCallID, call.createdDate, call.agentID, call.callID, "
			+ "call.recordingPath, call.archivePath from BeneficiaryCall call "
			+ "where call.phoneNo = :phoneNo and call.calledServiceID = :calledServiceID "
			+ "and call.beneficiaryRegID is not null order by call.benCallID desc")
	public List<Object[]> nueisanceCallHistory(@Param("phoneNo") String phoneNo,
			@Param("calledServiceID") Integer calledServiceID, Pageable pageable);

	// @Transactional(propagation = Propagation.REQUIRES_NEW)
	@Transactional
	@Modifying
	@Query("update BeneficiaryCall set recordingPath = :recordingPath, archivePath = :archivePath "
			+ "where benCallID = :benCallID")
	public int updateVoiceFilePath(@Param("benCallID") Long benCallID, @Param("recordingPath") String recordingPath,
			@Param("archivePath") String archivePath);

	public List<BeneficiaryCall> getCallsByBeneficiaryRegIDAndReceivedRoleName(Long beneficiaryRegID,
			String receivedRoleName);

	@Query("select call from BeneficiaryCall call where call.phoneNo = :phoneNo "
			+ "and call.callTypeID in :callTypeIDs and call.calledServiceID = :providerServiceMapID "
			+ "order by call.benCallID desc")
	public List<BeneficiaryCall> getCallHistoryByCallID(@Param("phoneNo") String phoneNo,
			@Param("providerServiceMapID") Integer providerServiceMapID,
			@Param("callTypeIDs") List<Integer> callTypeIDs, Pageable pageable);

	// @Transactional(propagation = Propagation.REQUIRES_NEW)
	@Transactional
	@Modifying
	@Query("update BeneficiaryCall set callEndUserID = :callEndUserID where callID = :callID")
	public int updateBeneficiaryCallEndedByUserID(@Param("callEndUserID") Integer callEndUserID,
			@Param("callID") String callID);

	// get file path if available for supervisor 104
	@Query("select distinct call.recordingPath from BeneficiaryCall call where call.recordingPath is not null and "
			+ "call.callID =:callID and call.agentID =:agentID")
	public String getUserFilepath(@Param("agentID") String agentID, @Param("callID") String callID);

	// update file path if available for supervisor 104
	// @Transactional(propagation = Propagation.REQUIRES_NEW)
	@Transactional
	@Modifying
	@Query("update BeneficiaryCall set recordingPath = :recordingPath, archivePath = :archivePath "
			+ "where agentID =:agentID and callID = :callID")
	public int updateVoiceFilePathNew(@Param("agentID") String agentID, @Param("callID") String callID,
			@Param("recordingPath") String recordingPath, @Param("archivePath") String archivePath);

	// checking the call ID is already present in the table or not for outbound
	// multiple record creation issue.
	@Query("SELECT b FROM BeneficiaryCall b WHERE b.callID = :callID")
	public BeneficiaryCall getExistingCallId(@Param("callID") String callID);

	@Query("SELECT bc FROM BeneficiaryCall bc WHERE bc.callID = :callID AND bc.agentID = :agentID  ")
	public ArrayList<BeneficiaryCall> getExistingBCByCallIDAndAgentID(@Param("callID") String callID,
			@Param("agentID") String agentID);

	@Query(value="select CallTypeID from db_iemr.m_callType where CallType='default null callType' order by CreatedDate DESC LIMIT 1",nativeQuery=true)
	public Integer getCallTypeId();
	
	@Transactional
	@Modifying
	@Query("update BeneficiaryCall set beneficiaryRegID = :beneficiaryRegID where benCallID = :benCallID AND beneficiaryRegID=:beneficiaryRegID")
	public int updateBeneficiaryRegIDInCall(@Param("benCallID") Long benCallID, @Param("beneficiaryRegID") Long beneficiaryRegID);
	
	BeneficiaryCall findByBenCallID(Long benCallID);
	
	@Query("SELECT b.remarks FROM BeneficiaryCall b WHERE b.beneficiaryRegID = :beneficiaryRegID")
	List<Object[]> fetchBenCallRemarks(@Param("beneficiaryRegID") Long beneficiaryRegID);

	@Query("SELECT b.callTypeID FROM BeneficiaryCall b WHERE b.beneficiaryRegID = :beneficiaryRegID")
	Integer fetchCallTypeID(@Param("beneficiaryRegID") Long beneficiaryRegID);

}
