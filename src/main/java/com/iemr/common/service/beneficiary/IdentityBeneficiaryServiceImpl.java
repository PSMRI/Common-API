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
package com.iemr.common.service.beneficiary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.iemr.common.dto.identity.BeneficiariesDTO;
import com.iemr.common.dto.identity.BeneficiariesPartialDTO;
import com.iemr.common.dto.identity.IdentityEditDTO;
import com.iemr.common.model.beneficiary.BeneficiaryGenModel;
import com.iemr.common.utils.config.ConfigProperties;
import com.iemr.common.utils.exception.IEMRException;
import com.iemr.common.utils.http.HttpUtils;
import com.iemr.common.utils.mapper.InputMapper;
import com.iemr.common.utils.mapper.OutputMapper;
import com.iemr.common.utils.response.OutputResponse;

@Service
public class IdentityBeneficiaryServiceImpl implements IdentityBeneficiaryService {

	// private InputMapper inputMapper = new InputMapper();

	Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	private static HttpUtils httpUtils = new HttpUtils();
	private InputMapper inputMapper = new InputMapper();
	@Value("${identity-api-url}")
	private String identityBaseURL;
	@Value("${identity-1097-api-url}")
	private String identity1097BaseURL;

	private static final String IDENTITY_BASE_URL = "IDENTITY_BASE_URL";

	private static final String BEN_GEN = ConfigProperties.getPropertyByName("genben-api");
	private static final String BEN_GEN_API_URL = ConfigProperties.getPropertyByName("generateBeneficiaryIDs-api-url");
	// public IdentityBeneficiaryServiceImpl()
	// {
	//
	//// if (urlRequest == null)
	//// {
	//// urlRequest = ConfigProperties.getPropertyByName("identity-api-url");
	//// }
	// }

	@Override
	// public List<Beneficiary> getBeneficiaryListByIDs() {// search by regID
	public List<BeneficiariesDTO> getBeneficiaryListByIDs(HashSet benIdList, String auth, Boolean is1097)
			throws IEMRException {
		List<BeneficiariesDTO> listBenDetailForOutboundDTO = new ArrayList<>();

		JsonParser parser = new JsonParser();

		String result;
		HashMap<String, Object> header = new HashMap<>();
		if (auth != null) {
			header.put("Authorization", auth);
		}


		result = httpUtils.post(ConfigProperties.getPropertyByName("identity-api-url-getByBenRegIdList").replace(
				IDENTITY_BASE_URL, (is1097 ? identity1097BaseURL : identityBaseURL)), benIdList.toString(), header);
		OutputResponse identityResponse = inputMapper.gson().fromJson(result, OutputResponse.class);
		if (identityResponse != null && identityResponse.getStatusCode() == OutputResponse.USERID_FAILURE) {
			throw new IEMRException(identityResponse.getErrorMessage());
		}
		if (null != result) {
			JsonObject responseObj = (JsonObject) parser.parse(result);
			// JsonArray data = (JsonArray) parser.parse(
			JsonObject data1 = (JsonObject) responseObj.get("response");
			String s = data1.get("data").getAsString();
			JsonArray responseArray = parser.parse(s).getAsJsonArray();

			// String data="s";
			// JsonArray responseArray = (JsonArray) parser.parse(data);

			for (JsonElement jsonElement : responseArray) {

				BeneficiariesDTO callRequest = inputMapper.gson().fromJson(jsonElement.toString(),
						BeneficiariesDTO.class);
				listBenDetailForOutboundDTO.add(callRequest);

			}
		}
		return listBenDetailForOutboundDTO;
	}

	@Override
	public List<BeneficiariesPartialDTO> getPartialBeneficiaryListByIDs(HashSet benIdList, String auth, Boolean is1097)
			throws IEMRException {
		// TODO Auto-generated method stub
		List<BeneficiariesPartialDTO> listBenDetailForOutboundDTO = new ArrayList<>();

		JsonParser parser = new JsonParser();

		String result;

		HashMap<String, Object> header = new HashMap<>();
		if (auth != null) {
			header.put("Authorization", auth);
		}
		result = httpUtils.post(
				ConfigProperties.getPropertyByName("identity-api-url-getByPartialBenRegIdList")
						.replace(IDENTITY_BASE_URL, (is1097 ? identity1097BaseURL : identityBaseURL)),
				benIdList.toString(), header);
		OutputResponse identityResponse = inputMapper.gson().fromJson(result, OutputResponse.class);
		if (identityResponse.getStatusCode() == OutputResponse.USERID_FAILURE) {
			throw new IEMRException(identityResponse.getErrorMessage());
		}
		JsonObject responseObj = (JsonObject) parser.parse(result);
		// JsonArray data = (JsonArray) parser.parse(
		JsonObject data1 = (JsonObject) responseObj.get("response");
		String s = data1.get("data").getAsString();
		JsonArray responseArray = parser.parse(s).getAsJsonArray();

		// String data="s";
		// JsonArray responseArray = (JsonArray) parser.parse(data);

		for (JsonElement jsonElement : responseArray) {

			BeneficiariesPartialDTO callRequest = inputMapper.gson().fromJson(jsonElement.toString(),
					BeneficiariesPartialDTO.class);
			listBenDetailForOutboundDTO.add(callRequest);
		}
		return listBenDetailForOutboundDTO;
	}

