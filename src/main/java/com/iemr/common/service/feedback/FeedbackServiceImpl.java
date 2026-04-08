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
package com.iemr.common.service.feedback;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.iemr.common.data.feedback.FeedbackDetails;
import com.iemr.common.data.feedback.FeedbackLog;
import com.iemr.common.data.feedback.FeedbackRequest;
import com.iemr.common.data.feedback.FeedbackResponse;
import com.iemr.common.data.feedback.FeedbackStatus;
import com.iemr.common.data.kmfilemanager.KMFileManager;
import com.iemr.common.data.users.EmailStatus;
import com.iemr.common.dto.identity.BeneficiariesDTO;
import com.iemr.common.mapper.BenCompleteDetailMapper;
import com.iemr.common.mapper.BenIdentityMapper;
import com.iemr.common.mapper.BenPhoneMapperDecorator;
import com.iemr.common.mapper.CommunityMapper;
import com.iemr.common.mapper.DistrictBlockMapper;
import com.iemr.common.mapper.DistrictBranchMapper;
import com.iemr.common.mapper.DistrictMapper;
import com.iemr.common.mapper.EducationMapper;
import com.iemr.common.mapper.FeedbackListMapper;
import com.iemr.common.mapper.FeedbackRequestMapper;
import com.iemr.common.mapper.FeedbackResponseMapper;
import com.iemr.common.mapper.GenderMapper;
import com.iemr.common.mapper.GovtIdentityTypeMapper;
import com.iemr.common.mapper.HealthCareWorkerMapper;
import com.iemr.common.mapper.IdentityBenEditMapper;
import com.iemr.common.mapper.MaritalStatusMapper;
import com.iemr.common.mapper.RelationshipMapper;
import com.iemr.common.mapper.SexualOrientationMapper;
import com.iemr.common.mapper.StateMapper;
import com.iemr.common.mapper.TitleMapper;
import com.iemr.common.model.beneficiary.BeneficiaryModel;
import com.iemr.common.model.feedback.FeedbackListRequestModel;
import com.iemr.common.model.feedback.FeedbackListResponseModel;
import com.iemr.common.repository.beneficiary.BeneficiaryRelationshipTypeRepository;
import com.iemr.common.repository.beneficiary.CommunityRepository;
import com.iemr.common.repository.beneficiary.EducationRepository;
import com.iemr.common.repository.beneficiary.GovtIdentityTypeRepository;
import com.iemr.common.repository.feedback.BalVivahComplaintRepo;
import com.iemr.common.repository.feedback.EmailStatusRepository;
import com.iemr.common.repository.feedback.FeedbackLogRepository;
import com.iemr.common.repository.feedback.FeedbackRepository;
import com.iemr.common.repository.feedback.FeedbackRequestRepository;
import com.iemr.common.repository.feedback.FeedbackResponseRepository;
import com.iemr.common.repository.feedback.FeedbackStatusRepository;
import com.iemr.common.repository.feedback.T_EpidemicOutbreakRepo;
import com.iemr.common.repository.location.LocationDistrictBlockRepository;
import com.iemr.common.repository.location.LocationDistrictRepository;
import com.iemr.common.repository.location.LocationDistrilctBranchRepository;
import com.iemr.common.repository.location.LocationStateRepository;
import com.iemr.common.repository.userbeneficiarydata.GenderRepository;
import com.iemr.common.repository.userbeneficiarydata.MaritalStatusRepository;
import com.iemr.common.repository.userbeneficiarydata.SexualOrientationRepository;
import com.iemr.common.repository.userbeneficiarydata.TitleRepository;
import com.iemr.common.service.beneficiary.IdentityBeneficiaryService;
import com.iemr.common.service.kmfilemanager.KMFileManagerService;
import com.iemr.common.utils.config.ConfigProperties;
import com.iemr.common.utils.mapper.InputMapper;
import com.iemr.common.utils.mapper.OutputMapper;

@Service
public class FeedbackServiceImpl implements FeedbackService {
	private Logger logger = LoggerFactory.getLogger(FeedbackServiceImpl.class);
	// private ExecutorService executor = Executors.newCachedThreadPool();


	@Value("${km-base-path}")
	private String dmsPath;
	
	@Value("${km-guest-user}")
	private String userName;
	
	@Value("${km-guest-password}")
	private String userPassword;
	
	@Value("${km-base-protocol}")
	private String dmsProtocol;	
	
	@Autowired
	private T_EpidemicOutbreakRepo t_EpidemicOutbreakRepo;

	@Autowired
	private BalVivahComplaintRepo balVivahComplaintRepo;

	/**
	 * Feedback Repository
	 */
	private FeedbackRepository feedbackRepository;

	@Autowired
	public void setFeedbackRepository(FeedbackRepository feedbackRepository) {

		this.feedbackRepository = feedbackRepository;
	}

	private FeedbackRequestRepository feedbackRequestRepository;

	@Autowired
	public void setFeedbackRequestRepository(FeedbackRequestRepository feedbackRequestRepository) {
		this.feedbackRequestRepository = feedbackRequestRepository;
	}

	private FeedbackResponseRepository feedbackResponseRepository;

	@Autowired
	public void setFeedbackResponseRepository(FeedbackResponseRepository feedbackResponseRepository) {
		this.feedbackResponseRepository = feedbackResponseRepository;
	}

	private FeedbackStatusRepository feedbackStatusRepository;

	@Autowired
	public void setFeedbackStatusRepository(FeedbackStatusRepository feedbackStatusRepository) {
		this.feedbackStatusRepository = feedbackStatusRepository;
	}

	private EmailStatusRepository emailStatusRepository;

	@Autowired
	public void setEmailStatusRepository(EmailStatusRepository emailStatusRepository) {
		this.emailStatusRepository = emailStatusRepository;
	}

