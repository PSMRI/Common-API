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
package com.iemr.common.controller.snomedct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.google.gson.Gson;
import com.iemr.common.data.snomedct.SCTDescription;
import com.iemr.common.service.snomedct.SnomedService;
import com.iemr.common.utils.mapper.InputMapper;
import com.iemr.common.utils.response.OutputResponse;
import io.lettuce.core.dynamic.annotation.Param;
import io.swagger.v3.oas.annotations.Operation;



@RequestMapping(value = "/snomed")
@RestController
public class SnomedController {
	private Logger logger = LoggerFactory.getLogger(SnomedController.class);

	private SnomedService snomedService;

	@Autowired
	public void setSnomedService(SnomedService snomedService) {
		this.snomedService = snomedService;
	}

	
	@Operation(summary = "Retrives snomed CT record")
	@RequestMapping(value = "/getSnomedCTRecord", method = RequestMethod.POST, headers = "Authorization", consumes = "application/json", produces = "application/json")
	public String getSnomedCTRecord(@Param(value = "{\"term\":\"String\"}") @RequestBody String request) {
		OutputResponse output = new OutputResponse();
		try {

			SCTDescription sctdescription = InputMapper.gson().fromJson(request, SCTDescription.class);

			logger.info("getSnomedCTRecord request " + sctdescription.toString());

			SCTDescription sctdescriptions = snomedService.findSnomedCTRecordFromTerm(sctdescription.getTerm());

			if (sctdescriptions == null || sctdescriptions.getConceptID() == null)
				output.setResponse("No Records Found");
			else
				output.setResponse(new Gson().toJson(sctdescriptions));

			logger.info("ggetSnomedCTRecord response: " + output);
		} catch (Exception e) {
			logger.error("ggetSnomedCTRecord failed with error " + e.getMessage(), e);
			output.setError(e);
		}
		return output.toString();
	}

	
	@Operation(summary = "Retrives snomed CT record list")
	@RequestMapping(value = "/getSnomedCTRecordList", method = RequestMethod.POST, headers = "Authorization", consumes = "application/json", produces = "application/json")
	public String getSnomedCTRecordList(@Param(value = "{\"term\":\"String\"}") @RequestBody String request) {
		OutputResponse output = new OutputResponse();
		try {

			SCTDescription sctdescription = InputMapper.gson().fromJson(request, SCTDescription.class);

			logger.info("getSnomedCTRecord request " + sctdescription.toString());

			String sctList = snomedService.findSnomedCTRecordList(sctdescription);

			if (sctList != null)
				output.setResponse(sctList);
			else
				output.setResponse("No Records Found");

			logger.info("ggetSnomedCTRecord response: " + output);
		} catch (Exception e) {
			logger.error("ggetSnomedCTRecord failed with error " + e.getMessage(), e);
			output.setError(e);
		}
		return output.toString();
	}

}
