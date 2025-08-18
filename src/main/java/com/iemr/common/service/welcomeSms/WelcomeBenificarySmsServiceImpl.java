package com.iemr.common.service.welcomeSms;

import com.iemr.common.data.sms.SMSTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
@Service
public class WelcomeBenificarySmsServiceImpl  implements   WelcomeBenificarySmsService{
    final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    @Value("${sms-username}")
    private String smsUserName;

    // @Value("${sms-password}")
    private String smsPassword = "]Kt9GAp8}$S*@";

    @Value("${sms-entityid}")
    private String smsEntityId;

//    @Value("${source-address}")
    private String smsSourceAddress ="PSMRAM";
    @Value("${send-message-url}")
    private  String SMS_GATEWAY_URL;

    @Override
    public String sendWelcomeSMStoBenificiary(String contactNo,String  beneficiaryName,String   beneficiaryId) {
        final RestTemplate restTemplate = new RestTemplate();

        String dltTemplateId = "1007006798022225953";

        String sendSMSAPI = SMS_GATEWAY_URL;

        try {

            String  message = "Dear "+beneficiaryName+", your unique Beneficiary ID is "+beneficiaryId+". Thank you for registering. Regards PSMRI";
            // Build payload
            Map<String, Object> payload = new HashMap<>();
            payload.put("customerId",smsUserName);
            payload.put("destinationAddress", contactNo);
            payload.put("message", message);
            payload.put("sourceAddress", smsSourceAddress);
            payload.put("messageType", "SERVICE_IMPLICIT");
            payload.put("dltTemplateId", dltTemplateId);
            payload.put("entityId",smsEntityId );
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
            return "Error sending SMS: " + e.getMessage().toString();
        }

    }
}
