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
import java.io.InputStream;

import com.iemr.common.data.report.CallDetailsReport;
import com.iemr.common.data.report.CallQualityReport;

public interface SecondaryReportService {
	ByteArrayInputStream getQualityReport(String request, String filename) throws Exception;

	ByteArrayInputStream getComplaintDetailReport(String jsonRequest,String filename) throws Exception;
	
	ByteArrayInputStream getCallSummaryReport(String jsonRequest,String filename) throws Exception;

	ByteArrayInputStream getDistrictWiseCallReport1(String jsonRequest, String filename) throws Exception;

	ByteArrayInputStream getDistrictWiseCallReport(String jsonRequest, String filename) throws Exception;
	
	ByteArrayInputStream getUnblockedUserReport(String jsonRequest,String filename) throws Exception;
	
	ByteArrayInputStream getAllBySexualOrientationReport(String jsonRequest,String filename) throws Exception;

	ByteArrayInputStream getCallQualityReport(CallQualityReport callQualityReport, String filename) throws Exception;

    ByteArrayInputStream getCountsByPrefferedLanguage(String jsonRequest, String filename) throws Exception;

	ByteArrayInputStream getAllByAgeGroup(String jsonRequest, String filename) throws Exception;
	
	ByteArrayInputStream getAllReportsByDate(String jsonRequest, String filename) throws Exception;
	
	ByteArrayInputStream getAllByGender(String jsonRequest, String filename) throws Exception;

}
