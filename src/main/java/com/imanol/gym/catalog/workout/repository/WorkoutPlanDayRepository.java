package com.imanol.gym.catalog.workout.repository;

import com.imanol.gym.catalog.workout.entity.WorkoutPlanDay;
import com.imanol.gym.common.repository.BaseRepository;

import java.util.List;
import java.util.Optional;

public interface WorkoutPlanDayRepository
        extends BaseRepository<WorkoutPlanDay, Long> {

    List<WorkoutPlanDay> findAllByWorkoutPlanIdOrderByDayOfWeekAsc(
            Long workoutPlanId
    );

    Optional<WorkoutPlanDay> findByWorkoutPlanIdAndDayOfWeek(
            Long workoutPlanId,
            Integer dayOfWeek
    );
}
