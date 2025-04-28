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
package com.iemr.common.service.sms;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.iemr.common.data.feedback.FeedbackDetails;
import com.iemr.common.data.feedback.FeedbackRequest;
import com.iemr.common.data.feedback.FeedbackResponse;
import com.iemr.common.data.helpline104history.COVIDHistory;
import com.iemr.common.data.helpline104history.Directoryservice;
import com.iemr.common.data.helpline104history.PrescribedDrug;
import com.iemr.common.data.helpline104history.RequestedInstitution;
import com.iemr.common.data.helpline104history.T_BloodRequest;
import com.iemr.common.data.helpline104history.T_EpidemicOutbreak;
import com.iemr.common.data.helpline104history.T_FoodSafetyCopmlaint;
import com.iemr.common.data.helpline104history.T_OrganDonation;
import com.iemr.common.data.helpline104history.T_RequestedBloodBank;
import com.iemr.common.data.institute.Institute;
import com.iemr.common.data.location.DistrictBlock;
import com.iemr.common.data.location.Districts;
import com.iemr.common.data.location.States;
import com.iemr.common.data.mctshistory.MctsDataReaderDetail;
import com.iemr.common.data.mctshistory.MctsOutboundCall;
import com.iemr.common.data.sms.SMSNotification;
import com.iemr.common.data.sms.SMSParameters;
import com.iemr.common.data.sms.SMSParametersMap;
import com.iemr.common.data.sms.SMSTemplate;
import com.iemr.common.data.sms.SMSType;
import com.iemr.common.data.telemedicine.PrescribedDrugDetail;
import com.iemr.common.data.users.User;
import com.iemr.common.data.videocall.VideoCallParameters;
import com.iemr.common.mapper.sms.SMSMapper;
import com.iemr.common.model.beneficiary.BeneficiaryModel;
import com.iemr.common.model.sms.CreateSMSRequest;
import com.iemr.common.model.sms.FullSMSTemplateResponse;
import com.iemr.common.model.sms.SMSParameterMapModel;
import com.iemr.common.model.sms.SMSParameterModel;
import com.iemr.common.model.sms.SMSRequest;
import com.iemr.common.model.sms.SMSTemplateResponse;
import com.iemr.common.model.sms.SMSTypeModel;
import com.iemr.common.model.sms.SmsAPIRequestModel;
import com.iemr.common.model.sms.UpdateSMSRequest;
import com.iemr.common.repository.callhandling.TCRequestModelRepo;
import com.iemr.common.repository.feedback.FeedbackRepository;
import com.iemr.common.repository.helpline104history.PrescribedDrugRepository;
import com.iemr.common.repository.institute.InstituteRepository;
import com.iemr.common.repository.location.LocationDistrictBlockRepository;
import com.iemr.common.repository.location.LocationDistrictRepository;
import com.iemr.common.repository.location.LocationStateRepository;
import com.iemr.common.repository.mctshistory.OutboundHistoryRepository;
import com.iemr.common.repository.sms.SMSNotificationRepository;
import com.iemr.common.repository.sms.SMSParameterMapRepository;
import com.iemr.common.repository.sms.SMSParameterRepository;
import com.iemr.common.repository.sms.SMSTemplateRepository;
import com.iemr.common.repository.sms.SMSTypeRepository;
import com.iemr.common.repository.users.IEMRUserRepositoryCustom;
import com.iemr.common.repository.videocall.VideoCallParameterRepository;
import com.iemr.common.service.beneficiary.IEMRSearchUserService;
import com.iemr.common.utils.CryptoUtil;
import com.iemr.common.utils.config.ConfigProperties;
import com.iemr.common.utils.http.HttpUtils;
import com.iemr.common.utils.mapper.OutputMapper;
//import java.util.Date;

@Service
public class SMSServiceImpl implements SMSService {

	@Value("${TMprescriptionTemplate}")
	private String prescription;
	@Autowired
	SMSMapper smsMapper;

	@Autowired
	private CryptoUtil cryptoUtil;

	@Autowired
	SMSTemplateRepository smsTemplateRepository;

	@Autowired
	SMSTypeRepository smsTypeRepository;

	@Autowired
	SMSParameterRepository smsParameterRepository;

	@Autowired
	SMSParameterMapRepository smsParameterMapRepository;

	@Autowired
	IEMRSearchUserService searchBeneficiary;

	@Autowired
	InstituteRepository instituteRepository;

	@Autowired
	IEMRUserRepositoryCustom userRepository;

	@Autowired
	FeedbackRepository feedbackReporsitory;

	@Autowired
	SMSNotificationRepository smsNotification;

	@Autowired
	LocationStateRepository stateRepository;

	@Autowired
	LocationDistrictRepository districtRepository;

	@Autowired
	LocationDistrictBlockRepository blockRepository;

	@Autowired
	HttpUtils httpUtils;

	@Autowired
	PrescribedDrugRepository prescribedDrugRepository;

	@Autowired
	OutboundHistoryRepository outboundHistoryRepository;

	@Autowired
	private TCRequestModelRepo tCRequestModelRepo;
	
	 @Autowired
	 private VideoCallParameterRepository videoCallParameterRepository;

	Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	private static Boolean publishingSMS = false;

	private static final String SMS_GATEWAY_URL = ConfigProperties.getPropertyByName("sms-gateway-url");

	@Override
	public String getSMSTemplates(SMSRequest smsRequest) throws Exception {
		List<SMSTemplate> smsTemplates;
		SMSTemplate request = smsMapper.requestToSMSTemplate(smsRequest);
		if (request.getSmsTypeID() == null) {
			smsTemplates = smsTemplateRepository
					.getSMSTemplateByProviderServiceMapIDOrderByDeletedSmsTypeIDDesc(request.getProviderServiceMapID());
		} else {
			smsTemplates = smsTemplateRepository.getSMSTemplateByProviderServiceMapIDAndSMSTypeID(
					request.getProviderServiceMapID(), request.getSmsTypeID());
		}
		List<SMSTemplateResponse> smsTemplateResponses = smsMapper.smsTemplateToResponse(smsTemplates);
		return OutputMapper.gsonWithoutExposeRestriction().toJson(smsTemplateResponses);
	}

	@Override
	public String updateSMSTemplate(UpdateSMSRequest smsRequest) throws Exception {
		SMSTemplate smsTemplate = null;
		SMSTemplate request = smsMapper.updateRequestToSMSTemplate(smsRequest);
		int updateCount = smsTemplateRepository.updateSMSTemplate(request.getSmsTemplateID(), request.getDeleted());
		if (updateCount > 0) {
			smsTemplate = smsTemplateRepository.findBySmsTemplateID(request.getSmsTemplateID());
		} else {
			throw new Exception("Failed to update the result");
		}
		return OutputMapper.gsonWithoutExposeRestriction().toJson(smsMapper.smsTemplateToResponse(smsTemplate));
	}

	@Override
	public String saveSMSTemplate(CreateSMSRequest smsRequest) throws Exception {
		SMSTemplate smsTemplate;
		SMSTemplate request = smsMapper.createRequestToSMSTemplate(smsRequest);
		smsTemplate = smsTemplateRepository.save(request);
		saveSMSParameterMaps(smsRequest, smsTemplate.getSmsTemplateID());
		smsTemplate = smsTemplateRepository.findBySmsTemplateID(smsTemplate.getSmsTemplateID());
		return OutputMapper.gsonWithoutExposeRestriction().toJson(smsMapper.smsTemplateToResponse(smsTemplate));
	}

