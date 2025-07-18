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
package com.iemr.common.service.beneficiaryOTPHandler;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.primitives.Ints;
import com.iemr.common.data.beneficiaryConsent.BeneficiaryConsentRequest;
import com.iemr.common.data.otp.OTPRequestParsor;
import com.iemr.common.data.sms.SMSTemplate;
import com.iemr.common.data.sms.SMSType;
import com.iemr.common.repository.sms.SMSTemplateRepository;
import com.iemr.common.repository.sms.SMSTypeRepository;
import com.iemr.common.service.otp.OTPHandler;
import com.iemr.common.service.users.IEMRAdminUserServiceImpl;
import com.iemr.common.utils.config.ConfigProperties;
import com.iemr.common.utils.http.HttpUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class BeneficiaryOTPHandlerImpl implements BeneficiaryOTPHandler {

    @Autowired
    HttpUtils httpUtils;
    @Autowired
    private IEMRAdminUserServiceImpl iEMRAdminUserServiceImpl;

    final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    @Autowired
    SMSTemplateRepository smsTemplateRepository;
    private LoadingCache<String, String> otpCache;

    @Autowired
    SMSTypeRepository smsTypeRepository;

    @Value("${sms-username}")
    private String smsUserName;

    @Value("${sms-password}")
    private String smsPassword;

    @Value("${sms-entityid}")
    private String smsEntityId;

    @Value("${source-address}")
    private String smsSourceAddress;
    @Value("${send-message-url}")
    private  String SMS_GATEWAY_URL;

    private static final Integer EXPIRE_MIN = 5;

    // Constructor for new object creation
    public BeneficiaryOTPHandlerImpl() {
        otpCache = CacheBuilder.newBuilder().expireAfterWrite(EXPIRE_MIN, TimeUnit.MINUTES)
                .build(new CacheLoader<String, String>() {
                    public String load(String key) {
                        return "0";
                    }
                });
    }

    /***
     * @param obj
     * @return success if OTP sent successfully
     */
    @Override
    public String sendOTP(BeneficiaryConsentRequest obj) throws Exception {
        int otp = generateOTP(obj.getMobNo());
        return sendSMS(otp, obj);
    }

    /***
     * @param obj
     * @return OTP verification success or failure
     *
     */
    @Override
    public JSONObject validateOTP(BeneficiaryConsentRequest obj) throws Exception {
        String cachedOTP = otpCache.get(obj.getMobNo());
        String inputOTPEncrypted = getEncryptedOTP(obj.getOtp());

        if (cachedOTP.equalsIgnoreCase(inputOTPEncrypted)) {
            JSONObject responseObj = new JSONObject();
            responseObj.put("userName", obj.getMobNo());
            responseObj.put("userID", obj.getMobNo());

            JSONObject responseOBJ = iEMRAdminUserServiceImpl.generateKeyPostOTPValidation(responseObj);

            return responseOBJ;
        } else {
            throw new Exception("Please enter valid OTP");
        }

    }

    /***
     * @param obj
     * @return success if OTP re-sent successfully
     */
    @Override
    public String resendOTP(BeneficiaryConsentRequest obj) throws Exception {
        int otp = generateOTP(obj.getMobNo());
        return sendSMS(otp, obj);
    }

    // generate 6 digit random no #
    public int generateOTP(String authKey) throws Exception {
        String generatedPassword = null;
        Random random = SecureRandom.getInstanceStrong();
        int otp = 100000 + random.nextInt(900000);

        generatedPassword = getEncryptedOTP(otp);

        if (otpCache != null)
            otpCache.put(authKey, generatedPassword);
        else {
            BeneficiaryOTPHandlerImpl obj = new BeneficiaryOTPHandlerImpl();
            obj.otpCache.put(authKey, generatedPassword);
        }
        return otp;
    }

    // SHA-256 encoding logic implemented
    private String getEncryptedOTP(int otp) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] bytes = md.digest(Ints.toByteArray(otp));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();
    }

    // send SMS to beneficiary


    public String sendSMS(int otp, BeneficiaryConsentRequest obj) {

        final RestTemplate restTemplate = new RestTemplate();

        String dltTemplateId = smsTemplateRepository.findDLTTemplateID(28);
        SMSTemplate template = smsTemplateRepository.findBySmsTemplateID(28);

        String sendSMSAPI = SMS_GATEWAY_URL;
        logger.info("sms template"+template);

        try {
                        String  message ="Hello! Your OTP for providing consent for registration on AMRIT is "+otp+". This OTP is valid for 10 minutes. Kindly share it only with Asha to complete the process. Regards PSMRI";
//            String message = template.getSmsTemplate()
//                    .replace("$$OTP$$",String.valueOf(otp))
//                    .replace("$$UserName$$", obj.getUserName())
//                    .replace("$$Designation$$", obj.getDesignation());

            // Build payload
            Map<String, Object> payload = new HashMap<>();
            payload.put("customerId",smsUserName);
            payload.put("destinationAddress", obj.getMobNo());
            payload.put("message", message);
            payload.put("sourceAddress", smsSourceAddress);
            payload.put("messageType", "SERVICE_IMPLICIT");
            payload.put("dltTemplateId", "1007730329175402034");
            payload.put("entityId",smsEntityId );
            payload.put("otp", true);
            // Set headers
            HttpHeaders headers = new HttpHeaders();
            String auth = smsUserName + ":" + smsPassword;
            headers.add("Authorization",
                    "Basic " + Base64.getEncoder().encodeToString(auth.getBytes()));

            headers.setContentType(MediaType.APPLICATION_JSON);
             logger.info("payload: "+payload);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            // Call API
            ResponseEntity<String> response = restTemplate.postForEntity(sendSMSAPI, request, String.class);
            logger.info("sms-response:"+response.getBody());
            if(response.getStatusCode().value()==200){
                return  "OTP sent successfully on register mobile number";
            }else {
                return "Fail";

            }

        } catch (Exception e) {
            return "Error sending SMS: " + e.getMessage();
        }
    }

}
