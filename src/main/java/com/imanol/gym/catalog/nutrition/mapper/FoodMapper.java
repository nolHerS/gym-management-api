package com.imanol.gym.catalog.nutrition.mapper;

import com.imanol.gym.catalog.nutrition.dto.FoodResponse;
import com.imanol.gym.catalog.nutrition.entity.Food;
import org.springframework.stereotype.Component;

@Component
public class FoodMapper {
    public FoodResponse toResponse(Food food) {
        return new FoodResponse(food.getId(), food.getName(), food.getDescription(),
                food.getCalories(), food.getProtein(),
                food.getCarbohydrates(), food.getFats(),
                food.getServingSize(), food.getServingUnit(), food.getActive(),
                food.getCreatedAt(), food.getUpdatedAt());
    }
}
