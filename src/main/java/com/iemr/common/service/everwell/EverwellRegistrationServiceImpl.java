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
package com.iemr.common.service.everwell;

//import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.iemr.common.data.beneficiary.BenPhoneMap;
import com.iemr.common.data.everwell.EverwellDetails;
import com.iemr.common.data.everwell.EverwellRegistration1097Identity;
import com.iemr.common.model.beneficiary.BeneficiaryDemographicsModel;
import com.iemr.common.model.user.LoginRequestModel;
import com.iemr.common.repository.everwell.EverwellFetchAndSync;
import com.iemr.common.repository.location.LocationDistrictRepository;
import com.iemr.common.repository.location.LocationStateRepository;
import com.iemr.common.utils.CryptoUtil;
import com.iemr.common.utils.RestTemplateUtil;
import com.iemr.common.utils.mapper.InputMapper;
import com.iemr.common.utils.response.OutputResponse;

@Service
@PropertySource("classpath:application.properties")
public class EverwellRegistrationServiceImpl implements EverwellRegistrationService {
	@Autowired
	private CryptoUtil cryptoUtil;

	private InputMapper inputMapper = new InputMapper();

	Logger logger = LoggerFactory.getLogger(this.getClass().getName());

//	@Autowired
//	private everwellBenRegRepo everwellBenRegRepo;
	@Autowired
	private EverwellFetchAndSync everwellFetchAndSync;
	@Autowired
	private LocationDistrictRepository locationDistrictRepository;

	@Autowired
	private LocationStateRepository locationStateRepository;

	@Value("${everwellGetPatientAdherenceUrl}")
	private String everwellGetPatientAdherenceUrl;

	@Value("${everwellEditDoses}")
	private String everwellEditDoses;

	@Value("${everwellAddSupportAction}")
	private String everwellAddSupportAction;

	@Value("${everwelluserAuthenticate}")
	private String everwelluserAuthenticate;

	@Value("${everwellRegisterBenficiary}")
	private String everwellRegisterBenficiary;

	@Value("${everwellVanID}")
	private Integer everwellVanID;

	@Value("${everwellProviderServiceMapID}")
	private Integer everwellProviderServiceMapID;

	@Value("${everwellgovtIdentityNo}")
	private Integer everwellgovtIdentityNo;

	@Value("${everwellgovtIdentityTypeID}")
	private Integer everwellgovtIdentityTypeID;

	@Value("${everwellmaritalStatusID}")
	private Integer everwellmaritalStatusID;

	@Value("${everwellbenRelationshipID}")
	private Integer everwellbenRelationshipID;

	@Value("${everwell1097userAuthenticate}")
	private String everwell1097userAuthenticate;

	@Value("${everwellUserName}")
	private String everwellUserName;

	@Value("${everwellPassword}")
	private String everwellPassword;

	@Value("${amritUserName}")
	private String amritUserName;

	@Value("${amritPassword}")
	private String amritPassword;

	private static String EVERWELL_AUTH_TOKEN;
	private static Long EVERWEll_TOKEN_EXP;

