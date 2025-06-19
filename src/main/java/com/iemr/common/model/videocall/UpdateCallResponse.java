package com.iemr.common.model.videocall;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class UpdateCallResponse {
	private String meetingLink;
    private String callStatus;
    private String callDuration;
    private String modifiedBy;
    private boolean isLinkUsed;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private Timestamp lastModified;

    public UpdateCallResponse() {
    }
}
