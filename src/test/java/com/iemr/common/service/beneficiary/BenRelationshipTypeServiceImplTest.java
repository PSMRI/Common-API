package com.iemr.common.service.beneficiary;

import com.iemr.common.data.beneficiary.BenRelationshipType;
import com.iemr.common.repository.beneficiary.BeneficiaryRelationshipTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BenRelationshipTypeServiceImplTest {
    @Mock
    private BeneficiaryRelationshipTypeRepository beneficiaryRelationshipTypeRepository;

    @InjectMocks
    private BenRelationshipTypeServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSetBeneficiaryRelationshipTypeRepository() {
        BenRelationshipTypeServiceImpl impl = new BenRelationshipTypeServiceImpl();
        BeneficiaryRelationshipTypeRepository repo = mock(BeneficiaryRelationshipTypeRepository.class);
        impl.setBeneficiaryRelationshipTypeRepository(repo);
        // No exception, repository is set
    }

    @Test
    void testGetActiveRelationshipTypes_withValidData() {
        Set<Object[]> repoResult = new HashSet<>();
        repoResult.add(new Object[]{1, "TestRelation"});
        BenRelationshipType expected = new BenRelationshipType(1, "TestRelation", false);
        when(beneficiaryRelationshipTypeRepository.getActiveRelationships()).thenReturn(repoResult);
        List<BenRelationshipType> result = service.getActiveRelationshipTypes();
        assertEquals(1, result.size());
        BenRelationshipType actual = result.get(0);
        assertEquals(expected.getBenRelationshipID(), actual.getBenRelationshipID());
        assertEquals(expected.getBenRelationshipType(), actual.getBenRelationshipType());
        assertEquals(expected.getDeleted(), actual.getDeleted());
    }

    @Test
    void testGetActiveRelationshipTypes_withNullObject() {
        Set<Object[]> repoResult = new HashSet<>();
        repoResult.add(null);
        when(beneficiaryRelationshipTypeRepository.getActiveRelationships()).thenReturn(repoResult);
        List<BenRelationshipType> result = service.getActiveRelationshipTypes();
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetActiveRelationshipTypes_withInvalidLength() {
        Set<Object[]> repoResult = new HashSet<>();
        repoResult.add(new Object[]{1}); // length < 2
        when(beneficiaryRelationshipTypeRepository.getActiveRelationships()).thenReturn(repoResult);
        List<BenRelationshipType> result = service.getActiveRelationshipTypes();
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetActiveRelationshipTypes_withEmptySet() {
        when(beneficiaryRelationshipTypeRepository.getActiveRelationships()).thenReturn(Collections.emptySet());
        List<BenRelationshipType> result = service.getActiveRelationshipTypes();
        assertTrue(result.isEmpty());
    }
}
