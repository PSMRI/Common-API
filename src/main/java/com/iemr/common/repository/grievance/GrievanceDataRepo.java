package com.iemr.common.repository.grievance;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.iemr.common.data.grievance.GrievanceDetails;

import jakarta.transaction.Transactional;

@Repository
public interface GrievanceDataRepo  extends CrudRepository<GrievanceDetails, Long>{

	@Query("SELECT COUNT(g) > 0 FROM GrievanceDetails g WHERE g.complaintID = :complaintId")
    boolean existsByComplaintId(@Param("complaintId") String complaintId);
	
	@Query("select count(request) "
			+ "from GrievanceDetails request where request.isAllocated = false")
	public Long fetchUnallocatedGrievanceCount();

    
    @Query("SELECT g FROM GrievanceDetails g WHERE g.createdDate BETWEEN :startDate AND :endDate AND g.isAllocated = false AND g.preferredLanguage = :language")
    List<GrievanceDetails> findGrievancesInDateRangeAndLanguage(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("language") String language);


    @Modifying
    @Query("UPDATE GrievanceDetails g SET g.isAllocated = true, g.userid = :userId WHERE g.grievanceid = :grievanceId")
    @Transactional
    public int allocateGrievance(@Param("grievanceId") Long grievanceId, @Param("userId") Integer userId);


	@Query(nativeQuery = true, value = "SELECT PreferredLanguageId, PreferredLanguage, VanSerialNo, VanID, ParkingPlaceId, VehicalNo FROM db_identity.i_beneficiarydetails WHERE BeneficiaryRegID = :benRegId")
	public ArrayList<Object[]> getBeneficiaryGrievanceDetails(@Param("benRegId") Long benRegId);
	
}
