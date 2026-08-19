package com.imanol.gym.catalog.exercise.dto;

public record ExerciseResponse(
        Long id,
        String name,
        String description,
        Long categoryId,
        String categoryName,
        Boolean active
) {
}