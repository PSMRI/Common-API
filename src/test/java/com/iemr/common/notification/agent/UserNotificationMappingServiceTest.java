package com.iemr.common.notification.agent;
import com.iemr.common.notification.agent.UserNotificationMapping;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import static org.mockito.ArgumentMatchers.anyString;
import com.iemr.common.notification.agent.UserNotificationMappingRepo;
import com.iemr.common.notification.agent.DTO.AlertAndNotificationCount;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.iemr.common.notification.agent.UserNotificationMappingService;
import com.iemr.common.notification.agent.DTO.UserNotificationDisplayMaxDTO;
import static org.mockito.Mockito.doNothing;
import org.mockito.Mockito;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import org.mockito.InjectMocks;
import com.iemr.common.notification.agent.DTO.AlertAndNotificationChangeStatusDTO;
import static org.mockito.Mockito.times;
import org.junit.jupiter.api.BeforeEach;
import static org.mockito.ArgumentMatchers.eq;
import com.iemr.common.notification.agent.DTO.UserNotificationDisplayMinDTO;
import org.springframework.test.util.ReflectionTestUtils;
import com.iemr.common.notification.agent.DTO.AlertAndNotificationCountDTO;
import static org.mockito.Mockito.mock;
import java.util.Collections;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.extension.ExtendWith;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.function.Executable;
import static org.mockito.Mockito.spy;
import java.sql.Timestamp;
import org.mockito.Mock;
import static org.mockito.Mockito.anyString;

@ExtendWith(MockitoExtension.class)
class UserNotificationMappingServiceTest {

private Logger mockLogger;

    // Simple TestObject class for testing purposes
    class TestObject {
    private String field1;
    private int field2;

    public TestObject(String field1, int field2) {
        this.field1 = field1;
        this.field2 = field2;
    }

    public String getField1() {
        return field1;
    }

    public void setField1(String field1) {
        this.field1 = field1;
    }

    public int getField2() {
        return field2;
    }

