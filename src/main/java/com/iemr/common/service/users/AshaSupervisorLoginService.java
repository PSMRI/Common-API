package com.iemr.common.service.users;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iemr.common.data.users.AshaSupervisorMapping;
import com.iemr.common.repository.users.AshaSupervisorLoginRepo;
import com.iemr.common.repository.users.FacilityLoginRepo;

@Service
public class AshaSupervisorLoginService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	@Autowired
	private AshaSupervisorLoginRepo ashaSupervisorLoginRepo;

	@Autowired
	private FacilityLoginRepo facilityLoginRepo;

	/**
	 * Common facilityData block for ALL users.
	 * Returns empty structure if user has no facility mapping.
	 */
	public JSONObject buildFacilityLoginData(Long userID, String roleName) {
		JSONObject result = new JSONObject();
		result.put("location", buildEmptyLocation());
		try {
			if ("ASHA Supervisor".equalsIgnoreCase(roleName)) {
				enrichAshaSupervisorData(result, userID.intValue());
			} else if ("ASHA".equalsIgnoreCase(roleName)) {
				enrichAshaData(result, userID.intValue());
			} else {
				enrichGeneralFacilityData(result, userID.intValue());
			}
		} catch (Exception e) {
			logger.error("Error building facility login data for userID " + userID + ": " + e.getMessage(), e);
		}
		return result;
	}

	private JSONObject buildEmptyLocation() {
		return new JSONObject()
				.put("state", "")
				.put("district", "")
				.put("blockOrUlb", "")
				.put("locationType", "");
	}

	// ==================== ASHA ====================

	private void enrichAshaData(JSONObject result, Integer ashaUserID) {
		List<Integer> facilityIDs = facilityLoginRepo.getUserFacilityIDs(ashaUserID);
		if (facilityIDs == null || facilityIDs.isEmpty())
			return;

		List<Object[]> facilityRows = facilityLoginRepo.getFacilityDetails(facilityIDs);
		if (facilityRows == null || facilityRows.isEmpty())
			return;

		populateLocation(result, facilityRows.get(0));

		// ASHA gets a single facility (first mapped)
		Object[] row = facilityRows.get(0);
		JSONObject facility = new JSONObject();
		facility.put("facilityId", row[0]);
		facility.put("facilityName", str(row[1]));
		facility.put("facilityType", str(row[9]));
		result.put("facility", facility);

		// Supervisor details
		List<Object[]> supervisorRows = facilityLoginRepo.getSupervisorForAsha(ashaUserID);
		if (supervisorRows != null && !supervisorRows.isEmpty()) {
			Object[] sRow = supervisorRows.get(0);
			JSONObject supervisor = new JSONObject();
			supervisor.put("userId", sRow[0]);
			supervisor.put("fullName", fullName(sRow[1], sRow[2]));
			supervisor.put("mobile", str(sRow[3]));
			result.put("supervisor", supervisor);
		} else {
			result.put("supervisor", JSONObject.NULL);
		}

		// Peers at same facility
		List<Object[]> peerRows = facilityLoginRepo.getPeersAtFacility(facilityIDs, ashaUserID);
		JSONArray peers = new JSONArray();
		if (peerRows != null) {
			for (Object[] pRow : peerRows) {
				JSONObject peer = new JSONObject();
				peer.put("userId", pRow[0]);
				peer.put("fullName", fullName(pRow[1], pRow[2]));
				peer.put("role", str(pRow[3]));
				peers.put(peer);
			}
		}
		result.put("peersAtFacility", peers);
	}

	// ==================== ASHA Supervisor ====================

	private void enrichAshaSupervisorData(JSONObject result, Integer supervisorUserID) {
		ArrayList<AshaSupervisorMapping> mappings = ashaSupervisorLoginRepo
				.findBySupervisorUserIDAndDeletedFalse(supervisorUserID);
		if (mappings == null || mappings.isEmpty())
			return;

		Set<Integer> facilityIDSet = new HashSet<>();
		for (AshaSupervisorMapping m : mappings) {
			if (m.getFacilityID() != null)
				facilityIDSet.add(m.getFacilityID());
		}
		if (facilityIDSet.isEmpty())
			return;
		List<Integer> facilityIDs = new ArrayList<>(facilityIDSet);

		List<Object[]> facilityRows = facilityLoginRepo.getFacilityDetails(facilityIDs);
		if (facilityRows == null || facilityRows.isEmpty())
			return;

		populateLocation(result, facilityRows.get(0));

		JSONArray facilitiesArray = new JSONArray();
		for (Object[] row : facilityRows) {
			JSONObject facility = new JSONObject();
			facility.put("facilityId", row[0]);
			facility.put("facilityName", str(row[1]));
			facility.put("facilityType", str(row[9]));
			facilitiesArray.put(facility);
		}
		result.put("facilities", facilitiesArray);

		// Count distinct ASHAs mapped to this supervisor
		Set<Integer> ashaUserIDs = new HashSet<>();
		for (AshaSupervisorMapping m : mappings) {
			if (m.getAshaUserID() != null)
				ashaUserIDs.add(m.getAshaUserID());
		}
		result.put("totalAshaCount", ashaUserIDs.size());
	}

	// ==================== General Facility User (CHO, ANM, etc.)
	// ====================

	private void enrichGeneralFacilityData(JSONObject result, Integer userID) {
		List<Integer> facilityIDs = facilityLoginRepo.getUserFacilityIDs(userID);
		if (facilityIDs == null || facilityIDs.isEmpty())
			return;

		List<Object[]> facilityRows = facilityLoginRepo.getFacilityDetails(facilityIDs);
		if (facilityRows == null || facilityRows.isEmpty())
			return;

		populateLocation(result, facilityRows.get(0));

		JSONArray facilitiesArray = new JSONArray();
		for (Object[] row : facilityRows) {
			JSONObject facility = new JSONObject();
			facility.put("facilityId", row[0]);
			facility.put("facilityName", str(row[1]));
			facility.put("facilityType", str(row[9]));
			facilitiesArray.put(facility);
		}
		result.put("facilities", facilitiesArray);
	}

	// ==================== Shared Helpers ====================

	private void populateLocation(JSONObject result, Object[] facilityRow) {
		JSONObject location = new JSONObject();
		location.put("state", str(facilityRow[3]));
		location.put("district", str(facilityRow[5]));
		location.put("blockOrUlb", str(facilityRow[7]));
		location.put("locationType", str(facilityRow[8]));
		result.put("location", location);
	}

	private String str(Object val) {
		return val != null ? val.toString() : "";
	}

	private String fullName(Object first, Object last) {
		return (str(first) + " " + str(last)).trim();
	}

	/**
	 * API 1: Get all ASHAs mapped to this supervisor with facility info.
	 */
	public JSONObject getSupervisorAshas(Integer supervisorUserID) {
		JSONObject result = new JSONObject();
		JSONArray ashas = new JSONArray();

		List<Object[]> rows = ashaSupervisorLoginRepo.getAllMappedAshas(supervisorUserID);
		if (rows == null || rows.isEmpty()) {
			result.put("totalAshaCount", 0);
			result.put("ashas", ashas);
			return result;
		}

		// Collect distinct facility IDs and build village map
		Set<Integer> facilityIDSet = new HashSet<>();
		for (Object[] row : rows) {
			if (row[3] != null)
				facilityIDSet.add((Integer) row[3]);
		}

		Map<Integer, List<JSONObject>> villageMap = new HashMap<>();
		if (!facilityIDSet.isEmpty()) {
			List<Object[]> villageRows = facilityLoginRepo
					.getVillagesForFacilities(new ArrayList<>(facilityIDSet));
			if (villageRows != null) {
				for (Object[] vRow : villageRows) {
					Integer facID = (Integer) vRow[0];
					JSONObject village = new JSONObject();
					village.put("villageId", vRow[1]);
					village.put("villageName", str(vRow[2]));
					villageMap.computeIfAbsent(facID, k -> new ArrayList<>()).add(village);
				}
			}
		}

		for (Object[] row : rows) {
			Integer facilityID = (Integer) row[3];
			JSONObject asha = new JSONObject();
			asha.put("userId", row[0]);
			asha.put("fullName", fullName(row[1], row[2]));
			asha.put("facilityId", facilityID);
			asha.put("facilityName", str(row[4]));
			asha.put("facilityType", str(row[5]));

			JSONArray villages = new JSONArray();
			List<JSONObject> vList = villageMap.get(facilityID);
			if (vList != null) {
				for (JSONObject v : vList)
					villages.put(v);
			}
			asha.put("villages", villages);

			ashas.put(asha);
		}

		result.put("totalAshaCount", ashas.length());
		result.put("ashas", ashas);
		return result;
	}

	/**
	 * API 2: Get ASHAs at a specific facility for this supervisor with detailed
	 * info.
	 */
	public JSONObject getAshasAtFacility(Integer supervisorUserID, Integer facilityID) {
		JSONObject result = new JSONObject();
		result.put("facilityId", facilityID);

		// Get facility name and type
		List<Object[]> facilityRows = facilityLoginRepo
				.getFacilityDetails(List.of(facilityID));
		if (facilityRows != null && !facilityRows.isEmpty()) {
			result.put("facilityName", str(facilityRows.get(0)[1]));
			result.put("facilityType", str(facilityRows.get(0)[9]));
		} else {
			result.put("facilityName", "");
			result.put("facilityType", "");
		}

		// Villages for this facility (to attach per ASHA)
		JSONArray facilityVillages = new JSONArray();
		List<Object[]> villageRows = facilityLoginRepo
				.getVillagesForFacilities(List.of(facilityID));
		if (villageRows != null) {
			for (Object[] vRow : villageRows) {
				JSONObject village = new JSONObject();
				village.put("villageId", vRow[1]);
				village.put("villageName", str(vRow[2]));
				facilityVillages.put(village);
			}
		}

		JSONArray ashas = new JSONArray();
		List<Object[]> rows = ashaSupervisorLoginRepo
				.getAshasAtFacility(supervisorUserID, facilityID);
		if (rows != null) {
			for (Object[] row : rows) {
				JSONObject asha = new JSONObject();
				asha.put("userId", row[0]);
				asha.put("fullName", fullName(row[1], row[2]));
				asha.put("employeeId", str(row[3]).isEmpty() ? JSONObject.NULL : str(row[3]));
				asha.put("mobile", str(row[4]).isEmpty() ? JSONObject.NULL : str(row[4]));
				asha.put("gender", str(row[5]).isEmpty() ? JSONObject.NULL : str(row[5]));
				asha.put("villages", facilityVillages);
				ashas.put(asha);
			}
		}

		result.put("ashaCount", ashas.length());
		result.put("ashas", ashas);
		return result;
	}

	public String getGenderName(Integer genderID) {
		if (genderID == null)
			return null;
		return facilityLoginRepo.getGenderName(genderID);
	}
}
