// package com.iemr.common.controller.beneficiary;

// import com.google.gson.Gson;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
// import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
// import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
// import org.springframework.boot.test.mock.mockito.MockBean;
// import org.springframework.http.MediaType;
// import org.springframework.test.context.ContextConfiguration;
// import org.springframework.test.web.servlet.MockMvc;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
// import static org.mockito.Mockito.*;

// // Required imports from prompt
// import com.iemr.common.data.beneficiary.BenPhoneMap;
// import com.iemr.common.data.beneficiary.BeneficiaryRegistrationData;
// import com.iemr.common.data.directory.Directory;
// import com.iemr.common.model.beneficiary.BeneficiaryModel;
// import com.iemr.common.service.beneficiary.BenRelationshipTypeService;
// import com.iemr.common.service.beneficiary.BeneficiaryOccupationService;
// import com.iemr.common.service.beneficiary.GovtIdentityTypeService;
// import com.iemr.common.service.beneficiary.IEMRBeneficiaryTypeService;
// import com.iemr.common.service.beneficiary.IEMRSearchUserService;
// import com.iemr.common.service.beneficiary.RegisterBenificiaryService;
// import com.iemr.common.service.beneficiary.SexualOrientationService;
// import com.iemr.common.service.directory.DirectoryService;
// import com.iemr.common.service.location.LocationService;
// import com.iemr.common.service.reports.CallReportsService;
// import com.iemr.common.service.userbeneficiarydata.CommunityService;
// import com.iemr.common.service.userbeneficiarydata.EducationService;
// import com.iemr.common.service.userbeneficiarydata.GenderService;
// import com.iemr.common.service.userbeneficiarydata.LanguageService;
// import com.iemr.common.service.userbeneficiarydata.MaritalStatusService;
// import com.iemr.common.service.userbeneficiarydata.StatusService;
// import com.iemr.common.service.userbeneficiarydata.TitleService;
// import com.iemr.common.utils.mapper.InputMapper;
// import com.iemr.common.utils.mapper.OutputMapper;
// import com.iemr.common.utils.response.OutputResponse;

// import java.sql.Timestamp;
// import java.util.Arrays;
// import java.util.Collections;
// import java.util.List;
// import java.util.Map;
// import java.util.HashMap;

// @WebMvcTest(controllers = BeneficiaryRegistrationController.class, 
//             excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class})
// @ContextConfiguration(classes = {BeneficiaryRegistrationController.class})
// class BeneficiaryRegistrationControllerTest {

//     @Autowired
//     private MockMvc mockMvc;

//     // Mocked services as per controller's setters
//     @MockBean BenRelationshipTypeService benRelationshipTypeService;
//     @MockBean BeneficiaryOccupationService beneficiaryOccupationService;
//     @MockBean IEMRSearchUserService iemrSearchUserService;
//     @MockBean IEMRBeneficiaryTypeService iemrBeneficiaryTypeService;
//     @MockBean RegisterBenificiaryService registerBenificiaryService;
//     @MockBean EducationService educationService;
//     @MockBean TitleService titleService;
//     @MockBean StatusService statusService;
//     @MockBean LocationService locationService;
//     @MockBean GenderService genderService;
//     @MockBean MaritalStatusService maritalStatusService;
//     @MockBean CommunityService communityService;
//     @MockBean LanguageService languageService;
//     @MockBean DirectoryService directoryService;
//     @MockBean SexualOrientationService sexualOrientationService;
//     @MockBean GovtIdentityTypeService govtIdentityTypeService;

//     private Gson gson = new Gson();

//     // Dummy classes for models not fully defined with packages/constructors in the prompt's context.
//     // These are minimal implementations to allow mocking and compilation.
//     // Placed as static nested classes to avoid "cannot find symbol" errors for assumed packages.

//     static class Beneficiary {
//         private Long beneficiaryRegID;
//         private String firstName;
//         private String phoneNo;

