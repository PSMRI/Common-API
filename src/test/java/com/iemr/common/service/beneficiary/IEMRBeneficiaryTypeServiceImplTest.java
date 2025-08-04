package com.iemr.common.service.beneficiary;

import com.iemr.common.data.beneficiary.BeneficiaryType;
import com.iemr.common.repository.beneficiary.IEMRBeneficiaryTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IEMRBeneficiaryTypeServiceImplTest {
    @Mock
    private IEMRBeneficiaryTypeRepository iemrBeneficiaryTypeRepository;

    @InjectMocks
    private IEMRBeneficiaryTypeServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAddRelation() {
        BeneficiaryType type = new BeneficiaryType();
        when(iemrBeneficiaryTypeRepository.save(type)).thenReturn(type);
        BeneficiaryType result = service.addRelation(type);
        assertSame(type, result);
        verify(iemrBeneficiaryTypeRepository).save(type);
    }

    @Test
    void testGetRelations() {
        BeneficiaryType type1 = new BeneficiaryType();
        BeneficiaryType type2 = new BeneficiaryType();
        List<BeneficiaryType> types = Arrays.asList(type1, type2);
        when(iemrBeneficiaryTypeRepository.findAll()).thenReturn(types);
        Iterable<BeneficiaryType> result = service.getRelations();
        assertTrue(result.iterator().hasNext());
        verify(iemrBeneficiaryTypeRepository).findAll();
    }
}
