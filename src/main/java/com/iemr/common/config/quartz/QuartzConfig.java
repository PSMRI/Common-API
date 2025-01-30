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



import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import com.iemr.common.utils.config.ConfigProperties;

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

	@PostConstruct
	public void init() {
		log.debug("QuartzConfig initialized.");
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
				processMQTriggerForAvniRegistration().getObject(),
				processMQTriggerForNHMDashboardData().getObject(), processMQTriggerForGrievanceDataSync().getObject() };

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
		Boolean startJob = ConfigProperties.getBoolean("start-unblock-scheduler");
		CronTriggerFactoryBean cronTriggerFactoryBean = new CronTriggerFactoryBean();
		;
		String scheduleConfig = quartzJobDefaultSchedule;
		if (startJob) {
			scheduleConfig = ConfigProperties.getPropertyByName("cron-scheduler-unblock");
		}
		cronTriggerFactoryBean.setJobDetail(processMQJobForUnblock().getObject());
		cronTriggerFactoryBean.setCronExpression(scheduleConfig);
		cronTriggerFactoryBean.setGroup(quartzJobGroup);
		return cronTriggerFactoryBean;
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
		Boolean startJob = ConfigProperties.getBoolean("start-sms-scheduler");
		CronTriggerFactoryBean cronTriggerFactoryBean = new CronTriggerFactoryBean();
		;
		String scheduleConfig = quartzJobDefaultSchedule;
		if (startJob) {
			scheduleConfig = ConfigProperties.getPropertyByName("cron-scheduler-sms");
		}
		cronTriggerFactoryBean.setJobDetail(processMQJobForSMS().getObject());
		cronTriggerFactoryBean.setCronExpression(scheduleConfig);
		cronTriggerFactoryBean.setGroup(quartzJobGroup);
		return cronTriggerFactoryBean;
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
		Boolean startJob = ConfigProperties.getBoolean("start-email-scheduler");
		CronTriggerFactoryBean cronTriggerFactoryBean = new CronTriggerFactoryBean();
		String scheduleConfig = quartzJobDefaultSchedule;
		if (Boolean.TRUE.equals(startJob)) {
			scheduleConfig = ConfigProperties.getPropertyByName("cron-scheduler-email");
		}
		cronTriggerFactoryBean.setJobDetail(processMQJobForEmail().getObject());
		cronTriggerFactoryBean.setCronExpression(scheduleConfig);
		cronTriggerFactoryBean.setGroup(quartzJobGroup);
		return cronTriggerFactoryBean;
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
		Boolean startJob = ConfigProperties.getBoolean("start-registration-scheduler");
		CronTriggerFactoryBean cronTriggerFactoryBean = new CronTriggerFactoryBean();
		String scheduleConfig = quartzJobDefaultSchedule;
		if (startJob) {
			scheduleConfig = ConfigProperties.getPropertyByName("cron-scheduler-registration");
		}
		cronTriggerFactoryBean.setJobDetail(processMQJobForRegistration().getObject());
		cronTriggerFactoryBean.setCronExpression(scheduleConfig);
		cronTriggerFactoryBean.setGroup(quartzJobGroup);
		return cronTriggerFactoryBean;
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
		Boolean startJob = ConfigProperties.getBoolean("start-everwelldatasync-scheduler");
		CronTriggerFactoryBean cronTriggerFactoryBean = new CronTriggerFactoryBean();
		String scheduleConfig = quartzJobDefaultSchedule;
		if (startJob) {
			scheduleConfig = ConfigProperties.getPropertyByName("cron-scheduler-everwelldatasync");
		}
		cronTriggerFactoryBean.setJobDetail(processMQJobForEverwellDataSync().getObject());
		cronTriggerFactoryBean.setCronExpression(scheduleConfig);
		cronTriggerFactoryBean.setGroup(quartzJobGroup);
		return cronTriggerFactoryBean;
	}

	//-----------------Grievance Data Sync Scheduler----------------------------------------------

	@Bean
	public JobDetailFactoryBean processMQJobForGrievanceDataSync() {
		JobDetailFactoryBean jobDetailFactory;
		jobDetailFactory = new JobDetailFactoryBean();
		jobDetailFactory.setJobClass(ScheduleForGrievanceDataSync.class);
		jobDetailFactory.setGroup(quartzJobGroup);
		return jobDetailFactory;
	}
	
	@Bean
	public CronTriggerFactoryBean processMQTriggerForGrievanceDataSync() {
		Boolean startJob = ConfigProperties.getBoolean("start-grievancedatasync-scheduler");
		CronTriggerFactoryBean cronTriggerFactoryBean = new CronTriggerFactoryBean();
		String scheduleConfig = quartzJobDefaultSchedule;
		if (startJob) {
			scheduleConfig = ConfigProperties.getPropertyByName("cron-scheduler-grievancedatasync");
		}
		cronTriggerFactoryBean.setJobDetail(processMQJobForGrievanceDataSync().getObject());
		cronTriggerFactoryBean.setCronExpression(scheduleConfig);
		cronTriggerFactoryBean.setGroup(quartzJobGroup);
		return cronTriggerFactoryBean;
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
		Boolean startJob = ConfigProperties.getBoolean("start-ctidatasync-scheduler");
		CronTriggerFactoryBean cronTriggerFactoryBean = new CronTriggerFactoryBean();
		String scheduleConfig = quartzJobDefaultSchedule;
		if (startJob) {
			scheduleConfig = ConfigProperties.getPropertyByName("cron-scheduler-ctidatasync");
		}
		cronTriggerFactoryBean.setJobDetail(processMQJobForCtiDataSync().getObject());
		cronTriggerFactoryBean.setCronExpression(scheduleConfig);
		cronTriggerFactoryBean.setGroup(quartzJobGroup);
		return cronTriggerFactoryBean;
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
		Boolean startJob = ConfigProperties.getBoolean("start-avni-scheduler");
		CronTriggerFactoryBean cronTriggerFactoryBean = new CronTriggerFactoryBean();
		String scheduleConfig = quartzJobDefaultSchedule;
		if (startJob) {
			scheduleConfig = ConfigProperties.getPropertyByName("cron-avni-registration");
		}
		cronTriggerFactoryBean.setJobDetail(processMQJobForAvniRegistration().getObject());
		cronTriggerFactoryBean.setCronExpression(scheduleConfig);
		cronTriggerFactoryBean.setGroup(quartzJobGroup);
		return cronTriggerFactoryBean;
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
		Boolean startJob = ConfigProperties.getBoolean("start-nhmdashboard-scheduler");
		CronTriggerFactoryBean cronTriggerFactoryBean = new CronTriggerFactoryBean();
		String scheduleConfig = quartzJobDefaultSchedule;
		if (startJob) {
			scheduleConfig = ConfigProperties.getPropertyByName("cron-scheduler-nhmdashboard");
		}
		cronTriggerFactoryBean.setJobDetail(processMQJobForNHMDashboardData().getObject());
		cronTriggerFactoryBean.setCronExpression(scheduleConfig);
		cronTriggerFactoryBean.setGroup(quartzJobGroup);

		return cronTriggerFactoryBean;
	}

}
