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
package com.iemr.common.service.reportSecondary;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.iemr.common.data.callhandling.CallType;
import com.iemr.common.data.report.CallDetailsReport;
import com.iemr.common.data.report.CallQualityReport;
import com.iemr.common.data.report.FeedbackReport;
import com.iemr.common.data.report.MedHistory;
import com.iemr.common.data.report.UnBlockedPhoneReport;
import com.iemr.common.model.excel.Criteria;
import com.iemr.common.model.excel.ExcelHelper;
import com.iemr.common.model.reports.ComplaintDetailReport;
import com.iemr.common.model.reports.Report1097RequestModel;
import com.iemr.common.repository.report.CRMCallReportRepo;
import com.iemr.common.secondary.repository.callreport.CallReportSecondaryRepo;
import com.iemr.common.utils.exception.IEMRException;
import com.iemr.common.utils.mapper.InputMapper;


@Profile("!swagger")
@Service
public class SecondaryReportServiceImpl implements SecondaryReportService {
	private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	@Autowired(required=false)
	public CallReportSecondaryRepo callReportRepoSecondary;
	
	@Autowired(required=false)
	private CRMCallReportRepo crmCallReportRepository;

	@Override
	public ByteArrayInputStream getQualityReport(String request,String fName) throws Exception {
		logger.debug("getQualityReport request: " + request);
		MedHistory benmedhistoryReport = InputMapper.gson().fromJson(request, MedHistory.class);
		switch (benmedhistoryReport.getReportTypeID()) {
		case 1:
			return getDSusedValidCallAtHAO(benmedhistoryReport,fName);// compare with ID

		case 2:
			return getHAHTDisconnectedCalls(benmedhistoryReport,fName);

		case 3:
			return getHAHTValidClosedCalls(benmedhistoryReport,fName);

		case 4:
			return getLAHTAlgorithmCalls(benmedhistoryReport,fName);

		case 5:
			return getLAHTTransferCallsAtMO(benmedhistoryReport,fName);

		case 6:
			return getOtherAdviceCalls(benmedhistoryReport,fName);

		case 7:
			return getRandomPickup(benmedhistoryReport,fName);

		case 8:
			return getPreviousQualityReport(benmedhistoryReport,fName);

		default:
			throw new IEMRException("Invalid Report type");
		}
	}

	public ByteArrayInputStream getRandomPickup(MedHistory benmedhistoryReport, String fName) throws IEMRException {
		String[] headers = { "SNo", "Date Of Call", "Call ID", "Agent ID", "Agent Name", "Skill Set", "Symptom",
				"Disease Summary Provided", "Call Type", "Call Duration(In seconds)", "Recording File Path" };
		ByteArrayInputStream response = null;
		try {
			Criteria c=new Criteria();
			c.setStart_Date(benmedhistoryReport.getStartDate().toString());
			c.setEnd_Date(benmedhistoryReport.getEndDate().toString());
			c.setService(fName);
			c.setAgentID(benmedhistoryReport.getAgentID());
			c.setRoleID(benmedhistoryReport.getRoleID() !=null ? benmedhistoryReport.getRoleID().toString() :"");
			List<Object[]> result = callReportRepoSecondary.getRandomCalls(benmedhistoryReport.getStartDate(),
					benmedhistoryReport.getEndDate(), "valid", "HYBRID HAO", benmedhistoryReport.getAgentID(),
					benmedhistoryReport.getProviderServiceMapID());
			if (result != null && result.size() > 0)
				response = ExcelHelper.tutorialsToExcel(headers, result,"qareport",c);
			else
				throw new IEMRException("No data found");
		} catch (IEMRException e) {
			throw new IEMRException(e.getMessage());
		}
		return response;

	}

