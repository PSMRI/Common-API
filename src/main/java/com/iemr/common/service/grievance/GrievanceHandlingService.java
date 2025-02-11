package com.iemr.common.service.grievance;

import org.json.JSONException;
import org.springframework.stereotype.Service;

import com.iemr.common.utils.exception.IEMRException;

@Service
public interface GrievanceHandlingService {
	public String allocateGrievances(String request) throws Exception;

	public String allocatedGrievanceRecordsCount(String request) throws IEMRException, JSONException;
	
	public String reallocateGrievances(String request) throws Exception;
	
	public String moveToBin(String request) throws Exception;

}