	@Override
	// search beneficiaries by phone number
	public List<BeneficiariesDTO> getBeneficiaryListByPhone(String phoneNo, String auth, Boolean is1097)
			throws IEMRException {

		List<BeneficiariesDTO> listBenDetailForOutboundDTO = new ArrayList<>();

		JsonParser parser = new JsonParser();

		String result;

		HashMap<String, Object> header = new HashMap<>();
		if (auth != null) {
			header.put("Authorization", auth);
		}
		result = httpUtils.post((ConfigProperties.getPropertyByName("identity-api-url-getByPhoneNum")
				.replace(IDENTITY_BASE_URL, (is1097 ? identity1097BaseURL : identityBaseURL))) + phoneNo, "", header);

		OutputResponse identityResponse = InputMapper.gson().fromJson(result, OutputResponse.class);
		if (identityResponse.getStatusCode() == OutputResponse.USERID_FAILURE) {
			throw new IEMRException(identityResponse.getErrorMessage());
		}
		JsonObject responseObj = (JsonObject) parser.parse(result);
		JsonObject data1 = (JsonObject) responseObj.get("response");
		String s = data1.get("data").getAsString();
		JsonArray responseArray = parser.parse(s).getAsJsonArray();

		for (JsonElement jsonElement : responseArray) {

			BeneficiariesDTO callRequest = InputMapper.gson().fromJson(jsonElement.toString(), BeneficiariesDTO.class);
			listBenDetailForOutboundDTO.add(callRequest);

		}
		return listBenDetailForOutboundDTO;
	}

	@Override
	// search beneficiary by beneficiary id
	public List<BeneficiariesDTO> getBeneficiaryListByBenID(String benId, String auth, Boolean is1097)
			throws IEMRException {

		List<BeneficiariesDTO> listBenDetailForOutboundDTO = new ArrayList<>();

		JsonParser parser = new JsonParser();

		String result;

		HashMap<String, Object> header = new HashMap<>();
		if (auth != null) {
			header.put("Authorization", auth);
		}
		result = httpUtils.post(ConfigProperties.getPropertyByName("identity-api-url-getByBenId")
				.replace(IDENTITY_BASE_URL, (is1097 ? identity1097BaseURL : identityBaseURL)) + benId, "", header);

		OutputResponse identityResponse = InputMapper.gson().fromJson(result, OutputResponse.class);
		if (identityResponse.getStatusCode() == OutputResponse.USERID_FAILURE) {
			throw new IEMRException(identityResponse.getErrorMessage());
		}
		JsonObject responseObj = (JsonObject) parser.parse(result);
		JsonObject data1 = (JsonObject) responseObj.get("response");
		String s = data1.get("data").getAsString();
		JsonArray responseArray = parser.parse(s).getAsJsonArray();

		for (JsonElement jsonElement : responseArray) {

			BeneficiariesDTO callRequest = InputMapper.gson().fromJson(jsonElement.toString(), BeneficiariesDTO.class);
			listBenDetailForOutboundDTO.add(callRequest);

		}
		return listBenDetailForOutboundDTO;
	}

