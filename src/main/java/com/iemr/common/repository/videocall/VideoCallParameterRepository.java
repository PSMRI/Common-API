package com.iemr.common.repository.videocall;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import com.iemr.common.data.videocall.VideoCallParameters;
import com.iemr.common.model.videocall.VideoCallRequest;

@Repository
public interface VideoCallParameterRepository extends CrudRepository<VideoCallParameters, Integer>{
	// Fetch all video call requests that are not null and have a valid meeting link
    // @Query("SELECT v FROM VideoCallRequest v WHERE v.meetingLink IS NOT NULL")
    // List<VideoCallParameters> findAllWithMeetingLinks();

	
}


