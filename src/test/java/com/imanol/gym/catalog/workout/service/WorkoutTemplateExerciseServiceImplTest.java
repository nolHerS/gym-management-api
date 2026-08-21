package com.imanol.gym.catalog.workout.service;

import com.imanol.gym.catalog.exercise.entity.Exercise;
import com.imanol.gym.catalog.exercise.service.ExerciseService;
import com.imanol.gym.catalog.workout.entity.WorkoutTemplate;
import com.imanol.gym.catalog.workout.entity.WorkoutTemplateExercise;
import com.imanol.gym.catalog.workout.repository.WorkoutTemplateExerciseRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkoutTemplateExerciseServiceImplTest {

    @Mock
    private WorkoutTemplateExerciseRepository workoutTemplateExerciseRepository;

    @Mock
    private WorkoutTemplateService workoutTemplateService;

    @Mock
    private ExerciseService exerciseService;

    @InjectMocks
    private WorkoutTemplateExerciseServiceImpl workoutTemplateExerciseService;

    private WorkoutTemplate workoutTemplate;
    private Exercise exercise;
    private WorkoutTemplateExercise workoutTemplateExercise;

    @BeforeEach
    void setUp() {

        workoutTemplate = new WorkoutTemplate();
        workoutTemplate.setId(1L);
        workoutTemplate.setName("Hypertrophy Template");
        workoutTemplate.setActive(true);

        exercise = new Exercise();
        exercise.setId(1L);
        exercise.setName("Bench Press");
        exercise.setActive(true);

        workoutTemplateExercise = new WorkoutTemplateExercise();
        workoutTemplateExercise.setId(1L);
        workoutTemplateExercise.setOrderIndex(1);
        workoutTemplateExercise.setSets(4);
        workoutTemplateExercise.setRepetitions(10);
        workoutTemplateExercise.setRestSeconds(90);
    }

    @Test
    void shouldCreateWorkoutTemplateExercise() {

        when(workoutTemplateService.findById(1L))
                .thenReturn(workoutTemplate);

        when(exerciseService.findById(1L))
                .thenReturn(exercise);

        when(workoutTemplateExerciseRepository.save(
                workoutTemplateExercise
        )).thenReturn(workoutTemplateExercise);

        WorkoutTemplateExercise result =
                workoutTemplateExerciseService.create(
                        1L,
                        1L,
                        workoutTemplateExercise
                );

        assertThat(result)
                .isEqualTo(workoutTemplateExercise);

        assertThat(workoutTemplateExercise.getWorkoutTemplate())
                .isEqualTo(workoutTemplate);

        assertThat(workoutTemplateExercise.getExercise())
                .isEqualTo(exercise);

        verify(workoutTemplateExerciseRepository)
                .save(workoutTemplateExercise);
    }

    @Test
    void shouldFindAllWorkoutTemplateExercises() {

        WorkoutTemplateExercise firstExercise =
                createWorkoutTemplateExercise(1L);

        WorkoutTemplateExercise secondExercise =
                createWorkoutTemplateExercise(2L);

        when(workoutTemplateService.findById(1L))
                .thenReturn(workoutTemplate);

        when(workoutTemplateExerciseRepository
                .findAllByWorkoutTemplateIdOrderByOrderIndexAsc(1L))
                .thenReturn(List.of(
                        firstExercise,
                        secondExercise
                ));

        List<WorkoutTemplateExercise> result =
                workoutTemplateExerciseService
                        .findAllByWorkoutTemplateId(1L);

        assertThat(result)
                .hasSize(2);

        verify(workoutTemplateService)
                .findById(1L);

        verify(workoutTemplateExerciseRepository)
                .findAllByWorkoutTemplateIdOrderByOrderIndexAsc(1L);
    }

    @Test
    void shouldFindWorkoutTemplateExerciseById() {

        when(workoutTemplateExerciseRepository.findById(1L))
                .thenReturn(Optional.of(workoutTemplateExercise));

        WorkoutTemplateExercise result =
                workoutTemplateExerciseService.findById(1L);

        assertThat(result)
                .isEqualTo(workoutTemplateExercise);
    }

    @Test
    void shouldThrowExceptionWhenWorkoutTemplateExerciseNotFound() {

        when(workoutTemplateExerciseRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                workoutTemplateExerciseService.findById(1L)
        )
                .isInstanceOf(ResourceNotFoundException.class);

        verify(workoutTemplateExerciseRepository)
                .findById(1L);
    }

    @Test
    void shouldUpdateWorkoutTemplateExercise() {

        WorkoutTemplateExercise existingExercise =
                new WorkoutTemplateExercise();

        existingExercise.setId(1L);
        existingExercise.setWorkoutTemplate(workoutTemplate);
        existingExercise.setExercise(exercise);
        existingExercise.setOrderIndex(1);
        existingExercise.setSets(3);
        existingExercise.setRepetitions(8);
        existingExercise.setRestSeconds(60);

        Exercise newExercise = new Exercise();
        newExercise.setId(2L);
        newExercise.setName("Incline Bench Press");
        newExercise.setActive(true);

        WorkoutTemplateExercise updatedData =
                new WorkoutTemplateExercise();

        updatedData.setOrderIndex(2);
        updatedData.setSets(4);
        updatedData.setRepetitions(12);
        updatedData.setRestSeconds(90);

        when(workoutTemplateExerciseRepository.findById(1L))
                .thenReturn(Optional.of(existingExercise));

        when(exerciseService.findById(2L))
                .thenReturn(newExercise);

        when(workoutTemplateExerciseRepository.save(existingExercise))
                .thenReturn(existingExercise);

        WorkoutTemplateExercise result =
                workoutTemplateExerciseService.update(
                        1L,
                        2L,
                        updatedData
                );

        assertThat(result)
                .isEqualTo(existingExercise);

        assertThat(result.getExercise())
                .isEqualTo(newExercise);

        assertThat(result.getOrderIndex())
                .isEqualTo(2);

        assertThat(result.getSets())
                .isEqualTo(4);

        assertThat(result.getRepetitions())
                .isEqualTo(12);

        assertThat(result.getRestSeconds())
                .isEqualTo(90);

        verify(workoutTemplateExerciseRepository)
                .save(existingExercise);
    }

    @Test
    void shouldDeleteWorkoutTemplateExercise() {

        when(workoutTemplateExerciseRepository.findById(1L))
                .thenReturn(Optional.of(workoutTemplateExercise));

        workoutTemplateExerciseService.deleteById(1L);

        verify(workoutTemplateExerciseRepository)
                .delete(workoutTemplateExercise);
    }

    @Test
    void shouldThrowExceptionWhenWorkoutTemplateNotFoundOnCreate() {

        when(workoutTemplateService.findById(1L))
                .thenThrow(
                        new ResourceNotFoundException(
                                "Workout template not found with id: 1"
                        )
                );

        assertThatThrownBy(() ->
                workoutTemplateExerciseService.create(
                        1L,
                        1L,
                        workoutTemplateExercise
                )
        )
                .isInstanceOf(ResourceNotFoundException.class);

        verify(workoutTemplateService)
                .findById(1L);

        verifyNoInteractions(exerciseService);

        verify(workoutTemplateExerciseRepository, never())
                .save(any());
    }

    @Test
    void shouldThrowExceptionWhenExerciseNotFoundOnCreate() {

        when(workoutTemplateService.findById(1L))
                .thenReturn(workoutTemplate);

        when(exerciseService.findById(1L))
                .thenThrow(
                        new ResourceNotFoundException(
                                "Exercise not found with id: 1"
                        )
                );

        assertThatThrownBy(() ->
                workoutTemplateExerciseService.create(
                        1L,
                        1L,
                        workoutTemplateExercise
                )
        )
                .isInstanceOf(ResourceNotFoundException.class);

        verify(workoutTemplateService)
                .findById(1L);

        verify(exerciseService)
                .findById(1L);

        verify(workoutTemplateExerciseRepository, never())
                .save(any());
    }

    @Test
    void shouldThrowExceptionWhenExerciseNotFoundOnUpdate() {

        WorkoutTemplateExercise updatedData =
                new WorkoutTemplateExercise();

        updatedData.setOrderIndex(2);
        updatedData.setSets(4);
        updatedData.setRepetitions(12);
        updatedData.setRestSeconds(90);

        when(workoutTemplateExerciseRepository.findById(1L))
                .thenReturn(Optional.of(workoutTemplateExercise));

        when(exerciseService.findById(2L))
                .thenThrow(
                        new ResourceNotFoundException(
                                "Exercise not found with id: 2"
                        )
                );

        assertThatThrownBy(() ->
                workoutTemplateExerciseService.update(
                        1L,
                        2L,
                        updatedData
                )
        )
                .isInstanceOf(ResourceNotFoundException.class);

        verify(workoutTemplateExerciseRepository, never())
                .save(any());
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistingWorkoutTemplateExercise() {

        when(workoutTemplateExerciseRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                workoutTemplateExerciseService.deleteById(1L)
        )
                .isInstanceOf(ResourceNotFoundException.class);

        verify(workoutTemplateExerciseRepository)
                .findById(1L);

        verify(workoutTemplateExerciseRepository, never())
                .delete(any());
    }

    private WorkoutTemplateExercise createWorkoutTemplateExercise(
            Long id
    ) {

        WorkoutTemplateExercise templateExercise =
                new WorkoutTemplateExercise();

        templateExercise.setId(id);
        templateExercise.setWorkoutTemplate(workoutTemplate);
        templateExercise.setExercise(exercise);
        templateExercise.setOrderIndex(id.intValue());
        templateExercise.setSets(4);
        templateExercise.setRepetitions(10);
        templateExercise.setRestSeconds(90);

        return templateExercise;
    }
}