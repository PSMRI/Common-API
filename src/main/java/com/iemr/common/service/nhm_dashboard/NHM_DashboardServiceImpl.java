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
package com.iemr.common.service.nhm_dashboard;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.iemr.common.data.nhm_dashboard.AbandonCallSummary;
import com.iemr.common.data.nhm_dashboard.AgentSummaryReport;
import com.iemr.common.data.nhm_dashboard.DetailedCallReport;
import com.iemr.common.notification.exception.IEMRException;
import com.iemr.common.repository.nhm_dashboard.AbandonCallSummaryRepo;
import com.iemr.common.repository.nhm_dashboard.AgentSummaryReportRepo;
import com.iemr.common.repository.nhm_dashboard.DetailedCallReportRepo;
import com.iemr.common.utils.config.ConfigProperties;
import com.iemr.common.utils.http.HttpUtils;
import com.iemr.common.utils.mapper.InputMapper;

@Service
public class NHM_DashboardServiceImpl implements NHM_DashboardService {
	private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	private static HttpUtils httpUtils = new HttpUtils();

	@Autowired
	private AbandonCallSummaryRepo abandonCallSummaryRepo;
//	@Autowired
//	private LineCallSummaryRepo lineCallSummaryRepo;
	@Autowired
	private AgentSummaryReportRepo agentSummaryReportRepo;
	@Autowired
	private DetailedCallReportRepo detailedCallReportRepo;

	@Value("${cti-server-ip}")
    private String serverURL;

	/**
	 * Number of days (ending yesterday) the detailed call report pull looks back to
	 * re-pull days that were missed. 1 = previous day only, i.e. old behaviour.
	 */
	@Value("${nhm-detailedcallreport-backfill-days:7}")
	private int detailedCallReportBackfillDays;

	@Value("${nhm-detailedcallreport-backfill-start-date:}")
	private String backfillStartDate;

	@Value("${nhm-detailedcallreport-backfill-end-date:}")
	private String backfillEndDate;

	private static final int MAX_EXPLICIT_BACKFILL_DAYS = 60;

	public String pushAbandonCalls(AbandonCallSummary abandonCallSummary) throws Exception {

		logger.info("NHM_abandon call push API request : " + abandonCallSummary.toString());
		AbandonCallSummary resultSet = abandonCallSummaryRepo.save(abandonCallSummary);

		if (resultSet != null) {
			logger.info("NHM_abandon call push data saved successfully in AMRIT");
			return "data saved successfully";
		} else
			throw new Exception("error in saving record, please contact administrator");
	}

	public String getAbandonCalls() throws Exception {

		Timestamp endDate = new Timestamp(System.currentTimeMillis());

		String[] arr = endDate.toString().split("-");
		String fromDateStr = arr[0].concat("-").concat(arr[1]).concat("-").concat("01 00:00:00.000");

		Timestamp startDate = Timestamp.valueOf(fromDateStr);

		List<AbandonCallSummary> resultSet = abandonCallSummaryRepo.findByCreatedDateBetween(startDate, endDate);

		return new Gson().toJson(resultSet);
	}

	public String getAgentSummaryReport() throws Exception {

		Timestamp endDate = new Timestamp(System.currentTimeMillis());

		String[] arr = endDate.toString().split("-");
		String fromDateStr = arr[0].concat("-").concat(arr[1]).concat("-").concat("01 00:00:00.000");

		Timestamp startDate = Timestamp.valueOf(fromDateStr);

		List<AgentSummaryReport> resultSet = agentSummaryReportRepo.findByCreatedDateBetween(startDate, endDate);

		return new Gson().toJson(resultSet);
	}

	public String getDetailedCallReport() throws Exception {

		Timestamp endDate = new Timestamp(System.currentTimeMillis());

		String[] arr = endDate.toString().split("-");
		String fromDateStr = arr[0].concat("-").concat(arr[1]).concat("-").concat("01 00:00:00.000");

		Timestamp startDate = Timestamp.valueOf(fromDateStr);

		List<DetailedCallReport> resultSet = detailedCallReportRepo.findByCallStartTimeBetween(startDate, endDate);

		return new Gson().toJson(resultSet);
	}

	public String pull_NHM_Data_CTI() throws IEMRException {
		String response = "";
		String result1 = "";
		String result2 = "";
		try {
			List<AgentSummaryReport> agentSummaryReportList = callAgentSummaryReportCTI_API();
			if (agentSummaryReportList.size() > 0) {
				result1 = saveAgentSummaryReport(agentSummaryReportList);

			}
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage());
		}

