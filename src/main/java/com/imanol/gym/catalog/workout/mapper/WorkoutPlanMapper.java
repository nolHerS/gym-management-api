package com.imanol.gym.catalog.workout.mapper;

import com.imanol.gym.catalog.workout.dto.WorkoutPlanResponse;
import com.imanol.gym.catalog.workout.entity.WorkoutPlan;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
        componentModel = "spring",
        uses = WorkoutPlanDayMapper.class
)
public interface WorkoutPlanMapper {

    @Mapping(target = "clientId", source = "client.id")
    @Mapping(target = "trainerId", source = "trainer.id")
    @Mapping(target = "sourceTemplateId", source = "sourceTemplate.id")
    WorkoutPlanResponse toResponse(WorkoutPlan entity);
}
