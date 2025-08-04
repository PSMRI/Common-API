package com.iemr.common.service.institute;

import com.iemr.common.data.institute.Designation;
import com.iemr.common.repository.institute.DesignationRepository;
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
public class DesignationServiceImplTest {
    @InjectMocks
    DesignationServiceImpl service;

    @Mock
    DesignationRepository designationRepository;
    @Mock
    Logger logger;

    @BeforeEach
    public void setup() {
        ReflectionTestUtils.setField(service, "logger", logger);
    }

    @Test
    public void testGetDesignations_withResults() {
        List<Designation> mockList = Arrays.asList(mock(Designation.class), mock(Designation.class));
        when(designationRepository.findAciveDesignations()).thenReturn(mockList);
        List<Designation> result = service.getDesignations();
        assertEquals(2, result.size());
        verify(designationRepository).findAciveDesignations();
        verify(logger).info(contains("getDesignations returning "));
    }

    @Test
    public void testGetDesignations_empty() {
        when(designationRepository.findAciveDesignations()).thenReturn(Collections.emptyList());
        List<Designation> result = service.getDesignations();
        assertTrue(result.isEmpty());
        verify(designationRepository).findAciveDesignations();
        verify(logger).info(contains("getDesignations returning "));
    }
}
