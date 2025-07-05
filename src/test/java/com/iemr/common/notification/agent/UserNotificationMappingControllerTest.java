// package com.iemr.common.notification.agent;

// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.junit.jupiter.api.Assertions.assertTrue;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.ArgumentMatchers.eq;
// import static org.mockito.Mockito.doThrow;
// import static org.mockito.Mockito.mock;
// import static org.mockito.Mockito.times;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.when;

// import java.lang.reflect.Field;
// import java.util.Arrays;
// import java.util.Collections;
// import java.util.List;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.ArgumentCaptor;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
// import org.slf4j.Logger;

// import com.google.gson.Gson;
// import com.iemr.common.notification.agent.DTO.AlertAndNotificationChangeStatusDTO;
// import com.iemr.common.notification.agent.DTO.AlertAndNotificationCountDTO;
// import com.iemr.common.notification.agent.DTO.AlertAndNotificationSetDeleteDTO;
// import com.iemr.common.notification.agent.DTO.SuccessObjectDTO;
// import com.iemr.common.notification.agent.DTO.UserNotificationDisplayMaxDTO;
// import com.iemr.common.notification.agent.DTO.UserNotificationDisplayMinDTO;
// import com.iemr.common.notification.util.InputMapper;
// import com.iemr.common.utils.response.OutputResponse;

// // Dummy class for UserNotificationMapping.
// // This class is imported in the controller, but its definition is not provided in the snippet.
// // A minimal definition is required for the test class to compile, especially for List<UserNotificationMapping>.
// class UserNotificationMapping {
//     private Integer id;
//     private String message;

//     public UserNotificationMapping(Integer id, String message) {
//         this.id = id;
//         this.message = message;
//     }

//     // Override toString() to match the controller's behavior of calling list.toString()
//     @Override
//     public String toString() {
//         return "UserNotificationMapping [id=" + id + ", message=" + message + "]";
//     }
// }

// @ExtendWith(MockitoExtension.class)
// class UserNotificationMappingControllerTest {

//     @Mock
//     private UserNotificationMappingService notificationService;

//     @InjectMocks
//     private UserNotificationMappingController userNotificationMappingController;

//     private Gson gson;
//     private Logger mockLogger;

//     @BeforeEach
//     void setUp() throws NoSuchFieldException, IllegalAccessException {
//         gson = new Gson();
        
//         // Mock the private final logger field using reflection
//         mockLogger = mock(Logger.class);
//         Field loggerField = UserNotificationMappingController.class.getDeclaredField("logger");
//         loggerField.setAccessible(true); // Allow access to private field
//         loggerField.set(userNotificationMappingController, mockLogger); // Set the mock logger instance
//     }

//     // Test for getAlertsAndNotificationCount method
//     @Test
//     void getAlertsAndNotificationCount_Success() {
//         // Arrange
//         String requestJson = "{\"userID\":1,\"roleID\":2,\"providerServiceMapID\":3,\"workingLocationID\":4}";
//         AlertAndNotificationCountDTO mockDto = new AlertAndNotificationCountDTO();
//         // mockDto.setAlertCount(10);
//         // mockDto.setNotificationCount(5);

//         when(notificationService.getAlertAndNotificationCount(any(UserNotificationDisplayMinDTO.class)))
//                 .thenReturn(mockDto);

//         // Act
//         String result = userNotificationMappingController.getAlertsAndNotificationCount(requestJson);

//         // Assert
//         ArgumentCaptor<UserNotificationDisplayMinDTO> captor = ArgumentCaptor.forClass(UserNotificationDisplayMinDTO.class);
//         verify(notificationService, times(1)).getAlertAndNotificationCount(captor.capture());
//         UserNotificationDisplayMinDTO capturedDto = captor.getValue();
//         assertEquals(1, capturedDto.getUserID(), "User ID should match input");
//         assertEquals(2, capturedDto.getRoleID(), "Role ID should match input");
//         assertEquals(3, capturedDto.getProviderServiceMapID(), "Provider Service Map ID should match input");
//         assertEquals(4, capturedDto.getWorkingLocationID(), "Working Location ID should match input");