	@Override
	// search beneficiary by beneficiary Reg id
	public List<BeneficiariesDTO> getBeneficiaryListByBenRegID(Long benRegId, String auth, Boolean is1097)
			throws IEMRException {

		List<BeneficiariesDTO> listBenDetailForOutboundDTO = new ArrayList<>();

		JsonParser parser = new JsonParser();
		String result;

		HashMap<String, Object> header = new HashMap<>();
		if (auth != null) {
			header.put("Authorization", auth);
		}
		result = httpUtils.post(ConfigProperties.getPropertyByName("identity-api-url-getByBenRegId")
				.replace(IDENTITY_BASE_URL, (is1097 ? identity1097BaseURL : identityBaseURL)) + benRegId, "", header);

		OutputResponse identityResponse = InputMapper.gson().fromJson(result, OutputResponse.class);
		if (identityResponse.getStatusCode() == OutputResponse.USERID_FAILURE) {
			throw new IEMRException(identityResponse.getErrorMessage());
		}
		JsonObject responseObj = (JsonObject) parser.parse(result);
		JsonObject data1 = (JsonObject) responseObj.get("response");
		String s = data1.get("data").getAsString();
		JsonArray responseArray = parser.parse(s).getAsJsonArray();

		for (JsonElement jsonElement : responseArray) {

			BeneficiariesDTO callRequest = InputMapper.gson().fromJson(jsonElement.toString(), BeneficiariesDTO.class);
			listBenDetailForOutboundDTO.add(callRequest);

		}
		return listBenDetailForOutboundDTO;
	}

	// search beneficiary by health id / ABHA address
	@Override
	public List<BeneficiariesDTO> getBeneficiaryListByHealthID_ABHAAddress(String healthID, String auth, Boolean is1097)
			throws IEMRException {

		List<BeneficiariesDTO> listBenDetailForOutboundDTO = new ArrayList<>();

		JsonParser parser = new JsonParser();
		String result;

		HashMap<String, Object> header = new HashMap<>();
		if (auth != null) {
			header.put("Authorization", auth);
		}
		result = httpUtils.post(ConfigProperties.getPropertyByName("identity-api-url-getByHealthID")
				.replace(IDENTITY_BASE_URL, (is1097 ? identity1097BaseURL : identityBaseURL)) + healthID, "", header);

		OutputResponse identityResponse = InputMapper.gson().fromJson(result, OutputResponse.class);
		if (identityResponse.getStatusCode() == OutputResponse.USERID_FAILURE) {
			throw new IEMRException(identityResponse.getErrorMessage());
		}
		JsonObject responseObj = (JsonObject) parser.parse(result);
		JsonObject data1 = (JsonObject) responseObj.get("response");
		String s = data1.get("data").getAsString();
		JsonArray responseArray = parser.parse(s).getAsJsonArray();

		for (JsonElement jsonElement : responseArray) {

			BeneficiariesDTO callRequest = InputMapper.gson().fromJson(jsonElement.toString(), BeneficiariesDTO.class);
			listBenDetailForOutboundDTO.add(callRequest);

		}
		return listBenDetailForOutboundDTO;
	}

	// search beneficiary by health id no / ABHA id no
	@Override
	public List<BeneficiariesDTO> getBeneficiaryListByHealthIDNo_ABHAIDNo(String healthIDNo, String auth,
			Boolean is1097) throws IEMRException {

		List<BeneficiariesDTO> listBenDetailForOutboundDTO = new ArrayList<>();

		JsonParser parser = new JsonParser();
		String result;

		HashMap<String, Object> header = new HashMap<>();
		if (auth != null) {
			header.put("Authorization", auth);
		}
		result = httpUtils.post(ConfigProperties.getPropertyByName("identity-api-url-getByHealthIDNo")
				.replace(IDENTITY_BASE_URL, (is1097 ? identity1097BaseURL : identityBaseURL)) + healthIDNo, "", header);

		OutputResponse identityResponse = InputMapper.gson().fromJson(result, OutputResponse.class);
		if (identityResponse.getStatusCode() == OutputResponse.USERID_FAILURE) {
			throw new IEMRException(identityResponse.getErrorMessage());
		}
		JsonObject responseObj = (JsonObject) parser.parse(result);
		JsonObject data1 = (JsonObject) responseObj.get("response");
		String s = data1.get("data").getAsString();
		JsonArray responseArray = parser.parse(s).getAsJsonArray();

		for (JsonElement jsonElement : responseArray) {

			BeneficiariesDTO callRequest = InputMapper.gson().fromJson(jsonElement.toString(), BeneficiariesDTO.class);
			listBenDetailForOutboundDTO.add(callRequest);

		}
		return listBenDetailForOutboundDTO;
	}

