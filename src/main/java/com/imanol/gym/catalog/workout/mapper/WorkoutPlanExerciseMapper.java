package com.imanol.gym.catalog.workout.mapper;

import com.imanol.gym.catalog.exercise.mapper.ExerciseMapper;
import com.imanol.gym.catalog.workout.dto.WorkoutPlanExerciseResponse;
import com.imanol.gym.catalog.workout.entity.WorkoutPlanExercise;
import org.mapstruct.Mapper;

@Mapper(
        componentModel = "spring",
        uses = ExerciseMapper.class
)
public interface WorkoutPlanExerciseMapper {

    WorkoutPlanExerciseResponse toResponse(WorkoutPlanExercise entity);
}
