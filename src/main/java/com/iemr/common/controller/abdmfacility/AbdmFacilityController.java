package com.iemr.common.controller.abdmfacility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iemr.common.service.abdmfacility.AbdmFacilityService;
import com.iemr.common.utils.response.OutputResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

@RequestMapping({ "/facility" })
@RestController
public class AbdmFacilityController {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	@Autowired
	private AbdmFacilityService abdmFacilityService;
	
	@CrossOrigin
	@Operation(summary = "Get Abdm facility mapped to worklocation")
	@GetMapping(value = { "/getWorklocationMappedAbdmFacility/{workLocationId}" })
	public String getStoreStockDetails(@PathVariable int workLocationId, @RequestHeader(value = "Authorization") String Authorization) {

		OutputResponse response = new OutputResponse();

		try {

			String resp = abdmFacilityService.getMappedAbdmFacility(workLocationId);

				response.setResponse(resp);

		} catch (Exception e) {

			response.setError(5000, e.getMessage());
			logger.error(e.getMessage());
		}
		logger.info("Get ABDM Registered facilities API response" + response.toString());
		return response.toString();
	}

}
