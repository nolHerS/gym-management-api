package com.imanol.gym.catalog.exercise.controller;

import com.imanol.gym.catalog.exercise.dto.ExerciseCategoryRequest;
import com.imanol.gym.catalog.exercise.dto.ExerciseCategoryResponse;
import com.imanol.gym.catalog.exercise.entity.ExerciseCategory;
import com.imanol.gym.catalog.exercise.mapper.ExerciseCategoryMapper;
import com.imanol.gym.catalog.exercise.service.ExerciseCategoryService;
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
    public ResponseEntity<ExerciseCategoryResponse> create(
            @Valid @RequestBody ExerciseCategoryRequest request) {

        ExerciseCategory category =
                exerciseCategoryMapper.toEntity(request);

        ExerciseCategory createdCategory =
                exerciseCategoryService.create(category);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(exerciseCategoryMapper.toResponse(createdCategory));
    }

    @GetMapping
    public ResponseEntity<List<ExerciseCategoryResponse>> findAll() {

        List<ExerciseCategoryResponse> categories =
                exerciseCategoryService.findAll()
                        .stream()
                        .map(exerciseCategoryMapper::toResponse)
                        .toList();

        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExerciseCategoryResponse> findById(
            @PathVariable Long id) {

        ExerciseCategory category =
                exerciseCategoryService.findById(id);

        return ResponseEntity.ok(
                exerciseCategoryMapper.toResponse(category)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExerciseCategoryResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ExerciseCategoryRequest request) {

        ExerciseCategory category =
                exerciseCategoryMapper.toEntity(request);

        ExerciseCategory updatedCategory =
                exerciseCategoryService.update(id, category);

        return ResponseEntity.ok(
                exerciseCategoryMapper.toResponse(updatedCategory)
        );
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activate(@PathVariable Long id) {

        exerciseCategoryService.activate(id);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {

        exerciseCategoryService.deactivate(id);

        return ResponseEntity.noContent().build();
    }
}