//         public Beneficiary() {}
//         public Long getBeneficiaryRegID() { return beneficiaryRegID; }
//         public void setBeneficiaryRegID(Long beneficiaryRegID) { this.beneficiaryRegID = beneficiaryRegID; }
//         public String getFirstName() { return firstName; }
//         public void setFirstName(String firstName) { this.firstName = firstName; }
//         public String getPhoneNo() { return phoneNo; }
//         public void setPhoneNo(String phoneNo) { this.phoneNo = phoneNo; }
//     }

//     static class BenRelationshipType {
//         private Integer benRelationshipID;
//         private String benRelationshipType;
//         public BenRelationshipType(Integer id, String type) { this.benRelationshipID = id; this.benRelationshipType = type; }
//         public Integer getBenRelationshipID() { return benRelationshipID; }
//         public String getBenRelationshipType() { return benRelationshipType; }
//     }

//     static class BeneficiaryOccupation {
//         private Long occupationID;
//         private String occupationType;
//         public BeneficiaryOccupation(Long id, String type) { this.occupationID = id; this.occupationType = type; }
//         public Long getOccupationID() { return occupationID; }
//         public String getOccupationType() { return occupationType; }
//     }

//     static class BeneficiaryType {
//         private Short beneficiaryTypeID;
//         private String beneficiaryType;
//         public BeneficiaryType(Short id, String type) { this.beneficiaryTypeID = id; this.beneficiaryType = type; }
//         public Short getBeneficiaryTypeID() { return beneficiaryTypeID; }
//         public String getBeneficiaryType() { return beneficiaryType; }
//     }

//     static class GovtIdentityType {
//         private Integer govtIdentityTypeID;
//         private String identityType;
//         public GovtIdentityType(Integer id, String type) { this.govtIdentityTypeID = id; this.identityType = type; }
//         public Integer getGovtIdentityTypeID() { return govtIdentityTypeID; }
//         public String getIdentityType() { return identityType; }
//     }

//     static class SexualOrientation {
//         private Short sexualOrientationId;
//         private String sexualOrientationName;
//         public SexualOrientation(Short id, String name) { this.sexualOrientationId = id; this.sexualOrientationName = name; }
//         public Short getSexualOrientationId() { return sexualOrientationId; }
//         public String getSexualOrientationName() { return sexualOrientationName; }
//     }

//     // Using BeneficiaryEducation as per dependency list, assuming it's the return type for EducationService
//     static class BeneficiaryEducation {
//         private Long educationID;
//         private String educationType;
//         public BeneficiaryEducation(Long id, String type) { this.educationID = id; this.educationType = type; }
//         public Long getEducationID() { return educationID; }
//         public String getEducationType() { return educationType; }
//     }

//     static class Title {
//         private Integer titleID;
//         private String titleName;
//         public Title(Integer id, String name) { this.titleID = id; this.titleName = name; }
//         public Integer getTitleID() { return titleID; }
//         public String getTitleName() { return titleName; }
//     }

//     static class Status {
//         private Integer statusID;
//         private String status;
//         public Status(Integer id, String status) { this.statusID = id; this.status = status; }
//         public Integer getStatusID() { return statusID; }
//         public String getStatus() { return status; }
//     }

//     static class States {
//         private Integer stateID;
//         private String stateName;
//         public States(Integer id, String name) { this.stateID = id; this.stateName = name; }
//         public Integer getStateID() { return stateID; }
//         public String getStateName() { return stateName; }
//     }

//     static class Districts {
//         private Integer districtID;
//         private String districtName;
//         public Districts(Integer id, String name) { this.districtID = id; this.districtName = name; }
//         public Integer getDistrictID() { return districtID; }
//         public String getDistrictName() { return districtName; }
//     }

//     static class DistrictBlock {
//         private Integer blockID;
//         private String blockName;
//         public DistrictBlock(Integer id, String name) { this.blockID = id; this.blockName = name; }
//         public Integer getBlockID() { return blockID; }
//         public String getBlockName() { return blockName; }
//     }

//     static class Country {
//         private Integer countryID;
//         private String countryName;
//         public Country(Integer id, String name) { this.countryID = id; this.countryName = name; }
//         public Integer getCountryID() { return countryID; }
//         public String getCountryName() { return countryName; }
//     }

