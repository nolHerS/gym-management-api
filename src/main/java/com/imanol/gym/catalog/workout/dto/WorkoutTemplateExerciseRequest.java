package com.imanol.gym.catalog.workout.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record WorkoutTemplateExerciseRequest(

        @NotNull
        Long exerciseId,

        @NotNull
        @Min(1)
        Integer orderIndex,

        @NotNull
        @Min(1)
        Integer sets,

        @NotNull
        @Min(1)
        Integer repetitions,

        @NotNull
        @Min(0)
        Integer restSeconds

) {
}