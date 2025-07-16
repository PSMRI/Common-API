package com.iemr.common.service.beneficiaryOTPHandler;

import com.iemr.common.data.beneficiaryConsent.BeneficiaryConsentRequest;
import com.iemr.common.data.otp.OTPRequestParsor;
import org.json.JSONObject;

public interface BeneficiaryOTPHandler {
    public String sendOTP(BeneficiaryConsentRequest obj) throws Exception;

    public JSONObject validateOTP(BeneficiaryConsentRequest obj) throws Exception;

    public String resendOTP(BeneficiaryConsentRequest obj) throws Exception;

}
