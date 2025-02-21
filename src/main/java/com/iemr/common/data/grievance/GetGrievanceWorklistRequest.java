package com.iemr.common.data.grievance;

import lombok.Data;

@Data
public class GetGrievanceWorklistRequest {
	   private Integer providerServiceMapID;
	    private Integer userId;

	    // Constructor
	    public GetGrievanceWorklistRequest(Integer providerServiceMapID, Integer userId) {
	        this.providerServiceMapID = providerServiceMapID;
	        this.userId = userId;
	    }

}
