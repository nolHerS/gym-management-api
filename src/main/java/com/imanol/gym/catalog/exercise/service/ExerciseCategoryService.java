package com.imanol.gym.catalog.exercise.service;

import com.imanol.gym.catalog.exercise.entity.ExerciseCategory;

import java.util.List;


public interface ExerciseCategoryService {

    ExerciseCategory create(ExerciseCategory exerciseCategory);

    List<ExerciseCategory> findAll();

    ExerciseCategory findById(Long id);

    ExerciseCategory update(Long id, ExerciseCategory exerciseCategory);

    void activate(Long id);

    void deactivate(Long id);
}
