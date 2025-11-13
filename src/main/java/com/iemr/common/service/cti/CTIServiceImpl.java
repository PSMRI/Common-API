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
package com.iemr.common.service.cti;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iemr.common.data.callhandling.BeneficiaryCall;
import com.iemr.common.data.callhandling.CallType;
import com.iemr.common.data.cti.AgentCallStats;
import com.iemr.common.data.cti.AgentLoginKey;
import com.iemr.common.data.cti.AgentSkills;
import com.iemr.common.data.cti.AgentState;
import com.iemr.common.data.cti.AutoPreviewDial;
import com.iemr.common.data.cti.BlockUnblockNumber;
import com.iemr.common.data.cti.CTICampaigns;
import com.iemr.common.data.cti.CTIResponse;
import com.iemr.common.data.cti.CTIResponseTemp;
import com.iemr.common.data.cti.CTIUser;
import com.iemr.common.data.cti.CTIVoiceFile;
import com.iemr.common.data.cti.CallBeneficiary;
import com.iemr.common.data.cti.CallDisposition;
import com.iemr.common.data.cti.CampaignNames;
import com.iemr.common.data.cti.CampaignRole;
import com.iemr.common.data.cti.CampaignSkills;
import com.iemr.common.data.cti.CustomerLanguage;
import com.iemr.common.data.cti.TransferCall;
import com.iemr.common.repository.callhandling.BeneficiaryCallRepository;
import com.iemr.common.repository.callhandling.IEMRCalltypeRepositoryImplCustom;
import com.iemr.common.utils.config.ConfigProperties;
import com.iemr.common.utils.encryption.AESUtil;
import com.iemr.common.utils.exception.IEMRException;
import com.iemr.common.utils.http.HttpUtils;
import com.iemr.common.utils.mapper.InputMapper;
import com.iemr.common.utils.response.OutputResponse;

@Service
public class CTIServiceImpl implements CTIService {
	private Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

	private InputMapper inputMapper = new InputMapper();

	private static HttpUtils httpUtils;// = new HttpUtils();

	private static final String SUCCESS = "success";
	private static final Integer SESSION_TIMEOUT = 60000;

	// Standard API response codes
	private static final String STD_API_SUCCESS = "1";
	private static final String STD_API_FAILURE = "0";

	// Custom API response codes
	private static final String CUSTOM_API_SUCCESS = "1";
	private static final String CUSTOM_API_FAILURE = "0";

	private static final String DEFAULT_IP = "0.0.0.0";
		
	private AESUtil aesUtil;


	@Autowired
	private BeneficiaryCallRepository beneficiaryCallRepository;

	@Autowired
	private CTIService ctiService;

	@Autowired
	private IEMRCalltypeRepositoryImplCustom iemrCalltypeRepositoryImplCustom;

	public CTIServiceImpl() {
		if (httpUtils == null) {
			httpUtils = new HttpUtils();
		}
	}

	@Override
	public String callUrl(String urlRequest) {
		String result = httpUtils.get(urlRequest);
		return result;
	}

	@Override
	public OutputResponse addUpdateAgentSkills(String request, String ipAddress) throws JSONException, IEMRException, JsonMappingException, JsonProcessingException {
		OutputResponse result = new OutputResponse();
		ObjectMapper objectMapper = new ObjectMapper();
		logger.debug("addUpdateAgentSkills input is " + request);
		String ctiURI = ConfigProperties.getPropertyByName("add-update-agent-skills-URL");
		String serverURL = ConfigProperties.getPropertyByName("cti-server-ip");
		AgentSkills agentSkills = objectMapper.readValue(request, AgentSkills.class);

		String agentID = (agentSkills.getAgentID() != null) ? agentSkills.getAgentID() : "";
		String agentIPResp = getAgentIP(agentID);
		String agentIP = !agentIPResp.equals(DEFAULT_IP) ? agentIPResp : ipAddress;

		ctiURI = ctiURI.replace("CTI_SERVER", serverURL);
		ctiURI = ctiURI.replace("AGENT_ID", agentID);
		ctiURI = ctiURI.replace("SKILL_NAME", (agentSkills.getSkill() == null ? "" : agentSkills.getSkill()));
		ctiURI = ctiURI.replace("WEIGHTAGE", (agentSkills.getWeight() == null ? "" : agentSkills.getWeight()));
		ctiURI = ctiURI.replace("OPERATION", (agentSkills.getType() == null ? "" : agentSkills.getType()));
		ctiURI = ctiURI.replace("AGENT_IP", agentIP);
		logger.info("calling URL " + ctiURI);
		String response = this.callUrl(ctiURI);// httpUtils.get(ctiURI);
		logger.info("URL " + ctiURI + " returned response " + response);
		AgentSkills skillsResponse = InputMapper.gson().fromJson(response, AgentSkills.class);
		CTIResponse ctiResponse = skillsResponse.getResponseObj();
		if (ctiResponse.getResponse_code().equals(CUSTOM_API_SUCCESS)) {
			agentSkills.setResponse(ctiResponse);
			result.setResponse(agentSkills.toString());
		} else {
			result.setError(OutputResponse.GENERIC_FAILURE, ctiResponse.getReason(), ctiResponse.getStatus());
		}
		return result;
	}

	@Override
	public OutputResponse getCampaignSkills(String request, String ipAddress) throws IEMRException, JSONException, JsonMappingException, JsonProcessingException {
		OutputResponse output = new OutputResponse();
		ObjectMapper objectMapper = new ObjectMapper();
		String ctiURI = ConfigProperties.getPropertyByName("get-campaign-skills-URL");
		String serverURL = ConfigProperties.getPropertyByName("cti-server-ip");
		CampaignSkills agentState = objectMapper.readValue(request, CampaignSkills.class);
		ctiURI = ctiURI.replace("CTI_SERVER", serverURL);
		ctiURI = ctiURI.replace("CAMPAIGN_NAME",
				((agentState.getCampaignName() != null) ? agentState.getCampaignName() : ""));
		ctiURI = ctiURI.replace("AGENT_IP", ipAddress);
		logger.info("calling URL " + ctiURI);
		String response = this.callUrl(ctiURI);// httpUtils.get(ctiURI);
//		logger.info("URL " + ctiURI + " returned response " + response);
		// JSONObject responseObj = new JSONObject(response);
		// response = responseObj.get("response").toString();
		CampaignSkills skillsResponse = InputMapper.gson().fromJson(response, CampaignSkills.class);
		CTIResponse ctiResponse = skillsResponse.getResponse();
		if (ctiResponse.getResponse_code().equals(CUSTOM_API_SUCCESS)) {
			agentState.setResponse(ctiResponse);
			output.setResponse(agentState.toString());
		} else {
			output.setError(OutputResponse.GENERIC_FAILURE, ctiResponse.getReason(), ctiResponse.getStatus());
		}
		return output;
	}

