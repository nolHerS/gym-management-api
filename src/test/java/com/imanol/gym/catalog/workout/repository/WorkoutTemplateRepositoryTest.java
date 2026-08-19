package com.imanol.gym.catalog.workout.repository;

import com.imanol.gym.catalog.workout.entity.WorkoutTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
class WorkoutTemplateRepositoryTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql =
            new MySQLContainer<>("mysql:8.0");

    @Autowired
    private WorkoutTemplateRepository workoutTemplateRepository;

    @Test
    void shouldSaveWorkoutTemplate() {

        WorkoutTemplate template = new WorkoutTemplate();
        template.setName("Hypertrophy 4 Days");
        template.setDescription("Four day hypertrophy routine");
        template.setActive(true);

        WorkoutTemplate savedTemplate =
                workoutTemplateRepository.save(template);

        assertThat(savedTemplate.getId())
                .isNotNull();

        assertThat(savedTemplate.getName())
                .isEqualTo("Hypertrophy 4 Days");

        assertThat(savedTemplate.getDescription())
                .isEqualTo("Four day hypertrophy routine");

        assertThat(savedTemplate.getActive())
                .isTrue();
    }

    @Test
    void shouldFindOnlyActiveWorkoutTemplates() {

        WorkoutTemplate activeTemplate = new WorkoutTemplate();
        activeTemplate.setName("Active Template");
        activeTemplate.setDescription("Active workout");
        activeTemplate.setActive(true);

        WorkoutTemplate inactiveTemplate = new WorkoutTemplate();
        inactiveTemplate.setName("Inactive Template");
        inactiveTemplate.setDescription("Inactive workout");
        inactiveTemplate.setActive(false);

        workoutTemplateRepository.save(activeTemplate);
        workoutTemplateRepository.save(inactiveTemplate);

        List<WorkoutTemplate> templates =
                workoutTemplateRepository.findAllByActiveTrue();

        assertThat(templates)
                .hasSize(1)
                .containsExactly(activeTemplate);
    }

    @Test
    void shouldFindWorkoutTemplateById() {

        WorkoutTemplate template = new WorkoutTemplate();
        template.setName("Strength Template");
        template.setDescription("Strength routine");
        template.setActive(true);

        WorkoutTemplate savedTemplate =
                workoutTemplateRepository.save(template);

        WorkoutTemplate foundTemplate =
                workoutTemplateRepository.findById(savedTemplate.getId())
                        .orElseThrow();

        assertThat(foundTemplate.getId())
                .isEqualTo(savedTemplate.getId());

        assertThat(foundTemplate.getName())
                .isEqualTo("Strength Template");

        assertThat(foundTemplate.getDescription())
                .isEqualTo("Strength routine");

        assertThat(foundTemplate.getActive())
                .isTrue();
    }
}