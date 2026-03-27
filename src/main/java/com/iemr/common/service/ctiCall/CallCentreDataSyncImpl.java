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
package com.iemr.common.service.ctiCall;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import com.iemr.common.data.callhandling.BeneficiaryCall;
import com.iemr.common.data.report.CTIData;
import com.iemr.common.data.report.CTIResponse;
import com.iemr.common.repository.report.CallReportRepo;
import com.iemr.common.service.cti.CTIService;
import com.iemr.common.utils.config.ConfigProperties;
import com.iemr.common.utils.http.HttpUtils;
import com.iemr.common.utils.mapper.InputMapper;
import com.iemr.common.utils.response.OutputResponse;

@Service
@PropertySource("classpath:application.properties")
public class CallCentreDataSyncImpl implements CallCentreDataSync {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

	@Autowired
	private CallReportRepo callReportRepo;
	@Value("${cti-server-ip}")
	private String ctiServerIP;

	@Value("${call-info-api-URL}")
	private String callinfoapiURL;

	@Value("${cz-duration}")
	private String CZduration;
	private static HttpUtils httpUtils;
	@Autowired
	private CTIService ctiService;
	private static String ctiLoggerURL = ConfigProperties.getPropertyByName("cti-logger_base_url");

	public CallCentreDataSyncImpl() {
		if (httpUtils == null) {
			httpUtils = new HttpUtils();
		}
	}

	@Override
	public String callUrl(String urlRequest) {
		httpUtils = new HttpUtils();
		String result = httpUtils.get(urlRequest);
		return result;
	}

	@Override
	public void ctiDataSync() {
		LocalDate currentDate = LocalDate.now();
	       // Look back 7 days to retry records that failed in previous runs
	       LocalDate startDate = currentDate.minusDays(7);
	       // Up to yesterday
	       LocalDate endDate = currentDate.minusDays(1);
	       // Convert LocalDate to LocalDateTime to set time as 00:00:00
	       LocalDateTime startDateTime = startDate.atTime(0, 0, 0);
	       LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
	       // Convert LocalDateTime to Timestamp
	       Timestamp startTimeStamp = Timestamp.valueOf(startDateTime);
	       Timestamp endTimeStamp = Timestamp.valueOf(endDateTime);
		   logger.info("startDate: " + startTimeStamp);
		logger.info("endDate: " + endTimeStamp);
		List<BeneficiaryCall> list = callReportRepo.getAllBenCallIDetails(startTimeStamp, endTimeStamp);

		if (!list.isEmpty()) {
			logger.info("Total records to process for CTI data sync: " + list.size());
			for (BeneficiaryCall call : list) {
				if (call.getCallID() == null) {
					logger.warn("Skipping record with null callID, benCallID: " + call.getBenCallID());
					continue;
				}
				String recordingPath = null;
				String callDuartion = null;
				String callEndTime = null;
				String callStartTime = null;
				try {
					JSONObject requestFile = new JSONObject();
					requestFile.put("agent_id", call.getAgentID());
					requestFile.put("session_id", call.getCallID());

					OutputResponse response1 = ctiService.getVoiceFileNew(requestFile.toString(), "extra parameter");
					if(response1 != null && response1.getStatusCode() == 200) {

						CTIResponse ctiResponsePath = InputMapper.gson().fromJson(response1.getData(),
								CTIResponse.class);
						String recordingFilePath = ctiResponsePath.getResponse().toString();
						if(recordingFilePath.length() > 20)
							recordingPath = recordingFilePath.substring(20);
						else if (!recordingFilePath.isEmpty())
							recordingPath = recordingFilePath;
						logger.info("recordingPath: " + recordingPath);
					}

					String callInfoURL = this.callinfoapiURL;
					String URL = callInfoURL.replace("CTI_SERVER", ctiServerIP).replace("AGENT_ID", call.getAgentID())
							.replace("SESSION_ID", call.getCallID()).replace("PHONE_NO", call.getPhoneNo());

					logger.info("calling CTI API url: " + URL);
					String ctiResponse = this.callUrl(URL);
					logger.info("calling CTI_CDR_CALL_INFO API returned " + ctiResponse);

					CTIData data = InputMapper.gson().fromJson(ctiResponse, CTIData.class);
					CTIResponse model = data.getResponse();

					if (model != null && "1".equals(model.getResponse_code())) {
						callDuartion = model.getCall_duration();
						callEndTime = model.getCall_end_date_time();
						callStartTime = model.getCall_start_date_time();
					} else {
						logger.warn("CTI API returned non-success for sessionID: " + call.getCallID()
								+ ", response_code: " + (model != null ? model.getResponse_code() : "null"));
					}

					// Only save if we got at least the call duration from CTI
					if (callDuartion != null) {
						call.setCZcallDuration(Integer.parseInt(callDuartion));
						call.setCZcallEndTime(callEndTime);
						call.setCZcallStartTime(callStartTime);
						call.setRecordingPath(recordingPath);
						callReportRepo.save(call);
						logger.info("CTI data sync saved for benCallID: " + call.getBenCallID());
					} else if (recordingPath != null) {
						// Duration not available yet, but recording path is — save path only
						call.setRecordingPath(recordingPath);
						callReportRepo.save(call);
						logger.info("Only recordingPath saved (duration pending) for benCallID: " + call.getBenCallID());
					} else {
						logger.warn("No CTI data available yet for sessionID: " + call.getCallID()
								+ ", benCallID: " + call.getBenCallID() + " - will retry next run");
					}
				} catch (Exception e) {
					logger.error("CTI data sync failed for benCallID: " + call.getBenCallID()
							+ ", sessionID: " + call.getCallID() + " - " + e.getMessage(), e);
				}
			}
		} else {
			logger.info("No pending records found for CTI data sync");
		}
	}
}