//         OutputResponse expectedResponse = new OutputResponse();
//         expectedResponse.setResponse(InputMapper.getInstance().gson().toJson(mockDto));
//         assertEquals(expectedResponse.toString(), result, "Response JSON should match expected success output");

//         verify(mockLogger, times(1)).info("UserNotificationMappingController.getAlertsAndNotificationCount - start");
//         verify(mockLogger, times(1)).info("UserNotificationMappingController.getAlertsAndNotificationCount : json");
//         verify(mockLogger, times(1)).info("UserNotificationMappingController.getAlertsAndNotificationCount: success - finish");
//     }

//     @Test
//     void getAlertsAndNotificationCount_ServiceException_ReturnsError() {
//         // Arrange
//         String requestJson = "{\"userID\":1,\"roleID\":2,\"providerServiceMapID\":3,\"workingLocationID\":4}";
//         RuntimeException serviceException = new RuntimeException("Simulated service error");

//         when(notificationService.getAlertAndNotificationCount(any(UserNotificationDisplayMinDTO.class)))
//                 .thenThrow(serviceException);

//         // Act
//         String result = userNotificationMappingController.getAlertsAndNotificationCount(requestJson);

//         // Assert
//         OutputResponse expectedResponse = new OutputResponse();
//         expectedResponse.setError(serviceException); 
//         assertEquals(expectedResponse.toString(), result, "Response JSON should indicate error from service exception");

//         verify(mockLogger, times(1)).info("UserNotificationMappingController.getAlertsAndNotificationCount - start");
//         verify(mockLogger, times(1)).info("UserNotificationMappingController.getAlertsAndNotificationCount : json");
//         verify(mockLogger, times(1)).error(eq("UserNotificationMappingController.getAlertsAndNotificationCount: failure - finish"), eq("Simulated service error"));
//     }

//     @Test
//     void getAlertsAndNotificationCount_InvalidJson_ReturnsError() {
//         // Arrange
//         String requestJson = "invalid json format"; 

//         // Act
//         String result = userNotificationMappingController.getAlertsAndNotificationCount(requestJson);

//         // Assert
//         assertTrue(result.contains("\"status\":\"error\""), "Response should indicate an error status");
//         assertTrue(result.contains("com.google.gson.JsonSyntaxException"), "Error message should contain JsonSyntaxException");
//         verify(mockLogger, times(1)).error(eq("UserNotificationMappingController.getAlertsAndNotificationCount: failure - finish"), any(String.class));
//     }

//     // Test for getAlertsAndNotificationDetail method
//     @Test
//     void getAlertsAndNotificationDetail_Success() {
//         // Arrange
//         String requestJson = "{\"userID\":1,\"roleID\":2,\"notificationTypeID\":5,\"providerServiceMapID\":3,\"workingLocationID\":4}";
//         List<UserNotificationMapping> mockList = Arrays.asList(
//                 new UserNotificationMapping(101, "Alert Message 1"),
//                 new UserNotificationMapping(102, "Notification Message 2")
//         );

//         when(notificationService.getAlertAndNotificationDetail(any(UserNotificationDisplayMaxDTO.class)))
//                 .thenReturn(mockList);

//         // Act
//         String result = userNotificationMappingController.getAlertsAndNotificationDetail(requestJson);

//         // Assert
//         ArgumentCaptor<UserNotificationDisplayMaxDTO> captor = ArgumentCaptor.forClass(UserNotificationDisplayMaxDTO.class);
//         verify(notificationService, times(1)).getAlertAndNotificationDetail(captor.capture());
//         UserNotificationDisplayMaxDTO capturedDto = captor.getValue();
//         assertEquals(1, capturedDto.getUserID(), "User ID should match input");
//         assertEquals(2, capturedDto.getRoleID(), "Role ID should match input");
//         assertEquals(5, capturedDto.getNotificationTypeID(), "Notification Type ID should match input");
//         assertEquals(3, capturedDto.getProviderServiceMapID(), "Provider Service Map ID should match input");
//         assertEquals(4, capturedDto.getWorkingLocationID(), "Working Location ID should match input");

