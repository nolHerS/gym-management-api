package com.imanol.gym.catalog.workout.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record WorkoutPlanRequest(
        Long sourceTemplateId,
        @NotNull LocalDate startDate,
        LocalDate endDate,
        @NotEmpty List<@Valid WorkoutPlanDayRequest> days
) {
}
