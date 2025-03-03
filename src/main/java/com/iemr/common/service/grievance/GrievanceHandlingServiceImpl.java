package com.iemr.common.service.grievance;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iemr.common.data.callhandling.CallType;
import com.iemr.common.data.grievance.GetGrievanceWorklistRequest;
import com.iemr.common.data.grievance.GrievanceAllocationRequest;
import com.iemr.common.data.grievance.GrievanceDetails;
import com.iemr.common.data.grievance.GrievanceReallocationRequest;
import com.iemr.common.data.grievance.GrievanceResponse;
import com.iemr.common.data.grievance.MoveToBinRequest;
import com.iemr.common.dto.grivance.GrievanceTransactionDTO;
import com.iemr.common.dto.grivance.GrievanceWorklistDTO;
import com.iemr.common.repository.callhandling.BeneficiaryCallRepository;
import com.iemr.common.repository.callhandling.IEMRCalltypeRepositoryImplCustom;
import com.iemr.common.repository.grievance.GrievanceDataRepo;
import com.iemr.common.repository.grievance.GrievanceOutboundRepository;
import com.iemr.common.utils.exception.IEMRException;
import com.iemr.common.utils.mapper.InputMapper;

import jakarta.transaction.Transactional;

@Service
public class GrievanceHandlingServiceImpl implements GrievanceHandlingService {

	private Logger logger = LoggerFactory.getLogger(GrievanceHandlingServiceImpl.class);

	private final GrievanceDataRepo grievanceDataRepo;
	private final GrievanceOutboundRepository grievanceOutboundRepo;
	private final BeneficiaryCallRepository beneficiaryCallRepo;
	private final IEMRCalltypeRepositoryImplCustom iEMRCalltypeRepositoryImplCustom;

	@Autowired
	public GrievanceHandlingServiceImpl(GrievanceDataRepo grievanceDataRepo, GrievanceOutboundRepository grievanceOutboundRepo, 
			BeneficiaryCallRepository beneficiaryCallRepo, IEMRCalltypeRepositoryImplCustom iEMRCalltypeRepositoryImplCustom) {
		this.grievanceDataRepo = grievanceDataRepo;
		this.grievanceOutboundRepo = grievanceOutboundRepo;
		this.beneficiaryCallRepo = beneficiaryCallRepo;
		this.iEMRCalltypeRepositoryImplCustom = iEMRCalltypeRepositoryImplCustom;
	}

	@Value("${grievanceAllocationRetryConfiguration}")
	private Integer grievanceAllocationRetryConfiguration; // Value from application.properties, can be used to
															// configure
															// retry logic

	private InputMapper inputMapper = new InputMapper(); // InputMapper used to map the JSON request

	@Override
	public String allocateGrievances(String request) throws Exception {
		// Step 1: Parse the request string into the appropriate
		// GrievanceAllocationRequest object
		GrievanceAllocationRequest allocationRequest = InputMapper.gson().fromJson(request,
				GrievanceAllocationRequest.class);

		// Step 2: Fetch grievances based on the start date, end date range, and
		// language
		List<GrievanceDetails> grievances = grievanceDataRepo.findGrievancesInDateRangeAndLanguage(
				allocationRequest.getStartDate(), allocationRequest.getEndDate(),
				allocationRequest.getPreferredLanguage());

		if (grievances.isEmpty()) {
			throw new Exception("No grievances found in the given date range and language.");
		}

		// Step 3: Sort grievances in ascending order based on creation date
		grievances.sort(Comparator.comparing(GrievanceDetails::getCreatedDate));

		// Step 4: Get the allocation parameters from the request
		int totalAllocated = 0;
		int userIndex = 0;
		List<Integer> userIds = allocationRequest.getUserID();
		int allocateNo = allocationRequest.getAllocateNo(); // Number of grievances to allocate per user

		for (int i = 0; i < grievances.size(); i++) {
			Integer userId = userIds.get(userIndex);
			GrievanceDetails grievance = grievances.get(i);

			int rowsAffected = grievanceDataRepo.allocateGrievance(grievance.getGrievanceId(), userId);
			if (rowsAffected > 0) {
				totalAllocated++;
				logger.debug("Allocated grievance ID {} to user ID {}", grievance.getGrievanceId(), userId);
			} else {
				logger.error("Failed to allocate grievance ID {} to user ID {}", grievance.getGrievanceId(), userId);
			}

			// Move to the next user after allocateNo grievances
			if ((i + 1) % allocateNo == 0) {
				userIndex = (userIndex + 1) % userIds.size();
			}
		}

		// Step 6: Return a message with the total number of grievances allocated
		return "Successfully allocated " + totalAllocated + " grievances to users.";
	}