	@Autowired
	private FeedbackListMapper feedbackListMapper;

	@Autowired
	private FeedbackRequestMapper feedbackRequestMapper;

	@Autowired
	private FeedbackResponseMapper feedbackResponseMapper;

	@Autowired
	private BenIdentityMapper benIdentityMapper;

	@Autowired
	private IdentityBenEditMapper identityBenEditMapper;
	@Autowired
	private BenCompleteDetailMapper benCompleteMapper;
	@Autowired
	private IdentityBeneficiaryService identityBeneficiaryService;

	@Autowired
	LocationStateRepository locationStateRepo;
	@Autowired
	StateMapper stateMapper;

	@Autowired
	LocationDistrictRepository locationDistrictRepository;

	@Autowired
	DistrictMapper districtMapper;
	@Autowired
	LocationDistrictBlockRepository blockRepository;
	@Autowired
	DistrictBlockMapper blockMapper;
	@Autowired
	LocationDistrilctBranchRepository branchRepository;
	@Autowired
	DistrictBranchMapper branchMapper;
	@Autowired
	EducationRepository educationRepository;
	@Autowired
	EducationMapper educationMapper;
	@Autowired
	CommunityRepository communityRepository;
	@Autowired
	CommunityMapper communityMapper;
	@Autowired
	BeneficiaryRelationshipTypeRepository relationshipRepository;
	@Autowired
	RelationshipMapper relationshipMapper;

	@Autowired
	GenderRepository genderRepository;
	@Autowired
	GenderMapper genderMapper;
	@Autowired
	TitleRepository titleRepository;
	@Autowired
	TitleMapper titleMapper;
	@Autowired
	MaritalStatusRepository maritalStatusRepository;
	@Autowired
	MaritalStatusMapper maritalStatusMapper;
	@Autowired
	SexualOrientationRepository sexualOrientationRepository;

	@Autowired
	SexualOrientationMapper sexualOrientationMapper;
	@Autowired
	GovtIdentityTypeRepository govtIdentityTypeRepository;
	@Autowired
	GovtIdentityTypeMapper govtIdentityTypeMapper;
	@Autowired
	HealthCareWorkerMapper healthCareWorkerMapper;
	@Autowired
	BenPhoneMapperDecorator benPhoneMapper;

	@Autowired
	FeedbackLogRepository feedbackLogRepository;

	private InputMapper inputMapper = new InputMapper();

	private static Integer feedbackStatusID;

	private static Integer emailStatusID;

	private KMFileManagerService kmFileManagerService;

	@Autowired
	public void setKmFileManagerService(KMFileManagerService kmFileManagerService) {
		this.kmFileManagerService = kmFileManagerService;
	}

	@Override
	public List<FeedbackDetails> getFeedbackRequests(Long id) {

		List<FeedbackDetails> feedbackList = new ArrayList<FeedbackDetails>();
		ArrayList<Object[]> lists = feedbackRepository.findByBeneficiaryID(id);

		for (Object[] objects : lists) {
			if (objects != null && objects.length >= 7) {
				feedbackList.add(new FeedbackDetails((Long) objects[0], (Integer) objects[1], (Integer) objects[2],
						(Integer) objects[3], (String) objects[4], (String) objects[5], (String) objects[6]));
			}
		}
		return feedbackList;
	}

	@Override
	public List<FeedbackDetails> getFeedbackRequest(Long id) {

		List<FeedbackDetails> feedbackList = new ArrayList<FeedbackDetails>();
		ArrayList<Object[]> lists = feedbackRepository.findByFeedbackId(id);

		for (Object[] objects : lists) {
			if (objects != null && objects.length >= 8) {
				feedbackList.add(new FeedbackDetails((Long) objects[0], (Integer) objects[1], (Integer) objects[2],
						(Integer) objects[3], (String) objects[4], (String) objects[5], (Timestamp) objects[6],
						(String) objects[7]));
			}
		}
		return feedbackList;
	}

	@Override
	public Integer updateFeedback(String feedbackString) throws Exception {
		FeedbackDetails feedback = inputMapper.gson().fromJson(feedbackString, FeedbackDetails.class);
		FeedbackRequest feedbackRequest = inputMapper.gson().fromJson(feedbackString.toString(), FeedbackRequest.class);
		feedbackRequest.setDeleted(false);
		FeedbackRequest savedFeedbackRequest = feedbackRequestRepository.save(feedbackRequest);
		Integer res = feedbackRequestRepository.updateFeedbackRequestStatus(feedbackRequest.getEmailStatusID(),
				feedbackRequest.getFeedbackID());
		if (feedbackRequest.getEmailStatusID() != null) {
			feedbackRepository.updateEmailStatusByID(feedbackRequest.getFeedbackID(),
					feedbackRequest.getEmailStatusID());
		}
		if (feedback.getFeedbackStatusID() != null) {
			feedbackRepository.updateFeedbackStatusByID(feedbackRequest.getFeedbackID(),
					feedback.getFeedbackStatusID());
		}
		return res;
	}

	@Override
	public String saveFeedback(String feedbackDetails) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		
		FeedbackDetails[] feedbacks = objectMapper.readValue(feedbackDetails, FeedbackDetails[].class);
		for (FeedbackDetails feedback : feedbacks) {
			if(feedback.getInstituteName() != null) {
                feedback.setInstiName(feedback.getInstituteName());
            }
			
			if (feedback.getFeedbackStatusID() == null) {
				getFeedbackStatusID();
				feedback.setFeedbackStatusID(feedbackStatusID);
			}
			if (feedback.getEmailStatusID() == null) {
				getEmailStatusID();
				feedback.setEmailStatusID(emailStatusID);
			}
		}
		Iterable<FeedbackDetails> savedFeedbackResponse = feedbackRepository.saveAll(Arrays.asList(feedbacks));

