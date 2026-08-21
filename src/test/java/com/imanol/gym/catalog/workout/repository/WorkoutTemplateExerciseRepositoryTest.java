package com.imanol.gym.catalog.workout.repository;

import com.imanol.gym.catalog.exercise.entity.Exercise;
import com.imanol.gym.catalog.exercise.entity.ExerciseCategory;
import com.imanol.gym.catalog.exercise.repository.ExerciseCategoryRepository;
import com.imanol.gym.catalog.exercise.repository.ExerciseRepository;
import com.imanol.gym.catalog.workout.entity.WorkoutTemplate;
import com.imanol.gym.catalog.workout.entity.WorkoutTemplateExercise;
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
class WorkoutTemplateExerciseRepositoryTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql =
            new MySQLContainer<>("mysql:8.0");

    @Autowired
    private WorkoutTemplateExerciseRepository workoutTemplateExerciseRepository;

    @Autowired
    private WorkoutTemplateRepository workoutTemplateRepository;

    @Autowired
    private ExerciseRepository exerciseRepository;

    @Autowired
    private ExerciseCategoryRepository exerciseCategoryRepository;

    @Test
    void shouldSaveWorkoutTemplateExercise() {

        WorkoutTemplate template = createAndSaveWorkoutTemplate();

        ExerciseCategory category =
                createAndSaveExerciseCategory();

        Exercise exercise =
                createAndSaveExercise(
                        "Bench Press",
                        category
                );

        WorkoutTemplateExercise templateExercise =
                new WorkoutTemplateExercise();

        templateExercise.setWorkoutTemplate(template);
        templateExercise.setExercise(exercise);
        templateExercise.setOrderIndex(1);
        templateExercise.setSets(4);
        templateExercise.setRepetitions(10);
        templateExercise.setRestSeconds(90);

        WorkoutTemplateExercise savedTemplateExercise =
                workoutTemplateExerciseRepository.save(templateExercise);

        assertThat(savedTemplateExercise.getId())
                .isNotNull();

        assertThat(savedTemplateExercise.getWorkoutTemplate().getId())
                .isEqualTo(template.getId());

        assertThat(savedTemplateExercise.getExercise().getId())
                .isEqualTo(exercise.getId());

        assertThat(savedTemplateExercise.getOrderIndex())
                .isEqualTo(1);

        assertThat(savedTemplateExercise.getSets())
                .isEqualTo(4);

        assertThat(savedTemplateExercise.getRepetitions())
                .isEqualTo(10);

        assertThat(savedTemplateExercise.getRestSeconds())
                .isEqualTo(90);
    }

    @Test
    void shouldFindWorkoutTemplateExercisesOrderedByOrderIndex() {

        WorkoutTemplate template =
                createAndSaveWorkoutTemplate();

        ExerciseCategory category =
                createAndSaveExerciseCategory();

        Exercise firstExercise =
                createAndSaveExercise(
                        "Bench Press",
                        category
                );

        Exercise secondExercise =
                createAndSaveExercise(
                        "Cable Fly",
                        category
                );

        WorkoutTemplateExercise secondTemplateExercise =
                createWorkoutTemplateExercise(
                        template,
                        secondExercise,
                        2
                );

        WorkoutTemplateExercise firstTemplateExercise =
                createWorkoutTemplateExercise(
                        template,
                        firstExercise,
                        1
                );

        workoutTemplateExerciseRepository.save(secondTemplateExercise);
        workoutTemplateExerciseRepository.save(firstTemplateExercise);

        List<WorkoutTemplateExercise> result =
                workoutTemplateExerciseRepository
                        .findAllByWorkoutTemplateIdOrderByOrderIndexAsc(
                                template.getId()
                        );

        assertThat(result)
                .hasSize(2);

        assertThat(result.get(0).getOrderIndex())
                .isEqualTo(1);

        assertThat(result.get(0).getExercise().getName())
                .isEqualTo("Bench Press");

        assertThat(result.get(1).getOrderIndex())
                .isEqualTo(2);

        assertThat(result.get(1).getExercise().getName())
                .isEqualTo("Cable Fly");
    }

    @Test
    void shouldFindOnlyExercisesFromSpecifiedWorkoutTemplate() {

        WorkoutTemplate firstTemplate =
                createAndSaveWorkoutTemplate();

        WorkoutTemplate secondTemplate = new WorkoutTemplate();
        secondTemplate.setName("Strength Template");
        secondTemplate.setDescription("Strength routine");
        secondTemplate.setActive(true);

        secondTemplate =
                workoutTemplateRepository.save(secondTemplate);

        ExerciseCategory category =
                createAndSaveExerciseCategory();

        Exercise firstExercise =
                createAndSaveExercise(
                        "Bench Press",
                        category
                );

        Exercise secondExercise =
                createAndSaveExercise(
                        "Squat",
                        category
                );

        WorkoutTemplateExercise firstTemplateExercise =
                createWorkoutTemplateExercise(
                        firstTemplate,
                        firstExercise,
                        1
                );

        WorkoutTemplateExercise secondTemplateExercise =
                createWorkoutTemplateExercise(
                        secondTemplate,
                        secondExercise,
                        1
                );

        workoutTemplateExerciseRepository.save(firstTemplateExercise);
        workoutTemplateExerciseRepository.save(secondTemplateExercise);

        List<WorkoutTemplateExercise> result =
                workoutTemplateExerciseRepository
                        .findAllByWorkoutTemplateIdOrderByOrderIndexAsc(
                                firstTemplate.getId()
                        );

        assertThat(result)
                .hasSize(1);

        assertThat(result.get(0).getWorkoutTemplate().getId())
                .isEqualTo(firstTemplate.getId());

        assertThat(result.get(0).getExercise().getName())
                .isEqualTo("Bench Press");
    }

    private WorkoutTemplate createAndSaveWorkoutTemplate() {

        WorkoutTemplate template = new WorkoutTemplate();
        template.setName("Hypertrophy Template");
        template.setDescription("Hypertrophy routine");
        template.setActive(true);

        return workoutTemplateRepository.save(template);
    }

    private ExerciseCategory createAndSaveExerciseCategory() {

        ExerciseCategory category = new ExerciseCategory();
        category.setName("Chest");
        category.setActive(true);

        return exerciseCategoryRepository.save(category);
    }

    private Exercise createAndSaveExercise(
            String name,
            ExerciseCategory category) {

        Exercise exercise = new Exercise();
        exercise.setName(name);
        exercise.setActive(true);
        exercise.setCategory(category);

        return exerciseRepository.save(exercise);
    }

    private WorkoutTemplateExercise createWorkoutTemplateExercise(
            WorkoutTemplate template,
            Exercise exercise,
            Integer orderIndex) {

        WorkoutTemplateExercise templateExercise =
                new WorkoutTemplateExercise();

        templateExercise.setWorkoutTemplate(template);
        templateExercise.setExercise(exercise);
        templateExercise.setOrderIndex(orderIndex);
        templateExercise.setSets(4);
        templateExercise.setRepetitions(10);
        templateExercise.setRestSeconds(90);

        return templateExercise;
    }
}