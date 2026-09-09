package com.imanol.gym.catalog.workout.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record WorkoutPlanDayRequest(
        @NotNull
        @Min(1)
        @Max(7)
        Integer dayOfWeek,
        @NotEmpty
        List<@Valid WorkoutPlanExerciseRequest> exercises
) {
}
