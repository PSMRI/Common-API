package com.iemr.common.service.email;

import com.iemr.common.data.email.EmailNotification;
import com.iemr.common.data.email.EmailTemplate;
import com.iemr.common.data.email.MDSR_CDREmail;
import com.iemr.common.data.email.StockAlertData;
import com.iemr.common.data.feedback.FeedbackDetails;
import com.iemr.common.model.beneficiary.BeneficiaryModel;
import com.iemr.common.model.email.EmailRequest;
import com.iemr.common.model.excel.ExcelHelper;
import com.iemr.common.model.feedback.AuthorityEmailID;
import com.iemr.common.repository.email.EmailRepository;
import com.iemr.common.repository.email.MDSR_CDREmailRepository;
import com.iemr.common.repository.email.StockAlertDataRepo;
import com.iemr.common.repository.feedback.FeedbackRepository;
import com.iemr.common.service.beneficiary.IEMRSearchUserService;
import com.iemr.common.utils.config.ConfigProperties;
import com.iemr.common.utils.http.HttpUtils;
import com.iemr.common.utils.mapper.InputMapper;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import java.sql.Timestamp;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailServiceImplTest {


    @Test
    public void testSendEmailWithAttachment_exceptionBranch() throws Exception {
        setField("port", "notanint");
        Exception ex = assertThrows(Exception.class, () -> service.sendEmailWithAttachment("recipient@example.com",
                new ByteArrayDataSource("excel".getBytes(), "application/vnd.ms-excel")));
        assertTrue(ex.getMessage().contains("Error while Sending Mail"));
    }
    @Test
    public void testMDSRCDREmail_stageOfDeath_DuringPregnency() {
        MDSR_CDREmail benDetails = new MDSR_CDREmail();
        benDetails.setBenCallID(1);
        benDetails.setProviderServiceMapID(2);
        benDetails.setCreatedBy("creator");
        benDetails.setBeneficiaryRegID(3);
        benDetails.setUserID(4);
        benDetails.setRequestID("reqid");
        benDetails.setCreatedDate(new java.sql.Timestamp(System.currentTimeMillis()));
        benDetails.setTypeOfInfromation("type");
        benDetails.setInformerCategory("cat");
        benDetails.setInformerName("name");
        benDetails.setInformerMobileNumber("1234567890");
        benDetails.setInformerAddress("address");
        benDetails.setIdentityType("idtype");
        benDetails.setInformerIdNo("idno");
        benDetails.setVictimName("victim");
        benDetails.setVictimGuardian("guardian");
        benDetails.setVictimAge(25);
        benDetails.setRelativeMobileNumber(9876543210L);
        benDetails.setFacilityName("facility");
        benDetails.setTransitType("transit");
        benDetails.setBaseCommunity("community");
        benDetails.setNoofDelivery(1);
        benDetails.setReasonOfDeath("reason");
        benDetails.setTypeOfDelivery("delivery");
        benDetails.setVictimDistrict(1);
        benDetails.setVictimTaluk(2);
        benDetails.setVictimVillage(3);
        benDetails.setInformerDistrictid(4);
        benDetails.setInformerTalukid(5);
        benDetails.setInformerVillageid(6);
        benDetails.setTransitTypeID(7);
        benDetails.setBaseCommunityID(8);
        // Only DuringPregnancy is "yes"
        benDetails.setDuringPregnancy("yes");
        benDetails.setDuringDelivery(null);
        benDetails.setWithin42daysOfDelivery(null);
        benDetails.setAbove42daysOfDelivery(null);
        ArrayList<Object[]> demoDetails = new ArrayList<>();
        demoDetails.add(new Object[]{"IDist", "VDist", "IBlock", "VBlock", "IVillage", "VVillage"});
        when(mDSR_CDREmailRepository.getMSDR_CDRBenDetails("reqid")).thenReturn(benDetails);
        EmailTemplate emailTemplate = new EmailTemplate();
        emailTemplate.setEmailTemplateID(1);
        emailTemplate.setEmailTemplate("$$StageOfDeath$$");
        when(emailRepository.getEmailTemplateByEmailType("MDSR-CDR Email")).thenReturn(emailTemplate);
        when(mDSR_CDREmailRepository.getDemographicDetails(any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(demoDetails);
        when(emailRepository.save(any(EmailNotification.class))).thenReturn(new EmailNotification());
        String result = service.mDSRCDREmail("reqid", "MDSR-CDR Email", "recipient@example.com", "token");
        assertNotNull(result);
        assertTrue(result.contains("EmailNotification"));
    }

    @Test
    public void testMDSRCDREmail_stageOfDeath_Within42Days() {
        MDSR_CDREmail benDetails = new MDSR_CDREmail();
        benDetails.setBenCallID(1);
        benDetails.setProviderServiceMapID(2);
        benDetails.setCreatedBy("creator");
        benDetails.setBeneficiaryRegID(3);
        benDetails.setUserID(4);
        benDetails.setRequestID("reqid");
        benDetails.setCreatedDate(new java.sql.Timestamp(System.currentTimeMillis()));
        benDetails.setTypeOfInfromation("type");
        benDetails.setInformerCategory("cat");
        benDetails.setInformerName("name");
        benDetails.setInformerMobileNumber("1234567890");
        benDetails.setInformerAddress("address");
        benDetails.setIdentityType("idtype");
        benDetails.setInformerIdNo("idno");
        benDetails.setVictimName("victim");
        benDetails.setVictimGuardian("guardian");
        benDetails.setVictimAge(25);
        benDetails.setRelativeMobileNumber(9876543210L);
        benDetails.setFacilityName("facility");
        benDetails.setTransitType("transit");
        benDetails.setBaseCommunity("community");
        benDetails.setNoofDelivery(1);
        benDetails.setReasonOfDeath("reason");
        benDetails.setTypeOfDelivery("delivery");
        benDetails.setVictimDistrict(1);
        benDetails.setVictimTaluk(2);
        benDetails.setVictimVillage(3);
        benDetails.setInformerDistrictid(4);
        benDetails.setInformerTalukid(5);
        benDetails.setInformerVillageid(6);
        benDetails.setTransitTypeID(7);
        benDetails.setBaseCommunityID(8);
        // Only Within42daysOfDelivery is "yes"
        benDetails.setWithin42daysOfDelivery("yes");
        benDetails.setDuringDelivery(null);
        benDetails.setDuringPregnancy(null);
        benDetails.setAbove42daysOfDelivery(null);
        ArrayList<Object[]> demoDetails = new ArrayList<>();
        demoDetails.add(new Object[]{"IDist", "VDist", "IBlock", "VBlock", "IVillage", "VVillage"});
        when(mDSR_CDREmailRepository.getMSDR_CDRBenDetails("reqid")).thenReturn(benDetails);
        EmailTemplate emailTemplate = new EmailTemplate();
        emailTemplate.setEmailTemplateID(1);
        emailTemplate.setEmailTemplate("$$StageOfDeath$$");
        when(emailRepository.getEmailTemplateByEmailType("MDSR-CDR Email")).thenReturn(emailTemplate);
        when(mDSR_CDREmailRepository.getDemographicDetails(any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(demoDetails);
        when(emailRepository.save(any(EmailNotification.class))).thenReturn(new EmailNotification());
        String result = service.mDSRCDREmail("reqid", "MDSR-CDR Email", "recipient@example.com", "token");
        assertNotNull(result);
        assertTrue(result.contains("EmailNotification"));
    }

    @Test
    public void testMDSRCDREmail_stageOfDeath_Above42Days() {
        MDSR_CDREmail benDetails = new MDSR_CDREmail();
        benDetails.setBenCallID(1);
        benDetails.setProviderServiceMapID(2);
        benDetails.setCreatedBy("creator");
        benDetails.setBeneficiaryRegID(3);
        benDetails.setUserID(4);
        benDetails.setRequestID("reqid");
        benDetails.setCreatedDate(new java.sql.Timestamp(System.currentTimeMillis()));
        benDetails.setTypeOfInfromation("type");
        benDetails.setInformerCategory("cat");
        benDetails.setInformerName("name");
        benDetails.setInformerMobileNumber("1234567890");
        benDetails.setInformerAddress("address");
        benDetails.setIdentityType("idtype");
        benDetails.setInformerIdNo("idno");
        benDetails.setVictimName("victim");
        benDetails.setVictimGuardian("guardian");
        benDetails.setVictimAge(25);
        benDetails.setRelativeMobileNumber(9876543210L);
        benDetails.setFacilityName("facility");
        benDetails.setTransitType("transit");
        benDetails.setBaseCommunity("community");
        benDetails.setNoofDelivery(1);
        benDetails.setReasonOfDeath("reason");
        benDetails.setTypeOfDelivery("delivery");
        benDetails.setVictimDistrict(1);
        benDetails.setVictimTaluk(2);
        benDetails.setVictimVillage(3);
        benDetails.setInformerDistrictid(4);
        benDetails.setInformerTalukid(5);
        benDetails.setInformerVillageid(6);
        benDetails.setTransitTypeID(7);
        benDetails.setBaseCommunityID(8);
        // Only Above42daysOfDelivery is "yes"
        benDetails.setAbove42daysOfDelivery("yes");
        benDetails.setDuringDelivery(null);
        benDetails.setDuringPregnancy(null);
        benDetails.setWithin42daysOfDelivery(null);
        ArrayList<Object[]> demoDetails = new ArrayList<>();
        demoDetails.add(new Object[]{"IDist", "VDist", "IBlock", "VBlock", "IVillage", "VVillage"});
        when(mDSR_CDREmailRepository.getMSDR_CDRBenDetails("reqid")).thenReturn(benDetails);
        EmailTemplate emailTemplate = new EmailTemplate();
        emailTemplate.setEmailTemplateID(1);
        emailTemplate.setEmailTemplate("$$StageOfDeath$$");
        when(emailRepository.getEmailTemplateByEmailType("MDSR-CDR Email")).thenReturn(emailTemplate);
        when(mDSR_CDREmailRepository.getDemographicDetails(any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(demoDetails);
        when(emailRepository.save(any(EmailNotification.class))).thenReturn(new EmailNotification());
        String result = service.mDSRCDREmail("reqid", "MDSR-CDR Email", "recipient@example.com", "token");
        assertNotNull(result);
        assertTrue(result.contains("EmailNotification"));
    }
    @InjectMocks
    EmailServiceImpl service;
    @Mock JavaMailSender javaMailSender;
    @Mock EmailRepository emailRepository;
    @Mock FeedbackRepository feedbackRepository;
    @Mock IEMRSearchUserService searchBeneficiary;
    @Mock MDSR_CDREmailRepository mDSR_CDREmailRepository;
    @Mock StockAlertDataRepo stockAlertDataRepo;
    @Mock HttpUtils httpUtils;

    @BeforeEach
    public void setup() throws Exception {
        // Set required fields via reflection
        setField("sender", "testsender@example.com");
        setField("password", "testpass");
        setField("subject", "Test Subject");
        setField("host", "smtp.example.com");
        setField("port", "587");
    }
    private void setField(String name, Object value) throws Exception {
        java.lang.reflect.Field f = EmailServiceImpl.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(service, value);
    }

    @Test
    public void testSendEmail_success() throws Exception {
        EmailNotification notification = new EmailNotification();
        notification.setFeedbackID(1L);
        notification.setEmailID("recipient@example.com");
        notification.setIs1097(false);
        String json = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(notification);
        FeedbackDetails feedbackDetail = new FeedbackDetails();
        feedbackDetail.setBenCallID(2L);
        feedbackDetail.setServiceID(3);
        feedbackDetail.setCreatedBy("creator");
        feedbackDetail.setBeneficiaryRegID(4L);
        feedbackDetail.setUserID(5);
        when(feedbackRepository.getFeedback(1L)).thenReturn(feedbackDetail);
        EmailTemplate emailTemplate = new EmailTemplate();
        emailTemplate.setEmailTemplateID(6);
        emailTemplate.setEmailTemplate("BENEFICIARY_NAME COMPLAINT_AGAINST FEEDBACK_DESCERIPTION"); // No SUB_DISTRICT_NAME
        when(emailRepository.getEmailTemplate()).thenReturn(emailTemplate);
        BeneficiaryModel beneficiary = mock(BeneficiaryModel.class);
        when(beneficiary.getFirstName()).thenReturn("John");
        when(beneficiary.getLastName()).thenReturn("Doe");
        com.iemr.common.model.beneficiary.BeneficiaryDemographicsModel demo = mock(com.iemr.common.model.beneficiary.BeneficiaryDemographicsModel.class);
        // Ensure getBlockName returns non-null
        when(demo.getBlockName()).thenReturn(""); // Always non-null
        when(beneficiary.getI_bendemographics()).thenReturn(demo);
        when(searchBeneficiary.userExitsCheckWithId(eq(4L), anyString(), eq(false))).thenReturn(Arrays.asList(beneficiary));
        String result = service.SendEmail(json, "token");
        assertNotNull(result);
        assertTrue(result.contains("EmailNotification"));
    }

    @Test
    public void testSendEmail_noBeneficiary() throws Exception {
        EmailNotification notification = new EmailNotification();
        notification.setFeedbackID(1L);
        notification.setEmailID("recipient@example.com");
        notification.setIs1097(false);
        String json = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(notification);
        FeedbackDetails feedbackDetail = new FeedbackDetails();
        feedbackDetail.setBenCallID(2L);
        feedbackDetail.setServiceID(3);
        feedbackDetail.setCreatedBy("creator");
        feedbackDetail.setBeneficiaryRegID(null);
        feedbackDetail.setUserID(5);
        when(feedbackRepository.getFeedback(1L)).thenReturn(feedbackDetail);
        EmailTemplate emailTemplate = new EmailTemplate();
        emailTemplate.setEmailTemplateID(6);
        emailTemplate.setEmailTemplate("BENEFICIARY_NAME COMPLAINT_AGAINST FEEDBACK_DESCERIPTION"); // No SUB_DISTRICT_NAME
        when(emailRepository.getEmailTemplate()).thenReturn(emailTemplate);
        // No beneficiary returned, so do not stub beneficiary or searchBeneficiary
        String result = service.SendEmail(json, "token");
        assertNotNull(result);
    }

    @Test
    public void testSendEmail_nullFeedbackDetail() throws Exception {
        EmailNotification notification = new EmailNotification();
        notification.setFeedbackID(1L);
        notification.setEmailID("recipient@example.com");
        notification.setIs1097(false);
        String json = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(notification);
        FeedbackDetails feedbackDetail = new FeedbackDetails();
        feedbackDetail.setBenCallID(2L);
        feedbackDetail.setServiceID(3);
        feedbackDetail.setCreatedBy("creator");
        feedbackDetail.setBeneficiaryRegID(4L);
        feedbackDetail.setUserID(5);
        when(feedbackRepository.getFeedback(1L)).thenReturn(feedbackDetail);
        EmailTemplate emailTemplate = new EmailTemplate();
        emailTemplate.setEmailTemplateID(6);
        emailTemplate.setEmailTemplate("TEMPLATE"); // No SUB_DISTRICT_NAME
        when(emailRepository.getEmailTemplate()).thenReturn(emailTemplate);
        BeneficiaryModel beneficiary = mock(BeneficiaryModel.class);
        when(beneficiary.getFirstName()).thenReturn("");
        when(beneficiary.getLastName()).thenReturn("");
        com.iemr.common.model.beneficiary.BeneficiaryDemographicsModel demo = mock(com.iemr.common.model.beneficiary.BeneficiaryDemographicsModel.class);
        // Ensure getBlockName returns non-null
        when(demo.getBlockName()).thenReturn(""); // Always non-null
        when(beneficiary.getI_bendemographics()).thenReturn(demo);
        when(searchBeneficiary.userExitsCheckWithId(anyLong(), anyString(), anyBoolean())).thenReturn(Arrays.asList(beneficiary));
        String result = service.SendEmail(json, "token");
        assertNotNull(result);
    }

    @Test
    public void testSendEmailGeneral_MDSRCDR() throws Exception {
        EmailRequest req = new EmailRequest();
        req.setEmailType("MDSR-CDR Email");
        req.setRequestID("reqid");
        req.setEmailID("recipient@example.com");
        String json = new com.google.gson.Gson().toJson(req);
        EmailServiceImpl spyService = Mockito.spy(service);
        doReturn("mdsr_result").when(spyService).mDSRCDREmail(anyString(), anyString(), anyString(), anyString());
        String result = spyService.sendEmailGeneral(json, "token");
        assertEquals("mdsr_result", result);
    }

    @Test
    public void testMDSRCDREmail_success() {
        MDSR_CDREmail benDetails = new MDSR_CDREmail();
        benDetails.setBenCallID(1);
        benDetails.setProviderServiceMapID(2);
        benDetails.setCreatedBy("creator");
        benDetails.setBeneficiaryRegID(3);
        benDetails.setUserID(4);
        benDetails.setRequestID("reqid");
        benDetails.setCreatedDate(new java.sql.Timestamp(System.currentTimeMillis()));
        benDetails.setTypeOfInfromation("type");
        benDetails.setInformerCategory("cat");
        benDetails.setInformerName("name");
        benDetails.setInformerMobileNumber("1234567890");
        benDetails.setInformerAddress("address");
        benDetails.setIdentityType("idtype");
        benDetails.setInformerIdNo("idno");
        benDetails.setVictimName("victim");
        benDetails.setVictimGuardian("guardian");
        benDetails.setVictimAge(25);
        benDetails.setRelativeMobileNumber(9876543210L);
        benDetails.setFacilityName("facility");
        benDetails.setTransitType("transit");
        benDetails.setBaseCommunity("community");
        benDetails.setNoofDelivery(1);
        benDetails.setReasonOfDeath("reason");
        benDetails.setTypeOfDelivery("delivery");
        benDetails.setVictimDistrict(1);
        benDetails.setVictimTaluk(2);
        benDetails.setVictimVillage(3);
        benDetails.setInformerDistrictid(4);
        benDetails.setInformerTalukid(5);
        benDetails.setInformerVillageid(6);
        benDetails.setTransitTypeID(7);
        benDetails.setBaseCommunityID(8);
        benDetails.setDuringDelivery("yes");
        benDetails.setDuringPregnancy("yes");
        benDetails.setWithin42daysOfDelivery("yes");
        benDetails.setAbove42daysOfDelivery("yes");
        ArrayList<Object[]> demoDetails = new ArrayList<>();
        demoDetails.add(new Object[]{"IDist", "VDist", "IBlock", "VBlock", "IVillage", "VVillage"});
        when(mDSR_CDREmailRepository.getMSDR_CDRBenDetails("reqid")).thenReturn(benDetails);
        EmailTemplate emailTemplate = new EmailTemplate();
        emailTemplate.setEmailTemplateID(1);
        emailTemplate.setEmailTemplate("$$DeathID$$ $$RegDate$$ $$regType$$ $$ICategory$$ $$IName$$ $$IMobile$$ $$IDist$$ $$IBlock$$ $$IVillage$$ $$IAddress$$ $$IIDProof$$ $$IIDNo.$$ $$VName$$ $$VHName$$ $$VAge$$ $$VDist$$ $$VBlock$$ $$VVillage$$ $$VRnumber$$ $$StageOfDeath$$ $$deliveryType$$ $$deathReason$$ $$NoOfDelivery$$ $$facilityBased$$ $$duringTransit$$ $$communityBased$$ $$ENTER$$");
        when(emailRepository.getEmailTemplateByEmailType("MDSR-CDR Email")).thenReturn(emailTemplate);
        when(mDSR_CDREmailRepository.getDemographicDetails(any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(demoDetails);
        when(emailRepository.save(any(EmailNotification.class))).thenReturn(new EmailNotification());
        String result = service.mDSRCDREmail("reqid", "MDSR-CDR Email", "recipient@example.com", "token");
        assertNotNull(result);
        assertTrue(result.contains("EmailNotification"));
    }

    @Test
    public void testGetAuthorityEmailID_success() throws Exception {
        AuthorityEmailID authorityEmailID = new AuthorityEmailID();
        authorityEmailID.setDistrictID(1);
        String json = new com.google.gson.Gson().toJson(authorityEmailID);
        List<String> emailList = Arrays.asList("a@example.com", "b@example.com");
        when(emailRepository.getAuthorityEmailID(1)).thenReturn(emailList);
        String result = service.getAuthorityEmailID(json);
        assertNotNull(result);
        assertTrue(result.contains("a@example.com"));
    }

    @Test
    public void testPublishEmail_success() throws Exception {
        StockAlertData data = new StockAlertData();
        data.setEmailid("recipient@example.com");
        List<StockAlertData> stockList = Arrays.asList(data);
        when(stockAlertDataRepo.checkThresholdLimit()).thenReturn(stockList);
        // Mock ExcelHelper static method
        try (MockedStatic<ExcelHelper> excelHelperMock = mockStatic(ExcelHelper.class)) {
            excelHelperMock.when(() -> ExcelHelper.InventoryDataToExcel(anyList())).thenReturn("excel".getBytes());
            ByteArrayDataSource byteArrayDataSource = new ByteArrayDataSource("excel".getBytes(), "application/vnd.ms-excel");
            EmailServiceImpl spyService = Mockito.spy(service);
            doReturn(1).when(spyService).sendEmailWithAttachment(anyString(), any(ByteArrayDataSource.class));
            spyService.publishEmail();
            // No assertion needed, just ensure no exception
        }
    }

    @Test
    public void testPublishEmail_noData() {
        when(stockAlertDataRepo.checkThresholdLimit()).thenReturn(new ArrayList<>());
        service.publishEmail();
        // Should log "No Alert emails to be sent"
    }

    @Test
    public void testSendEmailWithAttachment_success() throws Exception {
        // Use a spy to mock sendEmailWithAttachment and avoid real SMTP connection
        EmailServiceImpl spyService = Mockito.spy(service);
        doReturn(1).when(spyService).sendEmailWithAttachment(anyString(), any(ByteArrayDataSource.class));
        int result = spyService.sendEmailWithAttachment("recipient@example.com", new ByteArrayDataSource("excel".getBytes(), "application/vnd.ms-excel"));
        assertEquals(1, result);
    }

    @Test
    public void testSendEmailWithAttachment_exception() throws Exception {
        // Use invalid port to cause exception
        setField("port", "notanint");
        Exception ex = assertThrows(Exception.class, () -> service.sendEmailWithAttachment("recipient@example.com", new ByteArrayDataSource("excel".getBytes(), "application/vnd.ms-excel")));
        assertTrue(ex.getMessage().contains("Error while Sending Mail"));
    }

    @Test
public void testSendEmailWithAttachment_nullAttachment() throws Exception {
    Exception ex = assertThrows(Exception.class, () -> 
        service.sendEmailWithAttachment("recipient@example.com", null));
    assertTrue(ex.getMessage().contains("Error while Sending Mail"));
}

@Test
public void testSendEmailWithAttachment_sendThrowsException() throws Exception {
    EmailServiceImpl spyService = Mockito.spy(service);
    // Use a valid ByteArrayDataSource
    ByteArrayDataSource attachment = new ByteArrayDataSource("excel".getBytes(), "application/vnd.ms-excel");
    // Spy the JavaMailSenderImpl to throw when send is called
    JavaMailSenderImpl mockMailSender = mock(JavaMailSenderImpl.class);
    MimeMessage mockMimeMessage = mock(MimeMessage.class);
    setField("port", "587"); // valid port
    Exception ex = assertThrows(Exception.class, () -> 
        spyService.sendEmailWithAttachment("recipient@example.com", attachment));
    assertTrue(ex.getMessage().contains("Error while Sending Mail"));
} 

@Test
public void testPublishEmail_sendErrorBranch() throws Exception {
    StockAlertData data = new StockAlertData();
    data.setEmailid("recipient@example.com");
    List<StockAlertData> stockList = Arrays.asList(data);
    when(stockAlertDataRepo.checkThresholdLimit()).thenReturn(stockList);
    try (MockedStatic<ExcelHelper> excelHelperMock = mockStatic(ExcelHelper.class)) {
        excelHelperMock.when(() -> ExcelHelper.InventoryDataToExcel(anyList())).thenReturn("excel".getBytes());
        EmailServiceImpl spyService = Mockito.spy(service);
        doReturn(0).when(spyService).sendEmailWithAttachment(anyString(), any(ByteArrayDataSource.class));
        spyService.publishEmail();
        // Should log "Error while sending email"
    }
}

@Test
public void testPublishEmail_sendThrowsExceptionBranch() throws Exception {
    StockAlertData data = new StockAlertData();
    data.setEmailid("recipient@example.com");
    List<StockAlertData> stockList = Arrays.asList(data);
    when(stockAlertDataRepo.checkThresholdLimit()).thenReturn(stockList);
    try (MockedStatic<ExcelHelper> excelHelperMock = mockStatic(ExcelHelper.class)) {
        excelHelperMock.when(() -> ExcelHelper.InventoryDataToExcel(anyList())).thenReturn("excel".getBytes());
        EmailServiceImpl spyService = Mockito.spy(service);
        doThrow(new Exception("Send failed")).when(spyService).sendEmailWithAttachment(anyString(), any(ByteArrayDataSource.class));
        spyService.publishEmail();
        // Should log exception message
    }
}

@Test
public void testPublishEmail_outerCatchBranch() throws Exception {
    when(stockAlertDataRepo.checkThresholdLimit()).thenThrow(new RuntimeException("Repo error"));
    service.publishEmail();
    // Should log error
}

@Test
public void testMDSRCDREmail_exceptionBranch() {
    when(mDSR_CDREmailRepository.getMSDR_CDRBenDetails(anyString())).thenThrow(new RuntimeException("Repo error"));
    EmailTemplate emailTemplate = new EmailTemplate();
    emailTemplate.setEmailTemplateID(1);
    emailTemplate.setEmailTemplate("$$StageOfDeath$$");
    Exception ex = assertThrows(RuntimeException.class, () ->
        service.mDSRCDREmail("reqid", "MDSR-CDR Email", "recipient@example.com", "token"));
    assertTrue(ex.getMessage().contains("Repo error"));
}
}