	public ByteArrayInputStream getOtherAdviceCalls(MedHistory benmedhistoryReport, String fName) throws IEMRException {
		String[] headers = { "SNo", "Date Of Call", "Call ID", "Agent ID", "Agent Name", "Skill Set", "Symptom",
				"Disease Summary Provided", "Call Type", "Call Duration(In seconds)","Remarks", "Recording File Path" };
		ByteArrayInputStream response = null;
		try {
			Criteria c=new Criteria();
			c.setStart_Date(benmedhistoryReport.getStartDate().toString());
			c.setEnd_Date(benmedhistoryReport.getEndDate().toString());
			c.setService(fName);
			List<Object[]> result = callReportRepoSecondary.getOtherAdviceCalls(benmedhistoryReport.getStartDate(),
					benmedhistoryReport.getEndDate(), "valid", "HYBRID HAO", benmedhistoryReport.getAgentID(),
					benmedhistoryReport.getProviderServiceMapID());
			if (result != null && result.size() > 0)
				response = ExcelHelper.tutorialsToExcel(headers, result,"qareport",c);
			else
				throw new IEMRException("No data found");
		} catch (IEMRException e) {
			throw new IEMRException(e.getMessage());
		}
		return response;

	}

	public ByteArrayInputStream getLAHTTransferCallsAtMO(MedHistory benmedhistoryReport, String fName) throws IEMRException {
		String[] headers = { "SNo", "Date Of Call", "Call ID", "Agent ID", "Agent Name", "Skill Set", "Symptom",
				"Disease Summary Provided", "Call Type", "Call Duration(In seconds)", "Recording File Path" };
		ByteArrayInputStream response = null;
		try {
			Criteria c=new Criteria();
			c.setStart_Date(benmedhistoryReport.getStartDate().toString());
			c.setEnd_Date(benmedhistoryReport.getEndDate().toString());
			c.setService(fName);
			List<Object[]> result = callReportRepoSecondary.getLAHTTransferCallsToMO(
					benmedhistoryReport.getStartDate(), benmedhistoryReport.getEndDate(), "valid", "HYBRID HAO",
					benmedhistoryReport.getAgentID(), benmedhistoryReport.getProviderServiceMapID());
			if (result != null && result.size() > 0)
				response = ExcelHelper.tutorialsToExcel(headers, result,"qareport",c);
			else
				throw new IEMRException("No data found");
		} catch (IEMRException e) {
			throw new IEMRException(e.getMessage());
		}
		return response;

	}

	public ByteArrayInputStream getHAHTValidClosedCalls(MedHistory benmedhistoryReport, String fName) throws IEMRException {
		String[] headers = { "SNo", "Date Of Call", "Call ID", "Agent ID", "Agent Name", "Skill Set", "Symptom",
				"Disease Summary Provided", "Call Type", "Call Duration(In seconds)", "Recording File Path" };
		ByteArrayInputStream response = null;
		try {
			Criteria c=new Criteria();
			c.setStart_Date(benmedhistoryReport.getStartDate().toString());
			c.setEnd_Date(benmedhistoryReport.getEndDate().toString());
			c.setService(fName);
			List<Object[]> result = callReportRepoSecondary.getHAHTvalidCallsClosedAtHAO(
					benmedhistoryReport.getStartDate(), benmedhistoryReport.getEndDate(), "valid", "HYBRID HAO",
					benmedhistoryReport.getAgentID(), benmedhistoryReport.getProviderServiceMapID());
			if (result != null && result.size() > 0)
				response = ExcelHelper.tutorialsToExcel(headers, result,"qareport",c);
			else
				throw new IEMRException("No data found");
		} catch (IEMRException e) {
			throw new IEMRException(e.getMessage());
		}
		return response;

	}

	public ByteArrayInputStream getHAHTDisconnectedCalls(MedHistory benmedhistoryReport, String fName) throws IEMRException {
		String[] headers = { "SNo", "Date Of Call", "Call ID", "Agent ID", "Agent Name", "Skill Set", "Symptom",
				"Disease Summary Provided", "Call Type", "Call Duration(In seconds)", "Recording File Path" };
		ByteArrayInputStream response = null;
		try {
			Criteria c=new Criteria();
			c.setStart_Date(benmedhistoryReport.getStartDate().toString());
			c.setEnd_Date(benmedhistoryReport.getEndDate().toString());
			c.setService(fName);
			List<Object[]> result = callReportRepoSecondary.getHAHTDisconnectedCalls(
					benmedhistoryReport.getStartDate(), benmedhistoryReport.getEndDate(), "Incomplete", "HYBRID HAO",
					benmedhistoryReport.getAgentID(), benmedhistoryReport.getProviderServiceMapID());
			if (result != null && result.size() > 0)
				response = ExcelHelper.tutorialsToExcel(headers, result,"qareport",c);
			else
				throw new IEMRException("No data found");
		} catch (IEMRException e) {
			throw new IEMRException(e.getMessage());
		}
		return response;

	}

