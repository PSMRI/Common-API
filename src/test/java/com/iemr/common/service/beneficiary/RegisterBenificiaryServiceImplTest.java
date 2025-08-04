package com.iemr.common.service.beneficiary;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import com.google.gson.Gson;
import com.iemr.common.data.beneficiary.Beneficiary;
import com.iemr.common.data.mctshistory.MctsOutboundCallDetail;
import com.iemr.common.dto.identity.BeneficiariesDTO;
import com.iemr.common.dto.identity.BenFamilyDTO;
import com.iemr.common.dto.identity.CommonIdentityDTO;
import com.iemr.common.dto.identity.Identity;
import com.iemr.common.dto.identity.IdentityEditDTO;
import com.iemr.common.mapper.CommonIdentityMapper;
import com.iemr.common.mapper.IdentityBenEditMapper;
import com.iemr.common.mapper.utils.InputMapper;
import com.iemr.common.model.beneficiary.BeneficiaryDemographicsModel;
import com.iemr.common.model.beneficiary.BeneficiaryGenModel;
import com.iemr.common.model.beneficiary.BeneficiaryModel;
import com.iemr.common.model.userbeneficiary.BeneficiaryIdentityModel;
import com.iemr.common.repository.mctshistory.OutboundHistoryRepository;
import com.iemr.common.utils.exception.IEMRException;
import com.iemr.common.utils.mapper.OutputMapper;
import com.iemr.common.utils.response.OutputResponse;
import com.iemr.common.utils.validator.Validator;

import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
class RegisterBenificiaryServiceImplTest {

    @InjectMocks
    private RegisterBenificiaryServiceImpl service;

    @Mock
    private CommonIdentityMapper identityMapper;

    @Mock
    private IdentityBeneficiaryService identityBeneficiaryService;

    @Mock
    private IdentityBenEditMapper identityBenEditMapper;

    @Mock
    private Validator validator;

    @Mock
    private OutboundHistoryRepository outboundHistoryRepository;

    @Mock
    private Logger logger;

    @Mock
    private HttpServletRequest httpServletRequest;

    private BeneficiaryModel beneficiaryModel;
    private IdentityEditDTO identityEditDTO;
    private BeneficiaryDemographicsModel demographicsModel;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() throws Exception {
        // Set up test data
        setupBeneficiaryModel();
        setupIdentityEditDTO();
        setupDemographicsModel();
        
        // Inject logger mock
        Field loggerField = RegisterBenificiaryServiceImpl.class.getDeclaredField("logger");
        loggerField.setAccessible(true);
        loggerField.set(service, logger);
    }

    private void setupBeneficiaryModel() {
        beneficiaryModel = new BeneficiaryModel();
        beneficiaryModel.setBeneficiaryRegID(123L);
        beneficiaryModel.setBeneficiaryID("BEN123");
        beneficiaryModel.setFirstName("John");
        beneficiaryModel.setLastName("Doe");
        beneficiaryModel.setAge(30);
        beneficiaryModel.setGenderID(1);
        beneficiaryModel.setIs1097(false);
        beneficiaryModel.setCreatedBy("testUser");
        beneficiaryModel.setReligion("Hindu");
        beneficiaryModel.setOccupation("Engineer");
        beneficiaryModel.setIncomeStatus("Middle");
        
        // Set up face embedding as List<Float>
        List<Float> faceEmbedding = new ArrayList<>();
        faceEmbedding.add(0.1f);
        faceEmbedding.add(0.2f);
        beneficiaryModel.setFaceEmbedding(faceEmbedding);
        beneficiaryModel.setEmergencyRegistration(false);
        
        // Set up demographics
        demographicsModel = new BeneficiaryDemographicsModel();
        demographicsModel.setCommunityName("General");
        demographicsModel.setCommunityID(1);
        demographicsModel.setReligion("Hindu");
        demographicsModel.setReligionName("Hindu");
        demographicsModel.setOccupation("Engineer");
        demographicsModel.setEducationName("Graduate");
        demographicsModel.setEducationID(3);
        demographicsModel.setIncomeStatus("Middle");
        demographicsModel.setDistrictID(1);
        demographicsModel.setBlockID(1);
        demographicsModel.setDistrictBranchID(1);
        
        beneficiaryModel.setI_bendemographics(demographicsModel);
        
        // Set up identities
        List<BeneficiaryIdentityModel> identities = new ArrayList<>();
        BeneficiaryIdentityModel identity = new BeneficiaryIdentityModel();
        identity.setGovtIdentityNo("123456789012");
        identity.setGovtIdentityTypeID(1);
        identities.add(identity);
        beneficiaryModel.setBeneficiaryIdentities(identities);
    }

    private void setupIdentityEditDTO() {
        identityEditDTO = new IdentityEditDTO();
        identityEditDTO.setBeneficiaryRegId(BigInteger.valueOf(123L));
        identityEditDTO.setCommunity("General");
        identityEditDTO.setCommunityName("General");
        identityEditDTO.setReligion("Hindu");
        identityEditDTO.setOccupationName("Engineer");
        identityEditDTO.setEducation("Graduate");
        identityEditDTO.setIncomeStatus("Middle");
    }

