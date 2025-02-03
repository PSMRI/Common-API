package com.iemr.common.controller.grievance;

import javax.ws.rs.core.MediaType;

import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.iemr.common.data.grievance.UnallocationRequest;
import com.iemr.common.service.grievance.GrievanceDataSync;
import com.iemr.common.service.grievance.GrievanceHandlingService;
import com.iemr.common.utils.exception.IEMRException;
import com.iemr.common.utils.response.OutputResponse;

import io.lettuce.core.dynamic.annotation.Param;
import io.swagger.v3.oas.annotations.Operation;

@RestController
public class GrievanceController {
	final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	private GrievanceDataSync grievanceDataSync;

	private final GrievanceHandlingService grievanceHandlingService;

	@Autowired
	public GrievanceController(GrievanceHandlingService grievanceHandlingService, GrievanceDataSync grievanceDataSync) {
		this.grievanceDataSync = grievanceDataSync;
		this.grievanceHandlingService = grievanceHandlingService;
	}

	@Operation(summary = "/unallocatedGrievanceCount")
	@PostMapping(value = "/unallocatedGrievanceCount", consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String fetchUnallocatedGrievanceCount(@RequestBody UnallocationRequest request) {
		OutputResponse responseData = new OutputResponse();
		try {
			responseData.setResponse(grievanceDataSync.fetchUnallocatedGrievanceCount(request.getPreferredLanguageName()));
		} catch (IEMRException e) {
			logger.error("Business logic error in UnallocatedGrievanceCount" + e.getMessage(), e);
			responseData.setError(e);
		} catch (JSONException e) {
			logger.error("JSON processing error in UnallocatedGrievanceCount" + e.getMessage(), e);
			responseData.setError(e);
		} catch (Exception e) {
			logger.error("UnallocatedGrievanceCount failed with error" + e.getMessage(), e);
			responseData.setError(e);
		}
		return responseData.toString();

	}

	@Operation(summary = "Allocate grievances to users")
	@PostMapping(value = "/allocateGrievances", consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String allocateGrievances(
			@Param(value = "{\"startDate\":\"ISO-8601 format start date (e.g., 2022-12-01T07:49:00.000Z)\", "
					+ "\"endDate\":\"ISO-8601 format end date (e.g., 2025-01-16T07:49:30.561)\", "
					+ "\"userID\":\"Array list of User IDs (agents to be allocated grievances)\", "
					+ "\"allocateNo\":\"Integer - number of grievances to be allocated to each user\","
					+ "\"language\":\"String - language to filter grievances by\"}")

			@RequestBody String request) {
		OutputResponse response = new OutputResponse();
		try {
			// Call the service to allocate grievances based on the incoming JSON request
			response.setResponse(grievanceHandlingService.allocateGrievances(request));
		} catch (Exception e) {
			logger.error("Grievance allocation failed with error: " + e.getMessage(), e);
			response.setError(e);
		}
		return response.toString();
	}

	@Operation(summary = "Allocated Grievance Records Count")
	@RequestMapping(value = "/allocatedGrievanceRecordsCount", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String allocatedGrievanceRecordsCount(@Param(value = "{\"providerServiceMapID\":\"Service ID integer\", "
			+ "\"assignedUserID\":\"Optional - Integer user ID to whom grievances are assigned\"}") @RequestBody String request) {
		OutputResponse response = new OutputResponse();
		try {
			response.setResponse(grievanceHandlingService.allocatedGrievanceRecordsCount(request));
		} catch (Exception e) {
			logger.error("allocatedGrievanceRecordsCount failed with error " + e.getMessage(), e);
			response.setError(e);
		}
		return response.toString();
	}

	@Operation(summary = "Reallocate grievances to other users")
	@RequestMapping(value = "/reallocateGrievances", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String reallocateGrievances(@RequestBody String request) {
		OutputResponse response = new OutputResponse();
		try {
			// Call the service to reallocate grievances based on the incoming JSON request
			response.setResponse(grievanceHandlingService.reallocateGrievances(request));
		} catch (Exception e) {
			logger.error("Grievance reallocation failed with error: " + e.getMessage(), e);
			response.setError(e);
		}
		return response.toString();
	}

	@Operation(summary = "Move grievances to bin (unassign from agent)")
	@RequestMapping(value = "/moveToBin", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String moveToBin(@RequestBody String request) {

		OutputResponse response = new OutputResponse();
		try {
			response.setResponse(grievanceHandlingService.moveToBin(request));
		} catch (Exception e) {
			logger.error("Move to bin failed with error: " + e.getMessage(), e);
			response.setError(e);
		}
		return response.toString();
	}

}