	private void saveSMSParameterMaps(CreateSMSRequest smsRequest, Integer smsTemplateID) {
		List<SMSParameterMapModel> smsParameterMapModels = smsRequest.getSmsParameterMaps();
		for (SMSParameterMapModel smsParameterMapModel : smsParameterMapModels) {
			smsParameterMapModel.setSmsTemplateID(smsTemplateID);
			smsParameterMapRepository.save(smsMapper.smsParameterMapModelToSMSParametersMap(smsParameterMapModel));
		}
	}

	@Override
	public String getSMSTypes(SMSTypeModel request) throws Exception {
		List<SMSType> smsTypes = new ArrayList<SMSType>();
		SMSType smsTypeRequest = smsMapper.smsTypeModelToSMSType(request);
		smsTypes = smsTypeRepository.findSMSTypeByDeletedNotTrue(smsTypeRequest.getServiceID());
		return OutputMapper.gsonWithoutExposeRestriction().toJson(smsMapper.smsTypeToSMSTypeModel(smsTypes));
	}

	@Override
	public String getSMSParameters(SMSParameterModel request) throws Exception {
		List<SMSParameters> smsParameters = new ArrayList<SMSParameters>();
		SMSParameters smsParamsRequest = smsMapper.smsParameterModelToSMSParameters(request);
		smsParameters = smsParameterRepository.findSMSParametersByDeletedNotTrue(smsParamsRequest.getServiceID());
		List<SMSParameterModel> smsParametersModel = smsMapper.smsParametersToSMSParameterModel(smsParameters);
		Map<String, List<SMSParameterModel>> smsParamGroup = smsParametersModel.parallelStream()
				.collect(Collectors.groupingBy(SMSParameterModel::getSmsParameterType));
		JSONArray smaParamsResponse = new JSONArray();
		Set<String> keys = smsParamGroup.keySet();
		for (String key : keys) {
			JSONObject paramGroup = new JSONObject();
			paramGroup.put("smsParameterType", key);
			logger.debug("smsParamGroup.get(" + key + ") returned value "
					+ OutputMapper.gsonWithoutExposeRestriction().toJson(smsParamGroup.get(key)));
			paramGroup.put("smsParameters",
					new JSONArray(OutputMapper.gsonWithoutExposeRestriction().toJson(smsParamGroup.get(key))));
			smaParamsResponse.put(paramGroup);
		}
		return smaParamsResponse.toString();
	}

	 public VideoCallParameters getVideoCallParameters(String meetingLink) {
	        return videoCallParameterRepository.findByMeetingLink(meetingLink);
	    }
	 
	@Override
	public String sendSMS(List<SMSRequest> requests, String authToken) throws Exception {
		List<SMSNotification> sentSMS = new ArrayList<SMSNotification>();
		SMSTemplate smsTemplate = null;

		for (SMSRequest request : requests) {
			SMSNotification sms;
			
			smsTemplate = smsTemplateRepository.findBySmsTemplateID(request.getSmsTemplateID());
			
			if (null != smsTemplate && smsTemplate.getSmsTemplateName().equalsIgnoreCase(prescription)) {
				sentSMS = prepareTMSMS(request, authToken);
			}
			else if(null != smsTemplate && smsTemplate.getSmsTemplateName().equalsIgnoreCase("Video Consultation"))
			{
                String meetingLink = request.getSmsAdvice();
            	if (meetingLink == null || meetingLink.isEmpty()) {
                throw new Exception("Meeting link is missing in the request");
            }

            VideoCallParameters vcParams = getVideoCallParameters(meetingLink);
            if (vcParams == null) {
                throw new Exception("Video Call Parameters not found for the provided meeting link: " + meetingLink);
            }

            sms = prepareVideoCallSMS(request, vcParams, authToken);
            sentSMS.add(sms);
			}
			else {
				sms = prepareSMS(request, authToken);
				sentSMS.add(sms);
			}
		}
		return sentSMS.toString();
	}
	
	public SMSNotification prepareSMSWithVideoCall(SMSRequest request, String authToken, String meetingLink) throws Exception {
    VideoCallParameters vcParams = getVideoCallParameters(meetingLink);

    if (vcParams != null) {
        return prepareVideoCallSMS(request, vcParams, authToken);
    } else {
        throw new Exception("Video Call Parameters not found for the provided meeting link: " + meetingLink);
    }
}
	public SMSNotification prepareVideoCallSMS(SMSRequest request, VideoCallParameters vcParams, String authToken) throws Exception {

    SMSNotification sms = new SMSNotification();
    sms.setSmsStatus(SMSNotification.NOT_SENT);
    sms.setCreatedBy(request.getCreatedBy());
    sms.setSmsTriggerDate(new Timestamp(Calendar.getInstance().getTime().getTime()));
    sms.setBeneficiaryRegID(request.getBeneficiaryRegID());
    sms.setReceivingUserID(request.getUserID());

    String smsToSend = "";
    SMSTemplate smsTemplate = smsTemplateRepository.findBySmsTemplateID(request.getSmsTemplateID());

    if (smsTemplate != null) {
        sms.setSmsTemplateID(smsTemplate.getSmsTemplateID());
        smsToSend = smsTemplate.getSmsTemplate();

        List<SMSParametersMap> smsParameters = smsParameterMapRepository
                .findSMSParametersMapBySmsTemplateID(request.getSmsTemplateID());

        for (SMSParametersMap smsParametersMap : smsParameters) {
            String variable = smsParametersMap.getSmsParameterName();
            String methodName = smsParametersMap.getSmsParameter().getDataName();
            String variableValue = "";
			variableValue = getVideoCallData(methodName, vcParams);
 			smsToSend = smsToSend.replace("$$" + variable + "$$", variableValue);

            if ("VideoCall".equalsIgnoreCase(smsParametersMap.getSmsParameter().getSmsParameterType())) {
                variableValue = getVideoCallData(methodName, vcParams);
            }

            if ("SMS_PHONE_NO".equalsIgnoreCase(variable)) {
                sms.setPhoneNo(request.getBenPhoneNo() != null ? request.getBenPhoneNo() : variableValue);
            } 
			else {
                smsToSend = smsToSend.replace("$$" + variable + "$$", variableValue);
            }
        }

        if (request.getAlternateNo() != null) {
            sms.setPhoneNo(request.getAlternateNo());
        }
        if (request.getFacilityPhoneNo() != null) {
            sms.setPhoneNo(request.getFacilityPhoneNo());
        }
    }

    sms.setSms(smsToSend);
    return smsNotification.save(sms);  
}

	
	public String getVideoCallData(String methodName, VideoCallParameters videoCall) throws Exception {
	    String variableValue = "";
	    switch (methodName.toLowerCase()) {
	        case "videoconsultationlink":
	            variableValue = videoCall.getMeetingLink() != null ? videoCall.getMeetingLink() : "";
	            break;
	        case "consultationdate":
	            variableValue = videoCall.getDateOfCall() != null ? videoCall.getDateOfCall().toString() : "";
	            break;
			case "phoneno":
				variableValue = videoCall.getCallerPhoneNumber() !=null ? videoCall.getCallerPhoneNumber().toString() : "";
				break;
	        default:
	            Method method = videoCall.getClass().getDeclaredMethod("get" + capitalize(methodName));
	            method.setAccessible(true);
	            Object result = method.invoke(videoCall);
	            variableValue = result != null ? result.toString() : "";
	            break;
	    }

	    return variableValue.trim();
	}

