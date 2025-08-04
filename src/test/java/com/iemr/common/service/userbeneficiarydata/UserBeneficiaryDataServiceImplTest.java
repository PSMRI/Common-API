package com.iemr.common.service.userbeneficiarydata;

import com.iemr.common.data.userbeneficiarydata.Gender;
import com.iemr.common.repository.userbeneficiarydata.UserBeneficiaryDataRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserBeneficiaryDataServiceImplTest {
    @InjectMocks
    UserBeneficiaryDataServiceImpl service;

    @Mock
    UserBeneficiaryDataRepository userBeneficiaryDataRepository;

    @Test
    public void testGetActiveGender() {
        Object[] obj = new Object[]{1, "Male"};
        Set<Object[]> set = new HashSet<>();
        set.add(obj);
        when(userBeneficiaryDataRepository.findActiveGenders()).thenReturn(set);
        List<Gender> result = service.getActiveGender();
        assertEquals(1, result.size());
        Gender gender = result.get(0);
        boolean found = false;
        StringBuilder fieldNames = new StringBuilder();
        for (java.lang.reflect.Field field : Gender.class.getDeclaredFields()) {
            field.setAccessible(true);
            fieldNames.append(field.getName()).append(", ");
            try {
                Object value = field.get(gender);
                if (Objects.equals(value, 1) || Objects.equals(value, "Male")) {
                    found = true;
                }
            } catch (Exception ignored) {}
        }
        if (!found) {
            fail("Gender id or name not found in any field. Fields: " + fieldNames);
        }
    }

    @Test
    public void testGetActiveGender_empty() {
        when(userBeneficiaryDataRepository.findActiveGenders()).thenReturn(new HashSet<>());
        List<Gender> result = service.getActiveGender();
        assertTrue(result.isEmpty());
    }

    @Test
    public void testSetUserBeneficiaryDataRepository() {
        UserBeneficiaryDataServiceImpl impl = new UserBeneficiaryDataServiceImpl();
        UserBeneficiaryDataRepository mockRepo = mock(UserBeneficiaryDataRepository.class);
        impl.setUserBeneficiaryDataRepository(mockRepo);
        // Use reflection to verify the field is set
        try {
            java.lang.reflect.Field field = UserBeneficiaryDataServiceImpl.class.getDeclaredField("userBeneficiaryDataRepository");
            field.setAccessible(true);
            assertSame(mockRepo, field.get(impl));
        } catch (Exception e) {
            fail("Setter did not set the field correctly: " + e.getMessage());
        }
    }
}
