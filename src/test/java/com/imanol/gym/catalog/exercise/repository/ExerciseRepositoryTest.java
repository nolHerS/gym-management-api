package com.imanol.gym.catalog.exercise.repository;

import com.imanol.gym.catalog.exercise.entity.Exercise;
import com.imanol.gym.catalog.exercise.entity.ExerciseCategory;
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
class ExerciseRepositoryTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql =
            new MySQLContainer<>("mysql:8.0");

    @Autowired
    private ExerciseRepository exerciseRepository;

    @Autowired
    private ExerciseCategoryRepository exerciseCategoryRepository;

    @Test
    void shouldSaveExercise() {

        ExerciseCategory category = new ExerciseCategory();
        category.setName("Chest");
        category.setActive(true);

        ExerciseCategory savedCategory =
                exerciseCategoryRepository.save(category);

        Exercise exercise = new Exercise();
        exercise.setName("Bench Press");
        exercise.setDescription("Barbell bench press");
        exercise.setCategory(savedCategory);
        exercise.setActive(true);

        Exercise savedExercise =
                exerciseRepository.save(exercise);

        assertThat(savedExercise.getId()).isNotNull();
        assertThat(savedExercise.getName()).isEqualTo("Bench Press");
        assertThat(savedExercise.getCategory().getId())
                .isEqualTo(savedCategory.getId());
    }

    @Test
    void shouldFindOnlyActiveExercises() {

        ExerciseCategory category = createCategory();

        Exercise activeExercise = createExercise(
                "Bench Press",
                true,
                category
        );

        createExercise(
                "Old Bench Press",
                false,
                category
        );

        List<Exercise> exercises =
                exerciseRepository.findAllByActiveTrue();

        assertThat(exercises)
                .hasSize(1)
                .containsExactly(activeExercise);
    }

    @Test
    void shouldFindActiveExercisesByCategory() {

        ExerciseCategory chest = createCategory();

        ExerciseCategory legs = new ExerciseCategory();
        legs.setName("Legs");
        legs.setActive(true);
        legs = exerciseCategoryRepository.save(legs);

        Exercise benchPress =
                createExercise("Bench Press", true, chest);

        createExercise(
                "Old Bench Press",
                false,
                chest
        );

        createExercise(
                "Squat",
                true,
                legs
        );

        List<Exercise> exercises =
                exerciseRepository.findAllByCategoryIdAndActiveTrue(
                        chest.getId()
                );

        assertThat(exercises)
                .hasSize(1)
                .containsExactly(benchPress);
    }

    @Test
    void shouldPersistExerciseCategoryRelationship() {

        ExerciseCategory category = createCategory();

        Exercise exercise =
                createExercise(
                        "Bench Press",
                        true,
                        category
                );

        Exercise foundExercise =
                exerciseRepository.findById(exercise.getId())
                        .orElseThrow();

        assertThat(foundExercise.getCategory())
                .isNotNull();

        assertThat(foundExercise.getCategory().getId())
                .isEqualTo(category.getId());

        assertThat(foundExercise.getCategory().getName())
                .isEqualTo("Chest");
    }

    private ExerciseCategory createCategory() {

        ExerciseCategory category = new ExerciseCategory();
        category.setName("Chest");
        category.setActive(true);

        return exerciseCategoryRepository.save(category);
    }

    private Exercise createExercise(
            String name,
            boolean active,
            ExerciseCategory category) {

        Exercise exercise = new Exercise();
        exercise.setName(name);
        exercise.setDescription("Test exercise");
        exercise.setCategory(category);
        exercise.setActive(active);

        return exerciseRepository.save(exercise);
    }
}