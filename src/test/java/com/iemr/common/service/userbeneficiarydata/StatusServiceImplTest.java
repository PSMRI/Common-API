package com.iemr.common.service.userbeneficiarydata;

import com.iemr.common.data.userbeneficiarydata.Status;
import com.iemr.common.repository.userbeneficiarydata.StatusRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StatusServiceImplTest {
    @InjectMocks
    StatusServiceImpl service;

    @Mock
    StatusRepository statusRepository;

    @Test
    public void testGetActiveStatus() {
        Object[] obj = new Object[]{1, "Active"};
        Set<Object[]> set = new HashSet<>();
        set.add(obj);
        when(statusRepository.findAciveStatus()).thenReturn(set);
        List<Status> result = service.getActiveStatus();
        assertEquals(1, result.size());
        Status status = result.get(0);
        boolean found = false;
        StringBuilder fieldNames = new StringBuilder();
        for (java.lang.reflect.Field field : Status.class.getDeclaredFields()) {
            field.setAccessible(true);
            fieldNames.append(field.getName()).append(", ");
            try {
                Object value = field.get(status);
                if (Objects.equals(value, 1) || Objects.equals(value, "Active")) {
                    found = true;
                }
            } catch (Exception ignored) {}
        }
        if (!found) {
            fail("Status id or name not found in any field. Fields: " + fieldNames);
        }
    }

    @Test
    public void testGetActiveStatus_empty() {
        when(statusRepository.findAciveStatus()).thenReturn(new HashSet<>());
        List<Status> result = service.getActiveStatus();
        assertTrue(result.isEmpty());
    }

    @Test
    public void testSetTitleServiceImpl() {
        StatusServiceImpl impl = new StatusServiceImpl();
        StatusRepository mockRepo = mock(StatusRepository.class);
        impl.setTitleServiceImpl(mockRepo);
        // Use reflection to verify the field is set
        try {
            java.lang.reflect.Field field = StatusServiceImpl.class.getDeclaredField("statusRepository");
            field.setAccessible(true);
            assertSame(mockRepo, field.get(impl));
        } catch (Exception e) {
            fail("Setter did not set the field correctly: " + e.getMessage());
        }
    }
}
