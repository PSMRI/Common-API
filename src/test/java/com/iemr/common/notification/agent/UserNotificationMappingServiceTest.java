// package com.iemr.common.notification.agent;

// import com.iemr.common.notification.agent.DTO.AlertAndNotificationChangeStatusDTO;
// import com.iemr.common.notification.agent.DTO.AlertAndNotificationCount;
// import com.iemr.common.notification.agent.DTO.AlertAndNotificationCountDTO;
// import com.iemr.common.notification.agent.DTO.UserNotificationDisplayMaxDTO;
// import com.iemr.common.notification.agent.DTO.UserNotificationDisplayMinDTO;
// import com.fasterxml.jackson.core.JsonProcessingException; // For testing exception
// import com.fasterxml.jackson.databind.ObjectMapper; // For testing exception

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.ArgumentCaptor;
// import org.mockito.Captor;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
// import org.slf4j.Logger;
// import org.springframework.test.util.ReflectionTestUtils;

// import java.sql.Timestamp;
// import java.util.Arrays;
// import java.util.Collections;
// import java.util.List;
// import java.util.ArrayList;

// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.junit.jupiter.api.Assertions.assertNotNull;
// import static org.junit.jupiter.api.Assertions.assertTrue;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.ArgumentMatchers.anyBoolean;
// import static org.mockito.ArgumentMatchers.anyInt;
// import static org.mockito.ArgumentMatchers.anyList;
// import static org.mockito.ArgumentMatchers.anyString;
// import static org.mockito.ArgumentMatchers.eq;
// import static org.mockito.Mockito.doNothing;
// import static org.mockito.Mockito.times;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.when;

// @ExtendWith(MockitoExtension.class)
// class UserNotificationMappingServiceTest {

//     @Mock
//     private UserNotificationMappingRepo repo;

//     @InjectMocks
//     private UserNotificationMappingService service;

//     @Mock
//     private Logger mockLogger; // Mock for the logger field in the service

//     @Captor
//     private ArgumentCaptor<String> logMessageCaptor;

//     @BeforeEach
//     void setUp() {
//         // Use ReflectionTestUtils to inject the mock logger into the final field.
//         // This is necessary because the logger is initialized directly in the class
//         // and not via constructor injection or a setter, making it hard to mock
//         // with standard Mockito @Mock/@InjectMocks without this workaround.
//         ReflectionTestUtils.setField(service, "logger", mockLogger);
//     }

//     @Test
//     @DisplayName("getAlertAndNotificationCount - When workingLocationID is present, should call repo.getShortDisplayFormatWithWorkLocation")
//     void getAlertAndNotificationCount_WhenWorkingLocationIDPresent_ShouldCallCorrectRepoMethod() {
//         // Arrange
//         UserNotificationDisplayMinDTO dto = new UserNotificationDisplayMinDTO();
//         dto.setUserID(1);
//         dto.setRoleID(10);
//         dto.setProviderServiceMapID(100);
//         dto.setWorkingLocationID(1000);

//         List<AlertAndNotificationCount> mockList = Arrays.asList(
//                 new AlertAndNotificationCount("Type1", 5L),
//                 new AlertAndNotificationCount("Type2", 3L)
//         );

//         when(repo.getShortDisplayFormatWithWorkLocation(
//                 eq(dto.getUserID()), eq(dto.getRoleID()), eq(dto.getProviderServiceMapID()),
//                 eq(dto.getWorkingLocationID()), eq("unread"), eq(false), any(Timestamp.class)))
//                 .thenReturn(mockList);

//         // Act
//         AlertAndNotificationCountDTO result = service.getAlertAndNotificationCount(dto);

//         // Assert
//         assertNotNull(result, "Result should not be null");
//         assertEquals(dto.getUserID(), result.getUserId(), "User ID should match");
//         assertEquals(mockList.size(), result.getUserNotificationTypeList().size(), "List size should match");
//         assertEquals(mockList.get(0).getNotificationType(), result.getUserNotificationTypeList().get(0).getNotificationType(), "Notification type should match");
//         assertEquals(mockList.get(0).getNotificationCount(), result.getUserNotificationTypeList().get(0).getNotificationCount(), "Notification count should match");

//         verify(repo, times(1)).getShortDisplayFormatWithWorkLocation(
//                 eq(dto.getUserID()), eq(dto.getRoleID()), eq(dto.getProviderServiceMapID()),
//                 eq(dto.getWorkingLocationID()), eq("unread"), eq(false), any(Timestamp.class));
//         verify(repo, times(0)).getShortDisplayFormat(anyInt(), anyInt(), anyInt(), anyString(), anyBoolean(), any(Timestamp.class));

