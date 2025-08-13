package com.iemr.common.service.grievance;

import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.iemr.common.data.grievance.GetGrievanceWorklistRequest;
import com.iemr.common.data.grievance.GrievanceAllocationRequest;
import com.iemr.common.data.grievance.GrievanceDetails;
import com.iemr.common.data.grievance.GrievanceReallocationRequest;
import com.iemr.common.data.grievance.GrievanceResponse;
import com.iemr.common.data.grievance.MoveToBinRequest;
import com.iemr.common.dto.grivance.GrievanceTransactionDTO;
import com.iemr.common.dto.grivance.GrievanceWorklistDTO;
import com.iemr.common.repository.callhandling.BeneficiaryCallRepository;
import com.iemr.common.repository.grievance.GrievanceDataRepo;
import com.iemr.common.repository.grievance.GrievanceOutboundRepository;
import com.iemr.common.repository.grievance.GrievanceTransactionRepo;
import com.iemr.common.utils.exception.IEMRException;
import com.iemr.common.utils.mapper.InputMapper;

import jakarta.transaction.Transactional;

@Service
public class GrievanceHandlingServiceImpl implements GrievanceHandlingService {

	private Logger logger = LoggerFactory.getLogger(GrievanceHandlingServiceImpl.class);

	private final GrievanceDataRepo grievanceDataRepo;
	private final GrievanceOutboundRepository grievanceOutboundRepo;
	private final BeneficiaryCallRepository beneficiaryCallRepo;
	private final GrievanceTransactionRepo grievanceTransactionRepo;

	@Autowired
	public GrievanceHandlingServiceImpl(GrievanceDataRepo grievanceDataRepo, GrievanceOutboundRepository grievanceOutboundRepo, 
			BeneficiaryCallRepository beneficiaryCallRepo,GrievanceTransactionRepo grievanceTransactionRepo) {
		this.grievanceDataRepo = grievanceDataRepo;
		this.grievanceOutboundRepo = grievanceOutboundRepo;
		this.beneficiaryCallRepo = beneficiaryCallRepo;
		this.grievanceTransactionRepo = grievanceTransactionRepo;
	}

	@Value("${grievanceAllocationRetryConfiguration}")
	private Integer grievanceAllocationRetryConfiguration; // Value from application.properties, can be used to
															// configure
															// retry logic

	
	@Override
	public String allocateGrievances(String request) throws Exception {
	    // Step 1: Parse the request string into the appropriate GrievanceAllocationRequest object
	    GrievanceAllocationRequest allocationRequest = InputMapper.gson().fromJson(request, GrievanceAllocationRequest.class);

		// Step 2: Fetch grievances based on the start date, end date range, and
		// language
		List<GrievanceDetails> grievances = grievanceDataRepo.findGrievancesInDateRangeAndLanguage(
				allocationRequest.getStartDate(), allocationRequest.getEndDate(),
				allocationRequest.getLanguage());

		if (grievances.isEmpty()) {
			throw new Exception("No grievances found in the given date range and language.");
		}

	    // Step 3: Get the allocation parameters from the request
	    List<Integer> userIds = allocationRequest.getTouserID();
	    int allocateNo = allocationRequest.getAllocateNo(); // Number of grievances to allocate per user

	    // Step 4: Initialize counters
	    int grievanceIndex = 0;  // Start from the first grievance
	    int totalGrievances = grievances.size();
	    
	    // Step 5: Allocate grievances to users, ensuring each user gets exactly 'allocateNo' grievances
	    for (Integer userId : userIds) {
	        int allocatedToCurrentUser = 0;

	        // Allocate 'allocateNo' grievances to the current user
	        while (allocatedToCurrentUser < allocateNo && grievanceIndex < totalGrievances) {
	            GrievanceDetails grievance = grievances.get(grievanceIndex);

	            // Allocate the grievance to the user
	            int rowsAffected = grievanceDataRepo.allocateGrievance(grievance.getGrievanceId(), userId);
	            if (rowsAffected > 0) {
	                logger.debug("Allocated grievance ID {} to user ID {}", grievance.getGrievanceId(), userId);
	            } else {
	                logger.error("Failed to allocate grievance ID {} to user ID {}", grievance.getGrievanceId(), userId);
	            }

	            grievanceIndex++; // Move to the next grievance
	            allocatedToCurrentUser++; // Increment the number of grievances allocated to the current user
	        }

	        // If we have allocated the specified number of grievances, move on to the next user
	    }

	    // Step 6: Return a message with the total number of grievances allocated
	    return "Successfully allocated " + allocateNo + " grievance to each user.";
	}


