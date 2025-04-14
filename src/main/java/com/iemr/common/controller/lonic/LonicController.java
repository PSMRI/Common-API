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
package com.iemr.common.controller.lonic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.iemr.common.data.lonic.LonicDescription;
import com.iemr.common.service.lonic.LonicService;
import com.iemr.common.utils.mapper.InputMapper;
import com.iemr.common.utils.response.OutputResponse;

import io.lettuce.core.dynamic.annotation.Param;
import io.swagger.v3.oas.annotations.Operation;



@RequestMapping(value = "/lonic")
@RestController

public class LonicController {
	private Logger logger = LoggerFactory.getLogger(LonicController.class);

	private LonicService lonicService;

	@Autowired
	public void setLonicService(LonicService lonicService) {
		this.lonicService = lonicService;
	}


	@Operation(summary = "Get lonic record list")
	@RequestMapping(value = "/getlonicRecordList", method = RequestMethod.POST, headers = "Authorization", consumes = "application/json", produces = "application/json")
	public String getLonicRecordList(
			@Param("{\"term\":\"String\",\"pageNo\":\"Integer\"}") @RequestBody String request) {
		OutputResponse output = new OutputResponse();
		try {

			LonicDescription lonicDescription = InputMapper.gson().fromJson(request, LonicDescription.class);

			logger.info("getLonicRecord request " + lonicDescription.toString());

			String lonicList = lonicService.findLonicRecordList(lonicDescription);

			if (lonicList != null)
				output.setResponse(lonicList);
			else
				output.setResponse("No Records Found");

			logger.info("getLonicRecord response: " + output);
		} catch (Exception e) {
			logger.error("getLonicRecord failed with error " + e.getMessage(), e);
			output.setError(e);
		}
		return output.toString();
	}

}
