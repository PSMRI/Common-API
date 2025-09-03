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
package com.iemr.common.service.email;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
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

@Service
public class EmailServiceImpl implements EmailService {

	private InputMapper inputMapper = new InputMapper();
	@Autowired
	private JavaMailSender javaMailSender;
	@Value("${spring.mail.username}")
	private String sender;
	@Value("${spring.mail.password}")
	private String password;
	@Value("${mail-subject}")
	private String subject;
	@Value("${spring.mail.host}")
	private String host;
	@Value("${spring.mail.port}")
	private String port;
	@Autowired
	HttpUtils httpUtils;

	Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	private EmailRepository emailRepository;

	@Autowired
	private StockAlertDataRepo stockAlertDataRepo;

	/**
	 * @param emailRepository the emailRepository to set
	 */
	@Autowired
	public void setEmailRepository(EmailRepository emailRepository) {
		this.emailRepository = emailRepository;
	}

	private FeedbackRepository feedbackRepository;

	/**
	 * @param feedbackRepository the feedbackRepository to set
	 */
	@Autowired
	public void setFeedbackRepository(FeedbackRepository feedbackRepository) {
		this.feedbackRepository = feedbackRepository;
	}

	@Autowired
	IEMRSearchUserService searchBeneficiary;

	@Autowired
	MDSR_CDREmailRepository mDSR_CDREmailRepository;

	@Override
	public String SendEmail(String request, String authToken) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		EmailNotification notification = objectMapper.readValue(request, EmailNotification.class);

		FeedbackDetails feedbackDetail = feedbackRepository.getFeedback(notification.getFeedbackID());

		EmailTemplate emailTemplate = emailRepository.getEmailTemplate();

		EmailNotification emailNotification = new EmailNotification();

		emailNotification.setEmailTemplateID(emailTemplate.getEmailTemplateID());
		emailNotification.setBenCallID(feedbackDetail.getBenCallID());
		emailNotification.setProviderServiceMapID(feedbackDetail.getServiceID());
		emailNotification.setEmailStatus(EmailNotification.NOT_SENT);
		emailNotification.setCreatedBy(feedbackDetail.getCreatedBy());
		emailNotification.setEmailTriggerDate(new Timestamp(Calendar.getInstance().getTime().getTime()));
		emailNotification.setBeneficiaryRegID(feedbackDetail.getBeneficiaryRegID());
		emailNotification.setReceivingUserID(feedbackDetail.getUserID());
		emailNotification.setEmailID(notification.getEmailID());

		String emailToSend = "";

		BeneficiaryModel beneficiary = null;
		if (feedbackDetail != null && feedbackDetail.getBeneficiaryRegID() != null) {
			List<BeneficiaryModel> beneficiaries = searchBeneficiary
					.userExitsCheckWithId(feedbackDetail.getBeneficiaryRegID(), authToken, notification.getIs1097());
			if (beneficiaries.size() == 1)
				beneficiary = beneficiaries.get(0);
		}
		String benName = null, subDistrictName = null, complaintAgainst = null, feedbackdescription = null;
		if (beneficiary != null) {
			benName = beneficiary.getFirstName() + " " + beneficiary.getLastName();
			subDistrictName = beneficiary.getI_bendemographics().getBlockName();
		}

		if (feedbackDetail != null) {
			complaintAgainst = feedbackDetail.getFeedbackAgainst();
			feedbackdescription = feedbackDetail.getFeedback();
		}

		// Ensure all replacements are non-null
		benName = benName == null ? "" : benName;
		complaintAgainst = complaintAgainst == null ? "" : complaintAgainst;
		feedbackdescription = feedbackdescription == null ? "" : feedbackdescription;

		emailToSend = emailTemplate.getEmailTemplate().replace("BENEFICIARY_NAME", benName)
				.replace("COMPLAINT_AGAINST", complaintAgainst).replace("FEEDBACK_DESCERIPTION", feedbackdescription);

		if (subDistrictName != null) {
			emailToSend = emailToSend.replace("SUB_DISTRICT_NAME", subDistrictName);
		} else {
			emailToSend = emailToSend.replace("from SUB_DISTRICT_NAME", "");
		}
		emailNotification.setEmail(emailToSend);

