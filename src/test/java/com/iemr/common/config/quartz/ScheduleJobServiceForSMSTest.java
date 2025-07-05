package com.iemr.common.config.quartz;

import com.iemr.common.service.sms.SMSService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ScheduleJobServiceForSMSTest {

    @Mock
    private SMSService smsService;

    @Mock
    private JobExecutionContext jobExecutionContext;

    @InjectMocks
    private ScheduleJobServiceForSMS scheduleJobServiceForSMS;

    @Test
    void testExecute() throws JobExecutionException {
        // When
        scheduleJobServiceForSMS.execute(jobExecutionContext);

        // Then
        // Verify that the publishSMS method on smsService was called exactly once
        verify(smsService, times(1)).publishSMS();
        // No need to verify logger calls unless the test specifically targets logging behavior.
        // No need to stub jobExecutionContext methods as their return values are only used for logging.
    }
}