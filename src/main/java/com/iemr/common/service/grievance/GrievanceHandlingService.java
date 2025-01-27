package com.iemr.common.service.grievance;

import org.springframework.stereotype.Service;

@Service
public interface GrievanceHandlingService {
	public String allocateGrievances(String request) throws Exception;

}
