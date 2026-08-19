package com.imanol.gym.catalog.exercise.mapper;

import com.imanol.gym.catalog.exercise.dto.ExerciseResponse;
import com.imanol.gym.catalog.exercise.entity.Exercise;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ExerciseMapper {

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    ExerciseResponse toResponse(Exercise entity);
}