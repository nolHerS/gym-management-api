package com.imanol.gym.catalog.nutrition.repository;

import com.imanol.gym.catalog.nutrition.entity.NutritionPlan;
import com.imanol.gym.catalog.nutrition.entity.NutritionPlanStatus;
import com.imanol.gym.common.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface NutritionPlanRepository
        extends BaseRepository<NutritionPlan, Long> {
    List<NutritionPlan> findAllByClientIdOrderByStartDateDesc(Long clientId);
    List<NutritionPlan> findAllByClientIdAndStatusOrderByStartDateDesc(
            Long clientId, NutritionPlanStatus status);
    List<NutritionPlan> findAllByTrainerIdAndClientIdOrderByStartDateDesc(
            Long trainerId, Long clientId);

    @Query("""
            select p from NutritionPlan p
            where p.client.id = :clientId
              and p.status = :status
              and p.startDate <= :candidateEnd
              and (p.endDate is null or p.endDate >= :candidateStart)
            """)
    List<NutritionPlan> findOverlappingPlans(
            @Param("clientId") Long clientId,
            @Param("status") NutritionPlanStatus status,
            @Param("candidateStart") LocalDate candidateStart,
            @Param("candidateEnd") LocalDate candidateEnd);
}