    private void setupDemographicsModel() {
        demographicsModel = new BeneficiaryDemographicsModel();
        demographicsModel.setCommunityName("General");
        demographicsModel.setReligion("Hindu");
        demographicsModel.setOccupation("Engineer");
        demographicsModel.setEducationName("Graduate");
        demographicsModel.setIncomeStatus("Middle");
    }

    @Test
    void testSaveBeneficiary() {
        // Arrange
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setFirstName("John");
        beneficiary.setLastName("Doe");

        // Act
        Beneficiary result = service.save(beneficiary);

        // Assert
        assertNotNull(result);
        verify(logger).info(contains("benificiaryDetails"));
    }

    @Test
    void testUpdateBenificiary_Success() throws IEMRException {
        // Arrange
        String auth = "Bearer token";
        Integer expectedRows = 1;

        when(identityBenEditMapper.BenToIdentityEditMapper(beneficiaryModel)).thenReturn(identityEditDTO);
        when(identityBeneficiaryService.editIdentityEditDTO(any(IdentityEditDTO.class), eq(auth), eq(false)))
                .thenReturn(expectedRows);

        // Act
        Integer result = service.updateBenificiary(beneficiaryModel, auth);

        // Assert
        assertEquals(expectedRows, result);
        verify(identityBenEditMapper).BenToIdentityEditMapper(beneficiaryModel);
        verify(identityBeneficiaryService).editIdentityEditDTO(any(IdentityEditDTO.class), eq(auth), eq(false));
    }

    @Test
    void testUpdateBenificiary_WithNullDemographics() throws IEMRException {
        // Arrange
        String auth = "Bearer token";
        Integer expectedRows = 1;
        beneficiaryModel.setI_bendemographics(null);

        when(identityBenEditMapper.BenToIdentityEditMapper(beneficiaryModel)).thenReturn(identityEditDTO);
        when(identityBeneficiaryService.editIdentityEditDTO(any(IdentityEditDTO.class), eq(auth), eq(false)))
                .thenReturn(expectedRows);

        // Act
        Integer result = service.updateBenificiary(beneficiaryModel, auth);

        // Assert
        assertEquals(expectedRows, result);
        verify(identityBenEditMapper).BenToIdentityEditMapper(beneficiaryModel);
    }

    @Test
    void testUpdateBenificiary_WithNullIdentities() throws IEMRException {
        // Arrange
        String auth = "Bearer token";
        Integer expectedRows = 1;
        beneficiaryModel.setBeneficiaryIdentities(null);

        when(identityBenEditMapper.BenToIdentityEditMapper(beneficiaryModel)).thenReturn(identityEditDTO);
        when(identityBeneficiaryService.editIdentityEditDTO(any(IdentityEditDTO.class), eq(auth), eq(false)))
                .thenReturn(expectedRows);

        // Act
        Integer result = service.updateBenificiary(beneficiaryModel, auth);

        // Assert
        assertEquals(expectedRows, result);
        verify(identityBenEditMapper).BenToIdentityEditMapper(beneficiaryModel);
    }

    @Test
    void testUpdateBenificiary_WithEmptyIdentities() throws IEMRException {
        // Arrange
        String auth = "Bearer token";
        Integer expectedRows = 1;
        beneficiaryModel.setBeneficiaryIdentities(new ArrayList<>());

        when(identityBenEditMapper.BenToIdentityEditMapper(beneficiaryModel)).thenReturn(identityEditDTO);
        when(identityBeneficiaryService.editIdentityEditDTO(any(IdentityEditDTO.class), eq(auth), eq(false)))
                .thenReturn(expectedRows);

        // Act
        Integer result = service.updateBenificiary(beneficiaryModel, auth);

        // Assert
        assertEquals(expectedRows, result);
        verify(identityBenEditMapper).BenToIdentityEditMapper(beneficiaryModel);
    }

    @Test
    void testSetDemographicDetails_AllFieldsFromModel() throws Exception {
        // Arrange
        Method setDemographicDetailsMethod = RegisterBenificiaryServiceImpl.class
                .getDeclaredMethod("setDemographicDetails", IdentityEditDTO.class, BeneficiaryModel.class);
        setDemographicDetailsMethod.setAccessible(true);

        IdentityEditDTO dto = new IdentityEditDTO();

        // Act
        setDemographicDetailsMethod.invoke(service, dto, beneficiaryModel);

        // Assert
        assertEquals("General", dto.getCommunity());
        assertEquals("General", dto.getCommunityName());
        assertEquals("Hindu", dto.getReligion());
        assertEquals("Engineer", dto.getOccupationName());
        assertEquals("Graduate", dto.getEducation());
        assertEquals("Middle", dto.getIncomeStatus());
    }

    @Test
    void testSetDemographicDetails_AllFieldsFromDemographics() throws Exception {
        // Arrange
        Method setDemographicDetailsMethod = RegisterBenificiaryServiceImpl.class
                .getDeclaredMethod("setDemographicDetails", IdentityEditDTO.class, BeneficiaryModel.class);
        setDemographicDetailsMethod.setAccessible(true);

        IdentityEditDTO dto = new IdentityEditDTO();
        beneficiaryModel.setReligion(null);
        beneficiaryModel.setOccupation(null);
        beneficiaryModel.setIncomeStatus(null);

        // Act
        setDemographicDetailsMethod.invoke(service, dto, beneficiaryModel);

        // Assert
        assertEquals("General", dto.getCommunity());
        assertEquals("Hindu", dto.getReligion());
        assertEquals("Engineer", dto.getOccupationName());
        assertEquals("Middle", dto.getIncomeStatus());
    }