//         verify(mockLogger, times(2)).info(logMessageCaptor.capture());
//         List<String> capturedLogs = logMessageCaptor.getAllValues();
//         assertTrue(capturedLogs.get(0).contains("getAlertAndNotificationCount start"), "First log should indicate method start");
//         assertTrue(capturedLogs.get(1).contains("getAlertAndNotificationCount start"), "Second log should indicate method start (as per original code's double logging)");
//     }

//     @Test
//     @DisplayName("getAlertAndNotificationCount - When workingLocationID is null, should call repo.getShortDisplayFormat")
//     void getAlertAndNotificationCount_WhenWorkingLocationIDNull_ShouldCallCorrectRepoMethod() {
//         // Arrange
//         UserNotificationDisplayMinDTO dto = new UserNotificationDisplayMinDTO();
//         dto.setUserID(2);
//         dto.setRoleID(20);
//         dto.setProviderServiceMapID(200);
//         dto.setWorkingLocationID(null); // Null working location ID

//         List<AlertAndNotificationCount> mockList = Arrays.asList(
//                 new AlertAndNotificationCount("TypeA", 10L),
//                 new AlertAndNotificationCount("TypeB", 7L)
//         );

//         when(repo.getShortDisplayFormat(
//                 eq(dto.getUserID()), eq(dto.getRoleID()), eq(dto.getProviderServiceMapID()),
//                 eq("unread"), eq(false), any(Timestamp.class)))
//                 .thenReturn(mockList);

//         // Act
//         AlertAndNotificationCountDTO result = service.getAlertAndNotificationCount(dto);

//         // Assert
//         assertNotNull(result, "Result should not be null");
//         assertEquals(dto.getUserID(), result.getUserId(), "User ID should match");
//         assertEquals(mockList.size(), result.getUserNotificationTypeList().size(), "List size should match");

//         verify(repo, times(0)).getShortDisplayFormatWithWorkLocation(anyInt(), anyInt(), anyInt(), anyInt(), anyString(), anyBoolean(), any(Timestamp.class));
//         verify(repo, times(1)).getShortDisplayFormat(
//                 eq(dto.getUserID()), eq(dto.getRoleID()), eq(dto.getProviderServiceMapID()),
//                 eq("unread"), eq(false), any(Timestamp.class));

//         verify(mockLogger, times(2)).info(logMessageCaptor.capture());
//         List<String> capturedLogs = logMessageCaptor.getAllValues();
//         assertTrue(capturedLogs.get(0).contains("getAlertAndNotificationCount start"), "First log should indicate method start");
//         assertTrue(capturedLogs.get(1).contains("getAlertAndNotificationCount start"), "Second log should indicate method start (as per original code's double logging)");
//     }

//     @Test
//     @DisplayName("getAlertAndNotificationDetail - When workingLocationID is present, should call repo.findByUserIDAndRoleIDAndProviderServiceMapIDAndNotificationTypeIDAndWorkingLocationIDAndDeleted")
//     void getAlertAndNotificationDetail_WhenWorkingLocationIDPresent_ShouldCallCorrectRepoMethod() {
//         // Arrange
//         UserNotificationDisplayMaxDTO dto = new UserNotificationDisplayMaxDTO();
//         dto.setUserID(1);
//         dto.setRoleID(10);
//         dto.setProviderServiceMapID(100);
//         dto.setNotificationTypeID(1);
//         dto.setWorkingLocationID(1000);

//         List<UserNotificationMapping> mockList = Arrays.asList(new UserNotificationMapping(), new UserNotificationMapping());

//         when(repo.findByUserIDAndRoleIDAndProviderServiceMapIDAndNotificationTypeIDAndWorkingLocationIDAndDeleted(
//                 eq(dto.getUserID()), eq(dto.getRoleID()), eq(dto.getProviderServiceMapID()),
//                 eq(dto.getNotificationTypeID()), eq(dto.getWorkingLocationID()), eq(false), any(Timestamp.class)))
//                 .thenReturn(mockList);

//         // Act
//         List<UserNotificationMapping> result = service.getAlertAndNotificationDetail(dto);

//         // Assert
//         assertNotNull(result, "Result list should not be null");
//         assertEquals(mockList.size(), result.size(), "Result list size should match");

