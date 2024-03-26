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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.google.gson.annotations.Expose;
import com.iemr.common.data.beneficiary.BenOutboundCallAllocation;
import com.iemr.common.data.callhandling.BeneficiaryCall;
import com.iemr.common.data.callhandling.CallType;
import com.iemr.common.data.callhandling.OutboundCallRequest;
import com.iemr.common.data.callhandling.PhoneBlock;
import com.iemr.common.data.cti.CTIResponse;
import com.iemr.common.data.cti.CTIVoiceFile;
//import com.iemr.common.data.request_logger.CallLogger;
import com.iemr.common.data.service.SubService;
import com.iemr.common.data.users.ProviderServiceMapping;
import com.iemr.common.dto.identity.BeneficiariesDTO;
import com.iemr.common.dto.identity.BeneficiariesPartialDTO;
import com.iemr.common.mapper.BenCompleteDetailMapper;
import com.iemr.common.mapper.BenPhoneMapperDecorator;
import com.iemr.common.mapper.CallMapper;
import com.iemr.common.mapper.CommunityMapper;
import com.iemr.common.mapper.DistrictBlockMapper;
import com.iemr.common.mapper.DistrictBranchMapper;
import com.iemr.common.mapper.DistrictMapper;
import com.iemr.common.mapper.EducationMapper;
import com.iemr.common.mapper.GenderMapper;
import com.iemr.common.mapper.GovtIdentityTypeMapper;
import com.iemr.common.mapper.HealthCareWorkerMapper;
import com.iemr.common.mapper.MaritalStatusMapper;
import com.iemr.common.mapper.RelationshipMapper;
import com.iemr.common.mapper.SexualOrientationMapper;
import com.iemr.common.mapper.StateMapper;
import com.iemr.common.mapper.TitleMapper;
import com.iemr.common.model.beneficiary.BeneficiaryCallModel;
import com.iemr.common.model.beneficiary.BeneficiaryModel;
import com.iemr.common.model.beneficiary.CallRequestByIDModel;
import com.iemr.common.repository.beneficiary.BeneficiaryRelationshipTypeRepository;
import com.iemr.common.repository.beneficiary.CommunityRepository;
import com.iemr.common.repository.beneficiary.EducationRepository;
import com.iemr.common.repository.beneficiary.GovtIdentityTypeRepository;
import com.iemr.common.repository.callhandling.BeneficiaryCallRepository;
import com.iemr.common.repository.callhandling.DialPreferenceManualRepository;
// import com.iemr.common.repository.callhandling.DialPreferenceManualRepository;
import com.iemr.common.repository.callhandling.IEMRCalltypeRepositoryImplCustom;
import com.iemr.common.repository.callhandling.OutboundCallRequestRepository;
import com.iemr.common.repository.callhandling.PhoneBlockRepository;
import com.iemr.common.repository.location.LocationDistrictBlockRepository;
import com.iemr.common.repository.location.LocationDistrictRepository;
import com.iemr.common.repository.location.LocationDistrilctBranchRepository;
import com.iemr.common.repository.location.LocationStateRepository;
//import com.iemr.common.repository.request_logger.CallLoggerRepo;
import com.iemr.common.repository.userbeneficiarydata.GenderRepository;
import com.iemr.common.repository.userbeneficiarydata.MaritalStatusRepository;
import com.iemr.common.repository.userbeneficiarydata.SexualOrientationRepository;
import com.iemr.common.repository.userbeneficiarydata.TitleRepository;
import com.iemr.common.repository.users.ProviderServiceMapRepository;
import com.iemr.common.service.beneficiary.IdentityBeneficiaryService;
import com.iemr.common.service.cti.CTIService;
import com.iemr.common.utils.config.ConfigProperties;
import com.iemr.common.utils.exception.IEMRException;
import com.iemr.common.utils.mapper.InputMapper;
import com.iemr.common.utils.mapper.OutputMapper;
import com.iemr.common.utils.response.OutputResponse;
import com.iemr.common.utils.sessionobject.SessionObject;

@Service
public class BeneficiaryCallServiceImpl implements BeneficiaryCallService {
	@PersistenceContext
	private EntityManager entityManager;
	@PersistenceContext
	private EntityManager entityManager1;

	@Autowired
	private SessionObject s;

	// private ExecutorService executor = Executors.newCachedThreadPool();
	private static int max_nuiesance_calls = 10;
	private static int max_retry_count = 3;
	private static final int SECONDS_IN_DAY = 24 * 60 * 60;
	private static final int DISCONNECT_ONLY = 0;
	private static final int FEEDBACK_CONTINUE = 1;
	private static final int FEEDBACK_DISCONNECT = 2;

	private BeneficiaryCallRepository beneficiaryCallRepository;

	private Logger logger = LoggerFactory.getLogger(BeneficiaryCallServiceImpl.class);

	private static String ctiLoggerURL = ConfigProperties.getPropertyByName("cti-logger_base_url");
	@Autowired
	private IdentityBeneficiaryService identityBeneficiaryService;

	@Autowired
	private BenCompleteDetailMapper benCompleteMapper;

	@Autowired
	private CallMapper callMapper;

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
	HealthCareWorkerMapper healthCareWorkerMapper;

	@Autowired
	GovtIdentityTypeMapper govtIdentityTypeMapper;
	@Autowired
	BenPhoneMapperDecorator benPhoneMapper;

	@Autowired
	IEMRCalltypeRepositoryImplCustom callTypeRepository;
	@Value("${quality-Audit-PageSize}")
	private String qualityAuditPageSize;

	@Autowired
	private void setBeneficiaryCallRepository(BeneficiaryCallRepository beneficiaryCallRepository) {
		this.beneficiaryCallRepository = beneficiaryCallRepository;
	}

	private OutboundCallRequestRepository outboundCallRequestRepository;

	@Autowired
	public void setOutboundCallRequestRepository(OutboundCallRequestRepository outboundCallRequestRepository) {
		this.outboundCallRequestRepository = outboundCallRequestRepository;
	}

	// private BenPhoneMapRepository benPhoneMapRepository;
	//
	// @Autowired
	// public void setBenPhoneMapRepository(BenPhoneMapRepository
	// benPhoneMapRepository)
	// {
	// this.benPhoneMapRepository = benPhoneMapRepository;
	// }

	private ProviderServiceMapRepository providerServiceMapRepository;

	@Autowired
	public void setProviderServiceMapRepository(ProviderServiceMapRepository providerServiceMapRepository) {
		this.providerServiceMapRepository = providerServiceMapRepository;
	}

	private PhoneBlockRepository phoneBlockRepository;

	@Autowired
	public void setPhoneBlockRepository(PhoneBlockRepository phoneBlockRepository) {
		this.phoneBlockRepository = phoneBlockRepository;
	}

	// private ConfigProperties configProperties;

	// @Autowired
	// public void setConfigProperties(ConfigProperties configProperties)
	// {
	// this.configProperties = configProperties;
	// }

	private CTIService ctiService;

	@Autowired
	public void setCtiService(CTIService ctiService) {
		this.ctiService = ctiService;
	}

	@Autowired
	DialPreferenceManualRepository dialPreferenceManualRepository;

	private InputMapper inputMapper = new InputMapper();

//	@Autowired
//	private CallLogger callLogger;
//	@Autowired
//	private CallLoggerRepo callLoggerRepo;

	public BeneficiaryCall createCall(String request, String agentIPAddress) throws IEMRException {
		BeneficiaryCall benCalls = inputMapper.gson().fromJson(request, BeneficiaryCall.class);

		// removing 0 or 91 from phone no - output is 10 digit phone no
		StringBuffer phoneNo = new StringBuffer();
		if (benCalls != null && benCalls.getPhoneNo() != null && benCalls.getPhoneNo().length() > 10)
			phoneNo = new StringBuffer(benCalls.getPhoneNo().substring(benCalls.getPhoneNo().length() - 10));
		else if (benCalls != null && benCalls.getPhoneNo() != null)
			phoneNo = new StringBuffer(benCalls.getPhoneNo());

		benCalls.setPhoneNo(phoneNo.toString());

		// CallLogger callLogger = InputMapper.gson().fromJson(request,
		// CallLogger.class);

		benCalls.setCallTime(new Timestamp(Calendar.getInstance().getTimeInMillis()));
		if (benCalls.getAgentID() != null) {
			benCalls.setAgentIPAddress(ctiService.getAgentIP(benCalls.getAgentID()));
		} else if (benCalls.getAgentIPAddress() == null) {
			benCalls.setAgentIPAddress(agentIPAddress);
		}
		
		if(benCalls.getCallTypeID() == null) {

			benCalls.setCallTypeID(beneficiaryCallRepository.getCallTypeId());

		}

		if (benCalls.getCallTypeID() == null) {

			benCalls.setCallTypeID(beneficiaryCallRepository.getCallTypeId());

		}

		// changes from null to new object, in controller obj.toString() is getting used
		BeneficiaryCall savedCalls = new BeneficiaryCall();

		/// old code,06-07-2021, removing DB check for call id and introducing redis
		/// check
//		if (benCalls.getIsOutboundManualDial() != null && benCalls.getIsOutboundManualDial()) {
//			if (benCalls.getCallID() != null) {
//				BeneficiaryCall benCallDetails = beneficiaryCallRepository.getExistingCallId(benCalls.getCallID());
//				if (benCallDetails != null && benCallDetails.getBenCallID() != null)
//					throw new IEMRException("call ID already present in table");
//				else
//					savedCalls = beneficiaryCallRepository.save(benCalls);
//
//			}
//		} else
//			savedCalls = beneficiaryCallRepository.save(benCalls);
		/// end old code

		/// new code, 06-07-2021, NE298657
		String key = null;
		if (benCalls != null && benCalls.getCallID() != null && benCalls.getAgentID() != null) {
			try {
				key = benCalls.getCallID().toString() + "." + benCalls.getAgentID().toString();
				String agentID = s.getSessionObject(key);
				if (!agentID.equalsIgnoreCase(benCalls.getAgentID())) {
					s.setSessionObject(key, benCalls.getAgentID());
					logger.info("Start call data to be saved in DB - " + benCalls);
					savedCalls = beneficiaryCallRepository.save(benCalls);
					logger.info("Start call data response after saving DB - " + savedCalls);
				} else {
					ArrayList<BeneficiaryCall> resultSetBC = beneficiaryCallRepository
							.getExistingBCByCallIDAndAgentID(benCalls.getCallID(), benCalls.getAgentID());
					logger.info("start call query response for getExistingBCByCallIDAndAgentID - " + resultSetBC.size());					
					if (resultSetBC != null && resultSetBC.size() > 0) {
						savedCalls = resultSetBC.get(resultSetBC.size() - 1);
					logger.info("Start call data if agent id is not equals to request agent id - " + savedCalls);
					}
//					else
//						savedCalls = beneficiaryCallRepository.save(benCalls);
				}
			} catch (Exception e) {
				if (e.getMessage().equalsIgnoreCase("Unable to fetch session object from Redis server")) {
					if (key != null)
						s.setSessionObject(key, benCalls.getAgentID());
					savedCalls = beneficiaryCallRepository.save(benCalls);
				} else
					throw new IEMRException("unable to create ben_call_id : " + e.getMessage());
			}

		} else {
			logger.error("Error in saving start call data - call id is null");
		    throw new IEMRException("call id is null");
		}
		/// end new code

		return savedCalls;
	}