	public ByteArrayInputStream getDSusedValidCallAtHAO(MedHistory benmedhistoryReport, String fName) throws IEMRException {
		String[] headers = { "SNo", "Date Of Call", "Call ID", "Agent ID", "Agent Name", "Skill Set", "Symptom",
				"Disease Summary Provided", "Call Type", "Call Duration(In seconds)", "Recording File Path" };
		ByteArrayInputStream response = null;
		try {
			Criteria c=new Criteria();
			c.setStart_Date(benmedhistoryReport.getStartDate().toString());
			c.setEnd_Date(benmedhistoryReport.getEndDate().toString());
			c.setService(fName);
			List<Object[]> result = callReportRepoSecondary.getDSUsedValidCalls(benmedhistoryReport.getStartDate(),
					benmedhistoryReport.getEndDate(), "valid", "HYBRID HAO", benmedhistoryReport.getAgentID(),
					benmedhistoryReport.getProviderServiceMapID());
			if (result != null && result.size() > 0)
				response = ExcelHelper.tutorialsToExcel(headers, result,"qareport",c);
			else
				throw new IEMRException("No data found");
		} catch (IEMRException e) {
			throw new IEMRException(e.getMessage());
		}
		return response;

	}

	public ByteArrayInputStream getPreviousQualityReport(MedHistory benmedhistoryReport, String fName) throws Exception {

		String[] headers = { "SNo", "Date Of Call", "Caller Phone Number", "Agent ID", "Agent Name", "Skill Set",
				"Symptom", "Disease Summary Provided", "Closure Remark", "Call Type", "Call Sub Type", "Call Duration(In seconds)",
				"Recording File Path" };
		// }

		ByteArrayInputStream response = null;
		try {
			Criteria c=new Criteria();
			c.setStart_Date(benmedhistoryReport.getStartDate().toString());
			c.setEnd_Date(benmedhistoryReport.getEndDate().toString());
			c.setService(fName);
			List<Object[]> result = callReportRepoSecondary.get104QAReport(benmedhistoryReport.getStartDate(),
					benmedhistoryReport.getEndDate(), benmedhistoryReport.getRoleName(),
					benmedhistoryReport.getAgentID(), benmedhistoryReport.getProviderServiceMapID());
			if (result != null && result.size() > 0)
				response = ExcelHelper.tutorialsToExcel(headers, result,"qareport",c);
			else
				throw new IEMRException("No data found");
		} catch (IEMRException e) {
			throw new IEMRException(e.getMessage());
		}
		return response;

	}

	public ByteArrayInputStream getLAHTAlgorithmCalls(MedHistory benmedhistoryReport, String fName) throws IEMRException {
		String[] headers = { "SNo", "Date Of Call", "Call ID", "Agent ID", "Agent Name", "Skill Set", "Symptom",
				"Disease Summary Provided", "Call Type", "Call Duration(In seconds)", "Recording File Path" };
		ByteArrayInputStream response = null;
		try {
			Criteria c=new Criteria();
			c.setStart_Date(benmedhistoryReport.getStartDate().toString());
			c.setEnd_Date(benmedhistoryReport.getEndDate().toString());
			c.setService(fName);
			List<Object[]> result = callReportRepoSecondary.getLAHTAlgorithmCalls(benmedhistoryReport.getStartDate(),
					benmedhistoryReport.getEndDate(), "valid", "HYBRID HAO", benmedhistoryReport.getAgentID(),
					benmedhistoryReport.getProviderServiceMapID());
			if (result != null && result.size() > 0)
				response = ExcelHelper.tutorialsToExcel(headers, result,"qareport",c);
			else
				throw new IEMRException("No data found");

		} catch (IEMRException e) {
			throw new IEMRException(e.getMessage());
		}
		return response;

	}