//         verify(repo, times(1)).findByUserIDAndRoleIDAndProviderServiceMapIDAndNotificationTypeIDAndWorkingLocationIDAndDeleted(
//                 eq(dto.getUserID()), eq(dto.getRoleID()), eq(dto.getProviderServiceMapID()),
//                 eq(dto.getNotificationTypeID()), eq(dto.getWorkingLocationID()), eq(false), any(Timestamp.class));
//         verify(repo, times(0)).findByUserIDAndRoleIDAndProviderServiceMapIDAndNotificationTypeIDAndDeleted(anyInt(), anyInt(), anyInt(), anyInt(), anyBoolean(), any(Timestamp.class));

//         verify(mockLogger, times(2)).info(logMessageCaptor.capture());
//         List<String> capturedLogs = logMessageCaptor.getAllValues();
//         assertTrue(capturedLogs.get(0).contains("getAlertAndNotificationDetail start"), "First log should indicate method start");
//         assertTrue(capturedLogs.get(1).contains("getAlertAndNotificationDetail finish"), "Second log should indicate method finish");
//     }

//     @Test
//     @DisplayName("getAlertAndNotificationDetail - When workingLocationID is null, should call repo.findByUserIDAndRoleIDAndProviderServiceMapIDAndNotificationTypeIDAndDeleted")
//     void getAlertAndNotificationDetail_WhenWorkingLocationIDNull_ShouldCallCorrectRepoMethod() {
//         // Arrange
//         UserNotificationDisplayMaxDTO dto = new UserNotificationDisplayMaxDTO();
//         dto.setUserID(2);
//         dto.setRoleID(20);
//         dto.setProviderServiceMapID(200);
//         dto.setNotificationTypeID(2);
//         dto.setWorkingLocationID(null); // Null working location ID

//         List<UserNotificationMapping> mockList = Collections.singletonList(new UserNotificationMapping());

//         when(repo.findByUserIDAndRoleIDAndProviderServiceMapIDAndNotificationTypeIDAndDeleted(
//                 eq(dto.getUserID()), eq(dto.getRoleID()), eq(dto.getProviderServiceMapID()),
//                 eq(dto.getNotificationTypeID()), eq(false), any(Timestamp.class)))
//                 .thenReturn(mockList);

//         // Act
//         List<UserNotificationMapping> result = service.getAlertAndNotificationDetail(dto);

//         // Assert
//         assertNotNull(result, "Result list should not be null");
//         assertEquals(mockList.size(), result.size(), "Result list size should match");

//         verify(repo, times(0)).findByUserIDAndRoleIDAndProviderServiceMapIDAndNotificationTypeIDAndWorkingLocationIDAndDeleted(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyBoolean(), any(Timestamp.class));
//         verify(repo, times(1)).findByUserIDAndRoleIDAndProviderServiceMapIDAndNotificationTypeIDAndDeleted(
//                 eq(dto.getUserID()), eq(dto.getRoleID()), eq(dto.getProviderServiceMapID()),
//                 eq(dto.getNotificationTypeID()), eq(false), any(Timestamp.class));

//         verify(mockLogger, times(2)).info(logMessageCaptor.capture());
//         List<String> capturedLogs = logMessageCaptor.getAllValues();
//         assertTrue(capturedLogs.get(0).contains("getAlertAndNotificationDetail start"), "First log should indicate method start");
//         assertTrue(capturedLogs.get(1).contains("getAlertAndNotificationDetail finish"), "Second log should indicate method finish");
//     }

//     @Test
//     @DisplayName("markNotificationSingle - Should call repo.updateUserNotificationMappingSingle and return success")
//     void markNotificationSingle_ShouldUpdateAndReturnSuccess() {
//         // Arrange
//         String status = "read";
//         Integer userNotificationMapID = 123;
//         doNothing().when(repo).updateUserNotificationMappingSingle(anyString(), anyInt());

//         // Act
//         String result = service.markNotificationSingle(status, userNotificationMapID);

//         // Assert
//         assertEquals("success", result, "Result should be 'success'");
//         verify(repo, times(1)).updateUserNotificationMappingSingle(eq(status), eq(userNotificationMapID));
//     }

//     @Test
//     @DisplayName("markNotificationList - Should call repo.updateUserNotificationMappingList and return success")
//     void markNotificationList_ShouldUpdateAndReturnSuccess() {
//         // Arrange
//         String status = "read";
//         List<Integer> userNotificationMapIDList = Arrays.asList(1, 2, 3);
//         doNothing().when(repo).updateUserNotificationMappingList(anyString(), anyList());

