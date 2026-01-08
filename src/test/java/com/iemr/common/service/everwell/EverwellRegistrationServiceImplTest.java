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
package com.iemr.common.service.everwell;

import com.google.gson.JsonObject;
import com.iemr.common.data.beneficiary.BenPhoneMap;
import com.iemr.common.data.everwell.EverwellDetails;
import com.iemr.common.data.everwell.EverwellRegistration1097Identity;
import com.iemr.common.model.beneficiary.BeneficiaryDemographicsModel;
import com.iemr.common.model.user.LoginRequestModel;
import com.iemr.common.repository.everwell.EverwellFetchAndSync;
import com.iemr.common.repository.location.LocationDistrictRepository;
import com.iemr.common.repository.location.LocationStateRepository;
import com.iemr.common.utils.CryptoUtil;
import com.iemr.common.utils.RestTemplateUtil;
import com.iemr.common.utils.mapper.InputMapper;
import com.iemr.common.utils.response.OutputResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EverwellRegistrationServiceImplTest {
    @InjectMocks
    EverwellRegistrationServiceImpl service;

    @Mock
    CryptoUtil cryptoUtil;
    @Mock
    EverwellFetchAndSync everwellFetchAndSync;
    @Mock
    LocationDistrictRepository locationDistrictRepository;
    @Mock
    LocationStateRepository locationStateRepository;
    @Mock
    Logger logger;

    @BeforeEach
    void setUp() {
        // Set required @Value fields via reflection
        setField("everwellGetPatientAdherenceUrl", "http://test/adherence/");
        setField("everwellEditDoses", "editDoses");
        setField("everwellAddSupportAction", "addSupport");
        setField("everwelluserAuthenticate", "http://test/auth");
        setField("everwellRegisterBenficiary", "http://test/register");
        setField("everwellVanID", 1);
        setField("everwellProviderServiceMapID", 2);
        setField("everwellgovtIdentityNo", 3);
        setField("everwellgovtIdentityTypeID", 4);
        setField("everwellmaritalStatusID", 5);
        setField("everwellbenRelationshipID", 6);
        setField("everwell1097userAuthenticate", "http://test/1097auth");
        setField("everwellUserName", "user");
        setField("everwellPassword", "pass");
        setField("amritUserName", "amrituser");
        setField("amritPassword", "amritpass");
    }

    private void setField(String name, Object value) {
        try {
            java.lang.reflect.Field f = EverwellRegistrationServiceImpl.class.getDeclaredField(name);
            f.setAccessible(true);
            f.set(service, value);
        } catch (Exception ignored) {}
    }

    @Test
    void testRegisterBeneficiary_success() {
        // Mock RestTemplate and responses
        RestTemplate restTemplateLogin = mock(RestTemplate.class);
        RestTemplate restTemplateEverwellLogin = mock(RestTemplate.class);
        // Mock static RestTemplate creation
        try (MockedConstruction<RestTemplate> restTemplateMocked = mockConstruction(RestTemplate.class,
                (mock, context) -> {
                    if (context.arguments().isEmpty()) {
                        if (context.getCount() == 1) {
                            when(mock.exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class)))
                                    .thenReturn(new ResponseEntity<>(getLoginResponse(), HttpStatus.OK));
                        } else if (context.getCount() == 2) {
                            when(mock.exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class)))
                                    .thenReturn(new ResponseEntity<>(getEverwellAuthResponse(), HttpStatus.OK));
                        }
                    }
                })) {
            // Mock everWellDataSave
            EverwellDetails details = new EverwellDetails();
            List<EverwellDetails> detailsList = Collections.singletonList(details);
            EverwellRegistrationServiceImpl spyService = Mockito.spy(service);
            doReturn(detailsList).when(spyService).everWellDataSave(anyString(), anyString(), anyString());
            spyService.registerBeneficiary();
            verify(spyService, atLeastOnce()).everWellDataSave(anyString(), anyString(), anyString());
        }
    }

    @Test
    void testRegisterBeneficiary_exception() {
        EverwellRegistrationServiceImpl spyService = Mockito.spy(service);
        assertDoesNotThrow(spyService::registerBeneficiary);
    }

    @Test
    void testEverWellDataSave_successAndNoRecords() {
        // Mock RestTemplate
        try (MockedConstruction<RestTemplate> restTemplateMocked = mockConstruction(RestTemplate.class,
                (mock, context) -> {
                    when(mock.exchange(contains("adherence/0"), eq(HttpMethod.GET), any(), eq(String.class)))
                            .thenReturn(new ResponseEntity<>(getAdherenceResponse(false), HttpStatus.OK));
                })) {
            EverwellRegistrationServiceImpl spyService = Mockito.spy(service);
            List<EverwellDetails> result = spyService.everWellDataSave("auth", "user", "auth2");
            assertNull(result);
        }
    }

    @Test
    void testEverWellDataSave_exception() {
        try (MockedConstruction<RestTemplate> restTemplateMocked = mockConstruction(RestTemplate.class,
                (mock, context) -> {
                    when(mock.exchange(anyString(), eq(HttpMethod.GET), any(), eq(String.class)))
                            .thenThrow(new RuntimeException("fail"));
                })) {
            EverwellRegistrationServiceImpl spyService = Mockito.spy(service);
            List<EverwellDetails> result = spyService.everWellDataSave("auth", "user", "auth2");
            assertNull(result);
        }
    }

    @Test
    void testRegisterEverWellPatient_alreadyRegistered() {
        EverwellDetails details = new EverwellDetails();
        details.setId(0L);
        ArrayList<EverwellDetails> list = new ArrayList<>();
        list.add(details);
        ArrayList<Object[]> regStatus = new ArrayList<>();
        regStatus.add(new Object[]{1, true, 1L, 2, 3, 4L});
        when(everwellFetchAndSync.registrationStatus(any())).thenReturn(regStatus);
        when(everwellFetchAndSync.saveAll(anyList())).thenReturn(list);
        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        String result = service.registerEverWellPatient(list, "auth", mockRestTemplate);
        assertEquals("Success", result);
    }

    @Test
    void testRegisterEverWellPatient_newRegistration_success() {
        EverwellDetails details = new EverwellDetails();
        details.setId(0L);
        details.setGender("male");
        details.setState("state");
        details.setDistrict("district");
        details.setFirstName("f");
        details.setLastName("l");
        details.setCreatedBy("user");
        ArrayList<EverwellDetails> list = new ArrayList<>();
        list.add(details);
        ArrayList<Object[]> regStatus = new ArrayList<>();
        regStatus.add(new Object[]{0, false});
        when(everwellFetchAndSync.registrationStatus(any())).thenReturn(regStatus);
        when(locationStateRepository.getStateID(anyString())).thenReturn(1);
        when(locationDistrictRepository.getDistrictID(anyString())).thenReturn(2);
        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        when(everwellFetchAndSync.saveAll(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));
        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        when(mockRestTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class)))
            .thenReturn(new ResponseEntity<>(getRegisterResponse(), HttpStatus.OK));
        String result = service.registerEverWellPatient(list, "auth", mockRestTemplate);
        assertEquals("Success", result);
        // Get the updated object from the captured argument
        EverwellDetails updated = (EverwellDetails) captor.getValue().get(0);
        System.out.println("BeneficiaryRegId: " + updated.getBeneficiaryRegId());
        System.out.println("IsRegistered: " + updated.getIsRegistered());
        // Assert all fields set by the registration response
        assertEquals(1L, updated.getBeneficiaryRegId());
        assertEquals(2, updated.getVanId());
        assertEquals(3, updated.getProviderServiceMapId());
        assertEquals(4L, updated.getBeneficiaryID());
        assertTrue(updated.getIsRegistered());
    }

    @Test
    void testRegisterEverWellPatient_newRegistration_exception() {
        EverwellDetails details = new EverwellDetails();
        details.setId(0L);
        details.setGender("male");
        details.setState("state");
        details.setDistrict("district");
        details.setFirstName("f");
        details.setLastName("l");
        details.setCreatedBy("user");
        ArrayList<EverwellDetails> list = new ArrayList<>();
        list.add(details);
        ArrayList<Object[]> regStatus = new ArrayList<>();
        regStatus.add(new Object[]{0, false});
        when(everwellFetchAndSync.registrationStatus(any())).thenReturn(regStatus);
        when(locationStateRepository.getStateID(anyString())).thenReturn(1);
        when(locationDistrictRepository.getDistrictID(anyString())).thenReturn(2);
        // Make saveAll return null to simulate failure in registration (so the code returns "Failure")
        when(everwellFetchAndSync.saveAll(anyList())).thenReturn(null);
        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        String result = service.registerEverWellPatient(list, "auth", mockRestTemplate);
        assertEquals("Failure", result);
    }

    // --- Helper methods for JSON responses ---
    private String getLoginResponse() {
        return "{\"data\":{\"key\":\"authkey\",\"userName\":\"user\"}}";
    }

    private String getEverwellAuthResponse() {
        return "{\"token_type\":\"Bearer\",\"access_token\":\"token\"}";
    }

    private String getAdherenceResponse(boolean hasRecords) {
        if (hasRecords) {
            return "{\"TotalRecords\":1,\"Data\":[{\"state\":\"state\",\"district\":\"district\",\"gender\":\"male\",\"firstName\":\"f\",\"lastName\":\"l\",\"createdBy\":\"user\",\"NoInfoDoseDates\":[]}] }";
        } else {
            return "{\"TotalRecords\":0}";
        }
    }

    private String getRegisterResponse() {
        // Try with numbers instead of strings if parsing fails
        return "{\"data\":{\"beneficiaryRegID\":1,\"vanID\":2,\"providerServiceMapID\":3,\"beneficiaryID\":4}}";
    }
}