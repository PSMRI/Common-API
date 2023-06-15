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
package com.iemr.common.controller.otp;

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

import com.iemr.common.data.otp.OTPRequestParsor;
import com.iemr.common.service.otp.OTPHandler;
import com.iemr.common.utils.mapper.InputMapper;
import com.iemr.common.utils.response.OutputResponse;

import io.swagger.annotations.ApiParam;

/***
 * 
 * @author NE298657
 * @date DD/MM/YYYY - 10/08/2020
 * 
 * @implSpec handle OTP flow request and response
 */

@RestController
@RequestMapping(value = "/otp")
public class OTPGateway {
	final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	@Autowired
	private OTPHandler otpHandler;

	/***
	 * 
	 * @param requestOBJ
	 * @apiNote send OTP for authentication
	 * @return
	 * 
	 */
	@CrossOrigin()
	@RequestMapping(value = "/sendOTP", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String sendOTP(@ApiParam(value="{\"mobNo\":\"String\"}") @RequestBody String requestOBJ) {

		OutputResponse response = new OutputResponse();

		try {
			OTPRequestParsor obj = InputMapper.gson().fromJson(requestOBJ, OTPRequestParsor.class);

			String success = otpHandler.sendOTP(obj);
			if (success.equalsIgnoreCase("success"))
				response.setResponse(success);
			else
				response.setError(5000, "failure");

		} catch (Exception e) {
			logger.error("error in sending OTP : " + e);
			response.setError(5000, "error : " + e);
		}
		return response.toString();
	}

	/***
	 * 
	 * @param requestOBJ
	 * @apiNote validate OTP for authorization, and on success generate AuthKey
	 * @return
	 */
	@CrossOrigin()
	@RequestMapping(value = "/validateOTP", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String validateOTP(@ApiParam(value="{\"mobNo\":\"String\",\"otp\":\"Integer\"}") @RequestBody String requestOBJ) {

		OutputResponse response = new OutputResponse();

		try {
			OTPRequestParsor obj = InputMapper.gson().fromJson(requestOBJ, OTPRequestParsor.class);

			JSONObject responseOBJ = otpHandler.validateOTP(obj);
			if (responseOBJ != null)
				response.setResponse(responseOBJ.toString());
			else
				response.setError(5000, "failure");

		} catch (Exception e) {
			logger.error("error in validating OTP : " + e);
			response.setError(5000, "error : " + e);
		}
		return response.toString();
	}

	@CrossOrigin()
	@RequestMapping(value = "/resendOTP", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String resendOTP(@ApiParam(value="{\"mobNo\":\"String\"}") @RequestBody String requestOBJ) {

		OutputResponse response = new OutputResponse();

		try {
			OTPRequestParsor obj = InputMapper.gson().fromJson(requestOBJ, OTPRequestParsor.class);

			String success = otpHandler.resendOTP(obj);
			if (success.equalsIgnoreCase("success"))
				response.setResponse(success);
			else
				response.setError(5000, "failure");

		} catch (Exception e) {
			logger.error("error in re-sending OTP : " + e);
			response.setError(5000, "error : " + e);
		}
		return response.toString();
	}
}
