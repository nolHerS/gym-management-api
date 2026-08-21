package com.imanol.gym.catalog.workout.repository;

import com.imanol.gym.catalog.workout.entity.WorkoutTemplateExercise;
import com.imanol.gym.common.repository.BaseRepository;

import java.util.List;

public interface WorkoutTemplateExerciseRepository
        extends BaseRepository<WorkoutTemplateExercise, Long> {

    List<WorkoutTemplateExercise>
    findAllByWorkoutTemplateIdOrderByOrderIndexAsc(Long workoutTemplateId);
}