package com.iemr.common.config.quartz;

import com.iemr.common.service.everwell.EverwellDataSync;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import static org.mockito.Mockito.verify;

class ScheduleForEverwellDataSyncTest {

    @Mock
    private EverwellDataSync everwellDataSync;

    @Mock
    private JobExecutionContext jobExecutionContext;

    @InjectMocks
    private ScheduleForEverwellDataSync scheduleForEverwellDataSync;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testExecute() throws JobExecutionException {
        // Call the method under test
        scheduleForEverwellDataSync.execute(jobExecutionContext);

        // Verify that dataSyncToEverwell method was called on everwellDataSync
        verify(everwellDataSync).dataSyncToEverwell();
    }
}