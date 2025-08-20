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
package com.iemr.common.service.honeywell;

import com.google.gson.Gson;
import com.iemr.common.data.report.CallQualityReport;
import com.iemr.common.notification.exception.IEMRException;
import com.iemr.common.repository.report.CRMCallReportRepo;
import com.iemr.common.utils.mapper.InputMapper;
import com.iemr.common.utils.mapper.OutputMapper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.Serializable;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HoneywellServiceImplTest {

    @InjectMocks
    private HoneywellServiceImpl honeywellServiceImpl;

    @Mock
    private CRMCallReportRepo crmCallReportRepository;

    @Mock
    private Logger mockLogger;

    // Use the real CallQualityReport for all mocks and test data

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(honeywellServiceImpl, "logger", mockLogger);
    }

    @Test
    void getDistrictWiseCallReport_Success() throws Exception {
        String request = "{\"startDateTime\":\"2023-01-01T00:00:00.000Z\",\"endDateTime\":\"2023-01-02T00:00:00.000Z\"}";
        Timestamp startTs = Timestamp.valueOf("2023-01-01 00:00:00.000");
        Timestamp endTs = Timestamp.valueOf("2023-01-02 00:00:00.000");

        CallQualityReport callQualityReport = new CallQualityReport();
        callQualityReport.setStartDateTime(startTs);
        callQualityReport.setEndDateTime(endTs);

        List<Object[]> repoResult = Arrays.asList(
                new Object[]{1, "District A", BigInteger.valueOf(50)},
                new Object[]{2, "District B", BigInteger.valueOf(150)}
        );

        try (MockedStatic<InputMapper> mockedInputMapper = Mockito.mockStatic(InputMapper.class);
             MockedStatic<OutputMapper> mockedOutputMapper = Mockito.mockStatic(OutputMapper.class)) {
            InputMapper mockInputMapperInstance = Mockito.mock(InputMapper.class);
            mockedInputMapper.when(InputMapper::gson).thenReturn(mockInputMapperInstance);
            when(mockInputMapperInstance.fromJson(anyString(), eq(CallQualityReport.class)))
                    .thenReturn(callQualityReport);
            Gson mockGson = Mockito.mock(Gson.class);
            mockedOutputMapper.when(OutputMapper::gson).thenReturn(mockGson);
            when(mockGson.toJson(request)).thenReturn(request); // stub for logger
            when(crmCallReportRepository.allDistrictsCount(startTs, endTs)).thenReturn(repoResult);
            String result = honeywellServiceImpl.getDistrictWiseCallReport(request);
            assertNotNull(result);
            JSONArray jsonArray = new JSONArray(result);
            assertEquals(2, jsonArray.length());
            JSONObject districtA = jsonArray.getJSONObject(0);
            assertEquals("District A", districtA.getString("district"));
            assertEquals(" 25.00", districtA.getString("perc"));
            JSONObject districtB = jsonArray.getJSONObject(1);
            assertEquals("District B", districtB.getString("district"));
            assertEquals(" 75.00", districtB.getString("perc"));
            verify(mockLogger, times(1)).info("honeywell-getDistrictWisePercentageReport- start");
            verify(mockLogger, times(1)).debug("getDistrictWiseCallReport request: " + request);
            verify(mockLogger, times(2)).info(Mockito.startsWith("objects"));
            verify(mockLogger, times(2)).info(Mockito.startsWith("responseArray"));
            verify(mockLogger, times(1)).info("honeywell-getDistrictWisePercentageReport - end");
        }
    }

    @Test
    void getDistrictWiseCallReport_InvalidDateFormat() {
        String request = "invalid json";

        try (MockedStatic<InputMapper> mockedInputMapper = Mockito.mockStatic(InputMapper.class);
             MockedStatic<OutputMapper> mockedOutputMapper = Mockito.mockStatic(OutputMapper.class)) {
            InputMapper mockInputMapperInstance = Mockito.mock(InputMapper.class);
            mockedInputMapper.when(InputMapper::gson).thenReturn(mockInputMapperInstance);
            when(mockInputMapperInstance.fromJson(anyString(), eq(CallQualityReport.class)))
                    .thenThrow(new RuntimeException("Parsing error"));
            Gson mockGson = Mockito.mock(Gson.class);
            mockedOutputMapper.when(OutputMapper::gson).thenReturn(mockGson);
            when(mockGson.toJson(request)).thenReturn(request); // stub for logger
            IEMRException exception = assertThrows(IEMRException.class, () ->
                    honeywellServiceImpl.getDistrictWiseCallReport(request));
            assertEquals("Date format incorrect", exception.getMessage());
            verify(mockLogger, times(1)).info("honeywell-getDistrictWisePercentageReport- start");
            verify(mockLogger, times(1)).debug("getDistrictWiseCallReport request: " + request);
        }
    }

    @Test
    void getDistrictWiseCallReport_RepositoryException() {
        String request = "{\"startDateTime\":\"2023-01-01T00:00:00.000Z\",\"endDateTime\":\"2023-01-02T00:00:00.000Z\"}";
        Timestamp startTs = Timestamp.valueOf("2023-01-01 00:00:00.000");
        Timestamp endTs = Timestamp.valueOf("2023-01-02 00:00:00.000");

        CallQualityReport callQualityReport = new CallQualityReport();
        callQualityReport.setStartDateTime(startTs);
        callQualityReport.setEndDateTime(endTs);

        try (MockedStatic<InputMapper> mockedInputMapper = Mockito.mockStatic(InputMapper.class);
             MockedStatic<OutputMapper> mockedOutputMapper = Mockito.mockStatic(OutputMapper.class)) {
            InputMapper mockInputMapperInstance = Mockito.mock(InputMapper.class);
            mockedInputMapper.when(InputMapper::gson).thenReturn(mockInputMapperInstance);
            when(mockInputMapperInstance.fromJson(anyString(), eq(CallQualityReport.class)))
                    .thenReturn(callQualityReport);
            Gson mockGson = Mockito.mock(Gson.class);
            mockedOutputMapper.when(OutputMapper::gson).thenReturn(mockGson);
            when(mockGson.toJson(request)).thenReturn(request); // stub for logger
            when(crmCallReportRepository.allDistrictsCount(startTs, endTs)).thenThrow(new RuntimeException("DB error"));
            IEMRException exception = assertThrows(IEMRException.class, () ->
                    honeywellServiceImpl.getDistrictWiseCallReport(request));
            assertEquals("Error while fetching district data", exception.getMessage());
            verify(mockLogger, times(1)).info("honeywell-getDistrictWisePercentageReport- start");
            verify(mockLogger, times(1)).debug("getDistrictWiseCallReport request: " + request);
        }
    }

    @Test
    void getDistrictWiseCallReport_EmptyData() throws Exception {
        String request = "{\"startDateTime\":\"2023-01-01T00:00:00.000Z\",\"endDateTime\":\"2023-01-02T00:00:00.000Z\"}";
        Timestamp startTs = Timestamp.valueOf("2023-01-01 00:00:00.000");
        Timestamp endTs = Timestamp.valueOf("2023-01-02 00:00:00.000");

        CallQualityReport callQualityReport = new CallQualityReport();
        callQualityReport.setStartDateTime(startTs);
        callQualityReport.setEndDateTime(endTs);

        try (MockedStatic<InputMapper> mockedInputMapper = Mockito.mockStatic(InputMapper.class);
             MockedStatic<OutputMapper> mockedOutputMapper = Mockito.mockStatic(OutputMapper.class)) {
            InputMapper mockInputMapperInstance = Mockito.mock(InputMapper.class);
            mockedInputMapper.when(InputMapper::gson).thenReturn(mockInputMapperInstance);
            when(mockInputMapperInstance.fromJson(anyString(), eq(CallQualityReport.class)))
                    .thenReturn(callQualityReport);
            Gson mockGson = Mockito.mock(Gson.class);
            mockedOutputMapper.when(OutputMapper::gson).thenReturn(mockGson);
            when(mockGson.toJson(request)).thenReturn(request); // stub for logger
            when(crmCallReportRepository.allDistrictsCount(startTs, endTs)).thenReturn(Collections.emptyList());
            String result = honeywellServiceImpl.getDistrictWiseCallReport(request);
            assertNotNull(result);
            JSONArray jsonArray = new JSONArray(result);
            assertEquals(0, jsonArray.length());
            verify(mockLogger, times(1)).info("honeywell-getDistrictWisePercentageReport- start");
            verify(mockLogger, times(1)).debug("getDistrictWiseCallReport request: " + request);
            verify(mockLogger, times(1)).info("honeywell-getDistrictWisePercentageReport - end");
        }
    }

    @Test
    void getRealtimeDistrictWiseCallReport_Success() throws Exception {
        List<Object[]> repoResult = Arrays.asList(
                new Object[]{101, "District X", BigInteger.valueOf(75)},
                new Object[]{102, "District Y", BigInteger.valueOf(25)}
        );

        when(crmCallReportRepository.allDistrictsCount(any(Timestamp.class), any(Timestamp.class)))
                .thenReturn(repoResult);

        String result = honeywellServiceImpl.getRealtimeDistrictWiseCallReport();

        assertNotNull(result);
        JSONArray jsonArray = new JSONArray(result);
        assertEquals(2, jsonArray.length());

        JSONObject districtX = jsonArray.getJSONObject(0);
        assertEquals("District X", districtX.getString("district"));
        assertEquals(" 75.00", districtX.getString("perc"));

        JSONObject districtY = jsonArray.getJSONObject(1);
        assertEquals("District Y", districtY.getString("district"));
        assertEquals(" 25.00", districtY.getString("perc"));

        // The service logs 'honeywell-getDistrictWisePercentageReport- start' and 'end' once each
        verify(mockLogger, times(1)).info("honeywell-getDistrictWisePercentageReport- start");
        verify(mockLogger, times(1)).info("honeywell-getDistrictWisePercentageReport - end");
        // The service logs 'responseArray' for each district
        verify(mockLogger, times(2)).info(Mockito.startsWith("responseArray"));
    }

    @Test
    void getRealtimeDistrictWiseCallReport_RepositoryException() {
        try (MockedStatic<OutputMapper> mockedOutputMapper = Mockito.mockStatic(OutputMapper.class)) {
            Gson mockGson = Mockito.mock(Gson.class);
            mockedOutputMapper.when(OutputMapper::gson).thenReturn(mockGson);
            when(crmCallReportRepository.allDistrictsCount(any(Timestamp.class), any(Timestamp.class)))
                    .thenThrow(new RuntimeException("DB error"));
            IEMRException exception = assertThrows(IEMRException.class, () ->
                    honeywellServiceImpl.getRealtimeDistrictWiseCallReport());
            assertEquals("Error while fetching district data", exception.getMessage());
            verify(mockLogger, times(1)).info("honeywell-getDistrictWisePercentageReport- start");
        }
    }

    @Test
    void getRealtimeDistrictWiseCallReport_EmptyData() throws Exception {
        try (MockedStatic<OutputMapper> mockedOutputMapper = Mockito.mockStatic(OutputMapper.class)) {
            Gson mockGson = Mockito.mock(Gson.class);
            mockedOutputMapper.when(OutputMapper::gson).thenReturn(mockGson);
            when(crmCallReportRepository.allDistrictsCount(any(Timestamp.class), any(Timestamp.class)))
                    .thenReturn(Collections.emptyList());
            String result = honeywellServiceImpl.getRealtimeDistrictWiseCallReport();
            assertNotNull(result);
            JSONArray jsonArray = new JSONArray(result);
            assertEquals(0, jsonArray.length());
            verify(mockLogger, times(1)).info("honeywell-getDistrictWisePercentageReport- start");
            verify(mockLogger, times(1)).info("honeywell-getDistrictWisePercentageReport - end");
        }
    }

    @Test
    void getUrbanRuralCallReport_Success() throws Exception {
        String request = "{\"startDate\":\"2023-01-01\",\"endDate\":\"2023-01-02\"}";
        Timestamp startDate = Timestamp.valueOf("2023-01-01 00:00:00");
        Timestamp endDate = Timestamp.valueOf("2023-01-02 00:00:00");

        CallQualityReport callQualityReport = new CallQualityReport();
        callQualityReport.setStartDate(startDate);
        callQualityReport.setEndDate(endDate);

        List<Object[]> repoResult = Arrays.asList(
                new Object[]{true, BigInteger.valueOf(30)},
                new Object[]{false, BigInteger.valueOf(70)}
        );

        try (MockedStatic<InputMapper> mockedInputMapper = Mockito.mockStatic(InputMapper.class)) {
            InputMapper mockInputMapperInstance = Mockito.mock(InputMapper.class);
            mockedInputMapper.when(InputMapper::gson).thenReturn(mockInputMapperInstance);
            when(mockInputMapperInstance.fromJson(anyString(), eq(CallQualityReport.class)))
                    .thenReturn(callQualityReport);
            when(crmCallReportRepository.allRuralCount(startDate, endDate)).thenReturn(repoResult);
            String result = honeywellServiceImpl.getUrbanRuralCallReport(request);
            assertNotNull(result);
            JSONObject obj = new JSONObject(result);
            assertEquals(30.0, obj.getDouble("rural"));
            assertEquals(70.0, obj.getDouble("urban"));
            verify(mockLogger, times(2)).info(Mockito.startsWith("objects"));
        }
    }

    @Test
    void getUrbanRuralCallReport_InvalidRequest_NullCallQualityReport() {
        String request = "invalid json";

        try (MockedStatic<InputMapper> mockedInputMapper = Mockito.mockStatic(InputMapper.class)) {
            InputMapper mockInputMapperInstance = Mockito.mock(InputMapper.class);
            mockedInputMapper.when(InputMapper::gson).thenReturn(mockInputMapperInstance);
            when(mockInputMapperInstance.fromJson(anyString(), eq(CallQualityReport.class)))
                    .thenReturn(null);

            IEMRException exception = assertThrows(IEMRException.class, () ->
                    honeywellServiceImpl.getUrbanRuralCallReport(request));

            assertEquals("Error while fetching Urban And Rural data", exception.getMessage());
        }
    }

    @Test
    void getUrbanRuralCallReport_RepositoryException() {
        String request = "{\"startDate\":\"2023-01-01\",\"endDate\":\"2023-01-02\"}";
        Timestamp startDate = Timestamp.valueOf("2023-01-01 00:00:00");
        Timestamp endDate = Timestamp.valueOf("2023-01-02 00:00:00");

        CallQualityReport callQualityReport = new CallQualityReport();
        callQualityReport.setStartDate(startDate);
        callQualityReport.setEndDate(endDate);

        try (MockedStatic<InputMapper> mockedInputMapper = Mockito.mockStatic(InputMapper.class)) {
            InputMapper mockInputMapperInstance = Mockito.mock(InputMapper.class);
            mockedInputMapper.when(InputMapper::gson).thenReturn(mockInputMapperInstance);
            when(mockInputMapperInstance.fromJson(anyString(), eq(CallQualityReport.class)))
                    .thenReturn(callQualityReport);

            when(crmCallReportRepository.allRuralCount(any(Timestamp.class), any(Timestamp.class)))
                    .thenThrow(new RuntimeException("DB error"));

            IEMRException exception = assertThrows(IEMRException.class, () ->
                    honeywellServiceImpl.getUrbanRuralCallReport(request));

            assertEquals("Error while fetching Urban And Rural data", exception.getMessage());
        }
    }

    @Test
    void getUrbanRuralCallReport_ZeroTotalCount() throws Exception {
        String request = "{\"startDate\":\"2023-01-01\",\"endDate\":\"2023-01-02\"}";
        Timestamp startDate = Timestamp.valueOf("2023-01-01 00:00:00");
        Timestamp endDate = Timestamp.valueOf("2023-01-02 00:00:00");

        CallQualityReport callQualityReport = new CallQualityReport();
        callQualityReport.setStartDate(startDate);
        callQualityReport.setEndDate(endDate);

        List<Object[]> repoResult = Arrays.asList(
                new Object[]{true, BigInteger.valueOf(0)},
                new Object[]{false, BigInteger.valueOf(0)}
        );

        try (MockedStatic<InputMapper> mockedInputMapper = Mockito.mockStatic(InputMapper.class)) {
            InputMapper mockInputMapperInstance = Mockito.mock(InputMapper.class);
            mockedInputMapper.when(InputMapper::gson).thenReturn(mockInputMapperInstance);
            when(mockInputMapperInstance.fromJson(anyString(), eq(CallQualityReport.class)))
                    .thenReturn(callQualityReport);
            when(crmCallReportRepository.allRuralCount(startDate, endDate)).thenReturn(repoResult);
            String result = honeywellServiceImpl.getUrbanRuralCallReport(request);
            assertNotNull(result);
            assertEquals("{}", result);
            verify(mockLogger, times(2)).info(Mockito.startsWith("objects"));
        }
    }

    @Test
    void getUrbanRuralCallReport_EmptyRepoResult() throws Exception {
        String request = "{\"startDate\":\"2023-01-01\",\"endDate\":\"2023-01-02\"}";
        Timestamp startDate = Timestamp.valueOf("2023-01-01 00:00:00");
        Timestamp endDate = Timestamp.valueOf("2023-01-02 00:00:00");

        CallQualityReport callQualityReport = new CallQualityReport();
        callQualityReport.setStartDate(startDate);
        callQualityReport.setEndDate(endDate);

        try (MockedStatic<InputMapper> mockedInputMapper = Mockito.mockStatic(InputMapper.class)) {
            InputMapper mockInputMapperInstance = Mockito.mock(InputMapper.class);
            mockedInputMapper.when(InputMapper::gson).thenReturn(mockInputMapperInstance);
            when(mockInputMapperInstance.fromJson(anyString(), eq(CallQualityReport.class)))
                    .thenReturn(callQualityReport);
            when(crmCallReportRepository.allRuralCount(startDate, endDate)).thenReturn(Collections.emptyList());
            String result = honeywellServiceImpl.getUrbanRuralCallReport(request);
            assertNotNull(result);
            assertEquals("{}", result);
            verify(mockLogger, times(0)).info(Mockito.startsWith("objects"));
        }
    }
}