package com.iemr.common.service.userbeneficiarydata;

import com.iemr.common.data.userbeneficiarydata.Community;
import com.iemr.common.repository.beneficiary.CommunityRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommunityServiceImplTest {
    @InjectMocks
    CommunityServiceImpl service;

    @Mock
    CommunityRepository communityRepository;

    @Test
    public void testGetActiveCommunities() {
        Object[] obj = new Object[]{1, "CommunityName"};
        Set<Object[]> set = new HashSet<>();
        set.add(obj);
        when(communityRepository.findAciveCommunities()).thenReturn(set);
        List<Community> result = service.getActiveCommunities();
        assertEquals(1, result.size());
        // Try to assert on id and name using reflection, since getCommunity returns a Community object
        Community community = result.get(0);
        boolean found = false;
        StringBuilder fieldNames = new StringBuilder();
        for (java.lang.reflect.Field field : Community.class.getDeclaredFields()) {
            field.setAccessible(true);
            fieldNames.append(field.getName()).append(", ");
            try {
                Object value = field.get(community);
                if (Objects.equals(value, 1) || Objects.equals(value, "CommunityName")) {
                    found = true;
                }
            } catch (Exception ignored) {}
        }
        if (!found) {
            fail("Community id or name not found in any field. Fields: " + fieldNames);
        }
    }

    @Test
    public void testGetActiveCommunities_empty() {
        when(communityRepository.findAciveCommunities()).thenReturn(new HashSet<>());
        List<Community> result = service.getActiveCommunities();
        assertTrue(result.isEmpty());
    }

    @Test
    public void testSetCommunityServiceImpl() {
        CommunityServiceImpl impl = new CommunityServiceImpl();
        CommunityRepository mockRepo = mock(CommunityRepository.class);
        impl.setCommunityServiceImpl(mockRepo);
        // Use reflection to verify the field is set
        try {
            java.lang.reflect.Field field = CommunityServiceImpl.class.getDeclaredField("communityRepository");
            field.setAccessible(true);
            assertSame(mockRepo, field.get(impl));
        } catch (Exception e) {
            fail("Setter did not set the field correctly: " + e.getMessage());
        }
    }
}