	private class FollowupRequired {
		Boolean isFollowupRequired;
	}

	@Override
	public Integer closeCall(String request, String ipAddress) throws Exception {
//		CallLogger callLogger = InputMapper.gson().fromJson(request, CallLogger.class);
//		if (callLogger != null)
//		{
//			callLogger.setRequestOBJ(request);
//			callLogger.setAPIName("call/closeCall");
//			callLoggerRepo.save(callLogger);
//		}
//		else
//			logger.info("create request is invalid for call");

		Integer updateCounts = 0;
		BeneficiaryCall benificiaryCall = inputMapper.gson().fromJson(request, BeneficiaryCall.class);
		FollowupRequired followupRequired = inputMapper.gson().fromJson(request, FollowupRequired.class);

		// referral
		if (benificiaryCall != null && benificiaryCall.getInstNames() != null
				&& benificiaryCall.getInstNames().length > 0) {

			StringBuffer sb = new StringBuffer();
			for (String instNam : benificiaryCall.getInstNames()) {
				sb.append(instNam).append("||");
			}
			if (sb.length() > 2)
				benificiaryCall.setInstName(sb.substring(0, sb.length() - 2));
		}

		// logger.info("Closing call for benCallID "+benificiaryCall.getBenCallID()
		// +"for request object "+ request);
		updateCounts = beneficiaryCallRepository.closeCall(benificiaryCall.getBenCallID(), benificiaryCall.getRemarks(),
				new Timestamp(Calendar.getInstance().getTimeInMillis()), benificiaryCall.getCallClosureType(),
				benificiaryCall.getCallTypeID(), benificiaryCall.getDispositionStatusID(),
				benificiaryCall.getEmergencyType(), benificiaryCall.getExternalReferral(),
				benificiaryCall.getInstTypeId(), benificiaryCall.getInstName(),benificiaryCall.getIsOutbound());

		if (benificiaryCall.getBeneficiaryRegID() != null)
			beneficiaryCallRepository.updateBeneficiaryRegIDInCall(benificiaryCall.getBenCallID(),
					benificiaryCall.getBeneficiaryRegID());
		if (followupRequired.isFollowupRequired) {
			OutboundCallRequest outboundCallRequest = inputMapper.gson().fromJson(request, OutboundCallRequest.class);
			outboundCallRequestRepository.save(outboundCallRequest);
		}
		if (benificiaryCall.getFitToBlock()) {
			updateFitToBlock(benificiaryCall.getBenCallID());
		}
		if (benificiaryCall.getEndCall()) {
			// Below code is added to update call ended by user ID
			BeneficiaryCall benificiaryCallAtClose = beneficiaryCallRepository.findOne(benificiaryCall.getBenCallID());
			logger.info("Updated close call for "
					+ beneficiaryCallRepository.updateBeneficiaryCallEndedByUserID(
							benificiaryCallAtClose.getCallReceivedUserID(), benificiaryCallAtClose.getCallID())
					+ " rows where session ID was " + benificiaryCallAtClose.getCallID());

			if (benificiaryCall.getIsTransfered() == null || benificiaryCall.getIsTransfered() == false) {
				updateCallDisposition(benificiaryCall, benificiaryCall.getAgentIPAddress());

				Thread.sleep(1000);
				disconnectCallInCTI(benificiaryCall);
			}
		}

		return updateCounts;
	}

	@Override
	public Integer closeCallV1(String request, String ipAddress) throws Exception {
		Integer updateCounts = 0;
		BeneficiaryCall benificiaryCall = inputMapper.gson().fromJson(request, BeneficiaryCall.class);
		FollowupRequired followupRequired = inputMapper.gson().fromJson(request, FollowupRequired.class);

		// referral
		if (benificiaryCall != null && benificiaryCall.getInstNames() != null
				&& benificiaryCall.getInstNames().length > 0) {

			StringBuffer sb = new StringBuffer();
			for (String instNam : benificiaryCall.getInstNames()) {
				sb.append(instNam).append("||");
			}
			if (sb.length() > 2)
				benificiaryCall.setInstName(sb.substring(0, sb.length() - 2));
		}

		updateCounts = beneficiaryCallRepository.closeCall(benificiaryCall.getBenCallID(), benificiaryCall.getRemarks(),
				new Timestamp(Calendar.getInstance().getTimeInMillis()), benificiaryCall.getCallClosureType(),
				benificiaryCall.getCallTypeID(), benificiaryCall.getDispositionStatusID(),
				benificiaryCall.getEmergencyType(), benificiaryCall.getExternalReferral(),
				benificiaryCall.getInstTypeId(), benificiaryCall.getInstName(),benificiaryCall.getIsOutbound());
		if (followupRequired.isFollowupRequired) {
			OutboundCallRequest outboundCallRequest = inputMapper.gson().fromJson(request, OutboundCallRequest.class);
			outboundCallRequestRepository.save(outboundCallRequest);
		}
		if (benificiaryCall.getFitToBlock()) {
			updateFitToBlock(benificiaryCall.getBenCallID());
		}
		if (benificiaryCall.getEndCall()) {
			updateCallDisposition(benificiaryCall, benificiaryCall.getAgentIPAddress());
			disconnectCallInCTI(benificiaryCall);
		}

		return updateCounts;
	}

	/**
	 * 104-CPU Utilization issue
	 */
	@Async
	private void updateFileRecordingsPath(String callID, String ipAddress) {
//		try {
//			List<BeneficiaryCall> benificiaryCalls = beneficiaryCallRepository.findCallDetails(callID);
//			for (BeneficiaryCall beneficiaryCall : benificiaryCalls) {
//
//				if (beneficiaryCall.getAgentID() != null && beneficiaryCall.getCallID() != null) {
//					JSONObject request = new JSONObject();
//					request.put("agent_id", beneficiaryCall.getAgentID());
//					request.put("session_id", beneficiaryCall.getCallID());
//					JSONObject ctiResponse = new JSONObject();
//					OutputResponse response;
//					logger.info("calling createVoiceFile " + request);
//					response = ctiService.createVoiceFile(request.toString(), ipAddress);
//					logger.info("calling createVoiceFile response " + response);
////					if (response.getStatusCode() == OutputResponse.SUCCESS) {
////						CTIVoiceFile createVoiceFile = inputMapper.gson().fromJson(response.getData(),
////								CTIVoiceFile.class);
////						logger.info("calling getVoiceFile " + request);
////						OutputResponse response1 = ctiService.getVoiceFile(request.toString(), ipAddress);
////						logger.info("calling getVoiceFile response " + response);
////
////						if (response1.getStatusCode() == OutputResponse.SUCCESS) {
////							CTIVoiceFile getVoiceFile = inputMapper.gson().fromJson(response1.getData(),
////									CTIVoiceFile.class);
////							String recordingFilePath = getVoiceFile.getPath() + "/" + getVoiceFile.getFilename();
////							beneficiaryCallRepository.updateVoiceFilePath(beneficiaryCall.getBenCallID(),
////									recordingFilePath, null);
////						}
////					}
//				}
//			}
//		} catch (Exception e) {
//			logger.error("updateFileRecordingsPath failed with error " + e.getMessage(), e);
//		}
	}

	// @Async
	private void disconnectCallInCTI(BeneficiaryCall callToClose) {
		try {
			JSONObject request = new JSONObject();
			request.put("agent_id", callToClose.getAgentID());
			request.put("call_id", callToClose.getCallID());
			request.put("isFeedback",
					(callToClose.getIsFeedback() != null)
							? (callToClose.getIsFeedback() ? FEEDBACK_DISCONNECT : DISCONNECT_ONLY)
							: DISCONNECT_ONLY);
			ctiService.disconnectCall(request.toString(), callToClose.getAgentIPAddress());
			BeneficiaryCall callToClose1 = beneficiaryCallRepository.findOne(callToClose.getBenCallID());
			// 17-08-2020, new API shared by C-zentrix to create file and share path in 1
			// API
			// updateFileRecordingsPath(callToClose1.getCallID(),
			// callToClose1.getAgentIPAddress());
		} catch (Exception e) {
			logger.error("disconnectCallInCTI failed with error " + e.getMessage(), e);
		}
	}

	@Async
	private void updateCallDisposition(BeneficiaryCall benCall, String agentIP) {
		try {
			BeneficiaryCall callData = beneficiaryCallRepository.findCallDetails(benCall.getBenCallID());
			JSONObject dispRequestObj = new JSONObject();
			dispRequestObj.put("session_id", callData.getCallID());
			dispRequestObj.put("cust_disp", callData.getCallTypeObj().getCallType());
			dispRequestObj.put("agent_id", callData.getAgentID());
			dispRequestObj.put("category", callData.getCallTypeObj().getCallGroupType());
			logger.info(
					ctiService.setCallDisposition(dispRequestObj.toString(), benCall.getAgentIPAddress()).toString());

		} catch (Exception e) {
			logger.error("updateCallDisposition failed with error " + e.getMessage(), e);
		}
	}

	private void updateFitToBlock(Long benCallID) {
		BeneficiaryCall callData = beneficiaryCallRepository.findCallDetails(benCallID);
		Set<PhoneBlock> resultSet = phoneBlockRepository.getPhoneBlockStatus(callData.getCalledServiceID(),
				callData.getPhoneNo());
		if (resultSet.size() > 0) {
			updatePhoneBlockCountAndStatus(resultSet, callData);
		} else {
			createPhoneBlockData(callData);
		}
	}

