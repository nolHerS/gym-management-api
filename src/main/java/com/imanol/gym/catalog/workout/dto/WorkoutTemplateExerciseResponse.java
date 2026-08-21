package com.imanol.gym.catalog.workout.dto;

import com.imanol.gym.catalog.exercise.dto.ExerciseResponse;

public record WorkoutTemplateExerciseResponse(

        Long id,

        ExerciseResponse exercise,

        Integer orderIndex,

        Integer sets,

        Integer repetitions,

        Integer restSeconds

) {
}