package com.iemr.common.service.grievance;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.iemr.common.data.grievance.GetGrievanceWorklistRequest;
import com.iemr.common.data.grievance.GrievanceAllocationRequest;
import com.iemr.common.data.grievance.GrievanceDetails;
import com.iemr.common.data.grievance.GrievanceReallocationRequest;
import com.iemr.common.data.grievance.GrievanceWorklist;
import com.iemr.common.data.grievance.MoveToBinRequest;
import com.iemr.common.dto.grivance.GrievanceTransactionDTO;
import com.iemr.common.dto.grivance.GrievanceWorklistDTO;
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

	@Autowired
	public GrievanceHandlingServiceImpl(GrievanceDataRepo grievanceDataRepo, GrievanceOutboundRepository grievanceOutboundRepo) {
		this.grievanceDataRepo = grievanceDataRepo;
		this.grievanceOutboundRepo = grievanceOutboundRepo;
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
		Integer assignedUserID = grievanceRequest.getAssignedUserID();

		Set<Object[]> resultSet = grievanceDataRepo.fetchGrievanceRecordsCount(providerServiceMapID, assignedUserID);

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
				moveToBinRequest.getAssignedUserID(), moveToBinRequest.getPreferredLanguageName());

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
					moveToBinRequest.getAssignedUserID());
			if (rowsAffected > 0) {
				grievance.setIsAllocated(false); // Assuming there's a setter for this flag
				int updateFlagResult = grievanceDataRepo.updateGrievanceAllocationStatus(grievance.getGrievanceId(),
						false);
				if (updateFlagResult > 0) {
					totalUnassigned++;
					logger.debug("Unassigned grievance ID {} from user ID {}", grievance.getGrievanceId(),
							moveToBinRequest.getAssignedUserID());
				} else {
					logger.error("Failed to unassign grievance ID {} from user ID {}", grievance.getGrievanceId(),
							moveToBinRequest.getAssignedUserID());
				}
			} else {
				logger.error("Failed to unassign grievance ID {} from user ID {}", grievance.getGrievanceId(),
						moveToBinRequest.getAssignedUserID());
			}
		}

		// Step 5: Return the response as count of successfully unassigned grievances
		return totalUnassigned + " grievances successfully moved to bin.";
	}
	 
		@Transactional
	    public List<GrievanceWorklistDTO> getFormattedGrievanceData(String request) throws Exception {
			GetGrievanceWorklistRequest getGrievanceWorklistRequest = InputMapper.gson().fromJson(request, GetGrievanceWorklistRequest.class);

	    	List<GrievanceWorklistDTO> formattedGrievances = new ArrayList<>();

	        // Fetch grievance worklist data using @Procedure annotation
	        List<Object[]> worklistData = grievanceOutboundRepo.getGrievanceWorklistData(getGrievanceWorklistRequest.getProviderServiceMapID(), getGrievanceWorklistRequest.getUserId());

	        // Loop through the worklist data and format the response
	        for (Object[] row : worklistData) {
	            GrievanceWorklistDTO grievance = new GrievanceWorklistDTO(
	                (String) row[0], // complaintID
	                (String) row[1], // subjectOfComplaint
	                (String) row[2], // complaint
	                (Long) row[3],   // beneficiaryRegID
	                (Integer) row[4],// providerServiceMapID
			(String) row[5], // primaryNumber

	                (String) row[19], // firstName
	                (String) row[20], // lastName
	             
	                new ArrayList<>(),// transactions (initially empty, will be populated later)
	                (String) row[11], // severety
	                (String) row[12], // state
	                (Integer) row[13],// userId
	                (Boolean) row[14],// deleted
	                (String) row[15],// createdBy
	                (Timestamp) row[16], // createdDate
	                (Timestamp) row[17], // lastModDate
	                (Boolean) row[18], // isCompleted
	                (String) row[21], // gender
	                (String) row[22], // district
	                (Long) row[23], // beneficiaryID
	                (String) row[24], // age
	                (Boolean) row[25], // retryNeeded
	                (Integer) row[26] // callCounter
	                //(String) row[22] // lastCall
	            );

	            // Extract transactions from the current row and add them to the grievance object
	            GrievanceTransactionDTO transaction = new GrievanceTransactionDTO(
	           //     (String) row[23], // actionTakenBy
	           //     (String) row[24], // status
	                (String) row[22], // fileName
	                (String) row[6], // fileType
	                (String) row[7], // redressed
	                (Timestamp) row[8], // createdAt
	                (Timestamp) row[9], // updatedAt
	                (String) row[10] // comment
	            );
	            
	            grievance.getTransactions().add(transaction);  // Add the transaction to the grievance's list

	            // Add the grievance to the result list
	            formattedGrievances.add(grievance);
	        }

	        return formattedGrievances;
	    
	    }
}