//         // Act
//         String result = service.markNotificationList(status, userNotificationMapIDList);

//         // Assert
//         assertEquals("success", result, "Result should be 'success'");
//         verify(repo, times(1)).updateUserNotificationMappingList(eq(status), eq(userNotificationMapIDList));
//     }

//     @Test
//     @DisplayName("deleteNotificationSingle - Should call repo.setDeletedUserNotificationMappingSingle and return success")
//     void deleteNotificationSingle_ShouldSetDeletedAndReturnSuccess() {
//         // Arrange
//         Boolean isDeleted = true;
//         Integer userNotificationMapID = 456;
//         doNothing().when(repo).setDeletedUserNotificationMappingSingle(anyBoolean(), anyInt());

//         // Act
//         String result = service.deleteNotificationSingle(isDeleted, userNotificationMapID);

//         // Assert
//         assertEquals("success", result, "Result should be 'success'");
//         verify(repo, times(1)).setDeletedUserNotificationMappingSingle(eq(isDeleted), eq(userNotificationMapID));
//     }

//     @Test
//     @DisplayName("deleteNotificationList - Should call repo.setDeleteUserNotificationMappingList and return success")
//     void deleteNotificationList_ShouldSetDeletedAndReturnSuccess() {
//         // Arrange
//         Boolean isDeleted = true;
//         List<Integer> userNotificationMapIDList = Arrays.asList(4, 5, 6);
//         doNothing().when(repo).setDeleteUserNotificationMappingList(anyBoolean(), anyList());

//         // Act
//         String result = service.deleteNotificationList(isDeleted, userNotificationMapIDList);

//         // Assert
//         assertEquals("success", result, "Result should be 'success'");
//         verify(repo, times(1)).setDeleteUserNotificationMappingList(eq(isDeleted), eq(userNotificationMapIDList));
//     }

//     @Test
//     @DisplayName("createUserNotificationMapping - Should always return true regardless of input")
//     void createUserNotificationMapping_ShouldAlwaysReturnTrue() {
//         // Arrange
//         List<Integer> userIds1 = Arrays.asList(1, 2, 3);
//         List<Integer> userIds2 = Collections.emptyList();
//         List<Integer> userIds3 = null; // Test with null list

//         // Act & Assert
//         assertTrue(service.createUserNotificationMapping(userIds1), "Should return true for non-empty list");
//         assertTrue(service.createUserNotificationMapping(userIds2), "Should return true for empty list");
//         assertTrue(service.createUserNotificationMapping(userIds3), "Should return true for null list");
//         // No interactions with repo or other mocks expected as the forEach loop is empty
//     }

//     @Test
//     @DisplayName("getJsonAsString - When object can be serialized, should log info message")
//     void getJsonAsString_WhenObjectCanBeSerialized_ShouldLogInfo() {
//         // Arrange
//         String name = "TestObject";
//         Object obj = new SimplePojo("value1", 123); // A simple POJO for serialization

//         // Act
//         service.getJsonAsString(name, obj);

//         // Assert
//         verify(mockLogger, times(2)).info(logMessageCaptor.capture());
//         List<String> capturedLogs = logMessageCaptor.getAllValues();
//         assertTrue(capturedLogs.get(0).contains("getJsonAsString start"), "First log should indicate method start");
//         assertTrue(capturedLogs.get(1).contains("Object: TestObject :toJSON: {\"field1\":\"value1\",\"field2\":123}"), "Second log should contain JSON string");
//         verify(mockLogger, times(0)).error(anyString()); // No error expected
//     }

//     @Test
//     @DisplayName("getJsonAsString - When JsonProcessingException occurs, should log error message")
//     void getJsonAsString_WhenJsonProcessingExceptionOccurs_ShouldLogError() {
//         // Arrange
//         String name = "ErrorObject";
//         // Create an object that will cause JsonProcessingException (specifically JsonMappingException due to circular reference)
//         CircularRef obj = new CircularRef();

//         // Act
//         service.getJsonAsString(name, obj);

//         // Assert
//         verify(mockLogger, times(1)).info(logMessageCaptor.capture()); // Only "start" log before exception
//         assertTrue(logMessageCaptor.getValue().contains("getJsonAsString start"), "Log should contain 'start'");

//         verify(mockLogger, times(1)).error(logMessageCaptor.capture()); // Error log due to exception
//         assertTrue(logMessageCaptor.getValue().contains("Infinite recursion (StackOverflowError)"), "Error log should contain exception message related to circular reference");
//     }

