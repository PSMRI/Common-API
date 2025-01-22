package com.iemr.common.controller.grievance;


import com.iemr.common.service.grievance.GrievanceDataSync;
import com.iemr.common.utils.exception.IEMRException;
import com.iemr.common.utils.response.OutputResponse;

import io.swagger.v3.oas.annotations.Operation;


import javax.ws.rs.core.MediaType;

import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GrievanceController {
	final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    
    private GrievanceDataSync grievanceDataSync;

    @Autowired
    public GrievanceController(GrievanceDataSync grievanceDataSync) {
    	this.grievanceDataSync = grievanceDataSync;
    }
    
    @CrossOrigin()
   	@Operation(summary = "/unallocatedGrievanceCount")
   	@PostMapping(value = "/unallocatedGrievanceCount", consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
       public String fetchUnallocatedGrievanceCount() {
       	OutputResponse responseData = new OutputResponse();
       	try {
   			responseData.setResponse(grievanceDataSync.fetchUnallocatedGrievanceCount());
   		}
       	catch (IEMRException e) {
       		logger.error("Business logic error in UnallocatedGrievanceCount" + e.getMessage(), e);
       		responseData.setError(e);
       	}
       	catch (JSONException e) {
       		logger.error("JSON processing error in UnallocatedGrievanceCount" + e.getMessage(), e);
       		responseData.setError(e);
       	}
       	catch (Exception e) {
   			logger.error("UnallocatedGrievanceCount failed with error" + e.getMessage(), e);
   			responseData.setError(e);
   		}
   return responseData.toString();
           
       }
}