	private String capitalize(String str) {
	    return str == null || str.isEmpty() ? str : str.substring(0, 1).toUpperCase() + str.substring(1);
	}


	// Shubham Shekhar,16-10-2020,TM Prescription SMS
	private List<SMSNotification> prepareTMSMS(
			// SMSTemplate smsTemplate, List<SMSParametersMap> smsParameters,
			SMSRequest request, String authToken) throws Exception {
		List<SMSNotification> sentSMS = new ArrayList<SMSNotification>();
		SMSNotification sms = new SMSNotification();
		String benID = "", TMsms = "";
		SMSTemplate smsTemplate = null;
		sms.setSmsStatus(SMSNotification.NOT_SENT);
		sms.setCreatedBy(request.getCreatedBy());
		sms.setSmsTriggerDate(new Timestamp(Calendar.getInstance().getTime().getTime()));
		sms.setBeneficiaryRegID(request.getBeneficiaryRegID());
		sms.setReceivingUserID(request.getUserID());
		String smsToSend = "";
		BeneficiaryModel beneficiary = null;
		if (request.getBeneficiaryRegID() != null) {
			List<BeneficiaryModel> beneficiaries = searchBeneficiary.userExitsCheckWithId(request.getBeneficiaryRegID(),
					authToken, request.getIs1097());
			if (beneficiaries.size() == 1)
				beneficiary = beneficiaries.get(0);
		}
		if (request.getSmsText() != null) {
			smsToSend = request.getSmsText();
		} else {
			smsTemplate = smsTemplateRepository.findBySmsTemplateID(request.getSmsTemplateID());
			sms.setSmsTemplateID(smsTemplate.getSmsTemplateID());
			smsToSend = smsTemplate.getSmsTemplate();
			List<SMSParametersMap> smsParameters = smsParameterMapRepository
					.findSMSParametersMapBySmsTemplateID(request.getSmsTemplateID());
			int i = 0;
			if (smsTemplate != null && smsTemplate.getSmsTemplateName().equalsIgnoreCase(prescription)) {
				sms = getTMPrescription(smsToSend, request, beneficiary, sms);
			}
		}
		if (sms.getSms() != null && sms.getSms().length() > 120 && smsTemplate != null
				&& smsTemplate.getSmsTemplateName().equalsIgnoreCase(prescription))
		{
			double num = sms.getSms().length() / 120.0;
			String text = "";
			int start = 0, end = 120;
			if (num > (sms.getSms().length() / 120))
				num++;
			for (int a = 1; a <= num; a++) {
				if (end <= sms.getSms().length())
					text = sms.getSms().substring(start, end);
				SMSNotification sms1 = new SMSNotification();
				sms1.setSmsStatus(SMSNotification.NOT_SENT);
				sms1.setCreatedBy(request.getCreatedBy());
				sms1.setSmsTriggerDate(new Timestamp(Calendar.getInstance().getTime().getTime()));
				sms1.setBeneficiaryRegID(request.getBeneficiaryRegID());
				sms1.setReceivingUserID(request.getUserID());
				sms1.setPhoneNo(sms.getPhoneNo());
				sms1.setSmsTemplateID(sms.getSmsTemplateID());
				sms1.setSms(text);
				// SMSNotification sms1=new SMSNotification();
				sms1 = smsNotification.save(sms1);
				sentSMS.add(sms1);
				start = end;
				if ((end + 120) <= sms.getSms().length())
					end = end + 120;
				else
					end = end + (sms.getSms().length() - end);
			}

		} else {
			sms.setSms(sms.getSms());
			sms = smsNotification.save(sms);
			sentSMS.add(sms);
		}

		// publishSMS();
		return sentSMS;
	}


	// Shubham Shekhar,16-10-2020,TM Prescription SMS
	private SMSNotification getTMPrescription(String template, SMSRequest request, BeneficiaryModel beneficiary,
			SMSNotification sms) throws Exception {
		// template=" "+template+" ";
		String diagnosis = "";
		int c = template.indexOf("$$drug$$");
		List<PrescribedDrugDetail> pres = request.getPresObj();
		String str = template.substring(0, c);
		String temp1 = template;
		String str2 = template.substring(c, template.length());
		String temp = template;
		String sms1 = "", sms2 = "";
		// List<String> prescription=parseTemplate(str2);
		String variableValue = "_";
		int quantity = 0;
		// List<String> diagnose=parseTemplate(str);
		List<SMSParametersMap> smsParameters = smsParameterMapRepository
				.findSMSParametersMapBySmsTemplateID(request.getSmsTemplateID());

		List<String> variables = new ArrayList<String>();
		List<String> useVariable = new ArrayList<String>();
		String benID = "";
		for (SMSParametersMap smsParametersMap : smsParameters) {
			String variable = smsParametersMap.getSmsParameterName();// SMSParamName
			String paramType = smsParametersMap.getSmsParameter().getSmsParameterType(); // SMSParamSource
			String className = smsParametersMap.getSmsParameter().getDataClassName(); // DataClassName
			String methodName = smsParametersMap.getSmsParameter().getDataName(); // DataVariableName
			variables.add(methodName);
			useVariable.add(variable);
			switch (paramType) {
			case "Beneficiary":
				variableValue = getBeneficiaryData(className, methodName, request, beneficiary);
				if (variable.equalsIgnoreCase("SMS_PHONE_NO") == false)
					benID = variableValue;
				break;

			default:
				break;
			}
			if (variable.equalsIgnoreCase("SMS_PHONE_NO")) {
				if (request.getIsBloodBankSMS() == true) {
					T_RequestedBloodBank requestedBloodBank = prescribedDrugRepository
							.getBloodBankAddress(request.getRequestedBloodBankID());
					String callerContact = requestedBloodBank.getBBMobileNo() != null
							? requestedBloodBank.getBBMobileNo() + " "
							: "";
					sms.setPhoneNo(callerContact);
				} else {
					sms.setPhoneNo(variableValue);
				}
			}
		}
		for (int k = 0; k < variables.size(); k++) {
			switch (variables.get(k).toLowerCase()) {
			case "beneficiaryid":
				variableValue = benID;
				// benID=variableValue;
				break;
			case "prescriptionid":
				if (request.getPresObj() != null && request.getPresObj().size() > 0) {
					if (pres.get(0).getPrescriptionID() != null) {
						Long prescriptionid = pres.get(0).getPrescriptionID();
						variableValue = String.valueOf(prescriptionid);
					}
				} else
					variableValue = "_";
				break;
			case "diagnosis":
				if (request.getDiagnosis() != null && request.getDiagnosis().size() > 0) {

					String[] masterValues = request.getDiagnosis().get(0).toString().split("\\|\\|");
					for (int l = 0; l < masterValues.length; l++) {
						if (l != (masterValues.length - 1)) {
							diagnosis = diagnosis + masterValues[l].trim() + " && ";
						} else
							diagnosis = diagnosis + masterValues[l].trim();
					}

					variableValue = diagnosis;
				} else
					variableValue = "_";
				break;
			default:

				break;
			}
			if (variableValue == null)
				variableValue = "_";
			if (variables.get(k).equalsIgnoreCase("beneficiaryid")
					|| variables.get(k).equalsIgnoreCase("prescriptionid")
					|| variables.get(k).equalsIgnoreCase("diagnosis"))
				str = str.replace("$$" + useVariable.get(k) + "$$", variableValue);
		}
		if (request.getPresObj() != null && request.getPresObj().size() > 0) {
			for (int i = 0; i < pres.size(); i++) {
				temp = str2;
				for (int j = 0; j < variables.size(); j++) {
					switch (variables.get(j).toLowerCase()) {
					case "drugname":
						String drugName = pres.get(i).getDrugName();
						variableValue = drugName;
						break;
					case "dosage":
						String dosage = pres.get(i).getDose();
						variableValue = dosage;
						break;
					case "dosageinstruction":
						String drugForm = pres.get(i).getInstructions();
						variableValue = drugForm;
						break;
					case "frequency":
						String frequency = pres.get(i).getFrequency();
						variableValue = frequency;
						break;
					case "quantity":
						if (pres.get(i).getQtyPrescribed() != null)
							quantity = pres.get(i).getQtyPrescribed();
						variableValue = String.valueOf(quantity);
						break;
					default:
						break;
					}
					if (variableValue == null)
						variableValue = "_";
					if (variables.get(j).equalsIgnoreCase("drugname") || variables.get(j).equalsIgnoreCase("dosage")
							|| variables.get(j).equalsIgnoreCase("dosageinstruction")
							|| variables.get(j).equalsIgnoreCase("frequency")
							|| variables.get(j).equalsIgnoreCase("quantity"))
						temp = temp.replace("$$" + useVariable.get(j) + "$$", variableValue);
				}
				if (i != (pres.size() - 1))
					sms2 = sms2 + temp + " && ";
				else
					sms2 = sms2 + temp;
			}
		}
		sms.setSms(str + sms2);
		return sms;
	}

