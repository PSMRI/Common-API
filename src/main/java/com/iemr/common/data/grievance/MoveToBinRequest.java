package com.iemr.common.data.grievance;

import lombok.Data;

@Data
public class MoveToBinRequest {

	private Integer providerServiceMapID;
	private Integer assignedUserID;
	private String preferredLanguageName;
	private Boolean is1097;
	private Integer noOfCalls;

}
