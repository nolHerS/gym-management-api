package com.imanol.gym.catalog.workout.session.repository;
import com.imanol.gym.catalog.workout.session.entity.WorkoutSessionExercise;
import com.imanol.gym.common.repository.BaseRepository;
import java.util.Optional;
public interface WorkoutSessionExerciseRepository extends BaseRepository<WorkoutSessionExercise, Long> {
    Optional<WorkoutSessionExercise> findByIdAndSessionId(Long id, Long sessionId);
}
