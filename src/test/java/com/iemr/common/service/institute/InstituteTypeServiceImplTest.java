package com.iemr.common.service.institute;

import com.iemr.common.data.institute.Institute;
import com.iemr.common.data.institute.InstituteType;
import com.iemr.common.repository.institute.InstituteRepository;
import com.iemr.common.repository.institute.InstituteTypeRepository;
import com.iemr.common.utils.mapper.InputMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InstituteTypeServiceImplTest {
    @InjectMocks
    InstituteTypeServiceImpl service;

    @Mock
    InstituteTypeRepository instituteTypeRepository;
    @Mock
    InstituteRepository instituteRepository;

    @BeforeEach
    public void setup() {
        // No need to set inputMapper, use real static method
    }

    @Test
    public void testGetInstitutionTypes_withProviderServiceMapID() throws Exception {
        String json = "{\"providerServiceMapID\":123}";
        List<InstituteType> expected = Arrays.asList(mock(InstituteType.class));
        when(instituteTypeRepository.findAciveInstitutesTypes(123)).thenReturn(expected);
        List<InstituteType> result = service.getInstitutionTypes(json);
        assertEquals(expected, result);
    }

    @Test
    public void testGetInstitutionTypes_withoutProviderServiceMapID() throws Exception {
        String json = "{}";
        List<InstituteType> expected = Arrays.asList(mock(InstituteType.class));
        when(instituteTypeRepository.findAciveInstitutesTypes()).thenReturn(expected);
        List<InstituteType> result = service.getInstitutionTypes(json);
        assertEquals(expected, result);
    }

    @Test
    public void testGetInstitutionName_withResults() throws Exception {
        ArrayList<Object[]> repoResult = new ArrayList<>();
        repoResult.add(new Object[]{1, "Inst1"});
        repoResult.add(new Object[]{2, "Inst2"});
        when(instituteRepository.getInstitutionNameByType(5)).thenReturn(repoResult);
        List<Institute> result = service.getInstitutionName(5);
        assertEquals(2, result.size());
        Set<Integer> ids = new HashSet<>();
        Set<String> names = new HashSet<>();
        for (Institute inst : result) {
            ids.add(inst.getInstitutionID());
            names.add(inst.getInstitutionName());
        }
        assertTrue(ids.contains(1));
        assertTrue(ids.contains(2));
        assertTrue(names.contains("Inst1"));
        assertTrue(names.contains("Inst2"));
    }

    @Test
    public void testGetInstitutionName_empty() throws Exception {
        when(instituteRepository.getInstitutionNameByType(5)).thenReturn(new ArrayList<>());
        List<Institute> result = service.getInstitutionName(5);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetInstitutionName_nullObject() throws Exception {
        ArrayList<Object[]> repoResult = new ArrayList<>();
        repoResult.add(null);
        repoResult.add(new Object[]{});
        repoResult.add(new Object[]{1}); // length != 2
        when(instituteRepository.getInstitutionNameByType(5)).thenReturn(repoResult);
        List<Institute> result = service.getInstitutionName(5);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetInstitutionNameByTypeAndDistrict_withResults() throws Exception {
        ArrayList<Object[]> repoResult = new ArrayList<>();
        repoResult.add(new Object[]{10, "Branch1"});
        when(instituteRepository.getInstitutionNameByTypeAndDistrict(2, 3)).thenReturn(repoResult);
        List<Institute> result = service.getInstitutionNameByTypeAndDistrict(2, 3);
        assertEquals(1, result.size());
        assertEquals(10, result.get(0).getInstitutionID());
        assertEquals("Branch1", result.get(0).getInstitutionName());
    }

    @Test
    public void testGetInstitutionNameByTypeAndDistrict_empty() throws Exception {
        when(instituteRepository.getInstitutionNameByTypeAndDistrict(2, 3)).thenReturn(new ArrayList<>());
        List<Institute> result = service.getInstitutionNameByTypeAndDistrict(2, 3);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetInstitutionNameByTypeAndDistrict_nullObject() throws Exception {
        ArrayList<Object[]> repoResult = new ArrayList<>();
        repoResult.add(null);
        repoResult.add(new Object[]{});
        repoResult.add(new Object[]{1}); // length != 2
        when(instituteRepository.getInstitutionNameByTypeAndDistrict(2, 3)).thenReturn(repoResult);
        List<Institute> result = service.getInstitutionNameByTypeAndDistrict(2, 3);
        assertTrue(result.isEmpty());
    }
}
