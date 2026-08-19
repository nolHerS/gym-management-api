package com.imanol.gym.catalog.workout.dto;

public record WorkoutTemplateResponse(
        Long id,
        String name,
        String description,
        Boolean active
) {
}