	@Override
	public ByteArrayInputStream getComplaintDetailReport(String jsonRequest, String fName) throws Exception {
		String[] headers = { "SNo", "Type Of Request", "Count Of Grievance" };
		ByteArrayInputStream response = null;
		List<Object[]> result = new ArrayList<Object[]>();
		List<Object[]> resultFinal=new ArrayList<Object[]>();
		Criteria c=new Criteria();
		logger.info("CallReportsServiceImpl.getComplaintDetailReport - start");
		try {
			
			FeedbackReport[] feedbackDetails = InputMapper.gson().fromJson(jsonRequest, FeedbackReport[].class);
			List<FeedbackReport> requestList = Arrays.asList(feedbackDetails);
			Object[] obj;
			for (FeedbackReport feedback : requestList) {
				 c=new Criteria();
				c.setStart_Date(feedback.getStartDate().toString());
				c.setEnd_Date(feedback.getEndDate().toString());
				c.setService(fName);
				// report = new ComplaintDetailReport();
				obj = null;
//				report.setTypeOfRequest(feedback.getFeedbackTypeName());
				result = callReportRepoSecondary.getGrievanceDetailsReport(feedback.getStartDate(), feedback.getEndDate(),
						feedback.getFeedbackTypeName(), feedback.getProviderServiceMapID(),
						feedback.getFeedbackTypeID(), feedback.getFeedbackNatureID());
				if(result !=null && result.size() >0)
				{
					resultFinal.addAll(result);
				}
			}
		logger.info("CallReportsServiceImpl.getComplaintDetailReport - end");
		} catch (Exception e) {
			throw new IEMRException(e.getMessage());
		}
		if (result != null && result.size() > 0)
			response = ExcelHelper.tutorialsToExcel(headers, resultFinal,fName,c);
		else
			throw new IEMRException("No data found");
		return response;
	}
	@Override
	public ByteArrayInputStream getCallSummaryReport(String jsonRequest, String filename) throws Exception {

		String[] headers = { "SNo" , "Date Of Call", "Caller Phone Number", "Agent ID", "Agent Name", "Skill Set", "Call Type",
				"Call Sub Type", "Provider Service Map ID", "Closure Remark" };
		ByteArrayInputStream response = null;
		try {
			MedHistory benmedhistoryReport = InputMapper.gson().fromJson(jsonRequest, MedHistory.class);
			Criteria c = new Criteria();
			c.setStart_Date(benmedhistoryReport.getStartDate().toString());
			c.setEnd_Date(benmedhistoryReport.getEndDate().toString());
			c.setService(filename);
			c.setRoleID(benmedhistoryReport.getRoleName());
			c.setAgentID(benmedhistoryReport.getAgentID());
			c.setCallTypeID(benmedhistoryReport.getCallTypeID() != null ? benmedhistoryReport.getCallTypeID().toString():null);
			c.setCallTypeName(benmedhistoryReport.getCallTypeName());
			List<Object[]> result = callReportRepoSecondary.getCallSummaryReport(benmedhistoryReport.getStartDate(),
					benmedhistoryReport.getEndDate(), benmedhistoryReport.getRoleName(),benmedhistoryReport.getCallTypeID(),
					benmedhistoryReport.getAgentID(),
					benmedhistoryReport.getProviderServiceMapID());
			if (result != null && result.size() > 0)
				response = ExcelHelper.tutorialsToExcel(headers, result, filename,  c);
			else
				throw new IEMRException("No data found");
		} catch (IEMRException e) {
			throw new IEMRException(e.getMessage());
		}
		return response;
	}
	
