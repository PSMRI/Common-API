package com.iemr.common.controller.door_to_door_app;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.containsString;

import com.iemr.common.service.door_to_door_app.DoorToDoorService;
import com.iemr.common.data.door_to_door_app.RequestParser;
import com.iemr.common.utils.response.OutputResponse;

@ExtendWith(MockitoExtension.class)
class DoorToDoorAppControllerTest {
    private MockMvc mockMvc;

    @Mock
    DoorToDoorService doorToDoorService;

    @InjectMocks
    DoorToDoorAppController doorToDoorAppController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(doorToDoorAppController).build();
    }

    @Test
    void getUserDetails_Success() throws Exception {
        String requestJson = "{\"userId\":\"testUser\"}";
        String serviceResponse = "{\"userDetails\":\"some data\"}";
        OutputResponse expectedOutputResponse = new OutputResponse();
        expectedOutputResponse.setResponse(serviceResponse);

        when(doorToDoorService.getUserDetails(anyString())).thenReturn(serviceResponse);

        mockMvc.perform(post("/doortodoorapp/getUserDetails")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedOutputResponse.toString()));
    }

    @Test
    void getUserDetails_ServiceReturnsNull() throws Exception {
        String requestJson = "{\"userId\":\"testUser\"}";
        OutputResponse expectedOutputResponse = new OutputResponse();
        expectedOutputResponse.setError(5000, "user details not found");

        when(doorToDoorService.getUserDetails(anyString())).thenReturn(null);

        mockMvc.perform(post("/doortodoorapp/getUserDetails")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedOutputResponse.toString()));
    }

    @Test
    void getUserDetails_ServiceThrowsException() throws Exception {
        String requestJson = "{\"userId\":\"testUser\"}";
        OutputResponse expectedOutputResponse = new OutputResponse();
        Exception serviceException = new RuntimeException("Service error");
        expectedOutputResponse.setError(5000, "Unable to get user data, exception occured. " + serviceException.toString());

        when(doorToDoorService.getUserDetails(anyString())).thenThrow(serviceException);

        mockMvc.perform(post("/doortodoorapp/getUserDetails")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedOutputResponse.toString()));
    }

    @Test
    void getSuspectedData_HRP_TB_NCD_Success() throws Exception {
        String requestJson = "{\"benRegID\":123,\"suspectedTB\":\"Y\",\"suspectedHRP\":\"N\",\"suspectedNCD\":\"Y\",\"suspectedNCDDiseases\":\"Diabetes\"}";
        String serviceResponse = "{\"suspectedStatus\":\"success\"}";
        OutputResponse expectedOutputResponse = new OutputResponse();
        expectedOutputResponse.setResponse(serviceResponse);

        when(doorToDoorService.get_NCD_TB_HRP_Suspected_Status(any(RequestParser.class))).thenReturn(serviceResponse);

        mockMvc.perform(post("/doortodoorapp/getSuspectedData_HRP_TB_NCD")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedOutputResponse.toString()));
    }

    @Test
    void getSuspectedData_HRP_TB_NCD_ServiceReturnsNull() throws Exception {
        String requestJson = "{\"benRegID\":123,\"suspectedTB\":\"Y\",\"suspectedHRP\":\"N\",\"suspectedNCD\":\"Y\",\"suspectedNCDDiseases\":\"Diabetes\"}";
        OutputResponse expectedOutputResponse = new OutputResponse();
        expectedOutputResponse.setError(5000, "Error in getting suspected information");

        when(doorToDoorService.get_NCD_TB_HRP_Suspected_Status(any(RequestParser.class))).thenReturn(null);

        mockMvc.perform(post("/doortodoorapp/getSuspectedData_HRP_TB_NCD")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedOutputResponse.toString()));
    }

    @Test
    void getSuspectedData_HRP_TB_NCD_ServiceThrowsException() throws Exception {
        String requestJson = "{\"benRegID\":123,\"suspectedTB\":\"Y\",\"suspectedHRP\":\"N\",\"suspectedNCD\":\"Y\",\"suspectedNCDDiseases\":\"Diabetes\"}";
        OutputResponse expectedOutputResponse = new OutputResponse();
        Exception serviceException = new RuntimeException("Service error");
        expectedOutputResponse.setError(5000, "Error in getting suspected information, exception occured. " + serviceException.toString());

        when(doorToDoorService.get_NCD_TB_HRP_Suspected_Status(any(RequestParser.class))).thenThrow(serviceException);

        mockMvc.perform(post("/doortodoorapp/getSuspectedData_HRP_TB_NCD")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedOutputResponse.toString()));
    }

    @Test
    void getSuspectedData_HRP_TB_NCD_InvalidJson() throws Exception {
        String invalidRequestJson = "{invalid json}";

        mockMvc.perform(post("/doortodoorapp/getSuspectedData_HRP_TB_NCD")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content(invalidRequestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(5000)))
                // The 'status' field in OutputResponse is set to the error message when setError(int, String) is called.
                .andExpect(jsonPath("$.status", containsString("Error in getting suspected information, exception occured.")))
                .andExpect(jsonPath("$.errorMessage", containsString("Error in getting suspected information, exception occured.")));
    }
}