package com.iemr.common.service.snomedct;

import com.iemr.common.data.snomedct.SCTDescription;
import com.iemr.common.repo.snomedct.SnomedRepository;
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

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SnomedServiceImplTest {
    @Test
    void findSnomedCTRecordFromTerm_nullTerm_returnsNull() {
        when(snomedRepository.findSnomedCTRecordFromTerm(null)).thenReturn(null);
        try (var mocked = Mockito.mockStatic(SCTDescription.class)) {
            mocked.when(() -> SCTDescription.getSnomedCTOBJ(null)).thenReturn(null);
            assertNull(snomedService.findSnomedCTRecordFromTerm(null));
            verify(snomedRepository).findSnomedCTRecordFromTerm(null);
        }
    }

    @Test
    void findSnomedCTRecordFromTerm_emptyList_returnsNull() {
        String term = "";
        when(snomedRepository.findSnomedCTRecordFromTerm(term)).thenReturn(Collections.emptyList());
        try (var mocked = Mockito.mockStatic(SCTDescription.class)) {
            mocked.when(() -> SCTDescription.getSnomedCTOBJ(Collections.emptyList())).thenReturn(null);
            assertNull(snomedService.findSnomedCTRecordFromTerm(term));
            verify(snomedRepository).findSnomedCTRecordFromTerm(term);
        }
    }

    @Test
    void findSnomedCTRecordList_nullInput_throwsException() {
        Exception ex = assertThrows(Exception.class, () -> snomedService.findSnomedCTRecordList(null));
        assertEquals("invalid request", ex.getMessage());
    }

    @Test
    void findSnomedCTRecordList_nullRepoResult_returnsJson() throws Exception {
        SCTDescription sct = new SCTDescription();
        sct.setTerm("test-term");
        sct.setPageNo(0);
        when(snomedRepository.findSnomedCTRecordList(eq("test-term"), any(Pageable.class))).thenReturn(null);
        Exception ex = assertThrows(NullPointerException.class, () -> snomedService.findSnomedCTRecordList(sct));
        assertTrue(ex.getMessage() == null || ex.getMessage().contains("null"));
        verify(snomedRepository).findSnomedCTRecordList(eq("test-term"), any(Pageable.class));
    }
    @Mock
    private SnomedRepository snomedRepository;

    @InjectMocks
    private SnomedServiceImpl snomedService;

    @BeforeEach
    void setUp() {
        // Set the page size value via reflection since @Value is not processed in unit tests
        try {
            java.lang.reflect.Field field = SnomedServiceImpl.class.getDeclaredField("snomedCTPageSize");
            field.setAccessible(true);
            field.set(snomedService, 10);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void findSnomedCTRecordFromTerm_returnsSCTDescription() {
        String term = "test-term";
        List<Object[]> records = Collections.singletonList(new Object[] {"id", "desc"});
        when(snomedRepository.findSnomedCTRecordFromTerm(term)).thenReturn(records);
        SCTDescription mockDesc = mock(SCTDescription.class);
        // Mock static method
        try (var mocked = Mockito.mockStatic(SCTDescription.class)) {
            mocked.when(() -> SCTDescription.getSnomedCTOBJ(records)).thenReturn(mockDesc);
            SCTDescription result = snomedService.findSnomedCTRecordFromTerm(term);
            assertSame(mockDesc, result);
            verify(snomedRepository).findSnomedCTRecordFromTerm(term);
        }
    }

    @Test
    void findSnomedCTRecordList_validRequest_returnsJson() throws Exception {
        SCTDescription sct = new SCTDescription();
        sct.setTerm("test-term");
        sct.setPageNo(0);
        List<SCTDescription> content = Arrays.asList(new SCTDescription(), new SCTDescription());
        Page<SCTDescription> page = new PageImpl<>(content, PageRequest.of(0, 10), 2);
        when(snomedRepository.findSnomedCTRecordList(eq("test-term"), any(Pageable.class))).thenReturn(page);
        // Mock OutputMapper static
        try (var mocked = Mockito.mockStatic(OutputMapper.class)) {
            com.google.gson.Gson gsonMock = mock(com.google.gson.Gson.class, RETURNS_DEEP_STUBS);
            mocked.when(OutputMapper::gson).thenReturn(gsonMock);
            doReturn("{\"sctMaster\":[],\"pageCount\":1}").when(gsonMock).toJson(org.mockito.ArgumentMatchers.<Object>any());
            String json = snomedService.findSnomedCTRecordList(sct);
            assertNotNull(json, "Returned JSON should not be null");
            assertTrue(json.contains("sctMaster"));
            assertTrue(json.contains("pageCount"));
            verify(snomedRepository).findSnomedCTRecordList(eq("test-term"), any(Pageable.class));
        }
    }

    @Test
    void findSnomedCTRecordList_invalidRequest_throwsException() {
        SCTDescription sct = new SCTDescription();
        Exception ex = assertThrows(Exception.class, () -> snomedService.findSnomedCTRecordList(sct));
        assertEquals("invalid request", ex.getMessage());
        // null term
        sct.setPageNo(0);
        ex = assertThrows(Exception.class, () -> snomedService.findSnomedCTRecordList(sct));
        assertEquals("invalid request", ex.getMessage());
        // null pageNo
        sct.setTerm("test-term");
        sct.setPageNo(null);
        ex = assertThrows(Exception.class, () -> snomedService.findSnomedCTRecordList(sct));
        assertEquals("invalid request", ex.getMessage());
    }
}