	/*
	 * Created By DU20091017 register the everwell patient into AMRIT platform
	 * 
	 */
	@Override
	public void registerBeneficiary() {
		OutputResponse response1 = new OutputResponse();
		String Authorization = "";
//		String EVERWELL_AUTH_TOKEN = "";
		String registeringUser = "";
		RestTemplate restTemplateLogin = new RestTemplate();
		RestTemplate restTemplateEverwellLogin = new RestTemplate();

		try {

			// 1097 user authentication
			String amritUser = amritUserName;
			String amritPass = amritPassword;
			LoginRequestModel loginCredentials1097 = new LoginRequestModel(amritUser, amritPass);

			MultiValueMap<String, String> headersLogin = new LinkedMultiValueMap<String, String>();
			headersLogin.add("Content-Type", "application/json");
			// headersLogin.add("AUTHORIZATION", "");

			logger.info("AMRIT login request OBj " + loginCredentials1097.toString());

			HttpEntity<Object> requestLogin = new HttpEntity<Object>(loginCredentials1097, headersLogin);
			ResponseEntity<String> responseLogin = restTemplateLogin.exchange(everwell1097userAuthenticate,
					HttpMethod.POST, requestLogin, String.class);
			if (responseLogin.getStatusCodeValue() == 200 & responseLogin.hasBody()) {
				String responseStrLogin = responseLogin.getBody();
				JsonObject jsnOBJ = new JsonObject();
				JsonParser jsnParser = new JsonParser();
				JsonElement jsnElmnt = jsnParser.parse(responseStrLogin);
				jsnOBJ = jsnElmnt.getAsJsonObject();
				JsonObject everwellLoginJsonData = jsnOBJ.getAsJsonObject("data");
				Authorization = everwellLoginJsonData.get("key").getAsString();
				registeringUser = everwellLoginJsonData.get("userName").getAsString();

			}

			if (EVERWELL_AUTH_TOKEN != null && EVERWEll_TOKEN_EXP != null
					&& EVERWEll_TOKEN_EXP > System.currentTimeMillis()) {
				// no need of calling auth API
			} else {
				// everwell user authenticate API call;
				MultiValueMap<String, String> everwellUserLogin = new LinkedMultiValueMap<String, String>();
				everwellUserLogin.add("username", everwellUserName);
				everwellUserLogin.add("password", everwellPassword);
				everwellUserLogin.add("grant_type", "password");

				logger.info("Everwell user authenticate API request Obj " + everwellUserLogin);

				HttpHeaders everwellHeader = new HttpHeaders();
				everwellHeader.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
				everwellHeader.add("user-agent",
						"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");

				HttpEntity<Object> requestEverwellLogin = new HttpEntity<Object>(everwellUserLogin, everwellHeader);
				ResponseEntity<String> responseEverwellLogin = restTemplateEverwellLogin
						.exchange(everwelluserAuthenticate, HttpMethod.POST, requestEverwellLogin, String.class);
				if (responseEverwellLogin.getStatusCodeValue() == 200 & responseEverwellLogin.hasBody()) {
					String responseStrLogin = responseEverwellLogin.getBody();
					JsonObject jsnOBJ = new JsonObject();
					JsonParser jsnParser = new JsonParser();
					JsonElement jsnElmnt = jsnParser.parse(responseStrLogin);
					jsnOBJ = jsnElmnt.getAsJsonObject();
					EVERWELL_AUTH_TOKEN = jsnOBJ.get("token_type").getAsString() + " "
							+ jsnOBJ.get("access_token").getAsString();

					Date date = new Date();
					java.sql.Date sqlDate = new java.sql.Date(date.getTime());
					Calendar everwellCalendar = Calendar.getInstance();
					everwellCalendar.setTime(sqlDate);
					everwellCalendar.add(Calendar.DATE, 29);
					Date everwellTokenEndDate = everwellCalendar.getTime();

					EVERWEll_TOKEN_EXP = everwellTokenEndDate.getTime();
				}
			}

//			List<EverwellDetails> savedEverwellData = everWellDataSave(EVERWELL_AUTH_TOKEN, registeringUser,
//					Authorization);
			int count = 3;
			while (count > 0) {
				List<EverwellDetails> savedEverwellData = everWellDataSave(EVERWELL_AUTH_TOKEN, registeringUser,
						Authorization);
				if (savedEverwellData != null)
					break;
				else
					count--;
			}
		} catch (Exception e) {
			logger.info("error in saving and registering user" + e);
			response1.setError(e);
		}

	}

