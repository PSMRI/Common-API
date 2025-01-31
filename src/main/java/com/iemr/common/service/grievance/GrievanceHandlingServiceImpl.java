package com.iemr.common.service.grievance;

import java.util.ArrayList;
import java.util.Comparator;
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

import com.iemr.common.data.grievance.GrievanceAllocationRequest;
import com.iemr.common.data.grievance.GrievanceDetails;
import com.iemr.common.data.grievance.GrievanceReallocationRequest;
import com.iemr.common.data.grievance.MoveToBinRequest;
import com.iemr.common.repository.grievance.GrievanceDataRepo;
import com.iemr.common.utils.exception.IEMRException;
import com.iemr.common.utils.mapper.InputMapper;

@Service
public class GrievanceHandlingServiceImpl implements GrievanceHandlingService {

	private Logger logger = LoggerFactory.getLogger(GrievanceHandlingServiceImpl.class);

	private final GrievanceDataRepo grievanceDataRepo;

	@Autowired
	public GrievanceHandlingServiceImpl(GrievanceDataRepo grievanceDataRepo) {
		this.grievanceDataRepo = grievanceDataRepo;
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
		logger.debug("Request received for allocatedGrievanceRecordsCount: " + request);
		GrievanceDetails grievanceRequest = InputMapper.gson().fromJson(request, GrievanceDetails.class);

		Integer providerServiceMapID = grievanceRequest.getProviderServiceMapID();
		Integer assignedUserID = grievanceRequest.getAssignedUserID();

		Set<Object[]> resultSet = grievanceDataRepo.fetchGrievanceRecordsCount(providerServiceMapID, assignedUserID);

		JSONObject result = new JSONObject();
		result.put("All", 0);

		if (resultSet != null && !resultSet.isEmpty()) {
			for (Object[] record : resultSet) {
				String language = ((String) record[0]).trim();
				Long count = (Long) record[1];
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
		GrievanceReallocationRequest reallocationRequest = inputMapper.gson().fromJson(request,
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

}
