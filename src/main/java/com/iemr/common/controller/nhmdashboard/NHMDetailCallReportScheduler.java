package com.iemr.common.controller.nhmdashboard;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.iemr.common.data.callhandling.BeneficiaryCall;
import com.iemr.common.data.nhm_dashboard.DetailedCallReport;
import com.iemr.common.repository.callhandling.IEMRCalltypeRepositoryImplCustom;
import com.iemr.common.repository.nhm_dashboard.DetailedCallReportRepo;
import com.iemr.common.repository.report.CallReportRepo;
import com.iemr.common.utils.config.ConfigProperties;

@EnableScheduling
@Configuration
public class NHMDetailCallReportScheduler {

	@Autowired
	private DetailedCallReportRepo detailedCallReportRepo;

	@Autowired
	private CallReportRepo callReportRepo;
	
	@Autowired
	private IEMRCalltypeRepositoryImplCustom iEMRCalltypeRepositoryImplCustom;

	Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	@Value("${start-ctidatacheck-scheduler}")
	private boolean startCtiDataCheckFlag;


	@Scheduled(cron = "${cron-scheduler-ctidatacheck}")
	public void detailedCallReport() {
		if (startCtiDataCheckFlag) {
			try {
				String endDate = null;
				String fromDate = null;
				LocalDateTime date = null;
				date = LocalDateTime.now().minusDays(1);
				String[] dateArr = date.toString().split("T");
				endDate = dateArr[0].concat(" 23:59:59");
				fromDate = dateArr[0].concat(" 00:00:01");

				Timestamp fromTime = Timestamp.valueOf(fromDate);
				Timestamp endTime = Timestamp.valueOf(endDate);
				logger.info("Call detailed report check request - FromTime -" + fromTime + "Endtime - " + endTime);
				List<DetailedCallReport> findByCallStartTimeBetween = detailedCallReportRepo
						.findByCallStartTimeBetween(fromTime, endTime);
				for (DetailedCallReport detailedCallReport : findByCallStartTimeBetween) {
					String phoneNo = detailedCallReport.getPHONE();
					String sessionID = detailedCallReport.getSession_ID();
					if(null != phoneNo && phoneNo.length()>10) {
						phoneNo = phoneNo.replaceFirst("^0+", "");
					}
					BeneficiaryCall existRecord = callReportRepo.getBenCallDetailsBySessionIDAndPhone(sessionID,phoneNo);
					if (existRecord != null) {
						logger.info("Record already present in t_bencall table with sessionID : " + sessionID
								+ " phoneNo : " + phoneNo);
						logger.info("Existing record from t_bencall: " + existRecord);
						if (null != detailedCallReport.getOrientation_Type()
								&& detailedCallReport.getOrientation_Type().equalsIgnoreCase("OUTBOUND")) {
							if(existRecord.getCallTypeID() == null) {
								logger.info("Called Service Id: " + existRecord.getCalledServiceID());
							// Fetching callTypeId
							Integer callTypeId = getCallTypeId(true, existRecord.getCalledServiceID(),
									detailedCallReport);

							logger.info("CallTypeId after comparison: " + callTypeId);
							if (callTypeId != null) {
								callReportRepo.updateIsOutboundForCallWithCallType(true, sessionID, phoneNo,callTypeId);
							} else {
								callReportRepo.updateIsOutboundForCall(true, sessionID, phoneNo);
							}
							}
							else {
								callReportRepo.updateIsOutboundForCall(true, sessionID, phoneNo);
							}
						} else {
							if(existRecord.getCallTypeID() == null) {

								logger.info("Called Service Id: " + existRecord.getCalledServiceID());
							// Fetching callTypeId
							Integer callTypeId = getCallTypeId(false, existRecord.getCalledServiceID(),
									detailedCallReport);

							logger.info("CallTypeId after comparison: " + callTypeId);
							if (callTypeId != null) {
								callReportRepo.updateIsOutboundForCallWithCallType(false, sessionID, phoneNo, callTypeId);
							} else {
								callReportRepo.updateIsOutboundForCall(false, sessionID, phoneNo);
							}

						}
							else {
								callReportRepo.updateIsOutboundForCall(false, sessionID, phoneNo);
							}
						}
					} else {
						logger.info("Record missed in t_bencall table with sessionID : " + sessionID + " phoneNo : " + phoneNo);
						BeneficiaryCall callDetail = getCallDetail(detailedCallReport);
						callReportRepo.save(callDetail);
					}
				}
			} catch (Exception e) {
				logger.error("Error while fetching call detailed report " + e.getMessage());
			}

		}

	}
	private Integer getCallTypeId(Boolean isOutbound, Integer calledServiceId, DetailedCallReport detailedCallReport) {
		Set<Object[]> callTypesArray = new HashSet<Object[]>();

		if (detailedCallReport.getAgent_Disposition_Category() != null
				&& detailedCallReport.getAgent_Disposition() != null) {
			if (isOutbound == true) {
				callTypesArray = iEMRCalltypeRepositoryImplCustom.getOutboundCallTypes(calledServiceId, true);
			} else {
				callTypesArray = iEMRCalltypeRepositoryImplCustom.getInboundCallTypes(calledServiceId, true);
			}

			for (Object[] object : callTypesArray) {
				String callGroupType = (String) object[3];
				String callType = (String) object[0];
				String detailedCallGroupType = detailedCallReport.getAgent_Disposition_Category().replace("_", " ");
				String detailedCallType = detailedCallReport.getAgent_Disposition().replace("_", " ");
				logger.info("Detailed Call - CallGroupType: " + detailedCallGroupType);
				logger.info("Detailed Call - CallType: " + detailedCallType);

				if (callGroupType.equalsIgnoreCase(detailedCallGroupType)
						&& callType.equalsIgnoreCase(detailedCallType)) {
					return (Integer) object[1];
				}
			}
		}

		return null;

	}
	private BeneficiaryCall getCallDetail(DetailedCallReport detailedCallReport) {
		BeneficiaryCall beneficiaryCall = new BeneficiaryCall();
		beneficiaryCall.setCallID(detailedCallReport.getSession_ID());
		String phoneNo = detailedCallReport.getPHONE();
		if(null != phoneNo && phoneNo.length()>10)
			phoneNo = phoneNo.replaceFirst("^0+", "");
		beneficiaryCall.setPhoneNo(phoneNo);
		beneficiaryCall.setCalledServiceID(null);
		beneficiaryCall.setRemarks("missed records - Failure");
		beneficiaryCall.setIs1097(true);
		beneficiaryCall.setReceivedRoleName(detailedCallReport.getCampaign_Name());
		beneficiaryCall.setAgentID(String.valueOf(detailedCallReport.getAgent_ID()));

		beneficiaryCall.setCallEndTime(detailedCallReport.getCallEndTime());
		beneficiaryCall.setCallDuration(String.valueOf(detailedCallReport.getCall_Duration()));
		if(null != detailedCallReport.getOrientation_Type()
				&& detailedCallReport.getOrientation_Type().equalsIgnoreCase("OUTBOUND")) {
			beneficiaryCall.setIsOutbound(true);
		} else {
			beneficiaryCall.setIsOutbound(false);
		}
		beneficiaryCall.setCallTime(detailedCallReport.getCallStartTime());
		beneficiaryCall.setCallEndTime(detailedCallReport.getCallEndTime());
		beneficiaryCall.setCreatedBy("Admin");

		beneficiaryCall.setCallDuration(String.valueOf(detailedCallReport.getCall_Duration()));

		return beneficiaryCall;

	}
}