	@Override
	public String allocatedGrievanceRecordsCount(String request) throws IEMRException, JSONException {
		GrievanceDetails grievanceRequest = InputMapper.gson().fromJson(request, GrievanceDetails.class);

		Integer providerServiceMapID = grievanceRequest.getProviderServiceMapID();
		Integer userID = grievanceRequest.getUserID();

		Set<Object[]> resultSet = grievanceDataRepo.fetchGrievanceRecordsCount(providerServiceMapID, userID);

		JSONObject result = new JSONObject();
		result.put("All", 0);

		if (resultSet != null && !resultSet.isEmpty()) {
			for (Object[] recordSet : resultSet) {
				String language = ((String) recordSet[0]).trim();
				Long count = (Long) recordSet[1];
				result.put(language, count);
				result.put("All", result.getLong("All") + count);
			}
		}

		JSONArray resultArray = new JSONArray();
		Iterator<String> keys = result.keys();
		while (keys.hasNext()) {
			String key = keys.next();
			if (!key.equals("All") || result.getLong(key) > 0) { // Skip "All" if it's empty
				JSONObject temp = new JSONObject();
				temp.put("language", key);
				temp.put("count", result.getLong(key));
				resultArray.put(temp);
			}
		}

		return resultArray.toString();
	}

	@Override
	public String reallocateGrievances(String request) throws Exception {
		// Step 1: Parse the request string into the appropriate
		// GrievanceReallocationRequest object
		GrievanceReallocationRequest reallocationRequest = InputMapper.gson().fromJson(request,
				GrievanceReallocationRequest.class);

		// Step 2: Fetch grievances that are allocated to the 'fromUserId' and match the
		// criteria
		List<GrievanceDetails> grievances = grievanceDataRepo.findAllocatedGrievancesByUserAndLanguage(
				reallocationRequest.getFromUserId(), reallocationRequest.getLanguage());

		if (grievances.isEmpty()) {
			throw new Exception("No grievances found for the given user and language.");
		}

		// Step 3: Sort grievances in ascending order based on creation date
		grievances.sort(Comparator.comparing(GrievanceDetails::getCreatedDate));

		// Step 4: Get the allocation parameters from the request
		int totalReallocated = 0;
		int userIndex = 0;
		List<Integer> toUserIds = reallocationRequest.getTouserID();
		int allocateNo = reallocationRequest.getAllocateNo(); // Number of grievances to reallocate per user

		// Step 5: Reallocate grievances to users in a round-robin fashion
		for (int i = 0; i < grievances.size(); i++) {
			if (i % allocateNo == 0 && userIndex < toUserIds.size()) {
				// Reallocate to the next user when reaching the allocateNo threshold
				Integer toUserId = toUserIds.get(userIndex);
				GrievanceDetails grievance = grievances.get(i);

				// Call the repository method to reallocate the grievance to the new user
				int rowsAffected = grievanceDataRepo.reallocateGrievance(grievance.getGrievanceId(), toUserId);

				if (rowsAffected > 0) {
					totalReallocated++;
					logger.debug("Reallocated grievance ID " + grievance.getGrievanceId() + " to user ID " + toUserId);
				} else {
					logger.error("Failed to reallocate grievance ID " + grievance.getGrievanceId() + " to user ID "
							+ toUserId);
				}

				userIndex = (userIndex + 1) % toUserIds.size();
			}
		}

		// Step 6: Return a message with the total number of grievances reallocated
		return "Successfully reallocated " + totalReallocated + " grievances to users.";
	}

