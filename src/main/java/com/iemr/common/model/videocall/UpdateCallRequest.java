package com.iemr.common.model.videocall;

import lombok.Data;

@Data
public class UpdateCallRequest {
	
	private String meetingLink;
	 private String callStatus; 
	 private String callDuration; 
	 private String modifiedBy;
}