//     static class Gender {
//         private Integer genderID;
//         private String genderName;
//         public Gender(Integer id, String name) { this.genderID = id; this.genderName = name; }
//         public Integer getGenderID() { return genderID; }
//         public String getGenderName() { return genderName; }
//     }

//     static class MaritalStatus {
//         private Integer maritalStatusID;
//         private String status;
//         public MaritalStatus(Integer id, String status) { this.maritalStatusID = id; this.status = status; }
//         public Integer getMaritalStatusID() { return maritalStatusID; }
//         public String getStatus() { return status; }
//     }

//     static class Community {
//         private Integer communityID;
//         private String communityType;
//         public Community(Integer id, String type) { this.communityID = id; this.communityType = type; }
//         public Integer getCommunityID() { return communityID; }
//         public String getCommunityType() { return communityType; }
//     }

//     static class Language {
//         private Integer languageID;
//         private String languageName;
//         public Language(Integer id, String name) { this.languageID = id; this.languageName = name; }
//         public Integer getLanguageID() { return languageID; }
//         public String getLanguageName() { return languageName; }
//     }

//     // Test for createBeneficiary(@RequestBody String request, HttpServletRequest httpRequest)
//     @Test
//     void shouldCreateBeneficiary_whenValidInputProvided() throws Exception {
//         String requestJson = "{\"providerServiceMapID\":1,\"firstName\":\"John\",\"lastName\":\"Doe\",\"dOB\":\"2000-01-01 00:00:00\","
//                 + "\"ageUnits\":\"Years\",\"fatherName\":\"Sr. Doe\",\"spouseName\":\"Mrs. Doe\",\"govtIdentityNo\":\"123456789012\","
//                 + "\"govtIdentityTypeID\":1,\"emergencyRegistration\":false,\"createdBy\":\"testuser\","
//                 + "\"titleId\":1,\"statusID\":1,\"registeredServiceID\":1,\"maritalStatusID\":1,"
//                 + "\"genderID\":1,\"i_bendemographics\":{\"educationID\":1,\"occupationID\":1,"
//                 + "\"healthCareWorkerID\":1,\"communityID\":1,\"districtID\":1,\"stateID\":1,"
//                 + "\"pinCode\":\"123456\",\"blockID\":1,\"districtBranchID\":1,\"createdBy\":\"testuser\","
//                 + "\"addressLine1\":\"123 Main St\"},\"benPhoneMaps\":[{\"parentBenRegID\":null,\"phoneNo\":\"9876543210\","
//                 + "\"phoneTypeID\":1,\"benRelationshipID\":1,\"deleted\":false,\"createdBy\":\"testuser\"}],"
//                 + "\"faceEmbedding\":[]}";

//         // Mock the service response - should return the saved beneficiary model
//         BeneficiaryModel mockBeneficiary = new BeneficiaryModel();
//         when(registerBenificiaryService.save(any(BeneficiaryModel.class), any())).thenReturn(mockBeneficiary);

//         mockMvc.perform(post("/beneficiary/createBeneficiary")
//                 .header("Authorization", "Bearer token") // Required by controller
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(requestJson)
//                 .accept(MediaType.APPLICATION_JSON))
//                 .andExpect(status().isOk())
//                 .andExpect(content().contentType("text/plain;charset=UTF-8")) // Controller returns String
//                 .andExpect(jsonPath("$.statusCode").value(200))
//                 .andExpect(jsonPath("$.status").value("Success"))
//                 .andExpected(jsonPath("$.data").exists());
//     }

//     @Test
//     void shouldReturnBadRequest_whenCreateBeneficiaryInputIsInvalid() throws Exception {
//         String invalidRequestJson = "{\"firstName\":\"John\"}"; // Missing required fields

//         mockMvc.perform(post("/beneficiary/createBeneficiary")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(invalidRequestJson))
//                 .andExpect(status().isBadRequest()); // Assuming controller handles invalid JSON with 400
//     }