	private SMSNotification prepareSMS(
			// SMSTemplate smsTemplate, List<SMSParametersMap> smsParameters,
			SMSRequest request, String authToken) throws Exception {
		SMSNotification sms = new SMSNotification();
		String benID = "", TMsms = "";
		SMSTemplate smsTemplate = null;
		sms.setSmsStatus(SMSNotification.NOT_SENT);
		sms.setCreatedBy(request.getCreatedBy());
		sms.setSmsTriggerDate(new Timestamp(Calendar.getInstance().getTime().getTime()));
		sms.setBeneficiaryRegID(request.getBeneficiaryRegID());
		sms.setReceivingUserID(request.getUserID());
		String smsToSend = "";
		BeneficiaryModel beneficiary = null;
		if (request.getBeneficiaryRegID() != null) {
			List<BeneficiaryModel> beneficiaries = searchBeneficiary.userExitsCheckWithId(request.getBeneficiaryRegID(),
					authToken, request.getIs1097());
			if (beneficiaries.size() == 1)
				beneficiary = beneficiaries.get(0);
		}
		if (request.getSmsText() != null) {
			smsToSend = request.getSmsText();
		} else {
			smsTemplate = smsTemplateRepository.findBySmsTemplateID(request.getSmsTemplateID());
			if(null != smsTemplate) {
			sms.setSmsTemplateID(smsTemplate.getSmsTemplateID());
			smsToSend = smsTemplate.getSmsTemplate();
			}
			List<SMSParametersMap> smsParameters = smsParameterMapRepository
					.findSMSParametersMapBySmsTemplateID(request.getSmsTemplateID());
			int i = 0;
			for (SMSParametersMap smsParametersMap : smsParameters) {
				String variable = smsParametersMap.getSmsParameterName();// SMSParamName
				String paramType = smsParametersMap.getSmsParameter().getSmsParameterType(); // SMSParamSource
				String className = smsParametersMap.getSmsParameter().getDataClassName(); // DataClassName
				String methodName = smsParametersMap.getSmsParameter().getDataName(); // DataVariableName
				String variableValue = "";
				switch (paramType) {
				case "Beneficiary":
					variableValue = getBeneficiaryData(className, methodName, request, beneficiary);
					benID = variableValue;
					break;
				case "Institute":
					if (request.getIs1097() == true) {
						variableValue = getInstituteData(className, methodName, request, authToken);
					} else {
						variableValue = getDirectoryserviceData(className, methodName, request);
					}
					break;
				case "User":
					variableValue = getUserData(className, methodName, request, authToken);
					break;
				case "Feedback":
					variableValue = getFeedbackData(className, methodName, request, authToken);
					break;
				case "Prescription":
					variableValue = getPrescriptionData(className, methodName, request, beneficiary);
					break;
				case "Blood on Call":
					variableValue = getBloodOnCallData(className, methodName, request, beneficiary);
					break;
				case "Food Safety Complaint":
					variableValue = getFoodSafetyComplaintData(className, methodName, request);
					break;
				case "Epidemic Outbreak Complaint":
					variableValue = getEpidemicComplaintData(className, methodName, request);
					break;
				case "Grievance Tracking":
					variableValue = getGrievanceData(className, methodName, request, authToken, beneficiary);
					break;
				case "MCTS Call Alert":
					variableValue = getMCTSCallAlertData(className, methodName, request);
					break;
				case "Organ Donation":
					variableValue = getOrganDonationData(className, methodName, request);
					break;
				case "TM Schedule":
					variableValue = getSpecializationAndTcDateInfo(className, methodName, request);
					break;
				case "COVID-19":
					variableValue = getCOVIDData(className, methodName, request);
					break;
				case "IMRMMR":
					variableValue = getIMRMMRData(className, methodName, request);
					break;
				case "104 appointment":
					variableValue = getUptsuData(className, methodName, request);
					break;
				default:
					break;
				}
				if (variable.equalsIgnoreCase("SMS_PHONE_NO")) {
					if (request.getIsBloodBankSMS() == true) {
						T_RequestedBloodBank requestedBloodBank = prescribedDrugRepository
								.getBloodBankAddress(request.getRequestedBloodBankID());
						String callerContact = requestedBloodBank.getBBMobileNo() != null
								? requestedBloodBank.getBBMobileNo() + " "
								: "";
						sms.setPhoneNo(callerContact);
					} else {
						if (request.getNodalNumber() != null)
							variableValue = request.getNodalNumber();
						sms.setPhoneNo(variableValue);
					}

				} else {
					smsToSend = smsToSend.replace("$$" + variable + "$$", variableValue);
				}
			}

			if (null != smsTemplate && smsTemplate.getSmsTypeID() == 15 && request.getMoAdvice() != null) {
				smsToSend = smsToSend.concat(request.getMoAdvice());
			}
		}
		if (request.getAlternateNo() != null) {
			sms.setPhoneNo(request.getAlternateNo());
		}
		if (request.getFacilityPhoneNo() != null) {
			sms.setPhoneNo(request.getFacilityPhoneNo());
		}
		if (request.getBenPhoneNo() != null) {
			sms.setPhoneNo(request.getBenPhoneNo());
		}


		sms.setSms(smsToSend);
		sms = smsNotification.save(sms);
	
		return sms;
	}

