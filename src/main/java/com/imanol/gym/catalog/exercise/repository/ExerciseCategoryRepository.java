package com.imanol.gym.catalog.exercise.repository;

import com.imanol.gym.catalog.exercise.entity.ExerciseCategory;
import com.imanol.gym.common.repository.BaseRepository;

import java.util.List;

public interface ExerciseCategoryRepository
        extends BaseRepository<ExerciseCategory, Long> {

    List<ExerciseCategory> findAllByActiveTrue();
}