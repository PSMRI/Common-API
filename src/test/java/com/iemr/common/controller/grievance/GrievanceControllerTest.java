package com.iemr.common.controller.grievance;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import com.iemr.common.service.grievance.GrievanceHandlingService;
import com.iemr.common.service.grievance.GrievanceDataSync;
import com.iemr.common.dto.grivance.GrievanceWorklistDTO;
import com.iemr.common.utils.response.OutputResponse;
import com.iemr.common.utils.exception.IEMRException;
import com.iemr.common.data.grievance.UnallocationRequest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonParser;
import com.google.gson.LongSerializationPolicy;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.sql.Timestamp;

@ExtendWith(MockitoExtension.class)
class GrievanceControllerTest {
    private MockMvc mockMvc;

    @Mock
    GrievanceHandlingService grievanceHandlingService;

    @Mock
    GrievanceDataSync grievanceDataSync;

    @InjectMocks
    GrievanceController grievanceController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(grievanceController).build();
    }

    private Timestamp createTimestampWithoutMillis() {
        long currentTimeMillis = System.currentTimeMillis();
        return new Timestamp(currentTimeMillis - (currentTimeMillis % 1000));
    }

    @Test
    void allocatedGrievanceRecordsCount_Success() throws Exception {
        String requestBody = "{\"providerServiceMapID\":1, \"userID\":101}";
        String serviceResponseContent = "{\"count\":5}";

        when(grievanceHandlingService.allocatedGrievanceRecordsCount(anyString()))
                .thenReturn(serviceResponseContent);

        Map<String, Object> expectedMap = new HashMap<>();
        expectedMap.put("data", JsonParser.parseString(serviceResponseContent));
        expectedMap.put("statusCode", 200);
        expectedMap.put("errorMessage", "Success");
        expectedMap.put("status", "Success");

        Gson gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .setLongSerializationPolicy(LongSerializationPolicy.STRING)
                .create();
        String expectedJson = gson.toJson(expectedMap);

        mockMvc.perform(post("/allocatedGrievanceRecordsCount")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer token")
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson));
    }

    @Test
    void allocatedGrievanceRecordsCount_Failure() throws Exception {
        String requestBody = "{\"providerServiceMapID\":1, \"userID\":101}";
        IEMRException serviceException = new IEMRException("Service error");
        OutputResponse expectedOutputResponse = new OutputResponse();
        expectedOutputResponse.setError(serviceException);

        when(grievanceHandlingService.allocatedGrievanceRecordsCount(anyString()))
                .thenThrow(serviceException);

        mockMvc.perform(post("/allocatedGrievanceRecordsCount")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer token")
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedOutputResponse.toString(), false));
    }

    @Test
    void getGrievanceOutboundWorklist_Success() throws Exception {
        String requestBody = "{\"providerServiceMapId\":1, \"userId\":101}";
        List<GrievanceWorklistDTO> serviceResponse = new ArrayList<>();
        GrievanceWorklistDTO dto = new GrievanceWorklistDTO(
            "COMP001", 1L, "Subject1", "Complaint1", 1001L, 1, "1234567890", "High", "State1", 101, false, "user1",
            createTimestampWithoutMillis(), createTimestampWithoutMillis(), false,
            "John", "Doe", "Male", "District1", 1001L, "30", false, 0, createTimestampWithoutMillis(), true
        );
        serviceResponse.add(dto);

        when(grievanceHandlingService.getFormattedGrievanceData(anyString()))
                .thenReturn(serviceResponse);

        Map<String, Object> expectedResponseMap = new HashMap<>();
        expectedResponseMap.put("data", serviceResponse);
        expectedResponseMap.put("statusCode", 200);
        expectedResponseMap.put("errorMessage", "Success");
        expectedResponseMap.put("status", "Success");

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Date.class, new JsonSerializer<Date>() {
                    @Override
                    public JsonElement serialize(Date date, Type typeOfSrc, JsonSerializationContext context) {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        return context.serialize(sdf.format(date));
                    }
                })
                .create();
        String expectedJson = gson.toJson(expectedResponseMap);

        mockMvc.perform(post("/getGrievanceOutboundWorklist")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer token")
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson, true));
    }

    @Test
    void getGrievanceOutboundWorklist_Failure() throws Exception {
        String requestBody = "{\"providerServiceMapId\":1, \"userId\":101}";
        Exception serviceException = new Exception("Failed to fetch data");
         OutputResponse expectedOutputResponse = new OutputResponse();
         expectedOutputResponse.setError(serviceException);

        when(grievanceHandlingService.getFormattedGrievanceData(anyString()))
                .thenThrow(serviceException);


        mockMvc.perform(post("/getGrievanceOutboundWorklist")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer token")
                .content(requestBody))
                .andExpect(status().isOk())
                 .andExpect(content().json(expectedOutputResponse.toString(), false));
    }

    @Test
    void completeGrievanceCall_Success() throws Exception {
        String requestBody = "{\"complaintID\":\"C123\", \"userID\":1, \"isCompleted\":true, \"beneficiaryRegId\":100, \"callTypeID\":1, \"benCallID\":1, \"callID\":\"CALL001\", \"providerServiceMapID\":1, \"createdBy\":\"testUser\"}";
        String expectedServiceResponse = "{\"status\":\"Success\", \"message\":\"Call completed\"}";
        OutputResponse expectedOutputResponse = new OutputResponse();
        expectedOutputResponse.setResponse(expectedServiceResponse);

        when(grievanceDataSync.completeGrievanceCall(anyString()))
                .thenReturn(expectedServiceResponse);

        mockMvc.perform(post("/completeGrievanceCall")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer token")
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedOutputResponse.toString()));
    }

    @Test
    void completeGrievanceCall_Failure() throws Exception {
        String requestBody = "{\"complaintID\":\"C123\", \"userID\":1, \"isCompleted\":true, \"beneficiaryRegId\":100, \"callTypeID\":1, \"benCallID\":1, \"callID\":\"CALL001\", \"providerServiceMapID\":1, \"createdBy\":\"testUser\"}";
        Exception serviceException = new Exception("Call completion failed");
        OutputResponse expectedOutputResponse = new OutputResponse();
        expectedOutputResponse.setError(serviceException);

        when(grievanceDataSync.completeGrievanceCall(anyString()))
                .thenThrow(serviceException);

        mockMvc.perform(post("/completeGrievanceCall")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer token")
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedOutputResponse.toString(), false));
    }

    @Test
    void getGrievanceDetailsWithRemarks_Success() throws Exception {
        String requestBody = "{\"complaintID\":\"COMP001\"}";
        String serviceResponseContent = "{\"grievanceDetails\":{\"id\":1,\"subject\":\"Test\",\"remarks\":\"Some remarks\"}}";
        OutputResponse expectedOutputResponse = new OutputResponse();
        expectedOutputResponse.setResponse(serviceResponseContent);

        when(grievanceHandlingService.getGrievanceDetailsWithRemarks(anyString()))
                .thenReturn(serviceResponseContent);

        mockMvc.perform(post("/getCompleteGrievanceDetails")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer token")
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedOutputResponse.toString()));
    }

    @Test
    void getGrievanceDetailsWithRemarks_Failure() throws Exception {
        String requestBody = "{\"complaintID\":\"COMP001\"}";
        Exception serviceException = new Exception("Failed to get grievance details");
        OutputResponse expectedOutputResponse = new OutputResponse();
        expectedOutputResponse.setError(serviceException);

        when(grievanceHandlingService.getGrievanceDetailsWithRemarks(anyString()))
                .thenThrow(serviceException);

        mockMvc.perform(post("/getCompleteGrievanceDetails")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer token")
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedOutputResponse.toString(), false));
    }

    @Test
    void allocateGrievances_Success() throws Exception {
        String requestBody = "{\"startDate\":\"2022-12-01T07:49:00.000Z\", \"endDate\":\"2025-01-16T07:49:30.561Z\", \"userID\":[101,102], \"allocateNo\":5, \"language\":\"en\"}";
        String serviceResponseContent = "{\"status\":\"Success\", \"message\":\"Grievances allocated successfully\"}";
        OutputResponse expectedOutputResponse = new OutputResponse();
        expectedOutputResponse.setResponse(serviceResponseContent);

        when(grievanceHandlingService.allocateGrievances(anyString()))
                .thenReturn(serviceResponseContent);

        mockMvc.perform(post("/allocateGrievances")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer token")
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedOutputResponse.toString()));
    }

    @Test
    void allocateGrievances_Failure() throws Exception {
        String requestBody = "{\"startDate\":\"2022-12-01T07:49:00.000Z\", \"endDate\":\"2025-01-16T07:49:30.561Z\", \"userID\":[101,102], \"allocateNo\":5, \"language\":\"en\"}";
        Exception serviceException = new Exception("Grievance allocation failed");
        OutputResponse expectedOutputResponse = new OutputResponse();
        expectedOutputResponse.setError(serviceException);

        when(grievanceHandlingService.allocateGrievances(anyString()))
                .thenThrow(serviceException);

        mockMvc.perform(post("/allocateGrievances")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer token")
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedOutputResponse.toString(), false));
    }

    @Test
    void moveToBin_Success() throws Exception {
        String requestBody = "{\"complaintID\":\"COMP002\", \"reason\":\"Not reachable\"}";
        String serviceResponseContent = "{\"status\":\"Success\", \"message\":\"Grievance moved to bin\"}";
        OutputResponse expectedOutputResponse = new OutputResponse();
        expectedOutputResponse.setResponse(serviceResponseContent);

        when(grievanceHandlingService.moveToBin(anyString()))
                .thenReturn(serviceResponseContent);

        mockMvc.perform(post("/moveToBin")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer token")
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedOutputResponse.toString()));
    }

    @Test
    void moveToBin_Failure() throws Exception {
        String requestBody = "{\"complaintID\":\"COMP002\", \"reason\":\"Not reachable\"}";
        Exception serviceException = new Exception("Failed to move grievance to bin");
        OutputResponse expectedOutputResponse = new OutputResponse();
        expectedOutputResponse.setError(serviceException);

        when(grievanceHandlingService.moveToBin(anyString()))
                .thenThrow(serviceException);

        mockMvc.perform(post("/moveToBin")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer token")
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedOutputResponse.toString(), false));
    }

    @Test
    void saveComplaintResolution_Success() throws Exception {
        String requestBody = "{\"complaintID\":\"C001\", \"complaintResolution\":\"Resolved\", \"remarks\":\"No further action\", \"beneficiaryRegID\":123, \"providerServiceMapID\":1, \"userID\":101, \"createdBy\":\"testuser\", \"benCallID\":456}";
        String serviceResponseContent = "{\"status\":\"Success\", \"message\":\"Complaint resolution saved\"}";
        OutputResponse expectedOutputResponse = new OutputResponse();
        expectedOutputResponse.setResponse(serviceResponseContent);

        when(grievanceHandlingService.saveComplaintResolution(anyString()))
                .thenReturn(serviceResponseContent);

        mockMvc.perform(post("/saveComplaintResolution")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer token")
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedOutputResponse.toString()));
    }

    @Test
    void saveComplaintResolution_Failure() throws Exception {
        String requestBody = "{\"complaintID\":\"C001\", \"complaintResolution\":\"Resolved\", \"remarks\":\"No further action\", \"beneficiaryRegID\":123, \"providerServiceMapID\":1, \"userID\":101, \"createdBy\":\"testuser\", \"benCallID\":456}";
        Exception serviceException = new Exception("Failed to save resolution");
        OutputResponse expectedOutputResponse = new OutputResponse();
        expectedOutputResponse.setError(serviceException);

        when(grievanceHandlingService.saveComplaintResolution(anyString()))
                .thenThrow(serviceException);

        mockMvc.perform(post("/saveComplaintResolution")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer token")
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedOutputResponse.toString(), false));
    }

    @Test
    void reallocateGrievances_Success() throws Exception {
        String requestBody = "{\"grievanceIDs\":[\"G001\", \"G002\"], \"newUserID\":102, \"reallocatedBy\":\"admin\"}";
        String serviceResponseContent = "{\"status\":\"Success\", \"message\":\"Grievances reallocated\"}";
        OutputResponse expectedOutputResponse = new OutputResponse();
        expectedOutputResponse.setResponse(serviceResponseContent);

        when(grievanceHandlingService.reallocateGrievances(anyString()))
                .thenReturn(serviceResponseContent);

        mockMvc.perform(post("/reallocateGrievances")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer token")
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedOutputResponse.toString()));
    }

    @Test
    void reallocateGrievances_Failure() throws Exception {
        String requestBody = "{\"grievanceIDs\":[\"G001\", \"G002\"], \"newUserID\":102, \"reallocatedBy\":\"admin\"}";
        Exception serviceException = new Exception("Failed to reallocate grievances");
        OutputResponse expectedOutputResponse = new OutputResponse();
        expectedOutputResponse.setError(serviceException);

        when(grievanceHandlingService.reallocateGrievances(anyString()))
                .thenThrow(serviceException);

        mockMvc.perform(post("/reallocateGrievances")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer token")
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedOutputResponse.toString(), false));
    }

    @Test
    void fetchUnallocatedGrievanceCount_Success() throws Exception {
        String requestBody = "{\"preferredLanguageName\":\"en\", \"filterStartDate\":\"2023-01-01T00:00:00.000Z\", \"filterEndDate\":\"2023-01-31T23:59:59.999Z\", \"providerServiceMapID\":1}";
        String serviceResponseContent = "{\"count\":10}";
        OutputResponse expectedOutputResponse = new OutputResponse();
        expectedOutputResponse.setResponse(serviceResponseContent);

        when(grievanceDataSync.fetchUnallocatedGrievanceCount(anyString(), any(Timestamp.class), any(Timestamp.class), anyInt()))
                .thenReturn(serviceResponseContent);

        mockMvc.perform(post("/unallocatedGrievanceCount")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer token")
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedOutputResponse.toString()));
    }

    @Test
    void fetchUnallocatedGrievanceCount_Failure() throws Exception {
        String requestBody = "{\"preferredLanguageName\":\"en\", \"filterStartDate\":\"2023-01-01T00:00:00.000Z\", \"filterEndDate\":\"2023-01-31T23:59:59.999Z\", \"providerServiceMapID\":1}";
        IEMRException serviceException = new IEMRException("Failed to fetch count");
        OutputResponse expectedOutputResponse = new OutputResponse();
        expectedOutputResponse.setError(serviceException);

        when(grievanceDataSync.fetchUnallocatedGrievanceCount(anyString(), any(Timestamp.class), any(Timestamp.class), anyInt()))
                .thenThrow(serviceException);

        mockMvc.perform(post("/unallocatedGrievanceCount")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer token")
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedOutputResponse.toString(), false));
    }
}