//     @Test
//     void shouldReturnInternalServerError_whenCreateBeneficiaryServiceFails() throws Exception {
//         String requestJson = "{\"providerServiceMapID\":1,\"firstName\":\"John\",\"lastName\":\"Doe\",\"dOB\":\"2000-01-01 00:00:00\","
//                 + "\"ageUnits\":\"Years\",\"fatherName\":\"Sr. Doe\",\"spouseName\":\"Mrs. Doe\",\"govtIdentityNo\":\"123456789012\","
//                 + "\"govtIdentityTypeID\":1,\"emergencyRegistration\":false,\"createdBy\":\"testuser\","
//                 + "\"titleId\":1,\"statusID\":1,\"registeredServiceID\":1,\"maritalStatusID\":1,"
//                 + "\"genderID\":1,\"i_bendemographics\":{\"educationID\":1,\"occupationID\":1,"
//                 + "\"healthCareWorkerID\":1,\"communityID\":1,\"districtID\":1,\"stateID\":1,"
//                 + "\"pinCode\":\"123456\",\"blockID\":1,\"districtBranchID\":1,\"createdBy\":\"testuser\","
//                 + "\"addressLine1\":\"123 Main St\"},\"benPhoneMaps\":[{\"parentBenRegID\":null,\"phoneNo\":\"9876543210\","
//                 + "\"phoneTypeID\":1,\"benRelationshipID\":1,\"deleted\":false,\"createdBy\":\"testuser\"}],"
//                 + "\"faceEmbedding\":[]}";

//         when(registerBenificiaryService.save(any(Beneficiary.class))).thenThrow(new RuntimeException("Database error"));

//         mockMvc.perform(post("/beneficiary/createBeneficiary")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(requestJson))
//                 .andExpect(status().isInternalServerError()); // Assuming controller maps exceptions to 500
//     }

//     // Test for searchUserByID(@Param(value = "...") String request) - Assuming POST with JSON body
//     @Test
//     void shouldReturnUser_whenSearchUserByIDFindsUser() throws Exception {
//         String requestJson = "{\"beneficiaryRegID\":123,\"beneficiaryID\":null,\"HealthID\":null,\"HealthIDNo\":null}";
//         BeneficiaryModel mockBeneficiaryModel = new BeneficiaryModel();
//         mockBeneficiaryModel.setBeneficiaryRegID(123L);
//         mockBeneficiaryModel.setFirstName("Jane");

//         // Mocking one of the search methods that IEMRSearchUserService might call based on the input
//         when(iemrSearchUserService.userExitsCheckWithGovIdentity(anyString(), anyString(), anyBoolean()))
//                 .thenReturn(Collections.singletonList(mockBeneficiaryModel));

//         mockMvc.perform(post("/beneficiary/searchUserByID")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(requestJson))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.statusCode").value(200))
//                 .andExpect(jsonPath("$.data[0].beneficiaryRegID").value(123L));
//     }

//     @Test
//     void shouldReturnEmptyList_whenSearchUserByIDFindsNoUser() throws Exception {
//         String requestJson = "{\"beneficiaryRegID\":999,\"beneficiaryID\":null,\"HealthID\":null,\"HealthIDNo\":null}";

//         when(iemrSearchUserService.userExitsCheckWithGovIdentity(anyString(), anyString(), anyBoolean()))
//                 .thenReturn(Collections.emptyList());

//         mockMvc.perform(post("/beneficiary/searchUserByID")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(requestJson))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.statusCode").value(200))
//                 .andExpect(jsonPath("$.data").isArray())
//                 .andExpect(jsonPath("$.data").isEmpty());
//     }

//     // Test for searchUserByPhone(@Param(value = "...") String request) - Assuming POST with JSON body
//     @Test
//     void shouldReturnUsers_whenSearchUserByPhoneFindsUsers() throws Exception {
//         String requestJson = "{\"phoneNo\":\"9876543210\",\"pageNo\":0,\"rowsPerPage\":10}";
//         BeneficiaryModel mockBeneficiaryModel = new BeneficiaryModel();
//         mockBeneficiaryModel.setBeneficiaryRegID(123L);
//         mockBeneficiaryModel.setFirstName("Jane");

//         // Assuming controller calls a search method like userExitsCheckWithGovIdentity or similar
//         when(iemrSearchUserService.userExitsCheckWithGovIdentity(anyString(), anyString(), anyBoolean()))
//                 .thenReturn(Collections.singletonList(mockBeneficiaryModel));