	@Override
	public ByteArrayInputStream getDistrictWiseCallReport(String jsonRequest, String filename) throws Exception {
		
		String[] finalHeaders = {"Total  Calls", "Male", "Female"};
		ByteArrayInputStream response = null;
		try {
			FeedbackReport districtWiseCallReportDetails = InputMapper.gson().fromJson(jsonRequest, FeedbackReport.class);
			Criteria c = new Criteria();
			c.setStart_Date(districtWiseCallReportDetails.getStartDate().toString());
			c.setEnd_Date(districtWiseCallReportDetails.getEndDate().toString());
			
			List<CallType> callTypes = crmCallReportRepository.getCallTypeInOrder(districtWiseCallReportDetails.getProviderServiceMapID());
			String[] headers = new String[callTypes.size()+5];
			 headers[0] = "SNo";
			 headers[1] = "District";
			int i = 2;
			for (CallType callType : callTypes) {
				headers[i] = callType.getCallType(); 
				i++;
			}

			List<Object[]> result = callReportRepoSecondary.getDistrictWiseCallReport(districtWiseCallReportDetails.getStartDate(), districtWiseCallReportDetails.getEndDate(),
					districtWiseCallReportDetails.getProviderServiceMapID(), districtWiseCallReportDetails.getDistrictID() );
			
			for (int j = 0; j< finalHeaders.length; j++, i++) {
				headers[i] = finalHeaders[j]; 
			}
				if (result != null && result.size() > 0)
					response = ExcelHelper.tutorialsToExcel(headers, result, filename, c);
				else
					throw new IEMRException("No data found");
			} catch (IEMRException e) {
				throw new IEMRException(e.getMessage());
			}
			return response;
			
	}

	@Override
	public ByteArrayInputStream getDistrictWiseCallReport1(String jsonRequest, String filename) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public ByteArrayInputStream getUnblockedUserReport(String jsonRequest, String filename) throws Exception {

		String[] headers = { "SNo" , "Phone Number", "Entry Date"};
		ByteArrayInputStream response = null;
		try {
			UnBlockedPhoneReport phoneBlockList = InputMapper.gson().fromJson(jsonRequest, UnBlockedPhoneReport.class);
			Criteria c = new Criteria();
			c.setStart_Date(phoneBlockList.getBlockStartDate().toString());
			c.setEnd_Date(phoneBlockList.getBlockEndDate().toString());
			c.setService(filename);
			List<Object[]> result = callReportRepoSecondary.getUnblockedUserReport(phoneBlockList.getBlockStartDate(),
					phoneBlockList.getBlockEndDate(),phoneBlockList.getProviderServiceMapID());
			if (result != null && result.size() > 0)
				response = ExcelHelper.tutorialsToExcel(headers, result, filename,  c);
			else
				throw new IEMRException("No data found");
		} catch (IEMRException e) {
			throw new IEMRException(e.getMessage());
		}
		return response;
	}
	
	
	@Override
	public ByteArrayInputStream getAllBySexualOrientationReport(String jsonRequest, String filename) throws Exception {

		String[] headers = { "SNo" , "Sexual Orientation", "Service Provided Ratio ",  "Count"};
		ByteArrayInputStream response = null;
		try {
			Report1097RequestModel orientationList = InputMapper.gson().fromJson(jsonRequest, Report1097RequestModel.class);
			Criteria c = new Criteria();
			c.setState(orientationList.getState()!= null ? orientationList.getState() : "Any" );
			c.setDistrict(orientationList.getDistrict() != null ? orientationList.getDistrict() :"Any");
			c.setSexual_Orientation(orientationList.getBeneficiarySexualOrientation() != null ? orientationList.getBeneficiarySexualOrientation() : "All" );
			c.setStart_Date(orientationList.getStartTimestamp().toString());
			c.setEnd_Date(orientationList.getEndTimestamp().toString());
			List<Object[]> result = callReportRepoSecondary.getAllBySexualOrientationReport(orientationList.getStartTimestamp(),
					orientationList.getEndTimestamp(),orientationList.getState(), orientationList.getDistrict(),
					orientationList.getBeneficiarySexualOrientation(),orientationList.getProviderServiceMapID());
			if (result != null && result.size() > 0)
				response = ExcelHelper.tutorialsToExcel(headers, result, filename,  c);
			else
				throw new IEMRException("No data found");
		} catch (IEMRException e) {
			throw new IEMRException(e.getMessage());
		}
		return response;
	}
	
