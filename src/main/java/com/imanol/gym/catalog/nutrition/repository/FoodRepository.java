package com.imanol.gym.catalog.nutrition.repository;

import com.imanol.gym.catalog.nutrition.entity.Food;
import com.imanol.gym.common.repository.BaseRepository;

import java.util.List;

public interface FoodRepository extends BaseRepository<Food, Long> {
    List<Food> findAllByActiveTrueOrderByNameAsc();
    List<Food> findAllByOrderByNameAsc();
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