//         mockMvc.perform(post("/beneficiary/searchUserByPhone")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(requestJson))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.statusCode").value(200))
//                 .andExpect(jsonPath("$.data[0].firstName").value("Jane"));
//     }

//     // Test for searchBeneficiary(@Param(value = "...") String request) - Assuming POST with JSON body
//     @Test
//     void shouldReturnBeneficiaries_whenSearchBeneficiaryFindsMatches() throws Exception {
//         String requestJson = "{\"firstName\":\"Test\",\"lastName\":\"User\",\"genderID\":1,\"beneficiaryID\":null,"
//                 + "\"i_bendemographics\":{\"stateID\":1,\"districtID\":1,\"districtBranchID\":null}}";
//         BeneficiaryModel mockBeneficiaryModel = new BeneficiaryModel();
//         mockBeneficiaryModel.setBeneficiaryRegID(456L);
//         mockBeneficiaryModel.setFirstName("Test");

//         // Assuming controller calls a search method like userExitsCheckWithGovIdentity or similar
//         when(iemrSearchUserService.userExitsCheckWithGovIdentity(anyString(), anyString(), anyBoolean()))
//                 .thenReturn(Collections.singletonList(mockBeneficiaryModel));

//         mockMvc.perform(post("/beneficiary/searchBeneficiary")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(requestJson))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.statusCode").value(200))
//                 .andExpect(jsonPath("$.data[0].beneficiaryRegID").value(456L));
//     }

//     // Test for getRegistrationData()
//     @Test
//     void shouldReturnAllRegistrationData_whenGetRegistrationDataCalled() throws Exception {
//         when(benRelationshipTypeService.getRelations()).thenReturn(Arrays.asList(new BenRelationshipType(1, "Self")));
//         when(beneficiaryOccupationService.getBeneficiaryOccupations()).thenReturn(Arrays.asList(new BeneficiaryOccupation(1L, "Student")));
//         when(iemrBeneficiaryTypeService.getBeneficiaryTypes()).thenReturn(Arrays.asList(new BeneficiaryType( (short)1, "Patient")));
//         when(educationService.getEducations()).thenReturn(Arrays.asList(new BeneficiaryEducation(1L, "High School"))); // Using BeneficiaryEducation
//         when(titleService.getTitles()).thenReturn(Arrays.asList(new Title(1, "Mr.")));
//         when(statusService.getStatuses()).thenReturn(Arrays.asList(new Status(1, "Active")));
//         when(locationService.getStates(anyInt())).thenReturn(Arrays.asList(new States(1, "State1")));
//         when(locationService.getDistricts(anyInt())).thenReturn(Arrays.asList(new Districts(1, "District1")));
//         when(locationService.getDistrictBlocks(anyInt())).thenReturn(Arrays.asList(new DistrictBlock(1, "Block1")));
//         when(locationService.getCountries()).thenReturn(Arrays.asList(new Country(1, "India")));
//         when(genderService.getGenders()).thenReturn(Arrays.asList(new Gender(1, "Male")));
//         when(maritalStatusService.getMaritalStatuses()).thenReturn(Arrays.asList(new MaritalStatus(1, "Single")));
//         when(communityService.getCommunities()).thenReturn(Arrays.asList(new Community(1, "General")));
//         when(languageService.getLanguages()).thenReturn(Arrays.asList(new Language(1, "English")));
//         when(directoryService.getDirectories()).thenReturn(Arrays.asList(new Directory(1, "Directory1")));
//         when(sexualOrientationService.getSexualOrientations()).thenReturn(Arrays.asList(new SexualOrientation( (short)1, "Heterosexual")));
//         when(govtIdentityTypeService.getGovtIdentityTypes()).thenReturn(Arrays.asList(new GovtIdentityType(1, "Aadhaar")));

