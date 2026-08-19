package com.imanol.gym.catalog.exercise.controller;

import com.imanol.gym.catalog.exercise.dto.ExerciseRequest;
import com.imanol.gym.catalog.exercise.dto.ExerciseResponse;
import com.imanol.gym.catalog.exercise.entity.Exercise;
import com.imanol.gym.catalog.exercise.mapper.ExerciseMapper;
import com.imanol.gym.catalog.exercise.service.ExerciseService;
import com.imanol.gym.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exercises")
@RequiredArgsConstructor
public class ExerciseController {

    private final ExerciseService exerciseService;
    private final ExerciseMapper exerciseMapper;

    @PostMapping
    public ResponseEntity<ApiResponse<ExerciseResponse>> create(
            @Valid @RequestBody ExerciseRequest request) {

        Exercise createdExercise =
                exerciseService.create(request);

        ExerciseResponse response =
                exerciseMapper.toResponse(createdExercise);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        HttpStatus.CREATED.value(),
                        "Exercise created successfully",
                        response
                ));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ExerciseResponse>>> findAll() {

        List<ExerciseResponse> exercises =
                exerciseService.findAll()
                        .stream()
                        .map(exerciseMapper::toResponse)
                        .toList();

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        "Exercises retrieved successfully",
                        exercises
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ExerciseResponse>> findById(
            @PathVariable Long id) {

        Exercise exercise =
                exerciseService.findById(id);

        ExerciseResponse response =
                exerciseMapper.toResponse(exercise);

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        "Exercise retrieved successfully",
                        response
                )
        );
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<List<ExerciseResponse>>> findByCategory(
            @PathVariable Long categoryId) {

        List<ExerciseResponse> exercises =
                exerciseService.findAllByCategory(categoryId)
                        .stream()
                        .map(exerciseMapper::toResponse)
                        .toList();

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        "Exercises retrieved successfully",
                        exercises
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ExerciseResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody ExerciseRequest request) {

        Exercise updatedExercise =
                exerciseService.update(id, request);

        ExerciseResponse response =
                exerciseMapper.toResponse(updatedExercise);

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        "Exercise updated successfully",
                        response
                )
        );
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<Void>> activate(
            @PathVariable Long id) {

        exerciseService.activate(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        "Exercise activated successfully",
                        null
                )
        );
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivate(
            @PathVariable Long id) {

        exerciseService.deactivate(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        "Exercise deactivated successfully",
                        null
                )
        );
    }
}