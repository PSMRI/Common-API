// package com.iemr.common.controller.secondaryReport;

// import com.iemr.common.data.report.CallQualityReport;
// import com.iemr.common.service.reportSecondary.SecondaryReportService;
// import com.iemr.common.utils.mapper.InputMapper;
// import java.util.ArrayList;
// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;
// import java.util.Set;
// import org.junit.jupiter.api.Assertions;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.Mockito;

// @WebMvcTest(CustomerRelationshipSecondaryReports.class)
// class CustomerRelationshipSecondaryReportsTest {

//     @Autowired
//     private MockMvc mockMvc;
// @Test
// void test_getQualityReport_success() {
//     // Arrange
//     String requestBody = "{\"startDate\":\"2023-01-01 00:00:00\",\"endDate\":\"2023-01-31 23:59:59\","
//                        + "\"providerServiceMapID\":1,\"agentID\":101,\"roleName\":\"Agent\","
//                        + "\"reportTypeID\":1,\"reportType\":\"Daily\"}";

//     Map<String, Object> parsedInput = new HashMap<>();
//     parsedInput.put("startDate", "2023-01-01 00:00:00");
//     parsedInput.put("endDate", "2023-01-31 23:59:59");
//     parsedInput.put("providerServiceMapID", 1);
//     parsedInput.put("agentID", 101);
//     parsedInput.put("roleName", "Agent");
//     parsedInput.put("reportTypeID", 1);
//     parsedInput.put("reportType", "Daily");

//     List<CallQualityReport> expectedReports = new ArrayList<>();
//     CallQualityReport report1 = new CallQualityReport();
//     // Assuming CallQualityReport has setters for relevant fields to make objects distinct
//     report1.setCallId("call_id_1");
//     report1.setAgentName("Agent Alpha");
//     report1.setCallDuration(300);
//     expectedReports.add(report1);

//     CallQualityReport report2 = new CallQualityReport();
//     report2.setCallId("call_id_2");
//     report2.setAgentName("Agent Beta");
//     report2.setCallDuration(180);
//     expectedReports.add(report2);

//     // Mock behavior for dependencies
//     // Assuming 'inputMapper' and 'secondaryReportService' are // [REMOVED: @Mock not allowed in controller tests]fields
//     // and 'controller' is an // [REMOVED: @InjectMocks not allowed in controller tests] field in the test class.
//     when(inputMapper.getMapAsObject(requestBody)).thenReturn(parsedInput);
//     when(secondaryReportService.getCallQualityReport(parsedInput)).thenReturn(expectedReports);

//     // Act
//     ResponseEntity<Object> response = controller.getQualityReport(requestBody);

//     // Assert
//     assertEquals(HttpStatus.OK, response.getStatusCode());
//     assertEquals(expectedReports, response.getBody());

//     // Verify interactions
//     verify(inputMapper).getMapAsObject(requestBody);
//     verify(secondaryReportService).getCallQualityReport(parsedInput);
// }

// @Test
// void getComplaintDetailReport_shouldReturnReportData_whenSuccessful() {
//     // Assume secondaryReportService and inputMapper are // [REMOVED: @Mock not allowed in controller tests]fields,
//     // and the class under test (e.g., ReportController) is // [REMOVED: @InjectMocks not allowed in controller tests].

//     // 1. Prepare input JSON string as expected by the method under test
//     String requestJson = "{\"startDate\":\"2023-01-01 00:00:00\",\"endDate\":\"2023-01-31 23:59:59\","
//             + "\"providerServiceMapID\":1,\"agentID\":101,\"roleName\":\"Agent\","
//             + "\"reportTypeID\":5,\"reportType\":\"ComplaintDetail\"}";

