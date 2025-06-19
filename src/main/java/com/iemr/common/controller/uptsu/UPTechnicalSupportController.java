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
package com.iemr.common.controller.uptsu;

import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.iemr.common.service.uptsu.UptsuService;
import com.iemr.common.utils.response.OutputResponse;

import io.swagger.v3.oas.annotations.Operation;


@RequestMapping({ "/uptsu" })
@RestController
public class UPTechnicalSupportController {

	@Autowired
	private UptsuService uptsuService;
	private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	@Operation(summary = "Get facility master")
	@RequestMapping(value = "/get/facilityMaster/{providerServiceMapID}/{blockName}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String getFacility(@PathVariable Integer providerServiceMapID, @PathVariable String blockName) {
		OutputResponse response = new OutputResponse();
		logger.info("getFacility request ");
		try {

			response.setResponse(uptsuService.getFacility(providerServiceMapID, blockName));
		} catch (Exception e) {
			logger.info("getFacility failed with error " + e.getMessage(), e);
			response.setError(e);
		}

		return response.toString();
	}

	@Operation(summary = "Save appointment details")
	@RequestMapping(value = "/save/appointment-details", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String saveAppointmentDetails(@RequestBody String request,
			@RequestHeader(value = "Authorization") String Authorization) {
		OutputResponse response = new OutputResponse();

		try {

			response.setResponse(uptsuService.saveAppointmentDetails(request, Authorization));
		} catch (Exception e) {

			response.setError(5000, e.getLocalizedMessage());
		}

		return response.toString();
	}

}