	private void createPhoneBlockData(BeneficiaryCall callData) {
		ProviderServiceMapping providerServiceMapping = providerServiceMapRepository
				.findByID(callData.getCalledServiceID());
		PhoneBlock phoneBlock = PhoneBlock.initializePhoneBlockForCreate(null, callData.getPhoneNo(),
				callData.getCalledServiceID(), 1, false, providerServiceMapping.getCtiCampaignName(), null, null,
				callData.getBenCallID().toString());
		phoneBlock.setCreatedBy(callData.getCreatedBy());
		logger.info("Phone block entry: " + OutputMapper.gsonWithoutExposeRestriction().toJson(phoneBlock));
		logger.info(phoneBlockRepository.save(phoneBlock) + "");
	}

	private void updatePhoneBlockCountAndStatus(Set<PhoneBlock> resultSet, BeneficiaryCall callData) {
		Calendar cal = Calendar.getInstance();
		Timestamp blockStartDate = new Timestamp(cal.getTimeInMillis());
		Integer blockDays = ConfigProperties.getInteger("block-duration-days") * SECONDS_IN_DAY;
		max_nuiesance_calls = ConfigProperties.getInteger("max-nuiesance-call");
		cal.add(Calendar.SECOND, blockDays);
		Timestamp blockEndDate = new Timestamp(cal.getTimeInMillis());
		for (PhoneBlock phoneBlock : resultSet) {
			String ids = (phoneBlock.getCallIDs() == null) ? "" : (phoneBlock.getCallIDs() + ",");
			if (callData.getBenCallID() != null) {
				ids = ids + callData.getBenCallID();
			}
			if (ids.trim().length() == 0) {
				ids = null;
			}
			phoneBlock.setCallIDs(ids);
			phoneBlockRepository.updateNuisanceCallCount(callData.getCalledServiceID(), callData.getPhoneNo(), ids);
			Integer noOfNuisanceCall = phoneBlock.getNoOfNuisanceCall();
			if (noOfNuisanceCall >= (max_nuiesance_calls - 1)) {
				OutputResponse blockResponse = blockUnblockPhoneNoInCTI(
						phoneBlock.getProviderServiceMapping().getCtiCampaignName(), phoneBlock.getPhoneNo(), true);
				if (blockResponse.isSuccess()) {
					phoneBlockRepository.phoneNoBlockUnblock(callData.getCalledServiceID(), callData.getPhoneNo(), true,
							phoneBlock.getProviderServiceMapping().getCtiCampaignName(), noOfNuisanceCall,
							blockStartDate, blockEndDate, callData.getCreatedBy(), phoneBlock.getCallIDs());
				}
			}
		}
	}

	@Async
	private OutputResponse blockUnblockPhoneNoInCTI(String ctiCampaignName, String phoneNo, Boolean isBlocked) {
		OutputResponse response = new OutputResponse();
		try {
			String blockUnblockURL = "";
			JSONObject requestObj = new JSONObject();
			requestObj.put("phoneNo", phoneNo);
			requestObj.put("campaignName", ctiCampaignName);
			if (isBlocked) {
				// blockUnblockURL =
				// ConfigProperties.getPropertyByName("block-api-URL");
				response = ctiService.blockNumber(requestObj.toString(), "127.0.0.1");
			} else {
				// blockUnblockURL =
				// ConfigProperties.getPropertyByName("unblock-api-URL");
				response = ctiService.unblockNumber(requestObj.toString(), "127.0.0.1");
			}
			// String ctiServer =
			// ConfigProperties.getPropertyByName("cti-server-ip");
			// blockUnblockURL = blockUnblockURL.replace("CTI_SERVER",
			// ctiServer).replace("MOBILE", phoneNo)
			// .replace("CAMPAIGN_NAME", ctiCampaignName);
			// HttpUtils httpUtils = new HttpUtils();

			// logger.info(httpUtils.get(blockUnblockURL));
		} catch (Exception e) {
			logger.error("blockUnblockPhoneNoInCTI failed with error " + e.getMessage(), e);
		}
		return response;
	}

	@Override
	public Integer updateBeneficiaryIDInCall(String request) throws IEMRException {
//		CallLogger callLogger = InputMapper.gson().fromJson(request, CallLogger.class);
//		if (callLogger != null)
//		{
//			callLogger.setRequestOBJ(request);
//			callLogger.setAPIName("call/updatebeneficiaryincall");
//			callLoggerRepo.save(callLogger);
//		}
//		else
//			logger.info("create request is invalid for call");

		Integer updateCounts = 0;
		BeneficiaryCall benificiaryCallId = inputMapper.gson().fromJson(request, BeneficiaryCall.class);
		updateCounts = beneficiaryCallRepository.updateBeneficiaryIDInCall(benificiaryCallId.getBenCallID(),
				benificiaryCallId.getBeneficiaryRegID(), benificiaryCallId.getIsCalledEarlier());
		logger.info("update count for updateBeneficiaryIDInCall " + updateCounts);
		return updateCounts;
	}

	@Override
	public void updateBenCallIdsInPhoneBlock() {
		List<CallType> callTypes = callTypeRepository.getFitToBlockCallTypes();
		List<PhoneBlock> blockedNumbers = phoneBlockRepository.getPhoneBlocks();
		Map<Integer, List<Integer>> result = new HashMap<Integer, List<Integer>>();
		HashSet<Integer> providerServiceIDs = new HashSet<>();

		for (CallType callType : callTypes) {
			if (result.get(callType.getProviderServiceMapID()) == null) {
				result.put(callType.getProviderServiceMapID(), new ArrayList<Integer>());
			}
			result.get(callType.getProviderServiceMapID()).add(callType.getCallTypeID());
		}
		if (result.size() > 0) {
			for (PhoneBlock blockedNumber : blockedNumbers) {
				List<BeneficiaryCall> invalidCalls = beneficiaryCallRepository.getCallHistoryByCallID(
						blockedNumber.getPhoneNo(), blockedNumber.getProviderServiceMapID(),
						result.get(blockedNumber.getProviderServiceMapID()),
						new PageRequest(0, blockedNumber.getNoOfNuisanceCall()));
				String callIDs = null;
				for (BeneficiaryCall beneficiaryCall : invalidCalls) {
					if (callIDs == null) {
						callIDs = beneficiaryCall.getBenCallID() + "";
					} else {
						callIDs = callIDs + "," + beneficiaryCall.getBenCallID();
					}
				}
				phoneBlockRepository.updateCallIDs(blockedNumber.getPhoneBlockID(), callIDs);
			}
		}
	}

	@Override
	public String outboundCallList(String request, String auth) throws IEMRException {
		OutboundCallRequest callRequest = inputMapper.gson().fromJson(request, OutboundCallRequest.class);
		List<OutboundCallRequest> outboundCallRequests = new ArrayList<OutboundCallRequest>();
		Calendar cal = Calendar.getInstance();
		Timestamp startDate = new Timestamp(cal.getTimeInMillis());
		cal.add(Calendar.DATE, 7);
		Timestamp lastDate = new Timestamp(cal.getTimeInMillis());
		Set<Objects[]> resultSet = null;
		String executionCondition = getConditionForOutboundSerach(callRequest);

		// (date)(provider check)(service check)(user check)(language check)
		switch (executionCondition) {
		case "01010":
		case "01110":
		case "01111":
		case "11010":
		case "11110":
		case "11111":
			/* get calls assigned to user against provider */
			resultSet = outboundCallRequestRepository.getAllOutboundCalls(callRequest.getProviderServiceMapID(),
					callRequest.getAssignedUserID());
			break;
		case "01100":
		case "01101":
		case "11100":
		case "11101":
			/* get calls for a service against provider */
			resultSet = outboundCallRequestRepository.getAllOutboundCallsWithServID(
					callRequest.getProviderServiceMapID(), callRequest.getRequestedServiceID());
			break;
		case "01001":
			/* get calls for a user against language and provider */
			resultSet = outboundCallRequestRepository.getAllOutboundCallsByLanguage(
					callRequest.getProviderServiceMapID(), callRequest.getPreferredLanguageName());
			break;
		case "11001":
			/* get calls for a user against language and provider */
			startDate = (callRequest.getStartDate() != null) ? callRequest.getStartDate() : startDate;
			lastDate = (callRequest.getEndDate() != null) ? callRequest.getEndDate() : lastDate;
			resultSet = outboundCallRequestRepository.getAllOutboundCallsByLanguage(
					callRequest.getProviderServiceMapID(), startDate, lastDate, callRequest.getPreferredLanguageName());
			break;

		case "01011":
			resultSet = outboundCallRequestRepository.getAllOutboundCallsByLanguage(
					callRequest.getProviderServiceMapID(), callRequest.getPreferredLanguageName(),
					callRequest.getAssignedUserID());
			break;
		case "11011":
			startDate = (callRequest.getStartDate() != null) ? callRequest.getStartDate() : startDate;
			lastDate = (callRequest.getEndDate() != null) ? callRequest.getEndDate() : lastDate;
			resultSet = outboundCallRequestRepository.getAllOutboundCallsByLanguage(
					callRequest.getProviderServiceMapID(), startDate, lastDate, callRequest.getPreferredLanguageName(),
					callRequest.getAssignedUserID());
			break;
		case "01000":
		case "11000":
			/* get calls for a user against provider */
			startDate = (callRequest.getStartDate() != null) ? callRequest.getStartDate() : startDate;
			lastDate = (callRequest.getEndDate() != null) ? callRequest.getEndDate() : lastDate;
			resultSet = outboundCallRequestRepository.getAllOutboundCalls(callRequest.getProviderServiceMapID(),
					startDate, lastDate);
			break;
		}
		HashSet<Long> beneficiaryRegIDs = new HashSet<>();
		for (Object[] object : resultSet) {
			if (object != null && object.length >= 10) {
				beneficiaryRegIDs.add((Long) object[1]);
			}
		}

		Map<Long, BeneficiaryModel> result = null;
		if (beneficiaryRegIDs.size() > 0) {
			List<BeneficiariesDTO> benDetailForOutboundDTOList = identityBeneficiaryService
					.getBeneficiaryListByIDs(beneficiaryRegIDs, auth, callRequest.getIs1097());
			List<BeneficiaryModel> iben = new ArrayList<BeneficiaryModel>(benDetailForOutboundDTOList.size());
			iben = getBeneficiaryListFromMapper(benDetailForOutboundDTOList);
			result = iben.stream()
					.collect(Collectors.toMap(BeneficiaryModel::getBeneficiaryRegID, Function.identity()));
		}

		for (Object[] object : resultSet) {
			if (object != null && object.length >= 14 && result != null) {

				outboundCallRequests.add(new OutboundCallRequest().getOutboundCall((Long) object[0],
						(BeneficiaryModel) result.get((Long) object[1]), (Timestamp) object[2], (Integer) object[3],
						(String) object[4], /* (User) object[5] */null, (Integer) object[6], (SubService) object[7],
						(String) object[8], (String) object[9], (String) object[10], (Integer) object[11],
						(Boolean) object[12], (Timestamp) object[13]));

			}
		}
		return OutputMapper.gsonWithoutExposeRestriction().toJson(outboundCallRequests);
	}

