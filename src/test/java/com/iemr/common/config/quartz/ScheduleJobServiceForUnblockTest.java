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

import com.iemr.common.service.callhandling.BeneficiaryCallService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class ScheduleJobServiceForUnblockTest {

    @Mock
    private BeneficiaryCallService beneficiaryCallService;

    @Mock
    private JobExecutionContext jobExecutionContext;

    @InjectMocks
    private ScheduleJobServiceForUnblock scheduleJobServiceForUnblock;

    @BeforeEach
    void setUp() {
        // Mockito will inject the mocks automatically due to @InjectMocks and @Mock
    }

    @Test
    void testExecute() throws JobExecutionException {
        // When
        scheduleJobServiceForUnblock.execute(jobExecutionContext);

        // Then
        // Verify that unblockBlockedNumbers() method of beneficiaryCallService was called exactly once
        verify(beneficiaryCallService, times(1)).unblockBlockedNumbers();
        // No need to verify logger calls as they are side effects and not core business logic.
        // Also, JobExecutionContext is only used for logging its class name, which is trivial.
    }
}