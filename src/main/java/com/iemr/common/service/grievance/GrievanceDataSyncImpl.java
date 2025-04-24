package com.iemr.common.service.grievance;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.iemr.common.data.callhandling.CallType;
import com.iemr.common.data.grievance.GrievanceCallRequest;
import com.iemr.common.data.grievance.GrievanceDetails;
import com.iemr.common.data.grievance.GrievanceTransaction;
import com.iemr.common.repository.callhandling.IEMRCalltypeRepositoryImplCustom;
import com.iemr.common.repository.grievance.GrievanceDataRepo;
import com.iemr.common.repository.grievance.GrievanceFetchBenDetailsRepo;
import com.iemr.common.repository.grievance.GrievanceTransactionRepo;
import com.iemr.common.repository.location.LocationStateRepository;
import com.iemr.common.utils.exception.IEMRException;
import com.iemr.common.utils.mapper.InputMapper;

import jakarta.transaction.Transactional;

@Service
@PropertySource("classpath:application.properties")
public class GrievanceDataSyncImpl implements GrievanceDataSync {
	Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	RestTemplate restTemplateLogin = new RestTemplate();

	private static final String USER_AGENT_HEADER = "user-agent";
	private static final String USER_AGENT_VALUE = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36";
	private static final String STATUS_CODE = "statusCode";

	private static final String FILE_NAME = "fileName";
	private static final String FILE_TYPE = "fileType";
	private static final String PREFERRED_LANGUAGE = "language";
	private static final String COUNT_OF_PREFERRED_LANGUAGE = "count";

	private final GrievanceDataRepo grievanceDataRepo;
	private final GrievanceTransactionRepo grievanceTransactionRepo;
	private final GrievanceFetchBenDetailsRepo grievanceFetchBenDetailsRepo;
	private final LocationStateRepository locationStateRepository;
	private final IEMRCalltypeRepositoryImplCustom iEMRCalltypeRepositoryImplCustom;

	// Constructor-based injection
	@Autowired
	public GrievanceDataSyncImpl(GrievanceDataRepo grievanceDataRepo, GrievanceTransactionRepo grievanceTransactionRepo,
			GrievanceFetchBenDetailsRepo grievanceFetchBenDetailsRepo, LocationStateRepository locationStateRepository,
			IEMRCalltypeRepositoryImplCustom iEMRCalltypeRepositoryImplCustom) {
		this.grievanceDataRepo = grievanceDataRepo;
		this.grievanceTransactionRepo = grievanceTransactionRepo;
		this.grievanceFetchBenDetailsRepo = grievanceFetchBenDetailsRepo;
		this.locationStateRepository = locationStateRepository;
		this.iEMRCalltypeRepositoryImplCustom = iEMRCalltypeRepositoryImplCustom;
	}

	@Value("${grievanceUserAuthenticate}")
	private String grievanceUserAuthenticate;

	@Value("${updateGrievanceDetails}")
	private String updateGrievanceDetails;

	@Value("${updateGrievanceTransactionDetails}")
	private String updateGrievanceTransactionDetails;

	@Value("${grievanceUserName}")
	private String grievanceUserName;

	@Value("${grievancePassword}")
	private String grievancePassword;

	@Value("${grievanceDataSyncDuration}")
	private String grievanceDataSyncDuration;

	@Value("${grievanceAllocationRetryConfiguration}")
	private int grievanceAllocationRetryConfiguration;

	private String GRIEVANCE_AUTH_TOKEN;
	private Long GRIEVANCE_TOKEN_EXP;

