package com.iemr.common.service.category;

import com.iemr.common.data.category.SubCategoryDetails;
import com.iemr.common.repository.category.SubCategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubCategoryServiceImplTest {

    @Mock
    private SubCategoryRepository subCategoryRepository;

    @InjectMocks
    private SubCategoryServiceImpl subCategoryService;

    @Test
    void getSubCategories_shouldReturnListOfSubCategoryDetails_whenRepositoryReturnsData() {
        Integer categoryId = 1;
        ArrayList<Object[]> mockData = new ArrayList<>();
        mockData.add(new Object[]{101, "SubCategory A"});
        mockData.add(new Object[]{102, "SubCategory B"});

        when(subCategoryRepository.findBy(categoryId)).thenReturn(mockData);

        List<SubCategoryDetails> result = subCategoryService.getSubCategories(categoryId);

        assertNotNull(result);
        assertEquals(2, result.size());

        SubCategoryDetails scd1 = result.get(0);
        assertEquals(101, scd1.getSubCategoryID());
        assertEquals("SubCategory A", scd1.getSubCategoryName());

        SubCategoryDetails scd2 = result.get(1);
        assertEquals(102, scd2.getSubCategoryID());
        assertEquals("SubCategory B", scd2.getSubCategoryName());

        verify(subCategoryRepository).findBy(categoryId);
        verifyNoMoreInteractions(subCategoryRepository);
    }

    @Test
    void getSubCategories_shouldReturnEmptyList_whenRepositoryReturnsEmptyList() {
        Integer categoryId = 2;
        when(subCategoryRepository.findBy(categoryId)).thenReturn(new ArrayList<>());

        List<SubCategoryDetails> result = subCategoryService.getSubCategories(categoryId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(subCategoryRepository).findBy(categoryId);
        verifyNoMoreInteractions(subCategoryRepository);
    }

    @Test
    void getSubCategories_shouldFilterOutNullAndEmptyObjects_whenRepositoryReturnsMixedData() {
        Integer categoryId = 3;
        ArrayList<Object[]> mockData = new ArrayList<>();
        mockData.add(new Object[]{301, "Valid SubCategory 1"});
        mockData.add(null); // Null object array
        mockData.add(new Object[]{}); // Empty object array
        mockData.add(new Object[]{302, "Valid SubCategory 2"});

        when(subCategoryRepository.findBy(categoryId)).thenReturn(mockData);

        List<SubCategoryDetails> result = subCategoryService.getSubCategories(categoryId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(301, result.get(0).getSubCategoryID());
        assertEquals("Valid SubCategory 1", result.get(0).getSubCategoryName());
        assertEquals(302, result.get(1).getSubCategoryID());
        assertEquals("Valid SubCategory 2", result.get(1).getSubCategoryName());

        verify(subCategoryRepository).findBy(categoryId);
        verifyNoMoreInteractions(subCategoryRepository);
    }

    @Test
    void getSubCategories_shouldPropagateException_whenRepositoryThrowsException() {
        Integer categoryId = 4;
        RuntimeException expectedException = new RuntimeException("Database connection failed");
        when(subCategoryRepository.findBy(categoryId)).thenThrow(expectedException);

        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> subCategoryService.getSubCategories(categoryId));

        assertEquals("Database connection failed", thrown.getMessage());
        verify(subCategoryRepository).findBy(categoryId);
        verifyNoMoreInteractions(subCategoryRepository);
    }

    @Test
    void setSubCategoryRepository_shouldSetTheRepositoryInstance() throws NoSuchFieldException, IllegalAccessException {
        SubCategoryServiceImpl service = new SubCategoryServiceImpl();
        SubCategoryRepository mockRepo = mock(SubCategoryRepository.class);

        service.setSubCategoryRepository(mockRepo);

        Field field = SubCategoryServiceImpl.class.getDeclaredField("subCategoryRepository");
        field.setAccessible(true);
        SubCategoryRepository actualRepo = (SubCategoryRepository) field.get(service);
        field.setAccessible(false);

        assertEquals(mockRepo, actualRepo);
    }
}