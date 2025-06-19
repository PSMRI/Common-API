package com.iemr.common.controller.eausadha;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iemr.common.model.eAusadha.EAusadhaDTO;
import com.iemr.common.service.beneficiary.EAusadhaService;
import com.iemr.common.utils.response.OutputResponse;


@RequestMapping({ "/eAusadha" })
@RestController
public class EAusadhaController {
	
	@Autowired
	private EAusadhaService eAusadhaService;
	
	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	@PostMapping(value = { "/create-eAusadha" })
	public String createEAusadha(@RequestBody EAusadhaDTO eAusadhaDTO, @RequestHeader(value = "Authorization") String Authorization) {
		OutputResponse response = new OutputResponse();
		try {
			logger.info("get eausadha request:" + eAusadhaDTO);
			String res = eAusadhaService.createEAusadha(eAusadhaDTO, Authorization);
				response.setResponse(res);
			
		} catch (Exception e) {
			response.setError(5000, "Error while entering the Stocks." );
		}

		return response.toString();
	}
	}


