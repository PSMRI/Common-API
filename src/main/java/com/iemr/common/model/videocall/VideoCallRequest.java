package com.iemr.common.model.videocall;


import java.sql.Timestamp;

import lombok.Data;

@Data
public class VideoCallRequest {
	private Timestamp dateOfCall;
	private String callerPhoneNumber;
	private String agentID;
	private String agentName;
	private String meetingLink;
	private String callStatus;
	private String callDuration;
	private Integer providerServiceMapID;
	private Long beneficiaryRegID;
	private String closureRemark;
}