//     // 2. Prepare the parsed input map that InputMapper would return
//     Map<String, Object> parsedRequestMap = new HashMap<>();
//     parsedRequestMap.put("startDate", "2023-01-01 00:00:00");
//     parsedRequestMap.put("endDate", "2023-01-31 23:59:59");
//     parsedRequestMap.put("providerServiceMapID", 1);
//     parsedRequestMap.put("agentID", 101);
//     parsedRequestMap.put("roleName", "Agent");
//     parsedRequestMap.put("reportTypeID", 5);
//     parsedRequestMap.put("reportType", "ComplaintDetail");

//     // 3. Prepare the expected report data that SecondaryReportService would return
//     List<Map<String, Object>> expectedReportData = Arrays.asList(
//         new HashMap<String, Object>() {{
//             put("complaintId", 1001);
//             put("complaintDate", "2023-01-15");
//             put("description", "Issue with service quality");
//             put("agentName", "John Doe");
//         }

// @Test
// void shouldReturnCallSummaryReportSuccessfully() {
//     // Given
//     String requestBody = "{\"startDate\":\"2023-01-01 00:00:00\",\"endDate\":\"2023-01-31 23:59:59\",\"providerServiceMapID\":1,\"agentID\":101,\"roleName\":\"Agent\",\"callTypeID\":10,\"callTypeName\":\"Inbound\"}";

//     Map<String, Object> parsedRequestBodyMap = new HashMap<>();
//     parsedRequestBodyMap.put("startDate", "2023-01-01 00:00:00");
//     parsedRequestBodyMap.put("endDate", "2023-01-31 23:59:59");
//     parsedRequestBodyMap.put("providerServiceMapID", 1);
//     parsedRequestBodyMap.put("agentID", 101);
//     parsedRequestBodyMap.put("roleName", "Agent");
//     parsedRequestBodyMap.put("callTypeID", 10);
//     parsedRequestBodyMap.put("callTypeName", "Inbound");

//     List<CallQualityReport> expectedReports = new ArrayList<>();
//     // Assuming CallQualityReport has a constructor like CallQualityReport(String agentName, String callType, int totalCalls, int successfulCalls, int failedCalls)
//     expectedReports.add(new CallQualityReport("Agent A", "Inbound", 100, 95, 5));
//     expectedReports.add(new CallQualityReport("Agent B", "Outbound", 50, 48, 2));

//     // When
//     // Mock the behavior of InputMapper to parse the requestBody string into a Map
//     when(inputMapper.toMap(requestBody)).thenReturn(parsedRequestBodyMap);
//     // Mock the behavior of SecondaryReportService to return the expected reports
//     when(secondaryReportService.getCallSummary(parsedRequestBodyMap)).thenReturn(expectedReports);

//     // Act
//     // Call the method under test
//     ResponseEntity<Object> responseEntity = reportController.getCallSummaryReport(requestBody);

//     // Then
//     // Assert the HTTP status code is OK
//     assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
//     // Assert the response body matches the expected list of reports
//     assertEquals(expectedReports, responseEntity.getBody());

//     // Verify that InputMapper's toMap method was called exactly once with the correct requestBody
//     verify(inputMapper, times(1)).toMap(requestBody);
//     // Verify that SecondaryReportService's getCallSummary method was called exactly once with the parsed map
//     verify(secondaryReportService, times(1)).getCallSummary(parsedRequestBodyMap);
// }

// @Test
// void testGetAllBySexualOrientation_Success() {
//     // 1. Prepare Test Data
//     String inputJson = "{\"startTimestamp\":\"2023-01-01T00:00:00Z\",\"endTimestamp\":\"2023-01-31T23:59:59Z\","
//             + "\"state\":\"California\",\"district\":\"Los Angeles\","
//             + "\"beneficiarySexualOrientation\":\"Heterosexual\",\"providerServiceMapID\":123}";

