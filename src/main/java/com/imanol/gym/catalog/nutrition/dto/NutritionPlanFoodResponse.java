package com.imanol.gym.catalog.nutrition.dto;

import java.math.BigDecimal;

public record NutritionPlanFoodResponse(
        Long id,
        FoodResponse food,
        BigDecimal quantity,
        String unit,
        Integer orderIndex
) {
}
