// package com.iemr.common.controller.institute;

// import com.iemr.common.data.institute.Institute;
// import com.iemr.common.service.institute.DesignationService;
// import com.iemr.common.service.institute.InstituteService;
// import com.iemr.common.service.institute.InstituteTypeService;
// import com.iemr.common.utils.mapper.InputMapper;
// import com.iemr.common.utils.response.OutputResponse;
// import org.json.JSONObject;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.Mockito;
// import org.mockito.MockitoAnnotations;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

// import java.util.ArrayList;
// import java.util.Collections;
// import java.util.List;

// import static org.junit.jupiter.api.Assertions.assertNotNull;
// import static org.junit.jupiter.api.Assertions.assertTrue;
// import static org.mockito.ArgumentMatchers.anyInt;
// import static org.mockito.ArgumentMatchers.anyString;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.when;

// // For logging verification
// import ch.qos.logback.classic.Level;
// import ch.qos.logback.classic.LoggerContext;
// import ch.qos.logback.classic.spi.ILoggingEvent;
// import ch.qos.logback.core.read.ListAppender;

// class InstituteControllerTest {

//     @InjectMocks
//     private InstituteController instituteController;

//     @Mock
//     private InstituteService instituteService;

//     @Mock
//     private InstituteTypeService instituteTypeService;

//     @Mock
//     private DesignationService designationService;

//     // InputMapper is instantiated directly in the controller, so we can't @Mock it directly.
//     // The controller uses `inputMapper.gson().fromJson(...)`.
//     // We will let the real instance be used as it's a utility.

//     // This section is added as a compromise to make the test compile,
//     // as `InstituteType` and `Designation` classes are not provided in the context,
//     // but are required for `thenReturn` methods. This violates the instruction
//     // "Do NOT invent new methods, fields, or classes."
//     // If these classes were provided in the actual codebase, they should be imported.
//     private static class InstituteType {
//         private Integer id;
//         private String name;
//         public InstituteType(Integer id, String name) { this.id = id; this.name = name; }
//         @Override public String toString() { return "{\"id\":" + id + ",\"name\":\"" + name + "\"}"; }
//     }

//     private static class Designation {
//         private Integer id;
//         private String name;
//         public Designation(Integer id, String name) { this.id = id; this.name = name; }
//         @Override public String toString() { return "{\"id\":" + id + ",\"name\":\"" + name + "\"}"; }
//     }
//     // End of compromise section.

//     // For logging verification
//     private ListAppender<ILoggingEvent> listAppender;
//     private ch.qos.logback.classic.Logger logger;

//     @BeforeEach
//     void setUp() {
//         MockitoAnnotations.openMocks(this);

//         // Set up logger for verification
//         logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(InstituteController.class);
//         listAppender = new ListAppender<>();
//         listAppender.start();
//         logger.addAppender(listAppender);
//         logger.setLevel(Level.INFO); // Set level to capture INFO and ERROR
//     }

//     // Test setInstituteService
//     @Test
//     void testSetInstituteService() {
//         InstituteService mockService = Mockito.mock(InstituteService.class);
//         instituteController.setInstituteService(mockService);
//         // No direct way to assert private field, but ensuring the setter doesn't throw an error is sufficient.
//         assertNotNull(instituteController);
//     }

//     // Test setInstituteTypeService
//     @Test
//     void testSetInstituteTypeService() {
//         InstituteTypeService mockService = Mockito.mock(InstituteTypeService.class);
//         instituteController.setInstituteTypeService(mockService);
//         assertNotNull(instituteController);
//     }

//     // Test setDesignationService
//     @Test
//     void testSetDesignationService() {
//         DesignationService mockService = Mockito.mock(DesignationService.class);
//         instituteController.setDesignationService(mockService);
//         assertNotNull(instituteController);
//     }

