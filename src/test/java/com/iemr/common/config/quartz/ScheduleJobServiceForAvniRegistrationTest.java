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