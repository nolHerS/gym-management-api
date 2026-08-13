package com.imanol.gym.catalog.exercise.dto;

import java.time.LocalDateTime;

public record ExerciseCategoryResponse(
        Long id,
        String name,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}