	@Override
	public ByteArrayInputStream getCallQualityReport(CallQualityReport callQualityReport, String filename) throws Exception {

		String[] callTypeWiseHeaders = {"SNo", "Call Type", "Count Of Calls"};
		String[] agentWiseReportHeaders= { "SNo", "Agent Name", "Agent ID","Count Of Calls"};
		String[] skillsetWiseReportHeaders = { "SNo", "Skill Set", "Count Of Calls" };
		String[] dateWiseReportHeaders = { "SNo", "Date", "Count Of Calls"};
		String[] locationWiseReportHeaders = {"SNo", "Center", "Count Of Calls" };

		ByteArrayInputStream response = null;List<Object[]> result=null;
		try {
			Criteria c=new Criteria();
			c.setStart_Date(callQualityReport.getStartDate().toString());
			c.setEnd_Date(callQualityReport.getEndDate().toString());
			c.setService(filename);
			
			if(callQualityReport.getSearchCriteria().equalsIgnoreCase("callTypeWise")) {
				 result = callReportRepoSecondary.getcallTypeWise(callQualityReport.getStartDate(),
						 callQualityReport.getEndDate(), callQualityReport.getCallTypeID(),
						 callQualityReport.getProviderServiceMapID());
			} else if (callQualityReport.getSearchCriteria().equalsIgnoreCase("AgentWiseReport")) {
				 result = callReportRepoSecondary.getAgentWiseReport(callQualityReport.getStartDate(),
						 callQualityReport.getEndDate(), callQualityReport.getUserID(),
						 callQualityReport.getProviderServiceMapID());
			} else if (callQualityReport.getSearchCriteria().equalsIgnoreCase("SkillsetWiseReport")) {
				 result = callReportRepoSecondary.getSkillsetWiseReport(callQualityReport.getStartDate(),
						 callQualityReport.getEndDate(), callQualityReport.getRoleID(),
						 callQualityReport.getProviderServiceMapID());
			} else if (callQualityReport.getSearchCriteria().equalsIgnoreCase("DateWiseReport")) {
				result = callReportRepoSecondary.getDateWiseReport(callQualityReport.getStartDate(),
						callQualityReport.getEndDate(),callQualityReport.getProviderServiceMapID());
			} else if (callQualityReport.getSearchCriteria().equalsIgnoreCase("LocationWiseReport")){
				result = callReportRepoSecondary.getLocationWiseReport(callQualityReport.getStartDate(),
						callQualityReport.getEndDate(),callQualityReport.getLocationID(),
						callQualityReport.getProviderServiceMapID());
			}
			
			if (result != null && result.size() > 0)
			{
				if(callQualityReport.getSearchCriteria().equalsIgnoreCase("callTypeWise"))
					response = ExcelHelper.tutorialsToExcel(callTypeWiseHeaders, result, filename, c);
				else if (callQualityReport.getSearchCriteria().equalsIgnoreCase("AgentWiseReport"))
					response = ExcelHelper.tutorialsToExcel(agentWiseReportHeaders, result, filename, c);
			    else if (callQualityReport.getSearchCriteria().equalsIgnoreCase("SkillsetWiseReport"))
			    	response = ExcelHelper.tutorialsToExcel(skillsetWiseReportHeaders, result,filename, c);
			    else if(callQualityReport.getSearchCriteria().equalsIgnoreCase("DateWiseReport"))
			    	response = ExcelHelper.tutorialsToExcel(dateWiseReportHeaders, result, filename, c);
			    else if(callQualityReport.getSearchCriteria().equalsIgnoreCase("LocationWiseReport"))
			    	response = ExcelHelper.tutorialsToExcel(locationWiseReportHeaders, result, filename, c);
			}
			else
				throw new IEMRException("No data found");
		} catch (IEMRException e) {
			throw new IEMRException(e.getMessage());
		}
		return response;
	}
	
	@Override

