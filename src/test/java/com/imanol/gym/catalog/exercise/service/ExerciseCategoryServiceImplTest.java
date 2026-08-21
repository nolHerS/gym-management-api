package com.imanol.gym.catalog.exercise.service;

import com.imanol.gym.catalog.exercise.entity.ExerciseCategory;
import com.imanol.gym.catalog.exercise.repository.ExerciseCategoryRepository;
import com.imanol.gym.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExerciseCategoryServiceImplTest {

    @Mock
    private ExerciseCategoryRepository exerciseCategoryRepository;

    @InjectMocks
    private ExerciseCategoryServiceImpl exerciseCategoryService;


    @Test
    void shouldCreateExerciseCategory() {
        ExerciseCategory category = new ExerciseCategory();
        category.setName("Chest");
        category.setActive(false);

        when(exerciseCategoryRepository.save(category)).thenReturn(category);

        ExerciseCategory result = exerciseCategoryService.create(category);

        assertTrue(result.getActive());
        assertEquals("Chest", result.getName());

        verify(exerciseCategoryRepository).save(category);
    }

    @Test
    void shouldFindAllActiveExerciseCategories() {
        ExerciseCategory category1 = new ExerciseCategory();
        category1.setName("Chest");
        category1.setActive(true);

        ExerciseCategory category2 = new ExerciseCategory();
        category2.setName("Back");
        category2.setActive(true);

        List<ExerciseCategory> categories = List.of(category1, category2);

        when(exerciseCategoryRepository.findAllByActiveTrue())
                .thenReturn(categories);

        List<ExerciseCategory> result = exerciseCategoryService.findAll();

        assertEquals(2, result.size());
        assertEquals(categories, result);

        verify(exerciseCategoryRepository).findAllByActiveTrue();
    }

    @Test
    void shouldFindExerciseCategoryById() {
        ExerciseCategory category = new ExerciseCategory();
        category.setName("Chest");
        category.setActive(true);

        when(exerciseCategoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        ExerciseCategory result = exerciseCategoryService.findById(1L);

        assertEquals("Chest", result.getName());
        assertTrue(result.getActive());

        verify(exerciseCategoryRepository).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenCategoryDoesNotExist() {
        when(exerciseCategoryRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> exerciseCategoryService.findById(1L)
        );

        verify(exerciseCategoryRepository).findById(1L);
    }

    @Test
    void shouldDeactivateExerciseCategory() {
        ExerciseCategory category = new ExerciseCategory();
        category.setName("Chest");
        category.setActive(true);

        when(exerciseCategoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        exerciseCategoryService.deactivate(1L);

        assertFalse(category.getActive());

        verify(exerciseCategoryRepository).findById(1L);
        verify(exerciseCategoryRepository).save(category);
    }

    @Test
    void shouldActivateExerciseCategory() {
        ExerciseCategory category = new ExerciseCategory();
        category.setName("Chest");
        category.setActive(false);

        when(exerciseCategoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        exerciseCategoryService.activate(1L);

        assertTrue(category.getActive());

        verify(exerciseCategoryRepository).findById(1L);
        verify(exerciseCategoryRepository).save(category);
    }

    @Test
    void shouldUpdateExerciseCategory() {
        ExerciseCategory existingCategory = new ExerciseCategory();
        existingCategory.setName("Chest");
        existingCategory.setActive(true);

        ExerciseCategory updatedCategory = new ExerciseCategory();
        updatedCategory.setName("Upper Chest");

        when(exerciseCategoryRepository.findById(1L))
                .thenReturn(Optional.of(existingCategory));

        when(exerciseCategoryRepository.save(existingCategory))
                .thenReturn(existingCategory);

        ExerciseCategory result =
                exerciseCategoryService.update(1L, updatedCategory);

        assertEquals("Upper Chest", result.getName());
        assertTrue(result.getActive());

        verify(exerciseCategoryRepository).findById(1L);
        verify(exerciseCategoryRepository).save(existingCategory);
    }

}