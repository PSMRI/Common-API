package com.iemr.common.config.quartz;

import com.iemr.common.service.email.EmailService;
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
class ScheduleJobServiceForEmailTest {

    @InjectMocks
    private ScheduleJobServiceForEmail scheduleJobServiceForEmail;

    @Mock
    private EmailService emailService;

    @Mock
    private JobExecutionContext jobExecutionContext;

    @Test
    void testExecute() throws JobExecutionException {
        // Act
        scheduleJobServiceForEmail.execute(jobExecutionContext);

        // Assert
        verify(emailService, times(1)).publishEmail();
    }
}
