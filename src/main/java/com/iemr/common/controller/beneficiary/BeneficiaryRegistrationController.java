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
package com.iemr.common.controller.beneficiary;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.iemr.common.data.beneficiary.BenPhoneMap;
import com.iemr.common.data.beneficiary.BeneficiaryRegistrationData;
import com.iemr.common.data.directory.Directory;
import com.iemr.common.model.beneficiary.BeneficiaryModel;
import com.iemr.common.service.beneficiary.BenRelationshipTypeService;
import com.iemr.common.service.beneficiary.BeneficiaryOccupationService;
import com.iemr.common.service.beneficiary.GovtIdentityTypeService;
import com.iemr.common.service.beneficiary.IEMRBeneficiaryTypeService;
import com.iemr.common.service.beneficiary.IEMRSearchUserService;
import com.iemr.common.service.beneficiary.RegisterBenificiaryService;
import com.iemr.common.service.beneficiary.SexualOrientationService;
import com.iemr.common.service.directory.DirectoryService;
import com.iemr.common.service.location.LocationService;
import com.iemr.common.service.reports.CallReportsService;
import com.iemr.common.service.userbeneficiarydata.CommunityService;
import com.iemr.common.service.userbeneficiarydata.EducationService;
import com.iemr.common.service.userbeneficiarydata.GenderService;
import com.iemr.common.service.userbeneficiarydata.LanguageService;
import com.iemr.common.service.userbeneficiarydata.MaritalStatusService;
import com.iemr.common.service.userbeneficiarydata.StatusService;
import com.iemr.common.service.userbeneficiarydata.TitleService;
import com.iemr.common.utils.CookieUtil;
import com.iemr.common.utils.JwtUtil;
import com.iemr.common.utils.mapper.InputMapper;
import com.iemr.common.utils.mapper.OutputMapper;
import com.iemr.common.utils.response.OutputResponse;

import io.lettuce.core.dynamic.annotation.Param;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;

@RequestMapping({ "/beneficiary" })
@RestController
public class BeneficiaryRegistrationController {

	private InputMapper inputMapper = new InputMapper();

	private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	private static final String AUTHORIZATION = "authorization";
	private RegisterBenificiaryService registerBenificiaryService;
	private IEMRBeneficiaryTypeService iemrBeneficiaryTypeService;
	private IEMRSearchUserService iemrSearchUserService;
	private EducationService educationService;
	private TitleService titleService;
	private StatusService statusService;
	private LocationService locationService;
	private GenderService genderService;
	private MaritalStatusService maritalStatusService;
	private CommunityService communityService;
	private DirectoryService directoryService;
	private SexualOrientationService sexualOrientationService;
	private LanguageService languageService;
	private BenRelationshipTypeService benRelationshipTypeService;
	private BeneficiaryOccupationService beneficiaryOccupationService;
	private GovtIdentityTypeService govtIdentityTypeService;


   	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	public void setBenRelationshipTypeService(BenRelationshipTypeService benRelationshipTypeService) {
		this.benRelationshipTypeService = benRelationshipTypeService;
	}

	@Autowired
	public void setBeneficiaryOccupationService(BeneficiaryOccupationService beneficiaryOccupationService) {
		this.beneficiaryOccupationService = beneficiaryOccupationService;
	}

	@Autowired
	public void setIEMRSearchUserService(IEMRSearchUserService iemrSearchUserService) {
		this.iemrSearchUserService = iemrSearchUserService;
	}

	@Autowired
	public void setIEMRBeneficiaryTypeService(IEMRBeneficiaryTypeService iemrBeneficiaryTypeService) {
		this.iemrBeneficiaryTypeService = iemrBeneficiaryTypeService;
	}

	@Autowired
	public void setRegisterBenificiaryService(RegisterBenificiaryService registerBenificiaryService) {
		this.registerBenificiaryService = registerBenificiaryService;
	}

	@Autowired
	public void setEducationService(EducationService educationService1) {
		this.educationService = educationService1;
	}

	@Autowired
	public void setTitleService(TitleService titleService1) {
		this.titleService = titleService1;
	}

	@Autowired
	public void setStatusService(StatusService statusService1) {
		this.statusService = statusService1;
	}

	@Autowired
	public void setLocationService(LocationService locationService1) {
		this.locationService = locationService1;
	}

