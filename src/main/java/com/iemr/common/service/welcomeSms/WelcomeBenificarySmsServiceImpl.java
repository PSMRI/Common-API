package com.iemr.common.service.welcomeSms;

import com.google.common.cache.LoadingCache;
import com.iemr.common.data.sms.SMSTemplate;
import com.iemr.common.repository.sms.SMSTemplateRepository;
import com.iemr.common.repository.sms.SMSTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class WelcomeBenificarySmsServiceImpl implements WelcomeBenificarySmsService {
    final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    @Value("${sms-username}")
    private String smsUserName;

    @Value("${sms-password}")
    private String smsPassword;

    @Value("${sms-entityid}")
    private String smsEntityId;

    @Value("${sms-consent-source-address}")
    private String smsSourceAddress;
    
    @Value("${send-message-url}")
    private String SMS_GATEWAY_URL;

    @Autowired
    SMSTemplateRepository smsTemplateRepository;

    @Autowired
    SMSTypeRepository smsTypeRepository;

    private String smsTemplateName = "welcome_sms";

    private String smsTemplate =null;

    @Override
    @Async
    public String sendWelcomeSMStoBenificiary(String contactNo, String beneficiaryName, String beneficiaryId) {

        try {
            String sendSMSAPI = SMS_GATEWAY_URL;

            final RestTemplate restTemplate = new RestTemplate();

            Optional<SMSTemplate> smsTemplateData = smsTemplateRepository.findBySmsTemplateName(smsTemplateName);
            if (smsTemplateData.isPresent()) {
                smsTemplate = smsTemplateRepository.findBySmsTemplateID(smsTemplateData.get().getSmsTemplateID()).getSmsTemplate();
            }
            if(smsTemplate!=null){
                String message = smsTemplate.replace("$$BENE_NAME$$", beneficiaryName).replace("$$BEN_ID$$", beneficiaryId);
                // Build payload
                Map<String, Object> payload = new HashMap<>();
                payload.put("customerId", smsUserName);
                payload.put("destinationAddress", contactNo);
                payload.put("message", message);
                payload.put("sourceAddress", smsSourceAddress);
                payload.put("messageType", "SERVICE_IMPLICIT");
                payload.put("dltTemplateId", smsTemplateData.get().getDltTemplateId());
                payload.put("entityId", smsEntityId);
                // Set headers
                HttpHeaders headers = new HttpHeaders();
                String auth = smsUserName + ":" + smsPassword;
                headers.add("Authorization",
                        "Basic " + Base64.getEncoder().encodeToString(auth.getBytes()));

            headers.setContentType(MediaType.APPLICATION_JSON);
            logger.info("payload: " + payload);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

                // Call API
                ResponseEntity<String> response = restTemplate.postForEntity(sendSMSAPI, request, String.class);
                logger.info("sms-response:" + response.getBody());
                if (response.getStatusCode().value() == 200) {
                    return "OTP sent successfully on register mobile number";
                } else {
                    return "Fail";

                }
            }


        } catch (Exception e) {
            return "Error sending SMS: " + e.getMessage().toString();
        }
       return null;
    }
}
