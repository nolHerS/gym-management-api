package com.imanol.gym.catalog.workout.service;

import com.imanol.gym.catalog.exercise.entity.Exercise;
import com.imanol.gym.catalog.exercise.service.ExerciseService;
import com.imanol.gym.catalog.workout.entity.WorkoutTemplate;
import com.imanol.gym.catalog.workout.entity.WorkoutTemplateExercise;
import com.imanol.gym.catalog.workout.repository.WorkoutTemplateExerciseRepository;
import com.imanol.gym.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkoutTemplateExerciseServiceImpl
        implements WorkoutTemplateExerciseService {

    private final WorkoutTemplateExerciseRepository
            workoutTemplateExerciseRepository;

    private final WorkoutTemplateService workoutTemplateService;

    private final ExerciseService exerciseService;

    @Override
    public WorkoutTemplateExercise create(
            Long workoutTemplateId,
            Long exerciseId,
            WorkoutTemplateExercise workoutTemplateExercise
    ) {
        requireTrainerIfAuthenticated();
        WorkoutTemplate workoutTemplate =
                workoutTemplateService.findById(workoutTemplateId);

        Exercise exercise =
                exerciseService.findById(exerciseId);

        workoutTemplateExercise.setWorkoutTemplate(workoutTemplate);
        workoutTemplateExercise.setExercise(exercise);

        return workoutTemplateExerciseRepository.save(
                workoutTemplateExercise
        );
    }

    @Override
    public List<WorkoutTemplateExercise> findAllByWorkoutTemplateId(
            Long workoutTemplateId
    ) {
        workoutTemplateService.findById(workoutTemplateId);

        return workoutTemplateExerciseRepository
                .findAllByWorkoutTemplateIdOrderByOrderIndexAsc(
                        workoutTemplateId
                );
    }

    @Override
    public WorkoutTemplateExercise findById(Long id) {

        return workoutTemplateExerciseRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Workout template exercise not found with id: " + id
                        )
                );
    }

    @Override
    public WorkoutTemplateExercise update(
            Long id,
            Long exerciseId,
            WorkoutTemplateExercise workoutTemplateExercise
    ) {
        requireTrainerIfAuthenticated();
        WorkoutTemplateExercise existingWorkoutTemplateExercise =
                findById(id);

        Exercise exercise =
                exerciseService.findById(exerciseId);

        existingWorkoutTemplateExercise.setExercise(exercise);
        existingWorkoutTemplateExercise.setOrderIndex(
                workoutTemplateExercise.getOrderIndex()
        );
        existingWorkoutTemplateExercise.setSets(
                workoutTemplateExercise.getSets()
        );
        existingWorkoutTemplateExercise.setRepetitions(
                workoutTemplateExercise.getRepetitions()
        );
        existingWorkoutTemplateExercise.setRestSeconds(
                workoutTemplateExercise.getRestSeconds()
        );

        return workoutTemplateExerciseRepository.save(
                existingWorkoutTemplateExercise
        );
    }

    @Override
    public void deleteById(Long id) {
        requireTrainerIfAuthenticated();
        WorkoutTemplateExercise workoutTemplateExercise =
                findById(id);

        workoutTemplateExerciseRepository.delete(
                workoutTemplateExercise
        );
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