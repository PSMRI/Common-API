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
package com.iemr.common.controller.covid;

import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.iemr.common.data.covid.CovidVaccinationStatus;
import com.iemr.common.service.covid.CovidVaccinationService;
import com.iemr.common.utils.exception.IEMRException;
import com.iemr.common.utils.response.OutputResponse;

import io.lettuce.core.dynamic.annotation.Param;
import io.swagger.v3.oas.annotations.Operation;



@RequestMapping(value = "/covid")
@RestController
public class CovidVaccinationController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	@Autowired
	private CovidVaccinationService covidVaccinationService;

	/**
	 * @Objective provides list of vaccination type and dose taken
	 * @return list of vaccination type and dose taken
	 */

	
	@Operation(summary = "Master Data for Vaccination Type & Dose Taken")
	@GetMapping(value = {
			"/master/VaccinationTypeAndDoseTaken" }, produces = MediaType.APPLICATION_JSON)

	public String getVaccinationTypeAndDoseTaken(@RequestHeader(value = "Authorization") String Authorization) {
		OutputResponse response = new OutputResponse();
		try {
			response.setResponse(covidVaccinationService.getVaccinationTypeAndDoseTaken());
		} catch (Exception e) {
			logger.error("Get vaccination type and dose taken master failed with error " + e.getMessage(), e);
//			response.setError(e);
			response.setError(5000, e.getMessage());
		}
		logger.info("Get vaccination type and dose taken master response " + response.toString());
		return response.toString();

	}

	/***
	 * 
	 * @param request
	 * @param Authorization
	 * @return Covid vaccination details of a beneficiary
	 */
	
	@Operation(summary = "Getting beneficiary covid vaccination details")

	@PostMapping(value = { "/getCovidVaccinationDetails" })
	public String getCovidVaccinationDetails(
			@Param(value = "{\"beneficiaryRegID\":\"Long\"}") @RequestBody CovidVaccinationStatus covidVaccinationStatus,
			@RequestHeader(value = "Authorization") String Authorization) {

		OutputResponse response = new OutputResponse();
		logger.info("getCovidVaccinationDetails request, beneficiaryRegId :"
				+ covidVaccinationStatus.getBeneficiaryRegID());
		try {

			if (covidVaccinationStatus.getBeneficiaryRegID() != null) {

				String beneficiaryDetails = covidVaccinationService
						.getCovidVaccinationDetails(covidVaccinationStatus.getBeneficiaryRegID());
				if (beneficiaryDetails != null) {
					response.setResponse(beneficiaryDetails);
				} else {
					response.setResponse("Error in getting beneficiary vaccination details");
				}
			}

			else {
				throw new IEMRException("Invalid beneficiary Id");
			}
		} catch (Exception e) {
			logger.error("Error in getCovidVaccinationDetails :" + e);
//			response.setError(e);
			response.setError(5000, e.getMessage());
		}
		logger.info("getCovidVaccinationDetails response :" + response);
		return response.toString();

	}

	/***
	 * 
	 * @param request
	 * @param Authorization
	 * @return Saving beneficiary vaccination details
	 */

	
	@Operation(summary = "Save beneficiary covid vaccination details")
	@PostMapping(value = { "/saveCovidVaccinationDetails" })
	public String saveCovidVaccinationDetails(
			@Param(value = "{\"covidVSID\": \"Long\",\"beneficiaryRegID\":\"Long\","
					+ "\"CovidVaccineTypeID\":\"Integer\","
					+ "\"ProviderServiceMapID\":\"Integer\",\"CreatedBy\":\"String\","
					+ "\"ModifiedBy\":\"String\",\"VanID\":\"Integer\"}") @RequestBody String request,
			@RequestHeader(value = "Authorization") String Authorization) {
		logger.info("save Covid Vaccination Details request " + request);
		OutputResponse output = new OutputResponse();
		try {
			if (request != null) {
				String covidStatus = covidVaccinationService.saveBenCovidVaccinationDetails(request);
				if (covidStatus != null)
					output.setResponse(covidStatus);
				else
					throw new IEMRException("Error in saving covid vaccination details");
			} else {
				throw new IEMRException("Empty / null request object");
			}

		} catch (IEMRException e) {
			logger.error("saveCovidVaccinationDetails failed with error " + e.getMessage(), e);
//			output.setError(e);
			output.setError(5000, e.getMessage());
		} catch (Exception e) {
			logger.error("saveCovidVaccinationDetails failed with error " + e.getMessage(), e);
//			output.setError(e);
			output.setError(5000, e.getMessage());
		}
		logger.info("saveCovidVaccinationDetails response: " + output);
		return output.toString();
	}

}
