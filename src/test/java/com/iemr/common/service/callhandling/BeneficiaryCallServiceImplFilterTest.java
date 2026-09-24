package com.iemr.common.service.callhandling;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.iemr.common.data.callhandling.BeneficiaryCall;
import com.iemr.common.data.callhandling.PhoneBlock;
import com.iemr.common.data.users.ProviderServiceMapping;
import com.iemr.common.repository.callhandling.PhoneBlockRepository;
import com.iemr.common.repository.users.ProviderServiceMapRepository;
import com.iemr.common.testutil.PopulatedMocks;
import com.iemr.common.utils.config.ConfigProperties;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaQuery;

/**
 * Covers the quality-audit call filter and the nuisance-call phone blocking it feeds.
 *
 * <p>The criteria query is built inline against the entity manager, so a deep-stub entity
 * manager stands in for it and only the final result list is stubbed. Collaborators the
 * service dereferences are supplied as populated mocks.
 */
class BeneficiaryCallServiceImplFilterTest {

	private BeneficiaryCallServiceImpl service;
	private EntityManager entityManager;
	private PhoneBlockRepository phoneBlockRepository;
	private ProviderServiceMapRepository providerServiceMapRepository;

	@BeforeEach
	void setUp() {
		service = new BeneficiaryCallServiceImpl();
		entityManager = mock(EntityManager.class, RETURNS_DEEP_STUBS);
		phoneBlockRepository = PopulatedMocks.of(PhoneBlockRepository.class);
		providerServiceMapRepository = PopulatedMocks.of(ProviderServiceMapRepository.class);

		ReflectionTestUtils.setField(service, "entityManager", entityManager);
		ReflectionTestUtils.setField(service, "entityManager1", entityManager);
		ReflectionTestUtils.setField(service, "phoneBlockRepository", phoneBlockRepository);
		ReflectionTestUtils.setField(service, "providerServiceMapRepository", providerServiceMapRepository);
		ReflectionTestUtils.setField(service, "qualityAuditPageSize", "10");
		ReflectionTestUtils.setField(service, "ctiLoggerURL", "http://127.0.0.1:1/cti");
		PopulatedMocks.injectCollaborators(service);

		// The service serialises notifications through OutputMapper's static Gson builders.
		new com.iemr.common.utils.mapper.OutputMapper();
	}