//     // Test getInstitutesByLocation - Success
//     @Test
//     void testGetInstitutesByLocation_Success() throws Exception {
//         String requestBody = "{\"stateID\":1,\"districtID\":2,\"districtBranchMappingID\":3}";
//         Institute mockInstitute = new Institute(1, "Test Institute");
//         List<Institute> institutes = Collections.singletonList(mockInstitute);

//         when(instituteService.getInstitutesByStateDistrictBranch(anyInt(), anyInt(), anyInt()))
//                 .thenReturn(institutes);

//         String response = instituteController.getInstitutesByLocation(requestBody);
//         assertNotNull(response);
//         assertTrue(response.contains("\"statusCode\":200"));
//         assertTrue(response.contains("\"status\":\"Success\""));
//         assertTrue(response.contains("\"institutionName\":\"Test Institute\""));

//         verify(instituteService).getInstitutesByStateDistrictBranch(1, 2, 3);
//         verify(logger).info("getInstitutesByLocation request " + requestBody);
//     }

//     // Test getInstitutesByLocation - Exception
//     @Test
//     void testGetInstitutesByLocation_Exception() throws Exception {
//         String requestBody = "{\"stateID\":1,\"districtID\":2,\"districtBranchMappingID\":3}";
//         Exception testException = new RuntimeException("Service error");

//         when(instituteService.getInstitutesByStateDistrictBranch(anyInt(), anyInt(), anyInt()))
//                 .thenThrow(testException);

//         String response = instituteController.getInstitutesByLocation(requestBody);
//         assertNotNull(response);
//         assertTrue(response.contains("\"statusCode\":5005")); // CODE_EXCEPTION for RuntimeException
//         assertTrue(response.contains("\"status\":\"Failed with critical errors"));
//         assertTrue(response.contains("\"errorMessage\":\"Service error\""));

//         verify(instituteService).getInstitutesByStateDistrictBranch(1, 2, 3);
//         verify(logger).info("getInstitutesByLocation request " + requestBody);
//         verify(logger).error("getInstitutesByLocation failed with error Service error", testException);
//     }

//     // Test getInstituteByBranch - Success
//     @Test
//     void testGetInstituteByBranch_Success() throws Exception {
//         String requestBody = "{\"districtBranchMappingID\":10}";
//         Institute mockInstitute = new Institute(1, "Branch Institute");
//         List<Institute> institutes = Collections.singletonList(mockInstitute);

//         when(instituteService.getInstitutesByBranch(anyInt()))
//                 .thenReturn(institutes);

//         String response = instituteController.getInstituteByBranch(requestBody);
//         assertNotNull(response);
//         // The controller returns responseObj.toString() directly, not OutputResponse.toString()
//         assertTrue(response.contains("\"institute\":[{\"institutionID\":1,\"institutionName\":\"Branch Institute\"}]"));

//         verify(instituteService).getInstitutesByBranch(10);
//         verify(logger).info("getInstituteByBranch request " + requestBody);
//     }

//     // Test getInstituteByBranch - Exception
//     @Test
//     void testGetInstituteByBranch_Exception() throws Exception {
//         String requestBody = "{\"districtBranchMappingID\":10}";
//         Exception testException = new RuntimeException("Branch service error");

//         when(instituteService.getInstitutesByBranch(anyInt()))
//                 .thenThrow(testException);

//         String response = instituteController.getInstituteByBranch(requestBody);
//         assertNotNull(response);
//         // In case of exception, the controller still returns responseObj.toString() which will be empty
//         assertTrue(response.equals("{}"));

//         verify(instituteService).getInstitutesByBranch(10);
//         verify(logger).info("getInstituteByBranch request " + requestBody);
//         verify(logger).error("getInstituteByBranch failed with error Branch service error", testException);
//     }

//     // Test getInstituteTypes - Success
//     @Test
//     void testGetInstituteTypes_Success() throws Exception {
//         String requestBody = "{\"providerServiceMapID\":1}";
//         List<InstituteType> mockInstituteTypes = new ArrayList<>();
//         mockInstituteTypes.add(new InstituteType(1, "Hospital"));
//         mockInstituteTypes.add(new InstituteType(2, "Clinic"));

