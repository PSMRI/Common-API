package com.iemr.common.data.grievance;

import lombok.Data;

@Data
public class MoveToBinRequest {

	private Integer providerServiceMapID;
	private Integer userID;
	private String preferredLanguageName;
	private Boolean is1097;
	private Integer noOfCalls;

}
