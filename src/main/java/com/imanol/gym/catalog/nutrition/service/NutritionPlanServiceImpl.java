package com.imanol.gym.catalog.nutrition.service;

import com.imanol.gym.catalog.nutrition.dto.*;
import com.imanol.gym.catalog.nutrition.entity.*;
import com.imanol.gym.catalog.nutrition.repository.*;
import com.imanol.gym.common.exception.ResourceAlreadyExistsException;
import com.imanol.gym.common.exception.ResourceNotFoundException;
import com.imanol.gym.user.entity.User;
import com.imanol.gym.user.entity.UserRole;
import com.imanol.gym.user.repository.TrainerClientRepository;
import com.imanol.gym.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NutritionPlanServiceImpl implements NutritionPlanService {
    private final NutritionPlanRepository nutritionPlanRepository;
    private final NutritionPlanMealRepository mealRepository;
    private final NutritionPlanFoodRepository planFoodRepository;
    private final FoodRepository foodRepository;
    private final UserRepository userRepository;
    private final TrainerClientRepository trainerClientRepository;

    @Override
    @Transactional
    public NutritionPlan createForAuthenticatedTrainer(Long clientId,
                                                        NutritionPlanRequest request) {
        User trainer = authenticatedUser(UserRole.TRAINER);
        User client = findClientForUpdate(clientId);
        requireRelationship(trainer, client);
        validateDates(request.startDate(), request.endDate());
        validateMeals(request.meals());
        validateNoOverlap(clientId, request.startDate(), request.endDate(), null);

        NutritionPlan plan = new NutritionPlan();
        plan.setName(request.name());
        plan.setDescription(request.description());
        plan.setTrainer(trainer);
        plan.setClient(client);
        plan.setStartDate(request.startDate());
        plan.setEndDate(request.endDate());
        plan.setStatus(NutritionPlanStatus.ACTIVE);
        NutritionPlan saved = nutritionPlanRepository.save(plan);
        saveMeals(saved, request.meals());
        return saved;
    }

    @Override
    @Transactional
    public List<NutritionPlan> findForAuthenticatedTrainer(Long clientId,
                                                            NutritionPlanStatus status) {
        User trainer = authenticatedUser(UserRole.TRAINER);
        User client = findClient(clientId);
        requireRelationship(trainer, client);
        if (status == null) {
            return nutritionPlanRepository
                    .findAllByTrainerIdAndClientIdOrderByStartDateDesc(
                            trainer.getId(), clientId);
        }
        return nutritionPlanRepository
                .findAllByTrainerIdAndClientIdAndStatusOrderByStartDateDesc(
                        trainer.getId(), clientId, status);
    }

    @Override
    @Transactional
    public List<NutritionPlan> findForAuthenticatedClient() {
        User client = authenticatedUser(UserRole.CLIENT);
        return nutritionPlanRepository.findAllByClientIdOrderByStartDateDesc(
                client.getId());
    }

    @Override
    @Transactional
    public List<NutritionPlan> findActiveForAuthenticatedClient() {
        User client = authenticatedUser(UserRole.CLIENT);
        return nutritionPlanRepository
                .findAllByClientIdAndStatusOrderByStartDateDesc(
                        client.getId(), NutritionPlanStatus.ACTIVE);
    }

    @Override
    @Transactional
    public NutritionPlan findByIdForAuthenticatedUser(Long id) {
        NutritionPlan plan = findPlan(id);
        User user = authenticatedUser(null);
        if (user.getRole() == UserRole.CLIENT) {
            if (!user.getId().equals(plan.getClient().getId())) {
                throw new ResourceNotFoundException("Nutrition plan not found with id: " + id);
            }
        } else if (user.getRole() == UserRole.TRAINER) {
            authorizeTrainerPlan(plan, user);
        } else {
            throw new AccessDeniedException("User is not allowed");
        }
        return plan;
    }

    @Override
    @Transactional
    public NutritionPlan updateForAuthenticatedTrainer(Long id,
                                                        NutritionPlanUpdateRequest request) {
        NutritionPlan plan = findPlan(id);
        authorizeTrainerPlan(plan, authenticatedUser(UserRole.TRAINER));
        if (request.name() != null) {
            plan.setName(request.name());
        }
        if (request.description() != null) {
            plan.setDescription(request.description());
        }
        if (request.startDate() != null) {
            plan.setStartDate(request.startDate());
        }
        if (request.endDate() != null) {
            plan.setEndDate(request.endDate());
        }
        validateDates(plan.getStartDate(), plan.getEndDate());
        if (request.status() != null && request.status() != plan.getStatus()) {
            validateTransition(plan.getStatus(), request.status());
            plan.setStatus(request.status());
        }
        if (plan.getStatus() == NutritionPlanStatus.ACTIVE) {
            userRepository.findByIdForUpdate(plan.getClient().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Client not found"));
            validateNoOverlap(plan.getClient().getId(), plan.getStartDate(),
                    plan.getEndDate(), plan.getId());
        }
        if (request.meals() != null) {
            validateMeals(request.meals());
            deleteMeals(plan);
            saveMeals(plan, request.meals());
        }
        return nutritionPlanRepository.save(plan);
    }

    @Override
    @Transactional
    public void deactivateForAuthenticatedTrainer(Long id) {
        changeStatus(id, NutritionPlanStatus.INACTIVE);
    }

    @Override
    @Transactional
    public void completeForAuthenticatedTrainer(Long id) {
        changeStatus(id, NutritionPlanStatus.COMPLETED);
    }

    private void changeStatus(Long id, NutritionPlanStatus status) {
        NutritionPlan plan = findPlan(id);
        authorizeTrainerPlan(plan, authenticatedUser(UserRole.TRAINER));
        validateTransition(plan.getStatus(), status);
        plan.setStatus(status);
        nutritionPlanRepository.save(plan);
    }

    private void saveMeals(NutritionPlan plan,
                           List<NutritionPlanMealRequest> requests) {
        Map<Long, Food> foodsById = foodRepository.findAllById(
                        requests.stream()
                                .flatMap(request -> request.foods().stream())
                                .map(NutritionPlanFoodRequest::foodId)
                                .collect(Collectors.toSet()))
                .stream()
                .collect(Collectors.toMap(Food::getId, Function.identity()));

        for (NutritionPlanMealRequest request : requests) {
            NutritionPlanMeal meal = new NutritionPlanMeal();
            meal.setNutritionPlan(plan);
            meal.setName(request.name());
            meal.setOrderIndex(request.orderIndex());
            meal.setDescription(request.description());
            NutritionPlanMeal savedMeal = mealRepository.save(meal);
            plan.getMeals().add(savedMeal);
            for (NutritionPlanFoodRequest foodRequest : request.foods()) {
                Food food = foodsById.get(foodRequest.foodId());
                if (food == null) {
                    throw new ResourceNotFoundException(
                            "Food not found with id: " + foodRequest.foodId());
                }
                if (!Boolean.TRUE.equals(food.getActive())) {
                    throw new IllegalArgumentException("Food is inactive");
                }
                NutritionPlanFood planFood = new NutritionPlanFood();
                planFood.setNutritionPlanMeal(savedMeal);
                planFood.setFood(food);
                planFood.setQuantity(foodRequest.quantity());
                planFood.setUnit(foodRequest.unit());
                planFood.setOrderIndex(foodRequest.orderIndex());
                planFoodRepository.save(planFood);
                savedMeal.getFoods().add(planFood);
            }
        }
    }

    private void deleteMeals(NutritionPlan plan) {
        List<NutritionPlanMeal> meals = plan.getMeals();
        List<Long> mealIds = meals.stream()
                .map(NutritionPlanMeal::getId)
                .toList();
        if (!mealIds.isEmpty()) {
            planFoodRepository.deleteAll(
                    planFoodRepository.findAllByNutritionPlanMealIdIn(mealIds));
        }
        mealRepository.deleteAll(meals);
        meals.clear();
    }

    private void validateNoOverlap(Long clientId, LocalDate start, LocalDate end,
                                   Long excludedId) {
        boolean overlaps = nutritionPlanRepository.findOverlappingPlans(
                clientId,
                NutritionPlanStatus.ACTIVE,
                start,
                end
        ).stream().anyMatch(plan -> !plan.getId().equals(excludedId));
        if (overlaps) {
            throw new ResourceAlreadyExistsException(
                    "Active nutrition plan dates overlap");
        }
    }

    private void validateMeals(List<NutritionPlanMealRequest> meals) {
        Set<Integer> orders = new HashSet<>();
        Set<String> names = new HashSet<>();
        for (NutritionPlanMealRequest meal : meals) {
            if (!orders.add(meal.orderIndex())) {
                throw new ResourceAlreadyExistsException("Meal order is duplicated");
            }
            if (!names.add(meal.name().toLowerCase())) {
                throw new ResourceAlreadyExistsException("Meal name is duplicated");
            }
            Set<Long> foodIds = new HashSet<>();
            Set<Integer> foodOrders = new HashSet<>();
            for (NutritionPlanFoodRequest food : meal.foods()) {
                if (!foodIds.add(food.foodId())) {
                    throw new ResourceAlreadyExistsException(
                            "Food is duplicated in nutrition plan meal");
                }
                if (!foodOrders.add(food.orderIndex())) {
                    throw new ResourceAlreadyExistsException(
                                "Food order is duplicated in nutrition plan meal");
                }
            }
        }
    }

    private void validateDates(LocalDate start, LocalDate end) {
        if (start == null) {
            throw new IllegalArgumentException("startDate is required");
        }
        if (end != null && end.isBefore(start)) {
            throw new IllegalArgumentException(
                    "endDate must be greater than or equal to startDate");
        }
    }

    private void validateTransition(NutritionPlanStatus current,
                                    NutritionPlanStatus requested) {
        if (current == requested) {
            return;
        }
        if (current != NutritionPlanStatus.ACTIVE
                || (requested != NutritionPlanStatus.INACTIVE
                && requested != NutritionPlanStatus.COMPLETED)) {
            throw new IllegalArgumentException(
                    "Invalid nutrition plan status transition");
        }
    }

    private NutritionPlan findPlan(Long id) {
        return nutritionPlanRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException(
                        "Nutrition plan not found with id: " + id));
    }

    private User findClient(Long id) {
        User client = userRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Client not found with id: " + id));
        validateClient(client);
        return client;
    }

    private User findClientForUpdate(Long id) {
        User client = userRepository.findByIdForUpdate(id).orElseThrow(() ->
                new ResourceNotFoundException("Client not found with id: " + id));
        validateClient(client);
        return client;
    }

    private void validateClient(User client) {
        if (client.getRole() != UserRole.CLIENT) {
            throw new IllegalArgumentException("User is not a client");
        }
        if (!Boolean.TRUE.equals(client.getActive())) {
            throw new AccessDeniedException("Client is inactive");
        }
    }

    private void requireRelationship(User trainer, User client) {
        if (!trainerClientRepository.existsByTrainerIdAndClientId(
                trainer.getId(), client.getId())) {
            throw new AccessDeniedException("Trainer is not related to this client");
        }
    }

    private void authorizeTrainerPlan(NutritionPlan plan, User trainer) {
        if (!trainer.getId().equals(plan.getTrainer().getId())) {
            throw new ResourceNotFoundException(
                    "Nutrition plan not found with id: " + plan.getId());
        }
        requireRelationship(trainer, plan.getClient());
    }

    private User authenticatedUser(UserRole requiredRole) {
        Authentication authentication = SecurityContextHolder.getContext()
                .getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Authentication is required");
        }
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Authenticated user not found"));
        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new AccessDeniedException("User is inactive");
        }
        if (requiredRole != null && user.getRole() != requiredRole) {
            throw new AccessDeniedException("User role is not allowed");
        }
        return user;
    }
}
