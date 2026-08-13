package com.imanol.gym.catalog.exercise.service;

import com.imanol.gym.catalog.exercise.entity.ExerciseCategory;
import com.imanol.gym.common.service.BaseService;

import java.util.List;


public interface ExerciseCategoryService
        extends BaseService<ExerciseCategory, Long> {

    List<ExerciseCategory> findAll();

    void activate(Long id);

    void deactivate(Long id);

    ExerciseCategory update(Long id, ExerciseCategory entity);
}
