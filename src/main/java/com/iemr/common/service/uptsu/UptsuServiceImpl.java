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
package com.iemr.common.service.uptsu;

import java.math.BigInteger;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.iemr.common.data.uptsu.FacilityMaster;
import com.iemr.common.data.uptsu.SmsRequestOBJ;
import com.iemr.common.data.uptsu.T_104AppointmentDetails;
import com.iemr.common.model.sms.SMSRequest;
import com.iemr.common.repository.uptsu.FacilityMasterRepo;
import com.iemr.common.repository.uptsu.T_104AppointmentDetailsRepo;
import com.iemr.common.service.sms.SMSService;
import com.iemr.common.utils.RestTemplateUtil;
import com.iemr.common.utils.exception.IEMRException;
import com.iemr.common.utils.mapper.InputMapper;
import org.springframework.beans.factory.annotation.Value;



@Service
@PropertySource("classpath:application.properties")
public class UptsuServiceImpl implements UptsuService {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	
	
	@Autowired
	private FacilityMasterRepo facilityMasterRepo;
	@Autowired
	private T_104AppointmentDetailsRepo t_104AppointmentDetailsRepo;
	@Autowired
	SMSService smsService;
	
	@Value("${CHOSmsTemplate}")
	private String CHOSmsTemplate;
	
	@Value("${BeneficiarySmsTemplate}")
	private String BeneficiarySmsTemplate;
	
	@Value("${sendSMSUrl}")
	private String sendSMSUrl;
	

	public String getFacility(Integer providerServiceMapID, String blockname) {
		List<FacilityMaster> resultSet = facilityMasterRepo.findByProviderServiceMapIDAndBlockNameAndDeleted(providerServiceMapID, blockname, false);
		return new Gson().toJson(resultSet);
		
	}
	
	public String saveAppointmentDetails(String request, String Authorization) throws Exception {
		T_104AppointmentDetails t_104AppointmentDetails = InputMapper.gson().fromJson(request, T_104AppointmentDetails.class);
		
		// Set refferedFlag to false
	    t_104AppointmentDetails.setRefferedFlag(false);
	    
		T_104AppointmentDetails temp = t_104AppointmentDetailsRepo.save(t_104AppointmentDetails);
		Object[] beneficiaryInfo = t_104AppointmentDetailsRepo.findBeneficiaryNameAndBeneficiaryIdByBenRegId(t_104AppointmentDetails.getBenRegId());
		
		
//		LocalDateTime appointmentDateTime = t_104AppointmentDetails.getAppointmentDate().toLocalDateTime();
//	    String appointTime = DateTimeFormatter.ofPattern("h a").format(appointmentDateTime);
//
		LocalDateTime appointmentDateTime = t_104AppointmentDetails.getAppointmentDate().toLocalDateTime();
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
		String appointmentDateStr = sdf.format(t_104AppointmentDetails.getAppointmentDate());
		String appointTimeString = DateTimeFormatter.ofPattern("h:mm a").format(appointmentDateTime);
//		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm a");
//		LocalDateTime appointmentTime = LocalDateTime.parse(appointTimeString, formatter);
//		Timestamp appointTimestamp = Timestamp.valueOf(appointTimeString);
		
		
	    Object[] name = (Object[]) beneficiaryInfo[0];
	    String beneficiaryName = (String) name[0];
	//    Long beneficiaryId = (Long) name[1];
	    BigInteger beneficiaryId = (BigInteger) name[1];
	    Long beneficiaryIdLong = Long.valueOf(beneficiaryId.longValue());
//	    

		
	//	return "data saved successfully with ID : " + temp.getId();
		return createSmsGateway(appointmentDateStr, appointTimeString, t_104AppointmentDetails.getFacilityPhoneNo(),
				t_104AppointmentDetails.getChoName(), t_104AppointmentDetails.getEmployeeCode(), beneficiaryName, beneficiaryIdLong,
				t_104AppointmentDetails.getFacilityName(), t_104AppointmentDetails.getHfrId(), t_104AppointmentDetails.getAlternateMobNo(), t_104AppointmentDetails.getBenRegId(),Authorization, t_104AppointmentDetails.getCreatedBy()) ;
		
	
	}
	
