package com.iemr.common.repository.users;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.iemr.common.data.users.AshaSupervisorMapping;

@Repository
public interface AshaSupervisorLoginRepo extends CrudRepository<AshaSupervisorMapping, Long> {

	ArrayList<AshaSupervisorMapping> findBySupervisorUserIDAndDeletedFalse(Integer supervisorUserID);

	// API 1: All ASHAs mapped to a supervisor with facility info
	@Query(value = "SELECT DISTINCT asm.ashaUserID, u.FirstName, u.LastName, "
			+ "f.FacilityID, f.FacilityName, "
			+ "COALESCE(ft.FacilityTypeName,'') AS facilityTypeName, "
			+ "COALESCE(u.EmployeeID,'') AS employeeID "
			+ "FROM asha_supervisor_mapping asm "
			+ "JOIN m_User u ON u.UserID = asm.ashaUserID AND u.Deleted = false "
			+ "JOIN m_facility f ON f.FacilityID = asm.facilityID AND f.Deleted = false "
			+ "LEFT JOIN m_facilitytype ft ON ft.FacilityTypeID = f.FacilityTypeID "
			+ "WHERE asm.supervisorUserID = :supervisorUserID "
			+ "AND asm.deleted = false", nativeQuery = true)
	List<Object[]> getAllMappedAshas(@Param("supervisorUserID") Integer supervisorUserID);

	// API 2: ASHAs at a specific facility for a supervisor with user details
	@Query(value = "SELECT DISTINCT asm.ashaUserID, u.FirstName, u.LastName, "
			+ "COALESCE(u.EmployeeID,'') AS employeeID, "
			+ "COALESCE(u.EmergencyContactNo,'') AS mobile, "
			+ "COALESCE(g.GenderName,'') AS gender "
			+ "FROM asha_supervisor_mapping asm "
			+ "JOIN m_User u ON u.UserID = asm.ashaUserID AND u.Deleted = false "
			+ "LEFT JOIN m_gender g ON g.GenderID = u.GenderID "
			+ "WHERE asm.supervisorUserID = :supervisorUserID "
			+ "AND asm.facilityID = :facilityID "
			+ "AND asm.deleted = false", nativeQuery = true)
	List<Object[]> getAshasAtFacility(@Param("supervisorUserID") Integer supervisorUserID,
			@Param("facilityID") Integer facilityID);
}