	public ByteArrayInputStream getCountsByPrefferedLanguage(String jsonRequest, String filename) throws Exception {

		String[] headers = { "SNo" , "Preferred Language", "Service Provided Ratio","Count"};
		ByteArrayInputStream response = null;
		try {
			Report1097RequestModel prefferedLanguageRequest = InputMapper.gson().fromJson(jsonRequest, Report1097RequestModel.class);
			Criteria c = new Criteria();
			c.setState(prefferedLanguageRequest.getState() != null ? prefferedLanguageRequest.getState()  : "Any");
			c.setDistrict(prefferedLanguageRequest.getDistrict() != null ? prefferedLanguageRequest.getDistrict() : "Any");
			c.setLanguage(prefferedLanguageRequest.getBeneficiaryPreferredLanguage() != null ? prefferedLanguageRequest.getBeneficiaryPreferredLanguage() : "All");
			c.setStart_Date(prefferedLanguageRequest.getStartTimestamp().toString());
			c.setEnd_Date(prefferedLanguageRequest.getEndTimestamp().toString());
			List<Object[]> result = callReportRepoSecondary.getLanguageDistributionReport(prefferedLanguageRequest.getStartTimestamp(),
					prefferedLanguageRequest.getEndTimestamp(),prefferedLanguageRequest.getState(),
					prefferedLanguageRequest.getDistrict(),prefferedLanguageRequest.getBeneficiaryPreferredLanguage(),
					prefferedLanguageRequest.getProviderServiceMapID());
			if (result != null && result.size() > 0)
				response = ExcelHelper.tutorialsToExcel(headers, result, filename,  c);
			else
				throw new IEMRException("No data found");
		} catch (IEMRException e) {
			throw new IEMRException(e.getMessage());
		}
		return response;
	}

	public ByteArrayInputStream getAllByAgeGroup(String jsonRequest, String filename) throws Exception {

		String[] headers = { "SNo" , "Group Name",	"Min Age",	"Max Age",	"Service Provided Ratio", "Count"};
		ByteArrayInputStream response = null;
		try {
			Report1097RequestModel allReportsByAgeGroup = InputMapper.gson().fromJson(jsonRequest, Report1097RequestModel.class);
			Criteria c = new Criteria();
			c.setStart_Date(allReportsByAgeGroup.getStartTimestamp().toString());
			c.setEnd_Date(allReportsByAgeGroup.getEndTimestamp().toString());
			c.setState(allReportsByAgeGroup.getState() != null ? allReportsByAgeGroup.getState(): "Any");
			c.setDistrict(allReportsByAgeGroup.getDistrict() != null ? allReportsByAgeGroup.getDistrict(): "Any");
			c.setCallerAgeGroup(allReportsByAgeGroup.getCallerAgeGroup() != null ? allReportsByAgeGroup.getCallerAgeGroup(): "All");
			
			List<Object[]> result = callReportRepoSecondary.getAllByAgeGroup(allReportsByAgeGroup.getStartTimestamp(),
					allReportsByAgeGroup.getEndTimestamp(),allReportsByAgeGroup.getState(), allReportsByAgeGroup.getDistrict(),
					allReportsByAgeGroup.getMinAge(), allReportsByAgeGroup.getMaxAge(), allReportsByAgeGroup.getProviderServiceMapID());
			if (result != null && result.size() > 0)
				response = ExcelHelper.tutorialsToExcel(headers, result, filename,  c);
			else
				throw new IEMRException("No data found");
		} catch (IEMRException e) {
			throw new IEMRException(e.getMessage());
		}
		return response;
	}

