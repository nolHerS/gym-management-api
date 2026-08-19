package com.imanol.gym.catalog.workout.service;

import com.imanol.gym.catalog.workout.entity.WorkoutTemplate;
import com.imanol.gym.catalog.workout.repository.WorkoutTemplateRepository;
import com.imanol.gym.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkoutTemplateServiceImplTest {

    @Mock
    private WorkoutTemplateRepository workoutTemplateRepository;

    @InjectMocks
    private WorkoutTemplateServiceImpl workoutTemplateService;

    @Test
    void shouldCreateWorkoutTemplate() {

        WorkoutTemplate template = new WorkoutTemplate();
        template.setName("Hypertrophy 4 Days");
        template.setDescription("Four day hypertrophy routine");

        when(workoutTemplateRepository.save(template))
                .thenAnswer(invocation -> invocation.getArgument(0));

        WorkoutTemplate result =
                workoutTemplateService.create(template);

        assertThat(result.getName())
                .isEqualTo("Hypertrophy 4 Days");

        assertThat(result.getDescription())
                .isEqualTo("Four day hypertrophy routine");

        assertThat(result.getActive())
                .isTrue();

        verify(workoutTemplateRepository).save(template);
    }

    @Test
    void shouldFindWorkoutTemplateById() {

        WorkoutTemplate template = new WorkoutTemplate();
        template.setId(1L);
        template.setName("Hypertrophy 4 Days");
        template.setActive(true);

        when(workoutTemplateRepository.findById(1L))
                .thenReturn(Optional.of(template));

        WorkoutTemplate result =
                workoutTemplateService.findById(1L);

        assertThat(result)
                .isEqualTo(template);

        verify(workoutTemplateRepository).findById(1L);
    }

    @Test
    void shouldFindAllActiveWorkoutTemplates() {

        WorkoutTemplate template = new WorkoutTemplate();
        template.setId(1L);
        template.setName("Hypertrophy 4 Days");
        template.setActive(true);

        when(workoutTemplateRepository.findAllByActiveTrue())
                .thenReturn(List.of(template));

        List<WorkoutTemplate> result =
                workoutTemplateService.findAll();

        assertThat(result)
                .hasSize(1)
                .containsExactly(template);

        verify(workoutTemplateRepository)
                .findAllByActiveTrue();
    }

    @Test
    void shouldUpdateWorkoutTemplate() {

        WorkoutTemplate existingTemplate = new WorkoutTemplate();
        existingTemplate.setId(1L);
        existingTemplate.setName("Old Name");
        existingTemplate.setDescription("Old description");
        existingTemplate.setActive(true);

        WorkoutTemplate updateData = new WorkoutTemplate();
        updateData.setName("New Name");
        updateData.setDescription("New description");

        when(workoutTemplateRepository.findById(1L))
                .thenReturn(Optional.of(existingTemplate));

        when(workoutTemplateRepository.save(existingTemplate))
                .thenReturn(existingTemplate);

        WorkoutTemplate result =
                workoutTemplateService.update(1L, updateData);

        assertThat(result.getName())
                .isEqualTo("New Name");

        assertThat(result.getDescription())
                .isEqualTo("New description");

        assertThat(result.getActive())
                .isTrue();

        verify(workoutTemplateRepository).findById(1L);
        verify(workoutTemplateRepository).save(existingTemplate);
    }

    @Test
    void shouldActivateWorkoutTemplate() {

        WorkoutTemplate template = new WorkoutTemplate();
        template.setId(1L);
        template.setActive(false);

        when(workoutTemplateRepository.findById(1L))
                .thenReturn(Optional.of(template));

        when(workoutTemplateRepository.save(template))
                .thenReturn(template);

        workoutTemplateService.activate(1L);

        assertThat(template.getActive())
                .isTrue();

        verify(workoutTemplateRepository).save(template);
    }

    @Test
    void shouldDeactivateWorkoutTemplate() {

        WorkoutTemplate template = new WorkoutTemplate();
        template.setId(1L);
        template.setActive(true);

        when(workoutTemplateRepository.findById(1L))
                .thenReturn(Optional.of(template));

        when(workoutTemplateRepository.save(template))
                .thenReturn(template);

        workoutTemplateService.deactivate(1L);

        assertThat(template.getActive())
                .isFalse();

        verify(workoutTemplateRepository).save(template);
    }

    @Test
    void shouldThrowExceptionWhenWorkoutTemplateDoesNotExist() {

        when(workoutTemplateRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                workoutTemplateService.findById(99L)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Resource not found with id: 99");

        verify(workoutTemplateRepository).findById(99L);
    }
}