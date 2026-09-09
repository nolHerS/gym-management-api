package com.imanol.gym.catalog.workout.repository;

import com.imanol.gym.catalog.workout.entity.WorkoutPlan;
import com.imanol.gym.catalog.workout.entity.WorkoutPlanStatus;
import com.imanol.gym.user.entity.User;
import com.imanol.gym.user.entity.UserRole;
import com.imanol.gym.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
class WorkoutPlanRepositoryTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

    @Autowired
    private WorkoutPlanRepository workoutPlanRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldFindActivePlansForClientAndWeek() {
        User trainer = user("trainer@test.com", UserRole.TRAINER);
        User client = user("client@test.com", UserRole.CLIENT);
        userRepository.save(trainer);
        userRepository.save(client);

        WorkoutPlan plan = new WorkoutPlan();
        plan.setTrainer(trainer);
        plan.setClient(client);
        plan.setStartDate(LocalDate.of(2026, 9, 7));
        plan.setEndDate(null);
        plan.setStatus(WorkoutPlanStatus.ACTIVE);
        workoutPlanRepository.save(plan);

        List<WorkoutPlan> result = workoutPlanRepository.findActivePlansForWeek(
                client.getId(),
                WorkoutPlanStatus.ACTIVE,
                LocalDate.of(2026, 9, 7),
                LocalDate.of(2026, 9, 13)
        );

        assertThat(result).containsExactly(plan);
    }

    @Test
    void shouldFindPlansByClientAndStatus() {
        User trainer = user("trainer2@test.com", UserRole.TRAINER);
        User client = user("client2@test.com", UserRole.CLIENT);
        userRepository.save(trainer);
        userRepository.save(client);

        WorkoutPlan plan = new WorkoutPlan();
        plan.setTrainer(trainer);
        plan.setClient(client);
        plan.setStartDate(LocalDate.of(2026, 9, 7));
        plan.setStatus(WorkoutPlanStatus.COMPLETED);
        workoutPlanRepository.save(plan);

        assertThat(workoutPlanRepository
                .findAllByClientIdAndStatusOrderByStartDateDesc(
                        client.getId(),
                        WorkoutPlanStatus.COMPLETED
                ))
                .containsExactly(plan);
    }

    private User user(String email, UserRole role) {
        User user = new User();
        user.setFirstName("Test");
        user.setLastName("User");
        user.setEmail(email);
        user.setPassword("password");
        user.setRole(role);
        user.setActive(true);
        return user;
    }
}
