package com.imanol.gym.catalog.workout.service;

import com.imanol.gym.catalog.exercise.entity.Exercise;
import com.imanol.gym.catalog.exercise.repository.ExerciseRepository;
import com.imanol.gym.catalog.workout.dto.*;
import com.imanol.gym.catalog.workout.entity.*;
import com.imanol.gym.catalog.workout.repository.*;
import com.imanol.gym.user.entity.TrainerClient;
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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkoutPlanServiceImplTest {

    @Mock
    private WorkoutPlanRepository workoutPlanRepository;
    @Mock
    private WorkoutPlanDayRepository workoutPlanDayRepository;
    @Mock
    private WorkoutPlanExerciseRepository workoutPlanExerciseRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TrainerClientRepository trainerClientRepository;
    @Mock
    private WorkoutTemplateRepository workoutTemplateRepository;
    @Mock
    private WorkoutTemplateExerciseRepository workoutTemplateExerciseRepository;
    @Mock
    private ExerciseRepository exerciseRepository;

    @InjectMocks
    private WorkoutPlanServiceImpl workoutPlanService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldCreateWorkoutPlanDirectly() {
        User trainer = user(1L, UserRole.TRAINER);
        User client = user(2L, UserRole.CLIENT);
        authenticate(trainer);
        when(userRepository.findByEmail("user@test.com"))
                .thenReturn(Optional.of(trainer));
        when(userRepository.findByIdForUpdate(2L))
                .thenReturn(Optional.of(client));
        when(trainerClientRepository.existsByTrainerIdAndClientId(1L, 2L))
                .thenReturn(true);
        when(workoutPlanRepository.findOverlappingPlans(
                eq(2L), eq(WorkoutPlanStatus.ACTIVE),
                nullable(LocalDate.class), nullable(LocalDate.class)))
                .thenReturn(List.of());
        when(exerciseRepository.findById(5L))
                .thenReturn(Optional.of(exercise(5L)));
        when(workoutPlanRepository.save(any(WorkoutPlan.class)))
                .thenAnswer(invocation -> {
                    WorkoutPlan plan = invocation.getArgument(0);
                    plan.setId(10L);
                    return plan;
                });
        when(workoutPlanDayRepository.save(any(WorkoutPlanDay.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(workoutPlanExerciseRepository.save(
                any(WorkoutPlanExercise.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        WorkoutPlan result = workoutPlanService.createForAuthenticatedTrainer(
                2L,
                request(null, 5L, 1, 4, 8, 90)
        );

        assertThat(result.getStatus()).isEqualTo(WorkoutPlanStatus.ACTIVE);
        assertThat(result.getClient()).isEqualTo(client);
        verify(workoutPlanExerciseRepository).save(
                argThat(item -> item.getSets() == 4
                        && item.getRepetitions() == 8
                        && item.getRestSeconds() == 90)
        );
    }

    @Test
    void shouldCopyTemplateValuesAndAllowOverrides() {
        User trainer = user(1L, UserRole.TRAINER);
        User client = user(2L, UserRole.CLIENT);
        WorkoutTemplate template = new WorkoutTemplate();
        template.setId(3L);
        template.setActive(true);
        WorkoutTemplateExercise templateExercise =
                new WorkoutTemplateExercise();
        templateExercise.setId(10L);
        templateExercise.setWorkoutTemplate(template);
        templateExercise.setExercise(exercise(5L));
        templateExercise.setOrderIndex(1);
        templateExercise.setSets(3);
        templateExercise.setRepetitions(10);
        templateExercise.setRestSeconds(60);

        authenticate(trainer);
        when(userRepository.findByEmail("user@test.com"))
                .thenReturn(Optional.of(trainer));
        when(userRepository.findByIdForUpdate(2L))
                .thenReturn(Optional.of(client));
        when(trainerClientRepository.existsByTrainerIdAndClientId(1L, 2L))
                .thenReturn(true);
        when(workoutPlanRepository.findOverlappingPlans(
                eq(2L), eq(WorkoutPlanStatus.ACTIVE),
                nullable(LocalDate.class), nullable(LocalDate.class)))
                .thenReturn(List.of());
        when(workoutTemplateRepository.findById(3L))
                .thenReturn(Optional.of(template));
        when(workoutTemplateExerciseRepository.findById(10L))
                .thenReturn(Optional.of(templateExercise));
        when(exerciseRepository.findById(5L))
                .thenReturn(Optional.of(templateExercise.getExercise()));
        when(workoutPlanRepository.save(any(WorkoutPlan.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(workoutPlanDayRepository.save(any(WorkoutPlanDay.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(workoutPlanExerciseRepository.save(
                any(WorkoutPlanExercise.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        workoutPlanService.createForAuthenticatedTrainer(
                2L,
                request(3L, 5L, 1, 4, null, null)
        );

        verify(workoutPlanExerciseRepository).save(argThat(item ->
                item.getSourceTemplateExerciseId().equals(10L)
                        && item.getSets() == 4
                        && item.getRepetitions() == 10
                        && item.getRestSeconds() == 60
        ));
    }

    @Test
    void shouldRejectOverlappingActivePlans() {
        User trainer = user(1L, UserRole.TRAINER);
        User client = user(2L, UserRole.CLIENT);
        authenticate(trainer);
        when(userRepository.findByEmail("user@test.com"))
                .thenReturn(Optional.of(trainer));
        when(userRepository.findByIdForUpdate(2L))
                .thenReturn(Optional.of(client));
        when(trainerClientRepository.existsByTrainerIdAndClientId(1L, 2L))
                .thenReturn(true);
        WorkoutPlan existing = new WorkoutPlan();
        existing.setId(9L);
        existing.setStartDate(LocalDate.of(2026, 9, 1));
        existing.setEndDate(LocalDate.of(2026, 9, 30));
        when(workoutPlanRepository.findOverlappingPlans(
                eq(2L), eq(WorkoutPlanStatus.ACTIVE),
                nullable(LocalDate.class), nullable(LocalDate.class)))
                .thenReturn(List.of(existing));

        assertThatThrownBy(() -> workoutPlanService
                .createForAuthenticatedTrainer(
                        2L,
                        request(null, 5L, 1, 4, 8, 90)
                ))
                .hasMessage("Active workout plan dates overlap");

        verify(workoutPlanRepository, never()).save(any());
    }

    @Test
    void shouldRejectTrainerWithoutClientRelationship() {
        User trainer = user(1L, UserRole.TRAINER);
        User client = user(2L, UserRole.CLIENT);
        authenticate(trainer);
        when(userRepository.findByEmail("user@test.com"))
                .thenReturn(Optional.of(trainer));
        when(userRepository.findByIdForUpdate(2L))
                .thenReturn(Optional.of(client));
        when(trainerClientRepository.existsByTrainerIdAndClientId(1L, 2L))
                .thenReturn(false);

        assertThatThrownBy(() -> workoutPlanService
                .createForAuthenticatedTrainer(
                        2L,
                        request(null, 5L, 1, 4, 8, 90)
                ))
                .isInstanceOf(
                        org.springframework.security.access.AccessDeniedException.class
                );
    }

    @Test
    void shouldDeactivateAndCompletePlanWithValidTransitions() {
        User trainer = user(1L, UserRole.TRAINER);
        User client = user(2L, UserRole.CLIENT);
        WorkoutPlan plan = plan(10L, trainer, client);
        authenticate(trainer);
        when(userRepository.findByEmail("user@test.com"))
                .thenReturn(Optional.of(trainer));
        when(workoutPlanRepository.findById(10L))
                .thenReturn(Optional.of(plan));
        when(trainerClientRepository.existsByTrainerIdAndClientId(1L, 2L))
                .thenReturn(true);
        when(workoutPlanRepository.save(plan)).thenReturn(plan);

        workoutPlanService.completeForAuthenticatedUser(10L);

        assertThat(plan.getStatus()).isEqualTo(WorkoutPlanStatus.COMPLETED);
        assertThatThrownBy(() -> workoutPlanService
                .deactivateForAuthenticatedUser(10L))
                .hasMessage("Invalid workout plan status transition");
    }

    @Test
    void shouldAllowClientOnlyItsOwnPlan() {
        User client = user(2L, UserRole.CLIENT);
        User otherClient = user(3L, UserRole.CLIENT);
        WorkoutPlan plan = plan(10L, user(1L, UserRole.TRAINER), client);
        authenticate(client);
        when(userRepository.findByEmail("user@test.com"))
                .thenReturn(Optional.of(client));
        when(workoutPlanRepository.findById(10L))
                .thenReturn(Optional.of(plan));

        assertThat(workoutPlanService.findByIdForAuthenticatedUser(10L))
                .isEqualTo(plan);

        plan.setClient(otherClient);
        assertThatThrownBy(() ->
                workoutPlanService.findByIdForAuthenticatedUser(10L))
                .isInstanceOf(
                        org.springframework.security.access.AccessDeniedException.class
                );
    }

    @Test
    void shouldFindClientPlansForMondayWeek() {
        User client = user(2L, UserRole.CLIENT);
        authenticate(client);
        when(userRepository.findByEmail("user@test.com"))
                .thenReturn(Optional.of(client));
        when(workoutPlanRepository.findActivePlansForWeek(
                eq(2L),
                eq(WorkoutPlanStatus.ACTIVE),
                eq(LocalDate.of(2026, 9, 7)),
                eq(LocalDate.of(2026, 9, 13))
        )).thenReturn(List.of());

        assertThat(workoutPlanService.findWeekForAuthenticatedClient(
                LocalDate.of(2026, 9, 7)
        )).isEmpty();
    }

    @Test
    void shouldOnlyReturnPlansOwnedByAuthenticatedTrainer() {
        User trainer = user(1L, UserRole.TRAINER);
        User client = user(2L, UserRole.CLIENT);
        User otherTrainer = user(3L, UserRole.TRAINER);
        authenticate(trainer);
        when(userRepository.findByEmail("user@test.com"))
                .thenReturn(Optional.of(trainer));
        when(userRepository.findById(2L)).thenReturn(Optional.of(client));
        when(trainerClientRepository.existsByTrainerIdAndClientId(1L, 2L))
                .thenReturn(true);
        WorkoutPlan own = plan(10L, trainer, client);
        WorkoutPlan other = plan(11L, otherTrainer, client);
        when(workoutPlanRepository
                .findAllByTrainerIdAndClientIdOrderByStartDateDesc(1L, 2L))
                .thenReturn(List.of(own));

        assertThat(workoutPlanService.findForAuthenticatedTrainer(2L, null))
                .containsExactly(own)
                .doesNotContain(other);
        verify(workoutPlanRepository)
                .findAllByTrainerIdAndClientIdOrderByStartDateDesc(1L, 2L);
    }

    private WorkoutPlanRequest request(
            Long templateId,
            Long exerciseId,
            int day,
            Integer sets,
            Integer repetitions,
            Integer rest
    ) {
        return new WorkoutPlanRequest(
                templateId,
                LocalDate.of(2026, 9, 7),
                null,
                List.of(new WorkoutPlanDayRequest(
                        day,
                        List.of(new WorkoutPlanExerciseRequest(
                                templateId == null ? null : 10L,
                                exerciseId,
                                1,
                                sets,
                                repetitions,
                                rest
                        ))
                ))
        );
    }

    private User user(Long id, UserRole role) {
        User user = new User();
        user.setId(id);
        user.setEmail("user@test.com");
        user.setRole(role);
        user.setActive(true);
        return user;
    }

    private Exercise exercise(Long id) {
        Exercise exercise = new Exercise();
        exercise.setId(id);
        exercise.setName("Exercise " + id);
        exercise.setActive(true);
        return exercise;
    }

    private WorkoutPlan plan(Long id, User trainer, User client) {
        WorkoutPlan plan = new WorkoutPlan();
        plan.setId(id);
        plan.setTrainer(trainer);
        plan.setClient(client);
        plan.setStartDate(LocalDate.of(2026, 9, 7));
        plan.setStatus(WorkoutPlanStatus.ACTIVE);
        return plan;
    }

    private void authenticate(User user) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        user.getEmail(),
                        null,
                        List.of(new SimpleGrantedAuthority(
                                "ROLE_" + user.getRole().name()
                        ))
                )
        );
    }
}
