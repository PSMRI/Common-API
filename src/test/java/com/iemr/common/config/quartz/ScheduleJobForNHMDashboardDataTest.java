/*
* AMRIT – Accessible Medical Records via Integrated Technology 
* Integrated EHR (Electronic Health Records) Solution 
*
* Copyright (C) "Piramal Swasthya Management and Research Institute" 
*
* This file is part of AMRIT.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see https://www.gnu.org/licenses/.
*/
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