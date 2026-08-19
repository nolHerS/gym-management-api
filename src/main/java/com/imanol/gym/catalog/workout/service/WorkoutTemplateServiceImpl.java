package com.imanol.gym.catalog.workout.service;

import com.imanol.gym.catalog.workout.entity.WorkoutTemplate;
import com.imanol.gym.catalog.workout.repository.WorkoutTemplateRepository;
import com.imanol.gym.common.service.BaseServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WorkoutTemplateServiceImpl
        extends BaseServiceImpl<WorkoutTemplate, Long>
        implements WorkoutTemplateService {

    private final WorkoutTemplateRepository workoutTemplateRepository;

    public WorkoutTemplateServiceImpl(
            WorkoutTemplateRepository workoutTemplateRepository) {

        super(workoutTemplateRepository);

        this.workoutTemplateRepository = workoutTemplateRepository;
    }

    @Override
    public List<WorkoutTemplate> findAll() {
        return workoutTemplateRepository.findAllByActiveTrue();
    }

    @Override
    public WorkoutTemplate create(WorkoutTemplate entity) {

        entity.setActive(true);

        return super.create(entity);
    }

    @Override
    public WorkoutTemplate update(
            Long id,
            WorkoutTemplate entity) {

        WorkoutTemplate existingTemplate = findById(id);

        existingTemplate.setName(entity.getName());
        existingTemplate.setDescription(entity.getDescription());

        return workoutTemplateRepository.save(existingTemplate);
    }

    @Override
    public void activate(Long id) {

        WorkoutTemplate template = findById(id);

        template.setActive(true);

        workoutTemplateRepository.save(template);
    }

    @Override
    public void deactivate(Long id) {

        WorkoutTemplate template = findById(id);

        template.setActive(false);

        workoutTemplateRepository.save(template);
    }
}