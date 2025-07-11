package com.iemr.common.controller.covid;

import com.iemr.common.data.covid.CovidVaccinationStatus;
import com.iemr.common.service.covid.CovidVaccinationService;
import com.iemr.common.utils.exception.IEMRException;
import com.iemr.common.utils.response.OutputResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CovidVaccinationControllerTest {

    MockMvc mockMvc;

    @Mock
    CovidVaccinationService covidVaccinationService;

    @InjectMocks
    CovidVaccinationController covidVaccinationController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(covidVaccinationController).build();
    }

    @Test
    void getVaccinationTypeAndDoseTaken_Success() throws Exception {
        String serviceResponseData = "{\"data\":\"vaccine_types_data\"}";
        OutputResponse expectedOutputResponse = new OutputResponse();
        expectedOutputResponse.setResponse(serviceResponseData);

        when(covidVaccinationService.getVaccinationTypeAndDoseTaken()).thenReturn(serviceResponseData);

        mockMvc.perform(get("/covid/master/VaccinationTypeAndDoseTaken")
                .header("Authorization", "Bearer test_token"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedOutputResponse.toString()));
    }

    @Test
    void getVaccinationTypeAndDoseTaken_ServiceThrowsException() throws Exception {
        String errorMessage = "Failed to retrieve vaccination types";
        OutputResponse expectedOutputResponse = new OutputResponse();
        expectedOutputResponse.setError(5000, errorMessage);

        when(covidVaccinationService.getVaccinationTypeAndDoseTaken()).thenThrow(new IEMRException(errorMessage));

        mockMvc.perform(get("/covid/master/VaccinationTypeAndDoseTaken")
                .header("Authorization", "Bearer test_token"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedOutputResponse.toString()));
    }

    @Test
    void getCovidVaccinationDetails_Success() throws Exception {
        String requestBody = "{\"beneficiaryRegID\":123}";
        String serviceResponseData = "{\"data\":\"details_for_beneficiary_123\"}";
        OutputResponse expectedOutputResponse = new OutputResponse();
        expectedOutputResponse.setResponse(serviceResponseData);

        when(covidVaccinationService.getCovidVaccinationDetails(123L)).thenReturn(serviceResponseData);

        mockMvc.perform(post("/covid/getCovidVaccinationDetails")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer test_token")
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedOutputResponse.toString()));
    }

    @Test
    void getCovidVaccinationDetails_ServiceThrowsException() throws Exception {
        String requestBody = "{\"beneficiaryRegID\":123}";
        String errorMessage = "Error fetching vaccination details";
        OutputResponse expectedOutputResponse = new OutputResponse();
        expectedOutputResponse.setError(5000, errorMessage);

        when(covidVaccinationService.getCovidVaccinationDetails(123L)).thenThrow(new IEMRException(errorMessage));

        mockMvc.perform(post("/covid/getCovidVaccinationDetails")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer test_token")
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedOutputResponse.toString()));
    }

    @Test
    void saveCovidVaccinationDetails_Success() throws Exception {
        String requestBody = "{\"covidVSID\":1,\"beneficiaryRegID\":123,\"CovidVaccineTypeID\":1,\"ProviderServiceMapID\":1,\"CreatedBy\":\"test\",\"ModifiedBy\":\"test\",\"VanID\":1}";
        String serviceResponseData = "{\"data\":\"save_successful\"}";
        OutputResponse expectedOutputResponse = new OutputResponse();
        expectedOutputResponse.setResponse(serviceResponseData);

        when(covidVaccinationService.saveBenCovidVaccinationDetails(requestBody)).thenReturn(serviceResponseData);

        mockMvc.perform(post("/covid/saveCovidVaccinationDetails")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer test_token")
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedOutputResponse.toString()));
    }

    @Test
    void saveCovidVaccinationDetails_ServiceThrowsException() throws Exception {
        String requestBody = "{\"covidVSID\":1,\"beneficiaryRegID\":123,\"CovidVaccineTypeID\":1,\"ProviderServiceMapID\":1,\"CreatedBy\":\"test\",\"ModifiedBy\":\"test\",\"VanID\":1}";
        String errorMessage = "Failed to save vaccination details";
        OutputResponse expectedOutputResponse = new OutputResponse();
        expectedOutputResponse.setError(5000, errorMessage);

        when(covidVaccinationService.saveBenCovidVaccinationDetails(requestBody)).thenThrow(new IEMRException(errorMessage));

        mockMvc.perform(post("/covid/saveCovidVaccinationDetails")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer test_token")
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedOutputResponse.toString()));
    }
}