		/*
		 * for (FeedbackDetails feedback : savedFeedbackResponse) { String requestID =
		 * "GC/"+feedback.getDistrictID()+"/"+new
		 * SimpleDateFormat("ddMMyyyy").format(Calendar.getInstance().getTimeInMillis())
		 * +"/"+feedback.getFeedbackID(); feedback.setRequestID(requestID); }
		 * savedFeedbackResponse = feedbackRepository.save(savedFeedbackResponse);
		 */

		logger.info(savedFeedbackResponse.toString());
		return savedFeedbackResponse.toString();
	}

	private void getEmailStatusID() {
		if (emailStatusID == null) {
			emailStatusID = emailStatusRepository.findNewEmailStatusID();
		}
	}

	private void getFeedbackStatusID() {
		if (feedbackStatusID == null) {
			feedbackStatusID = feedbackStatusRepository.findNewFeedbackStatusID();
		}

	}

	@Override
	public String saveFeedbackResponseFromAuthority(String feedbackDetails) throws Exception {
		FeedbackDetails feedback = inputMapper.gson().fromJson(feedbackDetails, FeedbackDetails.class);
		FeedbackResponse feedbackResponse = inputMapper.gson().fromJson(feedbackDetails, FeedbackResponse.class);
		FeedbackResponse savedFeedbackResponse = null;
		savedFeedbackResponse = feedbackResponseRepository.save(feedbackResponse);
		logger.info(savedFeedbackResponse.toString());
		if (feedback.getEmailStatusID() != null) {
			feedbackRepository.updateEmailStatusByID(feedback.getFeedbackID(), feedback.getEmailStatusID());
		}
		if (feedback.getFeedbackStatusID() != null) {
			feedbackRepository.updateFeedbackStatusByID(feedback.getFeedbackID(), feedback.getFeedbackStatusID());
		}
		return savedFeedbackResponse.toString();
	}

	@Override
	public ArrayList<Object[]> getDateBetween(Timestamp startDate, Timestamp endDate) {
		return null;
	}

	@Override
	public ArrayList<Object[]> getFeedbackByID(Integer feedbackId) {
		return null;
	}

	@Override
	public String getAllData(String feedbackDetails) throws Exception {
		FeedbackDetails feedback = inputMapper.gson().fromJson(feedbackDetails, FeedbackDetails.class);
		Map<String, Object> resMap = null;
		List<Map<String, Object>> resList = new ArrayList<>();
		ArrayList<Object[]> resultSet = feedbackRepository.getAllData(feedback.getServiceID());

		if (resultSet != null && resultSet.size() > 0) {
			for (Object[] objList : resultSet) {
				resMap = new HashMap<>();
				resMap.put("feedBackID", objList[0]);
				resMap.put("iNSTUTION ID", objList[1]);
				resMap.put("designationID", objList[2]);
				resMap.put("severityID", objList[3]);
				resMap.put("feedbackStatusID", objList[4]);
				resMap.put("feedback", objList[5]);
				resMap.put("serviceID", objList[6]);
				resMap.put("userID", objList[7]);
				resMap.put("smsPhoneNo", objList[8]);
				resMap.put("serviceAvailDate", objList[9]);
				resMap.put("createdBy", objList[10]);
				resMap.put("createdDate", objList[11]);
				resMap.put("modifiedBy", objList[12]);
				resMap.put("lastModDate", objList[13]);
				resMap.put("emailStatusID", objList[14]);
				resMap.put("beneficiaryName", objList[15]);
				resMap.put("feedbackTypeName", objList[16]);
				resMap.put("institutionName", objList[17]);
				resMap.put("designationName", objList[18]);
				resMap.put("severityTypeName", objList[19]);
				resMap.put("feedbackStatus", objList[20]);
				resMap.put("serviceName", objList[21]);
				resMap.put("userName", objList[22]);
				resMap.put("emailStatus", objList[23]);
				resMap.put("feedbackAgainst", objList[24]);
				resMap.put("instituteType", objList[25]);
				resMap.put("instituteTypeID", objList[26]);
				resList.add(resMap);
			}
		}

		return OutputMapper.gsonWithoutExposeRestriction().toJson(resList);
	}

	@Override
	public FeedbackDetails createFeedback(FeedbackDetails feedbackDetails) {
		return null;
	}

	@Override
	public String updateFeedbackStatus(String feedbackRequest) throws Exception {
		FeedbackDetails details = inputMapper.gson().fromJson(feedbackRequest, FeedbackDetails.class);
		feedbackRepository.updateStatusByID(details.getFeedbackID(), details.getFeedbackStatusID(),
				details.getEmailStatusID());

		return details.getFeedbackID().toString();
	}

	@Override
	public String searchFeedback(String feedbackDetails) throws Exception {
		FeedbackDetails details = inputMapper.gson().fromJson(feedbackDetails, FeedbackDetails.class);
		Timestamp date1 = new Timestamp(details.getCreatedDate().getTime());
		ArrayList<Object[]> obj = feedbackRepository.findByDatesBetween(date1, details.getServiceID());
		return OutputMapper.gsonWithoutExposeRestriction().toJson(obj);
	}

	private class SearchFeedback {
		Timestamp startDate;
		Timestamp endDate;
		Long feedbackID;
		Integer serviceID;

		public Timestamp getEndDate() {
			return endDate;
		}

		public Timestamp getStartDate() {
			return startDate;
		}

		public Long getFeedbackID() {
			return feedbackID;
		}

		public Integer getServiceID() {
			return serviceID;
		}
	}

	@Override
	public String searchFeedback1(String request) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		SearchFeedback searchFeedback = inputMapper.gson().fromJson(request, SearchFeedback.class);
		Timestamp startDate = new Timestamp(searchFeedback.getStartDate().getTime());
		Timestamp endDate = new Timestamp(searchFeedback.getEndDate().getTime());
		Long feedbackId = searchFeedback.getFeedbackID();
		System.out.println(feedbackId);
		if (feedbackId > 0) {
			System.out.println(feedbackId);
			Map<String, Object> resMap = null;
			List<Map<String, Object>> resList = new ArrayList<>();
			ArrayList<Object[]> feedbackData = feedbackRepository.getFeedbackByID(feedbackId);
			if (feedbackData != null && feedbackData.size() > 0) {
				for (Object[] objList : feedbackData) {
					resMap = new HashMap<>();
					resMap.put("feedBackID", objList[0]);
					resMap.put("iNSTUTION ID", objList[1]);
					resMap.put("designationID", objList[2]);
					resMap.put("deverityID", objList[3]);
					resMap.put("feedbackStatusID", objList[4]);
					resMap.put("feedback", objList[5]);
					resMap.put("serviceID", objList[6]);
					resMap.put("userID", objList[7]);
					resMap.put("smsPhoneNo", objList[8]);
					resMap.put("serviceAvailDate", objList[9]);
					resMap.put("createdBy", objList[10]);
					resMap.put("createdDate", objList[11]);
					resMap.put("modifiedBy", objList[12]);
					resMap.put("lastModDate", objList[13]);
					resMap.put("emailStatusID", objList[14]);
					resMap.put("beneficiaryName", objList[15]);
					resMap.put("feedbackTypeName", objList[16]);
					resMap.put("institutionName", objList[17]);
					resMap.put("designationName", objList[18]);
					resMap.put("severityTypeName", objList[19]);
					resMap.put("feedbackStatus", objList[20]);
					resMap.put("serviceName", objList[21]);
					resMap.put("userName", objList[22]);
					resMap.put("emailStatus", objList[23]);
					resMap.put("feedbackAgainst", objList[24]);
					resMap.put("instituteType", objList[25]);
					resMap.put("instituteTypeID", objList[26]);
					resList.add(resMap);
				}
			}

			return objectMapper.writeValueAsString(resList);
		} else {
			Map<String, Object> resMap = null;
			List<Map<String, Object>> resList = new ArrayList<>();
			ArrayList<Object[]> dataa = feedbackRepository.getDateBetween(startDate, endDate,
					searchFeedback.getServiceID());

			if (dataa != null && dataa.size() > 0) {
				for (Object[] objList : dataa) {
					resMap = new HashMap<>();
					resMap.put("feedBackID", objList[0]);
					resMap.put("iNSTUTION ID", objList[1]);
					resMap.put("designationID", objList[2]);
					resMap.put("severityID", objList[3]);
					resMap.put("feedbackStatusID", objList[4]);
					resMap.put("feedback", objList[5]);
					resMap.put("serviceID", objList[6]);
					resMap.put("userID", objList[7]);
					resMap.put("smsPhoneNo", objList[8]);
					resMap.put("serviceAvailDate", objList[9]);
					resMap.put("createdBy", objList[10]);
					resMap.put("createdDate", objList[11]);
					resMap.put("modifiedBy", objList[12]);
					resMap.put("lastModDate", objList[13]);
					resMap.put("emailStatusID", objList[14]);
					resMap.put("beneficiaryName", objList[15]);
					resMap.put("feedbackTypeName", objList[16]);
					resMap.put("institutionName", objList[17]);
					resMap.put("designationName", objList[18]);
					resMap.put("severityTypeName", objList[19]);
					resMap.put("feedbackStatus", objList[20]);
					resMap.put("serviceName", objList[21]);
					resMap.put("userName", objList[22]);
					resMap.put("emailStatus", objList[23]);
					resMap.put("feedbackAgainst", objList[24]);
					resMap.put("instituteType", objList[25]);
					resMap.put("instituteTypeID", objList[26]);
					resList.add(resMap);
				}
			}

			return objectMapper.writeValueAsString(resList);
		}

	}

	@Override
	public String getFeedbackStatus(String request) {
		List<FeedbackStatus> feedbackStatus = new ArrayList<FeedbackStatus>();
		List<Object[]> feedbackResult = feedbackStatusRepository.findAllFeedbackStatus();
		for (Object[] object : feedbackResult) {
			if (object != null && object.length >= 3) {
				feedbackStatus.add(new FeedbackStatus((Integer) object[0], (String) object[1], (String) object[2]));
			}
		}
		return feedbackStatus.toString();
	}

	@Override
	public String getEmailStatus(String request) {
		List<EmailStatus> emailStatus = new ArrayList<EmailStatus>();
		List<Object[]> feedbackResult = emailStatusRepository.findAllEmailStatus();
		for (Object[] object : feedbackResult) {
			if (object != null && object.length >= 3) {
				EmailStatus emailStats = new EmailStatus();
				emailStatus.add(emailStats.getEmailStatus((Integer) object[0], (String) object[1], (String) object[2]));
			}
		}

		return emailStatus.toString();
	}

	@Override
	public String getFeedbacksList(FeedbackListRequestModel feedbackDetails, String authKey) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		List<FeedbackListResponseModel> resList = new ArrayList<FeedbackListResponseModel>();
		List<FeedbackDetails> resultSet = new ArrayList<FeedbackDetails>();
		if ((feedbackDetails.getRequestID() != null) && (feedbackDetails.getRequestID() != "")) {
			String ar[] = feedbackDetails.getRequestID().split("/");
			if ((ar.length > 0) && (ar[ar.length - 1]).matches("[0-9]+"))
			{
				logger.info("Parsed FeedbackID " + Long.parseLong(ar[ar.length - 1]));
				resultSet = feedbackRepository.findByFeedbackIDNew(Long.parseLong(ar[ar.length - 1]));
			}
	
		} else if (feedbackDetails.getPhoneNum() != null) {
			
			ArrayList<BigInteger> benRegIds=new ArrayList<BigInteger>();
			
			if(feedbackDetails.getIs1097() == true){
				 benRegIds = feedbackRepository.findByPhoneNumFor1097(feedbackDetails.getPhoneNum());
			
			}
			else
			{
				 benRegIds = feedbackRepository.findByPhoneNum(feedbackDetails.getPhoneNum());
			}
			
			List<Long> list1 = new ArrayList<>();
			for (BigInteger bi : benRegIds) {
				list1.add(bi.longValue());
			}
			if (list1 != null && list1.size() > 0)
				resultSet = feedbackRepository.findByBenRegIDs(list1);
		}

		else if ((feedbackDetails.getBeneficiaryRegID() != null) && (feedbackDetails.getBeneficiaryRegID() > 0)) {
			resultSet = feedbackRepository.getFeedbacksListForBeneficiary(feedbackDetails.getServiceID(),
					feedbackDetails.getBeneficiaryRegID());
		} else if ((feedbackDetails.getBenCallID() != null) && (feedbackDetails.getBenCallID() > 0)) {
			resultSet = feedbackRepository.getFeedbacksListForCallDetailID(feedbackDetails.getBenCallID());
		} else if ((feedbackDetails.getFeedbackTypeID() != null && feedbackDetails.getFeedbackTypeID() > 0)
				&& (feedbackDetails.getFeedbackID() == null || feedbackDetails.getFeedbackID() == 0)) {
			resultSet = feedbackRepository.getFeedbacksListByType(feedbackDetails.getServiceID(),
					feedbackDetails.getStartDate(), feedbackDetails.getEndDate(), feedbackDetails.getFeedbackTypeID());
		} else {
			if (feedbackDetails.getFeedbackID() == null || feedbackDetails.getFeedbackID() == 0) {
				resultSet = feedbackRepository.getFeedbacksList(feedbackDetails.getServiceID(),
						feedbackDetails.getStartDate(), feedbackDetails.getEndDate());
			} else {
				resultSet = feedbackRepository.getFeedbacksList(feedbackDetails.getServiceID(),
						feedbackDetails.getFeedbackID());
			}
		}

		HashSet<Long> beneficiaryRegIDs = new HashSet<>();

		for (FeedbackDetails feedback : resultSet) {
			beneficiaryRegIDs.add(feedback.getBeneficiaryRegID());
		}
	
		Map<Long, BeneficiaryModel> result = null;
		// for (FeedbackDetails feedback1 : resultSet)
		// {
		if (beneficiaryRegIDs.size() > 0) {
			List<BeneficiariesDTO> benDetailForOutboundDTOList = identityBeneficiaryService
					.getBeneficiaryListByIDs(beneficiaryRegIDs, authKey, feedbackDetails.getIs1097());

			List<BeneficiaryModel> iben = new ArrayList<BeneficiaryModel>(benDetailForOutboundDTOList.size());

			iben = getBeneficiaryListFromMapper(benDetailForOutboundDTOList);

			result = iben.stream()
					.collect(Collectors.toMap(BeneficiaryModel::getBeneficiaryRegID, Function.identity()));

		}
		if(result != null) {
		for (FeedbackDetails feedback1 : resultSet) {
			List<FeedbackRequest> feedbackRequests = getFeedbackRequestList(feedback1.getFeedbackID());
			List<FeedbackResponse> feedbackResponses = getFeedbackResponseList(feedback1.getFeedbackID());
			feedback1.setFeedbackRequests(feedbackRequests);
			feedback1.setFeedbackResponses(feedbackResponses);
			feedback1.setConsolidatedRequests(feedbackRequests, feedbackResponses);
			FeedbackListResponseModel response = feedbackListMapper.feedbackDetailsToResponse(feedback1);
			response.setBeneficiary(result.get(feedback1.getBeneficiaryRegID()));
			
			resList.add(response);
		}
		}
		return objectMapper.writeValueAsString(resList);
	}

	private List<FeedbackRequest> getFeedbackRequestList(Long feedbackID) {
		List<FeedbackRequest> feedbackRequests = new ArrayList<FeedbackRequest>();
		ArrayList<Object[]> resultSet = feedbackRequestRepository.getAllFeedback(feedbackID);
		for (Object[] object : resultSet) {
			if (object != null && object.length >= 9) {
				feedbackRequests.add(new FeedbackRequest((Long) object[0], (Long) object[1], (String) object[2],
						(Integer) object[3], (String) object[4], (Integer) object[5], (EmailStatus) object[6],
						(Timestamp) object[7], (String) object[8]));
			}
		}
		return feedbackRequests;
	}

	private List<FeedbackResponse> getFeedbackResponseList(Long feedbackID) {
		List<FeedbackResponse> feedbackResponses = new ArrayList<FeedbackResponse>();
		ArrayList<Object[]> resultSet = feedbackResponseRepository.getDataByFeedbackID(feedbackID);
		for (Object[] object : resultSet) {
			if (object != null && object.length >= 11) {
				FeedbackResponse newFeedbbackResponse = FeedbackResponse.initializeFeedbackResponse((String) object[0],
						(Long) object[1], (String) object[2], (String) object[3], (String) object[4], (Long) object[5],
						(Long) object[6], (Timestamp) object[7], (String) object[8], (Integer) object[9],
						(KMFileManager) object[10]);
				newFeedbbackResponse.setAttachmentPath(getFilePath(newFeedbbackResponse.getKmFileManager()));
				feedbackResponses.add(newFeedbbackResponse);
			}
		}
		return feedbackResponses;
	}

	@Override
	public String createFeedbackRequest(String feedbackRequestString) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		FeedbackRequest feedbackRequest = objectMapper.readValue(feedbackRequestString, FeedbackRequest.class);
		feedbackRequest = feedbackRequestRepository.save(feedbackRequest);
		if (feedbackRequest.getFeedbackRequestID() != null) {
			feedbackRepository.updateStatusByID(feedbackRequest.getFeedbackID(), feedbackRequest.getFeedbackStatusID(),
					feedbackRequest.getEmailStatusID());
		}
		return feedbackRequest.toString();
	}

	@Override
	public String updateResponse(String updateResponseString) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		FeedbackResponse feedbackResponse = objectMapper.readValue(updateResponseString, FeedbackResponse.class);

		feedbackResponse.setKmFileManagerID(null);
		Integer kmFileManagerID = 0;
		if (feedbackResponse.getKmFileManager() != null) {
			kmFileManagerID = uploadNotificationFileInKM(feedbackResponse);
			feedbackResponse.setKmFileManagerID(kmFileManagerID);
			feedbackResponse.setKmFileManager(null);
		}
		FeedbackResponse newFeedbackResponse = feedbackResponseRepository.save(feedbackResponse);
		if (feedbackResponse.getFeedbackResponseID() != null) {
			if (feedbackResponse.getFeedbackRequestID() != null) {
				feedbackRequestRepository.updateEmailStatus(feedbackResponse.getEmailStatusID(),
						feedbackResponse.getFeedbackRequestID());
			}
			if (feedbackResponse.getFeedbackID() != null) {
				feedbackRepository.updateStatusByID(feedbackResponse.getFeedbackID(),
						feedbackResponse.getFeedbackStatusID(), feedbackResponse.getEmailStatusID());
			}
		}
		newFeedbackResponse = feedbackResponseRepository.findByFeedbackResponseID(newFeedbackResponse.getFeedbackResponseID());
		newFeedbackResponse.setAttachmentPath(getFilePath(newFeedbackResponse.getKmFileManager()));
		return newFeedbackResponse.toString();
	}

	private String getFilePath(KMFileManager kmFileManager) {
		String fileUIDAsURI = null;
		if (kmFileManager != null && kmFileManager.getFileUID() != null) {
			String fileUID = kmFileManager.getFileUID();
			
			fileUIDAsURI = dmsProtocol + "://" + userName + ":" + userPassword + "@" + dmsPath + "/Download?uuid="
					+ fileUID;
		}
		return fileUIDAsURI;
	}

	@Async
	// @Transactional(propagation = Propagation.REQUIRES_NEW)
	private Integer uploadNotificationFileInKM(FeedbackResponse feedbackResponse) {
		Integer kmFileManagerID = 0;
		try {
			if (feedbackResponse.getKmFileManager() != null
					&& feedbackResponse.getKmFileManager().getFileContent() != null
					&& feedbackResponse.getKmFileManager().getFileExtension() != null
					&& feedbackResponse.getKmFileManager().getFileName() != null) {
				List<KMFileManager> list = new ArrayList<KMFileManager>();
				list.add(feedbackResponse.getKmFileManager());
				String kmFileManagerResp = kmFileManagerService.addKMFile(list.toString());
				KMFileManager[] kmFileManagerArray = inputMapper.gson().fromJson(kmFileManagerResp,
						KMFileManager[].class);
				for (KMFileManager kmFileManager : kmFileManagerArray) {
					kmFileManagerID = kmFileManager.getKmFileManagerID();
				}
			}
		} catch (Exception e) {
			logger.error("uploadNotificationFileInKM failed with error " + e.getMessage(), e);
		}
		return kmFileManagerID;
	}

	public List<BeneficiaryModel> getBeneficiaryListFromMapper(List<BeneficiariesDTO> benDetailForOutboundDTOList) {
		List<BeneficiaryModel> beneficiaryList = new ArrayList<BeneficiaryModel>();
		// List<Future<?>> futures1 = new ArrayList<Future<?>>();
		benDetailForOutboundDTOList.forEach(beneficiaryModel -> {
			// Future<?> future = executor.submit(() ->
			// {
			BeneficiaryModel beneficiary = benCompleteMapper.benDetailForOutboundDTOToIBeneficiary(beneficiaryModel);

			beneficiary.setBenPhoneMaps(benPhoneMapper.benPhoneMapToResponseByID(beneficiaryModel));
			beneficiary.setSexualOrientation(
					sexualOrientationMapper.sexualOrientationByIDToModel(beneficiary.getSexualOrientationID()));
			beneficiary.setGovtIdentityType(
					govtIdentityTypeMapper.govtIdentityTypeModelByIDToModel(beneficiary.getGovtIdentityTypeID()));
			beneficiary.setI_bendemographics(benCompleteMapper.createBenDemographicsModel(beneficiaryModel));
			beneficiary.getI_bendemographics().setHealthCareWorkerType(healthCareWorkerMapper
					.getModelByWorkerID(beneficiary.getI_bendemographics().getHealthCareWorkerID()));
			beneficiary.setM_gender(
					genderMapper.genderByIDToLoginResponse(beneficiaryModel.getBeneficiaryDetails().getGenderId()));
			beneficiary.setMaritalStatus(maritalStatusMapper
					.maritalStatusByIDToResponse(beneficiaryModel.getBeneficiaryDetails().getMaritalStatusId()));
			beneficiary
					.setM_title(titleMapper.titleByIDToResponse(beneficiaryModel.getBeneficiaryDetails().getTitleId()));

			beneficiaryList.add(beneficiary);
			// });
			// futures1.add(future);
		});

		return beneficiaryList;
	}

	@Override
	public String getGrievancesByCreatedDate(FeedbackListRequestModel feedbackDetails, String authKey)
			throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		List<FeedbackListResponseModel> resList = new ArrayList<FeedbackListResponseModel>();
		List<FeedbackDetails> resultSet = new ArrayList<FeedbackDetails>();

		resultSet = feedbackRepository.getFeedbacksList(feedbackDetails.getServiceID(), feedbackDetails.getStartDate(),
				feedbackDetails.getEndDate());

		HashSet<Long> beneficiaryRegIDs = new HashSet<>();

		for (FeedbackDetails feedback : resultSet) {
			beneficiaryRegIDs.add(feedback.getBeneficiaryRegID());
		}

		Map<Long, BeneficiaryModel> result = null;
		if (beneficiaryRegIDs.size() > 0) {
			List<BeneficiariesDTO> benDetailForOutboundDTOList = identityBeneficiaryService
					.getBeneficiaryListByIDs(beneficiaryRegIDs, authKey, feedbackDetails.getIs1097());

			List<BeneficiaryModel> iben = new ArrayList<BeneficiaryModel>(benDetailForOutboundDTOList.size());

			iben = getBeneficiaryListFromMapper(benDetailForOutboundDTOList);

			result = iben.stream()
					.collect(Collectors.toMap(BeneficiaryModel::getBeneficiaryRegID, Function.identity()));
		}
		for (FeedbackDetails feedback1 : resultSet) {
			List<FeedbackRequest> feedbackRequests = getFeedbackRequestList(feedback1.getFeedbackID());
			List<FeedbackResponse> feedbackResponses = getFeedbackResponseList(feedback1.getFeedbackID());
			feedback1.setFeedbackRequests(feedbackRequests);
			feedback1.setFeedbackResponses(feedbackResponses);
			feedback1.setConsolidatedRequests(feedbackRequests, feedbackResponses);
			FeedbackListResponseModel response = feedbackListMapper.feedbackDetailsToResponse(feedback1);

			if (feedback1.getRequestID() != null) {
				String s = feedback1.getRequestID().split("/")[0];
				if (s.equalsIgnoreCase("EC")) {
					response.setRequestType("EC");
					response.setEpidemicOutbreak(t_EpidemicOutbreakRepo.searchByRequestID(feedback1.getRequestID()));
				} else if (s.equalsIgnoreCase("BV")) {
					response.setRequestType("BV");
					response.setBalVivahComplaint(balVivahComplaintRepo.searchByRequestID(feedback1.getRequestID()));
				} else if (s.equalsIgnoreCase("GC")) {
					response.setRequestType("GC");
				}
			}
            if(result != null) {
			response.setBeneficiary(result.get(feedback1.getBeneficiaryRegID()));
            }
			resList.add(response);
		}
		return objectMapper.writeValueAsString(resList);
	}

	@Override
	public String getGrievancesByUpdatedDate(FeedbackListRequestModel feedbackDetails, String authKey)
			throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		List<FeedbackListResponseModel> resList = new ArrayList<FeedbackListResponseModel>();
		List<FeedbackDetails> resultSet = new ArrayList<FeedbackDetails>();

		resultSet = feedbackRepository.getGrievancesByUpdatedDate(feedbackDetails.getServiceID(),
				feedbackDetails.getStartDate(), feedbackDetails.getEndDate());

		HashSet<Long> beneficiaryRegIDs = new HashSet<>();

		for (FeedbackDetails feedback : resultSet) {
			beneficiaryRegIDs.add(feedback.getBeneficiaryRegID());
		}

		Map<Long, BeneficiaryModel> result = null;
		if (beneficiaryRegIDs.size() > 0) {
			List<BeneficiariesDTO> benDetailForOutboundDTOList = identityBeneficiaryService
					.getBeneficiaryListByIDs(beneficiaryRegIDs, authKey, feedbackDetails.getIs1097());

			List<BeneficiaryModel> iben = new ArrayList<BeneficiaryModel>(benDetailForOutboundDTOList.size());

			iben = getBeneficiaryListFromMapper(benDetailForOutboundDTOList);

			result = iben.stream()
					.collect(Collectors.toMap(BeneficiaryModel::getBeneficiaryRegID, Function.identity()));
		}
		for (FeedbackDetails feedback1 : resultSet) {
			List<FeedbackRequest> feedbackRequests = getFeedbackRequestList(feedback1.getFeedbackID());
			List<FeedbackResponse> feedbackResponses = getFeedbackResponseList(feedback1.getFeedbackID());
			feedback1.setFeedbackRequests(feedbackRequests);
			feedback1.setFeedbackResponses(feedbackResponses);
			feedback1.setConsolidatedRequests(feedbackRequests, feedbackResponses);
			FeedbackListResponseModel response = feedbackListMapper.feedbackDetailsToResponse(feedback1);

			if (feedback1.getRequestID() != null) {
				String s = feedback1.getRequestID().split("/")[0];
				if (s.equalsIgnoreCase("EC")) {
					response.setRequestType("EC");
					response.setEpidemicOutbreak(t_EpidemicOutbreakRepo.searchByRequestID(feedback1.getRequestID()));
				} else if (s.equalsIgnoreCase("BV")) {
					response.setRequestType("BV");
					response.setBalVivahComplaint(balVivahComplaintRepo.searchByRequestID(feedback1.getRequestID()));
				} else if (s.equalsIgnoreCase("GC")) {
					response.setRequestType("GC");
				}
			}
			if(result != null) {
			response.setBeneficiary(result.get(feedback1.getBeneficiaryRegID()));
			}
			resList.add(response);
		}
		return objectMapper.writeValueAsString(resList);
	}

	@Override
	public String saveFeedbackRequest(String feedbackRequestString) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		FeedbackRequest feedbackRequest = objectMapper.readValue(feedbackRequestString, FeedbackRequest.class);
		feedbackRequest = feedbackRequestRepository.save(feedbackRequest);

		FeedbackDetails feedbackDetailTemp = objectMapper.readValue(feedbackRequestString, FeedbackDetails.class);

		if (feedbackRequest.getFeedbackRequestID() != null) {
			FeedbackDetails feedbackDetail = feedbackRepository.getFeedbackDetail(feedbackRequest.getFeedbackID());

			if (feedbackDetail.getFeedback() != null && !feedbackDetail.getFeedback().isEmpty()) {
				if (feedbackDetail.getFeedback().equals(feedbackRequest.getFeedbackSupSummary())) {
					feedbackRepository.updateStatusByIDNew(feedbackRequest.getFeedbackID(),
							feedbackRequest.getFeedbackStatusID(), feedbackRequest.getEmailStatusID(),
							feedbackDetailTemp.getFeedbackTypeID(), feedbackDetailTemp.getInstiName(),
							feedbackDetailTemp.getInstituteTypeID(), feedbackDetailTemp.getDesignationID(),
							feedbackDetailTemp.getSeverityID(), feedbackDetailTemp.getFeedbackAgainst(),
							feedbackDetailTemp.getFeedbackNatureID(), feedbackRequest.getFeedbackSupSummary());
				} else {
					if (feedbackRequest.getFeedbackSupSummary() != null
							&& !feedbackRequest.getFeedbackSupSummary().isEmpty()) {
						feedbackRepository.updateStatusByIDNew(feedbackRequest.getFeedbackID(),
								feedbackRequest.getFeedbackStatusID(), feedbackRequest.getEmailStatusID(),
								feedbackDetailTemp.getFeedbackTypeID(), feedbackDetailTemp.getInstiName(),
								feedbackDetailTemp.getInstituteTypeID(), feedbackDetailTemp.getDesignationID(),
								feedbackDetailTemp.getSeverityID(), feedbackDetailTemp.getFeedbackAgainst(),
								feedbackDetailTemp.getFeedbackNatureID(), feedbackRequest.getFeedbackSupSummary());

						FeedbackLog feedbackLogs = new FeedbackLog();
						feedbackLogs.setFeedbackID(feedbackRequest.getFeedbackID());
						feedbackLogs.setCreatedBy(feedbackRequest.getModifiedBy());

						String changelog = "Grievance changed from " + feedbackDetail.getFeedback() + " to "
								+ feedbackRequest.getFeedbackSupSummary();
						feedbackLogs.setFeedbackLogs(changelog);

						feedbackLogs = feedbackLogRepository.save(feedbackLogs);
					} else {
						feedbackRepository.updateStatusByIDNew(feedbackRequest.getFeedbackID(),
								feedbackRequest.getFeedbackStatusID(), feedbackRequest.getEmailStatusID(),
								feedbackDetailTemp.getFeedbackTypeID(), feedbackDetailTemp.getInstiName(),
								feedbackDetailTemp.getInstituteTypeID(), feedbackDetailTemp.getDesignationID(),
								feedbackDetailTemp.getSeverityID(), feedbackDetailTemp.getFeedbackAgainst(),
								feedbackDetailTemp.getFeedbackNatureID(), feedbackRequest.getFeedbackSupSummary());

						FeedbackLog feedbackLogs = new FeedbackLog();
						feedbackLogs.setFeedbackID(feedbackRequest.getFeedbackID());
						feedbackLogs.setCreatedBy(feedbackRequest.getModifiedBy());

						String changelog = "Grievance deleted " + feedbackDetail.getFeedback();
						feedbackLogs.setFeedbackLogs(changelog);

						feedbackLogs = feedbackLogRepository.save(feedbackLogs);

					}

				}
			} else {
				if (feedbackRequest.getFeedbackSupSummary() != null
						&& !feedbackRequest.getFeedbackSupSummary().isEmpty()) {
					feedbackRepository.updateStatusByIDNew(feedbackRequest.getFeedbackID(),
							feedbackRequest.getFeedbackStatusID(), feedbackRequest.getEmailStatusID(),
							feedbackDetailTemp.getFeedbackTypeID(), feedbackDetailTemp.getInstiName(),
							feedbackDetailTemp.getInstituteTypeID(), feedbackDetailTemp.getDesignationID(),
							feedbackDetailTemp.getSeverityID(), feedbackDetailTemp.getFeedbackAgainst(),
							feedbackDetailTemp.getFeedbackNatureID(), feedbackRequest.getFeedbackSupSummary());

					FeedbackLog feedbackLogs = new FeedbackLog();
					feedbackLogs.setFeedbackID(feedbackRequest.getFeedbackID());
					feedbackLogs.setCreatedBy(feedbackRequest.getModifiedBy());

					String changelog = "Grievance changed to " + feedbackRequest.getFeedbackSupSummary();
					feedbackLogs.setFeedbackLogs(changelog);

					feedbackLogs = feedbackLogRepository.save(feedbackLogs);
				} else {
					feedbackRepository.updateStatusByIDNew(feedbackRequest.getFeedbackID(),
							feedbackRequest.getFeedbackStatusID(), feedbackRequest.getEmailStatusID(),
							feedbackDetailTemp.getFeedbackTypeID(), feedbackDetailTemp.getInstiName(),
							feedbackDetailTemp.getInstituteTypeID(), feedbackDetailTemp.getDesignationID(),
							feedbackDetailTemp.getSeverityID(), feedbackDetailTemp.getFeedbackAgainst(),
							feedbackDetailTemp.getFeedbackNatureID(), feedbackRequest.getFeedbackSupSummary());
				}
			}

		}
		return feedbackRequest.toString();
	}

	@Override
	public String getFeedbackLogs(FeedbackLog feedbackLogs) throws Exception {
		List<FeedbackLog> list = new ArrayList<FeedbackLog>();

		list = feedbackLogRepository.getFeedbackLogs(feedbackLogs.getFeedbackID());

		return list.toString();
	}
}