	public String createSmsGateway(String appointmentDate, String appointmentTime, String facilityPhoneNo, String choName, String employeeCode,
			String beneficiaryName, Long beneficiaryId, String facilityName, String hfrId, String benPhoneNo, Long benRegId, String Authorization, String createdBy) throws Exception {
//		int returnOBJ = 0;
//		int returnOBJTemp = 0;

		String smsRequestCho = createSMSRequestForCho(CHOSmsTemplate, appointmentDate, appointmentTime, facilityPhoneNo, choName, employeeCode,
				beneficiaryName, beneficiaryId, facilityName, hfrId, benRegId, createdBy);
		
		String smsRequestBen = createSMSRequestForBeneficiary(BeneficiarySmsTemplate, appointmentDate, appointmentTime, benPhoneNo, 
				beneficiaryName, beneficiaryId, facilityName, hfrId, benRegId, createdBy);

//		if (requestOBJ != null) {
//			String smsStatusForCho = restTemplate(requestOBJ, sendSMSUrl, Authorization);
//			if (smsStatusForCho != null) {
//				JsonObject jsnOBJ = new JsonObject();
//				JsonParser jsnParser = new JsonParser();
//				JsonElement jsnElmnt = jsnParser.parse(smsStatusForCho);
//				jsnOBJ = jsnElmnt.getAsJsonObject();
//				if (jsnOBJ != null && jsnOBJ.get("statusCode").getAsInt() == 200)
//					returnOBJ = 1;
//			}
//			
//		}

		// return returnOBJ;
//		if (returnOBJ == 1)
//			logger.info("CHO sms sent");
		
//		if (requestOBJTemp != null) {
//			String smsStatusForBeneficiary = restTemplate(requestOBJTemp, sendSMSUrl, Authorization);
//			if (smsStatusForBeneficiary != null) {
//				JsonObject jsnOBJ = new JsonObject();
//				JsonParser jsnParser = new JsonParser();
//				JsonElement jsnElmnt = jsnParser.parse(smsStatusForBeneficiary);
//				jsnOBJ = jsnElmnt.getAsJsonObject();
//				if (jsnOBJ != null && jsnOBJ.get("statusCode").getAsInt() == 200)
//					returnOBJTemp = 1;
//			}
//			
//		}
		try {
			SMSRequest[] benSmsRequest = InputMapper.gson().fromJson(smsRequestBen,SMSRequest[].class);
		smsService.sendSMS(Arrays.asList(benSmsRequest) , Authorization);
		
		SMSRequest[] choSmsRequest = InputMapper.gson().fromJson(smsRequestCho,SMSRequest[].class);
		smsService.sendSMS(Arrays.asList(choSmsRequest) , Authorization);
		
		return "Appointment scheduled successfully";
			
		}catch (Exception e) {
			throw new IEMRException("Error while scheduling appointment", e);
		}
		
		
//		if (returnOBJTemp == 1)
//			logger.info("Beneficiary sms sent");
		
//		if(returnOBJ == 1 && returnOBJTemp == 1)
//			return "Appointment scheduled successfully";
//		else
//			throw new IEMRException("Error while scheduling appointment");

	}
	
	public String createSMSRequestForCho(String choSms, String appointmentDate, String appointmentTime, String facilityPhoneNo, String choName,
			String employeeCode, String beneficiaryName, Long beneficiaryId, String facilityName, String hfrId, Long benRegId, String createdBy) {

		SmsRequestOBJ objCho;
		ArrayList<SmsRequestOBJ> objList = new ArrayList<>();
//		Date d = new Date((long) appointmentDate.getTime());
//		DateFormat f = new SimpleDateFormat("yyyy-MM-dd");
//		String dateStr = f.format(d);
		int smsTypeIDForCho;
		smsTypeIDForCho = t_104AppointmentDetailsRepo.getSMSTypeIDCho(choSms);

		if (smsTypeIDForCho != 0) {
			objCho = new SmsRequestOBJ();
			objCho.setSmsTemplateID(t_104AppointmentDetailsRepo.getSMSTemplateIDCho(smsTypeIDForCho));
			objCho.setAppointmentDate(appointmentDate);
			objCho.setAppointmentTime(appointmentTime);
			objCho.setChoSms(choSms);
			objCho.setChoName(choName);
			objCho.setEmployeeCode(employeeCode);
			objCho.setFacilityPhoneNo(facilityPhoneNo);
			objCho.setSmsTemplateTypeID(smsTypeIDForCho);
			objCho.setBeneficiaryName(beneficiaryName);
			objCho.setBeneficiaryId(beneficiaryId);
			objCho.setFacilityName(facilityName);
			objCho.setHfrId(hfrId);
			objCho.setBeneficiaryRegID(benRegId);
			objCho.setCreatedBy(createdBy);
			objList.add(objCho);
		}

		return new Gson().toJson(objList);
	}
	
	public String createSMSRequestForBeneficiary(String benSms, String appointmentDate, String appointmentTime, String benPhoneNo, 
			String beneficiaryName, Long beneficiaryId, String facilityName, String hfrId, Long benRegId, String createdBy) {
		
		SmsRequestOBJ objBen;
		ArrayList<SmsRequestOBJ> objList1 = new ArrayList<>();
//		Date d = new Date((long) appointmentDate.getTime());
//		DateFormat f = new SimpleDateFormat("yyyy-MM-dd");
//		String dateStr = f.format(d);
		int smsTypeIDForBeneficiary;
		smsTypeIDForBeneficiary = t_104AppointmentDetailsRepo.getSMSTypeIDBen(benSms);

		if (smsTypeIDForBeneficiary != 0) {
			objBen = new SmsRequestOBJ();
			objBen.setSmsTemplateID(t_104AppointmentDetailsRepo.getSMSTemplateIDBen(smsTypeIDForBeneficiary));
			objBen.setAppointmentDate(appointmentDate);
			objBen.setAppointmentTime(appointmentTime);
			objBen.setBenSms(benSms);
			objBen.setBenPhoneNo(benPhoneNo);
			objBen.setSmsTemplateTypeID(smsTypeIDForBeneficiary);
			objBen.setBeneficiaryName(beneficiaryName);
			objBen.setBeneficiaryId(beneficiaryId);
			objBen.setFacilityName(facilityName);
			objBen.setHfrId(hfrId);
			objBen.setBeneficiaryRegID(benRegId);
			objBen.setCreatedBy(createdBy);
			objList1.add(objBen);
		}

		return new Gson().toJson(objList1);
		
	}
	
	public String restTemplate(String requestOBJ, String url, String Authorization) {
		HttpEntity<Object> request = RestTemplateUtil.createRequestEntity(requestOBJ, Authorization);
		RestTemplate restTemplate = new RestTemplate();
		return restTemplate.exchange(url, HttpMethod.POST, request, String.class).getBody();
	}


}
