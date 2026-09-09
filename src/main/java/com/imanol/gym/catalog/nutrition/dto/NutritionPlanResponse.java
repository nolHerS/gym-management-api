package com.imanol.gym.catalog.nutrition.dto;

import com.imanol.gym.catalog.nutrition.entity.NutritionPlanStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record NutritionPlanResponse(
        Long id,
        String name,
        String description,
        Long clientId,
        Long trainerId,
        LocalDate startDate,
        LocalDate endDate,
        NutritionPlanStatus status,
        List<NutritionPlanMealResponse> meals,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
