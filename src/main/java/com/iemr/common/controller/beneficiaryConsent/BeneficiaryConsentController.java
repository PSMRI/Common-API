package com.iemr.common.controller.beneficiaryConsent;

import com.iemr.common.data.beneficiaryConsent.BeneficiaryConsentRequest;
import com.iemr.common.data.otp.OTPRequestParsor;
import com.iemr.common.service.beneficiaryOTPHandler.BeneficiaryOTPHandler;
import com.iemr.common.service.otp.OTPHandler;
import com.iemr.common.utils.mapper.InputMapper;
import com.iemr.common.utils.response.OutputResponse;
import io.lettuce.core.dynamic.annotation.Param;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.ws.rs.core.MediaType;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

public class BeneficiaryConsentController {
    final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Autowired
    private BeneficiaryOTPHandler beneficiaryOTPHandler;

    @Operation(summary = "Send Consent")
    @RequestMapping(value = "/sendConsent", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
    public String sendConsent(@Param(value = "{\"mobNo\":\"String\"}") @RequestBody String requestOBJ) {
        logger.info(requestOBJ.toString());

        OutputResponse response = new OutputResponse();

        try {
            BeneficiaryConsentRequest obj = InputMapper.gson().fromJson(requestOBJ, BeneficiaryConsentRequest.class);

            String success = beneficiaryOTPHandler.sendOTP(obj); // method name unchanged if internal logic still uses 'OTP'
            logger.info(success.toString());
            if (success.contains("otp"))
                response.setResponse(success);
            else
                response.setError(500, "failure");

        } catch (Exception e) {
            logger.error("error in sending Consent : " + e);
            response.setError(500, "error : " + e);
        }
        return response.toString();
    }

    @Operation(summary = "Validate Consent")
    @RequestMapping(value = "/validateConsent", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
    public String validateConsent(@Param(value = "{\"mobNo\":\"String\",\"otp\":\"Integer\"}") @RequestBody String requestOBJ) {

        OutputResponse response = new OutputResponse();

        try {
            OTPRequestParsor obj = InputMapper.gson().fromJson(requestOBJ, OTPRequestParsor.class);

            JSONObject responseOBJ = beneficiaryOTPHandler.validateOTP(obj);
            if (responseOBJ != null)
                response.setResponse(responseOBJ.toString());
            else
                response.setError(500, "failure");

        } catch (Exception e) {
            logger.error("error in validating Consent : " + e);
            response.setError(500, "error : " + e);
        }
        return response.toString();
    }

    @Operation(summary = "Resend Consent")
    @RequestMapping(value = "/resendConsent", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
    public String resendConsent(@Param(value = "{\"mobNo\":\"String\"}") @RequestBody String requestOBJ) {
        logger.info(requestOBJ.toString());

        OutputResponse response = new OutputResponse();

        try {
            OTPRequestParsor obj = InputMapper.gson().fromJson(requestOBJ, OTPRequestParsor.class);

            String success = beneficiaryOTPHandler.resendOTP(obj);
            logger.info(success.toString());

            if (success.contains("otp"))
                response.setResponse(success);
            else
                response.setError(500, "failure");

        } catch (Exception e) {
            logger.error("error in re-sending Consent : " + e);
            response.setError(500, "error : " + e);
        }
        return response.toString();
    }


}


