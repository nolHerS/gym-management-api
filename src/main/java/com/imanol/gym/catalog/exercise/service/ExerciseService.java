package com.imanol.gym.catalog.exercise.service;

import com.imanol.gym.catalog.exercise.dto.ExerciseRequest;
import com.imanol.gym.catalog.exercise.entity.Exercise;
import com.imanol.gym.common.service.BaseService;

import java.util.List;

public interface ExerciseService
        extends BaseService<Exercise, Long> {

    Exercise create(ExerciseRequest request);

    Exercise update(Long id, ExerciseRequest request);

    List<Exercise> findAll();

    List<Exercise> findAllByCategory(Long categoryId);

    void activate(Long id);

    void deactivate(Long id);
}