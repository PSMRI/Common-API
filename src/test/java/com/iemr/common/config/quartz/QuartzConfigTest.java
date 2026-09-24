package com.iemr.common.config.quartz;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Properties;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.function.Function;

/**
 * Covers the Quartz wiring for the background jobs.
 *
 * <p>Each job has a detail bean and a cron trigger, and a job that is switched off is
 * parked on a far-future cron expression rather than removed. Both of those are asserted
 * per job, along with the scheduler that collects every trigger.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class QuartzConfigTest {

	/** The cron expression the config parks a disabled job on: 31 December, never reached. */
	private static final String PARKED_SCHEDULE = "0 0 0 31 12 ? *";
	private static final String ENABLED_SCHEDULE = "0 0/5 * * * ?";
	private static final String JOB_GROUP = "spring-quartz";

	@Mock
	private PlatformTransactionManager transactionManager;
	@Mock
	private ApplicationContext applicationContext;

	private QuartzConfig config;

	@BeforeEach
	void setUp() {
		config = new QuartzConfig();
		ReflectionTestUtils.setField(config, "transactionManager", transactionManager);
		ReflectionTestUtils.setField(config, "applicationContext", applicationContext);
		for (String schedule : new String[] { "unblockSchedule", "smsSchedule", "emailSchedule",
				"registrationSchedule", "everwellDataSyncSchedule", "ctiDataSyncSchedule",
				"avniRegistrationSchedule", "nhmDashboardSchedule" }) {
			ReflectionTestUtils.setField(config, schedule, ENABLED_SCHEDULE);
		}
	}

	private void enable(String flag, boolean enabled) {
		ReflectionTestUtils.setField(config, flag, enabled);
	}

	private static CronTriggerFactoryBean initialised(CronTriggerFactoryBean trigger) {
		try {
			trigger.afterPropertiesSet();
		} catch (java.text.ParseException e) {
			throw new AssertionError("the configured cron expression should be parseable", e);
		}
		return trigger;
	}

	static Stream<Arguments> jobs() {
		return Stream.of(
				Arguments.of("unblock", "startUnblockJob",
						(Function<QuartzConfig, JobDetailFactoryBean>) QuartzConfig::processMQJobForUnblock,
						(Function<QuartzConfig, CronTriggerFactoryBean>) QuartzConfig::processMQTriggerForUnblock),
				Arguments.of("sms", "startSmsJob",
						(Function<QuartzConfig, JobDetailFactoryBean>) QuartzConfig::processMQJobForSMS,
						(Function<QuartzConfig, CronTriggerFactoryBean>) QuartzConfig::processMQTriggerForSMS),
				Arguments.of("email", "startEmailJob",
						(Function<QuartzConfig, JobDetailFactoryBean>) QuartzConfig::processMQJobForEmail,
						(Function<QuartzConfig, CronTriggerFactoryBean>) QuartzConfig::processMQTriggerForEmail),
				Arguments.of("registration", "startRegistrationJob",
						(Function<QuartzConfig, JobDetailFactoryBean>) QuartzConfig::processMQJobForRegistration,
						(Function<QuartzConfig, CronTriggerFactoryBean>) QuartzConfig::processMQTriggerForRegistration),
				Arguments.of("everwellDataSync", "startEverwellDataSyncJob",
						(Function<QuartzConfig, JobDetailFactoryBean>) QuartzConfig::processMQJobForEverwellDataSync,
						(Function<QuartzConfig, CronTriggerFactoryBean>) QuartzConfig::processMQTriggerForEverwellDataSync),
				Arguments.of("ctiDataSync", "startCtiDataSyncJob",
						(Function<QuartzConfig, JobDetailFactoryBean>) QuartzConfig::processMQJobForCtiDataSync,
						(Function<QuartzConfig, CronTriggerFactoryBean>) QuartzConfig::processMQTriggerForCtiDataSync),
				Arguments.of("avniRegistration", "startAvniRegistrationJob",
						(Function<QuartzConfig, JobDetailFactoryBean>) QuartzConfig::processMQJobForAvniRegistration,
						(Function<QuartzConfig, CronTriggerFactoryBean>) QuartzConfig::processMQTriggerForAvniRegistration),
				Arguments.of("nhmDashboard", "startNhmDashboardJob",
						(Function<QuartzConfig, JobDetailFactoryBean>) QuartzConfig::processMQJobForNHMDashboardData,
						(Function<QuartzConfig, CronTriggerFactoryBean>) QuartzConfig::processMQTriggerForNHMDashboardData));
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("jobs")
	@DisplayName("the job detail is registered in the Quartz group with a job class")
	void jobDetailIsRegistered(String name, String flag, Function<QuartzConfig, JobDetailFactoryBean> jobDetail,
			Function<QuartzConfig, CronTriggerFactoryBean> trigger) {
		JobDetailFactoryBean factory = jobDetail.apply(config);
		factory.afterPropertiesSet();

		assertThat(factory.getObject()).isNotNull();
		assertThat(factory.getObject().getKey().getGroup()).isEqualTo(JOB_GROUP);
		assertThat(factory.getObject().getJobClass()).isNotNull();
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("jobs")
	@DisplayName("an enabled job runs on its configured schedule")
	void enabledJobUsesItsSchedule(String name, String flag,
			Function<QuartzConfig, JobDetailFactoryBean> jobDetail,
			Function<QuartzConfig, CronTriggerFactoryBean> trigger) {
		enable(flag, true);

		assertThat(initialised(trigger.apply(config)).getObject().getCronExpression()).isEqualTo(ENABLED_SCHEDULE);
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("jobs")
	@DisplayName("a disabled job is parked on a schedule that never fires")
	void disabledJobIsParked(String name, String flag, Function<QuartzConfig, JobDetailFactoryBean> jobDetail,
			Function<QuartzConfig, CronTriggerFactoryBean> trigger) {
		enable(flag, false);

		assertThat(initialised(trigger.apply(config)).getObject().getCronExpression()).isEqualTo(PARKED_SCHEDULE);
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("jobs")
	@DisplayName("the trigger is registered in the Quartz group")
	void triggerIsRegisteredInTheGroup(String name, String flag,
			Function<QuartzConfig, JobDetailFactoryBean> jobDetail,
			Function<QuartzConfig, CronTriggerFactoryBean> trigger) {
		enable(flag, true);

		assertThat(initialised(trigger.apply(config)).getObject().getKey().getGroup()).isEqualTo(JOB_GROUP);
	}

	@Nested
	@DisplayName("quartzProperties")
	class QuartzProperties {

		@Test
		@DisplayName("loads the application properties for Quartz")
		void loadsApplicationProperties() {
			Properties properties = config.quartzProperties();

			assertThat(properties).isNotNull();
			assertThat(properties).isNotEmpty();
		}
	}

	@Nested
	@DisplayName("quartzScheduler")
	class Scheduler {

		@Test
		@DisplayName("collects a trigger for every background job")
		void collectsEveryTrigger() {
			SchedulerFactoryBean scheduler = config.quartzScheduler();

			assertThat(scheduler).isNotNull();
			assertThat((java.util.List<?>) ReflectionTestUtils.getField(scheduler, "triggers")).hasSize(8);
		}

		@Test
		@DisplayName("is named and set to overwrite jobs it already knows")
		void isNamedAndOverwrites() {
			SchedulerFactoryBean scheduler = config.quartzScheduler();

			assertThat(ReflectionTestUtils.getField(scheduler, "schedulerName"))
					.isEqualTo("jelies-quartz-scheduler");
			assertThat(ReflectionTestUtils.getField(scheduler, "overwriteExistingJobs")).isEqualTo(true);
		}

		@Test
		@DisplayName("wires the transaction manager and an autowiring job factory")
		void wiresItsCollaborators() {
			SchedulerFactoryBean scheduler = config.quartzScheduler();

			assertThat(ReflectionTestUtils.getField(scheduler, "transactionManager"))
					.isSameAs(transactionManager);
			assertThat(ReflectionTestUtils.getField(scheduler, "jobFactory"))
					.isInstanceOf(AutowiringSpringBeanJobFactory.class);
		}
	}

	@Test
	@DisplayName("initialisation logs and does no further work")
	void initialisationDoesNoFurtherWork() {
		config.init();

		org.mockito.Mockito.verifyNoInteractions(transactionManager, applicationContext);
	}
}
