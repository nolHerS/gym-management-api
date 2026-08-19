package com.imanol.gym.catalog.exercise.service;

import com.imanol.gym.catalog.exercise.dto.ExerciseRequest;
import com.imanol.gym.catalog.exercise.entity.Exercise;
import com.imanol.gym.catalog.exercise.entity.ExerciseCategory;
import com.imanol.gym.catalog.exercise.repository.ExerciseCategoryRepository;
import com.imanol.gym.catalog.exercise.repository.ExerciseRepository;
import com.imanol.gym.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExerciseServiceImplTest {

    @Mock
    private ExerciseRepository exerciseRepository;

    @Mock
    private ExerciseCategoryRepository exerciseCategoryRepository;

    @InjectMocks
    private ExerciseServiceImpl exerciseService;

    @Test
    void shouldCreateExercise() {

        ExerciseCategory category = new ExerciseCategory();
        category.setId(1L);
        category.setName("Chest");
        category.setActive(true);

        ExerciseRequest request = new ExerciseRequest(
                "Bench Press",
                "Barbell bench press",
                1L
        );

        when(exerciseCategoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        when(exerciseRepository.save(any(Exercise.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Exercise result = exerciseService.create(request);

        assertThat(result.getName())
                .isEqualTo("Bench Press");

        assertThat(result.getDescription())
                .isEqualTo("Barbell bench press");

        assertThat(result.getCategory())
                .isEqualTo(category);

        assertThat(result.getActive())
                .isTrue();

        verify(exerciseCategoryRepository).findById(1L);
        verify(exerciseRepository).save(result);
    }

    @Test
    void shouldThrowExceptionWhenCategoryDoesNotExist() {

        ExerciseRequest request = new ExerciseRequest(
                "Bench Press",
                "Barbell bench press",
                99L
        );

        when(exerciseCategoryRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                exerciseService.create(request)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Exercise category not found with id: 99");

        verify(exerciseCategoryRepository).findById(99L);
        verifyNoInteractions(exerciseRepository);
    }

    @Test
    void shouldUpdateExercise() {

        ExerciseCategory oldCategory = new ExerciseCategory();
        oldCategory.setId(1L);
        oldCategory.setName("Chest");
        oldCategory.setActive(true);

        ExerciseCategory newCategory = new ExerciseCategory();
        newCategory.setId(2L);
        newCategory.setName("Shoulders");
        newCategory.setActive(true);

        Exercise existingExercise = new Exercise();
        existingExercise.setId(10L);
        existingExercise.setName("Old Name");
        existingExercise.setDescription("Old description");
        existingExercise.setCategory(oldCategory);
        existingExercise.setActive(true);

        ExerciseRequest request = new ExerciseRequest(
                "Shoulder Press",
                "Dumbbell shoulder press",
                2L
        );

        when(exerciseRepository.findById(10L))
                .thenReturn(Optional.of(existingExercise));

        when(exerciseCategoryRepository.findById(2L))
                .thenReturn(Optional.of(newCategory));

        when(exerciseRepository.save(existingExercise))
                .thenReturn(existingExercise);

        Exercise result = exerciseService.update(10L, request);

        assertThat(result.getName())
                .isEqualTo("Shoulder Press");

        assertThat(result.getDescription())
                .isEqualTo("Dumbbell shoulder press");

        assertThat(result.getCategory())
                .isEqualTo(newCategory);

        assertThat(result.getActive())
                .isTrue();

        verify(exerciseRepository).findById(10L);
        verify(exerciseCategoryRepository).findById(2L);
        verify(exerciseRepository).save(existingExercise);
    }

    @Test
    void shouldFindAllActiveExercises() {

        Exercise exercise = new Exercise();
        exercise.setId(1L);
        exercise.setName("Bench Press");
        exercise.setActive(true);

        when(exerciseRepository.findAllByActiveTrue())
                .thenReturn(List.of(exercise));

        List<Exercise> result = exerciseService.findAll();

        assertThat(result)
                .hasSize(1)
                .containsExactly(exercise);

        verify(exerciseRepository).findAllByActiveTrue();
    }

    @Test
    void shouldFindActiveExercisesByCategory() {

        ExerciseCategory category = new ExerciseCategory();
        category.setId(1L);
        category.setName("Chest");
        category.setActive(true);

        Exercise exercise = new Exercise();
        exercise.setId(1L);
        exercise.setName("Bench Press");
        exercise.setCategory(category);
        exercise.setActive(true);

        when(exerciseCategoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        when(exerciseRepository.findAllByCategoryIdAndActiveTrue(1L))
                .thenReturn(List.of(exercise));

        List<Exercise> result =
                exerciseService.findAllByCategory(1L);

        assertThat(result)
                .hasSize(1)
                .containsExactly(exercise);

        verify(exerciseCategoryRepository).findById(1L);
        verify(exerciseRepository)
                .findAllByCategoryIdAndActiveTrue(1L);
    }

    @Test
    void shouldActivateExercise() {

        Exercise exercise = new Exercise();
        exercise.setId(1L);
        exercise.setActive(false);

        when(exerciseRepository.findById(1L))
                .thenReturn(Optional.of(exercise));

        when(exerciseRepository.save(exercise))
                .thenReturn(exercise);

        exerciseService.activate(1L);

        assertThat(exercise.getActive())
                .isTrue();

        verify(exerciseRepository).save(exercise);
    }

    @Test
    void shouldDeactivateExercise() {

        Exercise exercise = new Exercise();
        exercise.setId(1L);
        exercise.setActive(true);

        when(exerciseRepository.findById(1L))
                .thenReturn(Optional.of(exercise));

        when(exerciseRepository.save(exercise))
                .thenReturn(exercise);

        exerciseService.deactivate(1L);

        assertThat(exercise.getActive())
                .isFalse();

        verify(exerciseRepository).save(exercise);
    }
}