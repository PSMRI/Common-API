package com.iemr.common.repository.grievance;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.iemr.common.data.grievance.GrievanceDetails;

import jakarta.transaction.Transactional;

@Repository
public interface GrievanceDataRepo extends CrudRepository<GrievanceDetails, Long> {

	@Query("SELECT COUNT(g) > 0 FROM GrievanceDetails g WHERE g.complaintID = :complaintId")
	boolean existsByComplaintId(@Param("complaintId") String complaintId);


	@Query("SELECT g FROM GrievanceDetails g WHERE g.createdDate BETWEEN :startDate AND :endDate AND g.isAllocated = false AND g.preferredLanguage = :language")
	List<GrievanceDetails> findGrievancesInDateRangeAndLanguage(@Param("startDate") Timestamp startDate,
			@Param("endDate") Timestamp endDate, @Param("language") String language);


	@Modifying
	@Query("UPDATE GrievanceDetails g SET g.isAllocated = true, g.userID = :userId WHERE g.grievanceId = :grievanceId")
	@Transactional
	public int allocateGrievance(@Param("grievanceId") Long grievanceId,
			@Param("userId") Integer userId);

	@Query(nativeQuery = true, value = "SELECT PreferredLanguageId, PreferredLanguage, VanSerialNo, VanID, ParkingPlaceId, VehicalNo FROM db_identity.i_beneficiarydetails WHERE BeneficiaryRegID = :benRegId")
	public ArrayList<Object[]> getBeneficiaryGrievanceDetails(@Param("benRegId") Long benRegId);

	@Query("select grievance.preferredLanguage, count(distinct grievance.grievanceId) "
			+ "from GrievanceDetails grievance " + "where grievance.providerServiceMapID = :providerServiceMapID "
			+ "and grievance.userID = :userID " + "and grievance.deleted = false "
			+ "group by grievance.preferredLanguage")
	public Set<Object[]> fetchGrievanceRecordsCount(@Param("providerServiceMapID") Integer providerServiceMapID,
			@Param("userID") Integer userID);

	@Query("SELECT g FROM GrievanceDetails g WHERE g.userID = :userID AND g.preferredLanguage = :language AND g.isAllocated = true")
	List<GrievanceDetails> findAllocatedGrievancesByUserAndLanguage(@Param("userID") Integer userID,
			@Param("language") String language);

	@Modifying
	@Query("UPDATE GrievanceDetails g SET g.userID = :userID WHERE g.grievanceId = :grievanceId")
	@Transactional
	public int reallocateGrievance(@Param("grievanceId") Long grievanceId,
			@Param("userID") Integer userID);

	@Query("SELECT g FROM GrievanceDetails g WHERE g.userID = :userID "
			+ "AND g.preferredLanguage = :preferredLanguageName")
	List<GrievanceDetails> findGrievancesByUserAndLanguage(@Param("userID") Integer userID,
			@Param("preferredLanguageName") String language);

	@Modifying
	@Transactional
	@Query("UPDATE GrievanceDetails g SET g.userID = NULL WHERE g.grievanceId = :grievanceId AND g.userID = :userID")
	int unassignGrievance(@Param("grievanceId") Long grievanceId, @Param("userID") Integer userID);

	@Modifying
	@Transactional
	@Query("UPDATE GrievanceDetails g SET g.isAllocated = :isAllocated WHERE g.grievanceId = :grievanceId")
	int updateGrievanceAllocationStatus(@Param("grievanceId") Long grievanceId,
			@Param("isAllocated") Boolean isAllocated);

	@Query("Select grievance.preferredLanguage, count(grievance) from GrievanceDetails grievance where grievance.isAllocated=false group by grievance.preferredLanguage")
	public Set<Object[]> fetchUnallocatedGrievanceCount();
	
