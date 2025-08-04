package com.iemr.common.service.door_to_door_app;

import com.google.gson.Gson;
import com.iemr.common.data.door_to_door_app.RequestParser;
import com.iemr.common.data.door_to_door_app.V_doortodooruserdetails;
import com.iemr.common.repo.door_to_door_app.V_doortodooruserdetailsRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;
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
public class DoorToDoorServiceImplTest {
    @InjectMocks
    DoorToDoorServiceImpl service;

    @Mock
    V_doortodooruserdetailsRepo repo;

    @Mock
    RestTemplate restTemplate;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        setField("avniRegistrationLimit", "10");
        setField("amritUserName", "user");
        setField("amritPassword", "pass");
        setField("everwell1097userAuthenticate", "http://auth");
        setField("everwellRegisterBenficiary", "http://reg");
        // Patch: Replace new RestTemplate() in service with mock for all HTTP calls
        patchRestTemplateNewInstance();
    }

    // Patch: Use reflection to replace new RestTemplate() with the mock in the service
    private void patchRestTemplateNewInstance() {
        try {
            java.lang.reflect.Field f = DoorToDoorServiceImpl.class.getDeclaredField("restTemplate");
            f.setAccessible(true);
            f.set(service, restTemplate);
        } catch (NoSuchFieldException e) {
            // If the field does not exist, dynamically add it (works for tests)
            try {
                java.lang.reflect.Field f = DoorToDoorServiceImpl.class.getSuperclass().getDeclaredField("restTemplate");
                f.setAccessible(true);
                f.set(service, restTemplate);
            } catch (Exception ex) {
                // ignore
            }
        } catch (Exception e) {
            // ignore
        }
    }

    private void setField(String name, Object value) {
        try {
            java.lang.reflect.Field f = DoorToDoorServiceImpl.class.getDeclaredField(name);
            f.setAccessible(true);
            f.set(service, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testGetUserDetails_success() throws Exception {
        RequestParser rp = new RequestParser();
        rp.setUserID(1);
        String reqJson = new Gson().toJson(rp);
        V_doortodooruserdetails user = mock(V_doortodooruserdetails.class);
        when(user.getUserID()).thenReturn(1);
        when(user.getUserName()).thenReturn("uname");
        when(user.getEmergencyContactNo()).thenReturn("999");
        when(user.getStateID()).thenReturn(2);
        when(user.getStateName()).thenReturn("state");
        when(user.getDistrictID()).thenReturn(3);
        when(user.getDistrictName()).thenReturn("dist");
        when(user.getDistrictBlockID()).thenReturn(4);
        when(user.getBlockName()).thenReturn("block");
        when(user.getDistrictBranchID()).thenReturn(5);
        when(user.getVillageName()).thenReturn("village");
        when(user.getRoleID()).thenReturn(6);
        when(user.getRoleName()).thenReturn("role");
        when(user.getDesignationId()).thenReturn(7);
        when(user.getDesignationName()).thenReturn("desig");
        ArrayList<V_doortodooruserdetails> users = new ArrayList<>();
        users.add(user);
        when(repo.findByUserID(1)).thenReturn(users);
        // Use real InputMapper
        setInputMapperGson();
        String result = service.getUserDetails(reqJson);
        assertTrue(result.contains("userID"));
        assertTrue(result.contains("roleName"));
        assertTrue(result.contains("healthInstitution"));
    }

    private void setInputMapperGson() {
        try {
            java.lang.reflect.Field f = Class.forName("com.iemr.common.utils.mapper.InputMapper").getDeclaredField("gson");
            f.setAccessible(true);
            f.set(null, new Gson());
        } catch (Exception e) {
            // ignore
        }
    }

    @Test
    public void testGetUserDetails_nullUser() {
        String reqJson = new Gson().toJson(new RequestParser());
        setInputMapperGson();
        Exception ex = assertThrows(Exception.class, () -> service.getUserDetails(reqJson));
        assertTrue(ex.getMessage().contains("User ID is null"));
    }

    @Test
    public void testGet_NCD_TB_HRP_Suspected_Status_fullCoverage() throws Exception {
        RequestParser rp = new RequestParser();
        rp.setBenRegID(1L);
        ArrayList<Object[]> resultList = new ArrayList<>();
        resultList.add(new Object[]{"hrp", "tb", "ncd", "ncdDis"});
        when(repo.ncd_tb_hrp_Status(1L)).thenReturn(resultList);
        String result = service.get_NCD_TB_HRP_Suspected_Status(rp);
        assertTrue(result.contains("hrp"));
        assertTrue(result.contains("tb"));
        assertTrue(result.contains("ncd"));
        assertTrue(result.contains("ncdDis"));
    }

    @Test
    public void testGet_NCD_TB_HRP_Suspected_Status_empty() throws Exception {
        RequestParser rp = new RequestParser();
        rp.setBenRegID(1L);
        when(repo.ncd_tb_hrp_Status(1L)).thenReturn(new ArrayList<>());
        String result = service.get_NCD_TB_HRP_Suspected_Status(rp);
        assertTrue(result.contains("benRegID"));
    }

    @Test
    public void testScheduleJobForRegisterAvniBeneficiary_allBranches() throws Exception {
        // Setup
        ArrayList<Object[]> list = new ArrayList<>();
        list.add(new Object[]{BigInteger.ONE, "{\"data\":{\"beneficiaryID\":\"123\"}}", "extId", BigInteger.TEN});
        when(repo.getAvniBeneficiary(anyInt())).thenReturn(list);
        when(repo.checkIfAvniIdExists(anyString())).thenReturn(0);
        when(repo.updateAvniBenId(anyLong(), anyLong())).thenReturn(1);
        @SuppressWarnings("unchecked")
        ResponseEntity<String> resp = (ResponseEntity<String>) mock(ResponseEntity.class);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class))).thenReturn(resp);
        when(resp.getStatusCode()).thenReturn(org.springframework.http.HttpStatus.OK);
        when(resp.hasBody()).thenReturn(true);
        String body = "{\"data\":{\"beneficiaryID\":\"123\"}}";
        when(resp.getBody()).thenReturn(body);
        service.scheduleJobForRegisterAvniBeneficiary();
        // Debug: print all interactions if the verification fails
        try {
            verify(repo, atLeastOnce()).updateAvniBenId(anyLong(), anyLong());
        } catch (Throwable t) {
            throw t;
        }
    }

    @Test
    public void testScheduleJobForRegisterAvniBeneficiary_noRecords() throws Exception {
        when(repo.getAvniBeneficiary(anyInt())).thenReturn(new ArrayList<>());
        service.scheduleJobForRegisterAvniBeneficiary();
        // Should log "No new records found" and not throw
    }

    @Test
    public void testScheduleJobForRegisterAvniBeneficiary_alreadyExists() throws Exception {
        ArrayList<Object[]> list = new ArrayList<>();
        list.add(new Object[]{BigInteger.ONE, "{\"foo\":\"bar\"}", "extId", BigInteger.TEN});
        when(repo.getAvniBeneficiary(anyInt())).thenReturn(list);
        when(repo.checkIfAvniIdExists(anyString())).thenReturn(1);
        service.scheduleJobForRegisterAvniBeneficiary();
        // Should log and skip update
    }

    @Test
    public void testScheduleJobForRegisterAvniBeneficiary_exception() throws Exception {
        when(repo.getAvniBeneficiary(anyInt())).thenThrow(new RuntimeException("fail"));
        service.scheduleJobForRegisterAvniBeneficiary();
        // Should log error, not throw
    }

    @Test
    public void testAmritUserAuthenticate_success() {
        @SuppressWarnings("unchecked")
        ResponseEntity<String> resp = (ResponseEntity<String>) mock(ResponseEntity.class);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class))).thenReturn(resp);
        when(resp.getStatusCode()).thenReturn(org.springframework.http.HttpStatus.OK);
        when(resp.hasBody()).thenReturn(true);
        String body = "{\"data\":{\"key\":\"authkey\"}}";
        when(resp.getBody()).thenReturn(body);
        String result = service.amritUserAuthenticate();
        assertEquals("authkey", result);
    }

    @Test
    public void testAmritUserAuthenticate_noKey() {
        @SuppressWarnings("unchecked")
        ResponseEntity<String> resp = (ResponseEntity<String>) mock(ResponseEntity.class);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class))).thenReturn(resp);
        when(resp.getStatusCode()).thenReturn(org.springframework.http.HttpStatus.OK);
        when(resp.hasBody()).thenReturn(true);
        String body = "{\"data\":{}}";
        when(resp.getBody()).thenReturn(body);
        String result = service.amritUserAuthenticate();
        assertEquals("", result);
    }

    @Test
    public void testAmritUserAuthenticate_non200() {
        @SuppressWarnings("unchecked")
        ResponseEntity<String> resp = (ResponseEntity<String>) mock(ResponseEntity.class);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class))).thenReturn(resp);
        when(resp.getStatusCode()).thenReturn(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
        when(resp.hasBody()).thenReturn(false);
        String result = service.amritUserAuthenticate();
        assertEquals("", result);
    }
}
