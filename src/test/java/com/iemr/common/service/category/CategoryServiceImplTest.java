package com.iemr.common.service.category;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iemr.common.data.category.CategoryDetails;
import com.iemr.common.repository.category.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @BeforeEach
    void setUp() {

    }

    @Test
    void setCategoryRepository_shouldSetRepository() {
        CategoryRepository newMockRepository = mock(CategoryRepository.class);
        categoryService.setCategoryRepository(newMockRepository);
        CategoryRepository actualRepository = (CategoryRepository) ReflectionTestUtils.getField(categoryService, "categoryRepository");
        assertSame(newMockRepository, actualRepository);
    }

    @Test
    void getAllCategories_noArgs_shouldReturnAllCategories() {
        List<CategoryDetails> expectedCategories = Arrays.asList(
                new CategoryDetails(1, "Category A"),
                new CategoryDetails(2, "Category B")
        );
        when(categoryRepository.findBy()).thenReturn(new ArrayList<>(expectedCategories));

        List<CategoryDetails> actualCategories = categoryService.getAllCategories();

        assertNotNull(actualCategories);
        assertEquals(expectedCategories.size(), actualCategories.size());
        assertTrue(!actualCategories.isEmpty(), "Actual categories list should not be empty");
        verify(categoryRepository, times(1)).findBy();
    }

    @Test
    void getAllCategories_noArgs_shouldReturnEmptyListWhenNoCategoriesFound() {
        when(categoryRepository.findBy()).thenReturn(new ArrayList<>());

        List<CategoryDetails> actualCategories = categoryService.getAllCategories();

        assertNotNull(actualCategories);
        assertTrue(actualCategories.isEmpty());
        verify(categoryRepository, times(1)).findBy();
    }

    @Test
    void getAllCategories_withRequest_feedbackNatureIDNotNull_shouldCallGetCategoriesByNatureID() throws Exception {
        String requestJson = "{\"feedbackNatureID\": 101, \"providerServiceMapID\": 202}";

        CategoryDetails mockCategoryDetails = mock(CategoryDetails.class);
        when(mockCategoryDetails.getFeedbackNatureID()).thenReturn(101);
        when(mockCategoryDetails.getProviderServiceMapID()).thenReturn(202);

        List<CategoryDetails> expectedCategories = Arrays.asList(
                new CategoryDetails(1, "Feedback Cat 1"),
                new CategoryDetails(2, "Feedback Cat 2")
        );

        try (MockedConstruction<ObjectMapper> mockedConstruction = mockConstruction(ObjectMapper.class,
                (mock, context) -> {
                    try {
                        when(mock.readValue(eq(requestJson), eq(CategoryDetails.class))).thenReturn(mockCategoryDetails);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                })) {
            when(categoryRepository.getCategoriesByNatureID(anyInt(), anyInt())).thenReturn(new ArrayList<>(expectedCategories));

            List<CategoryDetails> actualCategories = categoryService.getAllCategories(requestJson);

            assertNotNull(actualCategories);
            assertEquals(expectedCategories.size(), actualCategories.size());
            assertTrue(!actualCategories.isEmpty(), "Actual categories list should not be empty");
            verify(mockedConstruction.constructed().get(0), times(1)).readValue(eq(requestJson), eq(CategoryDetails.class));
            verify(categoryRepository, times(1)).getCategoriesByNatureID(eq(101), eq(202));
            verify(categoryRepository, never()).getAllCategories(anyInt());
            verify(categoryRepository, never()).getAllCategories(anyInt(), anyBoolean());
            verify(categoryRepository, never()).getAllCategories(anyInt(), anyInt());
            verify(categoryRepository, never()).getAllCategories(anyInt(), anyInt(), anyBoolean());
            verify(categoryRepository, never()).findBy();
        }
    }

    @Test
    void getAllCategories_withRequest_isWellBeingNotNull_shouldCallGetAllCategoriesWithIsWellBeing() throws Exception {
        String requestJson = "{\"subServiceID\": 303, \"isWellBeing\": true}";

        CategoryDetails mockCategoryDetails = mock(CategoryDetails.class);
        when(mockCategoryDetails.getFeedbackNatureID()).thenReturn(null);
        when(mockCategoryDetails.getIsWellBeing()).thenReturn(true);
        when(mockCategoryDetails.getSubServiceID()).thenReturn(303);

        List<CategoryDetails> expectedCategories = Arrays.asList(
                new CategoryDetails(3, "WellBeing Cat 1", true),
                new CategoryDetails(4, "WellBeing Cat 2", true)
        );

        try (MockedConstruction<ObjectMapper> mockedConstruction = mockConstruction(ObjectMapper.class,
                (mock, context) -> {
                    try {
                        when(mock.readValue(eq(requestJson), eq(CategoryDetails.class))).thenReturn(mockCategoryDetails);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                })) {
            when(categoryRepository.getAllCategories(anyInt(), anyBoolean())).thenReturn(new ArrayList<>(expectedCategories));

            List<CategoryDetails> actualCategories = categoryService.getAllCategories(requestJson);

            assertNotNull(actualCategories);
            assertEquals(expectedCategories.size(), actualCategories.size());
            assertTrue(!actualCategories.isEmpty(), "Actual categories list should not be empty");
            verify(mockedConstruction.constructed().get(0), times(1)).readValue(eq(requestJson), eq(CategoryDetails.class));
            verify(categoryRepository, times(1)).getAllCategories(eq(303), eq(true));
            verify(categoryRepository, never()).getCategoriesByNatureID(anyInt(), anyInt());
            verify(categoryRepository, never()).getAllCategories(anyInt());
            verify(categoryRepository, never()).getAllCategories(anyInt(), anyInt());
            verify(categoryRepository, never()).getAllCategories(anyInt(), anyInt(), anyBoolean());
            verify(categoryRepository, never()).findBy();
        }
    }

    @Test
    void getAllCategories_withRequest_neitherFeedbackNatureIDNorIsWellBeingNotNull_shouldCallGetAllCategoriesWithSubServiceID() throws Exception {
        String requestJson = "{\"subServiceID\": 404}";

        CategoryDetails mockCategoryDetails = mock(CategoryDetails.class);
        when(mockCategoryDetails.getFeedbackNatureID()).thenReturn(null);
        when(mockCategoryDetails.getIsWellBeing()).thenReturn(null);
        when(mockCategoryDetails.getSubServiceID()).thenReturn(404);

        List<CategoryDetails> expectedCategories = Arrays.asList(
                new CategoryDetails(5, "General Cat 1"),
                new CategoryDetails(6, "General Cat 2")
        );

        try (MockedConstruction<ObjectMapper> mockedConstruction = mockConstruction(ObjectMapper.class,
                (mock, context) -> {
                    try {
                        when(mock.readValue(eq(requestJson), eq(CategoryDetails.class))).thenReturn(mockCategoryDetails);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                })) {
            when(categoryRepository.getAllCategories(anyInt())).thenReturn(new ArrayList<>(expectedCategories));

            List<CategoryDetails> actualCategories = categoryService.getAllCategories(requestJson);

            assertNotNull(actualCategories);
            assertEquals(expectedCategories.size(), actualCategories.size());
            assertTrue(!actualCategories.isEmpty(), "Actual categories list should not be empty");
            verify(mockedConstruction.constructed().get(0), times(1)).readValue(eq(requestJson), eq(CategoryDetails.class));
            verify(categoryRepository, times(1)).getAllCategories(eq(404));
            verify(categoryRepository, never()).getCategoriesByNatureID(anyInt(), anyInt());
            verify(categoryRepository, never()).getAllCategories(anyInt(), anyBoolean());
            verify(categoryRepository, never()).getAllCategories(anyInt(), anyInt());
            verify(categoryRepository, never()).getAllCategories(anyInt(), anyInt(), anyBoolean());
            verify(categoryRepository, never()).findBy();
        }
    }

    @Test
    void getAllCategories_withRequest_shouldThrowExceptionWhenObjectMapperFails() throws Exception {
        String invalidRequestJson = "{invalid json}";

        try (MockedConstruction<ObjectMapper> mockedConstruction = mockConstruction(ObjectMapper.class,
                (mock, context) -> {
                    try {
                        when(mock.readValue(eq(invalidRequestJson), eq(CategoryDetails.class)))
                                .thenThrow(new JsonProcessingException("Test parsing error") {});
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                })) {
            assertThrows(Exception.class, () ->
                    categoryService.getAllCategories(invalidRequestJson)
            );

            verify(mockedConstruction.constructed().get(0), times(1)).readValue(eq(invalidRequestJson), eq(CategoryDetails.class));
            verifyNoInteractions(categoryRepository);
        }
    }

    @Test
    void getAllCategories_withRequest_feedbackNatureIDNotNull_shouldReturnEmptyList() throws Exception {
        String requestJson = "{\"feedbackNatureID\": 101, \"providerServiceMapID\": 202}";

        CategoryDetails mockCategoryDetails = mock(CategoryDetails.class);
        when(mockCategoryDetails.getFeedbackNatureID()).thenReturn(101);
        when(mockCategoryDetails.getProviderServiceMapID()).thenReturn(202);

        try (MockedConstruction<ObjectMapper> mockedConstruction = mockConstruction(ObjectMapper.class,
                (mock, context) -> {
                    try {
                        when(mock.readValue(eq(requestJson), eq(CategoryDetails.class))).thenReturn(mockCategoryDetails);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                })) {
            when(categoryRepository.getCategoriesByNatureID(anyInt(), anyInt())).thenReturn(new ArrayList<>());

            List<CategoryDetails> actualCategories = categoryService.getAllCategories(requestJson);

            assertNotNull(actualCategories);
            assertTrue(actualCategories.isEmpty());
            verify(mockedConstruction.constructed().get(0), times(1)).readValue(eq(requestJson), eq(CategoryDetails.class));
            verify(categoryRepository, times(1)).getCategoriesByNatureID(eq(101), eq(202));
            verify(categoryRepository, never()).getAllCategories(anyInt());
            verify(categoryRepository, never()).getAllCategories(anyInt(), anyBoolean());
            verify(categoryRepository, never()).getAllCategories(anyInt(), anyInt());
            verify(categoryRepository, never()).getAllCategories(anyInt(), anyInt(), anyBoolean());
            verify(categoryRepository, never()).findBy();
        }
    }

    @Test
    void getAllCategories_withRequest_isWellBeingNotNull_shouldReturnEmptyList() throws Exception {
        String requestJson = "{\"subServiceID\": 303, \"isWellBeing\": true}";

        CategoryDetails mockCategoryDetails = mock(CategoryDetails.class);
        when(mockCategoryDetails.getFeedbackNatureID()).thenReturn(null);
        when(mockCategoryDetails.getIsWellBeing()).thenReturn(true);
        when(mockCategoryDetails.getSubServiceID()).thenReturn(303);

        try (MockedConstruction<ObjectMapper> mockedConstruction = mockConstruction(ObjectMapper.class,
                (mock, context) -> {
                    try {
                        when(mock.readValue(eq(requestJson), eq(CategoryDetails.class))).thenReturn(mockCategoryDetails);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                })) {
            when(categoryRepository.getAllCategories(anyInt(), anyBoolean())).thenReturn(new ArrayList<>());

            List<CategoryDetails> actualCategories = categoryService.getAllCategories(requestJson);

            assertNotNull(actualCategories);
            assertTrue(actualCategories.isEmpty());
            verify(mockedConstruction.constructed().get(0), times(1)).readValue(eq(requestJson), eq(CategoryDetails.class));
            verify(categoryRepository, times(1)).getAllCategories(eq(303), eq(true));
            verify(categoryRepository, never()).getCategoriesByNatureID(anyInt(), anyInt());
            verify(categoryRepository, never()).getAllCategories(anyInt());
            verify(categoryRepository, never()).getAllCategories(anyInt(), anyInt());
            verify(categoryRepository, never()).getAllCategories(anyInt(), anyInt(), anyBoolean());
            verify(categoryRepository, never()).findBy();
        }
    }

    @Test
    void getAllCategories_withRequest_neitherFeedbackNatureIDNorIsWellBeingNotNull_shouldReturnEmptyList() throws Exception {
        String requestJson = "{\"subServiceID\": 404}";

        CategoryDetails mockCategoryDetails = mock(CategoryDetails.class);
        when(mockCategoryDetails.getFeedbackNatureID()).thenReturn(null);
        when(mockCategoryDetails.getIsWellBeing()).thenReturn(null);
        when(mockCategoryDetails.getSubServiceID()).thenReturn(404);

        try (MockedConstruction<ObjectMapper> mockedConstruction = mockConstruction(ObjectMapper.class,
                (mock, context) -> {
                    try {
                        when(mock.readValue(eq(requestJson), eq(CategoryDetails.class))).thenReturn(mockCategoryDetails);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                })) {
            when(categoryRepository.getAllCategories(anyInt())).thenReturn(new ArrayList<>());

            List<CategoryDetails> actualCategories = categoryService.getAllCategories(requestJson);

            assertNotNull(actualCategories);
            assertTrue(actualCategories.isEmpty());
            verify(mockedConstruction.constructed().get(0), times(1)).readValue(eq(requestJson), eq(CategoryDetails.class));
            verify(categoryRepository, times(1)).getAllCategories(eq(404));
            verify(categoryRepository, never()).getCategoriesByNatureID(anyInt(), anyInt());
            verify(categoryRepository, never()).getAllCategories(anyInt(), anyBoolean());
            verify(categoryRepository, never()).getAllCategories(anyInt(), anyInt());
            verify(categoryRepository, never()).getAllCategories(anyInt(), anyInt(), anyBoolean());
            verify(categoryRepository, never()).findBy();
        }
    }
}