	@Override
	public String moveToBin(String request) throws Exception {
		// Step 1: Parse the request string into the appropriate MoveToBinRequest object
		MoveToBinRequest moveToBinRequest = InputMapper.gson().fromJson(request, MoveToBinRequest.class);

		// Step 2: Fetch grievances based on assigned user, language
		// condition
		List<GrievanceDetails> grievances = grievanceDataRepo.findGrievancesByUserAndLanguage(
				moveToBinRequest.getUserID(), moveToBinRequest.getPreferredLanguageName());

		if (grievances.isEmpty()) {
			throw new Exception("No grievances found for the given user, language, and condition.");
		}

		// Step 3: Filter grievances to select only the required number of calls
		List<GrievanceDetails> grievancesToMove = new ArrayList<>();
		int count = 0;

		for (GrievanceDetails grievance : grievances) {
			if (count >= moveToBinRequest.getNoOfCalls()) {
				break; // Stop once we've selected the required number of grievances
			}
			grievancesToMove.add(grievance);
			count++;
		}

		if (grievancesToMove.isEmpty()) {
			throw new Exception("No grievances found to move to bin.");
		}

		// Step 4: Unassign grievances from the user and "move them to bin"
		int totalUnassigned = 0;
		for (GrievanceDetails grievance : grievancesToMove) {
			int rowsAffected = grievanceDataRepo.unassignGrievance(grievance.getGrievanceId(),
					moveToBinRequest.getUserID());
			if (rowsAffected > 0) {
				grievance.setIsAllocated(false); // Assuming there's a setter for this flag
				int updateFlagResult = grievanceDataRepo.updateGrievanceAllocationStatus(grievance.getGrievanceId(),
						false);
				if (updateFlagResult > 0) {
					totalUnassigned++;
					logger.debug("Unassigned grievance ID {} from user ID {}", grievance.getGrievanceId(),
							moveToBinRequest.getUserID());
				} else {
					logger.error("Failed to unassign grievance ID {} from user ID {}", grievance.getGrievanceId(),
							moveToBinRequest.getUserID());
				}
			} else {
				logger.error("Failed to unassign grievance ID {} from user ID {}", grievance.getGrievanceId(),
						moveToBinRequest.getUserID());
			}
		}

		// Step 5: Return the response as count of successfully unassigned grievances
		return totalUnassigned + " grievances successfully moved to bin.";
	}
	 
		@Transactional
	    public List<GrievanceWorklistDTO> getFormattedGrievanceData(String request) throws Exception {
			if (request == null || request.trim().isEmpty()) {
				throw new IllegalArgumentException("Request cannot be null or empty");
			}
			
			GetGrievanceWorklistRequest getGrievanceWorklistRequest = InputMapper.gson().fromJson(request, GetGrievanceWorklistRequest.class);

	    	List<GrievanceWorklistDTO> formattedGrievances = new ArrayList<>();

	        // Fetch grievance worklist data using @Procedure annotation
	        List<Object[]> worklistData;
	        try {
	        	if (getGrievanceWorklistRequest.getProviderServiceMapID() == null || 
	        		getGrievanceWorklistRequest.getUserId() == null) {
	        			throw new IllegalArgumentException("ProviderServiceMapID and UserId are required");
	        		}
	        worklistData = grievanceOutboundRepo.getGrievanceWorklistData(getGrievanceWorklistRequest.getProviderServiceMapID(), getGrievanceWorklistRequest.getUserId());
	        if (worklistData == null || worklistData.isEmpty()) {
	        	logger.info("No grievance data found for the given criteria");
	        	return new ArrayList<>();
	        }
	       }
	        catch (Exception e) {
	        	logger.error("Failed to fetch grievance data: {}", e.getMessage());
	        	throw new Exception("Failed to retrieve grievance data", e);
	        }
	        
	        // Loop through the worklist data and format the response
	        for (Object[] row : worklistData) {
	        	if (row == null || row.length < 28)
	        	{
	        		logger.warn("invalid row data received");
	        		continue;
	        	}
	            GrievanceWorklistDTO grievance = new GrievanceWorklistDTO(
	                (String) row[0], // complaintID
	                (String) row[1], // subjectOfComplaint
	                (String) row[2], // complaint
	                (Long) row[3],   // beneficiaryRegID
	                (Integer) row[4],// providerServiceMapID
			(String) row[5], // primaryNumber

	                (String) row[20], // firstName
	                (String) row[21], // lastName
	             
	                new ArrayList<>(),// transactions (initially empty, will be populated later)
	                (String) row[12], // severety
	                (String) row[13], // state
	                (Integer) row[14],// userId
	                (Boolean) row[15],// deleted
	                (String) row[16],// createdBy
	                (Timestamp) row[17], // createdDate
	                (Timestamp) row[18], // lastModDate
	                (Boolean) row[19], // isCompleted
	                (String) row[22], // gender
	                (String) row[23], // district
	                (Long) row[24], // beneficiaryID
	                (String) row[25], // age
	                (Boolean) row[26], // retryNeeded
	                (Integer) row[27] // callCounter
	            );

	            // Extract transactions from the current row and add them to the grievance object
	            GrievanceTransactionDTO transaction = new GrievanceTransactionDTO(
	  
	                (String) row[6], // fileName
	                (String) row[7], // fileType
	                (String) row[8], // redressed
	                (Timestamp) row[9], // createdAt
	                (Timestamp) row[10], // updatedAt
	                (String) row[11] // comment
	            );
	            
	            grievance.getTransactions().add(transaction);  // Add the transaction to the grievance's list

	            // Add the grievance to the result list
	            formattedGrievances.add(grievance);
	        }

	        return formattedGrievances;
	    
	    }