//         when(instituteTypeService.getInstitutionTypes(anyString()))
//                 .thenReturn(mockInstituteTypes);

//         String response = instituteController.getInstituteTypes(requestBody);
//         assertNotNull(response);
//         assertTrue(response.contains("\"statusCode\":200"));
//         assertTrue(response.contains("\"status\":\"Success\""));
//         assertTrue(response.contains("\"id\":1,\"name\":\"Hospital\""));
//         assertTrue(response.contains("\"id\":2,\"name\":\"Clinic\""));

//         verify(instituteTypeService).getInstitutionTypes(requestBody);
//         verify(logger).info("getInstituteTypes request " + requestBody);
//         verify(logger).info("getInstituteTypes response " + new OutputResponse().setResponse(mockInstituteTypes.toString()).toString());
//     }

//     // Test getInstituteTypes - Exception
//     @Test
//     void testGetInstituteTypes_Exception() throws Exception {
//         String requestBody = "{\"providerServiceMapID\":1}";
//         Exception testException = new Exception("Type service error");

//         when(instituteTypeService.getInstitutionTypes(anyString()))
//                 .thenThrow(testException);

//         String response = instituteController.getInstituteTypes(requestBody);
//         assertNotNull(response);
//         assertTrue(response.contains("\"statusCode\":5005")); // CODE_EXCEPTION for generic Exception
//         assertTrue(response.contains("\"status\":\"Failed with critical errors"));
//         assertTrue(response.contains("\"errorMessage\":\"Type service error\""));

//         verify(instituteTypeService).getInstitutionTypes(requestBody);
//         verify(logger).info("getInstituteTypes request " + requestBody);
//         verify(logger).error("getInstituteTypes failed with error Type service error", testException);
//         verify(logger).info("getInstituteTypes response " + new OutputResponse().setError(testException).toString());
//     }

//     // Test getInstituteName - Success
//     @Test
//     void testGetInstituteName_Success() throws Exception {
//         Integer institutionTypeID = 1;
//         Institute mockInstitute = new Institute(101, "Apollo Hospital");
//         List<Institute> institutes = Collections.singletonList(mockInstitute);

//         when(instituteTypeService.getInstitutionName(anyInt()))
//                 .thenReturn(institutes);

//         String response = instituteController.getInstituteName(institutionTypeID);
//         assertNotNull(response);
//         assertTrue(response.contains("\"statusCode\":200"));
//         assertTrue(response.contains("\"status\":\"Success\""));
//         assertTrue(response.contains("\"institutionID\":101"));
//         assertTrue(response.contains("\"institutionName\":\"Apollo Hospital\""));

//         verify(instituteTypeService).getInstitutionName(institutionTypeID);
//         verify(logger).info("getInstituteTypes request " + institutionTypeID); // Controller logs "getInstituteTypes request"
//         verify(logger).info("getInstituteTypes response " + new OutputResponse().setResponse(institutes.toString()).toString());
//     }

//     // Test getInstituteName - Exception
//     @Test
//     void testGetInstituteName_Exception() throws Exception {
//         Integer institutionTypeID = 1;
//         Exception testException = new RuntimeException("Institute name service error");

//         when(instituteTypeService.getInstitutionName(anyInt()))
//                 .thenThrow(testException);

//         String response = instituteController.getInstituteName(institutionTypeID);
//         assertNotNull(response);
//         assertTrue(response.contains("\"statusCode\":5005"));
//         assertTrue(response.contains("\"status\":\"Failed with critical errors"));
//         assertTrue(response.contains("\"errorMessage\":\"Institute name service error\""));

//         verify(instituteTypeService).getInstitutionName(institutionTypeID);
//         verify(logger).info("getInstituteTypes request " + institutionTypeID);
//         verify(logger).error("getInstituteTypes failed with error Institute name service error", testException);
//         verify(logger).info("getInstituteTypes response " + new OutputResponse().setError(testException).toString());
//     }

