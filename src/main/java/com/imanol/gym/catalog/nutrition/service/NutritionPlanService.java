package com.imanol.gym.catalog.nutrition.service;

import com.imanol.gym.catalog.nutrition.dto.NutritionPlanRequest;
import com.imanol.gym.catalog.nutrition.dto.NutritionPlanUpdateRequest;
import com.imanol.gym.catalog.nutrition.entity.NutritionPlan;
import com.imanol.gym.catalog.nutrition.entity.NutritionPlanStatus;

import java.util.List;

public interface NutritionPlanService {
    NutritionPlan createForAuthenticatedTrainer(Long clientId,
                                                  NutritionPlanRequest request);
    List<NutritionPlan> findForAuthenticatedTrainer(Long clientId,
                                                     NutritionPlanStatus status);
    List<NutritionPlan> findForAuthenticatedClient();
    List<NutritionPlan> findActiveForAuthenticatedClient();
    NutritionPlan findByIdForAuthenticatedUser(Long id);
    NutritionPlan updateForAuthenticatedTrainer(Long id,
                                                 NutritionPlanUpdateRequest request);
    void deactivateForAuthenticatedTrainer(Long id);
    void completeForAuthenticatedTrainer(Long id);
}
