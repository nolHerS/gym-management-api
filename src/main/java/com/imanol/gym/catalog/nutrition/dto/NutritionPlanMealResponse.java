package com.imanol.gym.catalog.nutrition.dto;

import java.util.List;

public record NutritionPlanMealResponse(
        Long id,
        String name,
        String description,
        Integer orderIndex,
        List<NutritionPlanFoodResponse> foods
) {
}