    public void setField2(int field2) {
        this.field2 = field2;
    }
} // <-- Add this closing brace for TestObject class

@InjectMocks
private UserNotificationMappingService userNotificationMappingService;
@Mock
private UserNotificationMappingRepo repo;

@BeforeEach
void setUp() {
    mockLogger = mock(Logger.class);
    ReflectionTestUtils.setField(userNotificationMappingService, "logger", mockLogger);
}



@Test
void markNotificationSingle_shouldUpdateStatusAndReturnSuccess() {
    String status = "read";
    Integer notificationId = 1;

    String result = userNotificationMappingService.markNotificationSingle(status, notificationId);

    assertEquals("success", result);
    verify(repo, times(1)).updateUserNotificationMappingSingle(status, notificationId);
}

@Test
void deleteNotificationList_shouldSetDeletedStatusForListAndReturnSuccess() {
    Boolean isDeleted = true;
    List<Integer> notificationIds = Arrays.asList(1, 2, 3);

    String result = userNotificationMappingService.deleteNotificationList(isDeleted, notificationIds);

    assertEquals("success", result);
    verify(repo, times(1)).setDeleteUserNotificationMappingList(isDeleted, notificationIds);
}

@Test
void deleteNotificationList_shouldHandleEmptyList() {
    Boolean isDeleted = true;
    List<Integer> notificationIds = Collections.emptyList();

    String result = userNotificationMappingService.deleteNotificationList(isDeleted, notificationIds);

    assertEquals("success", result);
    verify(repo, times(1)).setDeleteUserNotificationMappingList(isDeleted, notificationIds);
}

@Test
void deleteNotificationList_shouldHandleNullList() {
    Boolean isDeleted = true;
    List<Integer> notificationIds = null;

    String result = userNotificationMappingService.deleteNotificationList(isDeleted, notificationIds);

    assertEquals("success", result);
    verify(repo, times(1)).setDeleteUserNotificationMappingList(isDeleted, null);
}

@Test
void deleteNotificationSingle_shouldSetDeletedStatusAndReturnSuccess() {
    Boolean isDeleted = true;
    Integer notificationId = 1;

    String result = userNotificationMappingService.deleteNotificationSingle(isDeleted, notificationId);

    assertEquals("success", result);
    verify(repo, times(1)).setDeletedUserNotificationMappingSingle(isDeleted, notificationId);
}

@Test
void getJsonAsString_shouldLogJsonForValidObject() {
    String name = "testObject";
    Object obj = new TestObject("value1", 123);

    userNotificationMappingService.getJsonAsString(name, obj);

    verify(mockLogger, times(1)).info("UserNotificationMappingController -> getJsonAsString start");
    verify(mockLogger, times(1)).info(Mockito.contains("Object: " + name + " :toJSON: "));
    verify(mockLogger, times(1)).info("UserNotificationMappingController -> getJsonAsString finish");
    verify(mockLogger, never()).error(anyString());
}

@Test
void getJsonAsString_shouldLogJsonForNullObject() {
    String name = "nullObject";
    Object obj = null;

    userNotificationMappingService.getJsonAsString(name, obj);

    verify(mockLogger, times(1)).info("UserNotificationMappingController -> getJsonAsString start");
    verify(mockLogger, times(1)).info(Mockito.contains("Object: " + name + " :toJSON: null"));
    verify(mockLogger, times(1)).info("UserNotificationMappingController -> getJsonAsString finish");
    verify(mockLogger, never()).error(anyString());
}

@Test
void querySelector_shouldReturnMarkQueryForMarkStatus() {
    AlertAndNotificationChangeStatusDTO dto = new AlertAndNotificationChangeStatusDTO();
    dto.setNotficationStatus("mark");

    String result = userNotificationMappingService.querySelector(dto);

    assertEquals("", result);
}

@Test
void querySelector_shouldReturnUnmarkQueryForUnmarkStatus() {
    AlertAndNotificationChangeStatusDTO dto = new AlertAndNotificationChangeStatusDTO();
    dto.setNotficationStatus("unmark");

    String result = userNotificationMappingService.querySelector(dto);

    assertEquals("", result);
}

@Test
void querySelector_shouldReturnDeleteQueryForDeleteStatus() {
    AlertAndNotificationChangeStatusDTO dto = new AlertAndNotificationChangeStatusDTO();
    dto.setNotficationStatus("delete");

    String result = userNotificationMappingService.querySelector(dto);

    assertEquals("", result);
}

@Test
void querySelector_shouldReturnEmptyStringForUnknownStatus() {
    AlertAndNotificationChangeStatusDTO dto = new AlertAndNotificationChangeStatusDTO();
    dto.setNotficationStatus("unknown");

    String result = userNotificationMappingService.querySelector(dto);

    assertEquals("", result);
}

@Test
void querySelector_shouldHandleCaseInsensitiveStatus() {
    AlertAndNotificationChangeStatusDTO dto = new AlertAndNotificationChangeStatusDTO();
    dto.setNotficationStatus("MaRk"); // Mixed case

    String result = userNotificationMappingService.querySelector(dto);

    assertEquals("", result);
}

@Test
void querySelector_shouldThrowNullPointerExceptionForNullStatus() {
    AlertAndNotificationChangeStatusDTO dto = new AlertAndNotificationChangeStatusDTO();
    dto.setNotficationStatus(null); // Null status

        assertThrows(NullPointerException.class, () -> userNotificationMappingService.querySelector(dto));
    }

@Test
void querySelector_shouldThrowNullPointerExceptionForNullDTO() {
    assertThrows(NullPointerException.class, () -> {
        userNotificationMappingService.querySelector(null);
    });
}
    @Test
    void getJsonAsString_shouldLogObjectAsJsonString() throws JsonProcessingException {
        String name = "testObject";
        TestObject obj = new TestObject("value1", 123); // Use a simple test object

        // Call the method
        userNotificationMappingService.getJsonAsString(name, obj);

        // Verify that logger.info was called with a string containing the object name and "toJSON"
        // The exact JSON string is generated by ObjectMapper, so we verify the pattern.
        // We can create an ObjectMapper instance in the test to get the expected JSON.
        String expectedJson = new ObjectMapper().writeValueAsString(obj);
        verify(mockLogger, times(1)).info("Object: " + name + " :toJSON: " + expectedJson);
    }
    @Test
    void querySelector_shouldCallGetMarkQueryWhenStatusIsMark() {
        // Spy on the service to mock its internal method calls
        UserNotificationMappingService spyService = spy(userNotificationMappingService);
        AlertAndNotificationChangeStatusDTO dto = new AlertAndNotificationChangeStatusDTO();
        dto.setNotficationStatus("mark");

        // Mock only the method that will be called
        doReturn("markQueryResult").when(spyService).getMarkQuery(any(AlertAndNotificationChangeStatusDTO.class));

        String result = spyService.querySelector(dto);

        assertEquals("markQueryResult", result);
        verify(spyService, times(1)).getMarkQuery(dto);
        verify(spyService, times(0)).getUnmarkQuery(any(AlertAndNotificationChangeStatusDTO.class));
        verify(spyService, times(0)).getDeleteQuery(any(AlertAndNotificationChangeStatusDTO.class));
    }
    @Test
    void querySelector_shouldCallGetUnmarkQueryWhenStatusIsUnmark() {
        UserNotificationMappingService spyService = spy(userNotificationMappingService);
        AlertAndNotificationChangeStatusDTO dto = new AlertAndNotificationChangeStatusDTO();
        dto.setNotficationStatus("unmark");

        // Mock only the method that will be called
        doReturn("unmarkQueryResult").when(spyService).getUnmarkQuery(any(AlertAndNotificationChangeStatusDTO.class));

        String result = spyService.querySelector(dto);

        assertEquals("unmarkQueryResult", result);
        verify(spyService, times(0)).getMarkQuery(any(AlertAndNotificationChangeStatusDTO.class));
        verify(spyService, times(1)).getUnmarkQuery(dto);
        verify(spyService, times(0)).getDeleteQuery(any(AlertAndNotificationChangeStatusDTO.class));
    }
    @Test
    void querySelector_shouldCallGetDeleteQueryWhenStatusIsDelete() {
        UserNotificationMappingService spyService = spy(userNotificationMappingService);
        AlertAndNotificationChangeStatusDTO dto = new AlertAndNotificationChangeStatusDTO();
        dto.setNotficationStatus("delete");

        // Mock only the method that will be called
        doReturn("deleteQueryResult").when(spyService).getDeleteQuery(any(AlertAndNotificationChangeStatusDTO.class));

        String result = spyService.querySelector(dto);

        assertEquals("deleteQueryResult", result);
        verify(spyService, times(0)).getMarkQuery(any(AlertAndNotificationChangeStatusDTO.class));
        verify(spyService, times(0)).getUnmarkQuery(any(AlertAndNotificationChangeStatusDTO.class));
        verify(spyService, times(1)).getDeleteQuery(dto);
    }
    @Test
    void querySelector_shouldReturnEmptyStringWhenStatusIsUnknown() {
        UserNotificationMappingService spyService = spy(userNotificationMappingService);
        AlertAndNotificationChangeStatusDTO dto = new AlertAndNotificationChangeStatusDTO();
        dto.setNotficationStatus("unknown"); // Not "mark", "unmark", or "delete"

        // No need to mock internal methods as they shouldn't be called
        String result = spyService.querySelector(dto);

        assertEquals("", result);
        verify(spyService, times(0)).getMarkQuery(any(AlertAndNotificationChangeStatusDTO.class));
        verify(spyService, times(0)).getUnmarkQuery(any(AlertAndNotificationChangeStatusDTO.class));
        verify(spyService, times(0)).getDeleteQuery(any(AlertAndNotificationChangeStatusDTO.class));
    }

@Test
    void getMarkQuery_shouldReturnEmptyString() {
        AlertAndNotificationChangeStatusDTO dto = new AlertAndNotificationChangeStatusDTO();
        String result = userNotificationMappingService.getMarkQuery(dto);
        assertEquals("", result);
    }

@Test
    void getMarkQuery_shouldReturnEmptyStringForNullDTO() {
        String result = userNotificationMappingService.getMarkQuery(null);
        assertEquals("", result);
    }

@Test
    void getDeleteQuery_shouldReturnEmptyString() {
        AlertAndNotificationChangeStatusDTO dto = new AlertAndNotificationChangeStatusDTO();
        String result = userNotificationMappingService.getDeleteQuery(dto);
        assertEquals("", result);
    }

@Test
    void getDeleteQuery_shouldReturnEmptyStringForNullDTO() {
        String result = userNotificationMappingService.getDeleteQuery(null);
        assertEquals("", result);
    }

@Test
    void createUserNotificationMapping_shouldReturnTrueForNonEmptyList() {
        List<Integer> userIds = Arrays.asList(1, 2, 3);
        Boolean result = userNotificationMappingService.createUserNotificationMapping(userIds);
        assertEquals(true, result);
    }

@Test
    void createUserNotificationMapping_shouldReturnTrueForEmptyList() {
        List<Integer> userIds = Collections.emptyList();
        Boolean result = userNotificationMappingService.createUserNotificationMapping(userIds);
        assertEquals(true, result);
    }

@Test
    void createUserNotificationMapping_shouldThrowNullPointerExceptionForNullList() {
        List<Integer> userIds = null;
        assertThrows(NullPointerException.class, () -> userNotificationMappingService.createUserNotificationMapping(userIds));
    }

@Test
    void markNotificationList_shouldUpdateStatusForListAndReturnSuccess() {
        String status = "read";
        List<Integer> notificationIds = Arrays.asList(1, 2, 3);

        String result = userNotificationMappingService.markNotificationList(status, notificationIds);

        assertEquals("success", result);
        verify(repo, times(1)).updateUserNotificationMappingList(status, notificationIds);
    }

@Test
    void markNotificationList_shouldHandleEmptyList() {
        String status = "read";
        List<Integer> notificationIds = Collections.emptyList();

        String result = userNotificationMappingService.markNotificationList(status, notificationIds);

        assertEquals("success", result);
        verify(repo, times(1)).updateUserNotificationMappingList(status, notificationIds);
    }

@Test
    void markNotificationList_shouldHandleNullList() {
        String status = "read";
        List<Integer> notificationIds = null;

        String result = userNotificationMappingService.markNotificationList(status, notificationIds);

        assertEquals("success", result);
        verify(repo, times(1)).updateUserNotificationMappingList(status, null);
    }

@Test
    void getAlertAndNotificationCount_shouldCallRepoWithWorkingLocationID_whenPresent() {
        UserNotificationDisplayMinDTO dto = new UserNotificationDisplayMinDTO();
        dto.setUserID(1);
        dto.setRoleID(10);
        dto.setProviderServiceMapID(100);
        dto.setWorkingLocationID(1000);

        AlertAndNotificationCount mockCount = mock(AlertAndNotificationCount.class);
        List<AlertAndNotificationCount> mockList = Arrays.asList(mockCount);
        doReturn(mockList).when(repo).getShortDisplayFormatWithWorkLocation(
                anyInt(), anyInt(), anyInt(), anyInt(), anyString(), anyBoolean(), any(Timestamp.class));

        AlertAndNotificationCountDTO result = userNotificationMappingService.getAlertAndNotificationCount(dto);

        verify(repo, times(1)).getShortDisplayFormatWithWorkLocation(
                eq(dto.getUserID()), eq(dto.getRoleID()), eq(dto.getProviderServiceMapID()),
                eq(dto.getWorkingLocationID()), eq("unread"), eq(false), any(Timestamp.class));
        verify(repo, never()).getShortDisplayFormat(anyInt(), anyInt(), anyInt(), anyString(), anyBoolean(), any(Timestamp.class));

        assertEquals(dto.getUserID(), result.getUserId());
        assertEquals(mockList, result.getUserNotificationTypeList());
        verify(mockLogger, times(2)).info(Mockito.contains("getAlertAndNotificationCount"));
    }

@Test
    void getAlertAndNotificationCount_shouldCallRepoWithoutWorkingLocationID_whenNull() {
        UserNotificationDisplayMinDTO dto = new UserNotificationDisplayMinDTO();
        dto.setUserID(1);
        dto.setRoleID(10);
        dto.setProviderServiceMapID(100);
        dto.setWorkingLocationID(null);

        AlertAndNotificationCount mockCount = mock(AlertAndNotificationCount.class);
        List<AlertAndNotificationCount> mockList = Arrays.asList(mockCount);
        doReturn(mockList).when(repo).getShortDisplayFormat(
                anyInt(), anyInt(), anyInt(), anyString(), anyBoolean(), any(Timestamp.class));

        AlertAndNotificationCountDTO result = userNotificationMappingService.getAlertAndNotificationCount(dto);

        verify(repo, times(1)).getShortDisplayFormat(
                eq(dto.getUserID()), eq(dto.getRoleID()), eq(dto.getProviderServiceMapID()),
                eq("unread"), eq(false), any(Timestamp.class));
        verify(repo, never()).getShortDisplayFormatWithWorkLocation(
                anyInt(), anyInt(), anyInt(), anyInt(), anyString(), anyBoolean(), any(Timestamp.class));

        assertEquals(dto.getUserID(), result.getUserId());
        assertEquals(mockList, result.getUserNotificationTypeList());
        verify(mockLogger, times(2)).info(Mockito.contains("getAlertAndNotificationCount"));
    }

@Test
    void getAlertAndNotificationCount_shouldReturnEmptyList_whenRepoReturnsEmpty() {
        UserNotificationDisplayMinDTO dto = new UserNotificationDisplayMinDTO();
        dto.setUserID(1);
        dto.setRoleID(10);
        dto.setProviderServiceMapID(100);
        dto.setWorkingLocationID(null);

        List<AlertAndNotificationCount> emptyList = Collections.emptyList();
        doReturn(emptyList).when(repo).getShortDisplayFormat(
                anyInt(), anyInt(), anyInt(), anyString(), anyBoolean(), any(Timestamp.class));

        AlertAndNotificationCountDTO result = userNotificationMappingService.getAlertAndNotificationCount(dto);

        assertEquals(dto.getUserID(), result.getUserId());
        assertEquals(emptyList, result.getUserNotificationTypeList());
        verify(mockLogger, times(2)).info(Mockito.contains("getAlertAndNotificationCount"));
    }

@Test
    void getAlertAndNotificationCount_shouldThrowNullPointerException_whenDTOIsNull() {
        assertThrows(NullPointerException.class, () -> userNotificationMappingService.getAlertAndNotificationCount(null));
    }
@Test
void getMarkQuery_shouldReturnEmptyStringForAnyDto() {
    AlertAndNotificationChangeStatusDTO dto = new AlertAndNotificationChangeStatusDTO();
    // The content of DTO doesn't affect the current implementation of getMarkQuery
    String result = userNotificationMappingService.getMarkQuery(dto);
    assertEquals("", result);
}
@Test
void getMarkQuery_shouldReturnEmptyStringForNullDto() {
    String result = userNotificationMappingService.getMarkQuery(null);
    assertEquals("", result);
}
@Test
void getDeleteQuery_shouldReturnEmptyStringForAnyDto() {
    AlertAndNotificationChangeStatusDTO dto = new AlertAndNotificationChangeStatusDTO();
    // The content of DTO doesn't affect the current implementation of getDeleteQuery
    String result = userNotificationMappingService.getDeleteQuery(dto);
    assertEquals("", result);
}
@Test
void getDeleteQuery_shouldReturnEmptyStringForNullDto() {
    String result = userNotificationMappingService.getDeleteQuery(null);
    assertEquals("", result);
}




@Test
void markNotificationList_shouldHandleNullListForMarkNotificationList() {
    String status = "read";
    List<Integer> notificationIds = null;

    String result = userNotificationMappingService.markNotificationList(status, notificationIds);

    assertEquals("success", result);
    verify(repo, times(1)).updateUserNotificationMappingList(status, null);
}
@Test
void getAlertAndNotificationCount_shouldReturnCountWithWorkingLocationID() {
    UserNotificationDisplayMinDTO dto = new UserNotificationDisplayMinDTO();
    dto.setUserID(101);
    dto.setRoleID(201);
    dto.setProviderServiceMapID(301);
    dto.setWorkingLocationID(401); // Not null

    AlertAndNotificationCount countA = new AlertAndNotificationCount(1, "TypeA", null, 5L);
    // If setters are needed, use them as below:
    // countA.setTypeId(1);
    // countA.setTypeName("TypeA");
    // countA.setCount(5L);

    AlertAndNotificationCount countB = new AlertAndNotificationCount(2, "TypeB", null, 10L);
    // countB.setTypeId(2);
    // countB.setTypeName("TypeB");
    // countB.setCount(10L);

    List<AlertAndNotificationCount> mockList = Arrays.asList(countA, countB);

    when(repo.getShortDisplayFormatWithWorkLocation(anyInt(), anyInt(), anyInt(), anyInt(), anyString(), anyBoolean(), any(Timestamp.class)))
        .thenReturn(mockList);

    AlertAndNotificationCountDTO result = userNotificationMappingService.getAlertAndNotificationCount(dto);

    assertNotNull(result);
    assertEquals(dto.getUserID(), result.getUserId());
    assertEquals(mockList, result.getUserNotificationTypeList());

    verify(repo, times(1)).getShortDisplayFormatWithWorkLocation(
        eq(dto.getUserID()), eq(dto.getRoleID()), eq(dto.getProviderServiceMapID()), eq(dto.getWorkingLocationID()), eq("unread"), eq(false), any(Timestamp.class));
    verify(repo, never()).getShortDisplayFormat(anyInt(), anyInt(), anyInt(), anyString(), anyBoolean(), any(Timestamp.class));
}
@Test
void getAlertAndNotificationCount_shouldReturnCountWithoutWorkingLocationID() {
    UserNotificationDisplayMinDTO dto = new UserNotificationDisplayMinDTO();
    dto.setUserID(102);
    dto.setRoleID(202);
    dto.setProviderServiceMapID(302);
    dto.setWorkingLocationID(null); // Null

    AlertAndNotificationCount countC = new AlertAndNotificationCount(3, "TypeC", null, 7L);
    List<AlertAndNotificationCount> mockList = Arrays.asList(countC);

    when(repo.getShortDisplayFormat(anyInt(), anyInt(), anyInt(), anyString(), anyBoolean(), any(Timestamp.class)))
        .thenReturn(mockList);

    AlertAndNotificationCountDTO result = userNotificationMappingService.getAlertAndNotificationCount(dto);

    assertNotNull(result);
    assertEquals(dto.getUserID(), result.getUserId());
    assertEquals(mockList, result.getUserNotificationTypeList());

    verify(repo, times(1)).getShortDisplayFormat(
        eq(dto.getUserID()), eq(dto.getRoleID()), eq(dto.getProviderServiceMapID()), eq("unread"), eq(false), any(Timestamp.class));
    verify(repo, never()).getShortDisplayFormatWithWorkLocation(anyInt(), anyInt(), anyInt(), anyInt(), anyString(), anyBoolean(), any(Timestamp.class));
}
@Test
void getAlertAndNotificationCount_shouldHandleEmptyListFromRepo() {
    UserNotificationDisplayMinDTO dto = new UserNotificationDisplayMinDTO();
    dto.setUserID(103);
    dto.setRoleID(203);
    dto.setProviderServiceMapID(303);
    dto.setWorkingLocationID(403);

    when(repo.getShortDisplayFormatWithWorkLocation(anyInt(), anyInt(), anyInt(), anyInt(), anyString(), anyBoolean(), any(Timestamp.class)))
        .thenReturn(Collections.emptyList());

    AlertAndNotificationCountDTO result = userNotificationMappingService.getAlertAndNotificationCount(dto);

    assertNotNull(result);
    assertEquals(dto.getUserID(), result.getUserId());
    assertTrue(result.getUserNotificationTypeList().isEmpty());

    verify(repo, times(1)).getShortDisplayFormatWithWorkLocation(
        eq(dto.getUserID()), eq(dto.getRoleID()), eq(dto.getProviderServiceMapID()), eq(dto.getWorkingLocationID()), eq("unread"), eq(false), any(Timestamp.class));
}
@Test
void getAlertAndNotificationCount_shouldThrowNullPointerExceptionForNullDto() {
    assertThrows(NullPointerException.class, () -> {
        userNotificationMappingService.getAlertAndNotificationCount(null);
    });
}

@Test
void getAlertAndNotificationDetail_shouldCallRepoWithWorkingLocationID_whenPresent() {
    // Arrange
    UserNotificationDisplayMaxDTO dto = new UserNotificationDisplayMaxDTO();
    dto.setUserID(1);
    dto.setRoleID(10);
    dto.setProviderServiceMapID(100);
    dto.setNotificationTypeID(1000);
    dto.setWorkingLocationID(500); // WorkingLocationID is present

    List<UserNotificationMapping> mockList = Arrays.asList(new UserNotificationMapping(), new UserNotificationMapping());
    when(repo.findByUserIDAndRoleIDAndProviderServiceMapIDAndNotificationTypeIDAndWorkingLocationIDAndDeleted(
            anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyBoolean(), any(Timestamp.class)))
            .thenReturn(mockList);

    // Act
    List<UserNotificationMapping> result = userNotificationMappingService.getAlertAndNotificationDetail(dto);

    // Assert
    assertNotNull(result);
    assertEquals(mockList.size(), result.size());
    assertEquals(mockList, result);

    verify(repo, times(1)).findByUserIDAndRoleIDAndProviderServiceMapIDAndNotificationTypeIDAndWorkingLocationIDAndDeleted(
            eq(dto.getUserID()), eq(dto.getRoleID()), eq(dto.getProviderServiceMapID()),
            eq(dto.getNotificationTypeID()), eq(dto.getWorkingLocationID()), eq(false), any(Timestamp.class));
    verify(repo, never()).findByUserIDAndRoleIDAndProviderServiceMapIDAndNotificationTypeIDAndDeleted(
            anyInt(), anyInt(), anyInt(), anyInt(), anyBoolean(), any(Timestamp.class));

    verify(mockLogger, times(1)).info("UserNotificationMappingService -> getAlertAndNotificationDetail start");
    verify(mockLogger, times(1)).info("UserNotificationMappingService -> getAlertAndNotificationDetail finish");
}

@Test
void getAlertAndNotificationDetail_shouldCallRepoWithoutWorkingLocationID_whenNull() {
    // Arrange
    UserNotificationDisplayMaxDTO dto = new UserNotificationDisplayMaxDTO();
    dto.setUserID(2);
    dto.setRoleID(20);
    dto.setProviderServiceMapID(200);
    dto.setNotificationTypeID(2000);
    dto.setWorkingLocationID(null); // WorkingLocationID is null

    List<UserNotificationMapping> mockList = Arrays.asList(new UserNotificationMapping());
    when(repo.findByUserIDAndRoleIDAndProviderServiceMapIDAndNotificationTypeIDAndDeleted(
            anyInt(), anyInt(), anyInt(), anyInt(), anyBoolean(), any(Timestamp.class)))
            .thenReturn(mockList);

    // Act
    List<UserNotificationMapping> result = userNotificationMappingService.getAlertAndNotificationDetail(dto);

    // Assert
    assertNotNull(result);
    assertEquals(mockList.size(), result.size());
    assertEquals(mockList, result);

    verify(repo, times(1)).findByUserIDAndRoleIDAndProviderServiceMapIDAndNotificationTypeIDAndDeleted(
            eq(dto.getUserID()), eq(dto.getRoleID()), eq(dto.getProviderServiceMapID()),
            eq(dto.getNotificationTypeID()), eq(false), any(Timestamp.class));
    verify(repo, never()).findByUserIDAndRoleIDAndProviderServiceMapIDAndNotificationTypeIDAndWorkingLocationIDAndDeleted(
            anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyBoolean(), any(Timestamp.class));

    verify(mockLogger, times(1)).info("UserNotificationMappingService -> getAlertAndNotificationDetail start");
    verify(mockLogger, times(1)).info("UserNotificationMappingService -> getAlertAndNotificationDetail finish");
}

@Test
void getAlertAndNotificationDetail_shouldReturnEmptyList_whenRepoReturnsEmpty() {
    // Arrange
    UserNotificationDisplayMaxDTO dto = new UserNotificationDisplayMaxDTO();
    dto.setUserID(3);
    dto.setRoleID(30);
    dto.setProviderServiceMapID(300);
    dto.setNotificationTypeID(3000);
    dto.setWorkingLocationID(null); // Can be null or not null, behavior is similar for empty list

    when(repo.findByUserIDAndRoleIDAndProviderServiceMapIDAndNotificationTypeIDAndDeleted(
            anyInt(), anyInt(), anyInt(), anyInt(), anyBoolean(), any(Timestamp.class)))
            .thenReturn(Collections.emptyList());

    // Act
    List<UserNotificationMapping> result = userNotificationMappingService.getAlertAndNotificationDetail(dto);

    // Assert
    assertNotNull(result);
    assertTrue(result.isEmpty());

    verify(repo, times(1)).findByUserIDAndRoleIDAndProviderServiceMapIDAndNotificationTypeIDAndDeleted(
            eq(dto.getUserID()), eq(dto.getRoleID()), eq(dto.getProviderServiceMapID()),
            eq(dto.getNotificationTypeID()), eq(false), any(Timestamp.class));

    verify(mockLogger, times(1)).info("UserNotificationMappingService -> getAlertAndNotificationDetail start");
    verify(mockLogger, times(1)).info("UserNotificationMappingService -> getAlertAndNotificationDetail finish");
}

@Test
void getAlertAndNotificationDetail_shouldThrowNullPointerException_whenDTOIsNull() {
    // Act & Assert
    assertThrows(NullPointerException.class, () -> {
        userNotificationMappingService.getAlertAndNotificationDetail(null);
    });
}
@Test
void getUnmarkQuery_shouldReturnEmptyString() {
    // Arrange
    AlertAndNotificationChangeStatusDTO dto = new AlertAndNotificationChangeStatusDTO();
    // The content of DTO doesn't affect the current implementation of getUnmarkQuery

    // Act
    String result = userNotificationMappingService.getUnmarkQuery(dto);

    // Assert
    assertEquals("", result);
}

@Test
void getUnmarkQuery_shouldReturnEmptyString_whenDTOIsNull() {
    // Arrange
    AlertAndNotificationChangeStatusDTO dto = null;

    // Act
    String result = userNotificationMappingService.getUnmarkQuery(dto);

    // Assert
    assertEquals("", result);
}

@Test
void getAlertAndNotificationDetail_shouldReturnEmptyList_whenRepoReturnsEmptyWithWorkingLocationID() {
    UserNotificationDisplayMaxDTO dto = new UserNotificationDisplayMaxDTO();
    dto.setUserID(3);
    dto.setRoleID(30);
    dto.setProviderServiceMapID(300);
    dto.setNotificationTypeID(3000);
    dto.setWorkingLocationID(6000);

    when(repo.findByUserIDAndRoleIDAndProviderServiceMapIDAndNotificationTypeIDAndWorkingLocationIDAndDeleted(
            anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyBoolean(), any(Timestamp.class)))
            .thenReturn(Collections.emptyList());

    List<UserNotificationMapping> result = userNotificationMappingService.getAlertAndNotificationDetail(dto);

    assertNotNull(result);
    assertTrue(result.isEmpty());

    verify(repo, times(1)).findByUserIDAndRoleIDAndProviderServiceMapIDAndNotificationTypeIDAndWorkingLocationIDAndDeleted(
            eq(dto.getUserID()), eq(dto.getRoleID()), eq(dto.getProviderServiceMapID()),
            eq(dto.getNotificationTypeID()), eq(dto.getWorkingLocationID()), eq(false), any(Timestamp.class));
    verify(mockLogger, times(1)).info("UserNotificationMappingService -> getAlertAndNotificationDetail start");
    verify(mockLogger, times(1)).info("UserNotificationMappingService -> getAlertAndNotificationDetail finish");
}
@Test
void getAlertAndNotificationDetail_shouldReturnEmptyList_whenRepoReturnsEmptyWithoutWorkingLocationID() {
    UserNotificationDisplayMaxDTO dto = new UserNotificationDisplayMaxDTO();
    dto.setUserID(4);
    dto.setRoleID(40);
    dto.setProviderServiceMapID(400);
    dto.setNotificationTypeID(4000);
    dto.setWorkingLocationID(null);

    when(repo.findByUserIDAndRoleIDAndProviderServiceMapIDAndNotificationTypeIDAndDeleted(
            anyInt(), anyInt(), anyInt(), anyInt(), anyBoolean(), any(Timestamp.class)))
            .thenReturn(Collections.emptyList());

    List<UserNotificationMapping> result = userNotificationMappingService.getAlertAndNotificationDetail(dto);

    assertNotNull(result);
    assertTrue(result.isEmpty());

    verify(repo, times(1)).findByUserIDAndRoleIDAndProviderServiceMapIDAndNotificationTypeIDAndDeleted(
            eq(dto.getUserID()), eq(dto.getRoleID()), eq(dto.getProviderServiceMapID()),
            eq(dto.getNotificationTypeID()), eq(false), any(Timestamp.class));
    verify(mockLogger, times(1)).info("UserNotificationMappingService -> getAlertAndNotificationDetail start");
    verify(mockLogger, times(1)).info("UserNotificationMappingService -> getAlertAndNotificationDetail finish");
}
}