//         mockMvc.perform(get("/beneficiary/getRegistrationData"))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.statusCode").value(200))
//                 .andExpect(jsonPath("$.data.benRelationshipTypes").isArray())
//                 .andExpect(jsonPath("$.data.beneficiaryOccupations").isArray())
//                 .andExpect(jsonPath("$.data.iemrBeneficiaryTypes").isArray())
//                 .andExpect(jsonPath("$.data.educations").isArray())
//                 .andExpect(jsonPath("$.data.titles").isArray())
//                 .andExpect(jsonPath("$.data.statuses").isArray())
//                 .andExpect(jsonPath("$.data.states").isArray())
//                 .andExpect(jsonPath("$.data.districts").isArray())
//                 .andExpect(jsonPath("$.data.districtBlocks").isArray())
//                 .andExpect(jsonPath("$.data.countries").isArray())
//                 .andExpect(jsonPath("$.data.genders").isArray())
//                 .andExpect(jsonPath("$.data.maritalStatuses").isArray())
//                 .andExpect(jsonPath("$.data.communities").isArray())
//                 .andExpect(jsonPath("$.data.languages").isArray())
//                 .andExpect(jsonPath("$.data.directories").isArray())
//                 .andExpect(jsonPath("$.data.sexualOrientations").isArray())
//                 .andExpect(jsonPath("$.data.govtIdentityTypes").isArray());
//     }

//     @Test
//     void shouldReturnInternalServerError_whenGetRegistrationDataServiceFails() throws Exception {
//         when(benRelationshipTypeService.getRelations()).thenThrow(new RuntimeException("Service unavailable"));

//         mockMvc.perform(get("/beneficiary/getRegistrationData"))
//                 .andExpect(status().isInternalServerError());
//     }

//     // Test for getRegistrationDataV1(@Param(value = "{\"providerServiceMapID\":\"Integer\"}") String request) - Assuming POST with JSON body
//     @Test
//     void shouldReturnRegistrationDataV1_whenProviderServiceMapIDProvided() throws Exception {
//         String requestJson = "{\"providerServiceMapID\":1}";
//         when(directoryService.getDirectories(anyInt())).thenReturn(Arrays.asList(new Directory(1, "DirectoryV1")));

//         mockMvc.perform(post("/beneficiary/getRegistrationDataV1")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(requestJson))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.statusCode").value(200))
//                 .andExpect(jsonPath("$.data.directories").isArray());
//     }

//     // Test for updateBenefciary(@Param(value = "...") String request) - Assuming POST with JSON body
//     @Test
//     void shouldUpdateBeneficiary_whenValidUpdateInputProvided() throws Exception {
//         String requestJson = "{\"beneficiaryRegID\":1,\"firstName\":\"UpdatedJohn\",\"lastName\":\"Doe\","
//                 + "\"dOB\":\"2000-01-01 00:00:00\",\"ageUnits\":\"Years\",\"fatherName\":\"Sr. Doe\",\"spouseName\":\"Mrs. Doe\","
//                 + "\"govtIdentityNo\":\"123456789012\",\"govtIdentityTypeID\":1,\"emergencyRegistration\":false,"
//                 + "\"createdBy\":\"testuser\",\"titleId\":1,\"statusID\":1,\"registeredServiceID\":1,"
//                 + "\"maritalStatusID\":1,\"genderID\":1,\"i_bendemographics\":{\"educationID\":1,"
//                 + "\"beneficiaryRegID\":1,\"occupationID\":1,\"healthCareWorkerID\":1,\"incomeStatusID\":1,"
//                 + "\"communityID\":1,\"preferredLangID\":1,\"districtID\":1,\"stateID\":1,"
//                 + "\"pinCode\":\"123456\",\"blockID\":1,\"districtBranchID\":1,\"createdBy\":\"testuser\","
//                 + "\"addressLine1\":\"123 Main St\"},\"benPhoneMaps\":[{\"parentBenRegID\":null,\"phoneNo\":\"9876543210\","
//                 + "\"phoneTypeID\":1,\"benRelationshipID\":1,\"deleted\":false,\"createdBy\":\"testuser\"}],"
//                 + "\"changeInSelfDetails\":true,\"changeInIdentities\":false,\"changeInOtherDetails\":false,"
//                 + "\"changeInAddress\":false,\"changeInContacts\":false,\"changeInFamilyDetails\":false}";

//         when(registerBenificiaryService.updateBenificiary(any(BeneficiaryModel.class), anyString())).thenReturn(1); // 1 for success