	// search beneficiary by Family ID
	@Override
	public List<BeneficiariesDTO> getBeneficiaryListByFamilyId(String familyId, String auth, Boolean is1097)
			throws IEMRException {

		List<BeneficiariesDTO> listBenDetailForOutboundDTO = new ArrayList<>();

		JsonParser parser = new JsonParser();
		String result;

		HashMap<String, Object> header = new HashMap<>();
		if (auth != null) {
			header.put("Authorization", auth);
		}
		result = httpUtils.post(ConfigProperties.getPropertyByName("identity-api-url-getByFamilyId")
				.replace(IDENTITY_BASE_URL, (is1097 ? identity1097BaseURL : identityBaseURL)) + familyId, "", header);

		OutputResponse identityResponse = InputMapper.gson().fromJson(result, OutputResponse.class);
		if (identityResponse.getStatusCode() == OutputResponse.USERID_FAILURE) {
			throw new IEMRException(identityResponse.getErrorMessage());
		}
		JsonObject responseObj = (JsonObject) parser.parse(result);
		JsonObject data1 = (JsonObject) responseObj.get("response");
		String s = data1.get("data").getAsString();
		JsonArray responseArray = parser.parse(s).getAsJsonArray();

		for (JsonElement jsonElement : responseArray) {

			BeneficiariesDTO callRequest = InputMapper.gson().fromJson(jsonElement.toString(), BeneficiariesDTO.class);
			listBenDetailForOutboundDTO.add(callRequest);

		}
		return listBenDetailForOutboundDTO;
	}

	// search beneficiary by Family ID
	@Override
	public List<BeneficiariesDTO> getBeneficiaryListByGovId(String identity, String auth, Boolean is1097)
			throws IEMRException {

		List<BeneficiariesDTO> listBenDetailForOutboundDTO = new ArrayList<>();

		JsonParser parser = new JsonParser();
		String result;

		HashMap<String, Object> header = new HashMap<>();
		if (auth != null) {
			header.put("Authorization", auth);
		}
		result = httpUtils.post(ConfigProperties.getPropertyByName("identity-api-url-getByGovIdentity")
				.replace(IDENTITY_BASE_URL, (is1097 ? identity1097BaseURL : identityBaseURL)) + identity, "", header);

		OutputResponse identityResponse = InputMapper.gson().fromJson(result, OutputResponse.class);
		if (identityResponse.getStatusCode() == OutputResponse.USERID_FAILURE) {
			throw new IEMRException(identityResponse.getErrorMessage());
		}
		JsonObject responseObj = (JsonObject) parser.parse(result);
		JsonObject data1 = (JsonObject) responseObj.get("response");
		String s = data1.get("data").getAsString();
		JsonArray responseArray = parser.parse(s).getAsJsonArray();

		for (JsonElement jsonElement : responseArray) {

			BeneficiariesDTO callRequest = InputMapper.gson().fromJson(jsonElement.toString(), BeneficiariesDTO.class);
			listBenDetailForOutboundDTO.add(callRequest);

		}
		return listBenDetailForOutboundDTO;
	}

	@Override
	public String getIdentityResponse(String request, String auth, Boolean is1097) throws IEMRException {
		String result;

		HashMap<String, Object> header = new HashMap<>();
		if (auth != null) {
			header.put("Authorization", auth);
		}
		String apiUrl = ConfigProperties.getPropertyByName("identity-api-url-benCreate")
				.replace(IDENTITY_BASE_URL, (is1097 ? identity1097BaseURL : identityBaseURL));

		logger.info("Calling URL: {}", apiUrl);
		logger.info("Request Payload: {}", request);
		result = httpUtils.post(apiUrl, request, header);

		if (result == null || result.isEmpty()) {
			logger.error("Empty response from Identity API");
			throw new IEMRException("No response received from Identity API");
		}

		try {
			OutputResponse identityResponse = inputMapper.gson().fromJson(result, OutputResponse.class);
			if (identityResponse.getStatusCode() == OutputResponse.USERID_FAILURE) {
				throw new IEMRException(identityResponse.getErrorMessage());
			}
		} catch (JsonSyntaxException e) {
			logger.error("JSON parsing error: {}", e.getMessage());
			throw new IEMRException("Invalid response format from Identity API");
		}

		return result;
	}


	public Integer editIdentityEditDTO(IdentityEditDTO identityEditDTO, String auth, Boolean is1097)
			throws IEMRException {
		JsonParser parser = new JsonParser();

		String result;
		String requestBody = OutputMapper.gsonWithoutExposeRestriction().toJson(identityEditDTO);
		HashMap<String, Object> header = new HashMap<>();
		if (auth != null) {
			header.put("Authorization", auth);
		}
		result = httpUtils.post(ConfigProperties.getPropertyByName("identity-api-url-benEdit")
				.replace(IDENTITY_BASE_URL, (is1097 ? identity1097BaseURL : identityBaseURL)), requestBody, header);

		OutputResponse identityResponse = inputMapper.gson().fromJson(result, OutputResponse.class);
		if (identityResponse.getStatusCode() == OutputResponse.USERID_FAILURE) {
			throw new IEMRException(identityResponse.getErrorMessage());
		}
		JsonObject responseObj = (JsonObject) parser.parse(result);
		// JsonArray data = (JsonArray) parser.parse(
		JsonObject data1 = (JsonObject) responseObj.get("response");
		String s = data1.get("data").getAsString();
		if (s.equalsIgnoreCase("Updated successfully")) {
			return 1;
		}

		return 0;

	}

