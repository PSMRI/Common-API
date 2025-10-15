package com.iemr.common.data.beneficiaryConsent;

import lombok.Data;

@Data
public class BeneficiaryConsentRequest {
    private String mobNo;
    private int otp;
    private String userName;
    private String  designation;

}
