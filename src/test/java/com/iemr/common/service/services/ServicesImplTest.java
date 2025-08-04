package com.iemr.common.service.services;

import com.iemr.common.data.users.ServiceMaster;
import com.iemr.common.repository.services.ServiesRepository;
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
public class ServicesImplTest {
    @InjectMocks
    ServicesImpl servicesImpl;

    @Mock
    ServiesRepository serviesRepository;

    @Test
    public void testServicesList_withResults() {
        Set<Object[]> repoResult = new HashSet<>();
        repoResult.add(new Object[]{1, "Service1", "Desc1", true});
        repoResult.add(new Object[]{2, "Service2", "Desc2", false});
        when(serviesRepository.getActiveServicesList()).thenReturn(repoResult);

        List<ServiceMaster> result = servicesImpl.servicesList();
        assertEquals(2, result.size());
        result.sort(Comparator.comparing(ServiceMaster::getServiceID));
        assertEquals(1, result.get(0).getServiceID());
        assertEquals("Service1", result.get(0).getServiceName());
        assertEquals("Desc1", result.get(0).getServiceDesc());
        assertTrue(result.get(0).isDeleted());
        assertEquals(2, result.get(1).getServiceID());
        assertEquals("Service2", result.get(1).getServiceName());
        assertEquals("Desc2", result.get(1).getServiceDesc());
        assertFalse(result.get(1).isDeleted());
    }

    @Test
    public void testServicesList_withNullObject() {
        Set<Object[]> repoResult = new HashSet<>();
        repoResult.add(null);
        when(serviesRepository.getActiveServicesList()).thenReturn(repoResult);
        List<ServiceMaster> result = servicesImpl.servicesList();
        assertEquals(0, result.size());
    }

    @Test
    public void testServicesList_withShortArray() {
        Set<Object[]> repoResult = new HashSet<>();
        repoResult.add(new Object[]{1, "Service1"}); // length < 4
        when(serviesRepository.getActiveServicesList()).thenReturn(repoResult);
        List<ServiceMaster> result = servicesImpl.servicesList();
        assertEquals(0, result.size());
    }

    @Test
    public void testServicesList_empty() {
        when(serviesRepository.getActiveServicesList()).thenReturn(new HashSet<>());
        List<ServiceMaster> result = servicesImpl.servicesList();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