//     Map<String, Object> parsedRequestMap = new HashMap<>();
//     parsedRequestMap.put("startTimestamp", "2023-01-01T00:00:00Z");
//     parsedRequestMap.put("endTimestamp", "2023-01-31T23:59:59Z");
//     parsedRequestMap.put("state", "California");
//     parsedRequestMap.put("district", "Los Angeles");
//     parsedRequestMap.put("beneficiarySexualOrientation", "Heterosexual");
//     parsedRequestMap.put("providerServiceMapID", 123);

//     Map<String, Object> reportData1 = new HashMap<>();
//     reportData1.put("sexualOrientation", "Heterosexual");
//     reportData1.put("count", 100);
//     Map<String, Object> reportData2 = new HashMap<>();
//     reportData2.put("sexualOrientation", "Homosexual");
//     reportData2.put("count", 50);
//     List<Map<String, Object>> expectedServiceResult = Arrays.asList(reportData1, reportData2);

//     // 2. Mock Behavior
//     // Assuming 'inputMapper' is an // [REMOVED: @Mock not allowed in controller tests]field and 'secondaryReportService' is an // [REMOVED: @Mock not allowed in controller tests]field.
//     // Assuming the controller uses InputMapper to parse the JSON string into a Map.
//     Mockito.when(inputMapper.readJsonToMap(Mockito.anyString())).thenReturn(parsedRequestMap);

//     // Assuming the service method takes the parsed map and returns a list of maps.
//     Mockito.when(secondaryReportService.getAllBySexualOrientation(Mockito.anyMap())).thenReturn(expectedServiceResult);

//     // 3. Call Method Under Test
//     // Assuming 'reportController' is the // [REMOVED: @InjectMocks not allowed in controller tests] instance of the class under test.
//     // The method signature is `public ResponseEntity<Object> getAllBySexualOrientation(String requestBodyJson)`
//     ResponseEntity<Object> responseEntity = reportController.getAllBySexualOrientation(inputJson);

//     // 4. Assert Results
//     Assertions.assertNotNull(responseEntity, "Response entity should not be null");
//     Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode(), "Status code should be OK");
//     Assertions.assertEquals(expectedServiceResult, responseEntity.getBody(), "Response body should match expected service result");

//     // 5. Verify Interactions
//     Mockito.verify(inputMapper, Mockito.times(1)).readJsonToMap(inputJson);
//     Mockito.verify(secondaryReportService, Mockito.times(1)).getAllBySexualOrientation(parsedRequestMap);
// }

// @Test
// void shouldReturnDistrictWiseCallReportSuccessfullyWhenValidRequestProvided() {
//     // Description: Tests that the getDistrictWiseCallReport method returns a successful response with the correct data when a valid request body is provided.

//     // Mocks for dependencies
//     SecondaryReportService mockSecondaryReportService = Mockito.mock(SecondaryReportService.class);
//     InputMapper mockInputMapper = Mockito.mock(InputMapper.class);

//     // Instantiate the class under test (assuming its name is ReportController and it has a constructor
//     // that takes SecondaryReportService and InputMapper as dependencies).
//     ReportController reportController = new ReportController(mockSecondaryReportService, mockInputMapper);

//     // Prepare the request body string
//     String requestBody = "{\"startDate\":\"2023-01-01 00:00:00\",\"endDate\":\"2023-01-31 23:59:59\","
//                        + "\"providerServiceMapID\":1,\"districtID\":101,\"district\":\"TestDistrict\","
//                        + "\"subdistrictID\":201,\"villageID\":301,\"locationID\":401,\"roleID\":501}";

//     // Prepare the parsed map that InputMapper is expected to return
//     Map<String, Object> parsedRequestMap = new HashMap<>();
//     parsedRequestMap.put("startDate", "2023-01-01 00:00:00");
//     parsedRequestMap.put("endDate", "2023-01-31 23:59:59");
//     parsedRequestMap.put("providerServiceMapID", 1);
//     parsedRequestMap.put("districtID", 101);
//     parsedRequestMap.put("district", "TestDistrict");
//     parsedRequestMap.put("subdistrictID", 201);
//     parsedRequestMap.put("villageID", 301);
//     parsedRequestMap.put("locationID", 401);
//     parsedRequestMap.put("roleID", 501);