	private String getFeedbackData(String className, String methodName, SMSRequest request, String authToken)
			throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {
		FeedbackDetails feedback = feedbackReporsitory.findByFeedbackID(request.getFeedbackID());
		Method method = null;
		String parameterValue = "";
		switch (className) {
		case "com.iemr.common.data.feedback.FeedbackRequest":
			List<FeedbackRequest> feedbackRequests = feedback.getFeedbackRequests();
			if (feedbackRequests.size() > 0) {
				method = feedbackRequests.get(feedbackRequests.size() - 1).getClass()
						.getDeclaredMethod("get" + methodName, null);
				parameterValue = method.invoke(feedbackRequests.get(feedbackRequests.size() - 1), null).toString();
			}
			break;
		case "com.iemr.common.data.feedback.FeedbackResponse":
			List<FeedbackResponse> feedbackResponses = feedback.getFeedbackResponses();
			if (feedbackResponses.size() > 0) {
				method = feedbackResponses.get(feedbackResponses.size() - 1).getClass()
						.getDeclaredMethod("get" + methodName, null);
				parameterValue = method.invoke(feedbackResponses.get(feedbackResponses.size() - 1), null).toString();
			}
			break;
		default:
			method = feedback.getClass().getDeclaredMethod("get" + methodName, null);
			parameterValue = method.invoke(feedback, null).toString();
			break;
		}
		return parameterValue;
	}

	private String getUserData(String className, String methodName, SMSRequest request, String authToken)
			throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {
		User user = userRepository.findByUserID(request.getUserID());
		Method method = user.getClass().getDeclaredMethod("get" + methodName, null);
		String variableValue = method.invoke(user, null).toString();
		return variableValue.trim();
	}

	private String getBeneficiaryData(String className, String methodName, SMSRequest request,
			BeneficiaryModel beneficiary) throws Exception {
		String variableValue = "";
		switch (methodName.toLowerCase()) {
		case "name":
			String fname = beneficiary.getFirstName() != null ? beneficiary.getFirstName() + " " : "";
			String mname = beneficiary.getMiddleName() != null ? beneficiary.getMiddleName() + " " : "";
			String lname = beneficiary.getLastName() != null ? beneficiary.getLastName() + " " : "";
			variableValue = fname + mname + lname;
			break;
		case "phoneno":
			variableValue = (beneficiary.getBenPhoneMaps().size() > 0
					? beneficiary.getBenPhoneMaps().get(0).getPhoneNo()
					: "");
			break;
		case "district":
			String district = beneficiary.getI_bendemographics().getM_district().getDistrictName() != null
					? beneficiary.getI_bendemographics().getM_district().getDistrictName() + " "
					: "";
			variableValue = district;
			break;
		case "gender":
			String gender = beneficiary.getGenderID() != null ? beneficiary.getM_gender().getGenderName() + " " : "";
			variableValue = gender;
			break;
		case "age":
			String age = beneficiary.getAge() != null ? beneficiary.getAge() + " " : "";
			variableValue = age;
			break;
		case "imrmmrname":
			String imrName = request.getInformerName() != null ? request.getInformerName() : "";
			variableValue = imrName;
			break;
		default:
			Class clazz = Class.forName(className);
			Method method = clazz.getDeclaredMethod("get" + methodName, null);
			variableValue = method.invoke(beneficiary, null).toString();
            break;
		}

		return variableValue.replace("null", "").trim();
	}

	private String getInstituteData(String className, String methodName, SMSRequest request, String authToken)
			throws Exception {

		Method method = null;

		String variableValue = "";
		Institute institute = instituteRepository.findByInstitutionID(request.getInstituteID());
		;
		switch (methodName.toLowerCase()) {
		case "statename":
			States state = stateRepository.findByStateID(request.getStateID());
			method = state.getClass().getDeclaredMethod("get" + methodName, null);
			variableValue = method.invoke(state, null).toString();
			break;
		case "districtname":
			Districts district = districtRepository.findByDistrictID(request.getDistrictID());
			method = district.getClass().getDeclaredMethod("get" + methodName, null);
			variableValue = method.invoke(district, null).toString();
			break;
		case "blockname":
			DistrictBlock block = blockRepository.findByBlockID(request.getBlockID());
			method = block.getClass().getDeclaredMethod("get" + methodName, null);
			variableValue = method.invoke(block, null).toString();
			break;
		case "institutename":
			method = institute.getClass().getDeclaredMethod("get" + methodName, null);
			variableValue = method.invoke(institute, null).toString();
			break;
		case "address":
			String address = institute.getAddress() != null ? institute.getAddress() + " " : "";
			variableValue = address;
			break;
		case "address1":
			String hospitalName1 = institute.getInstitutionName() != null ? institute.getInstitutionName() + " " : "";
			String addressValue1 = institute.getAddress() != null ? institute.getAddress() + " " : "";
			String truncatedAdress1 = hospitalName1.concat(" ").concat(addressValue1);
			if(!truncatedAdress1.isEmpty()) {
				variableValue = getSubstringInRange(truncatedAdress1, 0, 29);
			}else {
				variableValue = "";
			}
			break;
		case "address2":
			String hospitalName2 = institute.getInstitutionName() != null ? institute.getInstitutionName() + " " : "";
			String addressValue2 = institute.getAddress() != null ? institute.getAddress() + " " : "";
			String truncatedAdress2 = hospitalName2.concat(" ").concat(addressValue2);
			if(!truncatedAdress2.isEmpty()) {
				variableValue = getSubstringInRange(truncatedAdress2, 29, 59);
			}else {
				variableValue = "";
			}
			break;
		case "address3":
			String hospitalName3 = institute.getInstitutionName() != null ? institute.getInstitutionName() + " " : "";
			String addressValue3= institute.getAddress() != null ? institute.getAddress() + " " : "";
			String truncatedAdress3 = hospitalName3.concat(" ").concat(addressValue3);
			if(!truncatedAdress3.isEmpty()) {
				variableValue = getSubstringInRange(truncatedAdress3, 59, 89);
			}else {
				variableValue = "";
			}
			break;
		case "contactperson1":
			String contactPerson1 = institute.getContactPerson1() != null ? institute.getContactPerson1() + " " : "";
			variableValue = contactPerson1;
			break;
		case "contactphone1":
			String contactPhone1 = institute.getContactNo1() != null ? institute.getContactNo1() + " " : "";
			variableValue = contactPhone1;
			break;
		case "contactemail1":
			String contactEmail1 = institute.getContactPerson1_Email() != null
					? institute.getContactPerson1_Email() + " "
					: "";
			variableValue = contactEmail1;
			break;
		case "contactperson2":
			String contactPerson2 = institute.getContactPerson2() != null ? institute.getContactPerson2() + " " : "";
			variableValue = contactPerson2;
		case "contactphone2":
			String contactPhone2 = institute.getContactNo2() != null ? institute.getContactNo2() + " " : "";
			variableValue = contactPhone2;
			break;
		case "contactemail2":
			String contactEmail2 = institute.getContactPerson2_Email() != null
					? institute.getContactPerson2_Email() + " "
					: "";
			variableValue = contactEmail2;
		default:
			break;
		}
		return variableValue.trim();
	}
	
