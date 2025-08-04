package com.iemr.common.service.beneficiary;

import com.iemr.common.data.beneficiary.SexualOrientation;
import com.iemr.common.repository.userbeneficiarydata.SexualOrientationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SexualOrientationServiceImplTest {
    @Mock
    private SexualOrientationRepository sexualOrientationRepository;

    @InjectMocks
    private SexualOrientationServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSetDirectoryRepository() {
        SexualOrientationServiceImpl impl = new SexualOrientationServiceImpl();
        SexualOrientationRepository repo = mock(SexualOrientationRepository.class);
        impl.setDirectoryRepository(repo);
        // No exception, repository is set
    }

    @Test
    void testGetSexualOrientations_withValidData() {
        Set<Object[]> repoResult = new HashSet<>();
        repoResult.add(new Object[]{(short)1, "Test"});
        when(sexualOrientationRepository.findAciveOrientations()).thenReturn(repoResult);
        List<SexualOrientation> result = service.getSexualOrientations();
        assertEquals(1, result.size());
        assertEquals((short)1, result.get(0).getSexualOrientationId());
        assertEquals("Test", result.get(0).getSexualOrientation());
    }

    @Test
    void testGetSexualOrientations_withNullObject() {
        Set<Object[]> repoResult = new HashSet<>();
        repoResult.add(null);
        when(sexualOrientationRepository.findAciveOrientations()).thenReturn(repoResult);
        List<SexualOrientation> result = service.getSexualOrientations();
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetSexualOrientations_withInvalidLength() {
        Set<Object[]> repoResult = new HashSet<>();
        repoResult.add(new Object[]{(short)1}); // length != 2
        when(sexualOrientationRepository.findAciveOrientations()).thenReturn(repoResult);
        List<SexualOrientation> result = service.getSexualOrientations();
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetSexualOrientations_withEmptySet() {
        when(sexualOrientationRepository.findAciveOrientations()).thenReturn(Collections.emptySet());
        List<SexualOrientation> result = service.getSexualOrientations();
        assertTrue(result.isEmpty());
    }
}
