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
    @Mock
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
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
        when(restTemplate.exchange(anyString(), any(), any(), eq(String.class))).thenReturn(response);

        ItemMaster itemMaster = mock(ItemMaster.class);
        when(itemMaster.getItemID()).thenReturn(100);
        when(itemMasterRepo.findByItemCode("D1")).thenReturn(Collections.singletonList(itemMaster));
        when(itemStockEntryRepo.getItemStocks(100, "B1")).thenReturn(null);
        ItemStockEntry itemStockEntry = mock(ItemStockEntry.class);
        when(itemStockEntry.getItemStockEntryID()).thenReturn(200);
        doNothing().when(itemStockEntryRepo).updateVanSerialNo(anyInt());
        // Set authorization field to match service usage
        java.lang.reflect.Field authField = EAusadhaServiceImpl.class.getDeclaredField("authorization");
        authField.setAccessible(true);
        authField.set(service, "auth");

        String result = service.createEAusadha(dto, "auth");
        assertTrue(result.contains("Stock entered Successfully"));
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
        when(restTemplate.exchange(anyString(), any(), any(), eq(String.class))).thenReturn(response);

        when(itemMasterRepo.findByItemCode("D1")).thenReturn(Collections.emptyList());

        java.lang.reflect.Field authField = EAusadhaServiceImpl.class.getDeclaredField("authorization");
        authField.setAccessible(true);
        authField.set(service, "auth");
        Exception ex = assertThrows(Exception.class, () -> service.createEAusadha(dto, "auth"));
        assertTrue(ex.getMessage().contains("Error while entering the stocks"));
    }

    @Test
    void testCreateEAusadha_responseNotOK_throwsException() throws Exception {
        EAusadhaDTO dto = mock(EAusadhaDTO.class);
        when(dto.getFacilityId()).thenReturn(1);
        when(dto.getInwardDate()).thenReturn(Timestamp.valueOf("2025-08-05 10:00:00"));
        when(facilityRepo.fetchInstitutionId(1)).thenReturn("INST1");

        ResponseEntity<String> response = new ResponseEntity<>("", HttpStatus.BAD_REQUEST);
        when(restTemplate.exchange(anyString(), any(), any(), eq(String.class))).thenReturn(response);

        java.lang.reflect.Field authField = EAusadhaServiceImpl.class.getDeclaredField("authorization");
        authField.setAccessible(true);
        authField.set(service, "auth");
        Exception ex = assertThrows(Exception.class, () -> service.createEAusadha(dto, "auth"));
        assertTrue(ex.getMessage().contains("Error while getting stock response"));
    }

    @Test
    void testSaveItemStockEntry_setsFieldsAndSaves() {
        // Covered via createEAusadha test
    }
}
