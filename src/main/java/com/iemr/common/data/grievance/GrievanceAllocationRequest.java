package com.iemr.common.data.grievance;

import java.sql.Timestamp;
import java.util.List;

import lombok.Data;

@Data
public class GrievanceAllocationRequest {

	private Timestamp startDate; // Start date for filtering grievances
	private Timestamp endDate; // End date for filtering grievances
	private List<Integer> userID; // List of user IDs (agents) to whom grievances will be allocated
	private Integer allocateNo; // Number of grievances to be allocated to each user
	private String preferredLanguage;

	@Override
	public String toString() {
		return "GrievanceAllocationRequest{" + "startDate=" + startDate + ", endDate=" + endDate + ", userID=" + userID
				+ ", allocateNo=" + allocateNo + ", preferredLanguage=" + preferredLanguage + '}';
	}

}
