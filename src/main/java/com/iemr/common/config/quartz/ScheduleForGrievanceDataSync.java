package com.iemr.common.config.quartz;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


import com.iemr.common.service.grievance.GrievanceDataSync;

@Component
public class ScheduleForGrievanceDataSync {

	// @Value("${start-grievancedatasync-scheduler}")
	private boolean grievanceFlag=true;

	private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	private final GrievanceDataSync grievanceDataSync;

	@Autowired
	public ScheduleForGrievanceDataSync(GrievanceDataSync grievanceDataSync) {
		this.grievanceDataSync = grievanceDataSync;
	}

	@Scheduled(cron = "${cron-scheduler-grievancedatasync}")
	public void execute() {
		if (grievanceFlag) {
			logger.info("Started job for grievance data sync ");
			grievanceDataSync.dataSyncToGrievance();
			logger.info("Completed job for grievance data sync ");
		}

	}

}

