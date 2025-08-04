package com.iemr.common.service.beneficiary;

import com.iemr.common.data.beneficiary.BeneficiaryOccupation;
import com.iemr.common.repository.beneficiary.BeneficiaryOccupationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BeneficiaryOccupationServiceImplTest {
    @Mock
    private BeneficiaryOccupationRepository beneficiaryOccupationRepository;

    @InjectMocks
    private BeneficiaryOccupationServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSetBeneficiaryOccupationRepository() {
        BeneficiaryOccupationServiceImpl impl = new BeneficiaryOccupationServiceImpl();
        BeneficiaryOccupationRepository repo = mock(BeneficiaryOccupationRepository.class);
        impl.setBeneficiaryOccupationRepository(repo);
        // No exception, repository is set
    }

    @Test
    void testGetActiveOccupations_withValidData() {
        Set<Object[]> repoResult = new HashSet<>();
        repoResult.add(new Object[]{1L, "TestOccupation"});
        BeneficiaryOccupation occupation = new BeneficiaryOccupation().getOccupation(1L, "TestOccupation");
        when(beneficiaryOccupationRepository.getActiveOccupations()).thenReturn(repoResult);
        List<BeneficiaryOccupation> result = service.getActiveOccupations();
        assertEquals(1, result.size());
        BeneficiaryOccupation actual = result.get(0);
        assertEquals(occupation.getOccupationID(), actual.getOccupationID());
        assertEquals(occupation.getOccupationType(), actual.getOccupationType());
    }

    @Test
    void testGetActiveOccupations_withNullObject() {
        Set<Object[]> repoResult = new HashSet<>();
        repoResult.add(null);
        when(beneficiaryOccupationRepository.getActiveOccupations()).thenReturn(repoResult);
        List<BeneficiaryOccupation> result = service.getActiveOccupations();
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetActiveOccupations_withInvalidLength() {
        Set<Object[]> repoResult = new HashSet<>();
        repoResult.add(new Object[]{1L}); // length < 2
        when(beneficiaryOccupationRepository.getActiveOccupations()).thenReturn(repoResult);
        List<BeneficiaryOccupation> result = service.getActiveOccupations();
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetActiveOccupations_withEmptySet() {
        when(beneficiaryOccupationRepository.getActiveOccupations()).thenReturn(Collections.emptySet());
        List<BeneficiaryOccupation> result = service.getActiveOccupations();
        assertTrue(result.isEmpty());
    }
}