	@Override
	public String allocatedGrievanceRecordsCount(String request) throws IEMRException, JSONException {
		GrievanceDetails grievanceRequest = InputMapper.gson().fromJson(request, GrievanceDetails.class);

		Integer userID = grievanceRequest.getUserID();

		Set<Object[]> resultSet = grievanceDataRepo.fetchGrievanceRecordsCount(userID);

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
	    // Step 1: Parse the request string into the appropriate GrievanceReallocationRequest object
	    GrievanceReallocationRequest reallocationRequest = InputMapper.gson().fromJson(request, GrievanceReallocationRequest.class);

	    // Step 2: Fetch grievances that are allocated to the 'fromUserId' and match the criteria
	    List<GrievanceDetails> grievances = grievanceDataRepo.findAllocatedGrievancesByUserAndLanguage(
	            reallocationRequest.getFromUserId(), reallocationRequest.getLanguage());

	    if (grievances.isEmpty()) {
	        throw new Exception("No grievances found for the given user and language.");
	    }

	    // Step 3: Sort grievances in ascending order based on creation date
	    grievances.sort(Comparator.comparing(GrievanceDetails::getCreatedDate));

	    // Step 4: Get the allocation parameters from the request
	    int totalReallocated = 0;
	    int grievanceIndex = 0;  // Start from the first grievance
	    List<Integer> toUserIds = reallocationRequest.getTouserID();
	    int allocateNo = reallocationRequest.getAllocateNo(); // Number of grievances to reallocate per user

	    // Step 5: Reallocate grievances to users
	    for (Integer toUserId : toUserIds) {
	        int allocatedToCurrentUser = 0;

	        // Reallocate 'allocateNo' grievances to the current user
	        while (allocatedToCurrentUser < allocateNo && grievanceIndex < grievances.size()) {
	            GrievanceDetails grievance = grievances.get(grievanceIndex);

	            // Call the repository method to reallocate the grievance to the new user
	            int rowsAffected = grievanceDataRepo.reallocateGrievance(grievance.getGrievanceId(), toUserId);

	            if (rowsAffected > 0) {
	                totalReallocated++;
	                logger.debug("Reallocated grievance ID {} to user ID {}", grievance.getGrievanceId(), toUserId);
	            } else {
	                logger.error("Failed to reallocate grievance ID {} to user ID {}", grievance.getGrievanceId(), toUserId);
	            }

	            grievanceIndex++; // Move to the next grievance
	            allocatedToCurrentUser++; // Increment the number of grievances reallocated to the current user
	        }

	        // If the current user is allocated the specified number of grievances, move to the next user
	    }

	    // Step 6: Return a message with the total number of grievances reallocated
	    return "Successfully reallocated " + totalReallocated + " grievance to user.";
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
			grievance.setUserID(null);
			int rowsAffected = grievanceDataRepo.unassignGrievance(grievance.getUserID(), grievance.getGwid());
			if (rowsAffected > 0) {
				grievance.setIsAllocated(false); // Assuming there's a setter for this flag
				int updateFlagResult = grievanceDataRepo.updateGrievanceAllocationStatus(grievance.getGwid(),
						grievance.getIsAllocated());
				if (updateFlagResult > 0) {
					totalUnassigned++;
					logger.debug("Unassigned grievance gwid {} from user ID {}", grievance.getGwid(),
							moveToBinRequest.getUserID());
				} else {
					logger.error("Failed to unassign grievance gwid {} from user ID {}", grievance.getGwid(),
							moveToBinRequest.getUserID());
				}
			} else {
				logger.error("Failed to unassign grievance gwid {} from user ID {}", grievance.getGwid(),
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
	        	if (getGrievanceWorklistRequest.getUserId() == null) {
	        			throw new IllegalArgumentException("UserId are required");
	        		}
	        worklistData = grievanceOutboundRepo.getGrievanceWorklistData(getGrievanceWorklistRequest.getUserId());
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
	        	if (row == null || row.length < 24)
	        	{
	        		logger.warn("invalid row data received");
	        		continue;
	        	}
	        	
	            // Handle age conversion from Double to "x years" format
	            String ageFormatted = null;  // Default value for age if it's not available
	            if (row[20] != null) {
	                Long age = (Long) row[20];
	                ageFormatted = age.intValue() + " years";  // Convert the age to integer and append " years"
	            }
	            
	            GrievanceWorklistDTO grievance = new GrievanceWorklistDTO(
	                (String) row[0], // complaintID
	                (Long) row[1], //grievanceId
	                (String) row[2], // subjectOfComplaint
	                (String) row[3], // complaint
	                (Long) row[4],   // beneficiaryRegID
	                (Integer) row[5],// providerServiceMapID
	                (String) row[6], // primaryNumber
	                (String) row[7], // severety
	                (String) row[8], // state
	                (Integer) row[9],// userId
	                (Boolean) row[10],// deleted
	                (String) row[11],// createdBy
	                (Timestamp) row[12], // createdDate
	                (Timestamp) row[13], // lastModDate
	                (Boolean) row[14], // isCompleted
	               
	                (String) row[15], // firstName
	                (String) row[16], // lastName
	                (String) row[17], // gender
	                (String) row[18], // district
	                (Long) row[19], // beneficiaryID
	                ageFormatted,
	                (Boolean) row[21], // retryNeeded
	                (Integer) row[22], // callCounter
	                (Timestamp) row[13], // lastCall
    				(Boolean) row[23]   //beneficiaryConsent

	            );

	            // Extract transactions from the current row and add them to the grievance object
	            List<Object[]> transaction = grievanceTransactionRepo.getGrievanceTransaction((Long) row[1]);
	           List<GrievanceTransactionDTO> arrayList = new ArrayList<>();
	            for (Object[] tras : transaction) {
	            	String actionTakenBy = (tras[0]!=null)?(String) tras[0]:null;
	            	String status = (tras[1]!=null)?(String) tras[1]:null;
	            	String fileName = (tras[2]!=null)?(String) tras[2]:null;
	            	String fileType = (tras[3]!=null)?(String) tras[3]:null;
	            	String redressed = (tras[4]!=null)?(String) tras[4]:null;
	            	Timestamp createdAt = (tras[5]!=null)?(Timestamp) tras[5]:null;
	            	Timestamp updatedAt = (tras[6]!=null)?(Timestamp) tras[6]:null;
	            	String comment = (tras[7]!=null)?(String) tras[7]:null;
					GrievanceTransactionDTO grievanceTransactionDTO = new GrievanceTransactionDTO(actionTakenBy, status, fileName, fileType, redressed, createdAt, updatedAt,comment);
					arrayList.add(grievanceTransactionDTO);
	            }
	            grievance.setTransactions(arrayList);
	            
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
	                if(grievanceRequest.getBenCallID() == null) {
	                	throw new IllegalArgumentException("BencallId is required");
	                }
	        // Extract values from the request
	        String complaintID = grievanceRequest.getComplaintID();
	        String complaintResolution = grievanceRequest.getComplaintResolution();
	        String remarks = grievanceRequest.getRemarks();
	        Long beneficiaryRegID = grievanceRequest.getBeneficiaryRegID();
	        Integer providerServiceMapID = grievanceRequest.getProviderServiceMapID();
	        Integer userID = grievanceRequest.getUserID();
	        String createdBy = grievanceRequest.getCreatedBy();
	        Long benCallID = grievanceRequest.getBenCallID();
	      
	        String modifiedBy = createdBy;
	        int updateCount = 0;
	        if (remarks == null) {
	        	updateCount = grievanceDataRepo.updateComplaintResolution(complaintResolution, modifiedBy, benCallID, complaintID,
                        beneficiaryRegID, userID);
	        	logger.debug("updated complaint resolution without remarks for complaint id: {}", complaintID);
	        }
	        else {
	        updateCount = grievanceDataRepo.updateComplaintResolution(complaintResolution, remarks,  modifiedBy, benCallID, complaintID, 
	                                                                      beneficiaryRegID, userID);
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
			throw new IllegalArgumentException("Invalid date format in request:"+ dateStr);
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

		        if (fromDate == null || toDate == null) {
		        	throw new IllegalArgumentException("fromDate and toDate are required");
		        }
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
		            if(!StringUtils.isEmpty(grievance.getRemarks())) {
		            	remarks = grievance.getRemarks();
		            }else {
		            	remarks = fetchRemarksFromBenCallByComplaint(grievance.getComplaintID());
		            }
		            
		            
		            grievanceResponse.setRemarks(remarks);
		            
		            // Add to response list
		            grievanceResponseList.add(grievanceResponse);
		        }
		        Gson gson = new GsonBuilder()
		        		.serializeNulls()
			            .registerTypeAdapter(Date.class, new JsonSerializer<Date>() {
			                @Override
			                public JsonElement serialize(Date date, Type typeOfSrc, JsonSerializationContext context) {
			                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			                    return context.serialize(sdf.format(date));  // Format date
			                }
			            })
			            .create();
		        return gson.toJson(grievanceResponseList);
		        
		    } catch (Exception e) {
		        logger.error("Error while getting grievance details with remarks: " + e.getMessage(), e);
		        throw new Exception("Error processing grievance request");
		    }
		}
		


		private String fetchRemarksFromBenCallByComplaint(String complaintID) throws Exception {
		    // Query t_grievanceworklist to fetch the benRegId based on complaintID
		    List<GrievanceDetails> grievanceWorklist = grievanceDataRepo.fetchGrievanceWorklistByComplaintID(complaintID);

		    if (grievanceWorklist != null && !grievanceWorklist.isEmpty()) {
		        GrievanceDetails grievance = grievanceWorklist.get(0);
		        Long beneficiaryRegID = grievance.getBeneficiaryRegID();  // Fetch the beneficiaryRegID from the grievance
		        Long benCallID = grievance.getBenCallID();
		        // Query t_bencall to fetch remarks based on benRegId
		        List<Object[]> benCallResults = beneficiaryCallRepo.fetchBenCallRemarks(benCallID);

		        if (benCallResults != null && !benCallResults.isEmpty()) {
		        	if(null != benCallResults.get(0) && null != benCallResults.get(0)[0])
		        		return (String) benCallResults.get(0)[0];  // Fetch the remarks
		        }
		    }
		    
		    return "No remarks found";
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

