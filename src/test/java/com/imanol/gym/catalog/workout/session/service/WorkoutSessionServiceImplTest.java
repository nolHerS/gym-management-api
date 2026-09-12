package com.imanol.gym.catalog.workout.session.service;

import com.imanol.gym.catalog.exercise.entity.Exercise;
import com.imanol.gym.catalog.workout.entity.*;
import com.imanol.gym.catalog.workout.repository.WorkoutPlanRepository;
import com.imanol.gym.catalog.workout.session.dto.*;
import com.imanol.gym.catalog.workout.session.entity.*;
import com.imanol.gym.catalog.workout.session.repository.*;
import com.imanol.gym.common.exception.ResourceConflictException;
import com.imanol.gym.common.exception.ResourceNotFoundException;
import com.imanol.gym.user.entity.User;
import com.imanol.gym.user.entity.UserRole;
import com.imanol.gym.user.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class WorkoutSessionServiceImplTest {
    @Mock WorkoutSessionRepository sessions;
    @Mock WorkoutSessionExerciseRepository sessionExercises;
    @Mock WorkoutSessionSetRepository sessionSets;
    @Mock WorkoutPlanRepository plans;
    @Mock UserRepository users;
    @Mock TrainerClientRepository trainerClients;
    @InjectMocks WorkoutSessionServiceImpl service;

    @AfterEach void clear() { SecurityContextHolder.clearContext(); }

    @Test void createsSnapshotsAndPlannedSets() {
        User client = user(1L, UserRole.CLIENT);
        WorkoutPlan plan = plan(client, WorkoutPlanStatus.ACTIVE);
        Exercise exercise = exercise(7L, "Bench press");
        WorkoutPlanDay day = new WorkoutPlanDay(); day.setExercises(new ArrayList<>());
        WorkoutPlanExercise pe = new WorkoutPlanExercise();
        pe.setId(22L); pe.setExercise(exercise); pe.setSets(2); pe.setRepetitions(8); pe.setRestSeconds(90);
        day.getExercises().add(pe); plan.setDays(List.of(day));
        authenticate(client); when(users.findByEmail(client.getEmail())).thenReturn(Optional.of(client));
        when(users.findByIdForUpdate(1L)).thenReturn(Optional.of(client));
        when(plans.findById(plan.getId())).thenReturn(Optional.of(plan));
        when(sessions.findLocked(1L, plan.getId(), WorkoutSessionStatus.IN_PROGRESS)).thenReturn(List.of());
        when(sessions.save(any())).thenAnswer(i -> i.getArgument(0));

        WorkoutSession result = service.create(new WorkoutSessionCreateRequest(plan.getId()));

        assertThat(result.getStatus()).isEqualTo(WorkoutSessionStatus.IN_PROGRESS);
        assertThat(result.getExercises()).singleElement().satisfies(e -> {
            assertThat(e.getExerciseNameSnapshot()).isEqualTo("Bench press");
            assertThat(e.getSourceWorkoutPlanExerciseId()).isEqualTo(22L);
            assertThat(e.getOrderIndex()).isEqualTo(1);
            assertThat(e.getSets()).extracting(WorkoutSessionSet::getSetNumber).containsExactly(1, 2);
            assertThat(e.getSets()).extracting(WorkoutSessionSet::getPlannedRepetitions).containsOnly(8);
        });
    }

    @Test void rejectsNonClientAndForeignPlan() {
        User trainer = user(1L, UserRole.TRAINER);
        authenticate(trainer); when(users.findByEmail(trainer.getEmail())).thenReturn(Optional.of(trainer));
        assertThatThrownBy(() -> service.create(new WorkoutSessionCreateRequest(1L)))
                .isInstanceOf(AccessDeniedException.class);

        User client = user(2L, UserRole.CLIENT); User owner = user(3L, UserRole.CLIENT);
        WorkoutPlan plan = plan(owner, WorkoutPlanStatus.ACTIVE);
        authenticate(client); when(users.findByEmail(client.getEmail())).thenReturn(Optional.of(client));
        when(users.findByIdForUpdate(2L)).thenReturn(Optional.of(client));
        when(plans.findById(plan.getId())).thenReturn(Optional.of(plan));
        assertThatThrownBy(() -> service.create(new WorkoutSessionCreateRequest(plan.getId())))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test void rejectsInvalidPlanStatusDatesAndStructure() {
        User client = user(1L, UserRole.CLIENT); authenticate(client);
        when(users.findByEmail(client.getEmail())).thenReturn(Optional.of(client));
        when(users.findByIdForUpdate(1L)).thenReturn(Optional.of(client));
        for (WorkoutPlanStatus status : List.of(WorkoutPlanStatus.INACTIVE, WorkoutPlanStatus.COMPLETED)) {
            WorkoutPlan p = plan(client, status); when(plans.findById(p.getId())).thenReturn(Optional.of(p));
            assertThatThrownBy(() -> service.create(new WorkoutSessionCreateRequest(p.getId())))
                    .isInstanceOf(IllegalArgumentException.class);
        }
        WorkoutPlan future = plan(client, WorkoutPlanStatus.ACTIVE); future.setStartDate(LocalDate.now().plusDays(1));
        when(plans.findById(future.getId())).thenReturn(Optional.of(future));
        assertThatThrownBy(() -> service.create(new WorkoutSessionCreateRequest(future.getId())))
                .isInstanceOf(IllegalArgumentException.class);
        WorkoutPlan empty = plan(client, WorkoutPlanStatus.ACTIVE); empty.setDays(List.of());
        when(plans.findById(empty.getId())).thenReturn(Optional.of(empty));
        assertThatThrownBy(() -> service.create(new WorkoutSessionCreateRequest(empty.getId())))
                .hasMessage("Workout plan has no exercises");
    }

    @Test void rejectsDuplicateAndInvalidExercises() {
        User client = user(1L, UserRole.CLIENT); authenticate(client);
        when(users.findByEmail(client.getEmail())).thenReturn(Optional.of(client));
        when(users.findByIdForUpdate(1L)).thenReturn(Optional.of(client));
        WorkoutPlan p = plan(client, WorkoutPlanStatus.ACTIVE); p.setDays(new ArrayList<>());
        WorkoutPlanDay day = new WorkoutPlanDay(); day.setExercises(new ArrayList<>());
        WorkoutPlanExercise invalid = new WorkoutPlanExercise(); invalid.setSets(0); invalid.setRepetitions(5); invalid.setRestSeconds(0);
        day.getExercises().add(invalid); p.setDays(List.of(day)); when(plans.findById(p.getId())).thenReturn(Optional.of(p));
        assertThatThrownBy(() -> service.create(new WorkoutSessionCreateRequest(p.getId()))).isInstanceOf(IllegalArgumentException.class);
        WorkoutPlanDay validDay = new WorkoutPlanDay(); validDay.setExercises(new ArrayList<>());
        WorkoutPlanExercise valid = new WorkoutPlanExercise(); valid.setExercise(exercise(8L, "Squat"));
        valid.setSets(1); valid.setRepetitions(5); valid.setRestSeconds(0); validDay.getExercises().add(valid);
        p.setDays(List.of(validDay)); when(sessions.findLocked(1L, p.getId(), WorkoutSessionStatus.IN_PROGRESS))
                .thenReturn(List.of(new WorkoutSession()));
        assertThatThrownBy(() -> service.create(new WorkoutSessionCreateRequest(p.getId())))
                .isInstanceOf(ResourceConflictException.class);
    }

    @Test void enforcesOwnershipAndTrainerRelationship() {
        User client = user(1L, UserRole.CLIENT); User other = user(2L, UserRole.CLIENT);
        WorkoutSession s = session(5L, client, WorkoutSessionStatus.IN_PROGRESS);
        authenticate(other); when(users.findByEmail(other.getEmail())).thenReturn(Optional.of(other));
        when(sessions.findDetailedById(5L)).thenReturn(Optional.of(s));
        assertThatThrownBy(() -> service.find(5L)).isInstanceOf(AccessDeniedException.class);
        User trainer = user(3L, UserRole.TRAINER); authenticate(trainer);
        when(users.findByEmail(trainer.getEmail())).thenReturn(Optional.of(trainer));
        when(trainerClients.existsByTrainerIdAndClientId(3L, 1L)).thenReturn(false);
        assertThatThrownBy(() -> service.find(5L)).isInstanceOf(AccessDeniedException.class);
        verify(trainerClients).existsByTrainerIdAndClientId(3L, 1L);
    }

    @Test void savesSetValidatesPlannedNumberAndCrossSession() {
        User client = user(1L, UserRole.CLIENT); authenticate(client);
        WorkoutSession s = session(5L, client, WorkoutSessionStatus.IN_PROGRESS);
        WorkoutSessionExercise e = new WorkoutSessionExercise(); e.setId(9L); e.setSession(s); e.setPlannedSets(2); e.setPlannedRepetitions(8);
        when(users.findByEmail(client.getEmail())).thenReturn(Optional.of(client));
        when(sessions.findDetailedById(5L)).thenReturn(Optional.of(s));
        when(sessionExercises.findByIdAndSessionId(9L, 5L)).thenReturn(Optional.of(e));
        when(sessionSets.findBySessionExerciseIdAndSetNumber(9L, 1)).thenReturn(Optional.empty());
        when(sessionSets.save(any())).thenAnswer(i -> i.getArgument(0));
        WorkoutSessionSet saved = service.saveSet(5L, new WorkoutSessionSetRequest(9L, 1, 7, new BigDecimal("40.5"), 2, new BigDecimal("8.5"), true, LocalDateTime.now()));
        assertThat(saved.getPlannedRepetitions()).isEqualTo(8); assertThat(saved.getActualRepetitions()).isEqualTo(7);
        assertThat(saved.getWeight()).isEqualByComparingTo("40.5"); assertThat(saved.getCompleted()).isTrue();
        assertThatThrownBy(() -> service.saveSet(5L, new WorkoutSessionSetRequest(9L, 3, null, null, null, null, null, null)))
                .isInstanceOf(IllegalArgumentException.class);
        verify(sessionExercises, times(2)).findByIdAndSessionId(9L, 5L);
    }

    @Test void updatesExerciseAndRejectsTerminalMutation() {
        User client = user(1L, UserRole.CLIENT); authenticate(client);
        WorkoutSession s = session(5L, client, WorkoutSessionStatus.IN_PROGRESS);
        WorkoutSessionExercise e = new WorkoutSessionExercise(); e.setId(9L); e.setSession(s);
        when(users.findByEmail(client.getEmail())).thenReturn(Optional.of(client)); when(sessions.findDetailedById(5L)).thenReturn(Optional.of(s));
        when(sessionExercises.findByIdAndSessionId(9L, 5L)).thenReturn(Optional.of(e));
        service.updateExercise(5L, 9L, new WorkoutSessionExerciseUpdateRequest(true, "done"));
        assertThat(e.getCompleted()).isTrue(); assertThat(e.getNotes()).isEqualTo("done");
        s.setStatus(WorkoutSessionStatus.COMPLETED);
        assertThatThrownBy(() -> service.update(5L, new WorkoutSessionUpdateRequest("x", 1)))
                .isInstanceOf(ResourceConflictException.class);
    }

    @Test void finishesAndCancelsWithDurationAndTerminalProtection() {
        User client = user(1L, UserRole.CLIENT); authenticate(client);
        WorkoutSession s = session(5L, client, WorkoutSessionStatus.IN_PROGRESS);
        s.setStartedAt(LocalDateTime.now().minusSeconds(12));
        when(users.findByEmail(client.getEmail())).thenReturn(Optional.of(client)); when(sessions.findDetailedById(5L)).thenReturn(Optional.of(s));
        WorkoutSession finished = service.finish(5L);
        assertThat(finished.getStatus()).isEqualTo(WorkoutSessionStatus.COMPLETED);
        assertThat(finished.getFinishedAt()).isNotNull(); assertThat(finished.getDurationSeconds()).isBetween(11, 14);
        assertThatThrownBy(() -> service.cancel(5L)).isInstanceOf(ResourceConflictException.class);
        s.setStatus(WorkoutSessionStatus.IN_PROGRESS); s.setFinishedAt(null); s.setDurationSeconds(null);
        WorkoutSession cancelled = service.cancel(5L);
        assertThat(cancelled.getStatus()).isEqualTo(WorkoutSessionStatus.CANCELLED);
        assertThat(cancelled.getCancelledAt()).isNotNull();
    }

    private User user(Long id, UserRole role) { User u = new User(); u.setId(id); u.setEmail(id + "@test"); u.setRole(role); u.setActive(true); return u; }
    private Exercise exercise(Long id, String name) { Exercise e = new Exercise(); e.setId(id); e.setName(name); return e; }
    private WorkoutPlan plan(User client, WorkoutPlanStatus status) { WorkoutPlan p = new WorkoutPlan(); p.setId(10L + client.getId()); p.setClient(client); p.setStatus(status); p.setStartDate(LocalDate.now()); p.setDays(new ArrayList<>()); return p; }
    private WorkoutSession session(Long id, User client, WorkoutSessionStatus status) { WorkoutSession s = new WorkoutSession(); s.setId(id); s.setClient(client); s.setStatus(status); s.setStartedAt(LocalDateTime.now()); return s; }
    private void authenticate(User user) { SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user.getEmail(), null, List.of())); }
}
