package com.iemr.common.repository.users;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.iemr.common.data.users.AshaSupervisorMapping;

@Repository
public interface FacilityLoginRepo extends CrudRepository<AshaSupervisorMapping, Long> {

	// Get user's facility IDs from m_UserServiceRoleMapping (for ANY user)
	@Query(value = "SELECT DISTINCT usrm.FacilityID "
			+ "FROM m_UserServiceRoleMapping usrm "
			+ "WHERE usrm.UserID = :userID AND usrm.Deleted = false "
			+ "AND usrm.FacilityID IS NOT NULL", nativeQuery = true)
	List<Integer> getUserFacilityIDs(@Param("userID") Integer userID);

	// Get facility details with geo names and facilityType
	@Query(value = "SELECT DISTINCT f.FacilityID, f.FacilityName, "
			+ "f.StateID, COALESCE(s.StateName,'') AS stateName, "
			+ "f.DistrictID, COALESCE(d.DistrictName,'') AS districtName, "
			+ "f.BlockID, COALESCE(b.BlockName,'') AS blockName, "
			+ "COALESCE(f.RuralUrban,'') AS ruralUrban, "
			+ "COALESCE(ft.FacilityTypeName,'') AS facilityTypeName "
			+ "FROM m_facility f "
			+ "LEFT JOIN m_state s ON s.StateID = f.StateID "
			+ "LEFT JOIN m_district d ON d.DistrictID = f.DistrictID "
			+ "LEFT JOIN m_districtblock b ON b.BlockID = f.BlockID "
			+ "LEFT JOIN m_facilitytype ft ON ft.FacilityTypeID = f.FacilityTypeID "
			+ "WHERE f.FacilityID IN :facilityIDs AND f.Deleted = false", nativeQuery = true)
	List<Object[]> getFacilityDetails(@Param("facilityIDs") List<Integer> facilityIDs);

	// Recursive hierarchy: Medical College -> DH -> CHC -> PHC -> SC (all levels)
	@Query(value = "WITH RECURSIVE facility_tree AS ( "
			+ "  SELECT FacilityID FROM m_facility WHERE FacilityID IN :facilityIDs AND Deleted = false "
			+ "  UNION ALL "
			+ "  SELECT f.FacilityID FROM m_facility f "
			+ "  INNER JOIN facility_tree ft ON f.ParentFacilityID = ft.FacilityID "
			+ "  WHERE f.Deleted = false "
			+ ") "
			+ "SELECT fvm.FacilityID, fvm.DistrictBranchID, dbm.VillageName "
			+ "FROM facility_village_mapping fvm "
			+ "JOIN m_DistrictBranchMapping dbm ON dbm.DistrictBranchID = fvm.DistrictBranchID "
			+ "WHERE fvm.FacilityID IN (SELECT FacilityID FROM facility_tree) "
			+ "AND fvm.Deleted = false", nativeQuery = true)
	List<Object[]> getVillagesWithHierarchy(@Param("facilityIDs") List<Integer> facilityIDs);

	// ASHA login: get supervisor details
	@Query(value = "SELECT asm.supervisorUserID, u.FirstName, u.LastName, u.ContactNo "
			+ "FROM asha_supervisor_mapping asm "
			+ "JOIN m_User u ON u.UserID = asm.supervisorUserID "
			+ "WHERE asm.ashaUserID = :ashaUserID AND asm.deleted = false "
			+ "AND u.Deleted = false LIMIT 1", nativeQuery = true)
	List<Object[]> getSupervisorForAsha(@Param("ashaUserID") Integer ashaUserID);

	// ASHA login: get peers at same facility (ANM, CHO, etc.)
	@Query(value = "SELECT DISTINCT usrm.UserID, u.FirstName, u.LastName, r.RoleName "
			+ "FROM m_UserServiceRoleMapping usrm "
			+ "JOIN m_User u ON u.UserID = usrm.UserID "
			+ "JOIN m_Role r ON r.RoleID = usrm.RoleID "
			+ "WHERE usrm.FacilityID IN :facilityIDs "
			+ "AND usrm.UserID != :currentUserID "
			+ "AND usrm.Deleted = false AND u.Deleted = false", nativeQuery = true)
	List<Object[]> getPeersAtFacility(@Param("facilityIDs") List<Integer> facilityIDs,
			@Param("currentUserID") Integer currentUserID);
}
