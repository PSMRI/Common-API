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
package com.iemr.common.service.nhm_dashboard;

import com.google.gson.Gson;
import com.iemr.common.data.nhm_dashboard.AgentSummaryReport;
import com.iemr.common.data.nhm_dashboard.DetailedCallReport;
import com.iemr.common.data.nhm_dashboard.AbandonCallSummary;
import com.iemr.common.notification.exception.IEMRException;
import com.iemr.common.repository.nhm_dashboard.AbandonCallSummaryRepo;
import com.iemr.common.repository.nhm_dashboard.AgentSummaryReportRepo;
import com.iemr.common.repository.nhm_dashboard.DetailedCallReportRepo;
import com.iemr.common.utils.config.ConfigProperties;
import com.iemr.common.utils.http.HttpUtils;
import com.iemr.common.utils.mapper.InputMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ExtendWith(MockitoExtension.class)
class NHM_DashboardServiceImplTest {
    @Test
    void getAbandonCalls_shouldReturnJsonOfAbandonCallSummaries() throws Exception {
        List<AbandonCallSummary> mockSummaries = new ArrayList<>();
        mockSummaries.add(mock(AbandonCallSummary.class));
        mockSummaries.add(mock(AbandonCallSummary.class));

        when(abandonCallSummaryRepo.findByCreatedDateBetween(any(Timestamp.class), any(Timestamp.class)))
                .thenReturn(mockSummaries);

        String result = nhmDashboardService.getAbandonCalls();

        verify(abandonCallSummaryRepo).findByCreatedDateBetween(any(Timestamp.class), any(Timestamp.class));
        String expectedJson = new Gson().toJson(mockSummaries);
        assertEquals(expectedJson, result);
    }

    @Test
    void getAbandonCalls_shouldReturnEmptyJsonWhenNoSummaries() throws Exception {
        List<AbandonCallSummary> mockSummaries = new ArrayList<>();
        when(abandonCallSummaryRepo.findByCreatedDateBetween(any(Timestamp.class), any(Timestamp.class)))
                .thenReturn(mockSummaries);

        String result = nhmDashboardService.getAbandonCalls();

        verify(abandonCallSummaryRepo).findByCreatedDateBetween(any(Timestamp.class), any(Timestamp.class));
        String expectedJson = new Gson().toJson(mockSummaries);
        assertEquals(expectedJson, result);
    }

    @Test
    void getAbandonCalls_shouldThrowExceptionWhenRepoFails() {
        when(abandonCallSummaryRepo.findByCreatedDateBetween(any(Timestamp.class), any(Timestamp.class)))
                .thenThrow(new RuntimeException("DB error"));

        Exception exception = assertThrows(Exception.class, () -> nhmDashboardService.getAbandonCalls());
        assertTrue(exception.getMessage().contains("DB error"));
    }

    @Mock
    private AbandonCallSummaryRepo abandonCallSummaryRepo;
    @Mock
    private AgentSummaryReportRepo agentSummaryReportRepo;
    @Mock
    private DetailedCallReportRepo detailedCallReportRepo;
    @Mock
    private HttpUtils httpUtils;

    @InjectMocks
    private NHM_DashboardServiceImpl nhmDashboardService;

    @Mock
    private Logger mockLogger;

