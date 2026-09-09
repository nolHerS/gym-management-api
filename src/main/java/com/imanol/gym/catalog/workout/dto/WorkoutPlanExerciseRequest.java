package com.imanol.gym.catalog.workout.dto;

import jakarta.validation.constraints.Min;

public record WorkoutPlanExerciseRequest(
        Long sourceTemplateExerciseId,
        Long exerciseId,
        @Min(1) Integer orderIndex,
        @Min(1) Integer sets,
        @Min(1) Integer repetitions,
        @Min(0) Integer restSeconds
) {
}
