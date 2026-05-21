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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.iemr.common.data.cti.CustomerLanguage;
import com.iemr.common.utils.exception.IEMRException;
import com.iemr.common.utils.response.OutputResponse;

public interface CTIService
{
	String callUrl(String urlRequest);

	OutputResponse addUpdateAgentSkills(String request, String ipAddress) throws JSONException, IEMRException, JsonMappingException, JsonProcessingException;

	OutputResponse getCampaignSkills(String request, String ipAddress) throws IEMRException, JSONException, JsonMappingException, JsonProcessingException;

	OutputResponse getAgentState(String request, String ipAddress) throws JSONException, IEMRException, JsonMappingException, JsonProcessingException;

	OutputResponse getAgentCallStats(String request, String ipAddress) throws IEMRException, JSONException, JsonMappingException, JsonProcessingException;

	OutputResponse getCampaignNames(String request, String ipAddress) throws IEMRException, JSONException, JsonMappingException, JsonProcessingException;

	OutputResponse doAgentLogin(String request, String remoteAddr) throws IEMRException, JSONException, JsonMappingException, JsonProcessingException;

	OutputResponse getLoginKey(String request, String remoteAddr) throws IEMRException, JSONException, JsonMappingException, JsonProcessingException;

	OutputResponse agentLogout(String request, String remoteAddr) throws IEMRException, JSONException, JsonMappingException, JsonProcessingException;

	OutputResponse getOnlineAgents(String request, String remoteAddr) throws IEMRException, JSONException, JsonMappingException, JsonProcessingException;

	OutputResponse callBeneficiary(String request, String remoteAddr) throws IEMRException, JSONException, JsonMappingException, JsonProcessingException;

	OutputResponse addUpdateUserData(String request, String remoteAddr) throws IEMRException, JSONException, JsonMappingException, JsonProcessingException;

	OutputResponse getTransferCampaigns(String request, String remoteAddr) throws IEMRException, JSONException, JsonMappingException, JsonProcessingException;

	OutputResponse getCampaignRoles(String request, String remoteAddr) throws IEMRException, JSONException, JsonMappingException, JsonProcessingException;

	OutputResponse setCallDisposition(String request, String remoteAddr) throws IEMRException, JSONException, JsonMappingException, JsonProcessingException;

	OutputResponse createVoiceFile(String request, String remoteAddr) throws IEMRException, JSONException, JsonMappingException, JsonProcessingException;

	OutputResponse getVoiceFile(String request, String remoteAddr) throws IEMRException, JSONException, JsonMappingException, JsonProcessingException;

	OutputResponse disconnectCall(String request, String remoteAddr) throws IEMRException, JSONException, JsonMappingException, JsonProcessingException;

	OutputResponse switchToInbound(String request, String remoteAddr) throws IEMRException, JSONException, JsonMappingException, JsonProcessingException;

	OutputResponse switchToOutbound(String request, String remoteAddr) throws IEMRException, JSONException, JsonMappingException, JsonProcessingException;

	OutputResponse unblockNumber(String request, String remoteAddr) throws IEMRException, JSONException, JsonMappingException, JsonProcessingException;

	OutputResponse blockNumber(String request, String remoteAddr) throws IEMRException, JSONException, JsonMappingException, JsonProcessingException;

	OutputResponse getAgentIPAddress(String request, String remoteAddr) throws IEMRException, JSONException, JsonMappingException, JsonProcessingException;

	OutputResponse getAvailableAgentSkills(String request, String remoteAddr) throws IEMRException, JSONException, JsonMappingException, JsonProcessingException;

	OutputResponse transferCall(String request, String remoteAddr) throws IEMRException, JSONException, JsonMappingException, JsonProcessingException;

	String getAgentIP(String agentID) throws JsonMappingException, JsonProcessingException;

	OutputResponse customerPreferredLanguage(CustomerLanguage custLang, String remoteAddress) throws JsonMappingException, JsonProcessingException;

	OutputResponse addAutoDialNumbers(String request, String ipAddress) throws JSONException, IEMRException, JsonMappingException, JsonProcessingException;

	String callPostUrl(String urlRequest, String Json);

	OutputResponse setAutoDialNumbers(String request, String ipAddress) throws JSONException, IEMRException, JsonMappingException, JsonProcessingException;

	OutputResponse getIVRSPathDetails(String request, String remoteAddress)throws Exception;

	OutputResponse getVoiceFileNew(String request, String remoteAddr) throws IEMRException, JSONException;	

	OutputResponse getDispositionCount(String request, String ipAddress) throws IEMRException, JSONException, JsonMappingException, JsonProcessingException;
}
