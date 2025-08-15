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

import com.iemr.common.service.everwell.EverwellRegistrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class ScheduleForEverwellRegistrationTest {

    @Mock
    private EverwellRegistrationService registrationService;

    @Mock
    private JobExecutionContext jobExecutionContext; // Mock the JobExecutionContext passed to execute method

    @InjectMocks
    private ScheduleForEverwellRegistration scheduleForEverwellRegistration;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testExecute() throws JobExecutionException {
        // Call the execute method
        scheduleForEverwellRegistration.execute(jobExecutionContext);

        // Verify that the registerBeneficiary method of registrationService was called exactly once
        verify(registrationService, times(1)).registerBeneficiary();

        // No need to mock or verify the behavior of jobExecutionContext.getClass().getName()
        // as it's used for logging and doesn't affect the core logic being tested.
    }
}