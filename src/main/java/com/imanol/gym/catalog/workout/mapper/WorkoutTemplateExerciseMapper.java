package com.imanol.gym.catalog.workout.mapper;

import com.imanol.gym.catalog.exercise.mapper.ExerciseMapper;
import com.imanol.gym.catalog.workout.dto.WorkoutTemplateExerciseRequest;
import com.imanol.gym.catalog.workout.dto.WorkoutTemplateExerciseResponse;
import com.imanol.gym.catalog.workout.entity.WorkoutTemplateExercise;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
        componentModel = "spring",
        uses = ExerciseMapper.class
)
public interface WorkoutTemplateExerciseMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "workoutTemplate", ignore = true)
    @Mapping(target = "exercise", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    WorkoutTemplateExercise toEntity(
            WorkoutTemplateExerciseRequest request
    );

    WorkoutTemplateExerciseResponse toResponse(
            WorkoutTemplateExercise entity
    );
}