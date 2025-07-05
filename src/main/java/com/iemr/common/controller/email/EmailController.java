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
package com.iemr.common.controller.email;

import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.iemr.common.service.email.EmailService;
import com.iemr.common.utils.mapper.InputMapper;
import com.iemr.common.utils.response.OutputResponse;

import io.lettuce.core.dynamic.annotation.Param;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;

@RequestMapping(value = "/emailController")
@RestController
public class EmailController {

	InputMapper inputMapper = new InputMapper();
	final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	private EmailService emailService;

	@Autowired
	public void setEmailService(EmailService emailService) {
		this.emailService = emailService;
	}

	@Operation(summary = "Send email")
	@PostMapping(value = "/SendEmail", headers = "Authorization")
	public String SendEmail(
			@Param("{\"FeedbackID\":\"Long\",\"emailID\":\"String\",\"is1097\":\"Boolean\"}") @RequestBody String request,
			HttpServletRequest serverRequest) {
		OutputResponse response = new OutputResponse();
		logger.info("SendEmail request " + request);
		try {
			response.setResponse(emailService.SendEmail(request, serverRequest.getHeader("Authorization")));
		} catch (Exception e) {
			logger.error("SendEmail failed with error " + e.getMessage(), e);
			response.setError(e);
		}
		logger.debug("SendEmail response " + response.toString());
		logger.info("SendEmail response " + response.getStatusCode());
		return response.toString();
	}

	@Operation(summary = "Get authority email id")
	@PostMapping(value = "/getAuthorityEmailID", produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String getAuthorityEmailID(@Param(value = "{districtID : Integer}") @RequestBody String severityRequest) {

		OutputResponse response = new OutputResponse();
		try {
			response.setResponse(emailService.getAuthorityEmailID(severityRequest));
		} catch (Exception e) {
			logger.error("", e);
			response.setError(e);
		}
		return response.toString();
	}

	@Operation(summary = "Send email general")
	@PostMapping(value = "/sendEmailGeneral", headers = "Authorization")
	public String sendEmailGeneral(
			@Param("{\"requestID\":\"String\",\"emailType\":\"String\",\"emailID\":\"String\"}") @RequestBody String requestID,
			HttpServletRequest serverRequest) {
		OutputResponse response = new OutputResponse();
		try {
			response.setResponse(emailService.sendEmailGeneral(requestID, serverRequest.getHeader("Authorization")));
		} catch (Exception e) {
			logger.error("SendEmail failed with error " + e.getMessage(), e);
			response.setError(e);
		}
		logger.debug("SendEmail response " + response.toString());
		logger.info("SendEmail response " + response.getStatusCode());
		return response.toString();
	}
}
