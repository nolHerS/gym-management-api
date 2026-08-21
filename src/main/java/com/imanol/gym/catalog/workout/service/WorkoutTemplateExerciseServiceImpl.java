package com.imanol.gym.catalog.workout.service;

import com.imanol.gym.catalog.exercise.entity.Exercise;
import com.imanol.gym.catalog.exercise.service.ExerciseService;
import com.imanol.gym.catalog.workout.entity.WorkoutTemplate;
import com.imanol.gym.catalog.workout.entity.WorkoutTemplateExercise;
import com.imanol.gym.catalog.workout.repository.WorkoutTemplateExerciseRepository;
import com.imanol.gym.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkoutTemplateExerciseServiceImpl
        implements WorkoutTemplateExerciseService {

    private final WorkoutTemplateExerciseRepository
            workoutTemplateExerciseRepository;

    private final WorkoutTemplateService workoutTemplateService;

    private final ExerciseService exerciseService;

    @Override
    public WorkoutTemplateExercise create(
            Long workoutTemplateId,
            Long exerciseId,
            WorkoutTemplateExercise workoutTemplateExercise
    ) {

        WorkoutTemplate workoutTemplate =
                workoutTemplateService.findById(workoutTemplateId);

        Exercise exercise =
                exerciseService.findById(exerciseId);

        workoutTemplateExercise.setWorkoutTemplate(workoutTemplate);
        workoutTemplateExercise.setExercise(exercise);

        return workoutTemplateExerciseRepository.save(
                workoutTemplateExercise
        );
    }

    @Override
    public List<WorkoutTemplateExercise> findAllByWorkoutTemplateId(
            Long workoutTemplateId
    ) {

        workoutTemplateService.findById(workoutTemplateId);

        return workoutTemplateExerciseRepository
                .findAllByWorkoutTemplateIdOrderByOrderIndexAsc(
                        workoutTemplateId
                );
    }

    @Override
    public WorkoutTemplateExercise findById(Long id) {

        return workoutTemplateExerciseRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Workout template exercise not found with id: " + id
                        )
                );
    }

    @Override
    public WorkoutTemplateExercise update(
            Long id,
            Long exerciseId,
            WorkoutTemplateExercise workoutTemplateExercise
    ) {

        WorkoutTemplateExercise existingWorkoutTemplateExercise =
                findById(id);

        Exercise exercise =
                exerciseService.findById(exerciseId);

        existingWorkoutTemplateExercise.setExercise(exercise);
        existingWorkoutTemplateExercise.setOrderIndex(
                workoutTemplateExercise.getOrderIndex()
        );
        existingWorkoutTemplateExercise.setSets(
                workoutTemplateExercise.getSets()
        );
        existingWorkoutTemplateExercise.setRepetitions(
                workoutTemplateExercise.getRepetitions()
        );
        existingWorkoutTemplateExercise.setRestSeconds(
                workoutTemplateExercise.getRestSeconds()
        );

        return workoutTemplateExerciseRepository.save(
                existingWorkoutTemplateExercise
        );
    }

    @Override
    public void deleteById(Long id) {

        WorkoutTemplateExercise workoutTemplateExercise =
                findById(id);

        workoutTemplateExerciseRepository.delete(
                workoutTemplateExercise
        );
    }
}