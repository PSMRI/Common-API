package com.iemr.common.service.abdmfacility;

import com.iemr.common.data.users.ProviderServiceAddressMapping;
import com.iemr.common.repository.abdmfacility.AbdmFacilityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AbdmFacilityServiceImplTest {

    @Mock
    private AbdmFacilityRepository abdmFacilityRepo;

    @InjectMocks
    private AbdmFacilityServiceImpl abdmFacilityService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getMappedAbdmFacility_returnsToString() {
        int psmId = 123;
        ProviderServiceAddressMapping mapping = mock(ProviderServiceAddressMapping.class);
        when(abdmFacilityRepo.getAbdmFacility(psmId)).thenReturn(mapping);
        when(mapping.toString()).thenReturn("mockedToString");

        String result = abdmFacilityService.getMappedAbdmFacility(psmId);

        assertEquals("mockedToString", result);
        verify(abdmFacilityRepo).getAbdmFacility(psmId);
    }
}
