package com.imanol.gym.catalog.nutrition.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FoodResponse(
        Long id,
        String name,
        String description,
        BigDecimal calories,
        BigDecimal protein,
        BigDecimal carbohydrates,
        BigDecimal fats,
        BigDecimal servingSize,
        String servingUnit,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