		emailRepository.save(emailNotification);

		return emailNotification.toString();
	}

	/*
	 * DU20091017 For sending email by passing request ID and email Type
	 */
	@Override
	public String sendEmailGeneral(String emailRequest, String authToken) throws Exception {

		EmailRequest emailReq = InputMapper.gson().fromJson(emailRequest, EmailRequest.class);

		String emailResponse = null;
		switch (emailReq.getEmailType()) {
		case "MDSR-CDR Email":
			emailResponse = mDSRCDREmail(emailReq.getRequestID(), emailReq.getEmailType(), emailReq.getEmailID(),
					authToken);

		}

		return emailResponse;
	}

	/*
	 * DU20091017 send MDSR_CDR(IMR-MMR) email to Nodal officer
	 */
	public String mDSRCDREmail(String requestID, String emailType, String emailID, String authToken) {

		MDSR_CDREmail mDSR_CDRBenDetails = mDSR_CDREmailRepository.getMSDR_CDRBenDetails(requestID);

		EmailTemplate emailTemplate = emailRepository.getEmailTemplateByEmailType(emailType);

		EmailNotification emailNotification = new EmailNotification();

		emailNotification.setEmailTemplateID(emailTemplate.getEmailTemplateID());
		emailNotification.setBenCallID((long) mDSR_CDRBenDetails.getBenCallID());
		emailNotification.setProviderServiceMapID(mDSR_CDRBenDetails.getProviderServiceMapID());
		emailNotification.setEmailStatus(EmailNotification.NOT_SENT);
		emailNotification.setCreatedBy(mDSR_CDRBenDetails.getCreatedBy());
		emailNotification.setEmailTriggerDate(new Timestamp(Calendar.getInstance().getTime().getTime()));
		emailNotification.setBeneficiaryRegID((long) mDSR_CDRBenDetails.getBeneficiaryRegID());
		emailNotification.setReceivingUserID(mDSR_CDRBenDetails.getUserID());
		emailNotification.setEmailID(emailID);

//		if (mDSR_CDRBenDetails.getBeneficiaryRegID() != null) {
//			List<BeneficiaryModel> beneficiaries = searchBeneficiary
//					.userExitsCheckWithId(mDSR_CDRBenDetails.getBeneficiaryRegID(), authToken, notification.getIs1097());
//			if (beneficiaries.size() == 1)
//				beneficiary = beneficiaries.get(0);
//		}

		ArrayList<Object[]> benDemographicsDetails = mDSR_CDREmailRepository.getDemographicDetails(
				mDSR_CDRBenDetails.getVictimDistrict(), mDSR_CDRBenDetails.getVictimTaluk(),
				mDSR_CDRBenDetails.getVictimVillage(), mDSR_CDRBenDetails.getInformerDistrictid(),
				mDSR_CDRBenDetails.getInformerTalukid(), mDSR_CDRBenDetails.getInformerVillageid(),
				mDSR_CDRBenDetails.getTransitTypeID(), mDSR_CDRBenDetails.getBaseCommunityID());

		String stageOfDeath = null;

		if (mDSR_CDRBenDetails.getDuringDelivery() != null
				&& mDSR_CDRBenDetails.getDuringDelivery().equalsIgnoreCase("yes"))
			stageOfDeath = "During Delievry";

		if (mDSR_CDRBenDetails.getDuringPregnancy() != null
				&& mDSR_CDRBenDetails.getDuringPregnancy().equalsIgnoreCase("yes"))
			if (stageOfDeath != null)
				stageOfDeath = stageOfDeath + " & " + "During Pregnancy";
			else
				stageOfDeath = "During Pregnency";

		if (mDSR_CDRBenDetails.getWithin42daysOfDelivery() != null
				&& mDSR_CDRBenDetails.getWithin42daysOfDelivery().equalsIgnoreCase("yes"))
			if (stageOfDeath != null)
				stageOfDeath = stageOfDeath + " & " + "With In 42 Days of Delivery";
			else
				stageOfDeath = "With In 42 Days of Delivery";

		if (mDSR_CDRBenDetails.getAbove42daysOfDelivery() != null
				&& mDSR_CDRBenDetails.getAbove42daysOfDelivery().equalsIgnoreCase("yes"))
			if (stageOfDeath != null)
				stageOfDeath = stageOfDeath + " & " + "Above 42 Days of Delivery";
			else
				stageOfDeath = "Above 42 Days of Delivery";

		String emailToSend = "";

		emailToSend = emailTemplate.getEmailTemplate()
				.replace("$$DeathID$$",
						mDSR_CDRBenDetails.getRequestID() == null ? "" : mDSR_CDRBenDetails.getRequestID())
				.replace("$$RegDate$$",
						mDSR_CDRBenDetails.getCreatedDate() == null ? ""
								: mDSR_CDRBenDetails.getCreatedDate().toString())
				.replace("$$regType$$",
						mDSR_CDRBenDetails.getTypeOfInfromation() == null ? ""
								: mDSR_CDRBenDetails.getTypeOfInfromation())
				.replace("$$ICategory$$",
						mDSR_CDRBenDetails.getInformerCategory() == null ? ""
								: mDSR_CDRBenDetails.getInformerCategory())
				.replace("$$IName$$",
						mDSR_CDRBenDetails.getInformerName() == null ? "" : mDSR_CDRBenDetails.getInformerName())
				.replace("$$IMobile$$",
						mDSR_CDRBenDetails.getInformerMobileNumber() == null ? ""
								: mDSR_CDRBenDetails.getInformerMobileNumber())
				.replace("$$IDist$$",
						benDemographicsDetails.get(0)[0] == null ? ""
								: String.valueOf(benDemographicsDetails.get(0)[0]))
				.replace("$$IBlock$$",
						benDemographicsDetails.get(0)[2] == null ? ""
								: String.valueOf(benDemographicsDetails.get(0)[2]))
				.replace("$$IVillage$$",
						benDemographicsDetails.get(0)[4] == null ? ""
								: String.valueOf(benDemographicsDetails.get(0)[4]))
				.replace("$$IAddress$$",
						mDSR_CDRBenDetails.getInformerAddress() == null ? "" : mDSR_CDRBenDetails.getInformerAddress())
				.replace("$$IIDProof$$",
						mDSR_CDRBenDetails.getIdentityType() == null ? "" : mDSR_CDRBenDetails.getIdentityType())
				.replace("$$IIDNo.$$",
						mDSR_CDRBenDetails.getInformerIdNo() == null ? "" : mDSR_CDRBenDetails.getInformerIdNo())
				.replace("$$VName$$",
						mDSR_CDRBenDetails.getVictimName() == null ? "" : mDSR_CDRBenDetails.getVictimName())
				.replace("$$VHName$$",
						mDSR_CDRBenDetails.getVictimGuardian() == null ? "" : mDSR_CDRBenDetails.getVictimGuardian())
				.replace("$$VAge$$",
						mDSR_CDRBenDetails.getVictimAge() == null ? "" : mDSR_CDRBenDetails.getVictimAge().toString())
				.replace("$$VDist$$",
						benDemographicsDetails.get(0)[1] == null ? ""
								: String.valueOf(benDemographicsDetails.get(0)[1]))
				.replace("$$VBlock$$",
						benDemographicsDetails.get(0)[3] == null ? ""
								: String.valueOf(benDemographicsDetails.get(0)[3]))
				.replace("$$VVillage$$",
						benDemographicsDetails.get(0)[5] == null ? ""
								: String.valueOf(benDemographicsDetails.get(0)[5]))
				.replace("$$VRnumber$$",
						mDSR_CDRBenDetails.getRelativeMobileNumber() == null ? ""
								: mDSR_CDRBenDetails.getRelativeMobileNumber().toString())
				.replace("$$StageOfDeath$$", stageOfDeath)
				.replace("$$deliveryType$$",
						mDSR_CDRBenDetails.getTypeOfDelivery() == null ? "" : mDSR_CDRBenDetails.getTypeOfDelivery())
				.replace("$$deathReason$$",
						mDSR_CDRBenDetails.getReasonOfDeath() == null ? "" : mDSR_CDRBenDetails.getReasonOfDeath())
				.replace("$$NoOfDelivery$$",
						mDSR_CDRBenDetails.getNoofDelivery() == null ? ""
								: mDSR_CDRBenDetails.getNoofDelivery().toString())
				.replace("$$facilityBased$$",
						mDSR_CDRBenDetails.getFacilityName() == null ? "" : mDSR_CDRBenDetails.getFacilityName())
				.replace("$$duringTransit$$",
						mDSR_CDRBenDetails.getTransitType() == null ? "" : mDSR_CDRBenDetails.getTransitType())
				.replace("$$communityBased$$",
						mDSR_CDRBenDetails.getBaseCommunity() == null ? "" : mDSR_CDRBenDetails.getBaseCommunity())
				.replace("$$ENTER$$", "\n");

		emailNotification.setEmail(emailToSend);

		emailRepository.save(emailNotification);

		return emailNotification.toString();
//		return "Success";
	}

	@Override
	public String getAuthorityEmailID(String request) throws Exception {
		AuthorityEmailID authorityEmailID = inputMapper.gson().fromJson(request, AuthorityEmailID.class);

		List emaiList = emailRepository.getAuthorityEmailID(authorityEmailID.getDistrictID());

		return emaiList.toString();
	}

	@Async
	@Override
	public void publishEmail() {

		try {

			// fetch record from new view
			List<StockAlertData> stockAlertDataList = stockAlertDataRepo.checkThresholdLimit();
			HashMap<String, List<StockAlertData>> dataMap = new HashMap<>();
			List<StockAlertData> dataList;

			if (stockAlertDataList != null && stockAlertDataList.size() > 0) {
				for (StockAlertData stockAlertData : stockAlertDataList) {
					if (dataMap.containsKey(stockAlertData.getEmailid())) {
						List<StockAlertData> tempDataList = dataMap.get(stockAlertData.getEmailid());
						tempDataList.add(stockAlertData);
						dataMap.replace(stockAlertData.getEmailid(), tempDataList);

					} else {
						dataList = new ArrayList<>();
						dataList.add(stockAlertData);
						if (stockAlertData != null && stockAlertData.getEmailid() != null)
							dataMap.put(stockAlertData.getEmailid(), dataList);
					}
				}
			} else {
				logger.info("No Alert emails to be sent");
			}
			int sendMail = 0;
			for (Entry<String, List<StockAlertData>> entry : dataMap.entrySet()) {
				logger.info(entry.getKey() + ": key");
				logger.info(entry.getValue().toString() + ": value");
				try {
					byte[] bytes = ExcelHelper.InventoryDataToExcel(entry.getValue());
					ByteArrayDataSource byteArrayOutputStream = new ByteArrayDataSource(bytes,
							"application/vnd.ms-excel");
					sendMail = sendEmailWithAttachment(entry.getKey(), byteArrayOutputStream);
					if (sendMail == 1)
						logger.info("Email successfully sent to " + entry.getKey());
					else
						logger.info("Error while sending email to " + entry.getKey());
				} catch (Exception e) {
					logger.info(e.getLocalizedMessage());
				}
			}
		} catch (Exception e) {
			logger.error("publishEmail failed with error " + e.getMessage());
		}

	}

	int sendEmailWithAttachment(String recipient, ByteArrayDataSource attachment) throws Exception {
		try {
			// Creating a mime message
			JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
			mailSender.setHost(host);
			mailSender.setPort(Integer.parseInt(port));

			String decryptSender = sender;
			String decryptPass = password;

			mailSender.setUsername(decryptSender);
			mailSender.setPassword(decryptPass);
			Properties props = mailSender.getJavaMailProperties();
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.starttls.enable", "true");
			MimeMessage mimeMessage = mailSender.createMimeMessage();
			MimeMessageHelper mimeMessageHelper;
			mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
			mimeMessageHelper.setFrom(sender);
			mimeMessageHelper.setTo(recipient);
			mimeMessageHelper.setText("Sample");
			mimeMessageHelper.setSubject(subject);
			mimeMessageHelper.addAttachment("InventoryStockAlert.xlsx", attachment);

			// Sending the mail
			mailSender.send(mimeMessage);
			return 1;
		}

		catch (Exception e) {
			throw new Exception("Error while Sending Mail to " + recipient + " : " + e.getLocalizedMessage());
		}
	}
}