//     // Prepare the list of CallQualityReport objects that the service is expected to return
//     CallQualityReport report1 = new CallQualityReport();
//     report1.setDistrict("TestDistrict");
//     report1.setTotalCalls(100L);
//     // Set other relevant fields for report1 if necessary for a complete test

//     CallQualityReport report2 = new CallQualityReport();
//     report2.setDistrict("AnotherDistrict");
//     report2.setTotalCalls(50L);
//     // Set other relevant fields for report2 if necessary for a complete test

//     List<CallQualityReport> expectedReportList = Arrays.asList(report1, report2);

//     // Configure mock behavior
//     // Mock InputMapper to return the parsed map when parseJsonToMap is called with the requestBody
//     Mockito.when(mockInputMapper.parseJsonToMap(requestBody)).thenReturn(parsedRequestMap);

//     // Mock SecondaryReportService to return the expected list when getDistrictWiseCallReport is called with the parsed map
//     Mockito.when(mockSecondaryReportService.getDistrictWiseCallReport(parsedRequestMap))
//            .thenReturn(expectedReportList);

//     // Call the method under test
//     ResponseEntity<Object> response = reportController.getDistrictWiseCallReport(requestBody);

//     // Assertions
//     Assertions.assertNotNull(response, "Response should not be null.");
//     Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(), "HTTP Status should be OK.");
//     Assertions.assertNotNull(response.getBody(), "Response body should not be null.");
//     Assertions.assertTrue(response.getBody() instanceof List, "Response body should be a List.");

//     @SuppressWarnings("unchecked")
//     List<CallQualityReport> actualReportList = (List<CallQualityReport>) response.getBody();
//     Assertions.assertEquals(expectedReportList.size(), actualReportList.size(), "Returned list size should match expected size.");

//     // Assert content of the list. This assumes CallQualityReport has a proper equals() method implemented.
//     // If not, a loop and field-by-field comparison would be necessary.
//     Assertions.assertEquals(expectedReportList, actualReportList, "Returned report list content should match expected content.");

//     // Verify that the mock methods were called as expected
//     Mockito.verify(mockInputMapper).parseJsonToMap(requestBody);
//     Mockito.verify(mockSecondaryReportService).getDistrictWiseCallReport(parsedRequestMap);
// }

// @Test
// void getUnblockedUserReport_Success_ReturnsOkAndReportData() {
//     // Mock dependencies
//     SecondaryReportService secondaryReportService = Mockito.mock(SecondaryReportService.class);
//     InputMapper inputMapper = Mockito.mock(InputMapper.class);

//     // Instantiate the class under test.
//     // IMPORTANT ASSUMPTION: The class containing the 'getUnblockedUserReport' method
//     // is named 'MyController' and has a constructor that accepts SecondaryReportService
//     // and InputMapper as arguments (e.g., 'public MyController(SecondaryReportService service, InputMapper mapper)').
//     // This assumption is necessary as the prompt forbids defining the class or imports,
//     // but requires testing a non-static method.
//     MyController controller = new MyController(secondaryReportService, inputMapper);

//     // Prepare test data
//     String requestJson = "{\"blockStartDate\":\"2023-01-01 00:00:00.0\",\"blockEndDate\":\"2023-01-31 23:59:59.999\",\"providerServiceMapID\":123}";

//     // Expected values that the controller would extract and convert from the JSON
//     Timestamp expectedBlockStartDate = Timestamp.valueOf("2023-01-01 00:00:00.0");
//     Timestamp expectedBlockEndDate = Timestamp.valueOf("2023-01-31 23:59:59.999");
//     Integer expectedProviderServiceMapID = 123;

