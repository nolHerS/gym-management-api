package com.imanol.gym.catalog.exercise.mapper;

import com.imanol.gym.catalog.exercise.dto.ExerciseCategoryRequest;
import com.imanol.gym.catalog.exercise.dto.ExerciseCategoryResponse;
import com.imanol.gym.catalog.exercise.entity.ExerciseCategory;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ExerciseCategoryMapper {

    ExerciseCategory toEntity(ExerciseCategoryRequest request);

    ExerciseCategoryResponse toResponse(ExerciseCategory entity);
}