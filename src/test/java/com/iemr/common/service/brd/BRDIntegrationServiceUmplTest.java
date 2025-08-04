package com.iemr.common.service.brd;

import com.iemr.common.data.brd.BRDIntegrationData;
import com.iemr.common.repository.brd.BRDIntegrationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BRDIntegrationServiceUmplTest {

    @Mock
    private BRDIntegrationRepository repo;

    @InjectMocks
    private BRDIntegrationServiceUmpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

               @Test
    void getData_prescriptionIdNull_elseBranch_listAdd() {
        ArrayList<Object[]> repoData = new ArrayList<>();
        BRDIntegrationData data = mock(BRDIntegrationData.class);
        // This will trigger the 'else' branch: if (null != brdIntegrationData.getPrescriptionId()) ... else ...
        when(data.getPrescriptionId()).thenReturn(null);

        try (var mocked = org.mockito.Mockito.mockStatic(BRDIntegrationData.class)) {
            mocked.when(() -> BRDIntegrationData.getBRDDetails(any())).thenReturn(Arrays.asList(data));
            when(repo.getData(anyString(), anyString())).thenReturn(repoData);

            String result = service.getData("2020-01-01", "2020-01-02");
            assertNotNull(result);
            assertTrue(result.contains("["));
        }
    }

    @Test
    void getData_emptyData_returnsEmptyJsonArray() {
        when(repo.getData(anyString(), anyString())).thenReturn(new ArrayList<>());
        try (var mocked = org.mockito.Mockito.mockStatic(BRDIntegrationData.class)) {
            mocked.when(() -> BRDIntegrationData.getBRDDetails(any())).thenReturn(new ArrayList<>());
            String result = service.getData("2020-01-01", "2020-01-02");
            assertNotNull(result);
            assertTrue(result.contains("["));
        }
    }

    @Test
    void getData_nullPrescriptionId_goesToList() {
        ArrayList<Object[]> repoData = new ArrayList<>();
        BRDIntegrationData data = mock(BRDIntegrationData.class);
        when(data.getPrescriptionId()).thenReturn(null);

        try (var mocked = org.mockito.Mockito.mockStatic(BRDIntegrationData.class)) {
            mocked.when(() -> BRDIntegrationData.getBRDDetails(any())).thenReturn(Arrays.asList(data));
            when(repo.getData(anyString(), anyString())).thenReturn(repoData);

            String result = service.getData("2020-01-01", "2020-01-02");
            assertNotNull(result);
            assertTrue(result.contains("["));
        }
    }

    @Test
    void getData_duplicatePrescriptionId_concatsDrugName() {
        ArrayList<Object[]> repoData = new ArrayList<>();
        BRDIntegrationData data1 = mock(BRDIntegrationData.class);
        BRDIntegrationData data2 = mock(BRDIntegrationData.class);
        BigInteger pid = BigInteger.valueOf(1);

        when(data1.getPrescriptionId()).thenReturn(pid);
        when(data2.getPrescriptionId()).thenReturn(pid);
        when(data1.getDrugName()).thenReturn("DrugA");
        when(data2.getDrugName()).thenReturn("DrugB");

        try (var mocked = org.mockito.Mockito.mockStatic(BRDIntegrationData.class)) {
            mocked.when(() -> BRDIntegrationData.getBRDDetails(any())).thenReturn(Arrays.asList(data1, data2));
            when(repo.getData(anyString(), anyString())).thenReturn(repoData);

            String result = service.getData("2020-01-01", "2020-01-02");
            assertNotNull(result);
            assertTrue(result.contains("["));
            verify(data1, atLeastOnce()).setDrugName(contains(","));
        }
    }

    @Test
    void getData_uniquePrescriptionId_goesToMap() {
        ArrayList<Object[]> repoData = new ArrayList<>();
        BRDIntegrationData data = mock(BRDIntegrationData.class);
        BigInteger pid = BigInteger.valueOf(2);
        when(data.getPrescriptionId()).thenReturn(pid);

        try (var mocked = org.mockito.Mockito.mockStatic(BRDIntegrationData.class)) {
            mocked.when(() -> BRDIntegrationData.getBRDDetails(any())).thenReturn(Arrays.asList(data));
            when(repo.getData(anyString(), anyString())).thenReturn(repoData);

            String result = service.getData("2020-01-01", "2020-01-02");
            assertNotNull(result);
            assertTrue(result.contains("["));
        }
    }
    @Test
    void getData_prescriptionIdNotInMap_goesToMap() {
        ArrayList<Object[]> repoData = new ArrayList<>();
        BRDIntegrationData data = mock(BRDIntegrationData.class);
        BigInteger pid = BigInteger.valueOf(123);
        when(data.getPrescriptionId()).thenReturn(pid);

        try (var mocked = org.mockito.Mockito.mockStatic(BRDIntegrationData.class)) {
            mocked.when(() -> BRDIntegrationData.getBRDDetails(any())).thenReturn(Arrays.asList(data));
            when(repo.getData(anyString(), anyString())).thenReturn(repoData);

            String result = service.getData("2020-01-01", "2020-01-02");
            assertNotNull(result);
            assertTrue(result.contains("["));
            // The test will pass if no exception is thrown and result is as expected
        }
    }


}
