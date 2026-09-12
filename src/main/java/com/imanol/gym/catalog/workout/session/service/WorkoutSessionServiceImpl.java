package com.imanol.gym.catalog.workout.session.service;

import com.imanol.gym.catalog.workout.entity.*;
import com.imanol.gym.catalog.workout.repository.WorkoutPlanRepository;
import com.imanol.gym.catalog.workout.session.dto.*;
import com.imanol.gym.catalog.workout.session.entity.*;
import com.imanol.gym.catalog.workout.session.repository.*;
import com.imanol.gym.common.exception.ResourceNotFoundException;
import com.imanol.gym.common.exception.ResourceConflictException;
import com.imanol.gym.user.entity.*;
import com.imanol.gym.user.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.time.*;
import java.util.*;

@Service @RequiredArgsConstructor
public class WorkoutSessionServiceImpl implements WorkoutSessionService {
    private final WorkoutSessionRepository sessions;
    private final WorkoutSessionExerciseRepository sessionExercises;
    private final WorkoutSessionSetRepository sessionSets;
    private final WorkoutPlanRepository plans;
    private final UserRepository users;
    private final TrainerClientRepository trainerClients;

    private User current() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return users.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }
    private WorkoutSession owned(Long id, boolean mutate) {
        WorkoutSession s = sessions.findDetailedById(id).orElseThrow(() -> new ResourceNotFoundException("Workout session not found"));
        User u = current();
        if (u.getRole() == UserRole.CLIENT && !s.getClient().getId().equals(u.getId()))
            throw new org.springframework.security.access.AccessDeniedException("Session is not yours");
        if (u.getRole() == UserRole.TRAINER && (mutate || !trainerClients.existsByTrainerIdAndClientId(u.getId(), s.getClient().getId())))
            throw new org.springframework.security.access.AccessDeniedException("You are not related to this client");
        return s;
    }
    @Override @Transactional
    public WorkoutSession create(WorkoutSessionCreateRequest request) {
        User client = current();
        if (client.getRole() != UserRole.CLIENT) throw new org.springframework.security.access.AccessDeniedException("Only clients can start sessions");
        // Locking the owner serializes starts even when no active row exists yet.
        users.findByIdForUpdate(client.getId()).orElseThrow();
        WorkoutPlan plan = plans.findById(request.workoutPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("Workout plan not found"));
        LocalDate today = LocalDate.now();
        if (!plan.getClient().getId().equals(client.getId())) throw new org.springframework.security.access.AccessDeniedException("Plan is not yours");
        if (plan.getStatus() != WorkoutPlanStatus.ACTIVE || plan.getStartDate().isAfter(today) ||
                (plan.getEndDate() != null && plan.getEndDate().isBefore(today)))
            throw new IllegalArgumentException("Workout plan is not active or date-valid");
        if (plan.getDays() == null || plan.getDays().isEmpty()) throw new IllegalArgumentException("Workout plan has no exercises");
        if (!sessions.findLocked(client.getId(), plan.getId(), WorkoutSessionStatus.IN_PROGRESS).isEmpty())
            throw new ResourceConflictException("An in-progress session already exists for this plan");
        WorkoutSession s = new WorkoutSession(); s.setClient(client); s.setWorkoutPlan(plan);
        s.setStatus(WorkoutSessionStatus.IN_PROGRESS); s.setStartedAt(LocalDateTime.now()); s.setVersion(0L);
        int order = 1;
        for (WorkoutPlanDay day : plan.getDays()) {
            if (day.getExercises() == null || day.getExercises().isEmpty())
                throw new IllegalArgumentException("Workout plan contains an empty day");
            for (WorkoutPlanExercise pe : day.getExercises()) {
                if (pe.getExercise() == null || pe.getSets() == null || pe.getSets() < 1 ||
                        pe.getRepetitions() == null || pe.getRepetitions() < 1 || pe.getRestSeconds() == null || pe.getRestSeconds() < 0)
                    throw new IllegalArgumentException("Workout plan contains invalid exercises");
                WorkoutSessionExercise e = new WorkoutSessionExercise(); e.setSession(s); e.setExercise(pe.getExercise());
                e.setSourceWorkoutPlanExerciseId(pe.getId());
                e.setExerciseNameSnapshot(pe.getExercise().getName());
                e.setOrderIndex(order++); e.setPlannedSets(pe.getSets()); e.setPlannedRepetitions(pe.getRepetitions()); e.setPlannedRestSeconds(pe.getRestSeconds());
                for (int setNumber = 1; setNumber <= pe.getSets(); setNumber++) {
                    WorkoutSessionSet set = new WorkoutSessionSet();
                    set.setSessionExercise(e); set.setSetNumber(setNumber);
                    set.setPlannedRepetitions(pe.getRepetitions());
                    e.getSets().add(set);
                }
                s.getExercises().add(e);
            }
        }
        if (s.getExercises().isEmpty()) throw new IllegalArgumentException("Workout plan has no exercises");
        return sessions.save(s);
    }
    @Override @Transactional
    public List<WorkoutSession> search(WorkoutSessionStatus status, LocalDate from, LocalDate to) {
        User u = current(); LocalDateTime f = from == null ? null : from.atStartOfDay(), t = to == null ? null : to.plusDays(1).atStartOfDay().minusNanos(1);
        if (u.getRole() == UserRole.CLIENT) return sessions.search(u.getId(), status, f, t);
        List<Long> ids = trainerClients.findAllByTrainerId(u.getId()).stream()
                .map(x -> x.getClient().getId()).toList();
        if (ids.isEmpty()) return List.of();
        return sessions.searchForClients(ids, status, f, t);
    }
    @Override @Transactional public WorkoutSession find(Long id) { return owned(id, false); }
    private WorkoutSession writable(Long id) {
        WorkoutSession s = owned(id, true);
        if (s.getStatus() != WorkoutSessionStatus.IN_PROGRESS)
            throw new ResourceConflictException("Session is not in progress");
        return s;
    }
    @Override @Transactional public WorkoutSession update(Long id, WorkoutSessionUpdateRequest r) { WorkoutSession s=writable(id); s.setNotes(r.notes()); if(r.durationSeconds()!=null)s.setDurationSeconds(r.durationSeconds()); return s; }
    @Override @Transactional public WorkoutSessionSet saveSet(Long id, WorkoutSessionSetRequest r) {
        if (r.sessionExerciseId() == null) throw new IllegalArgumentException("sessionExerciseId is required");
        return saveSetForExercise(id, r.sessionExerciseId(), r);
    }
    public WorkoutSessionSet saveSetForExercise(Long sessionId, Long exerciseId, WorkoutSessionSetRequest r) {
        WorkoutSession s=writable(sessionId); WorkoutSessionExercise e=sessionExercises.findByIdAndSessionId(exerciseId,sessionId).orElseThrow(() -> new ResourceNotFoundException("Session exercise not found"));
        if(r.setNumber()>e.getPlannedSets()) throw new IllegalArgumentException("Set number exceeds planned sets");
        WorkoutSessionSet set=sessionSets.findBySessionExerciseIdAndSetNumber(exerciseId,r.setNumber()).orElseGet(WorkoutSessionSet::new);
        set.setSessionExercise(e); set.setSetNumber(r.setNumber());
        if (set.getPlannedRepetitions() == null) set.setPlannedRepetitions(e.getPlannedRepetitions());
        set.setActualRepetitions(r.actualRepetitions());
        set.setWeight(r.weight());
        set.setRir(r.rir());
        set.setRpe(r.rpe());
        set.setCompleted(Boolean.TRUE.equals(r.completed()));
        set.setPerformedAt(r.performedAt());
        return sessionSets.save(set);
    }
    @Override @Transactional public WorkoutSessionExercise updateExercise(Long sid,Long eid,WorkoutSessionExerciseUpdateRequest r){ WorkoutSession s=writable(sid); WorkoutSessionExercise e=sessionExercises.findByIdAndSessionId(eid,sid).orElseThrow(() -> new ResourceNotFoundException("Session exercise not found")); if(r.completed()!=null)e.setCompleted(r.completed());e.setNotes(r.notes());return e; }
    @Override @Transactional public WorkoutSession finish(Long id){WorkoutSession s=writable(id);s.setStatus(WorkoutSessionStatus.COMPLETED);s.setFinishedAt(LocalDateTime.now());s.setDurationSeconds((int)Duration.between(s.getStartedAt(),s.getFinishedAt()).toSeconds());return s;}
    @Override @Transactional public WorkoutSession cancel(Long id){WorkoutSession s=writable(id);s.setStatus(WorkoutSessionStatus.CANCELLED);s.setCancelledAt(LocalDateTime.now());s.setFinishedAt(s.getCancelledAt());s.setDurationSeconds((int)Duration.between(s.getStartedAt(),s.getFinishedAt()).toSeconds());return s;}
}
