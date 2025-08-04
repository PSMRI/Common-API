package com.iemr.common.service.beneficiary;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mockito;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.iemr.common.dto.identity.BeneficiariesDTO;
import com.iemr.common.dto.identity.BeneficiariesPartialDTO;
import com.iemr.common.dto.identity.IdentityEditDTO;
import com.iemr.common.model.beneficiary.BeneficiaryGenModel;
import com.iemr.common.utils.config.ConfigProperties;
import com.iemr.common.utils.exception.IEMRException;
import com.iemr.common.utils.http.HttpUtils;
import com.iemr.common.utils.mapper.InputMapper;
import com.iemr.common.utils.mapper.OutputMapper;
import com.iemr.common.utils.response.OutputResponse;

@ExtendWith(MockitoExtension.class)
class IdentityBeneficiaryServiceImplTest {

    @InjectMocks
    private IdentityBeneficiaryServiceImpl service;

    @Mock
    private HttpUtils httpUtils;

    @Mock
    private InputMapper inputMapper;

    @Mock
    private OutputResponse outputResponse;

    private String sampleJsonResponse;
    private String sampleDataString;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() throws Exception {
        // Set up sample JSON responses
        sampleDataString = "[{\"benRegId\":123,\"firstName\":\"John\",\"lastName\":\"Doe\"}]";
        sampleJsonResponse = "{\"statusCode\":200,\"response\":{\"data\":\"" + sampleDataString.replace("\"", "\\\"") + "\"}}";
        
        // Inject mocked HttpUtils
        Field httpUtilsField = IdentityBeneficiaryServiceImpl.class.getDeclaredField("httpUtils");
        httpUtilsField.setAccessible(true);
        httpUtilsField.set(null, httpUtils);
        
        // Inject mocked InputMapper
        Field inputMapperField = IdentityBeneficiaryServiceImpl.class.getDeclaredField("inputMapper");
        inputMapperField.setAccessible(true);
        inputMapperField.set(service, inputMapper);
        
        // Set the identityBaseURL and identity1097BaseURL fields
        Field identityBaseURLField = IdentityBeneficiaryServiceImpl.class.getDeclaredField("identityBaseURL");
        identityBaseURLField.setAccessible(true);
        identityBaseURLField.set(service, "http://localhost:8080/identity");
        
        Field identity1097BaseURLField = IdentityBeneficiaryServiceImpl.class.getDeclaredField("identity1097BaseURL");
        identity1097BaseURLField.setAccessible(true);
        identity1097BaseURLField.set(service, "http://localhost:8080/identity1097");
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGetBeneficiaryListByIDs_Success() throws IEMRException {
        // Arrange
        HashSet<Long> benIdList = new HashSet<>();
        benIdList.add(123L);
        String auth = "Bearer token";
        Boolean is1097 = false;

        when(httpUtils.post(anyString(), anyString(), any(HashMap.class))).thenReturn(sampleJsonResponse);
        
        // Mock the InputMapper.gson() static method
        try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class)) {
            InputMapper mockStaticMapper = mock(InputMapper.class);
            inputMapperMock.when(InputMapper::gson).thenReturn(mockStaticMapper);
            
            // Mock the response parsing
            OutputResponse mockResponse = mock(OutputResponse.class);
            when(mockResponse.getStatusCode()).thenReturn(200);
            when(mockStaticMapper.fromJson(anyString(), eq(OutputResponse.class))).thenReturn(mockResponse);
            
            BeneficiariesDTO mockDto = new BeneficiariesDTO();
            mockDto.setBenRegId(BigInteger.valueOf(123L));
            when(mockStaticMapper.fromJson(anyString(), eq(BeneficiariesDTO.class))).thenReturn(mockDto);

            try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
                configMock.when(() -> ConfigProperties.getPropertyByName("identity-api-url-getByBenRegIdList"))
                        .thenReturn("IDENTITY_BASE_URL/getByBenRegIdList");
                configMock.when(() -> ConfigProperties.getPropertyByName("identity-api-url"))
                        .thenReturn("http://localhost:8080/identity");

                // Act
                List<BeneficiariesDTO> result = service.getBeneficiaryListByIDs(benIdList, auth, is1097);

                // Assert
                assertNotNull(result);
                assertEquals(1, result.size());
                verify(httpUtils).post(anyString(), anyString(), any(HashMap.class));
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGetBeneficiaryListByIDs_With1097Flag() throws IEMRException {
        // Arrange
        HashSet<Long> benIdList = new HashSet<>();
        benIdList.add(123L);
        String auth = "Bearer token";
        Boolean is1097 = true;

        when(httpUtils.post(anyString(), anyString(), any(HashMap.class))).thenReturn(sampleJsonResponse);
        
        // Mock the InputMapper.gson() static method
        try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class)) {
            InputMapper mockStaticMapper = mock(InputMapper.class);
            inputMapperMock.when(InputMapper::gson).thenReturn(mockStaticMapper);
            
            // Mock the response parsing
            OutputResponse mockResponse = mock(OutputResponse.class);
            when(mockResponse.getStatusCode()).thenReturn(200);
            when(mockStaticMapper.fromJson(anyString(), eq(OutputResponse.class))).thenReturn(mockResponse);
            
            BeneficiariesDTO mockDto = new BeneficiariesDTO();
            mockDto.setBenRegId(BigInteger.valueOf(123L));
            when(mockStaticMapper.fromJson(anyString(), eq(BeneficiariesDTO.class))).thenReturn(mockDto);

            try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
                configMock.when(() -> ConfigProperties.getPropertyByName("identity-api-url-getByBenRegIdList"))
                        .thenReturn("IDENTITY_BASE_URL/getByBenRegIdList");
                configMock.when(() -> ConfigProperties.getPropertyByName("identity-1097-api-url"))
                        .thenReturn("http://localhost:8080/identity1097");

                // Act
                List<BeneficiariesDTO> result = service.getBeneficiaryListByIDs(benIdList, auth, is1097);

                // Assert
                assertNotNull(result);
                verify(httpUtils).post(contains("identity1097"), anyString(), any(HashMap.class));
            }
        }
    }

    @Test
    void testGetBeneficiaryListByIDs_WithNullAuth() throws IEMRException {
        // Arrange
        HashSet<Long> benIdList = new HashSet<>();
        benIdList.add(123L);
        String auth = null;
        Boolean is1097 = false;

        when(httpUtils.post(anyString(), anyString(), any(HashMap.class))).thenReturn(sampleJsonResponse);

        try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
            configMock.when(() -> ConfigProperties.getPropertyByName("identity-api-url-getByBenRegIdList"))
                    .thenReturn("IDENTITY_BASE_URL/getByBenRegIdList");
            configMock.when(() -> ConfigProperties.getPropertyByName("identity-api-url"))
                    .thenReturn("http://localhost:8080/identity");

            // Act
            List<BeneficiariesDTO> result = service.getBeneficiaryListByIDs(benIdList, auth, is1097);

            // Assert
            assertNotNull(result);
            verify(httpUtils).post(anyString(), anyString(), argThat(map -> !map.containsKey("Authorization")));
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGetBeneficiaryListByIDs_FailureResponse() throws IEMRException {
        // Arrange
        HashSet<Long> benIdList = new HashSet<>();
        benIdList.add(123L);
        String auth = "Bearer token";
        Boolean is1097 = false;

        when(httpUtils.post(anyString(), anyString(), any(HashMap.class))).thenReturn(sampleJsonResponse);
        
        // Mock the InputMapper.gson() static method
        try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class)) {
            InputMapper mockStaticMapper = mock(InputMapper.class);
            inputMapperMock.when(InputMapper::gson).thenReturn(mockStaticMapper);
            
            // Mock the response parsing to return failure
            OutputResponse mockResponse = mock(OutputResponse.class);
            when(mockResponse.getStatusCode()).thenReturn(OutputResponse.USERID_FAILURE);
            when(mockResponse.getErrorMessage()).thenReturn("User not found");
            when(mockStaticMapper.fromJson(anyString(), eq(OutputResponse.class))).thenReturn(mockResponse);

            try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
                configMock.when(() -> ConfigProperties.getPropertyByName("identity-api-url-getByBenRegIdList"))
                        .thenReturn("IDENTITY_BASE_URL/getByBenRegIdList");
                configMock.when(() -> ConfigProperties.getPropertyByName("identity-api-url"))
                        .thenReturn("http://localhost:8080/identity");

                // Act & Assert
                IEMRException exception = assertThrows(IEMRException.class, 
                    () -> service.getBeneficiaryListByIDs(benIdList, auth, is1097));
                assertEquals("User not found", exception.getMessage());
            }
        }
    }

    @Test
    void testGetBeneficiaryListByIDs_NullResult() throws IEMRException {
        // Arrange
        HashSet<Long> benIdList = new HashSet<>();
        benIdList.add(123L);
        String auth = "Bearer token";
        Boolean is1097 = false;

        when(httpUtils.post(anyString(), anyString(), any(HashMap.class))).thenReturn(null);

        try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
            configMock.when(() -> ConfigProperties.getPropertyByName("identity-api-url-getByBenRegIdList"))
                    .thenReturn("IDENTITY_BASE_URL/getByBenRegIdList");
            configMock.when(() -> ConfigProperties.getPropertyByName("identity-api-url"))
                    .thenReturn("http://localhost:8080/identity");

            // Act
            List<BeneficiariesDTO> result = service.getBeneficiaryListByIDs(benIdList, auth, is1097);

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGetPartialBeneficiaryListByIDs_Success() throws IEMRException {
        // Arrange
        HashSet<Long> benIdList = new HashSet<>();
        benIdList.add(123L);
        String auth = "Bearer token";
        Boolean is1097 = false;

        when(httpUtils.post(anyString(), anyString(), any(HashMap.class))).thenReturn(sampleJsonResponse);
        
        // Mock the InputMapper.gson() static method
        try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class)) {
            InputMapper mockStaticMapper = mock(InputMapper.class);
            inputMapperMock.when(InputMapper::gson).thenReturn(mockStaticMapper);
            
            // Mock the response parsing
            OutputResponse mockResponse = mock(OutputResponse.class);
            when(mockResponse.getStatusCode()).thenReturn(200);
            when(mockStaticMapper.fromJson(anyString(), eq(OutputResponse.class))).thenReturn(mockResponse);
            
            BeneficiariesPartialDTO mockDto = new BeneficiariesPartialDTO();
            mockDto.setBenRegId(123L);
            when(mockStaticMapper.fromJson(anyString(), eq(BeneficiariesPartialDTO.class))).thenReturn(mockDto);

            try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
                configMock.when(() -> ConfigProperties.getPropertyByName("identity-api-url-getByPartialBenRegIdList"))
                        .thenReturn("IDENTITY_BASE_URL/getByPartialBenRegIdList");
                configMock.when(() -> ConfigProperties.getPropertyByName("identity-api-url"))
                        .thenReturn("http://localhost:8080/identity");

                // Act
                List<BeneficiariesPartialDTO> result = service.getPartialBeneficiaryListByIDs(benIdList, auth, is1097);

                // Assert
                assertNotNull(result);
                assertEquals(1, result.size());
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGetPartialBeneficiaryListByIDs_Failure() throws IEMRException {
        // Arrange
        HashSet<Long> benIdList = new HashSet<>();
        benIdList.add(123L);
        String auth = "Bearer token";
        Boolean is1097 = false;

        when(httpUtils.post(anyString(), anyString(), any(HashMap.class))).thenReturn(sampleJsonResponse);
        
        // Mock the InputMapper.gson() static method
        try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class)) {
            InputMapper mockStaticMapper = mock(InputMapper.class);
            inputMapperMock.when(InputMapper::gson).thenReturn(mockStaticMapper);
            
            // Mock the response parsing to return failure
            OutputResponse mockResponse = mock(OutputResponse.class);
            when(mockResponse.getStatusCode()).thenReturn(OutputResponse.USERID_FAILURE);
            when(mockResponse.getErrorMessage()).thenReturn("Access denied");
            when(mockStaticMapper.fromJson(anyString(), eq(OutputResponse.class))).thenReturn(mockResponse);

            try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
                configMock.when(() -> ConfigProperties.getPropertyByName("identity-api-url-getByPartialBenRegIdList"))
                        .thenReturn("IDENTITY_BASE_URL/getByPartialBenRegIdList");
                configMock.when(() -> ConfigProperties.getPropertyByName("identity-api-url"))
                        .thenReturn("http://localhost:8080/identity");

                // Act & Assert
                IEMRException exception = assertThrows(IEMRException.class, 
                    () -> service.getPartialBeneficiaryListByIDs(benIdList, auth, is1097));
                assertEquals("Access denied", exception.getMessage());
            }
        }
    }

    @Test
    void testGetBeneficiaryListByPhone_Success() throws IEMRException {
        // Arrange
        String phoneNo = "1234567890";
        String auth = "Bearer token";
        Boolean is1097 = false;

        when(httpUtils.post(anyString(), anyString(), any(HashMap.class))).thenReturn(sampleJsonResponse);

        try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class);
             MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
            
            InputMapper mockStaticMapper = mock(InputMapper.class);
            inputMapperMock.when(InputMapper::gson).thenReturn(mockStaticMapper);
            when(mockStaticMapper.fromJson(anyString(), eq(OutputResponse.class))).thenReturn(outputResponse);
            
            BeneficiariesDTO mockDto = new BeneficiariesDTO();
            when(mockStaticMapper.fromJson(anyString(), eq(BeneficiariesDTO.class))).thenReturn(mockDto);
            
            configMock.when(() -> ConfigProperties.getPropertyByName("identity-api-url-getByPhoneNum"))
                    .thenReturn("IDENTITY_BASE_URL/getByPhoneNum/");
            configMock.when(() -> ConfigProperties.getPropertyByName("identity-api-url"))
                    .thenReturn("http://localhost:8080/identity");

            // Act
            List<BeneficiariesDTO> result = service.getBeneficiaryListByPhone(phoneNo, auth, is1097);

            // Assert
            assertNotNull(result);
            verify(httpUtils).post(contains(phoneNo), eq(""), any(HashMap.class));
        }
    }

    @Test
    void testGetBeneficiaryListByPhone_Failure() throws IEMRException {
        // Arrange
        String phoneNo = "1234567890";
        String auth = "Bearer token";
        Boolean is1097 = false;

        when(httpUtils.post(anyString(), anyString(), any(HashMap.class))).thenReturn(sampleJsonResponse);

        try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class);
             MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
            
            InputMapper mockStaticMapper = mock(InputMapper.class);
            inputMapperMock.when(InputMapper::gson).thenReturn(mockStaticMapper);
            
            OutputResponse failureResponse = mock(OutputResponse.class);
            when(mockStaticMapper.fromJson(anyString(), eq(OutputResponse.class))).thenReturn(failureResponse);
            when(failureResponse.getStatusCode()).thenReturn(OutputResponse.USERID_FAILURE);
            when(failureResponse.getErrorMessage()).thenReturn("Phone not found");
            
            configMock.when(() -> ConfigProperties.getPropertyByName("identity-api-url-getByPhoneNum"))
                    .thenReturn("IDENTITY_BASE_URL/getByPhoneNum/");
            configMock.when(() -> ConfigProperties.getPropertyByName("identity-api-url"))
                    .thenReturn("http://localhost:8080/identity");

            // Act & Assert
            IEMRException exception = assertThrows(IEMRException.class, 
                () -> service.getBeneficiaryListByPhone(phoneNo, auth, is1097));
            assertEquals("Phone not found", exception.getMessage());
        }
    }

    @Test
    void testGetBeneficiaryListByBenID_Success() throws IEMRException {
        // Arrange
        String benId = "BEN12345";
        String auth = "Bearer token";
        Boolean is1097 = false;

        when(httpUtils.post(anyString(), anyString(), any(HashMap.class))).thenReturn(sampleJsonResponse);

        try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class);
             MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
            
            InputMapper mockStaticMapper = mock(InputMapper.class);
            inputMapperMock.when(InputMapper::gson).thenReturn(mockStaticMapper);
            when(mockStaticMapper.fromJson(anyString(), eq(OutputResponse.class))).thenReturn(outputResponse);
            
            BeneficiariesDTO mockDto = new BeneficiariesDTO();
            when(mockStaticMapper.fromJson(anyString(), eq(BeneficiariesDTO.class))).thenReturn(mockDto);
            
            configMock.when(() -> ConfigProperties.getPropertyByName("identity-api-url-getByBenId"))
                    .thenReturn("IDENTITY_BASE_URL/getByBenId/");
            configMock.when(() -> ConfigProperties.getPropertyByName("identity-api-url"))
                    .thenReturn("http://localhost:8080/identity");

            // Act
            List<BeneficiariesDTO> result = service.getBeneficiaryListByBenID(benId, auth, is1097);

            // Assert
            assertNotNull(result);
            verify(httpUtils).post(contains(benId), eq(""), any(HashMap.class));
        }
    }

    @Test
    void testGetBeneficiaryListByBenRegID_Success() throws IEMRException {
        // Arrange
        Long benRegId = 123L;
        String auth = "Bearer token";
        Boolean is1097 = false;

        when(httpUtils.post(anyString(), anyString(), any(HashMap.class))).thenReturn(sampleJsonResponse);

        try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class);
             MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
            
            InputMapper mockStaticMapper = mock(InputMapper.class);
            inputMapperMock.when(InputMapper::gson).thenReturn(mockStaticMapper);
            when(mockStaticMapper.fromJson(anyString(), eq(OutputResponse.class))).thenReturn(outputResponse);
            
            BeneficiariesDTO mockDto = new BeneficiariesDTO();
            when(mockStaticMapper.fromJson(anyString(), eq(BeneficiariesDTO.class))).thenReturn(mockDto);
            
            configMock.when(() -> ConfigProperties.getPropertyByName("identity-api-url-getByBenRegId"))
                    .thenReturn("IDENTITY_BASE_URL/getByBenRegId/");
            configMock.when(() -> ConfigProperties.getPropertyByName("identity-api-url"))
                    .thenReturn("http://localhost:8080/identity");

            // Act
            List<BeneficiariesDTO> result = service.getBeneficiaryListByBenRegID(benRegId, auth, is1097);

            // Assert
            assertNotNull(result);
            verify(httpUtils).post(contains(benRegId.toString()), eq(""), any(HashMap.class));
        }
    }

    @Test
    void testGetBeneficiaryListByHealthID_ABHAAddress_Success() throws IEMRException {
        // Arrange
        String healthID = "john.doe@abdm";
        String auth = "Bearer token";
        Boolean is1097 = false;

        when(httpUtils.post(anyString(), anyString(), any(HashMap.class))).thenReturn(sampleJsonResponse);

        try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class);
             MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
            
            InputMapper mockStaticMapper = mock(InputMapper.class);
            inputMapperMock.when(InputMapper::gson).thenReturn(mockStaticMapper);
            when(mockStaticMapper.fromJson(anyString(), eq(OutputResponse.class))).thenReturn(outputResponse);
            
            BeneficiariesDTO mockDto = new BeneficiariesDTO();
            when(mockStaticMapper.fromJson(anyString(), eq(BeneficiariesDTO.class))).thenReturn(mockDto);
            
            configMock.when(() -> ConfigProperties.getPropertyByName("identity-api-url-getByHealthID"))
                    .thenReturn("IDENTITY_BASE_URL/getByHealthID/");
            configMock.when(() -> ConfigProperties.getPropertyByName("identity-api-url"))
                    .thenReturn("http://localhost:8080/identity");

            // Act
            List<BeneficiariesDTO> result = service.getBeneficiaryListByHealthID_ABHAAddress(healthID, auth, is1097);

            // Assert
            assertNotNull(result);
            verify(httpUtils).post(contains(healthID), eq(""), any(HashMap.class));
        }
    }

    @Test
    void testGetBeneficiaryListByHealthIDNo_ABHAIDNo_Success() throws IEMRException {
        // Arrange
        String healthIDNo = "12345678901234";
        String auth = "Bearer token";
        Boolean is1097 = false;

        when(httpUtils.post(anyString(), anyString(), any(HashMap.class))).thenReturn(sampleJsonResponse);

        try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class);
             MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
            
            InputMapper mockStaticMapper = mock(InputMapper.class);
            inputMapperMock.when(InputMapper::gson).thenReturn(mockStaticMapper);
            when(mockStaticMapper.fromJson(anyString(), eq(OutputResponse.class))).thenReturn(outputResponse);
            
            BeneficiariesDTO mockDto = new BeneficiariesDTO();
            when(mockStaticMapper.fromJson(anyString(), eq(BeneficiariesDTO.class))).thenReturn(mockDto);
            
            configMock.when(() -> ConfigProperties.getPropertyByName("identity-api-url-getByHealthIDNo"))
                    .thenReturn("IDENTITY_BASE_URL/getByHealthIDNo/");
            configMock.when(() -> ConfigProperties.getPropertyByName("identity-api-url"))
                    .thenReturn("http://localhost:8080/identity");

            // Act
            List<BeneficiariesDTO> result = service.getBeneficiaryListByHealthIDNo_ABHAIDNo(healthIDNo, auth, is1097);

            // Assert
            assertNotNull(result);
            verify(httpUtils).post(contains(healthIDNo), eq(""), any(HashMap.class));
        }
    }

    @Test
    void testGetBeneficiaryListByFamilyId_Success() throws IEMRException {
        // Arrange
        String familyId = "FAM12345";
        String auth = "Bearer token";
        Boolean is1097 = false;

        when(httpUtils.post(anyString(), anyString(), any(HashMap.class))).thenReturn(sampleJsonResponse);

        try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class);
             MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
            
            InputMapper mockStaticMapper = mock(InputMapper.class);
            inputMapperMock.when(InputMapper::gson).thenReturn(mockStaticMapper);
            when(mockStaticMapper.fromJson(anyString(), eq(OutputResponse.class))).thenReturn(outputResponse);
            
            BeneficiariesDTO mockDto = new BeneficiariesDTO();
            when(mockStaticMapper.fromJson(anyString(), eq(BeneficiariesDTO.class))).thenReturn(mockDto);
            
            configMock.when(() -> ConfigProperties.getPropertyByName("identity-api-url-getByFamilyId"))
                    .thenReturn("IDENTITY_BASE_URL/getByFamilyId/");
            configMock.when(() -> ConfigProperties.getPropertyByName("identity-api-url"))
                    .thenReturn("http://localhost:8080/identity");

            // Act
            List<BeneficiariesDTO> result = service.getBeneficiaryListByFamilyId(familyId, auth, is1097);

            // Assert
            assertNotNull(result);
            verify(httpUtils).post(contains(familyId), eq(""), any(HashMap.class));
        }
    }

    @Test
    void testGetBeneficiaryListByGovId_Success() throws IEMRException {
        // Arrange
        String identity = "AADHAAR123456789012";
        String auth = "Bearer token";
        Boolean is1097 = false;

        when(httpUtils.post(anyString(), anyString(), any(HashMap.class))).thenReturn(sampleJsonResponse);

        try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class);
             MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
            
            InputMapper mockStaticMapper = mock(InputMapper.class);
            inputMapperMock.when(InputMapper::gson).thenReturn(mockStaticMapper);
            when(mockStaticMapper.fromJson(anyString(), eq(OutputResponse.class))).thenReturn(outputResponse);
            
            BeneficiariesDTO mockDto = new BeneficiariesDTO();
            when(mockStaticMapper.fromJson(anyString(), eq(BeneficiariesDTO.class))).thenReturn(mockDto);
            
            configMock.when(() -> ConfigProperties.getPropertyByName("identity-api-url-getByGovIdentity"))
                    .thenReturn("IDENTITY_BASE_URL/getByGovIdentity/");
            configMock.when(() -> ConfigProperties.getPropertyByName("identity-api-url"))
                    .thenReturn("http://localhost:8080/identity");

            // Act
            List<BeneficiariesDTO> result = service.getBeneficiaryListByGovId(identity, auth, is1097);

            // Assert
            assertNotNull(result);
            verify(httpUtils).post(contains(identity), eq(""), any(HashMap.class));
        }
    }

    @Test
    void testGetIdentityResponse_Success() throws IEMRException {
        // Arrange
        String request = "{\"firstName\":\"John\",\"lastName\":\"Doe\"}";
        String auth = "Bearer token";
        Boolean is1097 = false;

        when(httpUtils.post(anyString(), anyString(), any(HashMap.class))).thenReturn(sampleJsonResponse);

        try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
            configMock.when(() -> ConfigProperties.getPropertyByName("identity-api-url-benCreate"))
                    .thenReturn("IDENTITY_BASE_URL/benCreate");
            configMock.when(() -> ConfigProperties.getPropertyByName("identity-api-url"))
                    .thenReturn("http://localhost:8080/identity");

            // Act
            String result = service.getIdentityResponse(request, auth, is1097);

            // Assert
            assertEquals(sampleJsonResponse, result);
            verify(httpUtils).post(anyString(), eq(request), any(HashMap.class));
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGetIdentityResponse_Failure() throws IEMRException {
        // Arrange
        String request = "{\"firstName\":\"John\",\"lastName\":\"Doe\"}";
        String auth = "Bearer token";
        Boolean is1097 = false;

        when(httpUtils.post(anyString(), anyString(), any(HashMap.class))).thenReturn(sampleJsonResponse);
        
        // Mock the InputMapper.gson() static method
        try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class)) {
            InputMapper mockStaticMapper = mock(InputMapper.class);
            inputMapperMock.when(InputMapper::gson).thenReturn(mockStaticMapper);
            
            // Mock the response parsing to return failure
            OutputResponse mockResponse = mock(OutputResponse.class);
            when(mockResponse.getStatusCode()).thenReturn(OutputResponse.USERID_FAILURE);
            when(mockResponse.getErrorMessage()).thenReturn("Creation failed");
            when(mockStaticMapper.fromJson(anyString(), eq(OutputResponse.class))).thenReturn(mockResponse);

            try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
                configMock.when(() -> ConfigProperties.getPropertyByName("identity-api-url-benCreate"))
                        .thenReturn("IDENTITY_BASE_URL/benCreate");
                configMock.when(() -> ConfigProperties.getPropertyByName("identity-api-url"))
                        .thenReturn("http://localhost:8080/identity");

                // Act & Assert
                IEMRException exception = assertThrows(IEMRException.class, 
                    () -> service.getIdentityResponse(request, auth, is1097));
                assertEquals("Creation failed", exception.getMessage());
            }
        }
    }

    @Test
    void testEditIdentityEditDTO_Success() throws IEMRException {
        // Arrange
        IdentityEditDTO identityEditDTO = new IdentityEditDTO();
        String auth = "Bearer token";
        Boolean is1097 = false;
        String successResponse = "{\"statusCode\":200,\"response\":{\"data\":\"Updated successfully\"}}";

        when(httpUtils.post(anyString(), anyString(), any(HashMap.class))).thenReturn(successResponse);

        try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class);
             MockedStatic<OutputMapper> outputMapperMock = mockStatic(OutputMapper.class)) {
            
            configMock.when(() -> ConfigProperties.getPropertyByName("identity-api-url-benEdit"))
                    .thenReturn("IDENTITY_BASE_URL/benEdit");
            configMock.when(() -> ConfigProperties.getPropertyByName("identity-api-url"))
                    .thenReturn("http://localhost:8080/identity");
            
            outputMapperMock.when(OutputMapper::gsonWithoutExposeRestriction)
                    .thenReturn(new com.google.gson.Gson());

            // Act
            Integer result = service.editIdentityEditDTO(identityEditDTO, auth, is1097);

            // Assert
            assertEquals(1, result);
        }
    }

    @Test
    void testEditIdentityEditDTO_Failure() throws IEMRException {
        // Arrange
        IdentityEditDTO identityEditDTO = new IdentityEditDTO();
        String auth = "Bearer token";
        Boolean is1097 = false;
        String failureResponse = "{\"statusCode\":200,\"response\":{\"data\":\"Update failed\"}}";

        when(httpUtils.post(anyString(), anyString(), any(HashMap.class))).thenReturn(failureResponse);

        try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class);
             MockedStatic<OutputMapper> outputMapperMock = mockStatic(OutputMapper.class)) {
            
            configMock.when(() -> ConfigProperties.getPropertyByName("identity-api-url-benEdit"))
                    .thenReturn("IDENTITY_BASE_URL/benEdit");
            configMock.when(() -> ConfigProperties.getPropertyByName("identity-api-url"))
                    .thenReturn("http://localhost:8080/identity");
            
            outputMapperMock.when(OutputMapper::gsonWithoutExposeRestriction)
                    .thenReturn(new com.google.gson.Gson());

            // Act
            Integer result = service.editIdentityEditDTO(identityEditDTO, auth, is1097);

            // Assert
            assertEquals(0, result);
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    void testEditIdentityEditDTO_Exception() throws IEMRException {
        // Arrange
        IdentityEditDTO identityEditDTO = new IdentityEditDTO();
        String auth = "Bearer token";
        Boolean is1097 = false;

        when(httpUtils.post(anyString(), anyString(), any(HashMap.class))).thenReturn(sampleJsonResponse);
        
        // Mock the InputMapper.gson() static method
        try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class)) {
            InputMapper mockStaticMapper = mock(InputMapper.class);
            inputMapperMock.when(InputMapper::gson).thenReturn(mockStaticMapper);
            
            // Mock the response parsing to return failure
            OutputResponse mockResponse = mock(OutputResponse.class);
            when(mockResponse.getStatusCode()).thenReturn(OutputResponse.USERID_FAILURE);
            when(mockResponse.getErrorMessage()).thenReturn("Edit failed");
            when(mockStaticMapper.fromJson(anyString(), eq(OutputResponse.class))).thenReturn(mockResponse);

            try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class);
                 MockedStatic<OutputMapper> outputMapperMock = mockStatic(OutputMapper.class)) {
                
                configMock.when(() -> ConfigProperties.getPropertyByName("identity-api-url-benEdit"))
                        .thenReturn("IDENTITY_BASE_URL/benEdit");
                configMock.when(() -> ConfigProperties.getPropertyByName("identity-api-url"))
                        .thenReturn("http://localhost:8080/identity");
                
                outputMapperMock.when(OutputMapper::gsonWithoutExposeRestriction)
                        .thenReturn(new com.google.gson.Gson());

                // Act & Assert
                IEMRException exception = assertThrows(IEMRException.class, 
                    () -> service.editIdentityEditDTO(identityEditDTO, auth, is1097));
                assertEquals("Edit failed", exception.getMessage());
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    void testSearchBeneficiaryList_Success() throws IEMRException {
        // Arrange
        String identitySearchDTO = "{\"firstName\":\"John\"}";
        String auth = "Bearer token";
        Boolean is1097 = false;

        when(httpUtils.post(anyString(), anyString(), any(HashMap.class))).thenReturn(sampleJsonResponse);
        
        // Mock the InputMapper.gson() static method
        try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class)) {
            InputMapper mockStaticMapper = mock(InputMapper.class);
            inputMapperMock.when(InputMapper::gson).thenReturn(mockStaticMapper);
            
            // Mock the response parsing
            OutputResponse mockResponse = mock(OutputResponse.class);
        
            BeneficiariesDTO mockDto = new BeneficiariesDTO();
            when(mockStaticMapper.fromJson(anyString(), eq(BeneficiariesDTO.class))).thenReturn(mockDto);

            try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
                configMock.when(() -> ConfigProperties.getPropertyByName("identity-api-url-advancesearch"))
                        .thenReturn("IDENTITY_BASE_URL/advancesearch");
                configMock.when(() -> ConfigProperties.getPropertyByName("identity-api-url"))
                        .thenReturn("http://localhost:8080/identity");

                // Act
                List<BeneficiariesDTO> result = service.searchBeneficiaryList(identitySearchDTO, auth, is1097);

                // Assert
                assertNotNull(result);
                verify(httpUtils).post(anyString(), eq(identitySearchDTO), any(HashMap.class));
            }
        }
    }

    @Test
    void testEditIdentityEditDTOCommunityorEducation_Success() throws IEMRException {
        // Arrange
        IdentityEditDTO identityEditDTO = new IdentityEditDTO();
        String auth = "Bearer token";
        Boolean is1097 = false;
        String successResponse = "{\"statusCode\":200,\"response\":{\"data\":\"Updated successfully\"}}";

        when(httpUtils.post(anyString(), anyString(), any(HashMap.class))).thenReturn(successResponse);

        try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class);
             MockedStatic<OutputMapper> outputMapperMock = mockStatic(OutputMapper.class)) {
            
            configMock.when(() -> ConfigProperties.getPropertyByName("identity-api-url-benEditEducationCommunity"))
                    .thenReturn("IDENTITY_BASE_URL/benEditEducationCommunity");
            configMock.when(() -> ConfigProperties.getPropertyByName("identity-api-url"))
                    .thenReturn("http://localhost:8080/identity");
            
            outputMapperMock.when(OutputMapper::gsonWithoutExposeRestriction)
                    .thenReturn(new com.google.gson.Gson());

            // Act
            Integer result = service.editIdentityEditDTOCommunityorEducation(identityEditDTO, auth, is1097);

            // Assert
            assertEquals(1, result);
        }
    }

    @Test
    void testEditIdentityEditDTOCommunityorEducation_Failure() throws IEMRException {
        // Arrange
        IdentityEditDTO identityEditDTO = new IdentityEditDTO();
        String auth = "Bearer token";
        Boolean is1097 = false;
        String failureResponse = "{\"statusCode\":200,\"response\":{\"data\":\"Update failed\"}}";

        when(httpUtils.post(anyString(), anyString(), any(HashMap.class))).thenReturn(failureResponse);

        try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class);
             MockedStatic<OutputMapper> outputMapperMock = mockStatic(OutputMapper.class)) {
            
            configMock.when(() -> ConfigProperties.getPropertyByName("identity-api-url-benEditEducationCommunity"))
                    .thenReturn("IDENTITY_BASE_URL/benEditEducationCommunity");
            configMock.when(() -> ConfigProperties.getPropertyByName("identity-api-url"))
                    .thenReturn("http://localhost:8080/identity");
            
            outputMapperMock.when(OutputMapper::gsonWithoutExposeRestriction)
                    .thenReturn(new com.google.gson.Gson());

            // Act
            Integer result = service.editIdentityEditDTOCommunityorEducation(identityEditDTO, auth, is1097);

            // Assert
            assertEquals(0, result);
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGenerateBeneficiaryIDs_Success() throws IEMRException {
        // Arrange
        String request = "{\"benIDRequired\":3,\"vanID\":101}";
        String auth = "Bearer token";
        String responseData = "[{\"beneficiaryID\":\"BEN001\"},{\"beneficiaryID\":\"BEN002\"}]";
        String response = "{\"statusCode\":200,\"response\":{\"data\":\"" + responseData.replace("\"", "\\\"") + "\"}}";

        when(httpUtils.post(anyString(), anyString(), any(HashMap.class))).thenReturn(response);
        
        // Mock the InputMapper.gson() static method
        try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class)) {
            InputMapper mockStaticMapper = mock(InputMapper.class);
            inputMapperMock.when(InputMapper::gson).thenReturn(mockStaticMapper);
            
            // Mock the response parsing to return success
            OutputResponse mockResponse = mock(OutputResponse.class);
            when(mockResponse.getStatusCode()).thenReturn(200);
            when(mockStaticMapper.fromJson(anyString(), eq(OutputResponse.class))).thenReturn(mockResponse);
            
            BeneficiaryGenModel mockModel = new BeneficiaryGenModel();
            mockModel.setBeneficiaryId(Long.valueOf("1"));
            when(mockStaticMapper.fromJson(anyString(), eq(BeneficiaryGenModel.class))).thenReturn(mockModel);

            try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
                configMock.when(() -> ConfigProperties.getPropertyByName("genben-api"))
                        .thenReturn("http://localhost:8080/genben");
                configMock.when(() -> ConfigProperties.getPropertyByName("generateBeneficiaryIDs-api-url"))
                        .thenReturn("/generateBeneficiaryIDs");

                // Act
                List<BeneficiaryGenModel> result = service.generateBeneficiaryIDs(request, auth);

                // Assert
                assertNotNull(result);
                assertEquals(2, result.size());
                verify(httpUtils).post(anyString(), eq(request), any(HashMap.class));
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGenerateBeneficiaryIDs_Failure() throws IEMRException {
        // Arrange
        String request = "{\"benIDRequired\":3,\"vanID\":101}";
        String auth = "Bearer token";

        when(httpUtils.post(anyString(), anyString(), any(HashMap.class))).thenReturn(sampleJsonResponse);
        
        // Mock the InputMapper.gson() static method
        try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class)) {
            InputMapper mockStaticMapper = mock(InputMapper.class);
            inputMapperMock.when(InputMapper::gson).thenReturn(mockStaticMapper);
            
            // Mock the response parsing to return failure
            OutputResponse mockResponse = mock(OutputResponse.class);
            when(mockResponse.getStatusCode()).thenReturn(OutputResponse.USERID_FAILURE);
            when(mockResponse.getErrorMessage()).thenReturn("Generation failed");
            when(mockStaticMapper.fromJson(anyString(), eq(OutputResponse.class))).thenReturn(mockResponse);

            try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
                configMock.when(() -> ConfigProperties.getPropertyByName("genben-api"))
                        .thenReturn("http://localhost:8080/genben");
                configMock.when(() -> ConfigProperties.getPropertyByName("generateBeneficiaryIDs-api-url"))
                        .thenReturn("/generateBeneficiaryIDs");

                // Act & Assert
                IEMRException exception = assertThrows(IEMRException.class, 
                    () -> service.generateBeneficiaryIDs(request, auth));
                assertEquals("Generation failed", exception.getMessage());
            }
        }
    }

    @Test
    void testServiceInstantiation() {
        // Test basic service instantiation and field initialization
        IdentityBeneficiaryServiceImpl testService = new IdentityBeneficiaryServiceImpl();
        assertNotNull(testService);
    }

    @Test
    void testLoggerInitialization() throws Exception {
        // Test logger field is properly initialized
        Field loggerField = IdentityBeneficiaryServiceImpl.class.getDeclaredField("logger");
        loggerField.setAccessible(true);
        Object logger = loggerField.get(service);
        assertNotNull(logger);
    }

    @Test
    void testStaticFieldsInitialization() {
        // Test static fields are properly initialized
        try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
            configMock.when(() -> ConfigProperties.getPropertyByName("identity-api-url"))
                    .thenReturn("http://localhost:8080/identity");
            configMock.when(() -> ConfigProperties.getPropertyByName("identity-1097-api-url"))
                    .thenReturn("http://localhost:8080/identity1097");
            configMock.when(() -> ConfigProperties.getPropertyByName("genben-api"))
                    .thenReturn("http://localhost:8080/genben");
            configMock.when(() -> ConfigProperties.getPropertyByName("generateBeneficiaryIDs-api-url"))
                    .thenReturn("/generateBeneficiaryIDs");

            IdentityBeneficiaryServiceImpl testService = new IdentityBeneficiaryServiceImpl();
            assertNotNull(testService);
        }
    }

    @Test
    void testConstants() {
        // Test that constants are properly defined
        assertEquals("BEN_GEN", IdentityBeneficiaryServiceImpl.class.getDeclaredFields()[6].getName());
    }

    @Test
    void testNullAuthorizationHandling() throws IEMRException {
        // Test that methods handle null authorization properly
        HashSet<Long> benIdList = new HashSet<>();
        benIdList.add(123L);

        when(httpUtils.post(anyString(), anyString(), any(HashMap.class))).thenReturn(sampleJsonResponse);

        try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
            configMock.when(() -> ConfigProperties.getPropertyByName("identity-api-url-getByBenRegIdList"))
                    .thenReturn("IDENTITY_BASE_URL/getByBenRegIdList");
            configMock.when(() -> ConfigProperties.getPropertyByName("identity-api-url"))
                    .thenReturn("http://localhost:8080/identity");

            // Act
            service.getBeneficiaryListByIDs(benIdList, null, false);

            // Assert - verify that the header map doesn't contain Authorization
            verify(httpUtils).post(anyString(), anyString(), 
                argThat(map -> map != null && !map.containsKey("Authorization")));
        }
    }
}
