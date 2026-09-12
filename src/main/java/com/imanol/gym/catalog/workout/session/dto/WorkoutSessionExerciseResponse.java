package com.imanol.gym.catalog.workout.session.dto;
import java.util.List;
public record WorkoutSessionExerciseResponse(Long id, Long exerciseId, String exerciseName,
        Integer orderIndex, Integer plannedSets, Integer plannedRepetitions, Integer plannedRestSeconds,
        Boolean completed, String notes, List<WorkoutSessionSetResponse> sets) {}
