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
package com.iemr.common.controller.cti;


import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.iemr.common.data.cti.CustomerLanguage;
import com.iemr.common.service.cti.CTIService;
import com.iemr.common.utils.mapper.InputMapper;
import com.iemr.common.utils.response.OutputResponse;

import io.lettuce.core.dynamic.annotation.Param;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(value = "/cti")
public class ComputerTelephonyIntegrationController {
	InputMapper inputMapper = new InputMapper();
	final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	CTIService ctiService;

	@Autowired
	public void setCtiService(CTIService ctiService) {
		this.ctiService = ctiService;
	}

	@Operation(summary = "Get campaign skills")
	@PostMapping(value = "/getCampaignSkills", produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String getCampaignSkills(
			@Param("{\"campaign_name\":\"String: Name of the campaign\"}") @RequestBody String request,
			HttpServletRequest serverRequest) {
		OutputResponse response = new OutputResponse();
		logger.info("getCampaignSkills received a request " + request);
		try {
			String remoteAddress = serverRequest.getHeader("X-FORWARDED-FOR");
			if (remoteAddress == null || remoteAddress.trim().length() == 0) {
				remoteAddress = serverRequest.getRemoteAddr();
			}
			response = ctiService.getCampaignSkills(request, remoteAddress);
		} catch (Exception e) {
			logger.error("getCampaignSkills failed with error " + e.getMessage(), e);
			response.setError(e);
		}
		logger.info("getCampaignSkills sending response " + response);
		return response.toString();
	}

	@Operation(summary = "Get agent state")
	@PostMapping(value = "/getAgentState", produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String getAgentState(@Param("{\"agent_id\":\"String: agent id\"}") @RequestBody String request,
			HttpServletRequest serverRequest) {
		OutputResponse response = new OutputResponse();
		logger.info("getAgentState received a request " + request);
		try {
			String remoteAddress = serverRequest.getHeader("X-FORWARDED-FOR");
			if (remoteAddress == null || remoteAddress.trim().length() == 0) {
				remoteAddress = serverRequest.getRemoteAddr();
			}
			response = ctiService.getAgentState(request, remoteAddress);
		} catch (Exception e) {
			logger.error("getAgentState failed with error " + e.getMessage(), e);
			response.setError(e);
		}
		logger.info("getAgentState sending response " + response);
		return response.toString();
	}

	@Operation(summary = "Get agent call stats")
	@PostMapping(value = "/getAgentCallStats", produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String getAgentCallStats(@Param("{\"agent_id\":\"String: agent id\"}") @RequestBody String request,
			HttpServletRequest serverRequest) {
		OutputResponse response = new OutputResponse();
		logger.info("getAgentCallStats received a request " + request);
		try {
			String remoteAddress = serverRequest.getHeader("X-FORWARDED-FOR");
			if (remoteAddress == null || remoteAddress.trim().length() == 0) {
				remoteAddress = serverRequest.getRemoteAddr();
			}
			response = ctiService.getAgentCallStats(request, remoteAddress);
		} catch (Exception e) {
			logger.error("getAgentCallStats failed with error " + e.getMessage(), e);
			response.setError(e);
		}
		logger.info("getAgentCallStats sending response " + response);
		return response.toString();
	}

	@Operation(summary = "Get campaign names")
	@PostMapping(value = "/getCampaignNames", produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String getCampaignNames(
			@Param("{\"serviceName\":\"String: service name\", "
					+ "\"type\":\"String - inbound, outbound\"}") @RequestBody String request,
			HttpServletRequest serverRequest) {
		OutputResponse response = new OutputResponse();
		logger.info("getCampaignNames received a request " + request);
		try {
			String remoteAddress = serverRequest.getHeader("X-FORWARDED-FOR");
			if (remoteAddress == null || remoteAddress.trim().length() == 0) {
				remoteAddress = serverRequest.getRemoteAddr();
			}
			response = ctiService.getCampaignNames(request, remoteAddress);
		} catch (Exception e) {
			logger.error("getCampaignNames failed with error " + e.getMessage(), e);
			response.setError(e);
		}
		logger.info("getCampaignNames sending response " + response);
		return response.toString();
	}

	@Operation(summary = "Get login key")
	@PostMapping(value = "/getLoginKey", produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String getLoginKey(
			@Param("{\"username\":\"String: user name\", \"password\":\"String: password\"}") @RequestBody String request,
			HttpServletRequest serverRequest) {
		OutputResponse response = new OutputResponse();
		logger.info("getLoginKey received a request " + request);
		try {
			String remoteAddress = serverRequest.getHeader("X-FORWARDED-FOR");
			if (remoteAddress == null || remoteAddress.trim().length() == 0) {
				remoteAddress = serverRequest.getRemoteAddr();
			}
			response = ctiService.getLoginKey(request, remoteAddress);
		} catch (Exception e) {
			logger.error("getLoginKey failed with error " + e.getMessage(), e);
			response.setError(e);
		}
		logger.info("getLoginKey sending response " + response);
		return response.toString();
	}

	@Operation(summary = "Do agent logout")
	@PostMapping(value = "/doAgentLogout", produces = MediaType.APPLICATION_JSON)
	public String doAgentLogout(@Param("{\"agent_id\":\"String: agent id\"}") @RequestBody String request,
			HttpServletRequest serverRequest) {
		OutputResponse response = new OutputResponse();
		logger.info("agentLogout received a request " + request);
		try {
			String remoteAddress = serverRequest.getHeader("X-FORWARDED-FOR");
			if (remoteAddress == null || remoteAddress.trim().length() == 0) {
				remoteAddress = serverRequest.getRemoteAddr();
			}
			response = ctiService.agentLogout(request, remoteAddress);
		} catch (Exception e) {
			logger.error("agentLogout failed with error " + e.getMessage(), e);
			response.setError(e);
		}
		logger.info("agentLogout sending response " + response);
		return response.toString();
	}

	@Operation(summary = "Get online agents")
	@PostMapping(value = "/getOnlineAgents", produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String getOnlineAgents(@Param("{\"agent_id\":\"String: agent id\"}") @RequestBody String request,
			HttpServletRequest serverRequest) {
		OutputResponse response = new OutputResponse();
		logger.info("getOnlineAgents received a request " + request);
		try {
			String remoteAddress = serverRequest.getHeader("X-FORWARDED-FOR");
			if (remoteAddress == null || remoteAddress.trim().length() == 0) {
				remoteAddress = serverRequest.getRemoteAddr();
			}
			response = ctiService.getOnlineAgents(request, remoteAddress);
		} catch (Exception e) {
			logger.error("getOnlineAgents failed with error " + e.getMessage(), e);
			response.setError(e);
		}
		logger.info("getOnlineAgents sending response " + response);
		return response.toString();
	}

	@Operation(summary = "Call beneficiary")
	@PostMapping(value = "/callBeneficiary", produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String callBeneficiary(
			@Param("{\"agent_id\":\"String: agent_id\", \"phone_num\":\"String - phone number\"}") @RequestBody String request,
			HttpServletRequest serverRequest) {
		OutputResponse response = new OutputResponse();
		logger.info("callBeneficiary received a request " + request);
		try {
			String remoteAddress = serverRequest.getHeader("X-FORWARDED-FOR");
			if (remoteAddress == null || remoteAddress.trim().length() == 0) {
				remoteAddress = serverRequest.getRemoteAddr();
			}
			response = ctiService.callBeneficiary(request, remoteAddress);
		} catch (Exception e) {
			logger.error("callBeneficiary failed with error " + e.getMessage(), e);
			response.setError(e);
		}
		logger.info("callBeneficiary sending response " + response);
		return response.toString();
	}

	@Operation(summary = "Add update user data")
	@PostMapping(value = "/addUpdateUserData", produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String addUpdateUserData(
			@Param("{\"username\":\"String: username\", \"password\":\"String - password\", "
					+ "\"firstname\":\"String - firstname\", \"lastname\":\"String - lastname\", "
					+ "\"phone\":\"String - phone\", \"email\":\"String - email\", \"role\":\"String - role\""
					+ ", \"designation\":\"String - designation\"}") @RequestBody String request,
			HttpServletRequest serverRequest) {
		OutputResponse response = new OutputResponse();
		logger.info("updateUserData received a request " + request);
		try {
			String remoteAddress = serverRequest.getHeader("X-FORWARDED-FOR");
			if (remoteAddress == null || remoteAddress.trim().length() == 0) {
				remoteAddress = serverRequest.getRemoteAddr();
			}
			response = ctiService.addUpdateUserData(request, remoteAddress);
		} catch (Exception e) {
			logger.error("updateUserData failed with error " + e.getMessage(), e);
			response.setError(e);
		}
		logger.info("updateUserData sending response " + response);
		return response.toString();
	}

	@Operation(summary = "Get transfer campaigns")
	@PostMapping(value = "/getTransferCampaigns", produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String getTransferCampaigns(@Param("{\"agent_id\":\"String: agentID\"}") @RequestBody String request,
			HttpServletRequest serverRequest) {
		OutputResponse response = new OutputResponse();
		logger.info("getTransferCampaigns received a request " + request);
		try {
			String remoteAddress = serverRequest.getHeader("X-FORWARDED-FOR");
			if (remoteAddress == null || remoteAddress.trim().length() == 0) {
				remoteAddress = serverRequest.getRemoteAddr();
			}
			response = ctiService.getTransferCampaigns(request, remoteAddress);
		} catch (Exception e) {
			logger.error("getTransferCampaigns failed with error " + e.getMessage(), e);
			response.setError(e);
		}
		logger.info("getTransferCampaigns sending response " + response);
		return response.toString();
	}

	@Operation(summary = "Get campaign roles")
	@PostMapping(value = "/getCampaignRoles", produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String getCampaignRoles(@Param("{\"campaign\":\"String: campaign name\"}") @RequestBody String request,
			HttpServletRequest serverRequest) {
		OutputResponse response = new OutputResponse();
		logger.info("getCampaignRoles received a request " + request);
		try {
			String remoteAddress = serverRequest.getHeader("X-FORWARDED-FOR");
			if (remoteAddress == null || remoteAddress.trim().length() == 0) {
				remoteAddress = serverRequest.getRemoteAddr();
			}
			response = ctiService.getCampaignRoles(request, remoteAddress);
		} catch (Exception e) {
			logger.error("getCampaignRoles failed with error " + e.getMessage(), e);
			response.setError(e);
		}
		logger.info("getCampaignRoles sending response " + response);
		return response.toString();
	}

	@Operation(summary = "Set call disposition")
	@PostMapping(value = "/setCallDisposition", produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String setCallDisposition(
			@Param("{\"agent_id\":\"String: agentID\", \"cust_disp\":\"String - call subtype\", "
					+ "\"category\":\"String: call type\", \"session_id\":\"String: unique call id from c-zentrix\"}") @RequestBody String request,
			HttpServletRequest serverRequest) {
		OutputResponse response = new OutputResponse();
		logger.info("setCallDisposition received a request " + request);
		try {
			String remoteAddress = serverRequest.getHeader("X-FORWARDED-FOR");
			if (remoteAddress == null || remoteAddress.trim().length() == 0) {
				remoteAddress = serverRequest.getRemoteAddr();
			}
			response = ctiService.setCallDisposition(request, remoteAddress);
		} catch (Exception e) {
			logger.error("setCallDisposition failed with error " + e.getMessage(), e);
			response.setError(e);
		}
		logger.info("setCallDisposition sending response " + response);
		return response.toString();
	}

	@Operation(summary = "Create voice file")
	@PostMapping(value = "/createVoiceFile", produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String createVoiceFile(
			@Param("{\"agent_id\":\"String: agentID\", "
					+ "\"session_id\":\"String: unique call id from c-zentrix\"}") @RequestBody String request,
			HttpServletRequest serverRequest) {
		OutputResponse response = new OutputResponse();
		logger.info("createVoiceFile received a request " + request);
		try {
			String remoteAddress = serverRequest.getHeader("X-FORWARDED-FOR");
			if (remoteAddress == null || remoteAddress.trim().length() == 0) {
				remoteAddress = serverRequest.getRemoteAddr();
			}
			response = ctiService.createVoiceFile(request, remoteAddress);
		} catch (Exception e) {
			logger.error("createVoiceFile failed with error " + e.getMessage(), e);
			response.setError(e);
		}
		logger.info("createVoiceFile sending response " + response);
		return response.toString();
	}

	@Operation(summary = "Get voice file")
	@PostMapping(value = "/getVoiceFile", produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String getVoiceFile(
			@Param("{\"agent_id\":\"String: agentID\", "
					+ "\"session_id\":\"String: unique call id from c-zentrix\"}") @RequestBody String request,
			HttpServletRequest serverRequest) {
		OutputResponse response = new OutputResponse();
		logger.info("getVoiceFile received a request " + request);
		try {
			String remoteAddress = serverRequest.getHeader("X-FORWARDED-FOR");
			if (remoteAddress == null || remoteAddress.trim().length() == 0) {
				remoteAddress = serverRequest.getRemoteAddr();
			}
			response = ctiService.getVoiceFile(request, remoteAddress);
		} catch (Exception e) {
			logger.error("getVoiceFile failed with error " + e.getMessage(), e);
			response.setError(e);
		}
		logger.info("getVoiceFile sending response " + response);
		return response.toString();
	}

	@Operation(summary = "Disconnect call")
	@PostMapping(value = "/disconnectCall", produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String disconnectCall(@Param("{\"agent_id\":\"String: agentID\"}") @RequestBody String request,
			HttpServletRequest serverRequest) {
		OutputResponse response = new OutputResponse();
		logger.info("disconnectCall received a request " + request);
		try {
			String remoteAddress = serverRequest.getHeader("X-FORWARDED-FOR");
			if (remoteAddress == null || remoteAddress.trim().length() == 0) {
				remoteAddress = serverRequest.getRemoteAddr();
			}
			response = ctiService.disconnectCall(request, remoteAddress);
		} catch (Exception e) {
			logger.error("disconnectCall failed with error " + e.getMessage(), e);
			response.setError(e);
		}
		logger.info("disconnectCall sending response " + response);
		return response.toString();
	}

	@Operation(summary = "Switch to inbound")
	@PostMapping(value = "/switchToInbound", produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String switchToInbound(@Param("{\"agent_id\":\"String: agentID\"}") @RequestBody String request,
			HttpServletRequest serverRequest) {
		OutputResponse response = new OutputResponse();
		logger.info("disconnectCall received a request " + request);
		try {
			String remoteAddress = serverRequest.getHeader("X-FORWARDED-FOR");
			if (remoteAddress == null || remoteAddress.trim().length() == 0) {
				remoteAddress = serverRequest.getRemoteAddr();
			}
			response = ctiService.switchToInbound(request, remoteAddress);
		} catch (Exception e) {
			logger.error("disconnectCall failed with error " + e.getMessage(), e);
			response.setError(e);
		}
		logger.info("disconnectCall sending response " + response);
		return response.toString();
	}

	@Operation(summary = "Switch to outbound")
	@PostMapping(value = "/switchToOutbound", produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String switchToOutbound(@Param("{\"agent_id\":\"String: agentID\"}") @RequestBody String request,
			HttpServletRequest serverRequest) {
		OutputResponse response = new OutputResponse();
		logger.info("disconnectCall received a request " + request);
		try {
			String remoteAddress = serverRequest.getHeader("X-FORWARDED-FOR");
			if (remoteAddress == null || remoteAddress.trim().length() == 0) {
				remoteAddress = serverRequest.getRemoteAddr();
			}
			response = ctiService.switchToOutbound(request, remoteAddress);
		} catch (Exception e) {
			logger.error("disconnectCall failed with error " + e.getMessage(), e);
			response.setError(e);
		}
		logger.info("disconnectCall sending response " + response);
		return response.toString();
	}

	@Operation(summary = "Get agent IP address")
	@PostMapping(value = "/getAgentIPAddress", produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String getAgentIPAddress(@Param("{\"agent_id\":\"String: agentID\"}") @RequestBody String request,
			HttpServletRequest serverRequest) {
		OutputResponse response = new OutputResponse();
		logger.info("getAgentIPAddress received a request " + request);
		try {
			String remoteAddress = serverRequest.getHeader("X-FORWARDED-FOR");
			if (remoteAddress == null || remoteAddress.trim().length() == 0) {
				remoteAddress = serverRequest.getRemoteAddr();
			}
			response = ctiService.getAgentIPAddress(request, remoteAddress);
		} catch (Exception e) {
			logger.error("getAgentIPAddress failed with error " + e.getMessage(), e);
			response.setError(e);
		}
		logger.info("getAgentIPAddress sending response " + response);
		return response.toString();
	}

	@Operation(summary = "Get available agent skills")
	@PostMapping(value = "/getAvailableAgentSkills", produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String getAvailableAgentSkills(
			@Param("{\"campaignName\":\"String: campaign name\", "
					+ "\"skill\":\"String: skill name\"}") @RequestBody String request,
			HttpServletRequest serverRequest) {
		OutputResponse response = new OutputResponse();
		logger.info("getAvailableAgentSkills received a request " + request);
		try {
			String remoteAddress = serverRequest.getHeader("X-FORWARDED-FOR");
			if (remoteAddress == null || remoteAddress.trim().length() == 0) {
				remoteAddress = serverRequest.getRemoteAddr();
			}
			response = ctiService.getAvailableAgentSkills(request, remoteAddress);
		} catch (Exception e) {
			logger.error("getAvailableAgentSkills failed with error " + e.getMessage(), e);
			response.setError(e);
		}
		logger.info("getAvailableAgentSkills sending response " + response);
		return response.toString();
	}

	@Operation(summary = "Transfer call")
	@PostMapping(value = "/transferCall", produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String transferCall(@Param("{\"transfer_from\":\"String - agent id who is transferring call\", "
			+ "\"transfer_to\":\"String - agent ID who would be receiving call\", "
			+ "\"transfer_campaign_info\": \"String - name of the campaign to which call will be transferred\", "
			+ "\"skill_transfer_flag\":\"Integer - 0 for no skill or 1 for skill based\", "
			+ "\"skill\":\"String - skill name\",\"benCallID\":\"Integer - callID as in CRM\", \"agentIPAddress\":\"Optional String - agent IP address\","
			+ "\"callTypeID\" : \"Integer\"}. transfer_to will be having higher precedence to all "
			+ "3 remaining inputs") @RequestBody String request, HttpServletRequest serverRequest) {
		OutputResponse response = new OutputResponse();
		logger.info("transferCall received a request " + request);
		try {
			String remoteAddress = serverRequest.getHeader("X-FORWARDED-FOR");
			if (remoteAddress == null || remoteAddress.trim().length() == 0) {
				remoteAddress = serverRequest.getRemoteAddr();
			}
			response = ctiService.transferCall(request, remoteAddress);
		} catch (Exception e) {
			logger.error("transferCall failed with error " + e.getMessage(), e);
			response.setError(e);
		}
		logger.info("transferCall sending response " + response);
		return response.toString();
	}

	@Operation(summary = "Customer preferred language")
	@PostMapping(value = "/customerPreferredLanguage", produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String customerPreferredLanguage(
			@Param("{\"cust_ph_no\":\"phone number of customer\", "
					+ "\"campaign_name\":\"campaign name for provider from provider service mapping\", "
					+ "\"language\": \"String - language name selected by user\", "
					+ "\"action\":\"String - select or update\"}") @RequestBody String request,
			HttpServletRequest serverRequest) {
		OutputResponse response = new OutputResponse();
		logger.info("customerPreferredLanguage received a request " + request);
		try {
			String remoteAddress = serverRequest.getHeader("X-FORWARDED-FOR");
			if (remoteAddress == null || remoteAddress.trim().length() == 0) {
				remoteAddress = serverRequest.getRemoteAddr();
			}
			CustomerLanguage custLang = InputMapper.gson().fromJson(request, CustomerLanguage.class);
			response = ctiService.customerPreferredLanguage(custLang, remoteAddress);
		} catch (Exception e) {
			logger.error("customerPreferredLanguage failed with error " + e.getMessage(), e);
			response.setError(e);
		}
		logger.info("customerPreferredLanguage sending response " + response);
		return response.toString();
	}

	@Operation(summary = "Get IVRS path details")
	@PostMapping(value = "/getIVRSPathDetails", produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String getIVRSPathDetails(@Param("{\"agent_id\":\"Integer\"}") @RequestBody String request,
			HttpServletRequest serverRequest) {
		OutputResponse response = new OutputResponse();
		logger.info("getIVRSPathDetails received a request " + request);
		try {

			String remoteAddress = serverRequest.getHeader("X-FORWARDED-FOR");
			if (remoteAddress == null || remoteAddress.trim().length() == 0) {
				remoteAddress = serverRequest.getRemoteAddr();
			}
			response = ctiService.getIVRSPathDetails(request, remoteAddress);
		} catch (Exception e) {
			logger.error("getIVRSPathDetails failed with error " + e.getMessage(), e);
			response.setError(e);
		}
		logger.info("getIVRSPathDetails sending response " + response);
		return response.toString();
	}
}
