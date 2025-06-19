package com.iemr.common.controller.grievance;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.iemr.common.data.grievance.UnallocationRequest;
import com.iemr.common.dto.grivance.GrievanceWorklistDTO;
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
			responseData.setResponse(grievanceDataSync.fetchUnallocatedGrievanceCount(request.getPreferredLanguageName(), request.getFilterStartDate(), request.getFilterEndDate(), request.getProviderServiceMapID()));
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
	@PostMapping(value = "/allocatedGrievanceRecordsCount", consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String allocatedGrievanceRecordsCount(@Param(value = "{\"providerServiceMapID\":\"Service ID integer\", "
			+ "\"userID\":\"Optional - Integer user ID to whom grievances are assigned\"}") @RequestBody String request) {
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
	@PostMapping(value = "/reallocateGrievances", consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
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
	@PostMapping(value = "/moveToBin", consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
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
	

	
	  @Operation(summary = "get grievance outbound worklist)")
			@PostMapping(value = "/getGrievanceOutboundWorklist", consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
		    public String getGrievanceOutboundWorklist(@Param(value = "{\"providerServiceMapId\":\" called service ID integer\", "
					+ "\"userId\":\"Optional - Integer ID of user that is assigned to\"}") @RequestBody String request) throws JsonProcessingException {
		        logger.info("Request received for grievance worklist");
		        List<GrievanceWorklistDTO> response = new ArrayList<>();
		        Map<String, Object> responseMap = new HashMap<>();
		        ObjectMapper objectMapper = new ObjectMapper();

				try {
					response = grievanceHandlingService.getFormattedGrievanceData(request);
					  // Prepare the success response structure
			        responseMap.put("data", response);
			        responseMap.put("statusCode", HttpStatus.OK.value());
			        responseMap.put("errorMessage", "Success");
			        responseMap.put("status", "Success");
				}
				
				catch (Exception e) {
					logger.error("grievanceOutboundWorklist failed with error " + e.getMessage(), e);
					List<GrievanceWorklistDTO> errorResponse = new ArrayList<>();
			        GrievanceWorklistDTO errorDTO = new GrievanceWorklistDTO();
			        errorDTO.setComplaint("Error fetching grievance data");
			        errorDTO.setSubjectOfComplaint(e.getMessage());
			        
			        // Return error response with empty list and error message
			        errorResponse.add(errorDTO);
			        
			        responseMap.put("data", errorResponse);
			        responseMap.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
			        responseMap.put("errorMessage", e.getMessage());
			        responseMap.put("status", "Error");
				}
				Gson gson = new GsonBuilder()
			            .registerTypeAdapter(Date.class, new JsonSerializer<Date>() {
			                @Override
			                public JsonElement serialize(Date date, Type typeOfSrc, JsonSerializationContext context) {
			                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			                    return context.serialize(sdf.format(date));  // Format date
			                }
			            })
			            .create();
				
		        return gson.toJson(responseMap);
		        }



	  @Operation(summary = "Save complaint resolution and remarks")
	  @PostMapping(value = "/saveComplaintResolution", consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	    public String saveComplaintResolution(       @Param(value = "{\"complaintID\":\"Complaint ID string\", " +
                "\"complaintResolution\":\"Resolution text\", " +
                "\"remarks\":\"Optional remarks\", " +
                "\"beneficiaryRegID\":\"Beneficiary registration ID\", " +
                "\"providerServiceMapID\":\"Provider service map ID\", " +
                "\"userID\":\"Assigned user ID\", " +
                "\"createdBy\":\"Creator of the complaint\", " +
                "\"benCallID\":\"Beneficiary call ID\"}")
 @RequestBody String request) {
	        OutputResponse response = new OutputResponse();
	        try {
	            response.setResponse(grievanceHandlingService.saveComplaintResolution(request));
	        } catch (Exception e) {
	            logger.error("saveComplaintResolution failed with error " + e.getMessage(), e);
	            response.setError(e);
	        }
	        return response.toString();
	    }
	  
	  
		// Controller method to handle reattempt logic
		  @Operation(summary = "Check reattempt logic for grievance")
		  @PostMapping(value = "/completeGrievanceCall", consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
		  public String completeGrievanceCall(
		      @Param(value = "{\"complaintID\":\"String - ComplaintID\", "
		      		  + "\"userID\":\"Integer - Assigned UserID\", "
		    		  + "\"isCompleted\":\"Boolean - completion status of call\", "
		              + "\"beneficiaryRegId\":\"Long - Beneficiary Registration ID\", "
		              + "\"callTypeID\":\"Integer - Call Type ID\", "
		              + "\"benCallID\":\"Long - Beneficiary Call ID\", "
		              +"\"callID\":\"String - call ID by czentrix\", "
		              + "\"providerServiceMapID\":\"Integer - providerServiceMapID\", "
		              + "\"createdBy\":\"String - Creator\"}") 
		      @RequestBody String request) {

				OutputResponse response = new OutputResponse();

				try {
					String s = grievanceDataSync.completeGrievanceCall(request);
						response.setResponse(s);
			
				} catch (Exception e) {
					logger.error("complete grievance outbound call failed with error " + e.getMessage(), e);
					response.setError(e);
				}
				return response.toString();
		  }


		  @Operation(summary = "Get Grievance Details with Remarks")
		  @PostMapping(value = "/getCompleteGrievanceDetails", consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
		  public String getGrievanceDetailsWithRemarks(@RequestBody String request) {
		      OutputResponse response = new OutputResponse();
		      try {
		          response.setResponse(grievanceHandlingService.getGrievanceDetailsWithRemarks(request));
		      } catch (Exception e) {
		          logger.error("getGrievanceDetailsWithRemarks failed with error " + e.getMessage(), e);
		          response.setError(e);
		      }
		      return response.toString();
		  }

}
