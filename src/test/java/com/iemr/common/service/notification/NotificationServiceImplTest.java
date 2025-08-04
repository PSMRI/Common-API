package com.iemr.common.service.notification;

import com.iemr.common.data.institute.Designation;
import com.iemr.common.data.kmfilemanager.KMFileManager;
import com.iemr.common.data.notification.EmergencyContacts;
import com.iemr.common.data.notification.Notification;
import com.iemr.common.data.notification.NotificationType;
import com.iemr.common.data.userbeneficiarydata.Language;
import com.iemr.common.data.users.ProviderServiceMapping;
import com.iemr.common.data.users.Role;
import com.iemr.common.data.users.User;
import com.iemr.common.data.users.WorkLocation;
import com.iemr.common.repository.notification.EmergencyContactsRepository;
import com.iemr.common.repository.notification.NotificationRepository;
import com.iemr.common.repository.notification.NotificationTypeRepository;
import com.iemr.common.service.kmfilemanager.KMFileManagerService;
import com.iemr.common.utils.config.ConfigProperties;
import com.iemr.common.utils.exception.IEMRException;
import com.iemr.common.utils.gateway.email.EmailService;
import com.iemr.common.utils.mapper.InputMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.test.util.ReflectionTestUtils;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class NotificationServiceImplTest {
    @InjectMocks
    NotificationServiceImpl service;

    @Mock
    NotificationRepository notificationRepository;
    @Mock
    NotificationTypeRepository notificationTypeRepository;
    @Mock
    EmergencyContactsRepository emergencyContactsRepository;
    @Mock
    KMFileManagerService kmFileManagerService;
    @Mock
    EmailService emailService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(service, "inputMapper", mock(InputMapper.class));
    }

    @Test
    public void testGetNotification_userIDsBranch() throws Exception {
        Notification req = new Notification();
        req.setUserIDs(Arrays.asList(1,2));
        req.setProviderServiceMapID(1);
        req.setNotificationTypeID(2);
        req.setOutputMapper(null);
        req.setValidFrom(new Timestamp(System.currentTimeMillis()));
        req.setValidTill(new Timestamp(System.currentTimeMillis()));
        ObjectMapper om = new ObjectMapper();
        String json = om.writeValueAsString(req);
        Set<Object[]> resultSet = new HashSet<>();
        Object[] notificationItems = new Object[19];
        notificationItems[0] = 1; notificationItems[1] = "n"; notificationItems[2] = "d"; notificationItems[3] = 2;
        notificationItems[4] = mock(NotificationType.class); notificationItems[5] = 3; notificationItems[6] = mock(Role.class);
        notificationItems[7] = 4; notificationItems[8] = new Timestamp(System.currentTimeMillis()); notificationItems[9] = new Timestamp(System.currentTimeMillis());
        notificationItems[10] = true; notificationItems[11] = 5; notificationItems[12] = mock(KMFileManager.class);
        notificationItems[13] = 6; notificationItems[14] = mock(WorkLocation.class); notificationItems[15] = 7;
        notificationItems[16] = mock(Language.class); notificationItems[17] = 8; notificationItems[18] = mock(User.class);
        resultSet.add(notificationItems);
        when(notificationRepository.getUserNotifications(any(), any(), any(), any(), any())).thenReturn(resultSet);
        String result = service.getNotification(json);
        assertTrue(result.contains("n"));
    }

    @Test
    public void testGetNotification_languageIDsBranch() throws Exception {
        Notification req = new Notification();
        req.setLanguageIDs(Arrays.asList(1,2));
        req.setProviderServiceMapID(1);
        req.setNotificationTypeID(2);
        req.setOutputMapper(null);
        req.setValidFrom(new Timestamp(System.currentTimeMillis()));
        req.setValidTill(new Timestamp(System.currentTimeMillis()));
        ObjectMapper om = new ObjectMapper();
        String json = om.writeValueAsString(req);
        Set<Object[]> resultSet = new HashSet<>();
        Object[] notificationItems = new Object[19];
        Arrays.fill(notificationItems, null);
        notificationItems[1] = "lang";
        resultSet.add(notificationItems);
        when(notificationRepository.getLanguageNotifications(any(), any(), any(), any(), any())).thenReturn(resultSet);
        String result = service.getNotification(json);
        assertTrue(result.contains("lang"));
    }

    @Test
    public void testGetNotification_workLocationIDsBranch() throws Exception {
        Notification req = new Notification();
        req.setWorkingLocationIDs(Arrays.asList(1,2));
        req.setProviderServiceMapID(1);
        req.setNotificationTypeID(2);
        req.setOutputMapper(null);
        req.setValidFrom(new Timestamp(System.currentTimeMillis()));
        req.setValidTill(new Timestamp(System.currentTimeMillis()));
        ObjectMapper om = new ObjectMapper();
        String json = om.writeValueAsString(req);
        Set<Object[]> resultSet = new HashSet<>();
        Object[] notificationItems = new Object[19];
        Arrays.fill(notificationItems, null);
        notificationItems[1] = "loc";
        resultSet.add(notificationItems);
        when(notificationRepository.getLocationNotifications(any(), any(), any(), any(), any())).thenReturn(resultSet);
        String result = service.getNotification(json);
        assertTrue(result.contains("loc"));
    }

    @Test
    public void testGetNotification_roleIDsBranch() throws Exception {
        Notification req = new Notification();
        req.setRoleIDs(Arrays.asList(1,2));
        req.setWorkingLocationIDs(Arrays.asList(1,2));
        req.setProviderServiceMapID(1);
        req.setNotificationTypeID(2);
        req.setOutputMapper(null);
        req.setValidFrom(new Timestamp(System.currentTimeMillis()));
        req.setValidTill(new Timestamp(System.currentTimeMillis()));
        ObjectMapper om = new ObjectMapper();
        String json = om.writeValueAsString(req);
        Set<Object[]> resultSet = new HashSet<>();
        Object[] notificationItems = new Object[19];
        Arrays.fill(notificationItems, null);
        notificationItems[1] = "role";
        resultSet.add(notificationItems);
        when(notificationRepository.getRoleNotifications(any(), any(), any(), any(), any(), any())).thenReturn(resultSet);
        String result = service.getNotification(json);
        assertTrue(result.contains("role"));
    }

    @Test
    public void testGetNotification_emptyResultSet() throws Exception {
        Notification req = new Notification();
        req.setProviderServiceMapID(1);
        req.setNotificationTypeID(2);
        ObjectMapper om = new ObjectMapper();
        req.setOutputMapper(null);
        String json = om.writeValueAsString(req);
        when(notificationRepository.getRoleNotifications(any(), any(), any(), any(), any(), any())).thenReturn(new HashSet<>());
        String result = service.getNotification(json);
        assertEquals("[]", result);
    }

    @Test
    public void testGetSupervisorNotification_allBranches() throws Exception {
        Notification req = new Notification();
        req.setUserIDs(Arrays.asList(1));
        req.setProviderServiceMapID(1);
        req.setNotificationTypeID(2);
        ObjectMapper om = new ObjectMapper();
        req.setOutputMapper(null);
        String json = om.writeValueAsString(req);
        Set<Object[]> resultSet = new HashSet<>();
        Object[] notificationItems = new Object[19];
        notificationItems[1] = "superuser";
        resultSet.add(notificationItems);
        when(notificationRepository.getSupervisorNotificationsByUser(any(), any(), any(), any(), any())).thenReturn(resultSet);
        String result = service.getSupervisorNotification(json);
        assertTrue(result.contains("superuser"));
        // languageIDs branch
        req.setUserIDs(null); req.setLanguageIDs(Arrays.asList(2));
        json = om.writeValueAsString(req);
        when(notificationRepository.getSupervisorNotificationsByLanguage(any(), any(), any(), any(), any())).thenReturn(resultSet);
        result = service.getSupervisorNotification(json);
        assertTrue(result.contains("superuser"));
        // locationIDs branch
        req.setLanguageIDs(null); req.setWorkingLocationIDs(Arrays.asList(3));
        json = om.writeValueAsString(req);
        when(notificationRepository.getSupervisorNotificationsByLocation(any(), any(), any(), any(), any())).thenReturn(resultSet);
        result = service.getSupervisorNotification(json);
        assertTrue(result.contains("superuser"));
        // roleIDs null branch
        req.setWorkingLocationIDs(null); req.setRoleIDs(null);
        json = om.writeValueAsString(req);
        when(notificationRepository.getSupervisorNotifications(any(), any(), any(), any())).thenReturn(resultSet);
        result = service.getSupervisorNotification(json);
        assertTrue(result.contains("superuser"));
        // roleIDs non-empty branch
        req.setRoleIDs(Arrays.asList(4));
        json = om.writeValueAsString(req);
        when(notificationRepository.getSupervisorNotificationsByRole(any(), any(), any(), any(), any())).thenReturn(resultSet);
        result = service.getSupervisorNotification(json);
        assertTrue(result.contains("superuser"));
    }

    @Test
    public void testCreateNotification_allBranches() throws Exception {
        Notification req = new Notification();
        req.setNotificationID(1);
        req.setKmFileManagerID(2);
        req.setDeleted(false);
        ObjectMapper om = new ObjectMapper();
        req.setOutputMapper(null);
        Notification[] arr = new Notification[]{req};
        String json = om.writeValueAsString(arr);
        Notification saved = new Notification();
        saved.setNotificationID(1);
        when(notificationRepository.save(any())).thenReturn(saved);
        String result = service.createNotification(json);
        assertTrue(result.contains("1"));
    }

    @Test
    public void testUpdateNotification_kmFileManagerBranch() throws Exception {
        Notification req = new Notification();
        req.setNotificationID(1);
        req.setNotification("n");
        req.setNotificationDesc("desc");
        req.setNotificationTypeID(2);
        req.setRoleID(3);
        req.setValidFrom(new Timestamp(System.currentTimeMillis()));
        req.setValidTill(new Timestamp(System.currentTimeMillis()));
        req.setDeleted(false);
        req.setModifiedBy("user");
        KMFileManager km = new KMFileManager();
        req.setKmFileManager(km);
        km.setOutputMapper(null);
        req.setOutputMapper(null); // Fix serialization error
        ObjectMapper om = new ObjectMapper();
        String json = om.writeValueAsString(req);
        when(notificationRepository.updateNotification(anyInt(), any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(1);
        String result = service.updateNotification(json);
        assertTrue(result.contains("updatecount"));
    }

    @Test
    public void testUpdateNotification_noKmFileManagerBranch() throws Exception {
        Notification req = new Notification();
        req.setNotificationID(1);
        req.setNotification("n");
        req.setNotificationDesc("desc");
        req.setNotificationTypeID(2);
        req.setRoleID(3);
        req.setValidFrom(new Timestamp(System.currentTimeMillis()));
        req.setValidTill(new Timestamp(System.currentTimeMillis()));
        req.setDeleted(false);
        req.setModifiedBy("user");
        ObjectMapper om = new ObjectMapper();
        req.setOutputMapper(null);
        String json = om.writeValueAsString(req);
        when(notificationRepository.updateNotification(anyInt(), any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(1);
        String result = service.updateNotification(json);
        assertTrue(result.contains("updatecount"));
    }

    @Test
    public void testGetNotificationType() throws Exception {
        Set<Object[]> resultSet = new HashSet<>();
        Object[] notificationTypeObjs = new Object[4];
        notificationTypeObjs[0] = 1; notificationTypeObjs[1] = "type"; notificationTypeObjs[2] = "desc"; notificationTypeObjs[3] = true;
        resultSet.add(notificationTypeObjs);
        when(notificationTypeRepository.getNotificationTypes()).thenReturn(resultSet);
        String result = service.getNotificationType("{}");
        assertTrue(result.contains("type"));
    }

    @Test
    public void testCreateNotificationType() throws Exception {
        NotificationType nt = new NotificationType();
        nt.setNotificationTypeID(1);
        nt.setNotificationType("type");
        nt.setNotificationTypeDesc("desc");
        nt.setDeleted(false);
        nt.setOutputMapper(null);
        NotificationType[] arr = new NotificationType[]{nt};
        ObjectMapper om = new ObjectMapper();
        String json = om.writeValueAsString(arr);
        // Fix ArrayList cast issue
        when(notificationTypeRepository.saveAll(any())).thenReturn(new ArrayList<>(Arrays.asList(arr)));
        String result = service.createNotificationType(json);
        assertTrue(result.contains("type"));
    }

    @Test
    public void testUpdateNotificationType() throws Exception {
        NotificationType nt = new NotificationType();
        nt.setNotificationTypeID(1);
        nt.setNotificationType("type");
        nt.setNotificationTypeDesc("desc");
        nt.setDeleted(false);
        nt.setModifiedBy("user");
        try (MockedStatic<InputMapper> mockedStatic = mockStatic(InputMapper.class)) {
            nt.setOutputMapper(null);
            InputMapper inputMapper = mock(InputMapper.class);
            mockedStatic.when(InputMapper::gson).thenReturn(inputMapper);
            when(inputMapper.fromJson(anyString(), eq(NotificationType.class))).thenReturn(nt);
            ReflectionTestUtils.setField(service, "inputMapper", inputMapper);
            when(notificationTypeRepository.updateNotificationType(anyInt(), any(), any(), any(), any())).thenReturn(1);
            String result = service.updateNotificationType("{}");
            assertTrue(result.contains("updatecount"));
        }
    }

    @Test
    public void testGetEmergencyContacts() throws Exception {
        EmergencyContacts ec = new EmergencyContacts();
        ec.setProviderServiceMapID(1);
        ec.setNotificationTypeID(2);
        ec.setMapper(null); // Fix serialization error
        try (MockedStatic<InputMapper> mockedStatic = mockStatic(InputMapper.class)) {
            InputMapper inputMapper = mock(InputMapper.class);
            mockedStatic.when(InputMapper::gson).thenReturn(inputMapper);
            when(inputMapper.fromJson(anyString(), eq(EmergencyContacts.class))).thenReturn(ec);
            ReflectionTestUtils.setField(service, "inputMapper", inputMapper);
        Set<Object[]> resultSet = new HashSet<>();
        Object[] emergencyContact = new Object[13];
        emergencyContact[0] = 1; emergencyContact[1] = 2; emergencyContact[2] = "name"; emergencyContact[3] = "desc";
        emergencyContact[4] = "num"; emergencyContact[5] = 3; emergencyContact[6] = mock(ProviderServiceMapping.class);
        emergencyContact[7] = 4; emergencyContact[8] = mock(NotificationType.class); emergencyContact[9] = "loc";
        emergencyContact[10] = true; emergencyContact[11] = new Timestamp(System.currentTimeMillis()); emergencyContact[12] = mock(Designation.class);
        resultSet.add(emergencyContact);
        when(emergencyContactsRepository.getEmergencyContacts(any(), any())).thenReturn(resultSet);
            String result = service.getEmergencyContacts("{}");
            assertTrue(result.contains("name"));
        }
    }

    @Test
    public void testGetSupervisorEmergencyContacts() throws Exception {
        EmergencyContacts ec = new EmergencyContacts();
        ec.setProviderServiceMapID(1);
        ec.setNotificationTypeID(2);
        ec.setMapper(null); // Fix serialization error
        ObjectMapper om = new ObjectMapper();
        String json = om.writeValueAsString(ec);
        Set<Object[]> resultSet = new HashSet<>();
        Object[] emergencyContact = new Object[13];
        emergencyContact[2] = "supervisor";
        resultSet.add(emergencyContact);
        when(emergencyContactsRepository.getSupervisorEmergencyContacts(any(), any())).thenReturn(resultSet);
        String result = service.getSupervisorEmergencyContacts(json);
        assertTrue(result.contains("supervisor"));
    }

    @Test
    public void testCreateEmergencyContacts() throws Exception {
        EmergencyContacts ec = new EmergencyContacts();
        ec.setEmergContactID(1);
        ec.setMapper(null); // Fix serialization error
        EmergencyContacts[] arr = new EmergencyContacts[]{ec};
        ObjectMapper om = new ObjectMapper();
        String json = om.writeValueAsString(arr);
        when(emergencyContactsRepository.save(any())).thenReturn(ec);
        String result = service.createEmergencyContacts(json);
        assertTrue(result.contains("1"));
    }

    @Test
    public void testUpdateEmergencyContacts_success() throws Exception {
        EmergencyContacts ec = new EmergencyContacts();
        ec.setEmergContactID(1);
        ec.setDesignationID(2);
        ec.setEmergContactNo("num");
        ec.setEmergContactDesc("desc");
        ec.setProviderServiceMapID(3);
        ec.setNotificationTypeID(4);
        ec.setDeleted(false);
        ec.setModifiedBy("user");
        ec.setEmergContactName("name");
        ec.setLocation("loc");
        ec.setMapper(null); // Fix serialization error
        ObjectMapper om = new ObjectMapper();
        String json = om.writeValueAsString(ec);
        when(emergencyContactsRepository.updateEmergencyContacts(anyInt(), anyInt(), any(), any(), anyInt(), anyInt(), any(), any(), any(), any())).thenReturn(1);
        String result = service.updateEmergencyContacts(json);
        assertTrue(result.contains("Updated the 1 rows for given request."));
    }

    @Test
    public void testUpdateEmergencyContacts_fail() throws Exception {
        EmergencyContacts ec = new EmergencyContacts();
        ec.setEmergContactID(1);
        ec.setMapper(null); // Fix serialization error
        ObjectMapper om = new ObjectMapper();
        String json = om.writeValueAsString(ec);
        when(emergencyContactsRepository.updateEmergencyContacts(anyInt(), anyInt(), any(), any(), anyInt(), anyInt(), any(), any(), any(), any())).thenReturn(0);
        String result = service.updateEmergencyContacts(json);
        assertEquals("Failed to update the given request.", result);
    }

    @Test
    public void testUpdateNotificationFile_allBranches() throws Exception {
        // Arrange
        NotificationServiceImpl service = new NotificationServiceImpl();
        KMFileManagerService kmFileManagerService = mock(KMFileManagerService.class);
        ReflectionTestUtils.setField(service, "kmFileManagerService", kmFileManagerService);
        Notification notificationRequest = new Notification();
        KMFileManager kmFileManager = mock(KMFileManager.class);
        // getFileContent returns String in this codebase
        when(kmFileManager.getFileContent()).thenReturn("dummy-content");
        when(kmFileManager.getFileExtension()).thenReturn("pdf");
        when(kmFileManager.getFileName()).thenReturn("file.pdf");
        notificationRequest.setKmFileManager(kmFileManager);
        // Simulate KMFileManagerService returning a JSON array string
        String kmFileManagerResp = "[{\"kmFileManagerID\":123}]";
        when(kmFileManagerService.addKMFile(anyString())).thenReturn(kmFileManagerResp);
        // Act
        java.lang.reflect.Method method = NotificationServiceImpl.class.getDeclaredMethod("updateNotificationFile", Notification.class);
        method.setAccessible(true);
        Notification result = (Notification) method.invoke(service, notificationRequest);
        // Assert
        assertNotNull(result);
        assertEquals(123, result.getKmFileManagerID());
    }
}
