package com.iemr.common.config.quartz;

import com.iemr.common.service.nhm_dashboard.NHM_DashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleJobForNHMDashboardDataTest {

    @Mock
    private NHM_DashboardService nhmDashboardService;

    @Mock
    private JobExecutionContext jobExecutionContext; // Mock JobExecutionContext as it's an input parameter

    @InjectMocks
    private ScheduleJobForNHMDashboardData scheduleJobForNHMDashboardData;

    @BeforeEach
    void setUp() {
        // No specific setup needed for mocks beyond @Mock and @InjectMocks
        // For jobExecutionContext.getClass().getName(), we can just return a dummy string.
        // However, since it's only used for logging and we're not verifying logging behavior directly,
        // it's not strictly necessary to stub it unless it causes a NullPointerException.
        // In this case, getClass().getName() on a mock object will just return the mock class name, which is fine.
    }

    @Test
    void testExecute_Success() throws JobExecutionException, Exception {
        // Arrange
        String expectedServiceResult = "Data pull successful";
        when(nhmDashboardService.pull_NHM_Data_CTI()).thenReturn(expectedServiceResult);

        // Act
        scheduleJobForNHMDashboardData.execute(jobExecutionContext);

        // Assert
        verify(nhmDashboardService, times(1)).pull_NHM_Data_CTI();
        // No direct assertion on logger output as per instructions to avoid unnecessary mocking.
    }

    @Test
    void testExecute_ExceptionDuringDataPull() throws JobExecutionException, Exception {
        // Arrange
        String errorMessage = "Failed to pull data";
        when(nhmDashboardService.pull_NHM_Data_CTI()).thenThrow(new RuntimeException(errorMessage));

        // Act
        scheduleJobForNHMDashboardData.execute(jobExecutionContext);

        // Assert
        verify(nhmDashboardService, times(1)).pull_NHM_Data_CTI();
        // No direct assertion on logger output as per instructions to avoid unnecessary mocking.
        // The method catches the exception and logs it, it does not rethrow JobExecutionException.
    }
}