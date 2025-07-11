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
package com.iemr.common.controller.location;

import java.util.List;

import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.iemr.common.data.location.Country;
import com.iemr.common.data.location.DistrictBlock;
import com.iemr.common.data.location.DistrictBranchMapping;
import com.iemr.common.data.location.Districts;
import com.iemr.common.data.location.States;
import com.iemr.common.service.location.LocationService;
import com.iemr.common.utils.response.OutputResponse;

import io.swagger.v3.oas.annotations.Operation;


@RequestMapping({ "/location" })
@RestController
public class LocationController {
	private LocationService locationService;
	private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	@Operation(summary = "Get states")
	@RequestMapping(value = "/states/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String getStates(@PathVariable("id") Integer id) {
		OutputResponse response = new OutputResponse();
		logger.info("getStates request " + id);
		try {
			List<States> stateList = this.locationService.getStates(id);
			response.setResponse(stateList.toString());
		} catch (Exception e) {
			logger.info("getStates failed with error " + e.getMessage(), e);
			response.setError(e);
		}
		return response.toString();
	}

	@Operation(summary = "Get districts")
	@RequestMapping(value = "/districts/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String getDistricts(@PathVariable("id") int id) {
		OutputResponse response = new OutputResponse();
		logger.info("getDistricts request " + id);
		try {
			List<Districts> districtsList = this.locationService.getDistricts(id);
			response.setResponse(districtsList.toString());
		} catch (Exception e) {
			logger.info("getDistricts failed with error " + e.getMessage(), e);
			response.setError(e);
		}
		return response.toString();
	}

	@Operation(summary = "Fetch state and district by id")
	@RequestMapping(value = "/statesDistricts/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String getStatetDistricts(@PathVariable("id") int id) {
		OutputResponse response = new OutputResponse();
		logger.info("getStatetDistricts request " + id);
		try {
			List<Districts> districtsList = this.locationService.findStateDistrictBy(id);
			response.setResponse(districtsList.toString());
		} catch (Exception e) {
			logger.info("getStatetDistricts failed with error " + e.getMessage(), e);
			response.setError(e);
		}
		logger.info("getStatetDistricts response " + response.toString());
		return response.toString();
	}

	@Operation(summary = "Get district blocks")
	@RequestMapping(value = "/taluks/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String getDistrictBlocks(@PathVariable("id") int id) {
		OutputResponse response = new OutputResponse();
		logger.info("getDistrictBlocks request " + id);
		try {
			List<DistrictBlock> districtBlockList = this.locationService.getDistrictBlocks(id);
			response.setResponse(districtBlockList.toString());
		} catch (Exception e) {
			logger.info("getDistrictBlocks failed with error " + e.getMessage(), e);
			response.setError(e);
		}
		return response.toString();
	}

	@Operation(summary = "Get city")
	@RequestMapping(value = "/city/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String getCity(@PathVariable("id") int id) {
		OutputResponse response = new OutputResponse();
		logger.info("getCity request " + id);
		try {
			List<DistrictBlock> districtBlockList = this.locationService.getDistrictBlocks(id);
			response.setResponse(districtBlockList.toString());
		} catch (Exception e) {
			logger.info("getCity failed with error " + e.getMessage(), e);
			response.setError(e);
		}
		logger.info("getCity response " + response.toString());
		return response.toString();
	}

	@Operation(summary = "Get villages")
	@RequestMapping(value = "/village/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String getVillages(@PathVariable("id") int id) {
		OutputResponse response = new OutputResponse();
		logger.info("getVillages request " + id);
		try {
			List<DistrictBranchMapping> districtBlockList = this.locationService.getDistrilctBranchs(id);
			response.setResponse(districtBlockList.toString());
		} catch (Exception e) {
			logger.info("getVillages failed with error " + e.getMessage(), e);
			response.setError(e);
		}
		return response.toString();
	}

	@Autowired
	public void setLocationService(LocationService locationService) {
		this.locationService = locationService;
	}

	@Operation(summary = "Get countries")
	@RequestMapping(value = "/getCountries", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String getCountries() {
		OutputResponse response = new OutputResponse();
		logger.info("getCountries request ");
		try {
			List<Country> stateList = this.locationService.getCountries();
			response.setResponse(stateList.toString());
		} catch (Exception e) {
			logger.info("getCountries failed with error " + e.getMessage(), e);
			response.setError(e);
		}
		logger.info("getStates response " + response.toString());
		return response.toString();
	}
}
