package com.iemr.common.config.quartz;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iemr.common.service.grievance.GrievanceDataSync;

@Service
@Transactional
public class ScheduleForGrievanceDataSync implements Job {

	private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	
	private final GrievanceDataSync grievanceDataSync;
	
	@Autowired
	public ScheduleForGrievanceDataSync(GrievanceDataSync grievanceDataSync) {
		this.grievanceDataSync = grievanceDataSync;
	}
	
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException
	{
		logger.info("Started job for grievance data sync {}", arg0.getClass().getName());
		grievanceDataSync.dataSyncToGrievance();
		logger.info("Completed job for grievance data sync {}" , arg0.getClass().getName());
	}

	

}
