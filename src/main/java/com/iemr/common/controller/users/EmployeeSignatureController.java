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
package com.iemr.common.controller.users;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.iemr.common.data.users.EmployeeSignature;
import com.iemr.common.service.users.EmployeeSignatureServiceImpl;
import com.iemr.common.utils.response.OutputResponse;

import io.swagger.v3.oas.annotations.Operation;


@PropertySource("classpath:application.properties")

@RestController
@RequestMapping(value = "/signature1")
public class EmployeeSignatureController {

	@Autowired
	EmployeeSignatureServiceImpl employeeSignatureServiceImpl;

	private Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

	@Operation(summary = "Fetch file")
	@RequestMapping(value = "/{userID}", headers = "Authorization", method = { RequestMethod.GET })
	public ResponseEntity<byte[]> fetchFile(@PathVariable("userID") Long userID) throws Exception {
		logger.debug("File download for userID" + userID);

		try {
			EmployeeSignature userSignID = employeeSignatureServiceImpl.fetchSignature(userID);
			HttpHeaders responseHeaders = new HttpHeaders();
			String fileName = URLEncoder.encode(userSignID.getFileName(), StandardCharsets.UTF_8);
			responseHeaders.set(HttpHeaders.CONTENT_DISPOSITION,
					"attachment; filename=\"" + fileName + "\"; filename*=UTF-8''" + fileName);

			MediaType mediaType;
			try {
				mediaType = MediaType.parseMediaType(userSignID.getFileType());
			} catch (InvalidMediaTypeException | NullPointerException e) {
				mediaType = MediaType.APPLICATION_OCTET_STREAM;
			}

			byte[] fileBytes = userSignID.getSignature(); // MUST be byte[]

			return ResponseEntity.ok().headers(responseHeaders).contentType(mediaType).contentLength(fileBytes.length)
					.body(fileBytes);

		} catch (Exception e) {
			logger.error("File download for userID failed with exception " + e.getMessage(), e);
			throw new Exception("Error while downloading file. Please contact administrator..");
		}

	}

	@Operation(summary = "Fetch file from central")
	@RequestMapping(value = "/getSignClass/{userID}", headers = "Authorization", method = { RequestMethod.GET })
	public String fetchFileFromCentral(@PathVariable("userID") Long userID) throws Exception {
		OutputResponse response = new OutputResponse();
		logger.debug("File download for userID" + userID);

		try {

			EmployeeSignature userSignID = employeeSignatureServiceImpl.fetchSignature(userID);
			if (userSignID != null)
				response.setResponse(new Gson().toJson(userSignID));
			else
				response.setError(5000, "No record found");

		} catch (Exception e) {
			response.setError(5000, e.getMessage());
			logger.error("File download for userID failed with exception " + e.getMessage(), e);
		}
		return response.toString();
	}

	@Operation(summary = "Download file based on userID")
	@RequestMapping(value = "/signexist/{userID}", headers = "Authorization", method = { RequestMethod.GET })
	public String existFile(@PathVariable("userID") Long userID) throws Exception {
		OutputResponse response = new OutputResponse();
		logger.debug("File download for userID" + userID);

		try {

			Boolean userSignID = employeeSignatureServiceImpl.existSignature(userID);
			response.setResponse(userSignID.toString());

		} catch (Exception e) {
			logger.error("File download for userID failed with exception " + e.getMessage(), e);
			response.setError(e);
		}

		logger.debug("response" + response);
		return response.toString();
	}
}