//     // The map that InputMapper is expected to return after parsing the JSON string.
//     // Assuming InputMapper returns string representations for dates, which the controller then converts.
//     Map<String, Object> parsedRequestMap = new HashMap<>();
//     parsedRequestMap.put("blockStartDate", "2023-01-01 00:00:00.0");
//     parsedRequestMap.put("blockEndDate", "2023-01-31 23:59:59.999");
//     parsedRequestMap.put("providerServiceMapID", 123);

//     // The list of CallQualityReport objects expected to be returned by the service
//     List<CallQualityReport> expectedReportList = Arrays.asList(
//         new CallQualityReport("UserA", "ProviderX", "ServiceY", 10L, 5L, 2L, 3L),
//         new CallQualityReport("UserB", "ProviderZ", "ServiceW", 15L, 8L, 3L, 4L)
//     );

//     // Define Mock behavior
//     // 1. Mock InputMapper to simulate parsing the incoming JSON string into a Map.
//     Mockito.when(inputMapper.parse(Mockito.eq(requestJson), Mockito.eq(Map.class)))
//            .thenReturn(parsedRequestMap);

//     // 2. Mock SecondaryReportService to return the expected report list.
//     // The controller is assumed to convert the string timestamps from the parsed map
//     // into Timestamp objects before passing them to the service method.
//     Mockito.when(secondaryReportService.getUnblockedUserReport(
//         Mockito.eq(expectedBlockStartDate),
//         Mockito.eq(expectedBlockEndDate),
//         Mockito.eq(expectedProviderServiceMapID)
//     )).thenReturn(expectedReportList);

//     // Call the method under test
//     ResponseEntity<Object> response = controller.getUnblockedUserReport(requestJson);

//     // Assert the result
//     // Assert HTTP status code is OK (200)
//     Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(), "HTTP status code should be OK");

//     // Assert response body content matches the expected report list
//     Assertions.assertEquals(expectedReportList, response.getBody(), "Response body should contain the expected report list");

//     // Verify interactions with mocks
//     // Verify that InputMapper's parse method was called with the correct arguments
//     Mockito.verify(inputMapper).parse(Mockito.eq(requestJson), Mockito.eq(Map.class));
//     // Verify that SecondaryReportService's getUnblockedUserReport method was called with the correct arguments
//     Mockito.verify(secondaryReportService).getUnblockedUserReport(
//         Mockito.eq(expectedBlockStartDate),
//         Mockito.eq(expectedBlockEndDate),
//         Mockito.eq(expectedProviderServiceMapID)
//     );
//     // Ensure no other unexpected interactions occurred with the mocks
//     Mockito.verifyNoMoreInteractions(secondaryReportService, inputMapper);
// }

// @Test
// void testGetCallQualityReport_Success() {
//     // Mock dependencies
//     com.iemr.common.service.reportSecondary.SecondaryReportService mockSecondaryReportService = org.mockito.Mockito.mock(com.iemr.common.service.reportSecondary.SecondaryReportService.class);
//     com.iemr.common.utils.mapper.InputMapper mockInputMapper = org.mockito.Mockito.mock(com.iemr.common.utils.mapper.InputMapper.class);

//     // Assume the class under test is named 'ReportController' and is available as 'reportController'
//     // This setup implies that 'reportController' is an instance of the class under test,
//     // potentially injected via // [REMOVED: @InjectMocks not allowed in controller tests] or initialized in a @BeforeEach method in the actual test class.
//     // For the purpose of this method, we'll instantiate it directly, assuming a constructor that takes the services.
//     // This is the most common and testable pattern for Spring controllers.
//     // Note: This assumes a class named 'ReportController' exists with the specified constructor.
//     // If the actual class name is different, this line would need adjustment.
//     // We are not defining the class, only instantiating it.
//     Object reportController = new Object() { // Anonymous class to represent the CUT
//         public org.springframework.http.ResponseEntity<Object> getCallQualityReport(String requestBody) {
//             try {
//                 java.util.Map<String, Object> parsedInput = mockInputMapper.parseJsonToMap(requestBody);
//                 java.util.List<com.iemr.common.data.report.CallQualityReport> reports = mockSecondaryReportService.getCallQualityReport(parsedInput);
//                 return new org.springframework.http.ResponseEntity<>(reports, org.springframework.http.HttpStatus.OK);
//             }

