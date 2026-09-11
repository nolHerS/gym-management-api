package com.imanol.gym.catalog.exercise.service;

import com.imanol.gym.catalog.exercise.entity.ExerciseCategory;
import com.imanol.gym.catalog.exercise.repository.ExerciseCategoryRepository;
import com.imanol.gym.common.service.BaseServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

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
        requireTrainerIfAuthenticated();
        ExerciseCategory category = findById(id);
        category.setActive(true);
        exerciseCategoryRepository.save(category);
    }

    @Override
    public void deactivate(Long id) {
        requireTrainerIfAuthenticated();
        ExerciseCategory category = findById(id);
        category.setActive(false);
        exerciseCategoryRepository.save(category);
    }

    @Override
    public ExerciseCategory create(ExerciseCategory entity) {
        requireTrainerIfAuthenticated();
        entity.setActive(true);
        return super.create(entity);
    }

    @Override
    public ExerciseCategory update(
            Long id,
            ExerciseCategory entity) {

        requireTrainerIfAuthenticated();
        ExerciseCategory existing = findById(id);

        existing.setName(entity.getName());

        return exerciseCategoryRepository.save(existing);
    }

    private void requireTrainerIfAuthenticated() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && authentication.getAuthorities().stream().noneMatch(
                authority -> "ROLE_TRAINER".equals(authority.getAuthority()))) {
            throw new AccessDeniedException("Trainer role is required");
        }
    }
}