	/**
	 * @param OutboundCallRequest
	 * @return String Value will be combination of the multiple conditions like
	 *         (date)(provider check)(service check)(user check)(language check)
	 */
	private String getConditionForOutboundSerach(OutboundCallRequest callRequest) {
		String result = "";
		result = result + ((callRequest.getStartDate() != null) ? 1 : 0);
		result = result + ((callRequest.getProviderServiceMapID() != null) ? 1 : 0);
		result = result + ((callRequest.getRequestedServiceID() != null) ? 1 : 0);
		result = result + ((callRequest.getAssignedUserID() != null) ? 1 : 0);
		result = result + ((callRequest.getPreferredLanguageName() != null) ? 1 : 0);
		return result;
	}

	private class FilteredCallList {
		@Expose
		String name;
		@Expose
		Long benCallID;
		@Expose
		Long beneficiaryRegID;
		@Expose
		String beneficiaryID;
		@Expose
		String remarks;
		@Expose
		String callType;
		@Expose
		Timestamp callTime;
		@Expose
		String phoneNo;
		@Expose
		String cDICallStatus;
		@Expose
		Timestamp lastCalledOn;
		@Expose
		String recordingPath;
		@Expose
		String agentID;
		@Expose
		BeneficiaryModel beneficiaryModel;
		@Expose
		String callID;
//		@Expose
//		int pageSize;

		// old constructor with file path
//		public FilteredCallList(Long benCallID, Long beneficiaryRegID, String name, String remarks, String callType,
//				Timestamp callEndTime, String phoneNo, String cDICallStatus, String beneficiaryID,
//				Timestamp lastCalledOn, String recordingPath, String agentID, BeneficiaryModel beneficiaryModel) {
//			this.benCallID = benCallID;
//			this.beneficiaryRegID = beneficiaryRegID;
//			this.beneficiaryID = beneficiaryID;
//			this.name = name;
//			this.remarks = remarks;
//			this.callTime = callEndTime;
//			this.callType = callType;
//			this.phoneNo = phoneNo;
//			this.cDICallStatus = cDICallStatus;
//			this.lastCalledOn = lastCalledOn;
//			this.recordingPath = recordingPath;
//			this.agentID = agentID;
//			this.beneficiaryModel = beneficiaryModel;
//			
//		}
		// new constructor with sessionId and removed file path
		public FilteredCallList(Long benCallID, Long beneficiaryRegID, String name, String remarks, String callType,
				Timestamp callEndTime, String phoneNo, String cDICallStatus, String beneficiaryID,
				Timestamp lastCalledOn, String agentID, BeneficiaryModel beneficiaryModel, String callID) {
			this.benCallID = benCallID;
			this.beneficiaryRegID = beneficiaryRegID;
			this.beneficiaryID = beneficiaryID;
			this.name = name;
			this.remarks = remarks;
			this.callTime = callEndTime;
			this.callType = callType;
			this.phoneNo = phoneNo;
			this.cDICallStatus = cDICallStatus;
			this.lastCalledOn = lastCalledOn;
			this.agentID = agentID;
			this.beneficiaryModel = beneficiaryModel;
			this.callID = callID;
		}

//		public FilteredCallList(Long benCallID, Long beneficiaryRegID, String name, String remarks, String callType,
//				Timestamp callEndTime, String phoneNo, String cDICallStatus, String beneficiaryID,
//				Timestamp lastCalledOn, String recordingPath, String agentID, BeneficiaryModel beneficiaryModel,
//				int pageSize) {
//			this.benCallID = benCallID;
//			this.beneficiaryRegID = beneficiaryRegID;
//			this.beneficiaryID = beneficiaryID;
//			this.name = name;
//			this.remarks = remarks;
//			this.callTime = callEndTime;
//			this.callType = callType;
//			this.phoneNo = phoneNo;
//			this.cDICallStatus = cDICallStatus;
//			this.lastCalledOn = lastCalledOn;
//			this.recordingPath = recordingPath;
//			this.agentID = agentID;
//			this.beneficiaryModel = beneficiaryModel;
//			this.pageSize = pageSize;
//		}

		@Override
		public String toString() {
			return OutputMapper.gson().toJson(this);
		}
	}

	/**
	 * 17-09-2019, Paagination implementd in this service
	 */

	@Override
	public String filterCallListWithPagination(String request, String auth) throws IEMRException {
		Map<String, Object> responseMap = new HashMap<>();
		BeneficiaryCall callRequest = inputMapper.gson().fromJson(request, BeneficiaryCall.class);
		List<FilteredCallList> filteredCalls = new ArrayList<FilteredCallList>();
		Calendar today = Calendar.getInstance();
		Calendar startDate = Calendar.getInstance();
		startDate.add(Calendar.DAY_OF_YEAR, -7);
		Timestamp filterStartDate = (callRequest.getFilterStartDate() != null) ? callRequest.getFilterStartDate()
				: (new Timestamp(startDate.getTimeInMillis()));
		Timestamp filterEndDate = (callRequest.getFilterEndDate() != null) ? callRequest.getFilterEndDate()
				: (new Timestamp(today.getTimeInMillis()));

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

		CriteriaQuery<BeneficiaryCall> criteriaQuery = criteriaBuilder.createQuery(BeneficiaryCall.class);
		Root<BeneficiaryCall> root = criteriaQuery.from(BeneficiaryCall.class);

		// add predicates to see more parameters
		List<Predicate> predicates = new ArrayList<Predicate>();
		List<BeneficiaryCall> resultSet = null;
		predicates.add(criteriaBuilder.like(root.get("receivedRoleName"),
				(callRequest.getReceivedRoleName() == null) ? "%%" : callRequest.getReceivedRoleName()));

		predicates.add(criteriaBuilder.like(root.get("agentID"),
				(callRequest.getAgentID() == null) ? "%%" : callRequest.getAgentID()));

//		if (callRequest.getCallTypeID() != null) {
//			predicates.add(criteriaBuilder.equal(root.get("callTypeID"), callRequest.getCallTypeID()));
//		}

		predicates.add(criteriaBuilder.like(root.get("phoneNo"),
				(callRequest.getPhoneNo() == null) ? "%%" : callRequest.getPhoneNo()));

		if (callRequest.getInboundOutbound() != null) {
			callRequest.setIsOutbound(callRequest.getInboundOutbound().equalsIgnoreCase("outbound") ? true : false);
			predicates.add(criteriaBuilder.equal(root.get("isOutbound"),
					(callRequest.getInboundOutbound().equalsIgnoreCase("outbound") ? true : false)));
		}
		if (callRequest.getBenCallIDs() == null) {
			predicates.add(criteriaBuilder.between(root.get("createdDate"), filterStartDate, filterEndDate));
		} else {
			Expression<String> callListExpression = root.get("benCallID");
			predicates.add(callListExpression.in(Arrays.asList(callRequest.getBenCallIDs().split(","))));
		}

		predicates.add(criteriaBuilder.equal(root.get("calledServiceID"), callRequest.getCalledServiceID()));

		if (callRequest.getCDICallStatus() != null && !callRequest.getCDICallStatus().trim().equalsIgnoreCase("All")) {
			predicates.add(criteriaBuilder.equal(root.get("cDICallStatus"), callRequest.getCDICallStatus()));
		}

		// filter only valid and transfered call
		List<String> callTypes = new ArrayList<>();
		callTypes.add("Valid");
		callTypes.add("Transfer");

		Expression<String> exp = root.get("callTypeObj").get("callGroupType");

		predicates.add(exp.in(callTypes));

		predicates.add(criteriaBuilder.notEqual(root.get("callTypeObj").get("callType"), "test"));

		// grouping by callID from C-zentric
		criteriaQuery.groupBy(root.get("callID"));

		// get total count for pagination
		Long totalCount = getTotalCallCountWithPaginationFor104(criteriaBuilder, callRequest, filterStartDate,
				filterEndDate);

		criteriaQuery.select(root).where(predicates.toArray(new Predicate[] {}));

		TypedQuery<BeneficiaryCall> typedQuery = entityManager.createQuery(criteriaQuery);

		if (callRequest.getPageSize() != null && callRequest.getPageNo() != null) {
			typedQuery.setMaxResults(callRequest.getPageSize())
					.setFirstResult((callRequest.getPageNo() - 1) * callRequest.getPageSize());
		}

		resultSet = typedQuery.getResultList();
		HashSet<Long> beneficiaryRegIDs = new HashSet<>();

		StringBuffer phoneNo = new StringBuffer();
		for (BeneficiaryCall call : resultSet) {
			beneficiaryRegIDs.add(call.getBeneficiaryRegID());

			// removing 0 or 91 from phone no - output is 10 digit phone no
			if (call.getPhoneNo() != null && call.getPhoneNo().length() > 10)
				phoneNo = new StringBuffer(call.getPhoneNo().substring(call.getPhoneNo().length() - 10));
			else if (call.getPhoneNo() != null)
				phoneNo = new StringBuffer(call.getPhoneNo());

			call.setPhoneNo(phoneNo.toString());
		}

		beneficiaryRegIDs.removeIf(Objects::isNull);
		Map<Long, BeneficiaryModel> result = null;
		if (beneficiaryRegIDs.size() > 0) {
			List<BeneficiariesDTO> benDetailForOutboundDTOList = identityBeneficiaryService
					.getBeneficiaryListByIDs(beneficiaryRegIDs, auth, callRequest.getIs1097());
			List<BeneficiaryModel> beneficaries = getBeneficiaryListFromMapper(benDetailForOutboundDTOList);
			if (beneficaries.size() > 0) {
				result = beneficaries.stream()
						.collect(Collectors.toMap(BeneficiaryModel::getBeneficiaryRegID, Function.identity()));
			}
		}

		for (BeneficiaryCall call : resultSet) {
			BeneficiaryModel i_ben = null;
			String name = null;
			String beneficiaryID = null;
			String callType = null;
			// 104 CPU utilization initially we are not storing the recording path
//			if (call.getRecordingPath() == null) {
//				logger.info("beneficiaryCall is not having recoding or archive path set for " + call.getBenCallID());
//				updateFileRecordingsPath(call.getCallID(), call.getAgentIPAddress());
//				call = beneficiaryCallRepository.findCallDetails(call.getBenCallID());
//			}
			if (result != null && call.getBeneficiaryRegID() != null) {
				Long benRegID = call.getBeneficiaryRegID();
				i_ben = result.get(benRegID);
				if (i_ben != null) {
					name = getConcatName(i_ben.getFirstName(), i_ben.getMiddleName(), i_ben.getLastName());
					beneficiaryID = i_ben.getBeneficiaryID();
				}
			}
			if (call.getCallTypeObj() != null) {
				callType = call.getCallTypeObj().getCallType();
			}

			// old process with recording path
//			filteredCalls.add(new FilteredCallList(call.getBenCallID(), call.getBeneficiaryRegID(), name,
//					call.getRemarks(), callType, call.getCallTime(), call.getPhoneNo(), call.getcDICallStatus(),
//					beneficiaryID, call.getLastModDate(),
//					((call.getRecordingPath() != null) ? (ctiLoggerURL + "/" + call.getRecordingPath())
//							: call.getRecordingPath()),
//					call.getAgentID(), i_ben));
			// new process with session ID and removed recording path (104 CPU utilization)
			filteredCalls.add(new FilteredCallList(call.getBenCallID(), call.getBeneficiaryRegID(), name,
					call.getRemarks(), callType, call.getCallTime(), call.getPhoneNo(), call.getCDICallStatus(),
					beneficiaryID, call.getLastModDate(), call.getAgentID(), i_ben, call.getCallID()));
		}
		responseMap.put("totalPages", getPageCount(totalCount, callRequest.getPageSize()));
		responseMap.put("workList", filteredCalls);
		return responseMap.toString();
	}

