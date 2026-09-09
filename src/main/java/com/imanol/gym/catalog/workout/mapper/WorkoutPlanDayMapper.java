package com.imanol.gym.catalog.workout.mapper;

import com.imanol.gym.catalog.workout.dto.WorkoutPlanDayResponse;
import com.imanol.gym.catalog.workout.entity.WorkoutPlanDay;
import org.mapstruct.Mapper;

@Mapper(
        componentModel = "spring",
        uses = WorkoutPlanExerciseMapper.class
)
public interface WorkoutPlanDayMapper {

    WorkoutPlanDayResponse toResponse(WorkoutPlanDay entity);
}
