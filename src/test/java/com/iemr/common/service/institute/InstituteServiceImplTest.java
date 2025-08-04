package com.iemr.common.service.institute;

import com.iemr.common.data.institute.Institute;
import com.iemr.common.repository.institute.InstituteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InstituteServiceImplTest {
    @InjectMocks
    InstituteServiceImpl instituteService;

    @Mock
    InstituteRepository instituteRepository;
    @Mock
    Logger logger;

    @BeforeEach
    public void setup() {
        // Inject a mock logger if needed
        ReflectionTestUtils.setField(instituteService, "logger", logger);
    }

    @Test
    public void testGetInstitutesByStateDistrictBranch_withResults() {
        Set<Object[]> repoResult = new HashSet<>();
        repoResult.add(new Object[]{1, "Inst1"});
        repoResult.add(new Object[]{2, "Inst2"});
        when(instituteRepository.findAciveInstitutesByStateDistBlockID(1, 2, 3)).thenReturn(repoResult);
        List<Institute> result = instituteService.getInstitutesByStateDistrictBranch(1, 2, 3);
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
        verify(logger).info(contains("response size"));
    }

    @Test
    public void testGetInstitutesByStateDistrictBranch_empty() {
        when(instituteRepository.findAciveInstitutesByStateDistBlockID(1, 2, 3)).thenReturn(Collections.emptySet());
        List<Institute> result = instituteService.getInstitutesByStateDistrictBranch(1, 2, 3);
        assertTrue(result.isEmpty());
        verify(logger).info(contains("No Beneficiary Found"));
    }

    @Test
    public void testGetInstitutesByStateDistrictBranch_nullObject() {
        Set<Object[]> repoResult = new HashSet<>();
        repoResult.add(null);
        repoResult.add(new Object[]{});
        repoResult.add(new Object[]{1}); // length != 2
        when(instituteRepository.findAciveInstitutesByStateDistBlockID(1, 2, 3)).thenReturn(repoResult);
        List<Institute> result = instituteService.getInstitutesByStateDistrictBranch(1, 2, 3);
        assertTrue(result.isEmpty());
        verify(logger).info(contains("No Beneficiary Found"));
    }

    @Test
    public void testGetInstitutesByBranch_withResults() {
        Set<Object[]> repoResult = new HashSet<>();
        repoResult.add(new Object[]{10, "Branch1"});
        when(instituteRepository.findAciveInstitutesByBranchID(5)).thenReturn(repoResult);
        List<Institute> result = instituteService.getInstitutesByBranch(5);
        assertEquals(1, result.size());
        assertEquals(10, result.get(0).getInstitutionID());
        assertEquals("Branch1", result.get(0).getInstitutionName());
        verify(logger).info(contains("response size"));
    }

    @Test
    public void testGetInstitutesByBranch_empty() {
        when(instituteRepository.findAciveInstitutesByBranchID(5)).thenReturn(Collections.emptySet());
        List<Institute> result = instituteService.getInstitutesByBranch(5);
        assertTrue(result.isEmpty());
        verify(logger).info(contains("No Beneficiary Found"));
    }

    @Test
    public void testGetInstitutesByBranch_nullObject() {
        Set<Object[]> repoResult = new HashSet<>();
        repoResult.add(null);
        repoResult.add(new Object[]{});
        repoResult.add(new Object[]{1}); // length != 2
        when(instituteRepository.findAciveInstitutesByBranchID(5)).thenReturn(repoResult);
        List<Institute> result = instituteService.getInstitutesByBranch(5);
        assertTrue(result.isEmpty());
        verify(logger).info(contains("No Beneficiary Found"));
    }
}
