package com.iemr.common.service.lonic;

import com.google.gson.Gson;
import com.iemr.common.data.lonic.LonicDescription;
import com.iemr.common.repository.lonic.LonicRepository;
import com.iemr.common.utils.mapper.OutputMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LonicServiceImplTest {

    @InjectMocks
    private LonicServiceImpl lonicService;

    @Mock
    private LonicRepository lonicRepository;

    private final Integer LOMIC_PAGE_SIZE = 10;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(lonicService, "lonicPageSize", LOMIC_PAGE_SIZE);
        new OutputMapper();
    }

    @Test
    void testFindLonicRecordList_Success() throws Exception {
        LonicDescription inputLonicDescription = new LonicDescription();
        inputLonicDescription.setTerm("testTerm");
        inputLonicDescription.setPageNo(0);

        LonicDescription lonic1 = new LonicDescription();
        lonic1.setLoinc_Num("12345-1");
        lonic1.setComponent("Component A");

        LonicDescription lonic2 = new LonicDescription();
        lonic2.setLoinc_Num("67890-2");
        lonic2.setComponent("Component B");

        List<LonicDescription> lonicListContent = Arrays.asList(lonic1, lonic2);
        Page<LonicDescription> mockPage = new PageImpl<>(lonicListContent, PageRequest.of(0, LOMIC_PAGE_SIZE), 2);

        when(lonicRepository.findLonicRecordList(eq("testTerm"), any(Pageable.class)))
                .thenReturn(mockPage);

        String resultJson = lonicService.findLonicRecordList(inputLonicDescription);

        assertNotNull(resultJson);

        Gson gson = OutputMapper.gson();
        Map<String, Object> resultMap = gson.fromJson(resultJson, Map.class);

        assertNotNull(resultMap);
        assertTrue(resultMap.containsKey("lonicMaster"));
        assertTrue(resultMap.containsKey("pageCount"));

        List<Map<String, Object>> lonicMaster = (List<Map<String, Object>>) resultMap.get("lonicMaster");
        assertEquals(2, lonicMaster.size());
        assertEquals(1.0, resultMap.get("pageCount"));

        verify(lonicRepository).findLonicRecordList(eq("testTerm"), any(Pageable.class));
    }

    @Test
    void testFindLonicRecordList_NoRecordsFound() throws Exception {
        LonicDescription inputLonicDescription = new LonicDescription();
        inputLonicDescription.setTerm("noMatch");
        inputLonicDescription.setPageNo(0);

        List<LonicDescription> emptyList = Collections.emptyList();
        Page<LonicDescription> mockEmptyPage = new PageImpl<>(emptyList, PageRequest.of(0, LOMIC_PAGE_SIZE), 0);

        when(lonicRepository.findLonicRecordList(eq("noMatch"), any(Pageable.class)))
                .thenReturn(mockEmptyPage);

        String resultJson = lonicService.findLonicRecordList(inputLonicDescription);

        assertNotNull(resultJson);

        Gson gson = OutputMapper.gson();
        Map<String, Object> resultMap = gson.fromJson(resultJson, Map.class);

        assertNotNull(resultMap);
        assertTrue(resultMap.containsKey("lonicMaster"));
        assertTrue(resultMap.containsKey("pageCount"));

        List<Map<String, Object>> lonicMaster = (List<Map<String, Object>>) resultMap.get("lonicMaster");
        assertTrue(lonicMaster.isEmpty());
        assertEquals(0.0, resultMap.get("pageCount"));

        verify(lonicRepository).findLonicRecordList(eq("noMatch"), any(Pageable.class));
    }

    @Test
    void testFindLonicRecordList_NullLonicDescriptionThrowsException() {
        LonicDescription inputLonicDescription = null;

        Exception exception = assertThrows(Exception.class, () -> {
            lonicService.findLonicRecordList(inputLonicDescription);
        });
        assertEquals("invalid request", exception.getMessage());
        Mockito.verifyNoInteractions(lonicRepository);
    }

    @Test
    void testFindLonicRecordList_NullTermThrowsException() {
        LonicDescription inputLonicDescription = new LonicDescription();
        inputLonicDescription.setTerm(null);
        inputLonicDescription.setPageNo(0);

        Exception exception = assertThrows(Exception.class, () -> {
            lonicService.findLonicRecordList(inputLonicDescription);
        });
        assertEquals("invalid request", exception.getMessage());
        Mockito.verifyNoInteractions(lonicRepository);
    }

    @Test
    void testFindLonicRecordList_NullPageNoThrowsException() {
        LonicDescription inputLonicDescription = new LonicDescription();
        inputLonicDescription.setTerm("testTerm");
        inputLonicDescription.setPageNo(null);

        Exception exception = assertThrows(Exception.class, () -> {
            lonicService.findLonicRecordList(inputLonicDescription);
        });
        assertEquals("invalid request", exception.getMessage());
        Mockito.verifyNoInteractions(lonicRepository);
    }

    @Test
    void testSetLonicRepository() {
        LonicRepository newMockRepository = Mockito.mock(LonicRepository.class);

        lonicService.setLonicRepository(newMockRepository);

        LonicRepository currentRepository = (LonicRepository) ReflectionTestUtils.getField(lonicService, "lonicRepository");
        assertEquals(newMockRepository, currentRepository);
    }
}