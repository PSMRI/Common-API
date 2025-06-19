package com.iemr.common.model.videocall;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.sql.Timestamp;
import lombok.Data;
import com.google.gson.Gson;

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
    private boolean isLinkUsed;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private Timestamp linkGeneratedAt;

	public String toJson() {
        return new Gson().toJson(this); 
    }
}