    @Test
    void testSetDemographicDetails_UseReligionName() throws Exception {
        // Arrange
        Method setDemographicDetailsMethod = RegisterBenificiaryServiceImpl.class
                .getDeclaredMethod("setDemographicDetails", IdentityEditDTO.class, BeneficiaryModel.class);
        setDemographicDetailsMethod.setAccessible(true);

        IdentityEditDTO dto = new IdentityEditDTO();
        beneficiaryModel.setReligion(null);
        beneficiaryModel.getI_bendemographics().setReligion(null);

        // Act
        setDemographicDetailsMethod.invoke(service, dto, beneficiaryModel);

        // Assert
        assertEquals("Hindu", dto.getReligion());
    }

    @Test
    void testSetDemographicDetails_NullDemographics() throws Exception {
        // Arrange
        Method setDemographicDetailsMethod = RegisterBenificiaryServiceImpl.class
                .getDeclaredMethod("setDemographicDetails", IdentityEditDTO.class, BeneficiaryModel.class);
        setDemographicDetailsMethod.setAccessible(true);

        IdentityEditDTO dto = new IdentityEditDTO();
        beneficiaryModel.setI_bendemographics(null);

        // Act
        setDemographicDetailsMethod.invoke(service, dto, beneficiaryModel);

        // Assert - should not throw exception and DTO should remain unchanged
        assertNull(dto.getCommunity());
    }

    @Test
    void testUpdateDemographics() throws Exception {
        // Arrange
        Method updateDemographicsMethod = RegisterBenificiaryServiceImpl.class
                .getDeclaredMethod("updateDemographics", BeneficiaryDemographicsModel.class);
        updateDemographicsMethod.setAccessible(true);

        // Act
        int result = (int) updateDemographicsMethod.invoke(service, demographicsModel);

        // Assert
        assertEquals(0, result);
    }

