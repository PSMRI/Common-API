package com.iemr.common.model.videocall;

import lombok.Data;

@Data
public class UpdateCallRequest {
	 String meetingLink;
	 String callStatus; 
	 String callDuration; 
	 String modifiedBy;
}
