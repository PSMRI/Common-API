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
package com.iemr.common.service.questionconfig;

import com.iemr.common.data.questionconfig.QuestionTypeDetail;
import com.iemr.common.repository.questionconfig.QuestionTypeRepository;
import com.iemr.common.utils.exception.IEMRException;
import com.google.gson.JsonSyntaxException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class QuestionTypeServiceImplTest {

    @Mock
    private QuestionTypeRepository questionTypeRepository;

    @InjectMocks
    private QuestionTypeServiceImpl questionTypeServiceImpl;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void setQuestionTypeRepository_shouldSetRepository() {
        QuestionTypeRepository newMockRepository = Mockito.mock(QuestionTypeRepository.class);
        ArrayList<QuestionTypeDetail> expectedList = new ArrayList<>();
        expectedList.add(new QuestionTypeDetail(1L, "TypeA", "DescA"));

        when(newMockRepository.getQuestionTypeList()).thenReturn(expectedList);

        questionTypeServiceImpl.setQuestionTypeRepository(newMockRepository);

        String result = questionTypeServiceImpl.getQuestionTypeList();
        verify(newMockRepository, times(1)).getQuestionTypeList();
        verify(questionTypeRepository, never()).getQuestionTypeList();

        Assertions.assertEquals(expectedList.toString(), result);
    }

    @Test
    void createQuestionType_shouldCreateQuestionTypesSuccessfully() throws IEMRException {
        String requestJson = "[{\"questionTypeID\":null,\"questionType\":\"TypeA\",\"questionTypeDesc\":\"DescA\"}," +
                             "{\"questionTypeID\":null,\"questionType\":\"TypeB\",\"questionTypeDesc\":\"DescB\"}]";

        when(questionTypeRepository.save(any(QuestionTypeDetail.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        String result = questionTypeServiceImpl.createQuestionType(requestJson);

        verify(questionTypeRepository, times(2)).save(any(QuestionTypeDetail.class));

        QuestionTypeDetail[] expectedDetails = {
            new QuestionTypeDetail(null, "TypeA", "DescA"),
            new QuestionTypeDetail(null, "TypeB", "DescB")
        };
        Assertions.assertEquals(Arrays.toString(expectedDetails), result);
    }

    @Test
    void createQuestionType_shouldHandleEmptyRequestSuccessfully() throws IEMRException {
        String requestJson = "[]";

        String result = questionTypeServiceImpl.createQuestionType(requestJson);

        verify(questionTypeRepository, never()).save(any(QuestionTypeDetail.class));
        Assertions.assertEquals("[]", result);
    }

    @Test
    void createQuestionType_shouldThrowJsonSyntaxExceptionForInvalidJson() {
        String invalidJson = "{invalid json}";

        Assertions.assertThrows(JsonSyntaxException.class, () -> {
            questionTypeServiceImpl.createQuestionType(invalidJson);
        });
        verify(questionTypeRepository, never()).save(any(QuestionTypeDetail.class));
    }

    @Test
    void createQuestionType_shouldPropagateExceptionWhenSaveFails() {
        String requestJson = "[{\"questionTypeID\":null,\"questionType\":\"TypeA\",\"questionTypeDesc\":\"DescA\"}]";
        RuntimeException expectedException = new RuntimeException("Database error");

        when(questionTypeRepository.save(any(QuestionTypeDetail.class)))
            .thenThrow(expectedException);

        RuntimeException thrown = Assertions.assertThrows(RuntimeException.class, () -> {
            questionTypeServiceImpl.createQuestionType(requestJson);
        });
        Assertions.assertSame(expectedException, thrown);
        verify(questionTypeRepository, times(1)).save(any(QuestionTypeDetail.class));
    }

    @Test
    void getQuestionTypeList_shouldReturnListOfQuestionTypes() {
        ArrayList<QuestionTypeDetail> mockList = new ArrayList<>();
        mockList.add(new QuestionTypeDetail(1L, "Type1", "Description1"));
        mockList.add(new QuestionTypeDetail(2L, "Type2", "Description2"));

        when(questionTypeRepository.getQuestionTypeList()).thenReturn(mockList);

        String result = questionTypeServiceImpl.getQuestionTypeList();

        verify(questionTypeRepository, times(1)).getQuestionTypeList();
        Assertions.assertEquals(mockList.toString(), result);
    }

    @Test
    void getQuestionTypeList_shouldReturnEmptyListWhenNoQuestionTypesExist() {
        ArrayList<QuestionTypeDetail> emptyList = new ArrayList<>();
        when(questionTypeRepository.getQuestionTypeList()).thenReturn(emptyList);

        String result = questionTypeServiceImpl.getQuestionTypeList();

        verify(questionTypeRepository, times(1)).getQuestionTypeList();
        Assertions.assertEquals(emptyList.toString(), result);
    }
}