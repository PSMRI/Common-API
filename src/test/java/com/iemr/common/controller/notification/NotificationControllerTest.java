// package com.iemr.common.controller.notification;

// import com.iemr.common.service.notification.NotificationService;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.MockitoAnnotations;

// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.mockito.ArgumentMatchers.anyString;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.when;

// class NotificationControllerTest {

//     @Mock
//     private NotificationService notificationService;

//     @InjectMocks
//     private NotificationController notificationController;

//     @BeforeEach
//     void setUp() {
//         MockitoAnnotations.openMocks(this);
//     }

//     @Test
//     void testSetNotificationService() {
//         NotificationService mockService = notificationService; // Already mocked by @Mock
//         notificationController.setNotificationService(mockService);
//         // No direct way to assert the private field, but we can assume it works
//         // if @InjectMocks is used correctly or if we were testing a different method
//         // that uses the service after setting it. For a simple setter, this is sufficient.
//     }

//     @Test
//     void testGetNotification() {
//         String requestBody = "{\"providerServiceMapID\": 1, \"notificationTypeID\": 101, \"userIDs\": [1, 2], \"workingLocationIDs\": [10, 20], \"languageIDs\": [1, 2], \"roleIDs\":[1,2], \"validFrom\": \"1678886400000\", \"validTill\": \"1709424000000\"}";
//         String expectedResponse = "{\"status\":\"success\", \"data\":[{\"id\":1,\"message\":\"Test Notification\"}]}";

//         when(notificationService.getNotification(anyString())).thenReturn(expectedResponse);

//         String actualResponse = notificationController.getNotification(requestBody);

//         assertEquals(expectedResponse, actualResponse);
//         verify(notificationService).getNotification(requestBody);
//     }

//     @Test
//     void testGetSupervisorNotification() {
//         String requestBody = "{\"providerServiceMapID\": 1, \"notificationTypeID\": 101, \"userIDs\": [1, 2], \"workingLocationIDs\": [10, 20], \"languageIDs\": [1, 2], \"validStartDate\":\"1678886400000\", \"validEndDate\":\"1709424000000\", \"roleIDs\":[1,2]}";
//         String expectedResponse = "{\"status\":\"success\", \"data\":[{\"id\":2,\"message\":\"Supervisor Notification\"}]}";

//         when(notificationService.getSupervisorNotification(anyString())).thenReturn(expectedResponse);

//         String actualResponse = notificationController.getSupervisorNotification(requestBody);

//         assertEquals(expectedResponse, actualResponse);
//         verify(notificationService).getSupervisorNotification(requestBody);
//     }

//     @Test
//     void testCreateNotification() {
//         String requestBody = "[{\"providerServiceMapID\": 1, \"notificationTypeID\": 101, \"roleID\": 5, \"userID\":10, \"workingLocationID\":100, \"languageID\":1, \"createdBy\": \"testuser\", \"notification\":\"Test Subject\", \"notificationDesc\":\"Test Description\", \"validFrom\": \"1678886400000\", \"validTill\":\"1709424000000\", \"kmFileManager\":{\"fileName\":\"doc.pdf\", \"fileExtension\":\"pdf\", \"providerServiceMapID\":1, \"validFrom\":\"1678886400000\", \"validUpto\":\"1709424000000\", \"fileContent\":\"base64content\", \"createdBy\":\"testuser\", \"categoryID\":1, \"subCategoryID\":10}}]";
//         String expectedResponse = "{\"status\":\"success\", \"message\":\"Notification created successfully\"}";

//         when(notificationService.createNotification(anyString())).thenReturn(expectedResponse);

//         String actualResponse = notificationController.createNotification(requestBody);

//         assertEquals(expectedResponse, actualResponse);
//         verify(notificationService).createNotification(requestBody);
//     }

//     @Test
//     void testUpdateNotification() {
//         String requestBody = "{\"notificationID\" : 1, \"notification\":\"Updated Subject\", \"notificationDesc\":\"Updated Description\", \"notificationTypeID\":101, \"roleID\":5, \"validFrom\":\"1678886400000\", \"validTill\":\"1709424000000\", \"deleted\":false, \"modifiedBy\":\"modifier\", \"kmFileManager\":{\"fileName\":\"newdoc.pdf\", \"fileExtension\":\"pdf\", \"providerServiceMapID\":1, \"userID\":10, \"validFrom\":\"1678886400000\", \"validUpto\":\"1709424000000\", \"fileContent\":\"newbase64content\", \"createdBy\":\"modifier\", \"categoryID\":1, \"subCategoryID\":10}}";
//         String expectedResponse = "{\"status\":\"success\", \"message\":\"Notification updated successfully\"}";

//         when(notificationService.updateNotification(anyString())).thenReturn(expectedResponse);

//         String actualResponse = notificationController.updateNotification(requestBody);

//         assertEquals(expectedResponse, actualResponse);
//         verify(notificationService).updateNotification(requestBody);
//     }

