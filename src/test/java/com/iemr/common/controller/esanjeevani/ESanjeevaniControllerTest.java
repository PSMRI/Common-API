package com.iemr.common.controller.esanjeevani;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.http.MediaType;

import com.iemr.common.service.esanjeevani.ESanjeevaniService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyLong;

@ExtendWith(MockitoExtension.class)
class ESanjeevaniControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private ESanjeevaniController eSanjeevaniController;

    @Mock
    private ESanjeevaniService eSanjeevaniService;

    // Test constants
    private static final String GET_URL_ENDPOINT = "/esanjeevani/getESanjeevaniUrl/{beneficiaryReqId}";
    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_TOKEN = "Bearer token";
    private static final String CONTENT_TYPE = "application/json";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(eSanjeevaniController).build();
    }

    @Test
    void shouldReturnESanjeevaniURL_whenServiceReturnsValidURL() throws Exception {
        Long beneficiaryReqId = 12345L;
        String mockServiceResponse = "https://esanjeevani.example.com/route";
        
        when(eSanjeevaniService.registerPatient(anyLong())).thenReturn(mockServiceResponse);

        mockMvc.perform(get("/esanjeevani/getESanjeevaniUrl/{beneficiaryReqId}", beneficiaryReqId)
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.status").value("Success"))
                .andExpect(jsonPath("$.errorMessage").value("Success"))
                .andExpect(jsonPath("$.data.response").value(mockServiceResponse));
    }

    @Test
    void shouldReturnError_whenServiceThrowsException() throws Exception {
        Long beneficiaryReqId = 12345L;
        RuntimeException testException = new RuntimeException("Connection timeout");
        
        when(eSanjeevaniService.registerPatient(anyLong()))
            .thenThrow(testException);

        mockMvc.perform(get("/esanjeevani/getESanjeevaniUrl/{beneficiaryReqId}", beneficiaryReqId)
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.statusCode").value(5000))
                .andExpect(jsonPath("$.errorMessage").value("Error while fetching E-sanjeevani route URL java.lang.RuntimeException: Connection timeout"));
    }

    @Test
    void shouldLogCorrectlyAndReturnError_whenServiceReturnsNull() throws Exception {
        Long beneficiaryReqId = 67890L;
        
        when(eSanjeevaniService.registerPatient(beneficiaryReqId)).thenReturn(null);

        mockMvc.perform(get("/esanjeevani/getESanjeevaniUrl/{beneficiaryReqId}", beneficiaryReqId)
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.statusCode").value(5000))
                .andExpect(jsonPath("$.errorMessage").value("Error while fetching E-sanjeevani route URL"));
    }

    @Test 
    void shouldTestWithDifferentBeneficiaryId_forLoggerCoverage() throws Exception {
        Long beneficiaryReqId = 99999L;
        String mockServiceResponse = "https://esanjeevani.test.gov.in/session/12345";
        
        when(eSanjeevaniService.registerPatient(beneficiaryReqId)).thenReturn(mockServiceResponse);

        mockMvc.perform(get("/esanjeevani/getESanjeevaniUrl/{beneficiaryReqId}", beneficiaryReqId)
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.status").value("Success"))
                .andExpect(jsonPath("$.errorMessage").value("Success"))
                .andExpect(jsonPath("$.data.response").value(mockServiceResponse));
    }

    @Test
    void shouldHandleServiceException_withNullMessage() throws Exception {
        Long beneficiaryReqId = 11111L;
        RuntimeException exceptionWithNullMessage = new RuntimeException();
        
        when(eSanjeevaniService.registerPatient(beneficiaryReqId))
            .thenThrow(exceptionWithNullMessage);

        mockMvc.perform(get("/esanjeevani/getESanjeevaniUrl/{beneficiaryReqId}", beneficiaryReqId)
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.statusCode").value(5000))
                .andExpect(jsonPath("$.errorMessage").value("Error while fetching E-sanjeevani route URL java.lang.RuntimeException"));
    }
}
