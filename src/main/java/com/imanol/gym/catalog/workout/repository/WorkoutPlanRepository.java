package com.imanol.gym.catalog.workout.repository;

import com.imanol.gym.catalog.workout.entity.WorkoutPlan;
import com.imanol.gym.catalog.workout.entity.WorkoutPlanStatus;
import com.imanol.gym.common.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.time.LocalDate;
import java.util.List;

public interface WorkoutPlanRepository
        extends BaseRepository<WorkoutPlan, Long> {

    List<WorkoutPlan> findAllByClientIdOrderByStartDateDesc(Long clientId);

    List<WorkoutPlan> findAllByClientIdAndStatusOrderByStartDateDesc(
            Long clientId,
            WorkoutPlanStatus status
    );

    List<WorkoutPlan> findAllByTrainerIdOrderByStartDateDesc(Long trainerId);

    List<WorkoutPlan> findAllByTrainerIdAndClientIdOrderByStartDateDesc(
            Long trainerId,
            Long clientId
    );

    List<WorkoutPlan> findAllByTrainerIdAndClientIdAndStatusOrderByStartDateDesc(
            Long trainerId,
            Long clientId,
            WorkoutPlanStatus status
    );

    @Query("""
            select p from WorkoutPlan p
            where p.client.id = :clientId
              and p.status = :status
              and p.startDate <= :weekEnd
              and (p.endDate is null or p.endDate >= :weekStart)
            order by p.startDate desc
            """)
    List<WorkoutPlan> findActivePlansForWeek(
            @Param("clientId") Long clientId,
            @Param("status") WorkoutPlanStatus status,
            @Param("weekStart") LocalDate weekStart,
            @Param("weekEnd") LocalDate weekEnd
    );

    @Query("""
            select p from WorkoutPlan p
            where p.client.id = :clientId
              and p.status = :status
              and (:candidateEnd is null or p.startDate <= :candidateEnd)
              and (p.endDate is null or p.endDate >= :candidateStart)
            """)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<WorkoutPlan> findOverlappingPlans(
            @Param("clientId") Long clientId,
            @Param("status") WorkoutPlanStatus status,
            @Param("candidateStart") LocalDate candidateStart,
            @Param("candidateEnd") LocalDate candidateEnd
    );
}