	private int getPageCount(Long totalCount, Integer pageSize) {
		if (pageSize > 0) {
			if (totalCount % pageSize == 0)
				return (int) (totalCount / pageSize);
			else
				return ((int) (totalCount / pageSize) + 1);
		} else
			return 0;
	}

	private Long getTotalCallCount(CriteriaBuilder criteriaBuilder, BeneficiaryCall callRequest,
			Timestamp filterStartDate, Timestamp filterEndDate) {
		CriteriaQuery<Long> cqCount = criteriaBuilder.createQuery(Long.class);
		Root<BeneficiaryCall> entityRoot = cqCount.from(BeneficiaryCall.class);
		cqCount.select(criteriaBuilder.count(entityRoot));

		List<Predicate> predicates = new ArrayList<Predicate>();
		predicates.add(criteriaBuilder.like(entityRoot.get("receivedRoleName"),
				(callRequest.getReceivedRoleName() == null) ? "%%" : callRequest.getReceivedRoleName()));

		predicates.add(criteriaBuilder.like(entityRoot.get("agentID"),
				(callRequest.getAgentID() == null) ? "%%" : callRequest.getAgentID()));

		if (callRequest.getCallTypeID() != null) {
			predicates.add(criteriaBuilder.equal(entityRoot.get("callTypeID"), callRequest.getCallTypeID()));
		}

//		predicates.add(criteriaBuilder.like(entityRoot.get("phoneNo"),
//				(callRequest.getPhoneNo() == null) ? "%%" : callRequest.getPhoneNo()));
		
		if (callRequest.getPhoneNo() != null) {
			predicates.add(criteriaBuilder.equal(entityRoot.get("phoneNo"), callRequest.getPhoneNo()));
		}

		if (callRequest.getInboundOutbound() != null) {
			callRequest.setIsOutbound(callRequest.getInboundOutbound().equalsIgnoreCase("outbound") ? true : false);
			predicates.add(criteriaBuilder.equal(entityRoot.get("isOutbound"),
					(callRequest.getInboundOutbound().equalsIgnoreCase("outbound") ? true : false)));
		}
		if (callRequest.getBenCallIDs() == null) {
			predicates.add(criteriaBuilder.between(entityRoot.get("createdDate"), filterStartDate, filterEndDate));
		} else {
			Expression<String> callListExpression = entityRoot.get("benCallID");
			predicates.add(callListExpression.in(Arrays.asList(callRequest.getBenCallIDs().split(","))));
		}

		predicates.add(criteriaBuilder.equal(entityRoot.get("calledServiceID"), callRequest.getCalledServiceID()));

		if (callRequest.getCDICallStatus() != null && !callRequest.getCDICallStatus().trim().equalsIgnoreCase("All")) {
			predicates
					.add(criteriaBuilder.equal(entityRoot.get("cDICallStatus"), callRequest.getCDICallStatus().trim()));
		}

//		// filter only valid and transfered call
//		List<String> callTypes = new ArrayList<>();
//		callTypes.add("Valid");
//		callTypes.add("Transfer");

//		Expression<String> exp = entityRoot.get("callTypeObj").get("callGroupType");
//		predicates.add(exp.in(callTypes));
		// grouping by callID from C-zentric
		// cqCount.groupBy(entityRoot.get("callID"));

		cqCount.where(predicates.toArray(new Predicate[] {}));

		List<Long> totalCountList = entityManager.createQuery(cqCount).getResultList();

		if (totalCountList != null && totalCountList.size() > 0)
			return (long) totalCountList.get(0);
		else
			return (long) 0;
	}

	private Long getTotalCallCountWithPaginationFor104(CriteriaBuilder criteriaBuilder, BeneficiaryCall callRequest,
			Timestamp filterStartDate, Timestamp filterEndDate) {
		CriteriaQuery<Long> cqCount = criteriaBuilder.createQuery(Long.class);
		Root<BeneficiaryCall> entityRoot = cqCount.from(BeneficiaryCall.class);
		cqCount.select(criteriaBuilder.count(entityRoot));

		List<Predicate> predicates = new ArrayList<Predicate>();
		predicates.add(criteriaBuilder.like(entityRoot.get("receivedRoleName"),
				(callRequest.getReceivedRoleName() == null) ? "%%" : callRequest.getReceivedRoleName()));

		predicates.add(criteriaBuilder.like(entityRoot.get("agentID"),
				(callRequest.getAgentID() == null) ? "%%" : callRequest.getAgentID()));

//		if (callRequest.getCallTypeID() != null) {
//			predicates.add(criteriaBuilder.equal(entityRoot.get("callTypeID"), callRequest.getCallTypeID()));
//		}

		// filter only valid and transfered call
		List<String> callTypes = new ArrayList<>();
		callTypes.add("Valid");
		callTypes.add("Transfer");

		Expression<String> exp = entityRoot.get("callTypeObj").get("callGroupType");
		predicates.add(exp.in(callTypes));

		predicates.add(criteriaBuilder.notEqual(entityRoot.get("callTypeObj").get("callType"), "test"));

		predicates.add(criteriaBuilder.like(entityRoot.get("phoneNo"),
				(callRequest.getPhoneNo() == null) ? "%%" : callRequest.getPhoneNo()));

		if (callRequest.getInboundOutbound() != null) {
			callRequest.setIsOutbound(callRequest.getInboundOutbound().equalsIgnoreCase("outbound") ? true : false);
			predicates.add(criteriaBuilder.equal(entityRoot.get("isOutbound"),
					(callRequest.getInboundOutbound().equalsIgnoreCase("outbound") ? true : false)));
		}
		if (callRequest.getBenCallIDs() == null) {
			predicates.add(criteriaBuilder.between(entityRoot.get("createdDate"), filterStartDate, filterEndDate));
		} else {
			Expression<String> callListExpression = entityRoot.get("benCallID");
			predicates.add(callListExpression.in(Arrays.asList(callRequest.getBenCallIDs().split(","))));
		}

		predicates.add(criteriaBuilder.equal(entityRoot.get("calledServiceID"), callRequest.getCalledServiceID()));

		if (callRequest.getCDICallStatus() != null && !callRequest.getCDICallStatus().trim().equalsIgnoreCase("All")) {
			predicates
					.add(criteriaBuilder.equal(entityRoot.get("cDICallStatus"), callRequest.getCDICallStatus().trim()));
		}

//		// filter only valid and transfered call
//		List<String> callTypes = new ArrayList<>();
//		callTypes.add("Valid");
//		callTypes.add("Transfer");

//		Expression<String> exp = entityRoot.get("callTypeObj").get("callGroupType");
//		predicates.add(exp.in(callTypes));
		// grouping by callID from C-zentric
		// cqCount.groupBy(entityRoot.get("callID"));

		cqCount.where(predicates.toArray(new Predicate[] {}));

		List<Long> totalCountList = entityManager.createQuery(cqCount).getResultList();

		if (totalCountList != null && totalCountList.size() > 0)
			return (long) totalCountList.get(0);
		else
			return (long) 0;
	}

