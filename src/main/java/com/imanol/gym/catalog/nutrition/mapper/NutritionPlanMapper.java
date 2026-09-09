package com.imanol.gym.catalog.nutrition.mapper;

import com.imanol.gym.catalog.nutrition.dto.*;
import com.imanol.gym.catalog.nutrition.entity.*;
import org.springframework.stereotype.Component;

@Component
public class NutritionPlanMapper {
    private final FoodMapper foodMapper;

    public NutritionPlanMapper(FoodMapper foodMapper) {
        this.foodMapper = foodMapper;
    }

    public NutritionPlanResponse toResponse(NutritionPlan plan) {
        return new NutritionPlanResponse(plan.getId(), plan.getName(), plan.getDescription(),
                plan.getClient().getId(),
                plan.getTrainer().getId(), plan.getStartDate(), plan.getEndDate(),
                plan.getStatus(), plan.getMeals().stream().map(this::toMeal).toList(),
                plan.getCreatedAt(), plan.getUpdatedAt());
    }

    private NutritionPlanMealResponse toMeal(NutritionPlanMeal meal) {
        return new NutritionPlanMealResponse(meal.getId(), meal.getName(),
                meal.getDescription(), meal.getOrderIndex(),
                meal.getFoods().stream().map(food -> new NutritionPlanFoodResponse(
                        food.getId(), foodMapper.toResponse(food.getFood()),
                        food.getQuantity(), food.getUnit(), food.getOrderIndex())).toList());
    }
}
