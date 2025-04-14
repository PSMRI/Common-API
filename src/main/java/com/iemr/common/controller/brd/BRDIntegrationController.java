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
package com.iemr.common.controller.brd;

import javax.ws.rs.core.MediaType;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.iemr.common.service.brd.BRDIntegrationService;
import com.iemr.common.utils.response.OutputResponse;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/brd")
public class BRDIntegrationController {

	private Logger logger = LoggerFactory.getLogger(BRDIntegrationController.class);

	@Autowired
	private BRDIntegrationService integrationService;

	@Operation(summary = "Get integration data")
	@RequestMapping(value = "/getIntegrationData", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String getDetails(@RequestBody String request) {
		OutputResponse response = new OutputResponse();
		String brdDetails = null;
		try {
			JSONObject json = new JSONObject(request);
			String startDate = json.getString("startDate");
			String endDate = json.getString("endDate");
			brdDetails = integrationService.getData(startDate, endDate);
			response.setResponse(brdDetails);
		} catch (Exception e) {
			logger.error("Error while getching BRD Integration data :" + e);
			response.setError(5000, "Unable to get BRD data");
		}
		return response.toStringWithSerializeNulls();
	}

}
