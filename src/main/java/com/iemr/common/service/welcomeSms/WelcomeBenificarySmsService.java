package com.iemr.common.service.welcomeSms;

import org.springframework.stereotype.Service;

@Service
public interface WelcomeBenificarySmsService {
    public String sendWelcomeSMStoBenificiary(String contactNo,String  beneficiaryName,String   beneficiaryId);

}
