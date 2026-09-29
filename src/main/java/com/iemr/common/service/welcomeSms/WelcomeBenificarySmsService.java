package com.iemr.common.service.welcomeSms;

import org.springframework.stereotype.Service;

@Service
public interface WelcomeBenificarySmsService {
    public void sendWelcomeSMStoBenificiary(String contactNo,String  beneficiaryName,String   beneficiaryId);

}
