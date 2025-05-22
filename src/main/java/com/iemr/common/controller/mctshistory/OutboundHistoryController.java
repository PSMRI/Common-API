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
package com.iemr.common.controller.mctshistory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.iemr.common.service.mctshistory.OutboundHistoryService;
import com.iemr.common.utils.response.OutputResponse;
import io.lettuce.core.dynamic.annotation.Param;
import io.swagger.v3.oas.annotations.Operation;



@RestController
@RequestMapping(value = "/mctsOutboundHistoryController")
public class OutboundHistoryController {

	private OutboundHistoryService outboundHistoryService;

	@Autowired
	public void setOutboundHistoryService(OutboundHistoryService outboundHistoryService) {
		this.outboundHistoryService = outboundHistoryService;
	}

	
	@Operation(summary = "Get MCTS call history")
	@RequestMapping(value = "/getMctsCallHistory", method = RequestMethod.POST, headers = "Authorization")
	public String getCallHistory(@Param("{\"beneficiaryRegID\":\"Long\"}") @RequestBody String request) {

		OutputResponse response = new OutputResponse();
		try {

			response.setResponse(outboundHistoryService.getCallHistory(request));
		} catch (Exception e) {

			response.setError(e);
		}
		return response.toStringWithSerialization();
	}

	
	@Operation(summary = "Get MCTS call response")
	@RequestMapping(value = "/getMctsCallResponse", method = RequestMethod.POST, headers = "Authorization")
	public String getMctsCallResponse(@Param("{\"callDetailID\":\"Long\"}") @RequestBody String request) {

		OutputResponse response = new OutputResponse();
		try {

			response.setResponse(outboundHistoryService.getMctsCallResponse(request));
		} catch (Exception e) {

			response.setError(e);
		}
		return response.toString();
	}

}