	@Override
	public OutputResponse getAgentState(String request, String ipAddress) throws IEMRException, JSONException, JsonMappingException, JsonProcessingException {
		OutputResponse output = new OutputResponse();
		ObjectMapper objectMapper = new ObjectMapper();
		String ctiURI = ConfigProperties.getPropertyByName("get-agent-status-URL");
		String serverURL = ConfigProperties.getPropertyByName("cti-server-ip");
		AgentState agentState = objectMapper.readValue(request, AgentState.class);

		String agentID = (agentState.getAgent_id() != null) ? agentState.getAgent_id() : "";

		String agentIPResp = getAgentIP(agentID);
		String agentIP = !agentIPResp.equals(DEFAULT_IP) ? agentIPResp : ipAddress;

		ctiURI = ctiURI.replace("CTI_SERVER", serverURL);
		ctiURI = ctiURI.replace("AGENT_ID", agentID);
		ctiURI = ctiURI.replace("AGENT_IP", agentIP);
		logger.info("calling URL " + ctiURI);
		String response = this.callUrl(ctiURI);
		logger.info("URL " + ctiURI + " returned response " + response);
		AgentState state = InputMapper.gson().fromJson(response, AgentState.class);
		CTIResponseTemp ctiResponse = state.getResponse();
		if (ctiResponse.getResponse_code().equals(STD_API_SUCCESS)) {
			agentState.setResponse(ctiResponse);
			output.setResponse(agentState.toString());
		} else {
			output.setError(OutputResponse.GENERIC_FAILURE, ctiResponse.getReason(), ctiResponse.getStatus());
		}
		return output;
	}

	@Override
	public OutputResponse getAgentCallStats(String request, String ipAddress) throws IEMRException, JSONException, JsonMappingException, JsonProcessingException {
		OutputResponse output = new OutputResponse();
		ObjectMapper objectMapper = new ObjectMapper();
		String ctiURI = ConfigProperties.getPropertyByName("get-agent-call-stats-URL");
		String serverURL = ConfigProperties.getPropertyByName("cti-server-ip");
		AgentCallStats agentState = objectMapper.readValue(request, AgentCallStats.class);

		String agentID = (agentState.getAgentID() != null) ? agentState.getAgentID() : "";

		String agentIPResp = getAgentIP(agentID);
		String agentIP = !agentIPResp.equals(DEFAULT_IP) ? agentIPResp : ipAddress;

		ctiURI = ctiURI.replace("CTI_SERVER", serverURL);
		ctiURI = ctiURI.replace("AGENT_ID", agentID);
		ctiURI = ctiURI.replace("AGENT_IP", agentIP);
		logger.info("calling URL " + ctiURI);
		String response = this.callUrl(ctiURI);// httpUtils.get(ctiURI);
		logger.info("URL " + ctiURI + " returned response " + response);
		// JSONObject responseObj = new JSONObject(response);
		// response = responseObj.get("response").toString();
		AgentCallStats state = InputMapper.gson().fromJson(response, AgentCallStats.class);
		CTIResponse ctiResponse = state.getResponse();
		if (ctiResponse.getResponse_code().equals(CUSTOM_API_SUCCESS)) {
			agentState.setResponse(ctiResponse);
			output.setResponse(agentState.toString());
		} else {
			output.setError(OutputResponse.GENERIC_FAILURE, ctiResponse.getReason(), ctiResponse.getStatus());
		}
		return output;
	}

	@Override
	public OutputResponse getCampaignNames(String request, String ipAddress) throws IEMRException, JSONException, JsonMappingException, JsonProcessingException {
		OutputResponse output = new OutputResponse();
		ObjectMapper objectMapper = new ObjectMapper();
		String ctiURI = ConfigProperties.getPropertyByName("get-campaign-name-URL");
		String serverURL = ConfigProperties.getPropertyByName("cti-server-ip");
		CampaignNames agentState = objectMapper.readValue(request, CampaignNames.class);
		ctiURI = ctiURI.replace("CTI_SERVER", serverURL);
		ctiURI = ctiURI.replace("SEARCH_KEY", (agentState.getServiceName() != null) ? agentState.getServiceName() : "");
		ctiURI = ctiURI.replace("CAMPAIGN_TYPE", (agentState.getType() != null) ? agentState.getType() : "");
		logger.info("calling URL " + ctiURI);
		ctiURI = ctiURI.replace("AGENT_IP", ipAddress);
		String response = this.callUrl(ctiURI);// httpUtils.get(ctiURI);
		logger.info("URL " + ctiURI + " returned response " + response);
		// JSONObject responseObj = new JSONObject(response);
		// response = responseObj.get("response").toString();
		CampaignNames state = InputMapper.gson().fromJson(response, CampaignNames.class);
		CTIResponseTemp ctiResponse = state.getResponse();
		if (ctiResponse.getResponse_code().equals(CUSTOM_API_SUCCESS)) {
			agentState.setResponse(ctiResponse);
			output.setResponse(agentState.toString());
		} else {
			output.setError(OutputResponse.GENERIC_FAILURE, ctiResponse.getReason(), ctiResponse.getStatus());
		}
		return output;
	}

	@Override
	public OutputResponse doAgentLogin(String request, String ipAddress) throws IEMRException, JSONException, JsonMappingException, JsonProcessingException {
		OutputResponse output = new OutputResponse();
		ObjectMapper objectMapper = new ObjectMapper();
		String ctiURI = ConfigProperties.getPropertyByName("do-agent-login-URL");
		String serverURL = ConfigProperties.getPropertyByName("cti-server-ip");
		AgentState agentState = objectMapper.readValue(request, AgentState.class);

		String agentID = (agentState.getAgent_id() != null) ? agentState.getAgent_id() : "";

		String agentIPResp = getAgentIP(agentID);
		String agentIP = !agentIPResp.equals(DEFAULT_IP) ? agentIPResp : ipAddress;

		ctiURI = ctiURI.replace("CTI_SERVER", serverURL);
		ctiURI = ctiURI.replace("AGENT_ID", agentID);
		ctiURI = ctiURI.replace("AGENT_IP", agentIP);
		logger.info("calling URL " + ctiURI);
		String response = this.callUrl(ctiURI);// httpUtils.get(ctiURI);
		logger.info("URL " + ctiURI + " returned response " + response);
		AgentState state = InputMapper.gson().fromJson(response, AgentState.class);
		CTIResponseTemp ctiResponse = state.getResponse();
		if (ctiResponse.getResponse_code().equals(STD_API_SUCCESS)) {
			agentState.setResponse(ctiResponse);
			output.setResponse(agentState.toString());
		} else {
			output.setError(OutputResponse.GENERIC_FAILURE, ctiResponse.getReason(), ctiResponse.getStatus());
		}
		return output;
	}

	@Override
	public OutputResponse getLoginKey(String request, String ipAddress) throws IEMRException, JSONException, JsonMappingException, JsonProcessingException {
		OutputResponse output = new OutputResponse();
		ObjectMapper objectMapper = new ObjectMapper();
		String ctiURI = ConfigProperties.getPropertyByName("get-login-key-URL");
		String serverURL = ConfigProperties.getPropertyByName("cti-server-ip");
		AgentLoginKey agentState = objectMapper.readValue(request, AgentLoginKey.class);

		String decryptPassword = aesUtil.decrypt("Piramal12Piramal", agentState.getPassword());


		ctiURI = ctiURI.replace("CTI_SERVER", serverURL);
		ctiURI = ctiURI.replace("USERNAME", (agentState.getUsername() != null) ? agentState.getUsername() : "");
		ctiURI = ctiURI.replace("PASSWORD", (decryptPassword != null) ? decryptPassword : "");
		logger.info("calling URL " + ctiURI);
		ctiURI = ctiURI.replace("AGENT_IP", ipAddress);
		String response = this.callUrl(ctiURI);// httpUtils.get(ctiURI);
		logger.info("URL " + ctiURI + " returned response " + response);
		AgentLoginKey state = InputMapper.gson().fromJson(response, AgentLoginKey.class);
		CTIResponse ctiResponse = state.getResponse();
		if (ctiResponse.getResponse_code().equals(CUSTOM_API_SUCCESS)) {
			agentState.setResponse(ctiResponse);
			output.setResponse(agentState.toString());
		} else {
			output.setError(OutputResponse.GENERIC_FAILURE, ctiResponse.getReason(), ctiResponse.getStatus());
		}
		return output;
	}

