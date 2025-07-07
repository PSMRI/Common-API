/*
* AMRIT – Accessible Medical Records via Integrated Technology 
* Integrated EHR (Electronic Health Records) Solution 
*
* Copyright (C) "Piramal Swasthya Management and Research Institute" 
*
* This file is part of AMRIT.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see https://www.gnu.org/licenses/.
*/
package com.iemr.common.controller.notification;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iemr.common.service.notification.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationController notificationController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(notificationController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testGetNotification_Success() throws Exception {
        String requestBody = "{\"providerServiceMapID\": 1, \"notificationTypeID\": 101, \"userIDs\": [1, 2], \"workingLocationIDs\": [10, 20], \"languageIDs\": [1, 2], \"roleIDs\":[1,2], \"validFrom\": \"1678886400000\", \"validTill\": \"1709424000000\"}";
        String serviceResponse = "[{\"id\":1,\"message\":\"Test Notification\"}]";
        
        when(notificationService.getNotification(anyString())).thenReturn(serviceResponse);

        MvcResult result = mockMvc.perform(post("/notification/getNotification")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer token")
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        JsonNode jsonResponse = objectMapper.readTree(responseBody);
        
        assertEquals(200, jsonResponse.get("statusCode").asInt());
        assertEquals("Success", jsonResponse.get("status").asText());
        assertNotNull(jsonResponse.get("data"));
        
        verify(notificationService).getNotification(requestBody);
    }

    @Test
    void testGetNotification_Exception() throws Exception {
        String requestBody = "{\"providerServiceMapID\": 1, \"notificationTypeID\": 101}";
        
        when(notificationService.getNotification(anyString())).thenThrow(new RuntimeException("Service error"));

        MvcResult result = mockMvc.perform(post("/notification/getNotification")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer token")
                .content(requestBody))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        JsonNode jsonResponse = objectMapper.readTree(responseBody);
        
        assertEquals(5000, jsonResponse.get("statusCode").asInt());
        assertTrue(jsonResponse.get("status").asText().startsWith("Failed with"));
        assertTrue(jsonResponse.get("errorMessage").asText().contains("Service error"));
        
        verify(notificationService).getNotification(requestBody);
    }

    @Test
    void testGetSupervisorNotification_Success() throws Exception {
        String requestBody = "{\"providerServiceMapID\": 1, \"notificationTypeID\": 101, \"userIDs\": [1, 2], \"workingLocationIDs\": [10, 20], \"languageIDs\": [1, 2], \"validStartDate\":\"1678886400000\", \"validEndDate\":\"1709424000000\", \"roleIDs\":[1,2]}";
        String serviceResponse = "[{\"id\":2,\"message\":\"Supervisor Notification\"}]";
        
        when(notificationService.getSupervisorNotification(anyString())).thenReturn(serviceResponse);

        MvcResult result = mockMvc.perform(post("/notification/getSupervisorNotification")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer token")
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        JsonNode jsonResponse = objectMapper.readTree(responseBody);
        
        assertEquals(200, jsonResponse.get("statusCode").asInt());
        assertEquals("Success", jsonResponse.get("status").asText());
        assertNotNull(jsonResponse.get("data"));
        
        verify(notificationService).getSupervisorNotification(requestBody);
    }

    @Test
    void testCreateNotification_Success() throws Exception {
        String requestBody = "[{\"providerServiceMapID\": 1, \"notificationTypeID\": 101, \"roleID\": 5, \"userID\":10, \"workingLocationID\":100, \"languageID\":1, \"createdBy\": \"testuser\", \"notification\":\"Test Subject\", \"notificationDesc\":\"Test Description\", \"validFrom\": \"1678886400000\", \"validTill\":\"1709424000000\", \"kmFileManager\":{\"fileName\":\"doc.pdf\", \"fileExtension\":\"pdf\", \"providerServiceMapID\":1, \"validFrom\":\"1678886400000\", \"validUpto\":\"1709424000000\", \"fileContent\":\"base64content\", \"createdBy\":\"testuser\", \"categoryID\":1, \"subCategoryID\":10}}]";
        String serviceResponse = "{\"message\":\"Notification created successfully\"}";
        
        when(notificationService.createNotification(anyString())).thenReturn(serviceResponse);

        MvcResult result = mockMvc.perform(post("/notification/createNotification")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer token")
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        JsonNode jsonResponse = objectMapper.readTree(responseBody);
        
        assertEquals(200, jsonResponse.get("statusCode").asInt());
        assertEquals("Success", jsonResponse.get("status").asText());
        assertNotNull(jsonResponse.get("data"));
        
        verify(notificationService).createNotification(requestBody);
    }

    @Test
    void testUpdateNotification_Success() throws Exception {
        String requestBody = "{\"notificationID\" : 1, \"notification\":\"Updated Subject\", \"notificationDesc\":\"Updated Description\", \"notificationTypeID\":101, \"roleID\":5, \"validFrom\":\"1678886400000\", \"validTill\":\"1709424000000\", \"deleted\":false, \"modifiedBy\":\"modifier\", \"kmFileManager\":{\"fileName\":\"newdoc.pdf\", \"fileExtension\":\"pdf\", \"providerServiceMapID\":1, \"userID\":10, \"validFrom\":\"1678886400000\", \"validUpto\":\"1709424000000\", \"fileContent\":\"newbase64content\", \"createdBy\":\"modifier\", \"categoryID\":1, \"subCategoryID\":10}}";
        String serviceResponse = "{\"message\":\"Notification updated successfully\"}";
        
        when(notificationService.updateNotification(anyString())).thenReturn(serviceResponse);

        MvcResult result = mockMvc.perform(post("/notification/updateNotification")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer token")
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        JsonNode jsonResponse = objectMapper.readTree(responseBody);
        
        assertEquals(200, jsonResponse.get("statusCode").asInt());
        assertEquals("Success", jsonResponse.get("status").asText());
        assertNotNull(jsonResponse.get("data"));
        
        verify(notificationService).updateNotification(requestBody);
    }

    @Test
    void testGetNotificationType_Success() throws Exception {
        String requestBody = "{\"providerServiceMapID\" : 1}";
        String serviceResponse = "[{\"id\":1,\"type\":\"General\"}]";
        
        when(notificationService.getNotificationType(anyString())).thenReturn(serviceResponse);

        MvcResult result = mockMvc.perform(post("/notification/getNotificationType")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer token")
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        JsonNode jsonResponse = objectMapper.readTree(responseBody);
        
        assertEquals(200, jsonResponse.get("statusCode").asInt());
        assertEquals("Success", jsonResponse.get("status").asText());
        assertNotNull(jsonResponse.get("data"));
        
        verify(notificationService).getNotificationType(requestBody);
    }

    @Test
    void testCreateNotificationType_Success() throws Exception {
        String requestBody = "{\"providerServiceMapID\" : 1, \"notificationType\":\"New Type\", \"notificationTypeDesc\":\"Description for new type\", \"createdBy\":\"admin\"}";
        String serviceResponse = "{\"message\":\"Notification type created successfully\"}";
        
        when(notificationService.createNotificationType(anyString())).thenReturn(serviceResponse);

        MvcResult result = mockMvc.perform(post("/notification/createNotificationType")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer token")
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        JsonNode jsonResponse = objectMapper.readTree(responseBody);
        
        assertEquals(200, jsonResponse.get("statusCode").asInt());
        assertEquals("Success", jsonResponse.get("status").asText());
        assertNotNull(jsonResponse.get("data"));
        
        verify(notificationService).createNotificationType(requestBody);
    }

    @Test
    void testUpdateNotificationType_Success() throws Exception {
        String requestBody = "{\"notificationTypeID\" : 1, \"notificationType\":\"Updated Type\", \"notificationTypeDesc\":\"Updated description\", \"deleted\":false, \"modifiedBy\":\"admin\"}";
        String serviceResponse = "{\"message\":\"Notification type updated successfully\"}";
        
        when(notificationService.updateNotificationType(anyString())).thenReturn(serviceResponse);

        MvcResult result = mockMvc.perform(post("/notification/updateNotificationType")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer token")
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        JsonNode jsonResponse = objectMapper.readTree(responseBody);
        
        assertEquals(200, jsonResponse.get("statusCode").asInt());
        assertEquals("Success", jsonResponse.get("status").asText());
        assertNotNull(jsonResponse.get("data"));
        
        verify(notificationService).updateNotificationType(requestBody);
    }

    @Test
    void testGetEmergencyContacts_Success() throws Exception {
        String requestBody = "{\"providerServiceMapID\": 1, \"notificationTypeID\": 101}";
        String serviceResponse = "[{\"id\":1,\"name\":\"John Doe\",\"contactNo\":\"1234567890\"}]";
        
        when(notificationService.getEmergencyContacts(anyString())).thenReturn(serviceResponse);

        MvcResult result = mockMvc.perform(post("/notification/getEmergencyContacts")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer token")
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        JsonNode jsonResponse = objectMapper.readTree(responseBody);
        
        assertEquals(200, jsonResponse.get("statusCode").asInt());
        assertEquals("Success", jsonResponse.get("status").asText());
        assertNotNull(jsonResponse.get("data"));
        
        verify(notificationService).getEmergencyContacts(requestBody);
    }

    @Test
    void testGetSupervisorEmergencyContacts_Success() throws Exception {
        String requestBody = "{\"providerServiceMapID\": 1, \"notificationTypeID\": 101}";
        String serviceResponse = "[{\"id\":2,\"name\":\"Jane Smith\",\"contactNo\":\"0987654321\"}]";
        
        when(notificationService.getSupervisorEmergencyContacts(anyString())).thenReturn(serviceResponse);

        MvcResult result = mockMvc.perform(post("/notification/getSupervisorEmergencyContacts")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer token")
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        JsonNode jsonResponse = objectMapper.readTree(responseBody);
        
        assertEquals(200, jsonResponse.get("statusCode").asInt());
        assertEquals("Success", jsonResponse.get("status").asText());
        assertNotNull(jsonResponse.get("data"));
        
        verify(notificationService).getSupervisorEmergencyContacts(requestBody);
    }

    @Test
    void testCreateEmergencyContacts_Success() throws Exception {
        String requestBody = "[{\"providerServiceMapID\": 1, \"notificationTypeID\": 101, \"createdBy\": \"testuser\", \"designationID\":1, \"emergContactName\":\"Contact 1\", \"location\":\"Office A\", \"emergContactNo\":\"1234567890\", \"emergContactDesc\": \"Emergency contact 1\", \"notificationTypeID\":101, \"createdBy\":\"testuser\"}]";
        String serviceResponse = "{\"message\":\"Emergency contacts created successfully\"}";
        
        when(notificationService.createEmergencyContacts(anyString())).thenReturn(serviceResponse);

        MvcResult result = mockMvc.perform(post("/notification/createEmergencyContacts")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer token")
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        JsonNode jsonResponse = objectMapper.readTree(responseBody);
        
        assertEquals(200, jsonResponse.get("statusCode").asInt());
        assertEquals("Success", jsonResponse.get("status").asText());
        assertNotNull(jsonResponse.get("data"));
        
        verify(notificationService).createEmergencyContacts(requestBody);
    }

    @Test
    void testUpdateEmergencyContacts_Success() throws Exception {
        String requestBody = "{\"providerServiceMapID\": 1, \"notificationTypeID\": 101, \"createdBy\": \"testuser\", \"designationID\":1, \"emergContactName\":\"Updated Contact\", \"location\":\"Office B\", \"emergContactNo\":\"0987654321\", \"emergContactDesc\": \"Updated emergency contact\", \"notificationTypeID\":101, \"createdBy\":\"testuser\"}";
        String serviceResponse = "{\"message\":\"Emergency contacts updated successfully\"}";
        
        when(notificationService.updateEmergencyContacts(anyString())).thenReturn(serviceResponse);

        MvcResult result = mockMvc.perform(post("/notification/updateEmergencyContacts")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer token")
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        JsonNode jsonResponse = objectMapper.readTree(responseBody);
        
        assertEquals(200, jsonResponse.get("statusCode").asInt());
        assertEquals("Success", jsonResponse.get("status").asText());
        assertNotNull(jsonResponse.get("data"));
        
        verify(notificationService).updateEmergencyContacts(requestBody);
    }

    @Test
    void testGetNotification_MissingAuthHeader() throws Exception {
        String requestBody = "{\"providerServiceMapID\": 1, \"notificationTypeID\": 101}";

        mockMvc.perform(post("/notification/getNotification")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isNotFound()); // Expecting 404 due to missing Authorization header
    }

    @Test
    void testGetNotification_InvalidJson() throws Exception {
        String invalidJson = "{\"providerServiceMapID\": 1, \"notificationTypeID\":";

        MvcResult result = mockMvc.perform(post("/notification/getNotification")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer token")
                .content(invalidJson))
                .andExpect(status().isOk()) // Controller handles invalid JSON gracefully
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        JsonNode jsonResponse = objectMapper.readTree(responseBody);
        
        // The controller handles invalid JSON gracefully and returns success with null data
        assertEquals(200, jsonResponse.get("statusCode").asInt());
        assertEquals("Success", jsonResponse.get("status").asText());
        
        // Check if data field exists and is null, or if it doesn't exist at all
        JsonNode dataNode = jsonResponse.get("data");
        assertTrue(dataNode == null || dataNode.isNull());
    }

    @Test
    void testGetNotification_EmptyBody() throws Exception {
        mockMvc.perform(post("/notification/getNotification")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer token")
                .content(""))
                .andExpect(status().isBadRequest()); // Expecting 400 due to empty body
    }

    @Test
    void testSetNotificationService() {
        // Test the setter method directly
        NotificationService mockService = notificationService;
        notificationController.setNotificationService(mockService);
        
        // The setter doesn't return anything, so we just verify it doesn't throw an exception
        assertNotNull(notificationController);
    }
}