	private static String getSubstringInRange(String input, int startIndex, int endIndex) {
		if(input != null && startIndex>=0 && startIndex<input.length()) {
			int truncatedEnd = Math.min(endIndex, input.length());
			return input.substring(startIndex, truncatedEnd);
			}else {
				return " ";
			}
	}

	@Async
	@Override
	public void publishSMS() {
		RestTemplate restTemplateLogin = new RestTemplate();

		try {
			if (!SMSServiceImpl.publishingSMS) {
				SMSServiceImpl.publishingSMS = true;
				Boolean doSendSMS = ConfigProperties.getBoolean("send-sms");
				String sendSMSURL = ConfigProperties.getPropertyByName("send-message-url");
				String senderName = ConfigProperties.getPropertyByName("sms-username");
				String senderPassword = ConfigProperties.getPropertyByName("sms-password");
				String sourceAddress = ConfigProperties.getPropertyByName("source-address");
				String smsMessageType = ConfigProperties.getPropertyByName("sms-message-type");
				String smsEntityID = ConfigProperties.getPropertyByName("sms-entityid");

				java.util.Date date = new java.util.Date();
				java.sql.Date sqlDate = new java.sql.Date(date.getTime());
				String text = sqlDate.toString();
				Timestamp currentDate = new Timestamp(sqlDate.getTime());
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(sqlDate);
				calendar.add(Calendar.DATE, -7);
				java.util.Date beforeDate = calendar.getTime();
				Timestamp lastDate = new Timestamp(beforeDate.getTime());
				List<SMSNotification> smsNotificationsToSend = smsNotification
						.findPendingSMSNotifications(SMSNotification.NOT_SENT, currentDate, lastDate);

				StringBuffer phoneNo = new StringBuffer();
				for (SMSNotification sms : smsNotificationsToSend) {
					try {
						if (sms.getPhoneNo() != null && sms.getPhoneNo().length() > 10)
							phoneNo = new StringBuffer(sms.getPhoneNo().substring(sms.getPhoneNo().length() - 10));
						else
							phoneNo = new StringBuffer(sms.getPhoneNo());
						String dltTemplateId = smsTemplateRepository.findDLTTemplateID(sms.getSmsTemplateID());
						if (dltTemplateId == null)
							throw new Exception("No dltTemplateId template ID mapped");

						SmsAPIRequestModel smsAPICredentials104 = new SmsAPIRequestModel(senderName, phoneNo,
								sms.getSms(), sourceAddress, smsMessageType, dltTemplateId, smsEntityID);
						MultiValueMap<String, String> headersLogin = new LinkedMultiValueMap<String, String>();
						headersLogin.add("Content-Type", "application/json");
						String auth = senderName + ":" + senderPassword;
						headersLogin.add("Authorization",
								"Basic " + Base64.getEncoder().encodeToString(auth.getBytes()));
						logger.info("SMS API login request OBj " + smsAPICredentials104.toString());
						sms.setSmsStatus(SMSNotification.IN_PROGRESS);
						sms = smsNotification.save(sms);
						HttpEntity<Object> requestLogin = new HttpEntity<Object>(smsAPICredentials104, headersLogin);
						ResponseEntity<String> responseLogin = restTemplateLogin.exchange(sendSMSURL, HttpMethod.POST,
								requestLogin, String.class);
						if (responseLogin.getStatusCodeValue() == 200 & responseLogin.hasBody()) {
							String smsResponse = responseLogin.getBody();
							JSONObject obj = new JSONObject(smsResponse);
							String messageRequestId = null;
							if (obj != null)
								messageRequestId = obj.getString("messageRequestId");

							sms.setTransactionError(null);
							sms.setTransactionID(messageRequestId);
							sms.setIsTransactionError(false);
							sms.setSmsSentDate(new Timestamp(Calendar.getInstance().getTime().getTime()));
							sms.setSmsStatus(SMSNotification.SENT);
							sms = smsNotification.save(sms);
						} else {
							throw new Exception(responseLogin.getStatusCodeValue() + " and error "
									+ responseLogin.getStatusCode().toString());
						}
					} catch (Exception e) {
						logger.error("Failed to send sms on phone no/benRegID: " + sms.getPhoneNo() + "/"
								+ sms.getBeneficiaryRegID() + " with error " + e.getMessage(), e);
						sms.setTransactionError(e.getMessage());
						sms.setIsTransactionError(true);
						sms.setTransactionID(null);
						sms.setSmsStatus(SMSNotification.NOT_SENT);
						sms = smsNotification.save(sms);
					}
				}
			}
		} catch (Exception e) {
			logger.error("publishSMS failed with error " + e.getMessage());
		} finally {
			SMSServiceImpl.publishingSMS = false;
		}
	}

	@Override
	public String getFullSMSTemplate(SMSRequest smsRequest) throws Exception {
		SMSTemplate smsTemplate;
		smsTemplate = smsTemplateRepository.findBySmsTemplateID(smsRequest.getSmsTemplateID());
		FullSMSTemplateResponse smsTemplateResponse = smsMapper.smsTemplateToFullResponse(smsTemplate);
		return OutputMapper.gsonWithoutExposeRestriction().toJson(smsTemplateResponse);
	}

	private String getPrescriptionData(String className, String methodName, SMSRequest request,
			BeneficiaryModel beneficiary) throws NoSuchMethodException, SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, ClassNotFoundException {
		PrescribedDrug prescribedDrug = prescribedDrugRepository.findByPrescribedDrugID(request.getPrescribedDrugID());
		String variableValue = "";
		switch (methodName.toLowerCase()) {
		case "name":
			String fname = beneficiary.getFirstName() != null ? beneficiary.getFirstName() + " " : "";
			String mname = beneficiary.getMiddleName() != null ? beneficiary.getMiddleName() + " " : "";
			String lname = beneficiary.getLastName() != null ? beneficiary.getLastName() + " " : "";
			variableValue = fname + mname + lname;
			break;
		case "callerid":
			String beneficiaryID = beneficiary.getBeneficiaryID() != null ? beneficiary.getBeneficiaryID() + " " : "";
			variableValue = beneficiaryID;
			break;
		case "prescriptionid":
			String prescriptionid = prescribedDrug.getPrescriptionID() != null
					? prescribedDrug.getPrescriptionID() + " "
					: "";
			variableValue = prescriptionid;
			break;
		case "diagnosis":
			String diagnosis = prescribedDrug.getT_prescription().getDiagnosisProvided() != null
					? prescribedDrug.getT_prescription().getDiagnosisProvided() + " "
					: "";
			variableValue = diagnosis;
			break;
		case "drugname":
			String drugName = prescribedDrug.getM_104drugmapping().getDrugName() != null
					? prescribedDrug.getM_104drugmapping().getDrugName() + " "
					: "";
			variableValue = drugName;
			break;
		case "dosage":
			String dosage = prescribedDrug.getDosage() != null ? prescribedDrug.getDosage() + " " : "";
			variableValue = dosage;
			break;

		case "usage":
			String drugForm = prescribedDrug.getM_104drugmapping().getRemarks() != null
					? prescribedDrug.getM_104drugmapping().getRemarks() + " "
					: "";
			variableValue = drugForm;
			break;
			
		case "frequency":
			String freq = prescribedDrug.getFrequency() != null ? prescribedDrug.getFrequency() + " " : "";
			variableValue = freq;
			break;

		case "timeToConsume":
			String frequency = prescribedDrug.getFrequency() != null ? prescribedDrug.getFrequency() + " " : "";
			variableValue = frequency;
			break;
		case "noofdays":
			String quantity = prescribedDrug.getNoOfDays() != null ? prescribedDrug.getNoOfDays() + " " : "";
			variableValue = quantity;
			break;
		case "by":
			String createdBy = prescribedDrug.getCreatedBy() != null ? prescribedDrug.getCreatedBy() + " " : "";
			variableValue = createdBy;
			break;
		default:
			break;
		}
		return variableValue;
	}

