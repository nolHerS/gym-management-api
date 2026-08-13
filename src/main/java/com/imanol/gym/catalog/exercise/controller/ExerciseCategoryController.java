package com.imanol.gym.catalog.exercise.controller;

import com.imanol.gym.catalog.exercise.dto.ExerciseCategoryRequest;
import com.imanol.gym.catalog.exercise.dto.ExerciseCategoryResponse;
import com.imanol.gym.catalog.exercise.entity.ExerciseCategory;
import com.imanol.gym.catalog.exercise.mapper.ExerciseCategoryMapper;
import com.imanol.gym.catalog.exercise.service.ExerciseCategoryService;
import com.imanol.gym.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exercise-categories")
@RequiredArgsConstructor
public class ExerciseCategoryController {

    private final ExerciseCategoryService exerciseCategoryService;
    private final ExerciseCategoryMapper exerciseCategoryMapper;

    @PostMapping
    public ResponseEntity<ApiResponse<ExerciseCategoryResponse>> create(
            @Valid @RequestBody ExerciseCategoryRequest request) {

        ExerciseCategory category =
                exerciseCategoryMapper.toEntity(request);

        ExerciseCategory createdCategory =
                exerciseCategoryService.create(category);

        ExerciseCategoryResponse response =
                exerciseCategoryMapper.toResponse(createdCategory);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        HttpStatus.CREATED.value(),
                        "Exercise category created successfully",
                        response
                ));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ExerciseCategoryResponse>>> findAll() {

        List<ExerciseCategoryResponse> categories =
                exerciseCategoryService.findAll()
                        .stream()
                        .map(exerciseCategoryMapper::toResponse)
                        .toList();

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        "Exercise categories retrieved successfully",
                        categories
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ExerciseCategoryResponse>> findById(
            @PathVariable Long id) {

        ExerciseCategory category =
                exerciseCategoryService.findById(id);

        ExerciseCategoryResponse response =
                exerciseCategoryMapper.toResponse(category);

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        "Exercise category retrieved successfully",
                        response
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ExerciseCategoryResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody ExerciseCategoryRequest request) {

        ExerciseCategory category =
                exerciseCategoryMapper.toEntity(request);

        ExerciseCategory updatedCategory =
                exerciseCategoryService.update(id, category);

        ExerciseCategoryResponse response =
                exerciseCategoryMapper.toResponse(updatedCategory);

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        "Exercise category updated successfully",
                        response
                )
        );
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<Void>> activate(
            @PathVariable Long id) {

        exerciseCategoryService.activate(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        "Exercise category activated successfully",
                        null
                )
        );
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivate(
            @PathVariable Long id) {

        exerciseCategoryService.deactivate(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        "Exercise category deactivated successfully",
                        null
                )
        );
    }
}