	@Override
	public OutputResponse agentLogout(String request, String ipAddress) throws IEMRException, JSONException, JsonMappingException, JsonProcessingException {
		OutputResponse output = new OutputResponse();
		ObjectMapper objectMapper = new ObjectMapper();
		String ctiURI = ConfigProperties.getPropertyByName("do-agent-logout-URL");
		String serverURL = ConfigProperties.getPropertyByName("cti-server-ip");
		AgentState agentState = objectMapper.readValue(request, AgentState.class);

		String agentID = (agentState.getAgent_id() != null) ? agentState.getAgent_id() : "";

		String agentIPResp = getAgentIP(agentID);
		String agentIP = !agentIPResp.equals(DEFAULT_IP) ? agentIPResp : ipAddress;

		ctiURI = ctiURI.replace("CTI_SERVER", serverURL);
		ctiURI = ctiURI.replace("AGENT_ID", agentID);
		ctiURI = ctiURI.replace("AGENT_IP", agentIP);
		logger.info("calling URL " + ctiURI);
		String response = this.callUrl(ctiURI);// httpUtils.get(ctiURI);
		logger.info("URL " + ctiURI + " returned response " + response);
		// JSONObject responseObj = new JSONObject(response);
		// response = responseObj.get("response").toString();
		AgentState state = InputMapper.gson().fromJson(response, AgentState.class);
		CTIResponseTemp ctiResponse = state.getResponse();
		if (ctiResponse.getResponse_code().equals(CUSTOM_API_SUCCESS)) {
			agentState.setResponse(ctiResponse);
			output.setResponse(agentState.toString());
		} else {
			output.setError(OutputResponse.GENERIC_FAILURE, ctiResponse.getReason(), ctiResponse.getStatus());
		}
		return output;
	}

	@Override
	public OutputResponse getOnlineAgents(String request, String ipAddress) throws IEMRException, JSONException, JsonMappingException, JsonProcessingException {
		OutputResponse output = new OutputResponse();
		ObjectMapper objectMapper = new ObjectMapper();
		String ctiURI = ConfigProperties.getPropertyByName("do-online-agent-URL");
		String serverURL = ConfigProperties.getPropertyByName("cti-server-ip");
		AgentState agentState = objectMapper.readValue(request, AgentState.class);

		String agentID = (agentState.getAgent_id() != null) ? agentState.getAgent_id() : "";

		String agentIPResp = getAgentIP(agentID);
		String agentIP = !agentIPResp.equals(DEFAULT_IP) ? agentIPResp : ipAddress;

		ctiURI = ctiURI.replace("CTI_SERVER", serverURL);
		ctiURI = ctiURI.replace("AGENT_ID", agentID);
		ctiURI = ctiURI.replace("AGENT_IP", agentIP);

		logger.info("calling URL " + ctiURI);
		String response = this.callUrl(ctiURI);// httpUtils.get(ctiURI);
		logger.info("URL " + ctiURI + " returned response " + response);
		AgentState state = InputMapper.gson().fromJson(response, AgentState.class);
		CTIResponseTemp ctiResponse = state.getResponse();
		if (ctiResponse.getResponse_code().equals(CUSTOM_API_SUCCESS)) {
			agentState.setResponse(ctiResponse);
			output.setResponse(agentState.toString());
		} else {
			output.setError(OutputResponse.GENERIC_FAILURE, ctiResponse.getReason(), ctiResponse.getStatus());
		}
		return output;
	}

	@Override
	public OutputResponse callBeneficiary(String request, String ipAddress) throws IEMRException, JSONException, JsonMappingException, JsonProcessingException {
		OutputResponse output = new OutputResponse();
		ObjectMapper objectMapper = new ObjectMapper();
		String ctiURI = ConfigProperties.getPropertyByName("call-beneficiary-URL");
		String serverURL = ConfigProperties.getPropertyByName("cti-server-ip");
		CallBeneficiary agentState = objectMapper.readValue(request, CallBeneficiary.class);

		String agentID = (agentState.getAgent_id() != null) ? agentState.getAgent_id() : "";

		String agentIPResp = getAgentIP(agentID);
		String agentIP = !agentIPResp.equals(DEFAULT_IP) ? agentIPResp : ipAddress;

		ctiURI = ctiURI.replace("CTI_SERVER", serverURL);
		ctiURI = ctiURI.replace("AGENT_ID", agentID);
		ctiURI = ctiURI.replace("PHONE_NO", (agentState.getPhone_num() != null) ? agentState.getPhone_num() : "");
		ctiURI = ctiURI.replace("AGENT_IP", agentIP);

		logger.info("calling URL " + ctiURI);
		String response = this.callUrl(ctiURI);// httpUtils.get(ctiURI);
		logger.info("URL " + ctiURI + " returned response " + response);
		CallBeneficiary state = InputMapper.gson().fromJson(response, CallBeneficiary.class);
		CTIResponse ctiResponse = state.getResponse();
		if (ctiResponse.getResponse_code().equals(CUSTOM_API_SUCCESS)) {
			agentState.setResponse(ctiResponse);
			output.setResponse(agentState.toString());
		} else {
			output.setError(OutputResponse.GENERIC_FAILURE, ctiResponse.getReason(), ctiResponse.getStatus());
		}
		return output;
	}

	@Override
	public OutputResponse addUpdateUserData(String request, String ipAddress) throws IEMRException, JSONException, JsonMappingException, JsonProcessingException {
		OutputResponse output = new OutputResponse();
		ObjectMapper objectMapper = new ObjectMapper();
		String ctiURI = ConfigProperties.getPropertyByName("add-update-user-data");

		/*
		 * http://CTI_SERVER/apps/cust_appsHandler.php?transaction_id=
		 * CTI_SYSTEM_USER&username=USERNAME&password=
		 * PASSWORD&firstname=FIRSTNAME&lastname=LASTNAME&phone=PHONE_NO&email=
		 * EMAIL&role=ROLE&sessiontimeout=
		 * SESSION_TIMEOUT&designation=DESIGNATION&resFormat=3
		 */

		String serverURL = ConfigProperties.getPropertyByName("cti-server-ip");
		CTIUser ctiUser = objectMapper.readValue(request, CTIUser.class);
		ctiURI = ctiURI.replace("CTI_SERVER", serverURL);
		ctiURI = ctiURI.replace("USERNAME", (ctiUser.getUsername() != null) ? ctiUser.getUsername() : "");
		ctiURI = ctiURI.replace("PASSWORD", (ctiUser.getPassword() != null) ? ctiUser.getPassword() : "");
		ctiURI = ctiURI.replace("FIRSTNAME", (ctiUser.getFirstname() != null) ? ctiUser.getFirstname() : "");
		ctiURI = ctiURI.replace("LASTNAME", (ctiUser.getLastname() != null) ? ctiUser.getLastname() : "");
		ctiURI = ctiURI.replace("PHONE_NO", (ctiUser.getPhone() != null) ? ctiUser.getPhone() : "");
		ctiURI = ctiURI.replace("EMAIL", (ctiUser.getEmail() != null) ? ctiUser.getEmail() : "");
		ctiURI = ctiURI.replace("ROLE", (ctiUser.getRole() != null) ? ctiUser.getRole() : "");
		ctiURI = ctiURI.replace("SESSION_TIMEOUT", SESSION_TIMEOUT.toString());
		ctiURI = ctiURI.replace("DESIGNATION", (ctiUser.getDesignation() != null) ? ctiUser.getDesignation() : "");
		ctiURI = ctiURI.replace("AGENT_IP", ipAddress);
		logger.info("calling URL " + ctiURI);
		String response = this.callUrl(ctiURI);// httpUtils.get(ctiURI);
		logger.info("URL " + ctiURI + " returned response " + response);
		CTIUser state = InputMapper.gson().fromJson(response, CTIUser.class);
		CTIResponse ctiResponse = state.getResponse();
		if (ctiResponse.getResponse_code().equals(CUSTOM_API_SUCCESS)) {
			ctiUser.setResponse(ctiResponse);
			output.setResponse(ctiUser.toString());
		} else {
			output.setError(OutputResponse.GENERIC_FAILURE, ctiResponse.getReason(), ctiResponse.getStatus());
		}
		return output;
	}