	public ByteArrayInputStream getAllReportsByDate(String jsonRequest, String filename) throws Exception {
		String[] headers = { "SNo" , "Agent ID", "User Role", "Call Start Time", "Call End Time",
				"Call Date", "Call Hour", "Call ID", "Phone No", "Call Type", "Call Sub Type",
				"User Name", "Preferred Language",	"Is Called Earlier", 
				"Beneficiary ID", "Title", "Dob", "Age", "Gender", "State", "District",
				"Sub District", "Occupation", "Education", "Sexual Orientation",
				"Marital Status", "Place Of Work", "Category Name", "Sub Category Name",
				"Document Name", "Counselling Category Name", "Counselling Sub Category Name",	
				"Counselling Document Name", "Feedback ID", "Feedback", "Remarks",	"Ben Created Date",	
				"Beneficiary Created By", "Username",	"Outbound Requested Language",	"Start Timestamp",	"End Timestamp",
				"Call Transfer Remarks", "Is Outbound",	"Beneficiary Call Type", "Beneficiary Call Sub Type" };
		ByteArrayInputStream response = null;
		try {
			Report1097RequestModel report1097RequestModel = InputMapper.gson().fromJson(jsonRequest, Report1097RequestModel.class);
			Criteria c = new Criteria();
			c.setStart_Date(report1097RequestModel.getStartTimestamp().toString());
			c.setEnd_Date(report1097RequestModel.getEndTimestamp().toString());
			c.setState(report1097RequestModel.getState() != null ? report1097RequestModel.getState():"Any");
			c.setDistrict(report1097RequestModel.getDistrict() != null ? report1097RequestModel.getDistrict():"Any");
			c.setLanguage(report1097RequestModel.getBeneficiaryPreferredLanguage() != null ? report1097RequestModel.getBeneficiaryPreferredLanguage():"Any");
			c.setCall_Type(report1097RequestModel.getBeneficiaryCallType()!= null ? report1097RequestModel.getBeneficiaryCallType():"Any");
			c.setCall_Sub_Type(report1097RequestModel.getBeneficiaryCallSubType() != null ? report1097RequestModel.getBeneficiaryCallSubType():"Any");
			c.setGender(report1097RequestModel.getGender() != null ? report1097RequestModel.getGender():"Any");
			c.setSexual_Orientation(report1097RequestModel.getBeneficiarySexualOrientation() != null ? report1097RequestModel.getBeneficiarySexualOrientation():"Any");
			List<Object[]> result = callReportRepoSecondary.getAllReportsByDate(report1097RequestModel.getStartTimestamp(),
					report1097RequestModel.getEndTimestamp(), report1097RequestModel.getState(),report1097RequestModel.getDistrict(), 
					report1097RequestModel.getBeneficiaryCallType(), report1097RequestModel.getBeneficiaryCallSubType(),report1097RequestModel.getBeneficiaryPreferredLanguage(), 
					report1097RequestModel.getGender(), report1097RequestModel.getBeneficiarySexualOrientation(),
					report1097RequestModel.getProviderServiceMapID());
			if (result != null && result.size() > 0)
				response = ExcelHelper.tutorialsToExcel(headers, result, filename,  c);
			else
				throw new IEMRException("No data found");
		} catch (IEMRException e) {
			throw new IEMRException(e.getMessage());
		}
		return response;
	}
	
	@Override
	public ByteArrayInputStream getAllByGender(String jsonRequest, String filename) throws Exception {

		String[] headers = { "SNo" , "Gender", "Service Provided Ratio", "Count" };
		ByteArrayInputStream response = null;
		try {
			Report1097RequestModel genderList = InputMapper.gson().fromJson(jsonRequest, Report1097RequestModel.class);
			Criteria c = new Criteria();
			c.setState(genderList.getState() != null ? genderList.getState():"Any");
			c.setDistrict(genderList.getDistrict() != null ? genderList.getDistrict():"Any");
			c.setGender(genderList.getGender() != null ? genderList.getGender():"All");
			c.setStart_Date(genderList.getStartTimestamp().toString());
			c.setEnd_Date(genderList.getEndTimestamp().toString());
			List<Object[]> result = callReportRepoSecondary.getAllByGender(genderList.getStartTimestamp(),
					genderList.getEndTimestamp(), genderList.getState(),
					genderList.getDistrict(), genderList.getGender(),
					genderList.getProviderServiceMapID());

			if (result != null && result.size() > 0)
				response = ExcelHelper.tutorialsToExcel(headers, result, filename,  c);
			else
				throw new IEMRException("No data found");
		} catch (IEMRException e) {
			throw new IEMRException(e.getMessage());
		}
		return response;
	}
}
