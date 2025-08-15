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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.iemr.common.data.everwell.EverwellFeedback;
import com.iemr.common.repository.everwell.EverwellFeedbackRepo;

@Service
@PropertySource("classpath:application.properties")
public class EverwellDataSyncImpl implements EverwellDataSync {

	Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	RestTemplate restTemplateLogin = new RestTemplate();

	@Autowired
	private EverwellFeedbackRepo everwellFeedbackRepo;

	@Value("${everwelluserAuthenticate}")
	private String everwelluserAuthenticate;

	@Value("${everwellEditDoses}")
	private String everwellEditDoses;

	@Value("${everwellEditMissedDoses}")
	private String everwellEditMissedDoses;

	@Value("${everwellAddSupportAction}")
	private String everwellAddSupportAction;

	@Value("${everwellEditSecondaryPhoneNo}")
	private String everwellEditSecondaryPhoneNo;

	@Value("${everwellUserName}")
	private String everwellUserName;

	@Value("${everwellPassword}")
	private String everwellPassword;

	@Value("${everwellDataSyncDuration}")
	private String everwellDataSyncDuration;

	private static String EVERWELL_AUTH_TOKEN;
	private static Long EVERWEll_TOKEN_EXP;

	/*
	 * created by DU20091017 sync the data to everwell through everwell API.
	 */
	@Override
	public void dataSyncToEverwell() {
		String addSupportAction = "";
		String editSecondaryPhoneNo = "";
		String manualMissedDoseMsg = "";
		if (EVERWELL_AUTH_TOKEN != null && EVERWEll_TOKEN_EXP != null
				&& EVERWEll_TOKEN_EXP > System.currentTimeMillis()) {
			// no need of calling auth API
		} else {
			// call methoed to generate Auth Token at everwell end
			generateEverwellAuthToken();
		}

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		headers.add("user-agent",
				"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");
		headers.add("AUTHORIZATION", EVERWELL_AUTH_TOKEN);

		Date date = new Date();
		java.sql.Date sqlDate = new java.sql.Date(date.getTime());
		String text = sqlDate.toString();
		Timestamp currentDate = new Timestamp(sqlDate.getTime());
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(sqlDate);
		calendar.add(Calendar.DATE, -Integer.parseInt(everwellDataSyncDuration));
		Date beforeDate = calendar.getTime();
		Timestamp lastDate = new Timestamp(beforeDate.getTime());

		ArrayList<EverwellFeedback> records = everwellFeedbackRepo.findRecordsForDataSyncFromFeedback(currentDate,
				lastDate);

		boolean check = false;
		int count=0;
		if (records != null && records.size() > 0) {
			Boolean isManualOrMissedCalledEarlier = false;
			String manualOrMissed = null;
			for (EverwellFeedback patientDetails : records) {
				try {
					count=0;
					manualOrMissed = null;
					addSupportAction = addSupportAction(headers, patientDetails);
					if (patientDetails.getSecondaryPhoneNo() != null) {
						editSecondaryPhoneNo = editSecondaryPhoneNo(headers, patientDetails);
					} else
						editSecondaryPhoneNo = "success";

					// check if manual or missed called earlier
					if ((patientDetails.getIsManualDoseProcessed() != null
							&& patientDetails.getIsManualDoseProcessed() == true)
							|| (patientDetails.getIsMissedDoseProcessed() != null
									&& patientDetails.getIsMissedDoseProcessed() == true))
						isManualOrMissedCalledEarlier = true;

					// call manual dose API based on conditions
					if (patientDetails.getSubCategory() != null && patientDetails.getSubCategory()
							.equalsIgnoreCase("Dose taken but not reported by technology")) {

						manualOrMissed = "manual";
					}
					// call confirm missed dose API
					else if (patientDetails.getSubCategory() != null
							&& patientDetails.getSubCategory().equalsIgnoreCase("Dose not taken")) {
						manualOrMissed = "missed";
					}
					if (manualOrMissed != null)
						manualMissedDoseMsg = addMannualMissedDoses(headers, patientDetails,
								isManualOrMissedCalledEarlier, manualOrMissed);
					else
						manualMissedDoseMsg = "success";

					if (addSupportAction.equalsIgnoreCase("success") && editSecondaryPhoneNo.equalsIgnoreCase("success")
							&& manualMissedDoseMsg.equalsIgnoreCase("success")) {
						patientDetails.setProcessed("P");
						if (manualOrMissed != null && manualOrMissed.equalsIgnoreCase("manual"))
							patientDetails.setIsManualDoseProcessed(true);
						else if (manualOrMissed != null && manualOrMissed.equalsIgnoreCase("missed"))
							patientDetails.setIsMissedDoseProcessed(true);
					} else {
						if (manualMissedDoseMsg.equalsIgnoreCase("success")) {
							if (manualOrMissed != null && manualOrMissed.equalsIgnoreCase("manual"))
								patientDetails.setIsManualDoseProcessed(true);
							else if (manualOrMissed != null && manualOrMissed.equalsIgnoreCase("missed"))
								patientDetails.setIsMissedDoseProcessed(true);
						}
					}
					
				   count=everwellFeedbackRepo.updateDuplicateRecords(patientDetails.getId(), patientDetails.getDateOfAction());
				} catch (Exception e) {
					logger.info("Error in everwell data sync - " + e);
				}
			}

			ArrayList<EverwellFeedback> resulSet = (ArrayList<EverwellFeedback>) everwellFeedbackRepo.saveAll(records);

			if (resulSet != null && resulSet.size() == records.size())
				logger.info("Data Synced successfully. Date : " + new Timestamp(System.currentTimeMillis()));
			else
				logger.info("Error in data sync. Date : " + new Timestamp(System.currentTimeMillis()));

		} else
			logger.info("no records available for sync. Date : " + new Timestamp(System.currentTimeMillis()));

	}

