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
package com.iemr.common.controller.scheme;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iemr.common.data.scheme.Scheme;
import com.iemr.common.data.users.UserServiceRoleMapping;
import com.iemr.common.service.scheme.SchemeServiceImpl;
import com.iemr.common.utils.mapper.InputMapper;
import com.iemr.common.utils.response.OutputResponse;

import io.lettuce.core.dynamic.annotation.Param;
import io.swagger.v3.oas.annotations.Operation;



@RequestMapping(value = "/beneficiary")
@RestController
public class SchemeController {
	InputMapper inputMapper = new InputMapper();
	private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	@Autowired
	private SchemeServiceImpl schemeServiceImpl;

	InputMapper mapper = new InputMapper();

	@Operation(summary = "Save scheme details")
	@RequestMapping(value = "/save/schemeDetails", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE, headers = "Authorization")
	public String saveSchemeDetails(@Param(value = "{\"providerServiceMapID\": \"integer-provider service map id\", "
			+ "\"schemeName\": \"Name of Scheme\", " + "\"schemeDesc\": \"Description of Scheme\", "
			+ "\"createdBy\": \"User name of the supervisor creating scheme\", "
			+ "\"kmFileManager\":{\"fileName\":\"String: name of file\", "
			+ "\"fileExtension\":\"String: file extension\", "
			+ "\"providerServiceMapID\":\"Integer: service provider map ID\", "
			+ "\"userID\":\"Integer: user ID of the supervisor\", "
			+ "\"fileContent\":\"String: base64encoded binary file content\", "
			+ "\"createdBy\":\"String: username of supervisor\"}}") @RequestBody String request) {
		OutputResponse output = new OutputResponse();
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			Scheme scheme = objectMapper.readValue(request, Scheme.class);
			Scheme schemeNew;
			schemeNew = schemeServiceImpl.save(scheme);
			output.setResponse(schemeNew.toString());
		} catch (Exception e) {
			logger.error("saveSchemeDetails failed with error " + e.getMessage(), e);
			output.setError(e);
		}
		logger.info("saveSchemeDetails response: " + output);
		return output.toString();
	}

	@Operation(summary = "Get scheme list")
	@RequestMapping(value = "/get/schemeList", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE, headers = "Authorization")
	public String getSchemeList(
			@Param(value = "{\"providerServiceMapID\":\"Integer\"}") @RequestBody String createRequest) {
		logger.info("getSchemeList request " + createRequest);
		OutputResponse output = new OutputResponse();
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			Scheme m_scheme = objectMapper.readValue(createRequest, Scheme.class);
			List<Scheme> schemes = null;
			schemes = schemeServiceImpl.getSchemeList(m_scheme.getProviderServiceMapID());
			if (schemes != null) {
				output.setResponse(schemes.toString());
			} else {
				output.setError(5000, "No schemes available");
			}

		} catch (Exception e) {
			logger.error("getSchemeList failed with error " + e.getMessage(), e);
			output.setError(e);
		}
		logger.info("getSchemeList response: " + output);
		return output.toString();
	}

	@Operation(summary = "Delete scheme")
	@RequestMapping(value = "/scheme/deleteScheme", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE, headers = "Authorization")
	public String deleteScheme(
			@Param(value = "{\"schemeID\":\"Integer\",\"deleted\":\"Boolean\"}") @RequestBody String createRequest) {
		logger.info("delete scheme request " + createRequest);
		OutputResponse output = new OutputResponse();
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			Scheme m_scheme = objectMapper.readValue(createRequest, Scheme.class);
			Scheme scheme = null;
			scheme = schemeServiceImpl.getSchemeByID(m_scheme.getSchemeID());
			if (scheme != null) {
				scheme.setDeleted(m_scheme.getDeleted());
				output.setResponse(schemeServiceImpl.deletedata(scheme));
			} else {
				output.setError(5000, "No schemes available");
			}

		} catch (Exception e) {
			logger.error("getSchemeList failed with error " + e.getMessage(), e);
			output.setError(e);
		}
		logger.info("getSchemeList response: " + output);
		return output.toString();
	}
}