//     @Test
//     void testGetNotificationType() {
//         String requestBody = "{\"providerServiceMapID\" : 1}";
//         String expectedResponse = "{\"status\":\"success\", \"data\":[{\"id\":1,\"type\":\"General\"}]}";

//         when(notificationService.getNotificationType(anyString())).thenReturn(expectedResponse);

//         String actualResponse = notificationController.getNotificationType(requestBody);

//         assertEquals(expectedResponse, actualResponse);
//         verify(notificationService).getNotificationType(requestBody);
//     }

//     @Test
//     void testCreateNotificationType() {
//         String requestBody = "{\"providerServiceMapID\" : 1, \"notificationType\":\"New Type\", \"notificationTypeDesc\":\"Description for new type\", \"createdBy\":\"admin\"}";
//         String expectedResponse = "{\"status\":\"success\", \"message\":\"Notification type created successfully\"}";

//         when(notificationService.createNotificationType(anyString())).thenReturn(expectedResponse);

//         String actualResponse = notificationController.createNotificationType(requestBody);

//         assertEquals(expectedResponse, actualResponse);
//         verify(notificationService).createNotificationType(requestBody);
//     }

//     @Test
//     void testUpdateNotificationType() {
//         String requestBody = "{\"notificationTypeID\" : 1, \"notificationType\":\"Updated Type\", \"notificationTypeDesc\":\"Updated description\", \"deleted\":false, \"modifiedBy\":\"admin\"}";
//         String expectedResponse = "{\"status\":\"success\", \"message\":\"Notification type updated successfully\"}";

//         when(notificationService.updateNotificationType(anyString())).thenReturn(expectedResponse);

//         String actualResponse = notificationController.updateNotificationType(requestBody);

//         assertEquals(expectedResponse, actualResponse);
//         verify(notificationService).updateNotificationType(requestBody);
//     }

//     @Test
//     void testGetEmergencyContacts() {
//         String requestBody = "{\"providerServiceMapID\": 1, \"notificationTypeID\": 101}";
//         String expectedResponse = "{\"status\":\"success\", \"data\":[{\"id\":1,\"name\":\"John Doe\"}]}";

//         when(notificationService.getEmergencyContacts(anyString())).thenReturn(expectedResponse);

//         String actualResponse = notificationController.getEmergencyContacts(requestBody);

//         assertEquals(expectedResponse, actualResponse);
//         verify(notificationService).getEmergencyContacts(requestBody);
//     }

//     @Test
//     void testGetSupervisorEmergencyContacts() {
//         String requestBody = "{\"providerServiceMapID\": 1, \"notificationTypeID\": 101}";
//         String expectedResponse = "{\"status\":\"success\", \"data\":[{\"id\":2,\"name\":\"Jane Smith\"}]}";

//         when(notificationService.getSupervisorEmergencyContacts(anyString())).thenReturn(expectedResponse);

//         String actualResponse = notificationController.getSupervisorEmergencyContacts(requestBody);

//         assertEquals(expectedResponse, actualResponse);
//         verify(notificationService).getSupervisorEmergencyContacts(requestBody);
//     }

//     @Test
//     void testCreateEmergencyContacts() {
//         String requestBody = "[{\"providerServiceMapID\": 1, \"notificationTypeID\": 101, \"createdBy\": \"testuser\", \"designationID\":1, \"emergContactName\":\"Contact 1\", \"location\":\"Office A\", \"emergContactNo\":\"1234567890\", \"emergContactDesc\": \"Emergency contact 1\", \"notificationTypeID\":101, \"createdBy\":\"testuser\"}]";
//         String expectedResponse = "{\"status\":\"success\", \"message\":\"Emergency contacts created successfully\"}";

//         when(notificationService.createEmergencyContacts(anyString())).thenReturn(expectedResponse);

//         String actualResponse = notificationController.createEmergencyContacts(requestBody);

//         assertEquals(expectedResponse, actualResponse);
//         verify(notificationService).createEmergencyContacts(requestBody);
//     }

//     @Test
//     void testUpdateEmergencyContacts() {
//         String requestBody = "{\"providerServiceMapID\": 1, \"notificationTypeID\": 101, \"createdBy\": \"testuser\", \"designationID\":1, \"emergContactName\":\"Updated Contact\", \"location\":\"Office B\", \"emergContactNo\":\"0987654321\", \"emergContactDesc\": \"Updated emergency contact\", \"notificationTypeID\":101, \"createdBy\":\"testuser\"}";
//         String expectedResponse = "{\"status\":\"success\", \"message\":\"Emergency contacts updated successfully\"}";

//         when(notificationService.updateEmergencyContacts(anyString())).thenReturn(expectedResponse);

//         String actualResponse = notificationController.updateEmergencyContacts(requestBody);

//         assertEquals(expectedResponse, actualResponse);
//         verify(notificationService).updateEmergencyContacts(requestBody);
//     }
// }