	@Override
	public String filterCallList(String request, String auth) throws IEMRException {
		Map<String, Object> responseMap = new HashMap<>();
		Integer pageSize = Integer.valueOf(qualityAuditPageSize);
		BeneficiaryCall callRequest = inputMapper.gson().fromJson(request, BeneficiaryCall.class);
		List<FilteredCallList> filteredCalls = new ArrayList<FilteredCallList>();
		Calendar today = Calendar.getInstance();
		Calendar startDate = Calendar.getInstance();
		startDate.add(Calendar.DAY_OF_YEAR, -7);
		Timestamp filterStartDate = (callRequest.getFilterStartDate() != null) ? callRequest.getFilterStartDate()
				: (new Timestamp(startDate.getTimeInMillis()));
		Timestamp filterEndDate = (callRequest.getFilterEndDate() != null) ? callRequest.getFilterEndDate()
				: (new Timestamp(today.getTimeInMillis()));

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<BeneficiaryCall> criteriaQuery = criteriaBuilder.createQuery(BeneficiaryCall.class);
		Root<BeneficiaryCall> root = criteriaQuery.from(BeneficiaryCall.class);
		// add predicates to see more parameters
		List<Predicate> predicates = new ArrayList<Predicate>();
		List<BeneficiaryCall> resultSet = null;
		predicates.add(criteriaBuilder.like(root.get("receivedRoleName"),
				(callRequest.getReceivedRoleName() == null) ? "%%" : callRequest.getReceivedRoleName()));

		predicates.add(criteriaBuilder.like(root.get("agentID"),
				(callRequest.getAgentID() == null) ? "%%" : callRequest.getAgentID()));

		if (callRequest.getCallTypeID() != null) {
			predicates.add(criteriaBuilder.equal(root.get("callTypeID"), callRequest.getCallTypeID()));
		}

//		predicates.add(criteriaBuilder.like(root.get("phoneNo"),
//				(callRequest.getPhoneNo() == null) ? "%%" : callRequest.getPhoneNo()));
		
		if (callRequest.getPhoneNo() != null) {
			predicates.add(criteriaBuilder.equal(root.get("phoneNo"), callRequest.getPhoneNo()));
		}

		if (callRequest.getInboundOutbound() != null) {
			callRequest.setIsOutbound(callRequest.getInboundOutbound().equalsIgnoreCase("outbound") ? true : false);
			predicates.add(criteriaBuilder.equal(root.get("isOutbound"),
					(callRequest.getInboundOutbound().equalsIgnoreCase("outbound") ? true : false)));
		}
		if (callRequest.getBenCallIDs() == null) {
			predicates.add(criteriaBuilder.between(root.get("createdDate"), filterStartDate, filterEndDate));
		} else {
			Expression<String> callListExpression = root.get("benCallID");
			predicates.add(callListExpression.in(Arrays.asList(callRequest.getBenCallIDs().split(","))));
		}

		predicates.add(criteriaBuilder.equal(root.get("calledServiceID"), callRequest.getCalledServiceID()));

		criteriaQuery.select(root).where(predicates.toArray(new Predicate[] {}))
				.orderBy(criteriaBuilder.asc(root.get("benCallID")));

		// Fetches the count of all records as per given criteria
		// get total count for pagination
		Long totalCount = getTotalCallCount(criteriaBuilder, callRequest, filterStartDate, filterEndDate);
		// Add pagination here
		if (totalCount > 0) {
			TypedQuery<BeneficiaryCall> typedQuery = entityManager.createQuery(criteriaQuery);
			if (pageSize != null && callRequest.getPageNo() != null) {
				if (callRequest.getPageNo() < 1)
					throw new IEMRException("Invalid page number");
				Integer startIndex = (callRequest.getPageNo() - 1) * pageSize;
				Integer endIndex = startIndex != 0 ? (pageSize + startIndex) : (pageSize - 1);
				typedQuery.setMaxResults(pageSize).setFirstResult(startIndex);
			}
			resultSet = typedQuery.getResultList();
			HashSet<Long> beneficiaryRegIDs = new HashSet<>();

			for (BeneficiaryCall call : resultSet) {
				beneficiaryRegIDs.add(call.getBeneficiaryRegID());
			}

			beneficiaryRegIDs.removeIf(Objects::isNull);
			Map<Long, BeneficiaryModel> result = null;
			if (beneficiaryRegIDs.size() > 0) {
				List<BeneficiariesDTO> benDetailForOutboundDTOList = identityBeneficiaryService
						.getBeneficiaryListByIDs(beneficiaryRegIDs, auth, callRequest.getIs1097());
				List<BeneficiaryModel> beneficaries = getBeneficiaryListFromMapper(benDetailForOutboundDTOList);
				if (beneficaries.size() > 0) {
					result = beneficaries.stream()
							.collect(Collectors.toMap(BeneficiaryModel::getBeneficiaryRegID, Function.identity()));
				}
			}

			for (BeneficiaryCall call : resultSet) {
				BeneficiaryModel i_ben = null;
				String name = null;
				String beneficiaryID = null;
				String callType = null;

				// 104 CPU utilization initially we are not storing the recording path
//			if (call.getRecordingPath() == null) {
//				logger.info("beneficiaryCall is not having recoding or archive path set for " + call.getBenCallID());
//				updateFileRecordingsPath(call.getCallID(), call.getAgentIPAddress());
//				call = beneficiaryCallRepository.findCallDetails(call.getBenCallID());
//			}
				if (result != null && call.getBeneficiaryRegID() != null) {
					Long benRegID = call.getBeneficiaryRegID();
					i_ben = result.get(benRegID);
					if (i_ben != null) {
						name = getConcatName(i_ben.getFirstName(), i_ben.getMiddleName(), i_ben.getLastName());
						beneficiaryID = i_ben.getBeneficiaryID();
					}
				}
				if (call.getCallTypeObj() != null) {
					callType = call.getCallTypeObj().getCallType();
				}
				// old process with recording Path
//			filteredCalls.add(new FilteredCallList(call.getBenCallID(), call.getBeneficiaryRegID(), name,
//					call.getRemarks(), callType, call.getCallTime(), call.getPhoneNo(), call.getcDICallStatus(),
//					beneficiaryID, call.getLastModDate(),
//					((call.getRecordingPath() != null) ? (ctiLoggerURL + "/" + call.getRecordingPath())
//							: call.getRecordingPath()),
//					call.getAgentID(), i_ben ));
				// new response process with session ID and removed recording path (104 CPU
				// utilization)
				filteredCalls.add(new FilteredCallList(call.getBenCallID(), call.getBeneficiaryRegID(), name,
						call.getRemarks(), callType, call.getCallTime(), call.getPhoneNo(), call.getCDICallStatus(),
						beneficiaryID, call.getLastModDate(), call.getAgentID(), i_ben, call.getCallID()));
			}
		}
		responseMap.put("totalPages", getPageCount(totalCount, pageSize));
		responseMap.put("workList", filteredCalls);
		return responseMap.toString();
	}

	@Override
	public String outboundAllocation(String request) throws IEMRException {
		BenOutboundCallAllocation allocation = inputMapper.gson().fromJson(request, BenOutboundCallAllocation.class);
		OutboundCallRequest[] callRequests = allocation.getOutboundCallRequests();
		Integer count = 0;
		logger.debug("reading object: ", allocation.toString());
		// if (allocation.getUserID().size() * allocation.getAllocateNo() <=
		// callRequests.length)
		// {
		for (int userIndex = 0; userIndex < allocation.getUserID().size(); userIndex++) {
			Integer userID = allocation.getUserID().get(userIndex);
			int outboundLastCall = userIndex * allocation.getAllocateNo() + allocation.getAllocateNo();
			List<Long> outboundCalls = new ArrayList<Long>();
			for (int outboundCallIndex = userIndex * allocation.getAllocateNo(); outboundCallIndex < outboundLastCall
					&& outboundCallIndex < callRequests.length; outboundCallIndex++) {
				outboundCalls.add(callRequests[outboundCallIndex].getOutboundCallReqID());
			}
			logger.debug("userid and outbound call id: ",
					allocation.getUserID().get(userIndex) + " : " + outboundCalls);

			outboundCallRequestRepository.allocateCallsInBulk(userID, outboundCalls);
			count += outboundCalls.size();
		}
		// }
		return count.toString();
	}

	@Override
	public String getBlacklistNumbers(String request) throws IEMRException {
		PhoneBlock requestObj = inputMapper.gson().fromJson(request, PhoneBlock.class);
		List<PhoneBlock> phoneBlocks = new ArrayList<PhoneBlock>();
		List<Boolean> blockedList = new ArrayList<Boolean>();
		if (requestObj.getIsBlocked() != null) {
			blockedList.add(requestObj.getIsBlocked());
		} else {
			blockedList.add(true);
			blockedList.add(false);
		}
		String phoneNoFilter = "%%";
		if (requestObj.getPhoneNo() != null && requestObj.getPhoneNo().trim().length() > 0) {
			phoneNoFilter = "%" + requestObj.getPhoneNo().trim() + "%";
		}
		Set<Objects[]> resultSet = phoneBlockRepository.getPhoneBlockListByServiceProviderMapID(
				requestObj.getProviderServiceMapID(), phoneNoFilter, blockedList);
		for (Object[] objects : resultSet) {
			PhoneBlock phoneBlock = PhoneBlock.initializePhoneBlock((Long) objects[0], (String) objects[1],
					(Integer) objects[2], (Integer) objects[3], (Boolean) objects[4], (String) objects[5],
					(Timestamp) objects[6], (Timestamp) objects[7], (ProviderServiceMapping) objects[8],
					(String) objects[9]);
			phoneBlocks.add(phoneBlock);
		}
		return phoneBlocks.toString();
	}

	@Override
	public OutputResponse blockPhoneNumber(String request) throws IEMRException {
		OutputResponse response = new OutputResponse();
		OutputResponse blockResponse;
		PhoneBlock phoneBlockRequest = inputMapper.gson().fromJson(request, PhoneBlock.class);
		PhoneBlock phoneBlock = phoneBlockRepository.findOne(phoneBlockRequest.getPhoneBlockID());
		Calendar cal = Calendar.getInstance();
		Timestamp blockStartDate = new Timestamp(cal.getTimeInMillis());
		cal.add(Calendar.SECOND, ConfigProperties.getInteger("block-duration-days") * SECONDS_IN_DAY);
		// cal.add(field, amount);
		Timestamp blockEndDate = new Timestamp(cal.getTimeInMillis());
		blockResponse = blockUnblockPhoneNoInCTI(phoneBlock.getProviderServiceMapping().getCtiCampaignName(),
				phoneBlock.getPhoneNo(), true);
		if (blockResponse.isSuccess()) {
			if (phoneBlockRepository.phoneNoBlockUnblock(phoneBlock.getProviderServiceMapID(), phoneBlock.getPhoneNo(),
					true, phoneBlock.getProviderServiceMapping().getCtiCampaignName(), phoneBlock.getNoOfNuisanceCall(),
					blockStartDate, blockEndDate, phoneBlockRequest.getModifiedBy(), phoneBlock.getCallIDs()) > 0) {
				response.setResponse("Successfully blocked " + phoneBlock.getPhoneNo() + " for camppaign "
						+ phoneBlock.getProviderServiceMapping().getCtiCampaignName());
			}
		}
		return response;
	}

	@Override
	public OutputResponse unblockPhoneNumber(String request) throws IEMRException {
		OutputResponse unblockResponse = new OutputResponse();
		String response = "failure";
		PhoneBlock phoneBlockRequest = inputMapper.gson().fromJson(request, PhoneBlock.class);
		PhoneBlock phoneBlock = phoneBlockRepository.findOne(phoneBlockRequest.getPhoneBlockID());
		unblockResponse = blockUnblockPhoneNoInCTI(phoneBlock.getProviderServiceMapping().getCtiCampaignName(),
				phoneBlock.getPhoneNo(), false);
		if (unblockResponse.isSuccess()) {
			if (phoneBlockRepository.phoneNoBlockUnblock(phoneBlock.getProviderServiceMapID(), phoneBlock.getPhoneNo(),
					false, phoneBlock.getProviderServiceMapping().getCtiCampaignName(), 0, null, null,
					phoneBlockRequest.getModifiedBy(), null) > 0) {
				unblockResponse.setResponse("Successfully unblocked " + phoneBlock.getPhoneNo() + " for camppaign "
						+ phoneBlock.getProviderServiceMapping().getCtiCampaignName());
			}
		}
		return unblockResponse;
	}