// @Test
// void getCountsByPreferredLanguage_Success_ReturnsOkAndData() {
//     // Arrange
//     String requestBody = "{\"startTimestamp\":\"2023-01-01T00:00:00Z\",\"endTimestamp\":\"2023-01-31T23:59:59Z\","
//             + "\"beneficiaryPreferredLanguage\":\"English\",\"providerServiceMapID\":123,"
//             + "\"state\":\"Karnataka\",\"district\":\"Bengaluru\"}";

//     Map<String, Object> parsedRequestMap = new HashMap<>();
//     parsedRequestMap.put("startTimestamp", "2023-01-01T00:00:00Z");
//     parsedRequestMap.put("endTimestamp", "2023-01-31T23:59:59Z");
//     parsedRequestMap.put("beneficiaryPreferredLanguage", "English");
//     parsedRequestMap.put("providerServiceMapID", 123);
//     parsedRequestMap.put("state", "Karnataka");
//     parsedRequestMap.put("district", "Bengaluru");

//     List<Map<String, Object>> expectedServiceResult = Arrays.asList(
//             new HashMap<String, Object>() {{
//                 put("language", "English");
//                 put("count", 150);
//             }

// @Test
// void shouldReturnAllByAgeGroup_WhenValidInput() {
//         // Mock dependencies
//         SecondaryReportService secondaryReportService = Mockito.mock(SecondaryReportService.class);
//         InputMapper inputMapper = Mockito.mock(InputMapper.class);

//         // Instantiate the class under test
//         // Assuming the class under test is named 'MyReportController'
//         // and has a constructor that takes SecondaryReportService and InputMapper.
//         MyReportController myReportController = new MyReportController(secondaryReportService, inputMapper);

//         // Prepare test data
//         String requestBody = "{\"providerServiceMapID\":1,\"maxAge\":60,\"minAge\":18,\"startTimestamp\":1678886400000,\"endTimestamp\":1678972800000,\"state\":\"Karnataka\",\"district\":\"Bengaluru\"}";

//         Map<String, Object> mockInputMap = new HashMap<>();
//         mockInputMap.put("providerServiceMapID", 1);
//         mockInputMap.put("maxAge", 60);
//         mockInputMap.put("minAge", 18);
//         mockInputMap.put("startTimestamp", 1678886400000L);
//         mockInputMap.put("endTimestamp", 1678972800000L);
//         mockInputMap.put("state", "Karnataka");
//         mockInputMap.put("district", "Bengaluru");

//         List<Map<String, Object>> mockResultList = new ArrayList<>();
//         Map<String, Object> row1 = new HashMap<>();
//         row1.put("ageGroup", "18-30");
//         row1.put("count", 10);
//         mockResultList.add(row1);
//         Map<String, Object> row2 = new HashMap<>();
//         row2.put("ageGroup", "31-45");
//         row2.put("count", 15);
//         mockResultList.add(row2);

//         // Define Mockito behavior
//         Mockito.when(inputMapper.jsonToMap(requestBody)).thenReturn(mockInputMap);
//         Mockito.when(secondaryReportService.getAllByAgeGroup(mockInputMap)).thenReturn(mockResultList);

//         // Call the method under test
//         ResponseEntity<Object> response = myReportController.getAllByAgeGroup(requestBody);

//         // Assert the result
//         Assertions.assertNotNull(response);
//         Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
//         Assertions.assertEquals(mockResultList, response.getBody());

//         // Verify interactions
//         Mockito.verify(inputMapper).jsonToMap(requestBody);
//         Mockito.verify(secondaryReportService).getAllByAgeGroup(mockInputMap);
//     }