	@Autowired
	public void setGenderService(GenderService genderService) {
		this.genderService = genderService;
	}

	@Autowired
	public void setMaritalStatusService(MaritalStatusService maritalStatusService) {
		this.maritalStatusService = maritalStatusService;
	}

	@Autowired
	public void setCommunityService(CommunityService communityService) {
		this.communityService = communityService;
	}

	@Autowired
	public void setLanguageService(LanguageService languageService) {
		this.languageService = languageService;
	}

	@Autowired
	public void setDirectoryService(DirectoryService directoryService) {
		this.directoryService = directoryService;
	}

	@Autowired
	public void setSexualOrientationService(SexualOrientationService sexualOrientationService) {
		this.sexualOrientationService = sexualOrientationService;
	}

	@Autowired
	public void setGovtIdentityTypeService(GovtIdentityTypeService govtIdentityTypeService) {
		this.govtIdentityTypeService = govtIdentityTypeService;
	}

	@Autowired
	private CallReportsService callReportsService;

	@Operation(summary = "Create a new beneficiary")
	@RequestMapping(value = "/create", method = RequestMethod.POST, produces = "application/json", consumes = "application/json", headers = "Authorization")

	public String createBeneficiary(
			@Param(value = "{\"providerServiceMapID\":\"Integer\",\"firstName\":\"String\",\"lastName\":\"String\",\"dOB\":\"Timestamp\","
					+ "\"ageUnits\":\"String\",\"fatherName\":\"String\",\"spouseName\":\"String\",\"govtIdentityNo\":\"String\","
					+ "\"govtIdentityTypeID\":\"Integer\",\"emergencyRegistration\":\"Boolean\",\"createdBy\":\"String\","
					+ "\"titleId\":\"Short\",\"statusID\":\"Short\",\"registeredServiceID\":\"Short\",\"maritalStatusID\":\"Short\","
					+ "\"genderID\":\"Integer\",\"i_bendemographics\":{\"educationID\":\"Long\",\"occupationID\":\"Integer\","
					+ "\"healthCareWorkerID\":\"Short\",\"communityID\":\"Integer\",\"districtID\":\"Integer\",\"stateID\":\"Integer\","
					+ "\"pinCode\":\"String\",\"blockID\":\"Integer\",\"districtBranchID\":\"Integer\",\"createdBy\":\"String\","
					+ "\"addressLine1\":\"String\"},\"benPhoneMaps\":{\"parentBenRegID\":\"Long\",\"phoneNo\":\"String\","
					+ "\"phoneTypeID\":\"Integer\",\"benRelationshipID\":\"Integer\",\"deleted\":\"Boolean\",\"createdBy\":\"String\"},"
					+ "\"faceEmbedding\":[\"Float\"]}") @RequestBody BeneficiaryModel beneficiaryModel,
			HttpServletRequest httpRequest) {
		OutputResponse response = new OutputResponse();

		logger.info("Create beneficiary request " + beneficiaryModel);
		try {

			response.setResponse(registerBenificiaryService.save(beneficiaryModel, httpRequest));
		} catch (Exception e) {
			logger.error("create beneficiary failed with error " + e.getMessage(), e);
			response.setError(e);
		}

		logger.info("create beneficiary response " + response.toString());
		return response.toString();
	}

	@Operation(summary = "Create a new beneficiary for customization")
	@RequestMapping(value = "/createBeneficiary", method = RequestMethod.POST, produces = "application/json", consumes = "application/json", headers = "Authorization")
	public String createBeneficiary(@RequestBody String request, HttpServletRequest httpRequest)
			throws JsonMappingException, JsonProcessingException {
		OutputResponse response = new OutputResponse();
		ObjectMapper objectMapper = new ObjectMapper();
		JsonElement json = JsonParser.parseString(request);
		String otherFields = checkExtraFields(json);
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		BeneficiaryModel beneficiaryModel = objectMapper.readValue(request, BeneficiaryModel.class);
		beneficiaryModel.setOtherFields(otherFields);

		logger.info("Create beneficiary request for customization " + beneficiaryModel);
		try {

			response.setResponse(registerBenificiaryService.save(beneficiaryModel, httpRequest));
		} catch (Exception e) {
			logger.error("create beneficiary failed with error " + e.getMessage(), e);
			response.setError(e);
		}

		logger.info("create beneficiary response for customization " + response.toString());
		return response.toString();
	}