	@Override
	public String completeOutboundCall(String request) throws IEMRException {
		OutboundCallRequest callRequest = inputMapper.gson().fromJson(request, OutboundCallRequest.class);
		int updateCount = 0;
		if (callRequest.getRequestedFor() != null) {
			updateCount = outboundCallRequestRepository.updateCompleteStatusInCall(callRequest.getOutboundCallReqID(),
					callRequest.getIsCompleted(), callRequest.getRequestedFor(), null);
		} else {
			updateCount = outboundCallRequestRepository.updateCompleteStatusInCall(callRequest.getOutboundCallReqID(),
					callRequest.getIsCompleted(), null);
		}
		String response = "failure";
		if (updateCount > 0) {
			response = "success";
		}
		return response;
	}

	@Override
	public String updateOutboundCall(String request) throws IEMRException {
		OutboundCallRequest callRequest = inputMapper.gson().fromJson(request, OutboundCallRequest.class);
		int updateCount = 0;
		OutboundCallRequest outBoundCallDetails = outboundCallRequestRepository
				.findOne(callRequest.getOutboundCallReqID());
		// if (ConfigProperties.getInteger("max_retry_count") != 0)
		// {
		// max_retry_count = ConfigProperties.getInteger("max_retry_count");
		// }

		Integer max_retry_count = callTypeRepository.getMaxRedialByCallTypeID(callRequest.getCallTypeID());

		if (max_retry_count == null) {
			max_retry_count = 1;
		}
		if (outBoundCallDetails.getNoOfTrials() == null) {
			outBoundCallDetails.setNoOfTrials(0);
		}
		Integer currentCallCount = outBoundCallDetails.getNoOfTrials() + 1;

		if (currentCallCount >= max_retry_count) {
			callRequest.setIsCompleted(true);
		}
		if (callRequest.getRequestedFor() != null) {
			updateCount = outboundCallRequestRepository.updateCompleteStatusInCall(callRequest.getOutboundCallReqID(),
					callRequest.getIsCompleted(), callRequest.getRequestedFor(), currentCallCount);
		} else {
			updateCount = outboundCallRequestRepository.updateCompleteStatusInCall(callRequest.getOutboundCallReqID(),
					callRequest.getIsCompleted(), currentCallCount);
		}
		String response = "failure";
		if (updateCount > 0) {
			response = "success";
		}
		return response;
	}

	@Override
	public String unblockBlockedNumbers() {
		List<PhoneBlock> phoneBlocks = new ArrayList<PhoneBlock>();
		Calendar cal = Calendar.getInstance();
		Timestamp endDate = new Timestamp(cal.getTimeInMillis());
		Set<Objects[]> resultSet = phoneBlockRepository.getPhoneBlockList(endDate);
		for (Object[] objects : resultSet) {
			PhoneBlock phoneBlock = PhoneBlock.initializePhoneBlock((Long) objects[0], (String) objects[1],
					(Integer) objects[2], (Integer) objects[3], (Boolean) objects[4], (String) objects[5],
					(Timestamp) objects[6], (Timestamp) objects[7], (ProviderServiceMapping) objects[8],
					(String) objects[9]);
			blockUnblockPhoneNoInCTI(phoneBlock.getCtiCampaignName(), phoneBlock.getPhoneNo(), false);
			if (phoneBlockRepository.phoneNoBlockUnblock(phoneBlock.getProviderServiceMapID(), phoneBlock.getPhoneNo(),
					false, phoneBlock.getCtiCampaignName(), phoneBlock.getNoOfNuisanceCall(), null, null, "system",
					null) > 0) {
				phoneBlocks.add(phoneBlock);
			}
		}
		return phoneBlocks.toString();
	}

	@Override
	public Integer updateBeneficiaryCallCDIStatus(String request) throws IEMRException {
		Integer updateCounts = 0;
		BeneficiaryCall benificiaryCallId = inputMapper.gson().fromJson(request, BeneficiaryCall.class);
		updateCounts = beneficiaryCallRepository.updateBeneficiaryCallCDIStatus(benificiaryCallId.getBenCallID(),
				benificiaryCallId.getCDICallStatus());
		return updateCounts;
	}

	@Override
	public List<BeneficiaryCall> getCallHistoryByCallID(String request) throws IEMRException {

		BeneficiaryCall benificiaryCallId = InputMapper.gson().fromJson(request, BeneficiaryCall.class);
		// updateCounts =
		// beneficiaryCallRepository.getCallHistoryByCallID(benificiaryCallId.getCallID());
		if (benificiaryCallId != null && benificiaryCallId.getCallID() != null
				&& benificiaryCallId.getCallID().length() > 0
				&& !benificiaryCallId.getCallID().equalsIgnoreCase("undefined"))
			return beneficiaryCallRepository.getCallHistoryByCallID(benificiaryCallId.getCallID());
		else {
			List<BeneficiaryCall> blankRes = new ArrayList<>();
			return blankRes;
		}

	}

	@Override
	public String outboundCallListByCallID(String request) throws IEMRException {
		OutboundCallRequest outboundCallRequest = inputMapper.gson().fromJson(request, OutboundCallRequest.class);
		BeneficiaryCall callRequest = inputMapper.gson().fromJson(request, BeneficiaryCall.class);
		Set<Objects[]> resultSet;
		List<OutboundCallRequest> outboundCallRequests = new ArrayList<OutboundCallRequest>();

		List<BeneficiaryCall> benCallRequests = beneficiaryCallRepository
				.getCallHistoryByCallID(callRequest.getCallID());
		for (BeneficiaryCall beneficiaryCall : benCallRequests)

		{
			if (outboundCallRequest.getProviderServiceMapID() != null) {

				resultSet = outboundCallRequestRepository.getOutboundCallListByCallID(
						outboundCallRequest.getProviderServiceMapID(), beneficiaryCall.getBenCallID());
			} else {
				resultSet = outboundCallRequestRepository.getOutboundCallListByCallID(beneficiaryCall.getBenCallID());
			}
			for (Object[] object : resultSet) {
				if (object != null && object.length >= 11) {
					outboundCallRequests.add(OutboundCallRequest.initializeOutboundCallListByCallID((Long) object[0],
							(Timestamp) object[1], (Integer) object[2], (String) object[3], (Integer) object[4],
							(SubService) object[5], (String) object[6], beneficiaryCall.getReceivedRoleName(),
							(String) object[7], (Integer) object[8], (Boolean) object[9], (Timestamp) object[10]));
				}

			}
		}
		return outboundCallRequests.toString();
	}

	@Override
	public String resetOutboundCall(String request) throws IEMRException {
		logger.debug("Request received for resetOutboundCall: " + request);
		OutboundCallRequest outboundCallRequest = inputMapper.gson().fromJson(request, OutboundCallRequest.class);
		Integer count = outboundCallRequestRepository.resetOutboundCall(outboundCallRequest.getOutboundCallReqIDs());
		logger.debug("Respone from resetOutboundCall: " + count);
		return count.toString();
	}

	@Override
	public String outboundCallCount(String request) throws IEMRException, JSONException {
		logger.debug("Request received for outboundCallCount: " + request);
		OutboundCallRequest outboundCallRequest = inputMapper.gson().fromJson(request, OutboundCallRequest.class);
		Calendar cal = Calendar.getInstance();
		Timestamp startDate = new Timestamp(cal.getTimeInMillis());
		cal.add(Calendar.DATE, 7);
		Timestamp lastDate = new Timestamp(cal.getTimeInMillis());
		Set<Objects[]> resultSet = null;
		startDate = (outboundCallRequest.getStartDate() != null) ? outboundCallRequest.getStartDate() : startDate;
		lastDate = (outboundCallRequest.getEndDate() != null) ? outboundCallRequest.getEndDate() : lastDate;
		String selectedLanguage = outboundCallRequest.getPreferredLanguageName();
		Integer assignedUserID = outboundCallRequest.getAssignedUserID();
		// if (assignedUserID != null) {
		if (outboundCallRequest.getStartDate() == null)
			outboundCallRequest.setFilterStartDate(startDate);

		if (assignedUserID != null && outboundCallRequest != null
				&& (outboundCallRequest.getStartDate() != null || outboundCallRequest.getEndDate() != null)) {
			resultSet = outboundCallRequestRepository.outboundCallCount(outboundCallRequest.getProviderServiceMapID(),
					assignedUserID);
		}
//			else {
//				resultSet = outboundCallRequestRepository
//						.outboundCallCount(outboundCallRequest.getProviderServiceMapID(), assignedUserID);
//			}
		// }
		else if (selectedLanguage != null) {
			resultSet = outboundCallRequestRepository.outboundCallCount(outboundCallRequest.getProviderServiceMapID(),
					startDate, lastDate, selectedLanguage);
		} else {
			resultSet = outboundCallRequestRepository.outboundCallCount(outboundCallRequest.getProviderServiceMapID(),
					startDate, lastDate);
		}
		JSONObject result = new JSONObject();
		if (selectedLanguage == null) {
			result.put("All", 0);
		} else {
			result.put(selectedLanguage, 0);
		}
		if (resultSet != null && resultSet.size() > 0) {
			for (Object[] objects : resultSet) {
				if (objects.length >= 2) {
					result.put(((String) objects[0]).trim(), objects[1]);
					if (selectedLanguage == null) {
						result.put("All", (result.getLong("All") + (Long) objects[1]));
					}
				}
			}
		}
		JSONArray resultArray = new JSONArray();
		Iterator<String> languages = result.keys();
		while (languages.hasNext()) {
			String language = languages.next();
			JSONObject temp = new JSONObject();
			temp.put("language", language);
			temp.put("count", result.getLong(language));
			resultArray.put(temp);
		}
		return resultArray.toString();
	}

	@Override
	public String nueisanceCallHistory(String request, String auth) throws IEMRException, JSONException {
		BeneficiaryCall callRequest = inputMapper.gson().fromJson(request, BeneficiaryCall.class);
		ArrayList<BeneficiaryCall> beneficiaryCalls = new ArrayList<BeneficiaryCall>();
		String phoneNo = callRequest.getPhoneNo();
		Integer count = callRequest.getCount();
		Integer providerServiceMapID = callRequest.getCalledServiceID();
		List<PhoneBlock> blockedData = phoneBlockRepository
				.getPhoneBlockListByServiceProviderMapID(providerServiceMapID, phoneNo);
		if (blockedData.size() > 0) {
			callRequest.setBenCallIDs(blockedData.get(0).getCallIDs());
		}
		return filterCallList(OutputMapper.gsonWithoutExposeRestriction().toJson(callRequest), auth);
	}

