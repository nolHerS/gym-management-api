package com.imanol.gym.catalog.workout.repository;

import com.imanol.gym.catalog.workout.entity.WorkoutTemplate;
import com.imanol.gym.common.repository.BaseRepository;

import java.util.List;

public interface WorkoutTemplateRepository
        extends BaseRepository<WorkoutTemplate, Long> {

    List<WorkoutTemplate> findAllByActiveTrue();
}