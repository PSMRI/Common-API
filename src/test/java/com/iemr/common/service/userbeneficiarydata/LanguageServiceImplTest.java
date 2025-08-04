package com.iemr.common.service.userbeneficiarydata;

import com.iemr.common.data.userbeneficiarydata.Language;
import com.iemr.common.repository.userbeneficiarydata.LanguageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LanguageServiceImplTest {
    @InjectMocks
    LanguageServiceImpl service;

    @Mock
    LanguageRepository languageRepository;

    @Test
    public void testGetActiveLanguages() {
        Object[] obj = new Object[]{1, "English"};
        Set<Object[]> set = new HashSet<>();
        set.add(obj);
        when(languageRepository.findAciveLanguages()).thenReturn(set);
        List<Language> result = service.getActiveLanguages();
        assertEquals(1, result.size());
        Language language = result.get(0);
        boolean found = false;
        StringBuilder fieldNames = new StringBuilder();
        for (java.lang.reflect.Field field : Language.class.getDeclaredFields()) {
            field.setAccessible(true);
            fieldNames.append(field.getName()).append(", ");
            try {
                Object value = field.get(language);
                if (Objects.equals(value, 1) || Objects.equals(value, "English")) {
                    found = true;
                }
            } catch (Exception ignored) {}
        }
        if (!found) {
            fail("Language id or name not found in any field. Fields: " + fieldNames);
        }
    }

    @Test
    public void testGetActiveLanguages_empty() {
        when(languageRepository.findAciveLanguages()).thenReturn(new HashSet<>());
        List<Language> result = service.getActiveLanguages();
        assertTrue(result.isEmpty());
    }

    @Test
    public void testSetGenderServiceImpl() {
        LanguageServiceImpl impl = new LanguageServiceImpl();
        LanguageRepository mockRepo = mock(LanguageRepository.class);
        impl.setGenderServiceImpl(mockRepo);
        // Use reflection to verify the field is set
        try {
            java.lang.reflect.Field field = LanguageServiceImpl.class.getDeclaredField("languageRepository");
            field.setAccessible(true);
            assertSame(mockRepo, field.get(impl));
        } catch (Exception e) {
            fail("Setter did not set the field correctly: " + e.getMessage());
        }
    }
}
