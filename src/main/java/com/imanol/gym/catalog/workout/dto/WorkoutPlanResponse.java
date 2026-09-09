package com.imanol.gym.catalog.workout.dto;

import com.imanol.gym.catalog.workout.entity.WorkoutPlanStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record WorkoutPlanResponse(
        Long id,
        Long clientId,
        Long trainerId,
        Long sourceTemplateId,
        LocalDate startDate,
        LocalDate endDate,
        WorkoutPlanStatus status,
        List<WorkoutPlanDayResponse> days,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
