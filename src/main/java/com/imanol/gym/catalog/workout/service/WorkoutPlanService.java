package com.imanol.gym.catalog.workout.service;

import com.imanol.gym.catalog.workout.dto.WorkoutPlanDayRequest;
import com.imanol.gym.catalog.workout.dto.WorkoutPlanRequest;
import com.imanol.gym.catalog.workout.dto.WorkoutPlanUpdateRequest;
import com.imanol.gym.catalog.workout.entity.WorkoutPlan;
import com.imanol.gym.catalog.workout.entity.WorkoutPlanDay;
import com.imanol.gym.catalog.workout.entity.WorkoutPlanExercise;
import com.imanol.gym.catalog.workout.entity.WorkoutPlanStatus;

import java.time.LocalDate;
import java.util.List;

public interface WorkoutPlanService {

    WorkoutPlan createForAuthenticatedTrainer(
            Long clientId,
            WorkoutPlanRequest request
    );

    List<WorkoutPlan> findForAuthenticatedTrainer(
            Long clientId,
            WorkoutPlanStatus status
    );

    List<WorkoutPlan> findForAuthenticatedClient();

    WorkoutPlan findByIdForAuthenticatedUser(Long id);

    List<WorkoutPlan> findWeekForAuthenticatedClient(LocalDate weekStart);

    WorkoutPlan updateForAuthenticatedUser(
            Long id,
            WorkoutPlanUpdateRequest request
    );

    WorkoutPlanDay addDayForAuthenticatedTrainer(
            Long planId,
            WorkoutPlanDayRequest request
    );

    void deleteDayForAuthenticatedTrainer(Long planId, Integer dayOfWeek);

    WorkoutPlanExercise addExerciseForAuthenticatedTrainer(
            Long planId,
            Integer dayOfWeek,
            com.imanol.gym.catalog.workout.dto.WorkoutPlanExerciseRequest request
    );

    WorkoutPlanExercise updateExerciseForAuthenticatedUser(
            Long exerciseId,
            com.imanol.gym.catalog.workout.dto.WorkoutPlanExerciseRequest request
    );

    void deleteExerciseForAuthenticatedUser(Long exerciseId);

    void deactivateForAuthenticatedUser(Long id);

    void completeForAuthenticatedUser(Long id);
}
