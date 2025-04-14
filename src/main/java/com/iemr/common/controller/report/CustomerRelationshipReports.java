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
package com.iemr.common.controller.report;

import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.iemr.common.mapper.Report1097Mapper;
import com.iemr.common.service.reports.CallReportsService;
import com.iemr.common.utils.mapper.InputMapper;
import com.iemr.common.utils.response.OutputResponse;

import io.swagger.v3.oas.annotations.Operation;


@CrossOrigin
@RequestMapping({ "/crmReports" })
@RestController
public class CustomerRelationshipReports {

	private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	InputMapper inputMapper = new InputMapper();

	private CallReportsService callReportsService;

	@Autowired
	public void setCallReportsService(CallReportsService callReportsService) {
		this.callReportsService = callReportsService;
	}

	@Autowired
	Report1097Mapper mapper;

	
	@Operation(summary = "QA report type master data")
	@RequestMapping(value = "/getReportTypes/{providerServiceMapID}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON)
	public String patientAppChiefComplaintsMasterData(
			@PathVariable("providerServiceMapID") Integer providerServiceMapID) throws Exception {
		logger.info("QA report type master Data for " + " providerServiceMapID:" + providerServiceMapID);
		;
		OutputResponse response = new OutputResponse();
		response.setResponse(callReportsService.getReportTypes(providerServiceMapID));
		logger.info("Nurse master Data for categoryID:" + response.toString());
		return response.toString();
	}
}
