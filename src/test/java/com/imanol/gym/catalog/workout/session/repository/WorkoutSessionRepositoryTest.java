package com.imanol.gym.catalog.workout.session.repository;

import com.imanol.gym.catalog.exercise.entity.*;
import com.imanol.gym.catalog.exercise.repository.*;
import com.imanol.gym.catalog.workout.entity.*;
import com.imanol.gym.catalog.workout.repository.*;
import com.imanol.gym.catalog.workout.session.entity.*;
import com.imanol.gym.user.entity.*;
import com.imanol.gym.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.persistence.EntityManager;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
class WorkoutSessionRepositoryTest {
    @Container @ServiceConnection static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");
    @Autowired WorkoutSessionRepository sessions;
    @Autowired WorkoutPlanRepository plans;
    @Autowired WorkoutPlanDayRepository days;
    @Autowired WorkoutPlanExerciseRepository planExercises;
    @Autowired ExerciseCategoryRepository categories;
    @Autowired ExerciseRepository exercises;
    @Autowired UserRepository users;
    @Autowired EntityManager entityManager;

    @Test void loadsExercisesAndSetsInDeclaredOrder() {
        User trainer = user("repo-trainer@test", UserRole.TRAINER);
        User client = user("repo-client@test", UserRole.CLIENT); users.saveAll(List.of(trainer, client));
        ExerciseCategory category = new ExerciseCategory(); category.setName("Repository category"); category.setActive(true); categories.save(category);
        Exercise first = exercise("First", category), second = exercise("Second", category); exercises.saveAll(List.of(first, second));
        WorkoutPlan plan = new WorkoutPlan(); plan.setTrainer(trainer); plan.setClient(client); plan.setStartDate(LocalDate.now()); plan.setStatus(WorkoutPlanStatus.ACTIVE); plans.save(plan);
        WorkoutPlanDay day = new WorkoutPlanDay(); day.setWorkoutPlan(plan); day.setDayOfWeek(1); days.save(day);
        WorkoutPlanExercise p1 = planExercise(day, second, 2), p2 = planExercise(day, first, 1); planExercises.saveAll(List.of(p1, p2));

        WorkoutSession session = new WorkoutSession(); session.setWorkoutPlan(plan); session.setClient(client);
        session.setStatus(WorkoutSessionStatus.COMPLETED); session.setStartedAt(LocalDateTime.now()); session.setVersion(0L);
        WorkoutSessionExercise e2 = sessionExercise(session, second, p1, 2, 2);
        WorkoutSessionExercise e1 = sessionExercise(session, first, p2, 1, 1);
        session.getExercises().add(e2); session.getExercises().add(e1);
        e2.getSets().add(set(e2, 2)); e2.getSets().add(set(e2, 1)); e1.getSets().add(set(e1, 1));
        WorkoutSession saved = sessions.saveAndFlush(session);
        entityManager.clear();

        WorkoutSession loaded = sessions.findDetailedById(saved.getId()).orElseThrow();
        assertThat(loaded.getExercises()).extracting(WorkoutSessionExercise::getOrderIndex).containsExactly(1, 2);
        assertThat(loaded.getExercises().get(1).getSets()).extracting(WorkoutSessionSet::getSetNumber).containsExactly(1, 2);
    }

    private WorkoutSessionExercise sessionExercise(WorkoutSession s, Exercise e, WorkoutPlanExercise source, int order, int sets) {
        WorkoutSessionExercise x = new WorkoutSessionExercise(); x.setSession(s); x.setExercise(e);
        x.setSourceWorkoutPlanExerciseId(source.getId()); x.setExerciseNameSnapshot(e.getName());
        x.setOrderIndex(order); x.setPlannedSets(sets); x.setPlannedRepetitions(8); x.setPlannedRestSeconds(60); return x;
    }
    private WorkoutSessionSet set(WorkoutSessionExercise e, int number) { WorkoutSessionSet s = new WorkoutSessionSet(); s.setSessionExercise(e); s.setSetNumber(number); s.setPlannedRepetitions(8); s.setCompleted(false); return s; }
    private WorkoutPlanExercise planExercise(WorkoutPlanDay d, Exercise e, int order) { WorkoutPlanExercise p = new WorkoutPlanExercise(); p.setWorkoutPlanDay(d); p.setExercise(e); p.setOrderIndex(order); p.setSets(order); p.setRepetitions(8); p.setRestSeconds(60); return p; }
    private Exercise exercise(String name, ExerciseCategory category) { Exercise e = new Exercise(); e.setName(name); e.setCategory(category); e.setActive(true); return e; }
    private User user(String email, UserRole role) { User u = new User(); u.setFirstName("Repository"); u.setLastName("Test"); u.setEmail(email); u.setPassword("password"); u.setRole(role); u.setActive(true); return u; }
}