	// call manual or missed dose api based on condition
	public String addMannualMissedDoses(HttpHeaders headers, EverwellFeedback record,
			Boolean isManualOrMissedCalledEarlier, String manualOrMissed) throws Exception {
		String message = "failure";
		String url = "";
		try {
			Long everwellPatientId = record.getId();

			// conditional URL generation for manual and missed dose
			if (manualOrMissed != null && manualOrMissed.equalsIgnoreCase("manual"))
				url = everwellEditDoses + "/" + everwellPatientId;
			else if (manualOrMissed != null && manualOrMissed.equalsIgnoreCase("missed"))
				url = everwellEditMissedDoses + "/" + everwellPatientId;

			if (record != null) {

				MultiValueMap<String, String> requestObjAddMannualMissedDosesForTrue = new LinkedMultiValueMap<String, String>();
				MultiValueMap<String, String> requestObjAddMannualMissedDosesForFalse = new LinkedMultiValueMap<String, String>();

				requestObjAddMannualMissedDosesForTrue.add("AddDoses", "true");
				requestObjAddMannualMissedDosesForTrue.add("Dates[" + 0 + "]",
						String.valueOf(record.getDateOfAction()));

				if (isManualOrMissedCalledEarlier) {
					requestObjAddMannualMissedDosesForFalse.add("AddDoses", "false");
					requestObjAddMannualMissedDosesForFalse.add("Dates[" + 0 + "]",
							String.valueOf(record.getDateOfAction()));

					// calling add manual dose sync API for false
					logger.info(url + " calling add manual/missed dose sync API for false, AI ID : " + record.getId());
					ResponseEntity<String> responseForFalse = restTemplate(requestObjAddMannualMissedDosesForFalse, url,
							headers);
					if (responseForFalse != null
							&& responseForFalse.getStatusCodeValue() == 200 & responseForFalse.hasBody()) {
						logger.info(
								url + " manual/missed dose sync API for false is success : AI ID " + record.getId());

						// calling add manual dose sync API for true
						logger.info(
								url + " calling add manual/missed dose sync API for true, AI ID : " + record.getId());
						ResponseEntity<String> responseForTrue = restTemplate(requestObjAddMannualMissedDosesForTrue,
								url, headers);
						if (responseForTrue != null
								&& responseForTrue.getStatusCodeValue() == 200 & responseForTrue.hasBody()) {
							logger.info(
									url + " manual/missed dose sync API for true is success : AI ID " + record.getId());
							message = "success";
						}
					}
				} else {
					// calling add manual dose sync API for true, first time
					logger.info(url + " calling add manual/missed dose sync API for true first time, AI ID : "
							+ record.getId());
					ResponseEntity<String> responseForTrue = restTemplate(requestObjAddMannualMissedDosesForTrue, url,
							headers);
					if (responseForTrue != null
							&& responseForTrue.getStatusCodeValue() == 200 & responseForTrue.hasBody())
						logger.info(url + " manual/missed dose sync API for true is success : AI ID " + record.getId());
					message = "success";
				}
			}

		} catch (Exception e) {
			logger.info(url + " error in add Manual/missed Dose data sync : " + e + " AI ID " + record.getId());
			throw new Exception(e);
		}
		return message;
	}

