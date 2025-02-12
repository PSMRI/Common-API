package com.iemr.common.data.grievance;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GrievanceReallocationRequest {

	private Integer providerServiceMapId;
	private String language;
	private Integer fromUserId;
	private List<Integer> touserID;
	private Integer allocateNo;

}
