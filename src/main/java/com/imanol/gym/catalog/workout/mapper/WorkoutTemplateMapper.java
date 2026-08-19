package com.imanol.gym.catalog.workout.mapper;

import com.imanol.gym.catalog.workout.dto.WorkoutTemplateRequest;
import com.imanol.gym.catalog.workout.dto.WorkoutTemplateResponse;
import com.imanol.gym.catalog.workout.entity.WorkoutTemplate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WorkoutTemplateMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    WorkoutTemplate toEntity(WorkoutTemplateRequest request);

    WorkoutTemplateResponse toResponse(WorkoutTemplate entity);
}