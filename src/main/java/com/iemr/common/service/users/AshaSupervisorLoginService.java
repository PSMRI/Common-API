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
		JSONObject result = buildEmptyFacilityData();
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

	private JSONObject buildEmptyFacilityData() {
		JSONObject result = new JSONObject();
		result.put("location", new JSONObject()
				.put("stateID", JSONObject.NULL).put("stateName", "")
				.put("districtID", JSONObject.NULL).put("districtName", "")
				.put("blockID", JSONObject.NULL).put("blockName", "")
				.put("locationType", ""));
		result.put("facilities", new JSONArray());
		result.put("mappedAshas", new JSONArray());
		result.put("totalAshaCount", 0);
		result.put("consolidatedVillages", new JSONArray());
		result.put("supervisor", JSONObject.NULL);
		result.put("peersAtFacility", new JSONArray());
		return result;
	}

	// ==================== ASHA Supervisor ====================

	private void enrichAshaSupervisorData(JSONObject result, Integer supervisorUserID) {
		// 1. Get facility IDs from asha_supervisor_mapping (existing repo, untouched)
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

		// 2. Get facility details with facilityType (new separate repo)
		List<Object[]> facilityRows = facilityLoginRepo.getFacilityDetails(facilityIDs);
		if (facilityRows == null || facilityRows.isEmpty())
			return;

		populateLocation(result, facilityRows.get(0));

		// 3. Get villages (hierarchy-aware, new separate repo)
		Map<Integer, List<JSONObject>> facilityVillageMap = new HashMap<>();
		List<Object[]> villageRows = facilityLoginRepo.getVillagesWithHierarchy(facilityIDs);
		if (villageRows != null) {
			for (Object[] vRow : villageRows) {
				Integer facID = (Integer) vRow[0];
				JSONObject village = new JSONObject();
				village.put("villageID", vRow[1]);
				village.put("villageName", vRow[2] != null ? vRow[2].toString() : "");
				facilityVillageMap.computeIfAbsent(facID, k -> new ArrayList<>()).add(village);
			}
		}

		// 4. Get mapped ASHAs (existing repo, untouched)
		List<Object[]> ashaRows = ashaSupervisorLoginRepo.getMappedAshaUsers(supervisorUserID);
		// Group ASHAs by facilityID
		Map<Integer, List<JSONObject>> facilityAshaMap = new HashMap<>();
		if (ashaRows != null) {
			for (Object[] aRow : ashaRows) {
				Integer ashaUserID = (Integer) aRow[0];
				String firstName = aRow[1] != null ? aRow[1].toString() : "";
				String lastName = aRow[2] != null ? aRow[2].toString() : "";
				Integer facilityID = (Integer) aRow[3];

				// Get this ASHA's villages from their facility
				JSONArray ashaVillages = new JSONArray();
				List<JSONObject> fVillages = facilityVillageMap.get(facilityID);
				if (fVillages != null) {
					for (JSONObject v : fVillages) {
						ashaVillages.put(new JSONObject()
								.put("villageID", v.get("villageID"))
								.put("villageName", v.get("villageName")));
					}
				}

				JSONObject asha = new JSONObject();
				asha.put("userId", ashaUserID);
				asha.put("fullName", (firstName + " " + lastName).trim());
				asha.put("villages", ashaVillages);

				facilityAshaMap.computeIfAbsent(facilityID, k -> new ArrayList<>()).add(asha);
			}
		}

		// 5. Build facilities array with ASHAs nested per facility
		JSONArray facilitiesArray = new JSONArray();
		Set<String> consolidatedKeys = new HashSet<>();
		JSONArray consolidatedVillages = new JSONArray();
		int totalAshaCount = 0;

		for (Object[] row : facilityRows) {
			Integer facilityID = (Integer) row[0];
			JSONObject facility = new JSONObject();
			facility.put("facilityID", facilityID);
			facility.put("facilityName", row[1] != null ? row[1].toString() : "");
			facility.put("facilityType", row[9] != null ? row[9].toString() : "");

			// Villages for this facility
			JSONArray facVillages = new JSONArray();
			List<JSONObject> vList = facilityVillageMap.get(facilityID);
			if (vList != null) {
				for (JSONObject v : vList) {
					facVillages.put(v);
					String key = v.get("villageID").toString();
					if (consolidatedKeys.add(key)) {
						consolidatedVillages.put(new JSONObject()
								.put("villageID", v.get("villageID"))
								.put("villageName", v.get("villageName")));
					}
				}
			}
			facility.put("villages", facVillages);

			// ASHAs for this facility
			JSONArray ashas = new JSONArray();
			List<JSONObject> ashaList = facilityAshaMap.get(facilityID);
			if (ashaList != null) {
				for (JSONObject a : ashaList) {
					ashas.put(a);
				}
				totalAshaCount += ashaList.size();
			}
			facility.put("ashas", ashas);

			facilitiesArray.put(facility);
		}

		result.put("facilities", facilitiesArray);
		result.put("totalAshaCount", totalAshaCount);
		result.put("consolidatedVillages", consolidatedVillages);
	}

	// ==================== ASHA ====================

	private void enrichAshaData(JSONObject result, Integer ashaUserID) {
		// 1. Facility from m_UserServiceRoleMapping (new separate repo)
		List<Integer> facilityIDs = facilityLoginRepo.getUserFacilityIDs(ashaUserID);
		if (facilityIDs == null || facilityIDs.isEmpty())
			return;

		List<Object[]> facilityRows = facilityLoginRepo.getFacilityDetails(facilityIDs);
		if (facilityRows == null || facilityRows.isEmpty())
			return;

		populateLocation(result, facilityRows.get(0));

		// 2. Build facilities with villages nested per facility
		Map<Integer, List<JSONObject>> facilityVillageMap = buildFacilityVillageMap(
				facilityLoginRepo.getVillagesWithHierarchy(facilityIDs));

		JSONArray facilitiesArray = new JSONArray();
		for (Object[] row : facilityRows) {
			Integer facilityID = (Integer) row[0];
			JSONObject facility = new JSONObject();
			facility.put("facilityID", facilityID);
			facility.put("facilityName", row[1] != null ? row[1].toString() : "");
			facility.put("facilityType", row[9] != null ? row[9].toString() : "");

			JSONArray facVillages = new JSONArray();
			List<JSONObject> vList = facilityVillageMap.get(facilityID);
			if (vList != null) {
				for (JSONObject v : vList) {
					facVillages.put(v);
				}
			}
			facility.put("villages", facVillages);
			facilitiesArray.put(facility);
		}
		result.put("facilities", facilitiesArray);

		// 3. Supervisor details (new separate repo)
		List<Object[]> supervisorRows = facilityLoginRepo.getSupervisorForAsha(ashaUserID);
		if (supervisorRows != null && !supervisorRows.isEmpty()) {
			Object[] sRow = supervisorRows.get(0);
			JSONObject supervisor = new JSONObject();
			supervisor.put("userId", sRow[0]);
			String sFirst = sRow[1] != null ? sRow[1].toString() : "";
			String sLast = sRow[2] != null ? sRow[2].toString() : "";
			supervisor.put("fullName", (sFirst + " " + sLast).trim());
			supervisor.put("mobile", sRow[3] != null ? sRow[3].toString() : "");
			result.put("supervisor", supervisor);
		}

		// 4. Peers at same facility (new separate repo)
		List<Object[]> peerRows = facilityLoginRepo.getPeersAtFacility(facilityIDs, ashaUserID);
		if (peerRows != null && !peerRows.isEmpty()) {
			JSONArray peers = new JSONArray();
			for (Object[] pRow : peerRows) {
				JSONObject peer = new JSONObject();
				peer.put("userId", pRow[0]);
				String pFirst = pRow[1] != null ? pRow[1].toString() : "";
				String pLast = pRow[2] != null ? pRow[2].toString() : "";
				peer.put("fullName", (pFirst + " " + pLast).trim());
				peer.put("role", pRow[3] != null ? pRow[3].toString() : "");
				peers.put(peer);
			}
			result.put("peersAtFacility", peers);
		}
	}

	// ==================== General Facility User (CHO, ANM, etc.) ====================

	private void enrichGeneralFacilityData(JSONObject result, Integer userID) {
		// 1. Facility from m_UserServiceRoleMapping (new separate repo)
		List<Integer> facilityIDs = facilityLoginRepo.getUserFacilityIDs(userID);
		if (facilityIDs == null || facilityIDs.isEmpty())
			return;

		List<Object[]> facilityRows = facilityLoginRepo.getFacilityDetails(facilityIDs);
		if (facilityRows == null || facilityRows.isEmpty())
			return;

		populateLocation(result, facilityRows.get(0));

		// 2. Build facilities with villages nested per facility
		Map<Integer, List<JSONObject>> facilityVillageMap = buildFacilityVillageMap(
				facilityLoginRepo.getVillagesWithHierarchy(facilityIDs));

		JSONArray facilitiesArray = new JSONArray();
		for (Object[] row : facilityRows) {
			Integer facilityID = (Integer) row[0];
			JSONObject facility = new JSONObject();
			facility.put("facilityID", facilityID);
			facility.put("facilityName", row[1] != null ? row[1].toString() : "");
			facility.put("facilityType", row[9] != null ? row[9].toString() : "");

			JSONArray facVillages = new JSONArray();
			List<JSONObject> vList = facilityVillageMap.get(facilityID);
			if (vList != null) {
				for (JSONObject v : vList) {
					facVillages.put(v);
				}
			}
			facility.put("villages", facVillages);
			facilitiesArray.put(facility);
		}
		result.put("facilities", facilitiesArray);
	}

	// ==================== Shared Helpers ====================

	private void populateLocation(JSONObject result, Object[] facilityRow) {
		JSONObject location = new JSONObject();
		location.put("stateID", facilityRow[2] != null ? facilityRow[2] : JSONObject.NULL);
		location.put("stateName", facilityRow[3] != null ? facilityRow[3].toString() : "");
		location.put("districtID", facilityRow[4] != null ? facilityRow[4] : JSONObject.NULL);
		location.put("districtName", facilityRow[5] != null ? facilityRow[5].toString() : "");
		location.put("blockID", facilityRow[6] != null ? facilityRow[6] : JSONObject.NULL);
		location.put("blockName", facilityRow[7] != null ? facilityRow[7].toString() : "");
		location.put("locationType", facilityRow[8] != null ? facilityRow[8].toString() : "");
		result.put("location", location);
	}

	private Map<Integer, List<JSONObject>> buildFacilityVillageMap(List<Object[]> villageRows) {
		Map<Integer, List<JSONObject>> map = new HashMap<>();
		if (villageRows != null) {
			for (Object[] vRow : villageRows) {
				Integer facID = (Integer) vRow[0];
				JSONObject village = new JSONObject();
				village.put("villageID", vRow[1]);
				village.put("villageName", vRow[2] != null ? vRow[2].toString() : "");
				map.computeIfAbsent(facID, k -> new ArrayList<>()).add(village);
			}
		}
		return map;
	}
}
