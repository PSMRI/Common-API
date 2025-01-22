package com.iemr.common.service.grievance;



	import java.sql.Timestamp;
	import java.util.ArrayList;
	import java.util.Arrays;
	import java.util.Calendar;
	import java.util.Date;
	import java.util.HashMap;
	import java.util.List;
	import java.util.Map;

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

	import com.google.gson.JsonElement;
	import com.google.gson.JsonObject;
	import com.google.gson.JsonParser;
	import com.iemr.common.data.grievance.GrievanceDetails;
	import com.iemr.common.data.grievance.GrievanceTransaction;

	import com.iemr.common.repository.grievance.GrievanceDataRepo;
	import com.iemr.common.repository.grievance.GrievanceFetchBenDetailsRepo;
	import com.iemr.common.repository.grievance.GrievanceTransactionRepo;
	import com.iemr.common.repository.location.LocationStateRepository;
import com.iemr.common.utils.exception.IEMRException;
import com.iemr.common.utils.mapper.InputMapper;

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

	    
	    private final GrievanceDataRepo grievanceDataRepo;
	    private final GrievanceTransactionRepo grievanceTransactionRepo;
	    private final GrievanceFetchBenDetailsRepo grievanceFetchBenDetailsRepo;
	    private final LocationStateRepository locationStateRepository;

	    // Constructor-based injection
	    @Autowired
	    public GrievanceDataSyncImpl(GrievanceDataRepo grievanceDataRepo, 
	                         GrievanceTransactionRepo grievanceTransactionRepo, 
	                         GrievanceFetchBenDetailsRepo grievanceFetchBenDetailsRepo,
	                         LocationStateRepository locationStateRepository) {
	        this.grievanceDataRepo = grievanceDataRepo;
	        this.grievanceTransactionRepo = grievanceTransactionRepo;
	        this.grievanceFetchBenDetailsRepo = grievanceFetchBenDetailsRepo;
	        this.locationStateRepository = locationStateRepository;
	    }

	    @Value("${greivanceUserAuthenticate}")
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

	    private String GRIEVANCE_AUTH_TOKEN;
	    private Long GRIEVANCE_TOKEN_EXP;

	    
	    	public List<Map<String, Object>> dataSyncToGrievance() {
	    	
	        int count = 0;
	        String registeringUser = "";
	        List<Map<String, Object>> responseData = new ArrayList<>();
	        List<GrievanceDetails> grievanceDetailsListAS = null;
	        try {
	            // Loop to fetch data for multiple pages
	            while (count >= 0) {
	                RestTemplate restTemplate = new RestTemplate();

	                if (GRIEVANCE_AUTH_TOKEN != null && GRIEVANCE_TOKEN_EXP != null
	                        && GRIEVANCE_TOKEN_EXP > System.currentTimeMillis()) {
	                    // no need of calling auth API
	                } else {
	                    // call method to generate Auth Token at Everwell end
	                    generateGrievanceAuthToken();
	                }

	                HttpHeaders headers = new HttpHeaders();
	                headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
	                headers.add(USER_AGENT_HEADER, USER_AGENT_VALUE);
	                headers.add("AUTHORIZATION", GRIEVANCE_AUTH_TOKEN);

	                Date date = new Date();
	                java.sql.Date sqlDate = new java.sql.Date(date.getTime());
	                Calendar calendar = Calendar.getInstance();
	                calendar.setTime(sqlDate);
	                calendar.add(Calendar.DATE, -Integer.parseInt(grievanceDataSyncDuration));

	                // Request object
	                HttpEntity<Object> request = new HttpEntity<Object>(headers);

	                // Call rest-template to call API to download master data for given table
	                ResponseEntity<String> response = restTemplate.exchange(updateGrievanceDetails, HttpMethod.POST, request,
	                        String.class);

	                if (response != null && response.hasBody()) {
	                    JSONObject obj = new JSONObject(response.getBody());
	                    if (obj != null && obj.has("data") && obj.has(STATUS_CODE) && obj.getInt(STATUS_CODE) == 200) {
	                        logger.info("Grievance data details response received successfully ");

	                        String responseStr = response.getBody();
	                        JsonObject jsnOBJ = new JsonObject();
	                        JsonParser jsnParser = new JsonParser();
	                        JsonElement jsnElmnt = jsnParser.parse(responseStr);
	                        jsnOBJ = jsnElmnt.getAsJsonObject();
	                        JsonObject grievanceJsonData = jsnOBJ.getAsJsonObject("data");

	                        registeringUser = grievanceJsonData.get("userName").getAsString();
	                        
	                        if (Integer.parseInt(jsnOBJ.get("TotalRecords").toString()) > 0) {
	                            GrievanceDetails[] grievanceDetailsArray = InputMapper.gson().fromJson(jsnOBJ.get("Data").toString(), GrievanceDetails[].class);
	                            List<GrievanceDetails> grievanceDetailsList = Arrays.asList(grievanceDetailsArray);


	                           //////////////////////
	                         // Loop through the fetched grievance list and integrate transaction details
	                            for (GrievanceDetails grievance : grievanceDetailsList) {
	                                String complaintId = grievanceJsonData.get("complainId").getAsString();
	                                String formattedComplaintId = complaintId.replace("\\/", "/");
	                                
	                                // Check if the complaintId is already present in the t_grievance_worklist table
	                                boolean complaintExists = grievanceDataRepo.existsByComplaintId(formattedComplaintId);
	                                if (complaintExists) {
	                                    throw new RuntimeException("Complaint ID " + formattedComplaintId + " already exists in the grievance worklist table.");
	                                }
	                                
	                                
	                                grievance.setComplaintId(formattedComplaintId);

	                                // Fetch related grievance transaction details
	                                List<GrievanceTransaction> transactionDetailsList = fetchGrievanceTransactions(grievance.getGrievanceId());

	                                if (transactionDetailsList != null && !transactionDetailsList.isEmpty()) {
	                                    // Loop through each transaction and set individual properties
	                                    for (GrievanceTransaction transactionDetails : transactionDetailsList) {

	                                    	  // Assuming transactionDetailsJson is the JSON object representing a single transaction from the API response
	                                        JsonObject transactionDetailsJson = grievanceJsonData.getAsJsonObject("transactionDetails"); // or another relevant path
	                                        
	                                        // Adding properties for each transaction detail
	                                        // Assuming these fields are coming from your API response
	                                        transactionDetails.setActionTakenBy(transactionDetailsJson.get("actionTakenBy").getAsString());
	                                        transactionDetails.setStatus(transactionDetailsJson.get("status").getAsString());
	                                        transactionDetails.setFileName(transactionDetailsJson.has(FILE_NAME) ? transactionDetailsJson.get(FILE_NAME).getAsString() : null);
	                                        transactionDetails.setFileType(transactionDetailsJson.has(FILE_TYPE) ? transactionDetailsJson.get(FILE_TYPE).getAsString() : null);
	                                        transactionDetails.setRedressed(transactionDetailsJson.get("redressed").getAsString());
	                                        transactionDetails.setCreatedAt(Timestamp.valueOf(transactionDetailsJson.get("createdAt").getAsString()));
	                                        transactionDetails.setUpdatedAt(Timestamp.valueOf(transactionDetailsJson.get("updatedAt").getAsString()));
	                                        transactionDetails.setComment(transactionDetailsJson.get("comment").getAsString());

	                                        // Save individual transaction detail in the t_grievance_transaction table
	                                        grievanceTransactionRepo.save(transactionDetails);
	                                    }

	                                    // Add the transaction list to the grievance object
	                                    grievance.setGrievanceTransactionDetails(transactionDetailsList);
	                                }

	                                // Adding other grievance-related fields (similar to the existing code)
	                                grievance.setSubjectOfComplaint(grievanceJsonData.get("subject").getAsString());
	                                ArrayList<Object[]> lists = grievanceFetchBenDetailsRepo.findByComplaintId(formattedComplaintId);

	                                for (Object[] objects : lists) {
	                                    if (objects != null && objects.length <= 4) {
	                                        grievance.setComplaintId((String) objects[0]);
	                                        grievance.setBeneficiaryRegId((Long) objects[1]);
	                                        grievance.setBencallId((Long) objects[2]);
	                                        grievance.setProviderServiceMapId((Integer) objects[3]);
	                                        String state = locationStateRepository.findByStateIDForGrievance((Integer) objects[4]);
	                                        grievance.setState(state);
	                                    }
	                                }

	                                // Setting remaining grievance properties (similar to the existing code)
	                                grievance.setAgentId(grievance.getAgentId());
	                                grievance.setDeleted(grievance.getDeleted());
	                                grievance.setCreatedBy(registeringUser);
	                                grievance.setProcessed('N');
	                                grievance.setIsAllocated(false);
	                                grievance.setCallCounter(0);
	                                grievance.setRetryNeeded(true);
	                            }

	                            // Save the grievance details to the t_grievance table
	                            grievanceDetailsListAS = (List<GrievanceDetails>) grievanceDataRepo.saveAll(grievanceDetailsList);

	                            // Combine grievance and transaction data for response 
	                            
	                            for (GrievanceDetails grievance : grievanceDetailsListAS) {
	                                Map<String, Object> combinedData = new HashMap<>();
	                                combinedData.put("complaintID", grievance.getGrievanceId());
	                                combinedData.put("subjectOfComplaint", grievance.getSubjectOfComplaint());
	                                combinedData.put("complaint", grievance.getComplaint());
	                                combinedData.put("beneficiaryRegID", grievance.getBeneficiaryRegId());
	                                combinedData.put("providerServiceMapId", grievance.getProviderServiceMapId());
	                
	                                combinedData.put("primaryNumber", grievance.getPrimaryNumber());

	                                // Add transaction data
	                                List<Map<String, Object>> transactions = new ArrayList<>();
	                                for (GrievanceTransaction transaction : grievance.getGrievanceTransactionDetails()) {
	                                    Map<String, Object> transactionData = new HashMap<>();
	                                    transactionData.put("actionTakenBy", transaction.getActionTakenBy());
	                                    transactionData.put("status", transaction.getStatus());
	                                    transactionData.put(FILE_NAME, transaction.getFileName());
	                                    transactionData.put(FILE_TYPE, transaction.getFileType());
	                                    transactionData.put("redressed", transaction.getRedressed());
	                                    transactionData.put("createdAt", transaction.getCreatedAt().toString());
	                                    transactionData.put("updatedAt", transaction.getUpdatedAt().toString());
	                                    transactionData.put("comment", transaction.getComment());
	                                    transactions.add(transactionData);
	                                }

	                                combinedData.put("transaction", transactions);
	                                combinedData.put("severity", grievance.getSeverety());
	                                combinedData.put("state", grievance.getState());
	                                combinedData.put("agentId", grievance.getAgentId());
	                                combinedData.put("deleted", grievance.getDeleted());
	                                combinedData.put("createdBy", grievance.getCreatedBy());
	                                combinedData.put("createdDate", grievance.getCreatedDate());
	                                combinedData.put("lastModDate", grievance.getLastModDate());
	                                combinedData.put("isCompleted", grievance.getIsCompleted());
	                      
	                                combinedData.put("retryNeeded", grievance.getRetryNeeded());
	                                combinedData.put("callCounter", grievance.getCallCounter());

	                                responseData.add(combinedData);
	                            }
	                        

	                            // Return the combined response as required

	                        } else {
	                            logger.info("No records found for page = {}" , count);
	                            count = -1;
	                        }
	                    }
	                }
	            }
	        } catch (Exception e) {
	            logger.error("Error in saving data into t_grievanceworklist: ", e);
	        }
	        return responseData;
	    }

	    private List<GrievanceTransaction> fetchGrievanceTransactions(Long grievanceId) {
	        List<GrievanceTransaction> transactionDetailsList = new ArrayList<>();
	        try {
	            HttpHeaders headers = new HttpHeaders();
	            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
	            headers.add(USER_AGENT_HEADER, USER_AGENT_VALUE);
	            headers.add("AUTHORIZATION", GRIEVANCE_AUTH_TOKEN);

	            HttpEntity<Object> request = new HttpEntity<Object>(headers);
	            RestTemplate restTemplate = new RestTemplate();

	            ResponseEntity<String> response = restTemplate.exchange(updateGrievanceTransactionDetails + grievanceId, HttpMethod.POST, request, String.class);

	            
	            
	            if (response != null && response.hasBody()) {
	                JSONObject obj = new JSONObject(response.getBody());
	                if (obj != null && obj.has("data") && obj.has(STATUS_CODE) && obj.getInt(STATUS_CODE) == 200) {
	                    JsonObject jsnOBJ = new JsonObject();
	                    JsonParser jsnParser = new JsonParser();
	                    JsonElement jsnElmnt = jsnParser.parse(response.getBody());
	                    jsnOBJ = jsnElmnt.getAsJsonObject();
	                    GrievanceTransaction[] transactionDetailsArray = InputMapper.gson().fromJson(jsnOBJ.get("Data").toString(), GrievanceTransaction[].class);
	                    transactionDetailsList = Arrays.asList(transactionDetailsArray);
	                }
	            }
	        } catch (Exception e) {
	            logger.error("Error fetching grievance transaction details for grievanceId " + grievanceId, e);
	        }
	        return transactionDetailsList;
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
	            String responseBody = responseEntity.getBody();
	            JsonObject jsnOBJ = new JsonObject();
	            JsonParser jsnParser = new JsonParser();
	            JsonElement jsnElmnt = jsnParser.parse(responseBody);
	            jsnOBJ = jsnElmnt.getAsJsonObject();
	            GRIEVANCE_AUTH_TOKEN = jsnOBJ.get("token_type").getAsString() + " "
	                    + jsnOBJ.get("access_token").getAsString();
	            
	            JsonObject grievanceLoginJsonData = jsnOBJ.getAsJsonObject("data");
	           
	            
	            logger.info("Auth key generated at : {}" , System.currentTimeMillis() + ", Key : {}" , GRIEVANCE_AUTH_TOKEN);

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
	                List<Map<String, Object>> savedGrievanceData = dataSyncToGrievance();
	                if (savedGrievanceData != null)
	                    break;
	                else {
	                    count--;
	              
	                if (count > 0) {
	                	Thread.sleep(5000);	                
	                	}
	                }
	            	
	            }  catch(InterruptedException e) {
	            	Thread.currentThread().interrupt();
	            	break;
	            }
	        }
	    }
	}
	    
	    public String fetchUnallocatedGrievanceCount() throws IEMRException, JSONException {
	        logger.debug("Request received for fetchUnallocatedGrievanceCount");

	        Long unallocatedCount = grievanceDataRepo.fetchUnallocatedGrievanceCount();
	        
	        if (unallocatedCount == null) {
	        	throw new IEMRException("Failed to fetch unallocated grievance count");
	        }
	        
	        JSONObject result = new JSONObject();
	        result.put("count", unallocatedCount);	        
	        return result.toString();
	    }

}
