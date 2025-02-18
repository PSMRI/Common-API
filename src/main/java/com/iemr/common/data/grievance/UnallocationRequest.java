package com.iemr.common.data.grievance;

import java.sql.Timestamp;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UnallocationRequest {
	
	private Integer providerServiceMapID;
	private String preferredLanguageName;
	private Timestamp filterStartDate;
	private Timestamp filterEndDate;

}
