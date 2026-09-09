package com.imanol.gym.catalog.nutrition.service;

import com.imanol.gym.catalog.nutrition.dto.FoodRequest;
import com.imanol.gym.catalog.nutrition.entity.Food;
import com.imanol.gym.catalog.nutrition.repository.FoodRepository;
import com.imanol.gym.common.exception.ResourceAlreadyExistsException;
import com.imanol.gym.common.exception.ResourceNotFoundException;
import com.imanol.gym.user.entity.UserRole;
import com.imanol.gym.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FoodServiceImpl implements FoodService {
    private final FoodRepository foodRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public Food create(FoodRequest request) {
        requireTrainer();
        if (foodRepository.existsByNameIgnoreCase(request.name())) {
            throw new ResourceAlreadyExistsException("Food already exists");
        }
        Food food = new Food();
        copy(food, request);
        food.setActive(true);
        return foodRepository.save(food);
    }

    @Override
    @Transactional
    public Food update(Long id, FoodRequest request) {
        requireTrainer();
        Food food = findById(id);
        if (foodRepository.existsByNameIgnoreCaseAndIdNot(request.name(), id)) {
            throw new ResourceAlreadyExistsException("Food already exists");
        }
        copy(food, request);
        return foodRepository.save(food);
    }

    @Override
    @Transactional
    public List<Food> findAll() {
        UserRole role = authenticatedRole();
        return role == UserRole.TRAINER
                ? foodRepository.findAllByOrderByNameAsc()
                : foodRepository.findAllByActiveTrueOrderByNameAsc();
    }

    @Override
    @Transactional
    public Food findById(Long id) {
        UserRole role = authenticatedRole();
        Food food = foodRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Food not found with id: " + id));
        if (role != UserRole.TRAINER && !Boolean.TRUE.equals(food.getActive())) {
            throw new ResourceNotFoundException("Food not found with id: " + id);
        }
        return food;
    }

    @Override
    @Transactional
    public void activate(Long id) {
        requireTrainer();
        Food food = foodRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Food not found with id: " + id));
        food.setActive(true);
        foodRepository.save(food);
    }

    @Override
    @Transactional
    public void deactivate(Long id) {
        requireTrainer();
        Food food = foodRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Food not found with id: " + id));
        food.setActive(false);
        foodRepository.save(food);
    }

    private void copy(Food food, FoodRequest request) {
        food.setName(request.name());
        food.setDescription(request.description());
        food.setCalories(request.calories());
        food.setProtein(request.protein());
        food.setCarbohydrates(request.carbohydrates());
        food.setFats(request.fats());
        food.setServingSize(request.servingSize());
        food.setServingUnit(request.servingUnit());
    }

    private void requireTrainer() {
        if (authenticatedRole() != UserRole.TRAINER) {
            throw new AccessDeniedException("Trainer role is required");
        }
    }

    private UserRole authenticatedRole() {
        Authentication authentication = SecurityContextHolder.getContext()
                .getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Authentication is required");
        }
        return userRepository.findByEmail(authentication.getName())
                .filter(user -> Boolean.TRUE.equals(user.getActive()))
                .map(user -> user.getRole())
                .orElseThrow(() -> new AccessDeniedException("User is inactive or not found"));
    }
}
