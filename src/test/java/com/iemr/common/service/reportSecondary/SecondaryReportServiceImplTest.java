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

import com.iemr.common.data.callhandling.CallType;
import com.iemr.common.data.report.CallQualityReport;
import com.iemr.common.data.report.FeedbackReport;
import com.iemr.common.data.report.MedHistory;
import com.iemr.common.data.report.UnBlockedPhoneReport;
import com.iemr.common.model.excel.Criteria;
import com.iemr.common.model.excel.ExcelHelper;
import com.iemr.common.model.reports.Report1097RequestModel;
import com.iemr.common.repository.report.CRMCallReportRepo;
import com.iemr.common.secondary.repository.callreport.CallReportSecondaryRepo;
import com.iemr.common.utils.exception.IEMRException;
import com.iemr.common.utils.mapper.InputMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.test.util.ReflectionTestUtils;
import java.io.ByteArrayInputStream;
import java.sql.Timestamp;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecondaryReportServiceImplTest {
    @Test
    void getOtherAdviceCalls_success_and_failure() throws Exception {
        MedHistory m = medHistory(6);
        try (MockedStatic<ExcelHelper> excelHelper = Mockito.mockStatic(ExcelHelper.class)) {
            when(callReportRepoSecondary.getOtherAdviceCalls(any(), any(), anyString(), anyString(), anyString(), anyInt())).thenReturn(Collections.singletonList(new Object[]{}));
            excelHelper.when(() -> ExcelHelper.tutorialsToExcel(any(), anyList(), anyString(), any(Criteria.class))).thenReturn(mockExcelStream);
            assertEquals(mockExcelStream, service.getOtherAdviceCalls(m, "file"));
            when(callReportRepoSecondary.getOtherAdviceCalls(any(), any(), anyString(), anyString(), anyString(), anyInt())).thenReturn(Collections.emptyList());
            IEMRException ex = assertThrows(IEMRException.class, () -> service.getOtherAdviceCalls(m, "file"));
            assertEquals("No data found", ex.getMessage());
            when(callReportRepoSecondary.getOtherAdviceCalls(any(), any(), anyString(), anyString(), anyString(), anyInt())).thenThrow(new RuntimeException("fail"));
            RuntimeException rex = assertThrows(RuntimeException.class, () -> service.getOtherAdviceCalls(m, "file"));
            assertEquals("fail", rex.getMessage());
        }
    }

    @Test
    void getLAHTTransferCallsAtMO_success_and_failure() throws Exception {
        MedHistory m = medHistory(5);
        try (MockedStatic<ExcelHelper> excelHelper = Mockito.mockStatic(ExcelHelper.class)) {
            when(callReportRepoSecondary.getLAHTTransferCallsToMO(any(), any(), anyString(), anyString(), anyString(), anyInt())).thenReturn(Collections.singletonList(new Object[]{}));
            excelHelper.when(() -> ExcelHelper.tutorialsToExcel(any(), anyList(), anyString(), any(Criteria.class))).thenReturn(mockExcelStream);
            assertEquals(mockExcelStream, service.getLAHTTransferCallsAtMO(m, "file"));
            when(callReportRepoSecondary.getLAHTTransferCallsToMO(any(), any(), anyString(), anyString(), anyString(), anyInt())).thenReturn(Collections.emptyList());
            IEMRException ex = assertThrows(IEMRException.class, () -> service.getLAHTTransferCallsAtMO(m, "file"));
            assertEquals("No data found", ex.getMessage());
            when(callReportRepoSecondary.getLAHTTransferCallsToMO(any(), any(), anyString(), anyString(), anyString(), anyInt())).thenThrow(new RuntimeException("fail"));
            RuntimeException rex = assertThrows(RuntimeException.class, () -> service.getLAHTTransferCallsAtMO(m, "file"));
            assertEquals("fail", rex.getMessage());
        }
    }

    @Test
    void getHAHTValidClosedCalls_success_and_failure() throws Exception {
        MedHistory m = medHistory(3);
        try (MockedStatic<ExcelHelper> excelHelper = Mockito.mockStatic(ExcelHelper.class)) {
            when(callReportRepoSecondary.getHAHTvalidCallsClosedAtHAO(any(), any(), anyString(), anyString(), anyString(), anyInt())).thenReturn(Collections.singletonList(new Object[]{}));
            excelHelper.when(() -> ExcelHelper.tutorialsToExcel(any(), anyList(), anyString(), any(Criteria.class))).thenReturn(mockExcelStream);
            assertEquals(mockExcelStream, service.getHAHTValidClosedCalls(m, "file"));
            when(callReportRepoSecondary.getHAHTvalidCallsClosedAtHAO(any(), any(), anyString(), anyString(), anyString(), anyInt())).thenReturn(Collections.emptyList());
            IEMRException ex = assertThrows(IEMRException.class, () -> service.getHAHTValidClosedCalls(m, "file"));
            assertEquals("No data found", ex.getMessage());
            when(callReportRepoSecondary.getHAHTvalidCallsClosedAtHAO(any(), any(), anyString(), anyString(), anyString(), anyInt())).thenThrow(new RuntimeException("fail"));
            RuntimeException rex = assertThrows(RuntimeException.class, () -> service.getHAHTValidClosedCalls(m, "file"));
            assertEquals("fail", rex.getMessage());
        }
    }

    @Test
    void getHAHTDisconnectedCalls_success_and_failure() throws Exception {
        MedHistory m = medHistory(2);
        try (MockedStatic<ExcelHelper> excelHelper = Mockito.mockStatic(ExcelHelper.class)) {
            when(callReportRepoSecondary.getHAHTDisconnectedCalls(any(), any(), anyString(), anyString(), anyString(), anyInt())).thenReturn(Collections.singletonList(new Object[]{}));
            excelHelper.when(() -> ExcelHelper.tutorialsToExcel(any(), anyList(), anyString(), any(Criteria.class))).thenReturn(mockExcelStream);
            assertEquals(mockExcelStream, service.getHAHTDisconnectedCalls(m, "file"));
            when(callReportRepoSecondary.getHAHTDisconnectedCalls(any(), any(), anyString(), anyString(), anyString(), anyInt())).thenReturn(Collections.emptyList());
            IEMRException ex = assertThrows(IEMRException.class, () -> service.getHAHTDisconnectedCalls(m, "file"));
            assertEquals("No data found", ex.getMessage());
            when(callReportRepoSecondary.getHAHTDisconnectedCalls(any(), any(), anyString(), anyString(), anyString(), anyInt())).thenThrow(new RuntimeException("fail"));
            RuntimeException rex = assertThrows(RuntimeException.class, () -> service.getHAHTDisconnectedCalls(m, "file"));
            assertEquals("fail", rex.getMessage());
        }
    }

    @Test
    void getDSusedValidCallAtHAO_success_and_failure() throws Exception {
        MedHistory m = medHistory(1);
        try (MockedStatic<ExcelHelper> excelHelper = Mockito.mockStatic(ExcelHelper.class)) {
            when(callReportRepoSecondary.getDSUsedValidCalls(any(), any(), anyString(), anyString(), anyString(), anyInt())).thenReturn(Collections.singletonList(new Object[]{}));
            excelHelper.when(() -> ExcelHelper.tutorialsToExcel(any(), anyList(), anyString(), any(Criteria.class))).thenReturn(mockExcelStream);
            assertEquals(mockExcelStream, service.getDSusedValidCallAtHAO(m, "file"));
            when(callReportRepoSecondary.getDSUsedValidCalls(any(), any(), anyString(), anyString(), anyString(), anyInt())).thenReturn(Collections.emptyList());
            IEMRException ex = assertThrows(IEMRException.class, () -> service.getDSusedValidCallAtHAO(m, "file"));
            assertEquals("No data found", ex.getMessage());
            when(callReportRepoSecondary.getDSUsedValidCalls(any(), any(), anyString(), anyString(), anyString(), anyInt())).thenThrow(new RuntimeException("fail"));
            RuntimeException rex = assertThrows(RuntimeException.class, () -> service.getDSusedValidCallAtHAO(m, "file"));
            assertEquals("fail", rex.getMessage());
        }
    }

    @Test
    void getPreviousQualityReport_success_and_failure() throws Exception {
        MedHistory m = medHistory(8);
        try (MockedStatic<ExcelHelper> excelHelper = Mockito.mockStatic(ExcelHelper.class)) {
            when(callReportRepoSecondary.get104QAReport(any(), any(), anyString(), anyString(), anyInt())).thenReturn(Collections.singletonList(new Object[]{}));
            excelHelper.when(() -> ExcelHelper.tutorialsToExcel(any(), anyList(), anyString(), any(Criteria.class))).thenReturn(mockExcelStream);
            assertEquals(mockExcelStream, service.getPreviousQualityReport(m, "file"));
            when(callReportRepoSecondary.get104QAReport(any(), any(), anyString(), anyString(), anyInt())).thenReturn(Collections.emptyList());
            IEMRException ex = assertThrows(IEMRException.class, () -> service.getPreviousQualityReport(m, "file"));
            assertEquals("No data found", ex.getMessage());
            when(callReportRepoSecondary.get104QAReport(any(), any(), anyString(), anyString(), anyInt())).thenThrow(new RuntimeException("fail"));
            RuntimeException rex = assertThrows(RuntimeException.class, () -> service.getPreviousQualityReport(m, "file"));
            assertEquals("fail", rex.getMessage());
        }
    }

    @Test
    void getLAHTAlgorithmCalls_success_and_failure() throws Exception {
        MedHistory m = medHistory(4);
        try (MockedStatic<ExcelHelper> excelHelper = Mockito.mockStatic(ExcelHelper.class)) {
            when(callReportRepoSecondary.getLAHTAlgorithmCalls(any(), any(), anyString(), anyString(), anyString(), anyInt())).thenReturn(Collections.singletonList(new Object[]{}));
            excelHelper.when(() -> ExcelHelper.tutorialsToExcel(any(), anyList(), anyString(), any(Criteria.class))).thenReturn(mockExcelStream);
            assertEquals(mockExcelStream, service.getLAHTAlgorithmCalls(m, "file"));
            when(callReportRepoSecondary.getLAHTAlgorithmCalls(any(), any(), anyString(), anyString(), anyString(), anyInt())).thenReturn(Collections.emptyList());
            IEMRException ex = assertThrows(IEMRException.class, () -> service.getLAHTAlgorithmCalls(m, "file"));
            assertEquals("No data found", ex.getMessage());
            when(callReportRepoSecondary.getLAHTAlgorithmCalls(any(), any(), anyString(), anyString(), anyString(), anyInt())).thenThrow(new RuntimeException("fail"));
            RuntimeException rex = assertThrows(RuntimeException.class, () -> service.getLAHTAlgorithmCalls(m, "file"));
            assertEquals("fail", rex.getMessage());
        }
    }
    @InjectMocks
    private SecondaryReportServiceImpl service;
    @Mock
    public CallReportSecondaryRepo callReportRepoSecondary;
    @Mock
    private CRMCallReportRepo crmCallReportRepository;
    @Mock
    private Logger logger;
    private ByteArrayInputStream mockExcelStream;
    private Timestamp startDate;
    private Timestamp endDate;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "logger", logger);
        mockExcelStream = new ByteArrayInputStream("excel_data".getBytes());
        startDate = new Timestamp(System.currentTimeMillis() - 86400000);
        endDate = new Timestamp(System.currentTimeMillis());
    }

    // Helper for MedHistory
    private MedHistory medHistory(int reportTypeId) {
        MedHistory m = new MedHistory();
        m.setStartDate(startDate);
        m.setEndDate(endDate);
        m.setAgentID("agent");
        m.setProviderServiceMapID(1);
        m.setRoleID(2L);
        m.setReportTypeID(reportTypeId);
        m.setRoleName("role");
        m.setCallTypeID(3);
        m.setCallTypeName("type");
        return m;
    }

    @Test
    void getQualityReport_allCases_success() throws Exception {
        for (int i = 1; i <= 8; i++) {
            MedHistory m = medHistory(i);
            String json = "{}";
            try (MockedStatic<InputMapper> inputMapperStatic = Mockito.mockStatic(InputMapper.class)) {
                InputMapper mockInputMapper = Mockito.mock(InputMapper.class);
                inputMapperStatic.when(InputMapper::gson).thenReturn(mockInputMapper);
                when(mockInputMapper.fromJson(anyString(), eq(MedHistory.class))).thenReturn(m);
                switch (i) {
                    case 1:
                        try (MockedStatic<ExcelHelper> excelHelper = Mockito.mockStatic(ExcelHelper.class)) {
                            when(callReportRepoSecondary.getDSUsedValidCalls(any(), any(), anyString(), anyString(), anyString(), anyInt())).thenReturn(Collections.singletonList(new Object[]{}));
                            excelHelper.when(() -> ExcelHelper.tutorialsToExcel(any(), anyList(), anyString(), any(Criteria.class))).thenReturn(mockExcelStream);
                            assertEquals(mockExcelStream, service.getQualityReport(json, "file"));
                        }
                        break;
                    case 2:
                        try (MockedStatic<ExcelHelper> excelHelper = Mockito.mockStatic(ExcelHelper.class)) {
                            when(callReportRepoSecondary.getHAHTDisconnectedCalls(any(), any(), anyString(), anyString(), anyString(), anyInt())).thenReturn(Collections.singletonList(new Object[]{}));
                            excelHelper.when(() -> ExcelHelper.tutorialsToExcel(any(), anyList(), anyString(), any(Criteria.class))).thenReturn(mockExcelStream);
                            assertEquals(mockExcelStream, service.getQualityReport(json, "file"));
                        }
                        break;
                    case 3:
                        try (MockedStatic<ExcelHelper> excelHelper = Mockito.mockStatic(ExcelHelper.class)) {
                            when(callReportRepoSecondary.getHAHTvalidCallsClosedAtHAO(any(), any(), anyString(), anyString(), anyString(), anyInt())).thenReturn(Collections.singletonList(new Object[]{}));
                            excelHelper.when(() -> ExcelHelper.tutorialsToExcel(any(), anyList(), anyString(), any(Criteria.class))).thenReturn(mockExcelStream);
                            assertEquals(mockExcelStream, service.getQualityReport(json, "file"));
                        }
                        break;
                    case 4:
                        try (MockedStatic<ExcelHelper> excelHelper = Mockito.mockStatic(ExcelHelper.class)) {
                            when(callReportRepoSecondary.getLAHTAlgorithmCalls(any(), any(), anyString(), anyString(), anyString(), anyInt())).thenReturn(Collections.singletonList(new Object[]{}));
                            excelHelper.when(() -> ExcelHelper.tutorialsToExcel(any(), anyList(), anyString(), any(Criteria.class))).thenReturn(mockExcelStream);
                            assertEquals(mockExcelStream, service.getQualityReport(json, "file"));
                        }
                        break;
                    case 5:
                        try (MockedStatic<ExcelHelper> excelHelper = Mockito.mockStatic(ExcelHelper.class)) {
                            when(callReportRepoSecondary.getLAHTTransferCallsToMO(any(), any(), anyString(), anyString(), anyString(), anyInt())).thenReturn(Collections.singletonList(new Object[]{}));
                            excelHelper.when(() -> ExcelHelper.tutorialsToExcel(any(), anyList(), anyString(), any(Criteria.class))).thenReturn(mockExcelStream);
                            assertEquals(mockExcelStream, service.getQualityReport(json, "file"));
                        }
                        break;
                    case 6:
                        try (MockedStatic<ExcelHelper> excelHelper = Mockito.mockStatic(ExcelHelper.class)) {
                            when(callReportRepoSecondary.getOtherAdviceCalls(any(), any(), anyString(), anyString(), anyString(), anyInt())).thenReturn(Collections.singletonList(new Object[]{}));
                            excelHelper.when(() -> ExcelHelper.tutorialsToExcel(any(), anyList(), anyString(), any(Criteria.class))).thenReturn(mockExcelStream);
                            assertEquals(mockExcelStream, service.getQualityReport(json, "file"));
                        }
                        break;
                    case 7:
                        try (MockedStatic<ExcelHelper> excelHelper = Mockito.mockStatic(ExcelHelper.class)) {
                            when(callReportRepoSecondary.getRandomCalls(any(), any(), anyString(), anyString(), anyString(), anyInt())).thenReturn(Collections.singletonList(new Object[]{}));
                            excelHelper.when(() -> ExcelHelper.tutorialsToExcel(any(), anyList(), anyString(), any(Criteria.class))).thenReturn(mockExcelStream);
                            assertEquals(mockExcelStream, service.getQualityReport(json, "file"));
                        }
                        break;
                    case 8:
                        try (MockedStatic<ExcelHelper> excelHelper = Mockito.mockStatic(ExcelHelper.class)) {
                            when(callReportRepoSecondary.get104QAReport(any(), any(), anyString(), anyString(), anyInt())).thenReturn(Collections.singletonList(new Object[] {}));
                            excelHelper.when(() -> ExcelHelper.tutorialsToExcel(any(), anyList(), anyString(), any(Criteria.class))).thenReturn(mockExcelStream);
                            assertEquals(mockExcelStream, service.getQualityReport(json, "file"));
                        }
                        break;
                }
            }
        }
    }

    @Test
    void getQualityReport_invalidType_throws() {
        MedHistory m = medHistory(99);
        String json = "{}";
        try (MockedStatic<InputMapper> inputMapperStatic = Mockito.mockStatic(InputMapper.class)) {
            InputMapper mockInputMapper = Mockito.mock(InputMapper.class);
            inputMapperStatic.when(InputMapper::gson).thenReturn(mockInputMapper);
            when(mockInputMapper.fromJson(anyString(), eq(MedHistory.class))).thenReturn(m);
            IEMRException ex = assertThrows(IEMRException.class, () -> service.getQualityReport(json, "file"));
            assertEquals("Invalid Report type", ex.getMessage());
        }
    }

    // For each report method, test both success and failure
    @Test
    void getRandomPickup_success_and_failure() throws Exception {
        MedHistory m = medHistory(7);
        try (MockedStatic<ExcelHelper> excelHelper = Mockito.mockStatic(ExcelHelper.class)) {
            when(callReportRepoSecondary.getRandomCalls(any(), any(), anyString(), anyString(), anyString(), anyInt())).thenReturn(Collections.singletonList(new Object[]{}));
            excelHelper.when(() -> ExcelHelper.tutorialsToExcel(any(), anyList(), anyString(), any(Criteria.class))).thenReturn(mockExcelStream);
            assertEquals(mockExcelStream, service.getRandomPickup(m, "file"));
            when(callReportRepoSecondary.getRandomCalls(any(), any(), anyString(), anyString(), anyString(), anyInt())).thenReturn(Collections.emptyList());
            IEMRException ex = assertThrows(IEMRException.class, () -> service.getRandomPickup(m, "file"));
            assertEquals("No data found", ex.getMessage());
            when(callReportRepoSecondary.getRandomCalls(any(), any(), anyString(), anyString(), anyString(), anyInt())).thenThrow(new RuntimeException("fail"));
            RuntimeException rex = assertThrows(RuntimeException.class, () -> service.getRandomPickup(m, "file"));
            assertEquals("fail", rex.getMessage());
        }
    }

    // Repeat for all other report methods (getOtherAdviceCalls, getLAHTTransferCallsAtMO, getHAHTValidClosedCalls, getHAHTDisconnectedCalls, getDSusedValidCallAtHAO, getPreviousQualityReport, getLAHTAlgorithmCalls)
    @Test
    void getComplaintDetailReport_success_and_failure() throws Exception {
        FeedbackReport feedback = mock(FeedbackReport.class);
        when(feedback.getStartDate()).thenReturn(startDate);
        when(feedback.getEndDate()).thenReturn(endDate);
        when(feedback.getFeedbackTypeName()).thenReturn("type");
        when(feedback.getProviderServiceMapID()).thenReturn(1);
        when(feedback.getFeedbackTypeID()).thenReturn(2);
        when(feedback.getFeedbackNatureID()).thenReturn(3);
        FeedbackReport[] arr = new FeedbackReport[]{feedback};
        String json = "[]";
        try (MockedStatic<InputMapper> inputMapperStatic = Mockito.mockStatic(InputMapper.class)) {
            InputMapper mockInputMapper = Mockito.mock(InputMapper.class);
            inputMapperStatic.when(InputMapper::gson).thenReturn(mockInputMapper);
            when(mockInputMapper.fromJson(anyString(), eq(FeedbackReport[].class))).thenReturn(arr);
            when(callReportRepoSecondary.getGrievanceDetailsReport(any(), any(), anyString(), anyInt(), anyInt(), anyInt())).thenReturn(Collections.singletonList(new Object[]{}));
            try (MockedStatic<ExcelHelper> excelHelper = Mockito.mockStatic(ExcelHelper.class)) {
                excelHelper.when(() -> ExcelHelper.tutorialsToExcel(any(), anyList(), anyString(), any(Criteria.class))).thenReturn(mockExcelStream);
                assertEquals(mockExcelStream, service.getComplaintDetailReport(json, "file"));
            }
            when(callReportRepoSecondary.getGrievanceDetailsReport(any(), any(), anyString(), anyInt(), anyInt(), anyInt())).thenReturn(Collections.emptyList());
            IEMRException ex = assertThrows(IEMRException.class, () -> service.getComplaintDetailReport(json, "file"));
            assertEquals("No data found", ex.getMessage());
            when(callReportRepoSecondary.getGrievanceDetailsReport(any(), any(), anyString(), anyInt(), anyInt(), anyInt())).thenThrow(new RuntimeException("fail"));
            IEMRException rex = assertThrows(IEMRException.class, () -> service.getComplaintDetailReport(json, "file"));
            assertEquals("fail", rex.getMessage());
        }
    }

    @Test
    void getCallSummaryReport_success_and_failure() throws Exception {
        MedHistory m = medHistory(1);
        String json = "{}";
        try (MockedStatic<InputMapper> inputMapperStatic = Mockito.mockStatic(InputMapper.class)) {
            InputMapper mockInputMapper = Mockito.mock(InputMapper.class);
            inputMapperStatic.when(InputMapper::gson).thenReturn(mockInputMapper);
            when(mockInputMapper.fromJson(anyString(), eq(MedHistory.class))).thenReturn(m);
            when(callReportRepoSecondary.getCallSummaryReport(any(), any(), anyString(), anyInt(), anyString(), anyInt())).thenReturn(Collections.singletonList(new Object[]{}));
            try (MockedStatic<ExcelHelper> excelHelper = Mockito.mockStatic(ExcelHelper.class)) {
                excelHelper.when(() -> ExcelHelper.tutorialsToExcel(any(), anyList(), anyString(), any(Criteria.class))).thenReturn(mockExcelStream);
                assertEquals(mockExcelStream, service.getCallSummaryReport(json, "file"));
            }
            when(callReportRepoSecondary.getCallSummaryReport(any(), any(), anyString(), anyInt(), anyString(), anyInt())).thenReturn(Collections.emptyList());
            IEMRException ex = assertThrows(IEMRException.class, () -> service.getCallSummaryReport(json, "file"));
            assertEquals("No data found", ex.getMessage());
            when(callReportRepoSecondary.getCallSummaryReport(any(), any(), anyString(), anyInt(), anyString(), anyInt())).thenThrow(new RuntimeException("fail"));
            RuntimeException rex = assertThrows(RuntimeException.class, () -> service.getCallSummaryReport(json, "file"));
            assertEquals("fail", rex.getMessage());
        }
    }

    @Test
    void getDistrictWiseCallReport_success_and_failure() throws Exception {
        FeedbackReport feedback = mock(FeedbackReport.class);
        when(feedback.getStartDate()).thenReturn(startDate);
        when(feedback.getEndDate()).thenReturn(endDate);
        when(feedback.getProviderServiceMapID()).thenReturn(1);
        when(feedback.getDistrictID()).thenReturn(2);
        String json = "{}";
        try (MockedStatic<InputMapper> inputMapperStatic = Mockito.mockStatic(InputMapper.class)) {
            InputMapper mockInputMapper = Mockito.mock(InputMapper.class);
            inputMapperStatic.when(InputMapper::gson).thenReturn(mockInputMapper);
            when(mockInputMapper.fromJson(anyString(), eq(FeedbackReport.class))).thenReturn(feedback);
            when(crmCallReportRepository.getCallTypeInOrder(anyInt())).thenReturn(Collections.singletonList(new CallType()));
            when(callReportRepoSecondary.getDistrictWiseCallReport(any(), any(), anyInt(), anyInt())).thenReturn(Collections.singletonList(new Object[]{}));
            try (MockedStatic<ExcelHelper> excelHelper = Mockito.mockStatic(ExcelHelper.class)) {
                excelHelper.when(() -> ExcelHelper.tutorialsToExcel(any(), anyList(), anyString(), any(Criteria.class))).thenReturn(mockExcelStream);
                assertEquals(mockExcelStream, service.getDistrictWiseCallReport(json, "file"));
            }
            when(callReportRepoSecondary.getDistrictWiseCallReport(any(), any(), anyInt(), anyInt())).thenReturn(Collections.emptyList());
            IEMRException ex = assertThrows(IEMRException.class, () -> service.getDistrictWiseCallReport(json, "file"));
            assertEquals("No data found", ex.getMessage());
            when(callReportRepoSecondary.getDistrictWiseCallReport(any(), any(), anyInt(), anyInt())).thenThrow(new RuntimeException("fail"));
            RuntimeException rex = assertThrows(RuntimeException.class, () -> service.getDistrictWiseCallReport(json, "file"));
            assertEquals("fail", rex.getMessage());
        }
    }

    @Test
    void getUnblockedUserReport_success_and_failure() throws Exception {
        UnBlockedPhoneReport report = mock(UnBlockedPhoneReport.class);
        when(report.getBlockStartDate()).thenReturn(startDate);
        when(report.getBlockEndDate()).thenReturn(endDate);
        when(report.getProviderServiceMapID()).thenReturn(1);
        String json = "{}";
        try (MockedStatic<InputMapper> inputMapperStatic = Mockito.mockStatic(InputMapper.class)) {
            InputMapper mockInputMapper = Mockito.mock(InputMapper.class);
            inputMapperStatic.when(InputMapper::gson).thenReturn(mockInputMapper);
            when(mockInputMapper.fromJson(anyString(), eq(UnBlockedPhoneReport.class))).thenReturn(report);
            when(callReportRepoSecondary.getUnblockedUserReport(any(), any(), anyInt())).thenReturn(Collections.singletonList(new Object[]{}));
            try (MockedStatic<ExcelHelper> excelHelper = Mockito.mockStatic(ExcelHelper.class)) {
                excelHelper.when(() -> ExcelHelper.tutorialsToExcel(any(), anyList(), anyString(), any(Criteria.class))).thenReturn(mockExcelStream);
                assertEquals(mockExcelStream, service.getUnblockedUserReport(json, "file"));
            }
            when(callReportRepoSecondary.getUnblockedUserReport(any(), any(), anyInt())).thenReturn(Collections.emptyList());
            IEMRException ex = assertThrows(IEMRException.class, () -> service.getUnblockedUserReport(json, "file"));
            assertEquals("No data found", ex.getMessage());
            when(callReportRepoSecondary.getUnblockedUserReport(any(), any(), anyInt())).thenThrow(new RuntimeException("fail"));
            RuntimeException rex = assertThrows(RuntimeException.class, () -> service.getUnblockedUserReport(json, "file"));
            assertEquals("fail", rex.getMessage());
        }
    }

    @Test
    void getAllBySexualOrientationReport_success_and_failure() throws Exception {
        Report1097RequestModel model = mock(Report1097RequestModel.class);
        when(model.getState()).thenReturn("state");
        when(model.getDistrict()).thenReturn("district");
        when(model.getBeneficiarySexualOrientation()).thenReturn("orientation");
        when(model.getStartTimestamp()).thenReturn(startDate);
        when(model.getEndTimestamp()).thenReturn(endDate);
        when(model.getProviderServiceMapID()).thenReturn(1);
        String json = "{}";
        try (MockedStatic<InputMapper> inputMapperStatic = Mockito.mockStatic(InputMapper.class)) {
            InputMapper mockInputMapper = Mockito.mock(InputMapper.class);
            inputMapperStatic.when(InputMapper::gson).thenReturn(mockInputMapper);
            when(mockInputMapper.fromJson(anyString(), eq(Report1097RequestModel.class))).thenReturn(model);
            when(callReportRepoSecondary.getAllBySexualOrientationReport(any(), any(), anyString(), anyString(), anyString(), anyInt())).thenReturn(Collections.singletonList(new Object[]{}));
            try (MockedStatic<ExcelHelper> excelHelper = Mockito.mockStatic(ExcelHelper.class)) {
                excelHelper.when(() -> ExcelHelper.tutorialsToExcel(any(), anyList(), anyString(), any(Criteria.class))).thenReturn(mockExcelStream);
                assertEquals(mockExcelStream, service.getAllBySexualOrientationReport(json, "file"));
            }
            when(callReportRepoSecondary.getAllBySexualOrientationReport(any(), any(), anyString(), anyString(), anyString(), anyInt())).thenReturn(Collections.emptyList());
            IEMRException ex = assertThrows(IEMRException.class, () -> service.getAllBySexualOrientationReport(json, "file"));
            assertEquals("No data found", ex.getMessage());
            when(callReportRepoSecondary.getAllBySexualOrientationReport(any(), any(), anyString(), anyString(), anyString(), anyInt())).thenThrow(new RuntimeException("fail"));
            RuntimeException rex = assertThrows(RuntimeException.class, () -> service.getAllBySexualOrientationReport(json, "file"));
            assertEquals("fail", rex.getMessage());
        }
    }

    @Test
    void getCallQualityReport_success_and_failure() throws Exception {
        CallQualityReport report = mock(CallQualityReport.class);
        when(report.getStartDate()).thenReturn(startDate);
        when(report.getEndDate()).thenReturn(endDate);
        when(report.getSearchCriteria()).thenReturn("callTypeWise");
        when(report.getCallTypeID()).thenReturn(1);
        when(report.getProviderServiceMapID()).thenReturn(2);
        String filename = "file";
        when(callReportRepoSecondary.getcallTypeWise(any(), any(), anyInt(), anyInt())).thenReturn(Collections.singletonList(new Object[]{}));
        try (MockedStatic<ExcelHelper> excelHelper = Mockito.mockStatic(ExcelHelper.class)) {
            excelHelper.when(() -> ExcelHelper.tutorialsToExcel(any(), anyList(), anyString(), any(Criteria.class))).thenReturn(mockExcelStream);
            assertEquals(mockExcelStream, service.getCallQualityReport(report, filename));
        }
        when(callReportRepoSecondary.getcallTypeWise(any(), any(), anyInt(), anyInt())).thenReturn(Collections.emptyList());
        IEMRException ex = assertThrows(IEMRException.class, () -> service.getCallQualityReport(report, filename));
        assertEquals("No data found", ex.getMessage());
        when(callReportRepoSecondary.getcallTypeWise(any(), any(), anyInt(), anyInt())).thenThrow(new RuntimeException("fail"));
        RuntimeException rex = assertThrows(RuntimeException.class, () -> service.getCallQualityReport(report, filename));
        assertEquals("fail", rex.getMessage());
    }

    @Test
    void getCountsByPrefferedLanguage_success_and_failure() throws Exception {
        Report1097RequestModel model = mock(Report1097RequestModel.class);
        when(model.getState()).thenReturn("state");
        when(model.getDistrict()).thenReturn("district");
        when(model.getBeneficiaryPreferredLanguage()).thenReturn("lang");
        when(model.getStartTimestamp()).thenReturn(startDate);
        when(model.getEndTimestamp()).thenReturn(endDate);
        when(model.getProviderServiceMapID()).thenReturn(1);
        String json = "{}";
        try (MockedStatic<InputMapper> inputMapperStatic = Mockito.mockStatic(InputMapper.class)) {
            InputMapper mockInputMapper = Mockito.mock(InputMapper.class);
            inputMapperStatic.when(InputMapper::gson).thenReturn(mockInputMapper);
            when(mockInputMapper.fromJson(anyString(), eq(Report1097RequestModel.class))).thenReturn(model);
            when(callReportRepoSecondary.getLanguageDistributionReport(any(), any(), anyString(), anyString(), anyString(), anyInt())).thenReturn(Collections.singletonList(new Object[]{}));
            try (MockedStatic<ExcelHelper> excelHelper = Mockito.mockStatic(ExcelHelper.class)) {
                excelHelper.when(() -> ExcelHelper.tutorialsToExcel(any(), anyList(), anyString(), any(Criteria.class))).thenReturn(mockExcelStream);
                assertEquals(mockExcelStream, service.getCountsByPrefferedLanguage(json, "file"));
            }
            when(callReportRepoSecondary.getLanguageDistributionReport(any(), any(), anyString(), anyString(), anyString(), anyInt())).thenReturn(Collections.emptyList());
            IEMRException ex = assertThrows(IEMRException.class, () -> service.getCountsByPrefferedLanguage(json, "file"));
            assertEquals("No data found", ex.getMessage());
            when(callReportRepoSecondary.getLanguageDistributionReport(any(), any(), anyString(), anyString(), anyString(), anyInt())).thenThrow(new RuntimeException("fail"));
            RuntimeException rex = assertThrows(RuntimeException.class, () -> service.getCountsByPrefferedLanguage(json, "file"));
            assertEquals("fail", rex.getMessage());
        }
    }

    @Test
    void getAllByAgeGroup_success_and_failure() throws Exception {
        Report1097RequestModel model = mock(Report1097RequestModel.class);
        when(model.getStartTimestamp()).thenReturn(startDate);
        when(model.getEndTimestamp()).thenReturn(endDate);
        when(model.getState()).thenReturn("state");
        when(model.getDistrict()).thenReturn("district");
        when(model.getCallerAgeGroup()).thenReturn("group");
        when(model.getMinAge()).thenReturn(1);
        when(model.getMaxAge()).thenReturn(99);
        when(model.getProviderServiceMapID()).thenReturn(1);
        String json = "{}";
        try (MockedStatic<InputMapper> inputMapperStatic = Mockito.mockStatic(InputMapper.class)) {
            InputMapper mockInputMapper = Mockito.mock(InputMapper.class);
            inputMapperStatic.when(InputMapper::gson).thenReturn(mockInputMapper);
            when(mockInputMapper.fromJson(anyString(), eq(Report1097RequestModel.class))).thenReturn(model);
            when(callReportRepoSecondary.getAllByAgeGroup(any(), any(), anyString(), anyString(), anyInt(), anyInt(), anyInt())).thenReturn(Collections.singletonList(new Object[]{}));
            try (MockedStatic<ExcelHelper> excelHelper = Mockito.mockStatic(ExcelHelper.class)) {
                excelHelper.when(() -> ExcelHelper.tutorialsToExcel(any(), anyList(), anyString(), any(Criteria.class))).thenReturn(mockExcelStream);
                assertEquals(mockExcelStream, service.getAllByAgeGroup(json, "file"));
            }
            when(callReportRepoSecondary.getAllByAgeGroup(any(), any(), anyString(), anyString(), anyInt(), anyInt(), anyInt())).thenReturn(Collections.emptyList());
            IEMRException ex = assertThrows(IEMRException.class, () -> service.getAllByAgeGroup(json, "file"));
            assertEquals("No data found", ex.getMessage());
            when(callReportRepoSecondary.getAllByAgeGroup(any(), any(), anyString(), anyString(), anyInt(), anyInt(), anyInt())).thenThrow(new RuntimeException("fail"));
            RuntimeException rex = assertThrows(RuntimeException.class, () -> service.getAllByAgeGroup(json, "file"));
            assertEquals("fail", rex.getMessage());
        }
    }

    @Test
    void getAllReportsByDate_success_and_failure() throws Exception {
        Report1097RequestModel model = mock(Report1097RequestModel.class);
        when(model.getStartTimestamp()).thenReturn(startDate);
        when(model.getEndTimestamp()).thenReturn(endDate);
        when(model.getState()).thenReturn("state");
        when(model.getDistrict()).thenReturn("district");
        when(model.getBeneficiaryPreferredLanguage()).thenReturn("lang");
        when(model.getBeneficiaryCallType()).thenReturn("type");
        when(model.getBeneficiaryCallSubType()).thenReturn("subtype");
        when(model.getGender()).thenReturn("gender");
        when(model.getBeneficiarySexualOrientation()).thenReturn("orientation");
        when(model.getProviderServiceMapID()).thenReturn(1);
        String json = "{}";
        try (MockedStatic<InputMapper> inputMapperStatic = Mockito.mockStatic(InputMapper.class)) {
            InputMapper mockInputMapper = Mockito.mock(InputMapper.class);
            inputMapperStatic.when(InputMapper::gson).thenReturn(mockInputMapper);
            when(mockInputMapper.fromJson(anyString(), eq(Report1097RequestModel.class))).thenReturn(model);
            when(callReportRepoSecondary.getAllReportsByDate(any(), any(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyInt())).thenReturn(Collections.singletonList(new Object[]{}));
            try (MockedStatic<ExcelHelper> excelHelper = Mockito.mockStatic(ExcelHelper.class)) {
                excelHelper.when(() -> ExcelHelper.tutorialsToExcel(any(), anyList(), anyString(), any(Criteria.class))).thenReturn(mockExcelStream);
                assertEquals(mockExcelStream, service.getAllReportsByDate(json, "file"));
            }
            when(callReportRepoSecondary.getAllReportsByDate(any(), any(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyInt())).thenReturn(Collections.emptyList());
            IEMRException ex = assertThrows(IEMRException.class, () -> service.getAllReportsByDate(json, "file"));
            assertEquals("No data found", ex.getMessage());
            when(callReportRepoSecondary.getAllReportsByDate(any(), any(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyInt())).thenThrow(new RuntimeException("fail"));
            RuntimeException rex = assertThrows(RuntimeException.class, () -> service.getAllReportsByDate(json, "file"));
            assertEquals("fail", rex.getMessage());
        }
    }

    @Test
    void getAllByGender_success_and_failure() throws Exception {
        Report1097RequestModel model = mock(Report1097RequestModel.class);
        when(model.getStartTimestamp()).thenReturn(startDate);
        when(model.getEndTimestamp()).thenReturn(endDate);
        when(model.getState()).thenReturn("state");
        when(model.getDistrict()).thenReturn("district");
        when(model.getGender()).thenReturn("gender");
        when(model.getProviderServiceMapID()).thenReturn(1);
        String json = "{}";
        try (MockedStatic<InputMapper> inputMapperStatic = Mockito.mockStatic(InputMapper.class)) {
            InputMapper mockInputMapper = Mockito.mock(InputMapper.class);
            inputMapperStatic.when(InputMapper::gson).thenReturn(mockInputMapper);
            when(mockInputMapper.fromJson(anyString(), eq(Report1097RequestModel.class))).thenReturn(model);
            when(callReportRepoSecondary.getAllByGender(any(), any(), anyString(), anyString(), anyString(), anyInt())).thenReturn(Collections.singletonList(new Object[]{}));
            try (MockedStatic<ExcelHelper> excelHelper = Mockito.mockStatic(ExcelHelper.class)) {
                excelHelper.when(() -> ExcelHelper.tutorialsToExcel(any(), anyList(), anyString(), any(Criteria.class))).thenReturn(mockExcelStream);
                assertEquals(mockExcelStream, service.getAllByGender(json, "file"));
            }
            when(callReportRepoSecondary.getAllByGender(any(), any(), anyString(), anyString(), anyString(), anyInt())).thenReturn(Collections.emptyList());
            IEMRException ex = assertThrows(IEMRException.class, () -> service.getAllByGender(json, "file"));
            assertEquals("No data found", ex.getMessage());
            when(callReportRepoSecondary.getAllByGender(any(), any(), anyString(), anyString(), anyString(), anyInt())).thenThrow(new RuntimeException("fail"));
            RuntimeException rex = assertThrows(RuntimeException.class, () -> service.getAllByGender(json, "file"));
            assertEquals("fail", rex.getMessage());
        }
    }
    // ...existing code...
}