//         OutputResponse expectedResponse = new OutputResponse();
//         expectedResponse.setResponse(mockList.toString()); // Controller uses list.toString()
//         assertEquals(expectedResponse.toString(), result, "Response JSON should match expected success output with list.toString()");

//         verify(mockLogger, times(1)).info("UserNotificationMappingController.getAlertsAndNotificationDetail - start");
//         verify(mockLogger, times(1)).info("UserNotificationMappingController.getAlertsAndNotificationDetail: success - finish");
//     }

//     @Test
//     void getAlertsAndNotificationDetail_ServiceException_ReturnsError() {
//         // Arrange
//         String requestJson = "{\"userID\":1,\"roleID\":2,\"notificationTypeID\":5,\"providerServiceMapID\":3,\"workingLocationID\":4}";
//         RuntimeException serviceException = new RuntimeException("Detail service error");

//         when(notificationService.getAlertAndNotificationDetail(any(UserNotificationDisplayMaxDTO.class)))
//                 .thenThrow(serviceException);

//         // Act
//         String result = userNotificationMappingController.getAlertsAndNotificationDetail(requestJson);

//         // Assert
//         OutputResponse expectedResponse = new OutputResponse();
//         expectedResponse.setError(serviceException);
//         assertEquals(expectedResponse.toString(), result, "Response JSON should indicate error from service exception");

//         verify(mockLogger, times(1)).info("UserNotificationMappingController.getAlertsAndNotificationDetail - start");
//         // The original code's catch block for this method does not log an error message, only info (which is commented out).
//         // So, we verify no error log is called.
//         verify(mockLogger, times(0)).error(any(String.class), any(String.class)); 
//     }

//     @Test
//     void getAlertsAndNotificationDetail_InvalidJson_ReturnsError() {
//         // Arrange
//         String requestJson = "malformed json";

//         // Act
//         String result = userNotificationMappingController.getAlertsAndNotificationDetail(requestJson);

//         // Assert
//         assertTrue(result.contains("\"status\":\"error\""), "Response should indicate an error status");
//         assertTrue(result.contains("com.google.gson.JsonSyntaxException"), "Error message should contain JsonSyntaxException");
//         // The original code's catch block for this method does not log an error message, only info (which is commented out).
//         verify(mockLogger, times(0)).error(any(String.class), any(String.class));
//     }

//     // Test for changeNotificationStatus method
//     @Test
//     void changeNotificationStatus_SingleId_Success() {
//         // Arrange
//         String requestJson = "{\"notficationStatus\":\"READ\",\"userNotificationMapIDList\":[101]}";
//         String status = "READ";
//         Integer id = 101;

//         // Act
//         String result = userNotificationMappingController.changeNotificationStatus(requestJson);

//         // Assert
//         verify(notificationService, times(1)).markNotificationSingle(status, id);
//         verify(notificationService, times(0)).markNotificationList(any(String.class), any(List.class));

//         SuccessObjectDTO expectedObj = new SuccessObjectDTO();
//         expectedObj.setOperation(status);
//         expectedObj.setStatus("success");
//         OutputResponse expectedResponse = new OutputResponse();
//         expectedResponse.setResponse(InputMapper.getInstance().gson().toJson(expectedObj));
//         assertEquals(expectedResponse.toString(), result, "Response JSON should indicate success for single ID update");

//         verify(mockLogger, times(1)).info("UserNotificationMappingController.changeNotificationStatus - start");
//         verify(mockLogger, times(1)).info("UserNotificationMappingController.changeNotificationStatus: success - finish");
//     }

//     @Test
//     void changeNotificationStatus_MultipleIds_Success() {
//         // Arrange
//         String requestJson = "{\"notficationStatus\":\"ARCHIVED\",\"userNotificationMapIDList\":[101, 102, 103]}";
//         String status = "ARCHIVED";
//         List<Integer> ids = Arrays.asList(101, 102, 103);

//         // Act
//         String result = userNotificationMappingController.changeNotificationStatus(requestJson);

//         // Assert
//         verify(notificationService, times(0)).markNotificationSingle(any(String.class), any(Integer.class));
//         verify(notificationService, times(1)).markNotificationList(status, ids);

