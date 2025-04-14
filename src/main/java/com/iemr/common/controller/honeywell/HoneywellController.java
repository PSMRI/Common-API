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
package com.iemr.common.controller.honeywell;

import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.iemr.common.service.honeywell.HoneywellService;
import com.iemr.common.utils.mapper.InputMapper;
import com.iemr.common.utils.response.OutputResponse;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;


@RequestMapping({ "/honeywell" })
@RestController
public class HoneywellController {
	private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	InputMapper inputMapper = new InputMapper();
	@Autowired
	private HoneywellService honeywellService;

	
	@Operation(summary = "Get real time district wise call report")
	@RequestMapping(value = "/realtimeDistrictDistribution", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String getRealtimeDistrictWiseCallReport() {
		OutputResponse response = new OutputResponse();
		try {
			response.setResponse(honeywellService.getRealtimeDistrictWiseCallReport());
		} catch (Exception e) {
			logger.error("getDistrictWiseCallReport failed with error " + e.getMessage(), e);
			response.setError(e);
		}
		return response.toString();
	}

	
	@Operation(summary = "District wise call distribution")
	@RequestMapping(value = "/districtWiseCallDistribution", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String getDistrictWiseCallReport(@RequestBody String request, HttpServletRequest httpRequest) {
		OutputResponse response = new OutputResponse();
		logger.info("getDistrictWiseCallReport request " + request);
		try {
			response.setResponse(honeywellService.getDistrictWiseCallReport(request));
		} catch (Exception e) {
			logger.error("getDistrictWiseCallReport failed with error " + e.getMessage(), e);
			response.setError(e);
		}
		return response.toString();
	}

	
	@Operation(summary = "Urban and rural calls")
	@RequestMapping(value = "/urban_rural_calls", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String getUrbanRuralCallReport(@RequestBody String request, HttpServletRequest httpRequest) {
		OutputResponse response = new OutputResponse();
		logger.info("getUrbanRuralCallReport request " + request);
		try {
			response.setResponse(honeywellService.getUrbanRuralCallReport(request));
		} catch (Exception e) {
			logger.error("getUrbanRuralCallReport failed with error " + e.getMessage(), e);
			response.setError(e);
		}
		return response.toString();
	}

}
