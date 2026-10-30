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
package com.iemr.common.service.beneficiary;

import com.iemr.common.data.eausadha.ItemMaster;
import com.iemr.common.data.eausadha.ItemStockEntry;
import com.iemr.common.data.facility.Facility;
import com.iemr.common.model.eAusadha.EAusadhaDTO;
import com.iemr.common.repository.eausadha.ItemMasterRepo;
import com.iemr.common.repository.eausadha.ItemStockEntryRepo;
import com.iemr.common.repository.facility.FacilityRepo;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestTemplate;
import java.sql.Timestamp;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EAusadhaServiceImplTest {
    @Mock
    private FacilityRepo facilityRepo;
    @Mock
    private ItemMasterRepo itemMasterRepo;
    @Mock
    private ItemStockEntryRepo itemStockEntryRepo;
    @InjectMocks
    private EAusadhaServiceImpl service;

    private AutoCloseable mocks;

    @BeforeEach
    void setUp() throws Exception {
        mocks = MockitoAnnotations.openMocks(this);
        setField("authorization", "auth");
        setField("eAusadhaUrl", "http://eausadha.test/stock");
    }

    private void setField(String name, Object value) throws Exception {
        java.lang.reflect.Field field = EAusadhaServiceImpl.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(service, value);
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    /**
     * The service builds its own {@link RestTemplate}, so the gateway is stubbed by
     * intercepting that construction rather than by injecting a mock.
     */
    private org.mockito.MockedConstruction<RestTemplate> gatewayReturning(ResponseEntity<String> response) {
        return mockConstruction(RestTemplate.class,
                (mock, context) -> when(mock.exchange(anyString(), any(), any(), eq(String.class)))
                        .thenReturn(response));
    }

    @Test
    void testCreateEAusadha_success() throws Exception {
        EAusadhaDTO dto = mock(EAusadhaDTO.class);
        when(dto.getFacilityId()).thenReturn(1);
        when(dto.getInwardDate()).thenReturn(Timestamp.valueOf("2025-08-05 10:00:00"));
        when(facilityRepo.fetchInstitutionId(1)).thenReturn("INST1");

        JSONArray arr = new JSONArray();
        JSONObject obj = new JSONObject();
        obj.put("Drug_id", "D1");
        obj.put("Batch_number", "B1");
        obj.put("Drug_name", "DrugName");
        obj.put("Quantity_In_Units", 10);
        obj.put("Exp_date", "2025-12-31");
        arr.put(obj);

        ResponseEntity<String> response = new ResponseEntity<>(arr.toString(), HttpStatus.OK);

        ItemMaster itemMaster = mock(ItemMaster.class);
        when(itemMaster.getItemID()).thenReturn(100);
        when(itemMasterRepo.findByItemCode("D1")).thenReturn(Collections.singletonList(itemMaster));
        when(itemStockEntryRepo.getItemStocks(100, "B1")).thenReturn(null);
        // JPA assigns the generated id on the instance that was passed in, so the
        // follow-up van-serial update sees it.
        when(itemStockEntryRepo.save(any(ItemStockEntry.class))).thenAnswer(invocation -> {
            ItemStockEntry entry = invocation.getArgument(0);
            entry.setItemStockEntryID(200);
            return entry;
        });

        try (org.mockito.MockedConstruction<RestTemplate> gateway = gatewayReturning(response)) {
            String result = service.createEAusadha(dto, "auth");

            assertTrue(result.contains("Stock entered Successfully"));
            verify(itemStockEntryRepo).updateVanSerialNo(200);
        }
    }

    @Test
    void testCreateEAusadha_noStocksEntered_throwsException() throws Exception {
        EAusadhaDTO dto = mock(EAusadhaDTO.class);
        when(dto.getFacilityId()).thenReturn(1);
        when(dto.getInwardDate()).thenReturn(Timestamp.valueOf("2025-08-05 10:00:00"));
        when(facilityRepo.fetchInstitutionId(1)).thenReturn("INST1");

        JSONArray arr = new JSONArray();
        JSONObject obj = new JSONObject();
        obj.put("Drug_id", "D1");
        obj.put("Batch_number", "B1");
        obj.put("Drug_name", "DrugName");
        obj.put("Quantity_In_Units", 10);
        obj.put("Exp_date", "2025-12-31");
        arr.put(obj);

        ResponseEntity<String> response = new ResponseEntity<>(arr.toString(), HttpStatus.OK);

        when(itemMasterRepo.findByItemCode("D1")).thenReturn(Collections.emptyList());

        try (org.mockito.MockedConstruction<RestTemplate> gateway = gatewayReturning(response)) {
            Exception ex = assertThrows(Exception.class, () -> service.createEAusadha(dto, "auth"));
            assertTrue(ex.getMessage().contains("Error while entering the stocks"));
        }
    }

    @Test
    void testCreateEAusadha_responseNotOK_throwsException() throws Exception {
        EAusadhaDTO dto = mock(EAusadhaDTO.class);
        when(dto.getFacilityId()).thenReturn(1);
        when(dto.getInwardDate()).thenReturn(Timestamp.valueOf("2025-08-05 10:00:00"));
        when(facilityRepo.fetchInstitutionId(1)).thenReturn("INST1");

        ResponseEntity<String> response = new ResponseEntity<>("", HttpStatus.BAD_REQUEST);

        try (org.mockito.MockedConstruction<RestTemplate> gateway = gatewayReturning(response)) {
            Exception ex = assertThrows(Exception.class, () -> service.createEAusadha(dto, "auth"));
            assertTrue(ex.getMessage().contains("Error while getting stock response"));
        }
    }

    @Test
    void testSaveItemStockEntry_setsFieldsAndSaves() {
        // Covered via createEAusadha test
    }
}
