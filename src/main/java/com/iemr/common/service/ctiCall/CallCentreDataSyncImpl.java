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
import com.iemr.common.repository.callhandling.BeneficiaryCallRepository;
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
	@Autowired
	private BeneficiaryCallRepository beneficiaryCallRepository;
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
	       // Calculate three days before the current date
	       LocalDate startDate = currentDate.minusDays(3);
	       // Calculate two days before the current date
	       LocalDate endDate = currentDate.minusDays(2);
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
			
			// List<Long> benList = new ArrayList<>();
			String callDuartion = null;
			String filePath = null;
			String URL = null;
			String callinfoapiURL = null;
			String ctiResponse = null;
			String callEndTime = null;
			String callStartTime = null;
			String recordingPath = "";
			for (BeneficiaryCall call : list) {
				if (call.getCallID() != null) {
					recordingPath = null;
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
							logger.info("recordingPath: " + recordingPath);
						}

						callDuartion = null;
						callinfoapiURL = this.callinfoapiURL;
						URL = callinfoapiURL.replace("CTI_SERVER", ctiServerIP).replace("AGENT_ID", call.getAgentID())
								.replace("SESSION_ID", call.getCallID()).replace("PHONE_NO", call.getPhoneNo());

						logger.info("calling CTI API url: " + URL);
						ctiResponse = this.callUrl(URL);
						logger.info("calling CTI_CDR_CALL_INFO API returned " + ctiResponse);

						CTIData data = InputMapper.gson().fromJson(ctiResponse, CTIData.class);
						CTIResponse model = data.getResponse();

						if (model.getResponse_code().equals("1")) {
							callDuartion = model.getCall_duration();
							callEndTime = model.getCall_end_date_time();
							callStartTime = model.getCall_start_date_time();
						}
						if (callDuartion != null)
							call.setCZcallDuration(Integer.parseInt(callDuartion));
						call.setRecordingPath(recordingPath);
						call.setCZcallEndTime(callEndTime);
						call.setCZcallStartTime(callStartTime);
						callReportRepo.save(call);
						logger.info("calling CTI_CDR_CALL_INFO after API call save response " + call);
					} catch (Exception e) {
						logger.error("VoiceFile failed with error " + e.getMessage(), e);
					}
				}
			}
		}
	}

	@Override
	public void syncCZCallData(Long benCallID) {
		try {
			BeneficiaryCall call = beneficiaryCallRepository.findByBenCallID(benCallID);
			if (call == null || call.getCallID() == null || call.getAgentID() == null || call.getPhoneNo() == null) {
				logger.info("syncCZCallData: insufficient data for benCallID " + benCallID);
				return;
			}

			// Fetch recording path
			String recordingPath = null;
			try {
				JSONObject requestFile = new JSONObject();
				requestFile.put("agent_id", call.getAgentID());
				requestFile.put("session_id", call.getCallID());

				OutputResponse response1 = ctiService.getVoiceFileNew(requestFile.toString(), "extra parameter");
				if (response1 != null && response1.getStatusCode() == 200) {
					CTIResponse ctiResponsePath = InputMapper.gson().fromJson(response1.getData(), CTIResponse.class);
					String recordingFilePath = ctiResponsePath.getResponse().toString();
					if (recordingFilePath.length() > 20)
						recordingPath = recordingFilePath.substring(20);
				}
			} catch (Exception e) {
				logger.error("syncCZCallData: voice file fetch failed for benCallID " + benCallID, e);
			}

			// Fetch CZ call duration and times from CTI CDR API
			String url = this.callinfoapiURL.replace("CTI_SERVER", ctiServerIP)
					.replace("AGENT_ID", call.getAgentID())
					.replace("SESSION_ID", call.getCallID())
					.replace("PHONE_NO", call.getPhoneNo());

			logger.info("syncCZCallData: calling CTI API url: " + url);
			String ctiResponse = this.callUrl(url);
			logger.info("syncCZCallData: CTI_CDR_CALL_INFO returned " + ctiResponse);

			CTIData data = InputMapper.gson().fromJson(ctiResponse, CTIData.class);
			CTIResponse model = data.getResponse();

			if (model.getResponse_code().equals("1")) {
				String callDuration = model.getCall_duration();
				if (callDuration != null)
					call.setCZcallDuration(Integer.parseInt(callDuration));
				call.setCZcallEndTime(model.getCall_end_date_time());
				call.setCZcallStartTime(model.getCall_start_date_time());
			}
			if (recordingPath != null)
				call.setRecordingPath(recordingPath);

			callReportRepo.save(call);
			logger.info("syncCZCallData: saved CZ data for benCallID " + benCallID);
		} catch (Exception e) {
			logger.error("syncCZCallData failed for benCallID " + benCallID + " with error " + e.getMessage(), e);
		}
	}
}