package com.imanol.gym.catalog.nutrition.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record NutritionPlanRequest(
        @NotBlank @Size(max = 150) String name,
        String description,
        @NotNull LocalDate startDate,
        LocalDate endDate,
        @NotEmpty List<@Valid NutritionPlanMealRequest> meals
) {
}