	private String checkExtraFields(JsonElement json) {
		String identityJson = new Gson().toJson(json);
		JsonObject identityJsonObject = new Gson().fromJson(identityJson, JsonObject.class);
		JsonObject otherFieldsJson = new JsonObject();
		Set<String> beneficiaryFieldNames = new HashSet<>();
		for (Field field : BeneficiaryModel.class.getDeclaredFields()) {
			beneficiaryFieldNames.add(field.getName());
		}
		for (Map.Entry<String, JsonElement> entry : identityJsonObject.entrySet()) {
			String fieldName = entry.getKey();

			// Check if the field is not present in either class
			if (!beneficiaryFieldNames.contains(fieldName)) {
				otherFieldsJson.add(fieldName, entry.getValue());
			}
		}
		if (identityJsonObject.has("createdDate")) {
			otherFieldsJson.add("createdDate", identityJsonObject.get("createdDate"));
		}
		return otherFieldsJson.toString();
	}

	@Operation(summary = "Provide the list of beneficiaries based on beneficiary id")
	@RequestMapping(value = "/searchUserByID", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String searchUserByID(
			@Param(value = "{\"beneficiaryRegID\":\"Long\", \"beneficiaryID\":\"Long\", \"HealthID\":\"String\", \"HealthIDNo\":\"String\"} ") @RequestBody String request,
			HttpServletRequest httpRequest) {
		OutputResponse response = new OutputResponse();
		ObjectMapper objectMapper = new ObjectMapper();

		String auth = httpRequest.getHeader(AUTHORIZATION);
		logger.info("Search user by ID request " + request);
		try {
			Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
			BeneficiaryModel benificiaryDetails = gson.fromJson(request, BeneficiaryModel.class);
			logger.debug(benificiaryDetails.toString());
			List<BeneficiaryModel> iBeneficiary = null;
			if (benificiaryDetails.getBeneficiaryID() != null) {
				iBeneficiary = iemrSearchUserService.userExitsCheckWithId(benificiaryDetails.getBeneficiaryID(), auth,
						benificiaryDetails.getIs1097());
			} else if (benificiaryDetails.getBeneficiaryRegID() != null) {
				iBeneficiary = iemrSearchUserService.userExitsCheckWithId(benificiaryDetails.getBeneficiaryRegID(),
						auth, benificiaryDetails.getIs1097());
			} else if (benificiaryDetails.getHealthID() != null) {
				iBeneficiary = iemrSearchUserService.userExitsCheckWithHealthId_ABHAId(benificiaryDetails.getHealthID(),
						auth, benificiaryDetails.getIs1097());
			} else if (benificiaryDetails.getHealthIDNumber() != null) {
				iBeneficiary = iemrSearchUserService.userExitsCheckWithHealthIdNo_ABHAIdNo(
						benificiaryDetails.getHealthIDNumber(), auth, benificiaryDetails.getIs1097());
			} else if (benificiaryDetails.getFamilyId() != null) {
				iBeneficiary = iemrSearchUserService.userExitsCheckWithFamilyId(benificiaryDetails.getFamilyId(), auth,
						benificiaryDetails.getIs1097());
			} else if (benificiaryDetails.getIdentity() != null) {
				iBeneficiary = iemrSearchUserService.userExitsCheckWithGovIdentity(benificiaryDetails.getIdentity(),
						auth, benificiaryDetails.getIs1097());
			}
			setBeneficiaryGender(iBeneficiary);
			ObjectMapper mapper = new ObjectMapper();
			String result = mapper.writeValueAsString(iBeneficiary);
			response.setResponse(result);
			logger.info("Search user by ID response size "
					+ (iBeneficiary != null ? iBeneficiary.size() : "No Beneficiary Found"));
		} catch (Exception e) {
			logger.error("search user failed with error " + e.getMessage());
			response.setError(e);
		}
		return response.toString();
	}

	@Operation(summary = "Provide the list of beneficiaries based on phone number")
	@RequestMapping(value = "/searchUserByPhone", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String searchUserByPhone(
			@Param(value = "{\"phoneNo\":\"String\",\"pageNo\":\"Integer\",\"rowsPerPage\":\"Integer\"}") @RequestBody String request,
			HttpServletRequest httpRequest) {
		OutputResponse response = new OutputResponse();
		String auth = httpRequest.getHeader(AUTHORIZATION);
		logger.info("Serach user by phone no request " + request);
		try {
			JSONObject requestObj = new JSONObject(request);
			BenPhoneMap benPhoneMap = OutputMapper.gson().fromJson(request, BenPhoneMap.class);
			int pageNumber = requestObj.has("pageNo") ? (requestObj.getInt("pageNo") - 1) : 0;
			int rows = requestObj.has("rowsPerPage") ? requestObj.getInt("rowsPerPage") : 1000;
			if (requestObj.has("is1097") && requestObj.getBoolean("is1097") == true) {
				benPhoneMap.setIs1097(true);
			}

			logger.info("benef iciary request:" + benPhoneMap);
			response.setResponse(
					iemrSearchUserService.findByBeneficiaryPhoneNo(benPhoneMap, pageNumber, rows, auth).toString());
		} catch (JSONException e) {
			logger.error("serach user by phone NO failed with error " + e.getMessage(), e);
			response.setError(e);
		} catch (Exception e) {
			logger.error("serach user by phone NO failed with error " + e.getMessage(), e);
			response.setError(e);
		}

		return response.toString();
	}

	@Operation(summary = "Provide the list of beneficiaries using Elasticsearch")
	@RequestMapping(value = "/searchUser", method = RequestMethod.POST, headers = "Authorization")
	public String searchUser(
			@RequestBody String request,
			@RequestHeader(value = "Authorization", required = false) String auth, HttpServletRequest httpRequest) {
		
		OutputResponse response = new OutputResponse();
		
		try {
			logger.info("Universal search request received");
			
			JsonParser parser = new JsonParser();
			JsonObject requestObj = parser.parse(request).getAsJsonObject();
			
			String searchQuery = null;
			if (requestObj.has("search") && !requestObj.get("search").isJsonNull()) {
				searchQuery = requestObj.get("search").getAsString();
			}
			
			if (searchQuery == null || searchQuery.trim().isEmpty()) {
				response.setError(5000, "Search query is required");
				return response.toString();
			}
			
		   String jwtToken = CookieUtil.getJwtTokenFromCookie(httpRequest);
			String userId = jwtUtil.getUserIdFromToken(jwtToken);
            int userID=Integer.parseInt(userId);
			
			Boolean is1097 = false;
			if (requestObj.has("is1097") && !requestObj.get("is1097").isJsonNull()) {
				is1097 = requestObj.get("is1097").getAsBoolean();
			}
			
			logger.info("Searching with query: {}, userId: {}, is1097: {}", searchQuery, userID, is1097);
			
			String result = iemrSearchUserService.searchUser(searchQuery, userID, auth, is1097);
			
			if (result == null || result.trim().isEmpty()) {
				response.setError(5000, "No beneficiaries found");
				return response.toString();
			}
			
			return result;
			
		} catch (Exception e) {
			logger.error("Error in universal search: {}", e.getMessage(), e);
			response.setError(5000, "Error searching beneficiaries: " + e.getMessage());
			return response.toString();
		}
			}

	@Operation(summary = "Provide the list of beneficiaries based on search criteria")
	@RequestMapping(value = "/searchBeneficiary", method = RequestMethod.POST, headers = "Authorization")
	public String searchBeneficiary(
			@Param(value = "{\"firstName\":\"String\",\"lastName\":\"String\",\"genderID\":\"Integer\",\"beneficiaryID\":\"String\","
					+ "\"i_bendemographics\":{\"stateID\":\"Integer\",\"districtID\":\"Integer\",\"districtBranchID\":\"Integer\"}}") @RequestBody BeneficiaryModel request,
			HttpServletRequest httpRequest) {
		logger.info("searchBeneficiary request " + request);
		OutputResponse output = new OutputResponse();
		String auth = httpRequest.getHeader(AUTHORIZATION);
		try {
			output.setResponse(iemrSearchUserService.findBeneficiary(request, auth));
		} catch (NumberFormatException ne) {
			logger.error("searchBeneficiary failed with error " + ne.getMessage(), ne);
			output.setError(ne);
		} catch (Exception e) {
			logger.error("searchBeneficiary failed with error " + e.getMessage(), e);
			output.setError(e);
		}

		return output.toString();
	}

	@Operation(summary = "Provide all common data list needed for beneficiary registration")
	@RequestMapping(value = "/getRegistrationData", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String getRegistrationData() {
		OutputResponse response = new OutputResponse();
		logger.info("Received get user registration data request");
		try {
			BeneficiaryRegistrationData beneficiaryRegistrationData = new BeneficiaryRegistrationData();
			beneficiaryRegistrationData.setM_Status(statusService.getActiveStatus());
			beneficiaryRegistrationData.setM_Title(titleService.getActiveTitles());
			beneficiaryRegistrationData.setI_BeneficiaryEducation(educationService.getActiveEducations());
			beneficiaryRegistrationData.setStates(locationService.getStates(1));
			beneficiaryRegistrationData.setM_genders(genderService.getActiveGenders());
			beneficiaryRegistrationData.setM_maritalStatuses(maritalStatusService.getActiveMaritalStatus());
			beneficiaryRegistrationData.setM_communities(communityService.getActiveCommunities());
			beneficiaryRegistrationData.setM_language(languageService.getActiveLanguages());
			beneficiaryRegistrationData.setDirectory(directoryService.getDirectories());
			beneficiaryRegistrationData.setSexualOrientations(sexualOrientationService.getSexualOrientations());
			beneficiaryRegistrationData
					.setBenRelationshipTypes(benRelationshipTypeService.getActiveRelationshipTypes());
			beneficiaryRegistrationData.setBeneficiaryOccupations(beneficiaryOccupationService.getActiveOccupations());
			beneficiaryRegistrationData.setGovtIdentityTypes(govtIdentityTypeService.getActiveIDTypes());
			response.setResponse(beneficiaryRegistrationData.toString());
		} catch (Exception e) {
			response.setError(e);
			logger.error("get user registration data failed with error " + e.getMessage(), e);
		}
		logger.info("get user registration data response " + response.toString());
		return response.toString();
	}

	@Operation(summary = "Provide all common data V1 list needed for beneficiary registration")
	@RequestMapping(value = "/getRegistrationDataV1", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String getRegistrationDataV1(
			@Param(value = "{\"providerServiceMapID\":\"Integer\"}") @RequestBody String request) {
		OutputResponse response = new OutputResponse();
		logger.info("Received get user registration data request");
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			Directory instituteDirectory = objectMapper.readValue(request, Directory.class);
			BeneficiaryRegistrationData beneficiaryRegistrationData = new BeneficiaryRegistrationData();
			beneficiaryRegistrationData.setM_Status(statusService.getActiveStatus());
			beneficiaryRegistrationData.setM_Title(titleService.getActiveTitles());
			beneficiaryRegistrationData.setI_BeneficiaryEducation(educationService.getActiveEducations());
			beneficiaryRegistrationData.setStates(locationService.getStates(1));
			beneficiaryRegistrationData.setM_genders(genderService.getActiveGenders());
			beneficiaryRegistrationData.setM_maritalStatuses(maritalStatusService.getActiveMaritalStatus());
			beneficiaryRegistrationData.setM_communities(communityService.getActiveCommunities());
			beneficiaryRegistrationData.setM_language(languageService.getActiveLanguages());
			beneficiaryRegistrationData
					.setDirectory(directoryService.getDirectories(instituteDirectory.getProviderServiceMapID()));
			beneficiaryRegistrationData.setSexualOrientations(sexualOrientationService.getSexualOrientations());
			beneficiaryRegistrationData
					.setBenRelationshipTypes(benRelationshipTypeService.getActiveRelationshipTypes());
			beneficiaryRegistrationData.setBeneficiaryOccupations(beneficiaryOccupationService.getActiveOccupations());
			beneficiaryRegistrationData.setGovtIdentityTypes(govtIdentityTypeService.getActiveIDTypes());
			response.setResponse(beneficiaryRegistrationData.toString());
		} catch (Exception e) {
			response.setError(e);
			logger.error("get user registration data failed with error " + e.getMessage(), e);
		}
		logger.info("get user registration data response " + response.toString());
		return response.toString();
	}

	@Operation(summary = "Update beneficiary details")
	@RequestMapping(value = "/update", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String updateBenefciary(
			@Param(value = "{\"beneficiaryRegID\":\"Long\",\"firstName\":\"String\",\"lastName\":\"String\","
					+ "\"dOB\":\"Timestamp\",\"ageUnits\":\"String\",\"fatherName\":\"String\",\"spouseName\":\"String\","
					+ "\"govtIdentityNo\":\"String\",\"govtIdentityTypeID\":\"Integer\",\"emergencyRegistration\":\"Boolean\","
					+ "\"createdBy\":\"String\",\"titleId\":\"Short\",\"statusID\":\"Short\",\"registeredServiceID\":\"Short\","
					+ "\"maritalStatusID\":\"Short\",\"genderID\":\"Integer\",\"i_bendemographics\":{\"educationID\":\"Long\","
					+ "\"beneficiaryRegID\":\"Long\",\"occupationID\":\"Integer\",\"healthCareWorkerID\":\"Short\",\"incomeStatusID\":\"Integer\","
					+ "\"communityID\":\"Integer\",\"preferredLangID\":\"Integer\",\"districtID\":\"Integer\",\"stateID\":\"Integer\","
					+ "\"pinCode\":\"String\",\"blockID\":\"Integer\",\"districtBranchID\":\"Integer\",\"createdBy\":\"String\",\"addressLine1\":\"String\"},"
					+ "\"benPhoneMaps\":{\"parentBenRegID\":\"Long\",\"phoneNo\":\"String\",\"phoneTypeID\":\"Integer\",\"benRelationshipID\":\"Integer\","
					+ "\"deleted\":\"Boolean\",\"createdBy\":\"String\"},\"changeInSelfDetails\":\"Boolean\",\"changeInIdentities\":\"Boolean\","
					+ "\"changeInOtherDetails\":\"Boolean\",\"changeInAddress\":\"Boolean\",\"changeInContacts\":\"Boolean\","
					+ "\"changeInFamilyDetails\":\"Boolean\"}") @RequestBody String benificiaryRequest,
			HttpServletRequest httpRequest) {
		OutputResponse response = new OutputResponse();
		String auth = httpRequest.getHeader(AUTHORIZATION);
		Integer updateCount = 0;
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			BeneficiaryModel benificiaryDetails = objectMapper.readValue(benificiaryRequest, BeneficiaryModel.class);
			updateCount = registerBenificiaryService.updateBenificiary(benificiaryDetails, auth);
			if (updateCount > 0) {
				List<BeneficiaryModel> userExitsCheckWithId = iemrSearchUserService.userExitsCheckWithId(
						benificiaryDetails.getBeneficiaryRegID(), auth, benificiaryDetails.getIs1097());
				BeneficiaryModel beneficiaryModel = userExitsCheckWithId.get(0);
				JSONObject responseObj = new JSONObject(beneficiaryModel);
				responseObj.put("updateCount", updateCount);
				response.setResponse(responseObj.toString());
			}
		} catch (JSONException e) {
			logger.error("Update beneficiary failed with error " + e.getMessage(), e);
			response.setError(e);
		} catch (Exception e) {
			logger.error("Update beneficiary failed with error " + e.getMessage(), e);
			response.setError(e);
		}
		logger.info("update beneficiary response " + response.toString());
		return response.toString();
	}

	@Operation(summary = "Update beneficiary details")
	@RequestMapping(value = "/updateBenefciaryDetails", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String updateBenefciaryDetails(@RequestBody String benificiaryRequest, HttpServletRequest httpRequest) {
		OutputResponse response = new OutputResponse();
		String auth = httpRequest.getHeader(AUTHORIZATION);
		Integer updateCount = 0;
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			JsonElement json = JsonParser.parseString(benificiaryRequest);
			String otherFields = checkExtraFields(json);
			objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			BeneficiaryModel benificiaryDetails = objectMapper.readValue(benificiaryRequest, BeneficiaryModel.class);
			benificiaryDetails.setOtherFields(otherFields);
			updateCount = registerBenificiaryService.updateBenificiary(benificiaryDetails, auth);
			if (updateCount > 0) {
				List<BeneficiaryModel> userExitsCheckWithId = iemrSearchUserService.userExitsCheckWithId(
						benificiaryDetails.getBeneficiaryRegID(), auth, benificiaryDetails.getIs1097());
				BeneficiaryModel beneficiaryModel = userExitsCheckWithId.get(0);
				JSONObject responseObj = new JSONObject(beneficiaryModel);
				responseObj.put("updateCount", updateCount);
				response.setResponse(responseObj.toString());
			}
		} catch (JSONException e) {
			logger.error("Update beneficiary failed with error " + e.getMessage(), e);
			response.setError(e);
		} catch (Exception e) {
			logger.error("Update beneficiary failed with error " + e.getMessage(), e);
			response.setError(e);
		}
		logger.info("update beneficiary response " + response.toString());
		return response.toString();
	}

	@Operation(summary = "Fetch beneficiary details by phone no")
	@RequestMapping(value = "/getBeneficiariesByPhoneNo", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String getBeneficiariesByPhone(@Param(value = "{\"phoneNo\":\"String\"}") @RequestBody String request,
			HttpServletRequest httpRequest) {
		OutputResponse response = new OutputResponse();
		String auth = httpRequest.getHeader(AUTHORIZATION);
		logger.info("getBeneficiariesByPhoneNo request " + request);
		try {
			BenPhoneMap benPhoneMap = inputMapper.gson().fromJson(request, BenPhoneMap.class);
			int pageNumber = 0;
			int rows = 1000;
			response.setResponse(iemrSearchUserService.findByBeneficiaryPhoneNo(benPhoneMap, pageNumber, rows, auth));
		} catch (Exception e) {
			response.setError(e);
			logger.error("getBeneficiariesByPhoneNo failed with error " + e.getMessage(), e);
		}
		return response.toString();
	}

	@Operation(summary = "Update beneficiary community or education")
	@RequestMapping(value = "/updateCommunityorEducation", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String updateBenefciaryCommunityorEducation(
			@Param(value = "{\"beneficiaryRegID\":\"Long\",\"i_bendemographics\":{\"communityID\":\"Integer\","
					+ "\"educationID\":\"Integer\"}}") @RequestBody String benificiaryRequest,
			HttpServletRequest httpRequest) {
		OutputResponse response = new OutputResponse();
		ObjectMapper objectMapper = new ObjectMapper();
		String auth = httpRequest.getHeader(AUTHORIZATION);
		Integer updateCount = 0;
		try {
			BeneficiaryModel benificiaryDetails = objectMapper.readValue(benificiaryRequest, BeneficiaryModel.class);
			updateCount = registerBenificiaryService.updateCommunityorEducation(benificiaryDetails, auth);
			if (updateCount > 0) {
				List<BeneficiaryModel> userExitsCheckWithId = iemrSearchUserService.userExitsCheckWithId(
						benificiaryDetails.getBeneficiaryRegID(), auth, benificiaryDetails.getIs1097());
				JSONArray updatedUserData = new JSONArray(userExitsCheckWithId);
				JSONObject responseObj = new JSONObject(updatedUserData.getJSONObject(0).toString());
				responseObj.put("updateCount", updateCount);
				response.setResponse(responseObj.toString());
			}
		} catch (Exception e) {
			logger.error("Update beneficiary failed with error " + e.getMessage(), e);
			response.setError(e);
		}
		logger.info("update beneficiary response " + response.toString());
		return response.toString();
	}

	@Operation(summary = "Generate beneficiary id")
	@RequestMapping(value = "/generateBeneficiaryIDs", headers = "Authorization", method = {
			RequestMethod.POST }, produces = { "application/json" })
	public String getBeneficiaryIDs(
			@Param("{\"benIDRequired\":\"Integer\",\"vanID\":\"Integer\"}") @RequestBody String request,
			HttpServletRequest httpRequest) {
		logger.info("generateBeneficiaryIDs request " + request);
		OutputResponse response = new OutputResponse();
		try {

			String res = registerBenificiaryService.generateBeneficiaryIDs(request, httpRequest);
			response.setResponse(res);
			logger.info("generateBeneficiaryIDs response " + response.toString());
		} catch (Exception e) {
			logger.error(e.getMessage());
			response.setError(e);
		}
		return response.toString();
	}

	private void setBeneficiaryGender(List<BeneficiaryModel> iBeneficiary) {
		for (BeneficiaryModel beneficiaryModel : iBeneficiary) {
			if (null != beneficiaryModel.getM_gender() && beneficiaryModel.getM_gender().getGenderName() != null)
				beneficiaryModel.setGenderName(beneficiaryModel.getM_gender().getGenderName());
		}

	}
}
