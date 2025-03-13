package com.iemr.common.service.grievance;

import java.util.List;

import org.json.JSONException;
import org.springframework.stereotype.Service;

import com.iemr.common.dto.grivance.GrievanceWorklistDTO;
import com.iemr.common.utils.exception.IEMRException;

@Service
public interface GrievanceHandlingService {
	public String allocateGrievances(String request) throws Exception;

	public String allocatedGrievanceRecordsCount(String request) throws IEMRException, JSONException;
	
	public String reallocateGrievances(String request) throws Exception;
	
	public String moveToBin(String request) throws Exception;
	
	public List<GrievanceWorklistDTO> getFormattedGrievanceData(String request) throws Exception;

	public String saveComplaintResolution(String request) throws Exception;
	
	public String getGrievanceDetailsWithRemarks(String request) throws Exception;


}
