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
package com.iemr.common.controller.lonic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import com.iemr.common.data.lonic.LonicDescription;
import com.iemr.common.service.lonic.LonicService;
import com.iemr.common.utils.mapper.InputMapper;
import com.iemr.common.utils.response.OutputResponse;
import com.google.gson.JsonObject;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LonicControllerTest {

    @Mock
    private LonicService lonicService;

    @InjectMocks
    private LonicController lonicController;

    private Logger mockLogger;

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        MockitoAnnotations.openMocks(this);

        mockLogger = Mockito.mock(Logger.class);
        Field loggerField = LonicController.class.getDeclaredField("logger");
        loggerField.setAccessible(true);
        loggerField.set(lonicController, mockLogger);
    }

    @Test
    void testSetLonicService() throws NoSuchFieldException, IllegalAccessException {
        LonicService newMockService = Mockito.mock(LonicService.class);
        lonicController.setLonicService(newMockService);

        Field serviceField = LonicController.class.getDeclaredField("lonicService");
        serviceField.setAccessible(true);
        assertEquals(newMockService, serviceField.get(lonicController));
    }

    @Test
    void testGetLonicRecordList_Success() throws Exception {
        String requestJson = "{\"term\":\"test\",\"pageNo\":1}";
        LonicDescription expectedParsed = new LonicDescription();
        expectedParsed.setTerm("test");
        expectedParsed.setPageNo(1);

        String serviceResponseJson = "[{\"loinc_Num\":\"123\",\"component\":\"Comp1\"}]";

        when(lonicService.findLonicRecordList(any(LonicDescription.class)))
                .thenReturn(serviceResponseJson);

        String result = lonicController.getLonicRecordList(requestJson);

        assertNotNull(result);
        OutputResponse expectedOutput = new OutputResponse();
        expectedOutput.setResponse(serviceResponseJson);
        assertEquals(expectedOutput.toString(), result);

        verify(mockLogger, times(1)).info(Mockito.startsWith("getLonicRecord request "));
        verify(mockLogger, times(1)).info(eq("getLonicRecord response: " + expectedOutput.toString()));

        verify(lonicService, times(1)).findLonicRecordList(argThat(ld ->
            ld.getTerm().equals(expectedParsed.getTerm()) &&
            ld.getPageNo().equals(expectedParsed.getPageNo())
        ));
    }

    @Test
    void testGetLonicRecordList_NoRecordsFound() throws Exception {
        String requestJson = "{\"term\":\"no_records\",\"pageNo\":1}";
        LonicDescription expectedParsed = new LonicDescription();
        expectedParsed.setTerm("no_records");
        expectedParsed.setPageNo(1);

        when(lonicService.findLonicRecordList(any(LonicDescription.class)))
                .thenReturn(null);

        String result = lonicController.getLonicRecordList(requestJson);

        assertNotNull(result);
        OutputResponse expectedOutput = new OutputResponse();
        expectedOutput.setResponse("No Records Found");
        assertEquals(expectedOutput.toString(), result);

        verify(mockLogger, times(1)).info(Mockito.startsWith("getLonicRecord request "));
        verify(mockLogger, times(1)).info(eq("getLonicRecord response: " + expectedOutput.toString()));

        verify(lonicService, times(1)).findLonicRecordList(argThat(ld ->
            ld.getTerm().equals(expectedParsed.getTerm()) &&
            ld.getPageNo().equals(expectedParsed.getPageNo())
        ));
    }

    @Test
    void testGetLonicRecordList_InputMapperException() throws Exception {
        String invalidRequestJson = "{invalid json}";

        String result = lonicController.getLonicRecordList(invalidRequestJson);

        assertNotNull(result);
        JsonObject resultJson = InputMapper.gson().fromJson(result, JsonObject.class);

        assertEquals(5000, resultJson.get("statusCode").getAsInt());
        assertTrue(resultJson.get("status").getAsString().startsWith("Failed with "));

        String errorMessage = resultJson.get("errorMessage").getAsString();
        assertNotNull(errorMessage);
        assertFalse(errorMessage.isEmpty());

        verify(mockLogger, times(1)).error(Mockito.startsWith("getLonicRecord failed with error "), any(Exception.class));

     
        verify(lonicService, times(0)).findLonicRecordList(any(LonicDescription.class));
    }

    @Test
    void testGetLonicRecordList_LonicServiceException() throws Exception {
        String requestJson = "{\"term\":\"error_case\",\"pageNo\":1}";
        LonicDescription expectedParsed = new LonicDescription();
        expectedParsed.setTerm("error_case");
        expectedParsed.setPageNo(1);
        Exception serviceException = new Exception("Service failed to retrieve data");

        when(lonicService.findLonicRecordList(any(LonicDescription.class)))
                .thenThrow(serviceException);

        String result = lonicController.getLonicRecordList(requestJson);

        assertNotNull(result);
        JsonObject resultJson = InputMapper.gson().fromJson(result, JsonObject.class);

        assertEquals(5000, resultJson.get("statusCode").getAsInt());
        assertTrue(resultJson.get("status").getAsString().startsWith("Failed with "));
        assertEquals(serviceException.getMessage(), resultJson.get("errorMessage").getAsString());

        verify(mockLogger, times(1)).info(Mockito.startsWith("getLonicRecord request "));
        verify(mockLogger, times(1)).error(eq("getLonicRecord failed with error " + serviceException.getMessage()), eq(serviceException));

       
        verify(lonicService, times(1)).findLonicRecordList(argThat(ld ->
            ld.getTerm().equals(expectedParsed.getTerm()) &&
            ld.getPageNo().equals(expectedParsed.getPageNo())
        ));
    }
}
