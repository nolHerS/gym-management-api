package com.imanol.gym.catalog.workout.dto;

import com.imanol.gym.catalog.exercise.dto.ExerciseResponse;

public record WorkoutPlanExerciseResponse(
        Long id,
        ExerciseResponse exercise,
        Long sourceTemplateExerciseId,
        Integer orderIndex,
        Integer sets,
        Integer repetitions,
        Integer restSeconds
) {
}
