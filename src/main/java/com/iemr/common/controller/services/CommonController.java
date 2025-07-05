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
package com.iemr.common.controller.services;

import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.iemr.common.service.services.CommonService;
import com.iemr.common.service.services.Services;
import com.iemr.common.service.services.ServicesImpl;
import com.iemr.common.utils.mapper.InputMapper;
import com.iemr.common.utils.response.OutputResponse;

import io.lettuce.core.dynamic.annotation.Param;
import io.swagger.v3.oas.annotations.Operation;



@RestController
@RequestMapping(value = "/service")
public class CommonController {
	InputMapper inputMapper = new InputMapper();
	final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	@Operation(summary = "Get category")
	@RequestMapping(value = "/category", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	// public Iterable<CategoryDetails> getCategories(){
	public String getCategories(@Param("{\"providerServiceMapID\":\"Integer\", "
			+ "\"subServiceID\":\"subServiceID\"}") @RequestBody String request) {
		OutputResponse response = new OutputResponse();
		try {
			response.setResponse(commonService.getCategories(request).toString());
		} catch (Exception e) {
			response.setError(e);
		}
		return response.toString();
	}

	@Operation(summary = "Get sub categories")
	@RequestMapping(value = "/subcategory", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String getSubcategories(@Param("{\"categoryID\":\"Integer\"}") @RequestBody String request) {
		OutputResponse response = new OutputResponse();
		try {
			response.setResponse(commonService.getSubCategories(request).toString());
		} catch (Exception e) {
			logger.error("", e);
			response.setError(e);
		}
		return response.toString();
	}

	@Operation(summary = "Get sub category files")
	@RequestMapping(value = "/getSubCategoryFiles", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String getSubCategoryFiles(@Param("{\"categoryID\":\"Integer\", \"providerServiceMapID\":\"Integer\", "
			+ "\"subCategoryID\":\"Integer\"}") @RequestBody String request) {
		OutputResponse response = new OutputResponse();
		try {
			response.setResponse(commonService.getSubCategoryFiles(request).toString());
		} catch (Exception e) {
			logger.error("", e);
			response.setError(e);
		}
		return response.toString();
	}

	@Operation(summary = "Get sub category files with URL")
	@RequestMapping(value = "/getSubCategoryFilesWithURL", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String getSubCategoryFilesWithURL(
			@Param("{\"categoryID\":\"Integer\", \"providerServiceMapID\":\"Integer\", "
					+ "\"subCategoryID\":\"Integer\"}") @RequestBody String request) {
		OutputResponse response = new OutputResponse();
		try {
			response.setResponse(commonService.getSubCategoryFilesWithURL(request).toString());
		} catch (Exception e) {
			logger.error("", e);
			response.setError(e);
		}
		return response.toString();
	}

	@Operation(summary = "Category by id")
	@RequestMapping(value = "/categoryByID", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String getcategoriesById(
			@Param(value = "\"{\\\"subServiceID\\\":\\\"String\\\",\\\"providerServiceMapID\\\":\\\"String\\\"}\"") @RequestBody String request) {
		OutputResponse response = new OutputResponse();
		try {
			response.setResponse(commonService.getCategories(request).toString());
		} catch (

		Exception e) {
			logger.error("", e);
			response.setError(e);
		}
		return response.toString();
	}

	@Operation(summary = "Service types")
	@RequestMapping(value = "/servicetypes", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String getservicetypes(@Param("{\"providerServiceMapID\":\"Integer\"}") @RequestBody String request) {
		OutputResponse response = new OutputResponse();
		try {
			response.setResponse(commonService.getActiveServiceTypes(request).toString());
		} catch (Exception e) {
			logger.error("", e);
			response.setError(e);
		}
		return response.toString();
	}

	/**
	 * Designation Service
	 */
	private CommonService commonService;
	private Services services;

	@Autowired
	public void setCommonService(CommonService commonService) {
		this.commonService = commonService;
	}

	@Autowired
	public void setServices(ServicesImpl services) {
		this.services = services;
	}

	@Operation(summary = "Service list")
	@RequestMapping(value = "/serviceList", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String serviceList(@RequestBody String request) {
		OutputResponse response = new OutputResponse();
		logger.info("serviceList request: " + request);
		try {
			response.setResponse(services.servicesList().toString());
		} catch (Exception e) {
			logger.error("serviceList failed with error " + e.getMessage(), e);
			response.setError(e);
		}
		logger.info("serviceList response: " + response.toString());
		return response.toString();
	}
}
