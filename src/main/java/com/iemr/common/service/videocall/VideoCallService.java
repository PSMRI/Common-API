package com.iemr.common.service.videocall;

import com.iemr.common.model.videocall.UpdateCallRequest;
import com.iemr.common.model.videocall.VideoCallRequest;

public interface VideoCallService {
	
	 public String generateMeetingLink() throws Exception;

	 public String sendMeetingLink(VideoCallRequest request) throws Exception;

	 public String updateCallStatus(UpdateCallRequest request) throws Exception;
}