	// save everwell data into t_everwellapi
	public List<EverwellDetails> everWellDataSave(String everwellAuthorization, String registeringUser,
			String Authorization) {
		int count = 0;
		List<EverwellDetails> everwellBenRegListAS = null;

		try {
			// calling the get adherence API for multiple pages.
			while (count >= 0) {
				RestTemplate restTemplate = new RestTemplate();

				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
				headers.add("user-agent",
						"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");
				headers.add("AUTHORIZATION", everwellAuthorization);
				HttpEntity<Object> request = new HttpEntity<Object>(headers);
				ResponseEntity<String> response = restTemplate.exchange(everwellGetPatientAdherenceUrl + count,
						HttpMethod.GET, request, String.class);
				if (response.getStatusCodeValue() == 200 & response.hasBody()) {

					logger.info("get pateint adherence details response " + response.getBody());

					String responseStr = response.getBody();
					JsonObject jsnOBJ = new JsonObject();
					JsonParser jsnParser = new JsonParser();
					JsonElement jsnElmnt = jsnParser.parse(responseStr);
					jsnOBJ = jsnElmnt.getAsJsonObject();

					// checking whether we get he records or not.
					if (Integer.parseInt(jsnOBJ.get("TotalRecords").toString()) > 0) {
						EverwellDetails[] everwellBenReg = InputMapper.gson().fromJson(jsnOBJ.get("Data").toString(),
								EverwellDetails[].class);
						List<EverwellDetails> everwellBenRegList = Arrays.asList(everwellBenReg);

						for (EverwellDetails obj : everwellBenRegList) {
							if (obj.getNoInfoDoseDates() != null && (obj.getNoInfoDoseDates()).length > 0) {
								String res = "";
								for (int i = 0; i < (obj.getNoInfoDoseDates()).length; i++) {
									if (i != (obj.getNoInfoDoseDates()).length - 1)
										res = res + (obj.getNoInfoDoseDates())[i] + "||";
									else
										res = res + (obj.getNoInfoDoseDates())[i];
								}
								if (!res.equals("") && res != null)
									obj.setNoInfoDosesDates(res);
							}
							// setting the language of pateint according to state.
							String stateLanguage = locationStateRepository.getStateLanguage(obj.getState());
							if (stateLanguage != null)
								obj.setPreferredLanguageName(stateLanguage);
							else
								obj.setPreferredLanguageName("Hindi");

							obj.setCreatedBy(registeringUser);
							obj.setProcessed("N");
							obj.setIsAllocated(false);
							obj.setCallCounter(0);
							obj.setRetryNeeded(true);
						}
						// saving the data in t_everwellAPI table
						everwellBenRegListAS = (List<EverwellDetails>) everwellFetchAndSync.saveAll(everwellBenRegList);

						if (everwellBenRegListAS != null) {

							registerEverWellPatient(everwellBenRegListAS, Authorization);
							logger.info("data saved successfully - size" + everwellBenRegListAS.size());
							count++;
						} else
							logger.info("data not saved successfully in t_everwellAPI");
					} else {
						logger.info("no records found to insert in t_everwellapi for page = " + count);
						count = -1;
					}

				}
			}

		} catch (Exception e) {
			logger.info("error in saving data into t_everwellAPI " + e);
		}
		logger.info("this everwelldataSave method is excuting");
		return everwellBenRegListAS;
	}

