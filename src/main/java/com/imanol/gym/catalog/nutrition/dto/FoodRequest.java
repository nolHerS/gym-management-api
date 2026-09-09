package com.imanol.gym.catalog.nutrition.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record FoodRequest(
        @NotBlank @Size(max = 150) String name,
        String description,
        @DecimalMin("0.0")
        @JsonAlias({"caloriesPer100g", "caloriesPerServing"}) BigDecimal calories,
        @DecimalMin("0.0")
        @JsonAlias({"proteinPer100g", "proteinPerServing"}) BigDecimal protein,
        @DecimalMin("0.0")
        @JsonAlias({"carbohydratesPer100g", "carbsPer100g", "carbs"})
        BigDecimal carbohydrates,
        @DecimalMin("0.0")
        @JsonAlias({"fat", "fatPer100g", "fatPerServing"}) BigDecimal fats,
        @DecimalMin("0.0") BigDecimal servingSize,
        @Size(max = 30) String servingUnit
) {
}
