package com.iemr.common.repository.grievance;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.iemr.common.data.grievance.GrievanceDetails;



@Repository
public interface GrievanceOutboundRepository extends JpaRepository<GrievanceDetails, Long> {


	@Query(value =" call db_iemr.Pr_Grievanceworklist(:providerServiceMapID, :userId)", nativeQuery = true)
    List<Object[]> getGrievanceWorklistData(@Param("providerServiceMapID") Integer providerServiceMapID,
    		@Param("userId") Integer userId);

}
