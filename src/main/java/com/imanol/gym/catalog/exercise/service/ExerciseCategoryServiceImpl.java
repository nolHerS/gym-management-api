package com.imanol.gym.catalog.exercise.service;

import com.imanol.gym.catalog.exercise.entity.ExerciseCategory;
import com.imanol.gym.catalog.exercise.repository.ExerciseCategoryRepository;
import com.imanol.gym.common.exception.ResourceNotFoundException;
import com.imanol.gym.common.service.BaseServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExerciseCategoryServiceImpl
        extends BaseServiceImpl<ExerciseCategory, Long>
        implements ExerciseCategoryService {

    private final ExerciseCategoryRepository exerciseCategoryRepository;

    public ExerciseCategoryServiceImpl(
            ExerciseCategoryRepository exerciseCategoryRepository) {

        super(exerciseCategoryRepository);

        this.exerciseCategoryRepository = exerciseCategoryRepository;
    }

    @Override
    public List<ExerciseCategory> findAll() {
        return exerciseCategoryRepository.findAllByActiveTrue();
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

    @Override
    public ExerciseCategory create(ExerciseCategory entity) {
        entity.setActive(true);
        return super.create(entity);
    }

    @Override
    public ExerciseCategory update(
            Long id,
            ExerciseCategory entity) {

        ExerciseCategory existing = findById(id);

        existing.setName(entity.getName());

        return exerciseCategoryRepository.save(existing);
    }
}