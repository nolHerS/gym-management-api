package com.imanol.gym.catalog.workout.session.dto;
import jakarta.validation.constraints.Min;
public record WorkoutSessionUpdateRequest(String notes, @Min(0) Integer durationSeconds) {}
