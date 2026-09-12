package com.imanol.gym.catalog.workout.session.repository;
import com.imanol.gym.catalog.workout.session.entity.*;
import com.imanol.gym.common.repository.BaseRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime; import java.util.List; import java.util.Optional;
public interface WorkoutSessionRepository extends BaseRepository<WorkoutSession, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from WorkoutSession s where s.client.id=:clientId and s.workoutPlan.id=:planId and s.status=:status")
    List<WorkoutSession> findLocked(@Param("clientId") Long clientId, @Param("planId") Long planId,
                                    @Param("status") WorkoutSessionStatus status);
    @EntityGraph(attributePaths={"workoutPlan","client","exercises","exercises.exercise"})
    List<WorkoutSession> findAllByClientIdOrderByStartedAtDesc(Long clientId);
    @EntityGraph(attributePaths={"workoutPlan","client","exercises","exercises.exercise"})
    List<WorkoutSession> findAllByClientIdAndStatusOrderByStartedAtDesc(Long clientId, WorkoutSessionStatus status);
    @EntityGraph(attributePaths={"workoutPlan","client","exercises","exercises.exercise"})
    @Query("select s from WorkoutSession s where s.client.id=:clientId and (:status is null or s.status=:status) and s.startedAt>=coalesce(:from,s.startedAt) and s.startedAt<=coalesce(:to,s.startedAt) order by s.startedAt desc")
    List<WorkoutSession> search(@Param("clientId") Long clientId, @Param("status") WorkoutSessionStatus status,
                                @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
    @EntityGraph(attributePaths={"workoutPlan","client","exercises","exercises.exercise"})
    @Query("select distinct s from WorkoutSession s where s.client.id in :clientIds and (:status is null or s.status=:status) and s.startedAt>=coalesce(:from,s.startedAt) and s.startedAt<=coalesce(:to,s.startedAt) order by s.startedAt desc")
    List<WorkoutSession> searchForClients(@Param("clientIds") List<Long> clientIds,
                                           @Param("status") WorkoutSessionStatus status,
                                           @Param("from") LocalDateTime from,
                                           @Param("to") LocalDateTime to);
    @EntityGraph(attributePaths={"workoutPlan","client","exercises","exercises.exercise"})
    Optional<WorkoutSession> findDetailedById(Long id);
}
