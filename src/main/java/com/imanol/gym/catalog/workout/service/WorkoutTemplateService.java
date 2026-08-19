package com.imanol.gym.catalog.workout.service;

import com.imanol.gym.catalog.workout.entity.WorkoutTemplate;
import com.imanol.gym.common.service.BaseService;

import java.util.List;

public interface WorkoutTemplateService
        extends BaseService<WorkoutTemplate, Long> {

    List<WorkoutTemplate> findAll();

    void activate(Long id);

    void deactivate(Long id);

    WorkoutTemplate update(Long id, WorkoutTemplate entity);
}