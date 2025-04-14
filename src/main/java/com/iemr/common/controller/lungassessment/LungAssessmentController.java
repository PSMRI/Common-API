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
package com.iemr.common.controller.lungassessment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.iemr.common.service.lungassessment.LungAssessmentService;
import com.iemr.common.utils.response.OutputResponse;

import io.swagger.v3.oas.annotations.Operation;


@RequestMapping(value = "/lungAssessment")
@RestController
public class LungAssessmentController {

	@Autowired
	private LungAssessmentService lungAssessmentService;

	private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	
	@Operation(summary = "Start assesment")
	@RequestMapping(value = "/startAssesment", method = RequestMethod.POST, headers = "Authorization")
	public String startAssesment(@RequestParam("file") MultipartFile file, @RequestParam("request") String request) {
		OutputResponse output = new OutputResponse();
		try {

			String res = lungAssessmentService.initiateAssesment(request, file);
			output.setResponse(res);

			logger.info("start assessment cough response: " + output);
		} catch (Exception e) {
			logger.error("Lung assessment failed with error " + e.getMessage(), e);
			output.setError(e);
		}
		return output.toString();
	}

	
	@Operation(summary = "Get assesment")
	@RequestMapping(value = "/getAssesment/{assessmentId}", method = RequestMethod.GET, headers = "Authorization")
	public String getAssessment(@PathVariable("assessmentId") String assessmentId) {
		OutputResponse output = new OutputResponse();
		try {

			String res = lungAssessmentService.getAssesment(assessmentId);
			output.setResponse(res);

			logger.info("get assessment cough response: " + output);
		} catch (Exception e) {
			logger.error("Lung assessment failed with error " + e.getMessage());
			output.setError(e);
		}
		return output.toString();
	}

	
	@Operation(summary = "Get assesment details")
	@RequestMapping(value = "/getAssesmentDetails/{patientId}", method = RequestMethod.GET, headers = "Authorization")
	public String getAssessmentDetails(@PathVariable("patientId") Long patientId) {
		OutputResponse output = new OutputResponse();
		try {

			String res = lungAssessmentService.getAssessmentDetails(patientId);
			output.setResponse(res);

			logger.info("get assessment details response: " + output);
		} catch (Exception e) {
			logger.error("get assessment details failed with error " + e.getMessage());
			output.setError(e);
		}
		return output.toString();
	}

}
