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