//         mockMvc.perform(post("/beneficiary/updateBenefciary")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(requestJson))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.statusCode").value(200))
//                 .andExpect(jsonPath("$.data").value(1)); // Assuming it returns the update count
//     }

//     @Test
//     void shouldReturnError_whenUpdateBeneficiaryFails() throws Exception {
//         String requestJson = "{\"beneficiaryRegID\":1,\"firstName\":\"UpdatedJohn\"}"; // Simplified for test

//         when(registerBenificiaryService.updateBenificiary(any(BeneficiaryModel.class), anyString())).thenReturn(0); // 0 for no update

//         mockMvc.perform(post("/beneficiary/updateBenefciary")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(requestJson))
//                 .andExpect(status().isOk()) // Still 200 OK, but data indicates failure
//                 .andExpect(jsonPath("$.statusCode").value(200))
//                 .andExpect(jsonPath("$.data").value(0));
//     }

//     // Test for updateBenefciaryDetails(@RequestBody String benificiaryRequest, HttpServletRequest httpRequest)
//     @Test
//     void shouldUpdateBeneficiaryDetails_whenValidInputProvided() throws Exception {
//         String requestJson = "{\"beneficiaryRegID\":1,\"firstName\":\"UpdatedJohn\",\"lastName\":\"Doe\","
//                 + "\"dOB\":\"2000-01-01 00:00:00\",\"ageUnits\":\"Years\",\"fatherName\":\"Sr. Doe\",\"spouseName\":\"Mrs. Doe\","
//                 + "\"govtIdentityNo\":\"123456789012\",\"govtIdentityTypeID\":1,\"emergencyRegistration\":false,"
//                 + "\"createdBy\":\"testuser\",\"titleId\":1,\"statusID\":1,\"registeredServiceID\":1,"
//                 + "\"maritalStatusID\":1,\"genderID\":1,\"i_bendemographics\":{\"educationID\":1,"
//                 + "\"beneficiaryRegID\":1,\"occupationID\":1,\"healthCareWorkerID\":1,\"incomeStatusID\":1,"
//                 + "\"communityID\":1,\"preferredLangID\":1,\"districtID\":1,\"stateID\":1,"
//                 + "\"pinCode\":\"123456\",\"blockID\":1,\"districtBranchID\":1,\"createdBy\":\"testuser\","
//                 + "\"addressLine1\":\"123 Main St\"},\"benPhoneMaps\":[{\"parentBenRegID\":null,\"phoneNo\":\"9876543210\","
//                 + "\"phoneTypeID\":1,\"benRelationshipID\":1,\"deleted\":false,\"createdBy\":\"testuser\"}],"
//                 + "\"changeInSelfDetails\":true,\"changeInIdentities\":false,\"changeInOtherDetails\":false,"
//                 + "\"changeInAddress\":false,\"changeInContacts\":false,\"changeInFamilyDetails\":false}";

//         when(registerBenificiaryService.updateBenificiary(any(BeneficiaryModel.class), anyString())).thenReturn(1);

//         mockMvc.perform(post("/beneficiary/updateBenefciaryDetails")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(requestJson))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.statusCode").value(200))
//                 .andExpect(jsonPath("$.data").value(1));
//     }

//     // Test for getBeneficiariesByPhone(@Param(value = "{\"phoneNo\":\"String\"}") String request) - Assuming POST with JSON body
//     @Test
//     void shouldReturnBeneficiariesByPhone_whenFound() throws Exception {
//         String requestJson = "{\"phoneNo\":\"9876543210\"}";
//         BeneficiaryModel mockBeneficiaryModel = new BeneficiaryModel();
//         mockBeneficiaryModel.setBeneficiaryRegID(789L);
//         mockBeneficiaryModel.setPhoneNo("9876543210");

//         // Assuming controller calls a search method like userExitsCheckWithGovIdentity or similar
//         when(iemrSearchUserService.userExitsCheckWithGovIdentity(anyString(), anyString(), anyBoolean()))
//                 .thenReturn(Collections.singletonList(mockBeneficiaryModel));

