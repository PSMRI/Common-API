package com.iemr.common.data.grievance;

import lombok.Data;

@Data
public class GrievanceCallRequest {

	  String complaintID;
	  Integer userID;
      Boolean isCompleted;
      Long beneficiaryRegID;
      Integer callTypeID;
      Long benCallID;
      Integer providerServiceMapId;
      String createdBy;
	
}
