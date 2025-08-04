package com.iemr.common.service.directory;

import com.iemr.common.data.directory.Directory;
import com.iemr.common.repository.directory.DirectoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DirectoryServiceImplTest {

    @Test
    void testSetDirectoryRepository() {
        DirectoryServiceImpl service = new DirectoryServiceImpl();
        DirectoryRepository mockRepo = mock(DirectoryRepository.class);
        service.setDirectoryRepository(mockRepo);
        // No assertion needed, just ensure no exception and setter is covered
    }
    @Mock
    private DirectoryRepository directoryRepository;

    @InjectMocks
    private DirectoryServiceImpl directoryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetDirectories_fromObjectArray() {
        Set<Object[]> mockResult = new HashSet<>();
        mockResult.add(new Object[]{1, "TestDir"});
        mockResult.add(new Object[]{2, "AnotherDir"});
        mockResult.add(null); // Should be ignored
        mockResult.add(new Object[]{}); // Should be ignored
        mockResult.add(new Object[]{3}); // Should be ignored (length < 2)
        when(directoryRepository.findAciveDirectories()).thenReturn(mockResult);

        List<Directory> result = directoryService.getDirectories();
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getInstituteDirectoryID());
        assertEquals("TestDir", result.get(0).getInstituteDirectoryName());
        assertEquals(2, result.get(1).getInstituteDirectoryID());
        assertEquals("AnotherDir", result.get(1).getInstituteDirectoryName());
        verify(directoryRepository).findAciveDirectories();
    }

    @Test
    void testGetDirectories_fromObjectArray_empty() {
        when(directoryRepository.findAciveDirectories()).thenReturn(Collections.emptySet());
        List<Directory> result = directoryService.getDirectories();
        assertTrue(result.isEmpty());
        verify(directoryRepository).findAciveDirectories();
    }

    @Test
    void testGetDirectories_withProviderServiceMapID() {
        List<Directory> mockList = Arrays.asList(
                new Directory(10, "DirA"),
                new Directory(20, "DirB")
        );
        when(directoryRepository.findAciveDirectories(123)).thenReturn(mockList);
        List<Directory> result = directoryService.getDirectories(123);
        assertEquals(2, result.size());
        assertEquals(10, result.get(0).getInstituteDirectoryID());
        assertEquals("DirA", result.get(0).getInstituteDirectoryName());
        assertEquals(20, result.get(1).getInstituteDirectoryID());
        assertEquals("DirB", result.get(1).getInstituteDirectoryName());
        verify(directoryRepository).findAciveDirectories(123);
    }

    @Test
    void testGetDirectories_withProviderServiceMapID_empty() {
        when(directoryRepository.findAciveDirectories(999)).thenReturn(Collections.emptyList());
        List<Directory> result = directoryService.getDirectories(999);
        assertTrue(result.isEmpty());
        verify(directoryRepository).findAciveDirectories(999);
    }
}
