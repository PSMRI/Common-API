package com.iemr.common.service.directory;

import com.iemr.common.data.directory.SubDirectory;
import com.iemr.common.repository.directory.SubDirectoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SubDirectoryServiceImplTest {
    @Mock
    private SubDirectoryRepository subDirectoryRepository;

    @InjectMocks
    private SubDirectoryServiceImpl subDirectoryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetSubDirectories_normal() {
        Set<Object[]> mockResult = new HashSet<>();
        mockResult.add(new Object[]{1, "SubA"});
        mockResult.add(new Object[]{2, "SubB"});
        mockResult.add(null); // Should be ignored
        mockResult.add(new Object[]{}); // Should be ignored
        mockResult.add(new Object[]{3}); // Should be ignored (length != 2)
        when(subDirectoryRepository.findAciveSubDirectories(10)).thenReturn(mockResult);

        List<SubDirectory> result = subDirectoryService.getSubDirectories(10);
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getInstituteSubDirectoryID());
        assertEquals("SubA", result.get(0).getInstituteSubDirectoryName());
        assertEquals(2, result.get(1).getInstituteSubDirectoryID());
        assertEquals("SubB", result.get(1).getInstituteSubDirectoryName());
        verify(subDirectoryRepository).findAciveSubDirectories(10);
    }

    @Test
    void testGetSubDirectories_empty() {
        when(subDirectoryRepository.findAciveSubDirectories(99)).thenReturn(Collections.emptySet());
        List<SubDirectory> result = subDirectoryService.getSubDirectories(99);
        assertTrue(result.isEmpty());
        verify(subDirectoryRepository).findAciveSubDirectories(99);
    }

    @Test
    void testSetSubDirectoryRepository() {
        SubDirectoryServiceImpl service = new SubDirectoryServiceImpl();
        SubDirectoryRepository mockRepo = mock(SubDirectoryRepository.class);
        service.setSubDirectoryRepository(mockRepo);
        // No assertion needed, just ensure setter is covered
    }
}
