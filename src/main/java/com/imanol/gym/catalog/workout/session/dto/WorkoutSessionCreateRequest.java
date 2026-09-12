package com.imanol.gym.catalog.workout.session.dto;
import jakarta.validation.constraints.NotNull;
public record WorkoutSessionCreateRequest(@NotNull Long workoutPlanId) {}
