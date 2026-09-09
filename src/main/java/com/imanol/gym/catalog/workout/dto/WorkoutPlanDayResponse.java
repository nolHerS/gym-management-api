package com.imanol.gym.catalog.workout.dto;

import java.util.List;

public record WorkoutPlanDayResponse(
        Long id,
        Integer dayOfWeek,
        List<WorkoutPlanExerciseResponse> exercises
) {
}
