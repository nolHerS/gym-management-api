package com.imanol.gym.catalog.workout.session.dto;
import java.math.BigDecimal;
import java.time.LocalDateTime;
public record WorkoutSessionSetResponse(
        Long id, Integer setNumber, Integer plannedRepetitions,
        Integer actualRepetitions, BigDecimal weight, Integer rir,
        BigDecimal rpe, Boolean completed, LocalDateTime performedAt
) {}
