package com.imanol.gym.catalog.workout.session.dto;
import com.imanol.gym.catalog.workout.session.entity.WorkoutSessionStatus;
import java.time.LocalDateTime; import java.util.List;
public record WorkoutSessionResponse(Long id, Long workoutPlanId, Long clientId, WorkoutSessionStatus status,
        LocalDateTime startedAt, LocalDateTime finishedAt, LocalDateTime cancelledAt, Integer durationSeconds,
        String notes, Long version, List<WorkoutSessionExerciseResponse> exercises) {}