	@Override
	public OutputResponse getTransferCampaigns(String request, String ipAddress) throws IEMRException, JSONException, JsonMappingException, JsonProcessingException {
		OutputResponse output = new OutputResponse();
		ObjectMapper objectMapper = new ObjectMapper();
		String ctiURI = ConfigProperties.getPropertyByName("fetch-transferrable-campaigns-URL");
		String serverURL = ConfigProperties.getPropertyByName("cti-server-ip");
		CTICampaigns agentState = objectMapper.readValue(request, CTICampaigns.class);

		String agentID = (agentState.getAgent_id() != null) ? agentState.getAgent_id() : "";

		String agentIPResp = getAgentIP(agentID);
		String agentIP = !agentIPResp.equals(DEFAULT_IP) ? agentIPResp : ipAddress;

		ctiURI = ctiURI.replace("CTI_SERVER", serverURL);
		ctiURI = ctiURI.replace("AGENT_ID", agentID);
		ctiURI = ctiURI.replace("AGENT_IP", agentIP);
		logger.info("calling URL " + ctiURI);
		String response = this.callUrl(ctiURI);// httpUtils.get(ctiURI);
		logger.info("URL " + ctiURI + " returned response " + response);
		// JSONObject responseObj = new JSONObject(response);
		// response = responseObj.get("response").toString();
		CTICampaigns state = InputMapper.gson().fromJson(response, CTICampaigns.class);
		CTIResponseTemp ctiResponse = state.getResponse();
		if (ctiResponse.getCampaign().size() > 0) {
			agentState.setResponse(ctiResponse);
			output.setResponse(agentState.toString());
		} else {
			output.setError(OutputResponse.GENERIC_FAILURE, "No Campaigns Available", "Failure");
		}
		return output;
	}

	@Override
	public OutputResponse getCampaignRoles(String request, String remoteAddr) throws IEMRException, JSONException, JsonMappingException, JsonProcessingException {
		OutputResponse output = new OutputResponse();
		ObjectMapper objectMapper = new ObjectMapper();
		String ctiURI = ConfigProperties.getPropertyByName("get-campaign-roles-URL");
		String serverURL = ConfigProperties.getPropertyByName("cti-server-ip");
		CampaignRole campaign = objectMapper.readValue(request, CampaignRole.class);
		ctiURI = ctiURI.replace("CTI_SERVER", serverURL);
		ctiURI = ctiURI.replace("CAMPAIGN_NAME", (campaign.getCampaign() != null) ? campaign.getCampaign() : "");
		logger.info("calling URL " + ctiURI);
		String response = this.callUrl(ctiURI);// httpUtils.get(ctiURI);
		logger.info("URL " + ctiURI + " returned response " + response);
		// JSONObject responseObj = new JSONObject(response);
		// response = responseObj.get("response").toString();
		CampaignRole state = InputMapper.gson().fromJson(response, CampaignRole.class);
		CTIResponse ctiResponse = state.getResponse();
		if (ctiResponse.getResponse_code().equals(CUSTOM_API_SUCCESS)) {
			campaign.setResponse(ctiResponse);
			output.setResponse(campaign.toString());
		} else {
			output.setError(OutputResponse.GENERIC_FAILURE, ctiResponse.getReason(), ctiResponse.getStatus());
		}
		return output;
	}

	@Override
	public OutputResponse setCallDisposition(String request, String remoteAddr) throws IEMRException, JSONException, JsonMappingException, JsonProcessingException {
		OutputResponse output = new OutputResponse();
		ObjectMapper objectMapper = new ObjectMapper();
		CallDisposition disposition = objectMapper.readValue(request, CallDisposition.class);

		String agentID = (disposition.getAgent_id() != null) ? disposition.getAgent_id() : "";

		String agentIPResp = getAgentIP(agentID);
		String agentIP = !agentIPResp.equals(DEFAULT_IP) ? agentIPResp : remoteAddr;

		String ctiURI = ConfigProperties.getPropertyByName("update-call-disposition-URL");
		String serverURL = ConfigProperties.getPropertyByName("cti-server-ip");
		ctiURI = ctiURI.replace("CTI_SERVER", serverURL);
		ctiURI = ctiURI.replace("AGENT_ID", agentID);
		ctiURI = ctiURI.replace("AGENT_IP", agentIP);
		ctiURI = ctiURI.replace("CALL_SUB_TYPE",
				(disposition.getCust_disp() != null) ? disposition.getCust_disp() : "");
		ctiURI = ctiURI.replace("CALL_TYPE", (disposition.getCategory() != null) ? disposition.getCategory() : "");
		ctiURI = ctiURI.replace("SESSION_ID", (disposition.getSession_id() != null) ? disposition.getSession_id() : "");
		logger.info("calling URL " + ctiURI);
		String response = this.callUrl(ctiURI);// httpUtils.get(ctiURI);
		logger.info("URL " + ctiURI + " returned response " + response);
		CallDisposition state = InputMapper.gson().fromJson(response, CallDisposition.class);
		CTIResponse ctiResponse = state.getResponse();
		if (ctiResponse.getResponse_code().equals(CUSTOM_API_SUCCESS)) {
			disposition.setResponse(ctiResponse);
			output.setResponse(disposition.toString());
		} else {
			output.setError(OutputResponse.GENERIC_FAILURE, ctiResponse.getReason(), ctiResponse.getStatus());
		}
		return output;
	}

	@Override
	public OutputResponse createVoiceFile(String request, String remoteAddr) throws IEMRException, JSONException, JsonMappingException, JsonProcessingException {
		OutputResponse output = new OutputResponse();
		ObjectMapper objectMapper = new ObjectMapper();
		CTIVoiceFile disposition = objectMapper.readValue(request, CTIVoiceFile.class);
		String ctiURI = ConfigProperties.getPropertyByName("mix-voice-file-URL");
		String serverURL = ConfigProperties.getPropertyByName("cti-server-ip");
		ctiURI = ctiURI.replace("CTI_SERVER", serverURL);
		ctiURI = ctiURI.replace("AGENT_ID", (disposition.getAgent_id() != null) ? disposition.getAgent_id() : "");
		// ctiURI = ctiURI.replace("AGENT_IP", remoteAddr);
		ctiURI = ctiURI.replace("SESSION_ID", (disposition.getSession_id() != null) ? disposition.getSession_id() : "");
		logger.info("calling URL " + ctiURI);
		String response = this.callUrl(ctiURI);// httpUtils.get(ctiURI);
		logger.info("URL " + ctiURI + " returned response " + response);
		CTIVoiceFile result = objectMapper.readValue(response, CTIVoiceFile.class);
		CTIResponse ctiResponse = result.getResponse();
		if (ctiResponse.getResponse_code().equals(CUSTOM_API_SUCCESS)) {
			disposition.setResponse(ctiResponse);
			output.setResponse(disposition.toString());
		} else {
			output.setError(OutputResponse.GENERIC_FAILURE, ctiResponse.getReason(), ctiResponse.getStatus());
		}
		return output;
	}

