package com.imanol.gym.catalog.exercise.service;

import com.imanol.gym.catalog.exercise.dto.ExerciseRequest;
import com.imanol.gym.catalog.exercise.entity.Exercise;
import com.imanol.gym.catalog.exercise.entity.ExerciseCategory;
import com.imanol.gym.catalog.exercise.repository.ExerciseCategoryRepository;
import com.imanol.gym.catalog.exercise.repository.ExerciseRepository;
import com.imanol.gym.common.exception.ResourceNotFoundException;
import com.imanol.gym.common.service.BaseServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExerciseServiceImpl
        extends BaseServiceImpl<Exercise, Long>
        implements ExerciseService {

    private final ExerciseRepository exerciseRepository;
    private final ExerciseCategoryRepository exerciseCategoryRepository;

    public ExerciseServiceImpl(
            ExerciseRepository exerciseRepository,
            ExerciseCategoryRepository exerciseCategoryRepository) {

        super(exerciseRepository);

        this.exerciseRepository = exerciseRepository;
        this.exerciseCategoryRepository = exerciseCategoryRepository;
    }

    @Override
    public Exercise create(ExerciseRequest request) {

        ExerciseCategory category =
                findCategoryById(request.categoryId());

        Exercise exercise = new Exercise();

        exercise.setName(request.name());
        exercise.setDescription(request.description());
        exercise.setCategory(category);
        exercise.setActive(true);

        return super.create(exercise);
    }

    @Override
    public Exercise update(
            Long id,
            ExerciseRequest request) {

        Exercise existingExercise = findById(id);

        ExerciseCategory category =
                findCategoryById(request.categoryId());

        existingExercise.setName(request.name());
        existingExercise.setDescription(request.description());
        existingExercise.setCategory(category);

        return exerciseRepository.save(existingExercise);
    }

    @Override
    public List<Exercise> findAll() {
        return exerciseRepository.findAllByActiveTrue();
    }

    @Override
    public List<Exercise> findAllByCategory(Long categoryId) {

        findCategoryById(categoryId);

        return exerciseRepository
                .findAllByCategoryIdAndActiveTrue(categoryId);
    }

    @Override
    public void activate(Long id) {

        Exercise exercise = findById(id);

        exercise.setActive(true);

        exerciseRepository.save(exercise);
    }

    @Override
    public void deactivate(Long id) {

        Exercise exercise = findById(id);

        exercise.setActive(false);

        exerciseRepository.save(exercise);
    }

    private ExerciseCategory findCategoryById(Long categoryId) {

        return exerciseCategoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Exercise category not found with id: "
                                        + categoryId
                        )
                );
    }
}