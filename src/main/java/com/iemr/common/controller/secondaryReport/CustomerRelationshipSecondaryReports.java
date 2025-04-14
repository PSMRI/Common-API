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
package com.iemr.common.controller.secondaryReport;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.iemr.common.data.report.CallQualityReport;
import com.iemr.common.service.reportSecondary.SecondaryReportService;
import com.iemr.common.utils.mapper.InputMapper;

import io.lettuce.core.dynamic.annotation.Param;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;


@RequestMapping({ "/crmReports" })
@RestController
public class CustomerRelationshipSecondaryReports {
	private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	@Autowired
	private SecondaryReportService secondaryReportService;

	
	@Operation(summary = "Get quality report")
	@RequestMapping(value = "/getQualityReport", method = RequestMethod.POST, headers = "Authorization")
	public ResponseEntity<Object> getQualityReport(
			@Param(value = "\"{\\\"startDate\\\":\\\"Timestamp\\\",\\\"endDate\\\":\\\"Timestamp\\\","
					+ "\\\"providerServiceMapID\\\":\\\"Integer\\\",\\\"agentID\\\":\\\"Integer\\\","
					+ "\\\"roleName\\\":\\\"String\\\",\\\"reportTypeID\\\":\\\"Integer\\\",\\\"reportType\\\":\\\"String\\\"}\"") @RequestBody String jsonRequest)

