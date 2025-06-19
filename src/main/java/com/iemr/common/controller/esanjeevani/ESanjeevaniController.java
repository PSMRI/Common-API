package com.iemr.common.controller.esanjeevani;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.iemr.common.service.esanjeevani.ESanjeevaniService;
import com.iemr.common.utils.response.OutputResponse;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping(value = "/esanjeevani", headers = "Authorization")
public class ESanjeevaniController {

	@Autowired
	private ESanjeevaniService eSanjeevaniService;

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	@Operation(summary = "Register patient in E-Sanjeevani and send portal url to route")
	@GetMapping(value = { "/getESanjeevaniUrl/{beneficiaryReqId}" }, consumes = "application/json", produces = "application/json")
	public String registerESanjeevaniPatient(@PathVariable Long beneficiaryReqId) {
		OutputResponse response = new OutputResponse();
		try {
			logger.info("E-Sangeevani register for benRegId - " + beneficiaryReqId);
			String res = eSanjeevaniService.registerPatient(beneficiaryReqId);
			if (res != null)
				response.setResponse(res);
			else 
				response.setError(5000, "Error while fetching E-sanjeevani route URL");
		} catch (Exception e) {
			response.setError(5000, "Error while fetching E-sanjeevani route URL" + e.toString());
		}

		return response.toString();
	}

}
