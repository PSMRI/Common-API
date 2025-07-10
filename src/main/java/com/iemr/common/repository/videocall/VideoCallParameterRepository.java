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

package com.iemr.common.repository.videocall;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import com.iemr.common.data.videocall.VideoCallParameters;
import com.iemr.common.model.videocall.VideoCallRequest;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;


@Repository
public interface VideoCallParameterRepository extends CrudRepository<VideoCallParameters, Integer> {

	@Transactional
	@Modifying
	@Query("UPDATE VideoCallParameters v SET v.callStatus = :callStatus, v.callDuration = :callDuration, v.modifiedBy = :modifiedBy WHERE v.meetingLink = :meetingLink")
	int updateCallStatusByMeetingLink(@Param("meetingLink") String meetingLink,
	                                  @Param("callStatus") String callStatus,
	                                  @Param("callDuration") String callDuration,
	                                  @Param("modifiedBy") String modifiedBy);

	@Query("SELECT v FROM VideoCallParameters v WHERE v.meetingLink = :meetingLink")
	VideoCallParameters findByMeetingLink(@Param("meetingLink") String meetingLink);

}



