package com.imanol.gym.catalog.nutrition.service;

import com.imanol.gym.catalog.nutrition.dto.*;
import com.imanol.gym.catalog.nutrition.entity.*;
import com.imanol.gym.catalog.nutrition.repository.*;
import com.imanol.gym.user.entity.User;
import com.imanol.gym.user.entity.UserRole;
import com.imanol.gym.user.repository.TrainerClientRepository;
import com.imanol.gym.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NutritionPlanServiceImplTest {

    @Mock private NutritionPlanRepository planRepository;
    @Mock private NutritionPlanMealRepository mealRepository;
    @Mock private NutritionPlanFoodRepository planFoodRepository;
    @Mock private FoodRepository foodRepository;
    @Mock private UserRepository userRepository;
    @Mock private TrainerClientRepository trainerClientRepository;

    @InjectMocks private NutritionPlanServiceImpl service;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldCreateNestedNutritionPlan() {
        User trainer = user(1L, UserRole.TRAINER);
        User client = user(2L, UserRole.CLIENT);
        Food food = new Food();
        food.setId(3L);
        food.setActive(true);
        authenticate(trainer);
        when(userRepository.findByEmail(trainer.getEmail())).thenReturn(Optional.of(trainer));
        when(userRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(client));
        when(trainerClientRepository.existsByTrainerIdAndClientId(1L, 2L)).thenReturn(true);
        when(planRepository.findAllByClientIdAndStatusOrderByStartDateDesc(
                2L, NutritionPlanStatus.ACTIVE)).thenReturn(List.of());
        when(foodRepository.findById(3L)).thenReturn(Optional.of(food));
        when(planRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(mealRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(planFoodRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        NutritionPlan result = service.createForAuthenticatedTrainer(2L, request());

        assertThat(result.getStatus()).isEqualTo(NutritionPlanStatus.ACTIVE);
        assertThat(result.getName()).isEqualTo("Plan definición");
        assertThat(result.getMeals()).hasSize(1);
        assertThat(result.getMeals().getFirst().getFoods()).hasSize(1);
    }

    @Test
    void shouldRejectOverlappingActivePlan() {
        User trainer = user(1L, UserRole.TRAINER);
        User client = user(2L, UserRole.CLIENT);
        NutritionPlan existing = new NutritionPlan();
        existing.setId(9L);
        existing.setStartDate(LocalDate.of(2026, 9, 1));
        existing.setEndDate(LocalDate.of(2026, 9, 30));
        authenticate(trainer);
        when(userRepository.findByEmail(trainer.getEmail())).thenReturn(Optional.of(trainer));
        when(userRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(client));
        when(trainerClientRepository.existsByTrainerIdAndClientId(1L, 2L)).thenReturn(true);
        when(planRepository.findAllByClientIdAndStatusOrderByStartDateDesc(
                2L, NutritionPlanStatus.ACTIVE)).thenReturn(List.of(existing));

        assertThatThrownBy(() -> service.createForAuthenticatedTrainer(2L, request()))
                .hasMessage("Active nutrition plan dates overlap");
        verify(planRepository, never()).save(any());
    }

    @Test
    void shouldRejectClientWithoutTrainerRelationship() {
        User trainer = user(1L, UserRole.TRAINER);
        User client = user(2L, UserRole.CLIENT);
        authenticate(trainer);
        when(userRepository.findByEmail(trainer.getEmail())).thenReturn(Optional.of(trainer));
        when(userRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(client));
        when(trainerClientRepository.existsByTrainerIdAndClientId(1L, 2L)).thenReturn(false);

        assertThatThrownBy(() -> service.createForAuthenticatedTrainer(2L, request()))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
    }

    private NutritionPlanRequest request() {
        return new NutritionPlanRequest(
                "Plan definición",
                "Plan inicial",
                LocalDate.of(2026, 9, 7),
                null,
                List.of(new NutritionPlanMealRequest(
                        "Desayuno",
                        "Primera comida",
                        1,
                        List.of(new NutritionPlanFoodRequest(
                                3L, BigDecimal.valueOf(100), "g", 1)))));
    }

    private User user(Long id, UserRole role) {
        User user = new User();
        user.setId(id);
        user.setEmail("user@test.com");
        user.setRole(role);
        user.setActive(true);
        return user;
    }

    private void authenticate(User user) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        user.getEmail(),
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))));
    }
}
