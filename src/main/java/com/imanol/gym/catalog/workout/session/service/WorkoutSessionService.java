package com.imanol.gym.catalog.workout.session.service;
import com.imanol.gym.catalog.workout.session.dto.*;
import com.imanol.gym.catalog.workout.session.entity.*;
import java.time.LocalDate; import java.time.LocalDateTime; import java.util.List;
public interface WorkoutSessionService {
    WorkoutSession create(WorkoutSessionCreateRequest request);
    List<WorkoutSession> search(WorkoutSessionStatus status, LocalDate from, LocalDate to);
    WorkoutSession find(Long id);
    WorkoutSession update(Long id, WorkoutSessionUpdateRequest request);
    WorkoutSessionSet saveSet(Long id, WorkoutSessionSetRequest request);
    WorkoutSessionExercise updateExercise(Long sessionId, Long exerciseId, WorkoutSessionExerciseUpdateRequest request);
    WorkoutSession finish(Long id); WorkoutSession cancel(Long id);
}
