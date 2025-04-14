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
package com.iemr.common.controller.everwellTest;

import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.iemr.common.model.user.LoginRequestModelEverwell;
import com.iemr.common.utils.mapper.InputMapper;
import com.iemr.common.utils.response.OutputResponse;

import io.swagger.v3.oas.annotations.Operation;


@RestController
@RequestMapping(value = "/everwell")
public class EverwellController {
	InputMapper inputMapper = new InputMapper();
	Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	
	@Operation(summary = "Get data")
	@RequestMapping(value = "/getjson", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String getdata() {
		OutputResponse response = new OutputResponse();
		try {
			String str = null;
			str = "{   \"Success\": true,\"TotalRecords\": 3,\"currentPage\": 1,\"Data\": [{ \r\n"
					+ "					        \"Id\": 1232, \r\n"
					+ "							\"FirstName\": \"Test\", \r\n"
					+ "					        \"LastName\": \"tester\", \r\n"
					+ "							\"Gender\":\"Female\",\r\n"
					+ "					        \"PrimaryNumber\": \"1111111111\", \r\n"
					+ "					        \"MissedDoses\":2, \r\n"
					+ "					        \"FacilityName\": \"Facility X\", \r\n"
					+ "					        \"State\": \"Uttar Pradesh\", \r\n"
					+ "							\"District\": \"Varanasi\", \r\n"
					+ "					        \"AdherencePercentage\": 50 \r\n" + "					      }]\r\n"
					+ "						  }";

			String str1 = "\"Success\":true";
			response.setResponse(str);
		} catch (Exception e) {
			logger.error("", e);
			response.setError(e);
		}
		return response.toString();
	}

	
	@Operation(summary = "Add support action")
	@RequestMapping(value = {
			"/addSupportAction/{id}" }, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String addSupportAction(@PathVariable("id") Long id, @RequestBody String request) {
		OutputResponse response = new OutputResponse();
		try {

			String str = "{   \"Id\": 123456789," + "\"UserId\": 123," + "\"UserDescription\": \"Demo Login\",\r\n"
					+ "\"ActionTaken\": \"Call\",\r\n" + " \"Comments\": \"Well, This is a Sample Comment\",\r\n"
					+ "\"Category\": \"Support_Action_Call\",\r\n" + "\"SubCategory\": \"Dose not taken\",\r\n"
					+ "\"Timestamp\": \"2020-04-02T04:18:43.4373395\",\r\n"
					+ "\"DateOfAction\": \"2020-03-02T00:00:00\" } ";
			response.setResponse(str);
		} catch (Exception e) {
			logger.error("", e);
			response.setError(e);
		}
		return response.toString();
	}

	
	@Operation(summary = "Edit manual doses")
	@RequestMapping(value = {
			"/editManualDoses/{id}" }, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String editManualDoses(@PathVariable("id") Long id, @RequestBody String request) {
		OutputResponse response = new OutputResponse();
		try {

			String str = "{     \"PatientId\": 123456789,     \"Note\": {         \"Id\": 0,         \"UserId\": 123,         \"UserDescription\": \"Demo Login\",         \"ActionTaken\": \"Manual doses marked for 2/3/2020, 3/3/2020, 7/3/2020\",         \"Comments\": null,         \"Category\": null,         \"SubCategory\": null,         \"Timestamp\": \"2020-03-20T04:13:17.6245221\",         \"DateOfAction\": null     },     \"AdherenceString\": \"8999999999966666666666666666666\" } \r\n";
			response.setResponse(str);
		} catch (Exception e) {
			logger.error("", e);
			response.setError(e);
		}
		return response.toString();
	}

	
	@Operation(summary = "Everwell login")
	@RequestMapping(value = "/login", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String everwellLogin(@RequestBody LoginRequestModelEverwell login) {

		OutputResponse response = new OutputResponse();
		logger.info("It reaches here ");
		try {
			String responseData = null;
			if (login.getEverwellUserName().equalsIgnoreCase("everwellUser")
					&& login.getEverwellPassword().equals("everwellpass")) {

				responseData = "{\r\n"
						+ " \"access_token\": \"Bearer XwvQ8FWJgL1r1coDA9hI9Zfn0BnzSe0MsI5ECb6UhhSFz96ASoh\",\r\n"
						+ " \"token_type\": \"bearer\",\r\n" + " \"expires_in\": 2591999,\r\n"
						+ " \"userName\": \"<username-used-in-login>\",\r\n"
						+ " \".issued\": \"Mon, 23 Nov 2019 16:18:08 GMT\",\r\n"
						+ " \".expires\": \"Wed, 22 Dec 2020 16:18:08 GMT\"\r\n" + "}\r\n" + "";

			}
			response.setResponse(responseData);
		} catch (Exception e) {
			response.setError(e);
		}

		return response.toString();
	}
}