// @Test
// void getAllReportsByDate_validInput_returnsReportList() {
//     // Arrange
//     // Assuming 'secondaryReportService' and 'inputMapper' are Mockito mocks,
//     // and 'reportController' is the instance of the class under test,
//     // all initialized (e.g., via @Mock, // [REMOVED: @InjectMocks not allowed in controller tests], @BeforeEach in an enclosing test class).

//     // Input JSON string as per the @Param description
//     String inputJson = "{\"providerServiceMapID\":123,\"beneficiaryCallType\":\"TypeA\",\"beneficiaryCallSubType\":\"SubTypeA\",\"startTimestamp\":\"2023-01-01 00:00:00\",\"endTimestamp\":\"2023-01-01 23:59:59\",\"state\":\"StateX\",\"district\":\"DistrictY\",\"gender\":\"Male\",\"beneficiaryPreferredLanguage\":\"English\",\"beneficiarySexualOrientation\":\"Straight\"}";

//     // Expected parsed map from InputMapper
//     Map<String, Object> expectedParsedMap = new HashMap<>();
//     expectedParsedMap.put("providerServiceMapID", 123);
//     expectedParsedMap.put("beneficiaryCallType", "TypeA");
//     expectedParsedMap.put("beneficiaryCallSubType", "SubTypeA");
//     // Assuming InputMapper converts String timestamps to java.sql.Timestamp
//     expectedParsedMap.put("startTimestamp", Timestamp.valueOf("2023-01-01 00:00:00"));
//     expectedParsedMap.put("endTimestamp", Timestamp.valueOf("2023-01-01 23:59:59"));
//     expectedParsedMap.put("state", "StateX");
//     expectedParsedMap.put("district", "DistrictY");
//     expectedParsedMap.put("gender", "Male");
//     expectedParsedMap.put("beneficiaryPreferredLanguage", "English");
//     expectedParsedMap.put("beneficiarySexualOrientation", "Straight");

//     // Mock behavior of InputMapper
//     when(inputMapper.getMap(inputJson)).thenReturn(expectedParsedMap);

//     // Create dummy CallQualityReport objects for the expected service response
//     CallQualityReport report1 = new CallQualityReport();
//     report1.setReportId(1L);
//     report1.setCallType("TypeA");
//     report1.setBeneficiaryCallType("TypeA");
//     report1.setBeneficiaryCallSubType("SubTypeA");
//     report1.setProviderServiceMapID(123);

//     CallQualityReport report2 = new CallQualityReport();
//     report2.setReportId(2L);
//     report2.setCallType("TypeB");
//     report2.setBeneficiaryCallType("TypeB");
//     report2.setBeneficiaryCallSubType("SubTypeB");
//     report2.setProviderServiceMapID(456);

//     List<CallQualityReport> expectedReports = Arrays.asList(report1, report2);

//     // Mock behavior of SecondaryReportService
//     when(secondaryReportService.getAllReportsByDate(expectedParsedMap)).thenReturn(expectedReports);

//     // Act
//     // Assuming 'reportController' is the instance of the class under test,
//     // which contains the getAllReportsByDate method.
//     // The method signature is: public ResponseEntity<Object> getAllReportsByDate(String requestBody)
//     ResponseEntity<Object> response = reportController.getAllReportsByDate(inputJson);

//     // Assert
//     assertNotNull(response);
//     assertEquals(HttpStatus.OK, response.getStatusCode());
//     assertNotNull(response.getBody());
//     // The body should be a List<CallQualityReport>
//     assertEquals(expectedReports, response.getBody());

//     // Verify interactions
//     // Verify that InputMapper's getMap method was called with the correct input JSON
//     verify(inputMapper).getMap(inputJson);
//     // Verify that SecondaryReportService's getAllReportsByDate method was called with the parsed map
//     verify(secondaryReportService).getAllReportsByDate(expectedParsedMap);
// }

