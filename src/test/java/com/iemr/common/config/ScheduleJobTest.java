package com.iemr.common.config;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerContext;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import com.iemr.common.service.callhandling.BeneficiaryCallService;

/** Covers the Quartz job that unblocks blocked beneficiary phone numbers. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ScheduleJobTest {

	@Mock
	private JobExecutionContext context;
	@Mock
	private BeneficiaryCallService beneficiaryCallService;

	@Test
	@DisplayName("an autowired job unblocks the blocked numbers")
	void wiredJobUnblocksNumbers() throws Exception {
		when(context.getFireInstanceId()).thenReturn("fire-1");
		ScheduleJob job = new ScheduleJob();
		ReflectionTestUtils.setField(job, "beneficiaryCallService", beneficiaryCallService);

		job.execute(context);

		verify(beneficiaryCallService).unblockBlockedNumbers();
	}

	@Test
	@DisplayName("a job with no service wired in logs and does nothing")
	void unwiredJobDoesNothing() {
		when(context.getFireInstanceId()).thenReturn("fire-2");

		assertThatCode(() -> new ScheduleJob().execute(context)).doesNotThrowAnyException();

		verifyNoInteractions(beneficiaryCallService);
	}

	@Test
	@DisplayName("a failure while unblocking is logged rather than failing the trigger")
	void failureWhileUnblockingIsSwallowed() throws Exception {
		when(context.getFireInstanceId()).thenReturn("fire-3");
		doThrow(new IllegalStateException("database offline")).when(beneficiaryCallService).unblockBlockedNumbers();
		ScheduleJob job = new ScheduleJob();
		ReflectionTestUtils.setField(job, "beneficiaryCallService", beneficiaryCallService);

		assertThatCode(() -> job.execute(context)).doesNotThrowAnyException();
	}

	@Test
	@DisplayName("the service is resolved from the scheduler's application context")
	void serviceIsResolvedFromTheSchedulerContext() throws Exception {
		ApplicationContext applicationContext = mock(ApplicationContext.class);
		when(applicationContext.getBean(BeneficiaryCallService.class)).thenReturn(beneficiaryCallService);
		SchedulerContext schedulerContext = new SchedulerContext();
		schedulerContext.put("applicationContext", applicationContext);
		Scheduler scheduler = mock(Scheduler.class);
		when(scheduler.getContext()).thenReturn(schedulerContext);
		when(context.getScheduler()).thenReturn(scheduler);
		when(context.getFireInstanceId()).thenReturn("fire-4");

		ScheduleJob job = new ScheduleJob();
		ReflectionTestUtils.invokeMethod(job, "getBeansFromContext", context);

		job.execute(context);
		verify(beneficiaryCallService).unblockBlockedNumbers();
	}
}
