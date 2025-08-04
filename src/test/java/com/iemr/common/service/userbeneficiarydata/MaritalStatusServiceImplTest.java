package com.iemr.common.service.userbeneficiarydata;

import com.iemr.common.data.userbeneficiarydata.MaritalStatus;
import com.iemr.common.repository.userbeneficiarydata.MaritalStatusRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MaritalStatusServiceImplTest {
    @InjectMocks
    MaritalStatusServiceImpl service;

    @Mock
    MaritalStatusRepository maritalStatusRepository;

    @Test
    public void testGetActiveMaritalStatus() {
        Object[] obj = new Object[]{1, "Single"};
        Set<Object[]> set = new HashSet<>();
        set.add(obj);
        when(maritalStatusRepository.findAciveMaritalStatus()).thenReturn(set);
        List<MaritalStatus> result = service.getActiveMaritalStatus();
        assertEquals(1, result.size());
        MaritalStatus maritalStatus = result.get(0);
        boolean found = false;
        StringBuilder fieldNames = new StringBuilder();
        for (java.lang.reflect.Field field : MaritalStatus.class.getDeclaredFields()) {
            field.setAccessible(true);
            fieldNames.append(field.getName()).append(", ");
            try {
                Object value = field.get(maritalStatus);
                if (Objects.equals(value, 1) || Objects.equals(value, "Single")) {
                    found = true;
                }
            } catch (Exception ignored) {}
        }
        if (!found) {
            fail("MaritalStatus id or name not found in any field. Fields: " + fieldNames);
        }
    }

    @Test
    public void testGetActiveMaritalStatus_empty() {
        when(maritalStatusRepository.findAciveMaritalStatus()).thenReturn(new HashSet<>());
        List<MaritalStatus> result = service.getActiveMaritalStatus();
        assertTrue(result.isEmpty());
    }

    @Test
    public void testSetMaritalStatusServiceImpl() {
        MaritalStatusServiceImpl impl = new MaritalStatusServiceImpl();
        MaritalStatusRepository mockRepo = mock(MaritalStatusRepository.class);
        impl.setMaritalStatusServiceImpl(mockRepo);
        // Use reflection to verify the field is set
        try {
            java.lang.reflect.Field field = MaritalStatusServiceImpl.class.getDeclaredField("maritalStatusRepository");
            field.setAccessible(true);
            assertSame(mockRepo, field.get(impl));
        } catch (Exception e) {
            fail("Setter did not set the field correctly: " + e.getMessage());
        }
    }
}