//         SuccessObjectDTO expectedObj = new SuccessObjectDTO();
//         expectedObj.setOperation(status);
//         expectedObj.setStatus("success");
//         OutputResponse expectedResponse = new OutputResponse();
//         expectedResponse.setResponse(InputMapper.getInstance().gson().toJson(expectedObj));
//         assertEquals(expectedResponse.toString(), result, "Response JSON should indicate success for multiple IDs update");

//         verify(mockLogger, times(1)).info("UserNotificationMappingController.changeNotificationStatus - start");
//         verify(mockLogger, times(1)).info("UserNotificationMappingController.changeNotificationStatus: success - finish");
//     }

//     @Test
//     void changeNotificationStatus_NoIds_ReturnsError() {
//         // Arrange
//         String requestJson = "{\"notficationStatus\":\"READ\",\"userNotificationMapIDList\":[]}";

//         // Act
//         String result = userNotificationMappingController.changeNotificationStatus(requestJson);

//         // Assert
//         verify(notificationService, times(0)).markNotificationSingle(any(String.class), any(Integer.class));
//         verify(notificationService, times(0)).markNotificationList(any(String.class), any(List.class));

//         OutputResponse expectedResponse = new OutputResponse();
//         expectedResponse.setError(new Throwable("Missing mandatory Parameter - at least 1 NotificationMapId needed."));
//         assertEquals(expectedResponse.toString(), result, "Response JSON should indicate error for no IDs provided");

//         verify(mockLogger, times(1)).info("UserNotificationMappingController.changeNotificationStatus - start");
//         verify(mockLogger, times(1)).info("UserNotificationMappingController.changeNotificationStatus: failure - finish");
//     }

//     @Test
//     void changeNotificationStatus_ServiceException_ReturnsError() {
//         // Arrange
//         String requestJson = "{\"notficationStatus\":\"READ\",\"userNotificationMapIDList\":[101]}";
//         RuntimeException serviceException = new RuntimeException("Change status service error");
//         doThrow(serviceException).when(notificationService).markNotificationSingle(any(String.class), any(Integer.class));

//         // Act
//         String result = userNotificationMappingController.changeNotificationStatus(requestJson);

//         // Assert
//         OutputResponse expectedResponse = new OutputResponse();
//         expectedResponse.setError(serviceException);
//         assertEquals(expectedResponse.toString(), result, "Response JSON should indicate error from service exception");

//         verify(mockLogger, times(1)).info("UserNotificationMappingController.changeNotificationStatus - start");
//         verify(mockLogger, times(1)).error(eq("UserNotificationMappingController.changeNotificationStatus: failure - finish"), eq("Change status service error"));
//     }

//     @Test
//     void changeNotificationStatus_InvalidJson_ReturnsError() {
//         // Arrange
//         String requestJson = "invalid json for status change";

//         // Act
//         String result = userNotificationMappingController.changeNotificationStatus(requestJson);

//         // Assert
//         assertTrue(result.contains("\"status\":\"error\""), "Response should indicate an error status");
//         assertTrue(result.contains("com.google.gson.JsonSyntaxException"), "Error message should contain JsonSyntaxException");
//         verify(mockLogger, times(1)).error(eq("UserNotificationMappingController.changeNotificationStatus: failure - finish"), any(String.class));
//     }

//     // Test for markDelete method
//     @Test
//     void markDelete_SingleId_Success() {
//         // Arrange
//         String requestJson = "{\"isDeleted\":true,\"userNotificationMapIDList\":[201]}";
//         Boolean isDeleted = true;
//         Integer id = 201;

//         // Act
//         String result = userNotificationMappingController.markDelete(requestJson);

//         // Assert
//         verify(notificationService, times(1)).deleteNotificationSingle(isDeleted, id);
//         verify(notificationService, times(0)).deleteNotificationList(any(Boolean.class), any(List.class));

//         SuccessObjectDTO expectedObj = new SuccessObjectDTO();
//         expectedObj.setOperation("isDeleted = " + isDeleted.toString());
//         expectedObj.setStatus("success");
//         OutputResponse expectedResponse = new OutputResponse();
//         expectedResponse.setResponse(InputMapper.getInstance().gson().toJson(expectedObj));
//         assertEquals(expectedResponse.toString(), result, "Response JSON should indicate success for single ID delete");

