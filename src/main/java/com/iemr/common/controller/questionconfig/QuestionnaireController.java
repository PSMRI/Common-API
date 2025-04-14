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
package com.iemr.common.controller.questionconfig;

import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.iemr.common.service.questionconfig.QuestionnaireService;
import com.iemr.common.utils.response.OutputResponse;

import io.swagger.v3.oas.annotations.Operation;


@RestController
@RequestMapping("/questionnaireController")
public class QuestionnaireController {

	final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	/**
	 * Question Type Repository
	 */
	private QuestionnaireService questionnaireService;

	/**
	 * Inject Question Type Repository
	 */
	public void setQuestionnaireService(QuestionnaireService questionnaireService) {

		this.questionnaireService = questionnaireService;
	}

	
	@Operation(summary = "Create questionnaire")
	@RequestMapping(value = "/put/questionnaire", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String createQuestionnaire(@RequestBody String request) {
		OutputResponse response = new OutputResponse();
		try {
			response.setResponse(questionnaireService.createQuestionnaire(request));
		} catch (Exception e) {
			logger.error("put-questionnaire failed with error " + e.getMessage(), e);
			response.setError(e);
		}

		return response.toString();
	}

	
	@Operation(summary = "Get questionnaire list")
	@RequestMapping(value = "/get/questionnaireList", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String questionTypeList() {
		OutputResponse response = new OutputResponse();
		try {
			response.setResponse(questionnaireService.getQuestionnaireList());
		} catch (Exception e) {
			logger.error("get-questionnaire failed with error " + e.getMessage(), e);
			response.setError(e);
		}

		return response.toString();
	}
}
