/*
* AMRIT – Accessible Medical Records via Integrated Technology 
* Integrated EHR (Electronic Health Records) Solution 
*
* Copyright (C) "Piramal Swasthya Management and Research Institute" 
*
* This file is part of AMRIT.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see https://www.gnu.org/licenses/.
*/
package com.iemr.common.config.quartz;

import java.io.IOException;
import java.util.Properties;

import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import jakarta.annotation.PostConstruct;

@Configuration
public class QuartzConfig {

	private final Logger log = LoggerFactory.getLogger(this.getClass().getName());
	private static final String quartzJobGroup = "spring-quartz";
	private static final String quartzJobDefaultSchedule = "0 0 0 31 12 ? *";

	@Autowired
	private PlatformTransactionManager transactionManager;

	@Autowired
	private ApplicationContext applicationContext;

	/*
	 * These are read through @Value and not through ConfigProperties on purpose.
	 * ConfigProperties keeps the Environment in a static field that is populated by
	 * an @Autowired setter on its own bean, so it can still be null while the @Bean
	 * methods below run - in that case it falls back to reading application.properties
	 * straight off the classpath, and any ${ENV_VAR} placeholder in there comes back
	 * as the literal text. getBoolean() then quietly turns that into false and the job
	 * is scheduled with quartzJobDefaultSchedule instead, with nothing in the log.
	 * Injected fields are set before any @Bean method is called and go through the
	 * normal placeholder resolution, so environment overrides are honoured.
	 */
	@Value("${start-unblock-scheduler:false}")
	private boolean startUnblockJob;
	@Value("${cron-scheduler-unblock:" + quartzJobDefaultSchedule + "}")
	private String unblockSchedule;

	@Value("${start-sms-scheduler:false}")
	private boolean startSmsJob;
	@Value("${cron-scheduler-sms:" + quartzJobDefaultSchedule + "}")
	private String smsSchedule;

	@Value("${start-email-scheduler:false}")
	private boolean startEmailJob;
	@Value("${cron-scheduler-email:" + quartzJobDefaultSchedule + "}")
	private String emailSchedule;

	@Value("${start-registration-scheduler:false}")
	private boolean startRegistrationJob;
	@Value("${cron-scheduler-registration:" + quartzJobDefaultSchedule + "}")
	private String registrationSchedule;

	@Value("${start-everwelldatasync-scheduler:false}")
	private boolean startEverwellDataSyncJob;
	@Value("${cron-scheduler-everwelldatasync:" + quartzJobDefaultSchedule + "}")
	private String everwellDataSyncSchedule;

	@Value("${start-ctidatasync-scheduler:false}")
	private boolean startCtiDataSyncJob;
	@Value("${cron-scheduler-ctidatasync:" + quartzJobDefaultSchedule + "}")
	private String ctiDataSyncSchedule;

	@Value("${start-avni-scheduler:false}")
	private boolean startAvniRegistrationJob;
	@Value("${cron-avni-registration:" + quartzJobDefaultSchedule + "}")
	private String avniRegistrationSchedule;

	@Value("${start-nhmdashboard-scheduler:false}")
	private boolean startNhmDashboardJob;
	@Value("${cron-scheduler-nhmdashboard:" + quartzJobDefaultSchedule + "}")
	private String nhmDashboardSchedule;

	@PostConstruct
	public void init() {
		log.debug("QuartzConfig initialized.");
	}

	/**
	 * Builds the trigger for a job, logging what it resolved to. A job that is
	 * switched off gets quartzJobDefaultSchedule, which only comes round on the 31st
	 * of December - so the log line is the only way to tell "off" apart from
	 * "misconfigured" without waiting until the end of the year.
	 */
	private CronTriggerFactoryBean cronTrigger(String jobName, boolean startJob, String schedule,
			JobDetail jobDetail) {
		String scheduleConfig = startJob ? schedule : quartzJobDefaultSchedule;
		log.info("Quartz job {} - enabled: {}, cron: {}", jobName, startJob, scheduleConfig);

		CronTriggerFactoryBean cronTriggerFactoryBean = new CronTriggerFactoryBean();
		cronTriggerFactoryBean.setJobDetail(jobDetail);
		cronTriggerFactoryBean.setCronExpression(scheduleConfig);
		cronTriggerFactoryBean.setGroup(quartzJobGroup);
		return cronTriggerFactoryBean;
	}

	@Bean
	public Properties quartzProperties() {
		PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
		propertiesFactoryBean.setLocation(new ClassPathResource("/application.properties"));
		Properties properties = null;
		try {
			propertiesFactoryBean.afterPropertiesSet();
			properties = propertiesFactoryBean.getObject();

		} catch (IOException e) {
			log.warn("Cannot load application.properties.");
		}

		return properties;
	}

	@Bean
	public SchedulerFactoryBean quartzScheduler() {
		SchedulerFactoryBean quartzScheduler = new SchedulerFactoryBean();

		// quartzScheduler.setso;
		quartzScheduler.setTransactionManager(transactionManager);
		quartzScheduler.setOverwriteExistingJobs(true);
		quartzScheduler.setSchedulerName("jelies-quartz-scheduler");

		// custom job factory of spring with DI support for @Autowired!
		AutowiringSpringBeanJobFactory jobFactory = new AutowiringSpringBeanJobFactory();
		jobFactory.setApplicationContext(applicationContext);
		quartzScheduler.setJobFactory(jobFactory);

		quartzScheduler.setQuartzProperties(quartzProperties());

		Trigger[] triggers = { processMQTriggerForUnblock().getObject(), processMQTriggerForSMS().getObject(),
				processMQTriggerForEmail().getObject(), processMQTriggerForRegistration().getObject(),
				processMQTriggerForEverwellDataSync().getObject(), processMQTriggerForCtiDataSync().getObject(),
				processMQTriggerForAvniRegistration().getObject(), processMQTriggerForNHMDashboardData().getObject() };

		quartzScheduler.setTriggers(triggers);

		return quartzScheduler;
	}

// --------------------------------------------------------------------------------------------------------------
	@Bean
	public JobDetailFactoryBean processMQJobForUnblock() {
		JobDetailFactoryBean jobDetailFactory;
		jobDetailFactory = new JobDetailFactoryBean();
		jobDetailFactory.setJobClass(ScheduleJobServiceForUnblock.class);
		jobDetailFactory.setGroup(quartzJobGroup);
		return jobDetailFactory;
	}

