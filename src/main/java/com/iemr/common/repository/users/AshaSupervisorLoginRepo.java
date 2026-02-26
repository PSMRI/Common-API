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

	@Query(value = "SELECT DISTINCT f.FacilityID, f.FacilityName, "
			+ "f.StateID, COALESCE(s.StateName,'') AS stateName, "
			+ "f.DistrictID, COALESCE(d.DistrictName,'') AS districtName, "
			+ "f.BlockID, COALESCE(b.BlockName,'') AS blockName, "
			+ "COALESCE(f.RuralUrban,'') AS ruralUrban "
			+ "FROM asha_supervisor_mapping asm "
			+ "JOIN m_facility f ON f.FacilityID = asm.facilityID AND f.Deleted = false "
			+ "LEFT JOIN m_state s ON s.StateID = f.StateID "
			+ "LEFT JOIN m_district d ON d.DistrictID = f.DistrictID "
			+ "LEFT JOIN m_districtblock b ON b.BlockID = f.BlockID "
			+ "WHERE asm.supervisorUserID = :supervisorUserID AND asm.deleted = false", nativeQuery = true)
	List<Object[]> getSupervisorFacilities(@Param("supervisorUserID") Integer supervisorUserID);

	@Query(value = "SELECT asm.ashaUserID, u.FirstName, u.LastName, asm.facilityID "
			+ "FROM asha_supervisor_mapping asm "
			+ "JOIN m_User u ON u.UserID = asm.ashaUserID "
			+ "WHERE asm.supervisorUserID = :supervisorUserID AND asm.deleted = false "
			+ "AND u.Deleted = false", nativeQuery = true)
	List<Object[]> getMappedAshaUsers(@Param("supervisorUserID") Integer supervisorUserID);

	@Query(value = "SELECT fvm.FacilityID, fvm.DistrictBranchID, dbm.VillageName "
			+ "FROM facility_village_mapping fvm "
			+ "JOIN m_DistrictBranchMapping dbm ON dbm.DistrictBranchID = fvm.DistrictBranchID "
			+ "WHERE fvm.FacilityID IN :facilityIDs AND fvm.Deleted = false", nativeQuery = true)
	List<Object[]> getVillagesForFacilities(@Param("facilityIDs") List<Integer> facilityIDs);
}