		    /**
		    * Saves the complaint resolution and remarks for a grievance.
		    *
		    * @param request JSON string containing complaint resolution details
		    * @return Success message if the update is successful
		    */
		 
		@Transactional
		public String saveComplaintResolution(String request) throws Exception {
	        // Parse the request JSON into a GrievanceDetails object
	        GrievanceDetails grievanceRequest = InputMapper.gson().fromJson(request, GrievanceDetails.class);

	                if (grievanceRequest.getComplaintID() == null || grievanceRequest.getComplaintID().trim().isEmpty()) {
	                    throw new IllegalArgumentException("ComplaintID is required");
	                }
	                if (grievanceRequest.getComplaintResolution() == null || grievanceRequest.getComplaintResolution().trim().isEmpty()) {
	                    throw new IllegalArgumentException("ComplaintResolution is required");
	                }
	                if (grievanceRequest.getBeneficiaryRegID() == null) {
	                    throw new IllegalArgumentException("BeneficiaryRegID is required");
	                }
	                if (grievanceRequest.getProviderServiceMapID() == null) {
	                    throw new IllegalArgumentException("ProviderServiceMapID is required");
	                }
	                if (grievanceRequest.getUserID() == null) {
	                    throw new IllegalArgumentException("AssignedUserID is required");
	                }
	                if (grievanceRequest.getCreatedBy() == null) {
	                    throw new IllegalArgumentException("CreatedBy is required");
	                }
	        // Extract values from the request
	        String complaintID = grievanceRequest.getComplaintID();
	        String complaintResolution = grievanceRequest.getComplaintResolution();
	        String remarks = grievanceRequest.getRemarks();
	        Long beneficiaryRegID = grievanceRequest.getBeneficiaryRegID();
	        Integer providerServiceMapID = grievanceRequest.getProviderServiceMapID();
	        Integer userID = grievanceRequest.getUserID();
	        String createdBy = grievanceRequest.getCreatedBy();

	      
	        String modifiedBy = createdBy;
	        int updateCount = 0;
	        if (remarks == null) {
	        	updateCount = grievanceDataRepo.updateComplaintResolution(complaintResolution, modifiedBy, complaintID,
                        beneficiaryRegID, providerServiceMapID, userID);
	        	logger.debug("updated complaint resolution without remarks for complaint id: {}", complaintID);
	        }
	        else {
	        updateCount = grievanceDataRepo.updateComplaintResolution(complaintResolution, remarks,  modifiedBy, complaintID, 
	                                                                      beneficiaryRegID, providerServiceMapID, userID);
        	logger.debug("updated complaint resolution with remarks for complaint id: {}", complaintID);

	        }
	        if (updateCount > 0) {
	            return "Complaint resolution updated successfully";
	        } else {
	            throw new Exception("Failed to update complaint resolution");
	        }
	    }
		
		
		