	public List<BeneficiariesDTO> searchBeneficiaryList(String identitySearchDTO, String auth, Boolean is1097)
			throws IEMRException {
		List<BeneficiariesDTO> listBenDetailForOutboundDTO = new ArrayList<>();

		JsonParser parser = new JsonParser();
		String result;

		HashMap<String, Object> header = new HashMap<>();
		if (auth != null) {
			header.put("Authorization", auth);
		}
		result = httpUtils.post(ConfigProperties.getPropertyByName("identity-api-url-advancesearch").replace(
				IDENTITY_BASE_URL, (is1097 ? identity1097BaseURL : identityBaseURL)), identitySearchDTO, header);

		JsonObject responseObj = (JsonObject) parser.parse(result);
		// JsonArray data = (JsonArray) parser.parse(
		JsonObject data1 = (JsonObject) responseObj.get("response");
		String s = data1.get("data").getAsString();
		JsonArray responseArray = parser.parse(s).getAsJsonArray();

		// String data="s";
		// JsonArray responseArray = (JsonArray) parser.parse(data);

		for (JsonElement jsonElement : responseArray) {

			BeneficiariesDTO callRequest = inputMapper.gson().fromJson(jsonElement.toString(), BeneficiariesDTO.class);
			// logger.info("response from identity - neeraj" + callRequest.toString());
			listBenDetailForOutboundDTO.add(callRequest);

		}
		return listBenDetailForOutboundDTO;
	}

	@Override
	public Integer editIdentityEditDTOCommunityorEducation(IdentityEditDTO identityEditDTO, String auth, Boolean is1097)
			throws IEMRException {
		JsonParser parser = new JsonParser();

		String result;
		String requestBody = OutputMapper.gsonWithoutExposeRestriction().toJson(identityEditDTO);
		HashMap<String, Object> header = new HashMap<>();
		if (auth != null) {
			header.put("Authorization", auth);
		}
		result = httpUtils.post(ConfigProperties.getPropertyByName("identity-api-url-benEditEducationCommunity")
				.replace(IDENTITY_BASE_URL, (is1097 ? identity1097BaseURL : identityBaseURL)), requestBody, header);

		OutputResponse identityResponse = inputMapper.gson().fromJson(result, OutputResponse.class);
		if (identityResponse.getStatusCode() == OutputResponse.USERID_FAILURE) {
			throw new IEMRException(identityResponse.getErrorMessage());
		}
		JsonObject responseObj = (JsonObject) parser.parse(result);
		// JsonArray data = (JsonArray) parser.parse(
		JsonObject data1 = (JsonObject) responseObj.get("response");
		String s = data1.get("data").getAsString();
		if (s.equalsIgnoreCase("Updated successfully")) {
			return 1;
		}

		return 0;

	}

	@Override
	public List<BeneficiaryGenModel> generateBeneficiaryIDs(String request, String auth) throws IEMRException {

		String result;
		JsonParser parser = new JsonParser();
		HashMap<String, Object> header = new HashMap<>();
		if (auth != null) {
			header.put("Authorization", auth);
		}
		result = httpUtils.post(BEN_GEN + BEN_GEN_API_URL, request, header);

		OutputResponse identityResponse = inputMapper.gson().fromJson(result, OutputResponse.class);

		if (identityResponse.getStatusCode() == OutputResponse.USERID_FAILURE) {
			throw new IEMRException(identityResponse.getErrorMessage());
		}
		JsonObject responseObj = (JsonObject) parser.parse(result);
		JsonObject data1 = (JsonObject) responseObj.get("response");
		String s = data1.get("data").getAsString();
		JsonArray responseArray = parser.parse(s).getAsJsonArray();

		List<BeneficiaryGenModel> listBen = new ArrayList<BeneficiaryGenModel>();
		for (JsonElement jsonElement : responseArray) {

			BeneficiaryGenModel beneficiaryGenModel = inputMapper.gson().fromJson(jsonElement.toString(),
					BeneficiaryGenModel.class);
			listBen.add(beneficiaryGenModel);

		}
		return listBen;
	}

}
