package com.imanol.gym.catalog.exercise.repository;

import com.imanol.gym.catalog.exercise.entity.ExerciseCategory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
class ExerciseCategoryRepositoryTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql =
            new MySQLContainer<>("mysql:8.0");

    @Autowired
    private ExerciseCategoryRepository exerciseCategoryRepository;

    @Test
    void shouldSaveAndFindExerciseCategory() {
        ExerciseCategory category = new ExerciseCategory();
        category.setName("Chest");
        category.setActive(true);

        ExerciseCategory savedCategory =
                exerciseCategoryRepository.save(category);

        assertNotNull(savedCategory.getId());

        ExerciseCategory result =
                exerciseCategoryRepository.findById(savedCategory.getId())
                        .orElseThrow();

        assertEquals("Chest", result.getName());
        assertTrue(result.getActive());
    }

    @Test
    void shouldFindOnlyActiveExerciseCategories() {
        ExerciseCategory activeCategory = new ExerciseCategory();
        activeCategory.setName("Chest");
        activeCategory.setActive(true);

        ExerciseCategory inactiveCategory = new ExerciseCategory();
        inactiveCategory.setName("Back");
        inactiveCategory.setActive(false);

        exerciseCategoryRepository.save(activeCategory);
        exerciseCategoryRepository.save(inactiveCategory);

        List<ExerciseCategory> result =
                exerciseCategoryRepository.findAllByActiveTrue();

        assertEquals(1, result.size());
        assertEquals("Chest", result.getFirst().getName());
        assertTrue(result.getFirst().getActive());
    }

    @Test
    void shouldNotAllowDuplicateCategoryName() {
        ExerciseCategory firstCategory = new ExerciseCategory();
        firstCategory.setName("Chest");
        firstCategory.setActive(true);

        ExerciseCategory duplicateCategory = new ExerciseCategory();
        duplicateCategory.setName("Chest");
        duplicateCategory.setActive(true);

        exerciseCategoryRepository.saveAndFlush(firstCategory);

        assertThrows(
                DataIntegrityViolationException.class,
                () -> exerciseCategoryRepository.saveAndFlush(duplicateCategory)
        );
    }
}