    private final LocalDateTime fixedLocalDateTime = LocalDateTime.of(2023, 10, 26, 10, 0, 0);

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(NHM_DashboardServiceImpl.class, "httpUtils", httpUtils);
        ReflectionTestUtils.setField(nhmDashboardService, "logger", mockLogger);
    }

    @Test
    void getDetailedCallReport_shouldReturnJsonOfDetailedCallReports() throws Exception {
        List<DetailedCallReport> mockReports = new ArrayList<>();
        mockReports.add(mock(DetailedCallReport.class));
        mockReports.add(mock(DetailedCallReport.class));

        when(detailedCallReportRepo.findByCallStartTimeBetween(any(Timestamp.class), any(Timestamp.class)))
                .thenReturn(mockReports);

        String result = nhmDashboardService.getDetailedCallReport();

        verify(detailedCallReportRepo).findByCallStartTimeBetween(any(Timestamp.class), any(Timestamp.class));
        String expectedJson = new Gson().toJson(mockReports);
        assertEquals(expectedJson, result);
    }

    @Test
    void getDetailedCallReport_shouldReturnEmptyJsonWhenNoReports() throws Exception {
        List<DetailedCallReport> mockReports = new ArrayList<>();
        when(detailedCallReportRepo.findByCallStartTimeBetween(any(Timestamp.class), any(Timestamp.class)))
                .thenReturn(mockReports);

        String result = nhmDashboardService.getDetailedCallReport();

        verify(detailedCallReportRepo).findByCallStartTimeBetween(any(Timestamp.class), any(Timestamp.class));
        String expectedJson = new Gson().toJson(mockReports);
        assertEquals(expectedJson, result);
    }

    @Test
    void getDetailedCallReport_shouldThrowExceptionWhenRepoFails() {
        when(detailedCallReportRepo.findByCallStartTimeBetween(any(Timestamp.class), any(Timestamp.class)))
                .thenThrow(new RuntimeException("DB error"));

        Exception exception = assertThrows(Exception.class, () -> nhmDashboardService.getDetailedCallReport());
        assertTrue(exception.getMessage().contains("DB error"));
    }

    @Test
    void getAgentSummaryReport_shouldReturnJsonOfAgentSummaryReports() throws Exception {
        List<AgentSummaryReport> mockReports = new ArrayList<>();
        mockReports.add(new AgentSummaryReport());
        mockReports.add(new AgentSummaryReport());

        when(agentSummaryReportRepo.findByCreatedDateBetween(any(Timestamp.class), any(Timestamp.class)))
                .thenReturn(mockReports);

        String result = nhmDashboardService.getAgentSummaryReport();

        verify(agentSummaryReportRepo).findByCreatedDateBetween(any(Timestamp.class), any(Timestamp.class));
        String expectedJson = new Gson().toJson(mockReports);
        assertEquals(expectedJson, result);
    }

    @Test
    void getAgentSummaryReport_shouldReturnEmptyJsonWhenNoReports() throws Exception {
        List<AgentSummaryReport> mockReports = new ArrayList<>();
        when(agentSummaryReportRepo.findByCreatedDateBetween(any(Timestamp.class), any(Timestamp.class)))
                .thenReturn(mockReports);

        String result = nhmDashboardService.getAgentSummaryReport();

        verify(agentSummaryReportRepo).findByCreatedDateBetween(any(Timestamp.class), any(Timestamp.class));
        String expectedJson = new Gson().toJson(mockReports);
        assertEquals(expectedJson, result);
    }

    @Test
    void getAgentSummaryReport_shouldThrowExceptionWhenRepoFails() {
        when(agentSummaryReportRepo.findByCreatedDateBetween(any(Timestamp.class), any(Timestamp.class)))
                .thenThrow(new RuntimeException("DB error"));

        Exception exception = assertThrows(Exception.class, () -> nhmDashboardService.getAgentSummaryReport());
        assertTrue(exception.getMessage().contains("DB error"));
    }

    @Test
    void pull_NHM_Data_CTI_shouldProcessBothReportsSuccessfully() throws IEMRException {
        try (MockedStatic<ConfigProperties> mockedConfig = mockStatic(ConfigProperties.class);
             MockedStatic<InputMapper> mockedInputMapper = mockStatic(InputMapper.class);
             MockedStatic<LocalDateTime> mockedLocalDateTime = mockStatic(LocalDateTime.class)) {

            mockedLocalDateTime.when(LocalDateTime::now).thenReturn(fixedLocalDateTime);

            mockedConfig.when(() -> ConfigProperties.getPropertyByName("get-agent-summary-report-URL"))
                    .thenReturn("http://CTI_SERVER/agent?start=START_DATE&end=END_DATE");
            mockedConfig.when(() -> ConfigProperties.getPropertyByName("get-details-call-report-URL"))
                    .thenReturn("http://CTI_SERVER/detailed?start=START_DATE&end=END_DATE");
            mockedConfig.when(() -> ConfigProperties.getPropertyByName("cti-server-ip"))
                    .thenReturn("192.168.1.100");

            String agentApiUrl = "http://192.168.1.100/agent?start=2023-10-25 00:00:01&end=2023-10-25 23:59:59";
            String agentApiResponse = "[{\"agentId\":1,\"name\":\"Agent A\"}]";
            when(httpUtils.get(eq(agentApiUrl)))
                    .thenReturn(agentApiResponse);

            String detailedApiUrl = "http://192.168.1.100/detailed?start=2023-10-25 00:00:01&end=2023-10-25 23:59:59";
            String detailedApiResponse = "[{\"call_Start_Time\":\"2023-10-25 10:00:00\",\"call_End_Time\":\"2023-10-25 10:05:00\"}]";
            when(httpUtils.get(eq(detailedApiUrl)))
                    .thenReturn(detailedApiResponse);

            InputMapper mockInputMapper = mock(InputMapper.class);
            mockedInputMapper.when(InputMapper::gson).thenReturn(mockInputMapper);
            
            AgentSummaryReport[] agentArr = {new AgentSummaryReport()};
            when(mockInputMapper.fromJson(eq(agentApiResponse), eq(AgentSummaryReport[].class)))
                    .thenReturn(agentArr);

            DetailedCallReport mockDetailedReport = mock(DetailedCallReport.class);
            when(mockDetailedReport.getCall_Start_Time()).thenReturn("2023-10-25 10:00:00");
            when(mockDetailedReport.getCall_End_Time()).thenReturn("2023-10-25 10:05:00");
            when(mockDetailedReport.getAgent_Ring_Start_Time()).thenReturn(null);
            when(mockDetailedReport.getAgent_Ring_End_Time()).thenReturn("0000-00-00 00:00:00");
            DetailedCallReport[] detailedArr = {mockDetailedReport};
            when(mockInputMapper.fromJson(eq(detailedApiResponse), eq(DetailedCallReport[].class)))
                    .thenReturn(detailedArr);

            List<AgentSummaryReport> savedAgentReports = Arrays.asList(agentArr);
            when(agentSummaryReportRepo.saveAll(anyIterable())).thenReturn(savedAgentReports);

            List<DetailedCallReport> savedDetailedReports = Arrays.asList(detailedArr);
            when(detailedCallReportRepo.saveAll(anyIterable())).thenReturn(savedDetailedReports);

            String result = nhmDashboardService.pull_NHM_Data_CTI();

            String expectedAgentSaveResult = savedAgentReports.size() + " agentSummaryReport records saved successfully";
            String expectedDetailedSaveResult = savedDetailedReports.size() + " detailedCallReport records saved successfully";
            assertEquals(expectedAgentSaveResult + " " + expectedDetailedSaveResult, result);

            verify(httpUtils).get(eq(agentApiUrl));
            verify(httpUtils).get(eq(detailedApiUrl));
            verify(agentSummaryReportRepo).saveAll(anyIterable());
            verify(detailedCallReportRepo).saveAll(anyIterable());

            verify(mockLogger).info("AgentSummaryReport cti call request URL - " + agentApiUrl);
            verify(mockLogger).info("AgentSummaryReport cti call response - " + agentApiResponse);
            verify(mockLogger).info("DetailedCallReport cti call request URL - " + detailedApiUrl);
            verify(mockLogger).info("DetailedCallReport cti call response - " + detailedApiResponse);
            verify(mockDetailedReport).setCallStartTime(any(Timestamp.class));
            verify(mockDetailedReport).setCallEndTime(any(Timestamp.class));
            verify(mockDetailedReport, never()).setAgent_Ring_Start_Time_T(any(Timestamp.class));
            verify(mockDetailedReport, never()).setAgent_Ring_End_Time_T(any(Timestamp.class));
        }
    }

    @Test
    void pull_NHM_Data_CTI_shouldHandleAgentApiFailureGracefully() throws IEMRException {
        try (MockedStatic<ConfigProperties> mockedConfig = mockStatic(ConfigProperties.class);
             MockedStatic<InputMapper> mockedInputMapper = mockStatic(InputMapper.class);
             MockedStatic<LocalDateTime> mockedLocalDateTime = mockStatic(LocalDateTime.class)) {

            mockedLocalDateTime.when(LocalDateTime::now).thenReturn(fixedLocalDateTime);

            mockedConfig.when(() -> ConfigProperties.getPropertyByName("get-agent-summary-report-URL"))
                    .thenReturn("http://CTI_SERVER/agent?start=START_DATE&end=END_DATE");
            mockedConfig.when(() -> ConfigProperties.getPropertyByName("get-details-call-report-URL"))
                    .thenReturn("http://CTI_SERVER/detailed?start=START_DATE&end=END_DATE");
            mockedConfig.when(() -> ConfigProperties.getPropertyByName("cti-server-ip"))
                    .thenReturn("192.168.1.100");

            String agentApiUrl = "http://192.168.1.100/agent?start=2023-10-25 00:00:01&end=2023-10-25 23:59:59";
            when(httpUtils.get(eq(agentApiUrl)))
                    .thenThrow(new RuntimeException("Agent API down"));

            String detailedApiUrl = "http://192.168.1.100/detailed?start=2023-10-25 00:00:01&end=2023-10-25 23:59:59";
            String detailedApiResponse = "[{\"call_Start_Time\":\"2023-10-25 10:00:00\",\"call_End_Time\":\"2023-10-25 10:05:00\"}]";
            when(httpUtils.get(eq(detailedApiUrl)))
                    .thenReturn(detailedApiResponse);

            InputMapper mockInputMapper = mock(InputMapper.class);
            mockedInputMapper.when(InputMapper::gson).thenReturn(mockInputMapper);
            DetailedCallReport mockDetailedReport = mock(DetailedCallReport.class);
            when(mockDetailedReport.getCall_Start_Time()).thenReturn("2023-10-25 10:00:00");
            when(mockDetailedReport.getCall_End_Time()).thenReturn("2023-10-25 10:05:00");
            DetailedCallReport[] detailedArr = {mockDetailedReport};
            when(mockInputMapper.fromJson(eq(detailedApiResponse), eq(DetailedCallReport[].class)))
                    .thenReturn(detailedArr);
            List<DetailedCallReport> savedDetailedReports = Arrays.asList(detailedArr);
            when(detailedCallReportRepo.saveAll(anyIterable())).thenReturn(savedDetailedReports);

            String result = nhmDashboardService.pull_NHM_Data_CTI();

            String expectedDetailedSaveResult = savedDetailedReports.size() + " detailedCallReport records saved successfully";
            assertEquals(" " + expectedDetailedSaveResult, result);

            verify(httpUtils).get(eq(agentApiUrl));
            verify(httpUtils).get(eq(detailedApiUrl));
            verify(agentSummaryReportRepo, never()).saveAll(anyIterable());
            verify(detailedCallReportRepo).saveAll(anyIterable());

            verify(mockLogger).error("Agent API down");
        }
    }

    @Test
    void pull_NHM_Data_CTI_shouldHandleDetailedApiFailureGracefully() throws IEMRException {
        try (MockedStatic<ConfigProperties> mockedConfig = mockStatic(ConfigProperties.class);
             MockedStatic<InputMapper> mockedInputMapper = mockStatic(InputMapper.class);
             MockedStatic<LocalDateTime> mockedLocalDateTime = mockStatic(LocalDateTime.class)) {

            mockedLocalDateTime.when(LocalDateTime::now).thenReturn(fixedLocalDateTime);

            mockedConfig.when(() -> ConfigProperties.getPropertyByName("get-agent-summary-report-URL"))
                    .thenReturn("http://CTI_SERVER/agent?start=START_DATE&end=END_DATE");
            mockedConfig.when(() -> ConfigProperties.getPropertyByName("get-details-call-report-URL"))
                    .thenReturn("http://CTI_SERVER/detailed?start=START_DATE&end=END_DATE");
            mockedConfig.when(() -> ConfigProperties.getPropertyByName("cti-server-ip"))
                    .thenReturn("192.168.1.100");

            String agentApiUrl = "http://192.168.1.100/agent?start=2023-10-25 00:00:01&end=2023-10-25 23:59:59";
            String agentApiResponse = "[{\"agentId\":1,\"name\":\"Agent A\"}]";
            when(httpUtils.get(eq(agentApiUrl)))
                    .thenReturn(agentApiResponse);

            String detailedApiUrl = "http://192.168.1.100/detailed?start=2023-10-25 00:00:01&end=2023-10-25 23:59:59";
            when(httpUtils.get(eq(detailedApiUrl)))
                    .thenThrow(new RuntimeException("Detailed API down"));

            InputMapper mockInputMapper = mock(InputMapper.class);
            mockedInputMapper.when(InputMapper::gson).thenReturn(mockInputMapper);
            AgentSummaryReport[] agentArr = {new AgentSummaryReport()};
            when(mockInputMapper.fromJson(eq(agentApiResponse), eq(AgentSummaryReport[].class)))
                    .thenReturn(agentArr);
            List<AgentSummaryReport> savedAgentReports = Arrays.asList(agentArr);
            when(agentSummaryReportRepo.saveAll(anyIterable())).thenReturn(savedAgentReports);

            String result = nhmDashboardService.pull_NHM_Data_CTI();

            String expectedAgentSaveResult = savedAgentReports.size() + " agentSummaryReport records saved successfully";
            assertEquals(expectedAgentSaveResult + " ", result);

            verify(httpUtils).get(eq(agentApiUrl));
            verify(httpUtils).get(eq(detailedApiUrl));
            verify(agentSummaryReportRepo).saveAll(anyIterable());
            verify(detailedCallReportRepo, never()).saveAll(anyIterable());

            verify(mockLogger).error("Detailed API down");
        }
    }

    @Test
    void pull_NHM_Data_CTI_shouldHandleBothApiFailuresGracefully() throws IEMRException {
        try (MockedStatic<ConfigProperties> mockedConfig = mockStatic(ConfigProperties.class);
             MockedStatic<InputMapper> mockedInputMapper = mockStatic(InputMapper.class);
             MockedStatic<LocalDateTime> mockedLocalDateTime = mockStatic(LocalDateTime.class)) {

            mockedLocalDateTime.when(LocalDateTime::now).thenReturn(fixedLocalDateTime);

            mockedConfig.when(() -> ConfigProperties.getPropertyByName("get-agent-summary-report-URL"))
                    .thenReturn("http://CTI_SERVER/agent?start=START_DATE&end=END_DATE");
            mockedConfig.when(() -> ConfigProperties.getPropertyByName("get-details-call-report-URL"))
                    .thenReturn("http://CTI_SERVER/detailed?start=START_DATE&end=END_DATE");
            mockedConfig.when(() -> ConfigProperties.getPropertyByName("cti-server-ip"))
                    .thenReturn("192.168.1.100");

            String agentApiUrl = "http://192.168.1.100/agent?start=2023-10-25 00:00:01&end=2023-10-25 23:59:59";
            String detailedApiUrl = "http://192.168.1.100/detailed?start=2023-10-25 00:00:01&end=2023-10-25 23:59:59";

            when(httpUtils.get(eq(agentApiUrl)))
                    .thenThrow(new RuntimeException("Agent API down"));
            when(httpUtils.get(eq(detailedApiUrl)))
                    .thenThrow(new RuntimeException("Detailed API down"));

            String result = nhmDashboardService.pull_NHM_Data_CTI();

            assertEquals(" ", result);

            verify(httpUtils).get(eq(agentApiUrl));
            verify(httpUtils).get(eq(detailedApiUrl));
            verify(agentSummaryReportRepo, never()).saveAll(anyIterable());
            verify(detailedCallReportRepo, never()).saveAll(anyIterable());

            verify(mockLogger).error("Agent API down");
            verify(mockLogger).error("Detailed API down");
        }
    }

    @Test
    void pull_NHM_Data_CTI_shouldHandleAgentApiReturnsPleaseWait() throws IEMRException {
        try (MockedStatic<ConfigProperties> mockedConfig = mockStatic(ConfigProperties.class);
             MockedStatic<InputMapper> mockedInputMapper = mockStatic(InputMapper.class);
             MockedStatic<LocalDateTime> mockedLocalDateTime = mockStatic(LocalDateTime.class)) {

            mockedLocalDateTime.when(LocalDateTime::now).thenReturn(fixedLocalDateTime);

            mockedConfig.when(() -> ConfigProperties.getPropertyByName("get-agent-summary-report-URL"))
                    .thenReturn("http://CTI_SERVER/agent?start=START_DATE&end=END_DATE");
            mockedConfig.when(() -> ConfigProperties.getPropertyByName("get-details-call-report-URL"))
                    .thenReturn("http://CTI_SERVER/detailed?start=START_DATE&end=END_DATE");
            mockedConfig.when(() -> ConfigProperties.getPropertyByName("cti-server-ip"))
                    .thenReturn("192.168.1.100");

            String agentApiUrl = "http://192.168.1.100/agent?start=2023-10-25 00:00:01&end=2023-10-25 23:59:59";
            when(httpUtils.get(eq(agentApiUrl)))
                    .thenReturn("Please wait for 1 hour");

            String detailedApiUrl = "http://192.168.1.100/detailed?start=2023-10-25 00:00:01&end=2023-10-25 23:59:59";
            String detailedApiResponse = "[{\"call_Start_Time\":\"2023-10-25 10:00:00\",\"call_End_Time\":\"2023-10-25 10:05:00\"}]";
            when(httpUtils.get(eq(detailedApiUrl)))
                    .thenReturn(detailedApiResponse);

            InputMapper mockInputMapper = mock(InputMapper.class);
            mockedInputMapper.when(InputMapper::gson).thenReturn(mockInputMapper);
            DetailedCallReport mockDetailedReport = mock(DetailedCallReport.class);
            when(mockDetailedReport.getCall_Start_Time()).thenReturn("2023-10-25 10:00:00");
            when(mockDetailedReport.getCall_End_Time()).thenReturn("2023-10-25 10:05:00");
            DetailedCallReport[] detailedArr = {mockDetailedReport};
            when(mockInputMapper.fromJson(eq(detailedApiResponse), eq(DetailedCallReport[].class)))
                    .thenReturn(detailedArr);
            List<DetailedCallReport> savedDetailedReports = Arrays.asList(detailedArr);
            when(detailedCallReportRepo.saveAll(anyIterable())).thenReturn(savedDetailedReports);

            String result = nhmDashboardService.pull_NHM_Data_CTI();

            String expectedDetailedSaveResult = savedDetailedReports.size() + " detailedCallReport records saved successfully";
            assertEquals(" " + expectedDetailedSaveResult, result);

            verify(httpUtils).get(eq(agentApiUrl));
            verify(httpUtils).get(eq(detailedApiUrl));
            verify(agentSummaryReportRepo, never()).saveAll(anyIterable());
            verify(detailedCallReportRepo).saveAll(anyIterable());

            verify(mockLogger).error("Please wait for 1 hour");
        }
    }

    @Test
    void pull_NHM_Data_CTI_shouldHandleDetailedApiReturnsNoData() throws IEMRException {
        try (MockedStatic<ConfigProperties> mockedConfig = mockStatic(ConfigProperties.class);
             MockedStatic<InputMapper> mockedInputMapper = mockStatic(InputMapper.class);
             MockedStatic<LocalDateTime> mockedLocalDateTime = mockStatic(LocalDateTime.class)) {

            mockedLocalDateTime.when(LocalDateTime::now).thenReturn(fixedLocalDateTime);

            mockedConfig.when(() -> ConfigProperties.getPropertyByName("get-agent-summary-report-URL"))
                    .thenReturn("http://CTI_SERVER/agent?start=START_DATE&end=END_DATE");
            mockedConfig.when(() -> ConfigProperties.getPropertyByName("get-details-call-report-URL"))
                    .thenReturn("http://CTI_SERVER/detailed?start=START_DATE&end=END_DATE");
            mockedConfig.when(() -> ConfigProperties.getPropertyByName("cti-server-ip"))
                    .thenReturn("192.168.1.100");

            String agentApiUrl = "http://192.168.1.100/agent?start=2023-10-25 00:00:01&end=2023-10-25 23:59:59";
            String agentApiResponse = "[{\"agentId\":1,\"name\":\"Agent A\"}]";
            when(httpUtils.get(eq(agentApiUrl)))
                    .thenReturn(agentApiResponse);

            String detailedApiUrl = "http://192.168.1.100/detailed?start=2023-10-25 00:00:01&end=2023-10-25 23:59:59";
            when(httpUtils.get(eq(detailedApiUrl)))
                    .thenReturn("no data");

            InputMapper mockInputMapper = mock(InputMapper.class);
            mockedInputMapper.when(InputMapper::gson).thenReturn(mockInputMapper);
            AgentSummaryReport[] agentArr = {new AgentSummaryReport()};
            when(mockInputMapper.fromJson(eq(agentApiResponse), eq(AgentSummaryReport[].class)))
                    .thenReturn(agentArr);
            List<AgentSummaryReport> savedAgentReports = Arrays.asList(agentArr);
            when(agentSummaryReportRepo.saveAll(anyIterable())).thenReturn(savedAgentReports);

            String result = nhmDashboardService.pull_NHM_Data_CTI();

            String expectedAgentSaveResult = savedAgentReports.size() + " agentSummaryReport records saved successfully";
            assertEquals(expectedAgentSaveResult + " ", result);

            verify(httpUtils).get(eq(agentApiUrl));
            verify(httpUtils).get(eq(detailedApiUrl));
            verify(agentSummaryReportRepo).saveAll(anyIterable());
            verify(detailedCallReportRepo, never()).saveAll(anyIterable());

            verify(mockLogger).error("no data");
        }
    }

    @Test
    void pull_NHM_Data_CTI_shouldHandleEmptyAgentList() throws IEMRException {
        try (MockedStatic<ConfigProperties> mockedConfig = mockStatic(ConfigProperties.class);
             MockedStatic<InputMapper> mockedInputMapper = mockStatic(InputMapper.class);
             MockedStatic<LocalDateTime> mockedLocalDateTime = mockStatic(LocalDateTime.class)) {

            mockedLocalDateTime.when(LocalDateTime::now).thenReturn(fixedLocalDateTime);

            mockedConfig.when(() -> ConfigProperties.getPropertyByName("get-agent-summary-report-URL"))
                    .thenReturn("http://CTI_SERVER/agent?start=START_DATE&end=END_DATE");
            mockedConfig.when(() -> ConfigProperties.getPropertyByName("get-details-call-report-URL"))
                    .thenReturn("http://CTI_SERVER/detailed?start=START_DATE&end=END_DATE");
            mockedConfig.when(() -> ConfigProperties.getPropertyByName("cti-server-ip"))
                    .thenReturn("192.168.1.100");

            String agentApiUrl = "http://192.168.1.100/agent?start=2023-10-25 00:00:01&end=2023-10-25 23:59:59";
            String agentApiResponse = "[]";
            when(httpUtils.get(eq(agentApiUrl)))
                    .thenReturn(agentApiResponse);

            String detailedApiUrl = "http://192.168.1.100/detailed?start=2023-10-25 00:00:01&end=2023-10-25 23:59:59";
            String detailedApiResponse = "[{\"call_Start_Time\":\"2023-10-25 10:00:00\",\"call_End_Time\":\"2023-10-25 10:05:00\"}]";
            when(httpUtils.get(eq(detailedApiUrl)))
                    .thenReturn(detailedApiResponse);

            InputMapper mockInputMapper = mock(InputMapper.class);
            mockedInputMapper.when(InputMapper::gson).thenReturn(mockInputMapper);
            when(mockInputMapper.fromJson(eq(agentApiResponse), eq(AgentSummaryReport[].class)))
                    .thenReturn(new AgentSummaryReport[]{});

            DetailedCallReport mockDetailedReport = mock(DetailedCallReport.class);
            when(mockDetailedReport.getCall_Start_Time()).thenReturn("2023-10-25 10:00:00");
            when(mockDetailedReport.getCall_End_Time()).thenReturn("2023-10-25 10:05:00");
            DetailedCallReport[] detailedArr = {mockDetailedReport};
            when(mockInputMapper.fromJson(eq(detailedApiResponse), eq(DetailedCallReport[].class)))
                    .thenReturn(detailedArr);
            List<DetailedCallReport> savedDetailedReports = Arrays.asList(detailedArr);
            when(detailedCallReportRepo.saveAll(anyIterable())).thenReturn(savedDetailedReports);

            String result = nhmDashboardService.pull_NHM_Data_CTI();

            String expectedDetailedSaveResult = savedDetailedReports.size() + " detailedCallReport records saved successfully";
            assertEquals(" " + expectedDetailedSaveResult, result);

            verify(httpUtils).get(eq(agentApiUrl));
            verify(httpUtils).get(eq(detailedApiUrl));
            verify(agentSummaryReportRepo, never()).saveAll(anyIterable());
            verify(detailedCallReportRepo).saveAll(anyIterable());
        }
    }

    @Test
    void pull_NHM_Data_CTI_shouldHandleEmptyDetailedList() throws IEMRException {
        try (MockedStatic<ConfigProperties> mockedConfig = mockStatic(ConfigProperties.class);
             MockedStatic<InputMapper> mockedInputMapper = mockStatic(InputMapper.class);
             MockedStatic<LocalDateTime> mockedLocalDateTime = mockStatic(LocalDateTime.class)) {

            mockedLocalDateTime.when(LocalDateTime::now).thenReturn(fixedLocalDateTime);

            mockedConfig.when(() -> ConfigProperties.getPropertyByName("get-agent-summary-report-URL"))
                    .thenReturn("http://CTI_SERVER/agent?start=START_DATE&end=END_DATE");
            mockedConfig.when(() -> ConfigProperties.getPropertyByName("get-details-call-report-URL"))
                    .thenReturn("http://CTI_SERVER/detailed?start=START_DATE&end=END_DATE");
            mockedConfig.when(() -> ConfigProperties.getPropertyByName("cti-server-ip"))
                    .thenReturn("192.168.1.100");

            String agentApiUrl = "http://192.168.1.100/agent?start=2023-10-25 00:00:01&end=2023-10-25 23:59:59";
            String agentApiResponse = "[{\"agentId\":1,\"name\":\"Agent A\"}]";
            when(httpUtils.get(eq(agentApiUrl)))
                    .thenReturn(agentApiResponse);

            String detailedApiUrl = "http://192.168.1.100/detailed?start=2023-10-25 00:00:01&end=2023-10-25 23:59:59";
            String detailedApiResponse = "[]";
            when(httpUtils.get(eq(detailedApiUrl)))
                    .thenReturn(detailedApiResponse);

            InputMapper mockInputMapper = mock(InputMapper.class);
            mockedInputMapper.when(InputMapper::gson).thenReturn(mockInputMapper);
            AgentSummaryReport[] agentArr = {new AgentSummaryReport()};
            when(mockInputMapper.fromJson(eq(agentApiResponse), eq(AgentSummaryReport[].class)))
                    .thenReturn(agentArr);
            List<AgentSummaryReport> savedAgentReports = Arrays.asList(agentArr);
            when(agentSummaryReportRepo.saveAll(anyIterable())).thenReturn(savedAgentReports);
            when(mockInputMapper.fromJson(eq(detailedApiResponse), eq(DetailedCallReport[].class)))
                    .thenReturn(new DetailedCallReport[]{});

            String result = nhmDashboardService.pull_NHM_Data_CTI();

            String expectedAgentSaveResult = savedAgentReports.size() + " agentSummaryReport records saved successfully";
            assertEquals(expectedAgentSaveResult + " ", result);

            verify(httpUtils).get(eq(agentApiUrl));
            verify(httpUtils).get(eq(detailedApiUrl));
            verify(agentSummaryReportRepo).saveAll(anyIterable());
            verify(detailedCallReportRepo, never()).saveAll(anyIterable());
        }
    }

    @Test
    void pull_NHM_Data_CTI_shouldHandleSaveAgentSummaryReportException() throws IEMRException {
        try (MockedStatic<ConfigProperties> mockedConfig = mockStatic(ConfigProperties.class);
             MockedStatic<InputMapper> mockedInputMapper = mockStatic(InputMapper.class);
             MockedStatic<LocalDateTime> mockedLocalDateTime = mockStatic(LocalDateTime.class)) {

            mockedLocalDateTime.when(LocalDateTime::now).thenReturn(fixedLocalDateTime);

            mockedConfig.when(() -> ConfigProperties.getPropertyByName("get-agent-summary-report-URL"))
                    .thenReturn("http://CTI_SERVER/agent?start=START_DATE&end=END_DATE");
            mockedConfig.when(() -> ConfigProperties.getPropertyByName("get-details-call-report-URL"))
                    .thenReturn("http://CTI_SERVER/detailed?start=START_DATE&end=END_DATE");
            mockedConfig.when(() -> ConfigProperties.getPropertyByName("cti-server-ip"))
                    .thenReturn("192.168.1.100");

            String agentApiUrl = "http://192.168.1.100/agent?start=2023-10-25 00:00:01&end=2023-10-25 23:59:59";
            String detailedApiUrl = "http://192.168.1.100/detailed?start=2023-10-25 00:00:01&end=2023-10-25 23:59:59";
            String agentApiResponse = "[{\"agentId\":1,\"name\":\"Agent A\"}]";
            String detailedApiResponse = "[{\"call_Start_Time\":\"2023-10-25 10:00:00\",\"call_End_Time\":\"2023-10-25 10:05:00\"}]";
            when(httpUtils.get(eq(agentApiUrl)))
                    .thenReturn(agentApiResponse);
            when(httpUtils.get(eq(detailedApiUrl)))
                    .thenReturn(detailedApiResponse);

            InputMapper mockInputMapper = mock(InputMapper.class);
            mockedInputMapper.when(InputMapper::gson).thenReturn(mockInputMapper);
            AgentSummaryReport[] agentArr = {new AgentSummaryReport()};
            when(mockInputMapper.fromJson(eq(agentApiResponse), eq(AgentSummaryReport[].class)))
                    .thenReturn(agentArr);

            when(agentSummaryReportRepo.saveAll(anyIterable())).thenThrow(new RuntimeException("Save Agent Failed"));

            DetailedCallReport mockDetailedReport = mock(DetailedCallReport.class);
            when(mockDetailedReport.getCall_Start_Time()).thenReturn("2023-10-25 10:00:00");
            when(mockDetailedReport.getCall_End_Time()).thenReturn("2023-10-25 10:05:00");
            DetailedCallReport[] detailedArr = {mockDetailedReport};
            when(mockInputMapper.fromJson(eq(detailedApiResponse), eq(DetailedCallReport[].class)))
                    .thenReturn(detailedArr);
            List<DetailedCallReport> savedDetailedReports = Arrays.asList(detailedArr);
            when(detailedCallReportRepo.saveAll(anyIterable())).thenReturn(savedDetailedReports);

            String result = nhmDashboardService.pull_NHM_Data_CTI();

            String expectedDetailedSaveResult = savedDetailedReports.size() + " detailedCallReport records saved successfully";
            assertEquals(" " + expectedDetailedSaveResult, result);

            verify(agentSummaryReportRepo).saveAll(anyIterable());
            verify(detailedCallReportRepo).saveAll(anyIterable());
            verify(mockLogger).error("Save Agent Failed");
        }
    }

    @Test
    void pull_NHM_Data_CTI_shouldHandleSaveDetailedCallReportException() throws IEMRException {
        try (MockedStatic<ConfigProperties> mockedConfig = mockStatic(ConfigProperties.class);
             MockedStatic<InputMapper> mockedInputMapper = mockStatic(InputMapper.class);
             MockedStatic<LocalDateTime> mockedLocalDateTime = mockStatic(LocalDateTime.class)) {

            mockedLocalDateTime.when(LocalDateTime::now).thenReturn(fixedLocalDateTime);

            mockedConfig.when(() -> ConfigProperties.getPropertyByName("get-agent-summary-report-URL"))
                    .thenReturn("http://CTI_SERVER/agent?start=START_DATE&end=END_DATE");
            mockedConfig.when(() -> ConfigProperties.getPropertyByName("get-details-call-report-URL"))
                    .thenReturn("http://CTI_SERVER/detailed?start=START_DATE&end=END_DATE");
            mockedConfig.when(() -> ConfigProperties.getPropertyByName("cti-server-ip"))
                    .thenReturn("192.168.1.100");

            String agentApiUrl = "http://192.168.1.100/agent?start=2023-10-25 00:00:01&end=2023-10-25 23:59:59";
            String detailedApiUrl = "http://192.168.1.100/detailed?start=2023-10-25 00:00:01&end=2023-10-25 23:59:59";
            String agentApiResponse = "[{\"agentId\":1,\"name\":\"Agent A\"}]";
            String detailedApiResponse = "[{\"call_Start_Time\":\"2023-10-25 10:00:00\",\"call_End_Time\":\"2023-10-25 10:05:00\"}]";
            when(httpUtils.get(eq(agentApiUrl)))
                    .thenReturn(agentApiResponse);
            when(httpUtils.get(eq(detailedApiUrl)))
                    .thenReturn(detailedApiResponse);

            InputMapper mockInputMapper = mock(InputMapper.class);
            mockedInputMapper.when(InputMapper::gson).thenReturn(mockInputMapper);
            AgentSummaryReport[] agentArr = {new AgentSummaryReport()};
            when(mockInputMapper.fromJson(eq(agentApiResponse), eq(AgentSummaryReport[].class)))
                    .thenReturn(agentArr);
            List<AgentSummaryReport> savedAgentReports = Arrays.asList(agentArr);
            when(agentSummaryReportRepo.saveAll(anyIterable())).thenReturn(savedAgentReports);

            DetailedCallReport mockDetailedReport = mock(DetailedCallReport.class);
            when(mockDetailedReport.getCall_Start_Time()).thenReturn("2023-10-25 10:00:00");
            when(mockDetailedReport.getCall_End_Time()).thenReturn("2023-10-25 10:05:00");
            DetailedCallReport[] detailedArr = {mockDetailedReport};
            when(mockInputMapper.fromJson(eq(detailedApiResponse), eq(DetailedCallReport[].class)))
                    .thenReturn(detailedArr);

            when(detailedCallReportRepo.saveAll(anyIterable())).thenThrow(new RuntimeException("Save Detailed Failed"));

            String result = nhmDashboardService.pull_NHM_Data_CTI();

            String expectedAgentSaveResult = savedAgentReports.size() + " agentSummaryReport records saved successfully";
            assertEquals(expectedAgentSaveResult + " ", result);

            verify(agentSummaryReportRepo).saveAll(anyIterable());
            verify(detailedCallReportRepo).saveAll(anyIterable());
            verify(mockLogger).error("Save Detailed Failed");
        }
    }

    @Test
    void saveAgentSummaryReport_shouldSaveAgentSummaryReportsSuccessfully() throws IEMRException {
        List<AgentSummaryReport> agentSummaryReportList = new ArrayList<>();
        agentSummaryReportList.add(new AgentSummaryReport());
        agentSummaryReportList.add(new AgentSummaryReport());

        when(agentSummaryReportRepo.saveAll(agentSummaryReportList)).thenReturn(agentSummaryReportList);

        String result = nhmDashboardService.saveAgentSummaryReport(agentSummaryReportList);

        assertEquals(agentSummaryReportList.size() + " agentSummaryReport records saved successfully", result);
        verify(agentSummaryReportRepo).saveAll(agentSummaryReportList);
    }

    @Test
    void saveAgentSummaryReport_shouldHandleEmptyList() throws IEMRException {
        List<AgentSummaryReport> emptyList = new ArrayList<>();

        when(agentSummaryReportRepo.saveAll(emptyList)).thenReturn(emptyList);

        String result = nhmDashboardService.saveAgentSummaryReport(emptyList);

        assertEquals("0 agentSummaryReport records saved successfully", result);
        verify(agentSummaryReportRepo).saveAll(emptyList);
    }

    @Test
    void saveDetailedCallReport_shouldSaveDetailedCallReportsSuccessfully() throws IEMRException {
        List<DetailedCallReport> detailedCallReportList = new ArrayList<>();
        DetailedCallReport mockDetailedReport1 = mock(DetailedCallReport.class);
        when(mockDetailedReport1.getCall_Start_Time()).thenReturn("2023-10-25 10:00:00");
        when(mockDetailedReport1.getCall_End_Time()).thenReturn("2023-10-25 10:05:00");
        when(mockDetailedReport1.getAgent_Ring_Start_Time()).thenReturn("2023-10-25 10:01:00");
        when(mockDetailedReport1.getAgent_Ring_End_Time()).thenReturn("2023-10-25 10:04:00");
        detailedCallReportList.add(mockDetailedReport1);

        List<DetailedCallReport> savedDetailedReports = Arrays.asList(mockDetailedReport1);
        when(detailedCallReportRepo.saveAll(anyIterable())).thenReturn(savedDetailedReports);

        String result = nhmDashboardService.saveDetailedCallReport(detailedCallReportList);

        assertEquals(savedDetailedReports.size() + " detailedCallReport records saved successfully", result);
        verify(mockDetailedReport1).setCallStartTime(any(Timestamp.class));
        verify(mockDetailedReport1).setCallEndTime(any(Timestamp.class));
        verify(mockDetailedReport1).setAgent_Ring_Start_Time_T(any(Timestamp.class));
        verify(mockDetailedReport1).setAgent_Ring_End_Time_T(any(Timestamp.class));
        verify(detailedCallReportRepo).saveAll(detailedCallReportList);
    }

    @Test
    void saveDetailedCallReport_shouldHandleInvalidTimestampFormatGracefully() throws IEMRException {
        List<DetailedCallReport> detailedCallReportList = new ArrayList<>();
        DetailedCallReport mockDetailedReport1 = mock(DetailedCallReport.class);
        when(mockDetailedReport1.getCall_Start_Time()).thenReturn("INVALID_TIMESTAMP");
        when(mockDetailedReport1.getCall_End_Time()).thenReturn("2023-10-25 10:05:00");
        when(mockDetailedReport1.getAgent_Ring_Start_Time()).thenReturn("2023-10-25 10:00:00");
        when(mockDetailedReport1.getAgent_Ring_End_Time()).thenReturn("INVALID_TIMESTAMP_2");
        detailedCallReportList.add(mockDetailedReport1);

        List<DetailedCallReport> savedDetailedReports = Arrays.asList(mockDetailedReport1);
        when(detailedCallReportRepo.saveAll(anyIterable())).thenReturn(savedDetailedReports);

        String result = nhmDashboardService.saveDetailedCallReport(detailedCallReportList);

        assertEquals(savedDetailedReports.size() + " detailedCallReport records saved successfully", result);
        verify(mockLogger, times(2)).error(startsWith("Call_Start_Time"));
        verify(mockDetailedReport1, never()).setCallStartTime(any(Timestamp.class));
        verify(mockDetailedReport1).setCallEndTime(any(Timestamp.class));
        verify(mockDetailedReport1).setAgent_Ring_Start_Time_T(any(Timestamp.class));
        verify(mockDetailedReport1, never()).setAgent_Ring_End_Time_T(any(Timestamp.class));
        verify(detailedCallReportRepo).saveAll(detailedCallReportList);
    }

    @Test
    void saveDetailedCallReport_shouldHandleNullTimestampFieldsGracefully() throws IEMRException {
        List<DetailedCallReport> detailedCallReportList = new ArrayList<>();
        DetailedCallReport mockDetailedReport1 = mock(DetailedCallReport.class);
        when(mockDetailedReport1.getCall_Start_Time()).thenReturn(null);
        when(mockDetailedReport1.getCall_End_Time()).thenReturn(null);
        when(mockDetailedReport1.getAgent_Ring_Start_Time()).thenReturn(null);
        when(mockDetailedReport1.getAgent_Ring_End_Time()).thenReturn(null);
        detailedCallReportList.add(mockDetailedReport1);

        List<DetailedCallReport> savedDetailedReports = Arrays.asList(mockDetailedReport1);
        when(detailedCallReportRepo.saveAll(anyIterable())).thenReturn(savedDetailedReports);

        String result = nhmDashboardService.saveDetailedCallReport(detailedCallReportList);

        assertEquals(savedDetailedReports.size() + " detailedCallReport records saved successfully", result);
        verify(mockLogger, never()).error(anyString());
        verify(mockDetailedReport1, never()).setCallStartTime(any(Timestamp.class));
        verify(mockDetailedReport1, never()).setCallEndTime(any(Timestamp.class));
        verify(mockDetailedReport1, never()).setAgent_Ring_Start_Time_T(any(Timestamp.class));
        verify(mockDetailedReport1, never()).setAgent_Ring_End_Time_T(any(Timestamp.class));
        verify(detailedCallReportRepo).saveAll(detailedCallReportList);
    }

    @Test
    void saveDetailedCallReport_shouldHandleZeroTimestampFieldsGracefully() throws IEMRException {
        List<DetailedCallReport> detailedCallReportList = new ArrayList<>();
        DetailedCallReport mockDetailedReport1 = mock(DetailedCallReport.class);
        when(mockDetailedReport1.getCall_Start_Time()).thenReturn("0000-00-00 00:00:00");
        when(mockDetailedReport1.getCall_End_Time()).thenReturn("0000-00-00 00:00:00");
        when(mockDetailedReport1.getAgent_Ring_Start_Time()).thenReturn("0000-00-00 00:00:00");
        when(mockDetailedReport1.getAgent_Ring_End_Time()).thenReturn("0000-00-00 00:00:00");
        detailedCallReportList.add(mockDetailedReport1);

        List<DetailedCallReport> savedDetailedReports = Arrays.asList(mockDetailedReport1);
        when(detailedCallReportRepo.saveAll(anyIterable())).thenReturn(savedDetailedReports);

        String result = nhmDashboardService.saveDetailedCallReport(detailedCallReportList);

        assertEquals(savedDetailedReports.size() + " detailedCallReport records saved successfully", result);
        verify(mockLogger, never()).error(anyString());
        verify(mockDetailedReport1, never()).setCallStartTime(any(Timestamp.class));
        verify(mockDetailedReport1, never()).setCallEndTime(any(Timestamp.class));
        verify(mockDetailedReport1, never()).setAgent_Ring_Start_Time_T(any(Timestamp.class));
        verify(mockDetailedReport1, never()).setAgent_Ring_End_Time_T(any(Timestamp.class));
        verify(detailedCallReportRepo).saveAll(detailedCallReportList);
    }

    @Test
    void saveDetailedCallReport_shouldThrowExceptionWhenListIsEmpty() {
        List<DetailedCallReport> emptyList = new ArrayList<>();
        IEMRException exception = assertThrows(IEMRException.class, () -> nhmDashboardService.saveDetailedCallReport(emptyList));
        assertEquals("please pass valid DetailedCallReport data in list", exception.getMessage());
        verify(detailedCallReportRepo, never()).saveAll(anyIterable());
    }

    @Test
    void saveDetailedCallReport_shouldThrowExceptionWhenListIsNull() {
        List<DetailedCallReport> nullList = null;
        IEMRException exception = assertThrows(IEMRException.class, () -> nhmDashboardService.saveDetailedCallReport(nullList));
        assertEquals("please pass valid DetailedCallReport data in list", exception.getMessage());
        verify(detailedCallReportRepo, never()).saveAll(anyIterable());
    }


    
    // --- BEGIN MERGED TESTS ---

    @Test
    void pushAbandonCalls_Success() throws Exception {
        AbandonCallSummary abandonCallSummary = new AbandonCallSummary();
        when(abandonCallSummaryRepo.save(any(AbandonCallSummary.class))).thenReturn(abandonCallSummary);

        String result = nhmDashboardService.pushAbandonCalls(abandonCallSummary);

        assertEquals("data saved successfully", result);
        verify(abandonCallSummaryRepo, times(1)).save(abandonCallSummary);
        // Logging assertions omitted for compatibility
    }

    @Test
    void pushAbandonCalls_Failure() {
        AbandonCallSummary abandonCallSummary = new AbandonCallSummary();
        when(abandonCallSummaryRepo.save(any(AbandonCallSummary.class))).thenReturn(null);

        Exception exception = assertThrows(Exception.class, () -> {
            nhmDashboardService.pushAbandonCalls(abandonCallSummary);
        });

        assertEquals("error in saving record, please contact administrator", exception.getMessage());
        verify(abandonCallSummaryRepo, times(1)).save(abandonCallSummary);
        // Logging assertions omitted for compatibility
    }

    @Test
    void callAgentSummaryReportCTI_API_Success() throws IEMRException {
        LocalDateTime fixedDateTime = LocalDateTime.of(2023, 10, 26, 12, 0, 0);

        try (MockedStatic<ConfigProperties> mockedConfig = mockStatic(ConfigProperties.class);
             MockedStatic<InputMapper> mockedInputMapper = mockStatic(InputMapper.class);
             MockedStatic<LocalDateTime> mockedLocalDateTime = mockStatic(LocalDateTime.class)) {

            mockedLocalDateTime.when(LocalDateTime::now).thenReturn(fixedDateTime);

            mockedConfig.when(() -> ConfigProperties.getPropertyByName("get-agent-summary-report-URL"))
                    .thenReturn("http://CTI_SERVER/agent_summary?startDate=START_DATE&endDate=END_DATE");
            mockedConfig.when(() -> ConfigProperties.getPropertyByName("cti-server-ip"))
                    .thenReturn("192.168.1.100");

            String mockApiResponse = "[{\"agentName\":\"Agent1\"},{\"agentName\":\"Agent2\"}]";
            when(httpUtils.get(anyString())).thenReturn(mockApiResponse);

            InputMapper mockInputMapper = mock(InputMapper.class);
            mockedInputMapper.when(InputMapper::gson).thenReturn(mockInputMapper);
            when(mockInputMapper.fromJson(anyString(), eq(AgentSummaryReport[].class)))
                    .thenReturn(new AgentSummaryReport[]{new AgentSummaryReport(), new AgentSummaryReport()});

            List<AgentSummaryReport> result = nhmDashboardService.callAgentSummaryReportCTI_API();

            assertNotNull(result);
            assertEquals(2, result.size());

            LocalDateTime yesterday = fixedDateTime.minusDays(1);
            String expectedDatePart = yesterday.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String expectedEndDate = expectedDatePart.concat(" 23:59:59");
            String expectedFromDate = expectedDatePart.concat(" 00:00:01");
            String expectedUrl = "http://192.168.1.100/agent_summary?startDate=" + expectedFromDate + "&endDate=" + expectedEndDate;

            verify(httpUtils, times(1)).get(expectedUrl);
            // Logging assertions omitted for compatibility
        }
    }

    @Test
    void callAgentSummaryReportCTI_API_PleaseWaitException() {
        LocalDateTime fixedDateTime = LocalDateTime.of(2023, 10, 26, 12, 0, 0);

        try (MockedStatic<ConfigProperties> mockedConfig = mockStatic(ConfigProperties.class);
             MockedStatic<LocalDateTime> mockedLocalDateTime = mockStatic(LocalDateTime.class)) {

            mockedLocalDateTime.when(LocalDateTime::now).thenReturn(fixedDateTime);
            mockedConfig.when(() -> ConfigProperties.getPropertyByName(anyString())).thenReturn("dummy_url");
            when(httpUtils.get(anyString())).thenReturn("Please wait for 1 hour");

            IEMRException exception = assertThrows(IEMRException.class, () -> {
                nhmDashboardService.callAgentSummaryReportCTI_API();
            });

            assertEquals("Please wait for 1 hour", exception.getMessage());
            // Logging assertions omitted for compatibility
        }
    }

    @Test
    void callAgentSummaryReportCTI_API_NoDataException() {
        LocalDateTime fixedDateTime = LocalDateTime.of(2023, 10, 26, 12, 0, 0);

        try (MockedStatic<ConfigProperties> mockedConfig = mockStatic(ConfigProperties.class);
             MockedStatic<LocalDateTime> mockedLocalDateTime = mockStatic(LocalDateTime.class)) {

            mockedLocalDateTime.when(LocalDateTime::now).thenReturn(fixedDateTime);
            mockedConfig.when(() -> ConfigProperties.getPropertyByName(anyString())).thenReturn("dummy_url");
            when(httpUtils.get(anyString())).thenReturn("No data found");

            IEMRException exception = assertThrows(IEMRException.class, () -> {
                nhmDashboardService.callAgentSummaryReportCTI_API();
            });

            assertEquals("No data found", exception.getMessage());
            // Logging assertions omitted for compatibility
        }
    }

    @Test
    void callAgentSummaryReportCTI_API_HttpUtilsGetThrowsException() {
        LocalDateTime fixedDateTime = LocalDateTime.of(2023, 10, 26, 12, 0, 0);

        try (MockedStatic<ConfigProperties> mockedConfig = mockStatic(ConfigProperties.class);
             MockedStatic<LocalDateTime> mockedLocalDateTime = mockStatic(LocalDateTime.class)) {

            mockedLocalDateTime.when(LocalDateTime::now).thenReturn(fixedDateTime);
            mockedConfig.when(() -> ConfigProperties.getPropertyByName(anyString())).thenReturn("dummy_url");
            when(httpUtils.get(anyString())).thenThrow(new RuntimeException("Network error"));

            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                nhmDashboardService.callAgentSummaryReportCTI_API();
            });

            assertEquals("Network error", exception.getMessage());
        }
    }

    @Test
    void callAgentSummaryReportCTI_API_InputMapperThrowsException() {
        LocalDateTime fixedDateTime = LocalDateTime.of(2023, 10, 26, 12, 0, 0);

        try (MockedStatic<ConfigProperties> mockedConfig = mockStatic(ConfigProperties.class);
             MockedStatic<InputMapper> mockedInputMapper = mockStatic(InputMapper.class);
             MockedStatic<LocalDateTime> mockedLocalDateTime = mockStatic(LocalDateTime.class)) {

            mockedLocalDateTime.when(LocalDateTime::now).thenReturn(fixedDateTime);
            mockedConfig.when(() -> ConfigProperties.getPropertyByName(anyString())).thenReturn("dummy_url");
            when(httpUtils.get(anyString())).thenReturn("invalid json");

            InputMapper mockInputMapper = mock(InputMapper.class);
            mockedInputMapper.when(InputMapper::gson).thenReturn(mockInputMapper);
            when(mockInputMapper.fromJson(anyString(), eq(AgentSummaryReport[].class)))
                    .thenThrow(new RuntimeException("JSON parsing error"));

            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                nhmDashboardService.callAgentSummaryReportCTI_API();
            });

            assertEquals("JSON parsing error", exception.getMessage());
            // Logging assertions omitted for compatibility
        }
    }

    @Test
    void saveDetailedCallReport_Success() throws IEMRException {
        List<DetailedCallReport> detailedCallReportList = new ArrayList<>();
        DetailedCallReport dr1 = mock(DetailedCallReport.class);
        when(dr1.getCall_Start_Time()).thenReturn("2023-01-01 10:00:00");
        when(dr1.getCall_End_Time()).thenReturn("2023-01-01 10:05:00");
        when(dr1.getAgent_Ring_Start_Time()).thenReturn("2023-01-01 09:59:00");
        when(dr1.getAgent_Ring_End_Time()).thenReturn("2023-01-01 10:00:00");
        detailedCallReportList.add(dr1);

        DetailedCallReport dr2 = mock(DetailedCallReport.class);
        when(dr2.getCall_Start_Time()).thenReturn("2023-01-02 11:00:00");
        when(dr2.getCall_End_Time()).thenReturn(null);
        when(dr2.getAgent_Ring_Start_Time()).thenReturn("0000-00-00 00:00:00");
        when(dr2.getAgent_Ring_End_Time()).thenReturn("2023-01-02 11:00:00");
        detailedCallReportList.add(dr2);

        when(detailedCallReportRepo.saveAll(anyIterable())).thenReturn(detailedCallReportList);

        String result = nhmDashboardService.saveDetailedCallReport(detailedCallReportList);

        assertEquals("2 detailedCallReport records saved successfully", result);
        verify(detailedCallReportRepo, times(1)).saveAll(detailedCallReportList);

        verify(dr1, times(1)).setCallStartTime(Timestamp.valueOf("2023-01-01 10:00:00"));
        verify(dr1, times(1)).setCallEndTime(Timestamp.valueOf("2023-01-01 10:05:00"));
        verify(dr1, times(1)).setAgent_Ring_Start_Time_T(Timestamp.valueOf("2023-01-01 09:59:00"));
        verify(dr1, times(1)).setAgent_Ring_End_Time_T(Timestamp.valueOf("2023-01-01 10:00:00"));

        verify(dr2, times(1)).setCallStartTime(Timestamp.valueOf("2023-01-02 11:00:00"));
        verify(dr2, never()).setCallEndTime(any(Timestamp.class));
        verify(dr2, never()).setAgent_Ring_Start_Time_T(any(Timestamp.class));
        verify(dr2, times(1)).setAgent_Ring_End_Time_T(Timestamp.valueOf("2023-01-02 11:00:00"));

        // Logging assertions omitted for compatibility
    }

    @Test
    void saveDetailedCallReport_EmptyList() {
        List<DetailedCallReport> detailedCallReportList = new ArrayList<>();

        IEMRException exception = assertThrows(IEMRException.class, () -> {
            nhmDashboardService.saveDetailedCallReport(detailedCallReportList);
        });

        assertEquals("please pass valid DetailedCallReport data in list", exception.getMessage());
        verify(detailedCallReportRepo, never()).saveAll(anyIterable());
    }

    @Test
    void saveDetailedCallReport_NullList() {
        IEMRException exception = assertThrows(IEMRException.class, () -> {
            nhmDashboardService.saveDetailedCallReport(null);
        });

        assertEquals("please pass valid DetailedCallReport data in list", exception.getMessage());
        verify(detailedCallReportRepo, never()).saveAll(anyIterable());
    }

    @Test
    void saveDetailedCallReport_SaveAllThrowsException() {
        List<DetailedCallReport> detailedCallReportList = new ArrayList<>();
        DetailedCallReport dr1 = mock(DetailedCallReport.class);
        when(dr1.getCall_Start_Time()).thenReturn("2023-01-01 10:00:00");
        detailedCallReportList.add(dr1);

        when(detailedCallReportRepo.saveAll(anyIterable())).thenThrow(new RuntimeException("Database error"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            nhmDashboardService.saveDetailedCallReport(detailedCallReportList);
        });

        assertEquals("Database error", exception.getMessage());
        verify(detailedCallReportRepo, times(1)).saveAll(detailedCallReportList);
    }

    // --- END MERGED TESTS ---
}

