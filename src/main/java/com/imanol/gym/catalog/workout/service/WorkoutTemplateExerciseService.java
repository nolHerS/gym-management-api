package com.imanol.gym.catalog.workout.service;

import com.imanol.gym.catalog.workout.entity.WorkoutTemplateExercise;

import java.util.List;

public interface WorkoutTemplateExerciseService {

    WorkoutTemplateExercise create(
            Long workoutTemplateId,
            Long exerciseId,
            WorkoutTemplateExercise workoutTemplateExercise
    );

    List<WorkoutTemplateExercise> findAllByWorkoutTemplateId(
            Long workoutTemplateId
    );

    WorkoutTemplateExercise findById(Long id);

    WorkoutTemplateExercise update(
            Long id,
            Long exerciseId,
            WorkoutTemplateExercise workoutTemplateExercise
    );

    void deleteById(Long id);
}