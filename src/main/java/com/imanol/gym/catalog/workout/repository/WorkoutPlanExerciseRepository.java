package com.imanol.gym.catalog.workout.repository;

import com.imanol.gym.catalog.workout.entity.WorkoutPlanExercise;
import com.imanol.gym.common.repository.BaseRepository;

import java.util.List;

public interface WorkoutPlanExerciseRepository
        extends BaseRepository<WorkoutPlanExercise, Long> {

    List<WorkoutPlanExercise> findAllByWorkoutPlanDayIdOrderByOrderIndexAsc(
            Long workoutPlanDayId
    );
}