	public String dataSyncToGrievance() {

		int count = 0;
		List<GrievanceDetails> grievanceDetailsListAll = new ArrayList<>();

		List<GrievanceTransaction> grievanceTransactionList = new ArrayList<>();
		GrievanceTransaction grievanceTransactionListObj = new GrievanceTransaction();
		List<Long> grievanceIds = new ArrayList<>();

		Long gwid;

		try {

			RestTemplate restTemplate = new RestTemplate();

			if (GRIEVANCE_AUTH_TOKEN != null && GRIEVANCE_TOKEN_EXP != null
					&& GRIEVANCE_TOKEN_EXP > System.currentTimeMillis()) {
			} else {
				generateGrievanceAuthToken();
			}

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.add(USER_AGENT_HEADER, USER_AGENT_VALUE);
			headers.add("AUTHORIZATION", GRIEVANCE_AUTH_TOKEN);

			Date date = new Date();
			java.sql.Date sqlDate = new java.sql.Date(date.getTime());
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(sqlDate);
			calendar.add(Calendar.DATE, -Integer.parseInt(grievanceDataSyncDuration));
			String json = prepareRequestObject();
			int contentLength = json.getBytes(StandardCharsets.UTF_8).length;
			headers.add("Content-Length", String.valueOf(contentLength));
			HttpEntity<String> request = new HttpEntity<>(json, headers);
			String url = updateGrievanceDetails.replace("PageNumber", "1");
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

			if (response != null && response.hasBody()) {
				JSONObject obj = new JSONObject(response.getBody());
				if (obj != null && obj.has("data") && obj.has("status") && obj.getInt("status") == 200) {
					logger.info("Grievance data details response received successfully ");

					String responseStr = response.getBody();
					JsonObject jsnOBJ = new JsonObject();
					JsonParser jsnParser = new JsonParser();
					JsonElement jsnElmnt = jsnParser.parse(responseStr);
					jsnOBJ = jsnElmnt.getAsJsonObject();
					int total = jsnOBJ.get("total").getAsInt();
					String GrievanceUrl = updateGrievanceDetails.replace("PageNumber", String.valueOf(total));
					ResponseEntity<String> grievienceResponse = restTemplate.exchange(GrievanceUrl, HttpMethod.POST,
							request, String.class);
					String respStr = grievienceResponse.getBody();

					JsonElement jsnElmntResp = jsnParser.parse(respStr);
					jsnOBJ = jsnElmntResp.getAsJsonObject();
					JsonArray grievanceJsonDataArray = jsnOBJ.getAsJsonArray("data");
					if (Integer.parseInt(jsnOBJ.get("total").toString()) > 0) {
						for (JsonElement grievanceElement : grievanceJsonDataArray) {
							String formattedComplaintId = null;
							try {
								JsonObject grievanceJsonData = grievanceElement.getAsJsonObject();
								GrievanceDetails grievance = new GrievanceDetails();
								String complaintId = grievanceJsonData.get("complainId").getAsString();
								formattedComplaintId = complaintId.replace("\\/", "/");
								boolean complaintExists = grievanceDataRepo.existsByComplaintId(formattedComplaintId);
								if (complaintExists) {
									logger.info("Complaint ID " + formattedComplaintId
											+ " already exists in the grievance worklist table.");
									continue;
								}

								grievance.setComplaintID(formattedComplaintId);

								// Fetch related grievance transaction details
								Long grievanceID = grievanceJsonData.get("grievanceId").getAsLong();
								grievance.setGrievanceId(grievanceID);
								grievanceIds.add(grievanceJsonData.get("grievanceId").getAsLong());

								grievance.setSubjectOfComplaint(grievanceJsonData.has("subject")
										&& !grievanceJsonData.get("subject").isJsonNull()
												? grievanceJsonData.get("subject").getAsString()
												: null);
								ArrayList<Object[]> lists = grievanceFetchBenDetailsRepo
										.findByComplaintId(formattedComplaintId);
								grievance.setComplaint(grievanceJsonData.has("Complaint")
										? grievanceJsonData.get("Complaint").getAsString()
										: null);
								String severityName = grievanceJsonData.has("severity")
										&& grievanceJsonData.get("severity").getAsJsonObject().has("severity")
												? grievanceJsonData.get("severity").getAsJsonObject().get("severity")
														.getAsString()
												: null;
								grievance.setSeverety(severityName);

								// Setting Level
								Integer levelId = grievanceJsonData.has("level")
										&& grievanceJsonData.get("level").getAsJsonObject().has("levelId")
												? grievanceJsonData.get("level").getAsJsonObject().get("levelId")
														.getAsInt()
												: null;
								grievance.setLevel(levelId);

								// Setting state
								String stateName = grievanceJsonData.has("state")
										&& grievanceJsonData.get("state").getAsJsonObject().has("stateName")
												? grievanceJsonData.get("state").getAsJsonObject().get("stateName")
														.getAsString()
												: null;
								grievance.setState(stateName);

								for (Object[] objects : lists) {
									if (objects != null && objects.length <= 4) {
										grievance.setComplaintID((String) objects[0]);
										grievance.setBenCallID((Long) objects[1]);
										grievance.setBeneficiaryRegID((Long) objects[2]);
										grievance.setProviderServiceMapID((Integer) objects[3]);
									}
								}
								Long benDetailsID = grievanceDataRepo
										.getBeneficiaryMapping(grievance.getBeneficiaryRegID());
								ArrayList<Object[]> list1 = grievanceDataRepo
										.getBeneficiaryGrievanceDetails(benDetailsID);
								for (Object[] objects : list1) {
									if (objects != null && objects.length >= 6) {
										grievance.setPreferredLanguageId((Integer) objects[0]);
										grievance.setPreferredLanguage((String) objects[1]);
										grievance.setVanSerialNo((Long) objects[2]);
										grievance.setVanID((Integer) objects[3]);
										grievance.setParkingPlaceID((Integer) objects[4]);
										grievance.setVehicalNo((String) objects[5]);
									}
								}
								String phoneNum = grievanceDataRepo.getPrimaryNumber(grievance.getBeneficiaryRegID());
								grievance.setPrimaryNumber(phoneNum);
								grievance.setDeleted(grievance.getDeleted());
								grievance.setCreatedBy("Admin");
								grievance.setProcessed('N');
								grievance.setIsAllocated(false);
								grievance.setCallCounter(0);
								grievance.setRetryNeeded(true);

								grievanceDataRepo.save(grievance);

								JsonArray transactionDetailsList = fetchGrievanceTransactions(grievanceID);
								if (transactionDetailsList != null && !transactionDetailsList.isEmpty()) {
									for (JsonElement transactionElement : transactionDetailsList) {
										JsonObject transactionDetailsJson = transactionElement.getAsJsonObject();
										JsonArray asJsonArray = transactionDetailsJson.get("transaction")
												.getAsJsonArray();
										for (JsonElement arr : asJsonArray) {
											JsonObject transaction = arr.getAsJsonObject();
											GrievanceTransaction grievanceTransaction = new GrievanceTransaction();
											gwid = grievanceDataRepo.getUniqueGwid(grievanceID);
											grievanceTransaction.setGwid(gwid);
											grievanceTransaction.setGrievanceId(grievanceID);
											grievanceTransaction.setActionTakenBy(transaction.has("actionTakenBy")
													&& !transaction.get("actionTakenBy").isJsonNull()
															? transaction.get("actionTakenBy").getAsString()
															: null);
											grievanceTransaction.setStatus(
													transaction.has("status") && !transaction.get("status").isJsonNull()
															? transaction.get("status").getAsString()
															: null);
											grievanceTransaction.setFileName(transaction.has(FILE_NAME)
													&& !transaction.get(FILE_NAME).isJsonNull()
															? transaction.get(FILE_NAME).getAsString()
															: null);
											grievanceTransaction.setFileType(transaction.has(FILE_TYPE)
													&& !transaction.get(FILE_TYPE).isJsonNull()
															? transaction.get(FILE_TYPE).getAsString()
															: null);
											grievanceTransaction.setRedressed(transaction.has("redressed")
													&& !transaction.get("redressed").isJsonNull()
															? transaction.get("redressed").getAsString()
															: null);
											grievanceTransaction.setCreatedAt(
													Timestamp.valueOf(transaction.get("createdAt").getAsString()));
											grievanceTransaction.setUpdatedAt(
													Timestamp.valueOf(transaction.get("updatedAt").getAsString()));
											grievanceTransaction.setComments(transaction.has("comment")
													? transaction.get("comment").getAsString()
													: null);
											grievanceTransaction.setCreatedBy("Admin");
											Timestamp timestamp = new Timestamp(System.currentTimeMillis());
											grievanceTransaction.setCreatedDate(timestamp);

											grievanceTransactionListObj = grievanceTransactionRepo
													.save(grievanceTransaction);
											grievanceTransactionList.add(grievanceTransactionListObj);
										}
									}

								}
							} catch (Exception e) {
								logger.error("Error while Grievance Details " + e.getMessage() + " Complaint ID "
										+ formattedComplaintId);
							}
						}
					}
				}
			}

		} catch (Exception e) {
			logger.error("Error in saving data into t_grievanceworklist: ", e);
		}
		// return responseData;
		return "Grievance Data saved successfully";
	}

