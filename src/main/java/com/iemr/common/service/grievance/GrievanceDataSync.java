package com.iemr.common.service.grievance;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import org.json.JSONException;

import com.iemr.common.utils.exception.IEMRException;

public interface GrievanceDataSync {
//	public List<Map<String, Object>> dataSyncToGrievance();
	
	public String dataSyncToGrievance();

	
	public String fetchUnallocatedGrievanceCount(String preferredLanguage, Timestamp filterStartDate, 
			Timestamp filterEndDate, Integer providerServiceMapID) throws IEMRException, JSONException;

	public String completeGrievanceCall(String request) throws Exception;

}
