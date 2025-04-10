package com.iemr.common.model.videocall;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class UpdateCallResponse {
	String meetingLink;
    String callStatus;
    String callDuration;
    String modifiedBy;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    Timestamp lastModified;

    public UpdateCallResponse() {
    }
}