	@Override
	public OutputResponse getVoiceFile(String request, String remoteAddr) throws IEMRException, JSONException, JsonMappingException, JsonProcessingException {
		OutputResponse output = new OutputResponse();
		ObjectMapper objectMapper = new ObjectMapper();
		CTIVoiceFile disposition = objectMapper.readValue(request, CTIVoiceFile.class);
		String ctiURI = ConfigProperties.getPropertyByName("get-voice-file-URL");
		String serverURL = ConfigProperties.getPropertyByName("cti-server-ip");
		ctiURI = ctiURI.replace("CTI_SERVER", serverURL);
		ctiURI = ctiURI.replace("AGENT_ID", (disposition.getAgent_id() != null) ? disposition.getAgent_id() : "");
		// ctiURI = ctiURI.replace("AGENT_IP", remoteAddr);
		ctiURI = ctiURI.replace("SESSION_ID", (disposition.getSession_id() != null) ? disposition.getSession_id() : "");
		logger.info("calling URL " + ctiURI);
		String response = this.callUrl(ctiURI);// httpUtils.get(ctiURI);
		logger.info("URL " + ctiURI + " returned response " + response);
		CTIVoiceFile result = objectMapper.readValue(response, CTIVoiceFile.class);
		CTIResponse ctiResponse = result.getResponse();
		if (ctiResponse.getResponse_code().equals(CUSTOM_API_SUCCESS)) {
			disposition.setResponse(ctiResponse);
			output.setResponse(disposition.toString());
		} else {
			output.setResponse("path not found");
			logger.info("URL " + ctiURI + " returned response " + response + " and saved response - path not found");;
//			output.setError(OutputResponse.GENERIC_FAILURE, ctiResponse.getReason(), ctiResponse.getStatus());
		}
		return output;
	}

	@Override
	public OutputResponse getVoiceFileNew(String request, String remoteAddr) throws IEMRException, JSONException {
		OutputResponse output = new OutputResponse();
		CTIVoiceFile disposition = InputMapper.gson().fromJson(request, CTIVoiceFile.class);
		String ctiURI = ConfigProperties.getPropertyByName("get-voice-file-URL-New");
		String serverURL = ConfigProperties.getPropertyByName("cti-server-ip");
		ctiURI = ctiURI.replace("CTI_SERVER", serverURL);
		ctiURI = ctiURI.replace("AGENT_ID", (disposition.getAgent_id() != null) ? disposition.getAgent_id() : "");
		// ctiURI = ctiURI.replace("AGENT_IP", remoteAddr);
		ctiURI = ctiURI.replace("SESSION_ID", (disposition.getSession_id() != null) ? disposition.getSession_id() : "");
		logger.info("calling URL " + ctiURI);
		String response = this.callUrl(ctiURI);// httpUtils.get(ctiURI);
		logger.info("URL " + ctiURI + " returned response " + response);

		if (response != null)
			output.setResponse(response);
		else {
			output.setResponse("path is null");
			logger.info("URL " + ctiURI + " returned response " + response + " and saved response - path is null");
		}
		return output;
	}

	@Override
	public OutputResponse disconnectCall(String request, String remoteAddr) throws IEMRException, JSONException, JsonMappingException, JsonProcessingException {
		OutputResponse output = new OutputResponse();
		String ctiDisconnectURL;
		ObjectMapper objectMapper = new ObjectMapper();
		AgentSkills agent = objectMapper.readValue(request, AgentSkills.class);

		String agentID = (agent.getAgentID() != null) ? agent.getAgentID() : "";
		String callID = (agent.getCallID() != null) ? agent.getCallID() : "";
		String agentIPResp = getAgentIP(agentID);
		String agentIP = !agentIPResp.equals(DEFAULT_IP) ? agentIPResp : remoteAddr;

		ctiDisconnectURL = ConfigProperties.getPropertyByName("disonnect-api-URL");
		String ctiServerIP = ConfigProperties.getPropertyByName("cti-server-ip");
		ctiDisconnectURL = ctiDisconnectURL.replace("cti_server", ctiServerIP).replace("AGENT_ID", agentID)
				.replace("SESSION_ID", callID).replace("AGENT_IP", agentIP)
				.replace("IS_FEEDBACK", agent.getIsFeedback() + "");

		// HttpUtils httpUtils = new HttpUtils();
		logger.info("Disconnect calls calling url: " + ctiDisconnectURL);
		String disconnectResponse = this.callUrl(ctiDisconnectURL);// httpUtils.get(ctiDisconnectURL);
		logger.info("Disonnect API returned " + disconnectResponse);
		AgentSkills result = objectMapper.readValue(disconnectResponse, AgentSkills.class);
		CTIResponse ctiResponse = result.getResponseObj();
		if (ctiResponse.getResponse_code().equals(CUSTOM_API_SUCCESS)) {
			agent.setResponse(ctiResponse);
			output.setResponse(agent.toString());
		} else {
			output.setError(OutputResponse.GENERIC_FAILURE, ctiResponse.getReason(), ctiResponse.getStatus());
		}
		return output;
	}

	@Override
	public OutputResponse switchToInbound(String request, String remoteAddr) throws IEMRException, JSONException, JsonMappingException, JsonProcessingException {
		OutputResponse output = new OutputResponse();
		String ctiDisconnectURL;
		ObjectMapper objectMapper = new ObjectMapper();
		CallBeneficiary agent = objectMapper.readValue(request, CallBeneficiary.class);

		String agentID = (agent.getAgent_id() != null) ? agent.getAgent_id() : "";

		String agentIPResp = getAgentIP(agentID);
		String agentIP = !agentIPResp.equals(DEFAULT_IP) ? agentIPResp : remoteAddr;

		ctiDisconnectURL = ConfigProperties.getPropertyByName("switch-to-inbound-URL");
		String ctiServerIP = ConfigProperties.getPropertyByName("cti-server-ip");
		ctiDisconnectURL = ctiDisconnectURL.replace("CTI_SERVER", ctiServerIP).replace("AGENT_ID", agentID)
				.replace("AGENT_IP", agentIP);

		// HttpUtils httpUtils = new HttpUtils();
		logger.info("switchToInbound calls calling url: " + ctiDisconnectURL);
		String disconnectResponse = this.callUrl(ctiDisconnectURL);// httpUtils.get(ctiDisconnectURL);
		logger.info("switchToInbound API returned " + disconnectResponse);
		CallBeneficiary result = objectMapper.readValue(disconnectResponse, CallBeneficiary.class);
		CTIResponse ctiResponse = result.getResponse();
		if (ctiResponse.getResponse_code().equals(STD_API_SUCCESS)) {
			agent.setResponse(ctiResponse);
			output.setResponse(agent.toString());
		} else {
			output.setError(OutputResponse.GENERIC_FAILURE, ctiResponse.getReason(), ctiResponse.getStatus());
		}
		return output;
	}

