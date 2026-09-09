package com.imanol.gym.catalog.nutrition.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record NutritionPlanFoodRequest(
        @NotNull Long foodId,
        @NotNull @DecimalMin("0.01")
        @JsonAlias({"quantityGrams", "grams", "amount"}) BigDecimal quantity,
        @Size(max = 30) String unit,
        @NotNull @Min(1) Integer orderIndex
) {
}