//         verify(mockLogger, times(1)).info("UserNotificationMappingController.markDelete - start");
//         verify(mockLogger, times(1)).info("UserNotificationMappingController.markDelete: success - finish");
//     }

//     @Test
//     void markDelete_MultipleIds_Success() {
//         // Arrange
//         String requestJson = "{\"isDeleted\":false,\"userNotificationMapIDList\":[201, 202, 203]}";
//         Boolean isDeleted = false;
//         List<Integer> ids = Arrays.asList(201, 202, 203);

//         // Act
//         String result = userNotificationMappingController.markDelete(requestJson);

//         // Assert
//         verify(notificationService, times(0)).deleteNotificationSingle(any(Boolean.class), any(Integer.class));
//         verify(notificationService, times(1)).deleteNotificationList(isDeleted, ids);

//         SuccessObjectDTO expectedObj = new SuccessObjectDTO();
//         expectedObj.setOperation("isDeleted = " + isDeleted.toString());
//         expectedObj.setStatus("success");
//         OutputResponse expectedResponse = new OutputResponse();
//         expectedResponse.setResponse(InputMapper.getInstance().gson().toJson(expectedObj));
//         assertEquals(expectedResponse.toString(), result, "Response JSON should indicate success for multiple IDs delete");

//         verify(mockLogger, times(1)).info("UserNotificationMappingController.markDelete - start");
//         verify(mockLogger, times(1)).info("UserNotificationMappingController.markDelete: success - finish");
//     }

//     @Test
//     void markDelete_NoIds_ReturnsError() {
//         // Arrange
//         String requestJson = "{\"isDeleted\":true,\"userNotificationMapIDList\":[]}";

//         // Act
//         String result = userNotificationMappingController.markDelete(requestJson);

//         // Assert
//         verify(notificationService, times(0)).deleteNotificationSingle(any(Boolean.class), any(Integer.class));
//         verify(notificationService, times(0)).deleteNotificationList(any(Boolean.class), any(List.class));

//         OutputResponse expectedResponse = new OutputResponse();
//         expectedResponse.setError(new Throwable("Missing mandatory Parameter - at least 1 NotificationMapId needed."));
//         assertEquals(expectedResponse.toString(), result, "Response JSON should indicate error for no IDs provided");

//         verify(mockLogger, times(1)).info("UserNotificationMappingController.markDelete - start");
//         verify(mockLogger, times(1)).info("UserNotificationMappingController.markDelete: failure - finish");
//     }

//     @Test
//     void markDelete_ServiceException_ReturnsError() {
//         // Arrange
//         String requestJson = "{\"isDeleted\":true,\"userNotificationMapIDList\":[201]}";
//         RuntimeException serviceException = new RuntimeException("Delete service error");
//         doThrow(serviceException).when(notificationService).deleteNotificationSingle(any(Boolean.class), any(Integer.class));

//         // Act
//         String result = userNotificationMappingController.markDelete(requestJson);

//         // Assert
//         OutputResponse expectedResponse = new OutputResponse();
//         expectedResponse.setError(serviceException);
//         assertEquals(expectedResponse.toString(), result, "Response JSON should indicate error from service exception");

//         verify(mockLogger, times(1)).info("UserNotificationMappingController.markDelete - start");
//         // The original code's catch block for this method logs info, not error.
//         verify(mockLogger, times(1)).info("UserNotificationMappingController.markDelete: failure - finish"); 
//     }

//     @Test
//     void markDelete_InvalidJson_ReturnsError() {
//         // Arrange
//         String requestJson = "invalid json for delete";

//         // Act
//         String result = userNotificationMappingController.markDelete(requestJson);

//         // Assert
//         assertTrue(result.contains("\"status\":\"error\""), "Response should indicate an error status");
//         assertTrue(result.contains("com.google.gson.JsonSyntaxException"), "Error message should contain JsonSyntaxException");
//         // The original code's catch block for this method logs info, not error.
//         verify(mockLogger, times(1)).info("UserNotificationMappingController.markDelete: failure - finish");
//     }
// }`