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
package com.iemr.common.controller.everwell.callhandle;

import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.iemr.common.service.everwell.EverwellCallHandlingService;
import com.iemr.common.utils.mapper.InputMapper;
import com.iemr.common.utils.response.OutputResponse;

import io.lettuce.core.dynamic.annotation.Param;
import io.swagger.v3.oas.annotations.Operation;


@RequestMapping(value = "/everwellCall")
@RestController
public class EverwellCallController {
	@Autowired
	private EverwellCallHandlingService beneficiaryCallService;
	InputMapper inputMapper = new InputMapper();
	final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	
	@Operation(summary = "Outbound call count")
	@RequestMapping(value = "/outboundCallCount", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String outboundCallCount(@Param(value = "{\"providerServiceMapID\":\"called service ID integer\", "
			+ "\"assignedUserID\":\"Optional - Integer user id to whom calls are assigned\"}") @RequestBody String request) {
		OutputResponse response = new OutputResponse();
		try {
			logger.info("outboundCallCount request " + request);
			response.setResponse(beneficiaryCallService.outboundCallCount(request));
		} catch (Exception e) {
			logger.error("outboundCallList failed with error " + e.getMessage(), e);
			response.setError(e);
		}

		return response.toString();
	}

	
	@Operation(summary = "Outbound allocation")
	@RequestMapping(value = "/outboundAllocation", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String outboundAllocation(@Param(value = "{\"AgentID\":[Integer Array list of Agent IDs], "
			+ "\"allocateNo\":\"Integer - number of calls to be allocated for user\", "
			+ "\"outboundCallRequests\":\"Array list of outbound calls\"}") @RequestBody String request) {
		OutputResponse response = new OutputResponse();
		try {
			response.setResponse(beneficiaryCallService.outboundAllocation(request));
		} catch (Exception e) {
			logger.error("outboundAllocation failed with error " + e.getMessage(), e);
			response.setError(e);
		}

		return response.toString();
	}

	
	@Operation(summary = "Outbound call list")
	@RequestMapping(value = "/outboundCallList", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String outboundCallList(@Param(value = "{\"providerServiceMapID\":\" called service ID integer\", "
			+ "\"AgentID\":\"Optional - Integer ID of agent that is assigned to\"}") @RequestBody String request) {
		OutputResponse response = new OutputResponse();
		// String auth = httpRequest.getHeader("authorization");
		try {
			response.setResponse(beneficiaryCallService.outboundCallList(request));
		} catch (Exception e) {
			logger.error("outboundCallList failed with error " + e.getMessage(), e);
			response.setError(e);
		}

		return response.toString();
	}

	
	@Operation(summary = "Reset outbound call")
	@RequestMapping(value = "/resetOutboundCall", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String resetOutboundCall(
			@Param(value = "{\"EAPIIDs\":\"[Integer - Array of Outbound call ids]\"}") @RequestBody String request) {
		OutputResponse response = new OutputResponse();
		try {
			response.setResponse(beneficiaryCallService.resetOutboundCall(request));
		} catch (Exception e) {
			logger.error("outboundAllocation failed with error " + e.getMessage(), e);
			response.setError(e);
		}

		return response.toString();
	}

	
	@Operation(summary = "Save feedback")
	@RequestMapping(value = "/saveFeedback", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String saveCallDetails(
			@Param(value = "{\"EAPIIDs\":\"[Integer - Array of Outbound call ids]\"}") @RequestBody String request) {
		OutputResponse response = new OutputResponse();
		try {
			response.setResponse(beneficiaryCallService.saveDetails(request));
		} catch (Exception e) {
			logger.error("outboundAllocation failed with error " + e.getMessage(), e);
			response.setError(e);
		}

		return response.toString();
	}

	
	@Operation(summary = "Complete outbound call")
	@RequestMapping(value = "/completeOutboundCall", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String completeOutboundCall(@Param(value = "{\"EAPIID\":\"Integer - Outbound call id\", "
			+ "\"isCompleted\":\"Boolean - Value indicating call is completed/pending\"}") @RequestBody String request) {
		OutputResponse response = new OutputResponse();
		try {
			String s = beneficiaryCallService.completeOutboundCall(request);
			if (s.equalsIgnoreCase("success"))
				response.setResponse(s);
			else
				response.setError(5000, "error in updating data");
		} catch (Exception e) {
			logger.error("outboundAllocation failed with error " + e.getMessage(), e);
			response.setError(e);
		}
		return response.toString();
	}

	
	@Operation(summary = "Get everwell feedback details")
	@RequestMapping(value = "/getEverwellfeedbackDetails", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String getEverwellfeedbackDetails(
			@Param(value = "{\"EverwellID\":\"Integer - Everwell id\"}") @RequestBody String request) {
		OutputResponse response = new OutputResponse();
		try {
			String s = beneficiaryCallService.getEverwellFeedback(request);
			if (s != null)
				response.setResponse(s);
			else
				response.setError(5000, "error in fetching data");
		} catch (Exception e) {
			logger.error("outboundAllocation failed with error " + e.getMessage(), e);
			response.setError(e);
		}
		return response.toString();
	}

	
	@Operation(summary = "Outbound call list with mobile number")
	@RequestMapping(value = "/outboundCallListWithMobileNumber", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String outboundCallListWithMobileNumber(
			@Param(value = "{\"PrimaryNumber\":\"PrimaryNumber\", \"providerServiceMapID\":\"called service ID integer\"}") @RequestBody String request) {
		OutputResponse response = new OutputResponse();
		try {
			response.setResponse(beneficiaryCallService.outboundCallListWithMobileNumber(request));
		} catch (Exception e) {
			logger.error("outboundCallList failed with error " + e.getMessage());
			response.setError(e);
		}
		return response.toString();
	}

	
	@Operation(summary = "Check if already called")
	@RequestMapping(value = "/checkIfAlreadyCalled", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String checkIfCalledOrNot(@Param(value = "{\"providerServiceMapID\":\" called service ID integer\", "
			+ "\"eapiId\":\" Integer\"}") @RequestBody String request) {
		OutputResponse response = new OutputResponse();
		try {
			response.setResponse(beneficiaryCallService.checkAlreadyCalled(request));
		} catch (Exception e) {
			logger.error("checkIfAlreadyCalled failed with error " + e.getMessage(), e);
			response.setError(e);
		}
		return response.toString();
	}
}
