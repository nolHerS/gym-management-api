package com.imanol.gym.catalog.workout.dto;

import com.imanol.gym.catalog.workout.entity.WorkoutPlanStatus;
import jakarta.validation.constraints.AssertTrue;

import java.time.LocalDate;

public record WorkoutPlanUpdateRequest(
        LocalDate startDate,
        LocalDate endDate,
        WorkoutPlanStatus status
) {
    @AssertTrue(message = "endDate must be greater than or equal to startDate")
    public boolean hasValidDates() {
        return startDate == null || endDate == null || !endDate.isBefore(startDate);
    }
}
