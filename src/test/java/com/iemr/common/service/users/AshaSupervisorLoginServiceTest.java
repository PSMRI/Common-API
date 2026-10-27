package com.iemr.common.service.users;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.iemr.common.data.users.AshaSupervisorMapping;
import com.iemr.common.repository.users.AshaSupervisorLoginRepo;
import com.iemr.common.repository.users.FacilityLoginRepo;

/**
 * Covers the facility login payload the service assembles for each kind of field user.
 *
 * <p>The three role branches (ASHA, ASHA Supervisor and everyone else) read different
 * repository projections, so each is driven with its own row fixtures, alongside the
 * short-circuit paths where a user has no facility mapping.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AshaSupervisorLoginServiceTest {

	@Mock
	private AshaSupervisorLoginRepo ashaSupervisorLoginRepo;

	@Mock
	private FacilityLoginRepo facilityLoginRepo;

	@InjectMocks
	private AshaSupervisorLoginService service;

	/** A facility projection row: id, name, then the location and type columns the service reads. */
	private static Object[] facilityRow(int id, String name, String state, String district, String block,
			String locationType, String facilityType) {
		return new Object[] { id, name, "ignored", state, "ignored", district, "ignored", block, locationType,
				facilityType };
	}

	private static Object[] personRow(Object... values) {
		return values;
	}

	private static List<Object[]> rows(Object[]... rows) {
		return List.of(rows);
	}

	/** The keys present on a JSONObject; this org.json build exposes only an iterator. */
	private static List<String> keysOf(JSONObject json) {
		List<String> keys = new ArrayList<>();
		json.keys().forEachRemaining(key -> keys.add(String.valueOf(key)));
		return keys;
	}

	private static AshaSupervisorMapping mapping(Integer facilityId) {
		AshaSupervisorMapping mapping = new AshaSupervisorMapping();
		mapping.setFacilityID(facilityId);
		return mapping;
	}

	@Nested
	@DisplayName("ASHA")
	class AshaRole {

		@Test
		@DisplayName("returns the single mapped facility with its supervisor and peers")
		void buildsAshaPayload() throws Exception {
			when(facilityLoginRepo.getUserFacilityIDs(7)).thenReturn(List.of(101));
			when(facilityLoginRepo.getFacilityDetails(anyList())).thenReturn(rows(facilityRow(101, "PHC Anekal", "Karnataka", "Bengaluru", "Anekal", "Rural", "PHC")));
			when(facilityLoginRepo.getSupervisorForAsha(7))
					.thenReturn(rows(personRow(9, "Meena", "Rao", "9876500000", "EMP9")));
			when(facilityLoginRepo.getPeersAtFacility(anyList(), anyInt()))
					.thenReturn(rows(personRow(8, "Latha", "Devi", "ASHA", "EMP8", "9876500001")));

			JSONObject result = service.buildFacilityLoginData(7L, "ASHA");

			assertThat(result.getJSONObject("location").getString("state")).isEqualTo("Karnataka");
			assertThat(result.getJSONObject("location").getString("district")).isEqualTo("Bengaluru");
			assertThat(result.getJSONObject("location").getString("blockOrUlb")).isEqualTo("Anekal");
			assertThat(result.getJSONObject("location").getString("locationType")).isEqualTo("Rural");
			assertThat(result.getJSONObject("facility").getString("facilityName")).isEqualTo("PHC Anekal");
			assertThat(result.getJSONObject("facility").getString("facilityType")).isEqualTo("PHC");
			assertThat(result.getJSONObject("supervisor").getString("fullName")).isEqualTo("Meena Rao");
			assertThat(result.getJSONObject("supervisor").getString("employeeId")).isEqualTo("EMP9");
			assertThat(result.getJSONArray("peersAtFacility").length()).isEqualTo(1);
			assertThat(result.getJSONArray("peersAtFacility").getJSONObject(0).getString("fullName"))
					.isEqualTo("Latha Devi");
		}

		@Test
		@DisplayName("records a null supervisor when the ASHA has none")
		void nullSupervisorWhenNoneMapped() throws Exception {
			when(facilityLoginRepo.getUserFacilityIDs(7)).thenReturn(List.of(101));
			when(facilityLoginRepo.getFacilityDetails(anyList()))
					.thenReturn(rows(facilityRow(101, "PHC", "KA", "BLR", "Anekal", "Rural", "PHC")));
			when(facilityLoginRepo.getSupervisorForAsha(7)).thenReturn(List.of());
			when(facilityLoginRepo.getPeersAtFacility(anyList(), anyInt())).thenReturn(null);

			JSONObject result = service.buildFacilityLoginData(7L, "ASHA");

			assertThat(result.isNull("supervisor")).isTrue();
			assertThat(result.getJSONArray("peersAtFacility").length()).isZero();
		}

		@Test
		@DisplayName("omits a blank employee id rather than reporting an empty string")
		void blankEmployeeIdBecomesNull() throws Exception {
			when(facilityLoginRepo.getUserFacilityIDs(7)).thenReturn(List.of(101));
			when(facilityLoginRepo.getFacilityDetails(anyList()))
					.thenReturn(rows(facilityRow(101, "PHC", "KA", "BLR", "Anekal", "Rural", "PHC")));
			when(facilityLoginRepo.getSupervisorForAsha(7))
					.thenReturn(rows(personRow(9, "Meena", "Rao", "9876500000", null)));
			when(facilityLoginRepo.getPeersAtFacility(anyList(), anyInt()))
					.thenReturn(rows(personRow(8, "Latha", "Devi", "ASHA", null, null)));

			JSONObject result = service.buildFacilityLoginData(7L, "ASHA");

			assertThat(result.getJSONObject("supervisor").isNull("employeeId")).isTrue();
			assertThat(result.getJSONArray("peersAtFacility").getJSONObject(0).isNull("employeeId")).isTrue();
			assertThat(result.getJSONArray("peersAtFacility").getJSONObject(0).isNull("mobile")).isTrue();
		}

		@Test
		@DisplayName("returns only the empty location when the ASHA has no facility mapping")
		void noFacilityMapping() throws Exception {
			when(facilityLoginRepo.getUserFacilityIDs(7)).thenReturn(List.of());

			JSONObject result = service.buildFacilityLoginData(7L, "ASHA");

			assertThat(keysOf(result)).containsExactly("location");
			assertThat(result.getJSONObject("location").getString("state")).isEmpty();
		}

		@Test
		@DisplayName("returns only the empty location when the mapped facility cannot be read")
		void facilityDetailsMissing() throws Exception {
			when(facilityLoginRepo.getUserFacilityIDs(7)).thenReturn(List.of(101));
			when(facilityLoginRepo.getFacilityDetails(anyList())).thenReturn(List.of());

			assertThat(keysOf(service.buildFacilityLoginData(7L, "ASHA"))).containsExactly("location");
		}
	}

	@Nested
	@DisplayName("ASHA Supervisor")
	class SupervisorRole {

		@Test
		@DisplayName("returns every mapped facility along with the supervised ASHA list")
		void buildsSupervisorPayload() throws Exception {
			when(ashaSupervisorLoginRepo.findBySupervisorUserIDAndDeletedFalse(5))
					.thenReturn(new ArrayList<>(List.of(mapping(101), mapping(102), mapping(null))));
			when(facilityLoginRepo.getFacilityDetails(anyList()))
					.thenReturn(rows(facilityRow(101, "PHC A", "Karnataka", "Bengaluru", "Anekal", "Rural", "PHC"),
							facilityRow(102, "PHC B", "Karnataka", "Bengaluru", "Anekal", "Rural", "PHC")));
			when(facilityLoginRepo.getMappedAshasBySupervisor(5))
					.thenReturn(rows(personRow(8, "Latha", "Devi", "EMP8", 101, "PHC A", "PHC", "9876500001"),
							personRow(9, "Sita", "Bai", null, 102, "PHC B", "PHC", null)));

			JSONObject result = service.buildFacilityLoginData(5L, "ASHA Supervisor");

			assertThat(result.getJSONArray("facilities").length()).isEqualTo(2);
			assertThat(result.getInt("totalAshaCount")).isEqualTo(2);
			assertThat(result.getJSONArray("ashaList").getJSONObject(0).getString("fullName")).isEqualTo("Latha Devi");
			assertThat(result.getJSONArray("ashaList").getJSONObject(1).isNull("employeeId")).isTrue();
			assertThat(result.getJSONArray("ashaList").getJSONObject(1).isNull("mobile")).isTrue();
		}

		@Test
		@DisplayName("returns only the empty location when the supervisor has no mappings")
		void noMappings() throws Exception {
			when(ashaSupervisorLoginRepo.findBySupervisorUserIDAndDeletedFalse(5)).thenReturn(new ArrayList<>());

			assertThat(keysOf(service.buildFacilityLoginData(5L, "ASHA Supervisor"))).containsExactly("location");
		}

		@Test
		@DisplayName("returns only the empty location when no mapping carries a facility")
		void mappingsWithoutFacilities() throws Exception {
			when(ashaSupervisorLoginRepo.findBySupervisorUserIDAndDeletedFalse(5))
					.thenReturn(new ArrayList<>(List.of(mapping(null))));

			assertThat(keysOf(service.buildFacilityLoginData(5L, "ASHA Supervisor"))).containsExactly("location");
			verifyNoInteractions(facilityLoginRepo);
		}

		@Test
		@DisplayName("reports an empty ASHA list when none is supervised")
		void noSupervisedAshas() throws Exception {
			when(ashaSupervisorLoginRepo.findBySupervisorUserIDAndDeletedFalse(5))
					.thenReturn(new ArrayList<>(List.of(mapping(101))));
			when(facilityLoginRepo.getFacilityDetails(anyList()))
					.thenReturn(rows(facilityRow(101, "PHC A", "KA", "BLR", "Anekal", "Rural", "PHC")));
			when(facilityLoginRepo.getMappedAshasBySupervisor(5)).thenReturn(null);

			JSONObject result = service.buildFacilityLoginData(5L, "ASHA Supervisor");

			assertThat(result.getInt("totalAshaCount")).isZero();
		}
	}

	@Nested
	@DisplayName("other facility users")
	class GeneralRole {

		@ParameterizedTest(name = "role {0}")
		@ValueSource(strings = { "CHO", "ANM", "Medical Officer" })
		@DisplayName("returns the mapped facilities and the ASHAs working at them")
		void buildsGeneralPayload(String roleName) throws Exception {
			when(facilityLoginRepo.getUserFacilityIDs(3)).thenReturn(List.of(101));
			when(facilityLoginRepo.getFacilityDetails(anyList()))
					.thenReturn(rows(facilityRow(101, "PHC A", "Karnataka", "Bengaluru", "Anekal", "Rural", "PHC")));
			when(facilityLoginRepo.getAshaListByFacilities(anyList()))
					.thenReturn(rows(personRow(8, "Latha", "Devi", "EMP8", 101, "PHC A", "PHC", "9876500001")));

			JSONObject result = service.buildFacilityLoginData(3L, roleName);

			assertThat(result.getJSONArray("facilities").length()).isEqualTo(1);
			assertThat(result.getInt("totalAshaCount")).isEqualTo(1);
		}

		@Test
		@DisplayName("returns only the empty location when the user has no facility mapping")
		void noFacilityMapping() throws Exception {
			when(facilityLoginRepo.getUserFacilityIDs(3)).thenReturn(null);

			assertThat(keysOf(service.buildFacilityLoginData(3L, "CHO"))).containsExactly("location");
		}

		@Test
		@DisplayName("returns only the empty location when the facility details are unavailable")
		void facilityDetailsMissing() throws Exception {
			when(facilityLoginRepo.getUserFacilityIDs(3)).thenReturn(List.of(101));
			when(facilityLoginRepo.getFacilityDetails(anyList())).thenReturn(null);

			assertThat(keysOf(service.buildFacilityLoginData(3L, "CHO"))).containsExactly("location");
		}
	}

	@Test
	@DisplayName("a repository failure leaves the empty location payload rather than propagating")
	void repositoryFailureIsContained() throws Exception {
		when(facilityLoginRepo.getUserFacilityIDs(anyInt())).thenThrow(new IllegalStateException("db down"));

		assertThat(keysOf(service.buildFacilityLoginData(3L, "CHO"))).containsExactly("location");
	}

	@Test
	@DisplayName("a null user id is reported as a contained failure rather than a crash")
	void nullUserIdIsContained() throws Exception {
		assertThat(keysOf(service.buildFacilityLoginData(null, "ASHA"))).containsExactly("location");
	}

	@Nested
	@DisplayName("getGenderName")
	class GenderName {

		@Test
		@DisplayName("looks up the gender name for a given id")
		void looksUpTheName() throws Exception {
			when(facilityLoginRepo.getGenderName(2)).thenReturn("Female");

			assertThat(service.getGenderName(2)).isEqualTo("Female");
		}

		@Test
		@DisplayName("returns null without a lookup when no id is given")
		void nullIdSkipsLookup() throws Exception {
			assertThat(service.getGenderName(null)).isNull();
			verifyNoInteractions(facilityLoginRepo);
		}
	}
}
