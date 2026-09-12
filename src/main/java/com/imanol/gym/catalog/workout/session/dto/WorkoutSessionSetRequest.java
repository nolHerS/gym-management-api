package com.imanol.gym.catalog.workout.session.dto;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WorkoutSessionSetRequest(
        @NotNull Long sessionExerciseId,
        @NotNull @Min(1) Integer setNumber,
        @Min(0) Integer actualRepetitions,
        @DecimalMin("0.0") BigDecimal weight,
        @Min(0) Integer rir,
        @DecimalMin("0.0") @DecimalMax("10.0") BigDecimal rpe,
        Boolean completed,
        LocalDateTime performedAt
) {}