	public String getConcatName(String firstname, String middlename, String lastname) {
		String fullname = "";
		if (firstname != null && firstname.trim().length() > 0) {
			fullname = firstname.trim();
		}
		if (middlename != null && middlename.trim().length() > 0) {
			fullname = fullname.trim() + " " + middlename.trim();
		}
		if (lastname != null && lastname.trim().length() > 0) {
			fullname = fullname.trim() + " " + lastname.trim();
		}

		return fullname;
	}

	@Override
	public BeneficiaryCallModel beneficiaryByCallID(CallRequestByIDModel request, String authKey) throws IEMRException {
//		CallLogger callLogger = new CallLogger();
//		if (request != null)
//		{
//			callLogger.setCallID(request.getCallID());
//			callLogger.setRequestOBJ(request.toString());
//			callLogger.setAPIName("call/beneficiaryByCallID");
//			callLoggerRepo.save(callLogger);
//		}
//		else
//			logger.info("create request is invalid for call");
		BeneficiaryCallModel beneficiaryInCall = null;
		HashSet<Long> beneficiaryRegIDs = new HashSet<Long>();
		List<BeneficiaryCall> resultSet = beneficiaryCallRepository.getCallHistoryByCallID(request.getCallID());
		if (resultSet.size() > 0) {
			beneficiaryInCall = callMapper.beneficiaryCallToModel(resultSet.get(resultSet.size() - 1));
			beneficiaryRegIDs.add(beneficiaryInCall.getBeneficiaryRegID());

			Map<Long, BeneficiaryModel> result = null;

			List<BeneficiariesDTO> benDetailForOutboundDTOList = identityBeneficiaryService
					.getBeneficiaryListByIDs(beneficiaryRegIDs, authKey, request.getIs1097());

			List<BeneficiaryModel> iben = new ArrayList<>();
			// for (BeneficiariesDTO beneficiariesDTO : benDetailForOutboundDTOList)
			// {
			// BeneficiaryModel beneficiary =
			// benCompleteMapper.BenDetailForOutboundDTOToI_Beneficiary(
			// beneficiariesDTO, locationStateRepo, stateMapper, locationDistrictRepository,
			// districtMapper,
			// blockRepository, blockMapper, branchRepository, branchMapper,
			// educationRepository,
			// educationMapper, communityRepository, communityMapper,relationshipRepository,
			// relationshipMapper);
			// beneficiary.setSexualOrientation(
			// sexualOrientationMapper.sexualOrientationByIDToModel(beneficiary.getSexualOrientationId()));
			// beneficiary.getI_bendemographics().setHealthCareWorkerType(healthCareWorkerMapper
			// .getModelByWorkerID(beneficiary.getI_bendemographics().getHealthCareWorkerID()));
			// beneficiary.setGovtIdentityType(
			// govtIdentityTypeMapper.govtIdentityTypeModelByIDToModel(beneficiary.getGovtIdentityTypeID()));
			// beneficiary.setM_gender(
			// genderMapper.genderByIDToLoginResponse(beneficiariesDTO.getBeneficiaryDetails().getGenderId()));
			// beneficiary.setMaritalStatus(
			// maritalStatusMapper.maritalStatusByIDToResponse(beneficiariesDTO.getBeneficiaryDetails().getMaritalStatusId()));
			// beneficiary.setM_title(
			// titleMapper.titleByIDToResponse(beneficiariesDTO.getBeneficiaryDetails().getTitleId()));
			// iben.add(beneficiary);
			// }
			iben = getBeneficiaryListFromMapper(benDetailForOutboundDTOList);
			result = iben.stream()
					.collect(Collectors.toMap(BeneficiaryModel::getBeneficiaryRegID, Function.identity()));

			BeneficiariesPartialDTO i_ben = new BeneficiariesPartialDTO();
			if (result != null && result.size() > 0) {
				beneficiaryInCall.setI_beneficiary(result.get(beneficiaryInCall.getBeneficiaryRegID()));
			}
		}
		return beneficiaryInCall;
	}

	public List<BeneficiaryModel> getBeneficiaryListFromMapper(List<BeneficiariesDTO> benDetailForOutboundDTOList) {
		List<BeneficiaryModel> beneficiaryList = new ArrayList<BeneficiaryModel>();
		List<Future<?>> futures1 = new ArrayList<Future<?>>();

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

		// try
		// {
		// for (Future<?> future : futures1)
		// {
		// future.get();
		// }
		// } catch (InterruptedException | ExecutionException e)
		// {
		// e.printStackTrace();
		// }
		return beneficiaryList;
	}

	@Override
	public Boolean isAvailed(BeneficiaryCallModel beneficiaryCallModel) {
		Boolean result = false;
		List<BeneficiaryCall> calls = beneficiaryCallRepository.getCallsByBeneficiaryRegIDAndReceivedRoleName(
				beneficiaryCallModel.getBeneficiaryRegID(), beneficiaryCallModel.getReceivedRoleName());
		if (calls.size() > 0)
			result = true;
		return result;
	}

	@Override
	public List<OutboundCallRequest> getBenRequestedOutboundCall(BeneficiaryCallModel beneficiaryCallModel) {
		List<OutboundCallRequest> result = null;
		result = outboundCallRequestRepository.getBenRequestedOutboundCall(beneficiaryCallModel.getCalledServiceID(),
				beneficiaryCallModel.getBeneficiaryRegID());
		return result;
	}

	@Override
	public String isAutoPreviewDialing(ProviderServiceMapping m_ProviderServiceMapping) {
		int updateCount = 0;
		updateCount = dialPreferenceManualRepository.updateautoPreviewDialFlag(
				m_ProviderServiceMapping.getProviderServiceMapID(),
				m_ProviderServiceMapping.getIsDialPreferenceManual(), m_ProviderServiceMapping.getPreviewWindowTime());
		String response = "failure";
		if (updateCount > 0) {
			response = "Auto preview dial added successfully";
		}
		return response;

	}

	@Override
	public String checkAutoPreviewDialing(ProviderServiceMapping m_ProviderServiceMapping) {
		ProviderServiceMapping mapping = null;
		List<Objects[]> list = null;
		list = dialPreferenceManualRepository
				.checkAutoPreviewDialing(m_ProviderServiceMapping.getProviderServiceMapID());
		Object[] objects = list.get(0);
		mapping = new ProviderServiceMapping((Boolean) objects[0], (Integer) objects[1]);

		return mapping.toString();
	}

	@Override
	public String CTIFilePath(String request) {

		BeneficiaryCall benificiaryCall = InputMapper.gson().fromJson(request, BeneficiaryCall.class);
		String recordingPath = "";
//		Map<String,String> CTIRecordingPath = new HashMap<>();

		if (benificiaryCall.getAgentID() != null && benificiaryCall.getCallID() != null) {

			String CTIUserFilePath = beneficiaryCallRepository.getUserFilepath(benificiaryCall.getAgentID(),
					benificiaryCall.getCallID());

			if (CTIUserFilePath != null) {
				recordingPath = ctiLoggerURL + "/" + CTIUserFilePath;
//					CTIRecordingPath.put("recordingPath" , recordingPath);

			} else {
				try {
					JSONObject requestFile = new JSONObject();
					requestFile.put("agent_id", benificiaryCall.getAgentID());
					requestFile.put("session_id", benificiaryCall.getCallID());
					OutputResponse response1 = ctiService.getVoiceFile(requestFile.toString(), "extra parameter");
					if (response1.getStatusCode() == OutputResponse.SUCCESS) {
						CTIVoiceFile getVoiceFile = InputMapper.gson().fromJson(response1.getData(),
								CTIVoiceFile.class);
						String recordingFilePath = getVoiceFile.getPath() + "/" + getVoiceFile.getFilename();
						beneficiaryCallRepository.updateVoiceFilePathNew(benificiaryCall.getAgentID(),
								benificiaryCall.getCallID(), recordingFilePath, null);
						recordingPath = ctiLoggerURL + "/" + recordingFilePath;
					}
				} catch (Exception e) {
					logger.error("updateVoiceFile failed with error " + e.getMessage(), e);
				}

			}

		}

		return recordingPath;
	}

	@Override
	public String cTIFilePathNew(String request) throws IEMRException {

		BeneficiaryCall benificiaryCall = InputMapper.gson().fromJson(request, BeneficiaryCall.class);
		String recordingPath = null, CTIUserFilePath = null;
//		Map<String,String> CTIRecordingPath = new HashMap<>();

		if (benificiaryCall.getAgentID() != null && benificiaryCall.getCallID() != null) {

			CTIUserFilePath = beneficiaryCallRepository.getUserFilepath(benificiaryCall.getAgentID(),
					benificiaryCall.getCallID());

			if (CTIUserFilePath != null) {
				recordingPath = ctiLoggerURL + "/" + CTIUserFilePath;
//					CTIRecordingPath.put("recordingPath" , recordingPath);

			} else {
				try {
					JSONObject requestFile = new JSONObject();
					requestFile.put("agent_id", benificiaryCall.getAgentID());
					requestFile.put("session_id", benificiaryCall.getCallID());
					OutputResponse response1 = ctiService.getVoiceFileNew(requestFile.toString(), "extra parameter");
					if (response1 != null && response1.getStatusCode() == 200) {
						CTIResponse ctiResponse = InputMapper.gson().fromJson(response1.getData(), CTIResponse.class);
						String recordingFilePath = ctiResponse.getResponse().toString();
						beneficiaryCallRepository.updateVoiceFilePathNew(benificiaryCall.getAgentID(),
								benificiaryCall.getCallID(), recordingFilePath.substring(20), null);
						recordingPath = ctiLoggerURL + "/" + recordingFilePath.substring(20);
					}

//					if (response1.getStatusCode() == OutputResponse.SUCCESS) {
//						CTIVoiceFile getVoiceFile = InputMapper.gson().fromJson(response1.getData(),
//								CTIVoiceFile.class);
//						String recordingFilePath = getVoiceFile.getPath() + "/" + getVoiceFile.getFilename();
//						beneficiaryCallRepository.updateVoiceFilePathNew(benificiaryCall.getAgentID(),
//								benificiaryCall.getCallID(), recordingFilePath, null);
//						recordingPath = ctiLoggerURL + "/" + recordingFilePath;
//					}
				} catch (Exception e) {
					logger.error("updateVoiceFile failed with error " + e.getMessage(), e);
				}

			}

		} else
			throw new IEMRException("invalid AgentID or SessionID");

		return recordingPath;
	}

}
