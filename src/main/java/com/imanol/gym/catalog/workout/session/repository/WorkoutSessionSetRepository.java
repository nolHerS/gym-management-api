package com.imanol.gym.catalog.workout.session.repository;
import com.imanol.gym.catalog.workout.session.entity.WorkoutSessionSet;
import com.imanol.gym.common.repository.BaseRepository;
import java.util.Optional;
public interface WorkoutSessionSetRepository extends BaseRepository<WorkoutSessionSet, Long> {
    Optional<WorkoutSessionSet> findBySessionExerciseIdAndSetNumber(Long exerciseId, Integer setNumber);
}