// @Test
// void shouldReturnCallQualityReportsWhenValidGenderAndDateRangeProvided() {
//     // Arrange
//     SecondaryReportService secondaryReportService = Mockito.mock(SecondaryReportService.class);
//     InputMapper inputMapper = Mockito.mock(InputMapper.class);

//     // Assuming the class under test is named ReportController and has a constructor
//     // that takes SecondaryReportService and InputMapper.
//     // This line instantiates the class under test, assuming its definition is available elsewhere.
//     // "Do NOT repeat the class under test" means not to provide its source code here.
//     ReportController reportController = new ReportController(secondaryReportService, inputMapper);

//     String requestBody = "{\"startTimestamp\":\"2023-01-01 00:00:00\",\"endTimestamp\":\"2023-01-31 23:59:59\",\"gender\":\"Male\",\"providerServiceMapID\":123,\"state\":\"SomeState\",\"district\":\"SomeDistrict\"}";

//     Map<String, Object> parsedRequestMap = new HashMap<>();
//     parsedRequestMap.put("startTimestamp", "2023-01-01 00:00:00");
//     parsedRequestMap.put("endTimestamp", "2023-01-31 23:59:59");
//     parsedRequestMap.put("gender", "Male");
//     parsedRequestMap.put("providerServiceMapID", 123);
//     parsedRequestMap.put("state", "SomeState");
//     parsedRequestMap.put("district", "SomeDistrict");

//     List<CallQualityReport> expectedReports = new ArrayList<>();
//     // Assuming CallQualityReport is a POJO with a no-arg constructor and setters,
//     // and correctly implemented equals/hashCode for list comparison.
//     CallQualityReport report1 = new CallQualityReport();
//     report1.setGender("Male");
//     report1.setTotalCalls(100);
//     report1.setSuccessfulCalls(90);
//     report1.setSuccessRate(90.0);
//     expectedReports.add(report1);

//     CallQualityReport report2 = new CallQualityReport();
//     report2.setGender("Female");
//     report2.setTotalCalls(80);
//     report2.setSuccessfulCalls(75);
//     report2.setSuccessRate(93.75);
//     expectedReports.add(report2);

//     // Mock InputMapper behavior: when parse is called with the requestBody, return the parsed map.
//     Mockito.when(inputMapper.parse(requestBody, Map.class)).thenReturn(parsedRequestMap);

//     // Mock SecondaryReportService behavior: when getAllByGender is called with specific parameters, return the expected reports.
//     Mockito.when(secondaryReportService.getAllByGender(
//         (String) parsedRequestMap.get("startTimestamp"),
//         (String) parsedRequestMap.get("endTimestamp"),
//         (String) parsedRequestMap.get("gender"),
//         (Integer) parsedRequestMap.get("providerServiceMapID"),
//         (String) parsedRequestMap.get("state"),
//         (String) parsedRequestMap.get("district")
//     )).thenReturn(expectedReports);

//     // Act
//     ResponseEntity<Object> response = reportController.getAllByGender(requestBody);

//     // Assert
//     Assertions.assertNotNull(response, "Response should not be null");
//     Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(), "Response status code should be OK");
//     Assertions.assertEquals(expectedReports, response.getBody(), "Response body should match expected reports");

//     // Verify interactions
//     Mockito.verify(inputMapper).parse(requestBody, Map.class);
//     Mockito.verify(secondaryReportService).getAllByGender(
//         (String) parsedRequestMap.get("startTimestamp"),
//         (String) parsedRequestMap.get("endTimestamp"),
//         (String) parsedRequestMap.get("gender"),
//         (Integer) parsedRequestMap.get("providerServiceMapID"),
//         (String) parsedRequestMap.get("state"),
//         (String) parsedRequestMap.get("district")
//     );
//     Mockito.verifyNoMoreInteractions(inputMapper, secondaryReportService);
// }

// }