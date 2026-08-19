package com.imanol.gym.catalog.workout.controller;

import com.imanol.gym.catalog.workout.dto.WorkoutTemplateRequest;
import com.imanol.gym.catalog.workout.dto.WorkoutTemplateResponse;
import com.imanol.gym.catalog.workout.entity.WorkoutTemplate;
import com.imanol.gym.catalog.workout.mapper.WorkoutTemplateMapper;
import com.imanol.gym.catalog.workout.service.WorkoutTemplateService;
import com.imanol.gym.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workout-templates")
@RequiredArgsConstructor
public class WorkoutTemplateController {

    private final WorkoutTemplateService workoutTemplateService;
    private final WorkoutTemplateMapper workoutTemplateMapper;

    @PostMapping
    public ResponseEntity<ApiResponse<WorkoutTemplateResponse>> create(
            @Valid @RequestBody WorkoutTemplateRequest request) {

        WorkoutTemplate template =
                workoutTemplateMapper.toEntity(request);

        WorkoutTemplate createdTemplate =
                workoutTemplateService.create(template);

        WorkoutTemplateResponse response =
                workoutTemplateMapper.toResponse(createdTemplate);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        HttpStatus.CREATED.value(),
                        "Workout template created successfully",
                        response
                ));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<WorkoutTemplateResponse>>> findAll() {

        List<WorkoutTemplateResponse> templates =
                workoutTemplateService.findAll()
                        .stream()
                        .map(workoutTemplateMapper::toResponse)
                        .toList();

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        "Workout templates retrieved successfully",
                        templates
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkoutTemplateResponse>> findById(
            @PathVariable Long id) {

        WorkoutTemplate template =
                workoutTemplateService.findById(id);

        WorkoutTemplateResponse response =
                workoutTemplateMapper.toResponse(template);

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        "Workout template retrieved successfully",
                        response
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkoutTemplateResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody WorkoutTemplateRequest request) {

        WorkoutTemplate template =
                workoutTemplateMapper.toEntity(request);

        WorkoutTemplate updatedTemplate =
                workoutTemplateService.update(id, template);

        WorkoutTemplateResponse response =
                workoutTemplateMapper.toResponse(updatedTemplate);

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        "Workout template updated successfully",
                        response
                )
        );
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<Void>> activate(
            @PathVariable Long id) {

        workoutTemplateService.activate(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        "Workout template activated successfully",
                        null
                )
        );
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivate(
            @PathVariable Long id) {

        workoutTemplateService.deactivate(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        "Workout template deactivated successfully",
                        null
                )
        );
    }
}