	@Override
	public OutputResponse switchToOutbound(String request, String remoteAddr) throws IEMRException, JSONException, JsonMappingException, JsonProcessingException {
		OutputResponse output = new OutputResponse();
		String ctiDisconnectURL;
		ObjectMapper objectMapper = new ObjectMapper();
		CallBeneficiary agent = objectMapper.readValue(request, CallBeneficiary.class);
		ctiDisconnectURL = ConfigProperties.getPropertyByName("switch-to-outbound-URL");
		String ctiServerIP = ConfigProperties.getPropertyByName("cti-server-ip");
		String agentID = (agent.getAgent_id() != null) ? agent.getAgent_id() : "";

		String agentIPResp = getAgentIP(agentID);
		String agentIP = !agentIPResp.equals(DEFAULT_IP) ? agentIPResp : remoteAddr;

		ctiDisconnectURL = ctiDisconnectURL.replace("CTI_SERVER", ctiServerIP).replace("AGENT_ID", agentID)
				.replace("AGENT_IP", agentIP);
		// HttpUtils httpUtils = new HttpUtils();
		logger.info("switchToInbound calls calling url: " + ctiDisconnectURL);
		String disconnectResponse = this.callUrl(ctiDisconnectURL);// httpUtils.get(ctiDisconnectURL);
		logger.info("switchToInbound API returned " + disconnectResponse);
		CallBeneficiary result = objectMapper.readValue(disconnectResponse, CallBeneficiary.class);
		CTIResponse ctiResponse = result.getResponse();
		if (ctiResponse.getResponse_code().equals(STD_API_SUCCESS)) {
			agent.setResponse(ctiResponse);
			output.setResponse(agent.toString());
		} else {
			output.setError(OutputResponse.GENERIC_FAILURE, ctiResponse.getReason(), ctiResponse.getStatus());
		}
		return output;
	}

	@Override
	public OutputResponse getAgentIPAddress(String request, String remoteAddr) throws IEMRException, JSONException, JsonMappingException, JsonProcessingException {
		OutputResponse output = new OutputResponse();
		ObjectMapper objectMapper = new ObjectMapper();
		AgentState agent = objectMapper.readValue(request, AgentState.class);
		String agentID = (agent.getAgent_id() != null) ? agent.getAgent_id() : "";
		String ctiDisconnectURL = ConfigProperties.getPropertyByName("get-agent-ip-address-URL");
		String ctiServerIP = ConfigProperties.getPropertyByName("cti-server-ip");
		ctiDisconnectURL = ctiDisconnectURL.replace("CTI_SERVER", ctiServerIP).replace("AGENT_ID", agentID);
		// HttpUtils httpUtils = new HttpUtils();
		logger.info("getAgentIPAddress calls calling url: " + ctiDisconnectURL);
		String disconnectResponse = this.callUrl(ctiDisconnectURL);// httpUtils.get(ctiDisconnectURL);
		logger.info("getAgentIPAddress API returned " + disconnectResponse);
		AgentState result = objectMapper.readValue(disconnectResponse, AgentState.class);
		CTIResponseTemp ctiResponse = result.getResponse();

		// CTIResponseTemp ctiResponse = getAgentIP(agentID);
		if (ctiResponse.getResponse_code().equals(STD_API_SUCCESS)) {
			agent.setResponse(ctiResponse);
			output.setResponse(agent.toString());
		} else {
			output.setError(OutputResponse.GENERIC_FAILURE, ctiResponse.getReason(), ctiResponse.getStatus());
		}
		return output;
	}

	@Override
	public String getAgentIP(String agentID) throws JsonMappingException, JsonProcessingException {
		String ctiDisconnectURL;
		String agentIP = DEFAULT_IP;
		ObjectMapper objectMapper = new ObjectMapper();
		ctiDisconnectURL = ConfigProperties.getPropertyByName("get-agent-ip-address-URL");
		String ctiServerIP = ConfigProperties.getPropertyByName("cti-server-ip");
		ctiDisconnectURL = ctiDisconnectURL.replace("CTI_SERVER", ctiServerIP).replace("AGENT_ID", agentID);
		// HttpUtils httpUtils = new HttpUtils();
		logger.info("getAgentIPAddress calls calling url: " + ctiDisconnectURL);
		String disconnectResponse = this.callUrl(ctiDisconnectURL);// httpUtils.get(ctiDisconnectURL);
		logger.info("getAgentIPAddress API returned " + disconnectResponse);
		AgentState result = objectMapper.readValue(disconnectResponse, AgentState.class);
		CTIResponseTemp ctiResponse = result.getResponse();
		if (ctiResponse.isSuccessResponse()) {
			agentIP = ctiResponse.getAgent_ip();
		}
		return agentIP;
	}

	@Override
	public OutputResponse blockNumber(String request, String remoteAddr) throws IEMRException, JSONException, JsonMappingException, JsonProcessingException {
		OutputResponse output = new OutputResponse();
		String blockNumberURL;
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		BlockUnblockNumber agent = objectMapper.readValue(request, BlockUnblockNumber.class);
		blockNumberURL = ConfigProperties.getPropertyByName("block-api-URL");
		String ctiServerIP = ConfigProperties.getPropertyByName("cti-server-ip");
		blockNumberURL = blockNumberURL.replace("CTI_SERVER", ctiServerIP)
				.replace("MOBILE", (agent.getPhoneNo() != null) ? agent.getPhoneNo() : "")
				.replace("CAMPAIGN_NAME", (agent.getCampaignName() != null) ? agent.getCampaignName() : "");
		// HttpUtils httpUtils = new HttpUtils();
		logger.info("blockNumber calls calling url: " + blockNumberURL);
		String unblockResponse = this.callUrl(blockNumberURL);// httpUtils.get(ctiDisconnectURL);
		logger.info("blockNumber API returned " + unblockResponse);
		BlockUnblockNumber result = objectMapper.readValue(unblockResponse, BlockUnblockNumber.class);
		CTIResponse ctiResponse = result.getResponse();
		if (ctiResponse.getResponse_code().equals(STD_API_SUCCESS)) {
			agent.setResponse(ctiResponse);
			output.setResponse(agent.toString());
		} else {
			output.setError(OutputResponse.GENERIC_FAILURE, ctiResponse.getReason(), ctiResponse.getStatus());
		}
		return output;
	}

	@Override
	public OutputResponse unblockNumber(String request, String remoteAddr) throws IEMRException, JSONException, JsonMappingException, JsonProcessingException {
		OutputResponse output = new OutputResponse();
		String unblockNumberURL;
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		BlockUnblockNumber agent = objectMapper.readValue(request, BlockUnblockNumber.class);
		unblockNumberURL = ConfigProperties.getPropertyByName("unblock-api-URL");
		String ctiServerIP = ConfigProperties.getPropertyByName("cti-server-ip");
		unblockNumberURL = unblockNumberURL.replace("CTI_SERVER", ctiServerIP)
				.replace("MOBILE", (agent.getPhoneNo() != null) ? agent.getPhoneNo() : "")
				.replace("CAMPAIGN_NAME", (agent.getCampaignName() != null) ? agent.getCampaignName() : "");
		// HttpUtils httpUtils = new HttpUtils();
		logger.info("blockNumber calls calling url: " + unblockNumberURL);
		String unblockResponse = this.callUrl(unblockNumberURL);// httpUtils.get(ctiDisconnectURL);
		logger.info("blockNumber API returned " + unblockResponse);
		BlockUnblockNumber result = objectMapper.readValue(unblockResponse, BlockUnblockNumber.class);
		CTIResponse ctiResponse = result.getResponse();
		if (ctiResponse.getResponse_code().equals(STD_API_SUCCESS)) {
			agent.setResponse(ctiResponse);
			output.setResponse(agent.toString());
		} else {
			output.setError(OutputResponse.GENERIC_FAILURE, ctiResponse.getReason(), ctiResponse.getStatus());
		}
		return output;
	}

