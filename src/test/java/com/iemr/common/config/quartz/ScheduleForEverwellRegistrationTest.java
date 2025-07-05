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