	@Deprecated
	public String addMissedDose(HttpHeaders headers, EverwellFeedback records, Boolean isManualOrMissedCalledEarlier) {
		String message = "failure";
//
//		try {
//			int arrayCountForTrue = 0, arrayCountForFalse = 0;
//			Long everwellPatientId = records.getId();
//			if (records != null) {
//				MultiValueMap<String, String> requestObjAddMissedDosesForTrue = new LinkedMultiValueMap<String, String>();
//				MultiValueMap<String, String> requestObjAddMissedDosesForFalse = new LinkedMultiValueMap<String, String>();
//
//				// Sending true and false response
//				if (records.getProcessed().equalsIgnoreCase("N")) {
//					String subcategory = records.getSubCategory();
//					if (subcategory.equalsIgnoreCase("Dose taken but not reported by technology")) {
//						requestObjAddMissedDosesForTrue.add("Dates[" + arrayCountForTrue + "]",
//								String.valueOf(records.getDateOfAction()));
//						arrayCountForTrue++;
//					}
//					requestObjAddMissedDosesForTrue.add("AddDoses", "true");
//
//				} else if (records.getProcessed().equalsIgnoreCase("U")) {
//
//					String subcategory = records.getSubCategory();
//					if (subcategory.equalsIgnoreCase("Dose not taken")) {
//						requestObjAddMissedDosesForTrue.add("Dates[" + arrayCountForTrue + "]",
//								String.valueOf(records.getDateOfAction()));
//						requestObjAddMissedDosesForFalse.add("Dates[" + arrayCountForTrue + "]",
//								String.valueOf(records.getDateOfAction()));
//						arrayCountForTrue++;
//					}
//					requestObjAddMissedDosesForTrue.add("AddDoses", "true");
//					requestObjAddMissedDosesForFalse.add("AddDoses", "false");
//				}
//
//				requestObjAddMissedDosesForTrue.add("AddDoses", "true");
//				requestObjAddMissedDosesForFalse.add("AddDoses", "false");
//
//				// creating rest template for calling everwell API
//				RestTemplate restTemplate = new RestTemplate();
//
//				logger.info("add Missed dose true request obj " + requestObjAddMissedDosesForTrue);
//				logger.info("add Missed dose false request obj " + requestObjAddMissedDosesForFalse);
//
//				String url = everwellEditDoses + "/" + everwellPatientId;
//
//				boolean trueResponse = false, falseResponse = false;
//
//				// sending the false response
//				if (requestObjAddMissedDosesForFalse.size() > 1) {
//					ResponseEntity<String> response = restTemplate(requestObjAddMissedDosesForFalse, url, headers);
//					if (response.getStatusCodeValue() == 200 & response.hasBody()) {
//						logger.info("response of add missed dose for false " + response.getBody());
//						falseResponse = true;
//					}
//				}
//				// sending the true response
//				if (requestObjAddMissedDosesForTrue.size() > 1) {
//					ResponseEntity<String> response = restTemplate(requestObjAddMissedDosesForTrue, url, headers);
//					if (response.getStatusCodeValue() == 200 & response.hasBody()) {
//						logger.info("response of add missed dose for true " + response.getBody());
//						trueResponse = true;
//					}
//				}
//
//				if (trueResponse) {
//					message = "success";
//					logger.info(
//							"Edit missed Dose API successfully sended response and updated in t_everwellFeedback table - "
//									+ everwellPatientId);
//
//				} else {
//					logger.info("Error - Edit missed Dose value not updated in t_everwellFeedback table - "
//							+ everwellPatientId);
//				}
//			}
//
//		} catch (Exception e) {
//			logger.info("error in performing - addMissedDose" + e);
//		}
//
//		logger.info("this addMissedDoses method is executed");
		return message;
	}