//     // Helper POJO for getJsonAsString test (successful serialization)
//     private static class SimplePojo {
//         public String field1;
//         public int field2;

//         public SimplePojo(String field1, int field2) {
//             this.field1 = field1;
//             this.field2 = field2;
//         }

//         // Getters are needed for ObjectMapper to serialize fields
//         public String getField1() { return field1; }
//         public int getField2() { return field2; }
//     }

//     // Helper class to cause JsonProcessingException (specifically JsonMappingException due to circular reference)
//     private static class CircularRef {
//         public CircularRef self;
//         public CircularRef() {
//             this.self = this;
//         }
//     }

//     @Test
//     @DisplayName("querySelector - When status is 'mark', should call getMarkQuery and return its result")
//     void querySelector_WhenStatusIsMark_ShouldCallGetMarkQuery() {
//         // Arrange
//         AlertAndNotificationChangeStatusDTO dto = new AlertAndNotificationChangeStatusDTO();
//         dto.setNotficationStatus("mark");

//         // Act
//         String result = service.querySelector(dto);

//         // Assert
//         // Since getMarkQuery returns "", we assert for ""
//         assertEquals("", result, "Should return empty string as per getMarkQuery implementation");
//     }

//     @Test
//     @DisplayName("querySelector - When status is 'unmark', should call getUnmarkQuery and return its result")
//     void querySelector_WhenStatusIsUnmark_ShouldCallGetUnmarkQuery() {
//         // Arrange
//         AlertAndNotificationChangeStatusDTO dto = new AlertAndNotificationChangeStatusDTO();
//         dto.setNotficationStatus("unmark");

//         // Act
//         String result = service.querySelector(dto);

//         // Assert
//         assertEquals("", result, "Should return empty string as per getUnmarkQuery implementation");
//     }

//     @Test
//     @DisplayName("querySelector - When status is 'delete', should call getDeleteQuery and return its result")
//     void querySelector_WhenStatusIsDelete_ShouldCallGetDeleteQuery() {
//         // Arrange
//         AlertAndNotificationChangeStatusDTO dto = new AlertAndNotificationChangeStatusDTO();
//         dto.setNotficationStatus("delete");

//         // Act
//         String result = service.querySelector(dto);

//         // Assert
//         assertEquals("", result, "Should return empty string as per getDeleteQuery implementation");
//     }

//     @Test
//     @DisplayName("querySelector - When status is unknown, should return empty string")
//     void querySelector_WhenStatusIsUnknown_ShouldReturnEmptyString() {
//         // Arrange
//         AlertAndNotificationChangeStatusDTO dto = new AlertAndNotificationChangeStatusDTO();
//         dto.setNotficationStatus("unknown"); // An unknown status

//         // Act
//         String result = service.querySelector(dto);

//         // Assert
//         assertEquals("", result, "Should return empty string for unknown status");
//     }

//     @Test
//     @DisplayName("getUnmarkQuery - Should return an empty string as per current implementation")
//     void getUnmarkQuery_ShouldReturnEmptyString() {
//         // Arrange
//         AlertAndNotificationChangeStatusDTO dto = new AlertAndNotificationChangeStatusDTO(); // DTO content doesn't affect current return

//         // Act
//         String result = service.getUnmarkQuery(dto);

//         // Assert
//         assertEquals("", result, "getUnmarkQuery should return an empty string as per current implementation");
//     }

//     @Test
//     @DisplayName("getMarkQuery - Should return an empty string as per current implementation")
//     void getMarkQuery_ShouldReturnEmptyString() {
//         // Arrange
//         AlertAndNotificationChangeStatusDTO dto = new AlertAndNotificationChangeStatusDTO(); // DTO content doesn't affect current return

//         // Act
//         String result = service.getMarkQuery(dto);

//         // Assert
//         assertEquals("", result, "getMarkQuery should return an empty string as per current implementation");
//     }

//     @Test
//     @DisplayName("getDeleteQuery - Should return an empty string as per current implementation")
//     void getDeleteQuery_ShouldReturnEmptyString() {
//         // Arrange
//         AlertAndNotificationChangeStatusDTO dto = new AlertAndNotificationChangeStatusDTO(); // DTO content doesn't affect current return

//         // Act
//         String result = service.getDeleteQuery(dto);

//         // Assert
//         assertEquals("", result, "getDeleteQuery should return an empty string as per current implementation");
//     }
// }