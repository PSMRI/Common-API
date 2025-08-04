package com.iemr.common.service.location;

import com.iemr.common.data.location.CityDetails;
import com.iemr.common.data.location.Country;
import com.iemr.common.data.location.DistrictBlock;
import com.iemr.common.data.location.DistrictBranchMapping;
import com.iemr.common.data.location.Districts;
import com.iemr.common.data.location.States;
import com.iemr.common.repository.location.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LocationServiceImplTest {
    @InjectMocks
    LocationServiceImpl service;

    @Mock
    LocationStateRepository locationStateRepository;
    @Mock
    LocationDistrictRepository locationDistrictRepository;
    @Mock
    LocationDistrictBlockRepository locationDistrictBlockRepository;
    @Mock
    LocationCityRepository locationCityRepository;
    @Mock
    LocationDistrilctBranchRepository locationDistrilctBranchRepository;
    @Mock
    LocationCountryRepository locationCountryRepository;

    @Test
    public void testGetStates() {
        Object[] stateObj = new Object[]{1, "StateName"};
        ArrayList<Object[]> list = new ArrayList<>();
        list.add(stateObj);
        when(locationStateRepository.findBy(1)).thenReturn(list);
        List<States> result = service.getStates(1);
        assertEquals(1, result.size());
        assertEquals("StateName", result.get(0).getStateName());
    }

    @Test
    public void testGetStates_empty() {
        when(locationStateRepository.findBy(1)).thenReturn(new ArrayList<>());
        List<States> result = service.getStates(1);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetDistricts() {
        Object[] districtObj = new Object[]{2, "DistrictName"};
        ArrayList<Object[]> list = new ArrayList<>();
        list.add(districtObj);
        when(locationDistrictRepository.findBy(2)).thenReturn(list);
        List<Districts> result = service.getDistricts(2);
        assertEquals(1, result.size());
        assertEquals("DistrictName", result.get(0).getDistrictName());
    }

    @Test
    public void testGetDistricts_empty() {
        when(locationDistrictRepository.findBy(2)).thenReturn(new ArrayList<>());
        List<Districts> result = service.getDistricts(2);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetDistrictBlocks() {
        Object[] blockObj = new Object[]{3, "BlockName"};
        Set<Object[]> set = new HashSet<>();
        set.add(blockObj);
        when(locationDistrictBlockRepository.findBy(3)).thenReturn(set);
        List<DistrictBlock> result = service.getDistrictBlocks(3);
        assertEquals(1, result.size());
        assertEquals("BlockName", result.get(0).getBlockName());
    }

    @Test
    public void testGetDistrictBlocks_empty() {
        when(locationDistrictBlockRepository.findBy(3)).thenReturn(new HashSet<>());
        List<DistrictBlock> result = service.getDistrictBlocks(3);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testFindStateDistrictBy() {
        Object[] obj = new Object[]{4, "DistrictName", "StateName", 5};
        ArrayList<Object[]> list = new ArrayList<>();
        list.add(obj);
        when(locationDistrictRepository.findStateDistrictBy(4)).thenReturn(list);
        List<Districts> result = service.findStateDistrictBy(4);
        assertEquals(1, result.size());
        assertEquals("DistrictName", result.get(0).getDistrictName());
    }

    @Test
    public void testFindStateDistrictBy_empty() {
        when(locationDistrictRepository.findStateDistrictBy(4)).thenReturn(new ArrayList<>());
        List<Districts> result = service.findStateDistrictBy(4);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetCities() {
        Object[] cityObj = new Object[]{6, "CityName"};
        Set<Object[]> set = new HashSet<>();
        set.add(cityObj);
        when(locationDistrictBlockRepository.findBy(6)).thenReturn(set);
        List<CityDetails> result = service.getCities(6);
        assertEquals(1, result.size());
        assertEquals("CityName", result.get(0).getCityName());
    }

    @Test
    public void testGetCities_empty() {
        when(locationDistrictBlockRepository.findBy(6)).thenReturn(new HashSet<>());
        List<CityDetails> result = service.getCities(6);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetDistrilctBranchs() {
        Object[] branchObj = new Object[]{7, "BranchName", "A", "B", "C"};
        ArrayList<Object[]> list = new ArrayList<>();
        list.add(branchObj);
        when(locationDistrilctBranchRepository.findAllBy(7)).thenReturn(list);
        List<DistrictBranchMapping> result = service.getDistrilctBranchs(7);
        assertEquals(1, result.size());
        // Print all fields to find which contains 'BranchName'
        try {
            java.lang.reflect.Field[] fields = result.get(0).getClass().getDeclaredFields();
            boolean found = false;
            for (java.lang.reflect.Field field : fields) {
                field.setAccessible(true);
                Object value = field.get(result.get(0));
                System.out.println("Field: " + field.getName() + " = " + value);
                if ("BranchName".equals(value)) {
                    found = true;
                    break;
                }
            }
            assertTrue(found, "BranchName not found in any field");
        } catch (Exception e) {
            fail("Reflection failed: " + e.getMessage());
        }
    }

    @Test
    public void testGetDistrilctBranchs_empty() {
        when(locationDistrilctBranchRepository.findAllBy(7)).thenReturn(new ArrayList<>());
        List<DistrictBranchMapping> result = service.getDistrilctBranchs(7);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetCountries() {
        List<Country> countries = Arrays.asList(new Country());
        when(locationCountryRepository.findAll()).thenReturn(countries);
        List<Country> result = service.getCountries();
        assertEquals(1, result.size());
    }

    @Test
    public void testGetCountries_empty() {
        when(locationCountryRepository.findAll()).thenReturn(new ArrayList<>());
        List<Country> result = service.getCountries();
        assertTrue(result.isEmpty());
    }
}
