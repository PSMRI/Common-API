package com.iemr.common.config.quartz;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.iemr.common.service.ctiCall.CallCentreDataSync;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ScheduleForCallCentreTest {

    @Mock
    private CallCentreDataSync callCentreDataSync;

    @Mock
    private JobExecutionContext jobExecutionContext;

    @InjectMocks
    private ScheduleForCallCentre scheduleForCallCentre;

    @Test
    void testExecute_CallsCtiDataSync() throws JobExecutionException {
        // When
        scheduleForCallCentre.execute(jobExecutionContext);

        // Then
        verify(callCentreDataSync, times(1)).ctiDataSync();
    }
}