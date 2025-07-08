package com.iemr.common.controller.secondaryReport;

import com.iemr.common.service.reportSecondary.SecondaryReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.ByteArrayInputStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CustomerRelationshipSecondaryReportsTest {
    private MockMvc mockMvc;
    private SecondaryReportService secondaryReportService;

    @BeforeEach
    void setUp() throws Exception {
        secondaryReportService = Mockito.mock(SecondaryReportService.class);
        CustomerRelationshipSecondaryReports controller = new CustomerRelationshipSecondaryReports();
        java.lang.reflect.Field serviceField = CustomerRelationshipSecondaryReports.class.getDeclaredField("secondaryReportService");
        serviceField.setAccessible(true);
        serviceField.set(controller, secondaryReportService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    private static final String AUTH_HEADER = "Authorization";
    private static final String AUTH_TOKEN = "Bearer token";
    private static final String JSON_BODY = "{\"startDate\":\"2024-01-01\",\"endDate\":\"2024-01-31\"}";
    private static final byte[] DUMMY_XLSX = "dummydata".getBytes();

    @Test
    void getQualityReport_success() throws Exception {
        when(secondaryReportService.getQualityReport(any(String.class), any(String.class)))
                .thenReturn(new ByteArrayInputStream(DUMMY_XLSX));
        mockMvc.perform(post("/crmReports/getQualityReport")
                .header(AUTH_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON_BODY))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("attachment; filename=104QAReport.xlsx")))
                .andExpect(content().contentType("application/vnd.ms-excel"));
    }

    @Test
    void getQualityReport_noDataFound() throws Exception {
        when(secondaryReportService.getQualityReport(any(String.class), any(String.class)))
                .thenThrow(new RuntimeException("No data found"));
        mockMvc.perform(post("/crmReports/getQualityReport")
                .header(AUTH_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON_BODY))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("No data found"));
    }

    @Test
    void getQualityReport_otherError() throws Exception {
        when(secondaryReportService.getQualityReport(any(String.class), any(String.class)))
                .thenThrow(new RuntimeException("Some error"));
        mockMvc.perform(post("/crmReports/getQualityReport")
                .header(AUTH_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON_BODY))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Some error"));
    }

    @Test
    void getComplaintDetailReport_success() throws Exception {
        when(secondaryReportService.getComplaintDetailReport(any(String.class), any(String.class)))
                .thenReturn(new ByteArrayInputStream(DUMMY_XLSX));
        mockMvc.perform(post("/crmReports/getComplaintDetailReport")
                .header(AUTH_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON_BODY))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("attachment; filename=Grievance_Details.xlsx")))
                .andExpect(content().contentType("application/vnd.ms-excel"));
    }

    @Test
    void getComplaintDetailReport_error() throws Exception {
        when(secondaryReportService.getComplaintDetailReport(any(String.class), any(String.class)))
                .thenThrow(new RuntimeException("No data found"));
        mockMvc.perform(post("/crmReports/getComplaintDetailReport")
                .header(AUTH_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON_BODY))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("No data found"));
    }

    @Test
    void getCallSummaryReport_success() throws Exception {
        when(secondaryReportService.getCallSummaryReport(any(String.class), any(String.class)))
                .thenReturn(new ByteArrayInputStream(DUMMY_XLSX));
        mockMvc.perform(post("/crmReports/getCallSummaryReport")
                .header(AUTH_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON_BODY))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("attachment; filename=Call_Summary_Report.xlsx")))
                .andExpect(content().contentType("application/vnd.ms-excel"));
    }

    @Test
    void getCallSummaryReport_error() throws Exception {
        when(secondaryReportService.getCallSummaryReport(any(String.class), any(String.class)))
                .thenThrow(new RuntimeException("No data found"));
        mockMvc.perform(post("/crmReports/getCallSummaryReport")
                .header(AUTH_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON_BODY))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("No data found"));
    }

    @Test
    void getAllBySexualOrientation_success() throws Exception {
        when(secondaryReportService.getAllBySexualOrientationReport(any(String.class), any(String.class)))
                .thenReturn(new ByteArrayInputStream(DUMMY_XLSX));
        mockMvc.perform(post("/crmReports/getAllBySexualOrientation")
                .header(AUTH_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON_BODY))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("attachment; filename=Sexual_Orientation_Report.xlsx")))
                .andExpect(content().contentType("application/vnd.ms-excel"));
    }

    @Test
    void getAllBySexualOrientation_error() throws Exception {
        when(secondaryReportService.getAllBySexualOrientationReport(any(String.class), any(String.class)))
                .thenThrow(new RuntimeException("No data found"));
        mockMvc.perform(post("/crmReports/getAllBySexualOrientation")
                .header(AUTH_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON_BODY))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("No data found"));
    }

    @Test
    void getDistrictWiseCallReport_success() throws Exception {
        when(secondaryReportService.getDistrictWiseCallReport(any(String.class), any(String.class)))
                .thenReturn(new ByteArrayInputStream(DUMMY_XLSX));
        mockMvc.perform(post("/crmReports/getDistrictWiseCallReport")
                .header(AUTH_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON_BODY))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("attachment; filename=District_Wise_Call_Volume_Report.xlsx")))
                .andExpect(content().contentType("application/vnd.ms-excel"));
    }

    @Test
    void getDistrictWiseCallReport_error() throws Exception {
        when(secondaryReportService.getDistrictWiseCallReport(any(String.class), any(String.class)))
                .thenThrow(new RuntimeException("No data found"));
        mockMvc.perform(post("/crmReports/getDistrictWiseCallReport")
                .header(AUTH_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON_BODY))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("No data found"));
    }

    @Test
    void getUnblockedUserReport_success() throws Exception {
        when(secondaryReportService.getUnblockedUserReport(any(String.class), any(String.class)))
                .thenReturn(new ByteArrayInputStream(DUMMY_XLSX));
        mockMvc.perform(post("/crmReports/getUnblockedUserReport")
                .header(AUTH_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON_BODY))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("attachment; filename=Unblock_User_Report.xlsx")))
                .andExpect(content().contentType("application/vnd.ms-excel"));
    }

    @Test
    void getUnblockedUserReport_error() throws Exception {
        when(secondaryReportService.getUnblockedUserReport(any(String.class), any(String.class)))
                .thenThrow(new RuntimeException("No data found"));
        mockMvc.perform(post("/crmReports/getUnblockedUserReport")
                .header(AUTH_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON_BODY))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("No data found"));
    }

    @Test
    void getCallQualityReport_success() throws Exception {
        when(secondaryReportService.getCallQualityReport(any(com.iemr.common.data.report.CallQualityReport.class), any(String.class)))
                .thenReturn(new ByteArrayInputStream(DUMMY_XLSX));
        mockMvc.perform(post("/crmReports/getCallQualityReport")
                .header(AUTH_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fileName\":\"TestFile\"}"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("attachment; filename=TestFile.xlsx")))
                .andExpect(content().contentType("application/vnd.ms-excel"));
    }

    @Test
    void getCallQualityReport_error() throws Exception {
        when(secondaryReportService.getCallQualityReport(any(com.iemr.common.data.report.CallQualityReport.class), any(String.class)))
                .thenThrow(new RuntimeException("No data found"));
        mockMvc.perform(post("/crmReports/getCallQualityReport")
                .header(AUTH_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fileName\":\"TestFile\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("No data found"));
    }

    @Test
    void getCountsByPreferredLanguage_success() throws Exception {
        when(secondaryReportService.getCountsByPrefferedLanguage(any(String.class), any(String.class)))
                .thenReturn(new ByteArrayInputStream(DUMMY_XLSX));
        mockMvc.perform(post("/crmReports/getCountsByPreferredLanguage")
                .header(AUTH_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON_BODY))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("attachment; filename=Language_Distribution_Report.xlsx")))
                .andExpect(content().contentType("application/vnd.ms-excel"));
    }

    @Test
    void getCountsByPreferredLanguage_error() throws Exception {
        when(secondaryReportService.getCountsByPrefferedLanguage(any(String.class), any(String.class)))
                .thenThrow(new RuntimeException("No data found"));
        mockMvc.perform(post("/crmReports/getCountsByPreferredLanguage")
                .header(AUTH_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON_BODY))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("No data found"));
    }

    @Test
    void getAllByAgeGroup_success() throws Exception {
        when(secondaryReportService.getAllByAgeGroup(any(String.class), any(String.class)))
                .thenReturn(new ByteArrayInputStream(DUMMY_XLSX));
        mockMvc.perform(post("/crmReports/getAllByAgeGroup")
                .header(AUTH_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON_BODY))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("attachment; filename=Caller_Age_Group_Report.xlsx")))
                .andExpect(content().contentType("application/vnd.ms-excel"));
    }

    @Test
    void getAllByAgeGroup_error() throws Exception {
        when(secondaryReportService.getAllByAgeGroup(any(String.class), any(String.class)))
                .thenThrow(new RuntimeException("No data found"));
        mockMvc.perform(post("/crmReports/getAllByAgeGroup")
                .header(AUTH_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON_BODY))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("No data found"));
    }

    @Test
    void getAllReportsByDate_success() throws Exception {
        when(secondaryReportService.getAllReportsByDate(any(String.class), any(String.class)))
                .thenReturn(new ByteArrayInputStream(DUMMY_XLSX));
        mockMvc.perform(post("/crmReports/getAllReportsByDate")
                .header(AUTH_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON_BODY))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("attachment; filename=Call_Type_Report.xlsx")))
                .andExpect(content().contentType("application/vnd.ms-excel"));
    }

    @Test
    void getAllReportsByDate_error() throws Exception {
        when(secondaryReportService.getAllReportsByDate(any(String.class), any(String.class)))
                .thenThrow(new RuntimeException("No data found"));
        mockMvc.perform(post("/crmReports/getAllReportsByDate")
                .header(AUTH_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON_BODY))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("No data found"));
    }

    @Test
    void getAllByGender_success() throws Exception {
        when(secondaryReportService.getAllByGender(any(String.class), any(String.class)))
                .thenReturn(new ByteArrayInputStream(DUMMY_XLSX));
        mockMvc.perform(post("/crmReports/getAllByGender")
                .header(AUTH_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON_BODY))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("attachment; filename=Gender_Distribution_Report.xlsx")))
                .andExpect(content().contentType("application/vnd.ms-excel"));
    }

    @Test
    void getAllByGender_error() throws Exception {
        when(secondaryReportService.getAllByGender(any(String.class), any(String.class)))
                .thenThrow(new RuntimeException("No data found"));
        mockMvc.perform(post("/crmReports/getAllByGender")
                .header(AUTH_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON_BODY))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("No data found"));
    }


    @Test
    void getAllByGender_otherError() throws Exception {
        when(secondaryReportService.getAllByGender(any(String.class), any(String.class)))
                .thenThrow(new RuntimeException("Some other error"));
        mockMvc.perform(post("/crmReports/getAllByGender")
                .header(AUTH_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON_BODY))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Some other error"));
    }
}
