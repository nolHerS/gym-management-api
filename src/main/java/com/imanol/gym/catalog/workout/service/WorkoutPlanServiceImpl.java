package com.imanol.gym.catalog.workout.service;

import com.imanol.gym.catalog.exercise.entity.Exercise;
import com.imanol.gym.catalog.exercise.repository.ExerciseRepository;
import com.imanol.gym.catalog.workout.dto.WorkoutPlanDayRequest;
import com.imanol.gym.catalog.workout.dto.WorkoutPlanExerciseRequest;
import com.imanol.gym.catalog.workout.dto.WorkoutPlanRequest;
import com.imanol.gym.catalog.workout.dto.WorkoutPlanUpdateRequest;
import com.imanol.gym.catalog.workout.entity.*;
import com.imanol.gym.catalog.workout.repository.*;
import com.imanol.gym.common.exception.ResourceAlreadyExistsException;
import com.imanol.gym.common.exception.ResourceNotFoundException;
import com.imanol.gym.user.entity.TrainerClient;
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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class WorkoutPlanServiceImpl implements WorkoutPlanService {

    private final WorkoutPlanRepository workoutPlanRepository;
    private final WorkoutPlanDayRepository workoutPlanDayRepository;
    private final WorkoutPlanExerciseRepository workoutPlanExerciseRepository;
    private final UserRepository userRepository;
    private final TrainerClientRepository trainerClientRepository;
    private final WorkoutTemplateRepository workoutTemplateRepository;
    private final WorkoutTemplateExerciseRepository workoutTemplateExerciseRepository;
    private final ExerciseRepository exerciseRepository;

    @Override
    @Transactional
    public WorkoutPlan createForAuthenticatedTrainer(
            Long clientId,
            WorkoutPlanRequest request
    ) {
        User trainer = authenticatedUser(UserRole.TRAINER);
        User client = findClientForUpdate(clientId);
        requireRelationship(trainer, client);
        validateDates(request.startDate(), request.endDate());
        validateNoDuplicateDays(request.days());
        validateNoOverlap(
                client.getId(),
                request.startDate(),
                request.endDate(),
                null
        );

        WorkoutTemplate sourceTemplate = null;
        if (request.sourceTemplateId() != null) {
            sourceTemplate = workoutTemplateRepository
                    .findById(request.sourceTemplateId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Workout template not found with id: "
                                    + request.sourceTemplateId()
                    ));
            if (!Boolean.TRUE.equals(sourceTemplate.getActive())) {
                throw new IllegalArgumentException(
                        "Workout template is inactive"
                );
            }
        }

        WorkoutPlan plan = new WorkoutPlan();
        plan.setClient(client);
        plan.setTrainer(trainer);
        plan.setSourceTemplate(sourceTemplate);
        plan.setStartDate(request.startDate());
        plan.setEndDate(request.endDate());
        plan.setStatus(WorkoutPlanStatus.ACTIVE);
        WorkoutPlan savedPlan = workoutPlanRepository.save(plan);

        for (WorkoutPlanDayRequest dayRequest : request.days()) {
            validateNoDuplicateExerciseValues(dayRequest.exercises());
            WorkoutPlanDay day = new WorkoutPlanDay();
            day.setWorkoutPlan(savedPlan);
            day.setDayOfWeek(dayRequest.dayOfWeek());
            WorkoutPlanDay savedDay = workoutPlanDayRepository.save(day);

            Set<Long> exerciseIds = new HashSet<>();
            Set<Integer> orderIndexes = new HashSet<>();
            for (WorkoutPlanExerciseRequest exerciseRequest :
                    dayRequest.exercises()) {
                WorkoutPlanExercise exercise = createExercise(
                        savedDay,
                        sourceTemplate,
                        exerciseRequest
                );
                if (!exerciseIds.add(exercise.getExercise().getId())) {
                    throw new ResourceAlreadyExistsException(
                            "Exercise is duplicated in workout plan day"
                    );
                }
                if (!orderIndexes.add(exercise.getOrderIndex())) {
                    throw new ResourceAlreadyExistsException(
                            "Exercise order is duplicated in workout plan day"
                    );
                }
                workoutPlanExerciseRepository.save(exercise);
            }
        }

        return savedPlan;
    }

    @Override
    @Transactional
    public List<WorkoutPlan> findForAuthenticatedTrainer(
            Long clientId,
            WorkoutPlanStatus status
    ) {
        User trainer = authenticatedUser(UserRole.TRAINER);
        User client = findClient(clientId);
        requireRelationship(trainer, client);
        if (status == null) {
            return workoutPlanRepository
                    .findAllByTrainerIdAndClientIdOrderByStartDateDesc(
                            trainer.getId(), clientId);
        }
        return workoutPlanRepository.findAllByTrainerIdAndClientIdOrderByStartDateDesc(
                        trainer.getId(), clientId
                ).stream()
                .filter(plan -> plan.getStatus() == status)
                .toList();
    }

    @Override
    @Transactional
    public List<WorkoutPlan> findForAuthenticatedClient() {
        User client = authenticatedUser(UserRole.CLIENT);
        return workoutPlanRepository
                .findAllByClientIdOrderByStartDateDesc(client.getId());
    }

    @Override
    @Transactional
    public WorkoutPlan findByIdForAuthenticatedUser(Long id) {
        WorkoutPlan plan = findPlan(id);
        authorizePlan(plan);
        return plan;
    }

    @Override
    @Transactional
    public List<WorkoutPlan> findWeekForAuthenticatedClient(
            LocalDate weekStart
    ) {
        if (weekStart.getDayOfWeek() != DayOfWeek.MONDAY) {
            throw new IllegalArgumentException(
                    "weekStart must be a Monday"
            );
        }
        User client = authenticatedUser(UserRole.CLIENT);
        LocalDate weekEnd = weekStart.plusDays(6);
        return workoutPlanRepository
                .findActivePlansForWeek(
                        client.getId(),
                        WorkoutPlanStatus.ACTIVE,
                        weekStart,
                        weekEnd
                );
    }

    @Override
    @Transactional
    public WorkoutPlan updateForAuthenticatedUser(
            Long id,
            WorkoutPlanUpdateRequest request
    ) {
        WorkoutPlan plan = findPlan(id);
        authorizeTrainerPlan(plan);
        if (request.startDate() != null) {
            plan.setStartDate(request.startDate());
        }
        if (request.endDate() != null) {
            plan.setEndDate(request.endDate());
        }
        if (plan.getStartDate() == null) {
            throw new IllegalArgumentException("startDate is required");
        }
        validateDates(plan.getStartDate(), plan.getEndDate());

        WorkoutPlanStatus requestedStatus = request.status();
        if (requestedStatus != null
                && requestedStatus != plan.getStatus()) {
            validateTransition(plan.getStatus(), requestedStatus);
            plan.setStatus(requestedStatus);
        }
        if (plan.getStatus() == WorkoutPlanStatus.ACTIVE) {
            userRepository.findByIdForUpdate(plan.getClient().getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Client not found with id: "
                                    + plan.getClient().getId()
                    ));
            validateNoOverlap(
                    plan.getClient().getId(),
                    plan.getStartDate(),
                    plan.getEndDate(),
                    plan.getId()
            );
        }
        return workoutPlanRepository.save(plan);
    }

    @Override
    @Transactional
    public WorkoutPlanDay addDayForAuthenticatedTrainer(
            Long planId,
            WorkoutPlanDayRequest request
    ) {
        WorkoutPlan plan = findPlan(planId);
        authorizeTrainerPlan(plan);
        requireActive(plan);
        validateDay(request.dayOfWeek());
        if (workoutPlanDayRepository
                .findByWorkoutPlanIdAndDayOfWeek(planId, request.dayOfWeek())
                .isPresent()) {
            throw new ResourceAlreadyExistsException(
                    "Workout plan day already exists"
            );
        }
        WorkoutPlanDay day = new WorkoutPlanDay();
        day.setWorkoutPlan(plan);
        day.setDayOfWeek(request.dayOfWeek());
        WorkoutPlanDay savedDay = workoutPlanDayRepository.save(day);
        validateNoDuplicateExerciseValues(request.exercises());
        for (WorkoutPlanExerciseRequest exerciseRequest :
                request.exercises()) {
            workoutPlanExerciseRepository.save(
                    createExercise(savedDay, null, exerciseRequest)
            );
        }
        return savedDay;
    }

    @Override
    @Transactional
    public void deleteDayForAuthenticatedTrainer(
            Long planId,
            Integer dayOfWeek
    ) {
        WorkoutPlan plan = findPlan(planId);
        authorizeTrainerPlan(plan);
        requireActive(plan);
        WorkoutPlanDay day = findDay(planId, dayOfWeek);
        workoutPlanExerciseRepository
                .deleteAll(workoutPlanExerciseRepository
                        .findAllByWorkoutPlanDayIdOrderByOrderIndexAsc(day.getId()));
        workoutPlanDayRepository.delete(day);
    }

    @Override
    @Transactional
    public WorkoutPlanExercise addExerciseForAuthenticatedTrainer(
            Long planId,
            Integer dayOfWeek,
            WorkoutPlanExerciseRequest request
    ) {
        WorkoutPlan plan = findPlan(planId);
        authorizeTrainerPlan(plan);
        requireActive(plan);
        WorkoutPlanDay day = findDay(planId, dayOfWeek);
        validateNoExistingExerciseConflict(day, request);
        WorkoutPlanExercise exercise = createExercise(day, null, request);
        return workoutPlanExerciseRepository.save(exercise);
    }

    @Override
    @Transactional
    public WorkoutPlanExercise updateExerciseForAuthenticatedUser(
            Long exerciseId,
            WorkoutPlanExerciseRequest request
    ) {
        WorkoutPlanExercise exercise = findExercise(exerciseId);
        authorizeTrainerPlan(exercise.getWorkoutPlanDay().getWorkoutPlan());
        requireActive(exercise.getWorkoutPlanDay().getWorkoutPlan());
        if (request.exerciseId() != null) {
            exercise.setExercise(findExerciseReference(request.exerciseId()));
        }
        if (request.orderIndex() != null) {
            exercise.setOrderIndex(request.orderIndex());
        }
        if (request.sets() != null) {
            exercise.setSets(request.sets());
        }
        if (request.repetitions() != null) {
            exercise.setRepetitions(request.repetitions());
        }
        if (request.restSeconds() != null) {
            exercise.setRestSeconds(request.restSeconds());
        }
        validateExerciseValues(exercise);
        validateNoExistingExerciseConflict(
                exercise.getWorkoutPlanDay(),
                exercise,
                request
        );
        return workoutPlanExerciseRepository.save(exercise);
    }

    @Override
    @Transactional
    public void deleteExerciseForAuthenticatedUser(Long exerciseId) {
        WorkoutPlanExercise exercise = findExercise(exerciseId);
        authorizeTrainerPlan(exercise.getWorkoutPlanDay().getWorkoutPlan());
        requireActive(exercise.getWorkoutPlanDay().getWorkoutPlan());
        workoutPlanExerciseRepository.delete(exercise);
    }

    @Override
    @Transactional
    public void deactivateForAuthenticatedUser(Long id) {
        WorkoutPlan plan = findPlan(id);
        authorizeTrainerPlan(plan);
        validateTransition(plan.getStatus(), WorkoutPlanStatus.INACTIVE);
        plan.setStatus(WorkoutPlanStatus.INACTIVE);
        workoutPlanRepository.save(plan);
    }

    @Override
    @Transactional
    public void completeForAuthenticatedUser(Long id) {
        WorkoutPlan plan = findPlan(id);
        authorizeTrainerPlan(plan);
        validateTransition(plan.getStatus(), WorkoutPlanStatus.COMPLETED);
        plan.setStatus(WorkoutPlanStatus.COMPLETED);
        workoutPlanRepository.save(plan);
    }

    private WorkoutPlanExercise createExercise(
            WorkoutPlanDay day,
            WorkoutTemplate sourceTemplate,
            WorkoutPlanExerciseRequest request
    ) {
        WorkoutTemplateExercise source = null;
        if (request.sourceTemplateExerciseId() != null) {
            if (sourceTemplate == null) {
                throw new IllegalArgumentException(
                        "sourceTemplateId is required for template exercises"
                );
            }
            source = workoutTemplateExerciseRepository
                    .findById(request.sourceTemplateExerciseId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Workout template exercise not found with id: "
                                    + request.sourceTemplateExerciseId()
                    ));
            if (!source.getWorkoutTemplate().getId()
                    .equals(sourceTemplate.getId())) {
                throw new IllegalArgumentException(
                        "Workout template exercise does not belong to template"
                );
            }
        }

        Long exerciseId = request.exerciseId() != null
                ? request.exerciseId()
                : source == null ? null : source.getExercise().getId();
        if (exerciseId == null) {
            throw new IllegalArgumentException(
                    "exerciseId or sourceTemplateExerciseId is required"
            );
        }

        WorkoutPlanExercise result = new WorkoutPlanExercise();
        result.setWorkoutPlanDay(day);
        result.setExercise(findExerciseReference(exerciseId));
        result.setSourceTemplateExerciseId(
                request.sourceTemplateExerciseId()
        );
        result.setOrderIndex(valueOrSource(
                request.orderIndex(),
                source == null ? null : source.getOrderIndex()
        ));
        result.setSets(valueOrSource(
                request.sets(),
                source == null ? null : source.getSets()
        ));
        result.setRepetitions(valueOrSource(
                request.repetitions(),
                source == null ? null : source.getRepetitions()
        ));
        result.setRestSeconds(valueOrSource(
                request.restSeconds(),
                source == null ? null : source.getRestSeconds()
        ));
        validateExerciseValues(result);
        return result;
    }

    private Integer valueOrSource(Integer value, Integer sourceValue) {
        return value != null ? value : sourceValue;
    }

    private Exercise findExerciseReference(Long id) {
        return exerciseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Exercise not found with id: " + id
                ));
    }

    private WorkoutPlan findPlan(Long id) {
        return workoutPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Workout plan not found with id: " + id
                ));
    }

    private WorkoutPlanDay findDay(Long planId, Integer dayOfWeek) {
        return workoutPlanDayRepository
                .findByWorkoutPlanIdAndDayOfWeek(planId, dayOfWeek)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Workout plan day not found"
                ));
    }

    private WorkoutPlanExercise findExercise(Long id) {
        return workoutPlanExerciseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Workout plan exercise not found with id: " + id
                ));
    }

    private User findClient(Long id) {
        User client = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Client not found with id: " + id
                ));
        if (client.getRole() != UserRole.CLIENT) {
            throw new IllegalArgumentException(
                    "User with id " + id + " is not a client"
            );
        }
        if (!Boolean.TRUE.equals(client.getActive())) {
            throw new AccessDeniedException("Client is inactive");
        }
        return client;
    }

    private User findClientForUpdate(Long id) {
        User client = userRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Client not found with id: " + id
                ));
        if (client.getRole() != UserRole.CLIENT) {
            throw new IllegalArgumentException(
                    "User with id " + id + " is not a client"
            );
        }
        if (!Boolean.TRUE.equals(client.getActive())) {
            throw new AccessDeniedException("Client is inactive");
        }
        return client;
    }

    private void requireRelationship(User trainer, User client) {
        if (!trainerClientRepository.existsByTrainerIdAndClientId(
                trainer.getId(),
                client.getId()
        )) {
            throw new AccessDeniedException(
                    "Trainer is not related to this client"
            );
        }
    }

    private void authorizePlan(WorkoutPlan plan) {
        User user = authenticatedUser(null);
        if (user.getRole() == UserRole.CLIENT) {
            if (!user.getId().equals(plan.getClient().getId())) {
                throw new AccessDeniedException(
                        "Client cannot access this workout plan"
                );
            }
            return;
        }
        if (user.getRole() == UserRole.TRAINER) {
            authorizeTrainerPlan(plan);
            return;
        }
        throw new AccessDeniedException("User is not allowed");
    }

    private void authorizeTrainerPlan(WorkoutPlan plan) {
        User trainer = authenticatedUser(UserRole.TRAINER);
        if (!trainer.getId().equals(plan.getTrainer().getId())) {
            throw new AccessDeniedException(
                    "Trainer does not own this workout plan"
            );
        }
        requireRelationship(trainer, plan.getClient());
    }

    private User authenticatedUser(UserRole requiredRole) {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Authentication is required");
        }
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Authenticated user not found"
                ));
        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new AccessDeniedException("User is inactive");
        }
        if (requiredRole != null && user.getRole() != requiredRole) {
            throw new AccessDeniedException("User role is not allowed");
        }
        return user;
    }

    private void validateNoOverlap(
            Long clientId,
            LocalDate startDate,
            LocalDate endDate,
            Long excludedPlanId
    ) {
        List<WorkoutPlan> activePlans =
                workoutPlanRepository
                        .findAllByClientIdAndStatusOrderByStartDateDesc(
                                clientId,
                                WorkoutPlanStatus.ACTIVE
                        );
        for (WorkoutPlan existing : activePlans) {
            if (existing.getId().equals(excludedPlanId)) {
                continue;
            }
            boolean startsBeforeCandidateEnds = endDate == null
                    || !existing.getStartDate().isAfter(endDate);
            boolean candidateStartsBeforeExistingEnds =
                    existing.getEndDate() == null
                            || !startDate.isAfter(existing.getEndDate());
            if (startsBeforeCandidateEnds
                    && candidateStartsBeforeExistingEnds) {
                throw new ResourceAlreadyExistsException(
                        "Active workout plan dates overlap"
                );
            }
        }
    }

    private void validateNoDuplicateDays(List<WorkoutPlanDayRequest> days) {
        Set<Integer> dayIndexes = new HashSet<>();
        for (WorkoutPlanDayRequest day : days) {
            validateDay(day.dayOfWeek());
            if (!dayIndexes.add(day.dayOfWeek())) {
                throw new ResourceAlreadyExistsException(
                        "Workout plan day is duplicated"
                );
            }
        }
    }

    private void validateDay(Integer dayOfWeek) {
        if (dayOfWeek == null || dayOfWeek < 1 || dayOfWeek > 7) {
            throw new IllegalArgumentException(
                    "dayOfWeek must be between 1 and 7"
            );
        }
    }

    private void validateDates(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) {
            throw new IllegalArgumentException("startDate is required");
        }
        if (endDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException(
                    "endDate must be greater than or equal to startDate"
            );
        }
    }

    private void validateExerciseValues(WorkoutPlanExercise exercise) {
        if (exercise.getOrderIndex() == null
                || exercise.getOrderIndex() < 1
                || exercise.getSets() == null
                || exercise.getSets() < 1
                || exercise.getRepetitions() == null
                || exercise.getRepetitions() < 1
                || exercise.getRestSeconds() == null
                || exercise.getRestSeconds() < 0) {
            throw new IllegalArgumentException(
                    "Exercise order and training values are invalid"
            );
        }
    }

    private void validateNoDuplicateExerciseValues(
            List<WorkoutPlanExerciseRequest> requests
    ) {
        Set<Integer> orderIndexes = new HashSet<>();
        Set<Long> exerciseIds = new HashSet<>();
        for (WorkoutPlanExerciseRequest request : requests) {
            if (request.orderIndex() != null
                    && !orderIndexes.add(request.orderIndex())) {
                throw new ResourceAlreadyExistsException(
                        "Exercise order is duplicated in workout plan day"
                );
            }
            if (request.exerciseId() != null
                    && !exerciseIds.add(request.exerciseId())) {
                throw new ResourceAlreadyExistsException(
                        "Exercise is duplicated in workout plan day"
                );
            }
        }
    }

    private void validateNoExistingExerciseConflict(
            WorkoutPlanDay day,
            WorkoutPlanExerciseRequest request
    ) {
        for (WorkoutPlanExercise existing :
                workoutPlanExerciseRepository
                        .findAllByWorkoutPlanDayIdOrderByOrderIndexAsc(
                                day.getId()
                        )) {
            if (request.orderIndex() != null
                    && request.orderIndex().equals(existing.getOrderIndex())) {
                throw new ResourceAlreadyExistsException(
                        "Exercise order is already used in workout plan day"
                );
            }
            if (request.exerciseId() != null
                    && request.exerciseId().equals(
                    existing.getExercise().getId())) {
                throw new ResourceAlreadyExistsException(
                        "Exercise is already used in workout plan day"
                );
            }
        }
    }

    private void validateNoExistingExerciseConflict(
            WorkoutPlanDay day,
            WorkoutPlanExercise current,
            WorkoutPlanExerciseRequest request
    ) {
        for (WorkoutPlanExercise existing :
                workoutPlanExerciseRepository
                        .findAllByWorkoutPlanDayIdOrderByOrderIndexAsc(
                                day.getId()
                        )) {
            if (existing.getId().equals(current.getId())) {
                continue;
            }
            if (current.getOrderIndex().equals(existing.getOrderIndex())
                    || current.getExercise().getId().equals(
                    existing.getExercise().getId())) {
                throw new ResourceAlreadyExistsException(
                        "Exercise order or exercise is already used in workout plan day"
                );
            }
        }
    }

    private void validateTransition(
            WorkoutPlanStatus current,
            WorkoutPlanStatus requested
    ) {
        if (current == requested) {
            return;
        }
        boolean valid = current == WorkoutPlanStatus.ACTIVE
                && (requested == WorkoutPlanStatus.COMPLETED
                || requested == WorkoutPlanStatus.INACTIVE);
        if (!valid) {
            throw new IllegalArgumentException(
                    "Invalid workout plan status transition"
            );
        }
    }

    private void requireActive(WorkoutPlan plan) {
        if (plan.getStatus() != WorkoutPlanStatus.ACTIVE) {
            throw new IllegalArgumentException(
                    "Workout plan is not active"
            );
        }
    }
}