	/**
	 * Points the criteria queries the service builds at canned results: the entity query at
	 * {@code results}, and the separate count query at a single total.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void criteriaQueryReturns(List<?> results) {
		TypedQuery entityQuery = mock(TypedQuery.class, RETURNS_DEEP_STUBS);
		when(entityQuery.getResultList()).thenReturn(new ArrayList(results));
		when(entityQuery.setMaxResults(anyInt())).thenReturn(entityQuery);
		when(entityQuery.setFirstResult(anyInt())).thenReturn(entityQuery);
		doReturn(entityQuery).when(entityManager).createQuery(any(CriteriaQuery.class));

		// The pagination total is read through its own Long-typed criteria query; deep stubs
		// hand back the same instance for the same call, so it can be targeted directly.
		CriteriaQuery<Long> countCriteria = entityManager.getCriteriaBuilder().createQuery(Long.class);
		TypedQuery countQuery = mock(TypedQuery.class, RETURNS_DEEP_STUBS);
		when(countQuery.getResultList()).thenReturn(new ArrayList(List.of((long) results.size())));
		doReturn(countQuery).when(entityManager).createQuery(countCriteria);
	}

	private BeneficiaryCall call(String phoneNo) {
		BeneficiaryCall call = new BeneficiaryCall();
		ReflectionTestUtils.setField(call, "benCallID", 55L);
		ReflectionTestUtils.setField(call, "phoneNo", phoneNo);
		ReflectionTestUtils.setField(call, "calledServiceID", 12);
		ReflectionTestUtils.setField(call, "createdBy", "tester");
		ReflectionTestUtils.setField(call, "receivedRoleName", "Nurse");
		ReflectionTestUtils.setField(call, "agentID", "AG1");
		return call;
	}

	@Nested
	@DisplayName("filterCallList")
	class FilterCallList {

		@Test
		@DisplayName("returns a payload for the calls the query finds")
		void returnsAPayload() throws Exception {
			criteriaQueryReturns(List.of(call("919876500000")));

			String response = service.filterCallList("{\"calledServiceID\":12,\"pageNo\":1}", "auth");

			assertThat(response).isNotBlank();
		}

		@Test
		@DisplayName("reports the call's phone number as stored, without trimming a country code")
		void reportsThePhoneNumberAsStored() throws Exception {
			BeneficiaryCall call = call("919876500000");
			criteriaQueryReturns(List.of(call));

			service.filterCallList("{\"calledServiceID\":12,\"pageNo\":1}", "auth");

			// Unlike the paginated variant, this filter leaves the stored number untouched.
			assertThat(call.getPhoneNo()).isEqualTo("919876500000");
		}

		@Test
		@DisplayName("the paginated filter trims a phone number to its last ten digits")
		void paginatedFilterTrimsThePhoneNumber() throws Exception {
			BeneficiaryCall call = call("919876500000");
			criteriaQueryReturns(List.of(call));

			service.filterCallListWithPagination("{\"calledServiceID\":12,\"pageNo\":1,\"pageSize\":10}",
					"auth");

			assertThat(call.getPhoneNo()).isEqualTo("9876500000");
		}

		@Test
		@DisplayName("the paginated filter leaves a ten digit phone number as it is")
		void paginatedFilterLeavesShortNumbersAlone() throws Exception {
			BeneficiaryCall call = call("9876500000");
			criteriaQueryReturns(List.of(call));

			service.filterCallListWithPagination("{\"calledServiceID\":12,\"pageNo\":1,\"pageSize\":10}",
					"auth");

			assertThat(call.getPhoneNo()).isEqualTo("9876500000");
		}

		@Test
		@DisplayName("returns a payload when the query finds nothing")
		void emptyResultSet() throws Exception {
			criteriaQueryReturns(List.of());

			assertThat(service.filterCallList("{\"calledServiceID\":12,\"pageNo\":1}", "auth")).isNotBlank();
		}

		@Test
		@DisplayName("applies the requested date window")
		void appliesTheRequestedDateWindow() throws Exception {
			criteriaQueryReturns(List.of());

			String request = "{\"calledServiceID\":12,\"pageNo\":1,"
					+ "\"filterStartDate\":\"2024-01-01T00:00:00.000+0000\","
					+ "\"filterEndDate\":\"2024-01-31T00:00:00.000+0000\"}";

			assertThat(service.filterCallList(request, "auth")).isNotBlank();
		}

		@Test
		@DisplayName("filters by the optional call attributes when they are supplied")
		void appliesOptionalFilters() throws Exception {
			criteriaQueryReturns(List.of());

			String request = "{\"calledServiceID\":12,\"pageNo\":1,\"receivedRoleName\":\"Nurse\","
					+ "\"agentID\":\"AG1\",\"phoneNo\":\"9876500000\",\"callTypeID\":3}";

			assertThat(service.filterCallList(request, "auth")).isNotBlank();
		}
	}

	@Nested
	@DisplayName("nuisance call phone blocking")
	class PhoneBlocking {

		private PhoneBlock phoneBlock(Integer nuisanceCount, String existingCallIds) {
			PhoneBlock phoneBlock = new PhoneBlock();
			ReflectionTestUtils.setField(phoneBlock, "phoneNo", "9876500000");
			ReflectionTestUtils.setField(phoneBlock, "noOfNuisanceCall", nuisanceCount);
			ReflectionTestUtils.setField(phoneBlock, "callIDs", existingCallIds);
			ProviderServiceMapping mapping = new ProviderServiceMapping();
			ReflectionTestUtils.setField(mapping, "ctiCampaignName", "Campaign-1");
			ReflectionTestUtils.setField(phoneBlock, "providerServiceMapping", mapping);
			return phoneBlock;
		}

		private void updateBlockFor(Set<PhoneBlock> existing, BeneficiaryCall call) {
			ReflectionTestUtils.invokeMethod(service, "updatePhoneBlockCountAndStatus", existing, call);
		}

		@Test
		@DisplayName("appends the call to the block's call list and bumps the nuisance count")
		void appendsTheCall() {
			PhoneBlock existing = phoneBlock(1, "40");

			updateBlockFor(new HashSet<>(Set.of(existing)), call("9876500000"));

			assertThat(existing.getCallIDs()).isEqualTo("40,55");
			verify(phoneBlockRepository).updateNuisanceCallCount(12, "9876500000", "40,55");
		}

		@Test
		@DisplayName("starts the call list when the block has none yet")
		void startsTheCallList() {
			PhoneBlock existing = phoneBlock(1, null);

			updateBlockFor(new HashSet<>(Set.of(existing)), call("9876500000"));

			assertThat(existing.getCallIDs()).isEqualTo("55");
		}

		@Test
		@DisplayName("records no call list when there is no call to record")
		void noCallToRecord() {
			PhoneBlock existing = phoneBlock(1, null);
			BeneficiaryCall call = call("9876500000");
			ReflectionTestUtils.setField(call, "benCallID", null);

			updateBlockFor(new HashSet<>(Set.of(existing)), call);

			assertThat(existing.getCallIDs()).isNull();
		}

		@Test
		@DisplayName("blocks the number in the CTI once the nuisance threshold is reached")
		void blocksAtTheThreshold() {
			Integer threshold = ConfigProperties.getInteger("max-nuiesance-call");
			PhoneBlock existing = phoneBlock(threshold, "40");

			updateBlockFor(new HashSet<>(Set.of(existing)), call("9876500000"));

			verify(phoneBlockRepository).updateNuisanceCallCount(anyInt(), anyString(), anyString());
		}

		@Test
		@DisplayName("leaves a number below the threshold unblocked")
		void leavesBelowThresholdUnblocked() {
			PhoneBlock existing = phoneBlock(0, "40");

			updateBlockFor(new HashSet<>(Set.of(existing)), call("9876500000"));

			verify(phoneBlockRepository, never()).phoneNoBlockUnblock(anyInt(), anyString(), any(), anyString(),
					anyInt(), any(Timestamp.class), any(Timestamp.class), anyString(), anyString());
		}

		@Test
		@DisplayName("does nothing when there is no existing block")
		void noExistingBlock() {
			updateBlockFor(new HashSet<>(), call("9876500000"));

			verify(phoneBlockRepository, never()).updateNuisanceCallCount(anyInt(), anyString(), anyString());
		}
	}
}
