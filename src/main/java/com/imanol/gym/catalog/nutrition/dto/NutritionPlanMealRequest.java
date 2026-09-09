package com.imanol.gym.catalog.nutrition.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record NutritionPlanMealRequest(
        @NotBlank @Size(max = 100) String name,
        String description,
        @NotNull @Min(1) Integer orderIndex,
        @NotEmpty List<@Valid NutritionPlanFoodRequest> foods
) {
}
