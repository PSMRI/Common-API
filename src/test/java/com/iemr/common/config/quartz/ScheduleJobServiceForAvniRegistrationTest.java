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

import com.iemr.common.service.door_to_door_app.DoorToDoorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.quartz.JobExecutionContext;

import static org.mockito.Mockito.*;

class ScheduleJobServiceForAvniRegistrationTest {

    @Mock
    private DoorToDoorService doorToDoorService;

    @Mock
    private JobExecutionContext jobExecutionContext;

    @InjectMocks
    private ScheduleJobServiceForAvniRegistration scheduleJobServiceForAvniRegistration;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testExecute_Success() throws Exception {
        // Mock behavior: do nothing when scheduleJobForRegisterAvniBeneficiary is called
        doNothing().when(doorToDoorService).scheduleJobForRegisterAvniBeneficiary();

        // Execute the method under test
        scheduleJobServiceForAvniRegistration.execute(jobExecutionContext);

        // Verify that doorToDoorService.scheduleJobForRegisterAvniBeneficiary was called exactly once
        verify(doorToDoorService, times(1)).scheduleJobForRegisterAvniBeneficiary();
        // Verify that no other interactions occurred with doorToDoorService
        verifyNoMoreInteractions(doorToDoorService);
    }

    @Test
    void testExecute_DoorToDoorServiceThrowsException() throws Exception {
        // Mock behavior: throw an exception when scheduleJobForRegisterAvniBeneficiary is called
        doThrow(new RuntimeException("Test Exception")).when(doorToDoorService).scheduleJobForRegisterAvniBeneficiary();

        // Execute the method under test
        // The execute method is expected to catch the exception and log it, not re-throw it.
        scheduleJobServiceForAvniRegistration.execute(jobExecutionContext);

        // Verify that doorToDoorService.scheduleJobForRegisterAvniBeneficiary was still attempted
        verify(doorToDoorService, times(1)).scheduleJobForRegisterAvniBeneficiary();
        // Verify that no other interactions occurred with doorToDoorService
        verifyNoMoreInteractions(doorToDoorService);
    }
}