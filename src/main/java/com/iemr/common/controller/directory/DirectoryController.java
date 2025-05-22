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
package com.iemr.common.controller.directory;

import java.util.List;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iemr.common.data.directory.Directory;
import com.iemr.common.data.directory.InstituteDirectoryMapping;
import com.iemr.common.data.directory.SubDirectory;
import com.iemr.common.service.directory.DirectoryMappingService;
import com.iemr.common.service.directory.DirectoryService;
import com.iemr.common.service.directory.SubDirectoryService;
import com.iemr.common.utils.response.OutputResponse;

import io.lettuce.core.dynamic.annotation.Param;
import io.swagger.v3.oas.annotations.Operation;



@RequestMapping(value = "/directory")
@RestController
public class DirectoryController
{
	private DirectoryService directoryService;

	@Autowired
	public void setDirectoryService(DirectoryService directoryService)
	{
		this.directoryService = directoryService;
	}

	private SubDirectoryService subDirectoryService;

	@Autowired
	public void setSubDirectoryService(SubDirectoryService subDirectoryService)
	{
		this.subDirectoryService = subDirectoryService;
	}

	private DirectoryMappingService directoryMappingService;

	@Autowired
	public void setDirectoryMappingService(DirectoryMappingService directoryMappingService)
	{
		this.directoryMappingService = directoryMappingService;
	}

	private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	
	@Operation(summary = "Get directory")
	@RequestMapping(value = "/getDirectory", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON,
			headers = "Authorization")
	public String getDirectory()
	{
		OutputResponse response = new OutputResponse();
		try
		{
			JSONObject responseObj = new JSONObject();
			responseObj.put("directory", directoryService.getDirectories());
			response.setResponse(responseObj.toString());
		} catch (Exception e)
		{
			logger.error("getDirectory failed with error " + e.getMessage(), e);
			response.setError(e);
		}
		logger.info("getDirectory response " + response.toString());
		return response.toString();
	}

	
	@Operation(summary = "Get directory V1")
	@RequestMapping(value = "/getDirectoryV1", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON,
			headers = "Authorization")
	public String getDirectoryV1(
			@Param(value = "{\"providerServiceMapID\":\"Integer\"}") @RequestBody String directoryRequest)
	{
		OutputResponse response = new OutputResponse();
		logger.info("getDirectory request " + directoryRequest);
		try
		{
			ObjectMapper objectMapper = new ObjectMapper();
			Directory directory = objectMapper.readValue(directoryRequest, Directory.class);
			response.setResponse("{\"directory\":"
					+ directoryService.getDirectories(directory.getProviderServiceMapID()).toString() + "}");
		} catch (Exception e)
		{
			logger.error("getDirectory failed with error " + e.getMessage(), e);
			response.setError(e);
		}
		logger.info("getDirectory response " + response.toString());
		return response.toString();
	}

	
	@Operation(summary = "This API is used for getting subdirectories for a given institute directory")
	@RequestMapping(value = "/getSubDirectory", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON,
			consumes = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String getSubDirectory(@Param("{\"instituteDirectoryID\":\"Integer\"}") @RequestBody String subDirectoryRequest)
	{
		OutputResponse response = new OutputResponse();
		logger.info("getSubDirectory request " + subDirectoryRequest);
		try
		{
			JSONObject responseObj = new JSONObject();
			ObjectMapper objectMapper = new ObjectMapper();
			SubDirectory subDirectoryQuery = objectMapper.readValue(subDirectoryRequest, SubDirectory.class);
			response.setResponse("{subDirectory:"
					+ subDirectoryService.getSubDirectories(subDirectoryQuery.getInstituteDirectoryID()) + "}");
		} catch (Exception e)
		{
			logger.error("getSubDirectory failed with error " + e.getMessage(), e);
			response.setError(e);
		}
		logger.info("getSubDirectory response " + response.toString());
		return response.toString();
	}

	
	@Operation(summary = "This API is used for getting institutes for a given directory, subdirectory and location")
	@RequestMapping(value = "/getInstitutesDirectories", method = RequestMethod.POST,
			produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String
			getInstitutesDirectories(@Param(value = "{\"instituteDirectoryID\":\"Integer - directory ID\", "
					+ "\"instituteSubDirectoryID\":\"Integer - sub directory ID\", "
					+ "\"stateID\":\"Integer - State ID\", \"districtID\":\"Integer - district ID\", "
					+ "\"blockID\":\"Optional - Integer - Block ID\"}") @RequestBody String request)
	{
		OutputResponse response = new OutputResponse();
		logger.info("getInstitutesDirectories request " + request);
		try
		{
			List<InstituteDirectoryMapping> instituteDirectoryMappings =
					directoryMappingService.findAciveInstituteDirectories(request);
			response.setResponse(instituteDirectoryMappings.toString());
		} catch (Exception e)
		{
			logger.error("getInstitutesDirectories failed with error " + e.getMessage(), e);
			response.setError(e);
		}
		logger.info("getInstitutesDirectories response " + response.toString());
		return response.toString();
	}
}
