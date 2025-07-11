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
package com.iemr.common.controller.sms;

import java.util.Arrays;

import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iemr.common.model.sms.CreateSMSRequest;
import com.iemr.common.model.sms.SMSParameterModel;
import com.iemr.common.model.sms.SMSRequest;
import com.iemr.common.model.sms.SMSTypeModel;
import com.iemr.common.model.sms.UpdateSMSRequest;
import com.iemr.common.service.sms.SMSService;
import com.iemr.common.utils.mapper.InputMapper;
import com.iemr.common.utils.mapper.OutputMapper;
import com.iemr.common.utils.response.OutputResponse;

import io.lettuce.core.dynamic.annotation.Param;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(value = "/sms")
public class SMSController {
	final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	@Autowired
	SMSService smsService;

	InputMapper inputMapper = new InputMapper();

	@Operation(summary = "Get SMS templates")
	@PostMapping(value = "/getSMSTemplates", produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String getSMSTemplates(
			@Param(value = "\"{\\\"providerServiceMapID\\\":\\\"Integer\\\"}\"") @RequestBody SMSRequest request,
			HttpServletRequest serverRequest) {
		OutputResponse response = new OutputResponse();
		logger.info("getSMSTemplates received request");
		logger.debug("getSMSTemplates received a request " + OutputMapper.gson().toJson(request));
		try {
			response.setResponse(smsService.getSMSTemplates(request));
		} catch (Exception e) {
			logger.error("getSMSTemplates failed with error " + e.getMessage(), e);
			response.setError(e);
		}
		logger.debug("getSMSTemplates sending response " + response);
		return response.toString();
	}

	@Operation(summary = "Get full SMS template")
	@PostMapping(value = "/getFullSMSTemplate", produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String getFullSMSTemplate(
			@Param(value = "\"{\\\"providerServiceMapID\\\":\\\"Integer\\\",\\\"smsTemplateID\\\":\\\"Integer\\\"}\"") @RequestBody SMSRequest request,
			HttpServletRequest serverRequest) {
		OutputResponse response = new OutputResponse();
		logger.info("getFullSMSTemplate received request");
		logger.debug("getFullSMSTemplate received a request " + OutputMapper.gson().toJson(request));
		try {
			response.setResponse(smsService.getFullSMSTemplate(request));
		} catch (Exception e) {
			logger.error("getFullSMSTemplate failed with error " + e.getMessage(), e);
			response.setError(e);
		}
		logger.debug("getFullSMSTemplate sending response " + response);
		return response.toString();
	}

	@Operation(summary = "Save SMS template")
	@PostMapping(value = "/saveSMSTemplate", produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String saveSMSTemplate(
			@Param(value = "\"{\\\"createdBy\\\":\\\"String\\\",\\\"providerServiceMapID\\\":\\\"String\\\",\\\"smsParameterMaps\\\":\\\"String\\\",\\\"smsTemplate\\\":\\\"String\\\",\\\"smsTemplateName\\\":\\\"String\\\",\\\"smsTypeID\\\":\\\"Integer\\\"}\"") @RequestBody CreateSMSRequest request,
			HttpServletRequest serverRequest) {
		OutputResponse response = new OutputResponse();
		logger.info("saveSMSTemplate received request");
		logger.debug("saveSMSTemplate received a request " + OutputMapper.gson().toJson(request));
		try {
			response.setResponse(smsService.saveSMSTemplate(request));
		} catch (Exception e) {
			logger.error("saveSMSTemplate failed with error " + e.getMessage(), e);
			response.setError(e);
		}
		logger.debug("saveSMSTemplate sending response " + response);
		return response.toString();
	}

	@Operation(summary = "Update SMS template")
	@PostMapping(value = "/updateSMSTemplate", produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String updateSMSTemplate(
			@Param(value = "\"{\\\"smsTemplateID\\\":\\\"String\\\",\\\"smsTemplateName\\\":\\\"String\\\",\\\"smsTemplate\\\":\\\"String\\\","
					+ "\\\"providerServiceMapID\\\":\\\"Integer\\\",\\\"smsType\\\":{\\\"smsTypeID\\\":\\\"Integer\\\",\\\"smsType\\\":\\\"String\\\","
					+ "\\\"description\\\":\\\"String\\\",\\\"serviceID\\\":\\\"Integer\\\",\\\"deleted\\\":\\\"Boolean\\\"},"
					+ "\\\"deleted\\\":\\\"Boolean\\\",\\\"createdBy\\\":\\\"String\\\","
					+ "\\\"createdDate\\\":\\\"Timestamp\\\",\\\"lastModDate\\\":\\\"Timestamp\\\",\\\"modifiedBy\\\":\\\"String\\\"}\"") @RequestBody UpdateSMSRequest request,
			HttpServletRequest serverRequest) {
		OutputResponse response = new OutputResponse();
		logger.info("updateSMSTemplate received request");
		logger.debug("updateSMSTemplate received a request " + OutputMapper.gson().toJson(request));
		try {
			response.setResponse(smsService.updateSMSTemplate(request));
		} catch (Exception e) {
			logger.error("updateSMSTemplate failed with error " + e.getMessage(), e);
			response.setError(e);
		}
		logger.debug("updateSMSTemplate sending response " + response);
		logger.info("updateSMSTemplate sending response");
		return response.toString();
	}

	@Operation(summary = "Get SMS types")
	@PostMapping(value = "/getSMSTypes", produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String getSMSTypes(
			@Param(value = "\"{\\\"serviceID\\\":\\\"Integer\\\"}\"") @RequestBody SMSTypeModel request,
			HttpServletRequest serverRequest) {
		OutputResponse response = new OutputResponse();
		logger.info("getSMSTypes received request");
		logger.debug("getSMSTypes received a request " + OutputMapper.gson().toJson(request));
		try {
			response.setResponse(smsService.getSMSTypes(request));
		} catch (Exception e) {
			logger.error("getSMSTypes failed with error " + e.getMessage(), e);
			response.setError(e);
		}
		logger.debug("getSMSTypes sending response " + response);
		logger.info("getSMSTypes sending response");
		return response.toString();
	}

	@Operation(summary = "Get SMS parameters")
	@PostMapping(value = "/getSMSParameters", produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String getSMSParameters(
			@Param(value = "\"{\\\"serviceID\\\":\\\"Integer\\\"}\"") @RequestBody SMSParameterModel request,
			HttpServletRequest serverRequest) {
		OutputResponse response = new OutputResponse();
		logger.info("getSMSParameters received request");
		logger.debug("getSMSParameters received a request " + OutputMapper.gson().toJson(request));
		try {
			response.setResponse(smsService.getSMSParameters(request));
		} catch (Exception e) {
			logger.error("getSMSParameters failed with error " + e.getMessage(), e);
			response.setError(e);
		}
		logger.debug("getSMSParameters sending response " + response);
		logger.info("getSMSParameters sending response");
		return response.toString();
	}

	@Operation(summary = "Send SMS")
	@PostMapping(value = "/sendSMS", produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String sendSMS(
			@Param(value = "\"{\\\"providerServiceMapID\\\":\\\"Integer\\\",\\\"smsTemplateTypeID\\\":\\\"Integer\\\","
					+ "\\\"smsTemplateID\\\":\\\"Integer\\\",\\\"beneficiaryRegID\\\":\\\"Long\\\",\\\"stateID\\\":\\\"Integer\\\","
					+ "\\\"districtID\\\":\\\"Integer\\\",\\\"blockID\\\":\\\"Integer\\\",\\\"is1097\\\":\\\"Boolean\\\","
					+ "\\\"userID\\\":\\\"Long\\\",\\\"InstituteID\\\":\\\"Integer\\\",\\\"feedbackID\\\":\\\"Long\\\","
					+ "\\\"createdBy\\\":\\\"String\\\",\\\"alternateNo\\\":\\\"String\\\",\\\"smsText\\\":\\\"String\\\","
					+ "\\\"prescribedDrugID\\\":\\\"Long\\\",\\\"bloodReqID\\\":\\\"Long\\\",\\\"directoryServiceID\\\":\\\"Long\\\","
					+ "\\\"fSComplaintID\\\":\\\"Long\\\",\\\"requestID\\\":\\\"String\\\",\\\"outbreakComplaintID\\\":\\\"Long\\\","
					+ "\\\"obCallID\\\":\\\"Long\\\",\\\"isBloodBankSMS\\\":\\\"Boolean\\\",\\\"moAdvice\\\":\\\"String\\\","
					+ "\\\"organDonationID\\\":\\\"Long\\\",\\\"requestedBloodBankID\\\":\\\"Long\\\",\\\"requestedInstitutionID\\\":\\\"Long\\\","
					+ "\\\"specializationID\\\":\\\"Integer\\\",\\\"smsTypeTM\\\":\\\"String\\\",\\\"tcDate\\\":\\\"String\\\","
					+ "\\\"tcPreviousDate\\\":\\\"String\\\",\\\"presObj\\\":\\\"String\\\",\\\"diagnosis\\\":\\\"List<Object>\\\",\\\"benHistoryID\\\":\\\"Long\\\",\\\"imrDate\\\":\\\"String\\\","
					+ "\\\"imrID\\\":\\\"String\\\",\\\"nodalNumber\\\":\\\"String\\\",\\\"informerName\\\":\\\"String\\\"}\"") @RequestBody String request,
			HttpServletRequest serverRequest) throws JsonMappingException, JsonProcessingException {
		OutputResponse response = new OutputResponse();
		logger.info("sendSMS received request");
		logger.debug("sendSMS received a request " + OutputMapper.gson().toJson(request));
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		SMSRequest[] requests = objectMapper.readValue(request, SMSRequest[].class);
		try {
			response.setResponse(smsService.sendSMS(Arrays.asList(requests), serverRequest.getHeader("Authorization")));
		} catch (Exception e) {
			logger.error("sendSMS failed with error " + e.getMessage(), e);
			response.setError(e);
		}
		logger.debug("sendSMS sending response " + response);
		logger.info("sendSMS sending response");
		String respStr = response.toString();
		respStr = respStr.replace("\\u003d", "=")
                 .replace("\\u003c", "<")
                 .replace("\\u003e", ">")
                 .replace("\\u0026", "&");
		return respStr;

	}

}