	@Override
	public OutputResponse getAvailableAgentSkills(String request, String remoteAddr)
			throws IEMRException, JSONException, JsonMappingException, JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		OutputResponse output = new OutputResponse();
		String availableAgentsURL;
		AgentSkills agent = objectMapper.readValue(request, AgentSkills.class);
		availableAgentsURL = ConfigProperties.getPropertyByName("get-available-agents-URL");
		String ctiServerIP = ConfigProperties.getPropertyByName("cti-server-ip");
		availableAgentsURL = availableAgentsURL.replace("CTI_SERVER", ctiServerIP)
				.replace("SKILL", (agent.getSkill() != null) ? agent.getSkill() : "")
				.replace("CAMPAIGN_NAME", (agent.getCampaignName() != null) ? agent.getCampaignName() : "");
		// HttpUtils httpUtils = new HttpUtils();
		logger.info("getAvailableAgentSkills calls calling url: " + availableAgentsURL);
		String unblockResponse = this.callUrl(availableAgentsURL);// httpUtils.get(ctiDisconnectURL);
		logger.info("getAvailableAgentSkills API returned " + unblockResponse);
		AgentSkills result = objectMapper.readValue(unblockResponse, AgentSkills.class);
		CTIResponse ctiResponse = result.getResponseObj();
		if (ctiResponse.getResponse_code().equals(STD_API_SUCCESS)) {
			agent.setResponse(ctiResponse);
			output.setResponse(agent.toString());
		} else {
			output.setError(OutputResponse.GENERIC_FAILURE, ctiResponse.getReason(), ctiResponse.getStatus());
		}
		return output;
	}

	@Override
	public OutputResponse transferCall(String request, String remoteAddr) throws IEMRException, JSONException, JsonMappingException, JsonProcessingException {
		OutputResponse output = new OutputResponse();
		String availableAgentsURL;
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		TransferCall transferCall = objectMapper.readValue(request, TransferCall.class);

		if (transferCall != null && transferCall.getAgentIPAddress() != null)
			updateCallDisposition(transferCall, transferCall.getAgentIPAddress());

		String transfer_to = (transferCall !=null && transferCall.getTransfer_to() != null) ? transferCall.getTransfer_to().trim() : "";
		String transfer_from = (transferCall !=null && transferCall.getTransfer_from() != null) ? transferCall.getTransfer_from().trim() : "";
		String transfer_campaign_info = (transferCall !=null && transferCall.getTransfer_campaign_info() != null)
				? transferCall.getTransfer_campaign_info().trim()
				: "";
		String skill_transfer_flag = (transferCall !=null && transferCall.getSkill_transfer_flag() != null)
				? transferCall.getSkill_transfer_flag().trim()
				: "";
		String skill = (transferCall !=null && transferCall.getSkill() != null) ? transferCall.getSkill().trim() : "";
		if (transfer_to.length() != 0) {
			availableAgentsURL = ConfigProperties.getPropertyByName("call-transfer-to-agent-URL");
			// transfer_campaign_info = "";
			// skill_transfer_flag = "";
			// skill = "";
		} else {
			availableAgentsURL = ConfigProperties.getPropertyByName("call-transfer-to-campaign-URL");
			// transfer_from = "";
		}
		String ctiServerIP = ConfigProperties.getPropertyByName("cti-server-ip");
		availableAgentsURL = availableAgentsURL.replace("CTI_SERVER", ctiServerIP)
				.replace("TRANSFER_FROM", transfer_from).replace("TRANSFER_TO", transfer_to)
				.replace("CAMPAIGN_NAME", transfer_campaign_info).replace("SKILL_NAME", skill)
				.replace("SKILL_TRANSFER_FLAG", skill_transfer_flag).replace("AGENT_IP", remoteAddr);
		logger.info("transferCall calling url: " + availableAgentsURL);
		String unblockResponse = this.callUrl(availableAgentsURL);
		logger.info("transferCall API returned " + unblockResponse);
		TransferCall result = objectMapper.readValue(unblockResponse, TransferCall.class);
		CTIResponse ctiResponse = result.getResponseObj();
		if (ctiResponse.getResponse_code().equals(STD_API_SUCCESS)) {
			transferCall.setResponse(ctiResponse);
			output.setResponse(transferCall.toString());
		} else {
			output.setError(OutputResponse.GENERIC_FAILURE, ctiResponse.getReason(), ctiResponse.getStatus());
		}
		return output;
	}

	@Async
	private void updateCallDisposition(TransferCall transferCall, String agentIP) {
		try {
			CallType callTypeData = iemrCalltypeRepositoryImplCustom.getCallTypeDetails(transferCall.getCallTypeID());
			BeneficiaryCall callData = beneficiaryCallRepository.findCallDetails(transferCall.getBenCallID());
			JSONObject dispRequestObj = new JSONObject();
			dispRequestObj.put("session_id", callData.getCallID());
			dispRequestObj.put("cust_disp", callTypeData.getCallType());
			dispRequestObj.put("agent_id", callData.getAgentID());
			dispRequestObj.put("category", callTypeData.getCallGroupType());
			logger.info("call disposition log : " + ctiService
					.setCallDisposition(dispRequestObj.toString(), transferCall.getAgentIPAddress()).toString());

		} catch (Exception e) {
			logger.error("updateCallDisposition failed with error " + e.getMessage(), e);
		}
	}

	@Override
	public OutputResponse customerPreferredLanguage(CustomerLanguage custLang, String remoteAddress) throws JsonMappingException, JsonProcessingException {
		OutputResponse output = new OutputResponse();
		String preferredLanguageURL = ConfigProperties.getPropertyByName("preferred-language-URL");
		String campaignName = (custLang.getCampaign_name() != null) ? custLang.getCampaign_name().trim() : "";
		String languageName = (custLang.getLanguage() != null) ? custLang.getLanguage().trim() : "";
		String custPhoneNo = (custLang.getCust_ph_no() != null) ? custLang.getCust_ph_no().trim() : "";
		String actionName = (custLang.getAction() != null) ? custLang.getAction().trim() : "";
		String ctiServerIP = ConfigProperties.getPropertyByName("cti-server-ip");
		preferredLanguageURL = preferredLanguageURL.replace("CTI_SERVER", ctiServerIP)
				.replace("CAMPAIGN_NAME", campaignName).replace("LANGUAGE_NAME", languageName)
				.replace("CUSTOMER_PHONE", custPhoneNo).replace("ACTION_NAME", actionName);
		logger.info("customerPreferredLanguage calling url: " + preferredLanguageURL);
		String unblockResponse = this.callUrl(preferredLanguageURL);
		logger.info("customerPreferredLanguage API returned " + unblockResponse);
		ObjectMapper objectMapper = new ObjectMapper();
		CustomerLanguage result = objectMapper.readValue(unblockResponse, CustomerLanguage.class);
		CTIResponse ctiResponse = result.getResponse();
		if (ctiResponse.getResponse_code().equals(STD_API_SUCCESS)) {
			custLang.setResponse(ctiResponse);
			output.setResponse(custLang.toString());
		} else {
			output.setError(OutputResponse.GENERIC_FAILURE, ctiResponse.getReason(), ctiResponse.getStatus());
		}
		return output;
	}

	// public static void main(String[] args) throws JSONException,
	// IEMRException
	// {
	// String ctiURI = "";
	// String response = "{ \"response\": { \"transaction_id\":
	// \"CTI_GET_CAMPAIGN_SKILLS\" , \"response_code\": \"1\",
	// "
	// + "\"status\": \"SUCCESS\", \"campaign\" : UAT_1097_CO, \"skills\" :
	// {\"English\":\"1\",\"Hindi\":\"3\","
	// + "\"Kannada\":\"2\"}, \"reason\": \"\"} }";
	// System.out.println("URL " + ctiURI + " returned response " + response);
	// JSONObject responseObj = new JSONObject(response);
	// // response = responseObj.get("response").toString();
	// responseObj = new
	// JSONObject(responseObj.getJSONObject("response").toString());
	// System.out.println(responseObj.toString());
	// CampaignSkills state =
	// InputMapper.gson().fromJson(responseObj.toString(),
	// CampaignSkills.class);
	// // state = state.setStateObj();
	// System.out.println(state.toString());
	// }

	@Override
	public String callPostUrl(String urlRequest, String Json) {
		String result = httpUtils.post(urlRequest, Json);
		return result;
	}

	@Override
	public OutputResponse addAutoDialNumbers(String request, String ipAddress) throws JSONException, IEMRException, JsonMappingException, JsonProcessingException {
		OutputResponse result = new OutputResponse();
		logger.debug("addUpdateAgentSkills input is " + request);
		String ctiURI = ConfigProperties.getPropertyByName("add-auto-dail-numbers-URL");
		String serverURL = ConfigProperties.getPropertyByName("cti-server-ip");
		ObjectMapper objectMapper = new ObjectMapper();
		AutoPreviewDial[] autoPreviewDialArray = objectMapper.readValue(request, AutoPreviewDial[].class);

		AutoPreviewDial autoPreviewDial = autoPreviewDialArray[0];

		String agentID = (autoPreviewDial.getAgent_id() != null) ? autoPreviewDial.getAgent_id() : "";
		String agentIPResp = getAgentIP(agentID);
		String agentIP = !agentIPResp.equals(DEFAULT_IP) ? agentIPResp : ipAddress;

		ctiURI = ctiURI.replace("CTI_SERVER", serverURL);
		/*
		 * ctiURI = ctiURI.replace("AGENT_ID", agentID); ctiURI =
		 * ctiURI.replace("CAMP_NAME",(autoPreviewDial.getCampaign_name() == null ? "" :
		 * autoPreviewDial.getCampaign_name())); ctiURI = ctiURI.replace("MOBILE",
		 * (autoPreviewDial.getMobile() == null ? "" : autoPreviewDial.getMobile()));
		 * ctiURI = ctiURI.replace("AGENT_IP", agentIP);
		 */
		logger.info("calling URL " + ctiURI);

		JSONObject requestObject = new JSONObject();
		requestObject.put("xyz", request);

		String response = this.callPostUrl(ctiURI, requestObject.toString());// httpUtils.get(ctiURI);
		logger.info("URL " + ctiURI + " returned response " + response);
		AutoPreviewDial autoPreviewDialResponse = InputMapper.gson().fromJson(response, AutoPreviewDial.class);
		CTIResponse ctiResponse = autoPreviewDialResponse.getResponse();
		if (ctiResponse.getResponse_code().equals(CUSTOM_API_SUCCESS)) {
			autoPreviewDial.setResponse(ctiResponse);
			result.setResponse(autoPreviewDial.toString());
		} else {
			result.setError(OutputResponse.GENERIC_FAILURE, ctiResponse.getReason(), ctiResponse.getStatus());
		}
		return result;
	}

	@Override
	public OutputResponse setAutoDialNumbers(String request, String ipAddress) throws JSONException, IEMRException, JsonMappingException, JsonProcessingException {
		OutputResponse result = new OutputResponse();
		logger.debug("setAutoDialNumbers input is " + request);
		String ctiURI = ConfigProperties.getPropertyByName("set-auto-dail-numbers-URL");
		String serverURL = ConfigProperties.getPropertyByName("cti-server-ip");
		ObjectMapper objectMapper = new ObjectMapper();
		AutoPreviewDial autoPreviewDial = objectMapper.readValue(request, AutoPreviewDial.class);

		String agentID = (autoPreviewDial.getAgent_id() != null) ? autoPreviewDial.getAgent_id() : "";
		String agentIPResp = getAgentIP(agentID);
		String agentIP = !agentIPResp.equals(DEFAULT_IP) ? agentIPResp : ipAddress;

		ctiURI = ctiURI.replace("CTI_SERVER", serverURL);
		ctiURI = ctiURI.replace("AGENT_ID", agentID);
		ctiURI = ctiURI.replace("CAMP_NAME",
				(autoPreviewDial.getCamp_name() == null ? "" : autoPreviewDial.getCamp_name()));
		ctiURI = ctiURI.replace("MOBILE", (autoPreviewDial.getMobile() == null ? "" : autoPreviewDial.getMobile()));
		ctiURI = ctiURI.replace("AGENT_IP", agentIP);

		logger.info("calling URL " + ctiURI);

		String response = this.callUrl(ctiURI);// httpUtils.get(ctiURI);
		logger.info("URL " + ctiURI + " returned response " + response);
		if (response.contains("SUCCESS") || response.contains("success")) {
			result.setResponse("success");
		}

		// AutoPreviewDial autoPreviewDialResponse =
		// InputMapper.gson().fromJson(response, AutoPreviewDial.class);
		// CTIResponse ctiResponse = autoPreviewDialResponse.getResponse();
		// if (ctiResponse.getResponse_code().equals(CUSTOM_API_SUCCESS))
		// {
		// autoPreviewDial.setResponse(ctiResponse);
		// result.setResponse(autoPreviewDial.toString());
		// } else
		// {
		// result.setError(OutputResponse.GENERIC_FAILURE, ctiResponse.getReason(),
		// ctiResponse.getStatus());
		// }
		return result;
	}

	@Override
	public OutputResponse getIVRSPathDetails(String request, String remoteAddress) throws Exception {
		OutputResponse result = new OutputResponse();
		logger.debug("getZoneDetails input is " + request);
		String ctiURI = ConfigProperties.getPropertyByName("agent-ivrs-path-URL");
		String serverURL = ConfigProperties.getPropertyByName("cti-server-ip");
		ObjectMapper objectMapper = new ObjectMapper();
		AgentState zoneData = objectMapper.readValue(request, AgentState.class);

		String agentID = (zoneData.getAgent_id() != null) ? zoneData.getAgent_id() : "";
		ctiURI = ctiURI.replace("CTI_SERVER", serverURL);
		ctiURI = ctiURI.replace("AGENT_ID", agentID);

		logger.info("calling URL " + ctiURI);

		String response = this.callUrl(ctiURI);// httpUtils.get(ctiURI);
		logger.info("URL " + ctiURI + " returned response " + response);

		AgentState autoPreviewDialResponse = InputMapper.gson().fromJson(response, AgentState.class);
		CTIResponseTemp ctiResponse = autoPreviewDialResponse.getResponse();
		if (ctiResponse.getResponse_code().equals(CUSTOM_API_SUCCESS)) {
			String[] ivrsPath = ctiResponse.getIvrs_path().split("->");
			if (ivrsPath.length >= 3) {
				zoneData.setZoneName(ivrsPath[ivrsPath.length - 2]);
				zoneData.setIvrs_language(ivrsPath[ivrsPath.length - 3]);
			}
			zoneData.setResponse(ctiResponse);
			result.setResponse(zoneData.toString());
		} else {
			result.setError(OutputResponse.GENERIC_FAILURE, ctiResponse.getReason(), ctiResponse.getStatus());
		}

		return result;
	}
}
