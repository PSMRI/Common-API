package com.iemr.common.service.grievance;

import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import com.iemr.common.data.grievance.GrievanceAllocationRequest;
import com.iemr.common.data.grievance.GrievanceDetails;
import com.iemr.common.repository.grievance.GrievanceDataRepo;
import com.iemr.common.utils.mapper.InputMapper;

@Service
@PropertySource("classpath:application.properties")
public class GrievanceHandlingServiceImpl implements GrievanceHandlingService {


    private Logger logger = LoggerFactory.getLogger(GrievanceHandlingService.class);

    @Autowired
    private GrievanceDataRepo grievanceDataRepo;  // Repository for grievance-related database operations

    @Value("${grievanceAllocationRetryConfiguration}")
    private int grievanceAllocationRetryConfiguration;  // Value from application.properties, can be used to configure retry logic

    private InputMapper inputMapper = new InputMapper();  // InputMapper used to map the JSON request

    @Override
    public String allocateGrievances(String request) throws Exception {
        // Step 1: Parse the request string into the appropriate GrievanceAllocationRequest object
        GrievanceAllocationRequest allocationRequest = inputMapper.gson().fromJson(request, GrievanceAllocationRequest.class);

        // Step 2: Fetch grievances based on the start date, end date range, and language
        List<GrievanceDetails> grievances = grievanceDataRepo.findGrievancesInDateRangeAndLanguage(
                allocationRequest.getStartDate(), allocationRequest.getEndDate(), allocationRequest.getLanguage());

        if (grievances.isEmpty()) {
            throw new Exception("No grievances found in the given date range and language.");
        }

        // Step 3: Sort grievances in ascending order based on creation date
        grievances.sort(Comparator.comparing(GrievanceDetails::getCreatedDate));

        // Step 4: Get the allocation parameters from the request
        int totalAllocated = 0;
        int userIndex = 0;
        List<Integer> userIds = allocationRequest.getUserID();
        int allocateNo = allocationRequest.getAllocateNo();  // Number of grievances to allocate per user

       
        for (int i = 0; i < grievances.size(); i++) {
        	    Integer userId = userIds.get(userIndex);
        	    GrievanceDetails grievance = grievances.get(i);
        	
        	    int rowsAffected = grievanceDataRepo.allocateGrievance(grievance.getGrievanceId(), userId);
        	    if (rowsAffected > 0) {
        	        totalAllocated++;
        	        logger.debug("Allocated grievance ID " + grievance.getGrievanceId() + " to user ID " + userId);
        	   } else {
        	        logger.error("Failed to allocate grievance ID " + grievance.getGrievanceId() + " to user ID " + userId);
        	    }
        	
        	    // Move to the next user after allocateNo grievances
        	    if ((i + 1) % allocateNo == 0) {
        	        userIndex = (userIndex + 1) % userIds.size();
        	    }
        	 }

        // Step 6: Return a message with the total number of grievances allocated
        return "Successfully allocated " + totalAllocated + " grievances to users.";
    }


}
