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
package com.iemr.common.service.esanjeevani;

import com.iemr.common.model.esanjeevani.ESanjeevaniPatientAddress;
import com.iemr.common.model.esanjeevani.ESanjeevaniPatientContactDetail;
import com.iemr.common.model.esanjeevani.ESanjeevaniPatientRegistration;
import com.iemr.common.repo.esanjeevani.ESanjeevaniRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.math.BigInteger;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ESanjeevaniServiceImplTest {
    @InjectMocks
    ESanjeevaniServiceImpl service;

    @Mock
    RestTemplate restTemplate;
    @Mock
    ESanjeevaniRepo eSanjeevaniRepo;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        // Set @Value fields via reflection
        setField("eSanjeevaniUrl", "http://test-url");
        setField("eSanjeevaniRegisterPatient", "http://register-url");
        setField("eSanjeevaniSource", "testSource");
        setField("eSanjeevaniSalt", "salt");
        setField("eSanjeevaniUserName", "user");
        setField("eSanjeevaniPassword", "pass");
        setField("eSanjeevaniRouteUrl", "http://route-url");
    }

    private void setField(String name, Object value) {
        try {
            java.lang.reflect.Field f = ESanjeevaniServiceImpl.class.getDeclaredField(name);
            f.setAccessible(true);
            f.set(service, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testGetProviderLogin_success() throws Exception {
        // ESanjeevaniProviderAuth reqObj = new ESanjeevaniProviderAuth();
        String tokenJson = "{\"model\":{\"access_token\":\"tok123\"}}";
        when(restTemplate.postForObject(anyString(), any(), eq(String.class))).thenReturn(tokenJson);
        String token = service.getProviderLogin();
        assertEquals("tok123", token);
    }

    @Test
    public void testGetProviderLogin_nullResponse() throws Exception {
        when(restTemplate.postForObject(anyString(), any(), eq(String.class))).thenReturn(null);
        String token = service.getProviderLogin();
        assertNull(token);
    }

    @Test
    public void testGetProviderLogin_error() throws Exception {
        when(restTemplate.postForObject(anyString(), any(), eq(String.class))).thenThrow(new RuntimeException("fail"));
        String token = service.getProviderLogin();
        assertTrue(token.contains("Error while fetching Authtoken"));
    }

    @Test
    public void testEncryptSHA512() {
        String hash = invokeEncryptSHA512("abc");
        assertNotNull(hash);
        assertEquals(128, hash.length());
    }

    private String invokeEncryptSHA512(String s) {
        try {
            java.lang.reflect.Method m = ESanjeevaniServiceImpl.class.getDeclaredMethod("encryptSHA512", String.class);
            m.setAccessible(true);
            return (String) m.invoke(service, s);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testRegisterPatient_success() throws Exception {
        // Setup repo mocks for mapping and abha details
        List<Object[]> mapping = new ArrayList<>();
        mapping.add(new Object[]{BigInteger.ONE, BigInteger.TWO, BigInteger.TEN});
        List<Object[]> abha = new ArrayList<>();
        abha.add(new Object[]{"abhaAddr", "abhaNum"});
        when(eSanjeevaniRepo.getBeneficiaryMappingIds(anyLong())).thenReturn(mapping);
        when(eSanjeevaniRepo.getBeneficiaryHealthIdDeatils(anyLong())).thenReturn(abha);
        // Details
        List<Object[]> details = new ArrayList<>();
        details.add(new Object[]{"John", "Doe", "M", "male", "2000-01-01 00:00:00"});
        when(eSanjeevaniRepo.getBeneficiaryDeatils(any())).thenReturn(details);
        // Address
        List<Object[]> addr = new ArrayList<>();
        addr.add(new Object[]{91, "India", 1, "State", 2, "District", 3, "Block", "Addr1", "123456"});
        when(eSanjeevaniRepo.getBeneficiaryAddressDetails(any())).thenReturn(addr);
        when(eSanjeevaniRepo.getGovCountyId(anyInt())).thenReturn("IN");
        when(eSanjeevaniRepo.getGovStateId(anyInt())).thenReturn(1);
        when(eSanjeevaniRepo.getGovDistrictId(anyInt())).thenReturn(2);
        List<Object[]> govBlock = new ArrayList<>();
        govBlock.add(new Object[]{3, 4, "Village"});
        when(eSanjeevaniRepo.getGovSubDistrictId(anyInt())).thenReturn(govBlock);
        // Contact
        when(eSanjeevaniRepo.getBeneficiaryContactDetails(any())).thenReturn("9999999999");
        // Auth
        String tokenJson = "{\"model\":{\"access_token\":\"tok123\"}}";
        when(restTemplate.postForObject(anyString(), any(), eq(String.class))).thenReturn(tokenJson);
        // Register
        Map<String, Object> respMap = new HashMap<>();
        respMap.put("success", true);
        respMap.put("msgCode", 1);
        respMap.put("message", "success");
        @SuppressWarnings("unchecked")
        ResponseEntity<Object> respEntity = (ResponseEntity<Object>) mock(ResponseEntity.class);
        when(respEntity.getStatusCode()).thenReturn(org.springframework.http.HttpStatus.OK);
        when(respEntity.getBody()).thenReturn(respMap);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Object.class))).thenReturn(respEntity);
        String result = service.registerPatient(1L);
        assertEquals("http://route-url", result);
    }

    @Test
    public void testRegisterPatient_noMapping() throws Exception {
        when(eSanjeevaniRepo.getBeneficiaryMappingIds(anyLong())).thenReturn(Collections.emptyList());
        when(eSanjeevaniRepo.getBeneficiaryHealthIdDeatils(anyLong())).thenReturn(Collections.emptyList());
        String result = service.registerPatient(1L);
        assertTrue(result.contains("No beneficiary Details found"));
    }

    @Test
    public void testRegisterPatient_mappingException() throws Exception {
        when(eSanjeevaniRepo.getBeneficiaryMappingIds(anyLong())).thenThrow(new RuntimeException("fail"));
        String result = service.registerPatient(1L);
        assertTrue(result.contains("Issue while fetching mapping details"));
    }

    @Test
    public void testRegisterPatient_authError() throws Exception {
        List<Object[]> mapping = new ArrayList<>();
        mapping.add(new Object[]{BigInteger.ONE, BigInteger.TWO, BigInteger.TEN});
        List<Object[]> abha = new ArrayList<>();
        abha.add(new Object[]{"abhaAddr", "abhaNum"});
        when(eSanjeevaniRepo.getBeneficiaryMappingIds(anyLong())).thenReturn(mapping);
        when(eSanjeevaniRepo.getBeneficiaryHealthIdDeatils(anyLong())).thenReturn(abha);
        // Details
        List<Object[]> details = new ArrayList<>();
        details.add(new Object[]{"John", "Doe", "M", "male", "2000-01-01 00:00:00"});
        when(eSanjeevaniRepo.getBeneficiaryDeatils(any())).thenReturn(details);
        // Address
        List<Object[]> addr = new ArrayList<>();
        addr.add(new Object[]{91, "India", 1, "State", 2, "District", 3, "Block", "Addr1", "123456"});
        when(eSanjeevaniRepo.getBeneficiaryAddressDetails(any())).thenReturn(addr);
        when(eSanjeevaniRepo.getGovCountyId(anyInt())).thenReturn("IN");
        when(eSanjeevaniRepo.getGovStateId(anyInt())).thenReturn(1);
        when(eSanjeevaniRepo.getGovDistrictId(anyInt())).thenReturn(2);
        List<Object[]> govBlock = new ArrayList<>();
        govBlock.add(new Object[]{3, 4, "Village"});
        when(eSanjeevaniRepo.getGovSubDistrictId(anyInt())).thenReturn(govBlock);
        // Contact
        when(eSanjeevaniRepo.getBeneficiaryContactDetails(any())).thenReturn("9999999999");
        // Auth error
        when(restTemplate.postForObject(anyString(), any(), eq(String.class))).thenReturn(null);
        String result = service.registerPatient(1L);
        assertNull(result);
    }

    @Test
    public void testRegisterPatient_registerError() throws Exception {
        List<Object[]> mapping = new ArrayList<>();
        mapping.add(new Object[]{BigInteger.ONE, BigInteger.TWO, BigInteger.TEN});
        List<Object[]> abha = new ArrayList<>();
        abha.add(new Object[]{"abhaAddr", "abhaNum"});
        when(eSanjeevaniRepo.getBeneficiaryMappingIds(anyLong())).thenReturn(mapping);
        when(eSanjeevaniRepo.getBeneficiaryHealthIdDeatils(anyLong())).thenReturn(abha);
        // Details
        List<Object[]> details = new ArrayList<>();
        details.add(new Object[]{"John", "Doe", "M", "male", "2000-01-01 00:00:00"});
        when(eSanjeevaniRepo.getBeneficiaryDeatils(any())).thenReturn(details);
        // Address
        List<Object[]> addr = new ArrayList<>();
        addr.add(new Object[]{91, "India", 1, "State", 2, "District", 3, "Block", "Addr1", "123456"});
        when(eSanjeevaniRepo.getBeneficiaryAddressDetails(any())).thenReturn(addr);
        List<Object[]> govBlock = new ArrayList<>();
        govBlock.add(new Object[]{3, 4, "Village"});
        when(eSanjeevaniRepo.getGovSubDistrictId(anyInt())).thenReturn(govBlock);
        // Contact
        // Removed unnecessary stubbing for getBeneficiaryContactDetails
        // Auth
        String tokenJson = "{\"model\":{\"access_token\":\"tok123\"}}";
        when(restTemplate.postForObject(anyString(), any(), eq(String.class))).thenReturn(tokenJson);
        // Register error
        @SuppressWarnings("unchecked")
        ResponseEntity<Object> respEntity = (ResponseEntity<Object>) mock(ResponseEntity.class);
        when(respEntity.getStatusCode()).thenReturn(org.springframework.http.HttpStatus.BAD_REQUEST);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Object.class))).thenReturn(respEntity);
        String result = service.registerPatient(1L);
        // The service returns this message for non-2xx response
        assertEquals("Empty response from E-sanjeevani Authorization API", result);
    }

    @Test
    public void testRegisterPatient_exception() {
        when(eSanjeevaniRepo.getBeneficiaryMappingIds(anyLong())).thenThrow(new RuntimeException("fail"));
        String result = null;
        try {
            result = service.registerPatient(1L);
        } catch (Exception e) {
            result = e.getMessage();
        }
        assertTrue(result.contains("Issue while fetching mapping details"));
    }

    @Test
    public void testCalculateAge() throws Exception {
        java.lang.reflect.Method m = ESanjeevaniServiceImpl.class.getDeclaredMethod("calculateAge", String.class);
        m.setAccessible(true);
        int age = (int) m.invoke(service, "2000-01-01");
        assertTrue(age >= 0);
    }

    @Test
    public void testSetBeneficiaryDetails_allBranches() throws Exception {
        // gender: male, female, other, null; dob present/missing
        ESanjeevaniPatientRegistration req = new ESanjeevaniPatientRegistration();
        // male
        List<Object[]> details = new ArrayList<>();
        details.add(new Object[]{"John", "Doe", "M", "male", "2000-01-01 00:00:00"});
        when(eSanjeevaniRepo.getBeneficiaryDeatils(any())).thenReturn(details);
        java.lang.reflect.Method m = ESanjeevaniServiceImpl.class.getDeclaredMethod("setBeneficiaryDetails", BigInteger.class, ESanjeevaniPatientRegistration.class);
        m.setAccessible(true);
        m.invoke(service, BigInteger.ONE, req);
        assertEquals("John", req.getFirstName());
        assertEquals("Doe", req.getLastName());
        assertEquals("M", req.getMiddleName());
        assertEquals("male", req.getGenderDisplay());
        assertEquals(1, req.getGenderCode());
        assertNotNull(req.getBirthdate());
        assertEquals("testSource", req.getSource());
        // female
        details.set(0, new Object[]{"Jane", "Doe", "F", "female", "2000-01-01 00:00:00"});
        m.invoke(service, BigInteger.ONE, req);
        assertEquals(2, req.getGenderCode());
        // other
        details.set(0, new Object[]{"Alex", "Doe", "X", "other", "2000-01-01 00:00:00"});
        m.invoke(service, BigInteger.ONE, req);
        assertEquals(3, req.getGenderCode());
        // null gender
        details.set(0, new Object[]{"Alex", "Doe", "X", null, "2000-01-01 00:00:00"});
        m.invoke(service, BigInteger.ONE, req);
        // null dob
        details.set(0, new Object[]{"Alex", "Doe", "X", "male", null});
        m.invoke(service, BigInteger.ONE, req);
    }

    @Test
    public void testSetBeneficiaryAddressDetails_allBranches() throws Exception {
        ArrayList<ESanjeevaniPatientAddress> list = new ArrayList<>();
        List<Object[]> addr = new ArrayList<>();
        addr.add(new Object[]{null, null, null, null, null, null, null, null, null, null});
        when(eSanjeevaniRepo.getBeneficiaryAddressDetails(any())).thenReturn(addr);
        when(eSanjeevaniRepo.getGovCountyId(anyInt())).thenReturn(null);
        when(eSanjeevaniRepo.getGovStateId(anyInt())).thenReturn(null);
        when(eSanjeevaniRepo.getGovDistrictId(anyInt())).thenReturn(null);
        List<Object[]> govBlock = new ArrayList<>();
        govBlock.add(new Object[]{null, null, "Village"}); // avoid NPE for objects[2].toString()
        when(eSanjeevaniRepo.getGovSubDistrictId(anyInt())).thenReturn(govBlock);
        java.lang.reflect.Method m = ESanjeevaniServiceImpl.class.getDeclaredMethod("setBeneficiaryAddressDetails", BigInteger.class, ArrayList.class);
        m.setAccessible(true);
        m.invoke(service, BigInteger.ONE, list);
        assertFalse(list.isEmpty());
    }

    @Test
    public void testSetBeneficiaryContactDetails_allBranches() throws Exception {
        ArrayList<ESanjeevaniPatientContactDetail> list = new ArrayList<>();
        when(eSanjeevaniRepo.getBeneficiaryContactDetails(any())).thenReturn(null);
        java.lang.reflect.Method m = ESanjeevaniServiceImpl.class.getDeclaredMethod("setBeneficiaryContactDetails", BigInteger.class, ArrayList.class);
        m.setAccessible(true);
        m.invoke(service, BigInteger.ONE, list);
        // now with value
        when(eSanjeevaniRepo.getBeneficiaryContactDetails(any())).thenReturn("9999999999");
        m.invoke(service, BigInteger.ONE, list);
        assertFalse(list.isEmpty());
    }
}
