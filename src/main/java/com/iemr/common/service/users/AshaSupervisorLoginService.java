package com.iemr.common.service.users;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

	// ==================== General Facility User (CHO, ANM, etc.) ====================

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

	public String getGenderName(Integer genderID) {
		if (genderID == null)
			return null;
		return facilityLoginRepo.getGenderName(genderID);
	}
}
