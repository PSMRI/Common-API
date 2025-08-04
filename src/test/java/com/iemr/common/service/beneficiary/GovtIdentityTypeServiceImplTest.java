package com.iemr.common.service.beneficiary;

import com.iemr.common.data.beneficiary.GovtIdentityType;
import com.iemr.common.repository.beneficiary.GovtIdentityTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GovtIdentityTypeServiceImplTest {
    @Mock
    private GovtIdentityTypeRepository govtIdentityTypeRepository;

    @InjectMocks
    private GovtIdentityTypeServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSetBeneficiaryOccupationRepository() {
        GovtIdentityTypeServiceImpl impl = new GovtIdentityTypeServiceImpl();
        GovtIdentityTypeRepository repo = mock(GovtIdentityTypeRepository.class);
        impl.setBeneficiaryOccupationRepository(repo);
        // No exception, repository is set
    }

    @Test
    void testGetActiveIDTypes_withValidData() {
        Set<Object[]> repoResult = new HashSet<>();
        repoResult.add(new Object[]{1, "Aadhaar", true});
        GovtIdentityType expected = new GovtIdentityType().getConstructor(1, "Aadhaar", true);
        when(govtIdentityTypeRepository.getActiveIDTypes()).thenReturn(repoResult);
        List<GovtIdentityType> result = service.getActiveIDTypes();
        assertEquals(1, result.size());
        GovtIdentityType actual = result.get(0);
        assertEquals(expected.getGovtIdentityTypeID(), actual.getGovtIdentityTypeID());
        assertEquals(expected.getIdentityType(), actual.getIdentityType());
        assertEquals(expected.getDeleted(), actual.getDeleted());
    }

    @Test
    void testGetActiveIDTypes_withNullObject() {
        Set<Object[]> repoResult = new HashSet<>();
        repoResult.add(null);
        when(govtIdentityTypeRepository.getActiveIDTypes()).thenReturn(repoResult);
        List<GovtIdentityType> result = service.getActiveIDTypes();
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetActiveIDTypes_withInvalidLength() {
        Set<Object[]> repoResult = new HashSet<>();
        repoResult.add(new Object[]{1, "Aadhaar"}); // length < 3
        when(govtIdentityTypeRepository.getActiveIDTypes()).thenReturn(repoResult);
        List<GovtIdentityType> result = service.getActiveIDTypes();
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetActiveIDTypes_withEmptySet() {
        when(govtIdentityTypeRepository.getActiveIDTypes()).thenReturn(Collections.emptySet());
        List<GovtIdentityType> result = service.getActiveIDTypes();
        assertTrue(result.isEmpty());
    }
}
