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