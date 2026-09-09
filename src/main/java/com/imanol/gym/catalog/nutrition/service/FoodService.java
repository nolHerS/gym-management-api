package com.imanol.gym.catalog.nutrition.service;

import com.imanol.gym.catalog.nutrition.dto.FoodRequest;
import com.imanol.gym.catalog.nutrition.entity.Food;

import java.util.List;

public interface FoodService {
    Food create(FoodRequest request);
    Food update(Long id, FoodRequest request);
    List<Food> findAll();
    Food findById(Long id);
    void activate(Long id);
    void deactivate(Long id);
}