		private Date parseDate(String dateStr) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			try {
				return dateFormat.parse(dateStr);
				} catch (ParseException e) {
						logger.error("Error parsing date for grievance: " + dateStr, e);
			throw new IllegalArgumentException("Invalid JSON format in request");
    }
}


		@Override
		public String getGrievanceDetailsWithRemarks(String request) throws Exception {
			   ObjectMapper objectMapper = new ObjectMapper();
			   
		    try {
		        // Parsing request to get the filter parameters (State, ComplaintResolution, StartDate, EndDate)
		        JSONObject requestObj = new JSONObject(request);
		        String complaintResolution = requestObj.optString("ComplaintResolution", null);
		        String state = requestObj.optString("State", null);
		        String fromDate = requestObj.optString("StartDate");
		        String toDate = requestObj.optString("EndDate");

		    	 // Convert StartDate and EndDate to Date objects
			    Date startDateStr = parseDate(fromDate);
			    Date endDateStr = parseDate(toDate);

			List<GrievanceDetails> grievanceDetailsList = grievanceDataRepo.fetchGrievanceDetailsBasedOnParams(state, complaintResolution, startDateStr, endDateStr); // Fetch grievance details based on the request

		    if (grievanceDetailsList == null || grievanceDetailsList.isEmpty()) {
		        return "No grievance details found for the provided request.";
		    }

		    List<GrievanceResponse> grievanceResponseList = new ArrayList<>();
		    
		        
		        // Determine if the complaintResolution is "resolved" or "unresolved" 
		        for (GrievanceDetails grievance : grievanceDetailsList) {
		            GrievanceResponse grievanceResponse = new GrievanceResponse();

		            // Set basic grievance details
		            grievanceResponse.setGrievanceId(grievance.getGrievanceId());
		            grievanceResponse.setComplaintID(grievance.getComplaintID());
		            grievanceResponse.setPrimaryNumber(grievance.getPrimaryNumber());
		            grievanceResponse.setComplaintResolution(grievance.getComplaintResolution());
		            grievanceResponse.setCreatedDate(grievance.getCreatedDate()); 
		            grievanceResponse.setLastModDate(grievance.getLastModDate());

		            // Fetch and set remarks based on complaintResolution value
		            String remarks = "";
		            if ("unresolved".equalsIgnoreCase(complaintResolution)) {
		                // Fetch remarks from t_bencall by joining with t_grievanceworklist based on benRegId
		                remarks = fetchRemarksFromBenCallByComplaint(grievance.getComplaintID());
		            } else if ("resolved".equalsIgnoreCase(complaintResolution)) {
		                // Fetch remarks from t_grievanceworklist
		                remarks = fetchRemarksFromGrievanceWorklist(grievance.getComplaintID());
		            } else {
		                // Default: Fetch remarks based on the grievance's specific conditions (no specific resolution status)
		            	String callRemarks = fetchRemarksFromBenCallByComplaint(grievance.getComplaintID());
		            	if(remarks != null && !remarks.startsWith("No remarks found")) {
		            		remarks = callRemarks;
		            	}
		            	else {
			                remarks = fetchRemarksFromGrievanceWorklist(grievance.getComplaintID());

		            	}
		            }
		            
		            grievanceResponse.setRemarks(remarks);
		            
		            // Add to response list
		            grievanceResponseList.add(grievanceResponse);
		        }

		        // Convert the list of GrievanceResponse objects to JSON and return as a string
		        return objectMapper.writeValueAsString(grievanceResponseList);
		        
		    } catch (Exception e) {
		        logger.error("Error while getting grievance details with remarks: " + e.getMessage(), e);
		        throw new Exception("Error processing grievance request");
		    }
		}
		


		private String fetchRemarksFromBenCallByComplaint(String complaintID) throws JSONException {
		    // Query t_grievanceworklist to fetch the benRegId based on complaintID
		    List<GrievanceDetails> grievanceWorklist = grievanceDataRepo.fetchGrievanceWorklistByComplaintID(complaintID);

		    if (grievanceWorklist != null && !grievanceWorklist.isEmpty()) {
		        GrievanceDetails grievance = grievanceWorklist.get(0);
		        Long beneficiaryRegID = grievance.getBeneficiaryRegID();  // Fetch the beneficiaryRegID from the grievance

		        // Query t_bencall to fetch remarks based on benRegId
		        List<Object[]> benCallResults = beneficiaryCallRepo.fetchBenCallRemarks(beneficiaryRegID);

		        if (benCallResults != null && !benCallResults.isEmpty()) {
		            return (String) benCallResults.get(0)[0];  // Fetch the remarks
		        }
		    }
		    
		    return "No remarks found in t_bencall";
		}

		    private String fetchRemarksFromGrievanceWorklist(String complaintID) throws JSONException {
		        // Query t_grievanceworklist to fetch remarks based on complaintID
		        List<Object[]> grievanceWorklistResults = grievanceDataRepo.fetchGrievanceWorklistRemarks(complaintID);

		        if (grievanceWorklistResults != null && !grievanceWorklistResults.isEmpty()) {
		            // Assuming grievanceWorklistResults has a format like [remarks] for simplicity
		            return (String) grievanceWorklistResults.get(0)[0];  // Fetch the remarks
		        }
		        return "No remarks found in t_grievanceworklist";
		    }
		    
    
}

