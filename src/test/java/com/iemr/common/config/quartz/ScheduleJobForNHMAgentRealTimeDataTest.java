package com.iemr.common.config.quartz;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.iemr.common.service.nhm_dashboard.NHM_AgentRealTimeDataService;

/** Covers the flag that gates the NHM agent real-time data pull. */
@ExtendWith(MockitoExtension.class)
class ScheduleJobForNHMAgentRealTimeDataTest {

	@Mock
	private NHM_AgentRealTimeDataService agentRealTimeDataService;

	private ScheduleJobForNHMAgentRealTimeData scheduler(boolean enabled) {
		ScheduleJobForNHMAgentRealTimeData scheduler = new ScheduleJobForNHMAgentRealTimeData();
		ReflectionTestUtils.setField(scheduler, "agentRealTimeDataService", agentRealTimeDataService);
		ReflectionTestUtils.setField(scheduler, "nhmFlag", enabled);
		return scheduler;
	}

	@Test
	@DisplayName("the pull runs when the scheduler is switched on")
	void pullRunsWhenEnabled() throws Exception {
		scheduler(true).getAllNHMAgentRealTimeData();

		verify(agentRealTimeDataService).getData();
	}

	@Test
	@DisplayName("the pull is skipped when the scheduler is switched off")
	void pullIsSkippedWhenDisabled() throws Exception {
		scheduler(false).getAllNHMAgentRealTimeData();

		verifyNoInteractions(agentRealTimeDataService);
	}
}
