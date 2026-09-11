package com.imanol.gym;

import com.imanol.gym.catalog.exercise.entity.Exercise;
import com.imanol.gym.catalog.exercise.entity.ExerciseCategory;
import com.imanol.gym.catalog.exercise.repository.ExerciseCategoryRepository;
import com.imanol.gym.catalog.exercise.repository.ExerciseRepository;
import com.imanol.gym.catalog.nutrition.entity.NutritionPlan;
import com.imanol.gym.catalog.nutrition.entity.NutritionPlanStatus;
import com.imanol.gym.catalog.nutrition.repository.NutritionPlanRepository;
import com.imanol.gym.catalog.workout.dto.WorkoutPlanDayRequest;
import com.imanol.gym.catalog.workout.dto.WorkoutPlanExerciseRequest;
import com.imanol.gym.catalog.workout.dto.WorkoutPlanRequest;
import com.imanol.gym.catalog.workout.entity.WorkoutPlan;
import com.imanol.gym.catalog.workout.entity.WorkoutPlanDay;
import com.imanol.gym.catalog.workout.entity.WorkoutPlanExercise;
import com.imanol.gym.catalog.workout.entity.WorkoutPlanStatus;
import com.imanol.gym.catalog.workout.repository.WorkoutPlanDayRepository;
import com.imanol.gym.catalog.workout.repository.WorkoutPlanExerciseRepository;
import com.imanol.gym.catalog.workout.repository.WorkoutPlanRepository;
import com.imanol.gym.catalog.workout.service.WorkoutPlanService;
import com.imanol.gym.common.exception.ResourceAlreadyExistsException;
import com.imanol.gym.user.entity.TrainerClient;
import com.imanol.gym.user.entity.User;
import com.imanol.gym.user.entity.UserRole;
import com.imanol.gym.user.repository.TrainerClientRepository;
import com.imanol.gym.user.repository.UserRepository;
import com.imanol.gym.user.service.TrainerClientService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class Phase5ConcurrencyIntegrationTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TrainerClientRepository trainerClientRepository;

    @Autowired
    private ExerciseCategoryRepository exerciseCategoryRepository;

    @Autowired
    private ExerciseRepository exerciseRepository;

    @Autowired
    private WorkoutPlanRepository workoutPlanRepository;

    @Autowired
    private WorkoutPlanDayRepository workoutPlanDayRepository;

    @Autowired
    private WorkoutPlanExerciseRepository workoutPlanExerciseRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private NutritionPlanRepository nutritionPlanRepository;

    @Autowired
    private WorkoutPlanService workoutPlanService;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private TrainerClientService trainerClientService;

    private CyclicBarrier concurrencyBarrier;

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void simultaneousWorkoutPlanCreationsAllowOnlyOneOverlappingPlan() throws Exception {
        User trainerOne = user("workout-trainer-one");
        User trainerTwo = user("workout-trainer-two");
        User client = user("workout-client");
        userRepository.saveAllAndFlush(List.of(trainerOne, trainerTwo, client));
        trainerClientRepository.saveAndFlush(relationship(trainerOne, client));
        trainerClientRepository.saveAndFlush(relationship(trainerTwo, client));
        Exercise exercise = exercise("concurrent-workout-exercise");

        WorkoutPlanRequest request = workoutRequest(exercise.getId());
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<Throwable> first = executor.submit(() ->
                    invokeWorkoutCreate(start, trainerOne, client, request));
            Future<Throwable> second = executor.submit(() ->
                    invokeWorkoutCreate(start, trainerTwo, client, request));
            start.countDown();

            List<Throwable> failures = Arrays.asList(first.get(), second.get()).stream()
                    .filter(java.util.Objects::nonNull)
                    .toList();

            assertThat(failures)
                    .withFailMessage("Unexpected workout creation failures: %s",
                            failures)
                    .hasSize(1);
            assertThat(workoutPlanRepository
                    .findAllByClientIdAndStatusOrderByStartDateDesc(
                            client.getId(), WorkoutPlanStatus.ACTIVE))
                    .hasSize(1);
            assertThat(failures).singleElement()
                    .isInstanceOf(ResourceAlreadyExistsException.class);
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void simultaneousWorkoutPlanUpdatesDetectOptimisticLocking() throws Exception {
        User trainer = user("workout-update-trainer");
        User client = user("workout-update-client");
        userRepository.saveAllAndFlush(List.of(trainer, client));
        WorkoutPlan plan = new WorkoutPlan();
        plan.setTrainer(trainer);
        plan.setClient(client);
        plan.setStartDate(LocalDate.of(2030, 1, 1));
        plan.setEndDate(LocalDate.of(2030, 1, 31));
        plan.setStatus(WorkoutPlanStatus.INACTIVE);
        workoutPlanRepository.saveAndFlush(plan);

        assertConcurrentVersionConflict(
                () -> transactionTemplate.executeWithoutResult(status -> {
                    WorkoutPlan current = workoutPlanRepository.findById(plan.getId())
                            .orElseThrow();
                    awaitConcurrentLoad();
                    current.setEndDate(LocalDate.of(2030, 1, 10));
                    workoutPlanRepository.saveAndFlush(current);
                }),
                () -> transactionTemplate.executeWithoutResult(status -> {
                    WorkoutPlan current = workoutPlanRepository.findById(plan.getId())
                            .orElseThrow();
                    awaitConcurrentLoad();
                    current.setEndDate(LocalDate.of(2030, 1, 20));
                    workoutPlanRepository.saveAndFlush(current);
                }));
    }

    @Test
    void simultaneousNutritionPlanUpdatesDetectOptimisticLocking() throws Exception {
        User trainer = user("nutrition-update-trainer");
        User client = user("nutrition-update-client");
        userRepository.saveAllAndFlush(List.of(trainer, client));
        NutritionPlan plan = new NutritionPlan();
        plan.setName("Original nutrition plan");
        plan.setTrainer(trainer);
        plan.setClient(client);
        plan.setStartDate(LocalDate.of(2031, 1, 1));
        plan.setStatus(NutritionPlanStatus.INACTIVE);
        nutritionPlanRepository.saveAndFlush(plan);

        assertConcurrentVersionConflict(
                () -> transactionTemplate.executeWithoutResult(status -> {
                    NutritionPlan current = nutritionPlanRepository.findById(plan.getId())
                            .orElseThrow();
                    awaitConcurrentLoad();
                    current.setName("Nutrition plan A");
                    nutritionPlanRepository.saveAndFlush(current);
                }),
                () -> transactionTemplate.executeWithoutResult(status -> {
                    NutritionPlan current = nutritionPlanRepository.findById(plan.getId())
                            .orElseThrow();
                    awaitConcurrentLoad();
                    current.setName("Nutrition plan B");
                    nutritionPlanRepository.saveAndFlush(current);
                }));
    }

    @Test
    void simultaneousTrainerClientAssignmentsAreProtectedByUniqueConstraint()
            throws Exception {
        User trainer = user("relationship-trainer");
        User client = user("relationship-client");
        userRepository.saveAllAndFlush(List.of(trainer, client));
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<Throwable> first = executor.submit(() ->
                    invokeRelationshipCreate(start, trainer, client));
            Future<Throwable> second = executor.submit(() ->
                    invokeRelationshipCreate(start, trainer, client));
            start.countDown();

            List<Throwable> failures = Arrays.asList(first.get(), second.get()).stream()
                    .filter(java.util.Objects::nonNull)
                    .toList();

            assertThat(trainerClientRepository
                    .existsByTrainerIdAndClientId(trainer.getId(), client.getId()))
                    .isTrue();
            assertThat(failures).singleElement()
                    .satisfies(failure -> assertThat(failure)
                            .isInstanceOfAny(
                                    ResourceAlreadyExistsException.class,
                                    org.springframework.dao.DataIntegrityViolationException.class));
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void nestedWorkoutContentLoadsAndTemplateExerciseReferencesRequireForeignKeys() {
        User trainer = user("fetch-trainer");
        User client = user("fetch-client");
        userRepository.saveAllAndFlush(List.of(trainer, client));
        Exercise exercise = exercise("fetch-exercise");

        WorkoutPlan plan = new WorkoutPlan();
        plan.setTrainer(trainer);
        plan.setClient(client);
        plan.setStartDate(LocalDate.of(2033, 1, 1));
        plan.setStatus(WorkoutPlanStatus.INACTIVE);
        plan = workoutPlanRepository.saveAndFlush(plan);

        WorkoutPlanDay day = new WorkoutPlanDay();
        day.setWorkoutPlan(plan);
        day.setDayOfWeek(1);
        day = workoutPlanDayRepository.saveAndFlush(day);

        WorkoutPlanExercise planExercise = new WorkoutPlanExercise();
        planExercise.setWorkoutPlanDay(day);
        planExercise.setExercise(exercise);
        planExercise.setOrderIndex(1);
        planExercise.setSets(3);
        planExercise.setRepetitions(8);
        planExercise.setRestSeconds(60);
        workoutPlanExerciseRepository.saveAndFlush(planExercise);
        final Long dayId = day.getId();
        final Long exerciseId = exercise.getId();

        transactionTemplate.executeWithoutResult(status -> {
            WorkoutPlan loaded = workoutPlanRepository
                    .findAllByClientIdOrderByStartDateDesc(client.getId())
                    .getFirst();
            assertThat(loaded.getDays()).singleElement()
                    .satisfies(loadedDay -> assertThat(loadedDay.getExercises())
                            .singleElement()
                            .extracting(WorkoutPlanExercise::getExercise)
                            .extracting(Exercise::getId)
                            .isEqualTo(exercise.getId()));
        });

        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO workout_plan_exercises (
                    workout_plan_day_id, exercise_id, source_template_exercise_id,
                    order_index, sets, repetitions, rest_seconds,
                    created_at, updated_at
                ) VALUES (?, ?, ?, 2, 1, 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """, dayId, exerciseId, 999999L))
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    }

    private Throwable invokeWorkoutCreate(
            CountDownLatch start,
            User trainer,
            User client,
            WorkoutPlanRequest request
    ) {
        await(start);
        authenticate(trainer);
        try {
            workoutPlanService.createForAuthenticatedTrainer(client.getId(), request);
            return null;
        } catch (Throwable failure) {
            return failure;
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private Throwable invokeRelationshipCreate(
            CountDownLatch start,
            User trainer,
            User client
    ) {
        await(start);
        authenticate(trainer);
        try {
            trainerClientService.assignClient(trainer.getId(), client.getId());
            return null;
        } catch (Throwable failure) {
            return failure;
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private void assertConcurrentVersionConflict(
            Runnable first,
            Runnable second
    ) throws Exception {
        concurrencyBarrier = new CyclicBarrier(2);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<Throwable> firstResult = executor.submit(() -> runAfter(
                    start, first));
            Future<Throwable> secondResult = executor.submit(() -> runAfter(
                    start, second));
            start.countDown();

            List<Throwable> failures = Arrays.asList(
                            firstResult.get(), secondResult.get()).stream()
                    .filter(java.util.Objects::nonNull)
                    .toList();
            assertThat(failures).singleElement()
                    .isInstanceOfAny(
                            org.springframework.orm.ObjectOptimisticLockingFailureException.class,
                            jakarta.persistence.OptimisticLockException.class,
                            org.hibernate.StaleStateException.class);
        } finally {
            executor.shutdownNow();
        }
    }

    private Throwable runAfter(CountDownLatch start, Runnable operation) {
        await(start);
        try {
            operation.run();
            return null;
        } catch (Throwable failure) {
            return rootCause(failure);
        }
    }

    private Throwable rootCause(Throwable failure) {
        Throwable current = failure;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current;
    }

    private void awaitConcurrentLoad() {
        try {
            concurrencyBarrier.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted during concurrency test",
                    exception);
        } catch (Exception exception) {
            throw new IllegalStateException("Concurrent operations did not meet",
                    exception);
        }
    }

    private void await(CountDownLatch latch) {
        try {
            assertThat(latch.await(10, TimeUnit.SECONDS)).isTrue();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted during concurrency test",
                    exception);
        }
    }

    private WorkoutPlanRequest workoutRequest(Long exerciseId) {
        return new WorkoutPlanRequest(
                null,
                LocalDate.of(2032, 1, 1),
                LocalDate.of(2032, 1, 31),
                List.of(new WorkoutPlanDayRequest(
                        1,
                        List.of(new WorkoutPlanExerciseRequest(
                                null, exerciseId, 1, 3, 8, 60)))));
    }

    private Exercise exercise(String suffix) {
        ExerciseCategory category = new ExerciseCategory();
        category.setName("Concurrent category " + suffix);
        category.setActive(true);
        category = exerciseCategoryRepository.saveAndFlush(category);
        Exercise exercise = new Exercise();
        exercise.setName(suffix);
        exercise.setCategory(category);
        exercise.setActive(true);
        return exerciseRepository.saveAndFlush(exercise);
    }

    private User user(String suffix) {
        User user = new User();
        user.setFirstName("Test");
        user.setLastName("Concurrent");
        user.setEmail(suffix + "@test.local");
        user.setPassword("password");
        user.setRole(suffix.contains("client")
                ? UserRole.CLIENT : UserRole.TRAINER);
        user.setActive(true);
        return user;
    }

    private TrainerClient relationship(User trainer, User client) {
        TrainerClient relationship = new TrainerClient();
        relationship.setTrainer(trainer);
        relationship.setClient(client);
        return relationship;
    }

    private void authenticate(User user) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        user.getEmail(), null, List.of()));
    }
}
