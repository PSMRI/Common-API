package com.iemr.common.service.grievance;

import java.util.List;
import java.util.Map;

import org.json.JSONException;

import com.iemr.common.utils.exception.IEMRException;

public interface GrievanceDataSync {
	public List<Map<String, Object>> dataSyncToGrievance();
	
	public String fetchUnallocatedGrievanceCount() throws IEMRException, JSONException;

}
