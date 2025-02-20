package com.iemr.common.repository.grievance;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.stereotype.Repository;

import com.iemr.common.data.grievance.GrievanceDetails;
import com.iemr.common.data.grievance.GrievanceWorklist;
import com.iemr.common.dto.grivance.GrievanceWorklistDTO;

import io.lettuce.core.dynamic.annotation.Param;
import jakarta.transaction.Transactional;

@Repository
public interface GrievanceOutboundRepository extends JpaRepository<GrievanceDetails, Long> {

	@Transactional
	@Procedure(procedureName = "Pr_Grievanceworklist")
    List<Object[]> getGrievanceWorklistData(@Param("providerServiceMapId") Integer providerServiceMapId,
                                            @Param("userId") Integer userId);

}
