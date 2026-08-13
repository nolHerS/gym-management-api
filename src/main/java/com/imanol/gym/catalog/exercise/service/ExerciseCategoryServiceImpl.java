package com.imanol.gym.catalog.exercise.service;

import com.imanol.gym.catalog.exercise.entity.ExerciseCategory;
import com.imanol.gym.catalog.exercise.repository.ExerciseCategoryRepository;
import com.imanol.gym.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExerciseCategoryServiceImpl implements ExerciseCategoryService {

    private final ExerciseCategoryRepository exerciseCategoryRepository;

    @Override
    public ExerciseCategory create(ExerciseCategory exerciseCategory) {
        exerciseCategory.setActive(true);
        return exerciseCategoryRepository.save(exerciseCategory);
    }

    @Override
    public List<ExerciseCategory> findAll() {
        return exerciseCategoryRepository.findAllByActiveTrue();
    }

    @Override
    public ExerciseCategory findById(Long id) {
        return exerciseCategoryRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Exercise category not found: " + id)
                );
    }

    @Override
    public ExerciseCategory update(Long id, ExerciseCategory exerciseCategory) {
        ExerciseCategory existingCategory = findById(id);

        existingCategory.setName(exerciseCategory.getName());

        return exerciseCategoryRepository.save(existingCategory);
    }

    @Override
    public void activate(Long id) {
        ExerciseCategory category = findById(id);
        category.setActive(true);
        exerciseCategoryRepository.save(category);
    }

    @Override
    public void deactivate(Long id) {
        ExerciseCategory category = findById(id);
        category.setActive(false);
        exerciseCategoryRepository.save(category);
    }
}