	@Modifying
	@Query("UPDATE GrievanceDetails g SET g.complaintResolution = :complaintResolution, g.remarks = :remarks, g.modifiedBy =  :modifiedBy "
	       + "WHERE g.complaintID = :complaintID AND g.beneficiaryRegID = :beneficiaryRegID AND g.providerServiceMapID = :providerServiceMapID"
		   + " AND g.userID = :userID")
	@Transactional
	int updateComplaintResolution(@Param("complaintResolution") String complaintResolution,
	                              @Param("remarks") String remarks,
	                              @Param("modifiedBy") String modifiedBy,
	                              @Param("complaintID") String complaintID,
	                              @Param("beneficiaryRegID") Long beneficiaryRegID,
	                              @Param("providerServiceMapID") Integer providerServiceMapID,
	                              @Param("userID") Integer userID);

	@Modifying
	@Query("UPDATE GrievanceDetails g SET g.complaintResolution = :complaintResolution, g.modifiedBy =  :modifiedBy "
	       + "WHERE g.complaintID = :complaintID AND g.beneficiaryRegID = :beneficiaryRegID AND g.providerServiceMapID = :providerServiceMapID"
			+ " AND g.userID = :userID")
	@Transactional
	int updateComplaintResolution(@Param("complaintResolution") String complaintResolution,
			   					  @Param("modifiedBy") String modifiedBy,
	                              @Param("complaintID") String complaintID,
	                              @Param("beneficiaryRegID") Long beneficiaryRegID,
	                              @Param("providerServiceMapID") Integer providerServiceMapID,
	                              @Param("userID") Integer userID);
	
	@Query(" Select grievance.callCounter, grievance.retryNeeded FROM GrievanceDetails grievance where complaintID = :complaintID")
	public List<Object[]> getCallCounter(@Param("complaintID") String complaintID);
	
	@Modifying
	@Query("UPDATE GrievanceDetails g SET g.isCompleted = :isCompleted, g.retryNeeded =  :retryNeeded "
			+ "WHERE g.complaintID = :complaintID AND g.userID = :userID AND g.beneficiaryRegID = :beneficiaryRegID "
			+ "AND g.providerServiceMapID = :providerServiceMapID")
	@Transactional
	public int updateCompletedStatusInCall(@Param("isCompleted") Boolean isCompleted,
			                               @Param("retryNeeded") Boolean retryNeeded,
										   @Param("complaintID") String complaintID,
										   @Param("userID") Integer userID,
										   @Param("beneficiaryRegID") Long beneficiaryRegID,
				                           @Param("providerServiceMapID") Integer providerServiceMapID);
										   

	@Modifying
	@Query("UPDATE GrievanceDetails g SET g.callCounter = :callCounter, g.retryNeeded =  :retryNeeded "
	       + "WHERE g.complaintID = :complaintID AND g.beneficiaryRegID = :beneficiaryRegID AND g.providerServiceMapID = :providerServiceMapID"
			+ " AND g.userID = :userID")
	@Transactional
	public int updateCallCounter(@Param("callCounter") Integer callCounter,
			   					  @Param("retryNeeded") Boolean retryNeeded,
	                              @Param("complaintID") String complaintID,
	                              @Param("beneficiaryRegID") Long beneficiaryRegID,
	                              @Param("providerServiceMapID") Integer providerServiceMapID,
	                              @Param("userID") Integer userID);
	
	@Query("SELECT g FROM GrievanceDetails g WHERE "
	        + "(g.state = :state OR :state IS NULL) "
	        + "AND (g.complaintResolution = :complaintResolution OR :complaintResolution IS NULL) "
	        + "AND g.createdDate BETWEEN :startDate AND :endDate")
	List<GrievanceDetails> fetchGrievanceDetailsBasedOnParams(
	    @Param("state") String state,
	    @Param("complaintResolution") String complaintResolution,
	    @Param("startDate") Date startDate,
	    @Param("endDate") Date endDate);


	@Query("SELECT g FROM GrievanceDetails g WHERE g.complaintID = :complaintID")
	List<GrievanceDetails> fetchGrievanceWorklistByComplaintID(@Param("complaintID") String complaintID);


@Query("SELECT g.remarks FROM GrievanceDetails g WHERE g.complaintID = :complaintID")
List<Object[]> fetchGrievanceWorklistRemarks(@Param("complaintID") String complaintID);



}
