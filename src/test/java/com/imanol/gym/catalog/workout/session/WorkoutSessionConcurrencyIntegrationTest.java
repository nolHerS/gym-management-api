package com.imanol.gym.catalog.workout.session;

import com.imanol.gym.catalog.exercise.entity.*;
import com.imanol.gym.catalog.exercise.repository.*;
import com.imanol.gym.catalog.workout.entity.*;
import com.imanol.gym.catalog.workout.repository.*;
import com.imanol.gym.catalog.workout.session.dto.WorkoutSessionCreateRequest;
import com.imanol.gym.catalog.workout.session.entity.*;
import com.imanol.gym.catalog.workout.session.repository.WorkoutSessionRepository;
import com.imanol.gym.catalog.workout.session.service.WorkoutSessionService;
import com.imanol.gym.user.entity.*;
import com.imanol.gym.user.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.*;
import java.util.*;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest @ActiveProfiles("test") @Testcontainers
class WorkoutSessionConcurrencyIntegrationTest {
    @Container @ServiceConnection static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");
    @Autowired UserRepository users; @Autowired WorkoutPlanRepository plans;
    @Autowired WorkoutPlanDayRepository days; @Autowired WorkoutPlanExerciseRepository planExercises;
    @Autowired ExerciseCategoryRepository categories; @Autowired ExerciseRepository exercises;
    @Autowired WorkoutSessionRepository sessions; @Autowired WorkoutSessionService service;
    @Autowired TransactionTemplate transactions;

    @AfterEach void clear() { SecurityContextHolder.clearContext(); }

    @Test void duplicateStartsAreSerializedByClientLock() throws Exception {
        Fixture f = fixture("duplicate");
        CountDownLatch start = new CountDownLatch(1); ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Future<Throwable> a = pool.submit(() -> createAfter(start, f.client, f.plan));
            Future<Throwable> b = pool.submit(() -> createAfter(start, f.client, f.plan)); start.countDown();
            List<Throwable> failures = Arrays.asList(a.get(20, TimeUnit.SECONDS), b.get(20, TimeUnit.SECONDS)).stream().filter(Objects::nonNull).toList();
            int active = transactions.execute(status -> sessions.findLocked(f.client.getId(), f.plan.getId(), WorkoutSessionStatus.IN_PROGRESS).size());
            assertThat(active).isEqualTo(1);
            assertThat(failures).hasSize(1);
        } finally { pool.shutdownNow(); }
    }

    @Test void concurrentFinishesProduceOneOptimisticConflict() throws Exception {
        Fixture f = fixture("finish");
        authenticate(f.client); WorkoutSession s = service.create(new WorkoutSessionCreateRequest(f.plan.getId()));
        CountDownLatch start = new CountDownLatch(1); ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Future<Throwable> a = pool.submit(() -> finishAfter(start, f.client, s.getId()));
            Future<Throwable> b = pool.submit(() -> finishAfter(start, f.client, s.getId())); start.countDown();
            List<Throwable> failures = Arrays.asList(a.get(20, TimeUnit.SECONDS), b.get(20, TimeUnit.SECONDS)).stream().filter(Objects::nonNull).toList();
            assertThat(failures).hasSize(1);
            assertThat(sessions.findById(s.getId()).orElseThrow().getStatus()).isEqualTo(WorkoutSessionStatus.COMPLETED);
        } finally { pool.shutdownNow(); }
    }

    @Test void concurrentSessionUpdatesProduceOneOptimisticConflict() throws Exception {
        Fixture f = fixture("update");
        authenticate(f.client);
        WorkoutSession session = service.create(new WorkoutSessionCreateRequest(f.plan.getId()));
        CountDownLatch loaded = new CountDownLatch(2);
        CountDownLatch proceed = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Future<Throwable> first = pool.submit(() ->
                    updateAfter(loaded, proceed, f.client, session.getId(), "first"));
            Future<Throwable> second = pool.submit(() ->
                    updateAfter(loaded, proceed, f.client, session.getId(), "second"));
            assertThat(loaded.await(10, TimeUnit.SECONDS)).isTrue();
            proceed.countDown();
            List<Throwable> failures = Arrays.asList(
                            first.get(20, TimeUnit.SECONDS),
                            second.get(20, TimeUnit.SECONDS))
                    .stream()
                    .filter(Objects::nonNull)
                    .toList();
            assertThat(failures).hasSize(1);
            assertThat(sessions.findById(session.getId()).orElseThrow().getNotes())
                    .isIn("first", "second");
        } finally {
            proceed.countDown();
            pool.shutdownNow();
        }
    }

    private Throwable createAfter(CountDownLatch latch, User client, WorkoutPlan plan) {
        await(latch); authenticate(client);
        try { service.create(new WorkoutSessionCreateRequest(plan.getId())); return null; } catch (Throwable t) { return root(t); } finally { clear(); }
    }
    private Throwable finishAfter(CountDownLatch latch, User client, Long id) {
        await(latch); authenticate(client);
        try { service.finish(id); return null; } catch (Throwable t) { return root(t); } finally { clear(); }
    }
    private Throwable updateAfter(CountDownLatch loaded, CountDownLatch proceed,
                                  User client, Long id, String notes) {
        authenticate(client);
        try {
            return transactions.execute(status -> {
                WorkoutSession current = sessions.findById(id).orElseThrow();
                loaded.countDown();
                await(proceed);
                current.setNotes(notes);
                sessions.saveAndFlush(current);
                return null;
            });
        } catch (Throwable t) {
            return root(t);
        } finally {
            clear();
        }
    }
    private Throwable root(Throwable t) { while (t.getCause() != null) t = t.getCause(); return t; }
    private void await(CountDownLatch latch) { try { assertThat(latch.await(10, TimeUnit.SECONDS)).isTrue(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new IllegalStateException(e); } }
    private void authenticate(User u) { SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(u.getEmail(), null, List.of())); }

    private Fixture fixture(String suffix) {
        User trainer = user("trainer-" + suffix + "@test"), client = user("client-" + suffix + "@test"); users.saveAllAndFlush(List.of(trainer, client));
        ExerciseCategory c = new ExerciseCategory(); c.setName("Category " + suffix); c.setActive(true); categories.saveAndFlush(c);
        Exercise ex = new Exercise(); ex.setName("Exercise " + suffix); ex.setCategory(c); ex.setActive(true); exercises.saveAndFlush(ex);
        WorkoutPlan p = new WorkoutPlan(); p.setTrainer(trainer); p.setClient(client); p.setStartDate(LocalDate.now()); p.setStatus(WorkoutPlanStatus.ACTIVE); plans.saveAndFlush(p);
        WorkoutPlanDay d = new WorkoutPlanDay(); d.setWorkoutPlan(p); d.setDayOfWeek(1); days.saveAndFlush(d);
        WorkoutPlanExercise pe = new WorkoutPlanExercise(); pe.setWorkoutPlanDay(d); pe.setExercise(ex); pe.setOrderIndex(1); pe.setSets(1); pe.setRepetitions(5); pe.setRestSeconds(30); planExercises.saveAndFlush(pe);
        return new Fixture(client, p);
    }
    private User user(String email) { User u = new User(); u.setFirstName("Concurrency"); u.setLastName("Test"); u.setEmail(email); u.setPassword("password"); u.setRole(email.startsWith("client") ? UserRole.CLIENT : UserRole.TRAINER); u.setActive(true); return u; }
    private record Fixture(User client, WorkoutPlan plan) {}
}
