package com.imanol.gym.catalog.nutrition.dto;

import com.imanol.gym.catalog.nutrition.entity.NutritionPlanStatus;
import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.List;
import jakarta.validation.constraints.Size;

public record NutritionPlanUpdateRequest(
        @Size(max = 150) String name,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        NutritionPlanStatus status,
        List<@Valid NutritionPlanMealRequest> meals
) {
}
