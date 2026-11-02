package com.iemr.common.config.quartz;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.iemr.common.service.grievance.GrievanceDataSync;

/** Covers the flag that gates the grievance data-sync job. */
@ExtendWith(MockitoExtension.class)
class ScheduleForGrievanceDataSyncTest {

	@Mock
	private GrievanceDataSync grievanceDataSync;

	private ScheduleForGrievanceDataSync scheduler(boolean enabled) {
		ScheduleForGrievanceDataSync scheduler = new ScheduleForGrievanceDataSync(grievanceDataSync);
		ReflectionTestUtils.setField(scheduler, "grievanceFlag", enabled);
		return scheduler;
	}

	@Test
	@DisplayName("the sync runs when the scheduler is switched on")
	void syncRunsWhenEnabled() {
		scheduler(true).execute();

		verify(grievanceDataSync).dataSyncToGrievance();
	}

	@Test
	@DisplayName("the sync is skipped when the scheduler is switched off")
	void syncIsSkippedWhenDisabled() {
		scheduler(false).execute();

		verifyNoInteractions(grievanceDataSync);
	}
}