	@Deprecated
	public String addMannualDoses(HttpHeaders headers, EverwellFeedback records) {
		String message = "failure";
////		for (int value = 0; value < patientIDs.size(); value++) {
//		try {
//			int arrayCountForTrue = 0;
//			Long everwellPatientId = records.getId();
////			ArrayList<EverwellFeedback> records = everwellFeedbackRepo.findRecordsForManualDose(everwellPatientId);
//			if (records != null) {
//				MultiValueMap<String, String> requestObjAddMannualDosesForTrue = new LinkedMultiValueMap<String, String>();
//				MultiValueMap<String, String> requestObjAddMannualDosesForFalse = new LinkedMultiValueMap<String, String>();
//
//				if (records.getProcessed().equalsIgnoreCase("N")) {
//					String subcategory = records.getSubCategory();
//					if (subcategory.equalsIgnoreCase("Dose taken but not reported by technology")) {
//						requestObjAddMannualDosesForTrue.add("Dates[" + arrayCountForTrue + "]",
//								String.valueOf(records.getDateOfAction()));
//						arrayCountForTrue++;
//					}
//					requestObjAddMannualDosesForTrue.add("AddDoses", "true");
//
//				} else if (records.getProcessed().equalsIgnoreCase("U")) {
//
//					String subcategory = records.getSubCategory();
//					if (subcategory.equalsIgnoreCase("Dose taken but not reported by technology")) {
//						requestObjAddMannualDosesForTrue.add("Dates[" + arrayCountForTrue + "]",
//								String.valueOf(records.getDateOfAction()));
//						requestObjAddMannualDosesForFalse.add("Dates[" + arrayCountForTrue + "]",
//								String.valueOf(records.getDateOfAction()));
//						arrayCountForTrue++;
//					}
//					requestObjAddMannualDosesForTrue.add("AddDoses", "true");
//					requestObjAddMannualDosesForFalse.add("AddDoses", "false");
//				}
//
//				// creating rest template for calling everwell API
//				RestTemplate restTemplate = new RestTemplate();
//
//				logger.info("add mannual dose true request obj " + requestObjAddMannualDosesForTrue);
//				logger.info("add mannual dose false request obj " + requestObjAddMannualDosesForFalse);//
//
//				String url = everwellEditDoses + "/" + everwellPatientId;
//
//				boolean trueResponse = false, falseResponse = false;
//				// sending the false response
//				if (requestObjAddMannualDosesForFalse.size() > 1) {
//					ResponseEntity<String> response = restTemplate(requestObjAddMannualDosesForFalse, url, headers);
//					if (response.getStatusCodeValue() == 200 & response.hasBody()) {
//						logger.info("response of add mannual dose for false " + response.getBody());
//						falseResponse = true;
//					}
//				}
//
//				// sending the true response
//				if (requestObjAddMannualDosesForTrue.size() > 1) {
//					ResponseEntity<String> response = restTemplate(requestObjAddMannualDosesForTrue, url, headers);
//					if (response.getStatusCodeValue() == 200 & response.hasBody()) {
//						logger.info("response of add mannual dose for true " + response.getBody());
//						trueResponse = true;
//					}
//				}
//
//				if (trueResponse) {
//					message = "success";
//					logger.info(
//							"Edit Manual Dose API successfully sended response and updated in t_everwellFeedback table - "
//									+ everwellPatientId);
//
//				} else {
//					logger.info("Error - Edit Manual Dose value not updated in t_everwellFeedback table - "
//							+ everwellPatientId);
//				}
//			}
////			else {
////				message = "success";
////				logger.info("No records for edit manual dose for - " + everwellPatientId);
////			}
//
//		} catch (Exception e) {
//			logger.info("error in performing - addManualDose" + e);
//		}
//
////		}
//
//		logger.info("this addMannualDoses method is executed");
		return message;
	}

	/*
	 * Add support actions to everwell thtough add support action API.
	 * https://beta-hub.everwell.org/Api/Patients/AddSupportAction.
	 */
	public String addSupportAction(HttpHeaders headers, EverwellFeedback record) throws Exception {
		String message = "failure";

		Long everwellPatientId = record.getId();		

		if (record != null) {
			try {
				MultiValueMap<String, String> requestObjAddSupportAction = new LinkedMultiValueMap<String, String>();
				requestObjAddSupportAction.add("Category", String.valueOf(record.getCategory()));
				requestObjAddSupportAction.add("ActionTaken", String.valueOf(record.getActionTaken()));
				requestObjAddSupportAction.add("Comments", String.valueOf(record.getComments()));
				if (record.getSubCategory().equalsIgnoreCase("Do not disturb for today")) {
					requestObjAddSupportAction.add("SubCategory", "Others");
				} else {
					requestObjAddSupportAction.add("SubCategory", String.valueOf(record.getSubCategory()));
				}
				requestObjAddSupportAction.add("DateOfAction", String.valueOf(record.getDateOfAction()));

				String url = everwellAddSupportAction + "/" + everwellPatientId;

				logger.info("add support actions request obj " + requestObjAddSupportAction);

				ResponseEntity<String> response = restTemplate(requestObjAddSupportAction, url, headers);
				if (response.getStatusCodeValue() == 200 & response.hasBody()) {
					logger.info("Add support Action sync successfully. Output Response : " + response.getBody()
							+ "everwell patient ID : " + everwellPatientId + " AI ID : " + record.getId());

					message = "success";
				} else {
					logger.info("addSupportAction sync failure. everwell patient ID : " + everwellPatientId
							+ " AI ID : " + record.getId());
				}

			} catch (Exception e) {
				logger.info("addSupportAction sync failure " + e + "everwell patient ID : " + everwellPatientId
						+ " AI ID : " + record.getId());
				throw new Exception(e);
			}

		}
		return message;
	}