	private String prepareRequestObject() {
		Map<String, Object> bodyMap = new HashMap<>();
		bodyMap.put("draw", 1);
		bodyMap.put("columns", new Object[] {});
		bodyMap.put("order", new Object[] {});
		bodyMap.put("start", 0);
		bodyMap.put("length", 10);

		Map<String, Object> search = new HashMap<>();
		search.put("value", "");
		search.put("regex", false);
		bodyMap.put("search", search);

		bodyMap.put("state_id", "");
		bodyMap.put("complain_id", "");
		bodyMap.put("color", "");
		bodyMap.put("level", "3");
		bodyMap.put("start_date", "");
		bodyMap.put("end_date", "");

		ObjectMapper objectMapper = new ObjectMapper();
		String jsonBody = "";
		try {
			jsonBody = objectMapper.writeValueAsString(bodyMap);
		} catch (Exception e) {
			logger.error("Error while preparing Grievance jsonRequest");
		}
		return jsonBody;
	}

	private JsonArray fetchGrievanceTransactions(Long grievanceId) {
		List<GrievanceTransaction> transactionDetailsList = new ArrayList<>();
		JsonArray transactionDataArray = new JsonArray();
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
			headers.add(USER_AGENT_HEADER, USER_AGENT_VALUE);
			headers.add("AUTHORIZATION", GRIEVANCE_AUTH_TOKEN);

			HttpEntity<Object> request = new HttpEntity<Object>(headers);
			RestTemplate restTemplate = new RestTemplate();

			ResponseEntity<String> response = restTemplate.exchange(updateGrievanceTransactionDetails + grievanceId,
					HttpMethod.GET, request, String.class);

			if (response != null && response.hasBody()) {
				JSONObject obj = new JSONObject(response.getBody());
				if (obj != null && obj.has("data") && obj.has("status") && obj.getInt("status") == 200) {
					JsonObject jsnOBJ = new JsonObject();
					JsonParser jsnParser = new JsonParser();
					JsonElement jsnElmnt = jsnParser.parse(response.getBody());
					jsnOBJ = jsnElmnt.getAsJsonObject();

					transactionDataArray = jsnOBJ.getAsJsonArray("data");
//	                if (transactionDataArray != null && transactionDataArray.size() > 0) {
//	                    GrievanceTransaction[] transactionDetailsArray = new Gson()
//	                            .fromJson(transactionDataArray, GrievanceTransaction[].class);
//	                    transactionDetailsList = Arrays.asList(transactionDetailsArray);
//				}
				}
			}

		} catch (Exception e) {
			logger.error("Error fetching grievance transaction details for grievanceId " + grievanceId, e);
		}
		return transactionDataArray;
	}

	private void generateGrievanceAuthToken() {

		MultiValueMap<String, String> requestData = new LinkedMultiValueMap<String, String>();
		requestData.add("username", grievanceUserName);
		requestData.add("password", grievancePassword);
		requestData.add("grant_type", "password");
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		httpHeaders.add(USER_AGENT_HEADER, USER_AGENT_VALUE);

		HttpEntity<MultiValueMap<String, String>> httpRequestEntity = new HttpEntity<MultiValueMap<String, String>>(
				requestData, httpHeaders);
		ResponseEntity<String> responseEntity = restTemplateLogin.exchange(grievanceUserAuthenticate, HttpMethod.POST,
				httpRequestEntity, String.class);

		if (responseEntity != null && responseEntity.getStatusCodeValue() == 200 && responseEntity.hasBody()) {
//			 JSONObject requestObj = new JSONObject(request);
//		        String complaintResolution = requestObj.optString("ComplaintResolution", null);
//		        String state = requestObj.optString("State", null);

			String responseBody = responseEntity.getBody();
			JsonObject jsnOBJ = new JsonObject();
			JsonParser jsnParser = new JsonParser();
			JsonElement jsnElmnt = jsnParser.parse(responseBody);
			jsnOBJ = jsnElmnt.getAsJsonObject();

			// Accessing the "data" object first
			if (jsnOBJ.has("data")) {
				JsonObject dataObj = jsnOBJ.getAsJsonObject("data");

				// Now check for "token_type" and "access_token" inside the "data" object
				if (dataObj.has("token_type") && dataObj.has("access_token")) {
					String tokenType = dataObj.get("token_type").getAsString();
					String accessToken = dataObj.get("access_token").getAsString();
					GRIEVANCE_AUTH_TOKEN = tokenType + " " + accessToken;
				} else {
					logger.error("Missing token_type or access_token in 'data' object: {}", responseBody);
					// Handle missing tokens gracefully
					return;
				}
			} else {
				logger.error("'data' object is missing in response: {}", responseBody);
				return;
			}

			// GRIEVANCE_AUTH_TOKEN = jsnOBJ.get("token_type").getAsString() + " "
			// + jsnOBJ.get("access_token").getAsString();

			JsonObject grievanceLoginJsonData = jsnOBJ.getAsJsonObject("data");

			logger.info("Auth key generated at : {}", System.currentTimeMillis() + ", Key : {}", GRIEVANCE_AUTH_TOKEN);

			Date date = new Date();
			java.sql.Date sqlDate = new java.sql.Date(date.getTime());
			Calendar grievanceCalendar = Calendar.getInstance();
			grievanceCalendar.setTime(sqlDate);
			grievanceCalendar.add(Calendar.DATE, 29);
			Date grievanceTokenEndDate = grievanceCalendar.getTime();
			// setting Token expiry - 29 days
			GRIEVANCE_TOKEN_EXP = grievanceTokenEndDate.getTime();

			int count = 3;
			while (count > 0) {
				try {
					// List<Map<String, Object>> savedGrievanceData = dataSyncToGrievance();
					String savedGrievanceData = dataSyncToGrievance();
					if (savedGrievanceData != null)
						break;
					else {
						count--;

						if (count > 0) {
							Thread.sleep(5000);
						}
					}

				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					break;
				}
			}
		}
	}

	@Override
	public String fetchUnallocatedGrievanceCount(String preferredLanguage, Timestamp filterStartDate,
			Timestamp filterEndDate, Integer providerServiceMapID) throws IEMRException, JSONException {
		logger.debug("Request received for fetchUnallocatedGrievanceCount");

		// Fetch all unallocated grievances count from the database
		Set<Object[]> resultSet = grievanceDataRepo.fetchUnallocatedGrievanceCount(filterStartDate, filterEndDate);

		JSONObject result = new JSONObject();
		boolean preferredLanguageFound = false;
		result.put("All", 0); // Initialize the "All" language count to 0

		if (resultSet != null && !resultSet.isEmpty()) {
			for (Object[] recordSet : resultSet) {
				String language = null;
				Long count = null;
				if(null != recordSet[0])
					language = ((String) recordSet[0]).trim();
				if(null != recordSet[1])
				count = (Long) recordSet[1];

				result.put(language, count);
				result.put("All", result.getLong("All") + count); // Add to the total "All" count

				if (preferredLanguage != null && preferredLanguage.equalsIgnoreCase(language)) {
					preferredLanguageFound = true;
				}
			}
		}

		if (preferredLanguage != null && !preferredLanguageFound) {
			result.put(preferredLanguage, 0);
		}

		JSONArray resultArray = new JSONArray();

		if (preferredLanguage != null) {
			JSONObject preferredLanguageEntry = new JSONObject();
			preferredLanguageEntry.put(PREFERRED_LANGUAGE, preferredLanguage);
			preferredLanguageEntry.put(COUNT_OF_PREFERRED_LANGUAGE, result.getLong(preferredLanguage));
			resultArray.put(preferredLanguageEntry);
		} else {
		
			JSONObject allEntry = new JSONObject();
			allEntry.put(PREFERRED_LANGUAGE, "All");
			allEntry.put(COUNT_OF_PREFERRED_LANGUAGE, result.getLong("All"));
			resultArray.put(allEntry);
			Iterator<String> keys = result.keys();
			while (keys.hasNext()) {
				String key = keys.next();
				if (!key.equals("All")) {
					JSONObject temp = new JSONObject();
					temp.put(PREFERRED_LANGUAGE, key);
					temp.put(COUNT_OF_PREFERRED_LANGUAGE, result.getLong(key));
					resultArray.put(temp);
				}
			}
		}

		return resultArray.toString();
	}

	@Override
	@Transactional
	public String completeGrievanceCall(String request) throws Exception {

		GrievanceCallRequest grievanceCallRequest = InputMapper.gson().fromJson(request, GrievanceCallRequest.class);
		String complaintID = grievanceCallRequest.getComplaintID();
		Integer userID = grievanceCallRequest.getUserID();
		Boolean isCompleted = grievanceCallRequest.getIsCompleted();
		Long beneficiaryRegID = grievanceCallRequest.getBeneficiaryRegID();
		Integer callTypeID = grievanceCallRequest.getCallTypeID();
		Integer providerServiceMapID = grievanceCallRequest.getProviderServiceMapID();

		CallType callTypeObj = new CallType();
		String response = "failure";
		int updateCount = 0;
		int updateCallCounter = 0;
		try {

			GrievanceDetails grievanceCallStatus = new GrievanceDetails();

			List<Object[]> lists = grievanceDataRepo.getCallCounter(complaintID);
			for (Object[] objects : lists) {
				if (objects != null && objects.length >= 2) {
					grievanceCallStatus.setCallCounter((Integer) objects[0]);
					grievanceCallStatus.setRetryNeeded((Boolean) objects[1]);
					grievanceCallStatus.setComplaintResolution((String) objects[2]);
				}
			}

			// Fetching CallDetails using BenCallID and CallTypeID
			Set<Object[]> callTypesArray = new HashSet<Object[]>();
			callTypesArray = iEMRCalltypeRepositoryImplCustom.getCallDetails(callTypeID);
			for (Object[] object : callTypesArray) {
				if (object != null && object.length >= 2) {
					callTypeObj.setCallGroupType((String) object[0]);
					callTypeObj.setCallType((String) object[1]);

				}

			}

			String callGroupType = callTypeObj.getCallGroupType();
			String callType = callTypeObj.getCallType();

			// Logic for reattempt based on call group type and call type
			boolean isRetryNeeded = grievanceCallStatus.getRetryNeeded();
			if ((null != grievanceCallStatus.getComplaintResolution()
					&& grievanceCallStatus.getComplaintResolution().equalsIgnoreCase("Resolved")) || (callGroupType.equalsIgnoreCase("Valid") && (callType.equalsIgnoreCase("Valid") || callType.equals("Test Call")))) {
				isRetryNeeded = false;
				updateCount = grievanceDataRepo.updateCompletedStatusInCall(true, false, complaintID, userID, beneficiaryRegID);
			}
			else if (callGroupType.equalsIgnoreCase("Invalid") && (callType.equalsIgnoreCase("Wrong Number") || callType.equalsIgnoreCase("Invalid Call"))) {
				isRetryNeeded = false;
				updateCount = grievanceDataRepo.updateCompletedStatusInCall(true, isRetryNeeded, complaintID, userID,
						beneficiaryRegID);
			}else {
				isRetryNeeded = true;
				updateCount = grievanceDataRepo.updateCompletedStatusInCall(false, isRetryNeeded, complaintID,
						userID, beneficiaryRegID);
			}
			// Check if max attempts (3) are reached
			if (isRetryNeeded && grievanceCallStatus.getCallCounter() < grievanceAllocationRetryConfiguration) {
				grievanceCallStatus.setCallCounter(grievanceCallStatus.getCallCounter() + 1);
				updateCallCounter = grievanceDataRepo.updateCallCounter(grievanceCallStatus.getCallCounter(),
						isRetryNeeded, grievanceCallRequest.getComplaintID(),
						grievanceCallRequest.getBeneficiaryRegID(),
						grievanceCallRequest.getUserID());
				if (updateCallCounter > 0)
					response = "Successfully closing call";
				else {
					response = "failure in closing call";
				}
			} else if (grievanceCallStatus.getCallCounter() == grievanceAllocationRetryConfiguration) {
				// Max attempts reached, no further reattempt
				isRetryNeeded = false;
				// isCompleted = true;
				updateCount = grievanceDataRepo.updateCompletedStatusInCall(isCompleted, isRetryNeeded, complaintID,
						userID, beneficiaryRegID);
				response = "max_attempts_reached"; // Indicate that max attempts are reached

			}else if(updateCount > 0) {
				response = "Successfully closing call";
			}

		} catch (Exception e) {
			response = "error: " + e.getMessage();
		}

		return response; // Return the response (either success or error message)
	}

}