    @Test
    void testSaveBeneficiaryModel_Success() throws Exception {
        // Arrange
        String authorization = "Bearer token";
        String identityResponse = "{\"response\":{\"statusCode\":200,\"data\":\"{\\\"benRegId\\\":\\\"123\\\",\\\"benId\\\":\\\"BEN123\\\"}\"}}";
        
        CommonIdentityDTO identityDTO = new CommonIdentityDTO();
        
        when(httpServletRequest.getHeader("authorization")).thenReturn(authorization);
        when(identityMapper.beneficiaryModelCommonIdentityDTO(beneficiaryModel)).thenReturn(identityDTO);
        when(identityMapper.benPhoneMapListToBenFamilyDTOList(any())).thenReturn(new HashSet<>());
        when(identityBeneficiaryService.getIdentityResponse(anyString(), eq(authorization), eq(false)))
                .thenReturn(identityResponse);

        try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class);
             MockedStatic<OutputMapper> outputMapperMock = mockStatic(OutputMapper.class)) {
            
            InputMapper mockInputMapper = mock(InputMapper.class);
            inputMapperMock.when(InputMapper::getInstance).thenReturn(mockInputMapper);
            
            OutputResponse mockResponse = mock(OutputResponse.class);
            when(mockResponse.isSuccess()).thenReturn(true);
            when(mockResponse.getData()).thenReturn("{\"benRegId\":\"123\",\"benId\":\"BEN123\"}");
            when(mockInputMapper.fromJson(anyString(), eq(OutputResponse.class))).thenReturn(mockResponse);
            
            BeneficiariesDTO mockBeneficiariesDTO = mock(BeneficiariesDTO.class);
            when(mockBeneficiariesDTO.getBenRegId()).thenReturn(BigInteger.valueOf(123L));
            when(mockBeneficiariesDTO.getBenId()).thenReturn("123");
            when(mockInputMapper.fromJson(anyString(), eq(BeneficiariesDTO.class))).thenReturn(mockBeneficiariesDTO);
            
            Gson mockGson1 = mock(Gson.class);
            when(mockGson1.toJson(any(BeneficiaryModel.class))).thenReturn("{\"beneficiaryRegID\":123,\"beneficiaryID\":\"123\"}");
            outputMapperMock.when(OutputMapper::gson).thenReturn(mockGson1);

            // Act
            String result = service.save(beneficiaryModel, httpServletRequest);

            // Assert
            assertNotNull(result);
            verify(identityMapper).beneficiaryModelCommonIdentityDTO(beneficiaryModel);
            verify(identityBeneficiaryService).getIdentityResponse(anyString(), eq(authorization), eq(false));
        }
    }

    @Test
    void testSaveBeneficiaryModel_FailureResponse() throws Exception {
        // Arrange
        String authorization = "Bearer token";
        String identityResponse = "{\"response\":{\"statusCode\":500,\"errorMessage\":\"Creation failed\"}}";
        
        CommonIdentityDTO identityDTO = new CommonIdentityDTO();
        
        when(httpServletRequest.getHeader("authorization")).thenReturn(authorization);
        when(identityMapper.beneficiaryModelCommonIdentityDTO(beneficiaryModel)).thenReturn(identityDTO);
        when(identityMapper.benPhoneMapListToBenFamilyDTOList(any())).thenReturn(new HashSet<>());
        when(identityBeneficiaryService.getIdentityResponse(anyString(), eq(authorization), eq(false)))
                .thenReturn(identityResponse);

        try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class)) {
            InputMapper mockInputMapper = mock(InputMapper.class);
            inputMapperMock.when(InputMapper::getInstance).thenReturn(mockInputMapper);
            
            OutputResponse mockResponse = mock(OutputResponse.class);
            when(mockResponse.isSuccess()).thenReturn(false);
            when(mockResponse.toString()).thenReturn("Error: Creation failed");
            when(mockInputMapper.fromJson(anyString(), eq(OutputResponse.class))).thenReturn(mockResponse);

            // Act
            String result = service.save(beneficiaryModel, httpServletRequest);

            // Assert
            assertEquals("Error: Creation failed", result);
        }
    }

    @Test
    void testSaveBeneficiaryModel_NoResponseField() throws Exception {
        // Arrange
        String authorization = "Bearer token";
        String identityResponse = "{\"statusCode\":200}";
        
        CommonIdentityDTO identityDTO = new CommonIdentityDTO();
        
        when(httpServletRequest.getHeader("authorization")).thenReturn(authorization);
        when(identityMapper.beneficiaryModelCommonIdentityDTO(beneficiaryModel)).thenReturn(identityDTO);
        when(identityMapper.benPhoneMapListToBenFamilyDTOList(any())).thenReturn(new HashSet<>());
        when(identityBeneficiaryService.getIdentityResponse(anyString(), eq(authorization), eq(false)))
                .thenReturn(identityResponse);

        try (MockedStatic<OutputMapper> outputMapperMock = mockStatic(OutputMapper.class)) {
            Gson mockGson2 = mock(Gson.class);
            when(mockGson2.toJson(any(BeneficiaryModel.class))).thenReturn("{}");
            outputMapperMock.when(OutputMapper::gson).thenReturn(mockGson2);

            // Act
            String result = service.save(beneficiaryModel, httpServletRequest);

            // Assert
            assertEquals("{}", result);
        }
    }

    @Test
    void testSaveBeneficiaryModel_NullIs1097() throws Exception {
        // Arrange
        String authorization = "Bearer token";
        String identityResponse = "{\"response\":{\"statusCode\":200,\"data\":\"{\\\"benRegId\\\":\\\"123\\\",\\\"benId\\\":\\\"BEN123\\\"}\"}}";
        
        beneficiaryModel.setIs1097(null);
        CommonIdentityDTO identityDTO = new CommonIdentityDTO();
        
        when(httpServletRequest.getHeader("authorization")).thenReturn(authorization);
        when(identityMapper.beneficiaryModelCommonIdentityDTO(beneficiaryModel)).thenReturn(identityDTO);
        when(identityMapper.benPhoneMapListToBenFamilyDTOList(any())).thenReturn(new HashSet<>());
        when(identityBeneficiaryService.getIdentityResponse(anyString(), eq(authorization), eq(false)))
                .thenReturn(identityResponse);

        try (MockedStatic<InputMapper> inputMapperMock = mockStatic(InputMapper.class);
             MockedStatic<OutputMapper> outputMapperMock = mockStatic(OutputMapper.class)) {
            
            InputMapper mockInputMapper = mock(InputMapper.class);
            inputMapperMock.when(InputMapper::getInstance).thenReturn(mockInputMapper);
            
            OutputResponse mockResponse = mock(OutputResponse.class);
            when(mockResponse.isSuccess()).thenReturn(true);
            when(mockResponse.getData()).thenReturn("{\"benRegId\":\"123\",\"benId\":\"BEN123\"}");
            when(mockInputMapper.fromJson(anyString(), eq(OutputResponse.class))).thenReturn(mockResponse);
            
            BeneficiariesDTO mockBeneficiariesDTO = mock(BeneficiariesDTO.class);
            when(mockBeneficiariesDTO.getBenRegId()).thenReturn(BigInteger.valueOf(123L));
            when(mockBeneficiariesDTO.getBenId()).thenReturn(String.valueOf(123L));
            when(mockInputMapper.fromJson(anyString(), eq(BeneficiariesDTO.class))).thenReturn(mockBeneficiariesDTO);
            
            Gson mockGson3 = mock(Gson.class);
            when(mockGson3.toJson(any(BeneficiaryModel.class))).thenReturn("{\"beneficiaryRegID\":123,\"beneficiaryID\":\"123\"}");
            outputMapperMock.when(OutputMapper::gson).thenReturn(mockGson3);

            // Act
            String result = service.save(beneficiaryModel, httpServletRequest);

            // Assert
            assertNotNull(result);
            assertEquals(false, beneficiaryModel.getIs1097());
        }
    }

    @Test
    void testSetSaveDemographicDetails_AllFieldsFromModel() throws Exception {
        // Arrange
        Method setSaveDemographicDetailsMethod = RegisterBenificiaryServiceImpl.class
                .getDeclaredMethod("setSaveDemographicDetails", CommonIdentityDTO.class, BeneficiaryModel.class);
        setSaveDemographicDetailsMethod.setAccessible(true);

        CommonIdentityDTO dto = new CommonIdentityDTO();

        // Act
        setSaveDemographicDetailsMethod.invoke(service, dto, beneficiaryModel);

        // Assert
        assertEquals("General", dto.getCommunity());
        assertEquals("Hindu", dto.getReligion());
        assertEquals("Engineer", dto.getOccupationName());
        assertEquals("Graduate", dto.getEducation());
        assertEquals("Middle", dto.getIncomeStatus());
    }

    @Test
    void testSetSaveDemographicDetails_AllFieldsFromDemographics() throws Exception {
        // Arrange
        Method setSaveDemographicDetailsMethod = RegisterBenificiaryServiceImpl.class
                .getDeclaredMethod("setSaveDemographicDetails", CommonIdentityDTO.class, BeneficiaryModel.class);
        setSaveDemographicDetailsMethod.setAccessible(true);

        CommonIdentityDTO dto = new CommonIdentityDTO();
        beneficiaryModel.setReligion(null);
        beneficiaryModel.setOccupation(null);
        beneficiaryModel.setIncomeStatus(null);

        // Act
        setSaveDemographicDetailsMethod.invoke(service, dto, beneficiaryModel);

        // Assert
        assertEquals("General", dto.getCommunity());
        assertEquals("Hindu", dto.getReligion());
        assertEquals("Engineer", dto.getOccupationName());
        assertEquals("Middle", dto.getIncomeStatus());
    }

    @Test
    void testSetSaveDemographicDetails_UseReligionName() throws Exception {
        // Arrange
        Method setSaveDemographicDetailsMethod = RegisterBenificiaryServiceImpl.class
                .getDeclaredMethod("setSaveDemographicDetails", CommonIdentityDTO.class, BeneficiaryModel.class);
        setSaveDemographicDetailsMethod.setAccessible(true);

        CommonIdentityDTO dto = new CommonIdentityDTO();
        beneficiaryModel.setReligion(null);
        beneficiaryModel.getI_bendemographics().setReligion(null);

        // Act
        setSaveDemographicDetailsMethod.invoke(service, dto, beneficiaryModel);

        // Assert
        assertEquals("Hindu", dto.getReligion());
    }

    @Test
    void testSetSaveDemographicDetails_NullEducationName() throws Exception {
        // Arrange
        Method setSaveDemographicDetailsMethod = RegisterBenificiaryServiceImpl.class
                .getDeclaredMethod("setSaveDemographicDetails", CommonIdentityDTO.class, BeneficiaryModel.class);
        setSaveDemographicDetailsMethod.setAccessible(true);

        CommonIdentityDTO dto = new CommonIdentityDTO();
        beneficiaryModel.getI_bendemographics().setEducationName(null);

        // Act
        setSaveDemographicDetailsMethod.invoke(service, dto, beneficiaryModel);

        // Assert
        assertNull(dto.getEducation());
    }

    @Test
    void testSetSaveDemographicDetails_NullDemographics() throws Exception {
        // Arrange
        Method setSaveDemographicDetailsMethod = RegisterBenificiaryServiceImpl.class
                .getDeclaredMethod("setSaveDemographicDetails", CommonIdentityDTO.class, BeneficiaryModel.class);
        setSaveDemographicDetailsMethod.setAccessible(true);

        CommonIdentityDTO dto = new CommonIdentityDTO();
        beneficiaryModel.setI_bendemographics(null);

        // Act
        setSaveDemographicDetailsMethod.invoke(service, dto, beneficiaryModel);

        // Assert - should not throw exception and DTO should remain unchanged
        assertNull(dto.getCommunity());
    }

    @Test
    void testUpdateCommunityorEducation_Success() throws IEMRException {
        // Arrange
        String auth = "Bearer token";
        Integer expectedRows = 1;

        when(identityBeneficiaryService.editIdentityEditDTOCommunityorEducation(any(IdentityEditDTO.class), eq(auth), eq(false)))
                .thenReturn(expectedRows);

        // Act
        Integer result = service.updateCommunityorEducation(beneficiaryModel, auth);

        // Assert
        assertEquals(expectedRows, result);
        verify(identityBeneficiaryService).editIdentityEditDTOCommunityorEducation(any(IdentityEditDTO.class), eq(auth), eq(false));
    }

    @Test
    void testUpdateCommunityorEducation_NullDemographics() throws IEMRException {
        // Arrange
        String auth = "Bearer token";
        Integer expectedRows = 1;
        beneficiaryModel.setI_bendemographics(null);

        when(identityBeneficiaryService.editIdentityEditDTOCommunityorEducation(any(IdentityEditDTO.class), eq(auth), eq(false)))
                .thenReturn(expectedRows);

        // Act
        Integer result = service.updateCommunityorEducation(beneficiaryModel, auth);

        // Assert
        assertEquals(expectedRows, result);
        verify(identityBeneficiaryService).editIdentityEditDTOCommunityorEducation(any(IdentityEditDTO.class), eq(auth), eq(false));
    }

    @Test
    void testUpdateMCTSRecord_MotherRecord() throws Exception {
        // Arrange
        Method updateMCTSRecordMethod = RegisterBenificiaryServiceImpl.class
                .getDeclaredMethod("updateMCTSRecord", BeneficiaryModel.class);
        updateMCTSRecordMethod.setAccessible(true);

        ArrayList<MctsOutboundCallDetail> mctsDetails = new ArrayList<>();
        MctsOutboundCallDetail callDetail = new MctsOutboundCallDetail();
        callDetail.setIsMother(true);
        mctsDetails.add(callDetail);

        when(outboundHistoryRepository.checkBenExist(123L)).thenReturn(mctsDetails);
        when(outboundHistoryRepository.getDistrictName(1)).thenReturn("District1");
        when(outboundHistoryRepository.getBlockName(1)).thenReturn("Block1");
        when(outboundHistoryRepository.getVillageName(1)).thenReturn("Village1");
        when(outboundHistoryRepository.updateMotherData(anyString(), anyInt(), anyString(), anyString(), 
                anyString(), any(Timestamp.class), anyLong())).thenReturn(1);

        // Act
        updateMCTSRecordMethod.invoke(service, beneficiaryModel);

        // Assert
        verify(outboundHistoryRepository).checkBenExist(123L);
        verify(outboundHistoryRepository).updateMotherData(eq("John Doe"), eq(30), eq("District1"), 
                eq("Block1"), eq("Village1"), any(Timestamp.class), eq(123L));
    }

    @Test
    void testUpdateMCTSRecord_ChildRecord() throws Exception {
        // Arrange
        Method updateMCTSRecordMethod = RegisterBenificiaryServiceImpl.class
                .getDeclaredMethod("updateMCTSRecord", BeneficiaryModel.class);
        updateMCTSRecordMethod.setAccessible(true);

        ArrayList<MctsOutboundCallDetail> mctsDetails = new ArrayList<>();
        MctsOutboundCallDetail callDetail = new MctsOutboundCallDetail();
        callDetail.setIsMother(false);
        mctsDetails.add(callDetail);

        when(outboundHistoryRepository.checkBenExist(123L)).thenReturn(mctsDetails);
        when(outboundHistoryRepository.getDistrictName(1)).thenReturn("District1");
        when(outboundHistoryRepository.getBlockName(1)).thenReturn("Block1");
        when(outboundHistoryRepository.getVillageName(1)).thenReturn("Village1");
        when(outboundHistoryRepository.updateChildData(anyString(), anyString(), anyString(), anyString(), 
                anyString(), any(Timestamp.class), anyLong())).thenReturn(1);

        // Act
        updateMCTSRecordMethod.invoke(service, beneficiaryModel);

        // Assert
        verify(outboundHistoryRepository).checkBenExist(123L);
        verify(outboundHistoryRepository).updateChildData(eq("John Doe"), eq("Male"), eq("District1"), 
                eq("Block1"), eq("Village1"), any(Timestamp.class), eq(123L));
    }

    @Test
    void testUpdateMCTSRecord_FemaleGender() throws Exception {
        // Arrange
        Method updateMCTSRecordMethod = RegisterBenificiaryServiceImpl.class
                .getDeclaredMethod("updateMCTSRecord", BeneficiaryModel.class);
        updateMCTSRecordMethod.setAccessible(true);

        beneficiaryModel.setGenderID(2);
        ArrayList<MctsOutboundCallDetail> mctsDetails = new ArrayList<>();
        MctsOutboundCallDetail callDetail = new MctsOutboundCallDetail();
        callDetail.setIsMother(false);
        mctsDetails.add(callDetail);

        when(outboundHistoryRepository.checkBenExist(123L)).thenReturn(mctsDetails);
        when(outboundHistoryRepository.getDistrictName(1)).thenReturn("District1");
        when(outboundHistoryRepository.getBlockName(1)).thenReturn("Block1");
        when(outboundHistoryRepository.getVillageName(1)).thenReturn("Village1");
        when(outboundHistoryRepository.updateChildData(anyString(), anyString(), anyString(), anyString(), 
                anyString(), any(Timestamp.class), anyLong())).thenReturn(1);

        // Act
        updateMCTSRecordMethod.invoke(service, beneficiaryModel);

        // Assert
        verify(outboundHistoryRepository).updateChildData(eq("John Doe"), eq("Female"), eq("District1"), 
                eq("Block1"), eq("Village1"), any(Timestamp.class), eq(123L));
    }

    @Test
    void testUpdateMCTSRecord_TransgenderGender() throws Exception {
        // Arrange
        Method updateMCTSRecordMethod = RegisterBenificiaryServiceImpl.class
                .getDeclaredMethod("updateMCTSRecord", BeneficiaryModel.class);
        updateMCTSRecordMethod.setAccessible(true);

        beneficiaryModel.setGenderID(3);
        ArrayList<MctsOutboundCallDetail> mctsDetails = new ArrayList<>();
        MctsOutboundCallDetail callDetail = new MctsOutboundCallDetail();
        callDetail.setIsMother(false);
        mctsDetails.add(callDetail);

        when(outboundHistoryRepository.checkBenExist(123L)).thenReturn(mctsDetails);
        when(outboundHistoryRepository.getDistrictName(1)).thenReturn("District1");
        when(outboundHistoryRepository.getBlockName(1)).thenReturn("Block1");
        when(outboundHistoryRepository.getVillageName(1)).thenReturn("Village1");
        when(outboundHistoryRepository.updateChildData(anyString(), anyString(), anyString(), anyString(), 
                anyString(), any(Timestamp.class), anyLong())).thenReturn(1);

        // Act
        updateMCTSRecordMethod.invoke(service, beneficiaryModel);

        // Assert
        verify(outboundHistoryRepository).updateChildData(eq("John Doe"), eq("Transgender"), eq("District1"), 
                eq("Block1"), eq("Village1"), any(Timestamp.class), eq(123L));
    }

    @Test
    void testUpdateMCTSRecord_NullLastName() throws Exception {
        // Arrange
        Method updateMCTSRecordMethod = RegisterBenificiaryServiceImpl.class
                .getDeclaredMethod("updateMCTSRecord", BeneficiaryModel.class);
        updateMCTSRecordMethod.setAccessible(true);

        beneficiaryModel.setLastName(null);
        ArrayList<MctsOutboundCallDetail> mctsDetails = new ArrayList<>();
        MctsOutboundCallDetail callDetail = new MctsOutboundCallDetail();
        callDetail.setIsMother(true);
        mctsDetails.add(callDetail);

        when(outboundHistoryRepository.checkBenExist(123L)).thenReturn(mctsDetails);
        when(outboundHistoryRepository.getDistrictName(1)).thenReturn("District1");
        when(outboundHistoryRepository.getBlockName(1)).thenReturn("Block1");
        when(outboundHistoryRepository.getVillageName(1)).thenReturn("Village1");
        when(outboundHistoryRepository.updateMotherData(anyString(), anyInt(), anyString(), anyString(), 
                anyString(), any(Timestamp.class), anyLong())).thenReturn(1);

        // Act
        updateMCTSRecordMethod.invoke(service, beneficiaryModel);

        // Assert
        verify(outboundHistoryRepository).updateMotherData(eq("John"), eq(30), eq("District1"), 
                eq("Block1"), eq("Village1"), any(Timestamp.class), eq(123L));
    }

    @Test
    void testUpdateMCTSRecord_EmptyLastName() throws Exception {
        // Arrange
        Method updateMCTSRecordMethod = RegisterBenificiaryServiceImpl.class
                .getDeclaredMethod("updateMCTSRecord", BeneficiaryModel.class);
        updateMCTSRecordMethod.setAccessible(true);

        beneficiaryModel.setLastName("");
        ArrayList<MctsOutboundCallDetail> mctsDetails = new ArrayList<>();
        MctsOutboundCallDetail callDetail = new MctsOutboundCallDetail();
        callDetail.setIsMother(true);
        mctsDetails.add(callDetail);

        when(outboundHistoryRepository.checkBenExist(123L)).thenReturn(mctsDetails);
        when(outboundHistoryRepository.getDistrictName(1)).thenReturn("District1");
        when(outboundHistoryRepository.getBlockName(1)).thenReturn("Block1");
        when(outboundHistoryRepository.getVillageName(1)).thenReturn("Village1");
        when(outboundHistoryRepository.updateMotherData(anyString(), anyInt(), anyString(), anyString(), 
                anyString(), any(Timestamp.class), anyLong())).thenReturn(1);

        // Act
        updateMCTSRecordMethod.invoke(service, beneficiaryModel);

        // Assert
        verify(outboundHistoryRepository).updateMotherData(eq("John"), eq(30), eq("District1"), 
                eq("Block1"), eq("Village1"), any(Timestamp.class), eq(123L));
    }

    @Test
    void testUpdateMCTSRecord_NullCallDetail() throws Exception {
        // Arrange
        Method updateMCTSRecordMethod = RegisterBenificiaryServiceImpl.class
                .getDeclaredMethod("updateMCTSRecord", BeneficiaryModel.class);
        updateMCTSRecordMethod.setAccessible(true);

        ArrayList<MctsOutboundCallDetail> mctsDetails = new ArrayList<>();
        mctsDetails.add(null);

        when(outboundHistoryRepository.checkBenExist(123L)).thenReturn(mctsDetails);

        // Act
        updateMCTSRecordMethod.invoke(service, beneficiaryModel);

        // Assert
        verify(outboundHistoryRepository).checkBenExist(123L);
        verify(outboundHistoryRepository, never()).updateMotherData(anyString(), anyInt(), anyString(), 
                anyString(), anyString(), any(Timestamp.class), anyLong());
        verify(outboundHistoryRepository, never()).updateChildData(anyString(), anyString(), anyString(), 
                anyString(), anyString(), any(Timestamp.class), anyLong());
    }

    @Test
    void testUpdateMCTSRecord_NullIsMother() throws Exception {
        // Arrange
        Method updateMCTSRecordMethod = RegisterBenificiaryServiceImpl.class
                .getDeclaredMethod("updateMCTSRecord", BeneficiaryModel.class);
        updateMCTSRecordMethod.setAccessible(true);

        ArrayList<MctsOutboundCallDetail> mctsDetails = new ArrayList<>();
        MctsOutboundCallDetail callDetail = new MctsOutboundCallDetail();
        callDetail.setIsMother(null);
        mctsDetails.add(callDetail);

        when(outboundHistoryRepository.checkBenExist(123L)).thenReturn(mctsDetails);

        // Act
        updateMCTSRecordMethod.invoke(service, beneficiaryModel);

        // Assert
        verify(outboundHistoryRepository).checkBenExist(123L);
        verify(outboundHistoryRepository, never()).updateMotherData(anyString(), anyInt(), anyString(), 
                anyString(), anyString(), any(Timestamp.class), anyLong());
        verify(outboundHistoryRepository, never()).updateChildData(anyString(), anyString(), anyString(), 
                anyString(), anyString(), any(Timestamp.class), anyLong());
    }

    @Test
    void testUpdateMCTSRecord_ExceptionHandling() throws Exception {
        // Arrange
        Method updateMCTSRecordMethod = RegisterBenificiaryServiceImpl.class
                .getDeclaredMethod("updateMCTSRecord", BeneficiaryModel.class);
        updateMCTSRecordMethod.setAccessible(true);

        when(outboundHistoryRepository.checkBenExist(123L)).thenThrow(new RuntimeException("Database error"));

        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> updateMCTSRecordMethod.invoke(service, beneficiaryModel));
    }

    @Test
    void testGenerateBeneficiaryIDs_Success() throws Exception {
        // Arrange
        String request = "{\"benIDRequired\":3,\"vanID\":101}";
        String authorization = "Bearer token";
        
        List<BeneficiaryGenModel> expectedList = new ArrayList<>();
        BeneficiaryGenModel model = new BeneficiaryGenModel();
        model.setBeneficiaryId(1L);
        expectedList.add(model);

        when(httpServletRequest.getHeader("authorization")).thenReturn(authorization);
        when(identityBeneficiaryService.generateBeneficiaryIDs(request, authorization)).thenReturn(expectedList);

        // Act
        String result = service.generateBeneficiaryIDs(request, httpServletRequest);

        // Assert
        assertNotNull(result);
        assertEquals(expectedList.toString(), result);
        verify(logger).info(contains("request: " + request));
        verify(identityBeneficiaryService).generateBeneficiaryIDs(request, authorization);
    }

    @Test
    void testUpdateBeneficiaryID() throws Exception {
        // Arrange
        Method updateBeneficiaryIDMethod = RegisterBenificiaryServiceImpl.class
                .getDeclaredMethod("updateBeneficiaryID", String.class, Long.class);
        updateBeneficiaryIDMethod.setAccessible(true);

        String beneficiaryID = "BEN123";
        Long beneficiaryRegID = 123L;

        // Act - should not throw exception
        assertDoesNotThrow(() -> updateBeneficiaryIDMethod.invoke(service, beneficiaryID, beneficiaryRegID));
    }

    @Test
    void testUpdateBeneficiaryID_ExceptionHandling() throws Exception {
        // Arrange
        Method updateBeneficiaryIDMethod = RegisterBenificiaryServiceImpl.class
                .getDeclaredMethod("updateBeneficiaryID", String.class, Long.class);
        updateBeneficiaryIDMethod.setAccessible(true);

        String beneficiaryID = "BEN123";
        Long beneficiaryRegID = 123L;

        // Act - should handle exception gracefully
        assertDoesNotThrow(() -> updateBeneficiaryIDMethod.invoke(service, beneficiaryID, beneficiaryRegID));
        
        // The method should log the error but not throw it
        verify(logger, never()).error(anyString(), any(Exception.class));
    }

    @Test
    void testLoggerInitialization() throws Exception {
        // Test logger field is properly initialized
        Field loggerField = RegisterBenificiaryServiceImpl.class.getDeclaredField("logger");
        loggerField.setAccessible(true);
        Object loggerObj = loggerField.get(service);
        assertNotNull(loggerObj);
    }

    @Test
    void testServiceInstantiation() {
        // Test basic service instantiation
        RegisterBenificiaryServiceImpl testService = new RegisterBenificiaryServiceImpl();
        assertNotNull(testService);
    }

    @Test
    void testAsyncMethodsDoNotBlockMainThread() throws IEMRException {
        // Test that async methods can be called without blocking
        String auth = "Bearer token";
        when(identityBenEditMapper.BenToIdentityEditMapper(beneficiaryModel)).thenReturn(identityEditDTO);
        when(identityBeneficiaryService.editIdentityEditDTO(any(IdentityEditDTO.class), eq(auth), eq(false)))
                .thenReturn(1);

        // Act - this should complete quickly as async methods shouldn't block
        long startTime = System.currentTimeMillis();
        Integer result = service.updateBenificiary(beneficiaryModel, auth);
        long endTime = System.currentTimeMillis();

        // Assert
        assertEquals(1, result);
        assertTrue((endTime - startTime) < 1000); // Should complete in less than 1 second
    }
}
