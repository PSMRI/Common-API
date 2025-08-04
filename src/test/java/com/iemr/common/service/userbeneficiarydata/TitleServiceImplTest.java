package com.iemr.common.service.userbeneficiarydata;

import com.iemr.common.data.userbeneficiarydata.Title;
import com.iemr.common.repository.userbeneficiarydata.TitleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TitleServiceImplTest {
    @InjectMocks
    TitleServiceImpl service;

    @Mock
    TitleRepository titleRepository;

    @Test
    public void testGetActiveTitles() {
        Object[] obj = new Object[]{1, "Mr", "Extra"};
        Set<Object[]> set = new HashSet<>();
        set.add(obj);
        when(titleRepository.findAciveTitles()).thenReturn(set);
        List<Title> result = service.getActiveTitles();
        assertEquals(1, result.size());
        Title title = result.get(0);
        boolean found = false;
        StringBuilder fieldNames = new StringBuilder();
        for (java.lang.reflect.Field field : Title.class.getDeclaredFields()) {
            field.setAccessible(true);
            fieldNames.append(field.getName()).append(", ");
            try {
                Object value = field.get(title);
                if (Objects.equals(value, 1) || Objects.equals(value, "Mr")) {
                    found = true;
                }
            } catch (Exception ignored) {}
        }
        if (!found) {
            fail("Title id or name not found in any field. Fields: " + fieldNames);
        }
    }

    @Test
    public void testGetActiveTitles_empty() {
        when(titleRepository.findAciveTitles()).thenReturn(new HashSet<>());
        List<Title> result = service.getActiveTitles();
        assertTrue(result.isEmpty());
    }

    @Test
    public void testSetTitleServiceImpl() {
        TitleServiceImpl impl = new TitleServiceImpl();
        TitleRepository mockRepo = mock(TitleRepository.class);
        impl.setTitleServiceImpl(mockRepo);
        // Use reflection to verify the field is set
        try {
            java.lang.reflect.Field field = TitleServiceImpl.class.getDeclaredField("titleRepository");
            field.setAccessible(true);
            assertSame(mockRepo, field.get(impl));
        } catch (Exception e) {
            fail("Setter did not set the field correctly: " + e.getMessage());
        }
    }
}
