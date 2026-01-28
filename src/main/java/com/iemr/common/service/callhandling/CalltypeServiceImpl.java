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
package com.iemr.common.service.callhandling;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iemr.common.data.callhandling.CallType;
import com.iemr.common.data.notification.Notification;
import com.iemr.common.repository.callhandling.IEMRCalltypeRepositoryImplCustom;
import com.iemr.common.utils.exception.IEMRException;
import com.iemr.common.utils.mapper.InputMapper;

@Service
public class CalltypeServiceImpl implements CalltypeService
{

	private Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());
	InputMapper inputMapper = new InputMapper();

	@Autowired
	private IEMRCalltypeRepositoryImplCustom iEMRCalltypeRepositoryImplCustom;

	@Override
	public List<CallType> getAllCalltypes(String request) throws IEMRException
	{
		CallType provider = inputMapper.gson().fromJson(request, CallType.class);
		List<CallType> callTypes = new ArrayList<CallType>();
		Set<Object[]> callTypesArray = new HashSet<Object[]>();
		if (provider.getIsInbound() != null && provider.getIsOutbound() != null)
		{
			callTypesArray = iEMRCalltypeRepositoryImplCustom.getCallTypes(provider.getProviderServiceMapID(),
					provider.getIsInbound(), provider.getIsOutbound());
		} else
		{
			callTypesArray = iEMRCalltypeRepositoryImplCustom.getCallTypes(provider.getProviderServiceMapID());
		}
		for (Object[] object : callTypesArray)
		{
			if (object != null && object.length >= 8)
			{
				callTypes.add(new CallType().createCallTypes((Integer) object[1], (String) object[0],
						(String) object[2], (String) object[3], (Boolean) object[4], (Boolean) object[5],
						(Boolean) object[6], (Boolean) object[7]));
			}
		}
		return callTypes;

	}

	@Override
	public String getAllCalltypesV1(String request) throws JSONException, IEMRException, JsonMappingException, JsonProcessingException
	{
		ObjectMapper objectMapper = new ObjectMapper();
		CallType provider = objectMapper.readValue(request, CallType.class);
		JSONArray callGroupTypes = new JSONArray();
		Set<Object[]> callTypesArray = new HashSet<Object[]>();
		if (provider.getIsInbound() != null && provider.getIsOutbound() != null)
		{
			callTypesArray = iEMRCalltypeRepositoryImplCustom.getCallTypes(provider.getProviderServiceMapID(),
					provider.getIsInbound(), provider.getIsOutbound());
		} else if (provider.getIsInbound() != null)
		{
			callTypesArray = iEMRCalltypeRepositoryImplCustom.getInboundCallTypes(provider.getProviderServiceMapID(),
					provider.getIsInbound());
		} else if (provider.getIsOutbound() != null)
		{
			callTypesArray = iEMRCalltypeRepositoryImplCustom.getOutboundCallTypes(provider.getProviderServiceMapID(),
					provider.getIsOutbound());
		} else
		{
			callTypesArray = iEMRCalltypeRepositoryImplCustom.getCallTypes(provider.getProviderServiceMapID());
		}
		
		for (Object[] object : callTypesArray)
		{
			if (object != null && object.length >= 8)
			{
				JSONObject callGroup = null;
				String callGroupType = (String) object[3];
				for (int callGrpIndex = 0; callGrpIndex < callGroupTypes.length(); callGrpIndex++)
				{
					if (callGroupType.equals(callGroupTypes.getJSONObject(callGrpIndex).getString("callGroupType")))
					{
						callGroup = callGroupTypes.getJSONObject(callGrpIndex);
						break;
					}
				}
				if (callGroup == null)
				{
					callGroup = new JSONObject();
					callGroup.put("callGroupType", callGroupType);
					callGroup.put("callTypes", new JSONArray());
					callGroupTypes.put(callGroup);
				}
				CallType callType = new CallType().createCallTypes((Integer) object[1], (String) object[0],
						(String) object[2], (String) object[3], (Boolean) object[4], (Boolean) object[5],
						(Boolean) object[6], (Boolean) object[7]);
				callGroup.getJSONArray("callTypes").put(new JSONObject(callType.toString()));
			}
		}
		return callGroupTypes.toString();

	}

	// @Override
	// public String updateCalltype(CallType callType) {
	//
	// iEMRCalltypeRepositoryImplCustom.updateCallType(new
	// Long(callType.getId()), callType.getCallType(),
	// callType.getRemarks());
	// // iEMRCalltypeRepositoryImplCustom.updateCallType(m_Calltype);
	// return "success";
	// }

}
