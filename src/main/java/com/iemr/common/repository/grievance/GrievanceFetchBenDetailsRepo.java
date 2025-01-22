package com.iemr.common.repository.grievance;


import java.util.ArrayList;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.iemr.common.data.feedback.FeedbackDetails;


@Repository
public interface GrievanceFetchBenDetailsRepo extends CrudRepository<FeedbackDetails, Long> {
	
	@Query("select f.requestID, f.benCallID, f.beneficiaryRegID, f.providerServiceMapID, f.stateID "
			+ " from FeedbackDetails f where f.requestID = :requestID order by f.requestID desc")
	public ArrayList<Object[]> findByComplaintId(@Param("requestID") String requestID);

}
