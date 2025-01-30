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
package com.iemr.common.repository.report;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.iemr.common.data.callhandling.BeneficiaryCall;
import com.iemr.common.data.report.CallDetailsReport;
import com.iemr.common.data.report.CallReport;

@Repository
public interface CallReportRepo extends CrudRepository<BeneficiaryCall, Integer> {
	@Query("select report from BeneficiaryCall report "
			+ "where (report.cZcallDuration is null or report.recordingPath is null) and report.agentID is not null and report.phoneNo!='undefined' and report.phoneNo is not null "
			+ "and report.createdDate >= :startDate and report.createdDate <= :endDate ")
	public List<BeneficiaryCall> getAllBenCallIDetails(@Param("startDate") Timestamp startDate, @Param("endDate") Timestamp endDate);//start date and end date as param for x days.

	@Query("SELECT call FROM BeneficiaryCall call WHERE call.callID = :sessionID and call.phoneNo like %:phoneNo%")
	public BeneficiaryCall getBenCallDetailsBySessionIDAndPhone(@Param("sessionID") String sessionID,@Param("phoneNo") String phoneNo);
	
	@Transactional
	@Modifying
	@Query("update BeneficiaryCall set isOutbound= :isOutbound where callID = :callID and phoneNo= :phoneNo")
	public int updateIsOutboundForCall(@Param("isOutbound") boolean isOutbound, @Param("callID") String callID, @Param("phoneNo") String phoneNo );

	@Transactional
	@Modifying
	@Query("update BeneficiaryCall set isOutbound= :isOutbound, callTypeID= :callTypeID where callID = :callID and phoneNo= :phoneNo")
	public int updateIsOutboundForCallWithCallType(@Param("isOutbound") boolean isOutbound, @Param("callID") String callID, @Param("phoneNo") String phoneNo, @Param("callTypeID") Integer callTypeID );
}


