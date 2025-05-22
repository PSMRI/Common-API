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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.iemr.common.service.questionconfig.QuestionTypeService;
import com.iemr.common.utils.response.OutputResponse;
import io.lettuce.core.dynamic.annotation.Param;
import io.swagger.v3.oas.annotations.Operation;



@RestController
@RequestMapping("questionTypeController")
public class QuestionTypeController {

	final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	/**
	 * Question Type Repository
	 */
	@Autowired
	private QuestionTypeService questionTypeService;

	/**
	 * Inject Question Type Repository
	 */
	public void setQuestionTypeService(QuestionTypeService questionTypeService) {

		this.questionTypeService = questionTypeService;
	}

	@CrossOrigin()
	@Operation(summary = "Create question type")
	@RequestMapping(value = "/put/questionType", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String createQuestionType(
			@Param(value = "{\\\"questionType\\\":\\\"String\\\",\\\"questionTypeDesc\\\":\\\"String\\\"}\"") @RequestBody String request) {
		OutputResponse response = new OutputResponse();
		try {
			response.setResponse(questionTypeService.createQuestionType(request));
		} catch (Exception e) {
			logger.error("put-questionTyp failed with error " + e.getMessage(), e);
			response.setError(e);
		}

		return response.toString();
	}

	@CrossOrigin()
	@Operation(summary = "Get question type list")
	@RequestMapping(value = "/get/questionTypeList", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON)
	public String questionTypeList() {
		OutputResponse response = new OutputResponse();
		try {
			response.setResponse(questionTypeService.getQuestionTypeList());
		} catch (Exception e) {
			logger.error("get-questionTyp failed with error " + e.getMessage(), e);
			response.setError(e);
		}

		return response.toString();
	}

}