	// register the saved everwell data into 1097 identity and update in
	// t_everwellAPI table;
	public String registerEverWellPatient(List<EverwellDetails> everwellBenRegListAS, String Authorization) {
		Integer failUserCount = 0;
		String everwellRegistration = "Failure";
		List<EverwellDetails> userDetailsRegister = everwellBenRegListAS;
		for (EverwellDetails obj : userDetailsRegister) {

			// checking the user is already existing or not
			ArrayList<Object[]> registrationStatus = everwellFetchAndSync.registrationStatus(obj.getId());
			if (Integer.parseInt(registrationStatus.get(0)[0].toString()) > 0
					&& registrationStatus.get(0)[1].equals(true)) {
				obj.setBeneficiaryRegId(Long.parseLong(registrationStatus.get(0)[2].toString()));
				obj.setProviderServiceMapId(Integer.parseInt(registrationStatus.get(0)[3].toString()));
				obj.setVanId(Integer.parseInt(registrationStatus.get(0)[4].toString()));
				obj.setBeneficiaryID(Long.parseLong(registrationStatus.get(0)[5].toString()));
				obj.setIsRegistered(true);
				everwellRegistration = "Success";
				// System.out.println("true case -- patient is aleardy registered with AMRIT.");
			} else {
				try {
					ArrayList<BenPhoneMap> benPhoneMaps = new ArrayList<BenPhoneMap>();
					benPhoneMaps.add(new BenPhoneMap(everwellbenRelationshipID, obj.getPrimaryNumber(),
							obj.getCreatedBy(), false));

					Integer genderID = 0;
					if (obj.getGender().equalsIgnoreCase("male")) {
						genderID = 1;
					} else if (obj.getGender().equalsIgnoreCase("female")) {
						genderID = 2;
					} else if (obj.getGender().equalsIgnoreCase("Transgender")) {
						genderID = 3;
					}

					Integer stateID = null;
					Integer districtID = null;
					Integer StateIDFromRepo = locationStateRepository.getStateID(obj.getState());
					Integer districtIDFromRepo = locationDistrictRepository.getDistrictID(obj.getDistrict());
					if (StateIDFromRepo != null) {
						stateID = StateIDFromRepo;
					}
					if (districtIDFromRepo != null) {
						districtID = districtIDFromRepo;
					}

					BeneficiaryDemographicsModel i_bendemographics = new BeneficiaryDemographicsModel(3, stateID,
							districtID, "452046", false);
					// 452046 = pincode, 3 =
					// communtiy ID

					EverwellRegistration1097Identity everwell1097 = new EverwellRegistration1097Identity(everwellVanID,
							everwellProviderServiceMapID, obj.getFirstName(), obj.getLastName(), genderID, 2,
							everwellmaritalStatusID, benPhoneMaps, everwellgovtIdentityNo, everwellgovtIdentityTypeID,
							false, obj.getCreatedBy(), 1, i_bendemographics, true);
					// here 1= statusID, 2 = titleId

					String data = new Gson().toJson(everwell1097).toString();

					logger.info("registration RequestObj" + data);

					RestTemplate restTemplate = new RestTemplate();

					HttpEntity<Object> request = RestTemplateUtil.createRequestEntity(data, Authorization);
					// registering the everwell patient into AMRIT
					ResponseEntity<String> response = restTemplate.exchange(everwellRegisterBenficiary, HttpMethod.POST,
							request, String.class);

					if (response.getStatusCodeValue() == 200 & response.hasBody()) {

						logger.info("registration api response " + response.getBody());

						String responseStr = response.getBody();
						JsonObject jsnOBJ = new JsonObject();
						JsonParser jsnParser = new JsonParser();
						JsonElement jsnElmnt = jsnParser.parse(responseStr);
						jsnOBJ = jsnElmnt.getAsJsonObject();
						JsonObject everwellJsonData = jsnOBJ.getAsJsonObject("data");

						obj.setBeneficiaryRegId(Long.parseLong(everwellJsonData.get("beneficiaryRegID").getAsString()));
						obj.setVanId(Integer.parseInt(everwellJsonData.get("vanID").getAsString()));
						obj.setProviderServiceMapId(
								Integer.parseInt(everwellJsonData.get("providerServiceMapID").getAsString()));
						obj.setBeneficiaryID(Long.parseLong(everwellJsonData.get("beneficiaryID").getAsString()));
						obj.setIsRegistered(true);

					}
				} catch (Exception e) {
					logger.info("error in registering Everwell user " + e);
					continue;
				}
			}

		}
		// updating the beneficiary data in t_everwellAPI
		ArrayList<EverwellDetails> everwellBenRegAF = (ArrayList<EverwellDetails>) everwellFetchAndSync
				.saveAll(userDetailsRegister);
		if (everwellBenRegAF != null) {
			everwellRegistration = "Success";
		}
		logger.info("this registerEverWellPatient method is excuting");
		return everwellRegistration;

	}

}
