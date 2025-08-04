package com.iemr.common.service.questionconfig;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.iemr.common.data.questionconfig.QuestionnaireDetail;
import com.iemr.common.repository.questionconfig.QuestionnaireRepository;
import com.iemr.common.utils.mapper.InputMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.refEq;
import org.mockito.ArgumentCaptor;

@ExtendWith(MockitoExtension.class)
class QuestionnaireServiceImplTest {

    @InjectMocks
    private QuestionnaireServiceImpl questionnaireService;

    @Mock
    private QuestionnaireRepository questionnaireRepository;

    @Mock
    private InputMapper mockInputMapper;

    @Mock
    private Gson mockGson;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(questionnaireService, "inputMapper", mockInputMapper);
        // Remove or correct the following line if InputMapper.gson() does not return Gson
        // when(mockInputMapper.gson()).thenReturn(mockGson);
    }

    @Test
    void setQuestionnaireRepository_shouldSetRepository() {
        QuestionnaireRepository newMockRepository = mock(QuestionnaireRepository.class);
        questionnaireService.setQuestionnaireRepository(newMockRepository);
        QuestionnaireRepository currentRepository = (QuestionnaireRepository) ReflectionTestUtils.getField(questionnaireService, "questionnaireRepository");
        assertSame(newMockRepository, currentRepository);
    }

    @Test
    void createQuestionnaire_shouldSaveAndReturnDetails() throws Exception {
        String requestJson = "[{\"questionID\":1,\"question\":\"Q1\",\"questionDesc\":\"Desc1\"},{\"questionID\":2,\"question\":\"Q2\",\"questionDesc\":\"Desc2\"}]";
        QuestionnaireDetail detail1 = new QuestionnaireDetail(1L, "Q1", "Desc1");
        QuestionnaireDetail detail2 = new QuestionnaireDetail(2L, "Q2", "Desc2");
        QuestionnaireDetail[] questionnaireDetails = {detail1, detail2};

        when(questionnaireRepository.save(any(QuestionnaireDetail.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String result = questionnaireService.createQuestionnaire(requestJson);

        assertEquals(Arrays.toString(questionnaireDetails), result);
        ArgumentCaptor<QuestionnaireDetail> captor = ArgumentCaptor.forClass(QuestionnaireDetail.class);
        verify(questionnaireRepository, times(2)).save(captor.capture());
        // Assert both QuestionnaireDetail objects were saved with correct fields
        boolean found1 = false, found2 = false;
        for (QuestionnaireDetail captured : captor.getAllValues()) {
            if (captured.getQuestionID().equals(detail1.getQuestionID()) &&
                captured.getQuestion().equals(detail1.getQuestion()) &&
                captured.getQuestionDesc().equals(detail1.getQuestionDesc())) {
                found1 = true;
            }
            if (captured.getQuestionID().equals(detail2.getQuestionID()) &&
                captured.getQuestion().equals(detail2.getQuestion()) &&
                captured.getQuestionDesc().equals(detail2.getQuestionDesc())) {
                found2 = true;
            }
        }
        assertEquals(true, found1, "detail1 was saved");
        assertEquals(true, found2, "detail2 was saved");
    }

    @Test
    void createQuestionnaire_shouldThrowJsonSyntaxException_whenInvalidJson() {
        String invalidJson = "invalid json";

        assertThrows(JsonSyntaxException.class, () -> questionnaireService.createQuestionnaire(invalidJson));
        verify(questionnaireRepository, never()).save(any(QuestionnaireDetail.class));
    }

    @Test
    void createQuestionnaire_shouldThrowRuntimeException_whenRepositorySaveFails() {
        String requestJson = "[{\"questionID\":1,\"question\":\"Q1\",\"questionDesc\":\"Desc1\"}]";
        QuestionnaireDetail detail1 = new QuestionnaireDetail(1L, "Q1", "Desc1");
        QuestionnaireDetail[] questionnaireDetails = {detail1};

        when(questionnaireRepository.save(any(QuestionnaireDetail.class))).thenThrow(new RuntimeException("DB error"));

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> questionnaireService.createQuestionnaire(requestJson));
        assertEquals("DB error", thrown.getMessage());
        ArgumentCaptor<QuestionnaireDetail> captor = ArgumentCaptor.forClass(QuestionnaireDetail.class);
        verify(questionnaireRepository, times(1)).save(captor.capture());
        QuestionnaireDetail captured = captor.getValue();
        assertEquals(detail1.getQuestionID(), captured.getQuestionID());
        assertEquals(detail1.getQuestion(), captured.getQuestion());
        assertEquals(detail1.getQuestionDesc(), captured.getQuestionDesc());
    }

    @Test
    void getQuestionnaireList_shouldReturnEmptyListString_whenNoQuestionnaires() {
        ArrayList<QuestionnaireDetail> emptyList = new ArrayList<>();
        when(questionnaireRepository.getQuestionnaireList()).thenReturn(emptyList);

        String result = questionnaireService.getQuestionnaireList();

        assertEquals("[]", result);
        verify(questionnaireRepository, times(1)).getQuestionnaireList();
    }

    @Test
    void getQuestionnaireList_shouldReturnQuestionnaireListString_whenQuestionnairesExist() {
        QuestionnaireDetail detail1 = new QuestionnaireDetail(1L, "Q1", "Desc1");
        QuestionnaireDetail detail2 = new QuestionnaireDetail(2L, "Q2", "Desc2");
        ArrayList<QuestionnaireDetail> questionnaireList = new ArrayList<>(Arrays.asList(detail1, detail2));

        when(questionnaireRepository.getQuestionnaireList()).thenReturn(questionnaireList);

        String result = questionnaireService.getQuestionnaireList();

        assertEquals(questionnaireList.toString(), result);
        verify(questionnaireRepository, times(1)).getQuestionnaireList();
    }

    @Test
    void getQuestionnaireList_shouldThrowRuntimeException_whenRepositoryFails() {
        when(questionnaireRepository.getQuestionnaireList()).thenThrow(new DataAccessException("DB connection failed") {});

        DataAccessException thrown = assertThrows(DataAccessException.class, () -> questionnaireService.getQuestionnaireList());
        assertEquals("DB connection failed", thrown.getMessage());
        verify(questionnaireRepository, times(1)).getQuestionnaireList();
    }
}