//         mockMvc.perform(post("/beneficiary/getBeneficiariesByPhone")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(requestJson))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.statusCode").value(200))
//                 .andExpect(jsonPath("$.data[0].beneficiaryRegID").value(789L));
//     }

//     @Test
//     void shouldReturnEmptyList_whenGetBeneficiariesByPhoneNotFound() throws Exception {
//         String requestJson = "{\"phoneNo\":\"1111111111\"}";

//         when(iemrSearchUserService.userExitsCheckWithGovIdentity(anyString(), anyString(), anyBoolean()))
//                 .thenReturn(Collections.emptyList());

//         mockMvc.perform(post("/beneficiary/getBeneficiariesByPhone")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(requestJson))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.statusCode").value(200))
//                 .andExpect(jsonPath("$.data").isArray())
//                 .andExpect(jsonPath("$.data").isEmpty());
//     }

//     // Test for updateBeneficiaryCommunityorEducation(@Param(value = "...") String request) - Assuming POST with JSON body
//     @Test
//     void shouldUpdateBeneficiaryCommunityOrEducation_whenValidInputProvided() throws Exception {
//         String requestJson = "{\"beneficiaryRegID\":1,\"i_bendemographics\":{\"communityID\":2,\"educationID\":3}}";

//         when(registerBenificiaryService.updateCommunityorEducation(any(BeneficiaryModel.class), anyString())).thenReturn(1);

//         mockMvc.perform(post("/beneficiary/updateBeneficiaryCommunityorEducation")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(requestJson))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.statusCode").value(200))
//                 .andExpect(jsonPath("$.data").value(1));
//     }

//     @Test
//     void shouldReturnError_whenUpdateBeneficiaryCommunityOrEducationFails() throws Exception {
//         String requestJson = "{\"beneficiaryRegID\":999,\"i_bendemographics\":{\"communityID\":2,\"educationID\":3}}";

//         when(registerBenificiaryService.updateCommunityorEducation(any(BeneficiaryModel.class), anyString())).thenReturn(0);

//         mockMvc.perform(post("/beneficiary/updateBeneficiaryCommunityorEducation")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(requestJson))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.statusCode").value(200))
//                 .andExpect(jsonPath("$.data").value(0));
//     }

//     // Test for getBeneficiaryIDs(@Param("{\"benIDRequired\":\"Integer\",\"vanID\":\"Integer\"}") String request) - Assuming POST with JSON body
//     @Test
//     void shouldReturnBeneficiaryIDs_whenGetBeneficiaryIDsCalled() throws Exception {
//         String requestJson = "{\"benIDRequired\":1,\"vanID\":101}";
        
//         BeneficiaryModel ben1 = new BeneficiaryModel(); ben1.setBeneficiaryRegID(1001L);
//         BeneficiaryModel ben2 = new BeneficiaryModel(); ben2.setBeneficiaryRegID(1002L);
        
//         // Assuming this method internally calls a search service that returns BeneficiaryModel list
//         // and then extracts IDs. Mocking one of the IEMRSearchUserService methods to provide data.
//         when(iemrSearchUserService.userExitsCheckWithHealthId_ABHAId(anyString(), anyString(), anyBoolean()))
//                 .thenReturn(Arrays.asList(ben1, ben2));

//         mockMvc.perform(post("/beneficiary/getBeneficiaryIDs")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(requestJson))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.statusCode").value(200))
//                 .andExpect(jsonPath("$.data").isArray()); // Expecting an array of beneficiaries or IDs
//     }

//     @Test
//     void shouldReturnEmptyList_whenGetBeneficiaryIDsFindsNoMatches() throws Exception {
//         String requestJson = "{\"benIDRequired\":1,\"vanID\":999}";

//         when(iemrSearchUserService.userExitsCheckWithHealthId_ABHAId(anyString(), anyString(), anyBoolean()))
//                 .thenReturn(Collections.emptyList());

//         mockMvc.perform(post("/beneficiary/getBeneficiaryIDs")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(requestJson))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.statusCode").value(200))
//                 .andExpect(jsonPath("$.data").isArray())
//                 .andExpect(jsonPath("$.data").isEmpty());
//     }
// }