	private String getBloodOnCallData(String className, String methodName, SMSRequest request,
			BeneficiaryModel beneficiary) throws NoSuchMethodException, SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, ClassNotFoundException {
		T_BloodRequest bloodRequest = prescribedDrugRepository.getBloodRequest(request.getBloodReqID());
		T_RequestedBloodBank requestedBloodBank = prescribedDrugRepository
				.getBloodBankAddress(request.getRequestedBloodBankID());
		String variableValue = "";
		switch (methodName.toLowerCase()) {
		case "dateofrequest":
 		Date requestDate = bloodRequest.getCreatedDate();
        String reportDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(requestDate);
        variableValue = reportDate + " ";
      	break;
		case "callercontactno":
			if (request.getIsBloodBankSMS() == true) {
				variableValue = (beneficiary.getBenPhoneMaps().size() > 0
						? beneficiary.getBenPhoneMaps().get(0).getPhoneNo()
						: "");
				break;
			} else {
				String callerContact = bloodRequest.getBbMobileNo() != null ? bloodRequest.getBbMobileNo() + " " : "";
				variableValue = callerContact;
			}
			break;
		case "nameofhospital":
			String hospitalName = bloodRequest.getHospitalAdmitted() != null ? bloodRequest.getHospitalAdmitted() + " "
					: "";
			variableValue = hospitalName;
			break;
		case "bloodgroup":
			String bloodGroup = bloodRequest.getM_bloodGroup().getBloodGroup() != null
					? bloodRequest.getM_bloodGroup().getBloodGroup() + " "
					: "";
			String componentType = bloodRequest.getM_componentType().getComponentType() != null
					? bloodRequest.getM_componentType().getComponentType() + " "
					: "";
			String unit = bloodRequest.getUnitRequired() != null ? bloodRequest.getUnitRequired() + " " : "";
			variableValue = bloodGroup + componentType + unit;
			break;
		case "patientname":
			String patientName = bloodRequest.getRecipientName() != null ? bloodRequest.getRecipientName() + " " : "";
			variableValue = patientName;
			break;
		case "addressofbloodbank":
			String bankAddress = requestedBloodBank.getBloodBankAddress() != null
					? requestedBloodBank.getBloodBankAddress() + " "
					: "";
			variableValue = bankAddress;
			break;
		case "contactperson":
			String contactPerson = requestedBloodBank.getBBPersonName() != null
					? requestedBloodBank.getBBPersonName() + " "
					: "";
			variableValue = contactPerson;
			break;
		case "contactmobilenumbers":
			String contactMobile = requestedBloodBank.getBBMobileNo() != null ? requestedBloodBank.getBBMobileNo() + " "
					: "";
			variableValue = contactMobile;
			break;
		default:
			break;
		}
		return variableValue;
	}

	private String getDirectoryserviceData(String className, String methodName, SMSRequest request)
			throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {
		Directoryservice directoryservice = prescribedDrugRepository
				.getDirectoryservice(request.getDirectoryServiceID());
		String variableValue = "";
		if(null != directoryservice && null != directoryservice.getInstitute()) {
		switch (methodName.toLowerCase()) {
		case "institutename":
			String instituteName = directoryservice.getInstitute().getInstitutionName() != null
					? directoryservice.getInstitute().getInstitutionName() + " "
					: "";
			variableValue = instituteName;
			break;
		case "statename":
			String stateName = directoryservice.getInstitute().getStates().getStateName() != null
					? directoryservice.getInstitute().getStates().getStateName() + " "
					: "";
			variableValue = stateName;
			break;
		case "districtname":
			String districtName = directoryservice.getInstitute().getDistrict().getDistrictName() != null
					? directoryservice.getInstitute().getDistrict().getDistrictName() + " "
					: "";
			variableValue = districtName;
			break;
		case "blockname":
			String blockName = directoryservice.getInstitute().getDistrictBlock().getBlockName() != null
					? directoryservice.getInstitute().getDistrictBlock().getBlockName() + " "
					: "";
			variableValue = blockName;
			break;
		case "address":
			String address = directoryservice.getInstitute().getAddress() != null
					? directoryservice.getInstitute().getAddress() + " "
					: "";
			variableValue = address;
			break;
		
		default:
			break;
		}
	}
		return variableValue;
	}

	private String getFoodSafetyComplaintData(String className, String methodName, SMSRequest request)
			throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {
		T_FoodSafetyCopmlaint foodSafetyCopmlaint = prescribedDrugRepository
				.getFoodSafetyCopmlaint(request.getFSComplaintID());
		String variableValue = "";
		switch (methodName.toLowerCase()) {
		case "fsgrievanceno":
			String requestID = foodSafetyCopmlaint.getRequestID() != null ? foodSafetyCopmlaint.getRequestID() + " "
					: "";
			variableValue = requestID;
			break;
		case "natureofcomplaint":
			String typeOfRequest = foodSafetyCopmlaint.getTypeOfRequest() != null
					? foodSafetyCopmlaint.getTypeOfRequest() + " "
					: "";
			variableValue = typeOfRequest;
			break;
		
		default:
			break;
		}
		return variableValue;
	}

	private String getEpidemicComplaintData(String className, String methodName, SMSRequest request)
			throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {
		T_EpidemicOutbreak epidemicOutbreak = prescribedDrugRepository
				.getEpidemicOutbreak(request.getOutbreakComplaintID());
		String variableValue = "";
		switch (methodName.toLowerCase()) {
		case "eogrievanceno":
			String requestID = epidemicOutbreak.getRequestID() != null ? epidemicOutbreak.getRequestID() + " " : "";
			variableValue = requestID;
			break;
		case "natureofcomplaint":
			String typeOfRequest = epidemicOutbreak.getNatureOfComplaint() != null
					? epidemicOutbreak.getNatureOfComplaint() + " "
					: "";
			variableValue = typeOfRequest;
			break;
		case "peopleaffected":
			String peopleaffected = epidemicOutbreak.getTotalPeopleAffected() != null
					? epidemicOutbreak.getTotalPeopleAffected() + " "
					: "";
			variableValue = peopleaffected;
			break;
		case "district":
			String district = epidemicOutbreak.getAffectedDistrictID() != null
					? epidemicOutbreak.getM_district().getDistrictName() + " "
					: "";
			variableValue = district;
			break;
		default:
			break;
		}
		return variableValue;
	}

