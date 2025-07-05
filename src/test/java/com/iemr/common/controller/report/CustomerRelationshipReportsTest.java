package com.iemr.common.controller.report;

import com.iemr.common.mapper.Report1097Mapper;
import com.iemr.common.service.reports.CallReportsService;
import com.iemr.common.utils.response.OutputResponse;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CustomerRelationshipReports.class, 
           excludeAutoConfiguration = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
@ContextConfiguration(classes = {CustomerRelationshipReports.class})
class CustomerRelationshipReportsTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CallReportsService callReportsService;

    @MockBean
    private Report1097Mapper mapper;

    @Test
    void shouldReturnReportTypes_whenServiceReturnsData() throws Exception {
        Integer providerServiceMapID = 1;
        // Based on the previous compilation error, CallReportsService.getReportTypes is assumed to return a String.
        // The controller then wraps this String in an OutputResponse object and calls its toString() method.
        // A common implementation of OutputResponse.toString() would serialize the OutputResponse object itself,
        // resulting in a JSON structure like {"response": "..."} where "..." is the string returned by the service.
        String mockServiceResponse = "[{\"id\":1,\"name\":\"Report A\"},{\"id\":2,\"name\":\"Report B\"}]";
        when(callReportsService.getReportTypes(anyInt())).thenReturn(mockServiceResponse);

        // Construct the expected JSON output from the controller, assuming OutputResponse wraps the string.
        String expectedControllerOutput = "{\"response\":" + mockServiceResponse + "}";

        mockMvc.perform(get("/crmReports/getReportTypes/{providerServiceMapID}", providerServiceMapID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedControllerOutput));
    }

    @Test
    void shouldReturnInternalServerError_whenServiceThrowsException() throws Exception {
        Integer providerServiceMapID = 2;
        String errorMessage = "Service unavailable";

        when(callReportsService.getReportTypes(anyInt())).thenThrow(new RuntimeException(errorMessage));

        mockMvc.perform(get("/crmReports/getReportTypes/{providerServiceMapID}", providerServiceMapID))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void shouldReturnEmptyArrayInResponse_whenServiceReturnsEmptyData() throws Exception {
        Integer providerServiceMapID = 3;
        String emptyServiceResponse = "[]"; // Empty JSON array string

        when(callReportsService.getReportTypes(anyInt())).thenReturn(emptyServiceResponse);

        // Construct the expected JSON output from the controller for an empty response.
        String expectedControllerOutput = "{\"response\":" + emptyServiceResponse + "}";

        mockMvc.perform(get("/crmReports/getReportTypes/{providerServiceMapID}", providerServiceMapID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedControllerOutput));
    }
}