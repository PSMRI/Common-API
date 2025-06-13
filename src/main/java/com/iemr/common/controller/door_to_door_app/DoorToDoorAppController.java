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
package com.iemr.common.controller.door_to_door_app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.iemr.common.data.door_to_door_app.RequestParser;
import com.iemr.common.service.door_to_door_app.DoorToDoorService;
import com.iemr.common.utils.mapper.InputMapper;
import com.iemr.common.utils.response.OutputResponse;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping(value = "/doortodoorapp", headers = "Authorization")
public class DoorToDoorAppController {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	@Autowired
	private DoorToDoorService doorToDoorService;

	@Operation(summary = "Get user details")
	@RequestMapping(value = { "/getUserDetails" }, method = { RequestMethod.POST }, consumes = "application/json", produces = "application/json")
	public String getUserDetails(@RequestBody String requestObj) {
		OutputResponse response = new OutputResponse();
		try {
			logger.info("Request object for getUserDetails" + requestObj);

			String s = doorToDoorService.getUserDetails(requestObj);
			if (s != null)
				response.setResponse(s);
			else
				response.setError(5000, "user details not found");
		} catch (Exception e) {
			logger.error(e.toString());
			response.setError(5000, "Unable to get user data, exception occured. " + e.toString());
		}

		return response.toString();
	}

	@Operation(summary = "Get NCD TB HRP suspected status")
	@PostMapping(value = { "/getSuspectedData_HRP_TB_NCD" }, consumes = "application/json", produces = "application/json")
	public String getSuspectedData_HRP_TB_NCD(@RequestBody String requestObj) {
		OutputResponse response = new OutputResponse();
		try {
			logger.info("Request object for getSuspectedData_HRP_TB_NCD" + requestObj);
			RequestParser rp = InputMapper.gson().fromJson(requestObj, RequestParser.class);
			String s = doorToDoorService.get_NCD_TB_HRP_Suspected_Status(rp);
			if (s != null)
				response.setResponse(s);
			else
				response.setError(5000, "Error in getting suspected information");
		} catch (Exception e) {
			logger.error(e.toString());
			response.setError(5000, "Error in getting suspected information, exception occured. " + e.toString());
		}

		return response.toString();
	}

}