	private String getGrievanceData(String className, String methodName, SMSRequest request, String authToken,
			BeneficiaryModel beneficiary) throws NoSuchMethodException, SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		FeedbackDetails feedback = feedbackReporsitory.findByFeedbackID(request.getFeedbackID());
		String variableValue = "";
		switch (methodName.toLowerCase()) {
		case "grievancecallfrom":
			String grievancecallfrom = feedback.getFeedbackID() != null
					? feedback.getFeedbackType().getFeedbackTypeName() + " "
					: "";
			variableValue = grievancecallfrom;
			break;
		case "dateofcomplaint":
			Timestamp dateofcomplaint = feedback.getServiceAvailDate();
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String stringDate = df.format(dateofcomplaint);
			variableValue = stringDate;
			break;
		case "complaintid":
			String complaintid = feedback.getRequestID() != null ? feedback.getRequestID() + " " : "";
			variableValue = complaintid;
			break;
		case "nameofhealthfacility":
			String nameofhospital = feedback.getInstitutionID() != null
					? feedback.getInstitute().getInstitutionName() + " "
					: "";
			variableValue = nameofhospital;
			break;
		case "responsiblefacilityincharge":
			String responsiblefacilityincharge = feedback.getInstituteTypeID() != null
					? feedback.getInstituteType().getInstitutionType() + " "
					: "";
			variableValue = responsiblefacilityincharge;
			break;
		case "briefcomplaint":
			String brieofcomplaint = feedback.getFeedback() != null ? feedback.getFeedback() + " " : "";
			variableValue = brieofcomplaint;
			break;
		}
		return variableValue;
	}

	private String getMCTSCallAlertData(String className, String methodName, SMSRequest request)
			throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {
		MctsOutboundCall mctsOutboundCall = outboundHistoryRepository.getMCTSCallStartDate(request.getObCallID());
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		String variableValue = "";
		switch (methodName.toLowerCase()) {
		case "callscheduleddate":
			Date callscheduleddate = mctsOutboundCall.getCallDateFrom();
			String stringDate = df.format(callscheduleddate);
			variableValue = stringDate;
			break;
		case "lmpdate":
			if (mctsOutboundCall.getMctsDataReaderDetail() != null) {
				MctsDataReaderDetail record = outboundHistoryRepository
						.getMCTSRecord(mctsOutboundCall.getMctsDataReaderDetail().getMCTSID_no());
				Date lmpDate = record.getLMP_Date();
				String stringLMPDate = df.format(lmpDate);
				variableValue = stringLMPDate;
			}
			break;
		case "outboundcalltype":
			String callType = mctsOutboundCall.getOutboundCallType() != null
					? mctsOutboundCall.getOutboundCallType() + " "
					: "";
			variableValue = callType.substring(3);
			break;

		}
		return variableValue;
	}

	private String getOrganDonationData(String className, String methodName, SMSRequest request)
			throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {
		T_OrganDonation organDonation = prescribedDrugRepository.getOrganDonation(request.getOrganDonationID());
		RequestedInstitution requestedInstitution = prescribedDrugRepository
				.getAcceptorHospitalAddress(request.getRequestedInstitutionID());
		String variableValue = "";
		switch (methodName.toLowerCase()) {
		case "organdonationid":
			String organdonationid = organDonation.getRequestID() != null ? organDonation.getRequestID() + " " : "";
			variableValue = organdonationid;
			break;
		case "organ":
			String organ = organDonation.getOrganDonationID() != null
					? organDonation.getM_donatableOrgan().getDonatableOrgan() + " "
					: "";
			variableValue = organ;
			break;
		case "acceptorhospital":
			String hospital = requestedInstitution.getInstitutionID() != null
					? requestedInstitution.getInstitute().getInstitutionName() + " "
					: "";
			String address = requestedInstitution.getInstitutionID() != null
					? requestedInstitution.getInstitute().getAddress() + " "
					: "";
			variableValue = hospital + address;
			break;
		default:
			break;
		}
		return variableValue;
	}

	private String getSpecializationAndTcDateInfo(String className, String methodName, SMSRequest request) {

		String variableValue = "";
		switch (methodName.toLowerCase()) {
		case "requestdate":
			if (request != null && request.getSmsTypeTM() != null && request.getSmsTypeTM().equals("cancel")
					&& request.getTcPreviousDate() != null)
				variableValue = request.getTcPreviousDate();
			else

				variableValue = (request != null && request.getTcDate() != null) ? request.getTcDate() : "DD-MM-YYYY";

			break;

		case "specializationid":
			String speciaLization = tCRequestModelRepo.getSpecializationDetail(request.getSpecializationID());
			variableValue = speciaLization != null ? speciaLization : "";
			break;
		default:
		}

		return variableValue;
	}

	private String getIMRMMRData(String className, String methodName, SMSRequest request) throws NoSuchMethodException,
			SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		String variableValue = "";
		Method method = null;
		switch (methodName.toLowerCase()) {

		case "imrmmrstate":

			if (request.getStateID() != null) {
				States state = stateRepository.findByStateID(request.getStateID());
				if (state.getStateName() != null)
					variableValue = state.getStateName();
			}
			break;

		case "imrmmrdate":
			if (request.getImrDate() != null) {
				variableValue = request.getImrDate();
			}
			break;
		case "deathid":
			if (request.getImrID() != null) {
				variableValue = request.getImrID();
			}
			break;
		default:
			break;
		}
		return variableValue;
	}

	private String getCOVIDData(String className, String methodName, SMSRequest request) throws NoSuchMethodException,
			SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		List<COVIDHistory> covidHistoryList = prescribedDrugRepository.getCOVIDData(request.getBeneficiaryRegID());

		COVIDHistory covidHistory = covidHistoryList.get(0);
		String variableValue = "";
		switch (methodName.toLowerCase()) {

		case "suspectedcovid19":

			if (covidHistory.getCovid19ID() != null) {
				Boolean supect = covidHistory.getSuspected_COVID19();
				if (supect == true) {
					variableValue = "Yes";
				} else {
					variableValue = "No";
				}
			}
			break;

		case "recommendation":
			String recommend = covidHistory.getCovid19ID() != null ? covidHistory.getRecommendation() + " " : "";
			variableValue = recommend;
			break;

		default:
			break;
		}
		return variableValue;
	}

	public String getUptsuData(String className, String methodName, SMSRequest request) throws NoSuchMethodException,
			SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		String variableValue = null;
		Method method = null;
		switch (methodName) {
		case "ChoName":
			if (request.getChoName() != null) {
				variableValue = request.getChoName();
			}
			break;
		case "employeeCode":
			if (request.getEmployeeCode() != null) {
				variableValue = request.getEmployeeCode();
			}
			break;
		case "beneficiaryName":
			if (request.getBeneficiaryName() != null) {
				variableValue = request.getBeneficiaryName();
			}
			break;
		case "beneficiaryId":
			if (request.getBeneficiaryId() != null) {
				variableValue = request.getBeneficiaryId().toString();
			}
			break;
		case "facilityName":
			if (request.getFacilityName() != null) {
				variableValue = request.getFacilityName();
			}
			break;
		case "HFRID":
			if (request.getHfrId() != null) {
				variableValue = request.getHfrId();
			}
			break;
		case "Date":
			if (request.getAppointmentDate() != null) {
				variableValue = request.getAppointmentDate();
			}
			break;
		case "Time":
			if (request.getAppointmentTime() != null) {
				variableValue = request.getAppointmentTime();
			}
			break;
		default:
			break;
		}
		return variableValue;
	}

}