	{
		String filename = getFileName(jsonRequest, "104QAReport");
		InputStreamResource file = null;
		try {
			file = new InputStreamResource(secondaryReportService.getQualityReport(jsonRequest, filename));
			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + (filename + ".xlsx"))
					.contentType(MediaType.parseMediaType("application/vnd.ms-excel")).body(file);
		} catch (Exception e) {
			logger.error("Report Name:" + filename + " Timestamp:" + System.currentTimeMillis() + " Error: "
					+ e.getMessage());
			if (e.getMessage().equalsIgnoreCase("No data found"))
				return ResponseEntity.status(500).body(e.getMessage());
			else
				return ResponseEntity.status(5000).body(e.getMessage());
		}
	}

	
	@Operation(summary = "Get complaint detail report")
	@RequestMapping(value = "/getComplaintDetailReport", method = RequestMethod.POST, headers = "Authorization")
	public ResponseEntity<Object> getComplaintDetailReport(
			@Param(value = "\"{\\\"startDate\\\":\\\"Timestamp\\\",\\\"endDate\\\":\\\"Timestamp\\\","
					+ "\\\"providerServiceMapID\\\":\\\"Integer\\\",\\\"agentID\\\":\\\"Integer\\\","
					+ "\\\"roleName\\\":\\\"String\\\",\\\"reportTypeID\\\":\\\"Integer\\\",\\\"reportType\\\":\\\"String\\\"}\"") @RequestBody String jsonRequest)

	{
		String filename = "Grievance_Details.xlsx";

		InputStreamResource file = null;
		try {
			file = new InputStreamResource(
					secondaryReportService.getComplaintDetailReport(jsonRequest, "Grievance_Details"));
			return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
					.contentType(MediaType.parseMediaType("application/vnd.ms-excel")).body(file);
		} catch (Exception e) {
			logger.error("Report Name:" + filename + " Timestamp:" + System.currentTimeMillis() + " Error: "
					+ e.getMessage());
			if (e.getMessage().equalsIgnoreCase("No data found"))
				return ResponseEntity.status(500).body(e.getMessage());
			else
				return ResponseEntity.status(5000).body(e.getMessage());
		}
	}

	
	@Operation(summary = "Get call summary report")
	@RequestMapping(value = "/getCallSummaryReport", method = RequestMethod.POST, headers = "Authorization")
	public ResponseEntity<Object> getCallSummaryReport(
			@Param(value = "\"{\\\"startDate\\\":\\\"Timestamp\\\",\\\"endDate\\\":\\\"Timestamp\\\","
					+ "\\\"providerServiceMapID\\\":\\\"Integer\\\",\\\"agentID\\\":\\\"Integer\\\",\\\"roleName\\\":\\\"String\\\","
					+ "\\\"callTypeID\\\":\\\"Integer\\\","
					+ "\\\"callTypeName\\\":\\\"String\\\"}\"") @RequestBody String jsonRequest)

	{
		String filename = getFileName(jsonRequest, "Call_Summary_Report");
		InputStreamResource file = null;

		try {
			file = new InputStreamResource(secondaryReportService.getCallSummaryReport(jsonRequest, filename));
			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + (filename + ".xlsx"))

					.contentType(MediaType.parseMediaType("application/vnd.ms-excel")).body(file);
		} catch (Exception e) {
			logger.error("Report Name:" + filename + " Timestamp:" + System.currentTimeMillis() + " Error: "
					+ e.getMessage());
			if (e.getMessage().equalsIgnoreCase("No data found"))
				return ResponseEntity.status(500).body(e.getMessage());
			else
				return ResponseEntity.status(5000).body(e.getMessage());
		}
	}

	
	@Operation(summary = "Get all by sexual orientation")
	@RequestMapping(value = "/getAllBySexualOrientation", method = RequestMethod.POST, headers = "Authorization")
	public ResponseEntity<Object> getAllBySexualOrientation(
			@Param(value = "\"{\\\"startTimestamp\\\":\\\"Timestamp\\\",\\\"endTimestamp\\\":\\\"Timestamp\\\","
					+ "\\\"state\\\":\\\"String\\\",\\\"district\\\":\\\"String\\\","
					+ "\\\"beneficiarySexualOrientation\\\":\\\"String\\\",\\\"providerServiceMapID\\\":\\\"Integer\\\" }\"") @RequestBody String jsonRequest) {
		String filename = getFileName(jsonRequest, "Sexual_Orientation_Report");
		InputStreamResource file = null;
		try {
			file = new InputStreamResource(
					secondaryReportService.getAllBySexualOrientationReport(jsonRequest, filename));
			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + (filename + ".xlsx"))

					.contentType(MediaType.parseMediaType("application/vnd.ms-excel")).body(file);
		} catch (Exception e) {
			logger.error("Report Name:" + filename + " Timestamp:" + System.currentTimeMillis() + " Error: "
					+ e.getMessage());
			if (e.getMessage().equalsIgnoreCase("No data found"))
				return ResponseEntity.status(500).body(e.getMessage());
			else
				return ResponseEntity.status(5000).body(e.getMessage());
		}

	}

	
	@Operation(summary = "Get district wise call report")
	@RequestMapping(value = "/getDistrictWiseCallReport", method = RequestMethod.POST, headers = "Authorization")
	public ResponseEntity<Object> getDistrictWiseCallReport(
			@Param(value = "\"{\\\"startDate\\\":\\\"Timestamp\\\",\\\"endDate\\\":\\\"Timestamp\\\","
					+ "\\\"providerServiceMapID\\\":\\\"Integer\\\",\\\"districtID\\\":\\\"Integer\\\",\\\"district\\\":\\\"String\\\",\\\"subdistrictID\\\":\\\"Integer\\\",\\\"villageID\\\":\\\"Integer\\\","
					+ "\\\"locationID\\\":\\\"Integer\\\",\\\"roleID\\\":\\\"Integer\\\"}\"") @RequestBody String jsonRequest) {
		String filename = getFileName(jsonRequest, "District_Wise_Call_Volume_Report");
		InputStreamResource file = null;

		try {
			file = new InputStreamResource(secondaryReportService.getDistrictWiseCallReport(jsonRequest, filename));
			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + (filename + ".xlsx"))

					.contentType(MediaType.parseMediaType("application/vnd.ms-excel")).body(file);
		} catch (Exception e) {
			logger.error("Report Name:" + filename + " Timestamp:" + System.currentTimeMillis() + " Error: "
					+ e.getMessage());
			if (e.getMessage().equalsIgnoreCase("No data found"))
				return ResponseEntity.status(500).body(e.getMessage());
			else
				return ResponseEntity.status(5000).body(e.getMessage());
		}
	}

	
	@Operation(summary = "Get unblocked user report")
	@RequestMapping(value = "/getUnblockedUserReport", method = RequestMethod.POST, headers = "Authorization")
	public ResponseEntity<Object> getUnblockedUserReport(
			@Param(value = "\"{\\\"blockStartDate\\\":\\\"Timestamp\\\",\\\"blockEndDate\\\":\\\"Timestamp\\\","
					+ "\\\"providerServiceMapID\\\":\\\"Integer\\\"}\"") @RequestBody String jsonRequest,
			HttpServletRequest httpRequest) {
		String filename = getFileName(jsonRequest, "Unblock_User_Report");
		InputStreamResource file = null;
		try {
			file = new InputStreamResource(secondaryReportService.getUnblockedUserReport(jsonRequest, filename));
			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + (filename + ".xlsx"))

					.contentType(MediaType.parseMediaType("application/vnd.ms-excel")).body(file);
		} catch (Exception e) {
			logger.error("Report Name:" + filename + " Timestamp:" + System.currentTimeMillis() + " Error: "
					+ e.getMessage());
			if (e.getMessage().equalsIgnoreCase("No data found"))
				return ResponseEntity.status(500).body(e.getMessage());
			else
				return ResponseEntity.status(5000).body(e.getMessage());
		}
	}

	
	@Operation(summary = "Get call quality report")
	@RequestMapping(value = "/getCallQualityReport", method = RequestMethod.POST, headers = "Authorization")
	public ResponseEntity<Object> getCallQualityReport(
			@Param(value = "\"{\\\"startDate\\\":\\\"Timestamp\\\",\\\"endDate\\\":\\\"Timestamp\\\","
					+ "\\\"providerServiceMapID\\\":\\\"Integer\\\",\\\"searchCriteria\\\":\\\"String\\\","
					+ "\\\"workingLocationID\\\":\\\"Integer\\\",\\\"filename\\\":\\\"String\\\"}\"") @RequestBody String jsonRequest,
			HttpServletRequest httpRequest) {
		String filename = null;
		InputStreamResource file = null;
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			CallQualityReport callQualityReport = objectMapper.readValue(jsonRequest, CallQualityReport.class);
			filename = getFileName(jsonRequest, callQualityReport.getFileName());
			file = new InputStreamResource(secondaryReportService.getCallQualityReport(callQualityReport, filename));
			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + (filename + ".xlsx"))

					.contentType(MediaType.parseMediaType("application/vnd.ms-excel")).body(file);
		} catch (Exception e) {
			logger.error("Report Name:" + filename + " Timestamp:" + System.currentTimeMillis() + " Error: "
					+ e.getMessage());
			if (e.getMessage().equalsIgnoreCase("No data found"))
				return ResponseEntity.status(500).body(e.getMessage());
			else
				return ResponseEntity.status(5000).body(e.getMessage());
		}
	}

	
	@Operation(summary = "Get counts by preferred language")
	@RequestMapping(value = "/getCountsByPreferredLanguage", method = RequestMethod.POST, headers = "Authorization")
	public ResponseEntity<Object> getCountsByPreferredLanguage(
			@Param(value = "\"{\\\"startTimestamp\\\":\\\"Timestamp\\\",\\\"endTimestamp\\\":\\\"Timestamp\\\","
					+ "\\\"beneficiaryPreferredLanguage\\\":\\\"String\\\",\\\"providerServiceMapID\\\":\\\"Integer\\\","
					+ "\\\"state\\\":\\\"String\\\",\\\"district\\\":\\\"String\\\"}\"") @RequestBody String jsonRequest,
			HttpServletRequest httpRequest) {
		String filename = null;
		InputStreamResource file = null;
		try {

			filename = getFileName(jsonRequest, "Language_Distribution_Report");
			file = new InputStreamResource(secondaryReportService.getCountsByPrefferedLanguage(jsonRequest, filename));
			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + (filename + ".xlsx"))

					.contentType(MediaType.parseMediaType("application/vnd.ms-excel")).body(file);
		} catch (Exception e) {
			logger.error("Report Name:" + filename + " Timestamp:" + System.currentTimeMillis() + " Error: "
					+ e.getMessage());
			if (e.getMessage().equalsIgnoreCase("No data found"))
				return ResponseEntity.status(500).body(e.getMessage());
			else
				return ResponseEntity.status(5000).body(e.getMessage());
		}
	}

	
	@Operation(summary = "Get caller by age group")
	@RequestMapping(value = "/getAllByAgeGroup", method = RequestMethod.POST, headers = "Authorization")
	public ResponseEntity<Object> getAllByAgeGroup(
			@Param(value = "\"{\\\"providerServiceMapID\\\":\\\"Integer\\\",\\\"maxAge\\\":\\\"Integer\\\",\\\"minAge\\\":\\\"Integer\\\",\\\"startTimestamp\\\":\\\"Timestamp\\\","
					+ "\\\"endTimestamp\\\":\\\"Timestamp\\\",\\\"state\\\":\\\"String\\\",\\\"district\\\":\\\"String\\\"}\"") @RequestBody String jsonRequest,
			HttpServletRequest httpRequest) {

		String filename = getFileName(jsonRequest, "Caller_Age_Group_Report");
		InputStreamResource file = null;
		try {
			file = new InputStreamResource(secondaryReportService.getAllByAgeGroup(jsonRequest, filename));
			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + (filename + ".xlsx"))

					.contentType(MediaType.parseMediaType("application/vnd.ms-excel")).body(file);
		} catch (Exception e) {
			logger.error("Report Name:" + filename + " Timestamp:" + System.currentTimeMillis() + " Error: "
					+ e.getMessage());
			if (e.getMessage().equalsIgnoreCase("No data found"))
				return ResponseEntity.status(500).body(e.getMessage());
			else
				return ResponseEntity.status(5000).body(e.getMessage());
		}
	}

	
	@Operation(summary = "Get all reports by date")
	@RequestMapping(value = "/getAllReportsByDate", method = RequestMethod.POST, headers = "Authorization")
	public ResponseEntity<Object> getAllReportsByDate(
			@Param(value = "\"{\\\"providerServiceMapID\\\":\\\"Integer\\\","
					+ "\\\"beneficiaryCallType\\\":\\\"String\\\",\\\"beneficiaryCallSubType\\\":\\\"String\\\","
					+ "\\\"startTimestamp\\\":\\\"Timestamp\\\",\\\"endTimestamp\\\":\\\"Timestamp\\\","
					+ "\\\"state\\\":\\\"String\\\",\\\"district\\\":\\\"String\\\",\\\"gender\\\":\\\"String\\\","
					+ "\\\"beneficiaryPreferredLanguage\\\":\\\"String\\\",\\\"beneficiarySexualOrientation\\\":\\\"String\\\"}\"") @RequestBody String jsonRequest) {
		String filename = getFileName(jsonRequest, "Call_Type_Report");
		InputStreamResource file = null;

		try {
			file = new InputStreamResource(secondaryReportService.getAllReportsByDate(jsonRequest, filename));
			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + (filename + ".xlsx"))

					.contentType(MediaType.parseMediaType("application/vnd.ms-excel")).body(file);
		} catch (Exception e) {
			logger.error("Report Name:" + filename + " Timestamp:" + System.currentTimeMillis() + " Error: "
					+ e.getMessage());
			if (e.getMessage().equalsIgnoreCase("No data found"))
				return ResponseEntity.status(500).body(e.getMessage());
			else
				return ResponseEntity.status(5000).body(e.getMessage());
		}
	}

	
	@Operation(summary = "Get all by gender")
	@RequestMapping(value = "/getAllByGender", method = RequestMethod.POST, headers = "Authorization")
	public ResponseEntity<Object> getAllByGender(
			@Param(value = "\"{\\\"startTimestamp\\\":\\\"Timestamp\\\",\\\"endTimestamp\\\":\\\"Timestamp\\\","
					+ "\\\"gender\\\":\\\"String\\\",\\\"providerServiceMapID\\\":\\\"Integer\\\","
					+ "\\\"state\\\":\\\"String\\\",\\\"district\\\":\\\"String\\\"}\"") @RequestBody String jsonRequest) {
		String filename = getFileName(jsonRequest, "Gender_Distribution_Report");
		InputStreamResource file = null;

		try {
			file = new InputStreamResource(secondaryReportService.getAllByGender(jsonRequest, filename));
			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + (filename + ".xlsx"))

					.contentType(MediaType.parseMediaType("application/vnd.ms-excel")).body(file);

		} catch (Exception e)

		{
			logger.error("Report Name:" + filename + " Timestamp:" + System.currentTimeMillis() + " Error: "
					+ e.getMessage());
			if (e.getMessage().equalsIgnoreCase("No data found"))
				return ResponseEntity.status(500).body(e.getMessage());
			else
				return ResponseEntity.status(5000).body(e.getMessage());
		}
	}

	public String getFileName(String jsonRequest, String name) {
		String fileName = null;
		JsonObject jsnOBJ = new JsonObject();
		JsonParser jsnParser = new JsonParser();
		JsonElement jsnElmnt = jsnParser.parse(jsonRequest);
		jsnOBJ = jsnElmnt.getAsJsonObject();
		if (jsnOBJ != null && jsnOBJ.has("fileName")) {
			fileName = jsnOBJ.get("fileName").getAsString();
		}
		if (fileName != null)
			return fileName;
		else
			return name;
	}
}