	/*
	 * Edit secondary phone number of everwell beneficiary.
	 * https://beta-hub.everwell.org/Api/Patients/EditPhoneNumber
	 */
	public String editSecondaryPhoneNo(HttpHeaders headers, EverwellFeedback record) throws Exception {
		String message = "failure";
		Long everwellPatientId = record.getId();

		if (record != null) {
			try {
				MultiValueMap<String, String> requestObjAddSecondaryPhoneNo = new LinkedMultiValueMap<String, String>();
				requestObjAddSecondaryPhoneNo.add("PhoneNumber", String.valueOf(record.getSecondaryPhoneNo()));

				String url = everwellEditSecondaryPhoneNo + "/" + everwellPatientId;

				ResponseEntity<String> response = restTemplatePUT(requestObjAddSecondaryPhoneNo, url, headers);
				if (response.getStatusCodeValue() == 200 & response.hasBody()) {
					logger.info("secondary phone no, sync successfully. Output Response : " + response.getBody()
							+ "everwell patient ID : " + everwellPatientId + " AI ID : " + record.getId());

					message = "success";
				} else {
					logger.info("secondary phone no sync failure. everwell patient ID : " + everwellPatientId
							+ " AI ID : " + record.getId());
				}

			} catch (Exception e) {
				logger.info("secondary phone no, sync failure " + e + "everwell patient ID : " + everwellPatientId
						+ " AI ID : " + record.getId());
				throw new Exception(e);
			}

		}
		return message;
	}

	/*
	 * Used for calling the everwekk API's
	 * 
	 */
	public ResponseEntity<String> restTemplate(MultiValueMap<String, String> requestObj, String url,
			HttpHeaders headers) {
		RestTemplate restTemplate = new RestTemplate();

		HttpEntity<Object> request = new HttpEntity<Object>(requestObj, headers);
		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

		return response;
	}

	public ResponseEntity<String> restTemplatePUT(MultiValueMap<String, String> requestObj, String url,
			HttpHeaders headers) {
		// Use the injected or field RestTemplate for testability
		HttpEntity<Object> request = new HttpEntity<Object>(requestObj, headers);
		ResponseEntity<String> response = restTemplateLogin.exchange(url, HttpMethod.PUT, request, String.class);
		return response;
	}

	private void generateEverwellAuthToken() {

		MultiValueMap<String, String> requestData = new LinkedMultiValueMap<String, String>();
		requestData.add("username", everwellUserName);
		requestData.add("password", everwellPassword);
		requestData.add("grant_type", "password");
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		httpHeaders.add("user-agent",
				"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");

		HttpEntity<MultiValueMap<String, String>> httpRequestEntity = new HttpEntity<MultiValueMap<String, String>>(
				requestData, httpHeaders);
		// call Token generator End-point
		ResponseEntity<String> responseEntity = restTemplateLogin.exchange(everwelluserAuthenticate, HttpMethod.POST,
				httpRequestEntity, String.class);

		if (responseEntity != null && responseEntity.getStatusCodeValue() == 200 && responseEntity.hasBody()) {

			String responseBody = responseEntity.getBody();
			JsonObject jsnOBJ = new JsonObject();
			JsonParser jsnParser = new JsonParser();
			JsonElement jsnElmnt = jsnParser.parse(responseBody);
			jsnOBJ = jsnElmnt.getAsJsonObject();
			EVERWELL_AUTH_TOKEN = jsnOBJ.get("token_type").getAsString() + " "
					+ jsnOBJ.get("access_token").getAsString();

			logger.info("Auth key generated at : " + System.currentTimeMillis() + ", Key : " + EVERWELL_AUTH_TOKEN);

			Date date = new Date();
			java.sql.Date sqlDate = new java.sql.Date(date.getTime());
			Calendar everwellCalendar = Calendar.getInstance();
			everwellCalendar.setTime(sqlDate);
			everwellCalendar.add(Calendar.DATE, 29);
			Date everwellTokenEndDate = everwellCalendar.getTime();
			// setting Token expiry - 29 days
			EVERWEll_TOKEN_EXP = everwellTokenEndDate.getTime();

		}
	}
}
