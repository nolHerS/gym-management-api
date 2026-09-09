package com.imanol.gym.catalog.nutrition.repository;

import com.imanol.gym.catalog.nutrition.entity.NutritionPlanFood;
import com.imanol.gym.common.repository.BaseRepository;

import java.util.List;

public interface NutritionPlanFoodRepository
        extends BaseRepository<NutritionPlanFood, Long> {
    List<NutritionPlanFood> findAllByNutritionPlanMealId(Long mealId);
}
