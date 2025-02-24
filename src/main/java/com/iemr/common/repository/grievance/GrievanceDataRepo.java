package com.iemr.common.repository.grievance;

import java.sql.Timestamp;
import java.util.ArrayList;
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
	@Query("UPDATE GrievanceDetails g SET g.isAllocated = true, g.assignedUserID = :assignedUserId WHERE g.grievanceId = :grievanceId")
	@Transactional
	public int allocateGrievance(@Param("grievanceId") Long grievanceId,
			@Param("assignedUserId") Integer assignedUserId);

	@Query(nativeQuery = true, value = "SELECT PreferredLanguageId, PreferredLanguage, VanSerialNo, VanID, ParkingPlaceId, VehicalNo FROM db_identity.i_beneficiarydetails WHERE BeneficiaryRegID = :benRegId")
	public ArrayList<Object[]> getBeneficiaryGrievanceDetails(@Param("benRegId") Long benRegId);

	@Query("select grievance.preferredLanguage, count(distinct grievance.grievanceId) "
			+ "from GrievanceDetails grievance " + "where grievance.providerServiceMapID = :providerServiceMapID "
			+ "and grievance.assignedUserID = :assignedUserID " + "and grievance.deleted = false "
			+ "group by grievance.preferredLanguage")
	public Set<Object[]> fetchGrievanceRecordsCount(@Param("providerServiceMapID") Integer providerServiceMapID,
			@Param("assignedUserID") Integer assignedUserID);

	@Query("SELECT g FROM GrievanceDetails g WHERE g.assignedUserID = :assignedUserID AND g.preferredLanguage = :language AND g.isAllocated = true")
	List<GrievanceDetails> findAllocatedGrievancesByUserAndLanguage(@Param("assignedUserID") Integer assignedUserID,
			@Param("language") String language);

	@Modifying
	@Query("UPDATE GrievanceDetails g SET g.assignedUserID = :assignedUserID WHERE g.grievanceId = :grievanceId")
	@Transactional
	public int reallocateGrievance(@Param("grievanceId") Long grievanceId,
			@Param("assignedUserID") Integer assignedUserID);

	@Query("SELECT g FROM GrievanceDetails g WHERE g.assignedUserID = :assignedUserID "
			+ "AND g.preferredLanguage = :preferredLanguageName")
	List<GrievanceDetails> findGrievancesByUserAndLanguage(@Param("assignedUserID") Integer assignedUserID,
			@Param("preferredLanguageName") String language);

	@Modifying
	@Transactional
	@Query("UPDATE GrievanceDetails g SET g.assignedUserID = NULL WHERE g.grievanceId = :grievanceId AND g.assignedUserID = :assignedUserID")
	int unassignGrievance(@Param("grievanceId") Long grievanceId, @Param("assignedUserID") Integer assignedUserID);

	@Modifying
	@Transactional
	@Query("UPDATE GrievanceDetails g SET g.isAllocated = :isAllocated WHERE g.grievanceId = :grievanceId")
	int updateGrievanceAllocationStatus(@Param("grievanceId") Long grievanceId,
			@Param("isAllocated") Boolean isAllocated);

	@Query("Select grievance.preferredLanguage, count(grievance) from GrievanceDetails grievance where grievance.isAllocated=false group by grievance.preferredLanguage")
	public Set<Object[]> fetchUnallocatedGrievanceCount();
	
	@Modifying
	@Query("UPDATE GrievanceDetails g SET g.complaintResolution = :complaintResolution, g.remarks = :remarks, g.modifiedBy =  :modifiedBy, "
	       + "WHERE g.complaintID = :complaintID AND g.beneficiaryRegID = :beneficiaryRegID AND g.providerServiceMapID = :providerServiceMapID"
		   + " AND g.assignedUserID = :assignedUserID")
	@Transactional
	int updateComplaintResolution(@Param("complaintResolution") String complaintResolution,
	                              @Param("remarks") String remark,
	                              @Param("modifiedBy") String modifiedBy,
	                              @Param("complaintID") String complaintID,
	                              @Param("beneficiaryRegID") Long beneficiaryRegID,
	                              @Param("providerServiceMapID") Integer providerServiceMapID,
	                              @Param("assignedUserID") Integer assignedUserID);

	@Modifying
	@Query("UPDATE GrievanceDetails g SET g.complaintResolution = :complaintResolution, g.modifiedBy =  :modifiedBy, "
	       + "WHERE g.complaintID = :complaintID AND g.beneficiaryRegID = :beneficiaryRegID AND g.providerServiceMapID = :providerServiceMapID"
			+ " AND g.assignedUserID = :assignedUserID")
	@Transactional
	int updateComplaintResolution(@Param("complaintResolution") String complaintResolution,
			   					  @Param("modifiedBy") String modifiedBy,
	                              @Param("complaintID") String complaintID,
	                              @Param("beneficiaryRegID") Long beneficiaryRegID,
	                              @Param("providerServiceMapID") Integer providerServiceMapID,
	                              @Param("assignedUserID") Integer assignedUserID);

}
