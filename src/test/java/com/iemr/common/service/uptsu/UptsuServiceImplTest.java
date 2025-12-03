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
package com.iemr.common.service.uptsu;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.iemr.common.data.uptsu.FacilityMaster;
import com.iemr.common.data.uptsu.SmsRequestOBJ;
import com.iemr.common.data.uptsu.T_104AppointmentDetails;
import com.iemr.common.model.sms.SMSRequest;
import com.iemr.common.repository.uptsu.FacilityMasterRepo;
import com.iemr.common.repository.uptsu.T_104AppointmentDetailsRepo;
import com.iemr.common.service.sms.SMSService;
import com.iemr.common.utils.RestTemplateUtil;
import com.iemr.common.utils.exception.IEMRException;
import com.iemr.common.utils.mapper.InputMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UptsuServiceImplTest {
    @InjectMocks
    UptsuServiceImpl service;

    @Mock
    FacilityMasterRepo facilityMasterRepo;
    @Mock
    T_104AppointmentDetailsRepo t_104AppointmentDetailsRepo;
    @Mock
    SMSService smsService;

    @BeforeEach
    public void setup() {
        // Set required @Value fields for template strings
        try {
            java.lang.reflect.Field choField = UptsuServiceImpl.class.getDeclaredField("CHOSmsTemplate");
            choField.setAccessible(true);
            choField.set(service, "template");
            java.lang.reflect.Field benField = UptsuServiceImpl.class.getDeclaredField("BeneficiarySmsTemplate");
            benField.setAccessible(true);
            benField.set(service, "template");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testGetFacility() {
        List<FacilityMaster> facilities = Arrays.asList(mock(FacilityMaster.class));
        when(facilityMasterRepo.findByProviderServiceMapIDAndBlockNameAndDeleted(1, "block", false)).thenReturn(facilities);
        String json = service.getFacility(1, "block");
        assertTrue(json.contains("[") && json.contains("]"));
    }

    @Test
    public void testSaveAppointmentDetails_success() throws Exception {
        long now = System.currentTimeMillis();
        Timestamp ts = new Timestamp(now);
        java.time.format.DateTimeFormatter isoFormatter = java.time.format.DateTimeFormatter.ISO_INSTANT;
        String appointmentDateStr = java.time.Instant.ofEpochMilli(ts.getTime()).toString();
        Gson gson = new Gson().newBuilder()
                .registerTypeAdapter(Timestamp.class, new com.google.gson.TypeAdapter<Timestamp>() {
                    @Override
                    public void write(com.google.gson.stream.JsonWriter out, Timestamp value) throws java.io.IOException {
                        if (value == null) out.nullValue();
                        else out.value(java.time.Instant.ofEpochMilli(value.getTime()).toString());
                    }
                    @Override
                    public Timestamp read(com.google.gson.stream.JsonReader in) throws java.io.IOException {
                        if (in.peek() == com.google.gson.stream.JsonToken.NULL) {
                            in.nextNull();
                            return null;
                        }
                        String str = in.nextString();
                        return Timestamp.from(java.time.Instant.parse(str));
                    }
                }).create();
        String request = "{" +
                "\"appointmentDate\":\"" + appointmentDateStr + "\"," +
                "\"benRegId\":123," +
                "\"facilityPhoneNo\":\"1234567890\"," +
                "\"choName\":\"cho\"," +
                "\"employeeCode\":\"emp\"," +
                "\"facilityName\":\"facility\"," +
                "\"hfrId\":\"hfr\"," +
                "\"alternateMobNo\":\"alt\"," +
                "\"createdBy\":\"creator\"," +
                "\"refferedFlag\":false" +
                "}";
        T_104AppointmentDetails details = new T_104AppointmentDetails();
        details.setAppointmentDate(ts);
        details.setBenRegId(123L);
        details.setFacilityPhoneNo("1234567890");
        details.setChoName("cho");
        details.setEmployeeCode("emp");
        details.setFacilityName("facility");
        details.setHfrId("hfr");
        details.setAlternateMobNo("alt");
        details.setCreatedBy("creator");
        details.setRefferedFlag(false);
        when(t_104AppointmentDetailsRepo.save(any(T_104AppointmentDetails.class))).thenReturn(details);
        Object[] benInfo = new Object[]{new Object[]{"benName", BigInteger.valueOf(456)}};
        when(t_104AppointmentDetailsRepo.findBeneficiaryNameAndBeneficiaryIdByBenRegId(anyLong())).thenReturn(benInfo);
        when(t_104AppointmentDetailsRepo.getSMSTypeIDCho(any())).thenReturn(1);
        when(t_104AppointmentDetailsRepo.getSMSTemplateIDCho(anyInt())).thenReturn(2);
        when(t_104AppointmentDetailsRepo.getSMSTypeIDBen(any())).thenReturn(1);
        when(t_104AppointmentDetailsRepo.getSMSTemplateIDBen(anyInt())).thenReturn(2);
        String result = service.saveAppointmentDetails(request, "auth");
        assertEquals("Appointment scheduled successfully", result);
    }

    @Test
    public void testSaveAppointmentDetails_exception() throws Exception {
        long now = System.currentTimeMillis();
        Timestamp ts = new Timestamp(now);
        String appointmentDateStr = java.time.Instant.ofEpochMilli(ts.getTime()).toString();
        Gson gson = new Gson().newBuilder()
                .registerTypeAdapter(Timestamp.class, new com.google.gson.TypeAdapter<Timestamp>() {
                    @Override
                    public void write(com.google.gson.stream.JsonWriter out, Timestamp value) throws java.io.IOException {
                        if (value == null) out.nullValue();
                        else out.value(java.time.Instant.ofEpochMilli(value.getTime()).toString());
                    }
                    @Override
                    public Timestamp read(com.google.gson.stream.JsonReader in) throws java.io.IOException {
                        if (in.peek() == com.google.gson.stream.JsonToken.NULL) {
                            in.nextNull();
                            return null;
                        }
                        String str = in.nextString();
                        return Timestamp.from(java.time.Instant.parse(str));
                    }
                }).create();
        String request = "{" +
                "\"appointmentDate\":\"" + appointmentDateStr + "\"," +
                "\"benRegId\":123," +
                "\"facilityPhoneNo\":\"1234567890\"," +
                "\"choName\":\"cho\"," +
                "\"employeeCode\":\"emp\"," +
                "\"facilityName\":\"facility\"," +
                "\"hfrId\":\"hfr\"," +
                "\"alternateMobNo\":\"alt\"," +
                "\"createdBy\":\"creator\"," +
                "\"refferedFlag\":false" +
                "}";
        T_104AppointmentDetails details = new T_104AppointmentDetails();
        details.setAppointmentDate(ts);
        details.setBenRegId(123L);
        details.setFacilityPhoneNo("1234567890");
        details.setChoName("cho");
        details.setEmployeeCode("emp");
        details.setFacilityName("facility");
        details.setHfrId("hfr");
        details.setAlternateMobNo("alt");
        details.setCreatedBy("creator");
        details.setRefferedFlag(false);
        when(t_104AppointmentDetailsRepo.save(any(T_104AppointmentDetails.class))).thenReturn(details);
        Object[] benInfo = new Object[]{new Object[]{"benName", BigInteger.valueOf(456)}};
        when(t_104AppointmentDetailsRepo.findBeneficiaryNameAndBeneficiaryIdByBenRegId(anyLong())).thenReturn(benInfo);
        when(t_104AppointmentDetailsRepo.getSMSTypeIDCho(any())).thenReturn(1);
        when(t_104AppointmentDetailsRepo.getSMSTemplateIDCho(anyInt())).thenReturn(2);
        when(t_104AppointmentDetailsRepo.getSMSTypeIDBen(any())).thenReturn(1);
        when(t_104AppointmentDetailsRepo.getSMSTemplateIDBen(anyInt())).thenReturn(2);
        doThrow(new RuntimeException("fail")).when(smsService).sendSMS(anyList(), anyString());
        Exception ex = assertThrows(IEMRException.class, () -> service.saveAppointmentDetails(request, "auth"));
        assertTrue(ex.getMessage().contains("Error while scheduling appointment"));
    }

    @Test
    public void testCreateSmsGateway_success() throws Exception {
        when(t_104AppointmentDetailsRepo.getSMSTypeIDCho(any())).thenReturn(1);
        when(t_104AppointmentDetailsRepo.getSMSTemplateIDCho(anyInt())).thenReturn(2);
        when(t_104AppointmentDetailsRepo.getSMSTypeIDBen(any())).thenReturn(1);
        when(t_104AppointmentDetailsRepo.getSMSTemplateIDBen(anyInt())).thenReturn(2);
        String result = service.createSmsGateway("2025-07-27", "10:00 AM", "1234567890", "cho", "emp", "ben", 456L, "facility", "hfr", "alt", 123L, "auth", "creator");
        assertEquals("Appointment scheduled successfully", result);
    }

    @Test
    public void testCreateSmsGateway_exception() throws Exception {
        when(t_104AppointmentDetailsRepo.getSMSTypeIDCho(any())).thenReturn(1);
        when(t_104AppointmentDetailsRepo.getSMSTemplateIDCho(anyInt())).thenReturn(2);
        when(t_104AppointmentDetailsRepo.getSMSTypeIDBen(any())).thenReturn(1);
        when(t_104AppointmentDetailsRepo.getSMSTemplateIDBen(anyInt())).thenReturn(2);
        doThrow(new RuntimeException("fail")).when(smsService).sendSMS(anyList(), anyString());
        Exception ex = assertThrows(IEMRException.class, () -> service.createSmsGateway("2025-07-27", "10:00 AM", "1234567890", "cho", "emp", "ben", 456L, "facility", "hfr", "alt", 123L, "auth", "creator"));
        assertTrue(ex.getMessage().contains("Error while scheduling appointment"));
    }

    @Test
    public void testCreateSMSRequestForCho_and_Beneficiary() {
        when(t_104AppointmentDetailsRepo.getSMSTypeIDCho(anyString())).thenReturn(1);
        when(t_104AppointmentDetailsRepo.getSMSTemplateIDCho(anyInt())).thenReturn(2);
        String choJson = service.createSMSRequestForCho("template", "2025-07-27", "10:00 AM", "1234567890", "cho", "emp", "ben", 456L, "facility", "hfr", 123L, "creator");
        assertTrue(choJson.contains("smsTemplateID"));
        when(t_104AppointmentDetailsRepo.getSMSTypeIDBen(anyString())).thenReturn(1);
        when(t_104AppointmentDetailsRepo.getSMSTemplateIDBen(anyInt())).thenReturn(2);
        String benJson = service.createSMSRequestForBeneficiary("template", "2025-07-27", "10:00 AM", "alt", "ben", 456L, "facility", "hfr", 123L, "creator");
        assertTrue(benJson.contains("smsTemplateID"));
    }

    @Test
    public void testCreateSMSRequestForCho_and_Beneficiary_zeroType() {
        when(t_104AppointmentDetailsRepo.getSMSTypeIDCho(anyString())).thenReturn(0);
        String choJson = service.createSMSRequestForCho("template", "2025-07-27", "10:00 AM", "1234567890", "cho", "emp", "ben", 456L, "facility", "hfr", 123L, "creator");
        assertEquals("[]", choJson);
        when(t_104AppointmentDetailsRepo.getSMSTypeIDBen(anyString())).thenReturn(0);
        String benJson = service.createSMSRequestForBeneficiary("template", "2025-07-27", "10:00 AM", "alt", "ben", 456L, "facility", "hfr", 123L, "creator");
        assertEquals("[]", benJson);
    }

    @Test
    public void testRestTemplate() {
        HttpEntity<Object> entity = mock(HttpEntity.class);
        try (org.mockito.MockedStatic<RestTemplateUtil> staticMock = mockStatic(RestTemplateUtil.class);
             org.mockito.MockedConstruction<RestTemplate> restTemplateMocked = mockConstruction(RestTemplate.class, (mock, context) -> {
                 String expected = "response";
                 ResponseEntity<String> responseEntity = mock(ResponseEntity.class);
                 when(responseEntity.getBody()).thenReturn(expected);
                 when(mock.exchange(anyString(), eq(HttpMethod.POST), eq(entity), eq(String.class))).thenReturn(responseEntity);
             })) {
            staticMock.when(() -> RestTemplateUtil.createRequestEntity(anyString(), anyString())).thenReturn(entity);
            String result = service.restTemplate("req", "url", "auth");
            assertEquals("response", result);
        }
    }
}