	@Bean
	public CronTriggerFactoryBean processMQTriggerForUnblock() {
		return cronTrigger("unblock", startUnblockJob, unblockSchedule, processMQJobForUnblock().getObject());
	}

	// --------------------------------------------------------------------------------------------------------------
	@Bean
	public JobDetailFactoryBean processMQJobForSMS() {
		JobDetailFactoryBean jobDetailFactory;
		jobDetailFactory = new JobDetailFactoryBean();
		jobDetailFactory.setJobClass(ScheduleJobServiceForSMS.class);
		jobDetailFactory.setGroup(quartzJobGroup);
		return jobDetailFactory;
	}

	@Bean
	public CronTriggerFactoryBean processMQTriggerForSMS() {
		return cronTrigger("sms", startSmsJob, smsSchedule, processMQJobForSMS().getObject());
	}

	// --------------------------------------------------------------------------------------------------------------
	@Bean
	public JobDetailFactoryBean processMQJobForEmail() {
		JobDetailFactoryBean jobDetailFactory;
		jobDetailFactory = new JobDetailFactoryBean();
		jobDetailFactory.setJobClass(ScheduleJobServiceForEmail.class);
		jobDetailFactory.setGroup(quartzJobGroup);
		return jobDetailFactory;
	}

	@Bean
	public CronTriggerFactoryBean processMQTriggerForEmail() {
		return cronTrigger("email", startEmailJob, emailSchedule, processMQJobForEmail().getObject());
	}

	// --------------------------------------------------------------------------------------------------------------
	@Bean
	public JobDetailFactoryBean processMQJobForRegistration() {
		JobDetailFactoryBean jobDetailFactory;
		jobDetailFactory = new JobDetailFactoryBean();
		jobDetailFactory.setJobClass(ScheduleForEverwellRegistration.class);
		jobDetailFactory.setGroup(quartzJobGroup);
		return jobDetailFactory;
	}

	@Bean
	public CronTriggerFactoryBean processMQTriggerForRegistration() {
		return cronTrigger("everwell-registration", startRegistrationJob, registrationSchedule,
				processMQJobForRegistration().getObject());
	}

	// --------------------------------------------------------------------------------------------------------------
	@Bean
	public JobDetailFactoryBean processMQJobForEverwellDataSync() {
		JobDetailFactoryBean jobDetailFactory;
		jobDetailFactory = new JobDetailFactoryBean();
		jobDetailFactory.setJobClass(ScheduleForEverwellDataSync.class);
		jobDetailFactory.setGroup(quartzJobGroup);
		return jobDetailFactory;
	}

	@Bean
	public CronTriggerFactoryBean processMQTriggerForEverwellDataSync() {
		return cronTrigger("everwell-datasync", startEverwellDataSyncJob, everwellDataSyncSchedule,
				processMQJobForEverwellDataSync().getObject());
	}

	// --------------------------------------------------------------------------------------------------------------
	@Bean
	public JobDetailFactoryBean processMQJobForCtiDataSync() {
		JobDetailFactoryBean jobDetailFactory;
		jobDetailFactory = new JobDetailFactoryBean();
		jobDetailFactory.setJobClass(ScheduleForCallCentre.class);
		jobDetailFactory.setGroup(quartzJobGroup);
		return jobDetailFactory;
	}

	@Bean
	public CronTriggerFactoryBean processMQTriggerForCtiDataSync() {
		return cronTrigger("cti-datasync", startCtiDataSyncJob, ctiDataSyncSchedule,
				processMQJobForCtiDataSync().getObject());
	}

	// --------------------------------------------------------------------------------------------------------------
	@Bean
	public JobDetailFactoryBean processMQJobForAvniRegistration() {
		JobDetailFactoryBean jobDetailFactory;
		jobDetailFactory = new JobDetailFactoryBean();
		jobDetailFactory.setJobClass(ScheduleJobServiceForAvniRegistration.class);
		jobDetailFactory.setGroup(quartzJobGroup);
		return jobDetailFactory;
	}

	@Bean
	public CronTriggerFactoryBean processMQTriggerForAvniRegistration() {
		return cronTrigger("avni-registration", startAvniRegistrationJob, avniRegistrationSchedule,
				processMQJobForAvniRegistration().getObject());
	}

	// --------------------------------------------------------------------------------------------------------------
	@Bean
	public JobDetailFactoryBean processMQJobForNHMDashboardData() {
		JobDetailFactoryBean jobDetailFactory;
		jobDetailFactory = new JobDetailFactoryBean();
		jobDetailFactory.setJobClass(ScheduleJobForNHMDashboardData.class);
		jobDetailFactory.setGroup(quartzJobGroup);
		return jobDetailFactory;
	}

	@Bean
	public CronTriggerFactoryBean processMQTriggerForNHMDashboardData() {
		return cronTrigger("nhm-dashboard", startNhmDashboardJob, nhmDashboardSchedule,
				processMQJobForNHMDashboardData().getObject());
	}

}