		StringBuilder detailedCallReportResult = new StringBuilder();
		for (LocalDate callDate : getPendingDetailedCallReportDates()) {
			try {
				List<DetailedCallReport> detailedCallReportList = callDetailedCallReportCTI_API(callDate);
				if (detailedCallReportList.size() > 0) {
					detailedCallReportResult.append(callDate).append(" : ")
							.append(saveNewDetailedCallReport(detailedCallReportList, callDate)).append("; ");
				}
			} catch (Exception e) {
				logger.error("DetailedCallReport pull failed for " + callDate + " - " + e.getLocalizedMessage());
			}
		}
		result2 = detailedCallReportResult.toString();

		return response.concat(result1).concat(" ").concat(result2);
	}

	
	List<LocalDate> getPendingDetailedCallReportDates() {
		LocalDate yesterday = LocalDate.now().minusDays(1);

		LocalDate explicitStart = parseBackfillDate(backfillStartDate, "start");
		LocalDate explicitEnd = parseBackfillDate(backfillEndDate, "end");
		if (explicitStart != null) {
			LocalDate lastDate = explicitEnd != null ? explicitEnd : yesterday;
			// today is still in progress, never pull it
			if (lastDate.isAfter(yesterday))
				lastDate = yesterday;
			if (lastDate.isBefore(explicitStart)) {
				logger.error("Configured detailed call report backfill range is empty - start " + explicitStart
						+ " is after end " + lastDate + ", falling back to the missing day check");
			} else {
				List<LocalDate> explicitDates = new ArrayList<>();
				for (LocalDate date = explicitStart; !date.isAfter(lastDate); date = date.plusDays(1)) {
					if (explicitDates.size() >= MAX_EXPLICIT_BACKFILL_DAYS) {
						logger.warn("Configured detailed call report backfill range exceeds "
								+ MAX_EXPLICIT_BACKFILL_DAYS + " days - stopping at " + date.minusDays(1)
								+ ", move the start date forward and run again to continue");
						break;
					}
					explicitDates.add(date);
				}
				logger.info("DetailedCallReport configured backfill range " + explicitStart + " to " + lastDate
						+ " - pulling " + explicitDates.size() + " day(s)");
				return explicitDates;
			}
		}

		int lookBackDays = detailedCallReportBackfillDays > 0 ? detailedCallReportBackfillDays : 1;
		LocalDate firstDate = yesterday.minusDays(lookBackDays - 1L);

		Set<LocalDate> existingDates = new HashSet<>();
		try {
			List<java.sql.Date> dates = detailedCallReportRepo.findExistingCallDates(
					Timestamp.valueOf(firstDate.atStartOfDay()),
					Timestamp.valueOf(yesterday.atTime(LocalTime.MAX).withNano(0)));
			for (java.sql.Date date : dates) {
				if (date != null)
					existingDates.add(date.toLocalDate());
			}
		} catch (Exception e) {
			// on any problem in gap detection, fall back to the previous behaviour
			logger.error("Error while detecting missing detailed call report dates - " + e.getLocalizedMessage());
			return Arrays.asList(yesterday);
		}

		List<LocalDate> pendingDates = new ArrayList<>();
		for (LocalDate date = firstDate; !date.isAfter(yesterday); date = date.plusDays(1)) {
			if (!existingDates.contains(date))
				pendingDates.add(date);
		}
		logger.info("DetailedCallReport pending dates between " + firstDate + " and " + yesterday + " : " + pendingDates);
		return pendingDates;
	}

	private LocalDate parseBackfillDate(String value, String label) {
		if (value == null || value.trim().isEmpty())
			return null;
		try {
			return LocalDate.parse(value.trim());
		} catch (Exception e) {
			logger.error("Ignoring detailed call report backfill " + label + " date '" + value
					+ "' - expected format yyyy-MM-dd");
			return null;
		}
	}

	String saveNewDetailedCallReport(List<DetailedCallReport> detailedCallReportList, LocalDate callDate)
			throws IEMRException {
		parseDetailedCallReportTimestamps(detailedCallReportList);

		Set<String> existingKeys = new HashSet<>();
		for (DetailedCallReport existing : detailedCallReportRepo.findByCallStartTimeBetween(
				Timestamp.valueOf(callDate.atStartOfDay()),
				Timestamp.valueOf(callDate.atTime(LocalTime.MAX).withNano(0)))) {
			existingKeys.add(getDetailedCallReportKey(existing));
		}

		List<DetailedCallReport> newRecords = new ArrayList<>();
		for (DetailedCallReport detailedCallReport : detailedCallReportList) {
			if (existingKeys.add(getDetailedCallReportKey(detailedCallReport)))
				newRecords.add(detailedCallReport);
		}

		int duplicates = detailedCallReportList.size() - newRecords.size();
		if (newRecords.isEmpty()) {
			logger.info("DetailedCallReport " + callDate + " - all " + detailedCallReportList.size()
					+ " record(s) already present, nothing to save");
			return "0 records saved, " + duplicates + " already present";
		}

		List<DetailedCallReport> resultSet = (List<DetailedCallReport>) detailedCallReportRepo.saveAll(newRecords);
		logger.info("DetailedCallReport " + callDate + " - pulled " + detailedCallReportList.size() + ", saved "
				+ resultSet.size() + ", already present " + duplicates);
		return resultSet.size() + " records saved, " + duplicates + " already present";
	}

	/**
	 * Natural key of a call record. A session can hold more than one leg (transfer,
	 * redial), so the phone number and start time are part of the key as well.
	 */
	private String getDetailedCallReportKey(DetailedCallReport detailedCallReport) {
		return String.valueOf(detailedCallReport.getSession_ID()) + '|' + detailedCallReport.getPHONE() + '|'
				+ detailedCallReport.getCallStartTime() + '|' + detailedCallReport.getAgent_ID();
	}

	public String saveAgentSummaryReport(List<AgentSummaryReport> agentSummaryReportList) throws IEMRException {

		List<AgentSummaryReport> resultSet = (List<AgentSummaryReport>) agentSummaryReportRepo
				.saveAll(agentSummaryReportList);

		return resultSet.size() + " agentSummaryReport records saved successfully";
	}

	public String saveDetailedCallReport(List<DetailedCallReport> detailedCallReportList) throws IEMRException {

		if (detailedCallReportList != null && detailedCallReportList.size() > 0) {
			parseDetailedCallReportTimestamps(detailedCallReportList);

			List<DetailedCallReport> resultSet = (List<DetailedCallReport>) detailedCallReportRepo
					.saveAll(detailedCallReportList);

			return resultSet.size() + " detailedCallReport records saved successfully";
		} else
			throw new IEMRException("please pass valid DetailedCallReport data in list");
	}

	/**
	 * CTI sends the times as strings; they are moved into the timestamp columns
	 * here. Has to run before the records are compared against what is already
	 * stored, because the comparison uses the parsed start time.
	 */
	private void parseDetailedCallReportTimestamps(List<DetailedCallReport> detailedCallReportList) {
		if (detailedCallReportList == null)
			return;

		for (DetailedCallReport detailedCallReport : detailedCallReportList) {
				try {
					if (detailedCallReport.getCall_Start_Time() != null
							&& !detailedCallReport.getCall_Start_Time().equalsIgnoreCase("0000-00-00 00:00:00"))
						detailedCallReport.setCallStartTime(Timestamp.valueOf(detailedCallReport.getCall_Start_Time()));
				} catch (Exception e) {
					logger.error("Call_Start_Time" + e.getLocalizedMessage());
				}

				try {
					if (detailedCallReport.getCall_End_Time() != null
							&& !detailedCallReport.getCall_End_Time().equalsIgnoreCase("0000-00-00 00:00:00"))
						detailedCallReport.setCallEndTime(Timestamp.valueOf(detailedCallReport.getCall_End_Time()));
				} catch (Exception e) {
					logger.error("Call_Start_Time" + e.getLocalizedMessage());
				}

				try {
					if (detailedCallReport.getAgent_Ring_Start_Time() != null
							&& !detailedCallReport.getAgent_Ring_Start_Time().equalsIgnoreCase("0000-00-00 00:00:00"))
						detailedCallReport.setAgent_Ring_Start_Time_T(
								Timestamp.valueOf(detailedCallReport.getAgent_Ring_Start_Time()));
				} catch (Exception e) {
					logger.error("Call_Start_Time" + e.getLocalizedMessage());
				}

				try {
					if (detailedCallReport.getAgent_Ring_End_Time() != null
							&& !detailedCallReport.getAgent_Ring_End_Time().equalsIgnoreCase("0000-00-00 00:00:00"))
						detailedCallReport.setAgent_Ring_End_Time_T(
								Timestamp.valueOf(detailedCallReport.getAgent_Ring_End_Time()));
				} catch (Exception e) {
					logger.error("Call_Start_Time" + e.getLocalizedMessage());
				}
		}
	}

	public List<AgentSummaryReport> callAgentSummaryReportCTI_API() throws IEMRException {
		List<AgentSummaryReport> agentSummaryReportList = new ArrayList<AgentSummaryReport>();
//		String job = ConfigProperties.getPropertyByName("get-agent-summary-report-job");

		String endDate = null;
		String fromDate = null;
		
		LocalDateTime date = null;
		date = LocalDateTime.now().minusDays(1);
		String[] dateArr = date.toString().split("T");
		endDate = dateArr[0].concat(" 23:59:59");
		fromDate = dateArr[0].concat(" 00:00:00");

//		if (job != null && job.toLowerCase().contains("hour")) {
//			String jobVal = job.split(" ")[0];
//			LocalDateTime nowTime = LocalDateTime.now();
//			endDate = nowTime.toString().replace("T", " ");
//			String[] arr = endDate.split("\\.");
//			endDate = arr[0];
//
//			LocalDateTime nowTime_hrs = nowTime.minusHours(Integer.valueOf(jobVal));
//			fromDate = nowTime_hrs.toString().replace("T", " ");
//			String[] arr1 = fromDate.split("\\.");
//			fromDate = arr1[0];
//
//		} else
//			throw new IEMRException("Please pass correct period for schedular - in hours");

		String ctiURI = ConfigProperties.getPropertyByName("get-agent-summary-report-URL");
		// String serverURL = ConfigProperties.getPropertyByName("cti-server-ip");
		ctiURI = ctiURI.replace("CTI_SERVER", serverURL);
		ctiURI = ctiURI.replace("END_DATE", endDate);
		ctiURI = ctiURI.replace("START_DATE", fromDate);
		logger.info("AgentSummaryReport cti call request URL - " + ctiURI);
		String response = httpUtils.get(ctiURI);
		logger.info("AgentSummaryReport cti call response - " + response);
		if (response.contains("Please wait for 1 hour"))
			throw new IEMRException(response);
		else if (response.toLowerCase().contains("no data"))
			throw new IEMRException(response);
		AgentSummaryReport[] arr = InputMapper.gson().fromJson(response, AgentSummaryReport[].class);
		agentSummaryReportList = Arrays.asList(arr);
		return agentSummaryReportList;
	}

	public List<DetailedCallReport> callDetailedCallReportCTI_API() throws IEMRException {
		return callDetailedCallReportCTI_API(LocalDate.now().minusDays(1));
	}

	public List<DetailedCallReport> callDetailedCallReportCTI_API(LocalDate callDate) throws IEMRException {
		List<DetailedCallReport> detailedCallReportList = new ArrayList<DetailedCallReport>();
//		String job = ConfigProperties.getPropertyByName("get-details-call-report-job");

		// full day window - 00:00:00 and not 00:00:01, else calls placed in the very
		// first second of the day are dropped
		String fromDate = callDate.toString().concat(" 00:00:00");
		String endDate = callDate.toString().concat(" 23:59:59");

//		if (job != null && job.toLowerCase().contains("hour")) {
//			String jobVal = job.split(" ")[0];
//			LocalDateTime nowTime = LocalDateTime.now();
//			endDate = nowTime.toString().replace("T", " ");
//			String[] arr = endDate.split("\\.");
//			endDate = arr[0];
//
//			LocalDateTime nowTime_hrs = nowTime.minusHours(Integer.valueOf(jobVal));
//			fromDate = nowTime_hrs.toString().replace("T", " ");
//			String[] arr1 = fromDate.split("\\.");
//			fromDate = arr1[0];
//
//		} else
//			throw new IEMRException("Please pass correct period for schedular - in hours");

		String ctiURI = ConfigProperties.getPropertyByName("get-details-call-report-URL");
		// String serverURL = ConfigProperties.getPropertyByName("cti-server-ip");
		ctiURI = ctiURI.replace("CTI_SERVER", serverURL);
		ctiURI = ctiURI.replace("END_DATE", endDate);
		ctiURI = ctiURI.replace("START_DATE", fromDate);
		logger.info("DetailedCallReport cti call request URL - " + ctiURI);
		String response = httpUtils.get(ctiURI);
		logger.info("DetailedCallReport cti call response - " + response);
		if (response.contains("Please wait for 1 hour"))
			throw new IEMRException(response);
		else if (response.toLowerCase().contains("no data"))
			throw new IEMRException(response);
		DetailedCallReport[] arr = InputMapper.gson().fromJson(response, DetailedCallReport[].class);
		detailedCallReportList = Arrays.asList(arr);
		return detailedCallReportList;
	}
}