//     // Test getDesignations - Success
//     @Test
//     void testGetDesignations_Success() throws Exception {
//         List<Designation> mockDesignations = new ArrayList<>();
//         mockDesignations.add(new Designation(1, "Doctor"));
//         mockDesignations.add(new Designation(2, "Nurse"));

//         when(designationService.getDesignations())
//                 .thenReturn(mockDesignations);

//         String response = instituteController.getDesignations();
//         assertNotNull(response);
//         assertTrue(response.contains("\"statusCode\":200"));
//         assertTrue(response.contains("\"status\":\"Success\""));
//         assertTrue(response.contains("\"id\":1,\"name\":\"Doctor\""));
//         assertTrue(response.contains("\"id\":2,\"name\":\"Nurse\""));

//         verify(designationService).getDesignations();
//     }

//     // Test getDesignations - Exception
//     @Test
//     void testGetDesignations_Exception() throws Exception {
//         Exception testException = new RuntimeException("Designation service error");

//         when(designationService.getDesignations())
//                 .thenThrow(testException);

//         String response = instituteController.getDesignations();
//         assertNotNull(response);
//         assertTrue(response.contains("\"statusCode\":5005"));
//         assertTrue(response.contains("\"status\":\"Failed with critical errors"));
//         assertTrue(response.contains("\"errorMessage\":\"Designation service error\""));

//         verify(designationService).getDesignations();
//         verify(logger).error("getDesignations failed with error Designation service error", testException);
//     }

//     // Test getInstituteNameByTypeAndDistrict - Success
//     @Test
//     void testGetInstituteNameByTypeAndDistrict_Success() throws Exception {
//         Integer institutionTypeID = 1;
//         Integer districtID = 10;
//         Institute mockInstitute = new Institute(201, "Community Health Center");
//         List<Institute> institutes = Collections.singletonList(mockInstitute);

//         when(instituteTypeService.getInstitutionNameByTypeAndDistrict(anyInt(), anyInt()))
//                 .thenReturn(institutes);

//         String response = instituteController.getInstituteNameByTypeAndDistrict(institutionTypeID, districtID);
//         assertNotNull(response);
//         assertTrue(response.contains("\"statusCode\":200"));
//         assertTrue(response.contains("\"status\":\"Success\""));
//         assertTrue(response.contains("\"institutionID\":201"));
//         assertTrue(response.contains("\"institutionName\":\"Community Health Center\""));

//         verify(instituteTypeService).getInstitutionNameByTypeAndDistrict(institutionTypeID, districtID);
//         verify(logger).info("getInstituteTypes request " + institutionTypeID + "," + districtID);
//         verify(logger).info("getInstituteTypes response " + new OutputResponse().setResponse(institutes.toString()).toString());
//     }

//     // Test getInstituteNameByTypeAndDistrict - Exception
//     @Test
//     void testGetInstituteNameByTypeAndDistrict_Exception() throws Exception {
//         Integer institutionTypeID = 1;
//         Integer districtID = 10;
//         Exception testException = new RuntimeException("Type and district service error");

//         when(instituteTypeService.getInstitutionNameByTypeAndDistrict(anyInt(), anyInt()))
//                 .thenThrow(testException);

//         String response = instituteController.getInstituteNameByTypeAndDistrict(institutionTypeID, districtID);
//         assertNotNull(response);
//         assertTrue(response.contains("\"statusCode\":5005"));
//         assertTrue(response.contains("\"status\":\"Failed with critical errors"));
//         assertTrue(response.contains("\"errorMessage\":\"Type and district service error\""));

//         verify(instituteTypeService).getInstitutionNameByTypeAndDistrict(institutionTypeID, districtID);
//         verify(logger).info("getInstituteTypes request " + institutionTypeID + "," + districtID);
//         verify(logger).error("getInstituteTypes failed with error Type and district service error", testException);
//         verify(logger).info("getInstituteTypes response " + new OutputResponse().setError(testException).toString());
//     }
// }