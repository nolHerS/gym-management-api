package com.imanol.gym.catalog.workout.controller;

import com.imanol.gym.catalog.workout.dto.WorkoutTemplateExerciseRequest;
import com.imanol.gym.catalog.workout.dto.WorkoutTemplateExerciseResponse;
import com.imanol.gym.catalog.workout.entity.WorkoutTemplateExercise;
import com.imanol.gym.catalog.workout.mapper.WorkoutTemplateExerciseMapper;
import com.imanol.gym.catalog.workout.service.WorkoutTemplateExerciseService;
import com.imanol.gym.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class WorkoutTemplateExerciseController {

    private final WorkoutTemplateExerciseService workoutTemplateExerciseService;
    private final WorkoutTemplateExerciseMapper workoutTemplateExerciseMapper;

    @PostMapping("/workout-templates/{workoutTemplateId}/exercises")
    public ResponseEntity<ApiResponse<WorkoutTemplateExerciseResponse>> create(
            @PathVariable Long workoutTemplateId,
            @Valid @RequestBody WorkoutTemplateExerciseRequest request
    ) {

        WorkoutTemplateExercise workoutTemplateExercise =
                workoutTemplateExerciseMapper.toEntity(request);

        WorkoutTemplateExercise createdWorkoutTemplateExercise =
                workoutTemplateExerciseService.create(
                        workoutTemplateId,
                        request.exerciseId(),
                        workoutTemplateExercise
                );

        WorkoutTemplateExerciseResponse response =
                workoutTemplateExerciseMapper.toResponse(
                        createdWorkoutTemplateExercise
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                HttpStatus.CREATED.value(),
                                "Workout template exercise created successfully",
                                response
                        )
                );
    }

    @GetMapping("/workout-templates/{workoutTemplateId}/exercises")
    public ResponseEntity<ApiResponse<List<WorkoutTemplateExerciseResponse>>> findAllByWorkoutTemplateId(
            @PathVariable Long workoutTemplateId
    ) {

        List<WorkoutTemplateExerciseResponse> response =
                workoutTemplateExerciseService
                        .findAllByWorkoutTemplateId(workoutTemplateId)
                        .stream()
                        .map(workoutTemplateExerciseMapper::toResponse)
                        .toList();

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        "Workout template exercises retrieved successfully",
                        response
                )
        );
    }

    @PutMapping("/workout-template-exercises/{id}")
    public ResponseEntity<ApiResponse<WorkoutTemplateExerciseResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody WorkoutTemplateExerciseRequest request
    ) {

        WorkoutTemplateExercise workoutTemplateExercise =
                workoutTemplateExerciseMapper.toEntity(request);

        WorkoutTemplateExercise updatedWorkoutTemplateExercise =
                workoutTemplateExerciseService.update(
                        id,
                        request.exerciseId(),
                        workoutTemplateExercise
                );

        WorkoutTemplateExerciseResponse response =
                workoutTemplateExerciseMapper.toResponse(
                        updatedWorkoutTemplateExercise
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        "Workout template exercise updated successfully",
                        response
                )
        );
    }

    @DeleteMapping("/workout-template-exercises/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id
    ) {

        workoutTemplateExerciseService.deleteById(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        "Workout template exercise deleted successfully",
                        null
                )
        );
    }
}