package com.iemr.common.service.userbeneficiarydata;

import com.iemr.common.data.userbeneficiarydata.Religion;
import com.iemr.common.repository.userbeneficiarydata.ReligionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RelegionServiceImplTest {
    @InjectMocks
    RelegionServiceImpl service;

    @Mock
    ReligionRepository relegionRepository;

    @Test
    public void testGetActiveReligions() {
        Object[] obj = new Object[]{1, "Hindu", "Description"};
        List<Object[]> list = new ArrayList<>();
        list.add(obj);
        when(relegionRepository.getActiveReligions()).thenReturn(list);
        List<Religion> result = service.getActiveReligions();
        assertEquals(1, result.size());
        Religion religion = result.get(0);
        boolean found = false;
        StringBuilder fieldNames = new StringBuilder();
        for (java.lang.reflect.Field field : Religion.class.getDeclaredFields()) {
            field.setAccessible(true);
            fieldNames.append(field.getName()).append(", ");
            try {
                Object value = field.get(religion);
                if (Objects.equals(value, 1) || Objects.equals(value, "Hindu") || Objects.equals(value, "Description")) {
                    found = true;
                }
            } catch (Exception ignored) {}
        }
        if (!found) {
            fail("Religion id, name, or description not found in any field. Fields: " + fieldNames);
        }
    }

    @Test
    public void testGetActiveReligions_empty() {
        when(relegionRepository.getActiveReligions()).thenReturn(new ArrayList<>());
        List<Religion> result = service.getActiveReligions();
        assertTrue(result.isEmpty());
    }

    @Test
    public void testSetRelegionServiceImpl() {
        RelegionServiceImpl impl = new RelegionServiceImpl();
        ReligionRepository mockRepo = mock(ReligionRepository.class);
        impl.setRelegionServiceImpl(mockRepo);
        // Use reflection to verify the field is set
        try {
            java.lang.reflect.Field field = RelegionServiceImpl.class.getDeclaredField("relegionRepository");
            field.setAccessible(true);
            assertSame(mockRepo, field.get(impl));
        } catch (Exception e) {
            fail("Setter did not set the field correctly: " + e.getMessage());
        }
    }
}
