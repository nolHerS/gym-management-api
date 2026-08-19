package com.imanol.gym.catalog.exercise.repository;

import com.imanol.gym.catalog.exercise.entity.Exercise;
import com.imanol.gym.common.repository.BaseRepository;

import java.util.List;

public interface ExerciseRepository
        extends BaseRepository<Exercise, Long> {

    List<Exercise> findAllByActiveTrue();

    List<Exercise> findAllByCategoryIdAndActiveTrue(Long categoryId);
}